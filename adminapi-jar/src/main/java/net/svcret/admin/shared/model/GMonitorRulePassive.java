package net.svcret.admin.shared.model;

import java.util.HashSet;
import java.util.Set;

import net.svcret.admin.shared.enm.MonitorRuleTypeEnum;


public class GMonitorRulePassive extends BaseDtoMonitorRule {

	private static final long serialVersionUID = 1L;

	private Set<GMonitorRuleAppliesTo> myAppliesTo;
	private Integer myPassiveFireForBackingServiceLatencyIsAboveMillis;
	private Integer myPassiveFireForBackingServiceLatencySustainTimeMins;
	private boolean myPassiveFireIfAllBackingUrlsAreUnavailable;
	private boolean myPassiveFireIfSingleBackingUrlIsUnavailable;

	public boolean appliesTo(BaseDtoServiceVersion theSvcVer) {
		return getAppliesToServiceVersion(theSvcVer) != null;
	}

	public boolean appliesTo(DtoDomain theDomain) {
		return getAppliesToDomain(theDomain) != null;
	}

	public boolean appliesTo(GService theService) {
		return getAppliesToService(theService) != null;
	}

	public void applyTo(DtoDomain theDomain, boolean theValue) {
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

	public void applyTo(DtoDomain theDomain, GService theService, BaseDtoServiceVersion theSvcVer, boolean theValue) {
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

	public void applyTo(DtoDomain theDomain, GService theService, boolean theValue) {
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
	public Integer getPassiveFireForBackingServiceLatencyIsAboveMillis() {
		return myPassiveFireForBackingServiceLatencyIsAboveMillis;
	}

	/**
	 * @return the fireForBackingServiceLatencySustainTimeMins
	 */
	public Integer getPassiveFireForBackingServiceLatencySustainTimeMins() {
		return myPassiveFireForBackingServiceLatencySustainTimeMins;
	}

	@Override
	public MonitorRuleTypeEnum getRuleType() {
		return MonitorRuleTypeEnum.PASSIVE;
	}

	/**
	 * @return the fireIfAllBackingUrlsAreUnavailable
	 */
	public boolean isPassiveFireIfAllBackingUrlsAreUnavailable() {
		return myPassiveFireIfAllBackingUrlsAreUnavailable;
	}

	/**
	 * @return the fireIfSingleBackingUrlIsUnavailable
	 */
	public boolean isPassiveFireIfSingleBackingUrlIsUnavailable() {
		return myPassiveFireIfSingleBackingUrlIsUnavailable;
	}

	/**
	 * @param thePassiveFireForBackingServiceLatencyIsAboveMillis
	 *            the fireForBackingServiceLatencyIsAboveMillis to set
	 */
	public void setPassiveFireForBackingServiceLatencyIsAboveMillis(Integer thePassiveFireForBackingServiceLatencyIsAboveMillis) {
		myPassiveFireForBackingServiceLatencyIsAboveMillis = thePassiveFireForBackingServiceLatencyIsAboveMillis;
	}

	/**
	 * @param thePassiveFireForBackingServiceLatencySustainTimeMins
	 *            the fireForBackingServiceLatencySustainTimeMins to set
	 */
	public void setPassiveFireForBackingServiceLatencySustainTimeMins(Integer thePassiveFireForBackingServiceLatencySustainTimeMins) {
		myPassiveFireForBackingServiceLatencySustainTimeMins = thePassiveFireForBackingServiceLatencySustainTimeMins;
	}

	/**
	 * @param thePassiveFireIfAllBackingUrlsAreUnavailable
	 *            the fireIfAllBackingUrlsAreUnavailable to set
	 */
	public void setPassiveFireIfAllBackingUrlsAreUnavailable(boolean thePassiveFireIfAllBackingUrlsAreUnavailable) {
		myPassiveFireIfAllBackingUrlsAreUnavailable = thePassiveFireIfAllBackingUrlsAreUnavailable;
	}

	/**
	 * @param thePassiveFireIfSingleBackingUrlIsUnavailable
	 *            the fireIfSingleBackingUrlIsUnavailable to set
	 */
	public void setPassiveFireIfSingleBackingUrlIsUnavailable(boolean thePassiveFireIfSingleBackingUrlIsUnavailable) {
		myPassiveFireIfSingleBackingUrlIsUnavailable = thePassiveFireIfSingleBackingUrlIsUnavailable;
	}

	private GMonitorRuleAppliesTo getAppliesToDomain(DtoDomain theDomain) {
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
			if (next.getServicePid() != null && next.getServicePid() == theService.getPid() && next.getServiceVersionPid() == null) {
				appliesTo = next;
				break;
			}
		}
		return appliesTo;
	}

	private GMonitorRuleAppliesTo getAppliesToServiceVersion(BaseDtoServiceVersion theSvcVer) {
		GMonitorRuleAppliesTo appliesTo = null;
		for (GMonitorRuleAppliesTo next : getAppliesTo()) {
			if (next.getServiceVersionPid() != null && next.getServiceVersionPid() == theSvcVer.getPid()) {
				appliesTo = next;
				break;
			}
		}
		return appliesTo;
	}

}
