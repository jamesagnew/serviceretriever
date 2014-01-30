package net.svcret.admin.shared.model;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

import net.svcret.admin.shared.enm.ResponseTypeEnum;

@XmlAccessorType(XmlAccessType.FIELD)
public class BaseDtoSavedTransaction extends BaseDtoObject {

	private static final long serialVersionUID = 1L;

	@XmlElement(name = "FailDescription", nillable = true)
	private String myFailDescription;
	@XmlElement(name = "ImplementationUrlHref")
	private String myImplementationUrlHref;
	@XmlElement(name = "ImplementationUrlId")
	private String myImplementationUrlId;
	@XmlElement(name = "ImplementationUrlPid")
	private long myImplementationUrlPid;
	@XmlElement(name = "MethodName")
	private String myMethodName;
	@XmlElement(name = "MethodPid")
	private Long myMethodPid;
	@XmlElement(name = "OutcomeDescription")
	private String myOutcomeDescription;
	@XmlElement(name = "RequestActionLine")
	private String myRequestActionLine;
	@XmlElement(name = "RequestContentType")
	private String myRequestContentType;
	@XmlElementWrapper(name = "RequestHeaders")
	@XmlElement(name = "Header")
	private List<Pair<String>> myRequestHeaders;
	@XmlElement(name = "RequestMessage")
	private String myRequestMessage;
	@XmlElement(name = "RequestUsername")
	private String myRequestUsername;
	@XmlElement(name = "RequestUserPid")
	private Long myRequestUserPid;
	@XmlElement(name = "ResponseContentType")
	private String myResponseContentType;
	@XmlElementWrapper(name = "ResponseHeaders")
	@XmlElement(name = "Header")
	private List<Pair<String>> myResponseHeaders;
	@XmlElement(name = "ResponseMessage")
	private String myResponseMessage;
	@XmlElement(name = "ResponseType")
	private ResponseTypeEnum myResponseType;
	@XmlElement(name = "StatusLine")
	private String myResponseStatusLine;
	@XmlElement(name = "TransactionMillis")
	private long myTransactionMillis;
	@XmlElement(name = "TransactionTime")
	private Date myTransactionTime;

	public BaseDtoSavedTransaction() {
		super();
	}

	// public GRecentMessage(long thePid, Date theDate, String theRequestHostIp,
	// String theRequestMessage, String theResponseMessage, String
	// theRequestActionLine, List<Pair<String>> theRequestHeaders,
	// List<Pair<String>> theResponseHeaders, String theRequestContentType,
	// String theResponseContentType) {
	// setPid(thePid);
	// myTransactionTime = theDate;
	// myRequestHostIp = theRequestHostIp;
	// myRequestMessage = theRequestMessage;
	// myResponseMessage = theResponseMessage;
	// myRequestHeaders = theRequestHeaders;
	// myResponseHeaders = theResponseHeaders;
	// myRequestContentType = theRequestContentType;
	// myResponseContentType = theResponseContentType;
	// myRequestActionLine = theRequestActionLine;
	// }

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

	public String getResponseStatusLine() {
		return myResponseStatusLine;
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

	public void setFailDescription(String theFailDescription) {
		myFailDescription = theFailDescription;
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

	public void setResponseStatusLine(String theStatusLine) {
		myResponseStatusLine = theStatusLine;
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
