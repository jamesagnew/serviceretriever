package ca.uhn.sail.proxy.admin.client.ui.config;

import ca.uhn.sail.proxy.admin.client.ui.components.HtmlBr;
import ca.uhn.sail.proxy.admin.shared.model.GDomain;
import ca.uhn.sail.proxy.admin.shared.model.Model;

import com.google.gwt.user.client.ui.FlowPanel;

public class EditDomainPanel extends FlowPanel {

	private GDomain myDomain;
	private String myDomainPid;

	public EditDomainPanel(String theDomainPid) {
		myDomainPid = theDomainPid;
		myDomain = Model.getInstance().getDomainList().getDomainByPid(myDomainPid);

		add(new EditDomainPropertiesPanel(myDomain));
		add(new EditDomainServicesPanel(myDomain));
	}

}
