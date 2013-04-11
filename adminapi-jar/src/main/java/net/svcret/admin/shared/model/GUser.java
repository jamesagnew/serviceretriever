package net.svcret.admin.shared.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GUser extends BaseGObject<GUser> implements IHasPermissions {

	private static final long serialVersionUID = 1L;
	
	private boolean myAllowAllDomains;
	private long myAuthHostPid;
	private String myChangePassword;
	private ArrayList<GUserDomainPermission> myDomainPermissions;
	private HashSet<UserGlobalPermissionEnum> myGlobalPermissions;
	private String myUsername;

	public void addDomainPermission(GUserDomainPermission thePermission) {
		if (myDomainPermissions == null) {
			myDomainPermissions = new ArrayList<GUserDomainPermission>();
		}
		myDomainPermissions.add(thePermission);
	}

	public void addGlobalPermission(UserGlobalPermissionEnum thePermission) {
		initGlobalPermissions();
		myGlobalPermissions.add(thePermission);
	}

	private void initGlobalPermissions() {
		if (myGlobalPermissions == null) {
			setGlobalPermissions(new HashSet<UserGlobalPermissionEnum>());
		}
	}

	/**
	 * @return the authHostPid
	 */
	public long getAuthHostPid() {
		return myAuthHostPid;
	}

	/**
	 * @return the changePassword
	 */
	public String getChangePassword() {
		return myChangePassword;
	}

	@Override
	public List<GUserDomainPermission> getDomainPermissions() {
		if (myDomainPermissions == null) {
			myDomainPermissions = new ArrayList<GUserDomainPermission>();
		}
		return myDomainPermissions;
	}

	/* (non-Javadoc)
	 * @see net.svcret.admin.shared.model.IHasPermissions#getGlobalPermissions()
	 */
	@Override
	public Set<UserGlobalPermissionEnum> getGlobalPermissions() {
		return myGlobalPermissions;
	}

	@Override
	public GUserDomainPermission getOrCreateDomainPermission(long theDomainPid) {
		for (GUserDomainPermission next : getDomainPermissions()) {
			if (next.getDomainPid() == theDomainPid) {
				return next;
			}
		}
		
		GUserDomainPermission permission = new GUserDomainPermission();
		myDomainPermissions.add(permission);
		permission.setDomainPid(theDomainPid);
		return permission;
	}

	/**
	 * @return the id
	 */
	public String getUsername() {
		return myUsername;
	}

	/* (non-Javadoc)
	 * @see net.svcret.admin.shared.model.IHasPermissions#isAllowAllDomains()
	 */
	@Override
	public boolean isAllowAllDomains() {
		return myAllowAllDomains;
	}

	@Override
	public void merge(GUser theUser) {
		setPid(theUser.getPid());
		setUsername(theUser.getUsername());
	}

	/* (non-Javadoc)
	 * @see net.svcret.admin.shared.model.IHasPermissions#setAllowAllDomains(boolean)
	 */
	@Override
	public void setAllowAllDomains(boolean theAllowAllDomains) {
		myAllowAllDomains = theAllowAllDomains;
	}

	/**
	 * @param theAuthHostPid the authHostPid to set
	 */
	public void setAuthHostPid(long theAuthHostPid) {
		myAuthHostPid = theAuthHostPid;
	}

	/**
	 * @param theChangePassword the changePassword to set
	 */
	public void setChangePassword(String theChangePassword) {
		myChangePassword = theChangePassword;
	}

	/* (non-Javadoc)
	 * @see net.svcret.admin.shared.model.IHasPermissions#setDomainPermissions(java.util.List)
	 */
	@Override
	public void setDomainPermissions(List<GUserDomainPermission> theDomainPermissions) {
		myDomainPermissions = (ArrayList<GUserDomainPermission>) theDomainPermissions;
	}

	/* (non-Javadoc)
	 * @see net.svcret.admin.shared.model.IHasPermissions#setGlobalPermissions(java.util.Set)
	 */
	@Override
	public void setGlobalPermissions(Set<UserGlobalPermissionEnum> theGlobalPermissions) {
		if (theGlobalPermissions.getClass() != HashSet.class) {
			myGlobalPermissions = new HashSet<UserGlobalPermissionEnum>(theGlobalPermissions);
		}else {
			myGlobalPermissions = (HashSet<UserGlobalPermissionEnum>) theGlobalPermissions;
		}
	}

	/**
	 * @param theId
	 *            the id to set
	 */
	public void setUsername(String theUsername) {
		myUsername = theUsername;
	}

	public void removeGlobalPermission(UserGlobalPermissionEnum thePermission) {
		initGlobalPermissions();
		myGlobalPermissions.remove(thePermission);
	}

	public void removeDomainPermission(GUserDomainPermission theGUserDomainPermission) {
		myDomainPermissions.remove(theGUserDomainPermission);
	}

}
