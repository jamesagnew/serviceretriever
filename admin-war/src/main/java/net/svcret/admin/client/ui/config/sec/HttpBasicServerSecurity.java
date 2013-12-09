package net.svcret.admin.client.ui.config.sec;

import net.svcret.admin.client.AdminPortal;
import net.svcret.admin.client.ui.components.TwoColumnGrid;
import net.svcret.admin.shared.model.DtoAuthenticationHostList;
import net.svcret.admin.shared.model.DtoHttpBasicAuthServerSecurity;

public class HttpBasicServerSecurity extends BaseServerSecurityViewAndEdit<DtoHttpBasicAuthServerSecurity> {


	@Override
	protected String provideName() {
		return AdminPortal.MSGS.httpBasicAuthServerSecurity_Name();
	}

	@Override
	protected void initViewPanel(int theRow, DtoHttpBasicAuthServerSecurity theObject, TwoColumnGrid thePanelToPopulate, DtoAuthenticationHostList theAuthenticationHostList) {
		//nothing
	}

	@Override
	protected void initEditPanel(int theRow, DtoHttpBasicAuthServerSecurity theObject, TwoColumnGrid thePanelToPopulate, DtoAuthenticationHostList theAuthenticationHostList) {
		//nothing
	}


}
