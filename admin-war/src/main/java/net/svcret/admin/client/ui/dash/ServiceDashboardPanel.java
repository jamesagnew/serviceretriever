package net.svcret.admin.client.ui.dash;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.svcret.admin.client.AdminPortal;
import net.svcret.admin.client.MyResources;
import net.svcret.admin.client.ui.components.CssConstants;
import net.svcret.admin.client.ui.components.EmptyCell;
import net.svcret.admin.client.ui.components.IProvidesTooltip;
import net.svcret.admin.client.ui.components.TooltipListener.Tooltip;
import net.svcret.admin.client.ui.dash.model.DashModelDomain;
import net.svcret.admin.client.ui.dash.model.DashModelLoading;
import net.svcret.admin.client.ui.dash.model.DashModelService;
import net.svcret.admin.client.ui.dash.model.DashModelServiceMethod;
import net.svcret.admin.client.ui.dash.model.DashModelServiceVersion;
import net.svcret.admin.client.ui.dash.model.IDashModel;
import net.svcret.admin.client.ui.stats.DateUtil;
import net.svcret.admin.shared.IAsyncLoadCallback;
import net.svcret.admin.shared.Model;
import net.svcret.admin.shared.model.BaseGDashboardObject;
import net.svcret.admin.shared.model.BaseGServiceVersion;
import net.svcret.admin.shared.model.GDomain;
import net.svcret.admin.shared.model.GDomainList;
import net.svcret.admin.shared.model.GService;
import net.svcret.admin.shared.model.GServiceMethod;
import net.svcret.admin.shared.model.HierarchyEnum;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class ServiceDashboardPanel extends FlowPanel implements IDestroyable {

	private static final int COL_ACTIONS = 7;
	private static final int COL_BACKING_URLS = 4;
	private static final int COL_LAST_INVOC = 5;
	private static final int COL_LATENCY = 3;
	private static final int COL_SECURITY = 6;
	private static final int COL_STATUS = 1;
	private static final int COL_USAGE = 2;

	private static final int NUM_STATUS_COLS = 6;

	private MyTable myGrid;
	private List<IDashModel> myUiList = new ArrayList<IDashModel>();
	private Label myLastUpdateLabel;
	private Timer myTimer;
	private boolean myUpdating;
	private Image myReloadButton;
	private Label myTimeSinceLastUpdateLabel;
	private Date myLastUpdate;
	private Timer myLastUpdateTimer;

	public ServiceDashboardPanel() {
		Model.getInstance().flushStats();

		setStylePrimaryName(CssConstants.MAIN_PANEL);

		HorizontalPanel titlePanel = new HorizontalPanel();
		titlePanel.setStyleName(CssConstants.MAIN_PANEL_TITLE);
		add(titlePanel);

		Label titleLabel = new Label("Service Dashboard");
		titleLabel.addStyleName(CssConstants.MAIN_PANEL_TITLE_TEXT);
		titlePanel.add(titleLabel);

		HTML spacer = new HTML("&nbsp;");
		titlePanel.add(spacer);
		titlePanel.setCellWidth(spacer, "100%");

		myLastUpdateLabel = new Label();
		myLastUpdateLabel.addStyleName(CssConstants.MAIN_PANEL_UPDATE);
		titlePanel.add(myLastUpdateLabel);
		titlePanel.setCellVerticalAlignment(myLastUpdateLabel, HasVerticalAlignment.ALIGN_MIDDLE);

		myTimeSinceLastUpdateLabel = new Label();
		myTimeSinceLastUpdateLabel.addStyleName(MyResources.CSS.dashboardTimeSinceLastUpdateLabel());
		titlePanel.add(myTimeSinceLastUpdateLabel);

		myReloadButton = new Image(AdminPortal.IMAGES.iconReload16());
		myReloadButton.addStyleName(MyResources.CSS.dashboardReloadButton());
		myReloadButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent theEvent) {
				myReloadButton.setResource(AdminPortal.IMAGES.dashboardSpinner());
				myUpdating = true;
				Model.getInstance().loadDomainListAndStats(new IAsyncLoadCallback<GDomainList>() {
					@Override
					public void onSuccess(GDomainList theResult) {
						myUpdating = false;
						updateView(theResult);
					}
				});
			}
		});
		titlePanel.add(myReloadButton);

		myGrid = new MyTable();
		myGrid.setCellPadding(2);
		myGrid.setCellSpacing(0);
		add(myGrid);

		myGrid.addStyleName("dashboardTable");
		myGrid.getRowFormatter().addStyleName(0, CssConstants.DASHBOARD_TABLE_HEADER);

		myGrid.setText(0, 0, "Name");
		myGrid.getFlexCellFormatter().setColSpan(0, 0, HierarchyEnum.getHighestOrdinal() + 2);
		myGrid.setText(0, COL_STATUS, "Monitoring");
		myGrid.setText(0, COL_USAGE, "Usage 1hr Avg");
		myGrid.setText(0, COL_LATENCY, "Latency 1hr Avg");
		myGrid.setText(0, COL_BACKING_URLS, "Backing URLs");
		myGrid.setText(0, COL_LAST_INVOC, "Last Invoc");
		myGrid.setText(0, COL_SECURITY, "Security");
		myGrid.setText(0, COL_ACTIONS, null);

		updateView();

		myTimer = new Timer() {
			@Override
			public void run() {
				if (myUpdating) {
					return;
				}
				myReloadButton.setResource(AdminPortal.IMAGES.dashboardSpinner());
				myUpdating = true;
				Model.getInstance().loadDomainListAndStats(new IAsyncLoadCallback<GDomainList>() {
					@Override
					public void onSuccess(GDomainList theResult) {
						myUpdating = false;
						updateView(theResult);
					}
				});
			}
		};
		myTimer.scheduleRepeating(30 * 1000);

		myLastUpdateTimer = new Timer() {

			@Override
			public void run() {
				if (myLastUpdate != null) {
					myTimeSinceLastUpdateLabel.setText(DateUtil.formatTimeElapsedForLastInvocation(myLastUpdate, true));
				}
			}
		};
		myLastUpdateTimer.scheduleRepeating(1000);

	}

	public void updateView() {
		if (myUpdating) {
			return;
		}
		myReloadButton.setResource(AdminPortal.IMAGES.dashboardSpinner());
		myUpdating = true;
		Model.getInstance().loadDomainList(new IAsyncLoadCallback<GDomainList>() {
			@Override
			public void onSuccess(GDomainList theResult) {
				myUpdating = false;
				updateView(theResult);
			}
		});
	}

	public void updateView(GDomainList theDomainList) {
		ArrayList<IDashModel> newUiList = new ArrayList<IDashModel>();

		boolean haveStatsToLoad = false;
		for (GDomain nextDomain : theDomainList) {
			if (!nextDomain.isStatsInitialized()) {
				addSpinnerToList(newUiList);
				haveStatsToLoad = true;
			} else {
				DashModelDomain nextUiObject = new DashModelDomain(nextDomain);
				newUiList.add(nextUiObject);

				if (nextDomain.isExpandedOnDashboard()) {
					for (GService nextService : nextDomain.getServiceList()) {
						if (!nextService.isStatsInitialized()) {
							addSpinnerToList(newUiList);
							haveStatsToLoad = true;
						} else {
							newUiList.add(new DashModelService(nextDomain, nextService));

							if (nextService.isExpandedOnDashboard()) {

								for (BaseGServiceVersion nextServiceVersion : nextService.getVersionList()) {
									if (!nextServiceVersion.isStatsInitialized()) {
										addSpinnerToList(newUiList);
										haveStatsToLoad = true;
									} else {
										newUiList.add(new DashModelServiceVersion(nextService, nextServiceVersion));

										if (nextServiceVersion.isExpandedOnDashboard()) {
											haveStatsToLoad = addServiceVersionChildren(newUiList, haveStatsToLoad, nextServiceVersion);
										}

									}

								} // for service versions

							}

						}

					}
				}
			}
		}

		updateRows(newUiList);

		if (haveStatsToLoad) {
			Model.getInstance().loadDomainListAndStats(new IAsyncLoadCallback<GDomainList>() {
				@Override
				public void onSuccess(GDomainList theResult) {
					updateView(theResult);
				}
			});
		} else {
			myReloadButton.setResource(AdminPortal.IMAGES.iconReload16());
			myLastUpdateLabel.setText("Updated " + DateTimeFormat.getFormat(PredefinedFormat.TIME_MEDIUM).format(new Date()));
			myLastUpdate = new Date();
		}
	}

	private boolean addServiceVersionChildren(ArrayList<IDashModel> newUiList, boolean haveStatsToLoad, BaseGServiceVersion nextServiceVersion) {
		for (GServiceMethod nextMethod : nextServiceVersion.getMethodList()) {
			if (!nextMethod.isStatsInitialized()) {
				addSpinnerToList(newUiList);
				haveStatsToLoad = true;
			} else {
				newUiList.add(new DashModelServiceMethod(nextMethod));
			}
		}
		return haveStatsToLoad;
	}

	private void addSpinnerToList(ArrayList<IDashModel> newUiList) {
		if (newUiList.size() > 0 && newUiList.get(newUiList.size() - 1) instanceof DashModelLoading) {
			// Don't add more than one in a row
			return;
		}
		newUiList.add(new DashModelLoading());
	}

	private void updateRows(ArrayList<IDashModel> theNewUiList) {
		int rowOffset = 1;

		for (int i = 0; i < theNewUiList.size(); i++) {
			IDashModel model = theNewUiList.get(i);

			HierarchyEnum type = model.getType();
			int offset = type.getOrdinal();

			if ((myUiList.size() - 1) <= i || !myUiList.get(i).equals(model)) {
				int beforeRow = i + rowOffset + 1;
				if (myGrid.getRowCount() <= beforeRow) {
					// don't need it
				} else {
					myGrid.insertRow(beforeRow);
				}
			}

			// Clear existing row contents
			for (int col = 0; col < offset + 1; col++) {
				myGrid.setWidget(i + rowOffset, col, null);
			}
			myGrid.clearTooltipRow(i + rowOffset);

			int colSpan = (HierarchyEnum.getHighestOrdinal() - offset + 1);

			boolean hideMostOfRow = false;
			if (hideMostOfRow) {
				colSpan += NUM_STATUS_COLS;
				// expanded = true;
			}
			for (int col = 0; col < (offset + 1); col++) {
				myGrid.getFlexCellFormatter().setColSpan(i + rowOffset, col, 1);
			}
			myGrid.getFlexCellFormatter().setColSpan(i + rowOffset, offset + 1, colSpan);
			for (int col = offset + 2; col < myGrid.getCellCount(i + rowOffset); col++) {
				myGrid.getFlexCellFormatter().setColSpan(i + rowOffset, col, 1);
			}

			// for (int col = 0; col < offset;col++) {
			// myGrid.setWidget(i+rowOffset, col, null);
			// }

			if (model.getModel() != null) {
				myGrid.setWidget(i + rowOffset, offset, new ExpandButton(this, model));
			} else {
				myGrid.setWidget(i + rowOffset, offset, null);
			}

			myGrid.getCellFormatter().setStyleName(i + rowOffset, offset, "dashboardTableExpandoCell");

			Widget rendered = EmptyCell.defaultWidget(model.renderName());
			String styleName = model.getCellStyle();
			myGrid.setWidget(i + rowOffset, offset + 1, rendered);
			myGrid.getCellFormatter().setStyleName(i + rowOffset, offset + 1, styleName);

			if (hideMostOfRow) {
				while (myGrid.getCellCount(i + rowOffset) > offset + 2) {
					myGrid.removeCell(i + rowOffset, offset + 2);
				}

				Widget actions = EmptyCell.defaultWidget(model.renderActions());
				myGrid.setWidget(i + rowOffset, offset + 2, actions);
				myGrid.getCellFormatter().addStyleName(i + rowOffset, offset + 2, styleName);

				continue;
			}

			Widget status = EmptyCell.defaultWidget(model.renderStatus());
			myGrid.setWidget(i + rowOffset, offset + COL_STATUS + 1, status);
			myGrid.getCellFormatter().setStyleName(i + rowOffset, offset + COL_STATUS + 1, styleName);

			Widget usage = EmptyCell.defaultWidget(model.renderUsage());
			myGrid.setWidget(i + rowOffset, offset + COL_USAGE + 1, usage);
			myGrid.getCellFormatter().setStyleName(i + rowOffset, offset + COL_USAGE + 1, styleName);
			myGrid.setTooltipProvider(i + rowOffset, offset + COL_USAGE + 1, model.getUsageTooltip());

			Widget urls = EmptyCell.defaultWidget(model.renderUrls());
			myGrid.setWidget(i + rowOffset, offset + COL_BACKING_URLS + 1, urls);
			myGrid.getCellFormatter().setStyleName(i + rowOffset, offset + COL_BACKING_URLS + 1, styleName);

			Widget latency = EmptyCell.defaultWidget(model.renderLatency());
			myGrid.setWidget(i + rowOffset, offset + COL_LATENCY + 1, latency);
			myGrid.getCellFormatter().setStyleName(i + rowOffset, offset + COL_LATENCY + 1, styleName);

			Widget security = EmptyCell.defaultWidget(model.renderSecurity());
			myGrid.setWidget(i + rowOffset, offset + COL_SECURITY + 1, security);
			myGrid.getCellFormatter().setStyleName(i + rowOffset, offset + COL_SECURITY + 1, styleName);

			Widget lastInvoc = EmptyCell.defaultWidget(model.renderLastInvocation());
			myGrid.setWidget(i + rowOffset, offset + COL_LAST_INVOC + 1, lastInvoc);
			myGrid.getCellFormatter().setStyleName(i + rowOffset, offset + COL_LAST_INVOC + 1, styleName);

			Widget actions = EmptyCell.defaultWidget(model.renderActions());
			myGrid.setWidget(i + rowOffset, offset + COL_ACTIONS + 1, actions);
			myGrid.getCellFormatter().setStyleName(i + rowOffset, offset + COL_ACTIONS + 1, styleName);

			while (myGrid.getCellCount(i + rowOffset) > (offset + COL_ACTIONS + 2)) {
				myGrid.removeCell(i + rowOffset, myGrid.getCellCount(i + rowOffset) - 1);
			}

		}

		while (myGrid.getRowCount() - rowOffset > theNewUiList.size()) {
			myGrid.removeRow(myGrid.getRowCount() - 1);
		}

		myUiList = theNewUiList;
	}

	@Override
	public void destroy() {
		myTimer.cancel();
		myLastUpdateTimer.cancel();
	}

	private class MyTable extends FlexTable {
		public MyTable() {
			sinkEvents(Event.ONMOUSEOVER | Event.ONMOUSEOUT);
		}

		private List<List<IProvidesTooltip<BaseGDashboardObject>>> myTooltipProviders = new ArrayList<List<IProvidesTooltip<BaseGDashboardObject>>>();

		public void setTooltipProvider(int theRow, int theCol, IProvidesTooltip<BaseGDashboardObject> theTooltipProvider) {
			ensureTooltipRow(theRow);
			ensureTooltipCol(theRow, theCol);
			myTooltipProviders.get(theRow).set(theCol, theTooltipProvider);
		}

		public void clearTooltipRow(int theRow) {
			ensureTooltipRow(theRow);
			for (int i = 0; i < myTooltipProviders.get(theRow).size(); i++) {
				myTooltipProviders.get(theRow).set(i, null);
			}
		}

		private void ensureTooltipCol(int theRow, int theCol) {
			while (myTooltipProviders.get(theRow).size() <= theCol) {
				myTooltipProviders.get(theRow).add(null);
			}
		}

		private void ensureTooltipRow(int theRow) {
			while (myTooltipProviders.size() <= theRow) {
				myTooltipProviders.add(new ArrayList< IProvidesTooltip<BaseGDashboardObject>>());
			}
		}

		private int myCurrentTooltipRow;
		private int myCurrentTooltipCol;
		private Tooltip myCurrentTooltip;

		@Override
		public void onBrowserEvent(Event theEvent) {
			super.onBrowserEvent(theEvent);

			Element td = getEventTargetCell(theEvent);
			if (td == null || !"TD".equalsIgnoreCase(td.getTagName())) {
				return;
			}

			Element tr = (Element) td.getParentNode();
			if (tr==null || !"TR".equalsIgnoreCase(tr.getTagName())) {
				return;
			}
			
			int rowStr = indexWithinParent(tr);
			int colStr = indexWithinParent(td);

			switch (DOM.eventGetType(theEvent)) {
			case Event.ONMOUSEOVER: {
				GWT.log("Mouseover row " + rowStr + " col " + colStr);
				if (rowStr == myCurrentTooltipRow && colStr == myCurrentTooltipCol) {
					return;
				}
				if (myCurrentTooltip != null) {
					myCurrentTooltip.hideTooltip();
					myCurrentTooltip = null;
				}

				if (myTooltipProviders.size() > rowStr) {
					List< IProvidesTooltip<BaseGDashboardObject>> cols = myTooltipProviders.get(rowStr);
					if (cols.size() > colStr) {
						 IProvidesTooltip<BaseGDashboardObject> col = cols.get(colStr);
						if (col != null) {
							int index = rowStr-1;
							IDashModel dashModel = myUiList.get(index);
							Widget tooltipContents = col.getTooltip(dashModel.getModel());
							if (tooltipContents != null) {
								GWT.log("Showing tooltip for row " + rowStr + " col " + colStr);
								Tooltip tooltip = new Tooltip(td, tooltipContents);
								tooltip.displayPopup();
								myCurrentTooltip = tooltip;
							}
						}
					}
				}

				myCurrentTooltipCol = colStr;
				myCurrentTooltipRow = rowStr;
				break;
			}
			case Event.ONMOUSEOUT: {
				GWT.log("Mouseout  row " + rowStr + " col " + colStr);
				if (rowStr == myCurrentTooltipRow && colStr == myCurrentTooltipCol) {
					if (myCurrentTooltip != null) {
						GWT.log("Hiding tooltip for row " + rowStr + " col " + colStr);
						myCurrentTooltip.hideTooltip();
						myCurrentTooltip = null;
						myCurrentTooltipCol=-1;
						myCurrentTooltipRow=-1;
					}
				}
				break;
			}
			}

		}

	}

	public int indexWithinParent(Element theElement) {
		Element parent = (Element)theElement.getParentNode();
		for (int i = 0; i < parent.getChildCount(); i++) {
			if (parent.getChild(i).equals(theElement)) {
				return i;
			}
		}
		return 0;
	}

}
