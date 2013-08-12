package net.svcret.admin.shared.model;

import net.svcret.admin.shared.enm.MonitorRuleTypeEnum;

public class DtoMonitorRuleActive extends BaseGMonitorRule {

	private static final long serialVersionUID = 1L;

	private DtoMonitorRuleActiveCheckList myCheckList = new DtoMonitorRuleActiveCheckList();

	public DtoMonitorRuleActiveCheckList getCheckList() {
		return myCheckList;
	}

	@Override
	public MonitorRuleTypeEnum getRuleType() {
		return MonitorRuleTypeEnum.ACTIVE;
	}

}
