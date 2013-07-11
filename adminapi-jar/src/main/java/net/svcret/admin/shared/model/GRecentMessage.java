package net.svcret.admin.shared.model;

import java.util.Date;
import java.util.List;

public class GRecentMessage extends BaseGObject<GRecentMessage> {

	private static final long serialVersionUID = 1L;

	private AuthorizationOutcomeEnum myAuthorizationOutcome;
	private String myDomainName;
	private long myDomainPid;
	private String myImplementationUrlHref;
	private String myImplementationUrlId;
	private long myImplementationUrlPid;
	private String myRequestContentType;
	private List<Pair<String>> myRequestHeaders;
	private String myRequestHostIp;
	private String myRequestMessage;
	private String myRequestUsername;
	private Long myRequestUserPid;
	private String myResponseContentType;
	private List<Pair<String>> myResponseHeaders;
	private String myResponseMessage;
	private String myServiceName;
	private long myServicePid;
	private String myServiceVersionId;
	private long myServiceVersionPid;
	private long myTransactionMillis;
	private Date myTransactionTime;
	
	public GRecentMessage() {
		super();
	}

	public GRecentMessage(long thePid, Date theDate, String theRequestHostIp, String theRequestMessage, String theResponseMessage, List<Pair<String>> theRequestHeaders, List<Pair<String>> theResponseHeaders, String theRequestContentType, String theResponseContentType) {
		setPid(thePid);
		myTransactionTime = theDate;
		myRequestHostIp = theRequestHostIp;
		myRequestMessage = theRequestMessage;
		myResponseMessage = theResponseMessage;
		myRequestHeaders = theRequestHeaders;
		myResponseHeaders = theResponseHeaders;
		myRequestContentType = theRequestContentType;
		myResponseContentType = theResponseContentType;
	}

	/**
	 * @return the authorizationOutcome
	 */
	public AuthorizationOutcomeEnum getAuthorizationOutcome() {
		return myAuthorizationOutcome;
	}


	public String getDomainName() {
		return myDomainName;
	}

	public long getDomainPid() {
		return myDomainPid;
	}

	public String getImplementationUrlHref() {
		return myImplementationUrlHref;
	}

	public String getImplementationUrlId() {
		return myImplementationUrlId;
	}

	public long getImplementationUrlPid() {
		return myImplementationUrlPid;
	}

	/**
	 * @return the requestContentType
	 */
	public String getRequestContentType() {
		return myRequestContentType;
	}

	/**
	 * @return the requestHeaders
	 */
	public List<Pair<String>> getRequestHeaders() {
		return myRequestHeaders;
	}

	/**
	 * @return the requestHostIp
	 */
	public String getRequestHostIp() {
		return myRequestHostIp;
	}

	public String getRequestMessage() {
		return myRequestMessage;
	}

	/**
	 * @return the requestUsername
	 */
	public String getRequestUsername() {
		return myRequestUsername;
	}

	/**
	 * @return the requestUserPid
	 */
	public Long getRequestUserPid() {
		return myRequestUserPid;
	}

	/**
	 * @return the responseContentType
	 */
	public String getResponseContentType() {
		return myResponseContentType;
	}

	/**
	 * @return the responseHeaders
	 */
	public List<Pair<String>> getResponseHeaders() {
		return myResponseHeaders;
	}

	/**
	 * @return the responseMessage
	 */
	public String getResponseMessage() {
		return myResponseMessage;
	}

	public String getServiceName() {
		return myServiceName;
	}

	public long getServicePid() {
		return myServicePid;
	}

	public String getServiceVersionId() {
		return myServiceVersionId;
	}

	public long getServiceVersionPid() {
		return myServiceVersionPid;
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

	@Override
	public void merge(GRecentMessage theObject) {

	}

	public void setAuthorizationOutcome(AuthorizationOutcomeEnum theAuthorizationOutcome) {
		myAuthorizationOutcome = theAuthorizationOutcome;
	}

	public void setDomainName(String theDomainName) {
		myDomainName = theDomainName;
	}

	public void setDomainPid(long theDomainPid) {
		myDomainPid = theDomainPid;
	}

	public void setImplementationUrlHref(String theImplementationUrlHref) {
		myImplementationUrlHref = theImplementationUrlHref;
	}

	public void setImplementationUrlId(String theImplementationUrlId) {
		myImplementationUrlId = theImplementationUrlId;
	}

	public void setImplementationUrlPid(long theImplementationUrlPid) {
		myImplementationUrlPid = theImplementationUrlPid;
	}

	/**
	 * @param theRequestContentType the requestContentType to set
	 */
	public void setRequestContentType(String theRequestContentType) {
		myRequestContentType = theRequestContentType;
	}

	/**
	 * @param theRequestHeaders the requestHeaders to set
	 */
	public void setRequestHeaders(List<Pair<String>> theRequestHeaders) {
		myRequestHeaders = theRequestHeaders;
	}

	/**
	 * @param theRequestHostIp
	 *            the requestHostIp to set
	 */
	public void setRequestHostIp(String theRequestHostIp) {
		myRequestHostIp = theRequestHostIp;
	}

	/**
	 * @param theRequestMessage
	 *            the requestMessage to set
	 */
	public void setRequestMessage(String theRequestMessage) {
		myRequestMessage = theRequestMessage;
	}

	public void setRequestUsername(String theUsername) {
		myRequestUsername = theUsername;
	}

	public void setRequestUserPid(Long thePid) {
		myRequestUserPid = thePid;
	}

	/**
	 * @param theResponseContentType the responseContentType to set
	 */
	public void setResponseContentType(String theResponseContentType) {
		myResponseContentType = theResponseContentType;
	}

	/**
	 * @param theResponseHeaders the responseHeaders to set
	 */
	public void setResponseHeaders(List<Pair<String>> theResponseHeaders) {
		myResponseHeaders = theResponseHeaders;
	}

	/**
	 * @param theResponseMessage
	 *            the responseMessage to set
	 */
	public void setResponseMessage(String theResponseMessage) {
		myResponseMessage = theResponseMessage;
	}

	public void setServiceName(String theServiceName) {
		myServiceName = theServiceName;
	}

	public void setServicePid(long theServicePid) {
		myServicePid = theServicePid;
	}

	public void setServiceVersionId(String theServiceVersionId) {
		myServiceVersionId = theServiceVersionId;
	}

	public void setServiceVersionPid(long theServiceVersionPid) {
		myServiceVersionPid = theServiceVersionPid;
	}

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
