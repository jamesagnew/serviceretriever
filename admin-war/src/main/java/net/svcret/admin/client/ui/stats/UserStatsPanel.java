package net.svcret.admin.client.ui.stats;

import static net.svcret.admin.client.AdminPortal.*;
import net.svcret.admin.client.AdminPortal;
import net.svcret.admin.client.ui.components.CssConstants;
import net.svcret.admin.client.ui.components.LoadingSpinner;
import net.svcret.admin.shared.Model;
import net.svcret.admin.shared.model.GRecentMessageLists;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

public class UserStatsPanel extends FlowPanel {

	private LoadingSpinner myLoadingSpinner;

	public UserStatsPanel(long theUserPid) {
		final FlowPanel listPanel = new FlowPanel();
		listPanel.setStylePrimaryName(CssConstants.MAIN_PANEL);
		add(listPanel);

		Label titleLabel = new Label(MSGS.editUser_RecentMessagesTitle());
		titleLabel.setStyleName(CssConstants.MAIN_PANEL_TITLE);
		listPanel.add(titleLabel);
		
		myLoadingSpinner = new LoadingSpinner();
		listPanel.add(myLoadingSpinner);
		
		AdminPortal.MODEL_SVC.loadRecentTransactionListForuser(theUserPid, new AsyncCallback<GRecentMessageLists>() {
			@Override
			public void onFailure(Throwable theCaught) {
				Model.handleFailure(theCaught);
			}

			@Override
			public void onSuccess(GRecentMessageLists theResult) {
				myLoadingSpinner.hideCompletely();
				
				listPanel.add(new RecentMessagesPanel(theResult, true));
			}
		});
	}
	
}
