package net.svcret.admin.client.ui.config.sec;

import net.svcret.admin.client.AdminPortal;
import net.svcret.admin.client.ui.components.HtmlBr;
import net.svcret.admin.client.ui.components.TwoColumnGrid;
import net.svcret.admin.shared.IAsyncLoadCallback;
import net.svcret.admin.shared.Model;
import net.svcret.admin.shared.model.BaseGAuthHost;
import net.svcret.admin.shared.model.GAuthenticationHostList;
import net.svcret.admin.shared.model.GWsSecServerSecurity;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;

public class SoapWsSecUsernameTokenServerSecurity extends BaseServerSecurityViewAndEdit<GWsSecServerSecurity> {

	@Override
	protected void initViewPanel(int theRow, GWsSecServerSecurity theObject, FlowPanel thePanelToPopulate, GAuthenticationHostList theAuthenticationHostList) {
		//nothing
	}

	@Override
	protected void initEditPanel(int theRow, GWsSecServerSecurity theObject, FlowPanel thePanelToPopulate, GAuthenticationHostList theAuthenticationHostList) {
		//nothing
	}

	@Override
	protected String provideName() {
		return AdminPortal.MSGS.wsSecServerSecurity_Name();
	}


}
