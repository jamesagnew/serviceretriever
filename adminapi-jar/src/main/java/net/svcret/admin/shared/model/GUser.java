package net.svcret.admin.shared.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GUser extends BaseGObject<GUser> implements IHasPermissions {

	private static final long serialVersionUID = 1L;

	private List<String> myAllowableSourceIps;
	private boolean myAllowAllDomains;
	private long myAuthHostPid;
	private String myChangePassword;
	private String myContactNotes;
	private ArrayList<GUserDomainPermission> myDomainPermissions;
	private HashSet<UserGlobalPermissionEnum> myGlobalPermissions;
	private Date myStatsInitialized;
	private Date myStatsLastAccess;
	private int[] myStatsSecurityFailTransactions;
	private double myStatsSecurityFailTransactionsAvgPerMin;
	private int[] myStatsSuccessTransactions;
	private double myStatsSuccessTransactionsAvgPerMin;
	private GThrottle myThrottle=new GThrottle();
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
		if (myAllowableSourceIps==null) {
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

	/**
	 * @return the contactNotes
	 */
	public String getContactNotes() {
		return myContactNotes;
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
	 * @return the statsInitialized
	 */
	public Date getStatsInitialized() {
		return myStatsInitialized;
	}

	/**
	 * @return the statsLastAccess
	 */
	public Date getStatsLastAccess() {
		return myStatsLastAccess;
	}

	/**
	 * @return the statsSecurityFailTransactions
	 */
	public int[] getStatsSecurityFailTransactions() {
		return myStatsSecurityFailTransactions;
	}

	/**
	 * @return the statsSecurityFailTransactionsAvgPerMin
	 */
	public double getStatsSecurityFailTransactionsAvgPerMin() {
		return myStatsSecurityFailTransactionsAvgPerMin;
	}

	/**
	 * @return the statsTransactions
	 */
	public int[] getStatsSuccessTransactions() {
		return myStatsSuccessTransactions;
	}

	/**
	 * @return the statsSuccessTransactionsAvgPerMin
	 */
	public double getStatsSuccessTransactionsAvgPerMin() {
		return myStatsSuccessTransactionsAvgPerMin;
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

	/**
	 * @return the isStatsInitialized
	 */
	public boolean isStatsInitialized() {
		return myStatsInitialized!=null;
	}

	@Override
	public void merge(GUser theUser) {
		setPid(theUser.getPid());
		setUsername(theUser.getUsername());
		setAllowableSourceIps(theUser.getAllowableSourceIps());
		setStatsInitialized(theUser.getStatsInitialized());
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

	public void setContactNotes(String theNotes) {
		myContactNotes = theNotes;
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
	 * @param theStatsInitialized
	 *            the isStatsInitialized to set
	 */
	public void setStatsInitialized(Date theStatsInitialized) {
		myStatsInitialized = theStatsInitialized;
	}

	/**
	 * @param theStatsLastAccess
	 *            the statsLastAccess to set
	 */
	public void setStatsLastAccess(Date theStatsLastAccess) {
		myStatsLastAccess = theStatsLastAccess;
	}

	/**
	 * @param theStatsSecurityFailTransactions
	 *            the statsSecurityFailTransactions to set
	 */
	public void setStatsSecurityFailTransactions(int[] theStatsSecurityFailTransactions) {
		myStatsSecurityFailTransactions = theStatsSecurityFailTransactions;
	}

	/**
	 * @param theStatsSecurityFailTransactionsAvgPerMin
	 *            the statsSecurityFailTransactionsAvgPerMin to set
	 */
	public void setStatsSecurityFailTransactionsAvgPerMin(double theStatsSecurityFailTransactionsAvgPerMin) {
		myStatsSecurityFailTransactionsAvgPerMin = theStatsSecurityFailTransactionsAvgPerMin;
	}

	/**
	 * @param theStatsSuccessTransactions
	 *            the statsTransactions to set
	 */
	public void setStatsSuccessTransactions(int[] theStatsSuccessTransactions) {
		myStatsSuccessTransactions = theStatsSuccessTransactions;
	}

	/**
	 * @param theStatsSuccessTransactionsAvgPerMin
	 *            the statsSuccessTransactionsAvgPerMin to set
	 */
	public void setStatsSuccessTransactionsAvgPerMin(double theStatsSuccessTransactionsAvgPerMin) {
		myStatsSuccessTransactionsAvgPerMin = theStatsSuccessTransactionsAvgPerMin;
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

}
