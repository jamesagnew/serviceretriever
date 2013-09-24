package net.svcret.admin.shared.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import net.svcret.admin.shared.util.XmlConstants;

@XmlType(namespace=XmlConstants.DTO_NAMESPACE, name="DomainList")
@XmlRootElement(namespace=XmlConstants.DTO_NAMESPACE, name="DomainList")
@XmlAccessorType(XmlAccessType.FIELD)
public class GDomainList extends BaseDtoList<GDomain> {

	private static final long serialVersionUID = 1L;

	private transient Map<Long, BaseGServiceVersion> myPidToServiceVersion;
	private transient Map<Long, GServiceVersionUrl> myPidToUrl;

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
		Set<Long> retVal = new HashSet<Long>();
		for (GDomain nextDoman : this) {
			for (GService nextSvc : nextDoman.getServiceList()) {
				for (BaseGServiceVersion nextVer : nextSvc.getVersionList()) {
					retVal.add(nextVer.getPid());
				}
			}
		}
		return retVal;
	}

	public Long getDomainPidWithService(Long theServicePid) {
		for (GDomain nextDomain : this) {
			for (GService nextSvc : nextDomain.getServiceList()) {
				if (nextSvc.getPid() == theServicePid) {
					return nextDomain.getPid();
				}
			}
		}
		return null;
	}

	public BaseGServiceVersion getFirstServiceVersion() {
		if (size() > 0) {
			return get(0).getServiceList().getFirstServiceVersion();
		}
		return null;
	}

}
