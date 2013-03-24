package net.svcret.admin.shared.model;

public class GServiceVersionList extends BaseGList<BaseGServiceVersion> {

	private static final long serialVersionUID = 1L;

	public BaseGServiceVersion getVersionByPid(long thePid) {
		for (BaseGServiceVersion next : this) {
			if  (next.getPid() == thePid) {
				return next;
			}
		}
		return null;
	}

	
}
