package net.svcret.admin.shared.model;

import java.util.ArrayList;
import java.util.List;

public class GUserDomainPermission extends BaseGObject<GUserDomainPermission> {

	private static final long serialVersionUID = 1L;
	
	private long myDomainPid;
	private boolean myAllowAllServices;
	private List<GUserServicePermission> myServicePermissions;
	/**
	 * @return the domainPid
	 */
	public long getDomainPid() {
		return myDomainPid;
	}
	/**
	 * @param theDomainPid the domainPid to set
	 */
	public void setDomainPid(long theDomainPid) {
		myDomainPid = theDomainPid;
	}
	/**
	 * @return the allowAllServices
	 */
	public boolean isAllowAllServices() {
		return myAllowAllServices;
	}
	/**
	 * @param theAllowAllServices the allowAllServices to set
	 */
	public void setAllowAllServices(boolean theAllowAllServices) {
		myAllowAllServices = theAllowAllServices;
	}
	/**
	 * @return the servicePermissions
	 */
	public List<GUserServicePermission> getServicePermissions() {
		if (myServicePermissions == null) {
			myServicePermissions = new ArrayList<GUserServicePermission>();
		}
		return myServicePermissions;
	}
	/**
	 * @param theServicePermissions the servicePermissions to set
	 */
	public void setServicePermissions(List<GUserServicePermission> theServicePermissions) {
		myServicePermissions = theServicePermissions;
	}
	
	@Override
	public void merge(GUserDomainPermission theObject) {
		setPid(theObject.getPid());
		setAllowAllServices(theObject.isAllowAllServices());
		setDomainPid(theObject.getDomainPid());
		setServicePermissions(theObject.getServicePermissions());
	}
	
	public GUserServicePermission getOrCreateServicePermission(long theServicePid) {
		for (GUserServicePermission next : getServicePermissions()) {
			if (next.getServicePid() == theServicePid) {
				return next;
			}
		}
		GUserServicePermission permission = new GUserServicePermission();
		permission.setServicePid(theServicePid);
		myServicePermissions.add(permission);
		return permission;
	}
	
}
