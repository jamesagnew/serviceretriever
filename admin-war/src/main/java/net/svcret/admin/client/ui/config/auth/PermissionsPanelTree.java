package net.svcret.admin.client.ui.config.auth;

import static net.svcret.admin.client.AdminPortal.*;

import com.google.gwt.safehtml.shared.SafeHtml;

public class PermissionsPanelTree extends DomainTreePanel {

	@Override
	protected SafeHtml getTextDomainAllServicesCheckbox(int theCount) {
		return MSGS.permissionsPanel_TreeAllServicesCheckbox(theCount);
	}

	@Override
	protected SafeHtml getTextMethodCheckbox() {
		return MSGS.permissionsPanel_TreeMethodCheckbox();
	}

	@Override
	protected SafeHtml getTextServiceAllVersionsCheckbox(int theCount) {
		return MSGS.permissionsPanel_TreeAllServiceVersionsCheckbox(theCount);
	}

	@Override
	protected SafeHtml getTextServiceVersionAllMethodsCheckbox(int theCount) {
		return MSGS.permissionsPanel_TreeAllMethodsCheckbox(theCount);
	}

	@Override
	protected boolean isShowMethods() {
		return true;
	}

}
