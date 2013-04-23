package net.svcret.admin.client.ui.layout;

import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.RootPanel;

public class TopBarPanel extends FlowPanel {

	public TopBarPanel() {
		setStylePrimaryName("outerLayoutTopBar");

		Image titleBanner = new Image("images/banner.png");
		titleBanner.setStyleName("");
		titleBanner.getElement().getStyle().setPosition(Position.ABSOLUTE);
		titleBanner.getElement().getStyle().setLeft(0, Unit.PX);
		titleBanner.getElement().getStyle().setTop(0, Unit.PX);
		
//		add(titleBanner);
		
		RootPanel.getBodyElement().appendChild(titleBanner.getElement());
		
		
	}
	
}
