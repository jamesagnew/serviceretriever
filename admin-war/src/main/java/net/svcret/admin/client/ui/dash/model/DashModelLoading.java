package net.svcret.admin.client.ui.dash.model;

import net.svcret.admin.client.ui.components.IProvidesTooltip;
import net.svcret.admin.client.ui.components.LoadingSpinner;
import net.svcret.admin.client.ui.components.PButton;
import net.svcret.admin.shared.model.BaseGDashboardObject;
import net.svcret.admin.shared.model.HierarchyEnum;

import com.google.gwt.user.client.ui.Widget;

public class DashModelLoading implements IDashModel {

	public DashModelLoading() {
	}

	@Override
	public Widget renderName() {
		LoadingSpinner spinner = new LoadingSpinner();
		spinner.show();
		return spinner;
	}

	@Override
	public Widget renderStatus() {
		return null;
	}

	@Override
	public Widget renderUsage() {
		return null;
	}

	@Override
	public HierarchyEnum getType() {
		return HierarchyEnum.DOMAIN;
	}

	@Override
	public BaseGDashboardObject getModel() {
		return null;
	}

	@Override
	public Widget renderUrls() {
		return null;
	}

	@Override
	public PButton renderActions() {
		return null;
	}
	
	@Override
	public String getCellStyle() {
		return "dashboardTableLoadingCell";
	}

	@Override
	public Widget renderLatency() {
		return null;
	}

	@Override
	public boolean hasChildren() {
		return false;
	}

	@Override
	public Widget renderSecurity() {
		return null;
	}

	@Override
	public Widget renderLastInvocation() {
		return null;
	}

	@Override
	public IProvidesTooltip<BaseGDashboardObject> getUsageTooltip() {
		return null;
	}

}
