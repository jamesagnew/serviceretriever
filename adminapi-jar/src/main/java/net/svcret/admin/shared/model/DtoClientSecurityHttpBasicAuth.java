package net.svcret.admin.shared.model;

public class DtoClientSecurityHttpBasicAuth extends BaseGClientSecurity {

	private static final long serialVersionUID = 1L;

	private String myUsername;
	private String myPassword;

	/**
	 * @return the username
	 */
	public String getUsername() {
		return myUsername;
	}

	/**
	 * @param theUsername
	 *            the username to set
	 */
	public void setUsername(String theUsername) {
		myUsername = theUsername;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return myPassword;
	}

	/**
	 * @param thePassword
	 *            the password to set
	 */
	public void setPassword(String thePassword) {
		myPassword = thePassword;
	}

	@Override
	public void merge(BaseGClientSecurity theObject) {
		super.merge(theObject);
		
		DtoClientSecurityHttpBasicAuth obj = (DtoClientSecurityHttpBasicAuth)theObject;
		setUsername(obj.getUsername());
		setPassword(obj.getPassword());
	}

	@Override
	public ClientSecurityEnum getType() {
		return ClientSecurityEnum.HTTP_BASICAUTH;
	}

}
