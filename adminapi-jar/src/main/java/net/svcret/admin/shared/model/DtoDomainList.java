package net.svcret.admin.shared.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import net.svcret.admin.shared.util.BaseGDashboardObjectComparator;

@XmlAccessorType(XmlAccessType.NONE)
public class DtoDomainList extends BaseDtoList<DtoDomain> {

	private static final long serialVersionUID = 1L;

	private transient Map<Long, BaseDtoServiceVersion> myPidToServiceVersion;
	private transient Map<Long, GServiceVersionUrl> myPidToUrl;

	@Override
	public String toString() {
		return super.getListForJaxb().toString();
	}

	public DtoDomainList() {
		setComparator(new BaseGDashboardObjectComparator());
	}

	@XmlElement(name="Domain")
	@Override
	public List<DtoDomain> getListForJaxb() {
		return super.getListForJaxb();
	}

	
	public DtoDomain getDomainByPid(long theDomainPid) {
		for (DtoDomain next : this) {
			if (next.getPid() == theDomainPid) {
				return next;
			}
		}
		return null;
	}

	public BaseDtoServiceVersion getServiceVersionByPid(long thePid) {
		if (myPidToServiceVersion == null) {
			myPidToServiceVersion = new HashMap<Long, BaseDtoServiceVersion>();
		}
		if (myPidToServiceVersion.containsKey(thePid)) {
			return myPidToServiceVersion.get(thePid);
		}

		for (DtoDomain nextDomain : this) {
			for (GService nextSvc : nextDomain.getServiceList()) {
				BaseDtoServiceVersion ver = nextSvc.getVersionList().getVersionByPid(thePid);
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

		for (DtoDomain nextDomain : this) {
			for (GService nextSvc : nextDomain.getServiceList()) {
				for (BaseDtoServiceVersion nextVer : nextSvc.getVersionList()) {
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
		for (DtoDomain nextDomain : this) {
			for (GService nextSvc : nextDomain.getServiceList()) {
				for (BaseDtoServiceVersion nextVer : nextSvc.getVersionList()) {
					if (nextVer.getPid() == theServiceVersionPid) {
						return nextDomain.getPid();
					}
				}
			}
		}
		return null;
	}

	public Long getServicePidWithServiceVersion(long theServiceVersionPid) {
		for (DtoDomain nextDomain : this) {
			for (GService nextSvc : nextDomain.getServiceList()) {
				for (BaseDtoServiceVersion nextVer : nextSvc.getVersionList()) {
					if (nextVer.getPid() == theServiceVersionPid) {
						return nextSvc.getPid();
					}
				}
			}
		}
		return null;
	}

	public GService getServiceWithServiceVersion(long theServiceVersionPid) {
		for (DtoDomain nextDomain : this) {
			for (GService nextSvc : nextDomain.getServiceList()) {
				for (BaseDtoServiceVersion nextVer : nextSvc.getVersionList()) {
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
		for (DtoDomain nextDoman : this) {
			for (GService nextSvc : nextDoman.getServiceList()) {
				for (BaseDtoServiceVersion nextVer : nextSvc.getVersionList()) {
					retVal.add(nextVer.getPid());
				}
			}
		}
		return retVal;
	}

	public Long getDomainPidWithService(Long theServicePid) {
		for (DtoDomain nextDomain : this) {
			for (GService nextSvc : nextDomain.getServiceList()) {
				if (nextSvc.getPid() == theServicePid) {
					return nextDomain.getPid();
				}
			}
		}
		return null;
	}

	public BaseDtoServiceVersion getFirstServiceVersion() {
		if (size() > 0) {
			return get(0).getServiceList().getFirstServiceVersion();
		}
		return null;
	}

	public DtoDomain getDomainWithServiceVersion(long theServiceVersionPid) {
		for (DtoDomain nextDomain : this) {
			for (GService nextSvc : nextDomain.getServiceList()) {
				for (BaseDtoServiceVersion nextVer : nextSvc.getVersionList()) {
					if (nextVer.getPid() == theServiceVersionPid) {
						return nextDomain;
					}
				}
			}
		}
		return null;
	}

}
