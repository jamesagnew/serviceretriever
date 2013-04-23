package net.svcret.admin.client.ui.config.domain;

import net.svcret.admin.client.AdminPortal;
import net.svcret.admin.client.nav.NavProcessor;
import net.svcret.admin.client.ui.components.HtmlBr;
import net.svcret.admin.client.ui.components.PButton;
import net.svcret.admin.shared.model.GDomain;
import net.svcret.admin.shared.model.GService;
import net.svcret.admin.shared.model.GServiceList;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;

public class EditDomainServicesPanel extends FlowPanel {

	public static final String SVC_DESC = "Domains have one or more services. A service is a collection of invokeable methods (or a single " + "method) which is accessible at a specific URL.";

	private static final int NUM_COLS = 3;

	private Button myAddServiceButton;
	private GDomain myDomain;
	private Grid myServicesGrid;

	public EditDomainServicesPanel(GDomain theDomain) {
		myDomain = theDomain;

		setStylePrimaryName("mainPanel");

		Label titleLabel = new Label("Services");
		titleLabel.setStyleName("mainPanelTitle");
		add(titleLabel);

		FlowPanel contentPanel = new FlowPanel();
		contentPanel.addStyleName("contentInnerPanel");
		add(contentPanel);

		contentPanel.add(new Label(SVC_DESC));

		myServicesGrid = new Grid();
		contentPanel.add(myServicesGrid);

		myServicesGrid.addStyleName("listTable");
		myServicesGrid.resize(1, NUM_COLS);
		myServicesGrid.setWidget(0, 0, new Label(""));
		myServicesGrid.setWidget(0, 1, new Label("Service ID"));
		myServicesGrid.setWidget(0, 2, new Label("Name"));

		contentPanel.add(new HtmlBr());

		myAddServiceButton = new PButton("Add Service");
		myAddServiceButton.addClickHandler(new AddServiceClickHandler());
		contentPanel.add(myAddServiceButton);

		updateList();
	}

	private void updateList() {
		GServiceList serviceList = myDomain.getServiceList();
		myServicesGrid.setVisible(true);

		myServicesGrid.resize(Math.max(2, serviceList.size() + 1), NUM_COLS);

		if (serviceList.size() == 0) {
			myServicesGrid.setWidget(1, 0, null);
			myServicesGrid.setWidget(1, 1, new Label("No Services"));
			myServicesGrid.setWidget(1, 2, null);
		} else {
			for (int i = 0; i < serviceList.size(); i++) {
				GService next = serviceList.get(i);
				myServicesGrid.setWidget(i + 1, 0, new ActionPanel(next));
				myServicesGrid.setWidget(i + 1, 1, new Label(next.getId(), true));
				myServicesGrid.setWidget(i + 1, 2, new Label(next.getName(), true));
			}
		}

	}

	public class ActionPanel extends FlowPanel {

		private GService mySvc;

		public ActionPanel(GService theSvc) {
			mySvc = theSvc;

			Button editBtn = new PButton(AdminPortal.IMAGES.iconEdit(), AdminPortal.MSGS.actions_Edit());
			add(editBtn);
			
			editBtn.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent theEvent) {
					String token = NavProcessor.getTokenEditService(true, myDomain.getPid(), mySvc.getPid());
					History.newItem(token);
				}
			});
			
		}

	}

	public class AddServiceClickHandler implements ClickHandler {

		@Override
		public void onClick(ClickEvent theEvent) {
			History.newItem(NavProcessor.getTokenAddService(true, myDomain.getPid()));
		}

	}

}
