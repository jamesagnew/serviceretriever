package net.svcret.admin.shared.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GDomainList extends BaseGList<GDomain> {

	private static final long serialVersionUID = 1L;

	private transient Map<Long, BaseGServiceVersion> myPidToServiceVersion;
	private transient Map<Long, GServiceVersionUrl> myPidToUrl;

	// public GDomain getDomainByPid(String theDomainId) {
	// if (StringUtil.isBlank(theDomainId)) {
	// return null;
	// }
	//
	// long pid = -1;
	// try {
	// pid = Long.parseLong(theDomainId);
	// } catch (Exception e) {
	// GWT.log("Failed to parse value: " + theDomainId, e);
	// }
	// return getDomainByPid(pid);
	// }

	// public void merge(GDomain theResult) {
	// GDomain original = getDomainByPid(theResult.getPid() + "");
	// original.merge(theResult);
	// }

	public GDomainList() {
		setComparator(new BaseGDashboardObjectComparator());
	}

	public GDomain getDomainByPid(long theDomainPid) {
		for (GDomain next : this) {
			if (next.getPid() == theDomainPid) {
				return next;
			}
		}
		return null;
	}

	public BaseGServiceVersion getServiceVersionByPid(long thePid) {
		if (myPidToServiceVersion == null) {
			myPidToServiceVersion = new HashMap<Long, BaseGServiceVersion>();
		}
		if (myPidToServiceVersion.containsKey(thePid)) {
			return myPidToServiceVersion.get(thePid);
		}

		for (GDomain nextDomain : this) {
			for (GService nextSvc : nextDomain.getServiceList()) {
				BaseGServiceVersion ver = nextSvc.getVersionList().getVersionByPid(thePid);
				if (ver != null) {
					myPidToServiceVersion.put(thePid, ver);
					return ver;
				}
			}
		}
		return null;
	}

	public GServiceVersionUrl getUrlByPid(Long thePid) {
		if (myPidToUrl == null) {
			myPidToUrl = new HashMap<Long, GServiceVersionUrl>();
		}
		if (myPidToUrl.containsKey(thePid)) {
			return myPidToUrl.get(thePid);
		}

		for (GDomain nextDomain : this) {
			for (GService nextSvc : nextDomain.getServiceList()) {
				for (BaseGServiceVersion nextVer : nextSvc.getVersionList()) {
					GServiceVersionUrl url = nextVer.getUrlList().getUrlWithPid(thePid);
					if (url != null) {
						myPidToUrl.put(thePid, url);
						return url;
					}
				}
			}
		}
		return null;
	}

	public Long getDomainPidWithServiceVersion(long theServiceVersionPid) {
		for (GDomain nextDomain : this) {
			for (GService nextSvc : nextDomain.getServiceList()) {
				for (BaseGServiceVersion nextVer : nextSvc.getVersionList()) {
					if (nextVer.getPid() == theServiceVersionPid) {
						return nextDomain.getPid();
					}
				}
			}
		}
		return null;
	}

	public Long getServicePidWithServiceVersion(long theServiceVersionPid) {
		for (GDomain nextDomain : this) {
			for (GService nextSvc : nextDomain.getServiceList()) {
				for (BaseGServiceVersion nextVer : nextSvc.getVersionList()) {
					if (nextVer.getPid() == theServiceVersionPid) {
						return nextSvc.getPid();
					}
				}
			}
		}
		return null;
	}

	public GService getServiceWithServiceVersion(long theServiceVersionPid) {
		for (GDomain nextDomain : this) {
			for (GService nextSvc : nextDomain.getServiceList()) {
				for (BaseGServiceVersion nextVer : nextSvc.getVersionList()) {
					if (nextVer.getPid() == theServiceVersionPid) {
						return nextSvc;
					}
				}
			}
		}
		return null;
	}

	public Collection<Long> getAllServiceVersionPids() {
		Set<Long> retVal =new HashSet<Long>();
		for (GDomain nextDoman : this) {
			for (GService nextSvc : nextDoman.getServiceList()) {
				for (BaseGServiceVersion nextVer : nextSvc.getVersionList()) {
					retVal.add(nextVer.getPid());
				}
			}
		}
		return retVal;
	}

}
