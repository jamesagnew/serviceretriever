package net.svcret.ejb.ejb;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.PostConstruct;
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
import net.svcret.ejb.api.IConfigService;
import net.svcret.ejb.api.IFilesystemAuditLogger;
import net.svcret.ejb.api.InvocationResponseResultsBean;
import net.svcret.ejb.ex.ProcessingException;
import net.svcret.ejb.model.entity.PersServiceVersionMethod;
import net.svcret.ejb.model.entity.PersServiceVersionUrl;
import net.svcret.ejb.model.entity.PersUser;

import org.apache.commons.lang3.StringUtils;

import com.google.common.annotations.VisibleForTesting;

@Singleton()
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class FilesystemAuditLoggerBean implements IFilesystemAuditLogger {

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(FilesystemAuditLoggerBean.class);

	private File myAuditPath;

	@EJB
	private IConfigService myConfigSvc;
	private volatile int myFailIfQueueExceedsSize = 10000;
	private ReentrantLock myFlushLockAuditRecord = new ReentrantLock();
	private SimpleDateFormat myItemDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSS Z");
	private AtomicLong myLastAuditRecordFlush = new AtomicLong(System.currentTimeMillis());
	private volatile int myTriggerQueueFlushAtMillisSinceLastFlush = 60 * 1000;
	private volatile int myTriggerQueueFlushAtQueueSize = 100;

	private ConcurrentLinkedQueue<UnflushedAuditRecord> myUnflushedAuditRecord = new ConcurrentLinkedQueue<UnflushedAuditRecord>();

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@Override
	public void flushAuditEventsIfNeeded() {
		try {
			flushIfNeccesary();
		} catch (ProcessingException e) {
			ourLog.error("Failed to flush transactions", e);
		}
	}

	@PostConstruct
	public void initialize() throws ProcessingException {
		ourLog.info("Initializing filesystem audit logger service");

		String path = myConfigSvc.getFilesystemAuditLoggerPath();
		if (StringUtils.isBlank(path)) {
			path = new File("sr-audit").getAbsolutePath();
			ourLog.info("No filesystem audit path specified, going to use the following path: {}", path);
		}

		myAuditPath = new File(path);
		if (!myAuditPath.exists()) {
			ourLog.info("Path does not exist, going to create it: {}", myAuditPath.getAbsolutePath());
			if (!myAuditPath.mkdirs()) {
				throw new ProcessingException("Failed to create path (do we have permission?): " + myAuditPath.getAbsoluteFile());
			}
		}

		if (!myAuditPath.isDirectory()) {
			throw new ProcessingException("Path exists but is not a directory: " + myAuditPath.getAbsoluteFile());
		}

	}

	@Override
	public void recordServiceTransaction(HttpRequestBean theRequest, PersServiceVersionMethod theMethod, PersUser theUser, String theRequestBody, InvocationResponseResultsBean theInvocationResponse,
			PersServiceVersionUrl theImplementationUrl, HttpResponseBean theHttpResponse, AuthorizationOutcomeEnum theAuthorizationOutcome) throws ProcessingException {

		validateQueueSize();

		UnflushedAuditRecord auditLog = new UnflushedAuditRecord(theRequest.getRequestTime(), theRequest, theMethod, theUser, theRequestBody, theInvocationResponse, theImplementationUrl,
				theHttpResponse, theAuthorizationOutcome, AuditLogTypeEnum.SVCVER);
		myUnflushedAuditRecord.add(auditLog);

		flushIfNeccesary();
	}

	@Override
	public void recordUserTransaction(HttpRequestBean theRequest, PersServiceVersionMethod theMethod, PersUser theUser, String theRequestBody, InvocationResponseResultsBean theInvocationResponse,
			PersServiceVersionUrl theImplementationUrl, HttpResponseBean theHttpResponse, AuthorizationOutcomeEnum theAuthorizationOutcome) throws ProcessingException {

		validateQueueSize();

		UnflushedAuditRecord auditLog = new UnflushedAuditRecord(theRequest.getRequestTime(), theRequest, theMethod, theUser, theRequestBody, theInvocationResponse, theImplementationUrl,
				theHttpResponse, theAuthorizationOutcome, AuditLogTypeEnum.USER);
		myUnflushedAuditRecord.add(auditLog);

		flushIfNeccesary();
	}

	private void addItem(FileWriter writer, String key, String value) throws IOException {
		writer.append(key);
		writer.append(": ");
		writer.append(value);
		writer.append("\n");
	}

	private void flush() throws ProcessingException {
		ourLog.info("About to begin flushing approximately {} records from audit log queue", myUnflushedAuditRecord.size());
		long start = System.currentTimeMillis();

		Map<String, FileWriter> writers = new HashMap<String, FileWriter>();
		Map<String, String> lookups = new HashMap<String, String>();

		int count = 0;
		while (true) {

			UnflushedAuditRecord nextToFlush = myUnflushedAuditRecord.peek();
			if (nextToFlush == null) {
				break;
			}

			try {
				String fileName = nextToFlush.createLogFile();
				if (!writers.containsKey(fileName)) {
					FileWriter writer = new FileWriter(new File(myAuditPath, fileName), true);
					writers.put(fileName, writer);
				}

				if (!lookups.containsKey(nextToFlush.myRequestHostIp)) {
					try {
						InetAddress addr = InetAddress.getByName(nextToFlush.myRequestHostIp);
						String host = addr.getHostName();
						lookups.put(nextToFlush.myRequestHostIp, host);
					} catch (Throwable e) {
						ourLog.debug("Failed to lookup hostname", e);
						lookups.put(nextToFlush.myRequestHostIp, null);
					}
				}

				FileWriter writer = writers.get(fileName);
				addItem(writer, "Date", myItemDateFormat.format(nextToFlush.myRequestTime));
				addItem(writer, "RequestorIp", nextToFlush.myRequestHostIp + " (" + lookups.get(nextToFlush.myRequestHostIp) + ")");
				addItem(writer, "DomainId", nextToFlush.myDomainId);
				addItem(writer, "ServiceId", nextToFlush.myServiceId);
				addItem(writer, "ServiceVersionId", nextToFlush.myServiceVersionId);
				addItem(writer, "ServiceVersionPid", Long.toString(nextToFlush.myServiceVersionPid));
				addItem(writer, "MethodName", nextToFlush.myMethodName);
				addItem(writer, "HandledByUrl", "[" + nextToFlush.myImplementationUrlId + "] " + nextToFlush.myImplementationUrl);
				if (nextToFlush.myUserPid == null) {
					addItem(writer, "User", "none");
				} else {
					addItem(writer, "User", "[" + nextToFlush.myUserPid + "] " + nextToFlush.myUsername);
				}
				writer.append("Request:\n");
				writer.append(formatMessage(nextToFlush.myRequestBody));
				writer.append("\nResponse:\n");
				writer.append(formatMessage(nextToFlush.myResponseBody));
				writer.append("\n\n");

			} catch (IOException e) {
				ourLog.error("Failed to write audit log", e);
				throw new ProcessingException("Failed to write to audit log", e);
			}

			myUnflushedAuditRecord.poll();
			count++;
		}

		for (FileWriter next : writers.values()) {
			try {
				next.close();
			} catch (IOException e) {
				ourLog.error("Failed to close writer", e);
			}

		}

		long delay = System.currentTimeMillis() - start;
		ourLog.info("Finished flushing {} audit records in {}ms", count, delay);
	}

	private void flushIfNeccesary() throws ProcessingException {
		if (myUnflushedAuditRecord.size() < myTriggerQueueFlushAtQueueSize) {
			if (myLastAuditRecordFlush.get() + myTriggerQueueFlushAtMillisSinceLastFlush > System.currentTimeMillis()) {
				return;
			}
		}

		if (!myFlushLockAuditRecord.tryLock()) {
			return;
		}

		try {
			flush();
		} finally {
			myFlushLockAuditRecord.unlock();
		}
	}

	private void validateQueueSize() throws ProcessingException {
		if (myUnflushedAuditRecord.size() > myFailIfQueueExceedsSize) {
			throw new ProcessingException("Audit log queue has exceeded maximum threshold of " + myFailIfQueueExceedsSize);
		}
	}

	@VisibleForTesting
	void setConfigServiceForUnitTests(IConfigService theCfgSvc) {
		myConfigSvc=theCfgSvc;
		
	}

	/**
	 * Cleans up line separators and indents each line of the string by two spaces
	 */
	@SuppressWarnings("fallthrough")
	static String formatMessage(String theInput) {
		if (theInput == null) {
			return null;
		}

		StringBuilder output = new StringBuilder(theInput.length() + 100);
		output.append("  ");

		for (int i = 0; i < theInput.length(); i++) {

			char nextChar = theInput.charAt(i);

			switch (nextChar) {
			case '\r':
				char followingChar = (i + 1) < theInput.length() ? theInput.charAt(i + 1) : ' ';
				if (followingChar == '\n') {
					continue;
				}
				// fall through

			case '\n':
				output.append("\n  ");
				continue;

			default:
				// nothing
			}

			output.append(nextChar);
		}

		// Trim trailing newlines
		while (output.length() > 2) {
			char lastChar = output.charAt(output.length() - 1);
			if (lastChar == '\n' || lastChar == '\r') {
				output.setLength(output.length() - 1);
			} else {
				break;
			}
		}

		return output.toString();
	}

	public enum AuditLogTypeEnum {
		/*
		 * Don't put underscores in these names!
		 */
		SVCVER {
			@Override
			public String createLogFile(UnflushedAuditRecord theRecord) {
				return "svcver_" + theRecord.getServiceVersionPid() + ".log";
			}
		},

		USER {
			@Override
			public String createLogFile(UnflushedAuditRecord theRecord) {
				return "user_" + theRecord.getUserPid() + ".log";
			}
		};

		public abstract String createLogFile(UnflushedAuditRecord theRecord);

	}

	private class UnflushedAuditRecord {

		public String myImplementationUrl;
		private AuditLogTypeEnum myAuditRecordType;
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
		private Long myServiceVersionPid;
		private Long myTransactionMillis;
		private String myUsername;
		private Long myUserPid;

		public UnflushedAuditRecord(Date theRequestTime, HttpRequestBean theRequest, PersServiceVersionMethod theMethod, PersUser theUser, String theRequestBody,
				InvocationResponseResultsBean theInvocationResponse, PersServiceVersionUrl theImplementationUrl, HttpResponseBean theHttpResponse, AuthorizationOutcomeEnum theAuthorizationOutcome,
				AuditLogTypeEnum theType) {

			if (theType == AuditLogTypeEnum.USER && theUser == null) {
				throw new IllegalArgumentException("No user provided for USER record");
			}

			myAuditRecordType = theType;
			myRequestTime = theRequestTime;
			myHeaders = theRequest.getRequestHeaders();
			myRequestBody = theRequestBody;
			myImplementationUrlId = theImplementationUrl.getUrlId();
			myImplementationUrl = theImplementationUrl.getUrl();
			myRequestHostIp = theRequest.getRequestHostIp();
			myResponseHeaders = theInvocationResponse.getResponseHeaders();
			myResponseBody = theInvocationResponse.getResponseBody();
			myResponseType = theInvocationResponse.getResponseType();
			myMethodName = theMethod.getName();
			if (theUser != null) {
				myUsername = theUser.getUsername();
				myUserPid = theUser.getPid();
			} else {
				myUsername = null;
			}
			myServiceVersionId = theMethod.getServiceVersion().getVersionId();
			myServiceId = theMethod.getServiceVersion().getService().getServiceId();
			myDomainId = theMethod.getServiceVersion().getService().getDomain().getDomainId();
			myServiceVersionPid = theMethod.getServiceVersion().getPid();

			long responseTime = theHttpResponse != null ? theHttpResponse.getResponseTime() : 0;
			myTransactionMillis = responseTime;
			
			assert myAuditRecordType != null;
			assert myRequestTime != null;
			assert myHeaders != null;
			assert StringUtils.isNotBlank(myRequestBody);
			assert StringUtils.isNotBlank(myImplementationUrl);
			assert StringUtils.isNotBlank(myImplementationUrlId);
			assert myRequestHostIp != null;
			assert myResponseHeaders != null;
			assert StringUtils.isNotBlank(myMethodName);
			assert StringUtils.isNotBlank(myDomainId);
			assert StringUtils.isNotBlank(myServiceId);
			assert StringUtils.isNotBlank(myServiceVersionId);
			assert myServiceVersionPid != null;
		}

		public Long getServiceVersionPid() {
			return myServiceVersionPid;
		}

		public Long getUserPid() {
			return myUserPid;
		}

		public String createLogFile() {
			return myAuditRecordType.createLogFile(this);
		}

	}

	public void forceFlush() throws ProcessingException {
		flush();
	}

}
