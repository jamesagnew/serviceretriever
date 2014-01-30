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
import net.svcret.ejb.api.IConfigService;
import net.svcret.ejb.api.IDao;
import net.svcret.ejb.api.IDao.ByteDelta;
import net.svcret.ejb.api.SrBeanIncomingRequest;
import net.svcret.ejb.api.SrBeanIncomingResponse;
import net.svcret.ejb.api.SrBeanProcessedRequest;
import net.svcret.ejb.api.SrBeanProcessedResponse;
import net.svcret.ejb.ex.ProcessingException;
import net.svcret.ejb.ex.UnexpectedFailureException;
import net.svcret.ejb.model.entity.BasePersSavedTransactionRecentMessage;
import net.svcret.ejb.model.entity.BasePersServiceVersion;
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
	 * @param theIncomingResponse
	 * @param theResponseBody
	 * @param theProcessedRequest
	 * @throws ProcessingException
	 * @throws UnexpectedFailureException
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	public void logTransaction(SrBeanIncomingRequest theIncomingRequest, PersUser theUser, SrBeanProcessedResponse theProcessedResponse, SrBeanIncomingResponse theIncomingResponse, AuthorizationOutcomeEnum theAuthorizationOutcome, SrBeanProcessedRequest theProcessedRequest)
			throws ProcessingException, UnexpectedFailureException {
		Validate.notNull(theProcessedRequest.getServiceVersion());
		Validate.notNull(theProcessedResponse);
		if (theProcessedResponse.getResponseType() != ResponseTypeEnum.FAIL) {
			Validate.notNull(theProcessedRequest.getMethodDefinition());
		}

		// Log to database
		{
			UnflushedServiceVersionRecentMessages newValue = new UnflushedServiceVersionRecentMessages(theProcessedRequest.getServiceVersion());
			UnflushedServiceVersionRecentMessages existing = myUnflushedMessages.putIfAbsent(theProcessedRequest.getServiceVersion(), newValue);
			if (existing == null) {
				existing = newValue;
			}
			existing.recordTransaction(myConfigSvc.getConfig(), theUser, theProcessedResponse, theIncomingRequest, theIncomingResponse, theAuthorizationOutcome, theProcessedRequest);
		}

		if (theUser != null) {
			UnflushedUserRecentMessages newValue = new UnflushedUserRecentMessages(theUser);
			UnflushedUserRecentMessages existing = myUnflushedUserMessages.putIfAbsent(theUser, newValue);
			if (existing == null) {
				existing = newValue;
			}

			existing.recordTransaction(myConfigSvc.getConfig(), theIncomingRequest, theUser, theProcessedResponse, theIncomingResponse, theAuthorizationOutcome, theProcessedRequest);

			// Audit log
			if (theUser.determineInheritedAuditLogEnable()) {
				myFilesystemAuditLogger.recordUserTransaction(theIncomingRequest, theProcessedRequest.getServiceVersion(), theProcessedRequest.getMethodDefinition(), theUser, theProcessedRequest.getObscuredRequestBody(), theProcessedResponse, theIncomingResponse,
						theAuthorizationOutcome, theProcessedRequest);
			}

		}

		// Audit Log
		if (theProcessedRequest.getServiceVersion().determineInheritedAuditLogEnable() == true) {
			myFilesystemAuditLogger.recordServiceTransaction(theIncomingRequest, theProcessedRequest.getServiceVersion(), theProcessedRequest.getMethodDefinition(), theUser, theProcessedRequest.getObscuredRequestBody(), theProcessedResponse, theIncomingResponse,
					theAuthorizationOutcome, theProcessedRequest);
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
		ourLog.info("Done saving {} recent transactions to database in {}ms. Added {} / Removed {}", new Object[] { saveCount, delay, LogUtil.formatByteCount(byteDelta.getAdded(), false), LogUtil.formatByteCount(byteDelta.getRemoved(), false) });
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
		myConfigSvc = theConfigSvc;
	}

}
