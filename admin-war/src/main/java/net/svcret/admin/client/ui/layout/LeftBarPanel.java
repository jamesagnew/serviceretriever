package net.svcret.admin.client.ui.layout;

import java.util.ArrayList;

import net.svcret.admin.client.nav.NavProcessor;
import net.svcret.admin.client.nav.PagesEnum;

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

		myHttpClientConfigsBtn = configure.addItem("HTTP Client Config", PagesEnum.HCC);
		myAllButtons.add(myHttpClientConfigsBtn);

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
		case DSH:
			myDashboardBtn.addStyleName("leftMenuButtonSelected");
			buttons.remove(myDashboardBtn);
			break;
		case ADD:
		case AD2:
			myAddDomainBtn.addStyleName("leftMenuButtonSelected");
			buttons.remove(myAddDomainBtn);
			break;
		case ASE:
			myAddSvcBtn.addStyleName("leftMenuButtonSelected");
			buttons.remove(myAddSvcBtn);
			break;
		case ASV:
		case AV2:
			myAddSvcVerBtn.addStyleName("leftMenuButtonSelected");
			buttons.remove(myAddSvcVerBtn);
			break;
		case HCC:
			myHttpClientConfigsBtn.addStyleName("leftMenuButtonSelected");
			buttons.remove(myHttpClientConfigsBtn);
			break;
		case EDO:
			break;
		}
		
		for (Hyperlink next : buttons) {
			next.removeStyleName("leftMenuButtonSelected");
		}
		
	}

}
