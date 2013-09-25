package net.svcret.admin.shared.model;

import javax.xml.bind.annotation.XmlElement;

import net.svcret.admin.shared.enm.ClientSecurityEnum;

public abstract class BaseDtoClientSecurity extends BaseDtoObject {

	private static final long serialVersionUID = 1L;

	private transient boolean myEditMode;

	@XmlElement(name="config_Password")
	private String myPassword;
	
	@XmlElement(name="config_Username")
	private String myUsername;

	/**
	 * @return the password
	 */
	public String getPassword() {
		return myPassword;
	}

	public abstract ClientSecurityEnum getType();

	/**
	 * @return the username
	 */
	public String getUsername() {
		return myUsername;
	}

	/**
	 * @return the editMode
	 */
	public boolean isEditMode() {
		return myEditMode;
	}

	@Override
	public void merge(BaseDtoObject theObject) {
		BaseDtoClientSecurity obj = (BaseDtoClientSecurity)theObject;
		setPid(theObject.getPid());
		setUsername(obj.getUsername());
		setPassword(obj.getPassword());
	}

	/**
	 * @param theEditMode
	 *            the editMode to set
	 */
	public void setEditMode(boolean theEditMode) {
		myEditMode = theEditMode;
	}

	/**
	 * @param thePassword
	 *            the password to set
	 */
	public void setPassword(String thePassword) {
		myPassword = thePassword;
	}

	/**
	 * @param theUsername
	 *            the username to set
	 */
	public void setUsername(String theUsername) {
		myUsername = theUsername;
	}

}
