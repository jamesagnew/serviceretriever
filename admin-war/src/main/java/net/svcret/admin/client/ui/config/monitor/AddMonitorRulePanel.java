package net.svcret.admin.client.ui.config.monitor;

import net.svcret.admin.shared.enm.MonitorRuleTypeEnum;
import net.svcret.admin.shared.model.BaseGMonitorRule;
import net.svcret.admin.shared.model.DtoMonitorRuleActive;
import net.svcret.admin.shared.model.GMonitorRulePassive;

public class AddMonitorRulePanel extends BaseMonitorRulePanel {

	public AddMonitorRulePanel(MonitorRuleTypeEnum theRuleType) {
		BaseGMonitorRule rule = null;
		switch (theRuleType) {
		case PASSIVE:
			rule = new GMonitorRulePassive();
			break;
		case ACTIVE:
			rule = new DtoMonitorRuleActive();
			break;
		}

		if (rule == null) {
			throw new IllegalStateException();
		}
		
		rule.setName("New Rule");
		rule.setActive(true);
		setRule(rule);
	}

	@Override
	protected String getPanelTitle() {
		return "Add Monitor Rule";
	}

}
