package net.svcret.admin.client.ui.config.service;

import net.svcret.admin.client.AdminPortal;
import net.svcret.admin.client.nav.NavProcessor;
import net.svcret.admin.client.ui.components.CssConstants;
import net.svcret.admin.client.ui.components.HtmlBr;
import net.svcret.admin.client.ui.components.PButton;
import net.svcret.admin.shared.model.BaseDtoServiceVersion;
import net.svcret.admin.shared.model.GDomain;
import net.svcret.admin.shared.model.GService;
import net.svcret.admin.shared.model.GServiceVersionList;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;

public class EditServiceVersionsPanel extends FlowPanel {

	private static final int NUM_COLS = 3;

	private Button myAddServiceButton;
	private GService myService;
	private Grid myServicesGrid;
	private GDomain myDomain;

	public EditServiceVersionsPanel(GDomain theDomain, GService theService) {
		myDomain = theDomain;
		myService = theService;

		add(new Label("A service must have at least one version defined in order to be invoked. " +
				"The service version defines the protocol, addressing, security, and other " +
				"configuration properties."));
		
		
		myServicesGrid = new Grid();
		add(myServicesGrid);

		myServicesGrid.addStyleName(CssConstants.PROPERTY_TABLE);
		myServicesGrid.resize(1, NUM_COLS);
		myServicesGrid.setWidget(0, 0, new Label(""));
		myServicesGrid.setWidget(0, 1, new Label("Version"));
		myServicesGrid.setWidget(0, 2, new Label("Protocol"));

		add(new HtmlBr());

		myAddServiceButton = new PButton(AdminPortal.IMAGES.iconAdd(), "Add Version");
		myAddServiceButton.addClickHandler(new AddServiceClickHandler());
		add(myAddServiceButton);

		updateList();
	}

	private void updateList() {
		GServiceVersionList versionList = myService.getVersionList();
		myServicesGrid.setVisible(true);

		myServicesGrid.resize(Math.max(2, versionList.size() + 1), NUM_COLS);

		if (versionList.size() == 0) {
			myServicesGrid.setWidget(1, 0, null);
			myServicesGrid.setWidget(1, 1, new Label("No Services"));
			myServicesGrid.setWidget(1, 2, null);
		} else {
			for (int i = 0; i < versionList.size(); i++) {
				BaseDtoServiceVersion next = versionList.get(i);
				myServicesGrid.setWidget(i + 1, 0, new ActionPanel(next));
				myServicesGrid.setWidget(i + 1, 1, new Label(next.getId(), true));
				myServicesGrid.setWidget(i + 1, 2, new Label(next.getProtocol().getNiceName(), true));
			}
		}

	}

	public class ActionPanel extends FlowPanel {

		private BaseDtoServiceVersion myVersion;

		public ActionPanel(BaseDtoServiceVersion theVersion) {
			myVersion = theVersion;

			Button editBtn = new PButton(AdminPortal.IMAGES.iconEdit(), AdminPortal.MSGS.actions_Edit());
			add(editBtn);
			
			editBtn.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent theEvent) {
					String token = NavProcessor.getTokenEditServiceVersion(myVersion.getPid());
					History.newItem(token);
				}
			});
			
		}

	}

	public class AddServiceClickHandler implements ClickHandler {

		@Override
		public void onClick(ClickEvent theEvent) {
			History.newItem(NavProcessor.getTokenAddServiceVersion(myDomain.getPid(), myService.getPid(), null));
		}

	}

}
