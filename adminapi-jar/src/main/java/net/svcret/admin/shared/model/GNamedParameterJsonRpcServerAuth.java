package net.svcret.admin.shared.model;


public class GNamedParameterJsonRpcServerAuth extends BaseGServerSecurity {

	private static final long serialVersionUID = 1L;

	private String myPasswordParameterName;
	private String myUsernameParameterName;

	/**
	 * @return the passwordParameterName
	 */
	public String getPasswordParameterName() {
		return myPasswordParameterName;
	}

	@Override
	public ServerSecurityEnum getType() {
		return ServerSecurityEnum.JSONRPC_NAMED_PARAMETER;
	}

	/**
	 * @return the usernameParameterName
	 */
	public String getUsernameParameterName() {
		return myUsernameParameterName;
	}

	/**
	 * @param thePasswordParameterName
	 *            the passwordParameterName to set
	 */
	public void setPasswordParameterName(String thePasswordParameterName) {
		myPasswordParameterName = thePasswordParameterName;
	}

	/**
	 * @param theUsernameParameterName
	 *            the usernameParameterName to set
	 */
	public void setUsernameParameterName(String theUsernameParameterName) {
		myUsernameParameterName = theUsernameParameterName;
	}

}
