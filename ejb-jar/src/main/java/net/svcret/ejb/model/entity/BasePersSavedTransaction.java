package net.svcret.ejb.model.entity;

import java.io.Serializable;
import java.util.ArrayList;
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

import net.svcret.admin.shared.enm.ResponseTypeEnum;
import net.svcret.admin.shared.model.BaseDtoSavedTransaction;
import net.svcret.admin.shared.model.Pair;
import net.svcret.ejb.api.RequestType;
import net.svcret.ejb.api.SrBeanIncomingRequest;
import net.svcret.ejb.api.SrBeanProcessedRequest;
import net.svcret.ejb.api.SrBeanProcessedResponse;

import com.google.common.annotations.VisibleForTesting;

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

	public abstract BasePersServiceVersion getServiceVersion();

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

	public void populate(PersConfig theConfig, SrBeanIncomingRequest theRequest, PersServiceVersionUrl theImplementationUrl, SrBeanProcessedResponse theProcessedResponse, SrBeanProcessedRequest theProcessedRequest) {
		setRequestBody(theConfig, theRequest, theProcessedRequest);
		setImplementationUrl(theImplementationUrl);
		setResponseBody(theConfig, theProcessedResponse);
		setResponseType(theProcessedResponse.getResponseType());
		setTransactionTime(theRequest.getRequestTime());
		setFailDescription(theProcessedResponse.getResponseFailureDescription());
	}

	public void setResponseBody(PersConfig theConfig, SrBeanProcessedResponse theProcessedResponse) {
		setResponseBody(extractHeadersForBody(theProcessedResponse.getResponseHeaders()) + theProcessedResponse.getObscuredResponseBody(), theConfig);
	}

	public void setRequestBody(PersConfig theConfig, SrBeanIncomingRequest theRequest, SrBeanProcessedRequest theProcessedRequest) {
		setRequestBody(extractHeadersForBody(theRequest) + theProcessedRequest.getObscuredRequestBody(), theConfig);
	}

	public void populateDto(BaseDtoSavedTransaction retVal, boolean theLoadMessageContents) {
		retVal.setPid(this.getPid());
		PersServiceVersionUrl implementationUrl = this.getImplementationUrl();
		if (implementationUrl != null) {
			retVal.setImplementationUrlId(implementationUrl.getUrlId());
			retVal.setImplementationUrlHref(implementationUrl.getUrl());
			retVal.setImplementationUrlPid(implementationUrl.getPid());
		}

		retVal.setTransactionTime(this.getTransactionTime());
		retVal.setTransactionMillis(this.getTransactionMillis());
		retVal.setFailDescription(this.getFailDescription());
		retVal.setResponseType(this.getResponseType());

		if (theLoadMessageContents) {
			int bodyIdx = this.getRequestBody().indexOf("\r\n\r\n");
			if (bodyIdx == -1) {
				retVal.setRequestMessage(this.getRequestBody());
				retVal.setRequestHeaders(new ArrayList<Pair<String>>());
				retVal.setRequestContentType("unknown");
			} else {
				retVal.setRequestMessage(this.getRequestBody().substring(bodyIdx + 4));
				retVal.setRequestActionLine(toActionLine(this.getRequestBody()));
				retVal.setRequestHeaders(toHeaders(this.getRequestBody().substring(0, bodyIdx)));
				retVal.setRequestContentType(toHeaderContentType(retVal.getRequestHeaders()));
			}

			bodyIdx = this.getResponseBody().indexOf("\r\n\r\n");
			if (bodyIdx == -1) {
				retVal.setResponseMessage(this.getResponseBody());
				retVal.setResponseHeaders(new ArrayList<Pair<String>>());
				retVal.setResponseContentType("unknown");
			} else {
				retVal.setResponseMessage(this.getResponseBody().substring(bodyIdx + 4));
				retVal.setResponseHeaders(toHeaders(this.getResponseBody().substring(0, bodyIdx)));
				retVal.setResponseContentType(toHeaderContentType(retVal.getResponseHeaders()));
			}

		}

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

	@VisibleForTesting
	public void setPidForUnitTest(Long thePid) {
		myPid = thePid;
	}

	/**
	 * @param theRequestBody
	 *            the requestBody to set
	 */
	private void setRequestBody(String theRequestBody, PersConfig theConfig) {
		if (theRequestBody != null) {
			String requestBody = BasePersObject.trimClobForUnitTest(theRequestBody);
			;
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
	private void setResponseBody(String theResponseBody, PersConfig theConfig) {
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

	private String extractHeadersForBody(SrBeanIncomingRequest theRequest) {
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

	private static String toActionLine(String theRequestBody) {
		int idx = theRequestBody.indexOf("\r\n");
		if (idx == -1) {
			return null;
		}

		String firstLine = theRequestBody.substring(0, idx);
		idx = firstLine.indexOf(": ");
		if (idx == -1) {
			// If the first line has no colon, it's the action line
			return firstLine;
		} else {
			return null;
		}
	}

	private static String toHeaderContentType(List<Pair<String>> theResponseHeaders) {
		for (Pair<String> pair : theResponseHeaders) {
			if (pair.getFirst().equalsIgnoreCase("content-type")) {
				return pair.getSecond().split(";")[0].trim();
			}
		}
		return null;
	}

	private static List<Pair<String>> toHeaders(String theHeaders) {
		ArrayList<Pair<String>> retVal = new ArrayList<Pair<String>>();
		int index = 0;
		for (String next : theHeaders.split("\\r\\n")) {
			int colonIndex = next.indexOf(": ");
			if (index == 0 && colonIndex == -1) {
				// First line is generally the action line for request messages
				continue;
			}
			retVal.add(new Pair<String>(next.substring(0, colonIndex), next.substring(colonIndex + 2)));
			index++;
		}
		return retVal;
	}


}
