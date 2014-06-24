package net.svcret.core.model.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import net.svcret.admin.api.ProcessingException;
import net.svcret.admin.api.UnexpectedFailureException;
import net.svcret.admin.shared.enm.ResponseTypeEnum;
import net.svcret.admin.shared.enm.ServerSecurityModeEnum;
import net.svcret.admin.shared.model.*;
import net.svcret.admin.shared.model.DtoMethod;
import net.svcret.admin.shared.util.ProxyUtil;
import net.svcret.admin.shared.util.Validate;
import net.svcret.core.admin.AdminServiceBean;
import net.svcret.core.api.IDao;
import net.svcret.core.api.IRuntimeStatusQueryLocal;
import net.svcret.core.api.IServiceRegistry;
import net.svcret.core.api.StatusesBean;
import net.svcret.core.status.RuntimeStatusQueryBean.StatsAccumulator;

import org.apache.commons.lang3.StringUtils;
 
@Entity
@Table(name = "PX_SVC_VER", uniqueConstraints = { @UniqueConstraint(columnNames = { "SERVICE_PID", "VERSION_ID" }) })
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "SVCVER_TYPE", length = 20, discriminatorType = DiscriminatorType.STRING)
public abstract class BasePersServiceVersion extends BasePersServiceCatalogItem {

	private static final long serialVersionUID = 1L;

	static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(BasePersServiceVersion.class);

	@Column(name = "ISACTIVE")
	private boolean myActive;

	@OneToMany(fetch = FetchType.LAZY, cascade = {}, orphanRemoval = true, mappedBy = "myServiceVersion")
	private Collection<PersMonitorRuleActiveCheck> myActiveChecks;

	@Transient
	private transient boolean myAssociationsLoaded;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "myServiceVersion")
	@OrderBy("CAUTH_ORDER")
	private List<PersBaseClientAuth<?>> myClientAuths;

	@Lob()
	@Column(name = "SVC_DESC_EXT", nullable = true)
	private String myDescriptionExtended;

//	@Column(name = "SVC_DESC", length = 2000, nullable = true)
//	private String myDescription;

	@Column(name = "EXPLICIT_PROXY_PATH", length = 400, nullable = true)
	private String myExplicitProxyPath;

	@ManyToOne(cascade = {}, fetch = FetchType.LAZY)
	@JoinColumn(name = "HTTP_CONFIG_PID", referencedColumnName = "PID", nullable = false, foreignKey=@ForeignKey(name = "PX_SVCVER_HTTP_CONFIG_PID"))
	private PersHttpClientConfig myHttpClientConfig;

	@Transient
	private transient Map<String, PersServiceVersionUrl> myIdToUrl;

	@OneToMany(cascade = {}, orphanRemoval = true, mappedBy = "myPk.myServiceVersion")
	private Collection<PersLibraryMessageAppliesTo> myLibraryMessages;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "myServiceVersion")
	@OrderBy("METHOD_ORDER")
	private List<PersMethod> myMethods;

	@OneToMany(fetch = FetchType.LAZY, cascade = {}, orphanRemoval = true, mappedBy = "myServiceVersion")
	private Collection<PersMonitorAppliesTo> myMonitorRules;

	@Transient
	private transient volatile Map<String, PersMethod> myNameToMethod;

	@Version()
	@Column(name = "OPTLOCK")
	private int myOptLock;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "PID")
	private Long myPid;

	@Transient
	private transient Map<Long, PersServiceVersionUrl> myPidToUrl;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "myPk.myServiceVersion")
	@OrderBy("CAPTURE_ORDER")
	private List<PersPropertyCapture> myPropertyCaptures;

	@OneToMany(fetch = FetchType.LAZY, cascade = {}, orphanRemoval = true, mappedBy = "myServiceVersion")
	private List<PersServiceVersionRecentMessage> myRecentMessages;

	@Transient
	private transient volatile HashMap<String, PersMethod> myRootElementNameToMethod;
	
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "myServiceVersion")
	@OrderBy("SAUTH_ORDER")
	private List<PersBaseServerAuth<?, ?>> myServerAuths;

	@Column(name = "SERVER_SEC_MODE", length = 50, nullable = false)
	@Enumerated(EnumType.STRING)
	private ServerSecurityModeEnum myServerSecurityMode;

	@ManyToOne()
	@JoinColumn(name = "SERVICE_PID", referencedColumnName = "PID", foreignKey=@ForeignKey(name = "PX_SVCVER_SERVICE_PID"))
	private PersService myService;

	@OneToOne(cascade = {}, fetch = FetchType.LAZY, mappedBy = "myServiceVersion", orphanRemoval = true)
	private PersServiceVersionStatus myStatus;

	@OneToMany(fetch = FetchType.LAZY, cascade = {}, orphanRemoval = true, mappedBy = "myPk.myServiceVersion")
	private Collection<PersStickySessionUrlBinding> myStickySessionUrlBindings;

	@OneToOne(optional=true, mappedBy="myServiceVersion", orphanRemoval=true, cascade=CascadeType.ALL)
	private PersServiceVersionThrottle myThrottle;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "myServiceVersion", orphanRemoval = true)
	@MapKey(name = "myResourceUrl")
	private Map<String, PersServiceVersionResource> myUriToResource;

	@Transient
	private transient AtomicInteger myUrlCounter = new AtomicInteger();

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "myServiceVersion")
	@OrderBy("URL_ORDER")
	private List<PersServiceVersionUrl> myUrls;

	@Transient
	private transient Map<String, PersServiceVersionUrl> myUrlToUrl;

	@Column(name = "DEFAULT_PROXY_PATH")
	private boolean myUseDefaultProxyPath = true;

	@OneToMany(fetch = FetchType.LAZY, cascade = {}, orphanRemoval = true, mappedBy = "myServiceVersion")
	private Collection<PersUserServiceVersionPermission> myUserPermissions;

	@OneToMany(fetch = FetchType.LAZY, cascade = {}, orphanRemoval = true, mappedBy = "myServiceVersion")
	private Collection<PersUserRecentMessage> myUserRecentMessages;

	@Column(name = "VERSION_ID", length = 200, nullable = false)
	private String myVersionId;

	public BasePersServiceVersion() {
		toString();
	}

	public void addClientAuth(int theIndex, PersBaseClientAuth<?> theAuth) {
		theAuth.setServiceVersion(this);
		getClientAuths();
		myClientAuths.add(theIndex, theAuth);
	}

	public void addClientAuth(PersBaseClientAuth<?> theAuth) {
		theAuth.setServiceVersion(this);
		getClientAuths();
		myClientAuths.add(theAuth);
	}

	public void addMethod(PersMethod method) {
		getMethods();
		myMethods.add(method);

		myNameToMethod = null;
	}

	public PersServiceVersionResource addResource(String theUrl, String theContentType, String theText) {
		PersServiceVersionResource res = new PersServiceVersionResource();
		res.setResourceText(theText);
		res.setResourceUrl(theUrl);
		res.setResourceContentType(theContentType);
		res.setServiceVersion(this);
		res.setNewlyCreated(true);
		getUriToResource().put(theUrl, res);
		return res;
	}

	public void addServerAuth(PersBaseServerAuth<?, ?> theAuth) {
		theAuth.setServiceVersion(this);
		getServerAuths();
		myServerAuths.add(theAuth);
	}

	public void addUrl(PersServiceVersionUrl theUrl) {
		Validate.notNull(theUrl, "URL");
		getUrls();
		if (!myUrls.contains(theUrl)) {
			myUrls.add(theUrl);
			urlsChanged();
		}
	}
	
	/**
	 * Should URLs with paths below (i.e. even longer) the target URL for this service be passed to the service
	 */
	@SuppressWarnings("static-method")
	public boolean isAllowSubUrls() {
		return false;
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
		return getService().determineInheritedAuditLogEnable();
	}

	@Override
	public Integer determineInheritedKeepNumRecentTransactions(ResponseTypeEnum theResultType) {
		if (myService == null) {
			return null;
		}
		return myService.determineKeepNumRecentTransactions(theResultType);
	}

	@Override
	public Set<String> determineInheritedObscureRequestElements() {
		return myService.determineObscureRequestElements();
	}

	@Override
	public Set<String> determineInheritedObscureResponseElements() {
		return myService.determineObscureResponseElements();
	}

	@Override
	public Integer determineKeepNumRecentTransactions(ResponseTypeEnum theResultType) {
		Integer retVal = super.determineKeepNumRecentTransactions(theResultType);
		if (retVal == null) {
			retVal = myService.determineKeepNumRecentTransactions(theResultType);
		}
		return retVal;
	}

	@Override
	public Set<String> determineObscureRequestElements() {
		Set<String> retVal = getObscureRequestElementsInLog();
		if (retVal.isEmpty()) {
			retVal = myService.determineObscureRequestElements();
		}
		return retVal;
	}

	@Override
	public Set<String> determineObscureResponseElements() {
		Set<String> retVal = getObscureResponseElementsInLog();
		if (retVal.isEmpty()) {
			retVal = myService.determineObscureResponseElements();
		}
		return retVal;
	}

	public String determineUsableProxyPath() {
		if (isUseDefaultProxyPath()) {
			return getDefaultProxyPath();
		}
		if (StringUtils.isNotBlank(getExplicitProxyPath())) {
			return getExplicitProxyPath();
		}
		return getDefaultProxyPath();
	}

	public Collection<PersMonitorRuleActiveCheck> getActiveChecks() {
		if (myActiveChecks == null) {
			myActiveChecks = new ArrayList<>();
		}
		return myActiveChecks;
	}

	@Override
	public Collection<? extends BasePersServiceVersion> getAllServiceVersions() {
		return Collections.singleton(this);
	}

	/**
	 * @return the clientAuths
	 */
	public List<PersBaseClientAuth<?>> getClientAuths() {
		if (myClientAuths == null) {
			myClientAuths = new ArrayList<>();
		}
		return Collections.unmodifiableList(myClientAuths);
	}

	public PersBaseClientAuth<?> getClientAuthWithPid(Long thePid) {
		for (PersBaseClientAuth<?> next : getClientAuths()) {
			if (next.getPid() != null && next.getPid().equals(thePid)) {
				return next;
			}
		}
		return null;
	}

	public String getDefaultProxyPath() {
		PersService service = getService();
		PersDomain domain = service.getDomain();
		String domainId = domain.getDomainId();
		String serviceId = service.getServiceId();
		String versionId = getVersionId();
		return ProxyUtil.createDefaultPath(domainId, serviceId, versionId);
	}

	public String getDescription() {
//		if (myDescriptionExtended != null) {
			return myDescriptionExtended;
//		}
//		return myDescription;
	}

	public String getExplicitProxyPath() {
		return myExplicitProxyPath;
	}

	/**
	 * @return the httpClientConfig
	 */
	public PersHttpClientConfig getHttpClientConfig() {
		return myHttpClientConfig;
	}

	public PersMethod getMethod(String theName) {
		/*
		 * We avoid synchronization here at the expense of the small chance
		 * we'll create the nameToMethod map more than once..
		 */

		if (myNameToMethod == null) {
			HashMap<String, PersMethod> nameToMethod = new HashMap<>();
			for (PersMethod next : getMethods()) {
				nameToMethod.put(next.getName(), next);
			}
			myNameToMethod = nameToMethod;
			return nameToMethod.get(theName);
		}

		return myNameToMethod.get(theName);
	}

	public PersMethod getMethodForRootElementName(String theName) {
		/*
		 * We avoid synchronization here at the expense of the small chance
		 * we'll create the nameToMethod map more than once..
		 */

		if (myRootElementNameToMethod == null) {
			HashMap<String, PersMethod> nameToMethod = new HashMap<>();
			for (PersMethod next : getMethods()) {
				nameToMethod.put(next.getRootElements(), next);
			}
			myRootElementNameToMethod = nameToMethod;
			return nameToMethod.get(theName);
		}

		return myRootElementNameToMethod.get(theName);
	}

	public List<String> getMethodNames() {
		ArrayList<String> retVal = new ArrayList<>();
		for (PersMethod nextMethod : getMethods()) {
			retVal.add(nextMethod.getName());
		}
		Collections.sort(retVal);
		return Collections.unmodifiableList(retVal);
	}

	/**
	 * @return the methods
	 */
	public List<PersMethod> getMethods() {
		if (myMethods == null) {
			myMethods = new ArrayList<>();
		}

		return Collections.unmodifiableList(myMethods);
	}

	public Collection<PersMonitorAppliesTo> getMonitorRules() {
		if (myMonitorRules == null) {
			myMonitorRules = new ArrayList<>();
		}
		return myMonitorRules;
	}

	/**
	 * @return the versionNum
	 */
	public int getOptLock() {
		return myOptLock;
	}

	public PersMethod getOrCreateAndAddMethodWithName(String theName) {
		List<PersMethod> methods = getMethods();
		for (PersMethod next : methods) {
			String name = next.getName();
			if (name.equals(theName)) {
				return next;
			}
		}

		PersMethod method = new PersMethod();
		method.setName(theName);
		method.setServiceVersion(this);

		getMethods();
		myMethods.add(method);

		return method;
	}

	/**
	 * @return the id
	 */
	@Override
	public Long getPid() {
		return myPid;
	}

	public Collection<PersPropertyCapture> getPropertyCaptures() {
		if (myPropertyCaptures == null) {
			myPropertyCaptures = new ArrayList<>();
		}
		return myPropertyCaptures;
	}

	public PersPropertyCapture getPropertyCaptureWithPropertyName(String thePropertyName) {
		for (PersPropertyCapture next : getPropertyCaptures()) {
			if (next.getPk().getPropertyName().equals(thePropertyName)) {
				return next;
			}
		}
		return null;
	}

	public abstract ServiceProtocolEnum getProtocol();

	public PersServiceVersionResource getResourceForUri(String theUri) {
		PersServiceVersionResource res = getUriToResource().get(theUri);
		if (res != null) {
			return res;
		}
		return null;
	}

	public String getResourceTextForUri(String theUri) {
		PersServiceVersionResource res = getUriToResource().get(theUri);
		if (res != null) {
			return res.getResourceText();
		}
		return null;
	}

	public PersServiceVersionResource getResourceWithPid(long theXsdNum) {
		for (PersServiceVersionResource next : getUriToResource().values()) {
			if (next.getPid().equals(theXsdNum)) {
				return next;
			}
		}
		return null;
	}

	/**
	 * @return the serverAuths
	 */
	public List<PersBaseServerAuth<?, ?>> getServerAuths() {
		if (myServerAuths == null) {
			myServerAuths = new ArrayList<>();
		}
		return Collections.unmodifiableList(myServerAuths);
	}

	public PersBaseServerAuth<?, ?> getServerAuthWithPid(Long thePid) {
		for (PersBaseServerAuth<?, ?> next : getServerAuths()) {
			if (next.getPid() != null && next.getPid().equals(thePid)) {
				return next;
			}
		}
		return null;
	}

	public ServerSecuredEnum getServerSecured() {
		switch (getServerSecurityMode()) {
		case REQUIRE_ALL:
		case REQUIRE_ANY:
			if (getServerAuths().size() > 0) {
				return ServerSecuredEnum.FULLY;
			}
			break;
		case ALLOW_ANY:
		case NONE:
			break;
		}

		return ServerSecuredEnum.NONE;
	}

	public ServerSecurityModeEnum getServerSecurityMode() {
		return myServerSecurityMode;
	}

	/**
	 * @return the service
	 */
	public PersService getService() {
		return myService;
	}

	/**
	 * Returns (and creates if neccesary) the status
	 */
	public PersServiceVersionStatus getStatus() {
		if (myStatus == null) {
			myStatus = new PersServiceVersionStatus();
		}
		return myStatus;
	}

	public PersServiceVersionThrottle getThrottle() {
		return myThrottle;
	}

	/**
	 * @return the uriToResource
	 */
	public Map<String, PersServiceVersionResource> getUriToResource() {
		if (myUriToResource == null) {
			myUriToResource = new HashMap<>();
		}
		return myUriToResource;
	}

	/**
	 * @return the urlCounter
	 */
	public AtomicInteger getUrlCounter() {
		return myUrlCounter;
	}

	/**
	 * @return the urls
	 */
	public List<PersServiceVersionUrl> getUrls() {
		if (myUrls == null) {
			myUrls = new ArrayList<>();
		}
		return (myUrls);
	}

	public PersServiceVersionUrl getUrlWithId(String theString) {
		Map<String, PersServiceVersionUrl> map = myIdToUrl;
		if (map == null) {
			map = new HashMap<>();
			for (PersServiceVersionUrl next : myUrls) {
				map.put(next.getUrlId(), next);
			}
			myIdToUrl = map;
		}
		return map.get(theString);
	}

	public PersServiceVersionUrl getUrlWithPid(long thePid) {
		Map<Long, PersServiceVersionUrl> map = myPidToUrl;
		if (map == null) {
			map = new HashMap<>();
			for (PersServiceVersionUrl next : myUrls) {
				map.put(next.getPid(), next);
			}
			myPidToUrl = map;
		}
		return map.get(thePid);
	}

	public PersServiceVersionUrl getUrlWithUrl(String theUrl) {
		Map<String, PersServiceVersionUrl> map = myUrlToUrl;
		if (map == null) {
			map = new HashMap<>();
			for (PersServiceVersionUrl next : getUrls()) {
				map.put(next.getUrl(), next);
			}
			myUrlToUrl = map;
		}
		return map.get(theUrl);
	}

	/**
	 * @return the versionId
	 */
	public String getVersionId() {
		return myVersionId;
	}

	/**
	 * @return the active
	 */
	public boolean isActive() {
		return myActive;
	}

	public boolean isUseDefaultProxyPath() {
		return myUseDefaultProxyPath;
	}

	public void loadAllAssociations() {
		if (myAssociationsLoaded) {
			return;
		}
		myAssociationsLoaded = true;

		for (PersBaseClientAuth<?> next : getClientAuths()) {
			next.loadAllAssociations();
		}

		for (PersBaseServerAuth<?, ?> next : getServerAuths()) {
			next.loadAllAssociations();
		}

		for (PersMethod next : getMethods()) {
			next.loadAllAssociations();
		}

		for (PersPropertyCapture next : getPropertyCaptures()) {
			next.loadAllAssociations();
		}

		for (PersServiceVersionResource next : getUriToResource().values()) {
			next.loadAllAssociations();
		}

		for (PersServiceVersionUrl next : getUrls()) {
			next.loadAllAssociations();
		}

		for (PersMonitorAppliesTo next : getMonitorRules()) {
			next.loadAllAssociations();
		}

		for (PersMonitorRuleActiveCheck next : getActiveChecks()) {
			next.loadAllAssociations();
		}

		myHttpClientConfig.loadAllAssociations();

		if (myThrottle != null) {
			myThrottle.loadAllAssociations();
		}
		
	}

	public void populateDtoWithMonitorRules(BaseDtoServiceCatalogItem theDto) {
		for (PersMonitorAppliesTo nextRule : this.getMonitorRules()) {
			if (nextRule.getItem().equals(this)) {
				theDto.getMonitorRulePids().add(nextRule.getPid());
			}
		}
		for (PersMonitorRuleActiveCheck next : this.getActiveChecks()) {
			theDto.getMonitorRulePids().add(next.getRule().getPid());
		}
	}

	public StatusEnum populateDtoWithStatusAndProvideStatusForParent(BaseDtoServiceCatalogItem theDashboardObject, StatusesBean theStatuses, StatusEnum theInitialStatus) {
		StatusEnum retVal = theInitialStatus;

		for (PersServiceVersionUrl nextUrl : this.getUrls()) {
			StatusEnum nextUrlStatus = theStatuses.getUrlStatusEnum(nextUrl.getPid());
			if (nextUrlStatus == null) {
				continue;
			}

			switch (nextUrlStatus) {
			case ACTIVE:
				retVal = StatusEnum.ACTIVE;
				theDashboardObject.setUrlsActive(theDashboardObject.getUrlsActiveAsIntWithDefault() + 1);
				break;
			case DOWN:
				if (retVal != StatusEnum.ACTIVE) {
					retVal = StatusEnum.DOWN;
				}
				theDashboardObject.setUrlsDown(theDashboardObject.getUrlsDownAsIntWithDefault() + 1);
				break;
			case UNKNOWN:
				theDashboardObject.setUrlsUnknown(theDashboardObject.getUrlsUnknownAsIntWithDefault() + 1);
				break;
			}

		} // end URL

		// XX myRuntimeStatusQuerySvc.extract60MinuteStats(nextVersion,
		// theAccumulator);

		// Failing monitor rules
		List<PersMonitorRuleFiring> failingRules = theStatuses.getFirings(this.getPid());
		for (PersMonitorRuleFiring next : failingRules) {
			theDashboardObject.getFailingApplicableRulePids().add(next.getPid());
		}

		return retVal;
	}

	/**
	 * Subclasses may override
	 */
	public void prePersist() {
		// nothing
	}

	public void putMethodAtIndex(PersMethod theMethod, int theIndex) {
		getMethods();

		if (!myMethods.contains(theMethod)) {
			throw new IllegalArgumentException("Method[" + theMethod.getName() + "] is not in version");
		}

		if (!myMethods.get(theIndex).equals(theMethod)) {
			myMethods.remove(theIndex);
			myMethods.add(theIndex, theMethod);
		}
	}

	public void removeClientAuth(PersBaseClientAuth<?> theClientAuth) {
		theClientAuth.setServiceVersion(null);
		getClientAuths();
		myClientAuths.remove(theClientAuth);
	}

	public void removePropertyCapture(PersPropertyCapture theNext) {
		getPropertyCaptures();
		myPropertyCaptures.remove(theNext);
	}

	public void removeServerAuth(PersBaseServerAuth<?, ?> theServerAuth) {
		theServerAuth.setServiceVersion(null);
		getServerAuths();
		myServerAuths.remove(theServerAuth);
	}

	/**
	 * Remove any URLs whose ID doesn't appear in the given IDs
	 */
	public void retainOnlyMethodsWithNames(String... theUrlIds) {
		retainOnlyMethodsWithNamesAndUnknownMethod(Arrays.asList(theUrlIds));
	}

	/**
	 * Remove any URLs whose ID doesn't appear in the given IDs
	 */
	public void retainOnlyMethodsWithNamesAndUnknownMethod(Collection<String> theIds) {
		ourLog.debug("Retaining method names: {}", theIds);
		getMethods();

		HashSet<String> ids = new HashSet<>(theIds);
		for (Iterator<PersMethod> iter = myMethods.iterator(); iter.hasNext();) {
			PersMethod next = iter.next();
			if (!BaseDtoServiceVersion.METHOD_NAME_UNKNOWN.equals(next.getName()) && !ids.contains(next.getName())) {
				ourLog.info("Removing Method with ID[{}] and NAME[{}] from Service Version with ID[{}/{}]", new Object[] { next.getPid(), next.getName(), getPid(), getVersionId() });
				iter.remove();
			}
		}
	}

	/**
	 * Remove any URLs whose ID doesn't appear in the given IDs
	 */
	public void retainOnlyUrlsWithIds(Collection<String> theIds) {
		HashSet<String> ids = new HashSet<>(theIds);
		int index = 0;
		for (Iterator<PersServiceVersionUrl> iter = myUrls.iterator(); iter.hasNext();) {
			PersServiceVersionUrl next = iter.next();
			if (!ids.contains(next.getUrlId())) {
				ourLog.info("Removing URL with ID[{}/{}] from Service Version with ID[{}/{}]", new Object[] { next.getPid(), next.getUrlId(), getPid(), getVersionId() });
				iter.remove();
			}
			next.setOrder(index++);
		}
		urlsChanged();
	}

	/**
	 * Remove any URLs whose ID doesn't appear in the given IDs
	 */
	public void retainOnlyUrlsWithIds(String... theUrlIds) {
		retainOnlyUrlsWithIds(Arrays.asList(theUrlIds));
	}

	/**
	 * @param theActive
	 *            the active to set
	 */
	public void setActive(boolean theActive) {
		myActive = theActive;
	}

	public void setDescription(String theDescription) {
//		if (theDescription == null) {
//			myDescription = null;
//			myDescriptionExtended = null;
//		} else if (theDescription.length() < 2000) {
//			myDescription = theDescription;
//			myDescriptionExtended = null;
//		} else {
			myDescriptionExtended = theDescription;
//			myDescription = null;
//		}
	}

	public void setExplicitProxyPath(String theExplicitProxyPath) throws ProcessingException {
		if (theExplicitProxyPath != null) {
			if (!theExplicitProxyPath.startsWith("/")) {
				throw new ProcessingException("Proxy path must start with '/'");
			}
		}
		myExplicitProxyPath = theExplicitProxyPath;
	}

	/**
	 * @param theHttpClientConfig
	 *            the httpClientConfig to set
	 */
	public void setHttpClientConfig(PersHttpClientConfig theHttpClientConfig) {
		myHttpClientConfig = theHttpClientConfig;
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

	public void setServerSecurityMode(ServerSecurityModeEnum theServerSecurityMode) {
		myServerSecurityMode = theServerSecurityMode;
	}

	/**
	 * @param theService
	 *            the service to set
	 */
	public void setService(PersService theService) {
		if (myService != theService) {
			theService.addVersion(this);
		}
		myService = theService;
	}

	public void setStatus(PersServiceVersionStatus theStatus) {
		Validate.notNull(theStatus, "Status");
		myStatus = theStatus;
	}

	public void setThrottle(PersServiceVersionThrottle theThrottle) {
		myThrottle = theThrottle;
	};

	public void setUseDefaultProxyPath(boolean theDefaultProxyPath) {
		myUseDefaultProxyPath = theDefaultProxyPath;
	}

	/**
	 * @param theVersionId
	 *            the versionId to set
	 */
	public void setVersionId(String theVersionId) {
		validateId(theVersionId);
		myVersionId = theVersionId;
	}

	public BaseDtoServiceVersion toDto() throws UnexpectedFailureException {
		Set<Long> emptySet = Collections.emptySet();
		return toDto(emptySet, null, null, emptySet, emptySet);
	}

	// TODO: refactor this method to put the set params together
	public BaseDtoServiceVersion toDto(Set<Long> theLoadVerStats, IRuntimeStatusQueryLocal theQuerySvc, StatusesBean theStatuses, Set<Long> theLoadMethodStats, Set<Long> theLoadUrlStats) throws UnexpectedFailureException {

		BaseDtoServiceVersion retVal = createDtoAndPopulateWithTypeSpecificEntries();

		retVal.setPid(this.getPid());
		retVal.setId(this.getVersionId());
		retVal.setName(this.getVersionId());
		retVal.setServerSecured(this.getServerSecured());
		retVal.setUseDefaultProxyPath(this.isUseDefaultProxyPath());
		retVal.setDefaultProxyPath(this.getDefaultProxyPath());
		retVal.setExplicitProxyPath(this.getExplicitProxyPath());
		retVal.setParentServiceName(this.getService().getServiceName());
		retVal.setParentServicePid(this.getService().getPid());
		retVal.setDescription(this.getDescription());
		retVal.setServerSecurityMode(this.getServerSecurityMode());
		
		if (this.getThrottle() != null) {
			retVal.setThrottle(this.getThrottle().toDto());
		}

		populateKeepRecentTransactionsToDto(retVal);
		populateServiceCatalogItemToDto(retVal);
		populateDtoWithMonitorRules(retVal);

		for (PersMethod nextMethod : this.getMethods()) {
			if (!BaseDtoServiceVersion.METHOD_NAME_UNKNOWN.equals(nextMethod.getName())) {
				boolean loadStats = theLoadMethodStats != null && theLoadMethodStats.contains(nextMethod.getPid());
				DtoMethod gMethod = nextMethod.toDto(loadStats, theQuerySvc, theStatuses);
				retVal.getMethodList().add(gMethod);
			}
		} // for methods

		for (PersServiceVersionUrl nextUrl : this.getUrls()) {
			boolean loadUrlStats = theLoadUrlStats != null && (theLoadUrlStats.contains(nextUrl.getPid()) || theLoadUrlStats.contains(AdminServiceBean.URL_PID_TO_LOAD_ALL));
			GServiceVersionUrl gUrl = nextUrl.toDto(loadUrlStats, theStatuses, theQuerySvc);
			retVal.getUrlList().add(gUrl);
		} // for URLs

		for (PersServiceVersionResource nextResource : this.getUriToResource().values()) {
			GServiceVersionResourcePointer gResource = nextResource.toDao();
			retVal.getResourcePointerList().add(gResource);
		} // for resources

		for (PersBaseServerAuth<?, ?> nextServerAuth : this.getServerAuths()) {
			BaseDtoServerSecurity gServerAuth = nextServerAuth.toDto();
			retVal.getServerSecurityList().add(gServerAuth);
		} // server auths

		for (PersBaseClientAuth<?> nextClientAuth : this.getClientAuths()) {
			BaseDtoClientSecurity gClientAuth = nextClientAuth.toDao();
			retVal.getClientSecurityList().add(gClientAuth);
		} // Client auths

		for (PersPropertyCapture next : getPropertyCaptures()) {
			DtoPropertyCapture dto = next.toDto();
			retVal.getPropertyCaptures().add(dto);
		}// property captures

		PersHttpClientConfig httpClientConfig = this.getHttpClientConfig();
		if (httpClientConfig == null) {
			throw new UnexpectedFailureException("Service version doesn't have an HTTP client config");
		}
		retVal.setHttpClientConfigPid(httpClientConfig.getPid());
		retVal.setHttpClientConfigId(httpClientConfig.getId());

		if (theLoadVerStats.contains(getPid())) {

			StatusEnum status = StatusEnum.UNKNOWN;
			StatsAccumulator accumulator = null;
			populateDtoWithStatusAndProvideStatusForParent(retVal, theStatuses, status);

			accumulator = theQuerySvc.extract60MinuteStats(this);
			accumulator.populateDto(retVal);

			retVal.setStatsInitialized(new Date());

			// This should be covered by populateDtoWithStatusAnd... above
			// int urlsActive = 0;
			// int urlsDown = 0;
			// int urlsUnknown = 0;
			// for (PersServiceVersionUrl nextUrl : this.getUrls()) {
			//
			// PersServiceVersionUrlStatus urlStatus =
			// theStatuses.getUrlStatus(nextUrl.getPid());
			// if (urlStatus == null) {
			// continue;
			// }
			//
			// switch (urlStatus.getStatus()) {
			// case ACTIVE:
			// urlsActive++;
			// break;
			// case DOWN:
			// urlsDown++;
			// break;
			// case UNKNOWN:
			// urlsUnknown++;
			// break;
			// }
			//
			// }
			// retVal.setUrlsActive(urlsActive);
			// retVal.setUrlsDown(urlsDown);
			// retVal.setUrlsUnknown(urlsUnknown);

			PersServiceVersionStatus svcVerStatus = theStatuses.getServiceVersionStatus(this.getPid());
			if (svcVerStatus != null) {
				retVal.setLastServerSecurityFailure(svcVerStatus.getLastServerSecurityFailure());
				retVal.setLastSuccessfulInvocation(svcVerStatus.getLastSuccessfulInvocation());
				retVal.setLastFaultInvocation(svcVerStatus.getLastFaultInvocation());
				retVal.setLastFailInvocation(svcVerStatus.getLastFailInvocation());
			}

			if (retVal.getUrlsDownAsIntWithDefault() > 0) {
				retVal.setStatus(net.svcret.admin.shared.model.StatusEnum.DOWN);
			} else if (retVal.getUrlsActiveAsIntWithDefault() > 0) {
				retVal.setStatus(net.svcret.admin.shared.model.StatusEnum.ACTIVE);
			} else {
				retVal.setStatus(net.svcret.admin.shared.model.StatusEnum.UNKNOWN);
			}

		}

		return retVal;
	}

	private void urlsChanged() {
		myIdToUrl = null;
		myPidToUrl = null;
		myUrlToUrl = null;
	}

	protected abstract BaseDtoServiceVersion createDtoAndPopulateWithTypeSpecificEntries();

	/**
	 * Subclasses may override
	 */
	@SuppressWarnings("unused")
	protected void fromDto(BaseDtoServiceVersion theDto, IDao theDao) throws ProcessingException {
	}

	public static <T extends BaseDtoServiceVersion> BasePersServiceVersion fromDto(T theDto, PersService theService, IDao theDao, IServiceRegistry theServiceRegistry) throws ProcessingException, UnexpectedFailureException {
		Validate.notNull(theDto);
		Validate.notNull(theService);

		String versionId = theDto.getId();

		BasePersServiceVersion retVal;
		if (theDto.getPidOrNull() != null) {
			ourLog.debug("Retrieving existing service version PID[{}]", theDto.getPidOrNull());
			retVal = theDao.getServiceVersionByPid(theDto.getPid());
		} else {
			ourLog.debug("Retrieving service version ID[{}]", versionId);
			retVal = theServiceRegistry.getOrCreateServiceVersionWithId(theService, theDto.getProtocol(), versionId);
			ourLog.debug("Found service version NEW[{}], PID[{}], PROTOCOL[{}]", new Object[] { retVal.isNewlyCreated(), retVal.getPid(), retVal.getProtocol().name() });
		}
		
		retVal = theDao.getServiceVersionByPid(retVal.getPid());

		retVal.fromDto(theDto, theDao);

		retVal.setActive(theDto.isActive());
		retVal.setVersionId(theDto.getId());
		retVal.setExplicitProxyPath(theDto.getExplicitProxyPath());
		retVal.setDescription(theDto.getDescription());
		retVal.setUseDefaultProxyPath(theDto.isUseDefaultProxyPath());

		retVal.setServerSecurityMode(theDto.getServerSecurityMode());
		if (retVal.getServerSecurityMode() == null) {
			if (theDto.getServerSecurityList().size() > 0) {
				retVal.setServerSecurityMode(ServerSecurityModeEnum.REQUIRE_ANY);
			} else {
				retVal.setServerSecurityMode(ServerSecurityModeEnum.NONE);
			}
		}

		PersHttpClientConfig httpClientConfig = theDao.getHttpClientConfig(theDto.getHttpClientConfigPid());
		if (httpClientConfig == null) {
			throw new ProcessingException("Unknown HTTP client config PID: " + theDto.getHttpClientConfigPid());
		}
		retVal.setHttpClientConfig(httpClientConfig);

		retVal.populateKeepRecentTransactionsFromDto(theDto);
		retVal.populateServiceCatalogItemFromDto(theDto);

		return retVal;
	}

	/**
	 * Should this service request authorization using HTTP 401, which prompts a browser for 
	 * credentials. This doesn't make sense for many services (e.g. SOAP) but might make
	 * sense for RESTful services.
	 */
	@SuppressWarnings("static-method")
	public boolean isRequestBrowserAuthentication() {
		return false;
	}

}
