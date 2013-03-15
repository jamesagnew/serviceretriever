package ca.uhn.sail.proxy.admin.client.ui.config;

import ca.uhn.sail.proxy.admin.client.AdminPortal;
import ca.uhn.sail.proxy.admin.shared.model.GDomain;
import ca.uhn.sail.proxy.admin.shared.model.Model;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

public class EditDomainPropertiesPanel extends FlowPanel {

	private GDomain myDomain;
	private String myDomainPid;
	private EditDomainBasicPropertiesPanel myEditDomainBasicPropertiesPanel;

	public EditDomainPropertiesPanel(GDomain theDomain) {
		myDomain = theDomain;

		setStylePrimaryName("mainPanel");

		Label titleLabel = new Label("Edit Domain");
		titleLabel.setStyleName("mainPanelTitle");
		add(titleLabel);

		FlowPanel contentPanel = new FlowPanel();
		contentPanel.addStyleName("contentInnerPanel");
		add(contentPanel);

		contentPanel.add(new Label(AddDomainPanel.DOMAIN_DESC));

		myEditDomainBasicPropertiesPanel = new EditDomainBasicPropertiesPanel(myDomain.getId(), myDomain.getName(), "Save", new MySaveButtonHandler());
		contentPanel.add(myEditDomainBasicPropertiesPanel);


	}

	

	public class MySaveButtonHandler implements ClickHandler, AsyncCallback<GDomain> {

		public void onClick(ClickEvent theEvent) {
			if (myEditDomainBasicPropertiesPanel.validateValues()) {
				myEditDomainBasicPropertiesPanel.showSpinner();
				String id = myEditDomainBasicPropertiesPanel.getId();
				String name = myEditDomainBasicPropertiesPanel.getName();
				AdminPortal.MODEL_SVC.saveDomain(myDomain.getPid(), id, name, this);
			}
		}

		public void onFailure(Throwable theCaught) {
			myEditDomainBasicPropertiesPanel.hideSpinner();
			myEditDomainBasicPropertiesPanel.showError(theCaught.getMessage());
		}

		public void onSuccess(GDomain theResult) {
			Model.getInstance().getDomainList().merge(theResult);
			myEditDomainBasicPropertiesPanel.hideSpinner();
		}

	}

}
