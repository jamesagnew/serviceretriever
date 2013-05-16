package net.svcret.admin.client.ui.config.sec;

import net.svcret.admin.client.AdminPortal;
import net.svcret.admin.shared.model.GAuthenticationHostList;
import net.svcret.admin.shared.model.GNamedParameterJsonRpcServerAuth;

import com.google.gwt.user.client.ui.FlowPanel;

public class JsonRpcNamedParameterServerSecurity extends BaseServerSecurityViewAndEdit<GNamedParameterJsonRpcServerAuth> {

	@Override
	protected String provideName() {
		return AdminPortal.MSGS.jsonRpcNamedParameterServerSecurity_Name();
	}

	@Override
	protected void initViewPanel(int theRow, GNamedParameterJsonRpcServerAuth theObject, FlowPanel thePanelToPopulate, GAuthenticationHostList theAuthenticationHostList) {
		
	}

	@Override
	protected void initEditPanel(int theRow, GNamedParameterJsonRpcServerAuth theObject, FlowPanel thePanelToPopulate, GAuthenticationHostList theAuthenticationHostList) {
		// TODO Auto-generated method stub
		
	}


}
