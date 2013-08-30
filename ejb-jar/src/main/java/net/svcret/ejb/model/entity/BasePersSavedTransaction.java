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

import net.svcret.admin.shared.enm.ResponseTypeEnum;
import net.svcret.ejb.api.HttpRequestBean;
import net.svcret.ejb.api.InvocationResponseResultsBean;

@MappedSuperclass()
public abstract class BasePersSavedTransaction implements Serializable {

	private static final long serialVersionUID = 1L;

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

	@Column(name = "RESP_BODY")
	@Lob()
	@Basic(fetch = FetchType.LAZY)
	private String myResponseBody;

	@Column(name = "RESPONSE_TYPE", nullable = false, length = EntityConstants.MAXLEN_RESPONSE_TYPE_ENUM)
	@Enumerated(EnumType.STRING)
	private ResponseTypeEnum myResponseType;

	@Column(name = "XACT_MILLIS", nullable = false)
	private long myTransactionMillis;

	@Column(name = "XACT_TIME", nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date myTransactionTime;

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

	/**
	 * @return the responseBody
	 */
	public String getResponseBody() {
		return myResponseBody;
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

	public void populate(Date theTransactionTime, HttpRequestBean theRequest, PersServiceVersionUrl theImplementationUrl, String theRequestBody, InvocationResponseResultsBean theInvocationResult, String theResponseBody) {
		myRequestBody = extractHeadersForBody(theRequest.getRequestHeaders()) + theRequestBody;
		myImplementationUrl = theImplementationUrl;
		myResponseBody = extractHeadersForBody(theInvocationResult.getResponseHeaders()) + theResponseBody;
		myResponseType = theInvocationResult.getResponseType();
		myTransactionTime = theTransactionTime;
	}

	private String extractHeadersForBody(Map<String, List<String>> headers) {
		StringBuilder b = new StringBuilder();
		if (headers != null) {
			for (String nextHeader : headers.keySet()) {
				for (String nextValue : headers.get(nextHeader)) {
					b.append(nextHeader).append(": ").append(nextValue).append("\r\n");
				}
			}
		}
		b.append("\r\n");
		return b.toString();
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
	public void setRequestBody(String theRequestBody) {
		myRequestBody = theRequestBody;
	}

	/**
	 * @param theResponseBody
	 *            the responseBody to set
	 */
	public void setResponseBody(String theResponseBody) {
		myResponseBody = theResponseBody;
	}

	/**
	 * @param theResponseType
	 *            the responseType to set
	 */
	public void setResponseType(ResponseTypeEnum theResponseType) {
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

}
