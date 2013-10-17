package net.svcret.admin.client.ui.dash.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.svcret.admin.client.MyResources;
import net.svcret.admin.client.ui.components.IProvidesTooltip;
import net.svcret.admin.client.ui.components.LoadingSpinner;
import net.svcret.admin.client.ui.config.svcver.BaseUrlGrid;
import net.svcret.admin.shared.IAsyncLoadCallback;
import net.svcret.admin.shared.Model;
import net.svcret.admin.shared.model.BaseDtoDashboardObject;
import net.svcret.admin.shared.model.BaseDtoServiceVersion;
import net.svcret.admin.shared.model.GDomainList;
import net.svcret.admin.shared.model.GServiceVersionUrl;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public final class UrlStatusTooltipProvider<T extends BaseDtoDashboardObject> implements IProvidesTooltip<T> {

	@Override
	public Widget getTooltip(BaseDtoDashboardObject theObject) {
		final FlowPanel retVal = new FlowPanel();

		SafeHtmlBuilder b = new SafeHtmlBuilder();
		if (theObject.getName() != null) {
			b.appendEscaped(theObject.getName());
			b.appendHtmlConstant("<br/>");
		}
		b.appendHtmlConstant("Backing URL Statuses");
		HTML header = new HTML(b.toSafeHtml());
		header.addStyleName(MyResources.CSS.usageTooltipTableHeaderLabel());
		retVal.add(header);

		final LoadingSpinner spinner = new LoadingSpinner();
		retVal.add(spinner);
		spinner.show();

		final List<BaseDtoServiceVersion> allSvcVers = theObject.getAllServiceVersions();
		Set<Long> urlPids = new HashSet<Long>();
		final List<GServiceVersionUrl> urls = new ArrayList<GServiceVersionUrl>();

		for (BaseDtoServiceVersion nextVer : allSvcVers) {
			for (GServiceVersionUrl nextUrl : nextVer.getUrlList()) {
				urlPids.add(nextUrl.getPid());
				urls.add(nextUrl);
			}
		}

		final FlexTable grid = new FlexTable();
		retVal.add(grid);

		populateGrid(grid, urls);
		grid.getCellFormatter().setWidth(0, 2, "100px");

		Model.getInstance().loadDomainListAndUrlStats(urlPids, new IAsyncLoadCallback<GDomainList>() {
			@Override
			public void onSuccess(GDomainList theResult) {
				spinner.hideCompletely();
				populateGrid(grid, urls);
			}
		});

		return retVal;
	}

	private void populateGrid(FlexTable theGrid, List<GServiceVersionUrl> theUrls) {
		theGrid.addStyleName(MyResources.CSS.usageTooltipTable());

		theGrid.setText(0, 0, "URL ID");
		theGrid.getFlexCellFormatter().addStyleName(0, 0, MyResources.CSS.usageTooltipTableNormalColumn());

		theGrid.setText(0, 1, "URL");
		theGrid.getFlexCellFormatter().addStyleName(0, 1, MyResources.CSS.usageTooltipTableNormalColumn());

		theGrid.setText(0, 2, "Status");
		theGrid.getFlexCellFormatter().addStyleName(0, 2, MyResources.CSS.usageTooltipTableNormalColumn());

		int row = 0;
		for (GServiceVersionUrl nextUrl : theUrls) {
			row++;

			theGrid.setText(row, 0, nextUrl.getId());
			theGrid.getFlexCellFormatter().addStyleName(row, 0, MyResources.CSS.usageTooltipTableValueColumn());

			theGrid.setText(row, 1, nextUrl.getUrl());
			theGrid.getFlexCellFormatter().addStyleName(row, 1, MyResources.CSS.usageTooltipTableValueColumn());

			FlowPanel statusPanel = new FlowPanel();
			statusPanel.add(BaseDashModel.returnImageForStatus(nextUrl.getStatus()));
			statusPanel.add(new Label(BaseUrlGrid.createUrlStatusText(nextUrl)));
			theGrid.setWidget(row, 2, statusPanel);
			theGrid.getFlexCellFormatter().addStyleName(row, 2, MyResources.CSS.usageTooltipTableValueColumn());

		}
	}
}
