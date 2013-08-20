package net.svcret.admin.client.ui.config.sec;

import net.svcret.admin.shared.model.DtoClientSecurityHttpBasicAuth;

public class HttpBasicAuthClientSecurityViewAndEdit extends BaseClientSecurityUsernameAndPasswordProvidesViewAndEdit<DtoClientSecurityHttpBasicAuth> {

	@Override
	protected String getModuleName() {
		return "HTTP Basic Auth";
	}

	@Override
	protected String getPassword(DtoClientSecurityHttpBasicAuth theObject) {
		return theObject.getPassword();
	}

	@Override
	protected String getUsername(DtoClientSecurityHttpBasicAuth theObject) {
		return theObject.getUsername();
	}

	@Override
	protected void setPassword(DtoClientSecurityHttpBasicAuth theObject, String theValue) {
		theObject.setPassword(theValue);
	}

	@Override
	protected void setUsername(DtoClientSecurityHttpBasicAuth theObject, String theValue) {
		theObject.setUsername(theValue);
	}



}
