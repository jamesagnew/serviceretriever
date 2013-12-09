package net.svcret.ejb.model.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.google.common.annotations.VisibleForTesting;

import net.svcret.admin.shared.enm.ResponseTypeEnum;
import net.svcret.ejb.api.HttpRequestBean;
import net.svcret.ejb.api.InvocationResponseResultsBean;
import net.svcret.ejb.api.RequestType;

@MappedSuperclass()
public abstract class BasePersSavedTransaction implements Serializable {

	private static final int FAIL_DESC_LENGTH = 500;

	private static final long serialVersionUID = 1L;

	@Column(name = "FAIL_DESC", nullable = true, length = FAIL_DESC_LENGTH)
	private String myFailDescription;

	@ManyToOne
	@JoinColumn(name = "URL_PID", referencedColumnName = "PID", nullable = true)
	private PersServiceVersionUrl myImplementationUrl;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "PID")
	private Long myPid;

	@Column(name = "REQ_BODY")
	@Lob()
	@Basic(fetch = FetchType.LAZY)
	private String myRequestBody;

	@Column(name = "REQ_BODY_BYTES")
	private int myRequestBodyBytes;

	@Column(name = "REQ_BODY_TRUNCATED")
	private boolean myRequestBodyTruncated;

	@Column(name = "RESP_BODY")
	@Lob()
	@Basic(fetch = FetchType.LAZY)
	private String myResponseBody;

	@Column(name = "RESP_BODY_BYTES")
	private int myResponseBodyBytes;

	@Column(name = "RESP_BODY_TRUNCATED")
	private boolean myResponseBodyTruncated;

	@Column(name = "RESPONSE_TYPE", nullable = false, length = EntityConstants.MAXLEN_RESPONSE_TYPE_ENUM)
	@Enumerated(EnumType.STRING)
	private ResponseTypeEnum myResponseType;

	@Column(name = "XACT_MILLIS", nullable = false)
	private long myTransactionMillis;

	@Column(name = "XACT_TIME", nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date myTransactionTime;

	public String getFailDescription() {
		return myFailDescription;
	}

	/**
	 * @return the implementationUrl
	 */
	public PersServiceVersionUrl getImplementationUrl() {
		return myImplementationUrl;
	}

	/**
	 * @return the pid
	 */
	public Long getPid() {
		return myPid;
	}

	@VisibleForTesting
	public void setPidForUnitTest(Long thePid) {
		myPid = thePid;
	}

	/**
	 * @return the requestBody
	 */
	public String getRequestBody() {
		return myRequestBody;
	}

	public int getRequestBodyBytes() {
		return myRequestBodyBytes;
	}

	/**
	 * @return the responseBody
	 */
	public String getResponseBody() {
		return myResponseBody;
	}

	public int getResponseBodyBytes() {
		return myResponseBodyBytes;
	}

	/**
	 * @return the responseType
	 */
	public ResponseTypeEnum getResponseType() {
		return myResponseType;
	}

	/**
	 * @return the transactionMillis
	 */
	public long getTransactionMillis() {
		return myTransactionMillis;
	}

	/**
	 * @return the transactionTime
	 */
	public Date getTransactionTime() {
		return myTransactionTime;
	}

	public boolean isRequestBodyTruncated() {
		return myRequestBodyTruncated;
	}

	public boolean isResponseBodyTruncated() {
		return myResponseBodyTruncated;
	}

	public void populate(PersConfig theConfig, Date theTransactionTime, HttpRequestBean theRequest, PersServiceVersionUrl theImplementationUrl, String theRequestBody, InvocationResponseResultsBean theInvocationResult,
			String theResponseBody) {
		setRequestBody(extractHeadersForBody(theRequest) + theRequestBody, theConfig);
		setImplementationUrl(theImplementationUrl);
		setResponseBody(extractHeadersForBody(theInvocationResult.getResponseHeaders()) + theResponseBody, theConfig);
		setResponseType(theInvocationResult.getResponseType());
		setTransactionTime(theTransactionTime);
		setFailDescription(theInvocationResult.getResponseFailureDescription());
	}

	public void setFailDescription(String theFailDescription) {
		if (theFailDescription != null && theFailDescription.length() > FAIL_DESC_LENGTH) {
			myFailDescription = theFailDescription.substring(0, FAIL_DESC_LENGTH - 3) + "...";
		} else {
			myFailDescription = theFailDescription;
		}
	}

	/**
	 * @param theImplementationUrl
	 *            the implementationUrl to set
	 */
	public void setImplementationUrl(PersServiceVersionUrl theImplementationUrl) {
		myImplementationUrl = theImplementationUrl;
	}

	/**
	 * @param theRequestBody
	 *            the requestBody to set
	 */
	public void setRequestBody(String theRequestBody, PersConfig theConfig) {
		if (theRequestBody != null) {
			String requestBody = BasePersObject.trimClobForUnitTest(theRequestBody);;
			if (theConfig.getTruncateRecentDatabaseTransactionsToBytes() != null) {
				if (requestBody.length() > theConfig.getTruncateRecentDatabaseTransactionsToBytes()) {
					requestBody = requestBody.substring(0, theConfig.getTruncateRecentDatabaseTransactionsToBytes());
				}
			}
			myRequestBody = requestBody;
			myRequestBodyBytes = myRequestBody.length();
			myRequestBodyTruncated = requestBody.length() < theRequestBody.length();
		} else {
			myRequestBody = null;
			myRequestBodyBytes = -1;
			myRequestBodyTruncated = false;
		}

	}

	/**
	 * @param theResponseBody
	 *            the responseBody to set
	 */
	public void setResponseBody(String theResponseBody, PersConfig theConfig) {
		if (theResponseBody != null) {
			String responseBody = BasePersObject.trimClobForUnitTest(theResponseBody);
			if (theConfig.getTruncateRecentDatabaseTransactionsToBytes() != null) {
				if (responseBody.length() > theConfig.getTruncateRecentDatabaseTransactionsToBytes()) {
					responseBody = responseBody.substring(0, theConfig.getTruncateRecentDatabaseTransactionsToBytes());
				}
			}
			myResponseBody = responseBody;
			myResponseBodyBytes = myResponseBody.length();
			myResponseBodyTruncated = responseBody.length() < theResponseBody.length();
		} else {
			myResponseBody = null;
			myResponseBodyBytes = -1;
			myResponseBodyTruncated = false;
		}
	}

	
	/**
	 * @param theResponseType
	 *            the responseType to set
	 */
	public void setResponseType(ResponseTypeEnum theResponseType) {
		if (theResponseType == null) {
			throw new NullPointerException();
		}
		myResponseType = theResponseType;
	}

	/**
	 * @param theTransactionMillis
	 *            the transactionMillis to set
	 */
	public void setTransactionMillis(long theTransactionMillis) {
		myTransactionMillis = theTransactionMillis;
	}

	/**
	 * @param theTransactionTime
	 *            the transactionTime to set
	 */
	public void setTransactionTime(Date theTransactionTime) {
		myTransactionTime = theTransactionTime;
	}

	private String extractHeadersForBody(HttpRequestBean theRequest) {
		StringBuilder b = new StringBuilder();

		RequestType requestType = theRequest.getRequestType();
		if (requestType != null) {
			b.append(requestType.name()).append(' ');
			b.append(theRequest.getRequestFullUri()).append(' ');
			b.append(theRequest.getProtocol()).append("\r\n");
		}

		Map<String, List<String>> headers = theRequest.getRequestHeaders();
		return extractHeadersForBody(headers, b);
	}

	private String extractHeadersForBody(Map<String, List<String>> theResponseHeaders) {
		return extractHeadersForBody(theResponseHeaders, new StringBuilder());
	}

	private String extractHeadersForBody(Map<String, List<String>> theHeaders, StringBuilder b) {
		if (theHeaders != null) {
			for (String nextHeader : theHeaders.keySet()) {
				for (String nextValue : theHeaders.get(nextHeader)) {
					b.append(nextHeader).append(": ").append(nextValue).append("\r\n");
				}
			}
		}
		b.append("\r\n");
		return b.toString();
	}

}
