package net.svcret.admin.shared.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import net.svcret.admin.shared.util.XmlConstants;

@XmlType(namespace=XmlConstants.DTO_NAMESPACE, name="StickySessionUrlBinding")
@XmlAccessorType(XmlAccessType.FIELD)
public class DtoStickySessionUrlBinding implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@XmlAttribute(name="SvcVer")
	private Long myServiceVersionPid;
	
	@XmlAttribute(name="SessionId")
	private String mySessionId;

	@XmlAttribute(name="Url")
	private Long myUrlPid;

	public Long getServiceVersionPid() {
		return myServiceVersionPid;
	}

	public String getSessionId() {
		return mySessionId;
	}

	public Long getUrlPid() {
		return myUrlPid;
	}

	public void setServiceVersionPid(Long thePid) {
		myServiceVersionPid=thePid;
	}

	public void setSessionId(String theSessionId) {
		mySessionId=theSessionId;
	}

	public void setUrlPid(Long thePid) {
		myUrlPid=thePid;
	}

}
