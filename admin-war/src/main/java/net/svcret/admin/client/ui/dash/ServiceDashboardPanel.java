package net.svcret.admin.client.ui.dash;

import java.util.ArrayList;
import java.util.List;

import net.svcret.admin.client.ui.components.CssConstants;
import net.svcret.admin.client.ui.components.EmptyCell;
import net.svcret.admin.client.ui.components.FlexTableWithTooltips;
import net.svcret.admin.client.ui.dash.model.DashModelDomain;
import net.svcret.admin.client.ui.dash.model.DashModelLoading;
import net.svcret.admin.client.ui.dash.model.DashModelService;
import net.svcret.admin.client.ui.dash.model.DashModelServiceMethod;
import net.svcret.admin.client.ui.dash.model.DashModelServiceVersion;
import net.svcret.admin.client.ui.dash.model.IDashModel;
import net.svcret.admin.shared.IAsyncLoadCallback;
import net.svcret.admin.shared.Model;
import net.svcret.admin.shared.model.BaseDtoDashboardObject;
import net.svcret.admin.shared.model.BaseDtoServiceVersion;
import net.svcret.admin.shared.model.DtoDomain;
import net.svcret.admin.shared.model.GDomainList;
import net.svcret.admin.shared.model.GService;
import net.svcret.admin.shared.model.GServiceMethod;
import net.svcret.admin.shared.model.HierarchyEnum;

import com.google.gwt.user.client.ui.Widget;

public class ServiceDashboardPanel extends BaseDashboardPanel{

	private static final int COL_ACTIONS = 7;
	private static final int COL_BACKING_URLS = 4;
	private static final int COL_LAST_INVOC = 5;
	private static final int COL_LATENCY = 3;
	private static final int COL_SECURITY = 6;
	private static final int COL_STATUS = 1;
	private static final int COL_USAGE = 2;
	private static final int NUM_STATUS_COLS = 6;
	private static ServiceDashboardPanel ourInstance;

	private FlexTableWithTooltips<BaseDtoDashboardObject> myGrid;
	private List<IDashModel> myUiList = new ArrayList<IDashModel>();
	private List<BaseDtoDashboardObject> myUiModelItems = new ArrayList<BaseDtoDashboardObject>();

	/**
	 * This class is a singleton so that it can keep updating
	 * in the background
	 */
	private ServiceDashboardPanel() {
		super();
		
		myGrid = new FlexTableWithTooltips<BaseDtoDashboardObject>(myUiModelItems);
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

	}
	
	private boolean addServiceVersionChildren(ArrayList<IDashModel> newUiList, boolean haveStatsToLoad, BaseDtoServiceVersion nextServiceVersion) {
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

		int peakLatency = 0;
		for (int i = 0; i < theNewUiList.size(); i++) {
			IDashModel model = theNewUiList.get(i);
			peakLatency = Math.max(peakLatency, model.getPeakLatency());
		}
		
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
			myGrid.setTooltipProvider(i + rowOffset, offset + COL_BACKING_URLS + 1, model.getUrlsTooltip());

			Widget latency = EmptyCell.defaultWidget(model.renderLatency(peakLatency));
			myGrid.setWidget(i + rowOffset, offset + COL_LATENCY + 1, latency);
			myGrid.getCellFormatter().setStyleName(i + rowOffset, offset + COL_LATENCY + 1, styleName);
			myGrid.setTooltipProvider(i + rowOffset, offset + COL_LATENCY + 1, model.getUsageTooltip());

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

		myUiModelItems.clear();
		for (IDashModel next : theNewUiList) {
			myUiModelItems.add(next.getModel());
		}
		myUiList = theNewUiList;
	}

	@Override
	public void updateView(GDomainList theDomainList) {
		ArrayList<IDashModel> newUiList = new ArrayList<IDashModel>();

		boolean haveStatsToLoad = false;
		for (DtoDomain nextDomain : theDomainList) {
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

								for (BaseDtoServiceVersion nextServiceVersion : nextService.getVersionList()) {
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
			updatingFinished();
		}
	}

	/**
	 * This class is a singleton so that it can keep updating
	 * in the background
	 */
	public static ServiceDashboardPanel getInstance() {
		if (ourInstance == null) {
			ourInstance = new ServiceDashboardPanel();
		}
		return ourInstance;
	}


}
