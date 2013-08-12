package net.svcret.admin.shared.model;

import java.util.HashSet;
import java.util.Set;

import net.svcret.admin.shared.enm.MonitorRuleTypeEnum;

public abstract class BaseGMonitorRule extends BaseGObject<BaseGMonitorRule> {

	private static final long serialVersionUID = 1L;

	private boolean isActive;
	private String myName;
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

	@Override
	public void merge(BaseGMonitorRule theObject) {

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

}
