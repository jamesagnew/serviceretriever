package net.svcret.admin.shared.model;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import net.svcret.admin.shared.enm.AuthorizationOutcomeEnum;
import net.svcret.admin.shared.enm.RecentMessageTypeEnum;
import net.svcret.admin.shared.enm.ResponseTypeEnum;

public class GRecentMessage extends BaseDtoObject {

	private static final long serialVersionUID = 1L;

	private AuthorizationOutcomeEnum myAuthorizationOutcome;
	private String myDomainName;
	private long myDomainPid;
	private String myFailDescription;
	private String myImplementationUrlHref;
	private String myImplementationUrlId;
	private long myImplementationUrlPid;
	private String myMethodName;
	private Long myMethodPid;
	private String myOutcomeDescription;
	private RecentMessageTypeEnum myRecentMessageType;
	private String myRequestActionLine;
	private String myRequestContentType;
	private List<Pair<String>> myRequestHeaders;
	private String myRequestHostIp;
	private String myRequestMessage;
	private String myRequestUsername;
	private Long myRequestUserPid;
	private String myResponseContentType;
	private List<Pair<String>> myResponseHeaders;
	private String myResponseMessage;
	private ResponseTypeEnum myResponseType;
	private String myServiceName;
	private long myServicePid;
	private String myServiceVersionId;
	private long myServiceVersionPid;
	private long myTransactionMillis;
	private Date myTransactionTime;
	
	public GRecentMessage() {
		super();
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

//	public GRecentMessage(long thePid, Date theDate, String theRequestHostIp, String theRequestMessage, String theResponseMessage, String theRequestActionLine, List<Pair<String>> theRequestHeaders, List<Pair<String>> theResponseHeaders, String theRequestContentType, String theResponseContentType) {
//		setPid(thePid);
//		myTransactionTime = theDate;
//		myRequestHostIp = theRequestHostIp;
//		myRequestMessage = theRequestMessage;
//		myResponseMessage = theResponseMessage;
//		myRequestHeaders = theRequestHeaders;
//		myResponseHeaders = theResponseHeaders;
//		myRequestContentType = theRequestContentType;
//		myResponseContentType = theResponseContentType;
//		myRequestActionLine = theRequestActionLine;
//	}

	public long getDomainPid() {
		return myDomainPid;
	}

	public String getFailDescription() {
		return myFailDescription;
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

	public String getMethodName() {
		return myMethodName;
	}

	public Long getMethodPid() {
		return myMethodPid;
	}

	public String getOutcomeDescription() {
		return myOutcomeDescription;
	}

	public RecentMessageTypeEnum getRecentMessageType() {
		return myRecentMessageType;
	}

	public String getRequestActionLine() {
		return myRequestActionLine;
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
		if (myRequestHeaders == null) {
			return Collections.emptyList();
		}
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
		if (myResponseHeaders == null) {
			return Collections.emptyList();
		}
		return myResponseHeaders;
	}

	/**
	 * @return the responseMessage
	 */
	public String getResponseMessage() {
		return myResponseMessage;
	}

	public ResponseTypeEnum getResponseType() {
		return myResponseType;
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

	public void setAuthorizationOutcome(AuthorizationOutcomeEnum theAuthorizationOutcome) {
		myAuthorizationOutcome = theAuthorizationOutcome;
	}

	public void setDomainName(String theDomainName) {
		myDomainName = theDomainName;
	}

	public void setDomainPid(long theDomainPid) {
		myDomainPid = theDomainPid;
	}

	public void setFailDescription(String theFailDescription) {
		myFailDescription=theFailDescription;
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

	public void setMethodName(String theMethodName) {
		myMethodName = theMethodName;
	}

	public void setMethodPid(Long thePid) {
		myMethodPid = thePid;
	}

	public void setOutcomeDescription(String theOutcomeDescription) {
		myOutcomeDescription = theOutcomeDescription;
	}

	public void setRecentMessageType(RecentMessageTypeEnum theRecentMessageType) {
		myRecentMessageType = theRecentMessageType;
	}

	public void setRequestActionLine(String theRequestActionLine) {
		myRequestActionLine = theRequestActionLine;
	}

	/**
	 * @param theRequestContentType
	 *            the requestContentType to set
	 */
	public void setRequestContentType(String theRequestContentType) {
		myRequestContentType = theRequestContentType;
	}

	/**
	 * @param theRequestHeaders
	 *            the requestHeaders to set
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
	 * @param theResponseContentType
	 *            the responseContentType to set
	 */
	public void setResponseContentType(String theResponseContentType) {
		myResponseContentType = theResponseContentType;
	}

	/**
	 * @param theResponseHeaders
	 *            the responseHeaders to set
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

	public void setResponseType(ResponseTypeEnum theResponseType) {
		myResponseType = theResponseType;
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
