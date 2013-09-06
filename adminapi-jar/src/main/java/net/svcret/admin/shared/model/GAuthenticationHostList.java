package net.svcret.admin.shared.model;

public class GAuthenticationHostList extends BaseDtoList<BaseGAuthHost>{

	private static final long serialVersionUID = 1L;

	public BaseGAuthHost getAuthHostByPid(long thePid) {
		for (BaseGAuthHost next : this) {
			if (next.getPid() == thePid) {
				return next;
			}
		}
		return null;
	}

}
