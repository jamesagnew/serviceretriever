package net.svcret.admin.shared.model;

public class GAuthenticationHostList extends BaseDtoList<BaseDtoAuthHost>{

	private static final long serialVersionUID = 1L;

	public BaseDtoAuthHost getAuthHostByPid(long thePid) {
		for (BaseDtoAuthHost next : this) {
			if (next.getPid() == thePid) {
				return next;
			}
		}
		return null;
	}

}
