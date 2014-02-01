package net.svcret.admin.client.ui.dash.model;

import net.svcret.admin.client.ui.components.IProvidesTooltip;
import net.svcret.admin.client.ui.components.PButton;
import net.svcret.admin.shared.model.BaseDtoDashboardObject;
import net.svcret.admin.shared.model.HierarchyEnum;

import com.google.gwt.user.client.ui.Widget;

public interface IDashModel {

	boolean hasChildren();
	
	Widget renderName();
	
	Widget renderLastInvocation();

	Widget renderStatus();
	
	Widget renderUsage();

	HierarchyEnum getType();
	
	BaseDtoDashboardObject getModel();

	Widget renderUrls();
	
	PButton renderActions();

	String getCellStyle();

	Widget renderLatency(int thePeakLatency);

	Widget renderSecurity();

	
	int getPeakLatency();
	
	IProvidesTooltip<BaseDtoDashboardObject> getUsageTooltip();

	IProvidesTooltip<BaseDtoDashboardObject> getUrlsTooltip();

	IProvidesTooltip<BaseDtoDashboardObject> getLastInvocationTooltip();
}
