package net.svcret.ejb.ejb;

import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import javax.ejb.EJB;
import javax.ejb.Singleton;

import net.svcret.ejb.api.HttpResponseBean;
import net.svcret.ejb.api.IDao;
import net.svcret.ejb.api.ITransactionLogger;
import net.svcret.ejb.api.InvocationResponseResultsBean;
import net.svcret.ejb.model.entity.BasePersServiceVersion;
import net.svcret.ejb.model.entity.PersServiceVersionRecentMessage;
import net.svcret.ejb.model.entity.PersServiceVersionUrl;
import net.svcret.ejb.model.entity.PersUser;
import net.svcret.ejb.model.entity.PersUserRecentMessage;
import net.svcret.ejb.util.Validate;

@Singleton
public class TransactionLoggerBean implements ITransactionLogger {

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(TransactionLoggerBean.class);

	@EJB
	private IDao myDao;
	private ReentrantLock myFlushLock = new ReentrantLock();
	private ConcurrentHashMap<BasePersServiceVersion, UnflushedServiceVersionRecentMessages> myUnflushedMessages = new ConcurrentHashMap<BasePersServiceVersion, UnflushedServiceVersionRecentMessages>();
	private ConcurrentHashMap<PersUser, UnflushedUserRecentMessages> myUnflushedUserMessages = new ConcurrentHashMap<PersUser, UnflushedUserRecentMessages>();

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
	 * @param theImplementationUrl 
	 * @param theHttpResponse 
	 */
	@Override
	public void logTransaction(Date theTransactionDate, String theRequestHostIp, BasePersServiceVersion theServiceVersion, PersUser theUser, String theRequestBody, InvocationResponseResultsBean theInvocationResponse, PersServiceVersionUrl theImplementationUrl, HttpResponseBean theHttpResponse) {
		Validate.notNull(theServiceVersion);

		{
			UnflushedServiceVersionRecentMessages newValue = new UnflushedServiceVersionRecentMessages();
			UnflushedServiceVersionRecentMessages existing = myUnflushedMessages.putIfAbsent(theServiceVersion, newValue);
			if (existing == null) {
				newValue.init();
				existing = newValue;
			}
			existing.recordTransaction(theTransactionDate, theServiceVersion, theUser, theRequestBody, theInvocationResponse, theRequestHostIp, theImplementationUrl, theHttpResponse);
		}

		if (theUser != null) {
			UnflushedUserRecentMessages newValue = new UnflushedUserRecentMessages();
			UnflushedUserRecentMessages existing = myUnflushedUserMessages.putIfAbsent(theUser, newValue);
			if (existing == null) {
				newValue.init();
				existing = newValue;
			}

			existing.recordTransaction(theTransactionDate, theRequestHostIp, theServiceVersion, theUser, theRequestBody, theInvocationResponse, theImplementationUrl, theHttpResponse);
		}
	}

	private void doFlush() {
		if (myUnflushedMessages.isEmpty()) {
			return;
		}

		ourLog.info("Going to flush recent transactions to database");

		for (BasePersServiceVersion next : new HashSet<BasePersServiceVersion>(myUnflushedMessages.keySet())) {
			UnflushedServiceVersionRecentMessages nextTransactions = myUnflushedMessages.remove(next);
			if (nextTransactions != null) {

			}
		}
	}

	private static class UnflushedServiceVersionRecentMessages {
		private LinkedList<PersServiceVersionRecentMessage> myFail;
		private LinkedList<PersServiceVersionRecentMessage> myFault;
		private LinkedList<PersServiceVersionRecentMessage> mySecurityFail;
		private LinkedList<PersServiceVersionRecentMessage> mySuccess;

		public void init() {
			mySuccess = new LinkedList<PersServiceVersionRecentMessage>();
			myFault = new LinkedList<PersServiceVersionRecentMessage>();
			myFail = new LinkedList<PersServiceVersionRecentMessage>();
			mySecurityFail = new LinkedList<PersServiceVersionRecentMessage>();
		}

		public void recordTransaction(Date theTransactionTime, BasePersServiceVersion theServiceVersion, PersUser theUser, String theRequestBody, InvocationResponseResultsBean theInvocationResponse, String theRequestHostIp, PersServiceVersionUrl theImplementationUrl, HttpResponseBean theHttpResponse) {
			Validate.notNull(theInvocationResponse);
			Validate.notNull(theServiceVersion);
			Validate.notNull(theTransactionTime);

			Integer keepRecent = theServiceVersion.determineKeepNumRecentTransactions(theInvocationResponse.getResponseType());
			if (keepRecent != null && keepRecent > 0) {

				PersServiceVersionRecentMessage message = new PersServiceVersionRecentMessage();
				message.populate(theTransactionTime, theRequestHostIp, theImplementationUrl, theRequestBody, theInvocationResponse);
				message.setServiceVersion(theServiceVersion);
				message.setUser(theUser);
				message.setTransactionTime(theTransactionTime);
				message.setTransactionMillis(theHttpResponse.getResponseTime());

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


	}

	private static void trimOldest(LinkedList<?> theList, int theSize) {
		while (theList.size() > theSize) {
			theList.pop();
		}
	}
	private static class UnflushedUserRecentMessages {
		private LinkedList<PersUserRecentMessage> myUserFail;
		private LinkedList<PersUserRecentMessage> myUserFault;
		private LinkedList<PersUserRecentMessage> myUserSecurityFail;
		private LinkedList<PersUserRecentMessage> myUserSuccess;

		public void init() {
			myUserSuccess = new LinkedList<PersUserRecentMessage>();
			myUserFault = new LinkedList<PersUserRecentMessage>();
			myUserFail = new LinkedList<PersUserRecentMessage>();
			myUserSecurityFail = new LinkedList<PersUserRecentMessage>();
		}

		public void recordTransaction(Date theTransactionTime, String theRequestHostIp, BasePersServiceVersion theServiceVersion, PersUser theUser, String theRequestBody, InvocationResponseResultsBean theInvocationResponse, PersServiceVersionUrl theImplementationUrl, HttpResponseBean theHttpResponse) {
			Validate.notNull(theInvocationResponse);
			
			Integer keepNum = theUser.getAuthenticationHost().determineKeepNumRecentTransactions(theInvocationResponse.getResponseType());
			if (keepNum != null && keepNum > 0) {

				PersUserRecentMessage userMessage = new PersUserRecentMessage();
				userMessage.populate(theTransactionTime, theRequestHostIp, theImplementationUrl, theRequestBody, theInvocationResponse);
				userMessage.setUser(theUser);
				userMessage.setServiceVersion(theServiceVersion);
				userMessage.setTransactionTime(theTransactionTime);
				userMessage.setTransactionMillis(theHttpResponse.getResponseTime());

				switch (theInvocationResponse.getResponseType()) {
				case FAIL:
					myUserFail.add(userMessage);
					trimOldest(myUserFail, keepNum);
					break;
				case FAULT:
					myUserFault.add(userMessage);
					trimOldest(myUserFault, keepNum);
					break;
				case SECURITY_FAIL:
					myUserSecurityFail.add(userMessage);
					trimOldest(myUserSecurityFail, keepNum);
					break;
				case SUCCESS:
					myUserSuccess.add(userMessage);
					trimOldest(myUserSuccess, keepNum);
					break;
				}

			}
		}

	}

}
