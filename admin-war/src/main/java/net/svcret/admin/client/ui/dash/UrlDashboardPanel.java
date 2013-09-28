package net.svcret.admin.client.ui.dash;

import net.svcret.admin.client.ui.components.CssConstants;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;

public class UrlDashboardPanel extends FlowPanel {

	public UrlDashboardPanel() {
		setStylePrimaryName(CssConstants.MAIN_PANEL);

		HorizontalPanel titlePanel = new HorizontalPanel();
		titlePanel.setStyleName(CssConstants.MAIN_PANEL_TITLE);
		add(titlePanel);

		Label titleLabel = new Label("Backing URL Dashboard");
		titleLabel.addStyleName(CssConstants.MAIN_PANEL_TITLE_TEXT);
		titlePanel.add(titleLabel);
		
		
		
	}
}
