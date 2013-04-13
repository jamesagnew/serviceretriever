package net.svcret.admin.client.ui.dash.model;

import net.svcret.admin.client.AdminPortal;
import net.svcret.admin.shared.model.BaseGDashboardObject;
import net.svcret.admin.shared.model.GService;
import net.svcret.admin.shared.model.HierarchyEnum;
import net.svcret.admin.shared.model.StatusEnum;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

public class DashModelService extends BaseDashModel implements IDashModel {

	private GService myService;

	public DashModelService(GService theService) {
		super(theService);
		myService = theService;
	}

	@Override
	public Widget renderName() {
		SafeHtmlBuilder b = new SafeHtmlBuilder().appendEscaped(myService.getName());
		if (myService.getVersionList().size()==0) {
			b.appendHtmlConstant(AdminPortal.MSGS.dashboard_ServiceNoServiceVersionsSuffix());
		}
		SafeHtml safeHtml = b.toSafeHtml();
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

	@Override
	public Widget renderStatus() {
		if (myService.getVersionList().size()==0) {
			return null;
		}
		StatusEnum status = myService.getStatus();
		return DashModelDomain.returnImageForStatus(status);
	}

	
	@Override
	public HierarchyEnum getType() {
		return HierarchyEnum.SERVICE;
	}

	@Override
	public BaseGDashboardObject<?> getModel() {
		return myService;
	}

	@Override
	public Widget renderUrls() {
		if (myService.getVersionList().size()==0) {
			return null;
		}
		return DashModelServiceVersion.renderUrlCount(myService);
	}

	@Override
	public Widget renderActions() {
		return null;
	}
	
	@Override
	public String getCellStyle() {
		return "dashboardTableServiceCell";
	}

	@Override
	public boolean hasChildren() {
		return myService.getVersionList().size() > 0;
	}

	
	
}
