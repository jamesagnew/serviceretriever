package net.svcret.admin.client.ui.stats;

import static net.svcret.admin.client.AdminPortal.*;

import java.util.Date;
import java.util.List;

import net.svcret.admin.client.AdminPortal;
import net.svcret.admin.client.ui.components.CssConstants;
import net.svcret.admin.client.ui.components.HtmlH1;
import net.svcret.admin.client.ui.components.LoadingSpinner;
import net.svcret.admin.client.ui.dash.model.BaseDashModel;
import net.svcret.admin.shared.IAsyncLoadCallback;
import net.svcret.admin.shared.Model;
import net.svcret.admin.shared.model.BaseGServiceVersion;
import net.svcret.admin.shared.model.GRecentMessageLists;
import net.svcret.admin.shared.model.GServiceVersionUrl;
import net.svcret.admin.shared.model.GUrlStatus;
import net.svcret.admin.shared.model.TimeRangeEnum;
import net.svcret.admin.shared.util.ChartParams;
import net.svcret.admin.shared.util.ChartTypeEnum;
import net.svcret.admin.shared.util.StringUtil;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;

public class ServiceVersionStatsPanel extends FlowPanel {

	private static DateTimeFormat ourDateTimeFormat = DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_MEDIUM);
	private LoadingSpinner myTopLoadingSpinner;
	private long myServiceVersionPid;
	private Label myTitleLabel;
	private FlowPanel myTopPanel;
	private FlowPanel myRecentMessagesPanel;
	private LoadingSpinner myRecentMessagesLoadingSpinner;
	private FlowPanel myChartsPanel;
	private HorizontalPanel myGraphsTimePanel;

	public ServiceVersionStatsPanel(final long theDomainPid, final long theServicePid, long theVersionPid) {
		myServiceVersionPid = theVersionPid;

		myTopPanel = new FlowPanel();
		add(myTopPanel);

		myTopPanel.setStylePrimaryName(CssConstants.MAIN_PANEL);

		myTitleLabel = new Label(MSGS.serviceVersionStats_Title(""));
		myTitleLabel.setStyleName(CssConstants.MAIN_PANEL_TITLE);
		myTopPanel.add(myTitleLabel);

		myTopLoadingSpinner = new LoadingSpinner();
		myTopLoadingSpinner.show();
		myTopPanel.add(myTopLoadingSpinner);

		Model.getInstance().loadServiceVersion(theDomainPid, theServicePid, theVersionPid, true, new IAsyncLoadCallback<BaseGServiceVersion>() {
			@Override
			public void onSuccess(BaseGServiceVersion theResult) {
				set01ServiceVersion(theDomainPid, theServicePid, theResult);
			}
		});

	}

	private String renderDate(Date theDate) {
		if (theDate == null) {
			return null;
		}
		return ourDateTimeFormat.format(theDate);
	}

	private void set01ServiceVersion(final long theDomainPid, final long theServicePid, final BaseGServiceVersion theResult) {
		myTitleLabel.setText(MSGS.serviceVersionStats_Title(theResult.getName()));

		myTopPanel.add(new ServiceVersionIndividualStatusPanel(theDomainPid, theServicePid, theResult.getPid()));

		AdminPortal.MODEL_SVC.loadServiceVersionUrlStatuses(myServiceVersionPid, new AsyncCallback<List<GUrlStatus>>() {

			@Override
			public void onFailure(Throwable theCaught) {
				Model.handleFailure(theCaught);
			}

			@Override
			public void onSuccess(List<GUrlStatus> theUrlStatuses) {
				set02UrlStatuses(theResult, theUrlStatuses);
			}
		});

	}

	private void set02UrlStatuses(BaseGServiceVersion theServiceVersion, List<GUrlStatus> theUrlStatuses) {
		FlowPanel urlsPanel = new FlowPanel();
		urlsPanel.setStylePrimaryName(CssConstants.MAIN_PANEL);
		add(urlsPanel);

		Label urlsTitleLabel = new Label(MSGS.serviceVersionStats_UrlsTitle());
		urlsTitleLabel.setStyleName(CssConstants.MAIN_PANEL_TITLE);
		urlsPanel.add(urlsTitleLabel);

		FlowPanel flowPanel = new FlowPanel();
		flowPanel.addStyleName(CssConstants.CONTENT_INNER_PANEL);
		urlsPanel.add(flowPanel);
		
		Grid urlGrid = new Grid(theUrlStatuses.size() + 1, 8);
		flowPanel.add(urlGrid);
		urlGrid.addStyleName(CssConstants.PROPERTY_TABLE);

		int URLTBL_COL_URL = 0;
		int URLTBL_COL_STATUS = 1;
		int URLTBL_COL_LAST_SUCCESS = 2;
		int URLTBL_COL_LAST_SUCCESS_MSG = 3;
		int URLTBL_COL_LAST_FAULT = 4;
		int URLTBL_COL_LAST_FAULT_MSG = 5;
		int URLTBL_COL_LAST_FAILURE = 6;
		int URLTBL_COL_LAST_FAILURE_MSG = 7;

		urlGrid.setText(0, URLTBL_COL_URL, "URL");
		urlGrid.setText(0, URLTBL_COL_STATUS, "Status");
		urlGrid.setText(0, URLTBL_COL_LAST_SUCCESS, "Last Success");
		urlGrid.setText(0, URLTBL_COL_LAST_SUCCESS_MSG, "Message");
		urlGrid.setText(0, URLTBL_COL_LAST_FAULT, "Last Fault");
		urlGrid.setText(0, URLTBL_COL_LAST_FAULT_MSG, "Message");
		urlGrid.setText(0, URLTBL_COL_LAST_FAILURE, "Last Failure");
		urlGrid.setText(0, URLTBL_COL_LAST_FAILURE_MSG, "Message");

		for (int i = 0; i < theUrlStatuses.size(); i++) {
			GUrlStatus status = theUrlStatuses.get(i);
			long urlPid = status.getUrlPid();
			GServiceVersionUrl url = theServiceVersion.getUrlList().getUrlWithPid(urlPid);
			if (url == null) {
				continue;
			}
			
			Anchor urlAnchor = new Anchor();
			urlAnchor.setHref(url.getUrl());
			if (StringUtil.isNotBlank(url.getId())) {
				urlAnchor.setText(url.getId());
			} else {
				urlAnchor.setText(url.getUrl());
			}
			urlGrid.setWidget(i + 1, URLTBL_COL_URL, urlAnchor);

			HorizontalPanel statusPanel = new HorizontalPanel();
			statusPanel.setStyleName(CssConstants.UNSTYLED_TABLE);
			statusPanel.add(BaseDashModel.returnImageForStatus(status.getStatus()));
			switch (status.getStatus()) {
			case ACTIVE:
				statusPanel.add(new Label("Ok"));
				break;
			case DOWN:
				statusPanel.add(new Label("Down"));
				break;
			case UNKNOWN:
				statusPanel.add(new Label("Unknown (no requests)"));
				break;
			}
			urlGrid.setWidget(i + 1, URLTBL_COL_STATUS, statusPanel);

			urlGrid.setText(i + 1, URLTBL_COL_LAST_SUCCESS, renderDate(status.getLastSuccess()));
			urlGrid.setText(i + 1, URLTBL_COL_LAST_SUCCESS_MSG, status.getLastSuccessMessage());
			urlGrid.setText(i + 1, URLTBL_COL_LAST_FAULT, renderDate(status.getLastFault()));
			urlGrid.setText(i + 1, URLTBL_COL_LAST_FAULT_MSG, status.getLastFaultMessage());
			urlGrid.setText(i + 1, URLTBL_COL_LAST_FAILURE, renderDate(status.getLastFailure()));
			urlGrid.setText(i + 1, URLTBL_COL_LAST_FAILURE_MSG, status.getLastFailureMessage());
		}

		set03Usage();

	}

	private void set03Usage() {
		FlowPanel graphsPanel = new FlowPanel();
		graphsPanel.setStylePrimaryName(CssConstants.MAIN_PANEL);
		add(graphsPanel);

		Label graphsTitleLabel = new Label(MSGS.serviceVersionStats_GraphsTitle());
		graphsTitleLabel.setStyleName(CssConstants.MAIN_PANEL_TITLE);
		graphsPanel.add(graphsTitleLabel);

		myGraphsTimePanel = new HorizontalPanel();

		// Graphs Time Dropdown
		final ListBox timeListBox = new ListBox();
		myGraphsTimePanel.add(timeListBox);
		for (TimeRangeEnum next : TimeRangeEnum.values()) {
			timeListBox.addItem(next.getFriendlyName(), next.name());
		}
		timeListBox.setSelectedIndex(1);
		timeListBox.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent theEvent) {
				redrawCharts(TimeRangeEnum.valueOf(timeListBox.getValue(timeListBox.getSelectedIndex())));
			}
		});

		myChartsPanel = new FlowPanel();
		myChartsPanel.addStyleName(CssConstants.CONTENT_INNER_PANEL);
		graphsPanel.add(myChartsPanel);

		redrawCharts(TimeRangeEnum.valueOf(timeListBox.getValue(timeListBox.getSelectedIndex())));

		myTopLoadingSpinner.hideCompletely();

		set04RecentMessages();
	}

	private void redrawCharts(TimeRangeEnum theTimeRange) {
		myChartsPanel.clear();
		myChartsPanel.add(myGraphsTimePanel);
		
		myChartsPanel.add(new HtmlH1(MSGS.serviceVersionStats_UsageTitle()));
		Image img = new Image("graph.png?ct=" + ChartTypeEnum.USAGE.name() + "&pid=" + myServiceVersionPid + "&" + ChartParams.RANGE + "=" + theTimeRange.name());
		addStatsImage(myChartsPanel, img);

		myChartsPanel.add(new HtmlH1(MSGS.serviceVersionStats_LatencyTitle()));
		img = new Image("graph.png?ct=" + ChartTypeEnum.LATENCY.name() + "&pid=" + myServiceVersionPid + "&" + ChartParams.RANGE + "=" + theTimeRange.name());
		addStatsImage(myChartsPanel, img);

		myChartsPanel.add(new HtmlH1(MSGS.serviceVersionStats_MessageSizeTitle()));
		img = new Image("graph.png?ct=" + ChartTypeEnum.PAYLOADSIZE.name() + "&pid=" + myServiceVersionPid + "&" + ChartParams.RANGE + "=" + theTimeRange.name());
		addStatsImage(myChartsPanel, img);
	}

	private void addStatsImage(FlowPanel graphsPanel, Image img) {
		final LoadingSpinner spinner = new LoadingSpinner();
		spinner.showMessage("Generating Graph...", true);
		graphsPanel.add(spinner);

		img.addStyleName(CssConstants.STATS_IMAGE);
		img.addLoadHandler(new LoadHandler() {
			@Override
			public void onLoad(LoadEvent theEvent) {
				spinner.hideCompletely();
			}
		});
		graphsPanel.add(img);
	}

	private void set04RecentMessages() {
		myRecentMessagesPanel = new FlowPanel();
		add(myRecentMessagesPanel);

		myRecentMessagesPanel.setStylePrimaryName(CssConstants.MAIN_PANEL);

		myTitleLabel = new Label("Recent Messages");
		myTitleLabel.setStyleName(CssConstants.MAIN_PANEL_TITLE);
		myRecentMessagesPanel.add(myTitleLabel);

		myRecentMessagesLoadingSpinner = new LoadingSpinner();
		myRecentMessagesLoadingSpinner.show();
		myRecentMessagesPanel.add(myRecentMessagesLoadingSpinner);

		AdminPortal.MODEL_SVC.loadRecentTransactionListForServiceVersion(myServiceVersionPid, new AsyncCallback<GRecentMessageLists>() {

			@Override
			public void onFailure(Throwable theCaught) {
				Model.handleFailure(theCaught);
			}

			@Override
			public void onSuccess(GRecentMessageLists theResult) {
				set04RecentMessages(theResult);
			}

		});

	}

	private void set04RecentMessages(GRecentMessageLists theLists) {
		myRecentMessagesLoadingSpinner.hideCompletely();

		myRecentMessagesPanel.add(new RecentMessagesPanel(theLists, false));

	}

}
