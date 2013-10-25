package net.svcret.admin.client.ui.config.monitor;

import net.svcret.admin.client.MyResources;
import net.svcret.admin.client.ui.components.IProvidesTooltip;
import net.svcret.admin.shared.DateUtil;
import net.svcret.admin.shared.model.DtoMonitorRuleActiveCheck;
import net.svcret.admin.shared.model.DtoMonitorRuleActiveCheckOutcome;
import net.svcret.admin.shared.util.StringUtil;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public final class ActiveChecksRecentOutcomeTooltipProvider implements IProvidesTooltip<DtoMonitorRuleActiveCheck> {

	@Override
	public Widget getTooltip(DtoMonitorRuleActiveCheck theObject) {
		FlowPanel retVal = new FlowPanel();
		if (theObject.getRecentOutcomesForUrl().isEmpty() || theObject.getRecentOutcomesForUrl().get(0).getOutcomes().isEmpty()) {
			retVal.add(new Label("This check has not yet fired"));
			return retVal;
		}
		
		SafeHtmlBuilder b = new SafeHtmlBuilder();
		b.appendHtmlConstant("Recent Invocations of this Check");
		HTML header = new HTML(b.toSafeHtml());
		header.addStyleName(MyResources.CSS.usageTooltipTableHeaderLabel());
		retVal.add(header);

		FlexTable grid = new FlexTable();
		grid.addStyleName(MyResources.CSS.usageTooltipTable());
		retVal.add(grid);

		grid.setText(0, 0, "");

		for (int col = 0; col < theObject.getRecentOutcomesForUrl().size(); col++) {
			int column = col+1;
			grid.setText(0, column, theObject.getRecentOutcomesForUrl().get(col).getUrlId());
			grid.getFlexCellFormatter().addStyleName(0, column, MyResources.CSS.monitorRuleActiveCheckTooltipTableColumnHeader());
			grid.getFlexCellFormatter().setColSpan(0, column, 2);
		}

		for (int row = 0; row < theObject.getRecentOutcomesForUrl().get(0).getOutcomes().size(); row++) {
			DtoMonitorRuleActiveCheckOutcome rowModelWhole = theObject.getRecentOutcomesForUrl().get(0).getOutcomes().get(row);
			grid.setText(row + 1, 0, DateUtil.formatTimeOnly(rowModelWhole.getTimestamp()));
			grid.getFlexCellFormatter().addStyleName(row+1, 0, MyResources.CSS.usageTooltipTableDateColumn());

			for (int col = 0; col < theObject.getRecentOutcomesForUrl().size(); col++) {
				DtoMonitorRuleActiveCheckOutcome rowModel = theObject.getRecentOutcomesForUrl().get(col).getOutcomes().get(row);
				int column = (col*2) + 1;

				String message;
				if (rowModel.isSuccess()) {
					message = "Passed";
				} else {
					message = rowModel.getFailureMessage();
					if (StringUtil.isBlank(message)) {
						message = "Failure";
					}
				}
				grid.setText(row + 1, column, message);
				grid.getFlexCellFormatter().addStyleName(row+1, column, MyResources.CSS.usageTooltipTableValueColumn());

				grid.setText(row + 1, column+1, rowModel.getLatency() + "ms");
				grid.getFlexCellFormatter().addStyleName(row+1, column+1, MyResources.CSS.usageTooltipTableValueColumn());

			}
		}

		return retVal;
	}
}
