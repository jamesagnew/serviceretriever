package net.svcret.admin.client.ui.dash.model;

import net.svcret.admin.client.ui.components.PButton;
import net.svcret.admin.shared.model.BaseDtoDashboardObject;
import net.svcret.admin.shared.model.GServiceMethod;
import net.svcret.admin.shared.model.HierarchyEnum;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

public class DashModelServiceMethod extends BaseDashModel implements IDashModel {

	private GServiceMethod myServiceMethod;

	public DashModelServiceMethod(GServiceMethod theServiceMethod) {
		super(theServiceMethod);
		myServiceMethod = theServiceMethod;
	}

	@Override
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

	@Override
	public Widget renderStatus() {
		return null;
	}

	
	@Override
	public HierarchyEnum getType() {
		return HierarchyEnum.METHOD;
	}

	@Override
	public BaseDtoDashboardObject getModel() {
		return myServiceMethod;
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
		return "dashboardTableServiceMethodCell";
	}

	@Override
	public boolean hasChildren() {
		return false;
	}

	
	
}
