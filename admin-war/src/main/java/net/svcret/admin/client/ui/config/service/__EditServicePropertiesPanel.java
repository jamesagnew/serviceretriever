package net.svcret.admin.client.ui.config.service;

import net.svcret.admin.client.AdminPortal;
import net.svcret.admin.client.ui.config.domain.EditDomainServicesPanel;
import net.svcret.admin.shared.Model;
import net.svcret.admin.shared.model.GDomainList;
import net.svcret.admin.shared.model.GService;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

public class __EditServicePropertiesPanel extends FlowPanel {

	private GService myService;
	private EditServiceBasicPropertiesPanel myEditServiceBasicPropertiesPanel;

	public __EditServicePropertiesPanel(GService theService) {
		myService = new GService();
		myService.mergeSimple(theService);

		setStylePrimaryName("mainPanel");

		Label titleLabel = new Label("Edit Service");
		titleLabel.setStyleName("mainPanelTitle");
		add(titleLabel);

		FlowPanel contentPanel = new FlowPanel();
		contentPanel.addStyleName("contentInnerPanel");
		add(contentPanel);

		contentPanel.add(new Label(EditDomainServicesPanel.SVC_DESC));

		myEditServiceBasicPropertiesPanel = new EditServiceBasicPropertiesPanel(myService, "Save", new MySaveButtonHandler(), AdminPortal.IMAGES.iconSave());
		contentPanel.add(myEditServiceBasicPropertiesPanel);

	}

	

	public class MySaveButtonHandler implements ClickHandler, AsyncCallback<GDomainList> {

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
