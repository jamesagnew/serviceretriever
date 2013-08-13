package net.svcret.admin.client.ui.config.lib;

import static net.svcret.admin.client.AdminPortal.*;

import java.util.Collection;
import java.util.Set;

import net.svcret.admin.client.nav.NavProcessor;
import net.svcret.admin.client.ui.alert.AlertGrid;
import net.svcret.admin.client.ui.components.CssConstants;
import net.svcret.admin.client.ui.components.PButtonCell;
import net.svcret.admin.client.ui.components.PCellTable;
import net.svcret.admin.client.ui.config.svcver.NullColumn;
import net.svcret.admin.shared.model.DtoLibraryMessage;
import net.svcret.admin.shared.model.GDomainList;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.SimplePager.TextLocation;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.view.client.ListDataProvider;

public abstract class BaseMessageLibraryTablePanel extends FlowPanel {
	private GDomainList myDomainList;
	private ListDataProvider<DtoLibraryMessage> myDataProvider;
	private CellTable<DtoLibraryMessage> myGrid;

	public BaseMessageLibraryTablePanel(GDomainList theDomainList) {
		myDomainList = theDomainList;

		myGrid = new PCellTable<DtoLibraryMessage>();
		myGrid.setWidth("100%");
		add(myGrid);

		myGrid.setEmptyTableWidget(new Label("No messages in library"));

		// Create a Pager to control the table.
		SimplePager.Resources pagerResources = GWT.create(SimplePager.Resources.class);
		SimplePager pager = new SimplePager(TextLocation.CENTER, pagerResources, false, 0, true);
		pager.setDisplay(myGrid);
		pager.setPageSize(5);
		add(pager);

		Column<DtoLibraryMessage, String> editColumn = new NullColumn<DtoLibraryMessage>(new PButtonCell(IMAGES.iconEdit(), MSGS.actions_Edit()));
		myGrid.addColumn(editColumn, "");
		editColumn.setFieldUpdater(new FieldUpdater<DtoLibraryMessage, String>() {
			@Override
			public void update(int theIndex, DtoLibraryMessage theObject, String theValue) {
				History.newItem(NavProcessor.getTokenEditLibraryMessage(true, theObject.getPid()));
			}
		});
		editColumn.setCellStyleNames(CssConstants.PCELLTABLE_ACTION_COLUMN);

		Column<DtoLibraryMessage, String> replayColumn = new NullColumn<DtoLibraryMessage>(new PButtonCell(IMAGES.iconPlay16(), MSGS.actions_Replay()));
		myGrid.addColumn(replayColumn, "");
		replayColumn.setFieldUpdater(new FieldUpdater<DtoLibraryMessage, String>() {
			@Override
			public void update(int theIndex, DtoLibraryMessage theObject, String theValue) {
				long svcVerPid = toSvcVerPid(theObject.getAppliesToServiceVersionPids());
				String token = NavProcessor.getTokenReplayLibraryMessage(true, svcVerPid, theObject.getPid());
				History.newItem(token);
			}
		});
		replayColumn.setCellStyleNames(CssConstants.PCELLTABLE_ACTION_COLUMN);
		
		Column<DtoLibraryMessage, SafeHtml> descColumn = new Column<DtoLibraryMessage, SafeHtml>(new SafeHtmlCell()) {
			@Override
			public SafeHtml getValue(DtoLibraryMessage theObject) {
				return SafeHtmlUtils.fromTrustedString(theObject.getDescription());
			}
		};
		myGrid.addColumn(descColumn, "Description");

		Column<DtoLibraryMessage, SafeHtml> ctColumn = new Column<DtoLibraryMessage, SafeHtml>(new SafeHtmlCell()) {
			@Override
			public SafeHtml getValue(DtoLibraryMessage theObject) {
				return SafeHtmlUtils.fromTrustedString(theObject.getContentType());
			}
		};
		myGrid.addColumn(ctColumn, "Content Type");

		Column<DtoLibraryMessage, SafeHtml> messageColumn = new Column<DtoLibraryMessage, SafeHtml>(new SafeHtmlCell()) {
			@Override
			public SafeHtml getValue(DtoLibraryMessage theObject) {
				return SafeHtmlUtils.fromTrustedString(theObject.getMessageLength() + " chars");
			}
		};
		myGrid.addColumn(messageColumn, "Message");

		Column<DtoLibraryMessage, SafeHtml> appliesToColumn = new Column<DtoLibraryMessage, SafeHtml>(new SafeHtmlCell()) {
			@Override
			public SafeHtml getValue(DtoLibraryMessage theObject) {
				SafeHtmlBuilder safeHtmlBuilder = new SafeHtmlBuilder();
				AlertGrid.createAppliesToHtml(safeHtmlBuilder, theObject.getAppliesToServiceVersionPids(), myDomainList);
				return safeHtmlBuilder.toSafeHtml();
			}
		};
		myGrid.addColumn(appliesToColumn, "Applies To");

		myDataProvider = new ListDataProvider<DtoLibraryMessage>();
		myDataProvider.addDataDisplay(myGrid);

	}

	protected void setMessages(Collection<DtoLibraryMessage> theMessages) {
		myDataProvider.getList().clear();
		myDataProvider.getList().addAll(theMessages);
		myDataProvider.refresh();
		
		myGrid.setRowCount(theMessages.size(), true);
	}
	
	protected abstract long toSvcVerPid(Set<Long> theAppliesToServiceVersionPids);

}
