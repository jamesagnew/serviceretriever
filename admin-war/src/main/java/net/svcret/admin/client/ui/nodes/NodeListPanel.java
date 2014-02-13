package net.svcret.admin.client.ui.nodes;

import net.svcret.admin.client.AdminPortal;
import net.svcret.admin.client.MyResources;
import net.svcret.admin.client.ui.components.CssConstants;
import net.svcret.admin.client.ui.components.LoadingSpinner;
import net.svcret.admin.client.ui.components.PCellTable;
import net.svcret.admin.shared.DateUtil;
import net.svcret.admin.shared.model.DtoMonitorRuleActiveCheckOutcome;
import net.svcret.admin.shared.model.DtoNodeStatus;

import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;

public class NodeListPanel extends FlowPanel {

	private LoadingSpinner myLoadingSpinner;
	private PCellTable<DtoNodeStatus> myListGrid;
	private ListDataProvider<DtoNodeStatus> myOutcomesDataProvider;

	public NodeListPanel() {
		FlowPanel topPanel = new FlowPanel();
		add(topPanel);

		topPanel.setStylePrimaryName(CssConstants.MAIN_PANEL);

		Label titleLabel = new Label("Nodes");
		titleLabel.setStyleName(CssConstants.MAIN_PANEL_TITLE);
		topPanel.add(titleLabel);

		FlowPanel contentPanel = new FlowPanel();
		contentPanel.addStyleName(CssConstants.CONTENT_INNER_PANEL);
		topPanel.add(contentPanel);

		myLoadingSpinner = new LoadingSpinner();
		myLoadingSpinner.show();
		contentPanel.add(myLoadingSpinner);

		
	}
	
	
	private void initOutcomesList(FlowPanel theContentPanel) {

		myListGrid = new PCellTable<>();
		myListGrid.setEmptyTableWidget(new Label("This check has not yet been executed"));
		theContentPanel.add(myListGrid);

		final SingleSelectionModel<DtoNodeStatus> selectionModel = new SingleSelectionModel<>();
	    myListGrid.setSelectionModel(selectionModel);
	    selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
	      @Override
		public void onSelectionChange(SelectionChangeEvent event) {
	        DtoNodeStatus selected = selectionModel.getSelectedObject();
	        setSelectedNode(selected);
	      }
	    });
	    
		myOutcomesDataProvider = new ListDataProvider<>();
				
		// ID

		Column<DtoNodeStatus, SafeHtml> timeCell = new Column<DtoNodeStatus, SafeHtml>(new SafeHtmlCell()) {
			@Override
			public SafeHtml getValue(DtoNodeStatus theObject) {
				return SafeHtmlUtils.fromString(theObject.getNodeId());
			}
		};
		myListGrid.addColumn(timeCell, "Node ID");

		// Status

		Column<DtoNodeStatus, SafeHtml> statusCell = new Column<DtoNodeStatus, SafeHtml>(new SafeHtmlCell()) {
			@Override
			public SafeHtml getValue(DtoNodeStatus theObject) {
				switch (theObject.getStatus()) {
				case ACTIVE:
					return SafeHtmlUtils.fromTrustedString("Active");
				case DOWN:
					return SafeHtmlUtils.fromTrustedString("Down");
				case NO_REQUESTS:
					return SafeHtmlUtils.fromTrustedString("Alive but inactive");
				case RECENTLY_STARTED:
					return SafeHtmlUtils.fromTrustedString("Recently restarted");
				}
				return null;
			}
		};
		myListGrid.addColumn(statusCell, "Status");

		// Status

		Column<DtoNodeStatus, SafeHtml> throughputCell = new Column<DtoNodeStatus, SafeHtml>(new SafeHtmlCell()) {
			@Override
			public SafeHtml getValue(DtoNodeStatus theObject) {
				switch (theObject.getStatus()) {
				case ACTIVE:
				case RECENTLY_STARTED:
					SafeHtmlBuilder b = new SafeHtmlBuilder();
					b.append(theObject.getTransactionsSuccessfulPerMinute());
					
				case DOWN:
					if (theObject.getTimeElapsedSinceDown() == null) {
						return SafeHtmlUtils.fromTrustedString("");
					}else {
						return SafeHtmlUtils.fromTrustedString("Down for " + DateUtil.formatTimeElapsed(theObject.getTimeElapsedSinceDown(), false));
					}
				case NO_REQUESTS:
					if (theObject.getTimeElapsedSinceDown() == null) {
						return SafeHtmlUtils.fromTrustedString("");
					}else {
						return SafeHtmlUtils.fromTrustedString("No requests for " + DateUtil.formatTimeElapsed(theObject.getTimeElapsedSinceLastTx(), false));
					}
				}
				return null;
			}
		};
		myListGrid.addColumn(throughputCell, "Throughput");

		myOutcomesDataProvider.addDataDisplay(myListGrid);
	
	}


	protected void setSelectedNode(DtoNodeStatus theSelected) {
		// TODO Auto-generated method stub
		
	}

}
