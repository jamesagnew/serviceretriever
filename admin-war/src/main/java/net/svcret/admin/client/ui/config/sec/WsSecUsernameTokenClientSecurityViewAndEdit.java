package net.svcret.admin.client.ui.config.sec;

import net.svcret.admin.shared.model.GWsSecUsernameTokenClientSecurity;

public class WsSecUsernameTokenClientSecurityViewAndEdit extends BaseClientSecurityUsernameAndPasswordProvidesViewAndEdit<GWsSecUsernameTokenClientSecurity> {

	@Override
	protected String getModuleName() {
		return "WS-Security UsernameToken";
	}

	@Override
	protected String getPassword(GWsSecUsernameTokenClientSecurity theObject) {
		return theObject.getPassword();
	}

	@Override
	protected String getUsername(GWsSecUsernameTokenClientSecurity theObject) {
		return theObject.getUsername()
				;
	}

	@Override
	protected void setPassword(GWsSecUsernameTokenClientSecurity theObject, String theValue) {
		theObject.setPassword(theValue);
	}

	@Override
	protected void setUsername(GWsSecUsernameTokenClientSecurity theObject, String theValue) {
		theObject.setUsername(theValue);
	}

}
