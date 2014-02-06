package net.svcret.ejb.model.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;

import net.svcret.admin.api.UnexpectedFailureException;
import net.svcret.admin.shared.enm.ResponseTypeEnum;
import net.svcret.admin.shared.model.BaseDtoServiceCatalogItem;
import net.svcret.admin.shared.model.DtoDomain;
import net.svcret.admin.shared.model.GService;
import net.svcret.admin.shared.model.ServerSecuredEnum;
import net.svcret.admin.shared.model.StatusEnum;
import net.svcret.ejb.api.IRuntimeStatusQueryLocal;
import net.svcret.ejb.api.StatusesBean;
import net.svcret.ejb.ejb.RuntimeStatusQueryBean.StatsAccumulator;

import org.apache.commons.lang3.StringUtils;

@Table(name = "PX_DOMAIN")
@Entity()
public class PersDomain extends BasePersServiceCatalogItem {

	private static final long serialVersionUID = 1L;

	@Transient
	private transient boolean myAllAssociationsLoaded;

	@Column(name = "DOMAIN_ID", length = 100, nullable = false)
	private String myDomainId;

	@Column(name = "DOMAIN_NAME", length = 200, nullable = true)
	private String myDomainName;

	@Lob
	@Column(name = "DOMAIN_DESC", nullable = true)
	private String myDescription;
	
	@Transient
	private volatile transient HashMap<String, PersService> myIdToServices;

	@OneToMany(fetch=FetchType.LAZY, cascade= {}, orphanRemoval=true, mappedBy="myDomain")
	private Collection<PersMonitorAppliesTo> myMonitorRules;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "PID")
	private Long myPid;

	@Transient
	private transient ServerSecuredEnum myServerSecured;

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy="myDomain")
	private Collection<PersService> myServices;
	
	@Transient
	private transient StatusEnum myStatus;

	@OneToMany(fetch=FetchType.LAZY, cascade= {}, orphanRemoval=true, mappedBy="myDomain")
	private Collection<PersUserDomainPermission> myUserPermissions;

	@Version()
	@Column(name = "OPTLOCK")
	protected int myOptLock;

	public PersDomain() {
		super();
	}

	public PersDomain(long thePid, String theDomainId) {
		myPid = thePid;
		myDomainId = theDomainId;
	}

	public void addService(PersService thePersService) {
		getServices();
		myServices.add(thePersService);
		myIdToServices = null;
	}

	@Override
	public boolean canInheritKeepNumRecentTransactions() {
		return false;
	}

	@Override
	public boolean canInheritObscureElements() {
		return false;
	}

	@Override
	public boolean determineInheritedAuditLogEnable() {
		if (getAuditLogEnable() != null) {
			return getAuditLogEnable();
		}
		return false;
	}

	@Override
	public Integer determineInheritedKeepNumRecentTransactions(ResponseTypeEnum theResultType) {
		return null;
	}

	@Override
	public Set<String> determineInheritedObscureRequestElements() {
		return null;
	}

	@Override
	public Set<String> determineInheritedObscureResponseElements() {
		return null;
	}


	@Override
	public Set<String> determineObscureRequestElements() {
		Set<String> retVal = getObscureRequestElementsInLog();
		return retVal;
	}

	@Override
	public Set<String> determineObscureResponseElements() {
		Set<String> retVal = getObscureResponseElementsInLog();
		return retVal;
	}

	public Collection<PersMethod> getAllServiceVersionMethods() {
		List<PersMethod> retVal = new ArrayList<PersMethod>();
		for (PersService nextService : getServices()) {
			retVal.addAll(nextService.getAllServiceVersionMethods());
		}
		return retVal;
	}


	@Override
	public Set<BasePersServiceVersion> getAllServiceVersions() {
		Set<BasePersServiceVersion> retVal = new HashSet<BasePersServiceVersion>();
		for (PersService next : getServices()) {
			for (BasePersServiceVersion nextVer : next.getAllServiceVersions()) {
				retVal.add(nextVer);
			}
		}
		return retVal;
	}

	/**
	 * @return the domainId
	 */
	public String getDomainId() {
		return myDomainId;
	}

	/**
	 * @return the domainName
	 */
	public String getDomainName() {
		return myDomainName;
	}

	public String getDomainNameOrId() {
		if (StringUtils.isNotBlank(getDomainName())) {
			return getDomainName();
		}
		return getDomainId();
	}

	public Collection<PersMonitorAppliesTo> getMonitorRules() {
		if (myMonitorRules==null) {
			myMonitorRules=new ArrayList<PersMonitorAppliesTo>();
		}
		return myMonitorRules;
	}

	/**
	 * @return the versionNum
	 */
	public int getOptLock() {
		return myOptLock;
	}

	/**
	 * @return the id
	 */
	public Long getPid() {
		return myPid;
	}

	public ServerSecuredEnum getServerSecured() {
		if (myServerSecured == null) {
			for (PersService next : getServices()) {
				myServerSecured = ServerSecuredEnum.merge(myServerSecured, next.getServerSecured());
			}
		}
		return myServerSecured;
	}

	/**
	 * @return the services
	 */
	public Collection<PersService> getServices() {
		if (myServices == null) {
			myServices = new ArrayList<PersService>();
		}
		return myServices;
	}

	public PersService getServiceWithId(String theId) {
		if (myIdToServices == null) {
			return initIdToServices().get(theId);
		}
		return myIdToServices.get(theId);
	}

	public StatusEnum getStatus() {
		return myStatus;
	}

	public void loadAllAssociations() {
		if (myAllAssociationsLoaded) {
			return;
		}

		initIdToServices();
		
		for (PersMonitorAppliesTo next : getMonitorRules()) {
			next.loadAllAssociations();
		}

		myAllAssociationsLoaded = true;
	}

	public void merge(BasePersObject theObj) {
		super.merge(theObj);
	
		PersDomain domain = (PersDomain)theObj;
		setDomainId(domain.getDomainId());
		setDomainName(domain.getDomainName());
		setDescription(domain.getDescription());
	}

	public void populateDtoWithMonitorRules(BaseDtoServiceCatalogItem theDto) {
		for (PersService nextSvc : getServices()) {
			nextSvc.populateDtoWithMonitorRules(theDto);
		}
		for (PersMonitorAppliesTo nextRule : getMonitorRules()) {
			if (nextRule.getItem().equals(this)) {
				theDto.getMonitorRulePids().add(nextRule.getPid());
			}
		}
	}

	/**
	 * @param theDomainId
	 *            the domainId to set
	 */
	public void setDomainId(String theDomainId) {
		validateId(theDomainId);
		myDomainId = theDomainId;
	}

	/**
	 * @param theDomainName
	 *            the domainName to set
	 */
	public void setDomainName(String theDomainName) {
		myDomainName = theDomainName;
	}

	/**
	 * @param theOptLock
	 *            the versionNum to set
	 */
	public void setOptLock(int theOptLock) {
		myOptLock = theOptLock;
	}

	/**
	 * @param theId
	 *            the id to set
	 */
	public void setPid(Long thePid) {
		myPid = thePid;
	}

	public DtoDomain toDto() throws UnexpectedFailureException {
		Set<Long> stats = Collections.emptySet();
		return toDto(stats, stats, stats, stats, stats, null, null);
	}

	public DtoDomain toDto(Set<Long> theLoadDomStats, Set<Long> theLoadSvcStats, Set<Long> theLoadVerStats, Set<Long> theLoadVerMethodStats, Set<Long> theLoadUrlStats, StatusesBean theStatuses, IRuntimeStatusQueryLocal theStatusQuerySvc) throws UnexpectedFailureException {
		DtoDomain retVal = new DtoDomain();
		retVal.setPid(this.getPid());
		retVal.setId(this.getDomainId());
		retVal.setName(this.getDomainName());
		retVal.setServerSecured(this.getServerSecured());
		retVal.setDescription(this.getDescription());
		
		this.populateDtoWithMonitorRules(retVal);
		this.populateKeepRecentTransactionsToDto(retVal);
		this.populateServiceCatalogItemToDto(retVal);

		for (PersService nextService : getServices()) {
			GService gService = nextService.toDto(theLoadSvcStats, theLoadVerStats, theLoadVerMethodStats, theLoadUrlStats, theStatuses, theStatusQuerySvc);
			retVal.getServiceList().add(gService);
		} // for services
		
		if (theLoadDomStats.contains(getPid())) {
			retVal.setStatsInitialized(new Date());
			StatusEnum status = StatusEnum.UNKNOWN;

			StatsAccumulator accumulator = null;
			for (PersService nextService : this.getServices()) {
				status = nextService.populateDtoWithStatusAndProvideStatusForParent(retVal, status, theStatuses);
			}
			
			accumulator = theStatusQuerySvc.extract60MinuteStats(this);
			accumulator.populateDto(retVal);

			int urlsActive = 0;
			int urlsDown = 0;
			int urlsUnknown = 0;
			Date lastServerSecurityFail = null;
			Date lastSuccess = null;
			for (PersService nextService : this.getServices()) {
				for (BasePersServiceVersion nextVersion : nextService.getVersions()) {
					for (PersServiceVersionUrl nextUrl : nextVersion.getUrls()) {
						StatusEnum urlStatus = theStatuses.getUrlStatusEnum(nextUrl.getPid());
						if (urlStatus == null) {
							continue;
						}
						switch (urlStatus) {
						case ACTIVE:
							urlsActive++;
							break;
						case DOWN:
							urlsDown++;
							break;
						case UNKNOWN:
							urlsUnknown++;
							break;
						}
					}

					PersServiceVersionStatus svcStatus = theStatuses.getServiceVersionStatus(nextVersion.getPid());
					if (svcStatus != null) {
						lastServerSecurityFail = PersServiceVersionStatus.newer(lastServerSecurityFail, svcStatus.getLastServerSecurityFailure());
						lastSuccess = PersServiceVersionStatus.newer(lastSuccess, svcStatus.getLastSuccessfulInvocation());
					}
				}
			}

			retVal.setUrlsActive(urlsActive);
			retVal.setUrlsDown(urlsDown);
			retVal.setUrlsUnknown(urlsUnknown);
			retVal.setLastServerSecurityFailure(lastServerSecurityFail);
			retVal.setLastSuccessfulInvocation(lastSuccess);
		}
		// retVal.get

		return retVal;
	}

	public String getDescription() {
		return myDescription;
	}

	public void setDescription(String theDomainDescription) {
		myDescription = theDomainDescription;
	}

	private HashMap<String, PersService> initIdToServices() {
		HashMap<String, PersService> idToServices = new HashMap<String, PersService>();
		for (PersService next : getServices()) {
			idToServices.put(next.getServiceId(), next);
			next.loadAllAssociations();
		}
		myIdToServices = idToServices;
		return idToServices;
	}

	
}
