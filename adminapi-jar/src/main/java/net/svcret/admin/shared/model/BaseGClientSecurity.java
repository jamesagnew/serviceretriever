package net.svcret.admin.shared.model;

public abstract class BaseGClientSecurity extends BaseGObject<BaseGClientSecurity> {

	private static final long serialVersionUID = 1L;

	private transient boolean myEditMode;
	private String myPassword;
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
	public void merge(BaseGClientSecurity theObject) {
		setPid(theObject.getPid());
		setUsername(theObject.getUsername());
		setPassword(theObject.getPassword());
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
