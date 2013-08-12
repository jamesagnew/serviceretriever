package net.svcret.admin.client.ui.dash.model;

import com.google.gwt.user.client.ui.PopupPanel;

final class DashActionPopupPanel extends PopupPanel {
	DashActionPopupPanel(boolean theAutoHide, boolean theModal) {
		super(theAutoHide, theModal);
	}

	@Override
	public void setPopupPosition(int theLeft, int theTop) {
		super.setPopupPosition(theLeft, theTop-1);
	}
}