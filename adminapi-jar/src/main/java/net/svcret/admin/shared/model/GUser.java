package net.svcret.admin.shared.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GUser extends BaseDtoDashboardObject implements IHasPermissions {

	private static final long serialVersionUID = 1L;

	private List<String> myAllowableSourceIps;
	private boolean myAllowAllDomains;
	private long myAuthHostPid;
	private String myChangePassword;
	private HashSet<String> myContactEmails;
	private String myContactNotes;
	private String myDescription;
	private ArrayList<GUserDomainPermission> myDomainPermissions;
	private HashSet<UserGlobalPermissionEnum> myGlobalPermissions;
	private Date myStatsLastAccess;
	private GThrottle myThrottle = new GThrottle();
	private String myUsername;

	public GUser() {
		super();
	}

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

	/**
	 * @return the allowableSourceIps
	 */
	public List<String> getAllowableSourceIps() {
		if (myAllowableSourceIps == null) {
			return Collections.emptyList();
		}
		return myAllowableSourceIps;
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

	public HashSet<String> getContactEmails() {
		if (myContactEmails == null) {
			myContactEmails = new HashSet<String>();
		}
		return myContactEmails;
	}

	/**
	 * @return the contactNotes
	 */
	public String getContactNotes() {
		return myContactNotes;
	}

	public String getDescription() {
		return myDescription;
	}

	@Override
	public GUserDomainPermission getDomainPermission(long theDomainPid) {
		for (GUserDomainPermission next : getDomainPermissions()) {
			if (next.getDomainPid() == theDomainPid) {
				return next;
			}
		}
		return null;
	}

	@Override
	public List<GUserDomainPermission> getDomainPermissions() {
		if (myDomainPermissions == null) {
			myDomainPermissions = new ArrayList<GUserDomainPermission>();
		}
		return myDomainPermissions;
	}

	/*
	 * (non-Javadoc)
	 * 
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
	 * @return the statsLastAccess
	 */
	public Date getStatsLastAccess() {
		return myStatsLastAccess;
	}

	public GThrottle getThrottle() {
		return myThrottle;
	}

	/**
	 * @return the id
	 */
	public String getUsername() {
		return myUsername;
	}

	private void initGlobalPermissions() {
		if (myGlobalPermissions == null) {
			setGlobalPermissions(new HashSet<UserGlobalPermissionEnum>());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.svcret.admin.shared.model.IHasPermissions#isAllowAllDomains()
	 */
	@Override
	public boolean isAllowAllDomains() {
		return myAllowAllDomains;
	}

	@Override
	public void merge(BaseDtoObject theObject) {
		super.merge(theObject);

		GUser obj = (GUser) theObject;
		setUsername(obj.getUsername());
		setAllowableSourceIps(obj.getAllowableSourceIps());
		setStatsInitialized(obj.getStatsInitialized());
	}

	public void removeDomainPermission(GUserDomainPermission theGUserDomainPermission) {
		myDomainPermissions.remove(theGUserDomainPermission);
	}

	@Override
	public void removeDomainPermission(long theDomainPid) {
		for (GUserDomainPermission next : getDomainPermissions()) {
			if (next.getDomainPid() == theDomainPid) {
				myDomainPermissions.remove(next);
				break;
			}
		}
	}

	public void removeGlobalPermission(UserGlobalPermissionEnum thePermission) {
		initGlobalPermissions();
		myGlobalPermissions.remove(thePermission);
	}

	public void setAllowableSourceIps(List<String> theAllowSourceIpsAsStrings) {
		myAllowableSourceIps = theAllowSourceIpsAsStrings;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.svcret.admin.shared.model.IHasPermissions#setAllowAllDomains(boolean)
	 */
	@Override
	public void setAllowAllDomains(boolean theAllowAllDomains) {
		myAllowAllDomains = theAllowAllDomains;
	}

	/**
	 * @param theAuthHostPid
	 *            the authHostPid to set
	 */
	public void setAuthHostPid(long theAuthHostPid) {
		myAuthHostPid = theAuthHostPid;
	}

	/**
	 * @param theChangePassword
	 *            the changePassword to set
	 */
	public void setChangePassword(String theChangePassword) {
		myChangePassword = theChangePassword;
	}

	public void setContactEmails(Set<String> theContactEmails) {
		if (theContactEmails == null) {
			throw new NullPointerException();
		}
		myContactEmails = new HashSet<String>(theContactEmails);
	}

	public void setContactNotes(String theNotes) {
		myContactNotes = theNotes;
	}

	public void setDescription(String theDescription) {
		myDescription = theDescription;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.svcret.admin.shared.model.IHasPermissions#setDomainPermissions(java
	 * .util.List)
	 */
	@Override
	public void setDomainPermissions(List<GUserDomainPermission> theDomainPermissions) {
		myDomainPermissions = (ArrayList<GUserDomainPermission>) theDomainPermissions;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.svcret.admin.shared.model.IHasPermissions#setGlobalPermissions(java
	 * .util.Set)
	 */
	@Override
	public void setGlobalPermissions(Set<UserGlobalPermissionEnum> theGlobalPermissions) {
		if (theGlobalPermissions.getClass() != HashSet.class) {
			myGlobalPermissions = new HashSet<UserGlobalPermissionEnum>(theGlobalPermissions);
		} else {
			myGlobalPermissions = (HashSet<UserGlobalPermissionEnum>) theGlobalPermissions;
		}
	}

	/**
	 * @param theStatsLastAccess
	 *            the statsLastAccess to set
	 */
	public void setStatsLastAccess(Date theStatsLastAccess) {
		myStatsLastAccess = theStatsLastAccess;
	}

	public void setThrottle(GThrottle theThrottle) {
		myThrottle = theThrottle;
	}

	/**
	 * @param theId
	 *            the id to set
	 */
	public void setUsername(String theUsername) {
		myUsername = theUsername;
	}

	@Override
	public List<BaseDtoServiceVersion> getAllServiceVersions() {
		throw new UnsupportedOperationException();
	}

}
