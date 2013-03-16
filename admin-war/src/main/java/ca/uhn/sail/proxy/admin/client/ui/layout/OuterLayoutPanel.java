package ca.uhn.sail.proxy.admin.client.ui.layout;

import ca.uhn.sail.proxy.admin.client.nav.NavProcessor;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.DockLayoutPanel;

public class OuterLayoutPanel extends DockLayoutPanel {

	public OuterLayoutPanel() {
		super(Unit.PX);
		
		addNorth(new TopBarPanel(), 75);
		addWest(new LeftBarPanel(), 300);
		//addEast(new RightBarPanel(), 300);
		add(new BodyPanel());
		
		NavProcessor.navigate();
	}

}
