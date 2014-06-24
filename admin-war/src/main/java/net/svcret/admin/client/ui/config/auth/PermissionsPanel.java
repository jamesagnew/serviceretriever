package net.svcret.admin.client.ui.config.auth;

import static net.svcret.admin.client.AdminPortal.*;

import java.util.Collections;
import java.util.Set;

import net.svcret.admin.client.ui.components.CssConstants;
import net.svcret.admin.client.ui.components.HtmlBr;
import net.svcret.admin.client.ui.components.HtmlH1;
import net.svcret.admin.client.ui.components.LoadingSpinner;
import net.svcret.admin.client.ui.components.TwoColumnGrid;
import net.svcret.admin.client.ui.config.auth.DomainTreePanel.ITreeStatusModel;
import net.svcret.admin.shared.IAsyncLoadCallback;
import net.svcret.admin.shared.Model;
import net.svcret.admin.shared.model.*;
import net.svcret.admin.shared.model.DtoMethod;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

public class PermissionsPanel extends FlowPanel {

	private CheckBox myAllDomainsCheckbox;
	private DtoDomainList myDomainList;
	private IHasPermissions myPermissions;
	private LoadingSpinner myServicePermissionsSpinner;
	private DomainTreePanel myServicePermissionsTree;
	private FlowPanel myServicePermissionsTreePanel;
	private CheckBox mySuperUserCheckbox;

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

		myServicePermissionsTree = new PermissionsPanelTree();
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
		Model.getInstance().loadDomainList(new IAsyncLoadCallback<DtoDomainList>() {
			@Override
			public void onSuccess(DtoDomainList theDomainList) {
				myDomainList = theDomainList;
				myServicePermissionsSpinner.hideCompletely();

				// TreeItem root = new TreeItem();
				// myServicePermissionsTree.addItem(root);

				DomainTreePanel.ITreeStatusModel model = new ITreeStatusModel() {


					@Override
					public boolean isEntireDomainChecked(DtoDomain theDomain) {
						GUserDomainPermission domainPermission = myPermissions.getDomainPermission(theDomain.getPid());
						return domainPermission != null && domainPermission.isAllowAllServices();
					}

					@Override
					public boolean isEntireServiceChecked(DtoDomain theNextDomain, GService theService) {
						GUserDomainPermission domain = myPermissions.getDomainPermission(theNextDomain.getPid());
						if (domain != null) {
							return domain.getServicePermission(theService.getPid()) != null;
						}
						return false;
					}

					@Override
					public boolean isEntireServiceVersionChecked(DtoDomain theDomain, GService theService, BaseDtoServiceVersion theServiceVersion) {
						GUserDomainPermission domain = myPermissions.getDomainPermission(theDomain.getPid());
						if (domain != null) {
							GUserServicePermission service = domain.getServicePermission(theService.getPid());
							if (service != null) {
								GUserServiceVersionPermission svcVer = service.getOrCreateServiceVersionPermission(theServiceVersion.getPid());
								if (svcVer!=null) {
								return svcVer.isAllowAllServiceVersionMethods();
								}
							}
						}
						return false;
					}

					@Override
					public boolean isMethodChecked(DtoDomain theDomain, GService theService, BaseDtoServiceVersion theSvcVer, DtoMethod theMethod) {
						GUserDomainPermission domain = myPermissions.getDomainPermission(theDomain.getPid());
						if (domain != null) {
							GUserServicePermission service = domain.getServicePermission(theService.getPid());
							if (service != null) {
								GUserServiceVersionPermission svcVer = service.getOrCreateServiceVersionPermission(theSvcVer.getPid());
								if (svcVer != null) {
									GUserServiceVersionMethodPermission method = svcVer.getServiceVersionMethodPermission(theMethod.getPid());
									return method != null;
								}
							}
						}
						return false;
					}

					@Override
					public void setEntireDomainChecked(DtoDomain theDomain, boolean theValue) {
						myPermissions.getOrCreateDomainPermission(theDomain.getPid()).setAllowAllServices(theValue);
					}

					@Override
					public void setEntireServiceChecked(DtoDomain theDomain, GService theService, boolean theValue) {
						myPermissions.getOrCreateDomainPermission(theDomain.getPid()).getOrCreateServicePermission(theService.getPid()).setAllowAllServiceVersions(theValue);
					}

					@Override
					public void setEntireServiceVersionChecked(DtoDomain theDomain, GService theService, BaseDtoServiceVersion theServiceVersion, boolean theValue) {
						myPermissions.getOrCreateDomainPermission(theDomain.getPid()).getOrCreateServicePermission(theService.getPid()).getOrCreateServiceVersionPermission(theServiceVersion.getPid()).setAllowAllServiceVersionMethods(theValue);
					}

					@Override
					public void setMethodChecked(DtoDomain theDomain, GService theService, BaseDtoServiceVersion theSvcVer, DtoMethod theMethod, Boolean theValue) {
						if (theValue) {
							myPermissions.getOrCreateDomainPermission(theDomain.getPid()).getOrCreateServicePermission(theService.getPid()).getOrCreateServiceVersionPermission(theSvcVer.getPid()).getOrCreateServiceVersionMethodPermission(theMethod.getPid());
						}else {
							GUserDomainPermission domain = myPermissions.getDomainPermission(theDomain.getPid());
							if (domain != null) {
								GUserServicePermission service = domain.getServicePermission(theService.getPid());
								if (service!=null) {
									GUserServiceVersionPermission svcVer = service.getServiceVersionPermission(theSvcVer.getPid());
									if (svcVer != null) {
										svcVer.removeMethodPermission(theMethod.getPid());
									}
								}
							}
						}
					}

				};

				myServicePermissionsTree.setModel(myDomainList, model);
			}
		});

	}

}
