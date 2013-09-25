package net.svcret.admin.client.ui.config.lib;

import static net.svcret.admin.client.AdminPortal.*;

import com.google.gwt.safehtml.shared.SafeHtml;

import net.svcret.admin.client.ui.config.auth.DomainTreePanel;
import net.svcret.admin.shared.model.BaseDtoServiceVersion;
import net.svcret.admin.shared.model.DtoLibraryMessage;
import net.svcret.admin.shared.model.GDomain;
import net.svcret.admin.shared.model.GDomainList;
import net.svcret.admin.shared.model.GService;
import net.svcret.admin.shared.model.GServiceMethod;

public class MessageAppliesToPanel extends DomainTreePanel {

	@Override
	protected boolean isShowMethods() {
		return false;
	}

	public void setMessage(GDomainList theDomainList, final DtoLibraryMessage theMessage) {
		setModel(theDomainList, new ITreeStatusModel() {

			@Override
			public void setMethodChecked(GDomain theDomain, GService theService, BaseDtoServiceVersion theSvcVer, GServiceMethod theMethod, Boolean theValue) {
				throw new UnsupportedOperationException();
			}

			@Override
			public void setEntireServiceVersionChecked(GDomain theDomain, GService theService, BaseDtoServiceVersion theServiceVersion, boolean theValue) {
				if (theValue) {
					theMessage.getAppliesToServiceVersionPids().add(theServiceVersion.getPid());
				} else {
					theMessage.getAppliesToServiceVersionPids().remove(theServiceVersion.getPid());
				}
			}

			@Override
			public void setEntireServiceChecked(GDomain theDomain, GService theService, boolean theValue) {
				if (theValue) {
					theMessage.getAppliesToServiceVersionPids().addAll(theService.getAllServiceVersionPids());
				} else {
					theMessage.getAppliesToServiceVersionPids().removeAll(theService.getAllServiceVersionPids());
				}
			}

			@Override
			public void setEntireDomainChecked(GDomain theDomain, boolean theValue) {
				throw new UnsupportedOperationException();
			}

			@Override
			public boolean isMethodChecked(GDomain theDomain, GService theService, BaseDtoServiceVersion theSvcVer, GServiceMethod theMethod) {
				throw new UnsupportedOperationException();
			}

			@Override
			public boolean isEntireServiceVersionChecked(GDomain theDomain, GService theService, BaseDtoServiceVersion theServiceVersion) {
				return theMessage.getAppliesToServiceVersionPids().contains(theServiceVersion.getPid());
			}

			@Override
			public boolean isEntireServiceChecked(GDomain theDomain, GService theService) {
				return false;
//				return theMessage.getAppliesToServiceVersionPids().containsAll(theService.getAllServiceVersionPids());
			}

			@Override
			public boolean isEntireDomainChecked(GDomain theDomain) {
				return false;
//				return theMessage.getAppliesToServiceVersionPids().containsAll(theDomain.getAllServiceVersionPids());
			}
		});
	}

	@Override
	protected SafeHtml getTextDomainAllServicesCheckbox(int theCount) {
		return MSGS.libraryMessageAppliesToPanel_TreeAllServicesCheckbox();
	}

	@Override
	protected SafeHtml getTextMethodCheckbox() {
		throw new UnsupportedOperationException();
	}

	@Override
	protected SafeHtml getTextServiceAllVersionsCheckbox(int theCount) {
		return MSGS.libraryMessageAppliesToPanel_TreeAllServiceVersionsCheckbox();
	}

	@Override
	protected SafeHtml getTextServiceVersionAllMethodsCheckbox(int theCount) {
		return MSGS.libraryMessageAppliesToPanel_TreeAllMethodsCheckbox();
	}

	@Override
	protected boolean isAllowDomainSelection() {
		return false;
	}

	@Override
	protected boolean isAllowServiceSelection() {
		return false;
	}

}
