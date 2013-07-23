package net.svcret.admin.shared.model;

public class GMonitorRuleList extends BaseGList<GMonitorRule> {

	private static final long serialVersionUID = 1L;

	public GMonitorRule getRuleByPid(long thePid) {
		for (GMonitorRule next : this) {
			if (next.getPid() == thePid) {
				return next;
			}
		}
		return null;
	}

}
