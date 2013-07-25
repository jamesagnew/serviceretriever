package net.svcret.ejb.model.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
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

import net.svcret.admin.shared.model.ServerSecuredEnum;
import net.svcret.admin.shared.model.ServiceProtocolEnum;
import net.svcret.ejb.api.ResponseTypeEnum;
import net.svcret.ejb.ex.ProcessingException;
import net.svcret.ejb.util.Validate;

import org.hibernate.annotations.ForeignKey;

@Entity
@Table(name = "PX_SVC_VER", uniqueConstraints = { @UniqueConstraint(columnNames = { "SERVICE_PID", "VERSION_ID" }) })
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "SVCVER_TYPE", length = 20, discriminatorType = DiscriminatorType.STRING)
public abstract class BasePersServiceVersion extends BasePersServiceCatalogItem {

	static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(BasePersServiceVersion.class);

	private static final long serialVersionUID = 1L;

	@Column(name = "ISACTIVE")
	private boolean myActive;

	@Transient
	private transient boolean myAssociationsLoaded;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "myServiceVersion")
	@OrderBy("CAUTH_ORDER")
	private List<PersBaseClientAuth<?>> myClientAuths;

	@OneToMany(fetch=FetchType.LAZY, cascade= {}, orphanRemoval=true, mappedBy="myServiceVersion")
	private Collection<PersMonitorAppliesTo> myMonitorRules;

	@Column(name = "SVC_DESC", length = 2000, nullable = true)
	private String myDescription;

	@Lob()
	@Column(name = "SVC_DESC_EXT", nullable = true)
	private String myDescriptionExtended;

	@Column(name = "EXPLICIT_PROXY_PATH", length = 400, nullable = true)
	private String myExplicitProxyPath;

	@ManyToOne(cascade = {}, fetch = FetchType.LAZY)
	@ForeignKey(name = "PX_SVCVER_HTTP_CONFIG_PID")
	@JoinColumn(name = "HTTP_CONFIG_PID", referencedColumnName = "PID", nullable = false)
	private PersHttpClientConfig myHttpClientConfig;

	@Transient
	private transient Map<String, PersServiceVersionUrl> myIdToUrl;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "myServiceVersion")
	@OrderBy("METHOD_ORDER")
	private List<PersServiceVersionMethod> myMethods;

	@Transient
	private transient Map<String, PersServiceVersionMethod> myNameToMethod;

	@Version()
	@Column(name = "OPTLOCK")
	private int myOptLock;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "PID")
	private Long myPid;

	@Transient
	private transient Map<Long, PersServiceVersionUrl> myPidToUrl;

	@OneToMany(fetch = FetchType.LAZY, cascade = {}, orphanRemoval = true, mappedBy = "myServiceVersion")
	private List<PersServiceVersionRecentMessage> myRecentMessages;

	@Transient
	private transient HashMap<String, PersServiceVersionMethod> myRootElementNameToMethod;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "myServiceVersion")
	@OrderBy("SAUTH_ORDER")
	private List<PersBaseServerAuth<?, ?>> myServerAuths;

	@ManyToOne()
	@ForeignKey(name = "PX_SVCVER_SERVICE_PID")
	@JoinColumn(name = "SERVICE_PID", referencedColumnName = "PID")
	private PersService myService;

	@OneToOne(cascade = {}, fetch = FetchType.LAZY, mappedBy = "myServiceVersion", orphanRemoval = true)
	private PersServiceVersionStatus myStatus;

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

	@OneToMany(fetch = FetchType.LAZY, cascade = {}, orphanRemoval = true, mappedBy = "myServiceVersion")
	private Collection<PersUserServiceVersionPermission> myUserPermissions;

	@OneToMany(fetch = FetchType.LAZY, cascade = {}, orphanRemoval = true, mappedBy = "myServiceVersion")
	private List<PersUserRecentMessage> myUserRecentMessages;

	@Column(name = "VERSION_ID", length = 200, nullable = false)
	private String myVersionId;

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

	public void addMethod(PersServiceVersionMethod method) {
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

	public Collection<PersMonitorAppliesTo> getMonitorRules() {
		if (myMonitorRules==null) {
			myMonitorRules=new ArrayList<PersMonitorAppliesTo>();
		}
		return myMonitorRules;
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

	@Override
	public Integer determineKeepNumRecentTransactions(ResponseTypeEnum theResultType) {
		Integer retVal = super.determineKeepNumRecentTransactions(theResultType);
		if (retVal == null) {
			retVal = myService.determineKeepNumRecentTransactions(theResultType);
		}
		return retVal;
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
			myClientAuths = new ArrayList<PersBaseClientAuth<?>>();
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

	public String getDescription() {
		if (myDescriptionExtended != null) {
			return myDescriptionExtended;
		}
		return myDescription;
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

	public PersServiceVersionMethod getMethod(String theName) {
		/*
		 * We avoid synchronization here at the expense of the small chance we'll create the nameToMethod map more than once..
		 */

		if (myNameToMethod == null) {
			HashMap<String, PersServiceVersionMethod> nameToMethod = new HashMap<String, PersServiceVersionMethod>();
			for (PersServiceVersionMethod next : getMethods()) {
				nameToMethod.put(next.getName(), next);
			}
			myNameToMethod = nameToMethod;
			return nameToMethod.get(theName);
		}

		return myNameToMethod.get(theName);
	}

	public PersServiceVersionMethod getMethodForRootElementName(String theName) {
		/*
		 * We avoid synchronization here at the expense of the small chance we'll create the nameToMethod map more than once..
		 */

		if (myRootElementNameToMethod == null) {
			HashMap<String, PersServiceVersionMethod> nameToMethod = new HashMap<String, PersServiceVersionMethod>();
			for (PersServiceVersionMethod next : getMethods()) {
				nameToMethod.put(next.getRootElements(), next);
			}
			myRootElementNameToMethod = nameToMethod;
			return nameToMethod.get(theName);
		}

		return myRootElementNameToMethod.get(theName);
	}

	public List<String> getMethodNames() {
		ArrayList<String> retVal = new ArrayList<String>();
		for (PersServiceVersionMethod nextMethod : getMethods()) {
			retVal.add(nextMethod.getName());
		}
		Collections.sort(retVal);
		return Collections.unmodifiableList(retVal);
	}

	/**
	 * @return the methods
	 */
	public List<PersServiceVersionMethod> getMethods() {
		if (myMethods == null) {
			myMethods = new ArrayList<PersServiceVersionMethod>();
		}

		return Collections.unmodifiableList(myMethods);
	}

	/**
	 * @return the versionNum
	 */
	public int getOptLock() {
		return myOptLock;
	}

	public PersServiceVersionMethod getOrCreateAndAddMethodWithName(String theName) {
		List<PersServiceVersionMethod> methods = getMethods();
		for (PersServiceVersionMethod next : methods) {
			String name = next.getName();
			if (name.equals(theName)) {
				return next;
			}
		}

		PersServiceVersionMethod method = new PersServiceVersionMethod();
		method.setName(theName);
		method.setServiceVersion(this);

		getMethods();
		myMethods.add(method);

		return method;
	}

	/**
	 * @return the id
	 */
	public Long getPid() {
		return myPid;
	}

	public abstract ServiceProtocolEnum getProtocol();

	public String getProxyPath() {
		if (getExplicitProxyPath() != null) {
			return getExplicitProxyPath();
		}
		PersService service = getService();
		PersDomain domain = service.getDomain();
		return "/" + domain.getDomainId() + "/" + service.getServiceId() + "/" + getVersionId();
	}

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
			myServerAuths = new ArrayList<PersBaseServerAuth<?, ?>>();
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
		if (getServerAuths().size() > 0) {
			return ServerSecuredEnum.FULLY;
		} else {
			return ServerSecuredEnum.NONE;
		}
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

	/**
	 * @return the uriToResource
	 */
	public Map<String, PersServiceVersionResource> getUriToResource() {
		if (myUriToResource == null) {
			myUriToResource = new HashMap<String, PersServiceVersionResource>();
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
			myUrls = new ArrayList<PersServiceVersionUrl>();
		}
		return (myUrls);
	}

	public PersServiceVersionUrl getUrlWithId(String theString) {
		Map<String, PersServiceVersionUrl> map = myIdToUrl;
		if (map == null) {
			map = new HashMap<String, PersServiceVersionUrl>();
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
			map = new HashMap<Long, PersServiceVersionUrl>();
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
			map = new HashMap<String, PersServiceVersionUrl>();
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

		for (PersServiceVersionMethod next : getMethods()) {
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

		myHttpClientConfig.loadAllAssociations();

	}

	public void putMethodAtIndex(PersServiceVersionMethod theMethod, int theIndex) {
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

	public void removeServerAuth(PersBaseServerAuth<?, ?> theServerAuth) {
		theServerAuth.setServiceVersion(null);
		getServerAuths();
		myServerAuths.remove(theServerAuth);
	}

	/**
	 * Remove any URLs whose ID doesn't appear in the given IDs
	 */
	public void retainOnlyMethodsWithNames(Collection<String> theIds) {
		ourLog.debug("Retaining method names: {}", theIds);

		HashSet<String> ids = new HashSet<String>(theIds);
		for (Iterator<PersServiceVersionMethod> iter = getMethods().iterator(); iter.hasNext();) {
			PersServiceVersionMethod next = iter.next();
			if (!ids.contains(next.getName())) {
				ourLog.info("Removing Method with ID[{}] and NAME[{}] from Service Version with ID[{}/{}]", new Object[] { next.getPid(), next.getName(), getPid(), getVersionId() });
				iter.remove();
			}
		}
	}

	/**
	 * Remove any URLs whose ID doesn't appear in the given IDs
	 */
	public void retainOnlyMethodsWithNames(String... theUrlIds) {
		retainOnlyMethodsWithNames(Arrays.asList(theUrlIds));
	}

	/**
	 * Remove any URLs whose ID doesn't appear in the given IDs
	 */
	public void retainOnlyUrlsWithIds(Collection<String> theIds) {
		HashSet<String> ids = new HashSet<String>(theIds);
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
		if (theDescription == null) {
			myDescription = null;
			myDescriptionExtended = null;
		} else if (theDescription.length() < 2000) {
			myDescription = theDescription;
			myDescriptionExtended = null;
		} else {
			myDescriptionExtended = theDescription;
			myDescription = null;
		}
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

	/**
	 * @param theVersionId
	 *            the versionId to set
	 */
	public void setVersionId(String theVersionId) {
		// TODO: validate characters
		myVersionId = theVersionId;
	}

	private void urlsChanged() {
		myIdToUrl = null;
		myPidToUrl = null;
		myUrlToUrl = null;
	}

}
