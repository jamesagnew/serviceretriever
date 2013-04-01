package net.svcret.admin.client.ui.config;

import net.svcret.admin.client.AdminPortal;
import net.svcret.admin.client.ui.components.LoadingSpinner;
import net.svcret.admin.shared.IAsyncLoadCallback;
import net.svcret.admin.shared.Model;
import net.svcret.admin.shared.model.BaseGListenable;
import net.svcret.admin.shared.model.GDomain;
import net.svcret.admin.shared.model.GDomainList;
import net.svcret.admin.shared.model.GService;
import net.svcret.admin.shared.model.IListener;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;

public class AddServicePanel extends FlowPanel {

	private ListBox myDomainListBox;
	private Long myDomainPid;
	private FlowPanel myDomainSelectorPanel;
	private EditServiceBasicPropertiesPanel myServicePropertiesPanel;
	private LoadingSpinner mySpinner;
	private GDomainList myDomainList;
	
	public AddServicePanel(Long theDomainPid) {
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
		
		mySpinner.show();
		Model.getInstance().loadDomainList(new IAsyncLoadCallback<GDomainList>() {
			@Override
			public void onSuccess(GDomainList theResult) {
				myDomainList = theResult;
				mySpinner.hideCompletely();
				updateDomains();
			}
		});
		
	}

	private void updateDomains() {
		mySpinner.hideCompletely();
		myDomainSelectorPanel.setVisible(true);
		myServicePropertiesPanel.setVisible(true);
		
		myDomainListBox.clear();
		for (GDomain next : myDomainList) {
			String nextValue = Long.toString(next.getPid());
			myDomainListBox.addItem(next.getName(), nextValue);
			if (Long.valueOf(next.getPid()).equals(myDomainPid)) {
				myDomainListBox.setSelectedIndex(myDomainListBox.getItemCount() - 1);
			}
		}
		
	}

	public class MyAddHandler implements ClickHandler {

		@Override
		public void onClick(ClickEvent theEvent) {
			String domainPidStr = myDomainListBox.getValue(myDomainListBox.getSelectedIndex());
			final long domainPid = Long.parseLong(domainPidStr);
			
			if (myServicePropertiesPanel.validateValues()) {
				String id = myServicePropertiesPanel.getId();
				String name = myServicePropertiesPanel.getName();
				boolean active = myServicePropertiesPanel.isActive();
				AsyncCallback<GService> callback = new AsyncCallback<GService>() {
					@Override
					public void onFailure(Throwable theCaught) {
						Model.handleFailure(theCaught);
					}
					@Override
					public void onSuccess(GService theResult) {
						Model.getInstance().addService(domainPid, theResult);
						// TODO: move to a step 2 panel
					}
				};
				AdminPortal.MODEL_SVC.addService(domainPid, id, name, active, callback);
			}
			
		}

	}

	public class MyDomainListListener implements IListener {

		@Override
		public void changed(BaseGListenable<?> theListenable) {
			updateDomains();
		}

		@Override
		public void loadingStarted(BaseGListenable<?> theListenable) {
			// ignore
		}

	}
	
}
