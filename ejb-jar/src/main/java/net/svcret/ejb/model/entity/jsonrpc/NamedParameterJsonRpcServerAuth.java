package net.svcret.ejb.model.entity.jsonrpc;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import net.svcret.admin.shared.model.BaseGServerSecurity;
import net.svcret.admin.shared.model.GNamedParameterJsonRpcServerAuth;
import net.svcret.admin.shared.model.ServerSecurityEnum;
import net.svcret.ejb.invoker.jsonrpc.IJsonReader;
import net.svcret.ejb.invoker.jsonrpc.IJsonWriter;
import net.svcret.ejb.model.entity.BasePersObject;
import net.svcret.ejb.model.entity.PersBaseServerAuth;

@Entity
@DiscriminatorValue("JSONRPC_NAMED_PARM")
public class NamedParameterJsonRpcServerAuth extends PersBaseServerAuth<NamedParameterJsonRpcServerAuth, NamedParameterJsonRpcCredentialGrabber> {
	private static final long serialVersionUID = 1L;

	@Column(name = "JSONRPC_NAMEDPARM_PW", length = 100, nullable = true)
	private String myPasswordParameterName;

	@Column(name = "JSONRPC_NAMEDPARM_UN", length = 100, nullable = true)
	private String myUsernameParameterName;

	public NamedParameterJsonRpcServerAuth() {
	}

	public NamedParameterJsonRpcServerAuth(String theUsernameParameterName, String thePasswordParameterName) {
		myUsernameParameterName = theUsernameParameterName;
		myPasswordParameterName = thePasswordParameterName;
	}

	@Override
	public ServerSecurityEnum getAuthType() {
		return ServerSecurityEnum.JSONRPC_NAMED_PARAMETER;
	}

	@Override
	public Class<? extends NamedParameterJsonRpcCredentialGrabber> getGrabberClass() {
		return NamedParameterJsonRpcCredentialGrabber.class;
	}

	/**
	 * @return the passwordParameterName
	 */
	public String getPasswordParameterName() {
		return myPasswordParameterName;
	}

	/**
	 * @return the usernameParameterName
	 */
	public String getUsernameParameterName() {
		return myUsernameParameterName;
	}

	@Override
	public void merge(BasePersObject theObj) {
		NamedParameterJsonRpcServerAuth auth = (NamedParameterJsonRpcServerAuth) theObj;
		setUsernameParameterName(auth.getUsernameParameterName());
		setPasswordParameterName(auth.getPasswordParameterName());
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

	public NamedParameterJsonRpcCredentialGrabber newCredentialGrabber(IJsonReader theJsonReader, IJsonWriter theJsonWriter) {
		return new NamedParameterJsonRpcCredentialGrabber(this, theJsonReader, theJsonWriter);
	}

	@Override
	protected BaseGServerSecurity createDtoAndPopulateWithTypeSpecificEntries() {
		GNamedParameterJsonRpcServerAuth auth = new GNamedParameterJsonRpcServerAuth();
		auth.setUsernameParameterName(getUsernameParameterName());
		auth.setPasswordParameterName(getPasswordParameterName());
		return auth;
	}

}
