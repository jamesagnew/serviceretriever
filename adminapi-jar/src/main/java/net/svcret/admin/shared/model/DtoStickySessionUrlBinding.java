package net.svcret.admin.shared.model;

import java.io.Serializable;
import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import net.svcret.admin.shared.util.XmlConstants;

@XmlType(namespace = XmlConstants.DTO_NAMESPACE, name = "StickySessionUrlBinding")
@XmlAccessorType(XmlAccessType.FIELD)
public class DtoStickySessionUrlBinding implements Serializable {

	private static final long serialVersionUID = 1L;

	@XmlAttribute(name = "Created")
	private Date myCreated;

	@XmlAttribute(name = "LastAccessed")
	private Date myLastAccessed;

	private String myRequestingIp;

	@XmlAttribute(name = "SvcVer")
	private Long myServiceVersionPid;

	@XmlAttribute(name = "SessionId")
	private String mySessionId;

	private String myUrlHref;

	private String myUrlId;

	@XmlAttribute(name = "Url")
	private Long myUrlPid;

	public Date getCreated() {
		return myCreated;
	}

	public Date getLastAccessed() {
		return myLastAccessed;
	}

	public String getRequestingIp() {
		return myRequestingIp;
	}

	public Long getServiceVersionPid() {
		return myServiceVersionPid;
	}

	public String getSessionId() {
		return mySessionId;
	}

	public String getUrlHref() {
		return myUrlHref;
	}

	public String getUrlId() {
		return myUrlId;
	}

	public Long getUrlPid() {
		return myUrlPid;
	}

	public void setCreated(Date theCreated) {
		myCreated = theCreated;
	}

	public void setLastAccessed(Date theLastAccessed) {
		myLastAccessed = theLastAccessed;
	}

	public void setRequestingIp(String theRequestingIp) {
		myRequestingIp = theRequestingIp;
	}

	public void setServiceVersionPid(Long thePid) {
		myServiceVersionPid = thePid;
	}

	public void setSessionId(String theSessionId) {
		mySessionId = theSessionId;
	}

	public void setUrlHref(String theUrl) {
		myUrlHref=theUrl;
	}

	public void setUrlId(String theUrlId) {
		myUrlId=theUrlId;
	}

	public void setUrlPid(Long thePid) {
		myUrlPid = thePid;
	}

}
