package net.svcret.admin.client.ui.dash;

import com.google.gwt.user.client.ui.Widget;

import net.svcret.admin.client.ui.config.svcver.BaseUrlGrid;
import net.svcret.admin.shared.model.GServiceVersionUrl;

public class UrlDashboardGrid extends BaseUrlGrid {

	public 
	
	@Override
	protected Widget createActionPanel(GServiceVersionUrl theUrl) {
		return null;
	}

	@Override
	protected String provideActionColumnHeaderText() {
		return "Service";
	}

}
