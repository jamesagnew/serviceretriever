package net.svcret.admin.shared.model;

import java.util.HashSet;
import java.util.Set;

public class GMonitorRule extends BaseGObject<GMonitorRule> {

	private static final long serialVersionUID = 1L;

	private boolean isActive;
	private Set<GMonitorRuleAppliesTo> myAppliesTo;
	private Integer myFireForBackingServiceLatencyIsAboveMillis;
	private Integer myFireForBackingServiceLatencySustainTimeMins;
	private boolean myFireIfAllBackingUrlsAreUnavailable;
	private boolean myFireIfSingleBackingUrlIsUnavailable;
	private String myName;
	private Set<String> myNotifyEmailContacts;

	public boolean appliesTo(BaseGServiceVersion theSvcVer) {
		return getAppliesToServiceVersion(theSvcVer) != null;
	}

	public boolean appliesTo(GDomain theDomain) {
		return getAppliesToDomain(theDomain) != null;
	}

	public boolean appliesTo(GService theService) {
		return getAppliesToService(theService) != null;
	}

	public void applyTo(GDomain theDomain, boolean theValue) {
		GMonitorRuleAppliesTo existing = getAppliesToDomain(theDomain);
		if (existing != null && !theValue) {
			myAppliesTo.remove(existing);
		} else if (existing == null && theValue) {
			GMonitorRuleAppliesTo appliesTo = new GMonitorRuleAppliesTo();
			appliesTo.setDomainPid(theDomain.getPid());
			appliesTo.setDomainName(theDomain.getName());
			myAppliesTo.add(appliesTo);
		}
	}

	public void applyTo(GDomain theDomain, GService theService, BaseGServiceVersion theSvcVer, boolean theValue) {
		GMonitorRuleAppliesTo existing = getAppliesToServiceVersion(theSvcVer);
		if (existing != null && !theValue) {
			myAppliesTo.remove(existing);
		} else if (existing == null && theValue) {
			GMonitorRuleAppliesTo appliesTo = new GMonitorRuleAppliesTo();
			appliesTo.setDomainPid(theDomain.getPid());
			appliesTo.setDomainName(theDomain.getName());
			appliesTo.setServicePid(theService.getPid());
			appliesTo.setServiceName(theService.getName());
			appliesTo.setServiceVersionPid(theSvcVer.getPid());
			appliesTo.setVersionId(theSvcVer.getId());
			myAppliesTo.add(appliesTo);
		}
	}

	public void applyTo(GDomain theDomain, GService theService, boolean theValue) {
		GMonitorRuleAppliesTo existing = getAppliesToService(theService);
		if (existing != null && !theValue) {
			myAppliesTo.remove(existing);
		} else if (existing == null && theValue) {
			GMonitorRuleAppliesTo appliesTo = new GMonitorRuleAppliesTo();
			appliesTo.setDomainPid(theDomain.getPid());
			appliesTo.setDomainName(theDomain.getName());
			appliesTo.setServicePid(theService.getPid());
			appliesTo.setServiceName(theService.getName());
			myAppliesTo.add(appliesTo);
		}
	}

	/**
	 * @return the appliesTo
	 */
	public Set<GMonitorRuleAppliesTo> getAppliesTo() {
		if (myAppliesTo == null) {
			myAppliesTo = new HashSet<GMonitorRuleAppliesTo>();
		}
		return myAppliesTo;
	}

	/**
	 * @return the fireForBackingServiceLatencyIsAboveMillis
	 */
	public Integer getFireForBackingServiceLatencyIsAboveMillis() {
		return myFireForBackingServiceLatencyIsAboveMillis;
	}

	/**
	 * @return the fireForBackingServiceLatencySustainTimeMins
	 */
	public Integer getFireForBackingServiceLatencySustainTimeMins() {
		return myFireForBackingServiceLatencySustainTimeMins;
	}

	public String getName() {
		return myName;
	}

	/**
	 * @return the notifyEmailContacts
	 */
	public Set<String> getNotifyEmailContacts() {
		if (myNotifyEmailContacts == null) {
			myNotifyEmailContacts = new HashSet<String>();
		}
		return myNotifyEmailContacts;
	}

	public boolean isActive() {
		return isActive;
	}

	/**
	 * @return the fireIfAllBackingUrlsAreUnavailable
	 */
	public boolean isFireIfAllBackingUrlsAreUnavailable() {
		return myFireIfAllBackingUrlsAreUnavailable;
	}

	/**
	 * @return the fireIfSingleBackingUrlIsUnavailable
	 */
	public boolean isFireIfSingleBackingUrlIsUnavailable() {
		return myFireIfSingleBackingUrlIsUnavailable;
	}

	@Override
	public void merge(GMonitorRule theObject) {

	}

	public void setActive(boolean theIsActive) {
		isActive = theIsActive;
	}

	/**
	 * @param theFireForBackingServiceLatencyIsAboveMillis
	 *            the fireForBackingServiceLatencyIsAboveMillis to set
	 */
	public void setFireForBackingServiceLatencyIsAboveMillis(Integer theFireForBackingServiceLatencyIsAboveMillis) {
		myFireForBackingServiceLatencyIsAboveMillis = theFireForBackingServiceLatencyIsAboveMillis;
	}

	/**
	 * @param theFireForBackingServiceLatencySustainTimeMins
	 *            the fireForBackingServiceLatencySustainTimeMins to set
	 */
	public void setFireForBackingServiceLatencySustainTimeMins(Integer theFireForBackingServiceLatencySustainTimeMins) {
		myFireForBackingServiceLatencySustainTimeMins = theFireForBackingServiceLatencySustainTimeMins;
	}

	/**
	 * @param theFireIfAllBackingUrlsAreUnavailable
	 *            the fireIfAllBackingUrlsAreUnavailable to set
	 */
	public void setFireIfAllBackingUrlsAreUnavailable(boolean theFireIfAllBackingUrlsAreUnavailable) {
		myFireIfAllBackingUrlsAreUnavailable = theFireIfAllBackingUrlsAreUnavailable;
	}

	/**
	 * @param theFireIfSingleBackingUrlIsUnavailable
	 *            the fireIfSingleBackingUrlIsUnavailable to set
	 */
	public void setFireIfSingleBackingUrlIsUnavailable(boolean theFireIfSingleBackingUrlIsUnavailable) {
		myFireIfSingleBackingUrlIsUnavailable = theFireIfSingleBackingUrlIsUnavailable;
	}

	/**
	 * @param theName
	 *            the name to set
	 */
	public void setName(String theName) {
		myName = theName;
	}

	private GMonitorRuleAppliesTo getAppliesToDomain(GDomain theDomain) {
		GMonitorRuleAppliesTo appliesTo = null;
		for (GMonitorRuleAppliesTo next : getAppliesTo()) {
			if (next.getDomainPid() == theDomain.getPid() && next.getServicePid() == null) {
				appliesTo = next;
				break;
			}
		}
		return appliesTo;
	}

	private GMonitorRuleAppliesTo getAppliesToService(GService theService) {
		GMonitorRuleAppliesTo appliesTo = null;
		for (GMonitorRuleAppliesTo next : getAppliesTo()) {
			if (next.getServicePid() == theService.getPid() && next.getServiceVersionPid() == null) {
				appliesTo = next;
				break;
			}
		}
		return appliesTo;
	}

	private GMonitorRuleAppliesTo getAppliesToServiceVersion(BaseGServiceVersion theSvcVer) {
		GMonitorRuleAppliesTo appliesTo = null;
		for (GMonitorRuleAppliesTo next : getAppliesTo()) {
			if (next.getServiceVersionPid() == theSvcVer.getPid()) {
				appliesTo = next;
				break;
			}
		}
		return appliesTo;
	}

}
