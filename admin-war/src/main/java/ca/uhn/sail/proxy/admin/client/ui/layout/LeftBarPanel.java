package ca.uhn.sail.proxy.admin.client.ui.layout;

import java.util.ArrayList;

import ca.uhn.sail.proxy.admin.client.nav.NavProcessor;
import ca.uhn.sail.proxy.admin.client.nav.PagesEnum;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
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
		
		LeftMenuComponent configure = new LeftMenuComponent("Configure");
		add(configure);

		myAddDomainBtn = configure.addItem("Add Domain", PagesEnum.ADD);
		myAllButtons.add(myAddDomainBtn);
		
		myAddSvcBtn = configure.addItem("Add Service", PagesEnum.ASE);
		myAllButtons.add(myAddSvcBtn);

		myAddSvcVerBtn = configure.addItem("Add Service Version", PagesEnum.ASV);
		myAllButtons.add(myAddSvcVerBtn);

		updateStyles();
		
		History.addValueChangeHandler(new ValueChangeHandler<String>() {
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
			myAddSvcVerBtn.addStyleName("leftMenuButtonSelected");
			buttons.remove(myAddSvcVerBtn);
			break;
		case EDO:
			break;
		}
		
		for (Hyperlink next : buttons) {
			next.removeStyleName("leftMenuButtonSelected");
		}
		
	}

	public class MyAddDomainHandler implements ClickHandler {

		public void onClick(ClickEvent theEvent) {
			
		}

	}

	public class MyAddServiceHandler implements ClickHandler {

		public void onClick(ClickEvent theEvent) {
			
		}

	}

	public class MyServiceDashboardHandler implements ClickHandler {

		public void onClick(ClickEvent theEvent) {
			NavProcessor.navRoot(PagesEnum.DSH);
		}

	}
}
