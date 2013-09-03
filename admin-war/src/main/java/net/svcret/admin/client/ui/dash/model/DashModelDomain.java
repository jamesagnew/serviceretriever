package net.svcret.admin.client.ui.dash.model;

import static net.svcret.admin.client.AdminPortal.*;
import net.svcret.admin.client.AdminPortal;
import net.svcret.admin.client.nav.NavProcessor;
import net.svcret.admin.client.ui.components.CssConstants;
import net.svcret.admin.client.ui.components.PButton;
import net.svcret.admin.shared.model.BaseGDashboardObject;
import net.svcret.admin.shared.model.GDomain;
import net.svcret.admin.shared.model.GService;
import net.svcret.admin.shared.model.HierarchyEnum;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

public class DashModelDomain extends BaseDashModel implements IDashModel {

	private PopupPanel myActionPopup;
	private GDomain myDomain;

	public DashModelDomain(GDomain theDomain) {
		super(theDomain);
		myDomain = theDomain;
	}

	private void actionMenu(PButton theRetVal) {
		if (myActionPopup == null || myActionPopup.isShowing() == false) {
			myActionPopup = new DashActionPopupPanel(true, true);
			myActionPopup.addStyleName(CssConstants.DASHBOARD_ACTION_PANEL);

			final FlowPanel content = new FlowPanel();

			content.add(new HeaderLabel(myDomain.getName()));

			// Edit domain

			Button editDomain = new ActionPButton(IMAGES.iconEdit(), "Edit Domain");
			editDomain.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent theEvent) {
					myActionPopup.hide();
					myActionPopup = null;
					History.newItem(NavProcessor.getTokenEditDomain(true, myDomain.getPid()));
				}
			});
			content.add(editDomain);

			// Remove Domain

			Button delete = new ActionPButton(IMAGES.iconRemove(), MSGS.actions_RemoveDomain());
			delete.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent theEvent) {
					myActionPopup.hide();
					History.newItem(NavProcessor.getTokenDeleteDomain(true, myDomain.getPid()));
				}
			});
			content.add(delete);

			// Message Library
			
			ActionPButton msgLib = new ActionPButton(AdminPortal.IMAGES.iconLibrary(), AdminPortal.MSGS.actions_MessageLibrary());
			msgLib.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent theEvent) {
					History.newItem(NavProcessor.getTokenMessageLibrary(true, HierarchyEnum.DOMAIN, myDomain.getPid()));
				}
			});
			content.add(msgLib);

			// Services

			int serviceCount = myDomain.getServiceList().size();
			content.add(new HeaderLabel(AdminPortal.MSGS.dashboard_ActionDomainServicesHeader(serviceCount)));

			// Add service

			Button addService = new ActionPButton(IMAGES.iconAdd(), "Add Service to Domain");
			addService.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent theEvent) {
					myActionPopup.hide();
					myActionPopup = null;
					String newToken = NavProcessor.getTokenAddService(true, myDomain.getPid());
					History.newItem(newToken);
				}
			});
			content.add(addService);

			for (final GService next : myDomain.getServiceList()) {
				PButton svcButton = new ActionPButton(next.getName());
				svcButton.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent theEvent) {
						myActionPopup.remove(content);
						myActionPopup.add(DashModelService.createActionPanel(myActionPopup, myDomain, next, content));
					}
				});
				content.add(svcButton.toForwardNavButtonPanel());
			}
			
			
			myActionPopup.add(content);
			myActionPopup.showRelativeTo(theRetVal);
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
	public BaseGDashboardObject getModel() {
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
	public PButton renderActions() {
		final PButton retVal = new PButton(AdminPortal.IMAGES.iconTools16());
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
		String postfix = null;
		if (myDomain.getServiceList().size() == 0) {
			postfix = AdminPortal.MSGS.dashboard_DomainNoServicesSuffix();
		}

		return renderName(AdminPortal.MSGS.dashboard_DomainPrefix(), myDomain.getName(), postfix);
	}

	@Override
	public Widget renderStatus() {
		return returnImageForStatus(myDomain);
	}

	@Override
	public Widget renderUrls() {
		if (myDomain.getServiceList().size() == 0) {
			return null;
		}
		return DashModelServiceVersion.renderUrlCount(myDomain);
	}

	@Override
	public boolean hasChildren() {
		return myDomain.getServiceList().size() > 0;
	}

}
