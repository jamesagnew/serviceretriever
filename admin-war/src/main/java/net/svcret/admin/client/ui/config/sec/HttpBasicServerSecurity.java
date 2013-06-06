package net.svcret.admin.client.ui.config.sec;

import net.svcret.admin.client.AdminPortal;
import net.svcret.admin.client.ui.components.TwoColumnGrid;
import net.svcret.admin.shared.model.GAuthenticationHostList;
import net.svcret.admin.shared.model.GHttpBasicAuthServerSecurity;

public class HttpBasicServerSecurity extends BaseServerSecurityViewAndEdit<GHttpBasicAuthServerSecurity> {


	@Override
	protected String provideName() {
		return AdminPortal.MSGS.httpBasicAuthServerSecurity_Name();
	}

	@Override
	protected void initViewPanel(int theRow, GHttpBasicAuthServerSecurity theObject, TwoColumnGrid thePanelToPopulate, GAuthenticationHostList theAuthenticationHostList) {
		//nothing
	}

	@Override
	protected void initEditPanel(int theRow, GHttpBasicAuthServerSecurity theObject, TwoColumnGrid thePanelToPopulate, GAuthenticationHostList theAuthenticationHostList) {
		//nothing
	}


}
