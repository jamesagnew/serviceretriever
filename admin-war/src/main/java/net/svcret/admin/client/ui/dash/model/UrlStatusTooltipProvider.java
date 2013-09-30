package net.svcret.admin.client.ui.dash.model;

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
		for (BaseDtoServiceVersion nextVer : allSvcVers) {
			for (GServiceVersionUrl nextUrl : nextVer.getUrlList()) {
				urlPids.add(nextUrl.getPid());
			}
		}

		Model.getInstance().loadDomainListAndUrlStats(urlPids, new IAsyncLoadCallback<GDomainList>() {
			@Override
			public void onSuccess(GDomainList theResult) {
				spinner.hideCompletely();

				FlexTable grid = new FlexTable();
				grid.addStyleName(MyResources.CSS.usageTooltipTable());
				retVal.add(grid);

				grid.setText(0, 0, "URL ID");
				grid.getFlexCellFormatter().addStyleName(0, 0, MyResources.CSS.usageTooltipTableNormalColumn());

				grid.setText(0, 1, "URL");
				grid.getFlexCellFormatter().addStyleName(0, 1, MyResources.CSS.usageTooltipTableNormalColumn());

				grid.setText(0, 2, "Status");
				grid.getFlexCellFormatter().addStyleName(0, 2, MyResources.CSS.usageTooltipTableNormalColumn());

				int row = 0;
				for (BaseDtoServiceVersion nextSvcVer : allSvcVers) {
					for (GServiceVersionUrl nextUrl : nextSvcVer.getUrlList()) {
						row++;

						grid.setText(row, 0, nextUrl.getId());
						grid.getFlexCellFormatter().addStyleName(row, 0, MyResources.CSS.usageTooltipTableValueColumn());

						grid.setText(row, 1, nextUrl.getUrl());
						grid.getFlexCellFormatter().addStyleName(row, 1, MyResources.CSS.usageTooltipTableValueColumn());

						FlowPanel statusPanel = new FlowPanel();
						statusPanel.add(BaseDashModel.returnImageForStatus(nextUrl.getStatus()));
						statusPanel.add(new Label(BaseUrlGrid.createUrlStatusText(nextUrl)));
						grid.setWidget(row, 2, statusPanel);
						grid.getFlexCellFormatter().addStyleName(row, 2, MyResources.CSS.usageTooltipTableValueColumn());

					}
				}

			}
		});

		return retVal;
	}
}
