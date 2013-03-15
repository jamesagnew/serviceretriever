package ca.uhn.sail.proxy.admin.client.ui.dash;

import java.util.ArrayList;
import java.util.List;

import ca.uhn.sail.proxy.admin.client.ui.components.EmptyCell;
import ca.uhn.sail.proxy.admin.client.ui.dash.model.DashModelDomain;
import ca.uhn.sail.proxy.admin.client.ui.dash.model.DashModelLoading;
import ca.uhn.sail.proxy.admin.client.ui.dash.model.DashModelService;
import ca.uhn.sail.proxy.admin.client.ui.dash.model.DashModelServiceMethod;
import ca.uhn.sail.proxy.admin.client.ui.dash.model.DashModelServiceVersion;
import ca.uhn.sail.proxy.admin.client.ui.dash.model.IDashModel;
import ca.uhn.sail.proxy.admin.shared.model.BaseGList;
import ca.uhn.sail.proxy.admin.shared.model.GDomain;
import ca.uhn.sail.proxy.admin.shared.model.GService;
import ca.uhn.sail.proxy.admin.shared.model.GServiceMethod;
import ca.uhn.sail.proxy.admin.shared.model.BaseGServiceVersion;
import ca.uhn.sail.proxy.admin.shared.model.HierarchyEnum;
import ca.uhn.sail.proxy.admin.shared.model.Model;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class ServiceDashboardPanel extends FlowPanel {

	private static final int COL_STATUS = HierarchyEnum.getHighestOrdinal() + 1;
	private static final int COL_USAGE = HierarchyEnum.getHighestOrdinal() + 2;

	private FlexTable myGrid;
	private List<IDashModel> myUiList = new ArrayList<IDashModel>();
	private BaseGList<GDomain> myDomainList;

	public ServiceDashboardPanel() {
		setStylePrimaryName("mainPanel");

		Label titleLabel = new Label("Service Dashboard");
		titleLabel.setStyleName("mainPanelTitle");
		add(titleLabel);

		// TreeViewModel viewModel = new DashboardTreeViewModel();
		// Object rootValue = Model.getInstance().getDomainList();
		// myDashboardTree = new CellTree(viewModel, rootValue);
		// add(myDashboardTree);

		myGrid = new FlexTable();
		add(myGrid);

		myDomainList = Model.getInstance().getDomainList();

		myGrid.addStyleName("dashboardTable");

		myGrid.setText(0, 0, "Name");
		myGrid.getFlexCellFormatter().setColSpan(0, 0, HierarchyEnum.getHighestOrdinal() + 2);
		myGrid.setText(0, 1, "Status");
		myGrid.setText(0, 2, "Usage");
		myGrid.setText(0, 3, "Backing URLs");
		myGrid.setText(0, 4, "Actions");

		updateView();

	}

	public void updateView() {

		ArrayList<IDashModel> newUiList = new ArrayList<IDashModel>();
		
//		if (newUiList.size() == 0) {
//			
//			if (!myDomainList.isInitialized()) {
//				newUiList.add(new DashModelLoading(this, myDomainList));
//			} else {
//				for (GDomain next : myDomainList) {
//					newUiList.add(new DashModelDomain(next));
//				}
//			}
//			
//		} else {
//			
//		}
		
		if (!myDomainList.isInitialized()) {

			newUiList.add(new DashModelLoading(this, myDomainList));

		} else {

			for (GDomain nextDomain : myDomainList) {
				DashModelDomain nextUiObject = new DashModelDomain(nextDomain);
				newUiList.add(nextUiObject);
				
				if (nextDomain.isExpandedOnDashboard()) {
					if (!nextDomain.getServiceList().isInitialized()) {
						newUiList.add(new DashModelLoading(this, nextDomain.getServiceList()));
					} else {
						
						for (GService nextService : nextDomain.getServiceList()) {
							newUiList.add(new DashModelService(nextService));
							
							if (nextService.isExpandedOnDashboard()) {
								if (!nextService.getVersionList().isInitialized()) {
									newUiList.add(new DashModelLoading(this, nextService.getVersionList()));
								} else {
									
									for (BaseGServiceVersion nextServiceVersion : nextService.getVersionList()) {
										newUiList.add(new DashModelServiceVersion(nextServiceVersion));
										
										if (nextServiceVersion.isExpandedOnDashboard()) {
											if (!nextServiceVersion.getMethodList().isInitialized()) {
												newUiList.add(new DashModelLoading(this, nextServiceVersion.getMethodList()));
											} else {
												for (GServiceMethod nextMethod : nextServiceVersion.getMethodList()) {
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
	}

	private void updateRows(ArrayList<IDashModel> theNewUiList) {
		int rowOffset = 1;

		for (int i = 0; i < theNewUiList.size(); i++) {
			IDashModel model = theNewUiList.get(i);

			HierarchyEnum type = model.getType();
			int offset = type.getOrdinal();
			
			boolean newRow = false;
			if ((myUiList.size() - 1) <= i || !myUiList.get(i).equals(model)) {
				myGrid.insertRow(i + rowOffset);
				newRow = true;
			}

			for (int col = 0; col < offset + 1; col++) {
				myGrid.getFlexCellFormatter().setColSpan(i + rowOffset, col, 1);
			}
			int colSpan = (HierarchyEnum.getHighestOrdinal() - offset + 1);
			boolean expanded = false;
			if (model.getModel() != null && model.getModel().isExpandedOnDashboard()) {
				colSpan += 3;
				expanded = true;
			}
			myGrid.getFlexCellFormatter().setColSpan(i + rowOffset, offset + 1, colSpan);
			for (int col = offset + 2; col < myGrid.getCellCount(i + rowOffset); col++) {
				myGrid.getFlexCellFormatter().setColSpan(i + rowOffset, col, 1);
			}
			
			if (model.getModel() != null) {
				myGrid.setWidget(i + rowOffset, offset, new ExpandButton(this, model));
			} else {
				myGrid.setWidget(i + rowOffset, offset, null);
			}
			
			myGrid.getCellFormatter().addStyleName(i + rowOffset, offset, "dashboardTableExpandoCell");
			
			Widget rendered = EmptyCell.defaultWidget(model.renderName());
			String styleName = model.getCellStyle();
			myGrid.setWidget(i + rowOffset, offset + 1, rendered);
			myGrid.getCellFormatter().addStyleName(i + rowOffset, offset + 1, styleName);
			
			if (expanded) {
				while (myGrid.getCellCount(i + rowOffset) > offset + 2) {
					myGrid.removeCell(i + rowOffset, offset + 2);
				}

				Widget actions = EmptyCell.defaultWidget(model.renderActions());
				myGrid.setWidget(i + rowOffset, offset + 2, actions);
				myGrid.getCellFormatter().addStyleName(i + rowOffset, offset + 2, styleName);

				continue;
			}
			
			Widget status = EmptyCell.defaultWidget(model.renderStatus());
			myGrid.setWidget(i + rowOffset, offset + 2, status);
			myGrid.getCellFormatter().addStyleName(i + rowOffset, offset + 2, styleName);

			Widget usage = EmptyCell.defaultWidget(model.renderUsage());
			myGrid.setWidget(i + rowOffset, offset + 3, usage);
			myGrid.getCellFormatter().addStyleName(i + rowOffset, offset + 3, styleName);

			Widget urls = EmptyCell.defaultWidget(model.renderUrls());
			myGrid.setWidget(i + rowOffset, offset + 4, urls);
			myGrid.getCellFormatter().addStyleName(i + rowOffset, offset + 4, styleName);
			
			Widget actions = EmptyCell.defaultWidget(model.renderActions());
			myGrid.setWidget(i + rowOffset, offset + 5, actions);
			myGrid.getCellFormatter().addStyleName(i + rowOffset, offset + 5, styleName);

		}

		while (myGrid.getRowCount() - rowOffset > theNewUiList.size()) {
			myGrid.removeRow(myGrid.getRowCount() - 1);
		}

		myUiList = theNewUiList;
	}

}
