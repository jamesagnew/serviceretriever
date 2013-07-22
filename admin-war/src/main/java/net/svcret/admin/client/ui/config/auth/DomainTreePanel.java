package net.svcret.admin.client.ui.config.auth;

import net.svcret.admin.client.ui.components.CssConstants;
import net.svcret.admin.shared.model.BaseGServiceVersion;
import net.svcret.admin.shared.model.GDomain;
import net.svcret.admin.shared.model.GDomainList;
import net.svcret.admin.shared.model.GService;
import net.svcret.admin.shared.model.GServiceMethod;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;

public abstract class DomainTreePanel extends FlowPanel {

	private GDomainList myDomainList;
	private ITreeStatusModel myModel;
	private Tree myServicePermissionsTree;

	public DomainTreePanel() {
		myServicePermissionsTree = new Tree();
		add(myServicePermissionsTree);
	}

	public void setModel(GDomainList theDomainList, ITreeStatusModel theModel) {
		if (theDomainList==null) {
			throw new NullPointerException("theDomainList");
		}
		myDomainList = theDomainList;

		if (theModel==null) {
			throw new NullPointerException("theModel");
		}
		myModel = theModel;
		repopulateTree();
	}

	protected abstract boolean isShowMethods();
	
	protected abstract SafeHtml getTextDomainAllServicesCheckbox(int theCount);

	protected abstract SafeHtml getTextMethodCheckbox();

	protected abstract SafeHtml getTextServiceAllVersionsCheckbox(int theCount);

	protected abstract SafeHtml getTextServiceVersionAllMethodsCheckbox(int theCount);

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
		if (myModel.isEntireDomainChecked(nextDomain)) {
			while (domainItem.getChildCount() > 0) {
				domainItem.removeItem(domainItem.getChild(0));
			}
		} else {
			for (int serviceIdx = 0; serviceIdx < nextDomain.getServiceList().size(); serviceIdx++) {
				repopulateService(nextDomain, domainItem, serviceIdx);
			} // for service

			while ((domainItem.getChildCount()) > nextDomain.getServiceList().size()) {
				domainItem.removeItem(domainItem.getChild(domainItem.getChildCount() - 1));
			}

		}
	}

	private void repopulateService(final GDomain nextDomain, TreeItem domainItem, int serviceIdx) {
		final GService nextService = nextDomain.getServiceList().get(serviceIdx);

		TreeItem serviceItem;
		if ((domainItem.getChildCount()) <= serviceIdx || !nextService.equals(domainItem.getChild(serviceIdx).getUserObject())) {
			FlowPanel servicePanel = new FlowPanel();
			servicePanel.addStyleName(CssConstants.PERMISSION_TREE_ENTRY_BLOCK);
			Label serviceLabel = new Label("Service: " + nextService.getId());
			serviceLabel.setStyleName(CssConstants.PERMISSION_TREE_ENTRY);
			servicePanel.add(serviceLabel);
			serviceItem = new TreeItem(servicePanel);
			serviceItem.setUserObject(nextService);
			insert(domainItem, serviceIdx + 1, serviceItem);

			CheckBox allServiceVersionsCheckbox = new CheckBox(getTextServiceAllVersionsCheckbox(nextService.getVersionList().size()));
			allServiceVersionsCheckbox.setStyleName(CssConstants.PERMISSION_TREE_ENTRY_ALL_CHILD_CHECK);
			// TreeItem allServiceVersions = new
			// TreeItem(allServiceVersionsCheckbox);
			//
			servicePanel.add(allServiceVersionsCheckbox);

			allServiceVersionsCheckbox.setValue(myModel.isEntireServiceChecked(nextDomain, nextService));

			allServiceVersionsCheckbox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
				@Override
				public void onValueChange(ValueChangeEvent<Boolean> theEvent) {
					myModel.setEntireServiceChecked(nextDomain, nextService, theEvent.getValue());
					repopulateTree();
				}
			});

		} else {
			serviceItem = domainItem.getChild(serviceIdx);
		}

		boolean allowAllSvcVer = myModel.isEntireServiceChecked(nextDomain, nextService);
		if (allowAllSvcVer) {
			while (serviceItem.getChildCount() > 0) {
				serviceItem.removeItem(serviceItem.getChild(0));
			}
		} else {

			for (int svcVerIdx = 0; svcVerIdx < nextService.getVersionList().size(); svcVerIdx++) {
				repopulateServiceVersion(nextDomain,nextService, serviceItem, svcVerIdx);
			} // for service version

			while ((serviceItem.getChildCount()) > nextService.getVersionList().size()) {
				serviceItem.removeItem(serviceItem.getChild(serviceItem.getChildCount() - 1));
			}

		}
	}

	private void repopulateServiceVersion(final GDomain nextDomain, final GService nextService, TreeItem serviceItem, int svcVerIdx) {
		final BaseGServiceVersion nextSvcVer = nextService.getVersionList().get(svcVerIdx);

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

			CheckBox allMethodsCheckbox = new CheckBox(getTextServiceVersionAllMethodsCheckbox(nextSvcVer.getMethodList().size()));
			allMethodsCheckbox.setStyleName(CssConstants.PERMISSION_TREE_ENTRY_ALL_CHILD_CHECK);
			svcVerPanel.add(allMethodsCheckbox);
			// TreeItem allsvcVerVersions = new TreeItem(allMethodsCheckbox);
			// svcVerItem.addItem(allsvcVerVersions);

			allMethodsCheckbox.setValue(myModel.isEntireServiceVersionChecked(nextDomain, nextService, nextSvcVer));

			allMethodsCheckbox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
				@Override
				public void onValueChange(ValueChangeEvent<Boolean> theEvent) {
					myModel.setEntireServiceVersionChecked(nextDomain, nextService, nextSvcVer, theEvent.getValue());
					repopulateTree();
				}
			});

		} else {
			svcVerItem = serviceItem.getChild(svcVerIdx);
		}

		if (!isShowMethods() || myModel.isEntireServiceVersionChecked(nextDomain, nextService, nextSvcVer)) {
			while (svcVerItem.getChildCount() > 0) {
				svcVerItem.removeItem(svcVerItem.getChild(0));
			}
		} else {
			for (int verMethodIdx = 0; verMethodIdx < nextSvcVer.getMethodList().size(); verMethodIdx++) {
				repopulateServiceVersionMethod(nextDomain, nextService, nextSvcVer, svcVerItem, verMethodIdx);
			} // for service version method

			while ((svcVerItem.getChildCount()) > nextSvcVer.getMethodList().size()) {
				svcVerItem.removeItem(svcVerItem.getChild(svcVerItem.getChildCount() - 1));
			}

		}
	}

	private void repopulateServiceVersionMethod(final GDomain theDomain, final GService theService, final BaseGServiceVersion nextSvcVer, TreeItem svcVerItem, int verMethodIdx) {
		final GServiceMethod nextMethod = nextSvcVer.getMethodList().get(verMethodIdx);

		TreeItem methodItem;
		if ((svcVerItem.getChildCount()) <= verMethodIdx || !nextMethod.equals(svcVerItem.getChild(verMethodIdx).getUserObject())) {

			FlowPanel methodPanel = new FlowPanel();
			methodPanel.addStyleName(CssConstants.PERMISSION_TREE_ENTRY_BLOCK);
			methodItem = new TreeItem(methodPanel);
			methodItem.setUserObject(nextMethod);
			insert(svcVerItem, verMethodIdx + 1, methodItem);

			Label tabLabel = new Label("Method: " + nextMethod.getName());
			tabLabel.setStyleName(CssConstants.PERMISSION_TREE_ENTRY);
			
			CheckBox nextMethodsCheckbox = new CheckBox(getTextMethodCheckbox());
			nextMethodsCheckbox.setStyleName(CssConstants.PERMISSION_TREE_ENTRY_ALL_CHILD_CHECK);
			methodPanel.add(nextMethodsCheckbox);

			nextMethodsCheckbox.setValue(myModel.isMethodChecked(theDomain, theService, nextSvcVer, nextMethod));
			nextMethodsCheckbox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
				@Override
				public void onValueChange(ValueChangeEvent<Boolean> theEvent) {
					myModel.setMethodChecked(theDomain, theService, nextSvcVer, nextMethod, theEvent.getValue());
					repopulateTree();
				}
			});

		}
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

				CheckBox allServicesCheckbox = new CheckBox(getTextDomainAllServicesCheckbox(nextDomain.getServiceList().size()));
				allServicesCheckbox.setStyleName(CssConstants.PERMISSION_TREE_ENTRY_ALL_CHILD_CHECK);
				widget.add(allServicesCheckbox);

				// TreeItem allServices = new TreeItem(allServicesCheckbox);
				// retVal.addItem(allServices);

				allServicesCheckbox.setValue(myModel.isEntireDomainChecked(nextDomain));

				allServicesCheckbox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
					@Override
					public void onValueChange(ValueChangeEvent<Boolean> theEvent) {
						myModel.setEntireDomainChecked(nextDomain, theEvent.getValue());
						repopulateTree();
					}
				});

			} else {
				// Entry already exists
				retVal = theRoot.getItem(domainIdx);
			}

			repopulateDomain(nextDomain, retVal);

		}// for domain

		while (theRoot.getItemCount() - 1 > myDomainList.size()) {
			theRoot.removeItem(theRoot.getItem(myDomainList.size() - 1));
		}


	}

	public interface ITreeStatusModel {

		boolean isEntireDomainChecked(GDomain theDomain);

		boolean isEntireServiceChecked(GDomain theDomain, GService theService);

		boolean isEntireServiceVersionChecked(GDomain theDomain, GService theService, BaseGServiceVersion theServiceVersion);

		boolean isMethodChecked(GDomain theDomain, GService theService, BaseGServiceVersion theSvcVer, GServiceMethod theMethod);

		void setEntireDomainChecked(GDomain theDomain, boolean theValue);

		void setEntireServiceChecked(GDomain theDomain, GService theService, boolean theValue);

		void setEntireServiceVersionChecked(GDomain theDomain, GService theService, BaseGServiceVersion theServiceVersion, boolean theValue);

		void setMethodChecked(GDomain theDomain, GService theService, BaseGServiceVersion theSvcVer, GServiceMethod theMethod, Boolean theValue);


	}

}
