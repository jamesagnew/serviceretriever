package net.svcret.admin.client.ui.dash.model;

import net.svcret.admin.shared.model.BaseGDashboardObject;
import net.svcret.admin.shared.model.HierarchyEnum;

import com.google.gwt.user.client.ui.Widget;

public interface IDashModel {

	boolean hasChildren();
	
	Widget renderName();
	
	Widget renderLastInvocation();

	Widget renderStatus();
	
	Widget renderUsage();

	HierarchyEnum getType();
	
	BaseGDashboardObject<?> getModel();

	Widget renderUrls();
	
	Widget renderActions();

	String getCellStyle();

	Widget renderLatency();

	Widget renderSecurity();
}
