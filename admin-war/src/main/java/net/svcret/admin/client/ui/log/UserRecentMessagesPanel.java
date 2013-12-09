package net.svcret.admin.client.ui.log;

import net.svcret.admin.client.AdminPortal;
import net.svcret.admin.client.rpc.ModelUpdateService;
import net.svcret.admin.client.ui.components.LoadingSpinner;
import net.svcret.admin.client.ui.dash.model.ActionPButton;
import net.svcret.admin.shared.Model;
import net.svcret.admin.shared.model.GRecentMessageLists;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;

public class UserRecentMessagesPanel extends BaseRecentMessagesPanel {

	private long myUserPid;

	public UserRecentMessagesPanel(final long theUserPid, boolean theFailedToLoadLast) {
		myUserPid = theUserPid;
		
		if (theFailedToLoadLast) {
			showFailedToLoadLastTransaction();
		}
		
		loadTransactions();
	}

	private void loadTransactions() {
		AdminPortal.MODEL_SVC.loadRecentTransactionListForuser(myUserPid, new AsyncCallback<GRecentMessageLists>() {
			@Override
			public void onFailure(Throwable theCaught) {
				Model.handleFailure(theCaught);
			}

			@Override
			public void onSuccess(final GRecentMessageLists theResult) {
				AdminPortal.MODEL_SVC.loadUser(myUserPid, false, new AsyncCallback<ModelUpdateService.UserAndAuthHost>() {

					@Override
					public void onFailure(Throwable theCaught) {
						Model.handleFailure(theCaught);
					}

					@Override
					public void onSuccess(ModelUpdateService.UserAndAuthHost theUser) {
						FlowPanel headerPanel = new FlowPanel();
						headerPanel.add(new Label("Displaying recent transactions for user: " + theUser.getUser().getUsername()));

						HorizontalPanel refPanel = new HorizontalPanel();
						final LoadingSpinner spinner = new LoadingSpinner();
						refPanel.add(new ActionPButton(AdminPortal.IMAGES.iconReload16(), AdminPortal.MSGS.actions_Refresh(), new ClickHandler() {
							@Override
							public void onClick(ClickEvent theEvent) {
								spinner.show();
								loadTransactions();
							}
						}));
						refPanel.add(spinner);

						setRecentMessages(theResult, "User", headerPanel);
					}
				});
			}
		});
	}

	@Override
	protected String getDialogTitle() {
		return "View Recent Transactions for User";
	}

	@Override
	protected String getCatalogItemTypeThisPanelIsDisplaying() {
		return "User";
	}

}
