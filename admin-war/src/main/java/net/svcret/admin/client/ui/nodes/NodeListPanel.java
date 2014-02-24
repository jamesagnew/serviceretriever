package net.svcret.admin.client.ui.nodes;

import net.svcret.admin.client.AdminPortal;
import net.svcret.admin.client.ui.components.CssConstants;
import net.svcret.admin.client.ui.components.LoadingSpinner;
import net.svcret.admin.client.ui.components.MemorySparkline;
import net.svcret.admin.client.ui.components.PCellTable;
import net.svcret.admin.client.ui.components.UsageSparkline;
import net.svcret.admin.shared.DateUtil;
import net.svcret.admin.shared.Model;
import net.svcret.admin.shared.model.DtoNodeStatistics;
import net.svcret.admin.shared.model.DtoNodeStatus;
import net.svcret.admin.shared.model.DtoNodeStatusAndStatisticsList;

import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;

public class NodeListPanel extends FlowPanel {

	private LoadingSpinner myLoadingSpinner;
	private PCellTable<DtoNodeStatus> myListGrid;
	private ListDataProvider<DtoNodeStatus> myOutcomesDataProvider;
	private DtoNodeStatusAndStatisticsList myNodeStatusAndStatisticsList;

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

		initNodesList(contentPanel);
		
		AdminPortal.MODEL_SVC.loadNodeListAndStatistics(new AsyncCallback<DtoNodeStatusAndStatisticsList>() {
			@Override
			public void onFailure(Throwable theArg0) {
				Model.handleFailure(theArg0);
			}

			@Override
			public void onSuccess(DtoNodeStatusAndStatisticsList theArg0) {
				myLoadingSpinner.hideCompletely();
				myNodeStatusAndStatisticsList = theArg0;
				myOutcomesDataProvider.getList().clear();
				myOutcomesDataProvider.getList().addAll(theArg0.getNodeStatuses());
			}});
		
		
	}
	
	
	private void initNodesList(FlowPanel theContentPanel) {

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
					return SafeHtmlUtils.fromTrustedString("Alive");
				case DOWN:
					return SafeHtmlUtils.fromTrustedString("Down");
				case NO_REQUESTS:
					return SafeHtmlUtils.fromTrustedString("Alive (no TX)");
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
				SafeHtmlBuilder b = new SafeHtmlBuilder();
				switch (theObject.getStatus()) {
				case ACTIVE:
				case RECENTLY_STARTED:
					
					DtoNodeStatistics stats = myNodeStatusAndStatisticsList.getNodeStatisticsForNodeId(theObject.getNodeId());
					int[] success=stats.getSuccessTransactions();
					int[] fault=stats.getFaultTransactions();
					int[] fail=stats.getFailTransactions();
					int[] secFail=stats.getSecFailTransactions();
					UsageSparkline.renderTransactionGraphsAsHtml(b, success, fault, fail, secFail, "45px", "30px");
					b.appendHtmlConstant("<br/>");
					
					b.append(theObject.getTransactionsSuccessfulPerMinute());
					b.appendEscaped(" successful/min");
					
					if (theObject.getTransactionsFaultPerMinute() > 0.0) {
						b.appendHtmlConstant("<br/>");
						b.append(theObject.getTransactionsFaultPerMinute());
						b.appendEscaped(" faults/min");
					}
					if (theObject.getTransactionsFailPerMinute() > 0.0) {
						b.appendHtmlConstant("<br/>");
						b.append(theObject.getTransactionsFailPerMinute());
						b.appendEscaped(" failed transactions/min");
					}
					if (theObject.getTransactionsSecurityFailPerMinute() > 0.0) {
						b.appendHtmlConstant("<br/>");
						b.append(theObject.getTransactionsSecurityFailPerMinute());
						b.appendEscaped(" security failures/min");
					}
					break;
				case DOWN:
					if (theObject.getTimeElapsedSinceDown() == null) {
						b.appendHtmlConstant("");
					}else {
						b.appendHtmlConstant("Down for " + DateUtil.formatTimeElapsed(theObject.getTimeElapsedSinceDown(), false));
					}
					break;
				case NO_REQUESTS:
					if (theObject.getTimeElapsedSinceDown() == null) {
						b.appendHtmlConstant("");
					}else {
						b.appendHtmlConstant("No requests for " + DateUtil.formatTimeElapsed(theObject.getTimeElapsedSinceLastTx(), false));
					}
					break;
				}
				return b.toSafeHtml();
			}
		};
		myListGrid.addColumn(throughputCell, "Throughput");

//		// Throughput
//
//		Column<DtoNodeStatus, SafeHtml> throughputCell = new Column<DtoNodeStatus, SafeHtml>(new SafeHtmlCell()) {
//			@Override
//			public SafeHtml getValue(DtoNodeStatus theObject) {
//				SafeHtmlBuilder b = new SafeHtmlBuilder();
//				DtoNodeStatistics stats = myNodeStatusAndStatisticsList.getNodeStatisticsForNodeId(theObject.getNodeId());
//				MemorySparkline.renderTransactionGraphsAsHtml(b, stats.getMemoryUsed(), stats.getMemoryMax());
//				return b.toSafeHtml();
//			}
//		};
//		myListGrid.addColumn(throughputCell, "Throughput");

		// Memory

		Column<DtoNodeStatus, SafeHtml> memoryCell = new Column<DtoNodeStatus, SafeHtml>(new SafeHtmlCell()) {
			@Override
			public SafeHtml getValue(DtoNodeStatus theObject) {
				SafeHtmlBuilder b = new SafeHtmlBuilder();
				DtoNodeStatistics stats = myNodeStatusAndStatisticsList.getNodeStatisticsForNodeId(theObject.getNodeId());
				MemorySparkline.renderTransactionGraphsAsHtml(b, stats.getMemoryUsed(), stats.getMemoryMax(), "35px", "30px");
				return b.toSafeHtml();
			}
		};
		myListGrid.addColumn(memoryCell, "Memory");

		myOutcomesDataProvider.addDataDisplay(myListGrid);
	
	}


	protected void setSelectedNode(DtoNodeStatus theSelected) {
		// TODO Auto-generated method stub
		
	}

}
