package net.svcret.admin.client.ui.dash.model;

import java.util.Date;

import net.svcret.admin.client.AdminPortal;
import net.svcret.admin.client.MyResources;
import net.svcret.admin.client.ui.stats.DateUtil;
import net.svcret.admin.shared.model.BaseGDashboardObject;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public final class UsageSparklineTooltipProvider implements IProvidesWidget {
	private BaseGDashboardObject myModel;
	public UsageSparklineTooltipProvider(BaseGDashboardObject theModel) {
		myModel=theModel;
	}

	@Override
	public Widget provideWidget() {
		FlowPanel retVal = new FlowPanel();
		SafeHtmlBuilder b = new SafeHtmlBuilder();
		b.appendEscaped(myModel.getName());
		b.appendHtmlConstant("<br/>");
		b.appendHtmlConstant("Usage in last 60 minutes");
		HTML header = new HTML(b.toSafeHtml());
		header.addStyleName(MyResources.CSS.usageTooltipTableHeaderLabel());
		retVal.add(header);
		
		FlexTable grid = new FlexTable();
		grid.addStyleName(MyResources.CSS.usageTooltipTable());
		retVal.add(grid);
		
		grid.setText(0, 0, "");
		grid.setText(0, 1, "Successful Invocations");
		grid.getFlexCellFormatter().addStyleName(0, 1, MyResources.CSS.usageTooltipTableSuccessColumn());
		
		grid.setText(0, 2, "Fault Invocations");
		grid.getFlexCellFormatter().addStyleName(0, 2, MyResources.CSS.usageTooltipTableFaultColumn());
		
		grid.setText(0, 3, "Failed Invocations");
		grid.getFlexCellFormatter().addStyleName(0, 3, MyResources.CSS.usageTooltipTableFailColumn());

		grid.setText(0, 4, "Security Failures");
		grid.getFlexCellFormatter().addStyleName(0, 4, MyResources.CSS.usageTooltipTableSecFailColumn());

		Date nextDate = myModel.getStatistics60MinuteFirstDate();
		int row = 1;
		int incrementMins = 10;
		for (int i = 0; i < 60; i += incrementMins, row++) {
		
			int success = 0;
			int fault = 0;
			int fail = 0;
			int secFail = 0;
			for (int j = i; j < i + incrementMins; j++) {
				success += myModel.getTransactions60mins()[j];
				fault += myModel.getTransactionsFault60mins()[j];
				fail += myModel.getTransactionsFail60mins()[j];
				secFail += myModel.getTransactionsSecurityFail60mins()[j];
			}

			Date nextEndDate = new Date(nextDate.getTime() + ((incrementMins-1)*DateUtil.MILLIS_PER_MINUTE));
			grid.setText(row, 0, DateUtil.formatTimeOnly(nextDate) + " - " + DateUtil.formatTimeOnly(nextEndDate));
			grid.getFlexCellFormatter().addStyleName(row, 0, MyResources.CSS.usageTooltipTableDateColumn());
			
			grid.setText(row, 1, Integer.toString(success));
			grid.getFlexCellFormatter().addStyleName(row, 1, MyResources.CSS.usageTooltipTableValueColumn());
			
			grid.setText(row, 2, Integer.toString(fault));
			grid.getFlexCellFormatter().addStyleName(row, 2, MyResources.CSS.usageTooltipTableValueColumn());

			grid.setText(row, 3, Integer.toString(fail));
			grid.getFlexCellFormatter().addStyleName(row, 3, MyResources.CSS.usageTooltipTableValueColumn());

			grid.setText(row, 4, Integer.toString(secFail));
			grid.getFlexCellFormatter().addStyleName(row, 4, MyResources.CSS.usageTooltipTableValueColumn());

			nextDate = new Date(nextDate.getTime() + (incrementMins * 60 * 1000L));
		}
		
		return retVal;
	}
}
