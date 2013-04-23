package net.svcret.ejb.ejb;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import net.svcret.admin.shared.model.BaseGAuthHost;
import net.svcret.admin.shared.model.BaseGClientSecurity;
import net.svcret.admin.shared.model.BaseGDashboardObjectWithUrls;
import net.svcret.admin.shared.model.BaseGServerSecurity;
import net.svcret.admin.shared.model.BaseGServiceVersion;
import net.svcret.admin.shared.model.GAuthenticationHostList;
import net.svcret.admin.shared.model.GConfig;
import net.svcret.admin.shared.model.GDomain;
import net.svcret.admin.shared.model.GDomainList;
import net.svcret.admin.shared.model.GHttpClientConfig;
import net.svcret.admin.shared.model.GHttpClientConfigList;
import net.svcret.admin.shared.model.GLocalDatabaseAuthHost;
import net.svcret.admin.shared.model.GPartialUserList;
import net.svcret.admin.shared.model.GResource;
import net.svcret.admin.shared.model.GService;
import net.svcret.admin.shared.model.GServiceMethod;
import net.svcret.admin.shared.model.GServiceVersionResourcePointer;
import net.svcret.admin.shared.model.GServiceVersionUrl;
import net.svcret.admin.shared.model.GSoap11ServiceVersion;
import net.svcret.admin.shared.model.GSoap11ServiceVersionAndResources;
import net.svcret.admin.shared.model.GUser;
import net.svcret.admin.shared.model.GUserDomainPermission;
import net.svcret.admin.shared.model.GUserList;
import net.svcret.admin.shared.model.GUserServicePermission;
import net.svcret.admin.shared.model.GUserServiceVersionMethodPermission;
import net.svcret.admin.shared.model.GUserServiceVersionPermission;
import net.svcret.admin.shared.model.GWsSecServerSecurity;
import net.svcret.admin.shared.model.GWsSecUsernameTokenClientSecurity;
import net.svcret.admin.shared.model.ModelUpdateRequest;
import net.svcret.admin.shared.model.ModelUpdateResponse;
import net.svcret.admin.shared.model.PartialUserListRequest;
import net.svcret.admin.shared.model.StatusEnum;
import net.svcret.ejb.api.IAdminService;
import net.svcret.ejb.api.IConfigService;
import net.svcret.ejb.api.IDao;
import net.svcret.ejb.api.IRuntimeStatus;
import net.svcret.ejb.api.ISecurityService;
import net.svcret.ejb.api.IServiceInvoker;
import net.svcret.ejb.api.IServiceRegistry;
import net.svcret.ejb.ex.ProcessingException;
import net.svcret.ejb.model.entity.BasePersAuthenticationHost;
import net.svcret.ejb.model.entity.BasePersInvocationStats;
import net.svcret.ejb.model.entity.BasePersServiceVersion;
import net.svcret.ejb.model.entity.InvocationStatsIntervalEnum;
import net.svcret.ejb.model.entity.PersAuthenticationHostLocalDatabase;
import net.svcret.ejb.model.entity.PersBaseClientAuth;
import net.svcret.ejb.model.entity.PersBaseServerAuth;
import net.svcret.ejb.model.entity.PersConfig;
import net.svcret.ejb.model.entity.PersConfigProxyUrlBase;
import net.svcret.ejb.model.entity.PersDomain;
import net.svcret.ejb.model.entity.PersHttpClientConfig;
import net.svcret.ejb.model.entity.PersInvocationStatsPk;
import net.svcret.ejb.model.entity.PersService;
import net.svcret.ejb.model.entity.PersServiceVersionMethod;
import net.svcret.ejb.model.entity.PersServiceVersionResource;
import net.svcret.ejb.model.entity.PersServiceVersionStatus;
import net.svcret.ejb.model.entity.PersServiceVersionUrl;
import net.svcret.ejb.model.entity.PersServiceVersionUrlStatus;
import net.svcret.ejb.model.entity.PersUser;
import net.svcret.ejb.model.entity.PersUserDomainPermission;
import net.svcret.ejb.model.entity.PersUserServicePermission;
import net.svcret.ejb.model.entity.PersUserServiceVersionMethodPermission;
import net.svcret.ejb.model.entity.PersUserServiceVersionPermission;
import net.svcret.ejb.model.entity.soap.PersServiceVersionSoap11;
import net.svcret.ejb.model.entity.soap.PersWsSecUsernameTokenClientAuth;
import net.svcret.ejb.model.entity.soap.PersWsSecUsernameTokenServerAuth;
import net.svcret.ejb.util.Validate;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

@TransactionAttribute(TransactionAttributeType.REQUIRED)
@Stateless
public class AdminServiceBean implements IAdminService {

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(AdminServiceBean.class);

	@EJB(name = "SOAP11Invoker")
	private IServiceInvoker<PersServiceVersionSoap11> myInvokerSoap11;

	@EJB
	private IDao myPersSvc;

	@EJB
	private IServiceRegistry myServiceRegistry;

	@EJB
	private ISecurityService mySecurityService;

	@EJB
	private IRuntimeStatus myStatusSvc;

	@Override
	public GDomain addDomain(GDomain theDomain) throws ProcessingException {
		Validate.notNull(theDomain, "ID");
		Validate.isNull(theDomain.getPidOrNull(), "PID");
		Validate.notBlank(theDomain.getId(), "ID");
		Validate.notBlank(theDomain.getName(), "Name");

		ourLog.info("Creating domain {}/{}", theDomain.getId(), theDomain.getName());

		PersDomain domain = myServiceRegistry.getOrCreateDomainWithId(theDomain.getId());
		if (!domain.isNewlyCreated()) {
			throw new IllegalArgumentException("Domain with ID[" + theDomain.getId() + "] already exists");
		}

		domain.setDomainName(theDomain.getName());
		myServiceRegistry.saveDomain(domain);

		return toUi(domain, false);
	}

	@Override
	public GService addService(long theDomainPid, String theId, String theName, boolean theActive) throws ProcessingException {
		Validate.notBlank(theId, "ID");
		Validate.notBlank(theName, "Name");

		ourLog.info("Adding service with ID[{}] to domain PID[{}]", theId, theDomainPid);

		PersDomain domain = myPersSvc.getDomainByPid(theDomainPid);
		if (domain == null) {
			throw new IllegalArgumentException("Unknown Domain PID: " + theDomainPid);
		}

		PersService service = myServiceRegistry.getOrCreateServiceWithId(domain, theId);
		if (!service.isNewlyCreated()) {
			throw new IllegalArgumentException("Service " + theId + " already exists for domain: " + domain.getDomainId());
		}

		service.setServiceName(theName);
		service.setActive(theActive);
		myServiceRegistry.saveService(service);

		return toUi(service, false);
	}

	public GServiceMethod addServiceVersionMethod(long theServiceVersionPid, GServiceMethod theMethod) throws ProcessingException {
		ourLog.info("Adding method {} to service version {}", theMethod.getName(), theServiceVersionPid);

		BasePersServiceVersion sv = myPersSvc.getServiceVersionByPid(theServiceVersionPid);
		PersServiceVersionMethod ui = fromUi(theMethod, theServiceVersionPid);
		sv.addMethod(ui);
		sv = myServiceRegistry.saveServiceVersion(sv);
		return toUi(sv.getMethod(theMethod.getName()), false);
	}

	@Override
	public GAuthenticationHostList deleteAuthenticationHost(long thePid) throws ProcessingException {

		BasePersAuthenticationHost authHost = myPersSvc.getAuthenticationHostByPid(thePid);
		if (authHost == null) {
			ourLog.info("Invalid request to delete unknown authentication host with PID {}", thePid);
			throw new ProcessingException("Unknown authentication host: " + thePid);
		}

		ourLog.info("Removing authentication host {} / {}", thePid, authHost.getModuleId());

		myPersSvc.deleteAuthenticationHost(authHost);

		return loadAuthHostList();
	}

	@Override
	public void deleteDomain(long thePid) throws ProcessingException {
		PersDomain domain = myPersSvc.getDomainByPid(thePid);
		if (domain == null) {
			throw new IllegalArgumentException("Unknown domain PID: " + thePid);
		}

		ourLog.info("DELETING domain with PID {} and ID {}", thePid, domain.getDomainId());

		myServiceRegistry.removeDomain(domain);

	}

	@Override
	public GHttpClientConfigList deleteHttpClientConfig(long thePid) throws ProcessingException {

		PersHttpClientConfig config = myPersSvc.getHttpClientConfig(thePid);
		if (config == null) {
			throw new ProcessingException("Unknown HTTP Client Config PID: " + thePid);
		}

		ourLog.info("Deleting HTTP Client Config {} / {}", thePid, config.getId());

		myServiceRegistry.deleteHttpClientConfig(config);

		return loadHttpClientConfigList();
	}

	@Override
	public long getDefaultHttpClientConfigPid() {
		return myPersSvc.getHttpClientConfigs().iterator().next().getPid();
	}

	@Override
	public GDomain getDomainByPid(long theDomain) throws ProcessingException {
		PersDomain domain = myPersSvc.getDomainByPid(theDomain);
		if (domain != null) {
			Set<Long> empty = Collections.emptySet();
			return loadDomain(domain, empty, empty, empty, empty);
		}
		return null;
	}

	@Override
	public long getDomainPid(String theDomainId) throws ProcessingException {
		PersDomain domain = myPersSvc.getDomainById(theDomainId);
		if (domain == null) {
			throw new ProcessingException("Unknown ID: " + theDomainId);
		}
		return domain.getPid();
	}

	@Override
	public GService getServiceByPid(long theService) {
		PersService service = myPersSvc.getServiceByPid(theService);
		if (service != null) {
			return toUi(service, false);
		}
		return null;
	}

	@Override
	public long getServicePid(long theDomainPid, String theServiceId) throws ProcessingException {
		PersService service = myPersSvc.getServiceById(theDomainPid, theServiceId);
		if (service == null) {
			throw new ProcessingException("Unknown ID: " + theServiceId);
		}
		return service.getPid();
	}

	@Override
	public BaseGAuthHost loadAuthenticationHost(long thePid) throws ProcessingException {
		ourLog.info("Loading authentication host with PID: {}", thePid);

		BasePersAuthenticationHost authHost = myPersSvc.getAuthenticationHostByPid(thePid);
		if (authHost == null) {
			throw new ProcessingException("Unknown authentication host: " + thePid);
		}

		return toUi(authHost);
	}

	@Override
	public GDomainList loadDomainList() throws ProcessingException {
		Set<Long> set = Collections.emptySet();
		return loadDomainList(set, set, set, set);
	}

	@Override
	public ModelUpdateResponse loadModelUpdate(ModelUpdateRequest theRequest) throws ProcessingException {
		ModelUpdateResponse retVal = new ModelUpdateResponse();

		if (theRequest.isLoadHttpClientConfigs()) {
			GHttpClientConfigList configList = loadHttpClientConfigList();
			retVal.setHttpClientConfigList(configList);
		}

		if (theRequest.isLoadUsers()) {
			GUserList userList = loadUserList();
			retVal.setUserList(userList);
		}

		if (theRequest.isLoadAuthHosts()) {
			GAuthenticationHostList hostList = loadAuthHostList();
			retVal.setAuthenticationHostList(hostList);
		}

		Set<Long> loadDomStats = theRequest.getDomainsToLoadStats();
		Set<Long> loadSvcStats = theRequest.getServicesToLoadStats();
		Set<Long> loadVerStats = theRequest.getVersionsToLoadStats();
		Set<Long> loadVerMethodStats = theRequest.getVersionMethodsToLoadStats();
		GDomainList domainList = loadDomainList(loadDomStats, loadSvcStats, loadVerStats, loadVerMethodStats);

		retVal.setDomainList(domainList);

		return retVal;
	}

	@Override
	public GSoap11ServiceVersionAndResources loadServiceVersion(long theServiceVersionPid) throws ProcessingException {
		BasePersServiceVersion svcVer = myPersSvc.getServiceVersionByPid(theServiceVersionPid);
		BaseGServiceVersion uiService = toUi(svcVer, false);
		GSoap11ServiceVersionAndResources retVal = toUi(uiService, svcVer);
		return retVal;
	}

	@Override
	public GSoap11ServiceVersionAndResources loadSoap11ServiceVersionFromWsdl(GSoap11ServiceVersion theService, String theWsdlUrl) throws ProcessingException {
		Validate.notNull(theService, "Definition");
		Validate.notBlank(theWsdlUrl, "URL");

		ourLog.info("Loading service version from URL: {}", theWsdlUrl);
		PersServiceVersionSoap11 def = myInvokerSoap11.introspectServiceFromUrl(theWsdlUrl);

		GSoap11ServiceVersionAndResources retVal = toUi(theService, def);
		return retVal;
	}

	@Override
	public GUser loadUser(long thePid) throws ProcessingException {
		ourLog.info("Loading user {}", thePid);

		PersUser persUser = myPersSvc.getUser(thePid);
		if (persUser == null) {
			throw new ProcessingException("Unknown user PID: " + thePid);
		}

		return toUi(persUser);
	}

	@Override
	public GPartialUserList loadUsers(PartialUserListRequest theRequest) {
		GPartialUserList retVal = new GPartialUserList();

		ourLog.info("Loading user list: " + theRequest.toString());

		for (PersUser next : myPersSvc.getAllServiceUsers()) {
			retVal.add(toUi(next));
		}

		return retVal;
	}

	@Override
	public GAuthenticationHostList saveAuthenticationHost(GLocalDatabaseAuthHost theAuthHost) throws ProcessingException {

		PersAuthenticationHostLocalDatabase host;
		if (theAuthHost.getPid() > 0) {
			host = (PersAuthenticationHostLocalDatabase) myPersSvc.getAuthenticationHostByPid(theAuthHost.getPid());
		} else {
			host = new PersAuthenticationHostLocalDatabase();
		}
		fromUi(host, theAuthHost);

		ourLog.info("Saving authentication host of type {} with id {} / {}", new Object[] { host.getClass().getSimpleName(), theAuthHost.getPid(), theAuthHost.getModuleId() });

		myPersSvc.saveAuthenticationHost(host);

		return loadAuthHostList();
	}

	@Override
	public GHttpClientConfig saveHttpClientConfig(GHttpClientConfig theConfig) throws ProcessingException {
		Validate.notNull(theConfig, "HttpClientConfig");

		PersHttpClientConfig existing = null;
		boolean isDefault = false;

		if (theConfig.getPid() <= 0) {
			ourLog.info("Saving new HTTP client config");
		} else {
			ourLog.info("Saving HTTP client config ID[{}]", theConfig.getPid());
			existing = myPersSvc.getHttpClientConfig(theConfig.getPid());
			if (existing == null) {
				throw new ProcessingException("Unknown client config PID: " + theConfig.getPid());
			}
			if (existing.getId().equals(GHttpClientConfig.DEFAULT_ID)) {
				isDefault = true;
			}
		}

		PersHttpClientConfig config = fromUi(theConfig);
		if (isDefault) {
			config.setId(config.getId());
			config.setName(config.getName());
		}

		return toUi(myServiceRegistry.saveHttpClientConfig(config));
	}

	@Override
	public <T extends BaseGServiceVersion> T saveServiceVersion(long theDomain, long theService, T theVersion, List<GResource> theResources) throws ProcessingException {
		Validate.notBlank(theVersion.getId(), "Version#ID");

		ourLog.info("Adding service version {} to domain {} / service {}", new Object[] { theVersion.getPid(), theDomain, theService });

		PersDomain domain = myPersSvc.getDomainByPid(theDomain);
		if (domain == null) {
			throw new ProcessingException("Unknown domain ID: " + theDomain);
		}

		PersService service = myPersSvc.getServiceByPid(theService);
		if (service == null) {
			throw new ProcessingException("Unknown service ID: " + theService);
		}

		if (!domain.equals(service.getDomain())) {
			throw new ProcessingException("Service with ID " + theService + " is not a part of domain " + theDomain);
		}

		String versionId = theVersion.getId();

		PersServiceVersionSoap11 version = null;
		switch (theVersion.getProtocol()) {
		case SOAP11:
			version = myServiceRegistry.getOrCreateServiceVersionWithId(service, versionId);
			fromUi(version, (GSoap11ServiceVersion) theVersion);
		}

		Map<String, PersServiceVersionResource> uriToResource = version.getUriToResource();
		Set<String> urls = new HashSet<String>();
		for (GResource next : theResources) {
			urls.add(next.getUrl());
			if (uriToResource.containsKey(next.getUrl())) {
				PersServiceVersionResource existing = uriToResource.get(next.getUrl());
				existing.merge(fromUi(next, version));
			} else {
				uriToResource.put(next.getUrl(), fromUi(next, version));
			}
		}
		for (Iterator<Map.Entry<String, PersServiceVersionResource>> iter = uriToResource.entrySet().iterator(); iter.hasNext();) {
			if (!urls.contains(iter.next().getKey())) {
				iter.remove();
			}
		}

		/*
		 * Urls
		 */
		urls = new HashSet<String>();
		for (GServiceVersionUrl next : theVersion.getUrlList()) {
			urls.add(next.getUrl());
			PersServiceVersionUrl existing = version.getUrlWithUrl(next.getUrl());
			if (existing != null) {
				existing.merge(fromUi(next, version));
			} else {
				version.getUrls().add(fromUi(next, version));
			}
		}
		for (Iterator<PersServiceVersionUrl> iter = version.getUrls().iterator(); iter.hasNext();) {
			if (!urls.contains(iter.next().getUrl())) {
				iter.remove();
			}
		}
		int index = 0;
		for (PersServiceVersionUrl next : version.getUrls()) {
			next.setOrder(index++);
		}

		/*
		 * Methods
		 */

		HashSet<String> methods = new HashSet<String>();
		for (GServiceMethod next : theVersion.getMethodList()) {
			methods.add(next.getName());
			PersServiceVersionMethod existing = version.getMethod(next.getName());
			if (existing != null) {
				existing.merge(fromUi(next, version.getPid()));
			} else {
				version.addMethod(fromUi(next, version.getPid()));
			}
		}
		version.retainOnlyMethodsWithNames(methods);
		index = 0;
		for (PersServiceVersionMethod next : version.getMethods()) {
			next.setOrder(index++);
		}

		/*
		 * Client Auths
		 */
		Set<Long> pids = new HashSet<Long>();
		for (BaseGClientSecurity next : theVersion.getClientSecurityList()) {
			PersBaseClientAuth<?> nextPers = fromUi(next, version);
			if (nextPers.getPid() != null) {
				PersBaseClientAuth<?> existing = version.getClientAuthWithPid(nextPers.getPid());
				if (existing != null) {
					existing.merge(nextPers);
				} else {
					pids.add(nextPers.getPid());
				}
				pids.add(nextPers.getPid());
			} else {
				nextPers = myPersSvc.saveClientAuth(nextPers);
				pids.add(nextPers.getPid());
				version.addClientAuth(nextPers);
			}
		}
		index = 0;
		for (PersBaseClientAuth<?> next : new ArrayList<PersBaseClientAuth<?>>(version.getClientAuths())) {
			if (next.getPid() != null && !pids.contains(next.getPid())) {
				version.removeClientAuth(next);
			} else {
				next.setOrder(index++);
			}
		}

		/*
		 * Server Auths
		 */
		pids = new HashSet<Long>();
		for (BaseGServerSecurity next : theVersion.getServerSecurityList()) {
			if (next.getAuthHostPid() <= 0) {
				throw new IllegalArgumentException("No auth host PID specified");
			}

			PersBaseServerAuth<?, ?> nextPers = fromUi(next, version);
			if (nextPers.getPid() != null) {
				PersBaseServerAuth<?, ?> existing = version.getServerAuthWithPid(nextPers.getPid());
				if (existing != null) {
					existing.merge(nextPers);
				} else {
					pids.add(nextPers.getPid());
				}
				pids.add(nextPers.getPid());
			} else {
				nextPers = myPersSvc.saveServerAuth(nextPers);
				pids.add(nextPers.getPid());
				version.addServerAuth(nextPers);
			}
			version.addServerAuth(nextPers);
		}
		index = 0;
		for (PersBaseServerAuth<?, ?> next : new ArrayList<PersBaseServerAuth<?, ?>>(version.getServerAuths())) {
			if (next.getPid() != null && !pids.contains(next.getPid())) {
				version.removeServerAuth(next);
			} else {
				next.setOrder(index++);
			}
		}

		version = (PersServiceVersionSoap11) myServiceRegistry.saveServiceVersion(version);

		@SuppressWarnings("unchecked")
		T retVal = (T) toUi(version, false);

		return retVal;
	}

	@Override
	public GUser saveUser(GUser theUser) throws ProcessingException {
		ourLog.info("Saving user with PID {}", theUser.getPid());

		PersUser user = fromUi(theUser);

		user = mySecurityService.saveServiceUser(user);

		return toUi(user);
	}

	/**
	 * FOR UNIT TESTS ONLY
	 */
	public void setRuntimeStatusBean(RuntimeStatusBean theRs) {
		myStatusSvc = theRs;
	}

	@Override
	public String suggestNewVersionNumber(Long theDomainPid, Long theServicePid) {
		Validate.throwIllegalArgumentExceptionIfNotPositive(theDomainPid, "DomainPID");
		Validate.throwIllegalArgumentExceptionIfNotPositive(theServicePid, "ServicePID");

		PersService service = myPersSvc.getServiceByPid(theServicePid);

		for (int i = 1;; i++) {
			String name = i + ".0";
			if (service.getVersionWithId(name) == null) {
				return name;
			}
		}

	}

	private int addToInt(int theAddTo, long theNumberToAdd) {
		long newValue = theAddTo + theNumberToAdd;
		if (newValue > Integer.MAX_VALUE) {
			return Integer.MAX_VALUE;
		}
		return (int) newValue;
	}

	private StatusEnum extractStatus(BaseGDashboardObjectWithUrls<?> theDashboardObject, int[] the60MinInvCount, long[] the60minTime, StatusEnum theStatus, BasePersServiceVersion nextVersion) {
		StatusEnum status = theStatus;

		for (PersServiceVersionUrl nextUrl : nextVersion.getUrls()) {
			PersServiceVersionUrlStatus nextUrlStatus = nextUrl.getStatus();
			switch (nextUrlStatus.getStatus()) {
			case ACTIVE:
				status = StatusEnum.ACTIVE;
				theDashboardObject.setUrlsActive(theDashboardObject.getUrlsActive() + 1);
				break;
			case DOWN:
				if (status != StatusEnum.ACTIVE) {
					status = StatusEnum.DOWN;
				}
				theDashboardObject.setUrlsDown(theDashboardObject.getUrlsDown() + 1);
				break;
			case UNKNOWN:
				theDashboardObject.setUrlsUnknown(theDashboardObject.getUrlsUnknown() + 1);
				break;
			}

		} // end URL

		for (PersServiceVersionMethod nextMethod : nextVersion.getMethods()) {
			extractStatus(the60MinInvCount, the60minTime, nextMethod);

		}
		return status;
	}

	private StatusEnum extractStatus(BaseGDashboardObjectWithUrls<?> theDashboardObject, StatusEnum theInitialStatus, int[] the60MinInvCount, long[] the60minTime, PersService theService) {

		// Value will be changed below
		StatusEnum status = theInitialStatus;

		for (BasePersServiceVersion nextVersion : theService.getVersions()) {
			status = extractStatus(theDashboardObject, the60MinInvCount, the60minTime, status, nextVersion);

		} // end VERSION
		return status;
	}

	private void extractStatus(int[] the60MinInvCount, long[] the60minTime, PersServiceVersionMethod nextMethod) {
		Date date60MinsAgo = new Date(System.currentTimeMillis() - (60 * DateUtils.MILLIS_PER_MINUTE));
		Date date = DateUtils.truncate(date60MinsAgo, Calendar.MINUTE);
		for (int min = 0; min <= 59; min++, date = new Date(date.getTime() + DateUtils.MILLIS_PER_MINUTE)) {
			PersInvocationStatsPk pk = new PersInvocationStatsPk(InvocationStatsIntervalEnum.MINUTE, date, nextMethod);
			BasePersInvocationStats stats = myStatusSvc.getOrCreateInvocationStatsSynchronously(pk);
			the60MinInvCount[min] = addToInt(the60MinInvCount[min], stats.getSuccessInvocationCount());
			the60minTime[min] = the60minTime[min] + stats.getSuccessInvocationTotalTime();
		}
	}

	private PersBaseClientAuth<?> fromUi(BaseGClientSecurity theObj, BasePersServiceVersion theServiceVersion) {
		switch (theObj.getType()) {
		case WSSEC_UT:
			GWsSecUsernameTokenClientSecurity obj = (GWsSecUsernameTokenClientSecurity) theObj;
			PersWsSecUsernameTokenClientAuth retVal = new PersWsSecUsernameTokenClientAuth();
			retVal.setPid(obj.getPidOrNull());
			retVal.setUsername(obj.getUsername());
			retVal.setPassword(obj.getPassword());
			retVal.setServiceVersion(theServiceVersion);
			return retVal;
		}
		return null;
	}

	private PersBaseServerAuth<?, ?> fromUi(BaseGServerSecurity theObj, PersServiceVersionSoap11 theSvcVer) {
		switch (theObj.getType()) {
		case WSSEC_UT:
			PersWsSecUsernameTokenServerAuth retVal = new PersWsSecUsernameTokenServerAuth();
			retVal.setAuthenticationHost(myPersSvc.getAuthenticationHostByPid(theObj.getAuthHostPid()));
			retVal.setServiceVersion(theSvcVer);
			return retVal;
		}
		return null;
	}

	private PersHttpClientConfig fromUi(GHttpClientConfig theConfig) {
		PersHttpClientConfig retVal = new PersHttpClientConfig();

		if (theConfig.getPid() > 0) {
			retVal.setPid(theConfig.getPid());
		}

		retVal.setId(theConfig.getId());
		retVal.setName(theConfig.getName());
		retVal.setCircuitBreakerEnabled(theConfig.isCircuitBreakerEnabled());
		retVal.setCircuitBreakerTimeBetweenResetAttempts(theConfig.getCircuitBreakerTimeBetweenResetAttempts());
		retVal.setConnectTimeoutMillis(theConfig.getConnectTimeoutMillis());
		retVal.setFailureRetriesBeforeAborting(theConfig.getFailureRetriesBeforeAborting());
		retVal.setReadTimeoutMillis(theConfig.getReadTimeoutMillis());
		retVal.setUrlSelectionPolicy(theConfig.getUrlSelectionPolicy());

		return retVal;
	}

	private PersServiceVersionResource fromUi(GResource theRes, BasePersServiceVersion theServiceVersion) {
		PersServiceVersionResource retVal = new PersServiceVersionResource();
		retVal.setResourceContentType(theRes.getContentType());
		retVal.setResourceText(theRes.getText());
		retVal.setResourceUrl(theRes.getUrl());
		retVal.setServiceVersion(theServiceVersion);
		return retVal;
	}

	private PersServiceVersionMethod fromUi(GServiceMethod theMethod, long theServiceVersionPid) {
		PersServiceVersionMethod retVal = new PersServiceVersionMethod();
		retVal.setName(theMethod.getName());
		retVal.setPid(theMethod.getPidOrNull());
		retVal.setServiceVersion(myPersSvc.getServiceVersionByPid(theServiceVersionPid));
		return retVal;
	}

	private PersServiceVersionUrl fromUi(GServiceVersionUrl theUrl, BasePersServiceVersion theServiceVersion) {
		PersServiceVersionUrl retVal = new PersServiceVersionUrl();
		retVal.setUrlId(theUrl.getId());
		retVal.setUrl(theUrl.getUrl());
		retVal.setServiceVersion(theServiceVersion);
		return retVal;
	}

	private PersUser fromUi(GUser thePersUser) throws ProcessingException {
		PersUser retVal = new PersUser();
		retVal.setPid(thePersUser.getPidOrNull());
		retVal.setAllowAllDomains(thePersUser.isAllowAllDomains());
		retVal.setPermissions(thePersUser.getGlobalPermissions());
		retVal.setUsername(thePersUser.getUsername());
		retVal.setDomainPermissions(fromUi(thePersUser.getDomainPermissions()));
		retVal.setAuthenticationHost(myPersSvc.getAuthenticationHostByPid(thePersUser.getAuthHostPid()));

		if (thePersUser.getPidOrNull() != null && thePersUser.getChangePassword() == null) {
			PersUser existing = myPersSvc.getUser(thePersUser.getPid());
			if (StringUtils.isNotBlank(existing.getPasswordHash())) {
				retVal.setPasswordHash(existing.getPasswordHash());
			}
		} else if (thePersUser.getChangePassword() != null) {
			ourLog.info("Changing password for user {}", thePersUser.getPidOrNull());
			retVal.setPassword(thePersUser.getChangePassword());
		}

		return retVal;
	}

	private PersUserDomainPermission fromUi(GUserDomainPermission theObj) {
		PersUserDomainPermission retVal = new PersUserDomainPermission();
		retVal.setPid(theObj.getPidOrNull());
		retVal.setAllowAllServices(theObj.isAllowAllServices());
		retVal.setServiceDomain(myPersSvc.getDomainByPid(theObj.getDomainPid()));
		retVal.setServicePermissions(new ArrayList<PersUserServicePermission>());
		for (GUserServicePermission next : theObj.getServicePermissions()) {
			retVal.addServicePermission(fromUi(next));
		}
		return retVal;
	}

	private PersUserServicePermission fromUi(GUserServicePermission theObj) {
		PersUserServicePermission retVal = new PersUserServicePermission();
		retVal.setPid(theObj.getPidOrNull());
		retVal.setAllowAllServiceVersions(theObj.isAllowAllServiceVersions());
		retVal.setService(myPersSvc.getServiceByPid(theObj.getServicePid()));
		retVal.setServiceVersionPermissions(new ArrayList<PersUserServiceVersionPermission>());
		for (GUserServiceVersionPermission next : theObj.getServiceVersionPermissions()) {
			retVal.addServiceVersionPermission(fromUi(next));
		}
		return retVal;
	}

	private PersUserServiceVersionMethodPermission fromUi(GUserServiceVersionMethodPermission theObj) {
		PersUserServiceVersionMethodPermission retVal = new PersUserServiceVersionMethodPermission();
		retVal.setPid(theObj.getPidOrNull());
		retVal.setServiceVersionMethod(myPersSvc.getServiceVersionMethodByPid(theObj.getServiceVersionMethodPid()));
		retVal.setAllow(theObj.isAllow());
		return retVal;
	}

	private PersUserServiceVersionPermission fromUi(GUserServiceVersionPermission theObj) {
		PersUserServiceVersionPermission retVal = new PersUserServiceVersionPermission();
		retVal.setPid(theObj.getPidOrNull());
		retVal.setAllowAllServiceVersionMethods(theObj.isAllowAllServiceVersionMethods());
		retVal.setServiceVersion(myPersSvc.getServiceVersionByPid(theObj.getServiceVersionPid()));
		retVal.setServiceVersionMethodPermissions(new ArrayList<PersUserServiceVersionMethodPermission>());
		for (GUserServiceVersionMethodPermission next : theObj.getServiceVersionMethodPermissions()) {
			retVal.addServiceVersionMethodPermissions(fromUi(next));
		}
		return retVal;
	}

	private Collection<PersUserDomainPermission> fromUi(List<GUserDomainPermission> theDomainPermissions) {
		Collection<PersUserDomainPermission> retVal = new ArrayList<PersUserDomainPermission>();
		for (GUserDomainPermission next : theDomainPermissions) {
			retVal.add(fromUi(next));
		}
		return retVal;
	}

	private void fromUi(PersAuthenticationHostLocalDatabase theHost, GLocalDatabaseAuthHost theAuthHost) {

		if (theAuthHost.getPid() <= 0) {
			theHost.setPid(null);
		} else {
			theHost.setPid(theAuthHost.getPid());
		}

		theHost.setAutoCreateAuthorizedUsers(theAuthHost.isAutoCreateAuthorizedUsers());
		theHost.setCacheSuccessfulCredentialsForMillis(theAuthHost.getCacheSuccessesForMillis());
		theHost.setModuleId(theAuthHost.getModuleId());
		theHost.setModuleName(theAuthHost.getModuleName());

	}

	private PersServiceVersionSoap11 fromUi(PersServiceVersionSoap11 thePersVersion, GSoap11ServiceVersion theVersion) throws ProcessingException {
		thePersVersion.setActive(thePersVersion.isActive());
		thePersVersion.setVersionId(theVersion.getId());
		thePersVersion.setWsdlUrl(theVersion.getWsdlLocation());

		PersHttpClientConfig httpClientConfig = myPersSvc.getHttpClientConfig(theVersion.getHttpClientConfigPid());
		if (httpClientConfig == null) {
			throw new ProcessingException("Unknown HTTP client config PID: " + theVersion.getHttpClientConfigPid());
		}
		thePersVersion.setHttpClientConfig(httpClientConfig);
		return thePersVersion;
	}

	private GAuthenticationHostList loadAuthHostList() {
		GAuthenticationHostList retVal = new GAuthenticationHostList();
		for (BasePersAuthenticationHost next : myPersSvc.getAllAuthenticationHosts()) {
			GLocalDatabaseAuthHost uiObject = toUi(next);
			retVal.add(uiObject);
		}
		return retVal;
	}

	private GDomainList loadDomainList(Set<Long> theLoadDomStats, Set<Long> theLoadSvcStats, Set<Long> theLoadVerStats, Set<Long> theLoadVerMethodStats) throws ProcessingException {
		GDomainList domainList = new GDomainList();

		for (PersDomain nextDomain : myPersSvc.getAllDomains()) {
			GDomain gDomain = loadDomain(nextDomain, theLoadDomStats, theLoadSvcStats, theLoadVerStats, theLoadVerMethodStats);

			domainList.add(gDomain);
		} // for domains
		return domainList;
	}

	private GDomain loadDomain(PersDomain nextDomain, Set<Long> theLoadDomStats, Set<Long> theLoadSvcStats, Set<Long> theLoadVerStats, Set<Long> theLoadVerMethodStats) throws ProcessingException {
		GDomain gDomain = toUi(nextDomain, theLoadDomStats.contains(nextDomain.getPid()));

		for (PersService nextService : nextDomain.getServices()) {
			GService gService = toUi(nextService, theLoadSvcStats.contains(nextService.getPid()));
			gDomain.getServiceList().add(gService);

			for (BasePersServiceVersion nextVersion : nextService.getVersions()) {
				BaseGServiceVersion gVersion = toUi(nextVersion, theLoadVerStats.contains(nextVersion.getPid()));
				gService.getVersionList().add(gVersion);

				for (PersServiceVersionMethod nextMethod : nextVersion.getMethods()) {
					GServiceMethod gMethod = toUi(nextMethod, theLoadVerMethodStats.contains(nextMethod.getPid()));
					gVersion.getMethodList().add(gMethod);
				} // for methods

				for (PersServiceVersionUrl nextUrl : nextVersion.getUrls()) {
					GServiceVersionUrl gUrl = toUi(nextUrl);
					gVersion.getUrlList().add(gUrl);
				} // for URLs

				for (PersServiceVersionResource nextResource : nextVersion.getUriToResource().values()) {
					GServiceVersionResourcePointer gResource = toUi(nextResource);
					gVersion.getResourcePointerList().add(gResource);
				} // for resources

				for (PersBaseServerAuth<?, ?> nextServerAuth : nextVersion.getServerAuths()) {
					BaseGServerSecurity gServerAuth = toUi(nextServerAuth);
					gVersion.getServerSecurityList().add(gServerAuth);
				} // server auths

				for (PersBaseClientAuth<?> nextClientAuth : nextVersion.getClientAuths()) {
					BaseGClientSecurity gClientAuth = toUi(nextClientAuth);
					gVersion.getClientSecurityList().add(gClientAuth);
				} // Client auths

			} // for service versions
		} // for services
		return gDomain;
	}

	private GHttpClientConfigList loadHttpClientConfigList() {
		GHttpClientConfigList configList = new GHttpClientConfigList();
		for (PersHttpClientConfig next : myPersSvc.getHttpClientConfigs()) {
			configList.add(toUi(next));
		}
		return configList;
	}

	private GUserList loadUserList() {
		GUserList retVal = new GUserList();
		Collection<PersUser> users = myPersSvc.getAllServiceUsers();
		for (PersUser persUser : users) {
			retVal.add(toUi(persUser));
		}
		return retVal;
	}

	private int[] toLatency(int[] theCounts, long[] theTimes) {
		assert theCounts.length == theTimes.length;

		int[] retVal = new int[theCounts.length];
		int prevValue = -1;
		for (int i = 0; i < theCounts.length; i++) {
			if (theCounts[i] > 0) {
				retVal[i] = (int) Math.min(theTimes[i] / theCounts[i], Integer.MAX_VALUE);
				prevValue = retVal[i];
			} else if (prevValue > -1) {
				retVal[i] = prevValue;
			}
		}

		return retVal;
	}

	private GSoap11ServiceVersionAndResources toUi(BaseGServiceVersion theUiService, BasePersServiceVersion theSvcVer) throws ProcessingException {
		GSoap11ServiceVersionAndResources retVal = new GSoap11ServiceVersionAndResources();

		retVal.setServiceVersion((GSoap11ServiceVersion) theUiService);

		theUiService.getMethodList().clear();
		for (PersServiceVersionMethod next : theSvcVer.getMethods()) {
			retVal.getServiceVersion().getMethodList().add(toUi(next, false));
		}

		theUiService.getResourcePointerList().clear();
		for (PersServiceVersionResource next : theSvcVer.getUriToResource().values()) {
			GResource res = new GResource();
			if (next.getPid() != null) {
				res.setPid(next.getPid());
			}
			res.setText(next.getResourceText());
			res.setUrl(next.getResourceUrl());
			res.setContentType(next.getResourceContentType());
			retVal.getResource().add(res);
			theUiService.getResourcePointerList().add(res.asPointer());
		}

		theUiService.getUrlList().clear();
		for (PersServiceVersionUrl next : theSvcVer.getUrls()) {
			theUiService.getUrlList().add(toUi(next));
		}

		theUiService.getClientSecurityList().clear();
		for (PersBaseClientAuth<?> next : theSvcVer.getClientAuths()) {
			theUiService.getClientSecurityList().add(toUi(next));
		}

		theUiService.getServerSecurityList().clear();
		for (PersBaseServerAuth<?, ?> next : theSvcVer.getServerAuths()) {
			theUiService.getServerSecurityList().add(toUi(next));
		}

		return retVal;
	}

	private GLocalDatabaseAuthHost toUi(BasePersAuthenticationHost next) {
		GLocalDatabaseAuthHost uiObject = null;
		switch (next.getType()) {
		case LOCAL_DATABASE:
			uiObject = toUi((PersAuthenticationHostLocalDatabase) next);
			break;
		}
		return uiObject;
	}

	private BaseGServiceVersion toUi(BasePersServiceVersion theVersion, boolean theLoadStats) throws ProcessingException {
		BaseGServiceVersion retVal = null;
		switch (theVersion.getProtocol()) {
		case SOAP11:
			PersServiceVersionSoap11 persSoap11 = (PersServiceVersionSoap11) theVersion;
			GSoap11ServiceVersion soap11RetVal = new GSoap11ServiceVersion();
			soap11RetVal.setWsdlLocation(persSoap11.getWsdlUrl());
			retVal = soap11RetVal;
			break;
		}

		if (retVal == null) {
			throw new ProcessingException("Don't know how to handle service of type " + theVersion.getProtocol());
		}

		retVal.setPid(theVersion.getPid());
		retVal.setId(theVersion.getVersionId());
		retVal.setName(theVersion.getVersionId());
		retVal.setServerSecured(theVersion.getServerSecured());
		retVal.setProxyPath(theVersion.getProxyPath());

		PersHttpClientConfig httpClientConfig = theVersion.getHttpClientConfig();
		if (httpClientConfig == null) {
			throw new ProcessingException("Service version doesn't have an HTTP client config");
		}
		retVal.setHttpClientConfigPid(httpClientConfig.getPid());

		if (theLoadStats) {
			retVal.setStatsInitialized(true);
			int[] t60minCount = new int[60];
			long[] t60minTime = new long[60];

			StatusEnum status = StatusEnum.UNKNOWN;
			extractStatus(retVal, t60minCount, t60minTime, status, theVersion);

			retVal.setTransactions60mins(t60minCount);
			retVal.setLatency60mins(toLatency(t60minCount, t60minTime));

			int urlsActive = 0;
			int urlsDown = 0;
			int urlsUnknown = 0;
			for (PersServiceVersionUrl nextUrl : theVersion.getUrls()) {
				switch (nextUrl.getStatus().getStatus()) {
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
			retVal.setUrlsActive(urlsActive);
			retVal.setUrlsDown(urlsDown);
			retVal.setUrlsUnknown(urlsUnknown);
			retVal.setLastServerSecurityFailure(theVersion.getStatus().getLastServerSecurityFailure());
			retVal.setLastSuccessfulInvocation(theVersion.getStatus().getLastSuccessfulInvocation());

			if (urlsDown > 0) {
				retVal.setStatus(net.svcret.admin.shared.model.StatusEnum.DOWN);
			} else if (urlsActive > 0) {
				retVal.setStatus(net.svcret.admin.shared.model.StatusEnum.ACTIVE);
			} else {
				retVal.setStatus(net.svcret.admin.shared.model.StatusEnum.UNKNOWN);
			}
		}

		return retVal;
	}

	private List<GUserDomainPermission> toUi(Collection<PersUserDomainPermission> theDomainPermissions) {
		List<GUserDomainPermission> retVal = new ArrayList<GUserDomainPermission>();
		for (PersUserDomainPermission next : theDomainPermissions) {
			retVal.add(toUi(next));
		}
		return retVal;
	}

	private GLocalDatabaseAuthHost toUi(PersAuthenticationHostLocalDatabase theObj) {
		GLocalDatabaseAuthHost retVal = new GLocalDatabaseAuthHost();
		retVal.setPid(theObj.getPid());
		retVal.setAutoCreateAuthorizedUsers(theObj.isAutoCreateAuthorizedUsers());
		retVal.setCacheSuccessesForMillis(theObj.getCacheSuccessfulCredentialsForMillis());
		retVal.setModuleId(theObj.getModuleId());
		retVal.setModuleName(theObj.getModuleName());
		retVal.setSupportsPasswordChange(theObj.isSupportsPasswordChange());
		return retVal;
	}

	private BaseGClientSecurity toUi(PersBaseClientAuth<?> theAuth) throws ProcessingException {
		BaseGClientSecurity retVal = null;

		switch (theAuth.getAuthType()) {
		case WS_SECURITY_USERNAME_TOKEN:
			PersWsSecUsernameTokenClientAuth obj = (PersWsSecUsernameTokenClientAuth) theAuth;
			GWsSecUsernameTokenClientSecurity auth = new GWsSecUsernameTokenClientSecurity();
			auth.setUsername(obj.getUsername());
			auth.setPassword(obj.getPassword());
			retVal = auth;
			break;
		}

		if (retVal == null) {
			throw new ProcessingException("Unknown auth type; " + theAuth.getAuthType());
		}

		retVal.setPid(theAuth.getPid());

		return retVal;
	}

	// private GHttpClientConfig toUi(PersHttpClientConfig theConfig) {
	// GHttpClientConfig retVal = new GHttpClientConfig();
	//
	// if (theConfig.getPid() > 0) {
	// retVal.setPid(theConfig.getPid());
	// }
	//
	// retVal.setId(theConfig.getId());
	// retVal.setName(theConfig.getName());
	// retVal.setCircuitBreakerEnabled(theConfig.isCircuitBreakerEnabled());
	// retVal.setCircuitBreakerTimeBetweenResetAttempts(theConfig.getCircuitBreakerTimeBetweenResetAttempts());
	// retVal.setConnectTimeoutMillis(theConfig.getConnectTimeoutMillis());
	// retVal.setFailureRetriesBeforeAborting(theConfig.getFailureRetriesBeforeAborting());
	// retVal.setReadTimeoutMillis(theConfig.getReadTimeoutMillis());
	// retVal.setUrlSelectionPolicy(theConfig.getUrlSelectionPolicy());
	//
	// return retVal;
	// }

	private BaseGServerSecurity toUi(PersBaseServerAuth<?, ?> theAuth) throws ProcessingException {
		BaseGServerSecurity retVal = null;

		switch (theAuth.getAuthType()) {
		case WS_SECURITY_USERNAME_TOKEN:
			GWsSecServerSecurity auth = new GWsSecServerSecurity();
			retVal = auth;
			break;
		}

		if (retVal == null) {
			throw new ProcessingException("Unknown auth type; " + theAuth.getAuthType());
		}

		retVal.setPid(theAuth.getPid());
		retVal.setAuthHostPid(theAuth.getAuthenticationHost().getPid());

		return retVal;
	}

	private GDomain toUi(PersDomain theDomain, boolean theLoadStats) {
		GDomain retVal = new GDomain();
		retVal.setPid(theDomain.getPid());
		retVal.setId(theDomain.getDomainId());
		retVal.setName(theDomain.getDomainName());
		retVal.setServerSecured(theDomain.getServerSecured());

		if (theLoadStats) {
			retVal.setStatsInitialized(true);
			StatusEnum status = StatusEnum.UNKNOWN;
			int[] t60minCount = new int[60];
			long[] t60minTime = new long[60];

			for (PersService nextService : theDomain.getServices()) {
				status = extractStatus(retVal, status, t60minCount, t60minTime, nextService);
			}

			retVal.setTransactions60mins(t60minCount);
			retVal.setLatency60mins(toLatency(t60minCount, t60minTime));
			retVal.setStatus(net.svcret.admin.shared.model.StatusEnum.valueOf(status.name()));

			int urlsActive = 0;
			int urlsDown = 0;
			int urlsUnknown = 0;
			Date lastServerSecurityFail = null;
			Date lastSuccess = null;
			for (PersService nextService : theDomain.getServices()) {
				for (BasePersServiceVersion nextVersion : nextService.getVersions()) {
					for (PersServiceVersionUrl nextUrl : nextVersion.getUrls()) {
						switch (nextUrl.getStatus().getStatus()) {
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

					PersServiceVersionStatus svcStatus = nextVersion.getStatus();
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
		// retVal.get

		return retVal;
	}

	private GHttpClientConfig toUi(PersHttpClientConfig theConfig) {
		GHttpClientConfig retVal = new GHttpClientConfig();

		retVal.setPid(theConfig.getPid());
		retVal.setId(theConfig.getId());
		retVal.setName(theConfig.getName());

		retVal.setCircuitBreakerEnabled(theConfig.isCircuitBreakerEnabled());
		retVal.setCircuitBreakerTimeBetweenResetAttempts(theConfig.getCircuitBreakerTimeBetweenResetAttempts());

		retVal.setConnectTimeoutMillis(theConfig.getConnectTimeoutMillis());
		retVal.setReadTimeoutMillis(theConfig.getReadTimeoutMillis());

		retVal.setFailureRetriesBeforeAborting(theConfig.getFailureRetriesBeforeAborting());

		retVal.setUrlSelectionPolicy(theConfig.getUrlSelectionPolicy());

		return retVal;
	}

	private GService toUi(PersService theService, boolean theLoadStats) {
		GService retVal = new GService();
		retVal.setPid(theService.getPid());
		retVal.setId(theService.getServiceId());
		retVal.setName(theService.getServiceName());
		retVal.setActive(theService.isActive());
		retVal.setServerSecured(theService.getServerSecured());

		if (theLoadStats) {
			retVal.setStatsInitialized(true);
			StatusEnum status = StatusEnum.UNKNOWN;
			int[] t60minCount = new int[60];
			long[] t60minTime = new long[60];

			status = extractStatus(retVal, status, t60minCount, t60minTime, theService);

			retVal.setTransactions60mins(t60minCount);
			retVal.setLatency60mins(toLatency(t60minCount, t60minTime));
			retVal.setStatus(net.svcret.admin.shared.model.StatusEnum.valueOf(status.name()));

			int urlsActive = 0;
			int urlsDown = 0;
			int urlsUnknown = 0;
			Date lastServerSecurityFail = null;
			Date lastSuccess = null;
			for (BasePersServiceVersion nextVersion : theService.getVersions()) {
				for (PersServiceVersionUrl nextUrl : nextVersion.getUrls()) {
					switch (nextUrl.getStatus().getStatus()) {
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

				PersServiceVersionStatus svcStatus = nextVersion.getStatus();
				lastServerSecurityFail = PersServiceVersionStatus.newer(lastServerSecurityFail, svcStatus.getLastServerSecurityFailure());
				lastSuccess = PersServiceVersionStatus.newer(lastSuccess, svcStatus.getLastSuccessfulInvocation());

			}

			retVal.setUrlsActive(urlsActive);
			retVal.setUrlsDown(urlsDown);
			retVal.setUrlsUnknown(urlsUnknown);
			retVal.setLastServerSecurityFailure(lastServerSecurityFail);
			retVal.setLastSuccessfulInvocation(lastSuccess);

		}

		return retVal;
	}

	private GServiceMethod toUi(PersServiceVersionMethod theMethod, boolean theLoadStats) {
		GServiceMethod retVal = new GServiceMethod();
		if (theMethod.getPid() != null) {
			retVal.setPid(theMethod.getPid());
		}
		retVal.setId(theMethod.getName());
		retVal.setName(theMethod.getName());

		if (theLoadStats) {
			retVal.setStatsInitialized(true);
			StatusEnum status = StatusEnum.UNKNOWN;
			int[] t60minCount = new int[60];
			long[] t60minTime = new long[60];

			extractStatus(t60minCount, t60minTime, theMethod);

			retVal.setTransactions60mins(t60minCount);
			retVal.setLatency60mins(toLatency(t60minCount, t60minTime));
			retVal.setStatus(net.svcret.admin.shared.model.StatusEnum.valueOf(status.name()));

		}

		return retVal;
	}

	private GServiceVersionResourcePointer toUi(PersServiceVersionResource theResource) {
		GServiceVersionResourcePointer retVal = new GServiceVersionResourcePointer();
		retVal.setPid(theResource.getPid());
		retVal.setSize(theResource.getResourceText().length());
		retVal.setType(theResource.getResourceContentType());
		retVal.setUrl(theResource.getResourceUrl());
		return retVal;
	}

	private GServiceVersionUrl toUi(PersServiceVersionUrl theUrl) {
		GServiceVersionUrl retVal = new GServiceVersionUrl();
		if (theUrl.getPid() != null) {
			retVal.setPid(theUrl.getPid());
		}
		retVal.setId(theUrl.getUrlId());
		retVal.setUrl(theUrl.getUrl());
		return retVal;
	}

	private GUser toUi(PersUser thePersUser) {
		GUser retVal = new GUser();
		retVal.setPid(thePersUser.getPid());
		retVal.setAllowAllDomains(thePersUser.isAllowAllDomains());
		retVal.setGlobalPermissions(thePersUser.getPermissions());
		retVal.setUsername(thePersUser.getUsername());
		retVal.setDomainPermissions(toUi(thePersUser.getDomainPermissions()));
		retVal.setAuthHostPid(thePersUser.getAuthenticationHost().getPid());
		return retVal;
	}

	private GUserDomainPermission toUi(PersUserDomainPermission theObj) {
		GUserDomainPermission retVal = new GUserDomainPermission();
		retVal.setPid(theObj.getPid());
		retVal.setAllowAllServices(theObj.isAllowAllServices());
		retVal.setDomainPid(theObj.getServiceDomain().getPid());
		retVal.setServicePermissions(new ArrayList<GUserServicePermission>());
		for (PersUserServicePermission next : theObj.getServicePermissions()) {
			retVal.getServicePermissions().add(toUi(next));
		}
		return retVal;
	}

	private GUserServicePermission toUi(PersUserServicePermission theObj) {
		GUserServicePermission retVal = new GUserServicePermission();
		retVal.setPid(theObj.getPid());
		retVal.setAllowAllServiceVersions(theObj.isAllowAllServiceVersions());
		retVal.setServicePid(theObj.getService().getPid());
		retVal.setServiceVersionPermissions(new ArrayList<GUserServiceVersionPermission>());
		for (PersUserServiceVersionPermission next : theObj.getServiceVersionPermissions()) {
			retVal.getServiceVersionPermissions().add(toUi(next));
		}
		return retVal;
	}

	private GUserServiceVersionMethodPermission toUi(PersUserServiceVersionMethodPermission theObj) {
		GUserServiceVersionMethodPermission retVal = new GUserServiceVersionMethodPermission();
		retVal.setPid(theObj.getPid());
		retVal.setServiceVersionMethodPid(theObj.getServiceVersionMethod().getPid());
		retVal.setAllow(theObj.isAllow());
		return retVal;
	}

	private GUserServiceVersionPermission toUi(PersUserServiceVersionPermission theObj) {
		GUserServiceVersionPermission retVal = new GUserServiceVersionPermission();
		retVal.setPid(theObj.getPid());
		retVal.setAllowAllServiceVersionMethods(theObj.isAllowAllServiceVersionMethods());
		retVal.setServiceVersionPid(theObj.getServiceVersion().getPid());
		retVal.setServiceVersionMethodPermissions(new ArrayList<GUserServiceVersionMethodPermission>());
		for (PersUserServiceVersionMethodPermission next : theObj.getServiceVersionMethodPermissions()) {
			retVal.getServiceVersionMethodPermissions().add(toUi(next));
		}
		return retVal;
	}

	/**
	 * Unit test only
	 */
	void setInvokerSoap11(IServiceInvoker<PersServiceVersionSoap11> theInvokerSoap11) {
		myInvokerSoap11 = theInvokerSoap11;
	}

	void setPersSvc(DaoBean thePersSvc) {
		assert myPersSvc == null;
		myPersSvc = thePersSvc;
	}

	/**
	 * FOR UNIT TESTS ONLY
	 */
	void setSecuritySvc(SecurityServiceBean theSecSvc) {
		mySecurityService = theSecSvc;
	}

	/**
	 * FOR UNIT TESTS ONLY
	 */
	void setServiceRegistry(ServiceRegistryBean theSvcReg) {
		myServiceRegistry = theSvcReg;
	}

	@Override
	public GDomain saveDomain(GDomain theDomain) throws ProcessingException {
		ourLog.info("Saving domain with PID {}", theDomain.getPid());

		PersDomain domain = myPersSvc.getDomainByPid(theDomain.getPid());
		PersDomain newDomain = fromUi(theDomain);
		domain.merge(newDomain);

		domain = myServiceRegistry.saveDomain(domain);

		return getDomainByPid(domain.getPid());
	}

	private PersDomain fromUi(GDomain theDomain) {
		PersDomain retVal = new PersDomain();
		retVal.setPid(theDomain.getPidOrNull());
		retVal.setDomainId(theDomain.getId());
		retVal.setDomainName(theDomain.getName());
		return retVal;
	}

	/**
	 * Convenience for Unit Tests
	 */
	GDomain addDomain(String theId, String theName) throws ProcessingException {
		GDomain domain = new GDomain();
		domain.setId(theId);
		domain.setName(theName);
		GDomain retVal = addDomain(domain);
		return retVal;
	}

	@Override
	public GDomainList deleteService(long theServicePid) throws ProcessingException {
		ourLog.info("Deleting service {}", theServicePid);
		
		PersService service = myPersSvc.getServiceByPid(theServicePid);
		if (service==null) {
			throw new ProcessingException("Unknown service PID "+theServicePid);
		}
		
		myPersSvc.deleteService(service);
		
		return loadDomainList();
	}

	@Override
	public GDomainList saveService(GService theService) throws ProcessingException {
		ourLog.info("Saving service {}", theService.getPid());
		
		PersService service = myPersSvc.getServiceByPid(theService.getPid());
		if (service==null) {
			throw new ProcessingException("Unknown service PID "+theService.getPid());
		}
		
		PersService newService = fromUi(theService);
		service.merge(newService);
		myPersSvc.saveService(newService);
		
		return loadDomainList();
	}

	private PersService fromUi(GService theService) {
		PersService retVal = new PersService();
		retVal.setActive(theService.isActive());
		retVal.setServiceId(theService.getId());
		retVal.setServiceName(theService.getName());
		return retVal;
	}

	@EJB
	private IConfigService myConfigSvc;
	
	@Override
	public GConfig loadConfig() throws ProcessingException {
		return toUi(myConfigSvc.getConfig());
	}

	private GConfig toUi(PersConfig theConfig) {
		GConfig retVal = new GConfig();
		
		for (PersConfigProxyUrlBase next : theConfig.getProxyUrlBases()) {
			retVal.getProxyUrlBases().add(next.getUrlBase());
		}
		
		return retVal;
	}

	// private GDomain toUi(PersDomain theDomain) {
	// GDomain retVal=new GDomain();
	// retVal.setPid(theDomain.getPid());
	// retVal.setId(theDomain.getDomainId());
	// retVal.setName(theDomain.getDomainName());
	// return retVal;
	// }

}
