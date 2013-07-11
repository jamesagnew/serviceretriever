package net.svcret.ejb.ejb;

import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import javax.ejb.EJB;
import javax.ejb.Singleton;

import net.svcret.admin.shared.model.AuthorizationOutcomeEnum;
import net.svcret.ejb.api.HttpRequestBean;
import net.svcret.ejb.api.HttpResponseBean;
import net.svcret.ejb.api.IDao;
import net.svcret.ejb.api.ITransactionLogger;
import net.svcret.ejb.api.InvocationResponseResultsBean;
import net.svcret.ejb.model.entity.BasePersRecentMessage;
import net.svcret.ejb.model.entity.BasePersServiceVersion;
import net.svcret.ejb.model.entity.PersServiceVersionRecentMessage;
import net.svcret.ejb.model.entity.PersServiceVersionUrl;
import net.svcret.ejb.model.entity.PersUser;
import net.svcret.ejb.model.entity.PersUserRecentMessage;
import net.svcret.ejb.util.Validate;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.google.common.annotations.VisibleForTesting;

@Singleton
public class TransactionLoggerBean implements ITransactionLogger {

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(TransactionLoggerBean.class);

	@EJB
	private IDao myDao;

	private ReentrantLock myFlushLock = new ReentrantLock();

	private ConcurrentHashMap<BasePersServiceVersion, UnflushedServiceVersionRecentMessages> myUnflushedMessages = new ConcurrentHashMap<BasePersServiceVersion, UnflushedServiceVersionRecentMessages>();
	private ConcurrentHashMap<PersUser, UnflushedUserRecentMessages> myUnflushedUserMessages = new ConcurrentHashMap<PersUser, UnflushedUserRecentMessages>();
	private void doFlush() {
		ourLog.debug("Flushing recent transactions");
		
		doFlush(myUnflushedMessages);
		doFlush(myUnflushedUserMessages);
	}

	private void doFlush(ConcurrentHashMap<?, ? extends BaseUnflushed<? extends BasePersRecentMessage>> unflushedMessages) {
		if (unflushedMessages.isEmpty()) {
			return;
		}

		ourLog.debug("Going to flush recent transactions to database: {}", unflushedMessages.values());
		long start = System.currentTimeMillis();

		int saveCount = 0;
		for (Object next : new HashSet<Object>(unflushedMessages.keySet())) {
			BaseUnflushed<? extends BasePersRecentMessage> nextTransactions = unflushedMessages.remove(next);
			if (nextTransactions != null) {
				myDao.saveRecentMessagesAndTrimInNewTransaction(nextTransactions);
				saveCount += nextTransactions.getCount();
			}
		}
		
		long delay = System.currentTimeMillis() - start;
		ourLog.info("Done saving {} recent transactions to database in {}ms", saveCount, delay);
	}

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
	 */
	@Override
	public void logTransaction(Date theTransactionDate, HttpRequestBean theRequest, BasePersServiceVersion theServiceVersion, PersUser theUser, String theRequestBody, InvocationResponseResultsBean theInvocationResponse, PersServiceVersionUrl theImplementationUrl,
			HttpResponseBean theHttpResponse, AuthorizationOutcomeEnum theAuthorizationOutcome) {
		Validate.notNull(theServiceVersion);

		{
			UnflushedServiceVersionRecentMessages newValue = new UnflushedServiceVersionRecentMessages(theServiceVersion);
			UnflushedServiceVersionRecentMessages existing = myUnflushedMessages.putIfAbsent(theServiceVersion, newValue);
			if (existing == null) {
				newValue.init();
				existing = newValue;
			}
			existing.recordTransaction(theTransactionDate, theServiceVersion, theUser, theRequestBody, theInvocationResponse, theRequest, theImplementationUrl, theHttpResponse, theAuthorizationOutcome);
		}

		if (theUser != null) {
			UnflushedUserRecentMessages newValue = new UnflushedUserRecentMessages(theUser);
			UnflushedUserRecentMessages existing = myUnflushedUserMessages.putIfAbsent(theUser, newValue);
			if (existing == null) {
				newValue.init();
				existing = newValue;
			}

			existing.recordTransaction(theTransactionDate, theRequest, theServiceVersion, theUser, theRequestBody, theInvocationResponse, theImplementationUrl, theHttpResponse, theAuthorizationOutcome);
		}
	}

	@VisibleForTesting
	public void setDao(IDao theDao) {
		myDao = theDao;
	}

	private static void trimOldest(LinkedList<?> theList, int theSize) {
		while (theList.size() > theSize) {
			theList.pop();
		}
	}

	public static abstract class BaseUnflushed<T extends BasePersRecentMessage> {
		protected LinkedList<T> myFail;
		protected LinkedList<T> myFault;
		protected LinkedList<T> mySecurityFail;
		protected LinkedList<T> mySuccess;

		public int getCount() {
			return myFail.size() + mySecurityFail.size()+mySuccess.size()+myFault.size();
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
		private BasePersServiceVersion mySrvVer;

		public UnflushedServiceVersionRecentMessages(BasePersServiceVersion theSrvVer) {
			mySrvVer = theSrvVer;
		}

		public void recordTransaction(Date theTransactionTime, BasePersServiceVersion theServiceVersion, PersUser theUser, String theRequestBody, InvocationResponseResultsBean theInvocationResponse, HttpRequestBean theRequest, PersServiceVersionUrl theImplementationUrl,
				HttpResponseBean theHttpResponse, AuthorizationOutcomeEnum theAuthorizationOutcome) {
			Validate.notNull(theInvocationResponse);
			Validate.notNull(theServiceVersion);
			Validate.notNull(theTransactionTime);

			Integer keepRecent = theServiceVersion.determineKeepNumRecentTransactions(theInvocationResponse.getResponseType());
			
			ourLog.debug("Keeping {} recent SvcVer transactions for response type {}", keepRecent, theInvocationResponse.getResponseType());
			
			if (keepRecent != null && keepRecent > 0) {

				PersServiceVersionRecentMessage message = new PersServiceVersionRecentMessage();
				message.populate(theTransactionTime, theRequest, theImplementationUrl, theRequestBody, theInvocationResponse);
				message.setServiceVersion(theServiceVersion);
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
				}

			}

		}

		@Override
		public String toString() {
			ToStringBuilder b = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
			b.append("svcVer", mySrvVer.getPid());
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

		public void recordTransaction(Date theTransactionTime, HttpRequestBean theRequest, BasePersServiceVersion theServiceVersion, PersUser theUser, String theRequestBody, InvocationResponseResultsBean theInvocationResponse, PersServiceVersionUrl theImplementationUrl,
				HttpResponseBean theHttpResponse, AuthorizationOutcomeEnum theAuthorizationOutcome) {
			Validate.notNull(theInvocationResponse);

			Integer keepNum = theUser.getAuthenticationHost().determineKeepNumRecentTransactions(theInvocationResponse.getResponseType());
			
			ourLog.debug("Keeping {} recent User transactions for response type {}", keepNum, theInvocationResponse.getResponseType());

			if (keepNum != null && keepNum > 0) {

				PersUserRecentMessage userMessage = new PersUserRecentMessage();
				userMessage.populate(theTransactionTime, theRequest, theImplementationUrl, theRequestBody, theInvocationResponse);
				userMessage.setUser(theUser);
				userMessage.setServiceVersion(theServiceVersion);
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
