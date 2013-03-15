package ca.uhn.sail.proxy.admin.client.ui.layout;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;

public class TopBarPanel extends FlowPanel {

	public TopBarPanel() {
		setStylePrimaryName("outerLayoutTopBar");

		Image titleBanner = new Image("images/banner.png");
		add(titleBanner);
//		setWidgetTopBottom(titleWidget, 50, Unit.PCT, 50, Unit.PCT);
	}
	
}
