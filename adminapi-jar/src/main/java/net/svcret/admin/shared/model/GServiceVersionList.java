package net.svcret.admin.shared.model;

public class GServiceVersionList extends BaseGList<BaseGServiceVersion> {

	private static final long serialVersionUID = 1L;

	public GServiceVersionList() {
		setComparator(new BaseGDashboardObjectComparator());
	}

	public BaseGServiceVersion getVersionByPid(long thePid) {
		for (BaseGServiceVersion next : this) {
			if  (next.getPid() == thePid) {
				return next;
			}
		}
		return null;
	}

	public BaseGServiceVersion getFirstServiceVersion() {
		if (size() >0) {
			return get(0);
		}
		return null;
	}

	
}
