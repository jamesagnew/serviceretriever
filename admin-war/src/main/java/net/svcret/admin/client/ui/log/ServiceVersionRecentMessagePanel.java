package net.svcret.admin.client.ui.log;

import net.svcret.admin.client.AdminPortal;
import net.svcret.admin.client.ui.components.LoadingSpinner;
import net.svcret.admin.client.ui.components.TwoColumnGrid;
import net.svcret.admin.client.ui.dash.model.ActionPButton;
import net.svcret.admin.shared.IAsyncLoadCallback;
import net.svcret.admin.shared.Model;
import net.svcret.admin.shared.model.DtoDomainList;
import net.svcret.admin.shared.model.GRecentMessageLists;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;

public class ServiceVersionRecentMessagePanel extends BaseRecentMessagesPanel {

	private long myServiceVersionPid;

	public ServiceVersionRecentMessagePanel(long theServiceVersionPid, boolean theFailedToLoadLast) {
		myServiceVersionPid = theServiceVersionPid;
		
		if (theFailedToLoadLast) {
			showFailedToLoadLastTransaction();
		}
		
		loadTransactions();

	}

	private void loadTransactions() {
		AdminPortal.MODEL_SVC.loadRecentTransactionListForServiceVersion(myServiceVersionPid, new AsyncCallback<GRecentMessageLists>() {

			@Override
			public void onFailure(Throwable theCaught) {
				Model.handleFailure(theCaught);
			}

			@Override
			public void onSuccess(final GRecentMessageLists theResult) {
				Model.getInstance().loadDomainList(new IAsyncLoadCallback<DtoDomainList>() {
					@Override
					public void onSuccess(DtoDomainList theDomainList) {
						
						FlowPanel headerPanel = new FlowPanel();
						headerPanel.add(new Label("Displaying recent transactions for:"));

						TwoColumnGrid grid = new TwoColumnGrid();
						headerPanel.add(grid);
						BaseViewRecentMessagePanel.addServiceVersionInfoToPropertyGrid(grid, theDomainList, myServiceVersionPid);
						
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
						
						setRecentMessages(theResult, "Service Version", headerPanel);
					}
				});
			}

		});
	}

	@Override
	protected String getDialogTitle() {
		return "Recent Transactions for Service Version";
	}

	@Override
	protected String getCatalogItemTypeThisPanelIsDisplaying() {
		return "Service Version";
	}

}
