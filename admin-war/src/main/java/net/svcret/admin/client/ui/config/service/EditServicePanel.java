package net.svcret.admin.client.ui.config.service;

import net.svcret.admin.client.AdminPortal;
import net.svcret.admin.client.nav.NavProcessor;
import net.svcret.admin.client.ui.components.LoadingSpinner;
import net.svcret.admin.client.ui.config.domain.EditDomainServicesPanel;
import net.svcret.admin.shared.IAsyncLoadCallback;
import net.svcret.admin.shared.Model;
import net.svcret.admin.shared.model.GDomain;
import net.svcret.admin.shared.model.GDomainList;
import net.svcret.admin.shared.model.GService;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

public class EditServicePanel extends FlowPanel {
	private EditServiceBasicPropertiesPanel myEditServiceBasicPropertiesPanel;

	public EditServicePanel(final long theDomainPid, final long theServicePid) {
		setStylePrimaryName("mainPanel");

		Label titleLabel = new Label("Edit Service");
		titleLabel.setStyleName("mainPanelTitle");
		add(titleLabel);

		final FlowPanel contentPanel = new FlowPanel();
		contentPanel.addStyleName("contentInnerPanel");
		add(contentPanel);

		contentPanel.add(new Label(EditDomainServicesPanel.SVC_DESC));
		
		final LoadingSpinner spinner = new LoadingSpinner();
		spinner.show();
		contentPanel.add(spinner);
		
		IAsyncLoadCallback<GDomainList> callback=new IAsyncLoadCallback<GDomainList>() {

			@Override
			public void onSuccess(GDomainList theResult) {
				
				GDomain domain = theResult.getDomainByPid(theDomainPid);
				if (domain == null) {
					GWT.log("Unknown domain PID: " + theDomainPid);
					NavProcessor.goHome();
					return;
				}
				
				GService service = domain.getServiceList().getServiceByPid(theServicePid);
				if (service	 == null) {
					GWT.log("Unknown service PID: " + theDomainPid);
					NavProcessor.goHome();
					return;
				}
				
				spinner.hideCompletely();
				
				myEditServiceBasicPropertiesPanel = new EditServiceBasicPropertiesPanel(service, "Save", new MySaveButtonHandler(service), AdminPortal.IMAGES.iconSave());
				contentPanel.add(myEditServiceBasicPropertiesPanel);
//				add(new EditServicePropertiesPanel(service));
			}
		};
		Model.getInstance().loadDomainList(callback);
	}
	
	
	
	public class MySaveButtonHandler implements ClickHandler, AsyncCallback<GDomainList> {

		private GService myService;

		public MySaveButtonHandler(GService theService) {
			myService=theService;
		}

		@Override
		public void onClick(ClickEvent theEvent) {
			if (myEditServiceBasicPropertiesPanel.validateValues()) {
				myEditServiceBasicPropertiesPanel.showMessage("Saving Domain...", true);
				AdminPortal.MODEL_SVC.saveService(myService, this);
			}
		}

		@Override
		public void onFailure(Throwable theCaught) {
			myEditServiceBasicPropertiesPanel.hideSpinner();
			myEditServiceBasicPropertiesPanel.showError(theCaught.getMessage());
		}

		@Override
		public void onSuccess(GDomainList theResult) {
			Model.getInstance().mergeDomainList(theResult);
			myEditServiceBasicPropertiesPanel.showMessage("Service has been saved", false);
		}

	}


}
