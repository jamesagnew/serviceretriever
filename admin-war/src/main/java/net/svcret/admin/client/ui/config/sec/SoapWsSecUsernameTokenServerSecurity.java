package net.svcret.admin.client.ui.config.sec;

import net.svcret.admin.client.AdminPortal;
import net.svcret.admin.client.ui.components.TwoColumnGrid;
import net.svcret.admin.shared.model.GAuthenticationHostList;
import net.svcret.admin.shared.model.GWsSecServerSecurity;

public class SoapWsSecUsernameTokenServerSecurity extends BaseServerSecurityViewAndEdit<GWsSecServerSecurity> {


	@Override
	protected String provideName() {
		return AdminPortal.MSGS.wsSecServerSecurity_Name();
	}

	@Override
	protected void initViewPanel(int theRow, GWsSecServerSecurity theObject, TwoColumnGrid thePanelToPopulate, GAuthenticationHostList theAuthenticationHostList) {
		//nothing
	}

	@Override
	protected void initEditPanel(int theRow, GWsSecServerSecurity theObject, TwoColumnGrid thePanelToPopulate, GAuthenticationHostList theAuthenticationHostList) {
		//nothing
	}


}
