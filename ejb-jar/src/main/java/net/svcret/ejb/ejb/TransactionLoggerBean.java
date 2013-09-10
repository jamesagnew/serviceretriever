package net.svcret.ejb.ejb;

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

import net.svcret.admin.shared.enm.ResponseTypeEnum;
import net.svcret.admin.shared.model.AuthorizationOutcomeEnum;
import net.svcret.ejb.api.HttpRequestBean;
import net.svcret.ejb.api.HttpResponseBean;
import net.svcret.ejb.api.IDao;
import net.svcret.ejb.api.IFilesystemAuditLogger;
import net.svcret.ejb.api.ITransactionLogger;
import net.svcret.ejb.api.InvocationResponseResultsBean;
import net.svcret.ejb.ejb.log.BaseUnflushed;
import net.svcret.ejb.ejb.log.UnflushedServiceVersionRecentMessages;
import net.svcret.ejb.ejb.log.UnflushedUserRecentMessages;
import net.svcret.ejb.ex.ProcessingException;
import net.svcret.ejb.model.entity.BasePersSavedTransactionRecentMessage;
import net.svcret.ejb.model.entity.BasePersServiceVersion;
import net.svcret.ejb.model.entity.PersServiceVersionMethod;
import net.svcret.ejb.model.entity.PersServiceVersionUrl;
import net.svcret.ejb.model.entity.PersUser;
import net.svcret.ejb.util.Validate;

import com.google.common.annotations.VisibleForTesting;

@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class TransactionLoggerBean implements ITransactionLogger {

	static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(TransactionLoggerBean.class);

	@EJB
	private IDao myDao;
	@EJB
	private IFilesystemAuditLogger myFilesystemAuditLogger;
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
	 * @throws ProcessingException 
	 */
	@Override
	public void logTransaction(HttpRequestBean theRequest,BasePersServiceVersion theSvcVer, PersServiceVersionMethod theMethod, PersUser theUser, String theRequestBody, InvocationResponseResultsBean theInvocationResponse,
			PersServiceVersionUrl theImplementationUrl, HttpResponseBean theHttpResponse, AuthorizationOutcomeEnum theAuthorizationOutcome, String theResponseBody) throws ProcessingException {
		Validate.notNull(theSvcVer);
		Validate.notNull(theInvocationResponse);
		if (theInvocationResponse.getResponseType() != ResponseTypeEnum.FAIL) {
			Validate.notNull(theMethod);
		}

		// Log to database
		{
			UnflushedServiceVersionRecentMessages newValue = new UnflushedServiceVersionRecentMessages(theSvcVer);
			UnflushedServiceVersionRecentMessages existing = myUnflushedMessages.putIfAbsent(theSvcVer, newValue);
			if (existing == null) {
				existing = newValue;
			}
			existing.recordTransaction(theRequest.getRequestTime(), theMethod, theUser, theRequestBody, theInvocationResponse, theRequest, theImplementationUrl, theHttpResponse,
					theAuthorizationOutcome, theResponseBody);
		}

		if (theUser != null) {
			UnflushedUserRecentMessages newValue = new UnflushedUserRecentMessages(theUser);
			UnflushedUserRecentMessages existing = myUnflushedUserMessages.putIfAbsent(theUser, newValue);
			if (existing == null) {
				existing = newValue;
			}

			existing.recordTransaction(theRequest.getRequestTime(), theRequest, theMethod, theUser, theRequestBody, theInvocationResponse, theImplementationUrl, theHttpResponse,
					theAuthorizationOutcome, theResponseBody);

			// Audit log
			if (theUser.determineInheritedAuditLogEnable()) {
				myFilesystemAuditLogger.recordUserTransaction(theRequest, theMethod, theUser, theRequestBody, theInvocationResponse, theImplementationUrl, theHttpResponse, theAuthorizationOutcome);
			}

		}

		// Audit Log
		BasePersServiceVersion svcVer = theMethod.getServiceVersion();
		if (svcVer.determineInheritedAuditLogEnable() == true) {
			myFilesystemAuditLogger.recordServiceTransaction(theRequest, theMethod, theUser, theRequestBody, theInvocationResponse, theImplementationUrl, theHttpResponse, theAuthorizationOutcome);
		}

	}

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

	public static void trimOldest(LinkedList<?> theList, int theSize) {
		while (theList.size() > theSize) {
			theList.pop();
		}
	}


}
