package net.svcret.admin.client.ui.config.domain;

import net.svcret.admin.client.AdminPortal;
import net.svcret.admin.client.nav.NavProcessor;
import net.svcret.admin.client.ui.components.CssConstants;
import net.svcret.admin.shared.Model;
import net.svcret.admin.shared.model.GDomain;

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
		setStylePrimaryName(CssConstants.MAIN_PANEL);

		Label titleLabel = new Label("Add Domain");
		titleLabel.setStyleName(CssConstants.MAIN_PANEL_TITLE);
		add(titleLabel);

		FlowPanel contentPanel = new FlowPanel();
		contentPanel.addStyleName(CssConstants.CONTENT_INNER_PANEL);
		add(contentPanel);
		
		Label intro = new Label(DOMAIN_DESC);
		contentPanel.add(intro);
		
		myEditDomainBasicPropertiesPanel = new EditDomainBasicPropertiesPanel("", "", "Add", this);
		contentPanel.add(myEditDomainBasicPropertiesPanel);
	}

	@Override
	public void onClick(ClickEvent theEvent) {
		if (myEditDomainBasicPropertiesPanel.validateValues()) {
			myEditDomainBasicPropertiesPanel.showSpinner();
			String id = myEditDomainBasicPropertiesPanel.getId();
			String name = myEditDomainBasicPropertiesPanel.getName();
			AdminPortal.MODEL_SVC.addDomain(id, name, this);
		}
		
	}

	@Override
	public void onFailure(Throwable theCaught) {
		myEditDomainBasicPropertiesPanel.hideSpinner();
		myEditDomainBasicPropertiesPanel.showError(theCaught.getMessage());
	}

	@Override
	public void onSuccess(GDomain theResult) {
		Model.getInstance().addDomain(theResult);
		History.newItem(NavProcessor.getTokenAddDomainStep2(theResult.getPid()));
	}
	
}
