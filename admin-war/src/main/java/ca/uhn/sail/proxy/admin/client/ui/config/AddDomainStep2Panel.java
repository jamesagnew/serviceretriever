package ca.uhn.sail.proxy.admin.client.ui.config;

import ca.uhn.sail.proxy.admin.client.nav.NavProcessor;
import ca.uhn.sail.proxy.admin.client.ui.components.HtmlBr;
import ca.uhn.sail.proxy.admin.shared.model.GDomain;
import ca.uhn.sail.proxy.admin.shared.model.Model;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

public class AddDomainStep2Panel  extends FlowPanel implements ClickHandler {

	private String myDomainId;

	public AddDomainStep2Panel(String theDomainId) {
		myDomainId = theDomainId;
		
		setStylePrimaryName("mainPanel");

		Label titleLabel = new Label("Add Domain");
		titleLabel.setStyleName("mainPanelTitle");
		add(titleLabel);

		FlowPanel contentPanel = new FlowPanel();
		contentPanel.addStyleName("contentInnerPanel");
		add(contentPanel);

		GDomain domain = Model.getInstance().getDomainList().getDomainByPid(theDomainId);
		
		Label text = new Label("Domain \"" + domain.getName() + "\" has been successfully added.");
		contentPanel.add(text);
		
		add(new HtmlBr());
		add(new Button("Edit It", this));
		
	}

	public void onClick(ClickEvent theEvent) {
		History.newItem(NavProcessor.getTokenEditDomain(false, Long.parseLong(myDomainId)));
	}
	
}
