package net.svcret.admin.client.ui.config.monitor;

import static net.svcret.admin.client.AdminPortal.*;
import net.svcret.admin.client.ui.config.auth.DomainTreePanel;

import com.google.gwt.safehtml.shared.SafeHtml;

public class RuleAppliesToTree extends DomainTreePanel {

	@Override
	protected SafeHtml getTextDomainAllServicesCheckbox(int theCount) {
		return MSGS.ruleAppliesToPanel_TreeAllServicesCheckbox(theCount);
	}

	@Override
	protected SafeHtml getTextMethodCheckbox() {
		return MSGS.ruleAppliesToPanel_TreeMethodCheckbox();
	}

	@Override
	protected SafeHtml getTextServiceAllVersionsCheckbox(int theCount) {
		return MSGS.ruleAppliesToPanel_TreeAllServiceVersionsCheckbox(theCount);
	}

	@Override
	protected SafeHtml getTextServiceVersionAllMethodsCheckbox(int theCount) {
		return MSGS.ruleAppliesToPanel_TreeAllMethodsCheckbox();
	}
	
	@Override
	protected boolean isShowMethods() {
		return false;
	}


}
