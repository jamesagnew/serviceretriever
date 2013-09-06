package net.svcret.admin.shared.model;

public class GMonitorRuleList extends BaseDtoList<BaseGMonitorRule> {

	private static final long serialVersionUID = 1L;

	public BaseGMonitorRule getRuleByPid(long thePid) {
		for (BaseGMonitorRule next : this) {
			if (next.getPid() == thePid) {
				return next;
			}
		}
		return null;
	}

}
