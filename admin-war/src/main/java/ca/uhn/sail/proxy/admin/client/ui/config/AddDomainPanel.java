package ca.uhn.sail.proxy.admin.client.ui.config;

import ca.uhn.sail.proxy.admin.client.AdminPortal;
import ca.uhn.sail.proxy.admin.client.nav.NavProcessor;
import ca.uhn.sail.proxy.admin.shared.model.GDomain;
import ca.uhn.sail.proxy.admin.shared.model.Model;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

public class AddDomainPanel extends FlowPanel implements ClickHandler, AsyncCallback<GDomain> {

	public static final String DOMAIN_DESC = "A Domain is a logical grouping of services. This allows you to classify groups " +
			"of services in whatever classification makes sense to you. There is no specific " +
			"right way to group services, and if you want you can always move services from " +
			"one domain to another.";
	private EditDomainBasicPropertiesPanel myEditDomainBasicPropertiesPanel;
	

	public AddDomainPanel() {
		setStylePrimaryName("mainPanel");

		Label titleLabel = new Label("Add Domain");
		titleLabel.setStyleName("mainPanelTitle");
		add(titleLabel);

		FlowPanel contentPanel = new FlowPanel();
		contentPanel.addStyleName("contentInnerPanel");
		add(contentPanel);
		
		Label intro = new Label(DOMAIN_DESC);
		contentPanel.add(intro);
		
		myEditDomainBasicPropertiesPanel = new EditDomainBasicPropertiesPanel("", "", "Add", this);
		contentPanel.add(myEditDomainBasicPropertiesPanel);
	}

	public void onClick(ClickEvent theEvent) {
		if (myEditDomainBasicPropertiesPanel.validateValues()) {
			myEditDomainBasicPropertiesPanel.showSpinner();
			String id = myEditDomainBasicPropertiesPanel.getId();
			String name = myEditDomainBasicPropertiesPanel.getName();
			AdminPortal.MODEL_SVC.addDomain(id, name, this);
		}
		
	}

	public void onFailure(Throwable theCaught) {
		myEditDomainBasicPropertiesPanel.hideSpinner();
		myEditDomainBasicPropertiesPanel.showError(theCaught.getMessage());
	}

	public void onSuccess(GDomain theResult) {
		Model.getInstance().getDomainList().add(theResult);
		History.newItem(NavProcessor.getTokenAddDomainStep2(theResult.getPid()));
	}
	
}
