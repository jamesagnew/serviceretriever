package ca.uhn.sail.proxy.admin.client.ui.layout;

import ca.uhn.sail.proxy.admin.client.nav.PagesEnum;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;

public class LeftMenuComponent extends FlowPanel {

	public LeftMenuComponent(String theTitle) {
		
		setStylePrimaryName("leftMenuComponent");
		
		Label title = new Label(theTitle);
		title.setStyleName("leftMenuTitle");
		add(title);
		
	}

	public Hyperlink addItem(String theName, PagesEnum thePage) {
		
		Hyperlink retVal = new Hyperlink(theName, thePage.name());
		retVal.setStyleName("leftMenuButton");
		add(retVal);
		return retVal;
	}
}
