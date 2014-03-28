package net.svcret.admin.client.ui.config.monitor;

import net.svcret.admin.client.AdminPortal;
import net.svcret.admin.client.MyResources;
import net.svcret.admin.client.ui.components.CssConstants;
import net.svcret.admin.client.ui.components.LoadingSpinner;
import net.svcret.admin.client.ui.components.PCellTable;
import net.svcret.admin.client.ui.components.TwoColumnGrid;
import net.svcret.admin.shared.DateUtil;
import net.svcret.admin.shared.Model;
import net.svcret.admin.shared.ObjectUtil;
import net.svcret.admin.shared.model.BaseDtoMonitorRule;
import net.svcret.admin.shared.model.DtoMonitorRuleActive;
import net.svcret.admin.shared.model.DtoMonitorRuleActiveCheck;
import net.svcret.admin.shared.model.DtoMonitorRuleActiveCheckOutcome;
import net.svcret.admin.shared.model.DtoMonitorRuleActiveCheckOutcomeList;

import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;

public class ViewActiveCheckOutcomePanel extends FlowPanel {

	private long myActiveCheckPid;
	private long myUrlPid;
	private DtoMonitorRuleActiveCheckOutcomeList myRecentOutcomesList;
	private ListDataProvider<DtoMonitorRuleActiveCheckOutcome> myOutcomesDataProvider;
	private PCellTable<DtoMonitorRuleActiveCheckOutcome> myOutcomeListGrid;
	private DtoMonitorRuleActiveCheckOutcome mySelectedOutcome;
	private ViewActiveCheckOutcomeDetailPanel myDetailPanel;

	public ViewActiveCheckOutcomePanel(final long theRulePid, long theActiveCheckPid, long theUrlPid) {
		myActiveCheckPid = theActiveCheckPid;
		myUrlPid = theUrlPid;
		
		FlowPanel introPanel = new FlowPanel();
		introPanel.setStylePrimaryName(CssConstants.MAIN_PANEL);
		add(introPanel);

		Label titleLabel = new Label("View Active Check Outcome Details");
		titleLabel.setStyleName(CssConstants.MAIN_PANEL_TITLE);
		introPanel.add(titleLabel);

		final FlowPanel contentPanel = new FlowPanel();
		contentPanel.addStyleName(CssConstants.CONTENT_INNER_PANEL);
		introPanel.add(contentPanel);

		final LoadingSpinner spinner = new LoadingSpinner();
		spinner.show();
		contentPanel.add(spinner);
		
		AdminPortal.MODEL_SVC.loadMonitorRule(theRulePid, new AsyncCallback<BaseDtoMonitorRule>(){
			@Override
			public void onSuccess(BaseDtoMonitorRule theResult) {
				if (!(theResult instanceof DtoMonitorRuleActive)) {
					Model.handleFailure(new Exception("Rule "+theRulePid+ " is not an active rule!"));
					return;
				}
				initContents(contentPanel, (DtoMonitorRuleActive) theResult);
				spinner.hideCompletely();
			}

			@Override
			public void onFailure(Throwable theCaught) {
				Model.handleFailure(theCaught);
			}});
		
	}

	protected void initContents(FlowPanel theContentPanel, DtoMonitorRuleActive theRule) {

		TwoColumnGrid grid = new TwoColumnGrid();
		theContentPanel.add(grid);

		grid.addRow("Rule Name", theRule.getName());
		grid.addRow("Enabled", theRule.isActive() ? AdminPortal.MSGS.yes() : AdminPortal.MSGS.no());

//		HorizontalPanel controlsPanel = new HorizontalPanel();
//		theContentPanel.add(controlsPanel);
//
//		PButton saveButton = new PButton(AdminPortal.IMAGES.iconSave(), AdminPortal.MSGS.actions_Save());
//		saveButton.addClickHandler(new ClickHandler() {
//			@Override
//			public void onClick(ClickEvent theEvent) {
//				save();
//			}
//		});
//		controlsPanel.add(saveButton);

		DtoMonitorRuleActiveCheck check = theRule.getCheckList().getCheckWithPid(myActiveCheckPid);
		for (DtoMonitorRuleActiveCheckOutcomeList next : check.getRecentOutcomesForUrl()) {
			if (next.getUrlPid() == myUrlPid) {
				myRecentOutcomesList = next;
			}
		}
		
		initOutcomesList(theContentPanel);
		myOutcomesDataProvider.getList().addAll(myRecentOutcomesList.getOutcomesFromMostRecent());
		myOutcomesDataProvider.refresh();
		
		if (!myOutcomesDataProvider.getList().isEmpty()) {
			myOutcomeListGrid.getSelectionModel().setSelected(myOutcomesDataProvider.getList().get(0), true);
		}
		
	}

	private void initOutcomesList(FlowPanel theContentPanel) {

		myOutcomeListGrid = new PCellTable<>();
		myOutcomeListGrid.setEmptyTableWidget(new Label("This check has not yet been executed"));
		theContentPanel.add(myOutcomeListGrid);

		final SingleSelectionModel<DtoMonitorRuleActiveCheckOutcome> selectionModel = new SingleSelectionModel<>();
	    myOutcomeListGrid.setSelectionModel(selectionModel);
	    selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
	      @Override
		public void onSelectionChange(SelectionChangeEvent event) {
	        DtoMonitorRuleActiveCheckOutcome selected = selectionModel.getSelectedObject();
	        setSelectedOutcome(selected);
	      }
	    });
	    
		myOutcomesDataProvider = new ListDataProvider<>();
				
		// Time

		Column<DtoMonitorRuleActiveCheckOutcome, SafeHtml> timeCell = new Column<DtoMonitorRuleActiveCheckOutcome, SafeHtml>(new SafeHtmlCell()) {
			@Override
			public SafeHtml getValue(DtoMonitorRuleActiveCheckOutcome theObject) {
				return SafeHtmlUtils.fromTrustedString(DateUtil.formatTime(theObject.getTransactionTime()));
			}
		};
		myOutcomeListGrid.addColumn(timeCell, "Time");

		// Outcome

		Column<DtoMonitorRuleActiveCheckOutcome, SafeHtml> outcomeCell = new Column<DtoMonitorRuleActiveCheckOutcome, SafeHtml>(new SafeHtmlCell()) {
			@Override
			public SafeHtml getValue(DtoMonitorRuleActiveCheckOutcome theObject) {
				String css;
				String msg;
				if (!theObject.isFailed()) {
					css = MyResources.CSS.activeCheckViewOutcomesListSuccess();
					msg = AdminPortal.MSGS.generic_OutcomeSuccess();
				}else {
					css = MyResources.CSS.activeCheckViewOutcomesListFailure();
					msg=AdminPortal.MSGS.generic_OutcomeFail();
				}
				String retVal = "<span class=\"" + css + "\">" + msg + "</span>";
				return SafeHtmlUtils.fromTrustedString(retVal);
			}
		};
		myOutcomeListGrid.addColumn(outcomeCell, "Outcome");
		myOutcomesDataProvider.addDataDisplay(myOutcomeListGrid);
	
	}

	private void setSelectedOutcome(DtoMonitorRuleActiveCheckOutcome theSelected) {
		if (theSelected == null || ObjectUtil.equals(mySelectedOutcome, theSelected)) {
			return;
		}
		mySelectedOutcome = theSelected;
		
		if (myDetailPanel == null) {
			myDetailPanel = new ViewActiveCheckOutcomeDetailPanel();
			add(myDetailPanel);
		}
		
		myDetailPanel.showSpinner();
		AdminPortal.MODEL_SVC.loadMonitorRuleActiveCheckOutcomeDetails(mySelectedOutcome.getPid(), new AsyncCallback<DtoMonitorRuleActiveCheckOutcome>() {

			@Override
			public void onFailure(Throwable theCaught) {
				Model.handleFailure(theCaught);
			}

			@Override
			public void onSuccess(DtoMonitorRuleActiveCheckOutcome theResult) {
				myDetailPanel.setMessage(theResult);
			}
		});
		
	}

}
