package net.svcret.admin.client.ui.dash;

import net.svcret.admin.client.AdminPortal;
import net.svcret.admin.client.ui.components.CssConstants;
import net.svcret.admin.client.ui.components.LoadingSpinner;
import net.svcret.admin.shared.IAsyncLoadCallback;
import net.svcret.admin.shared.Model;
import net.svcret.admin.shared.model.DtoDomainList;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;

public class UrlDashboardPanel extends FlowPanel {

	public UrlDashboardPanel() {
		setStylePrimaryName(CssConstants.MAIN_PANEL);

		HorizontalPanel titlePanel = new HorizontalPanel();
		titlePanel.setStyleName(CssConstants.MAIN_PANEL_TITLE);
		add(titlePanel);

		Label titleLabel = new Label(AdminPortal.MSGS.urlDashboardPanel_Title());
		titleLabel.addStyleName(CssConstants.MAIN_PANEL_TITLE_TEXT);
		titlePanel.add(titleLabel);
		
		final LoadingSpinner loadingSpinner = new LoadingSpinner();
		add(loadingSpinner);
		loadingSpinner.show();
		
		Model.getInstance().loadDomainListAndUrlStats(new IAsyncLoadCallback<DtoDomainList>() {
			
			@Override
			public void onSuccess(DtoDomainList theResult) {
				loadingSpinner.hideCompletely();
				UrlDashboardGrid grid = new UrlDashboardGrid(theResult);
				add(grid);
			}
		});
		
	}
}
