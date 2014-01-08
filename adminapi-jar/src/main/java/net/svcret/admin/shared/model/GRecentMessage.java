package net.svcret.admin.shared.model;

import javax.xml.bind.annotation.XmlElement;

import net.svcret.admin.shared.enm.AuthorizationOutcomeEnum;
import net.svcret.admin.shared.enm.RecentMessageTypeEnum;

public class GRecentMessage extends BaseDtoSavedTransaction {

	private static final long serialVersionUID = 1L;

	private AuthorizationOutcomeEnum myAuthorizationOutcome;
	private String myRequestHostIp;

	@XmlElement(name = "RecentMessageType")
	private RecentMessageTypeEnum myRecentMessageType;
	@XmlElement(name = "DomainName")
	private String myDomainName;
	@XmlElement(name = "DomainPid")
	private long myDomainPid;
	@XmlElement(name = "ServiceName")
	private String myServiceName;
	@XmlElement(name = "ServicePid")
	private long myServicePid;
	@XmlElement(name = "ServiceVersionId")
	private String myServiceVersionId;
	@XmlElement(name = "ServiceVersionPid")
	private long myServiceVersionPid;

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

	public long getDomainPid() {
		return myDomainPid;
	}

	public RecentMessageTypeEnum getRecentMessageType() {
		return myRecentMessageType;
	}

	public String getRequestHostIp() {
		return myRequestHostIp;
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

	public void setAuthorizationOutcome(AuthorizationOutcomeEnum theAuthorizationOutcome) {
		myAuthorizationOutcome = theAuthorizationOutcome;
	}

	public void setDomainName(String theDomainName) {
		myDomainName = theDomainName;
	}

	public void setDomainPid(long theDomainPid) {
		myDomainPid = theDomainPid;
	}

	public void setRecentMessageType(RecentMessageTypeEnum theRecentMessageType) {
		myRecentMessageType = theRecentMessageType;
	}

	public void setRequestHostIp(String theRequestHostIp) {
		myRequestHostIp = theRequestHostIp;
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

}
