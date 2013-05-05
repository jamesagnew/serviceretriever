package net.svcret.admin.client.ui.config.auth;

import static net.svcret.admin.client.AdminPortal.*;

import java.util.Collections;
import java.util.Set;

import net.svcret.admin.client.ui.components.CssConstants;
import net.svcret.admin.client.ui.components.HtmlBr;
import net.svcret.admin.client.ui.components.HtmlH1;
import net.svcret.admin.client.ui.components.LoadingSpinner;
import net.svcret.admin.client.ui.components.TwoColumnGrid;
import net.svcret.admin.shared.IAsyncLoadCallback;
import net.svcret.admin.shared.Model;
import net.svcret.admin.shared.model.BaseGServiceVersion;
import net.svcret.admin.shared.model.GDomain;
import net.svcret.admin.shared.model.GDomainList;
import net.svcret.admin.shared.model.GService;
import net.svcret.admin.shared.model.GServiceMethod;
import net.svcret.admin.shared.model.GUserDomainPermission;
import net.svcret.admin.shared.model.GUserServicePermission;
import net.svcret.admin.shared.model.GUserServiceVersionMethodPermission;
import net.svcret.admin.shared.model.GUserServiceVersionPermission;
import net.svcret.admin.shared.model.IHasPermissions;
import net.svcret.admin.shared.model.UserGlobalPermissionEnum;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;

public class PermissionsPanel extends FlowPanel {

	private CheckBox mySuperUserCheckbox;
	private LoadingSpinner myServicePermissionsSpinner;
	private Tree myServicePermissionsTree;
	private CheckBox myAllDomainsCheckbox;
	private IHasPermissions myPermissions;
	private GDomainList myDomainList;
	private FlowPanel myServicePermissionsTreePanel;

	public PermissionsPanel() {
		add(new HtmlH1(MSGS.permissionsPanel_AdministrationPermissions()));

		TwoColumnGrid adminPermsGrid = new TwoColumnGrid();
		add(adminPermsGrid);
		
		mySuperUserCheckbox = new CheckBox();
		adminPermsGrid.addRow(MSGS.permissionsPanel_SuperUserCheckbox(), mySuperUserCheckbox);
		adminPermsGrid.addDescription(MSGS.permissionsPanel_SuperUserDesc());
		
		add(new HtmlH1(MSGS.permissionsPanel_ServicePermissionsTitle()));
		add(new Label(MSGS.permissionsPanel_ServicePermissionsDesc()));

		myServicePermissionsSpinner = new LoadingSpinner();
		add(myServicePermissionsSpinner);

		TwoColumnGrid srvPermGrid = new TwoColumnGrid();
		add(srvPermGrid);
		
		myAllDomainsCheckbox = new CheckBox();
		myAllDomainsCheckbox.setStyleName(CssConstants.PERMISSION_TREE_ENTRY_ALL_CHILD_CHECK);
		myAllDomainsCheckbox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> theEvent) {
				myPermissions.setAllowAllDomains(myAllDomainsCheckbox.getValue());
				myServicePermissionsTreePanel.setVisible(!myAllDomainsCheckbox.getValue());
			}
		});
		srvPermGrid.addRow(MSGS.permissionsPanel_AllDomainsCheckbox(), myAllDomainsCheckbox);
		srvPermGrid.addDescription(MSGS.permissionsPanel_AllDomainsDesc());

		myServicePermissionsTreePanel = new FlowPanel();
		add(myServicePermissionsTreePanel);
		
		myServicePermissionsTreePanel.add(new HtmlBr());
		myServicePermissionsTreePanel.add(new Label("Select individual domains, services, etc. that this user is allowed to access in the tree below:"));
		
		myServicePermissionsTree = new Tree();
		myServicePermissionsTreePanel.add(myServicePermissionsTree);
		
	}

	public void setPermissions(IHasPermissions theResult) {
		myPermissions = theResult;

		Set<UserGlobalPermissionEnum> globalPerms = theResult.getGlobalPermissions();
		if (globalPerms == null) {
			globalPerms = Collections.emptySet();
		}

		mySuperUserCheckbox.setValue(globalPerms.contains(UserGlobalPermissionEnum.SUPERUSER));

		myAllDomainsCheckbox.setValue(theResult.isAllowAllDomains());
		myServicePermissionsTreePanel.setVisible(!myAllDomainsCheckbox.getValue());

		myServicePermissionsSpinner.show();
		Model.getInstance().loadDomainList(new IAsyncLoadCallback<GDomainList>() {
			@Override
			public void onSuccess(GDomainList theDomainList) {
				myDomainList = theDomainList;
				myServicePermissionsSpinner.hideCompletely();

//				TreeItem root = new TreeItem();
//				myServicePermissionsTree.addItem(root);

				repopulateTree();
			}
		});

	}

	private void repopulateTree() {
		Tree theRoot = myServicePermissionsTree;
		for (int domainIdx = 0; domainIdx < myDomainList.size(); domainIdx++) {

			final GDomain nextDomain = myDomainList.get(domainIdx);

			TreeItem retVal;
			if (theRoot.getItemCount() <= domainIdx || !nextDomain.equals(theRoot.getItem(domainIdx).getUserObject())) {
				// Create a new entry
				
				FlowPanel widget = new FlowPanel();
				widget.addStyleName(CssConstants.PERMISSION_TREE_ENTRY_BLOCK);
				Label tabLabel = new Label("Domain: " + nextDomain.getId());
				tabLabel.setStyleName(CssConstants.PERMISSION_TREE_ENTRY);
				widget.add(tabLabel);
				retVal = new TreeItem(widget);
				retVal.setUserObject(nextDomain);
				insert(theRoot, domainIdx, retVal);

				CheckBox allServicesCheckbox = new CheckBox(MSGS.permissionsPanel_TreeAllServicesCheckbox());
				allServicesCheckbox.setStyleName(CssConstants.PERMISSION_TREE_ENTRY_ALL_CHILD_CHECK);
				widget.add(allServicesCheckbox);
				
//				TreeItem allServices = new TreeItem(allServicesCheckbox);
//				retVal.addItem(allServices);

				GUserDomainPermission domainPermission = myPermissions.getOrCreateDomainPermission(nextDomain.getPid());
				allServicesCheckbox.setValue(domainPermission.isAllowAllServices());

				allServicesCheckbox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
					@Override
					public void onValueChange(ValueChangeEvent<Boolean> theEvent) {
						myPermissions.getOrCreateDomainPermission(nextDomain.getPid()).setAllowAllServices(theEvent.getValue());
						repopulateTree();
					}
				});

			} else {
				// Entry already exists
				retVal = theRoot.getItem(domainIdx);
			}

			repopulateDomain(nextDomain, retVal);

		}// for domain

		while (theRoot.getItemCount() -1 > myDomainList.size()) {
			theRoot.removeItem(theRoot.getItem(myDomainList.size() - 1));
		}

//		theRoot.addItem(new Label("ABAB"));
		
	}

	private void insert(Tree theParent, int theIndex, TreeItem theItem) {
		if (theParent.getItemCount() <= theIndex) {
			theParent.addItem(theItem);
		} else {
			theParent.insertItem(theIndex + 1, theItem);
		}
	}

	private void insert(final TreeItem theParent, int theIndex, TreeItem theItem) {
		if (theParent.getChildCount() <= theIndex) {
			theParent.addItem(theItem);
		} else {
			theParent.insertItem(theIndex + 1, theItem);
		}
	}

	private void repopulateDomain(final GDomain nextDomain, TreeItem domainItem) {
		GUserDomainPermission domainPermission = myPermissions.getOrCreateDomainPermission(nextDomain.getPid());
		boolean allServices = domainPermission.isAllowAllServices();
		if (allServices) {
			while (domainItem.getChildCount() > 0) {
				domainItem.removeItem(domainItem.getChild(0));
			}
		} else {
			for (int serviceIdx = 0; serviceIdx < nextDomain.getServiceList().size(); serviceIdx++) {
				repopulateService(nextDomain, domainItem, domainPermission, serviceIdx);
			} // for service

			while ((domainItem.getChildCount()) > nextDomain.getServiceList().size()) {
				domainItem.removeItem(domainItem.getChild(domainItem.getChildCount() - 1));
			}

		}
	}

	private void repopulateService(final GDomain nextDomain, TreeItem domainItem, GUserDomainPermission domainPermission, int serviceIdx) {
		final GService nextService = nextDomain.getServiceList().get(serviceIdx);
		final GUserServicePermission servicePermission = domainPermission.getOrCreateServicePermission(nextService.getPid());

		TreeItem serviceItem;
		if ((domainItem.getChildCount() ) <= serviceIdx || !nextService.equals(domainItem.getChild(serviceIdx).getUserObject())) {
			FlowPanel servicePanel = new FlowPanel();
			servicePanel.addStyleName(CssConstants.PERMISSION_TREE_ENTRY_BLOCK);
			Label serviceLabel = new Label("Service: " + nextService.getId());
			serviceLabel.setStyleName(CssConstants.PERMISSION_TREE_ENTRY);
			servicePanel.add(serviceLabel);
			serviceItem = new TreeItem(servicePanel);
			serviceItem.setUserObject(nextService);
			insert(domainItem, serviceIdx + 1, serviceItem);

			CheckBox allServiceVersionsCheckbox = new CheckBox(MSGS.permissionsPanel_TreeAllServiceVersionsCheckbox());
			allServiceVersionsCheckbox.setStyleName(CssConstants.PERMISSION_TREE_ENTRY_ALL_CHILD_CHECK);
//			TreeItem allServiceVersions = new TreeItem(allServiceVersionsCheckbox);
//			
			servicePanel.add(allServiceVersionsCheckbox);

			allServiceVersionsCheckbox.setValue(servicePermission.isAllowAllServiceVersions());

			allServiceVersionsCheckbox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
				@Override
				public void onValueChange(ValueChangeEvent<Boolean> theEvent) {
					servicePermission.setAllowAllServiceVersions(theEvent.getValue());
					repopulateTree();
				}
			});

		} else {
			serviceItem = domainItem.getChild(serviceIdx);
		}

		boolean allowAllSvcVer = servicePermission.isAllowAllServiceVersions();
		if (allowAllSvcVer) {
			while (serviceItem.getChildCount() > 0) {
				serviceItem.removeItem(serviceItem.getChild(0));
			}
		} else {

			for (int svcVerIdx = 0; svcVerIdx < nextService.getVersionList().size(); svcVerIdx++) {
				repopulateServiceVersion(nextService, servicePermission, serviceItem, svcVerIdx);
			} // for service version

			while ((serviceItem.getChildCount()) > nextService.getVersionList().size()) {
				serviceItem.removeItem(serviceItem.getChild(serviceItem.getChildCount() - 1));
			}

		}
	}

	private void repopulateServiceVersion(final GService nextService, final GUserServicePermission servicePermission, TreeItem serviceItem, int svcVerIdx) {
		BaseGServiceVersion nextSvcVer = nextService.getVersionList().get(svcVerIdx);
		final GUserServiceVersionPermission svcVerPermission = servicePermission.getOrCreateServiceVersionPermission(nextSvcVer.getPid());

		TreeItem svcVerItem;
		if ((serviceItem.getChildCount()) <= svcVerIdx || !nextSvcVer.equals(serviceItem.getChild(svcVerIdx).getUserObject())) {
			FlowPanel svcVerPanel = new FlowPanel();
			svcVerPanel.addStyleName(CssConstants.PERMISSION_TREE_ENTRY_BLOCK);
			Label svcVerLabel = new Label("Service Version: " + nextSvcVer.getId());
			svcVerLabel.setStyleName(CssConstants.PERMISSION_TREE_ENTRY);
			svcVerPanel.add(svcVerLabel);
			svcVerItem = new TreeItem(svcVerPanel);
			svcVerItem.setUserObject(nextSvcVer);
			insert(serviceItem, svcVerIdx + 1, svcVerItem);

			CheckBox allMethodsCheckbox = new CheckBox(MSGS.permissionsPanel_TreeAllMethodsCheckbox());
			allMethodsCheckbox.setStyleName(CssConstants.PERMISSION_TREE_ENTRY_ALL_CHILD_CHECK);
			svcVerPanel.add(allMethodsCheckbox);
//			TreeItem allsvcVerVersions = new TreeItem(allMethodsCheckbox);
//			svcVerItem.addItem(allsvcVerVersions);

			allMethodsCheckbox.setValue(svcVerPermission.isAllowAllServiceVersionMethods());

			allMethodsCheckbox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
				@Override
				public void onValueChange(ValueChangeEvent<Boolean> theEvent) {
					svcVerPermission.setAllowAllServiceVersionMethods(theEvent.getValue());
					repopulateTree();
				}
			});

		} else {
			svcVerItem = serviceItem.getChild(svcVerIdx);
		}

		if (svcVerPermission.isAllowAllServiceVersionMethods()) {
			while (svcVerItem.getChildCount() > 0) {
				svcVerItem.removeItem(svcVerItem.getChild(0));
			}
		} else {
			for (int verMethodIdx = 0; verMethodIdx < nextSvcVer.getMethodList().size(); verMethodIdx++) {
				repopulateServiceVersionMethod(nextSvcVer, svcVerPermission, svcVerItem, verMethodIdx);
			} // for service version method

			while ((svcVerItem.getChildCount()) > nextSvcVer.getMethodList().size()) {
				svcVerItem.removeItem(svcVerItem.getChild(svcVerItem.getChildCount() - 1));
			}

		}
	}

	private void repopulateServiceVersionMethod(BaseGServiceVersion nextSvcVer, final GUserServiceVersionPermission svcVerPermission, TreeItem svcVerItem, int verMethodIdx) {
		GServiceMethod nextMethod = nextSvcVer.getMethodList().get(verMethodIdx);
		final GUserServiceVersionMethodPermission methodPermission = svcVerPermission.getOrCreateServiceVersionMethodPermission(nextMethod.getPid());

		TreeItem methodItem;
		if ((svcVerItem.getChildCount()) <= verMethodIdx || !nextMethod.equals(svcVerItem.getChild(verMethodIdx).getUserObject())) {

			FlowPanel methodPanel = new FlowPanel();
			methodPanel.addStyleName(CssConstants.PERMISSION_TREE_ENTRY_BLOCK);
			methodItem = new TreeItem(methodPanel);
			methodItem.setUserObject(nextMethod);
			insert(svcVerItem, verMethodIdx + 1, methodItem);

			CheckBox nextMethodsCheckbox = new CheckBox("Allow Method: " + nextMethod.getName());
			nextMethodsCheckbox.setStyleName(CssConstants.PERMISSION_TREE_ENTRY_ALL_CHILD_CHECK);
			methodPanel.add(nextMethodsCheckbox);

			nextMethodsCheckbox.setValue(methodPermission.isAllow());
			nextMethodsCheckbox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
				@Override
				public void onValueChange(ValueChangeEvent<Boolean> theEvent) {
					methodPermission.setAllow(theEvent.getValue());
					repopulateTree();
				}
			});

		}
	}
}
