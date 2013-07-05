package net.svcret.admin.client.ui.dash;

import java.util.ArrayList;
import java.util.List;

import net.svcret.admin.client.ui.components.CssConstants;
import net.svcret.admin.client.ui.components.EmptyCell;
import net.svcret.admin.client.ui.components.LoadingSpinner;
import net.svcret.admin.client.ui.dash.model.DashModelDomain;
import net.svcret.admin.client.ui.dash.model.DashModelLoading;
import net.svcret.admin.client.ui.dash.model.DashModelService;
import net.svcret.admin.client.ui.dash.model.DashModelServiceMethod;
import net.svcret.admin.client.ui.dash.model.DashModelServiceVersion;
import net.svcret.admin.client.ui.dash.model.IDashModel;
import net.svcret.admin.shared.IAsyncLoadCallback;
import net.svcret.admin.shared.Model;
import net.svcret.admin.shared.model.BaseGServiceVersion;
import net.svcret.admin.shared.model.GDomain;
import net.svcret.admin.shared.model.GDomainList;
import net.svcret.admin.shared.model.GService;
import net.svcret.admin.shared.model.GServiceMethod;
import net.svcret.admin.shared.model.HierarchyEnum;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class ServiceDashboardPanel extends FlowPanel {

	private static final int COL_ACTIONS = 7;
	private static final int COL_BACKING_URLS = 4;
	private static final int COL_LAST_INVOC = 5;
	private static final int COL_LATENCY = 3;
	private static final int COL_SECURITY = 6;
	private static final int COL_STATUS = 1;
	private static final int COL_USAGE = 2;

	private static final int NUM_STATUS_COLS = 6;

	private FlexTable myGrid;
	private LoadingSpinner myLoadingSpinner;
	private List<IDashModel> myUiList = new ArrayList<IDashModel>();

	public ServiceDashboardPanel() {
		setStylePrimaryName(CssConstants.MAIN_PANEL);

		Label titleLabel = new Label("Service Dashboard");
		titleLabel.setStyleName(CssConstants.MAIN_PANEL_TITLE);
		add(titleLabel);

		// TreeViewModel viewModel = new DashboardTreeViewModel();
		// Object rootValue = Model.getInstance().getDomainList();
		// myDashboardTree = new CellTree(viewModel, rootValue);
		// add(myDashboardTree);

		myLoadingSpinner = new LoadingSpinner();
		myLoadingSpinner.show();
		add(myLoadingSpinner);

		myGrid = new FlexTable();
		add(myGrid);

		myGrid.addStyleName("dashboardTable");

		myGrid.setText(0, 0, "Name");
		myGrid.getFlexCellFormatter().setColSpan(0, 0, HierarchyEnum.getHighestOrdinal() + 2);
		myGrid.setText(0, COL_STATUS, "Status");
		myGrid.setText(0, COL_USAGE, "Usage 1hr Avg");
		myGrid.setText(0, COL_LATENCY, "Latency 1hr Avg");
		myGrid.setText(0, COL_BACKING_URLS, "Backing URLs");
		myGrid.setText(0, COL_LAST_INVOC, "Last Invoc");
		myGrid.setText(0, COL_SECURITY, "Security");
		myGrid.setText(0, COL_ACTIONS, "Actions");

		updateView();
	}

	public void updateView() {
		Model.getInstance().loadDomainList(new IAsyncLoadCallback<GDomainList>() {
			@Override
			public void onSuccess(GDomainList theResult) {
				updateView(theResult);
			}
		});
	}

	public void updateView(GDomainList theDomainList) {
		myLoadingSpinner.hideCompletely();

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
										newUiList.add(new DashModelServiceVersion(nextDomain, nextService, nextServiceVersion));

										if (nextServiceVersion.isExpandedOnDashboard()) {
											for (GServiceMethod nextMethod : nextServiceVersion.getMethodList()) {
												if (!nextMethod.isStatsInitialized()) {
													addSpinnerToList(newUiList);
													haveStatsToLoad = true;
												} else {
													newUiList.add(new DashModelServiceMethod(nextMethod));
												}
											}
										}

									}

								}

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
		}
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

			for (int col = 0; col < offset + 1; col++) {
				myGrid.setWidget(i + rowOffset, col, null);
			}

			int colSpan = (HierarchyEnum.getHighestOrdinal() - offset + 1);
			// boolean expanded = false;

			boolean hideMostOfRow = model.getModel() != null && model.getModel().isExpandedOnDashboard() && model.getModel().hideDashboardRowWhenExpanded();
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

}
