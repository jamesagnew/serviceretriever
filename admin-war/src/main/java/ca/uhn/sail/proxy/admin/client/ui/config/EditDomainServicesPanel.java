package ca.uhn.sail.proxy.admin.client.ui.config;

import ca.uhn.sail.proxy.admin.client.AdminPortal;
import ca.uhn.sail.proxy.admin.client.nav.NavProcessor;
import ca.uhn.sail.proxy.admin.client.ui.components.HtmlBr;
import ca.uhn.sail.proxy.admin.client.ui.components.LoadingSpinner;
import ca.uhn.sail.proxy.admin.shared.model.DomainListUpdateRequest;
import ca.uhn.sail.proxy.admin.shared.model.GDomain;
import ca.uhn.sail.proxy.admin.shared.model.GDomainList;
import ca.uhn.sail.proxy.admin.shared.model.GService;
import ca.uhn.sail.proxy.admin.shared.model.GServiceList;
import ca.uhn.sail.proxy.admin.shared.model.Model;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
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
	private LoadingSpinner mySpinner;

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

		mySpinner = new LoadingSpinner();
		contentPanel.add(mySpinner);

		myServicesGrid = new Grid();
		contentPanel.add(myServicesGrid);

		myServicesGrid.addStyleName("listTable");
		myServicesGrid.resize(1, NUM_COLS);
		myServicesGrid.setWidget(0, 0, new Label(""));
		myServicesGrid.setWidget(0, 1, new Label("Service ID"));
		myServicesGrid.setWidget(0, 2, new Label("Name"));

		contentPanel.add(new HtmlBr());
		
		myAddServiceButton = new Button("Add Service");
		myAddServiceButton.addClickHandler(new AddServiceClickHandler());
		contentPanel.add(myAddServiceButton);

		updateList();

	}

	private void updateList() {
		GServiceList serviceList = myDomain.getServiceList();
		if (serviceList.isInitialized() == false) {
			myServicesGrid.setVisible(false);
			mySpinner.show();
			DomainListUpdateRequest req = new DomainListUpdateRequest();
			req.addDomainToLoad(myDomain.getPid());
			AsyncCallback<GDomainList> callback = new MyLoadDomainCallback();
			AdminPortal.MODEL_SVC.loadDomainList(req, callback);
			return;
		}

		mySpinner.hide();
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

			Button editBtn = new Button("Edit");
			add(editBtn);
			// TODO: implement
		}

	}

	public class AddServiceClickHandler implements ClickHandler {

		public void onClick(ClickEvent theEvent) {
			History.newItem(NavProcessor.getTokenAddService(true, myDomain.getPid()));
		}

	}

	public class MyLoadDomainCallback implements AsyncCallback<GDomainList> {

		public void onFailure(Throwable theCaught) {
			GWT.log("Failed to load!", theCaught);
		}

		public void onSuccess(GDomainList theResult) {
			Model.getInstance().getDomainList().mergeResults(theResult);
			updateList();
		}

	}

}
