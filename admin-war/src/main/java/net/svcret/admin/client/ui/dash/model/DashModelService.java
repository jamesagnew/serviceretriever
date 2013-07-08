package net.svcret.admin.client.ui.dash.model;

import static net.svcret.admin.client.AdminPortal.*;
import net.svcret.admin.client.AdminPortal;
import net.svcret.admin.client.nav.NavProcessor;
import net.svcret.admin.shared.model.BaseGDashboardObject;
import net.svcret.admin.shared.model.BaseGServiceVersion;
import net.svcret.admin.shared.model.GDomain;
import net.svcret.admin.shared.model.GService;
import net.svcret.admin.shared.model.GServiceVersionList;
import net.svcret.admin.shared.model.HierarchyEnum;
import net.svcret.admin.shared.model.StatusEnum;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

public class DashModelService extends BaseDashModel implements IDashModel {

	private GService myService;
	private PopupPanel myActionPopup;
	private GDomain myDomain;

	public DashModelService(GDomain theDomain, GService theService) {
		super(theService);
		myDomain = theDomain;
		myService = theService;
	}

	@Override
	public Widget renderName() {

		String postFix = null;
		String name = myService.getName();
		GServiceVersionList versionList = myService.getVersionList();
		
		if (versionList.size() == 0) {
			postFix = AdminPortal.MSGS.dashboard_ServiceNoServiceVersionsSuffix();
		} else if (versionList.size() == 1) {
			name = name + " (" + versionList.get(0).getName() + ")";
		}
		
		return renderName(AdminPortal.MSGS.dashboard_ServicePrefix(), name, postFix);
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
		return ((DashModelService) theObj).myService.equals(myService);
	}

	@Override
	public Widget renderStatus() {
		if (myService.getVersionList().size() == 0) {
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
		if (myService.getVersionList().size() == 0) {
			return null;
		}
		return DashModelServiceVersion.renderUrlCount(myService);
	}

	@Override
	public Widget renderActions() {
		final Image retVal = (new Image("images/tools_16.png"));
		retVal.addStyleName("dashboardActionButton");
		retVal.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent theClickEvent) {

				if (myActionPopup == null || myActionPopup.isShowing() == false) {
					myActionPopup = new PopupPanel(true, true);

					FlowPanel content = new FlowPanel();
					myActionPopup.add(content);

					content.add(new HeaderLabel(myService.getName()));

					// Edit domain

					Button editDomain = new ActionPButton(IMAGES.iconEdit(), "Edit Service");
					editDomain.addClickHandler(new ClickHandler() {
						@Override
						public void onClick(ClickEvent theEvent) {
							myActionPopup.hide();
							myActionPopup = null;
							History.newItem(NavProcessor.getTokenEditService(true, myDomain.getPid(), myService.getPid()));
						}
					});
					content.add(editDomain);

					// Remove Domain

					Button delete = new ActionPButton(IMAGES.iconRemove(), MSGS.actions_RemoveService());
					delete.addClickHandler(new ClickHandler() {
						@Override
						public void onClick(ClickEvent theEvent) {
							myActionPopup.hide();
							History.newItem(NavProcessor.getTokenDeleteService(true, myDomain.getPid(), myService.getPid()));
						}
					});
					content.add(delete);

					// Add service

					Button addService = new ActionPButton(IMAGES.iconAdd(), "Add New Version to Service");
					addService.addClickHandler(new ClickHandler() {
						@Override
						public void onClick(ClickEvent theEvent) {
							myActionPopup.hide();
							myActionPopup = null;
							String newToken = NavProcessor.getTokenAddServiceVersion(true, myDomain.getPid(), myService.getPid(), null);
							History.newItem(newToken);
						}
					});
					content.add(addService);

					if (myService.getVersionList().size() == 1) {
						BaseGServiceVersion svcVer = myService.getVersionList().get(0);
						DashModelServiceVersion.addToActions(content, myActionPopup, myDomain, myService, svcVer, false);
					}
					
					
					myActionPopup.showRelativeTo(retVal);

				} else {
					myActionPopup.hide();
					myActionPopup = null;
				}
			}
		});
		return retVal;
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
