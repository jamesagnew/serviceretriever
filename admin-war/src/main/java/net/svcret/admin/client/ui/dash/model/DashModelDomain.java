package net.svcret.admin.client.ui.dash.model;

import net.svcret.admin.client.nav.NavProcessor;
import net.svcret.admin.client.ui.components.HtmlBr;
import net.svcret.admin.client.ui.components.PButton;
import net.svcret.admin.shared.model.BaseGDashboardObject;
import net.svcret.admin.shared.model.GDomain;
import net.svcret.admin.shared.model.HierarchyEnum;
import net.svcret.admin.shared.model.StatusEnum;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

public class DashModelDomain extends BaseDashModel implements IDashModel {

	private PopupPanel myActionPopup;
	private GDomain myDomain;

	public DashModelDomain(GDomain theDomain) {
		super(theDomain);
		myDomain = theDomain;
	}

	private void actionMenu(Image theButton) {
		if (myActionPopup == null) {
			myActionPopup = new PopupPanel();
			
			FlowPanel content = new FlowPanel();
			myActionPopup.add(content);
			
			content.add(new Label(myDomain.getName()));
			content.add(new HtmlBr());
			
			Button editDomain = new PButton("Edit Domain");
			editDomain.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent theEvent) {
					myActionPopup.hide();
					myActionPopup = null;
					History.newItem(NavProcessor.getTokenEditDomain(true, myDomain.getPid()));
				}
			});
			content.add(editDomain);

			content.add(new HtmlBr());
			
			Button addService = new PButton("Add Service");
			addService.addClickHandler(new ClickHandler() {
				@Override
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

	@Override
	public boolean equals(Object theObj) {
		if (!(theObj instanceof DashModelDomain)) {
			return false;
		}
		return ((DashModelDomain) theObj).myDomain.equals(myDomain);
	}

	@Override
	public String getCellStyle() {
		return "dashboardTableDomainCell";
	}

	@Override
	public BaseGDashboardObject<?> getModel() {
		return myDomain;
	}

	@Override
	public HierarchyEnum getType() {
		return HierarchyEnum.DOMAIN;
	}

	@Override
	public int hashCode() {
		return myDomain.hashCode();
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

	@Override
	public Widget renderName() {
		SafeHtml safeHtml = new SafeHtmlBuilder().appendEscaped(myDomain.getName()).toSafeHtml();
		return new HTML(safeHtml);
	}

	@Override
	public Widget renderStatus() {
		StatusEnum status = myDomain.getStatus();
		return returnImageForStatus(status);
	}

	@Override
	public Widget renderUrls() {
		return DashModelServiceVersion.renderUrlCount(myDomain);
	}

	@Override
	public boolean hasChildren() {
		return myDomain.getServiceList().size() > 0;
	}



}
