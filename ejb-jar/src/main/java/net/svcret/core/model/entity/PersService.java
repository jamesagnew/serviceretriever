package net.svcret.core.model.entity;

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
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import net.svcret.admin.api.UnexpectedFailureException;
import net.svcret.admin.shared.enm.ResponseTypeEnum;
import net.svcret.admin.shared.model.BaseDtoServiceCatalogItem;
import net.svcret.admin.shared.model.BaseDtoServiceVersion;
import net.svcret.admin.shared.model.GService;
import net.svcret.admin.shared.model.ServerSecuredEnum;
import net.svcret.admin.shared.model.StatusEnum;
import net.svcret.core.api.IRuntimeStatusQueryLocal;
import net.svcret.core.api.StatusesBean;
import net.svcret.core.status.RuntimeStatusQueryBean.StatsAccumulator;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.ForeignKey;

import com.google.common.base.Objects;

@Table(name = "PX_SVC", uniqueConstraints = { @UniqueConstraint(name = "PX_SVC_CONS_DOMSVC", columnNames = { "DOMAIN_PID", "SERVICE_ID" }) })
@Entity
@NamedQueries(value = { @NamedQuery(name = Queries.SERVICE_FIND, query = Queries.SERVICE_FIND_Q) })
public class PersService extends BasePersServiceCatalogItem {

	private static final long serialVersionUID = 1L;

	@Column(name = "SVC_ACTIVE")
	private boolean myActive;

	@ManyToOne()
	@JoinColumn(name = "DOMAIN_PID", referencedColumnName = "PID", nullable = false)
	@ForeignKey(name = "PX_SVC_DOMAIN_PID")
	private PersDomain myDomain;

	@Transient
	private transient HashMap<String, BasePersServiceVersion> myIdToVersion;

	@OneToMany(fetch = FetchType.LAZY, cascade = {}, orphanRemoval = true, mappedBy = "myService")
	private Collection<PersMonitorAppliesTo> myMonitorRules;

	@Version()
	@Column(name = "OPTLOCK")
	private int myOptLock;

	@Lob
	@Column(name = "SVC_DESC", nullable = true)
	private String myDescription;

	public String getDescription() {
		return myDescription;
	}

	public void setDescription(String theDescription) {
		myDescription = theDescription;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "PID")
	private Long myPid;

	@Transient
	private transient ServerSecuredEnum myServerSecured;

	@Column(name = "SERVICE_ID", nullable = false, length = 100)
	private String myServiceId;

	@Column(name = "SERVICE_NAME", nullable = false, length = 200)
	private String myServiceName = "";

	@OneToMany(fetch = FetchType.LAZY, cascade = {}, orphanRemoval = true, mappedBy = "myService")
	private Collection<PersUserServicePermission> myUserPermissions;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true, mappedBy = "myService")
	private Collection<BasePersServiceVersion> myVersions;

	public PersService() {
		super();
	}

	public PersService(long thePid, PersDomain theDomain, String theServiceId, String theServiceName) {
		setPid(thePid);
		setServiceId(theServiceId);
		setServiceName(theServiceName);
		setDomain(theDomain);
	}

	public void addVersion(BasePersServiceVersion theVersion) {
		getVersions();
		if (!myVersions.contains(theVersion)) {
			myVersions.add(theVersion);
		}
	}

	@Override
	public boolean canInheritKeepNumRecentTransactions() {
		return true;
	}

	@Override
	public boolean canInheritObscureElements() {
		return true;
	}

	@Override
	public boolean determineInheritedAuditLogEnable() {
		if (getAuditLogEnable() != null) {
			return getAuditLogEnable();
		}
		return getDomain().determineInheritedAuditLogEnable();
	}

	@Override
	public Integer determineInheritedKeepNumRecentTransactions(ResponseTypeEnum theResultType) {
		return myDomain.determineKeepNumRecentTransactions(theResultType);
	}

	@Override
	public Set<String> determineInheritedObscureRequestElements() {
		return myDomain.determineObscureRequestElements();
	}

	@Override
	public Set<String> determineInheritedObscureResponseElements() {
		return myDomain.determineObscureResponseElements();
	}

	@Override
	public Integer determineKeepNumRecentTransactions(ResponseTypeEnum theResultType) {
		Integer retVal = super.determineKeepNumRecentTransactions(theResultType);
		if (retVal == null) {
			retVal = myDomain.determineKeepNumRecentTransactions(theResultType);
		}
		return retVal;
	}

	@Override
	public Set<String> determineObscureRequestElements() {
		Set<String> retVal = getObscureRequestElementsInLog();
		if (retVal.isEmpty()) {
			retVal = myDomain.determineObscureRequestElements();
		}
		return retVal;
	}

	@Override
	public Set<String> determineObscureResponseElements() {
		Set<String> retVal = getObscureResponseElementsInLog();
		if (retVal.isEmpty()) {
			retVal = myDomain.determineObscureResponseElements();
		}
		return retVal;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object theObj) {
		if (!(theObj instanceof PersService)) {
			return false;
		}

		PersService obj = (PersService) theObj;
		if (myPid == null && obj.getPid() != null) {
			return false;
		}
		if (obj.getPid() == null && myPid != null) {
			return false;
		}
		if (myPid == null) {
			return Objects.equal(myServiceId, obj.myServiceId);
		}

		return Objects.equal(myPid, obj.myPid);
	}

	public Collection<PersMethod> getAllServiceVersionMethods() {
		List<PersMethod> retVal = new ArrayList<PersMethod>();
		for (BasePersServiceVersion nextServicVersion : getVersions()) {
			retVal.addAll(nextServicVersion.getMethods());
		}
		return Collections.unmodifiableList(retVal);
	}

	@Override
	public Set<BasePersServiceVersion> getAllServiceVersions() {
		Set<BasePersServiceVersion> retVal = new HashSet<BasePersServiceVersion>();
		for (BasePersServiceVersion next : getVersions()) {
			retVal.addAll(next.getAllServiceVersions());
		}
		return retVal;
	}

	/**
	 * @return the persDomain
	 */
	public PersDomain getDomain() {
		return myDomain;
	}

	public Collection<PersMonitorAppliesTo> getMonitorRules() {
		if (myMonitorRules == null) {
			myMonitorRules = new ArrayList<PersMonitorAppliesTo>();
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
			for (BasePersServiceVersion next : getVersions()) {
				myServerSecured = ServerSecuredEnum.merge(myServerSecured, next.getServerSecured());
			}
		}
		return myServerSecured;
	}

	/**
	 * @return the serviceId
	 */
	public String getServiceId() {
		return myServiceId;
	}

	/**
	 * @return the serviceName
	 */
	public String getServiceName() {
		return myServiceName;
	}

	public String getServiceNameOrId() {
		if (StringUtils.isNotBlank(getServiceName())) {
			return getServiceName();
		}
		return getServiceId();
	}

	/**
	 * @return the versions
	 */
	public Collection<BasePersServiceVersion> getVersions() {
		if (myVersions == null) {
			myVersions = new ArrayList<BasePersServiceVersion>();
		}
		return Collections.unmodifiableCollection(myVersions);
	}

	public BasePersServiceVersion getVersionWithId(String theId) {
		for (BasePersServiceVersion next : getVersions()) {
			if (next.getVersionId().equals(theId)) {
				return next;
			}
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		if (myPid == null) {
			return 0;
		}
		return Objects.hashCode(myPid);
	}

	/**
	 * @return the active
	 */
	public boolean isActive() {
		return myActive;
	}

	public void loadAllAssociations() {
		myIdToVersion = new HashMap<String, BasePersServiceVersion>();
		for (BasePersServiceVersion next : getVersions()) {
			myIdToVersion.put(next.getVersionId(), next);
			next.loadAllAssociations();
		}

		for (PersMonitorAppliesTo next : getMonitorRules()) {
			next.loadAllAssociations();
		}

	}

	public void merge(PersService theService) {
		super.merge(theService);
		setServiceId(theService.getServiceId());
		setServiceName(theService.getServiceName());
		setDescription(theService.getDescription());
		setActive(theService.isActive());
	}

	public void populateDtoWithMonitorRules(BaseDtoServiceCatalogItem theDto) {
		for (BasePersServiceVersion nextSvcVer : getVersions()) {
			nextSvcVer.populateDtoWithMonitorRules(theDto);
		}
		for (PersMonitorAppliesTo nextRule : getMonitorRules()) {
			if (nextRule.getItem().equals(this)) {
				theDto.getMonitorRulePids().add(nextRule.getPid());
			}
		}
	}

	public StatusEnum populateDtoWithStatusAndProvideStatusForParent(BaseDtoServiceCatalogItem theDashboardObject, StatusEnum theInitialStatus, StatusesBean theStatuses) {

		// Value will be changed below
		StatusEnum status = theInitialStatus;

		for (BasePersServiceVersion nextVersion : getVersions()) {
			status = nextVersion.populateDtoWithStatusAndProvideStatusForParent(theDashboardObject, theStatuses, status);
		} // end VERSION
		
		theDashboardObject.setStatus(status);
		
		return status;
	}

	public void removeVersion(BasePersServiceVersion theVersion) {
		getVersions();
		myVersions.remove(theVersion);
	}

	/**
	 * @param theActive
	 *            the active to set
	 */
	public void setActive(boolean theActive) {
		myActive = theActive;
	}

	/**
	 * @param thePersDomain
	 *            the persDomain to set
	 */
	public void setDomain(PersDomain thePersDomain) {
		if (!thePersDomain.getServices().contains(this)) {
			thePersDomain.addService(this);
		}
		myDomain = thePersDomain;
	}

	/**
	 * @param theOptLock
	 *            the versionNum to set
	 */
	public void setOptLock(int theOptLock) {
		myOptLock = theOptLock;
	}

	/**
	 * @param thePid
	 *            the id to set
	 */
	public void setPid(Long thePid) {
		myPid = thePid;
	}

	/**
	 * @param theServiceId
	 *            the serviceId to set
	 */
	public void setServiceId(String theServiceId) {
		validateId(theServiceId);
		myServiceId = theServiceId;
	}

	/**
	 * @param theServiceName
	 *            the serviceName to set
	 */
	public void setServiceName(String theServiceName) {
		myServiceName = theServiceName;
	}
	
	public GService toDto() throws UnexpectedFailureException {
		Set<Long> stats = Collections.emptySet();
		return toDto(stats, stats, stats, stats, null, null);
	}

	public GService toDto(Set<Long> theLoadSvcStats, Set<Long> theLoadVerStats, Set<Long> theLoadVerMethodStats, Set<Long> theLoadUrlStats, StatusesBean theStatuses, IRuntimeStatusQueryLocal theRuntimeStatusQuerySvc) throws UnexpectedFailureException {
		GService retVal = new GService();
		retVal.setPid(this.getPid());
		retVal.setId(this.getServiceId());
		retVal.setName(this.getServiceName());
		retVal.setActive(this.isActive());
		retVal.setServerSecured(this.getServerSecured());
		retVal.setDescription(this.getDescription());

		this.populateKeepRecentTransactionsToDto(retVal);
		this.populateServiceCatalogItemToDto(retVal);
		this.populateDtoWithMonitorRules(retVal);

		for (BasePersServiceVersion nextVersion : getVersions()) {
			BaseDtoServiceVersion gVersion = nextVersion.toDto(theLoadVerStats, theRuntimeStatusQuerySvc, theStatuses, theLoadVerMethodStats, theLoadUrlStats);
			retVal.getVersionList().add(gVersion);
		} // for service versions

		if (theLoadSvcStats.contains(getPid())) {
			retVal.setStatsInitialized(new Date());
			StatusEnum status = StatusEnum.UNKNOWN;

			StatsAccumulator accumulator = null;
			status = this.populateDtoWithStatusAndProvideStatusForParent(retVal, status, theStatuses);

			accumulator = theRuntimeStatusQuerySvc.extract60MinuteStats(this);
			accumulator.populateDto(retVal);

			int urlsActive = 0;
			int urlsDown = 0;
			int urlsUnknown = 0;
			Date lastServerSecurityFail = null;
			Date lastSuccess = null;
			for (BasePersServiceVersion nextVersion : this.getVersions()) {
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

			retVal.setUrlsActive(urlsActive);
			retVal.setUrlsDown(urlsDown);
			retVal.setUrlsUnknown(urlsUnknown);
			retVal.setLastServerSecurityFailure(lastServerSecurityFail);
			retVal.setLastSuccessfulInvocation(lastSuccess);

		}

		return retVal;
	}

	public static PersService fromDto(GService theService) {
		PersService retVal = new PersService();
		retVal.setServiceId(theService.getId());
		retVal.setServiceName(theService.getName());
		retVal.setDescription(theService.getDescription());
		retVal.setActive(theService.isActive());
		return retVal;
	}

	

}
