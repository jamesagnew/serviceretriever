package net.svcret.admin.client.ui.dash.model;

import net.svcret.admin.client.AdminPortal;
import net.svcret.admin.client.nav.NavProcessor;
import net.svcret.admin.shared.model.BaseGDashboardObject;
import net.svcret.admin.shared.model.BaseGServiceVersion;
import net.svcret.admin.shared.model.GService;
import net.svcret.admin.shared.model.HierarchyEnum;
import net.svcret.admin.shared.model.IProvidesUrlCount;
import net.svcret.admin.shared.model.StatusEnum;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

public class DashModelServiceVersion extends BaseDashModel implements IDashModel {

	private BaseGServiceVersion myObj;
	private PopupPanel myActionPopup;
	private GService mySvc;

	public DashModelServiceVersion(GService theService, BaseGServiceVersion theServiceVersion) {
		super(theServiceVersion);
		mySvc = theService;
		myObj = theServiceVersion;
	}

	@Override
	public Widget renderName() {
		SafeHtmlBuilder b = new SafeHtmlBuilder();
		b.appendHtmlConstant(AdminPortal.MSGS.dashboard_ServiceVersionPrefix());
		b.appendEscaped(myObj.getId()).toSafeHtml();
		return new HTML(b.toSafeHtml());
	}

	@Override
	public int hashCode() {
		return myObj.hashCode();
	}

	@Override
	public boolean equals(Object theObj) {
		if (!(theObj instanceof DashModelServiceVersion)) {
			return false;
		}
		return ((DashModelServiceVersion) theObj).myObj.equals(theObj);
	}

	@Override
	public Widget renderStatus() {
		StatusEnum status = myObj.getStatus();
		return DashModelDomain.returnImageForStatus(status);
	}

	@Override
	public HierarchyEnum getType() {
		return HierarchyEnum.VERSION;
	}

	@Override
	public BaseGDashboardObject<?> getModel() {
		return myObj;
	}

	@Override
	public Widget renderUrls() {
		return renderUrlCount(myObj);
	}

	public static Widget renderUrlCount(IProvidesUrlCount theObj) {
		FlowPanel retVal = new FlowPanel();
		retVal.setStyleName("urlStatusSummaryPanel");

		boolean found = false;
		if (theObj.getUrlsActive() > 0) {
			retVal.add(new Label("" + theObj.getUrlsActive()));
			retVal.add(new Image("images/icon_check_16.png"));
			found = true;
		}

		if (theObj.getUrlsUnknown() > 0) {
			retVal.add(new Label("" + theObj.getUrlsUnknown()));
			retVal.add(new Image("images/icon_unknown_16.png"));
			found = true;
		}

		if (theObj.getUrlsDown() > 0) {
			retVal.add(new Label("" + theObj.getUrlsDown()));
			retVal.add(new Image("images/icon_warn_16.png"));
			found = true;
		}

		if (!found) {
			retVal.add(new Label("0"));
			retVal.add(new Image("images/icon_unknown_16.png"));
		}

		return retVal;
	}

	@Override
	public Widget renderActions() {
		final Image retVal = (new Image("images/tools_16.png"));
		retVal.addStyleName("dashboardActionButton");
		retVal.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent theEvent) {
				actionMenu(retVal);
			}
		});
		return retVal;
	}

	private void actionMenu(Image theButton) {
		if (myActionPopup == null || myActionPopup.isShowing() == false) {

			myActionPopup = new PopupPanel(true, true);
			FlowPanel content = new FlowPanel();
			myActionPopup.add(content);

			SafeHtmlBuilder titleB = new SafeHtmlBuilder();
			titleB.appendEscaped(mySvc.getName());
			titleB.appendHtmlConstant("<br/>");
			titleB.appendEscaped(myObj.getId());
			content.add(new HeaderLabel(titleB.toSafeHtml()));

			Button editDomain = new ActionPButton(AdminPortal.IMAGES.iconEdit(), "Edit");
			editDomain.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent theEvent) {
					myActionPopup.hide();
					myActionPopup = null;
					History.newItem(NavProcessor.getTokenEditServiceVersion(myObj.getPid()));
				}
			});
			content.add(editDomain);

			myActionPopup.showRelativeTo(theButton);
		} else {
			myActionPopup.hide();
			myActionPopup = null;
		}
	}

	@Override
	public String getCellStyle() {
		return "dashboardTableServiceVersionCell";
	}

	@Override
	public boolean hasChildren() {
		return myObj.getMethodList().size() > 0;
	}

}
