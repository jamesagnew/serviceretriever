package net.svcret.admin.shared.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

@XmlAccessorType(XmlAccessType.FIELD)
public class DtoLibraryMessage implements Serializable {

	private static final long serialVersionUID = 1L;

	@XmlElement(name="Pid")
	@XmlElementWrapper(name="config_AppliesToServiceVersionPids")
	private Set<Long> myAppliesToServiceVersionPids;
	
	@XmlElement(name="config_ContentType")
	private String myContentType;

	@XmlElement(name="config_Description")
	private String myDescription;

	@XmlElement(name="config_Message")
	private String myMessage;

	@XmlElement(name="runtime_MessageLength")
	private int myMessageLength;

	@XmlElement(name="Pid")
	private Long myPid;

	private transient String myAppliesToSortText;

	public Set<Long> getAppliesToServiceVersionPids() {
		if (myAppliesToServiceVersionPids == null) {
			myAppliesToServiceVersionPids = new HashSet<Long>();
		}
		return myAppliesToServiceVersionPids;
	}

	public String getContentType() {
		return myContentType;
	}

	public String getDescription() {
		return myDescription;
	}

	public String getMessage() {
		return myMessage;
	}

	public int getMessageLength() {
		return myMessageLength;
	}

	public Long getPid() {
		return myPid;
	}

	public void setAppliesToServiceVersionPids(Long... theAppliesToServiceVersionPids) {
		getAppliesToServiceVersionPids().clear();
		for (long l : theAppliesToServiceVersionPids) {
			getAppliesToServiceVersionPids().add(l);
		}
	}

	public void setContentType(String theContentType) {
		myContentType = theContentType;
	}

	public void setDescription(String theDescription) {
		myDescription = theDescription;
	}

	public void setMessage(String theMessage) {
		myMessage = theMessage;
	}

	public void setMessageLength(int theMessageLength) {
		myMessageLength = theMessageLength;
	}

	public void setPid(Long thePid) {
		myPid = thePid;
	}

	public String getAppliesToSortText(GDomainList theDomainList) {
		if (myAppliesToSortText == null) {

			String firstString = "";
			if (getAppliesToServiceVersionPids().isEmpty() == false) {
				for (Long nextPid : getAppliesToServiceVersionPids()) {
					Long nextDomainPid = theDomainList.getDomainPidWithServiceVersion(nextPid);
					DtoDomain nextDomain = theDomainList.getDomainByPid(nextDomainPid);
					Long nextServicePid = theDomainList.getServicePidWithServiceVersion(nextPid);
					GService nextService = nextDomain.getServiceList().getServiceByPid(nextServicePid);
					BaseDtoServiceVersion nextSvcVer = theDomainList.getServiceVersionByPid(nextPid);

					String nextString = nextDomain.getName() + " " + nextService.getName() + " " + nextSvcVer.getId();
					if (firstString.equals("") || firstString.compareTo(nextString) > 0) {
						firstString = nextString;
					}
				}

			}
			myAppliesToSortText = firstString;
		}

		return myAppliesToSortText;
	}

}
