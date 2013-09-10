package net.svcret.admin.client.ui.test;

import net.svcret.admin.client.ui.stats.BaseViewRecentMessagePanel;

public class ServiceVersionTestResponsePanel extends BaseViewRecentMessagePanel {

	public ServiceVersionTestResponsePanel() {
		setHideRequest(true);
	}
	
	@Override
	protected String getPanelTitle() {
		return "Transaction Results";
	}

}
