package net.svcret.admin.client.ui.config.auth;

import java.util.ArrayList;
import java.util.List;

import net.svcret.admin.client.ui.components.CssConstants;
import net.svcret.admin.shared.model.BaseDtoServiceVersion;
import net.svcret.admin.shared.model.DtoDomain;
import net.svcret.admin.shared.model.DtoDomainList;
import net.svcret.admin.shared.model.GService;
import net.svcret.admin.shared.model.GServiceMethod;

import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;

public abstract class DomainTreePanel extends FlowPanel {

	private DtoDomainList myDomainList;
	private ITreeStatusModel myModel;
	private Tree myServicePermissionsTree;
	private List<ChangeHandler> myChangeHandlers;

	public DomainTreePanel() {
		myServicePermissionsTree = new Tree();
		add(myServicePermissionsTree);
	}

	/**
	 * NOTE THAT NO CHANGE EVENTS WILL BE SENT WITH THE onChange() call
	 */
	public void addChangeHandler(ChangeHandler theHandler) {
		if (myChangeHandlers==null) {
			myChangeHandlers = new ArrayList<>();
		}
		myChangeHandlers.add(theHandler);
	}
	
	private void notifyChangeHandlers() {
		if (myChangeHandlers==null) {
			for (ChangeHandler next : myChangeHandlers) {
				next.onChange(null);
			}
		}
	}
	
	public void setModel(DtoDomainList theDomainList, ITreeStatusModel theModel) {
		if (theDomainList == null) {
			throw new NullPointerException("theDomainList");
		}
		myDomainList = theDomainList;

		if (theModel == null) {
			throw new NullPointerException("theModel");
		}
		myModel = theModel;
		repopulateTree();
	}

	protected abstract boolean isShowMethods();

	protected abstract boolean isAllowDomainSelection();

	protected abstract boolean isAllowServiceSelection();

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

	private boolean repopulateDomain(final DtoDomain nextDomain, TreeItem domainItem) {
		boolean retVal = false;
		if (isAllowDomainSelection() && myModel.isEntireDomainChecked(nextDomain)) {
			while (domainItem.getChildCount() > 0) {
				domainItem.removeItem(domainItem.getChild(0));
			}
		} else {
			for (int serviceIdx = 0; serviceIdx < nextDomain.getServiceList().size(); serviceIdx++) {
				retVal |= repopulateService(nextDomain, domainItem, serviceIdx);
			} // for service

			while ((domainItem.getChildCount()) > nextDomain.getServiceList().size()) {
				domainItem.removeItem(domainItem.getChild(domainItem.getChildCount() - 1));
			}

		}
		return retVal;
	}

	private boolean repopulateService(final DtoDomain nextDomain, TreeItem domainItem, int serviceIdx) {
		boolean retVal = false;
		
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

			if (isAllowServiceSelection()) {
				CheckBox allServiceVersionsCheckbox = new CheckBox(getTextServiceAllVersionsCheckbox(nextService.getVersionList().size()));
				allServiceVersionsCheckbox.setStyleName(CssConstants.PERMISSION_TREE_ENTRY_ALL_CHILD_CHECK);
				// TreeItem allServiceVersions = new
				// TreeItem(allServiceVersionsCheckbox);
				//
				servicePanel.add(allServiceVersionsCheckbox);

				boolean checked = myModel.isEntireServiceChecked(nextDomain, nextService);
				retVal |= checked;
				allServiceVersionsCheckbox.setValue(checked);

				allServiceVersionsCheckbox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
					@Override
					public void onValueChange(ValueChangeEvent<Boolean> theEvent) {
						myModel.setEntireServiceChecked(nextDomain, nextService, theEvent.getValue());
						repopulateTree();
						notifyChangeHandlers();
					}
				});
			}

		} else {
			serviceItem = domainItem.getChild(serviceIdx);
		}

		if (isAllowServiceSelection() && myModel.isEntireServiceChecked(nextDomain, nextService)) {
			while (serviceItem.getChildCount() > 0) {
				serviceItem.removeItem(serviceItem.getChild(0));
			}
		} else {

			for (int svcVerIdx = 0; svcVerIdx < nextService.getVersionList().size(); svcVerIdx++) {
				retVal |= repopulateServiceVersion(nextDomain, nextService, serviceItem, svcVerIdx);
			} // for service version

			while ((serviceItem.getChildCount()) > nextService.getVersionList().size()) {
				serviceItem.removeItem(serviceItem.getChild(serviceItem.getChildCount() - 1));
			}

		}
		
		if (retVal) {
			serviceItem.setState(true);
		}
		
		return retVal;
	}

	private boolean repopulateServiceVersion(final DtoDomain nextDomain, final GService nextService, TreeItem serviceItem, int svcVerIdx) {
		boolean retVal = false;
		
		final BaseDtoServiceVersion nextSvcVer = nextService.getVersionList().get(svcVerIdx);

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

			boolean checked = myModel.isEntireServiceVersionChecked(nextDomain, nextService, nextSvcVer);
			retVal |= checked;
			allMethodsCheckbox.setValue(checked);

			allMethodsCheckbox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
				@Override
				public void onValueChange(ValueChangeEvent<Boolean> theEvent) {
					myModel.setEntireServiceVersionChecked(nextDomain, nextService, nextSvcVer, theEvent.getValue());
					repopulateTree();
					notifyChangeHandlers();
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
				retVal |= repopulateServiceVersionMethod(nextDomain, nextService, nextSvcVer, svcVerItem, verMethodIdx);
			} // for service version method

			while ((svcVerItem.getChildCount()) > nextSvcVer.getMethodList().size()) {
				svcVerItem.removeItem(svcVerItem.getChild(svcVerItem.getChildCount() - 1));
			}

		}
		
		if (retVal) {
			svcVerItem.setState(true);
		}
		
		return retVal;
	}

	private boolean repopulateServiceVersionMethod(final DtoDomain theDomain, final GService theService, final BaseDtoServiceVersion nextSvcVer, TreeItem svcVerItem, int verMethodIdx) {
		boolean retVal = false;
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

			boolean checked = myModel.isMethodChecked(theDomain, theService, nextSvcVer, nextMethod);
			retVal |= checked;
			nextMethodsCheckbox.setValue(checked);
			
			nextMethodsCheckbox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
				@Override
				public void onValueChange(ValueChangeEvent<Boolean> theEvent) {
					myModel.setMethodChecked(theDomain, theService, nextSvcVer, nextMethod, theEvent.getValue());
					repopulateTree();
					notifyChangeHandlers();
				}
			});

		}
		return retVal;
	}

	private void repopulateTree() {
		Tree theRoot = myServicePermissionsTree;
		for (int domainIdx = 0; domainIdx < myDomainList.size(); domainIdx++) {

			final DtoDomain nextDomain = myDomainList.get(domainIdx);

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

				if (isAllowDomainSelection()) {
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
							notifyChangeHandlers();
						}
					});
				}

			} else {
				// Entry already exists
				retVal = theRoot.getItem(domainIdx);
			}

			boolean foundValues = repopulateDomain(nextDomain, retVal);
			if (foundValues) {
				retVal.setState(true);
			}

		}// for domain

		while (theRoot.getItemCount() - 1 > myDomainList.size()) {
			theRoot.removeItem(theRoot.getItem(myDomainList.size() - 1));
		}

	}

	public interface ITreeStatusModel {

		boolean isEntireDomainChecked(DtoDomain theDomain);

		boolean isEntireServiceChecked(DtoDomain theDomain, GService theService);

		boolean isEntireServiceVersionChecked(DtoDomain theDomain, GService theService, BaseDtoServiceVersion theServiceVersion);

		boolean isMethodChecked(DtoDomain theDomain, GService theService, BaseDtoServiceVersion theSvcVer, GServiceMethod theMethod);

		void setEntireDomainChecked(DtoDomain theDomain, boolean theValue);

		void setEntireServiceChecked(DtoDomain theDomain, GService theService, boolean theValue);

		void setEntireServiceVersionChecked(DtoDomain theDomain, GService theService, BaseDtoServiceVersion theServiceVersion, boolean theValue);

		void setMethodChecked(DtoDomain theDomain, GService theService, BaseDtoServiceVersion theSvcVer, GServiceMethod theMethod, Boolean theValue);

	}

}
