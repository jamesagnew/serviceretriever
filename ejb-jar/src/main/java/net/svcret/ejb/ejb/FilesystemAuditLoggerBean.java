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
import java.util.regex.Pattern;

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
import net.svcret.ejb.model.entity.BasePersServiceVersion;
import net.svcret.ejb.model.entity.PersServiceVersionMethod;
import net.svcret.ejb.model.entity.PersServiceVersionUrl;
import net.svcret.ejb.model.entity.PersUser;

import org.apache.commons.lang3.StringUtils;

import com.google.common.annotations.VisibleForTesting;

@Singleton()
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class FilesystemAuditLoggerBean implements IFilesystemAuditLogger {

	private static final Pattern PARAM_VALUE_WHITESPACE = Pattern.compile("\\r|\\n", Pattern.MULTILINE);

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
	public void recordServiceTransaction(HttpRequestBean theRequest, BasePersServiceVersion theSvcVer, PersServiceVersionMethod theMethod, PersUser theUser, String theRequestBody,
			InvocationResponseResultsBean theInvocationResponse, PersServiceVersionUrl theImplementationUrl, HttpResponseBean theHttpResponse, AuthorizationOutcomeEnum theAuthorizationOutcome)
			throws ProcessingException {

		validateQueueSize();

		UnflushedAuditRecord auditLog = new UnflushedAuditRecord(theRequest.getRequestTime(), theRequest, theSvcVer, theMethod, theUser, theRequestBody, theInvocationResponse, theImplementationUrl,
				theHttpResponse, theAuthorizationOutcome, AuditLogTypeEnum.SVCVER);
		myUnflushedAuditRecord.add(auditLog);

		flushIfNeccesary();
	}

	@Override
	public void recordUserTransaction(HttpRequestBean theRequest, BasePersServiceVersion theSvcVer, PersServiceVersionMethod theMethod, PersUser theUser, String theRequestBody,
			InvocationResponseResultsBean theInvocationResponse, PersServiceVersionUrl theImplementationUrl, HttpResponseBean theHttpResponse, AuthorizationOutcomeEnum theAuthorizationOutcome)
			throws ProcessingException {

		validateQueueSize();

		UnflushedAuditRecord auditLog = new UnflushedAuditRecord(theRequest.getRequestTime(), theRequest, theSvcVer, theMethod, theUser, theRequestBody, theInvocationResponse, theImplementationUrl,
				theHttpResponse, theAuthorizationOutcome, AuditLogTypeEnum.USER);
		myUnflushedAuditRecord.add(auditLog);

		flushIfNeccesary();
	}

	private void addItem(FileWriter writer, String key, String value) throws IOException {
		writer.append(key);
		writer.append(": ");
		writer.append(formatParamValue(value));
		writer.append("\n");
	}

	private CharSequence formatParamValue(String theValue) {
		return PARAM_VALUE_WHITESPACE.matcher(theValue).replaceAll(" ");
	}

	private void flush() throws ProcessingException {
		ourLog.info("About to begin flushing approximately {} records from audit log queue", myUnflushedAuditRecord.size());
		long start = System.currentTimeMillis();

		Map<String, FileWriter> writers = new HashMap<String, FileWriter>();
		Map<String, String> lookups = new HashMap<String, String>();

		int count = 0;
		while (true) {

			UnflushedAuditRecord next = myUnflushedAuditRecord.peek();
			if (next == null) {
				break;
			}

			try {
				String fileName = next.createLogFile();
				if (!writers.containsKey(fileName)) {
					FileWriter writer = new FileWriter(new File(myAuditPath, fileName), true);
					writers.put(fileName, writer);
				}

				if (!lookups.containsKey(next.myRequestHostIp)) {
					try {
						InetAddress addr = InetAddress.getByName(next.myRequestHostIp);
						String host = addr.getHostName();
						lookups.put(next.myRequestHostIp, host);
					} catch (Throwable e) {
						ourLog.debug("Failed to lookup hostname", e);
						lookups.put(next.myRequestHostIp, null);
					}
				}

				FileWriter writer = writers.get(fileName);
				addItem(writer, "Date", myItemDateFormat.format(next.myRequestTime));
				addItem(writer, "Latency", Long.toString(next.myTransactionMillis));
				addItem(writer, "ResponseType", next.myResponseType.name());
				if (StringUtils.isNotBlank(next.myFailureDescription)) {
					addItem(writer, "FailureDescription", next.myFailureDescription);
				}
				addItem(writer, "RequestorIp", next.myRequestHostIp + " (" + lookups.get(next.myRequestHostIp) + ")");
				addItem(writer, "DomainId", next.myDomainId);
				addItem(writer, "ServiceId", next.myServiceId);
				addItem(writer, "ServiceVersionId", next.myServiceVersionId);
				addItem(writer, "ServiceVersionPid", Long.toString(next.myServiceVersionPid));
				if (StringUtils.isNotBlank(next.myMethodName)) {
					addItem(writer, "MethodName", next.myMethodName);
				}
				if (next.myImplementationUrl != null) {
					addItem(writer, "HandledByUrl", "[" + next.myImplementationUrlId + "] " + next.myImplementationUrl);
				}
				if (next.myUserPid == null) {
					addItem(writer, "User", "none");
				} else {
					addItem(writer, "User", "[" + next.myUserPid + "] " + next.myUsername);
				}
				if (next.myAuthorizationOutcome != null) {
					addItem(writer, "AuthorizationOutcome", next.myAuthorizationOutcome.name());
				}
				writer.append("Request:\n");
				String formatedRequest = formatMessage(next.myRequestBody);
				writer.append(formatedRequest);

				if (next.myResponseBody != null) {
					writer.append("\nResponse:\n");
					String formattedResponse = formatMessage(next.myResponseBody);
					writer.append(formattedResponse);
				}

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
		myConfigSvc = theCfgSvc;

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
			if (Character.isWhitespace(lastChar)) {
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
		private String myFailureDescription;
		private AuthorizationOutcomeEnum myAuthorizationOutcome;

		public UnflushedAuditRecord(Date theRequestTime, HttpRequestBean theRequest, BasePersServiceVersion theSvcVer, PersServiceVersionMethod theMethod, PersUser theUser, String theRequestBody,
				InvocationResponseResultsBean theInvocationResponse, PersServiceVersionUrl theImplementationUrl, HttpResponseBean theHttpResponse, AuthorizationOutcomeEnum theAuthorizationOutcome,
				AuditLogTypeEnum theType) {

			if (theType == AuditLogTypeEnum.USER && theUser == null) {
				throw new IllegalArgumentException("No user provided for USER record");
			}

			myAuditRecordType = theType;
			myRequestTime = theRequestTime;
			myHeaders = theRequest.getRequestHeaders();
			myRequestBody = theRequestBody;
			if (theImplementationUrl != null) {
				myImplementationUrlId = theImplementationUrl.getUrlId();
				myImplementationUrl = theImplementationUrl.getUrl();
			}
			myRequestHostIp = theRequest.getRequestHostIp();
			myResponseHeaders = theInvocationResponse.getResponseHeaders();
			myResponseBody = theInvocationResponse.getResponseBody();
			myResponseType = theInvocationResponse.getResponseType();
			myFailureDescription = theInvocationResponse.getResponseFailureDescription();
			myAuthorizationOutcome = theAuthorizationOutcome;
			myMethodName = theMethod != null ? theMethod.getName() : null;
			if (theUser != null) {
				myUsername = theUser.getUsername();
				myUserPid = theUser.getPid();
			} else {
				myUsername = null;
			}
			myServiceVersionId = theSvcVer.getVersionId();
			myServiceId = theSvcVer.getService().getServiceId();
			myDomainId = theSvcVer.getService().getDomain().getDomainId();
			myServiceVersionPid = theSvcVer.getPid();
			myTransactionMillis = theHttpResponse != null ? theHttpResponse.getResponseTime() : 0;

			assert myAuditRecordType != null;
			assert myRequestTime != null;
			assert myHeaders != null;
			assert StringUtils.isNotBlank(myRequestBody);
			assert theImplementationUrl == null || StringUtils.isNotBlank(myImplementationUrl);
			assert theImplementationUrl == null || StringUtils.isNotBlank(myImplementationUrlId);
			assert myRequestHostIp != null;
			assert StringUtils.isNotBlank(myDomainId);
			assert StringUtils.isNotBlank(myServiceId);
			assert StringUtils.isNotBlank(myServiceVersionId);
			assert myServiceVersionPid != null;
			assert myResponseType != null;
			assert myTransactionMillis != null;
			assert myResponseBody == null || myResponseHeaders != null;
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
