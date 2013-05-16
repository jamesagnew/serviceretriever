package net.svcret.admin.client.ui.config.auth;

import static net.svcret.admin.client.AdminPortal.*;
import net.svcret.admin.client.AdminPortal;
import net.svcret.admin.client.nav.NavProcessor;
import net.svcret.admin.client.ui.components.CssConstants;
import net.svcret.admin.client.ui.components.LoadingSpinner;
import net.svcret.admin.client.ui.components.PButton;
import net.svcret.admin.client.ui.dash.model.BaseDashModel;
import net.svcret.admin.client.ui.stats.DateUtil;
import net.svcret.admin.shared.IAsyncLoadCallback;
import net.svcret.admin.shared.Model;
import net.svcret.admin.shared.model.BaseGAuthHost;
import net.svcret.admin.shared.model.GAuthenticationHostList;
import net.svcret.admin.shared.model.GPartialUserList;
import net.svcret.admin.shared.model.GUser;
import net.svcret.admin.shared.model.PartialUserListRequest;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Panel;

public class EditUsersPanel extends FlowPanel {

	private static final int COL_USERNAME = 0;
	private static final int COL_LAST_SVC_ACCESS = 1;
	private static final int COL_SUCCESSFUL_XACTS = 2;
	private static final int COL_SECURITY_FAILURE_XACTS = 3;
	private static final int COL_ACTIONS = 4;

	private FlexTable myTable;
	private GPartialUserList myUserList;
	private LoadingSpinner myLoadingSpinner;
	private ListBox myAuthHostsListBox;

	public EditUsersPanel() {
		FlowPanel listPanel = new FlowPanel();
		listPanel.setStylePrimaryName(CssConstants.MAIN_PANEL);
		add(listPanel);

		Label titleLabel = new Label(MSGS.editUsersPanel_Title());
		titleLabel.setStyleName(CssConstants.MAIN_PANEL_TITLE);
		listPanel.add(titleLabel);

		FlowPanel contentPanel = new FlowPanel();
		contentPanel.addStyleName(CssConstants.CONTENT_INNER_PANEL);
		listPanel.add(contentPanel);

		contentPanel.add(new Label(MSGS.editUsersPanel_ListDescription()));

		myLoadingSpinner = new LoadingSpinner();
		contentPanel.add(myLoadingSpinner);

		myTable = new FlexTable();
		contentPanel.add(myTable);

		myTable.addStyleName(CssConstants.PROPERTY_TABLE);
		myTable.setText(0, COL_USERNAME, MSGS.editUsersPanel_ColumnUsername());
		myTable.setText(0, COL_ACTIONS, MSGS.editUsersPanel_ColumnActions());
		myTable.setText(0, COL_LAST_SVC_ACCESS, MSGS.editUsersPanel_ColumnLastServiceAccess());
		myTable.setText(0, COL_SECURITY_FAILURE_XACTS, MSGS.editUsersPanel_ColumnSecurityFailures());
		myTable.setText(0, COL_SUCCESSFUL_XACTS, MSGS.editUsersPanel_ColumnSuccessfulTransactions());

		HorizontalPanel addPanel = new HorizontalPanel();
		contentPanel.add(addPanel);
		
		addPanel.add(new PButton(IMAGES.iconAdd(), MSGS.actions_AddNewDotDotDot(), new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent theEvent) {
				History.newItem(NavProcessor.getTokenAddUser(true, Long.parseLong(myAuthHostsListBox.getValue(myAuthHostsListBox.getSelectedIndex()))));
			}
		}));
		addPanel.add(new Label("Using Authentication Host:"));
		
		myAuthHostsListBox = new ListBox();
		addPanel.add(myAuthHostsListBox);
		
		Model.getInstance().loadAuthenticationHosts(new IAsyncLoadCallback<GAuthenticationHostList>() {
			@Override
			public void onSuccess(GAuthenticationHostList theResult) {
				for (BaseGAuthHost next:theResult) {
					myAuthHostsListBox.addItem(next.getModuleId(), next.getPidOrNull().toString());
				}
				myAuthHostsListBox.setSelectedIndex(0);
			}
		});
		
		loadUserList();

	}

	private void loadUserList() {
		myLoadingSpinner.show();
		PartialUserListRequest request = new PartialUserListRequest();
		AsyncCallback<GPartialUserList> callback = new AsyncCallback<GPartialUserList>() {
			@Override
			public void onFailure(Throwable theCaught) {
				Model.handleFailure(theCaught);
			}

			@Override
			public void onSuccess(GPartialUserList theResult) {
				setUserlist(theResult);
				myLoadingSpinner.hide();
			}
		};
		AdminPortal.MODEL_SVC.loadUsers(request, callback);
	}

	private void setUserlist(GPartialUserList theUserList) {
		myUserList = theUserList;

		for (int i = 0; i < myUserList.size(); i++) {

			final GUser nextUser = myUserList.get(i);
			int row = i + 1;

			myTable.setText(row, COL_USERNAME, nextUser.getUsername());
			myTable.setText(row, COL_LAST_SVC_ACCESS, DateUtil.formatTime(nextUser.getStatsLastAccess()));
			myTable.setWidget(row, COL_SUCCESSFUL_XACTS, BaseDashModel.returnSparklineFor60Mins(nextUser.getStatsSuccessTransactions(), nextUser.getStatsSuccessTransactionsAvgPerMin()));
			myTable.setWidget(row, COL_SECURITY_FAILURE_XACTS, BaseDashModel.returnSparklineFor60Mins(nextUser.getStatsSecurityFailTransactions(), nextUser.getStatsSecurityFailTransactionsAvgPerMin()));

			Panel actionPanel = new HorizontalPanel();
			myTable.setWidget(row, COL_ACTIONS, actionPanel);

			actionPanel.add(new PButton(IMAGES.iconEdit(), MSGS.actions_Edit(), new ClickHandler() {
				@Override
				public void onClick(ClickEvent theEvent) {
					editUser(nextUser);
				}
			}));
			actionPanel.add(new PButton(IMAGES.iconStatus(), MSGS.actions_ViewStats(), new ClickHandler() {
				@Override
				public void onClick(ClickEvent theEvent) {
					viewUserStats(nextUser);
				}
			}));
		}
	}

	private void viewUserStats(GUser theNextUser) {
		String token = NavProcessor.getTokenViewUserStats(true, theNextUser.getPid());
		History.newItem(token);
	}

	private void editUser(GUser theNextUser) {
		String token = NavProcessor.getTokenEditUser(true, theNextUser.getPid());
		History.newItem(token);
	}

}
