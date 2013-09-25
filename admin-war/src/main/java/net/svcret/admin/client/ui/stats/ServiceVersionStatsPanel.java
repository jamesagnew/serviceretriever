package net.svcret.admin.client.ui.stats;

import static net.svcret.admin.client.AdminPortal.*;
import net.svcret.admin.client.ui.components.CssConstants;
import net.svcret.admin.client.ui.components.HtmlH1;
import net.svcret.admin.client.ui.components.LoadingSpinner;
import net.svcret.admin.shared.IAsyncLoadCallback;
import net.svcret.admin.shared.Model;
import net.svcret.admin.shared.model.BaseDtoServiceVersion;
import net.svcret.admin.shared.model.TimeRange;
import net.svcret.admin.shared.util.ChartParams;
import net.svcret.admin.shared.util.ChartTypeEnum;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;

public class ServiceVersionStatsPanel extends FlowPanel {

	public static final String GRAPH_FILENAME = "../charts/largegraph.png";
	private LoadingSpinner myTopLoadingSpinner;
	private long myServiceVersionPid;
	private Label myTitleLabel;
	private FlowPanel myTopPanel;
	private FlowPanel myChartsPanel;
	private HorizontalPanel myGraphsTimePanel;
	private FlowPanel myTopContentPanel;

	public ServiceVersionStatsPanel(long theVersionPid) {
		myServiceVersionPid = theVersionPid;

		myTopPanel = new FlowPanel();
		add(myTopPanel);

		myTopPanel.setStylePrimaryName(CssConstants.MAIN_PANEL);

		myTitleLabel = new Label(MSGS.serviceVersionStats_Title(""));
		myTitleLabel.setStyleName(CssConstants.MAIN_PANEL_TITLE);
		myTopPanel.add(myTitleLabel);

		myTopContentPanel = new FlowPanel();
		myTopPanel.add(myTopContentPanel);
		myTopContentPanel.addStyleName(CssConstants.CONTENT_INNER_PANEL);

		myTopLoadingSpinner = new LoadingSpinner();
		myTopLoadingSpinner.show();
		myTopContentPanel.add(myTopLoadingSpinner);

		Model.getInstance().loadServiceVersion(theVersionPid, true, new IAsyncLoadCallback<BaseDtoServiceVersion>() {
			@Override
			public void onSuccess(BaseDtoServiceVersion theResult) {
				initUi(theResult);
			}
		});

	}

	private void initUi(final BaseDtoServiceVersion theResult) {
		myTopLoadingSpinner.hideCompletely();
		myTitleLabel.setText(MSGS.serviceVersionStats_Title(theResult.getName()));


		myGraphsTimePanel = new HorizontalPanel();

		// Graphs Time Dropdown
		final TimeRangeSelectorPanel timeListBox = new TimeRangeSelectorPanel(true);
		timeListBox.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent theEvent) {
				redrawCharts(timeListBox.getSelectedRange());
			}
		});
		myGraphsTimePanel.add(timeListBox);
		
		myChartsPanel = new FlowPanel();
		myChartsPanel.addStyleName(CssConstants.CONTENT_INNER_PANEL);
		myTopContentPanel.add(myChartsPanel);

		redrawCharts(timeListBox.getSelectedRange());

		myTopLoadingSpinner.hideCompletely();

	}

	private void redrawCharts(TimeRange theTimeRange) {
		myChartsPanel.clear();
		myChartsPanel.add(myGraphsTimePanel);
		
		myChartsPanel.add(new HtmlH1(MSGS.serviceVersionStats_UsageTitle()));
		Image img = new Image(ServiceVersionStatsPanel.GRAPH_FILENAME + "?ct=" + ChartTypeEnum.USAGE.name() + "&pid=" + myServiceVersionPid + "&" + ChartParams.RANGE + "=" + theTimeRange.toUrlValue());
		addStatsImage(myChartsPanel, img);

		myChartsPanel.add(new HtmlH1(MSGS.serviceVersionStats_LatencyTitle()));
		img = new Image(ServiceVersionStatsPanel.GRAPH_FILENAME + "?ct=" + ChartTypeEnum.LATENCY.name() + "&pid=" + myServiceVersionPid + "&" + ChartParams.RANGE + "=" + theTimeRange.toUrlValue());
		addStatsImage(myChartsPanel, img);

		myChartsPanel.add(new HtmlH1(MSGS.serviceVersionStats_MessageSizeTitle()));
		img = new Image(ServiceVersionStatsPanel.GRAPH_FILENAME + "?ct=" + ChartTypeEnum.PAYLOADSIZE.name() + "&pid=" + myServiceVersionPid + "&" + ChartParams.RANGE + "=" + theTimeRange.toUrlValue());
		addStatsImage(myChartsPanel, img);

		myChartsPanel.add(new HtmlH1(MSGS.serviceVersionStats_ThrottlingTitle()));
		img = new Image(ServiceVersionStatsPanel.GRAPH_FILENAME + "?ct=" + ChartTypeEnum.THROTTLING.name() + "&pid=" + myServiceVersionPid + "&" + ChartParams.RANGE + "=" + theTimeRange.toUrlValue());
		addStatsImage(myChartsPanel, img);
		
	}

	public static void addStatsImage(FlowPanel graphsPanel, Image img) {
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


}
