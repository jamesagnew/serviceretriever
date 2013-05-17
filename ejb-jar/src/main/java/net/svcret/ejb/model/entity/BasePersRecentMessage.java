package net.svcret.ejb.model.entity;

import java.io.Serializable;
import java.util.Date;

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
import javax.persistence.TemporalType;
import javax.persistence.Temporal;

import net.svcret.admin.shared.model.AuthorizationOutcomeEnum;
import net.svcret.ejb.api.IDao;
import net.svcret.ejb.api.InvocationResponseResultsBean;
import net.svcret.ejb.api.ResponseTypeEnum;

@MappedSuperclass()
public abstract class BasePersRecentMessage implements Serializable {

	private static final long serialVersionUID = 1L;

	@Column(name="AUTHN_OUTCOME")
	@Enumerated(EnumType.STRING)
	private AuthorizationOutcomeEnum myAuthorizationOutcome;
	
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

	@Column(name = "REQ_HOST_IP", nullable = false, length = 200)
	private String myRequestHostIp;

	@Column(name = "RESP_BODY")
	@Lob()
	@Basic(fetch = FetchType.LAZY)
	private String myResponseBody;

	@Column(name = "RESPONSE_TYPE", nullable = false)
	private ResponseTypeEnum myResponseType;

	@Column(name = "XACT_MILLIS", nullable = false)
	private long myTransactionMillis;

	@Column(name = "XACT_TIME", nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date myTransactionTime;
	
	public abstract void addUsingDao(IDao theDaoBean);

	/**
	 * @return the authorizationOutcome
	 */
	public AuthorizationOutcomeEnum getAuthorizationOutcome() {
		return myAuthorizationOutcome;
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

	/**
	 * @return the requestBody
	 */
	public String getRequestBody() {
		return myRequestBody;
	}

	/**
	 * @return the requestHostIp
	 */
	public String getRequestHostIp() {
		return myRequestHostIp;
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

	public void populate(Date theTransactionTime, String theRequestHostIp, PersServiceVersionUrl theImplementationUrl, String theRequestBody, InvocationResponseResultsBean theInvocationResult) {
		myRequestBody = theRequestBody;
		myImplementationUrl = theImplementationUrl;
		myRequestHostIp = theRequestHostIp;
		myResponseBody = theInvocationResult.getResponseBody();
		myResponseType = theInvocationResult.getResponseType();
		myTransactionTime = theTransactionTime;
	}

	/**
	 * @param theAuthorizationOutcome the authorizationOutcome to set
	 */
	public void setAuthorizationOutcome(AuthorizationOutcomeEnum theAuthorizationOutcome) {
		myAuthorizationOutcome = theAuthorizationOutcome;
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
	 * @param theRequestHostIp
	 *            the requestHostIp to set
	 */
	public void setRequestHostIp(String theRequestHostIp) {
		myRequestHostIp = theRequestHostIp;
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
	 * @param theTransactionMillis the transactionMillis to set
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

	public abstract void trimUsingDao(IDao theDaoBean);
}
