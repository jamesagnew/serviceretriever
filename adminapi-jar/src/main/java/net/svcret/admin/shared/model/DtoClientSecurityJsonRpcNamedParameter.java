package net.svcret.admin.shared.model;

public class DtoClientSecurityJsonRpcNamedParameter extends BaseGClientSecurity {

	private static final long serialVersionUID = 1L;

	@Override
	public void merge(BaseGClientSecurity theObject) {
		super.merge(theObject);

		DtoClientSecurityJsonRpcNamedParameter obj = (DtoClientSecurityJsonRpcNamedParameter) theObject;
		setUsernameParameterName(obj.getUsernameParameterName());
		setPasswordParameterName(obj.getPasswordParameterName());
	}

	public String getPasswordParameterName() {
		return myPasswordParameterName;
	}

	public void setPasswordParameterName(String thePasswordParameterName) {
		myPasswordParameterName = thePasswordParameterName;
	}

	public String getUsernameParameterName() {
		return myUsernameParameterName;
	}

	public void setUsernameParameterName(String theUsernameParameterName) {
		myUsernameParameterName = theUsernameParameterName;
	}

	private String myPasswordParameterName;
	private String myUsernameParameterName;

	@Override
	public ClientSecurityEnum getType() {
		return ClientSecurityEnum.JSONRPC_NAMPARM;
	}

}
