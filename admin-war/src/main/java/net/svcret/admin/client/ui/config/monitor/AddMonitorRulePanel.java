package net.svcret.admin.client.ui.config.monitor;

import net.svcret.admin.shared.model.GMonitorRule;

public class AddMonitorRulePanel extends BaseMonitorRulePanel {

	public AddMonitorRulePanel() {
		GMonitorRule rule = new GMonitorRule();
		rule.setName("New Rule");
		rule.setActive(true);
		setRule(rule);
	}
	
	@Override
	protected String getPanelTitle() {
		return "Add Monitor Rule";
	}

}
