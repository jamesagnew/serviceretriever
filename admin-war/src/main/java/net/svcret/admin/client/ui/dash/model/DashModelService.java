package net.svcret.admin.client.ui.dash.model;

import static net.svcret.admin.client.AdminPortal.*;
import net.svcret.admin.client.AdminPortal;
import net.svcret.admin.client.nav.NavProcessor;
import net.svcret.admin.client.ui.components.PButton;
import net.svcret.admin.shared.model.BaseDtoDashboardObject;
import net.svcret.admin.shared.model.BaseDtoServiceVersion;
import net.svcret.admin.shared.model.GDomain;
import net.svcret.admin.shared.model.GService;
import net.svcret.admin.shared.model.GServiceVersionList;
import net.svcret.admin.shared.model.HierarchyEnum;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
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
		return DashModelDomain.returnImageForStatus(myService);
	}

	@Override
	public HierarchyEnum getType() {
		return HierarchyEnum.SERVICE;
	}

	@Override
	public BaseDtoDashboardObject getModel() {
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
	public PButton renderActions() {
		final PButton retVal = new PButton(AdminPortal.IMAGES.iconTools16());
		retVal.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent theClickEvent) {

				if (myActionPopup == null || myActionPopup.isShowing() == false) {
					myActionPopup = new DashActionPopupPanel(true, true);

					final GDomain domain = myDomain;
					final GService service = myService;

					FlowPanel content = createActionPanel(myActionPopup, domain, service, null);

					
					myActionPopup.add(content);
					myActionPopup.showRelativeTo(retVal);

				} else {
					myActionPopup.hide();
					myActionPopup = null;
				}
			}

		});
		return retVal;
	}

	
	static FlowPanel createActionPanel(final PopupPanel thePopupPanel, final GDomain domain, final GService service, FlowPanel thePreviousContent) {
		final FlowPanel content = new FlowPanel();

		if (thePreviousContent!=null) {
			createBackButton(thePopupPanel, thePreviousContent, content);
		}
		
		content.add(new HeaderLabel(service.getName()));

		// Edit domain

		Button editDomain = new ActionPButton(IMAGES.iconEdit(), "Edit Service");
		editDomain.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent theEvent) {
				thePopupPanel.hide();
				History.newItem(NavProcessor.getTokenEditService(domain.getPid(), service.getPid()));
			}
		});
		content.add(editDomain);

		// Remove Domain

		Button delete = new ActionPButton(IMAGES.iconRemove(), MSGS.actions_RemoveService());
		delete.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent theEvent) {
				thePopupPanel.hide();
				History.newItem(NavProcessor.getTokenDeleteService(domain.getPid(), service.getPid()));
			}
		});
		content.add(delete);

		// Message Library
		
		ActionPButton msgLib = new ActionPButton(AdminPortal.IMAGES.iconLibrary(), AdminPortal.MSGS.actions_MessageLibrary());
		msgLib.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent theEvent) {
				History.newItem(NavProcessor.getTokenMessageLibrary(HierarchyEnum.SERVICE, service.getPid()));
			}
		});
		content.add(msgLib);

		// Services

		int versionCount = service.getVersionList().size();
		content.add(new HeaderLabel(AdminPortal.MSGS.dashboard_ActionServiceVersionsHeader(versionCount)));

		// Add Version

		Button addService = new ActionPButton(IMAGES.iconAdd(), "Add New Version to Service");
		addService.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent theEvent) {
				thePopupPanel.hide();
				String newToken = NavProcessor.getTokenAddServiceVersion(domain.getPid(), service.getPid(), null);
				History.newItem(newToken);
			}
		});
		content.add(addService);

		// Versions
		
		for (final BaseDtoServiceVersion nextVersion : service.getVersionList()) {
			PButton svcButton = new ActionPButton(nextVersion.getId());
			svcButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent theEvent) {
					thePopupPanel.remove(content);
					thePopupPanel.add(DashModelServiceVersion.createActionPanel(thePopupPanel, service, nextVersion, false, content));
				}
			});
			content.add(svcButton.toForwardNavButtonPanel());
		}

		return content;

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
