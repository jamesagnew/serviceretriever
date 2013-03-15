package ca.uhn.sail.proxy.admin.client.ui.dash.model;

import java.util.Date;

import ca.uhn.sail.proxy.admin.client.nav.NavProcessor;
import ca.uhn.sail.proxy.admin.client.ui.components.HtmlBr;
import ca.uhn.sail.proxy.admin.client.ui.components.Sparkline;
import ca.uhn.sail.proxy.admin.shared.model.BaseGDashboardObject;
import ca.uhn.sail.proxy.admin.shared.model.GDomain;
import ca.uhn.sail.proxy.admin.shared.model.HierarchyEnum;
import ca.uhn.sail.proxy.admin.shared.model.StatusEnum;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

public class DashModelDomain extends BaseDashModel implements IDashModel {

	private GDomain myDomain;
	private PopupPanel myActionPopup;

	public DashModelDomain(GDomain theDomain) {
		myDomain = theDomain;
	}

	public Widget renderName() {
		SafeHtml safeHtml = new SafeHtmlBuilder().appendEscaped(myDomain.getName()).toSafeHtml();
		return new HTML(safeHtml);
	}

	@Override
	public int hashCode() {
		return myDomain.hashCode();
	}

	@Override
	public boolean equals(Object theObj) {
		if (!(theObj instanceof DashModelDomain)) {
			return false;
		}
		return ((DashModelDomain) theObj).myDomain.equals(myDomain);
	}

	public Widget renderStatus() {
		StatusEnum status = myDomain.getStatus();
		return returnImageForStatus(status);
	}

	public static Widget returnImageForStatus(StatusEnum status) {
		if (status == null) {
			GWT.log("Status is null");
			return null;
		}

		switch (status) {
		case ACTIVE:
			return new Image("images/icon_check_16.png");
		case DOWN:
			return new Image("images/icon_warn_16.png");
		case UNKNOWN:
			return new Image("images/icon_unknown_16.png");
		}
		return null;
	}

	public Widget renderUsage() {
		int[] list = myDomain.getTransactions60mins();
		return returnSparklineFor60mins(list);
	}

	public HierarchyEnum getType() {
		return HierarchyEnum.DOMAIN;
	}

	public BaseGDashboardObject<?> getModel() {
		return myDomain;
	}

	public static Widget returnSparklineFor60mins(int[] theList) {
		if (theList == null) {
			GWT.log(new Date() + " - No 60 minutes data");
			return null;
		}
		String text = theList[theList.length - 1] + "/min";
		Sparkline retVal = new Sparkline(theList, text);
		retVal.setWidth("100px");
		return retVal;
	}

	public Widget renderUrls() {
		return null;
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
			FlowPanel content = new FlowPanel();
			myActionPopup.add(content);
			
			Button editDomain = new Button("Edit Domain");
			editDomain.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent theEvent) {
					myActionPopup.hide();
					myActionPopup = null;
					History.newItem(NavProcessor.getTokenEditDomain(true, myDomain.getPid()));
				}
			});
			content.add(editDomain);

			content.add(new HtmlBr());
			
			Button addService = new Button("Add Service");
			addService.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent theEvent) {
					myActionPopup.hide();
					myActionPopup=null;
					String newToken = NavProcessor.getTokenAddService(true, myDomain.getPid());
					History.newItem(newToken);
				}
			});
			content.add(addService);
			
			myActionPopup.showRelativeTo(theButton);
		} else {
			myActionPopup.hide();
			myActionPopup = null;
		}
	}

	public String getCellStyle() {
		return "dashboardTableDomainCell";
	}

}
