package net.svcret.admin.shared.model;

import java.util.ArrayList;
import java.util.List;

public class GUserServicePermission extends BaseGObject<GUserServicePermission> {

	private static final long serialVersionUID = 1L;

	private long myServicePid;
	private boolean myAllowAllServiceVersions;
	private List<GUserServiceVersionPermission> myServiceVersionPermissions;

	/**
	 * @return the ServicePid
	 */
	public long getServicePid() {
		return myServicePid;
	}

	/**
	 * @param theServicePid
	 *            the ServicePid to set
	 */
	public void setServicePid(long theServicePid) {
		myServicePid = theServicePid;
	}

	/**
	 * @return the allowAllServiceVersions
	 */
	public boolean isAllowAllServiceVersions() {
		return myAllowAllServiceVersions;
	}

	/**
	 * @param theAllowAllServiceVersions
	 *            the allowAllServiceVersions to set
	 */
	public void setAllowAllServiceVersions(boolean theAllowAllServiceVersions) {
		myAllowAllServiceVersions = theAllowAllServiceVersions;
	}

	/**
	 * @return the ServiceVersionPermissions
	 */
	public List<GUserServiceVersionPermission> getServiceVersionPermissions() {
		if (myServiceVersionPermissions == null) {
			myServiceVersionPermissions = new ArrayList<GUserServiceVersionPermission>();
		}
		return myServiceVersionPermissions;
	}

	public GUserServiceVersionPermission getServiceVersionPermission(long theServiceVersionPid) {
		for (GUserServiceVersionPermission next : getServiceVersionPermissions()) {
			if (next.getServiceVersionPid() == theServiceVersionPid) {
				return next;
			}
		}
		return null;
	}

	public GUserServiceVersionPermission getOrCreateServiceVersionPermission(long theServiceVersionPid) {
		for (GUserServiceVersionPermission next : getServiceVersionPermissions()) {
			if (next.getServiceVersionPid() == theServiceVersionPid) {
				return next;
			}
		}
		GUserServiceVersionPermission permission = new GUserServiceVersionPermission();
		permission.setServiceVersionPid(theServiceVersionPid);
		myServiceVersionPermissions.add(permission);
		return permission;
	}

	/**
	 * @param theServiceVersionPermissions
	 *            the ServiceVersionPermissions to set
	 */
	public void setServiceVersionPermissions(List<GUserServiceVersionPermission> theServiceVersionPermissions) {
		myServiceVersionPermissions = theServiceVersionPermissions;
	}

	@Override
	public void merge(GUserServicePermission theObject) {
		setPid(theObject.getPid());
		setAllowAllServiceVersions(theObject.isAllowAllServiceVersions());
		setServicePid(theObject.getServicePid());
		setServiceVersionPermissions(theObject.getServiceVersionPermissions());
	}

	public void removeServiceVersionPermission(long thePid) {
		for (GUserServiceVersionPermission next : getServiceVersionPermissions()) {
			if (next.getServiceVersionPid() == thePid) {
				myServiceVersionPermissions.remove(next);
				break;
			}
		}
	}

}
