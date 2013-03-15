package ca.uhn.sail.proxy.admin.client.ui.dash.model;

import ca.uhn.sail.proxy.admin.shared.model.BaseGDashboardObject;
import ca.uhn.sail.proxy.admin.shared.model.GService;
import ca.uhn.sail.proxy.admin.shared.model.HierarchyEnum;
import ca.uhn.sail.proxy.admin.shared.model.StatusEnum;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

public class DashModelService extends BaseDashModel implements IDashModel {

	private GService myService;

	public DashModelService(GService theService) {
		myService = theService;
	}

	public Widget renderName() {
		SafeHtml safeHtml = new SafeHtmlBuilder().appendEscaped(myService.getName()).toSafeHtml();
		return new HTML(safeHtml);
	}

	@Override
	public int hashCode() {
		return myService.hashCode();
	}

	@Override
	public boolean equals(Object theObj) {
		if (!(theObj instanceof DashModelService)) {
			return false;
		}
		return ((DashModelService)theObj).myService.equals(myService);
	}

	public Widget renderStatus() {
		StatusEnum status = myService.getStatus();
		return DashModelDomain.returnImageForStatus(status);
	}

	public Widget renderUsage() {
		int[] list = myService.getTransactions60mins();
		return DashModelDomain.returnSparklineFor60mins(list);
	}
	
	public HierarchyEnum getType() {
		return HierarchyEnum.SERVICE;
	}

	public BaseGDashboardObject<?> getModel() {
		return myService;
	}

	public Widget renderUrls() {
		return null;
	}

	public Widget renderActions() {
		return null;
	}
	
	public String getCellStyle() {
		return "dashboardTableServiceCell";
	}

	
	
}
