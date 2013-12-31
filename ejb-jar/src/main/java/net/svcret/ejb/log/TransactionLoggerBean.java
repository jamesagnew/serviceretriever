package net.svcret.ejb.log;

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

import net.svcret.admin.shared.enm.AuthorizationOutcomeEnum;
import net.svcret.admin.shared.enm.ResponseTypeEnum;
import net.svcret.ejb.api.SrBeanIncomingRequest;
import net.svcret.ejb.api.SrBeanIncomingResponse;
import net.svcret.ejb.api.IConfigService;
import net.svcret.ejb.api.IDao;
import net.svcret.ejb.api.IDao.ByteDelta;
import net.svcret.ejb.api.SrBeanProcessedResponse;
import net.svcret.ejb.api.SrBeanProcessedRequest;
import net.svcret.ejb.ex.ProcessingException;
import net.svcret.ejb.ex.UnexpectedFailureException;
import net.svcret.ejb.model.entity.BasePersSavedTransactionRecentMessage;
import net.svcret.ejb.model.entity.BasePersServiceVersion;
import net.svcret.ejb.model.entity.PersServiceVersionMethod;
import net.svcret.ejb.model.entity.PersServiceVersionUrl;
import net.svcret.ejb.model.entity.PersUser;
import net.svcret.ejb.util.LogUtil;
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
	@EJB
	private IConfigService myConfigSvc;
	
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
	 * @param theInvocationResults 
	 * @throws ProcessingException 
	 * @throws UnexpectedFailureException 
	 */
	@Override
	public void logTransaction(SrBeanIncomingRequest theRequest,BasePersServiceVersion theSvcVer, PersServiceVersionMethod theMethod, PersUser theUser, String theRequestBody, SrBeanProcessedResponse theInvocationResponse,
			PersServiceVersionUrl theImplementationUrl, SrBeanIncomingResponse theHttpResponse, AuthorizationOutcomeEnum theAuthorizationOutcome, String theResponseBody, SrBeanProcessedRequest theInvocationResults) throws ProcessingException, UnexpectedFailureException {
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
			existing.recordTransaction(myConfigSvc.getConfig(),theRequest.getRequestTime(), theSvcVer, theMethod, theUser, theRequestBody, theInvocationResponse, theRequest, theImplementationUrl, theHttpResponse,
					theAuthorizationOutcome, theResponseBody);
		}

		if (theUser != null) {
			UnflushedUserRecentMessages newValue = new UnflushedUserRecentMessages(theUser);
			UnflushedUserRecentMessages existing = myUnflushedUserMessages.putIfAbsent(theUser, newValue);
			if (existing == null) {
				existing = newValue;
			}

			existing.recordTransaction(myConfigSvc.getConfig(), theRequest.getRequestTime(), theRequest, theSvcVer, theMethod, theUser, theRequestBody, theInvocationResponse, theImplementationUrl, theHttpResponse,
					theAuthorizationOutcome, theResponseBody);

			// Audit log
			if (theUser.determineInheritedAuditLogEnable()) {
				myFilesystemAuditLogger.recordUserTransaction(theRequest, theSvcVer, theMethod, theUser, theRequestBody, theInvocationResponse, theImplementationUrl, theHttpResponse, theAuthorizationOutcome, theInvocationResults);
			}

		}

		// Audit Log
		if (theSvcVer.determineInheritedAuditLogEnable() == true) {
			myFilesystemAuditLogger.recordServiceTransaction(theRequest, theSvcVer, theMethod, theUser, theRequestBody, theInvocationResponse, theImplementationUrl, theHttpResponse, theAuthorizationOutcome, theInvocationResults);
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

		ByteDelta byteDelta = new ByteDelta();
		int saveCount = 0;
		for (Object next : new HashSet<Object>(unflushedMessages.keySet())) {
			BaseUnflushed<? extends BasePersSavedTransactionRecentMessage> nextTransactions = unflushedMessages.remove(next);
			if (nextTransactions != null) {
				ByteDelta nextByteDelta = myDao.saveRecentMessagesAndTrimInNewTransaction(nextTransactions);
				byteDelta.add(nextByteDelta);
				saveCount += nextTransactions.getCount();
			}
		}

		long delay = System.currentTimeMillis() - start;
		ourLog.info("Done saving {} recent transactions to database in {}ms. Added {} / Removed {}", 
				new Object[] {saveCount, delay, LogUtil.formatByteCount(byteDelta.getAdded(), false), LogUtil.formatByteCount(byteDelta.getRemoved(), false)});
	}

	@VisibleForTesting
	public void setFilesystemAuditLoggerForUnitTests(IFilesystemAuditLogger theFilesystemAuditLogger) {
		myFilesystemAuditLogger = theFilesystemAuditLogger;
	}

	public static void trimOldest(LinkedList<?> theList, int theSize) {
		while (theList.size() > theSize) {
			theList.pop();
		}
	}

	@VisibleForTesting
	public void setConfigServiceForUnitTests(IConfigService theConfigSvc) {
		myConfigSvc=theConfigSvc;
	}


}
