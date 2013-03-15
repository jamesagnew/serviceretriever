package ca.uhn.sail.proxy.admin.client.ui.dash.model;

import ca.uhn.sail.proxy.admin.shared.model.BaseGDashboardObject;
import ca.uhn.sail.proxy.admin.shared.model.HierarchyEnum;

import com.google.gwt.user.client.ui.Widget;

public interface IDashModel {

	Widget renderName();
	
	Widget renderStatus();
	
	Widget renderUsage();

	HierarchyEnum getType();
	
	BaseGDashboardObject<?> getModel();

	Widget renderUrls();
	
	Widget renderActions();

	String getCellStyle();
}
