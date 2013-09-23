package net.svcret.admin.client.ui.config.auth;

import static net.svcret.admin.client.AdminPortal.IMAGES;
import static net.svcret.admin.client.AdminPortal.MSGS;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import net.svcret.admin.client.AdminPortal;
import net.svcret.admin.client.nav.NavProcessor;
import net.svcret.admin.client.ui.components.CellWithTooltip;
import net.svcret.admin.client.ui.components.CssConstants;
import net.svcret.admin.client.ui.components.LoadingSpinner;
import net.svcret.admin.client.ui.components.PButton;
import net.svcret.admin.client.ui.components.PButtonCell;
import net.svcret.admin.client.ui.components.PCellTable;
import net.svcret.admin.client.ui.config.svcver.BaseDetailPanel;
import net.svcret.admin.client.ui.config.svcver.NullColumn;
import net.svcret.admin.client.ui.dash.model.UsageSparklineTooltipProvider;
import net.svcret.admin.shared.DateUtil;
import net.svcret.admin.shared.IAsyncLoadCallback;
import net.svcret.admin.shared.Model;
import net.svcret.admin.shared.model.BaseGAuthHost;
import net.svcret.admin.shared.model.GAuthenticationHostList;
import net.svcret.admin.shared.model.GPartialUserList;
import net.svcret.admin.shared.model.GUser;
import net.svcret.admin.shared.model.PartialUserListRequest;
import net.svcret.admin.shared.util.StringUtil;

import com.google.gwt.cell.client.CompositeCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.HasCell;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.ColumnSortList.ColumnSortInfo;
import com.google.gwt.user.cellview.client.IdentityColumn;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.SimplePager.TextLocation;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.view.client.ListDataProvider;

public class EditUsersPanel extends FlowPanel {

	private CellTable<GUser> myTable;
	private GPartialUserList myUserList;
	private LoadingSpinner myLoadingSpinner;
	private ListBox myAuthHostsListBox;
	private GAuthenticationHostList myAuthenticationHostList;
	private ListDataProvider<GUser> myMethodDataProvider;

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

		myTable = new PCellTable<GUser>();
		myMethodDataProvider = new ListDataProvider<GUser>();
		contentPanel.add(myTable);

		// Create a Pager to control the table.
		SimplePager.Resources pagerResources = GWT.create(SimplePager.Resources.class);
		SimplePager pager = new SimplePager(TextLocation.CENTER, pagerResources, false, 0, true);
		pager.setDisplay(myTable);
		pager.setPageSize(10);
		contentPanel.add(pager);

		PButtonCell editCell = new PButtonCell(AdminPortal.IMAGES.iconEdit(), AdminPortal.MSGS.actions_Edit());
		Column<GUser, String> editColumn = new NullColumn<GUser>(editCell);
		editColumn.setFieldUpdater(new FieldUpdater<GUser, String>() {
			@Override
			public void update(int theIndex, GUser theObject, String theValue) {
				editUser(theObject);
			}
		});
		editCell.addStyle(CssConstants.USERS_ACTION_BUTTON);

		PButtonCell viewRecentMessagesCell = new PButtonCell(AdminPortal.IMAGES.iconTransactions(), "Trans.");
		Column<GUser, String> viewRecentMessagesColumn = new NullColumn<GUser>(viewRecentMessagesCell);
		viewRecentMessagesColumn.setFieldUpdater(new FieldUpdater<GUser, String>() {
			@Override
			public void update(int theIndex, GUser theObject, String theValue) {
				viewUserRecentMessages(theObject);
			}
		});
		viewRecentMessagesCell.addStyle(CssConstants.USERS_ACTION_BUTTON);

		PButtonCell statsCell = new PButtonCell(AdminPortal.IMAGES.iconStatus(), "Stats");
		Column<GUser, String> statsColumn = new NullColumn<GUser>(statsCell);
		statsColumn.setFieldUpdater(new FieldUpdater<GUser, String>() {
			@Override
			public void update(int theIndex, GUser theObject, String theValue) {
				viewUserStats(theObject);
			}
		});
		statsCell.addStyle(CssConstants.USERS_ACTION_BUTTON);

		List<HasCell<GUser, ?>> actionCells = new ArrayList<HasCell<GUser, ?>>();
		actionCells.add(editColumn);
		actionCells.add(viewRecentMessagesColumn);
		actionCells.add(statsColumn);
		CompositeCell<GUser> actionColumn = new CompositeCell<GUser>(actionCells);
		myTable.addColumn(new IdentityColumn<GUser>(actionColumn), "");

		// Auth Host

		Column<GUser, SafeHtml> authHostCol = new Column<GUser, SafeHtml>(new SafeHtmlCell()) {
			@Override
			public SafeHtml getValue(GUser theObject) {
				BaseGAuthHost authHostByPid = myAuthenticationHostList.getAuthHostByPid(theObject.getAuthHostPid());
				if (authHostByPid == null) {
					return SafeHtmlUtils.fromString("ERROR");
				}
				return SafeHtmlUtils.fromString(authHostByPid.getModuleName());
			}
		};
		myTable.addColumn(authHostCol, MSGS.editUsersPanel_ColumnAuthHost());

		// Username

		Column<GUser, SafeHtml> usernameCol = new Column<GUser, SafeHtml>(new SafeHtmlCell()) {
			@Override
			public SafeHtml getValue(GUser theObject) {
				return SafeHtmlUtils.fromString(theObject.getUsername());
			}
		};
		myTable.addColumn(usernameCol, MSGS.editUsersPanel_ColumnUsername());

		// Description

		Column<GUser, SafeHtml> descriptionCol = new Column<GUser, SafeHtml>(new SafeHtmlCell()) {
			@Override
			public SafeHtml getValue(GUser theObject) {
				return SafeHtmlUtils.fromString(StringUtil.defaultString(theObject.getDescription()));
			}
		};
		myTable.addColumn(descriptionCol, MSGS.editUsersPanel_ColumnDescription());

		// Last Access

		Column<GUser, SafeHtml> lastAccessCol = new Column<GUser, SafeHtml>(new SafeHtmlCell()) {
			@Override
			public SafeHtml getValue(GUser theObject) {
				return SafeHtmlUtils.fromString(DateUtil.formatTimeElapsedForLastInvocation(theObject.getStatsLastAccess()));
			}
		};
		myTable.addColumn(lastAccessCol, MSGS.editUsersPanel_ColumnLastServiceAccess());

		// Transactions

		Column<GUser, SafeHtml> transactionsCol = new Column<GUser, SafeHtml>(new CellWithTooltip<GUser>(myMethodDataProvider, new UsageSparklineTooltipProvider<GUser>())) {
			@Override
			public SafeHtml getValue(GUser theObject) {
				SafeHtmlBuilder b = new SafeHtmlBuilder();
				int[] success = theObject.getTransactions60mins();
				int[] fault = theObject.getTransactionsFault60mins();
				int[] fail = theObject.getTransactionsFail60mins();
				int[] secFail = theObject.getTransactionsSecurityFail60mins();
				Date lastAccess = theObject.getStatsLastAccess();
				BaseDetailPanel.renderTransactionGraphsAsHtml(b, success, fault, fail, secFail, true,lastAccess);

				return b.toSafeHtml();
			}
		};
		myTable.addColumn(transactionsCol, MSGS.editUsersPanel_ColumnTransactions());

		myMethodDataProvider.addDataDisplay(myTable);

		// Sorting
		ListHandler<GUser> columnSortHandler = new ListHandler<GUser>(myMethodDataProvider.getList());

		authHostCol.setSortable(true);
		columnSortHandler.setComparator(authHostCol, new Comparator<GUser>() {
			@Override
			public int compare(GUser theO1, GUser theO2) {
				String ah1 = myAuthenticationHostList.getAuthHostByPid(theO1.getAuthHostPid()).getModuleName();
				String ah2 = myAuthenticationHostList.getAuthHostByPid(theO2.getAuthHostPid()).getModuleName();
				return StringUtil.compare(ah1, ah2);
			}
		});
		usernameCol.setSortable(true);
		columnSortHandler.setComparator(usernameCol, new Comparator<GUser>() {
			@Override
			public int compare(GUser theO1, GUser theO2) {
				String ah1 = theO1.getUsername();
				String ah2 = theO2.getUsername();
				return StringUtil.compare(ah1, ah2);
			}
		});
		descriptionCol.setSortable(true);
		columnSortHandler.setComparator(descriptionCol, new Comparator<GUser>() {
			@Override
			public int compare(GUser theO1, GUser theO2) {
				return StringUtil.compare(theO1.getUsername(), theO2.getUsername());
			}
		});
		lastAccessCol.setSortable(true);
		columnSortHandler.setComparator(lastAccessCol, new Comparator<GUser>() {
			@Override
			public int compare(GUser theO1, GUser theO2) {
				Date la1 = theO1.getStatsLastAccess();
				Date la2 = theO2.getStatsLastAccess();
				if (la1 == null && la2 == null) {
					return 0;
				}
				if (la1 == null) {
					return -1;
				}
				if (la2 == null) {
					return 1;
				}
				long cmp = (la1.getTime() - la2.getTime());
				if (cmp > 0) {
					return 1;
				} else if (cmp < 0) {
					return -1;
				} else {
					return 0;
				}
			}
		});
		transactionsCol.setSortable(true);
		columnSortHandler.setComparator(transactionsCol, new Comparator<GUser>() {
			@Override
			public int compare(GUser theO1, GUser theO2) {
				double cmp = theO1.getMaxTotalTransactionsPerMin() - theO2.getMaxTotalTransactionsPerMin();
				if (cmp > 0.0) {
					return 1;
				} else if (cmp < 0.0) {
					return -1;
				} else {
					return 0;
				}
			}
		});

		myTable.addColumnSortHandler(columnSortHandler);
		myTable.getColumnSortList().clear();
		myTable.getColumnSortList().push(new ColumnSortInfo(usernameCol, true));

		HorizontalPanel addPanel = new HorizontalPanel();
		contentPanel.add(addPanel);

		addPanel.add(new PButton(IMAGES.iconAdd(), MSGS.actions_AddNewDotDotDot(), new ClickHandler() {

			@Override
			public void onClick(ClickEvent theEvent) {
				History.newItem(NavProcessor.getTokenAddUser(Long.parseLong(myAuthHostsListBox.getValue(myAuthHostsListBox.getSelectedIndex()))));
			}
		}));
		addPanel.add(new Label("Using Authentication Host:"));

		myAuthHostsListBox = new ListBox();
		addPanel.add(myAuthHostsListBox);

		Model.getInstance().loadAuthenticationHosts(new IAsyncLoadCallback<GAuthenticationHostList>() {
			@Override
			public void onSuccess(GAuthenticationHostList theResult) {
				for (BaseGAuthHost next : theResult) {
					myAuthHostsListBox.addItem(next.getModuleId(), next.getPidOrNull().toString());
				}
				myAuthHostsListBox.setSelectedIndex(0);
			}
		});

		loadUserList();

	}

	private void viewUserStats(GUser theObject) {
		String token = NavProcessor.getTokenUserStats(theObject.getPid());
		History.newItem(token);
	}

	private void loadUserList() {
		myLoadingSpinner.show();

		PartialUserListRequest request = new PartialUserListRequest();
		request.setLoadStats(true);

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

	private void setUserlist(final GPartialUserList theUserList) {
		Model.getInstance().loadAuthenticationHosts(new IAsyncLoadCallback<GAuthenticationHostList>() {
			@Override
			public void onSuccess(GAuthenticationHostList theResult) {
				myAuthenticationHostList = theResult;
				myUserList = theUserList;

				myMethodDataProvider.getList().addAll(myUserList.toCollection());
				myMethodDataProvider.refresh();
				ColumnSortEvent.fire(myTable, myTable.getColumnSortList());
			}
		});

	}

	private void viewUserRecentMessages(GUser theNextUser) {
		String token = NavProcessor.getTokenUserRecentMessages(theNextUser.getPid());
		History.newItem(token);
	}

	private void editUser(GUser theNextUser) {
		String token = NavProcessor.getTokenEditUser(theNextUser.getPid());
		History.newItem(token);
	}

}
