package net.svcret.admin.client.ui.layout;

import net.svcret.admin.client.MyResources;
import net.svcret.admin.client.nav.PagesEnum;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;

public class LeftMenuComponent extends FlowPanel {

	public LeftMenuComponent(String theTitle) {
		
		setStylePrimaryName(MyResources.CSS.leftMenuComponent());
		
		Label title = new Label(theTitle);
		title.setStyleName(MyResources.CSS.leftMenuTitle());
		add(title);
		
	}

	public Hyperlink addItem(String theName, PagesEnum thePage) {
		
		Hyperlink retVal = new Hyperlink(theName, thePage.name());
		retVal.setStyleName("leftMenuButton");
		add(retVal);
		return retVal;
	}
}
