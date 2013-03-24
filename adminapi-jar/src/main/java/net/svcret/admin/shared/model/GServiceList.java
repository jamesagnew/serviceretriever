package net.svcret.admin.shared.model;

public class GServiceList extends BaseGList<GService> {

	private static final long serialVersionUID = 1L;

	public GService getServiceById(String theServiceId) {
		for (GService next : this) {
			if (next.getId().equals(theServiceId)) {
				return next;
			}
		}
		return null;
	}

	public GService getServiceByPid(long theServicePid) {
		for (GService next : this) {
			if (next.getId().equals(theServicePid)) {
				return next;
			}
		}
		return null;
	}

}
