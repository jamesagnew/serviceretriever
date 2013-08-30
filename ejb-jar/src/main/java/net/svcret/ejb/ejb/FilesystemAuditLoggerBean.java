package net.svcret.ejb.ejb;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantLock;

import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Singleton;

import net.svcret.admin.shared.enm.ResponseTypeEnum;
import net.svcret.admin.shared.model.AuthorizationOutcomeEnum;
import net.svcret.ejb.api.HttpRequestBean;
import net.svcret.ejb.api.HttpResponseBean;
import net.svcret.ejb.api.IFilesystemAuditLogger;
import net.svcret.ejb.api.InvocationResponseResultsBean;
import net.svcret.ejb.model.entity.PersServiceVersionMethod;
import net.svcret.ejb.model.entity.PersServiceVersionUrl;
import net.svcret.ejb.model.entity.PersUser;

@Singleton()
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
@SuppressWarnings("unused") // TODO: REMOVE THIS
public class FilesystemAuditLoggerBean implements IFilesystemAuditLogger {

	private ReentrantLock mtAuditRecordFlushLock = new ReentrantLock();
	private ConcurrentLinkedQueue<UnflushedAuditRecord> myUnflushedAuditRecord = new ConcurrentLinkedQueue<UnflushedAuditRecord>();
	private ConcurrentLinkedQueue<UnflushedAuditRecord> myUnflushedUserAuditRecord = new ConcurrentLinkedQueue<UnflushedAuditRecord>();
	private int myTriggerQueueFlushAtQueueSize = 100;

	private static class UnflushedAuditRecord {

		private String myDomainId;
		private Map<String, List<String>> myHeaders;
		private String myImplementationUrlId;
		private String myMethodName;
		private String myRequestBody;
		private String myRequestHostIp;
		private Date myRequestTime;
		private String myResponseBody;
		private Map<String, List<String>> myResponseHeaders;
		private ResponseTypeEnum myResponseType;
		private String myServiceId;
		private String myServiceVersionId;
		private Long myTransactionMillis;
		private String myUsername;

		public UnflushedAuditRecord(Date theRequestTime, HttpRequestBean theRequest, PersServiceVersionMethod theMethod, PersUser theUser, String theRequestBody,
				InvocationResponseResultsBean theInvocationResponse, PersServiceVersionUrl theImplementationUrl, HttpResponseBean theHttpResponse, AuthorizationOutcomeEnum theAuthorizationOutcome) {

			myRequestTime = theRequestTime;
			myHeaders = theRequest.getRequestHeaders();
			myRequestBody = theRequestBody;
			myImplementationUrlId = theImplementationUrl.getUrlId();
			myRequestHostIp = theRequest.getRequestHostIp();
			myResponseHeaders = theInvocationResponse.getResponseHeaders();
			myResponseBody = theInvocationResponse.getResponseBody();
			myResponseType = theInvocationResponse.getResponseType();
			myMethodName = theMethod.getName();
			myUsername = theUser != null ? theUser.getUsername() : null;
			myServiceVersionId = theMethod.getServiceVersion().getVersionId();
			myServiceId = theMethod.getServiceVersion().getService().getServiceId();
			myDomainId = theMethod.getServiceVersion().getService().getDomain().getDomainId();

			long responseTime = theHttpResponse != null ? theHttpResponse.getResponseTime() : 0;
			myTransactionMillis = responseTime;
		}

	}

	@Override
	public void recordServiceTransaction(HttpRequestBean theRequest, PersServiceVersionMethod theMethod, PersUser theUser, String theRequestBody, InvocationResponseResultsBean theInvocationResponse,
			PersServiceVersionUrl theImplementationUrl, HttpResponseBean theHttpResponse, AuthorizationOutcomeEnum theAuthorizationOutcome) {

		UnflushedAuditRecord auditLog = new UnflushedAuditRecord(theRequest.getRequestTime(), theRequest, theMethod, theUser, theRequestBody, theInvocationResponse, theImplementationUrl,
				theHttpResponse, theAuthorizationOutcome);
		myUnflushedAuditRecord.add(auditLog);
		
	}

	@Override
	public void recordUserTransaction(HttpRequestBean theRequest, PersServiceVersionMethod theMethod, PersUser theUser, String theRequestBody, InvocationResponseResultsBean theInvocationResponse,
			PersServiceVersionUrl theImplementationUrl, HttpResponseBean theHttpResponse, AuthorizationOutcomeEnum theAuthorizationOutcome) {

		UnflushedAuditRecord auditLog = new UnflushedAuditRecord(theRequest.getRequestTime(), theRequest, theMethod, theUser, theRequestBody, theInvocationResponse, theImplementationUrl,
				theHttpResponse, theAuthorizationOutcome);
		myUnflushedUserAuditRecord.add(auditLog);
		
	}

}
