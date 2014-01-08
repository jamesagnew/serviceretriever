package net.svcret.admin.client.ui.layout;

import net.svcret.admin.client.nav.NavProcessor;
import net.svcret.admin.client.ui.components.CssConstants;
import net.svcret.admin.shared.util.StringUtil;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.DockLayoutPanel;

public class OuterLayoutPanel extends DockLayoutPanel {

	public OuterLayoutPanel() {
		super(Unit.PX);
		
		addNorth(TopBarPanel.getInstance(), 62);
		addWest(LeftBarPanel.getInstance(), 200);
		//addEast(new RightBarPanel(), 300);
		
		DockLayoutPanel bodyDock = new DockLayoutPanel(Unit.PX);
		add(bodyDock);
		
		BreadcrumbPanel breadcrumbPanel = new BreadcrumbPanel();
		bodyDock.addNorth(breadcrumbPanel, 23);
		breadcrumbPanel.getElement().getParentElement().addClassName(CssConstants.BREADCRUMB_PANEL);
		
		bodyDock.add(new BodyPanel());
		
		String initPage = Cookies.getCookie("sr-first-page");
		if (StringUtil.isNotBlank(initPage)) {
			History.newItem(initPage, false);
			Cookies.removeCookie("sr-first-page");
			LeftBarPanel.getInstance().updateStyles();
		}
		
		NavProcessor.navigate();
	}

}
