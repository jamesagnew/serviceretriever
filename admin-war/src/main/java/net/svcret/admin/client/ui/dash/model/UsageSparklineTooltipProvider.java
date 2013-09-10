package net.svcret.admin.client.ui.dash.model;

import java.util.Date;

import net.svcret.admin.client.ui.components.IProvidesTooltip;
import net.svcret.admin.client.ui.stats.DateUtil;
import net.svcret.admin.shared.model.BaseGDashboardObject;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public final class UsageSparklineTooltipProvider<T extends BaseGDashboardObject> implements IProvidesTooltip<T> {

	@Override
	public Widget getTooltip(BaseGDashboardObject theObject) {
		FlowPanel retVal = new FlowPanel();
		retVal.add(new Label(theObject.getName()));
		retVal.add(new Label("Usage in last 60 minutes"));
		
		FlexTable grid = new FlexTable();
		retVal.add(grid);
		
		grid.setText(0, 0, "");
		grid.setText(0, 1, "Successful Invocations");
		grid.setText(0, 2, "Fault Invocations");
		grid.setText(0, 3, "Failed Invocations");
		grid.setText(0, 4, "Security Failures");
		
		Date nextDate = theObject.getStatistics60MinuteFirstDate();
		int row = 1;
		int incrementMins = 10;
		for (int i = 0; i < 60; i += incrementMins, row++) {
		
			int success = 0;
			int fault = 0;
			int fail = 0;
			int secFail = 0;
			for (int j = i; j < i + incrementMins; j++) {
				success += theObject.getTransactions60mins()[j];
				fault += theObject.getTransactionsFault60mins()[j];
				fail += theObject.getTransactionsFail60mins()[j];
				secFail += theObject.getTransactionsSecurityFail60mins()[j];
			}

			Date nextEndDate = new Date(nextDate.getTime() + ((incrementMins-1)*DateUtil.MILLIS_PER_MINUTE));
			grid.setText(row, 0, DateUtil.formatTimeOnly(nextEndDate) + " - " + DateUtil.formatTimeOnly(nextDate));
			grid.setText(row, 1, Integer.toString(success));
			grid.setText(row, 2, Integer.toString(fault));
			grid.setText(row, 3, Integer.toString(fail));
			grid.setText(row, 4, Integer.toString(secFail));
			
			nextDate = new Date(nextDate.getTime() + (incrementMins * 60 * 1000L));
		}
		
		return retVal;
	}

}