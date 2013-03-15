package ca.uhn.sail.proxy.admin.client.ui.dash.model;

import ca.uhn.sail.proxy.admin.shared.model.BaseGDashboardObject;
import ca.uhn.sail.proxy.admin.shared.model.GServiceMethod;
import ca.uhn.sail.proxy.admin.shared.model.HierarchyEnum;
import ca.uhn.sail.proxy.admin.shared.model.StatusEnum;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

public class DashModelServiceMethod extends BaseDashModel implements IDashModel {

	private GServiceMethod myServiceMethod;

	public DashModelServiceMethod(GServiceMethod theService) {
		myServiceMethod = theService;
	}

	public Widget renderName() {
		SafeHtml safeHtml = new SafeHtmlBuilder().appendEscaped(myServiceMethod.getName()).toSafeHtml();
		return new HTML(safeHtml);
	}

	@Override
	public int hashCode() {
		return myServiceMethod.hashCode();
	}

	@Override
	public boolean equals(Object theObj) {
		if (!(theObj instanceof DashModelServiceMethod)) {
			return false;
		}
		return ((DashModelServiceMethod)theObj).myServiceMethod.equals(myServiceMethod);
	}

	public Widget renderStatus() {
		StatusEnum status = myServiceMethod.getStatus();
		return DashModelDomain.returnImageForStatus(status);
	}

	public Widget renderUsage() {
		int[] list = myServiceMethod.getTransactions60mins();
		return DashModelDomain.returnSparklineFor60mins(list);
	}
	
	public HierarchyEnum getType() {
		return HierarchyEnum.METHOD;
	}

	public BaseGDashboardObject<?> getModel() {
		return myServiceMethod;
	}

	public Widget renderUrls() {
		return null;
	}

	public Widget renderActions() {
		return null;
	}
	
	public String getCellStyle() {
		return "dashboardTableServiceMethodCell";
	}

	
	
}
