package net.svcret.admin.client.ui.layout;

import static net.svcret.admin.client.AdminPortal.*;

import java.util.ArrayList;

import net.svcret.admin.client.nav.NavProcessor;
import net.svcret.admin.client.nav.PagesEnum;
import net.svcret.admin.client.ui.components.CssConstants;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Hyperlink;

public class LeftBarPanel extends FlowPanel {

	private Hyperlink myDashboardBtn;
	private Hyperlink myAddDomainBtn;
	private Hyperlink myAddSvcBtn;
	private ArrayList<Hyperlink> myAllButtons;
	private Hyperlink myAddSvcVerBtn;
	private Hyperlink myHttpClientConfigsBtn;
	private Hyperlink myAuthenticationHostsBtn;
	private Hyperlink myEditUsersBtn;
	private Hyperlink mySvcCatalogBtn;
	private Hyperlink myProxyConfigBtn;

	public LeftBarPanel() {
		setStylePrimaryName("outerLayoutLeftBar");
		
		myAllButtons = new ArrayList<Hyperlink>();
		
		/*
		 * Dashboard submenu
		 */
		
		LeftMenuComponent dashboard = new LeftMenuComponent("Dashboard");
		add(dashboard);
		
		myDashboardBtn = dashboard.addItem("Service Dashboard", PagesEnum.DSH);
		myAllButtons.add(myDashboardBtn);

		mySvcCatalogBtn = dashboard.addItem("Service Catalog", PagesEnum.SEC);
		myAllButtons.add(mySvcCatalogBtn);

		/*
		 * Configure Subment
		 */
		
		LeftMenuComponent serviceRegistry = new LeftMenuComponent("Service Registry");
		add(serviceRegistry);

		myAddDomainBtn = serviceRegistry.addItem("Add Domain", PagesEnum.ADD);
		myAllButtons.add(myAddDomainBtn);
		
		myAddSvcBtn = serviceRegistry.addItem("Add Service", PagesEnum.ASE);
		myAllButtons.add(myAddSvcBtn);

		myAddSvcVerBtn = serviceRegistry.addItem("Add Service Version", PagesEnum.ASV);
		myAllButtons.add(myAddSvcVerBtn);

		/*
		 * Configuration
		 */
		
		LeftMenuComponent configure = new LeftMenuComponent("Configuration");
		add(configure);

		myProxyConfigBtn = configure.addItem(MSGS.leftPanel_Configuration(), PagesEnum.CFG);
		myAllButtons.add(myProxyConfigBtn);

		myHttpClientConfigsBtn = configure.addItem(MSGS.leftPanel_HttpClients(), PagesEnum.HCC);
		myAllButtons.add(myHttpClientConfigsBtn);
		
		myAuthenticationHostsBtn = configure.addItem(MSGS.leftPanel_AuthenticationHosts(), PagesEnum.AHL);
		myAllButtons.add(myAuthenticationHostsBtn);
		
		myEditUsersBtn = configure.addItem(MSGS.leftPanel_EditUsers(), PagesEnum.EUL);
		myAllButtons.add(myEditUsersBtn);
		
		updateStyles();
		
		History.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> theEvent) {
				updateStyles();
			}
		});
		
	}

	private void updateStyles() {
		PagesEnum current = NavProcessor.getCurrentPage();
		
		ArrayList<Hyperlink> buttons = new ArrayList<Hyperlink>(myAllButtons);
		
		switch (current) {
		case AHL:
			myAuthenticationHostsBtn.addStyleName(CssConstants.LEFTBAR_LINK_SELECTED);
			buttons.remove(myAuthenticationHostsBtn);
			break;
		case DSH:
			myDashboardBtn.addStyleName(CssConstants.LEFTBAR_LINK_SELECTED);
			buttons.remove(myDashboardBtn);
			break;
		case ADD:
		case AD2:
			myAddDomainBtn.addStyleName(CssConstants.LEFTBAR_LINK_SELECTED);
			buttons.remove(myAddDomainBtn);
			break;
		case ASE:
			myAddSvcBtn.addStyleName(CssConstants.LEFTBAR_LINK_SELECTED);
			buttons.remove(myAddSvcBtn);
			break;
		case ASV:
		case AV2:
			myAddSvcVerBtn.addStyleName(CssConstants.LEFTBAR_LINK_SELECTED);
			buttons.remove(myAddSvcVerBtn);
			break;
		case CFG:
			myProxyConfigBtn.addStyleName(CssConstants.LEFTBAR_LINK_SELECTED);
			buttons.remove(myProxyConfigBtn);
			break;
		case HCC:
			myHttpClientConfigsBtn.addStyleName(CssConstants.LEFTBAR_LINK_SELECTED);
			buttons.remove(myHttpClientConfigsBtn);
			break;
		case EDO:
			break;
		case EUL:
		case EDU:
			myEditUsersBtn.addStyleName(CssConstants.LEFTBAR_LINK_SELECTED);
			buttons.remove(myEditUsersBtn);
			break;
		case SEC:
			mySvcCatalogBtn.addStyleName(CssConstants.LEFTBAR_LINK_SELECTED);
			buttons.remove(mySvcCatalogBtn);
		}
		
		for (Hyperlink next : buttons) {
			next.removeStyleName(CssConstants.LEFTBAR_LINK_SELECTED);
		}
		
	}

}
