package net.svcret.admin.client.ui.stats;

import net.svcret.admin.client.AdminPortal;
import net.svcret.admin.client.rpc.ModelUpdateService;
import net.svcret.admin.client.ui.config.svcver.BaseRecentMessagesPanel;
import net.svcret.admin.shared.Model;
import net.svcret.admin.shared.model.GRecentMessageLists;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class UserStatsPanel extends BaseRecentMessagesPanel {

	public UserStatsPanel(final long theUserPid) {

		AdminPortal.MODEL_SVC.loadRecentTransactionListForuser(theUserPid, new AsyncCallback<GRecentMessageLists>() {
			@Override
			public void onFailure(Throwable theCaught) {
				Model.handleFailure(theCaught);
			}

			@Override
			public void onSuccess(final GRecentMessageLists theResult) {
				AdminPortal.MODEL_SVC.loadUser(theUserPid, false, new AsyncCallback<ModelUpdateService.UserAndAuthHost>() {

					@Override
					public void onFailure(Throwable theCaught) {
						Model.handleFailure(theCaught);
					}

					@Override
					public void onSuccess(ModelUpdateService.UserAndAuthHost theUser) {
						setRecentMessages(theResult, "User", "Displaying recent transactions for user: " + theUser.getUser().getUsername());
					}
				});
			}
		});
	}

	@Override
	protected String getDialogTitle() {
		return "View Recent Transactions for User";
	}

}
