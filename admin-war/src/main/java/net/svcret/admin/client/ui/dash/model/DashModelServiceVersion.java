package net.svcret.admin.client.ui.dash.model;

import static net.svcret.admin.client.AdminPortal.*;
import net.svcret.admin.client.AdminPortal;
import net.svcret.admin.client.nav.NavProcessor;
import net.svcret.admin.client.ui.components.PButton;
import net.svcret.admin.shared.model.BaseDtoDashboardObject;
import net.svcret.admin.shared.model.BaseDtoServiceVersion;
import net.svcret.admin.shared.model.GService;
import net.svcret.admin.shared.model.HierarchyEnum;
import net.svcret.admin.shared.model.IProvidesUrlCount;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

public class DashModelServiceVersion extends BaseDashModel implements IDashModel {

	private BaseDtoServiceVersion mySvcVer;
	private PopupPanel myActionPopup;
	private GService myService;

	public DashModelServiceVersion(GService theService, BaseDtoServiceVersion theServiceVersion) {
		super(theServiceVersion);
		myService = theService;
		mySvcVer = theServiceVersion;
	}

	@Override
	public Widget renderName() {
		return renderName(AdminPortal.MSGS.dashboard_ServiceVersionPrefix(), mySvcVer.getId(), null);
	}

	@Override
	public int hashCode() {
		return mySvcVer.hashCode();
	}

	@Override
	public boolean equals(Object theObj) {
		if (!(theObj instanceof DashModelServiceVersion)) {
			return false;
		}
		return ((DashModelServiceVersion) theObj).mySvcVer.equals(theObj);
	}

	@Override
	public Widget renderStatus() {
		return DashModelDomain.returnImageForStatus(mySvcVer);
	}

	@Override
	public HierarchyEnum getType() {
		return HierarchyEnum.VERSION;
	}

	@Override
	public BaseDtoDashboardObject getModel() {
		return mySvcVer;
	}

	@Override
	public Widget renderUrls() {
		return renderUrlCount(mySvcVer);
	}

	public static Widget renderUrlCount(IProvidesUrlCount theObj) {
		FlowPanel retVal = new FlowPanel();
		retVal.setStyleName("urlStatusSummaryPanel");

		boolean found = false;
		if (theObj.getUrlsActive() != null && theObj.getUrlsActive() > 0) {
			retVal.add(new Label("" + theObj.getUrlsActive()));
			retVal.add(new Image("images/icon_check_16.png"));
			found = true;
		}

		if (theObj.getUrlsUnknown() != null && theObj.getUrlsUnknown() > 0) {
			retVal.add(new Label("" + theObj.getUrlsUnknown()));
			retVal.add(new Image("images/icon_unknown_16.png"));
			found = true;
		}

		if (theObj.getUrlsDown() != null && theObj.getUrlsDown() > 0) {
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

	private void actionMenu(PButton theRetVal) {
		if (myActionPopup == null || myActionPopup.isShowing() == false) {

			myActionPopup = new DashActionPopupPanel(true, true);

			FlowPanel content = createActionPanel(myActionPopup, myService, mySvcVer, true, null);

			myActionPopup.add(content);
			myActionPopup.showRelativeTo(theRetVal);
		} else {
			myActionPopup.hide();
			myActionPopup = null;
		}
	}

	static FlowPanel createActionPanel(final PopupPanel theActionPopup, final GService theService, final BaseDtoServiceVersion theSvcVer, boolean addServiceToTitle, final FlowPanel thePreviousContent) {
		final FlowPanel content = new FlowPanel();

		if (thePreviousContent != null) {
			createBackButton(theActionPopup, thePreviousContent, content);
		}

		SafeHtmlBuilder titleB = new SafeHtmlBuilder();
		if (addServiceToTitle) {
			titleB.appendEscaped(theService.getName());
			titleB.appendHtmlConstant("<br/>");
		}

		titleB.appendEscaped(theSvcVer.getId());
		content.add(new HeaderLabel(titleB.toSafeHtml()));

		// Edit Service Version

		Button editServiceVersion = new ActionPButton(AdminPortal.IMAGES.iconEdit(), MSGS.actions_openEdit());
		editServiceVersion.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent theEvent) {
				theActionPopup.hide();
				History.newItem(NavProcessor.getTokenEditServiceVersion(theSvcVer.getPid()));
			}
		});
		content.add(editServiceVersion);

		// Delete

		Button deleteServiceVersion = new ActionPButton(AdminPortal.IMAGES.iconRemove(), AdminPortal.MSGS.actions_Remove());
		deleteServiceVersion.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent theEvent) {
				theActionPopup.hide();
				History.newItem(NavProcessor.getTokenDeleteServiceVersion(theSvcVer.getPid()));
			}
		});
		content.add(deleteServiceVersion);

		// View Runtime Status

		ActionPButton viewStatus = new ActionPButton(IMAGES.iconStatus(), MSGS.actions_Stats());
		viewStatus.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent theEvent) {
				History.newItem(NavProcessor.getTokenServiceVersionStats(theSvcVer.getPid()));
			}
		});
		content.add(viewStatus);

		// View Recent Transactions

		ActionPButton viewRecentTransactions = new ActionPButton(AdminPortal.IMAGES.iconTransactions(), AdminPortal.MSGS.actions_RecentTransactions());
		viewRecentTransactions.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent theEvent) {
				History.newItem(NavProcessor.getTokenServiceVersionRecentMessages(theSvcVer.getPid(), false));
			}
		});
		content.add(viewRecentTransactions);

		// Test Version

		ActionPButton testSvcVer = new ActionPButton(IMAGES.iconTest16(), MSGS.actions_TestServiceVersion());
		testSvcVer.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent theEvent) {
				History.newItem(NavProcessor.getTokenTestServiceVersion(theSvcVer.getPid()));
			}
		});
		content.add(testSvcVer);

		// Message Library

		ActionPButton msgLib = new ActionPButton(AdminPortal.IMAGES.iconLibrary(), AdminPortal.MSGS.actions_MessageLibrary());
		msgLib.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent theEvent) {
				History.newItem(NavProcessor.getTokenMessageLibrary(HierarchyEnum.VERSION, theSvcVer.getPid()));
			}
		});
		content.add(msgLib);

		return content;
	}

	@Override
	public String getCellStyle() {
		return "dashboardTableServiceVersionCell";
	}

	@Override
	public boolean hasChildren() {
		return mySvcVer.getMethodList().size() > 0;
	}

}
