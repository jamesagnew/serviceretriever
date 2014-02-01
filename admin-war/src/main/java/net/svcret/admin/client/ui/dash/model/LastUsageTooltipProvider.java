package net.svcret.admin.client.ui.dash.model;

import net.svcret.admin.client.MyResources;
import net.svcret.admin.client.ui.components.IProvidesTooltip;
import net.svcret.admin.shared.DateUtil;
import net.svcret.admin.shared.model.BaseDtoDashboardObject;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public final class LastUsageTooltipProvider<T extends BaseDtoDashboardObject> implements IProvidesTooltip<T> {

	@Override
	public Widget getTooltip(BaseDtoDashboardObject theObject) {
		final FlowPanel retVal = new FlowPanel();

		SafeHtmlBuilder b = new SafeHtmlBuilder();
		if (theObject.getName() != null) {
			b.appendEscaped(theObject.getName());
			b.appendHtmlConstant("<br/>");
		}
		b.appendHtmlConstant("Last Usage");
		HTML header = new HTML(b.toSafeHtml());
		header.addStyleName(MyResources.CSS.usageTooltipTableHeaderLabel());
		retVal.add(header);

		if (!theObject.isStatsInitialized()) {
			// should not happen
			retVal.add(new Label("Unknown"));
			return retVal;
		}

		if (theObject.getLastSuccessfulInvocation() != null) {
			retVal.add(new Label("Last successful invocation: " + DateUtil.formatTimeElapsedForLastInvocation(theObject.getLastSuccessfulInvocation())));
		} else {
			retVal.add(new Label("No successful invocations ever"));
		}

		if (theObject.getLastFaultInvocation() != null) {
			retVal.add(new Label("Last fault invocation: " + DateUtil.formatTimeElapsedForLastInvocation(theObject.getLastFaultInvocation())));
		}

		if (theObject.getLastFailInvocation() != null) {
			retVal.add(new Label("Last failure invocation: " + DateUtil.formatTimeElapsedForLastInvocation(theObject.getLastFailInvocation())));
		}

		if (theObject.getLastFaultInvocation() != null) {
			retVal.add(new Label("Last security failure: " + DateUtil.formatTimeElapsedForLastInvocation(theObject.getLastServerSecurityFailure())));
		}

		return retVal;
	}
}
