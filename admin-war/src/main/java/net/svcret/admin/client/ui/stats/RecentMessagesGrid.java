package net.svcret.admin.client.ui.stats;

import static net.svcret.admin.client.AdminPortal.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import net.svcret.admin.client.AdminPortal;
import net.svcret.admin.client.nav.NavProcessor;
import net.svcret.admin.client.ui.components.CssConstants;
import net.svcret.admin.client.ui.components.PButtonCell;
import net.svcret.admin.client.ui.components.PCellTable;
import net.svcret.admin.client.ui.config.svcver.NullColumn;
import net.svcret.admin.shared.model.GRecentMessage;
import net.svcret.admin.shared.util.StringUtil;

import com.google.gwt.cell.client.CompositeCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.HasCell;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.IdentityColumn;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.view.client.ListDataProvider;

public class RecentMessagesGrid extends FlowPanel {

	public RecentMessagesGrid(List<GRecentMessage> theList) {

		final CellTable<GRecentMessage> grid = new PCellTable<GRecentMessage>();
		add(grid);
		grid.setEmptyTableWidget(new Label("No messages"));

		ListDataProvider<GRecentMessage> dataProvider = new ListDataProvider<GRecentMessage>();
		dataProvider.addDataDisplay(grid);
		
		// Action
		List<HasCell<GRecentMessage, ?>> actionCells = new ArrayList<HasCell<GRecentMessage, ?>>();
		// View Button - TODO: better icon (view magnifying glass?)
		PButtonCell viewCell = new PButtonCell(AdminPortal.IMAGES.iconEdit(), AdminPortal.MSGS.actions_View());
		viewCell.addStyle(CssConstants.RECENT_TRANSACTIONS_ACTION_BUTTON);
		Column<GRecentMessage, String> viewColumn = new NullColumn<GRecentMessage>(viewCell);
		actionCells.add(viewColumn);
		viewColumn.setFieldUpdater(new FieldUpdater<GRecentMessage, String>() {
			@Override
			public void update(int theIndex, GRecentMessage theObject, String theValue) {
				switch (theObject.getRecentMessageType()) {
				case USER:
					History.newItem(NavProcessor.getTokenViewUserRecentMessage(true, theObject.getPid()));
					break;
				case SVCVER:
					History.newItem(NavProcessor.getTokenViewServiceVersionRecentMessage(true, theObject.getPid()));
					break;
				}
			}
		});
		// Replay Button
		PButtonCell replayCell = new PButtonCell(AdminPortal.IMAGES.iconPlay16(), AdminPortal.MSGS.actions_Replay());
		viewCell.addStyle(CssConstants.RECENT_TRANSACTIONS_ACTION_BUTTON);
		Column<GRecentMessage, String> replayColumn = new NullColumn<GRecentMessage>(replayCell);
		actionCells.add(replayColumn);
		replayColumn.setFieldUpdater(new FieldUpdater<GRecentMessage, String>() {
			@Override
			public void update(int theIndex, GRecentMessage theObject, String theValue) {
				History.newItem(NavProcessor.getTokenReplayMessage(true, theObject.getPid()));
			}
		});
		// Save Button
		PButtonCell saveCell = new PButtonCell(AdminPortal.IMAGES.iconSave(), AdminPortal.MSGS.actions_Save());
		saveCell.addStyle(CssConstants.RECENT_TRANSACTIONS_ACTION_BUTTON);
		Column<GRecentMessage, String> saveColumn = new NullColumn<GRecentMessage>(saveCell);
		actionCells.add(saveColumn);
		saveColumn.setFieldUpdater(new FieldUpdater<GRecentMessage, String>() {
			@Override
			public void update(int theIndex, GRecentMessage theObject, String theValue) {
				History.newItem(NavProcessor.getTokenSaveRecentMessageToLibrary(true, theObject.getRecentMessageType(), theObject.getPid()));
			}
		});
		CompositeCell<GRecentMessage> actionCell = new CompositeCell<GRecentMessage>(actionCells);
		Column<GRecentMessage, GRecentMessage> actionColumn = new IdentityColumn<GRecentMessage>(actionCell);
		grid.addColumn(actionColumn, "");

		// Timestamp
		Column<GRecentMessage, SafeHtml> timestampColumn = new Column<GRecentMessage, SafeHtml>(new SafeHtmlCell()) {
			@Override
			public SafeHtml getValue(GRecentMessage theObject) {
				return DateUtil.formatTimeElapsedForMessage(theObject.getTransactionTime());
			}
		};
		grid.addColumn(timestampColumn, MSGS.recentMessagesGrid_ColTimestamp());
		grid.getColumn(grid.getColumnCount() - 1).setSortable(true);
		ListHandler<GRecentMessage> timestampSortHandler = new ListHandler<GRecentMessage>(dataProvider.getList());
		timestampSortHandler.setComparator(timestampColumn, new Comparator<GRecentMessage>() {
			@Override
			public int compare(GRecentMessage theO1, GRecentMessage theO2) {
				long cmp = theO1.getTransactionTime().getTime() - theO2.getTransactionTime().getTime();
				if (cmp > 0) {
					return 1;
				} else if (cmp < 0) {
					return -1;
				} else {
					return 0;
				}
			}
		});
		grid.getColumnSortList().push(timestampColumn);

		// Service
		Column<GRecentMessage, SafeHtml> serviceColumn = new Column<GRecentMessage, SafeHtml>(new SafeHtmlCell()) {
			@Override
			public SafeHtml getValue(GRecentMessage theObject) {
				SafeHtmlBuilder b = new SafeHtmlBuilder();
				if (StringUtil.isNotBlank(theObject.getServiceName())) {
					b.appendEscaped(theObject.getServiceName());
				}
				if (StringUtil.isNotBlank(theObject.getServiceVersionId())) {
					b.appendHtmlConstant(" <a href=\"#" + NavProcessor.getTokenEditServiceVersion(true, theObject.getServiceVersionPid())+"\">"+theObject.getServiceVersionId()+"</a>");
				}
				return b.toSafeHtml();
			}
		};
		grid.addColumn(serviceColumn, MSGS.recentMessagesGrid_ColService());
		grid.getColumn(grid.getColumnCount() - 1).setSortable(true);
		ListHandler<GRecentMessage> serviceSortHandler = new ListHandler<GRecentMessage>(dataProvider.getList());
		serviceSortHandler.setComparator(grid.getColumn(grid.getColumnCount() - 1), new Comparator<GRecentMessage>() {
			@Override
			public int compare(GRecentMessage theO1, GRecentMessage theO2) {
				int cmp = StringUtil.defaultString(theO1.getServiceName()).compareTo(StringUtil.defaultString(theO2.getServiceName()));
				if (cmp == 0) {
					cmp = StringUtil.defaultString(theO1.getServiceVersionId()).compareTo(StringUtil.defaultString(theO2.getServiceVersionId()));
				}
				return cmp;
			}
		});

		// Method
		Column<GRecentMessage, SafeHtml> methodColumn = new Column<GRecentMessage, SafeHtml>(new SafeHtmlCell()) {
			@Override
			public SafeHtml getValue(GRecentMessage theObject) {
				SafeHtmlBuilder b = new SafeHtmlBuilder();
				if (StringUtil.isNotBlank(theObject.getMethodName())) {
					b.appendEscaped(theObject.getMethodName());
				}
				return b.toSafeHtml();
			}
		};
		grid.addColumn(methodColumn, MSGS.recentMessagesGrid_ColMethod());
		grid.getColumn(grid.getColumnCount() - 1).setSortable(true);
		ListHandler<GRecentMessage> methodSortHandler = new ListHandler<GRecentMessage>(dataProvider.getList());
		methodSortHandler.setComparator(grid.getColumn(grid.getColumnCount() - 1), new Comparator<GRecentMessage>() {
			@Override
			public int compare(GRecentMessage theO1, GRecentMessage theO2) {
				int cmp = StringUtil.defaultString(theO1.getMethodName()).compareTo(StringUtil.defaultString(theO2.getMethodName()));
				return cmp;
			}
		});

		// IP
		Column<GRecentMessage, SafeHtml> ipColumn = new Column<GRecentMessage, SafeHtml>(new SafeHtmlCell()) {
			@Override
			public SafeHtml getValue(GRecentMessage theObject) {
				return SafeHtmlUtils.fromString(theObject.getRequestHostIp());
			}
		};
		grid.addColumn(ipColumn, MSGS.recentMessagesGrid_ColIp());
		grid.getColumn(grid.getColumnCount() - 1).setSortable(true);
		ListHandler<GRecentMessage> ipSortHandler = new ListHandler<GRecentMessage>(dataProvider.getList());
		ipSortHandler.setComparator(grid.getColumn(grid.getColumnCount() - 1), new Comparator<GRecentMessage>() {
			@Override
			public int compare(GRecentMessage theO1, GRecentMessage theO2) {
				int cmp = StringUtil.defaultString(theO1.getRequestHostIp()).compareTo(StringUtil.defaultString(theO2.getRequestHostIp()));
				return cmp;
			}
		});

		// URL
		Column<GRecentMessage, SafeHtml> urlColumn = new Column<GRecentMessage, SafeHtml>(new SafeHtmlCell()) {
			@Override
			public SafeHtml getValue(GRecentMessage theObject) {
				SafeHtmlBuilder b = new SafeHtmlBuilder();
				b.appendHtmlConstant("<a href=\""+theObject.getImplementationUrlHref()+"\">"+theObject.getImplementationUrlId()+"</a>");
				return b.toSafeHtml();
			}
		};
		grid.addColumn(urlColumn, MSGS.recentMessagesGrid_ColImplementationUrl());
		grid.getColumn(grid.getColumnCount() - 1).setSortable(true);
		ListHandler<GRecentMessage> urlSortHandler = new ListHandler<GRecentMessage>(dataProvider.getList());
		urlSortHandler.setComparator(grid.getColumn(grid.getColumnCount() - 1), new Comparator<GRecentMessage>() {
			@Override
			public int compare(GRecentMessage theO1, GRecentMessage theO2) {
				int cmp = StringUtil.defaultString(theO1.getImplementationUrlId()).compareTo(StringUtil.defaultString(theO2.getImplementationUrlId()));
				return cmp;
			}
		});

		// Millis
		Column<GRecentMessage, SafeHtml> millisColumn = new Column<GRecentMessage, SafeHtml>(new SafeHtmlCell()) {
			@Override
			public SafeHtml getValue(GRecentMessage theObject) {
				SafeHtmlBuilder b = new SafeHtmlBuilder();
				b.append(theObject.getTransactionMillis());
				return b.toSafeHtml();
			}
		};
		grid.addColumn(millisColumn, MSGS.recentMessagesGrid_ColMillis());
		grid.getColumn(grid.getColumnCount() - 1).setSortable(true);
		ListHandler<GRecentMessage> millisSortHandler = new ListHandler<GRecentMessage>(dataProvider.getList());
		millisSortHandler.setComparator(grid.getColumn(grid.getColumnCount() - 1), new Comparator<GRecentMessage>() {
			@Override
			public int compare(GRecentMessage theO1, GRecentMessage theO2) {
				long cmp = (theO1.getTransactionMillis() - theO2.getTransactionMillis());
				if (cmp<0) {
					return -1;
				}else if (cmp > 0) {
					return 1;
				}else {
					return 0;
				}
			}
		});

		// Authorization
		Column<GRecentMessage, SafeHtml> authorizationColumn = new Column<GRecentMessage, SafeHtml>(new SafeHtmlCell()) {
			@Override
			public SafeHtml getValue(GRecentMessage theObject) {
				SafeHtmlBuilder b = new SafeHtmlBuilder();
				if (theObject.getAuthorizationOutcome() != null) {
					b.appendHtmlConstant(theObject.getAuthorizationOutcome().getDescription());
				}
				return b.toSafeHtml();
			}
		};
		grid.addColumn(authorizationColumn, MSGS.recentMessagesGrid_ColAuthorization());
		grid.getColumn(grid.getColumnCount() - 1).setSortable(true);
		ListHandler<GRecentMessage> authSortHandler = new ListHandler<GRecentMessage>(dataProvider.getList());
		authSortHandler.setComparator(grid.getColumn(grid.getColumnCount() - 1), new Comparator<GRecentMessage>() {
			@Override
			public int compare(GRecentMessage theO1, GRecentMessage theO2) {
				int cmp = StringUtil.defaultString(theO1.getAuthorizationOutcome().getDescription()).compareTo(StringUtil.defaultString(theO2.getAuthorizationOutcome().getDescription()));
				return cmp;
			}
		});

		// User
		Column<GRecentMessage, SafeHtml> userColumn = new Column<GRecentMessage, SafeHtml>(new SafeHtmlCell()) {
			@Override
			public SafeHtml getValue(GRecentMessage theObject) {
				SafeHtmlBuilder b = new SafeHtmlBuilder();
				if (theObject.getRequestUsername() != null) {
					b.appendHtmlConstant("<a href=\""+"#" + NavProcessor.getTokenEditUser(true, theObject.getRequestUserPid())+"\">"+theObject.getRequestUsername()+"</a>");
				}
				b.append(theObject.getTransactionMillis());
				return b.toSafeHtml();
			}
		};
		grid.addColumn(userColumn, MSGS.recentMessagesGrid_ColUser());
		grid.getColumn(grid.getColumnCount() - 1).setSortable(true);
		ListHandler<GRecentMessage> userSortHandler = new ListHandler<GRecentMessage>(dataProvider.getList());
		userSortHandler.setComparator(grid.getColumn(grid.getColumnCount() - 1), new Comparator<GRecentMessage>() {
			@Override
			public int compare(GRecentMessage theO1, GRecentMessage theO2) {
				int cmp = StringUtil.defaultString(theO1.getRequestUsername()).compareTo(StringUtil.defaultString(theO2.getRequestUsername()));
				return cmp;
			}
		});

		// Add data
		dataProvider.getList().addAll(theList);
		dataProvider.refresh();

	}
}
