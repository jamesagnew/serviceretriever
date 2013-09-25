package net.svcret.admin.shared.model;

import net.svcret.admin.shared.enm.MonitorRuleTypeEnum;

public class GMonitorRuleList extends BaseDtoList<BaseDtoMonitorRule> {

	private static final long serialVersionUID = 1L;

	public BaseDtoMonitorRule getRuleByPid(long thePid) {
		for (BaseDtoMonitorRule next : this) {
			if (next.getPid() == thePid) {
				return next;
			}
		}
		return null;
	}

	public DtoMonitorRuleActiveCheck getActiveCheckByPid(long thePid) {
		for (BaseDtoMonitorRule next : this) {
			if (next.getRuleType() == MonitorRuleTypeEnum.ACTIVE) {
				DtoMonitorRuleActive active = (DtoMonitorRuleActive)next;
				for (DtoMonitorRuleActiveCheck nextCheck : active.getCheckList()) {
					if (nextCheck.getPid() == thePid) {
						return nextCheck;
					}
				}
			}
		}
		return null;
	}

}
