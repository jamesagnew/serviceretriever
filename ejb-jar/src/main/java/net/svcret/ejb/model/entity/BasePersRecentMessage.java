package net.svcret.ejb.model.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.MappedSuperclass;

import net.svcret.ejb.api.InvocationResponseResultsBean;
import net.svcret.ejb.api.ResponseTypeEnum;

@MappedSuperclass()
public class BasePersRecentMessage implements Serializable {

	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
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
	
	@Column(name = "RESPONSE_TYPE", nullable = false)
	private ResponseTypeEnum myResponseType;
	
	@Column(name = "XACT_TIME", nullable = false)
	private Date myTransactionTime;

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
	 * @return the transactionTime
	 */
	public Date getTransactionTime() {
		return myTransactionTime;
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
	 * @param theTransactionTime
	 *            the transactionTime to set
	 */
	public void setTransactionTime(Date theTransactionTime) {
		myTransactionTime = theTransactionTime;
	}

	
	public void populate(Date theTransactionTime, String theRequestBody, InvocationResponseResultsBean theInvocationResult) {
		myRequestBody = theRequestBody;
		myResponseBody = theInvocationResult.getResponseBody();
		myResponseType = theInvocationResult.getResponseType();
		myTransactionTime = theTransactionTime;
	}

}
