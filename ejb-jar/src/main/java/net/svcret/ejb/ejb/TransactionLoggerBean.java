package net.svcret.ejb.ejb;

import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import net.svcret.admin.shared.model.AuthorizationOutcomeEnum;
import net.svcret.ejb.api.HttpRequestBean;
import net.svcret.ejb.api.HttpResponseBean;
import net.svcret.ejb.api.IDao;
import net.svcret.ejb.api.IFilesystemAuditLogger;
import net.svcret.ejb.api.ITransactionLogger;
import net.svcret.ejb.api.InvocationResponseResultsBean;
import net.svcret.ejb.model.entity.BasePersSavedTransaction;
import net.svcret.ejb.model.entity.BasePersSavedTransactionRecentMessage;
import net.svcret.ejb.model.entity.BasePersServiceVersion;
import net.svcret.ejb.model.entity.PersServiceVersionMethod;
import net.svcret.ejb.model.entity.PersServiceVersionRecentMessage;
import net.svcret.ejb.model.entity.PersServiceVersionUrl;
import net.svcret.ejb.model.entity.PersUser;
import net.svcret.ejb.model.entity.PersUserRecentMessage;
import net.svcret.ejb.util.Validate;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.google.common.annotations.VisibleForTesting;

@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class TransactionLoggerBean implements ITransactionLogger {

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(TransactionLoggerBean.class);

	@EJB
	private IDao myDao;
	private final ReentrantLock myFlushLock = new ReentrantLock();
	private final ConcurrentHashMap<BasePersServiceVersion, UnflushedServiceVersionRecentMessages> myUnflushedMessages = new ConcurrentHashMap<BasePersServiceVersion, UnflushedServiceVersionRecentMessages>();
	private final ConcurrentHashMap<PersUser, UnflushedUserRecentMessages> myUnflushedUserMessages = new ConcurrentHashMap<PersUser, UnflushedUserRecentMessages>();

	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	@Override
	public void flush() {

		/*
		 * Make sure the flush only happens once at a time
		 */
		if (!myFlushLock.tryLock()) {
			return;
		}
		try {
			doFlush();
		} finally {
			myFlushLock.unlock();
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @param theImplementationUrl
	 * @param theHttpResponse
	 * @param theResponseBody 
	 */
	@Override
	public void logTransaction(HttpRequestBean theRequest, PersServiceVersionMethod theMethod, PersUser theUser, String theRequestBody, InvocationResponseResultsBean theInvocationResponse,
			PersServiceVersionUrl theImplementationUrl, HttpResponseBean theHttpResponse, AuthorizationOutcomeEnum theAuthorizationOutcome, String theResponseBody) {
		Validate.notNull(theMethod);

		// Log to database
		{
			UnflushedServiceVersionRecentMessages newValue = new UnflushedServiceVersionRecentMessages(theMethod);
			UnflushedServiceVersionRecentMessages existing = myUnflushedMessages.putIfAbsent(theMethod.getServiceVersion(), newValue);
			if (existing == null) {
				newValue.init();
				existing = newValue;
			}
			existing.recordTransaction(theRequest.getRequestTime(), theMethod, theUser, theRequestBody, theInvocationResponse, theRequest, theImplementationUrl, theHttpResponse,
					theAuthorizationOutcome, theResponseBody);
		}

		if (theUser != null) {
			UnflushedUserRecentMessages newValue = new UnflushedUserRecentMessages(theUser);
			UnflushedUserRecentMessages existing = myUnflushedUserMessages.putIfAbsent(theUser, newValue);
			if (existing == null) {
				newValue.init();
				existing = newValue;
			}

			existing.recordTransaction(theRequest.getRequestTime(), theRequest, theMethod, theUser, theRequestBody, theInvocationResponse, theImplementationUrl, theHttpResponse,
					theAuthorizationOutcome,theResponseBody);
		}

		// Audit Log
		{
			BasePersServiceVersion svcVer = theMethod.getServiceVersion();
			if (svcVer.determineInheritedAuditLogEnable() == true) {
				myFilesystemAuditLogger.recordServiceTransaction(theRequest, theMethod, theUser, theRequestBody, theInvocationResponse, theImplementationUrl, theHttpResponse, theAuthorizationOutcome);
			}
		}

	}

	@EJB
	private IFilesystemAuditLogger myFilesystemAuditLogger;

	@VisibleForTesting
	public void setDao(IDao theDao) {
		myDao = theDao;
	}

	private void doFlush() {
		ourLog.debug("Flushing recent transactions");

		doFlush(myUnflushedMessages);
		doFlush(myUnflushedUserMessages);
	}

	private void doFlush(ConcurrentHashMap<?, ? extends BaseUnflushed<? extends BasePersSavedTransactionRecentMessage>> unflushedMessages) {
		if (unflushedMessages.isEmpty()) {
			return;
		}

		ourLog.debug("Going to flush recent transactions to database: {}", unflushedMessages.values());
		long start = System.currentTimeMillis();

		int saveCount = 0;
		for (Object next : new HashSet<Object>(unflushedMessages.keySet())) {
			BaseUnflushed<? extends BasePersSavedTransactionRecentMessage> nextTransactions = unflushedMessages.remove(next);
			if (nextTransactions != null) {
				myDao.saveRecentMessagesAndTrimInNewTransaction(nextTransactions);
				saveCount += nextTransactions.getCount();
			}
		}

		long delay = System.currentTimeMillis() - start;
		ourLog.info("Done saving {} recent transactions to database in {}ms", saveCount, delay);
	}

	private static void trimOldest(LinkedList<?> theList, int theSize) {
		while (theList.size() > theSize) {
			theList.pop();
		}
	}

	public static abstract class BaseUnflushed<T extends BasePersSavedTransaction> {
		protected LinkedList<T> myFail;
		protected LinkedList<T> myFault;
		protected LinkedList<T> mySecurityFail;
		protected LinkedList<T> mySuccess;

		public int getCount() {
			return myFail.size() + mySecurityFail.size() + mySuccess.size() + myFault.size();
		}

		/**
		 * @return the fail
		 */
		public LinkedList<T> getFail() {
			return myFail;
		}

		/**
		 * @return the fault
		 */
		public LinkedList<T> getFault() {
			return myFault;
		}

		/**
		 * @return the securityFail
		 */
		public LinkedList<T> getSecurityFail() {
			return mySecurityFail;
		}

		/**
		 * @return the success
		 */
		public LinkedList<T> getSuccess() {
			return mySuccess;
		}

		public void init() {
			mySuccess = new LinkedList<T>();
			myFault = new LinkedList<T>();
			myFail = new LinkedList<T>();
			mySecurityFail = new LinkedList<T>();
		}
	}

	private static class UnflushedServiceVersionRecentMessages extends BaseUnflushed<PersServiceVersionRecentMessage> {
		private PersServiceVersionMethod myMethod;

		public UnflushedServiceVersionRecentMessages(PersServiceVersionMethod theMethod) {
			myMethod = theMethod;
		}

		public void recordTransaction(Date theTransactionTime, PersServiceVersionMethod theMethod, PersUser theUser, String theRequestBody, InvocationResponseResultsBean theInvocationResponse,
				HttpRequestBean theRequest, PersServiceVersionUrl theImplementationUrl, HttpResponseBean theHttpResponse, AuthorizationOutcomeEnum theAuthorizationOutcome, String theResponseBody) {
			Validate.notNull(theInvocationResponse);
			Validate.notNull(theMethod);
			Validate.notNull(theTransactionTime);

			BasePersServiceVersion svcVer = theMethod.getServiceVersion();
			Integer keepRecent = svcVer.determineKeepNumRecentTransactions(theInvocationResponse.getResponseType());

			ourLog.debug("Keeping {} recent SvcVer transactions for response type {}", keepRecent, theInvocationResponse.getResponseType());

			if (keepRecent != null && keepRecent > 0) {

				PersServiceVersionRecentMessage message = new PersServiceVersionRecentMessage();
				message.populate(theTransactionTime, theRequest, theImplementationUrl, theRequestBody, theInvocationResponse, theResponseBody);
				message.setServiceVersion(svcVer);
				message.setMethod(theMethod);
				message.setUser(theUser);
				message.setTransactionTime(theTransactionTime);
				message.setAuthorizationOutcome(theAuthorizationOutcome);

				long responseTime = theHttpResponse != null ? theHttpResponse.getResponseTime() : 0;
				message.setTransactionMillis(responseTime);

				switch (theInvocationResponse.getResponseType()) {
				case FAIL:
					myFail.add(message);
					trimOldest(myFail, keepRecent);
					break;
				case FAULT:
					myFault.add(message);
					trimOldest(myFault, keepRecent);
					break;
				case SECURITY_FAIL:
					mySecurityFail.add(message);
					trimOldest(mySecurityFail, keepRecent);
					break;
				case SUCCESS:
					mySuccess.add(message);
					trimOldest(mySuccess, keepRecent);
					break;
				case THROTTLE_REJ:
					throw new UnsupportedOperationException();
				}

			}

		}

		@Override
		public String toString() {
			ToStringBuilder b = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
			b.append("svcVer", myMethod.getPid());
			b.append("fail", myFail.size());
			b.append("secfail", mySecurityFail.size());
			b.append("fault", myFault.size());
			b.append("success", mySuccess.size());
			return b.build();
		}

	}

	private static class UnflushedUserRecentMessages extends BaseUnflushed<PersUserRecentMessage> {
		private PersUser myUser;

		public UnflushedUserRecentMessages(PersUser theUser) {
			myUser = theUser;
		}

		public void recordTransaction(Date theTransactionTime, HttpRequestBean theRequest, PersServiceVersionMethod theMethod, PersUser theUser, String theRequestBody,
				InvocationResponseResultsBean theInvocationResponse, PersServiceVersionUrl theImplementationUrl, HttpResponseBean theHttpResponse, AuthorizationOutcomeEnum theAuthorizationOutcome, String theResponseBody) {
			Validate.notNull(theInvocationResponse);

			Integer keepNum = theUser.determineInheritedKeepNumRecentTransactions(theInvocationResponse.getResponseType());

			ourLog.debug("Keeping {} recent User transactions for response type {}", keepNum, theInvocationResponse.getResponseType());

			if (keepNum != null && keepNum > 0) {

				PersUserRecentMessage userMessage = new PersUserRecentMessage();
				userMessage.populate(theTransactionTime, theRequest, theImplementationUrl, theRequestBody, theInvocationResponse,theResponseBody);
				userMessage.setUser(theUser);
				userMessage.setServiceVersion(theMethod.getServiceVersion());
				userMessage.setMethod(theMethod);
				userMessage.setTransactionTime(theTransactionTime);
				userMessage.setAuthorizationOutcome(theAuthorizationOutcome);
				long responseTime = theHttpResponse != null ? theHttpResponse.getResponseTime() : 0;
				userMessage.setTransactionMillis(responseTime);

				switch (theInvocationResponse.getResponseType()) {
				case FAIL:
					myFail.add(userMessage);
					trimOldest(myFail, keepNum);
					break;
				case FAULT:
					myFault.add(userMessage);
					trimOldest(myFault, keepNum);
					break;
				case SECURITY_FAIL:
					mySecurityFail.add(userMessage);
					trimOldest(mySecurityFail, keepNum);
					break;
				case SUCCESS:
					mySuccess.add(userMessage);
					trimOldest(mySuccess, keepNum);
					break;
				case THROTTLE_REJ:
					throw new UnsupportedOperationException();
				}

			}
		}

		@Override
		public String toString() {
			ToStringBuilder b = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
			b.append("user", myUser.getPid());
			b.append("fail", myFail.size());
			b.append("secfail", mySecurityFail.size());
			b.append("fault", myFault.size());
			b.append("success", mySuccess.size());
			return b.build();
		}

	}

}
