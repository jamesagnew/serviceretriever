package net.svcret.admin.client.ui.config.lib;

import static net.svcret.admin.client.AdminPortal.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import net.svcret.admin.client.AdminPortal;
import net.svcret.admin.client.nav.NavProcessor;
import net.svcret.admin.client.ui.alert.AlertGrid;
import net.svcret.admin.client.ui.components.CssConstants;
import net.svcret.admin.client.ui.components.LoadingSpinner;
import net.svcret.admin.client.ui.components.PButton;
import net.svcret.admin.client.ui.components.PButtonCell;
import net.svcret.admin.client.ui.components.PCellTable;
import net.svcret.admin.client.ui.components.VersionPickerPanel;
import net.svcret.admin.client.ui.components.VersionPickerPanel.ChangeListener;
import net.svcret.admin.client.ui.config.svcver.NullColumn;
import net.svcret.admin.shared.IAsyncLoadCallback;
import net.svcret.admin.shared.Model;
import net.svcret.admin.shared.model.DtoLibraryMessage;
import net.svcret.admin.shared.model.GDomainList;
import net.svcret.admin.shared.model.HierarchyEnum;
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
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.IdentityColumn;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.SimplePager.TextLocation;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.view.client.ListDataProvider;

public class MessageLibraryPanel extends FlowPanel {

	private FlowPanel myContentPanel;
	private LoadingSpinner myDataLoadingSpinner;
	private ListDataProvider<DtoLibraryMessage> myDataProvider;
	private GDomainList myDomainList;
	private PCellTable<DtoLibraryMessage> myGrid;
	private LoadingSpinner myInitialLoadingSpinner;
	private Long myInitialPid;
	private HierarchyEnum myInitialType;
	private FlowPanel myTopPanel;
	private VersionPickerPanel myVersionPicker;

	public MessageLibraryPanel() {
		this(null, null);
	}

	public MessageLibraryPanel(HierarchyEnum theInitialType, Long theInitialPid) {
		myInitialType = theInitialType;
		myInitialPid = theInitialPid;

		myTopPanel = new FlowPanel();
		add(myTopPanel);

		myTopPanel.setStylePrimaryName(CssConstants.MAIN_PANEL);

		Label titleLabel = new Label("Message Library");
		titleLabel.setStyleName(CssConstants.MAIN_PANEL_TITLE);
		myTopPanel.add(titleLabel);

		myContentPanel = new FlowPanel();
		myContentPanel.addStyleName(CssConstants.CONTENT_INNER_PANEL);
		myTopPanel.add(myContentPanel);

		Label intro = new Label("The message library stores transactions for a specific version of a " + "service so that they can be used as examples, and replayed for testing.");
		myContentPanel.add(intro);

		myInitialLoadingSpinner = new LoadingSpinner();
		myInitialLoadingSpinner.show();
		myContentPanel.add(myInitialLoadingSpinner);

		Model.getInstance().loadDomainList(new IAsyncLoadCallback<GDomainList>() {
			@Override
			public void onSuccess(final GDomainList theDomainList) {
				myDomainList = theDomainList;
				myInitialLoadingSpinner.hideCompletely();
				initUi();

				// AdminPortal.MODEL_SVC.loadLibraryMessagesForServiveVersion(thePid,
				// new AsyncCallback<Collection<DtoLibraryMessage>>() {
				// @Override
				// public void onFailure(Throwable theCaught) {
				// Model.handleFailure(theCaught);
				// }
				//
				// @Override
				// public void onSuccess(Collection<DtoLibraryMessage>
				// theMessages) {
				// myLoadingSpinner.hideCompletely();
				// }
				//
				// });
			}
		});

	}

	private void handleSelectedEntryChange() {
		GWT.log("Going to load library messages");

		myDataLoadingSpinner.show();
		myDataProvider.getList().clear();
		myDataProvider.refresh();

		Long selectedDomainPid = myVersionPicker.getSelectedDomainPid();
		if (selectedDomainPid != VersionPickerPanel.ALL_PID) {
			Long selectedServicePid = myVersionPicker.getSelectedServicePid();
			if (selectedServicePid != VersionPickerPanel.ALL_PID) {
				Long selectedVersionPid = myVersionPicker.getSelectedVersionPid();
				if (selectedVersionPid != VersionPickerPanel.ALL_PID) {
					AdminPortal.MODEL_SVC.loadLibraryMessages(HierarchyEnum.VERSION, selectedVersionPid, new MyLoadCallback());
				} else {
					AdminPortal.MODEL_SVC.loadLibraryMessages(HierarchyEnum.SERVICE, selectedServicePid, new MyLoadCallback());
				}
			} else {
				AdminPortal.MODEL_SVC.loadLibraryMessages(HierarchyEnum.DOMAIN, selectedDomainPid, new MyLoadCallback());
			}
		} else {
			AdminPortal.MODEL_SVC.loadLibraryMessages(new MyLoadCallback());
		}

	}

	private void initUi() {

		myVersionPicker = new VersionPickerPanel(myDomainList, true);
		myContentPanel.add(myVersionPicker);

		if (myInitialType != null) {
			switch (myInitialType) {
			case DOMAIN:
				myVersionPicker.tryToSelectDomain(myInitialPid);
				break;
			case SERVICE:
				myVersionPicker.tryToSelectService(myInitialPid);
				break;
			case METHOD:
				// should not happen
			case VERSION:
				myVersionPicker.tryToSelectServiceVersion(myInitialPid);
				break;
			}
		}

		myDataLoadingSpinner = new LoadingSpinner();
		myContentPanel.add(myDataLoadingSpinner);

		myGrid = new PCellTable<DtoLibraryMessage>();
		myGrid.setWidth("100%");
		myContentPanel.add(myGrid);

		myGrid.setEmptyTableWidget(new Label("No messages found"));

		myDataProvider = new ListDataProvider<DtoLibraryMessage>();
		myDataProvider.addDataDisplay(myGrid);

		Column<DtoLibraryMessage, String> editColumn = new NullColumn<DtoLibraryMessage>(new PButtonCell(IMAGES.iconEdit(), MSGS.actions_Edit()));
		editColumn.setFieldUpdater(new FieldUpdater<DtoLibraryMessage, String>() {
			@Override
			public void update(int theIndex, DtoLibraryMessage theObject, String theValue) {
				History.newItem(NavProcessor.getTokenEditLibraryMessage(true, theObject.getPid()));
			}
		});
		editColumn.setCellStyleNames(CssConstants.PCELLTABLE_ACTION_COLUMN);
		Column<DtoLibraryMessage, String> replayColumn = new NullColumn<DtoLibraryMessage>(new PButtonCell(IMAGES.iconPlay16(), MSGS.actions_Replay()));
		replayColumn.setFieldUpdater(new FieldUpdater<DtoLibraryMessage, String>() {
			@Override
			public void update(int theIndex, DtoLibraryMessage theObject, String theValue) {
				long svcVerPid = tryToSelectAppropriateReplayDestinationForMessage(theObject.getAppliesToServiceVersionPids());
				String token = NavProcessor.getTokenReplayLibraryMessage(true, svcVerPid, theObject.getPid());
				History.newItem(token);
			}
		});
		replayColumn.setCellStyleNames(CssConstants.PCELLTABLE_ACTION_COLUMN);
		List<HasCell<DtoLibraryMessage, ?>> actionsCells = new ArrayList<HasCell<DtoLibraryMessage, ?>>();
		actionsCells.add(editColumn);
		actionsCells.add(replayColumn);
		IdentityColumn<DtoLibraryMessage> actionsColumn = new IdentityColumn<DtoLibraryMessage>(new CompositeCell<DtoLibraryMessage>(actionsCells));
		myGrid.addColumn(actionsColumn, "");

		Column<DtoLibraryMessage, SafeHtml> descColumn = new Column<DtoLibraryMessage, SafeHtml>(new SafeHtmlCell()) {
			@Override
			public SafeHtml getValue(DtoLibraryMessage theObject) {
				return SafeHtmlUtils.fromTrustedString(theObject.getDescription());
			}
		};
		myGrid.addColumn(descColumn, "Description");
		myGrid.getColumn(myGrid.getColumnCount() - 1).setSortable(true);
		ListHandler<DtoLibraryMessage> serviceSortHandler = new ListHandler<DtoLibraryMessage>(myDataProvider.getList());
		serviceSortHandler.setComparator(myGrid.getColumn(myGrid.getColumnCount() - 1), new Comparator<DtoLibraryMessage>() {
			@Override
			public int compare(DtoLibraryMessage theO1, DtoLibraryMessage theO2) {
				return StringUtil.compare(theO1.getDescription(), theO2.getDescription());
			}
		});
		myGrid.getColumnSortList().push(descColumn);

		Column<DtoLibraryMessage, SafeHtml> ctColumn = new Column<DtoLibraryMessage, SafeHtml>(new SafeHtmlCell()) {
			@Override
			public SafeHtml getValue(DtoLibraryMessage theObject) {
				return SafeHtmlUtils.fromTrustedString(theObject.getContentType());
			}
		};
		myGrid.addColumn(ctColumn, "Content Type");
		myGrid.getColumn(myGrid.getColumnCount() - 1).setSortable(true);
		ListHandler<DtoLibraryMessage> ctSortHandler = new ListHandler<DtoLibraryMessage>(myDataProvider.getList());
		ctSortHandler.setComparator(myGrid.getColumn(myGrid.getColumnCount() - 1), new Comparator<DtoLibraryMessage>() {
			@Override
			public int compare(DtoLibraryMessage theO1, DtoLibraryMessage theO2) {
				return StringUtil.compare(theO1.getContentType(), theO2.getContentType());
			}
		});

		Column<DtoLibraryMessage, SafeHtml> messageColumn = new Column<DtoLibraryMessage, SafeHtml>(new SafeHtmlCell()) {
			@Override
			public SafeHtml getValue(DtoLibraryMessage theObject) {
				return SafeHtmlUtils.fromTrustedString(theObject.getMessageLength() + " chars");
			}
		};
		myGrid.addColumn(messageColumn, "Message");
		myGrid.getColumn(myGrid.getColumnCount() - 1).setSortable(true);
		ListHandler<DtoLibraryMessage> msgSortHandler = new ListHandler<DtoLibraryMessage>(myDataProvider.getList());
		msgSortHandler.setComparator(myGrid.getColumn(myGrid.getColumnCount() - 1), new Comparator<DtoLibraryMessage>() {
			@Override
			public int compare(DtoLibraryMessage theO1, DtoLibraryMessage theO2) {
				return theO1.getMessageLength() - theO2.getMessageLength();
			}
		});

		Column<DtoLibraryMessage, SafeHtml> appliesToColumn = new Column<DtoLibraryMessage, SafeHtml>(new SafeHtmlCell()) {
			@Override
			public SafeHtml getValue(DtoLibraryMessage theObject) {
				SafeHtmlBuilder safeHtmlBuilder = new SafeHtmlBuilder();
				AlertGrid.createAppliesToHtml(safeHtmlBuilder, theObject.getAppliesToServiceVersionPids(), myDomainList);
				return safeHtmlBuilder.toSafeHtml();
			}
		};
		myGrid.addColumn(appliesToColumn, "Applies To");
		myGrid.getColumn(myGrid.getColumnCount() - 1).setSortable(true);
		ListHandler<DtoLibraryMessage> atSortHandler = new ListHandler<DtoLibraryMessage>(myDataProvider.getList());
		atSortHandler.setComparator(myGrid.getColumn(myGrid.getColumnCount() - 1), new Comparator<DtoLibraryMessage>() {
			@Override
			public int compare(DtoLibraryMessage theO1, DtoLibraryMessage theO2) {
				return StringUtil.compare(theO1.getAppliesToSortText(myDomainList), theO2.getAppliesToSortText(myDomainList));
			}
		});

		handleSelectedEntryChange();
		myVersionPicker.addVersionChangeHandler(new ChangeListener() {
			@Override
			public void onChange(Long theDomainPid, Long theServicePid, Long theServiceVersionPid) {
				handleSelectedEntryChange();
			}
		});

		// Create a Pager to control the table.
		SimplePager.Resources pagerResources = GWT.create(SimplePager.Resources.class);
		SimplePager pager = new SimplePager(TextLocation.CENTER, pagerResources, false, 0, true);
		pager.setDisplay(myGrid);
		pager.setPageSize(10);
		myContentPanel.add(pager);

		// Actions
		PButton addButton = new PButton(AdminPortal.IMAGES.iconAdd(), AdminPortal.MSGS.actions_Add());
		addButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent theEvent) {
				Long pid = myVersionPicker.getSelectedVersionPid();
				if (pid != null && pid != VersionPickerPanel.ALL_PID) {
					History.newItem(NavProcessor.getTokenMessageLibraryAdd(true, HierarchyEnum.VERSION, pid));
				} else {
					pid = myVersionPicker.getSelectedServicePid();
					if (pid != null && pid != VersionPickerPanel.ALL_PID) {
						History.newItem(NavProcessor.getTokenMessageLibraryAdd(true, HierarchyEnum.SERVICE, pid));
					} else {
						pid = myVersionPicker.getSelectedDomainPid();
						if (pid != null && pid != VersionPickerPanel.ALL_PID) {
							History.newItem(NavProcessor.getTokenMessageLibraryAdd(true, HierarchyEnum.DOMAIN, pid));
						} else {
							History.newItem(NavProcessor.getTokenMessageLibraryAdd(true));
						}
					}
				}
			}
		});
		myContentPanel.add(addButton);

	}

	private long tryToSelectAppropriateReplayDestinationForMessage(Set<Long> theAppliesToServiceVersionPids) {
		Long selectedDomainPid = myVersionPicker.getSelectedDomainPid();
		if (selectedDomainPid == VersionPickerPanel.ALL_PID) {
			return theAppliesToServiceVersionPids.iterator().next();
		} else {
			Long selectedServicePid = myVersionPicker.getSelectedServicePid();
			if (selectedServicePid == VersionPickerPanel.ALL_PID) {
				return theAppliesToServiceVersionPids.iterator().next();
			} else {
				Long selectedVersionPid = myVersionPicker.getSelectedVersionPid();
				if (theAppliesToServiceVersionPids.contains(selectedVersionPid)) {
					return selectedDomainPid;
				} else {
					return theAppliesToServiceVersionPids.iterator().next();
				}
			}
		}
	}

	public class MyLoadCallback implements AsyncCallback<Collection<DtoLibraryMessage>> {

		@Override
		public void onFailure(Throwable theCaught) {
			myDataLoadingSpinner.hide();
			Model.handleFailure(theCaught);
		}

		@Override
		public void onSuccess(Collection<DtoLibraryMessage> theResult) {
			myDataLoadingSpinner.hide();
			myDataProvider.getList().clear();
			myDataProvider.getList().addAll(theResult);
			myDataProvider.refresh();
		}

	}
}
