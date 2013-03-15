package ca.uhn.sail.proxy.admin.client.ui.config;

import ca.uhn.sail.proxy.admin.client.AdminPortal;
import ca.uhn.sail.proxy.admin.client.ui.components.LoadingSpinner;
import ca.uhn.sail.proxy.admin.shared.model.BaseGListenable;
import ca.uhn.sail.proxy.admin.shared.model.GDomain;
import ca.uhn.sail.proxy.admin.shared.model.GService;
import ca.uhn.sail.proxy.admin.shared.model.GServiceList;
import ca.uhn.sail.proxy.admin.shared.model.IListener;
import ca.uhn.sail.proxy.admin.shared.model.Model;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;

public class AddServicePanel extends FlowPanel {

	private ListBox myDomainListBox;
	private String myDomainPid;
	private FlowPanel myDomainSelectorPanel;
	private EditServiceBasicPropertiesPanel myServicePropertiesPanel;
	private LoadingSpinner mySpinner;
	
	public AddServicePanel(String theDomainPid) {
		myDomainPid = theDomainPid;
		
		setStylePrimaryName("mainPanel");

		Label titleLabel = new Label("Services");
		titleLabel.setStyleName("mainPanelTitle");
		add(titleLabel);

		FlowPanel contentPanel = new FlowPanel();
		contentPanel.addStyleName("contentInnerPanel");
		add(contentPanel);

		contentPanel.add(new Label(EditDomainServicesPanel.SVC_DESC));

		mySpinner = new LoadingSpinner();
		contentPanel.add(mySpinner);

		/*
		 * Domain selector
		 */
		
		myDomainSelectorPanel = new FlowPanel();
		myDomainSelectorPanel.setVisible(false);
		contentPanel.add(myDomainSelectorPanel);
		
		myDomainListBox = new ListBox(false);
		myDomainSelectorPanel.add(myDomainListBox);
		
		/*
		 * Service properties 
		 */
		
		ClickHandler addHandler = new MyAddHandler();
		myServicePropertiesPanel = new EditServiceBasicPropertiesPanel("", "", true, "Add", addHandler);
		myServicePropertiesPanel.setVisible(false);
		contentPanel.add(myServicePropertiesPanel);
		
		
		
		
		if (Model.getInstance().getDomainList().isInitialized()) {
			updateDomains();
		} else {
			mySpinner.show();

			Model.getInstance().getDomainList().addListener(new MyDomainListListener());
		}
		
	}

	private void updateDomains() {
		mySpinner.hideCompletely();
		myDomainSelectorPanel.setVisible(true);
		myServicePropertiesPanel.setVisible(true);
		
		myDomainListBox.clear();
		for (GDomain next : Model.getInstance().getDomainList()) {
			String nextValue = "" + next.getPid();
			myDomainListBox.addItem(next.getName(), nextValue);
			if (nextValue.equals(myDomainPid)) {
				myDomainListBox.setSelectedIndex(myDomainListBox.getItemCount() - 1);
			}
		}
		
	}

	public class MyAddHandler implements ClickHandler {

		public void onClick(ClickEvent theEvent) {
			String domainPidStr = myDomainListBox.getValue(myDomainListBox.getSelectedIndex());
			long domainPid = Long.parseLong(domainPidStr);
			
			if (myServicePropertiesPanel.validateValues()) {
				String id = myServicePropertiesPanel.getId();
				String name = myServicePropertiesPanel.getName();
				boolean active = myServicePropertiesPanel.isActive();
				AsyncCallback<GServiceList> callback = Model.getInstance().getAddOrEditServiceCallback(domainPid);
				AdminPortal.MODEL_SVC.addService(domainPid, id, name, active, callback);
			}
			
		}

	}

	public class MyDomainListListener implements IListener {

		public void changed(BaseGListenable<?> theListenable) {
			updateDomains();
		}

		public void loadingStarted(BaseGListenable<?> theListenable) {
			// ignore
		}

	}
	
}
