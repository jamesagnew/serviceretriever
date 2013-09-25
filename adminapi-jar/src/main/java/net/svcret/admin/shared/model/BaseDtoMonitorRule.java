package net.svcret.admin.shared.model;

import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;

import net.svcret.admin.shared.enm.MonitorRuleTypeEnum;

public abstract class BaseDtoMonitorRule extends BaseDtoObject {

	private static final long serialVersionUID = 1L;

	@XmlElement(name="config_Active")
	private boolean isActive;
	
	@XmlElement(name="config_Name")
	private String myName;
	
	@XmlElement(name="config_NotifyEmailContacts")
	private Set<String> myNotifyEmailContacts;

	public String getName() {
		return myName;
	}

	/**
	 * @return the notifyEmailContacts
	 */
	public Set<String> getNotifyEmailContacts() {
		if (myNotifyEmailContacts == null) {
			myNotifyEmailContacts = new HashSet<String>();
		}
		return myNotifyEmailContacts;
	}

	public abstract MonitorRuleTypeEnum getRuleType();

	public boolean isActive() {
		return isActive;
	}


	public void setActive(boolean theIsActive) {
		isActive = theIsActive;
	}

	/**
	 * @param theName
	 *            the name to set
	 */
	public void setName(String theName) {
		myName = theName;
	}

	public void setNotifyEmailContacts(Set<String> theEmails) {
		if (theEmails==null) {
			throw new NullPointerException();
		}
		
		getNotifyEmailContacts().clear();
		getNotifyEmailContacts().addAll(theEmails);
	}

}
