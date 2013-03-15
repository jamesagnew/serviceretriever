package ca.uhn.sail.proxy.admin.client.ui.dash.model;

import ca.uhn.sail.proxy.admin.client.ui.components.LoadingSpinner;
import ca.uhn.sail.proxy.admin.client.ui.dash.ServiceDashboardPanel;
import ca.uhn.sail.proxy.admin.shared.model.BaseGDashboardObject;
import ca.uhn.sail.proxy.admin.shared.model.BaseGListenable;
import ca.uhn.sail.proxy.admin.shared.model.HierarchyEnum;
import ca.uhn.sail.proxy.admin.shared.model.IListener;

import com.google.gwt.user.client.ui.Widget;

public class DashModelLoading implements IListener, IDashModel {

	private ServiceDashboardPanel myPanel;

	public DashModelLoading(ServiceDashboardPanel thePanel, BaseGListenable<?> theList) {
		myPanel = thePanel;
		theList.addListener(this);
	}

	public void changed(BaseGListenable<?> theListenable) {
		theListenable.removeListener(this);
		myPanel.updateView();
	}

	public Boolean isExpanded() {
		return null; // Can't be expanded
	}

	public void loadingStarted(BaseGListenable<?> theListenable) {
		
	}

	public Widget renderName() {
		LoadingSpinner spinner = new LoadingSpinner();
		spinner.show();
		return spinner;
	}

	public Widget renderStatus() {
		return null;
	}

	public Widget renderUsage() {
		return null;
	}

	public void setExpanded(Boolean theExpanded) {
		// ignore
	}

	public HierarchyEnum getType() {
		return HierarchyEnum.DOMAIN;
	}

	public BaseGDashboardObject<?> getModel() {
		return null;
	}

	public Widget renderUrls() {
		return null;
	}

	public Widget renderActions() {
		return null;
	}
	
	public String getCellStyle() {
		return "dashboardTableLoadingCell";
	}

}
