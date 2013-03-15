package ca.uhn.sail.proxy.admin.client.ui.dash.model;

import ca.uhn.sail.proxy.admin.shared.model.BaseGDashboardObject;
import ca.uhn.sail.proxy.admin.shared.model.BaseGServiceVersion;
import ca.uhn.sail.proxy.admin.shared.model.HierarchyEnum;
import ca.uhn.sail.proxy.admin.shared.model.StatusEnum;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.Widget;

public class DashModelServiceVersion extends BaseDashModel implements IDashModel {

	private BaseGServiceVersion myServiceVersion;
	private PopupPanel myActionPopup;

	public DashModelServiceVersion(BaseGServiceVersion theServiceVersion) {
		myServiceVersion = theServiceVersion;
	}

	public Widget renderName() {
		SafeHtml safeHtml = new SafeHtmlBuilder().appendEscaped(myServiceVersion.getId()).toSafeHtml();
		return new HTML(safeHtml);
	}

	@Override
	public int hashCode() {
		return myServiceVersion.hashCode();
	}

	@Override
	public boolean equals(Object theObj) {
		if (!(theObj instanceof DashModelServiceVersion)) {
			return false;
		}
		return ((DashModelServiceVersion) theObj).myServiceVersion.equals(myServiceVersion);
	}

	public Widget renderStatus() {
		StatusEnum status = myServiceVersion.getStatus();
		return DashModelDomain.returnImageForStatus(status);
	}

	public Widget renderUsage() {
		int[] list = myServiceVersion.getTransactions60mins();
		return DashModelDomain.returnSparklineFor60mins(list);
	}

	public HierarchyEnum getType() {
		return HierarchyEnum.VERSION;
	}

	public BaseGDashboardObject<?> getModel() {
		return myServiceVersion;
	}

	public Widget renderUrls() {
		FlowPanel retVal = new FlowPanel();
		retVal.setStyleName("urlStatusSummaryPanel");

		if (myServiceVersion.getUrlsActive() > 0) {
			retVal.add(new Label("" + myServiceVersion.getUrlsActive()));
			retVal.add(new Image("images/icon_check_16.png"));
		}

		if (myServiceVersion.getUrlsUnknown() > 0) {
			retVal.add(new Label("" + myServiceVersion.getUrlsUnknown()));
			retVal.add(new Image("images/icon_unknown_16.png"));
		}

		if (myServiceVersion.getUrlsFailed() > 0) {
			retVal.add(new Label("" + myServiceVersion.getUrlsFailed()));
			retVal.add(new Image("images/icon_warn_16.png"));
		}

		return retVal;
	}

	public Widget renderActions() {
		final Image retVal = (new Image("images/tools_16.png"));
		retVal.addStyleName("dashboardActionButton");
		retVal.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent theEvent) {
				actionMenu(retVal);
			}
		});
		return retVal;
	}

	private void actionMenu(Image theButton) {
		if (myActionPopup == null) {
			myActionPopup = new PopupPanel();
			myActionPopup.add(new Hyperlink("Edit Version", ""));
			myActionPopup.showRelativeTo(theButton);
		} else {
			myActionPopup.hide();
			myActionPopup = null;
		}
	}
	
	public String getCellStyle() {
		return "dashboardTableServiceVersionCell";
	}


}
