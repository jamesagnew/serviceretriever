package net.svcret.admin.shared.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import net.svcret.admin.shared.enm.ClientSecurityEnum;
import net.svcret.admin.shared.util.XmlConstants;

@XmlType(namespace=XmlConstants.DTO_NAMESPACE, name="ClientSecurityJsonRpcNamedParameter")
@XmlAccessorType(XmlAccessType.FIELD)
public class DtoClientSecurityJsonRpcNamedParameter extends BaseDtoClientSecurity {

	private static final long serialVersionUID = 1L;

	@XmlElement(name="config_PasswordParameterName")
	private String myPasswordParameterName;

	@XmlElement(name="config_UsernameParamaterName")
	private String myUsernameParameterName;

	public String getPasswordParameterName() {
		return myPasswordParameterName;
	}

	@Override
	public ClientSecurityEnum getType() {
		return ClientSecurityEnum.JSONRPC_NAMPARM;
	}

	public String getUsernameParameterName() {
		return myUsernameParameterName;
	}

	@Override
	public void merge(BaseDtoObject theObject) {
		super.merge(theObject);

		DtoClientSecurityJsonRpcNamedParameter obj = (DtoClientSecurityJsonRpcNamedParameter) theObject;
		setUsernameParameterName(obj.getUsernameParameterName());
		setPasswordParameterName(obj.getPasswordParameterName());
	}
	public void setPasswordParameterName(String thePasswordParameterName) {
		myPasswordParameterName = thePasswordParameterName;
	}

	public void setUsernameParameterName(String theUsernameParameterName) {
		myUsernameParameterName = theUsernameParameterName;
	}

}
