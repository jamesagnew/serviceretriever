package net.svcret.ejb.ejb;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
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
import net.svcret.admin.shared.model.GHttpBasicAuthServerSecurity;
import net.svcret.admin.shared.model.GHttpClientConfig;
import net.svcret.admin.shared.model.GHttpClientConfigList;
import net.svcret.admin.shared.model.GLdapAuthHost;
import net.svcret.admin.shared.model.GLocalDatabaseAuthHost;
import net.svcret.admin.shared.model.GNamedParameterJsonRpcServerAuth;
import net.svcret.admin.shared.model.GPartialUserList;
import net.svcret.admin.shared.model.GRecentMessage;
import net.svcret.admin.shared.model.GRecentMessageLists;
import net.svcret.admin.shared.model.GResource;
import net.svcret.admin.shared.model.GService;
import net.svcret.admin.shared.model.GServiceMethod;
import net.svcret.admin.shared.model.GServiceVersionDetailedStats;
import net.svcret.admin.shared.model.GServiceVersionJsonRpc20;
import net.svcret.admin.shared.model.GServiceVersionResourcePointer;
import net.svcret.admin.shared.model.GServiceVersionUrl;
import net.svcret.admin.shared.model.GSoap11ServiceVersion;
import net.svcret.admin.shared.model.GSoap11ServiceVersionAndResources;
import net.svcret.admin.shared.model.GUrlStatus;
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
import net.svcret.admin.shared.model.TimeRange;
import net.svcret.ejb.api.IAdminService;
import net.svcret.ejb.api.IConfigService;
import net.svcret.ejb.api.IDao;
import net.svcret.ejb.api.IRuntimeStatus;
import net.svcret.ejb.api.ISecurityService;
import net.svcret.ejb.api.IServiceInvoker;
import net.svcret.ejb.api.IServiceInvokerSoap11;
import net.svcret.ejb.api.IServiceRegistry;
import net.svcret.ejb.api.ResponseTypeEnum;
import net.svcret.ejb.ex.ProcessingException;
import net.svcret.ejb.model.entity.BasePersAuthenticationHost;
import net.svcret.ejb.model.entity.BasePersInvocationStats;
import net.svcret.ejb.model.entity.BasePersRecentMessage;
import net.svcret.ejb.model.entity.BasePersServiceVersion;
import net.svcret.ejb.model.entity.InvocationStatsIntervalEnum;
import net.svcret.ejb.model.entity.PersAuthenticationHostLdap;
import net.svcret.ejb.model.entity.PersAuthenticationHostLocalDatabase;
import net.svcret.ejb.model.entity.PersBaseClientAuth;
import net.svcret.ejb.model.entity.PersBaseServerAuth;
import net.svcret.ejb.model.entity.PersConfig;
import net.svcret.ejb.model.entity.PersConfigProxyUrlBase;
import net.svcret.ejb.model.entity.PersDomain;
import net.svcret.ejb.model.entity.PersHttpClientConfig;
import net.svcret.ejb.model.entity.PersInvocationStatsPk;
import net.svcret.ejb.model.entity.PersInvocationUserStatsPk;
import net.svcret.ejb.model.entity.PersService;
import net.svcret.ejb.model.entity.PersServiceVersionMethod;
import net.svcret.ejb.model.entity.PersServiceVersionRecentMessage;
import net.svcret.ejb.model.entity.PersServiceVersionResource;
import net.svcret.ejb.model.entity.PersServiceVersionStatus;
import net.svcret.ejb.model.entity.PersServiceVersionUrl;
import net.svcret.ejb.model.entity.PersServiceVersionUrlStatus;
import net.svcret.ejb.model.entity.PersUser;
import net.svcret.ejb.model.entity.PersUserDomainPermission;
import net.svcret.ejb.model.entity.PersUserRecentMessage;
import net.svcret.ejb.model.entity.PersUserServicePermission;
import net.svcret.ejb.model.entity.PersUserServiceVersionMethodPermission;
import net.svcret.ejb.model.entity.PersUserServiceVersionPermission;
import net.svcret.ejb.model.entity.PersUserStatus;
import net.svcret.ejb.model.entity.http.PersHttpBasicServerAuth;
import net.svcret.ejb.model.entity.jsonrpc.NamedParameterJsonRpcServerAuth;
import net.svcret.ejb.model.entity.jsonrpc.PersServiceVersionJsonRpc20;
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

	@EJB
	private IConfigService myConfigSvc;

	@EJB()
	private IServiceInvokerSoap11 myInvokerSoap11;

	@EJB
	private IDao myDao;

	@EJB
	private ISecurityService mySecurityService;

	@EJB
	private IServiceRegistry myServiceRegistry;

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

		ourLog.info("Adding service with ID[{}] and NAME[{}] to domain PID[{}]", new Object[] { theId, theName, theDomainPid });

		PersDomain domain = myDao.getDomainByPid(theDomainPid);
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

		BasePersServiceVersion sv = myDao.getServiceVersionByPid(theServiceVersionPid);
		PersServiceVersionMethod ui = fromUi(theMethod, theServiceVersionPid);
		sv.addMethod(ui);
		sv = myServiceRegistry.saveServiceVersion(sv);
		return toUi(sv.getMethod(theMethod.getName()), false);
	}

	@Override
	public GAuthenticationHostList deleteAuthenticationHost(long thePid) throws ProcessingException {

		BasePersAuthenticationHost authHost = myDao.getAuthenticationHostByPid(thePid);
		if (authHost == null) {
			ourLog.info("Invalid request to delete unknown authentication host with PID {}", thePid);
			throw new ProcessingException("Unknown authentication host: " + thePid);
		}

		ourLog.info("Removing authentication host {} / {}", thePid, authHost.getModuleId());

		myDao.deleteAuthenticationHost(authHost);

		return loadAuthHostList();
	}

	@Override
	public void deleteDomain(long thePid) throws ProcessingException {
		PersDomain domain = myDao.getDomainByPid(thePid);
		if (domain == null) {
			throw new IllegalArgumentException("Unknown domain PID: " + thePid);
		}

		ourLog.info("DELETING domain with PID {} and ID {}", thePid, domain.getDomainId());

		myServiceRegistry.removeDomain(domain);

	}

	@Override
	public GHttpClientConfigList deleteHttpClientConfig(long thePid) throws ProcessingException {

		PersHttpClientConfig config = myDao.getHttpClientConfig(thePid);
		if (config == null) {
			throw new ProcessingException("Unknown HTTP Client Config PID: " + thePid);
		}

		ourLog.info("Deleting HTTP Client Config {} / {}", thePid, config.getId());

		myServiceRegistry.deleteHttpClientConfig(config);

		return loadHttpClientConfigList();
	}

	@Override
	public GDomainList deleteService(long theServicePid) throws ProcessingException {
		ourLog.info("Deleting service {}", theServicePid);

		PersService service = myDao.getServiceByPid(theServicePid);
		if (service == null) {
			throw new ProcessingException("Unknown service PID " + theServicePid);
		}

		myDao.deleteService(service);

		return loadDomainList();
	}

	@Override
	public long getDefaultHttpClientConfigPid() {
		return myDao.getHttpClientConfigs().iterator().next().getPid();
	}

	@Override
	public GDomain getDomainByPid(long theDomain) throws ProcessingException {
		PersDomain domain = myDao.getDomainByPid(theDomain);
		if (domain != null) {
			Set<Long> empty = Collections.emptySet();
			return loadDomain(domain, empty, empty, empty, empty);
		}
		return null;
	}

	@Override
	public long getDomainPid(String theDomainId) throws ProcessingException {
		PersDomain domain = myDao.getDomainById(theDomainId);
		if (domain == null) {
			throw new ProcessingException("Unknown ID: " + theDomainId);
		}
		return domain.getPid();
	}

	@Override
	public GService getServiceByPid(long theService) throws ProcessingException {
		PersService service = myDao.getServiceByPid(theService);
		if (service != null) {
			return toUi(service, false);
		}
		return null;
	}

	@Override
	public long getServicePid(long theDomainPid, String theServiceId) throws ProcessingException {
		PersService service = myDao.getServiceById(theDomainPid, theServiceId);
		if (service == null) {
			throw new ProcessingException("Unknown ID: " + theServiceId);
		}
		return service.getPid();
	}

	@Override
	public BaseGAuthHost loadAuthenticationHost(long thePid) throws ProcessingException {
		ourLog.info("Loading authentication host with PID: {}", thePid);

		BasePersAuthenticationHost authHost = myDao.getAuthenticationHostByPid(thePid);
		if (authHost == null) {
			throw new ProcessingException("Unknown authentication host: " + thePid);
		}

		return toUi(authHost);
	}

	@Override
	public GConfig loadConfig() throws ProcessingException {
		return toUi(myConfigSvc.getConfig());
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
			GUserList userList = loadUserList(false);
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
		BasePersServiceVersion svcVer = myDao.getServiceVersionByPid(theServiceVersionPid);
		if (svcVer == null) {
			throw new ProcessingException("Unknown service version PID: " + theServiceVersionPid);
		}

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
	public GUser loadUser(long thePid, boolean theLoadStats) throws ProcessingException {
		ourLog.info("Loading user {}", thePid);

		PersUser persUser = myDao.getUser(thePid);
		if (persUser == null) {
			throw new ProcessingException("Unknown user PID: " + thePid);
		}

		return toUi(persUser, theLoadStats);
	}

	@Override
	public GPartialUserList loadUsers(PartialUserListRequest theRequest) throws ProcessingException {
		GPartialUserList retVal = new GPartialUserList();

		ourLog.info("Loading user list: " + theRequest.toString());

		for (PersUser next : myDao.getAllUsersAndInitializeThem()) {
			retVal.add(toUi(next, theRequest.isLoadStats()));
		}

		return retVal;
	}

	@Override
	public GAuthenticationHostList saveAuthenticationHost(BaseGAuthHost theAuthHost) throws ProcessingException {
		Validate.notNull(theAuthHost);

		BasePersAuthenticationHost host = fromUi(theAuthHost);

		if (theAuthHost.getPid() > 0) {
			BasePersAuthenticationHost existingHost = myDao.getAuthenticationHostByPid(theAuthHost.getPid());
			existingHost.merge(host);
			ourLog.info("Saving existing authentication host of type {} with id {} / {}", new Object[] { host.getClass().getSimpleName(), theAuthHost.getPid(), theAuthHost.getModuleId() });
			myDao.saveAuthenticationHost(existingHost);
		} else {
			ourLog.info("Saving new authentication host of type {} with id {} / {}", new Object[] { host.getClass().getSimpleName(), theAuthHost.getPid(), theAuthHost.getModuleId() });
			myDao.saveAuthenticationHost(host);
		}

		return loadAuthHostList();
	}

	@Override
	public GConfig saveConfig(GConfig theConfig) throws ProcessingException {
		ourLog.info("Saving config");

		ourLog.info("Proxy config now contains the following URL Bases: {}", theConfig.getProxyUrlBases());

		PersConfig existing = myConfigSvc.getConfig();
		existing.merge(fromUi(theConfig));

		PersConfig newCfg = myConfigSvc.saveConfig(existing);

		return toUi(newCfg);

		//
		// PersConfig existing =
		// myPersSvc.getConfigByPid(PersConfig.DEFAULT_ID);
		// existing.merge(fromUi(theConfig));
		// PersConfig retVal = myPersSvc.saveConfig(existing);
		// return toUi(retVal);
	}

	@Override
	public GDomain saveDomain(GDomain theDomain) throws ProcessingException {
		ourLog.info("Saving domain with PID {}", theDomain.getPid());

		PersDomain domain = myDao.getDomainByPid(theDomain.getPid());
		PersDomain newDomain = fromUi(theDomain);
		domain.merge(newDomain);

		domain = myServiceRegistry.saveDomain(domain);

		return getDomainByPid(domain.getPid());
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
			existing = myDao.getHttpClientConfig(theConfig.getPid());
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
	public GDomainList saveService(GService theService) throws ProcessingException {
		ourLog.info("Saving service {}", theService.getPid());

		PersService service = myDao.getServiceByPid(theService.getPid());
		if (service == null) {
			throw new ProcessingException("Unknown service PID " + theService.getPid());
		}

		PersService newService = fromUi(theService);
		service.merge(newService);
		myDao.saveService(newService);

		return loadDomainList();
	}

	@Override
	public <T extends BaseGServiceVersion> T saveServiceVersion(long theDomain, long theService, T theVersion, List<GResource> theResources) throws ProcessingException {
		Validate.notBlank(theVersion.getId(), "Version#ID");

		ourLog.info("Adding service version {} to domain {} / service {}", new Object[] { theVersion.getPid(), theDomain, theService });

		PersDomain domain = myDao.getDomainByPid(theDomain);
		if (domain == null) {
			throw new ProcessingException("Unknown domain ID: " + theDomain);
		}

		PersService service = myDao.getServiceByPid(theService);
		if (service == null) {
			throw new ProcessingException("Unknown service ID: " + theService);
		}

		if (!domain.equals(service.getDomain())) {
			throw new ProcessingException("Service with ID " + theService + " is not a part of domain " + theDomain);
		}

		String versionId = theVersion.getId();

		BasePersServiceVersion version = fromUi(theVersion, service, theVersion.getPidOrNull(), versionId);

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
				nextPers = myDao.saveClientAuth(nextPers);
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
				nextPers = myDao.saveServerAuth(nextPers);
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

		version = myServiceRegistry.saveServiceVersion(version);

		@SuppressWarnings("unchecked")
		T retVal = (T) toUi(version, false);

		return retVal;
	}

	private <T extends BaseGServiceVersion> BasePersServiceVersion fromUi(T theVersion, PersService theService, Long theVersionPid, String theVersionId) throws ProcessingException {
		Validate.notNull(theVersion);
		Validate.notNull(theService);
		Validate.notBlank(theVersionId);

		BasePersServiceVersion retVal;
<<<<<<< HEAD
		
		if (theVersionPid!=null) {
			retVal = myDao.getServiceVersionByPid(theVersionPid);
		}else {
			retVal = myServiceRegistry.getOrCreateServiceVersionWithId(theService, theVersion.getProtocol(), theVersionId);
		}
		
=======
		if (theVersion.getPidOrNull() != null) {
			ourLog.debug("Retrieving existing service version PID[{}]", theVersion.getPidOrNull());
			retVal = myDao.getServiceVersionByPid(theVersion.getPid());
		} else {
			ourLog.debug("Retrieving service version ID[{}]", theVersionId);
			retVal = myServiceRegistry.getOrCreateServiceVersionWithId(theService, theVersion.getProtocol(), theVersionId);
			ourLog.debug("Found service version NEW[{}], PID[{}], PROTOCOL[{}]", new Object[] {retVal.isNewlyCreated(), retVal.getPid(), retVal.getProtocol().name()});
		}

>>>>>>> 4bfab8e1a4dbf19a3c44a49db7619d04f59b312e
		switch (theVersion.getProtocol()) {
		case SOAP11:
			fromUi((PersServiceVersionSoap11) retVal, (GSoap11ServiceVersion) theVersion);
			break;
		case JSONRPC20:
			fromUi((PersServiceVersionJsonRpc20) retVal, (GServiceVersionJsonRpc20) theVersion);
			break;
		}

		retVal.setActive(theVersion.isActive());
		retVal.setVersionId(theVersion.getId());
		retVal.setExplicitProxyPath(theVersion.getExplicitProxyPath());

		PersHttpClientConfig httpClientConfig = myDao.getHttpClientConfig(theVersion.getHttpClientConfigPid());
		if (httpClientConfig == null) {
			throw new ProcessingException("Unknown HTTP client config PID: " + theVersion.getHttpClientConfigPid());
		}
		retVal.setHttpClientConfig(httpClientConfig);

		retVal.populateKeepRecentTransactionsFromDto(theVersion);

		return retVal;
	}

	@SuppressWarnings("unused")
	private void fromUi(PersServiceVersionJsonRpc20 theRetVal, GServiceVersionJsonRpc20 theVersion) {
		// nothing in here yet
	}

	@Override
	public GUser saveUser(GUser theUser) throws ProcessingException {
		ourLog.info("Saving user with PID {}", theUser.getPid());

		PersUser user = fromUi(theUser);

		user = mySecurityService.saveServiceUser(user);

		return toUi(user, false);
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

		PersService service = myDao.getServiceByPid(theServicePid);

		for (int i = 1;; i++) {
			String name = i + ".0";
			if (service.getVersionWithId(name) == null) {
				return name;
			}
		}

	}

	public static int addToInt(int theAddTo, long theNumberToAdd) {
		long newValue = theAddTo + theNumberToAdd;
		if (newValue > Integer.MAX_VALUE) {
			return Integer.MAX_VALUE;
		}
		return (int) newValue;
	}

	private StatusEnum extractStatus(BaseGDashboardObjectWithUrls<?> theDashboardObject, List<Integer> the60MinInvCount, List<Long> the60minTime, StatusEnum theStatus,
			BasePersServiceVersion nextVersion) throws ProcessingException {
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
			extractStatus(60, the60MinInvCount, the60minTime, nextMethod);

		}
		return status;
	}

	private StatusEnum extractStatus(BaseGDashboardObjectWithUrls<?> theDashboardObject, StatusEnum theInitialStatus, List<Integer> the60MinInvCount, List<Long> the60minTime, PersService theService)
			throws ProcessingException {

		// Value will be changed below
		StatusEnum status = theInitialStatus;

		for (BasePersServiceVersion nextVersion : theService.getVersions()) {
			status = extractStatus(theDashboardObject, the60MinInvCount, the60minTime, status, nextVersion);

		} // end VERSION
		return status;
	}

	private void extractStatus(int theNumMinsBack, List<Integer> the60MinInvCount, List<Long> the60minTime, PersServiceVersionMethod nextMethod) throws ProcessingException {
		IRuntimeStatus statusSvc = myStatusSvc;
		extractSuccessfulInvocationInvocationTimes(myConfigSvc.getConfig(), theNumMinsBack, the60MinInvCount, the60minTime, nextMethod, statusSvc);
	}

	/**
	 * @return The start timestamp
	 */
	public static void extractSuccessfulInvocationInvocationTimes(PersConfig theConfig, int theNumMinsBack, final List<Integer> the60MinInvCount, final List<Long> the60minTime,
			PersServiceVersionMethod nextMethod, IRuntimeStatus statusSvc) {
		doWithStatsByMinute(theConfig, theNumMinsBack, statusSvc, nextMethod, new IWithStats() {
			@Override
			public void withStats(int theIndex, BasePersInvocationStats theStats) {
				growToSizeInt(the60MinInvCount, theIndex);
				growToSizeLong(the60minTime, theIndex);
				the60MinInvCount.set(theIndex, addToInt(the60MinInvCount.get(theIndex), theStats.getSuccessInvocationCount()));
				the60minTime.set(theIndex, the60minTime.get(theIndex) + theStats.getSuccessInvocationTotalTime());
			}
		});
	}

	public static void growToSizeInt(List<Integer> theCountArray, int theIndex) {
		while (theCountArray.size() <= (theIndex)) {
			theCountArray.add(0);
		}
	}

	public static void growToSizeDouble(List<Double> theCountArray, int theIndex) {
		while (theCountArray.size() <= (theIndex)) {
			theCountArray.add(0.0);
		}
	}

	public static void growToSizeLong(List<Long> theCountArray, int theIndex) {
		while (theCountArray.size() <= (theIndex)) {
			theCountArray.add(0L);
		}
	}

	public static void doWithStatsByMinute(PersConfig theConfig, int theNumberOfMinutes, IRuntimeStatus statusSvc, PersServiceVersionMethod theMethod, IWithStats theOperator) {
		Date start = getDateXMinsAgo(theNumberOfMinutes);
		Date end = new Date();

		doWithStatsByMinute(theConfig, statusSvc, theMethod, theOperator, start, end);
	}

	private static void doWithStatsByMinute(PersConfig theConfig, IRuntimeStatus statusSvc, PersServiceVersionMethod theMethod, IWithStats theOperator, Date start, Date end) {
		Date date = start;
		for (int min = 0; date.before(end); min++) {

			InvocationStatsIntervalEnum interval = doWithStatsSupportFindInterval(theConfig, date);
			date = doWithStatsSupportFindDate(date, interval);

			PersInvocationStatsPk pk = new PersInvocationStatsPk(interval, date, theMethod);
			BasePersInvocationStats stats = statusSvc.getOrCreateInvocationStatsSynchronously(pk);
			theOperator.withStats(min, stats);

			date = doWithStatsSupportIncrement(date, interval);

		}
	}

	private static Date doWithStatsSupportIncrement(Date date, InvocationStatsIntervalEnum interval) {
		Date retVal = new Date(date.getTime() + interval.millis());
		return retVal;
	}

	private static Date doWithStatsSupportFindDate(Date date, InvocationStatsIntervalEnum interval) {
		Date retVal = interval.truncate(date);
		return retVal;
	}

	public static void doWithUserStatsByMinute(PersConfig theConfig, PersUser theUser, int theNumberOfMinutes, IRuntimeStatus statusSvc, IWithStats theOperator) {
		Date xMinsAgo = getDateXMinsAgo(theNumberOfMinutes);
		Date date = xMinsAgo;

		for (int min = 0; date.before(new Date()); min++) {

			InvocationStatsIntervalEnum interval = doWithStatsSupportFindInterval(theConfig, date);
			date = doWithStatsSupportFindDate(date, interval);

			PersInvocationUserStatsPk pk = new PersInvocationUserStatsPk(interval, date, theUser);
			BasePersInvocationStats stats = statusSvc.getOrCreateUserInvocationStatsSynchronously(pk);
			theOperator.withStats(min, stats);

			date = doWithStatsSupportIncrement(date, interval);

		}
	}

	private static InvocationStatsIntervalEnum doWithStatsSupportFindInterval(PersConfig theConfig, Date date) {
		InvocationStatsIntervalEnum interval;
		if (date.before(theConfig.getCollapseStatsToDaysCutoff())) {
			interval = InvocationStatsIntervalEnum.DAY;
		} else if (date.before(theConfig.getCollapseStatsToHoursCutoff())) {
			interval = InvocationStatsIntervalEnum.HOUR;
		} else if (date.before(theConfig.getCollapseStatsToTenMinutesCutoff())) {
			interval = InvocationStatsIntervalEnum.TEN_MINUTE;
		} else {
			interval = InvocationStatsIntervalEnum.MINUTE;
		}
		return interval;
	}

	public interface IWithStats {

		void withStats(int theIndex, BasePersInvocationStats theStats);

	}

	public static Date getDateXMinsAgo(int theNumberOfMinutes) {
		Date date60MinsAgo = new Date(System.currentTimeMillis() - (theNumberOfMinutes * DateUtils.MILLIS_PER_MINUTE));
		Date date = DateUtils.truncate(date60MinsAgo, Calendar.MINUTE);
		return date;
	}

	private BasePersAuthenticationHost fromUi(BaseGAuthHost theAuthHost) {
		BasePersAuthenticationHost retVal = null;

		switch (theAuthHost.getType()) {
		case LDAP:
			PersAuthenticationHostLdap pLdap = new PersAuthenticationHostLdap();
			GLdapAuthHost uiLdap = (GLdapAuthHost) theAuthHost;
			pLdap.setAuthenticateBaseDn(uiLdap.getAuthenticateBaseDn());
			pLdap.setAuthenticateFilter(uiLdap.getAuthenticateFilter());
			pLdap.setBindPassword(uiLdap.getBindUserPassword());
			pLdap.setBindUserDn(uiLdap.getBindUserDn());
			pLdap.setUrl(uiLdap.getUrl());
			retVal = pLdap;
			break;
		case LOCAL_DATABASE:
			retVal = new PersAuthenticationHostLocalDatabase();
			break;
		}

		if (retVal == null) {
			throw new IllegalStateException("Unknown type:" + theAuthHost.getType());
		}

		retVal.populateKeepRecentTransactionsFromDto(theAuthHost);
		retVal.setAutoCreateAuthorizedUsers(theAuthHost.isAutoCreateAuthorizedUsers());
		retVal.setCacheSuccessfulCredentialsForMillis(theAuthHost.getCacheSuccessesForMillis());
		retVal.setModuleId(theAuthHost.getModuleId());
		retVal.setModuleName(theAuthHost.getModuleName());
		retVal.setPid(theAuthHost.getPidOrNull());
		retVal.setSupportsPasswordChange(theAuthHost.isSupportsPasswordChange());

		return retVal;
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

	private PersBaseServerAuth<?, ?> fromUi(BaseGServerSecurity theObj, BasePersServiceVersion theSvcVer) {
		switch (theObj.getType()) {
		case WSSEC_UT: {
			PersWsSecUsernameTokenServerAuth retVal = new PersWsSecUsernameTokenServerAuth();
			retVal.setAuthenticationHost(myDao.getAuthenticationHostByPid(theObj.getAuthHostPid()));
			retVal.setServiceVersion(theSvcVer);
			return retVal;
		}
		case HTTP_BASIC_AUTH: {
			PersHttpBasicServerAuth retVal = new PersHttpBasicServerAuth();
			retVal.setAuthenticationHost(myDao.getAuthenticationHostByPid(theObj.getAuthHostPid()));
			retVal.setServiceVersion(theSvcVer);
			return retVal;
		}
		case JSONRPC_NAMED_PARAMETER: {
			GNamedParameterJsonRpcServerAuth obj = (GNamedParameterJsonRpcServerAuth) theObj;
			NamedParameterJsonRpcServerAuth retVal = new NamedParameterJsonRpcServerAuth();
			retVal.setAuthenticationHost(myDao.getAuthenticationHostByPid(theObj.getAuthHostPid()));
			retVal.setServiceVersion(theSvcVer);
			retVal.setUsernameParameterName(obj.getUsernameParameterName());
			retVal.setPasswordParameterName(obj.getPasswordParameterName());
			return retVal;
		}
		}
		throw new IllegalArgumentException("Unknown type: " + theObj.getType());
	}

	private PersConfig fromUi(GConfig theConfig) {
		PersConfig retVal = new PersConfig();

		for (String next : theConfig.getProxyUrlBases()) {
			retVal.addProxyUrlBase(new PersConfigProxyUrlBase(next));
		}

		return retVal;
	}

	private PersDomain fromUi(GDomain theDomain) {
		PersDomain retVal = new PersDomain();
		retVal.setPid(theDomain.getPidOrNull());
		retVal.setDomainId(theDomain.getId());
		retVal.setDomainName(theDomain.getName());
		retVal.populateKeepRecentTransactionsFromDto(theDomain);
		return retVal;
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

	private PersService fromUi(GService theService) {
		PersService retVal = new PersService();
		retVal.setPid(theService.getPidOrNull());
		retVal.setActive(theService.isActive());
		retVal.setServiceId(theService.getId());
		retVal.setServiceName(theService.getName());
		retVal.populateKeepRecentTransactionsFromDto(theService);
		return retVal;
	}

	private PersServiceVersionMethod fromUi(GServiceMethod theMethod, long theServiceVersionPid) {
		PersServiceVersionMethod retVal = new PersServiceVersionMethod();
		retVal.setName(theMethod.getName());
		retVal.setPid(theMethod.getPidOrNull());
		retVal.setServiceVersion(myDao.getServiceVersionByPid(theServiceVersionPid));
		retVal.setRootElements(theMethod.getRootElements());
		return retVal;
	}

	private PersServiceVersionUrl fromUi(GServiceVersionUrl theUrl, BasePersServiceVersion theServiceVersion) {
		PersServiceVersionUrl retVal = new PersServiceVersionUrl();
		retVal.setUrlId(theUrl.getId());
		retVal.setUrl(theUrl.getUrl());
		retVal.setServiceVersion(theServiceVersion);
		return retVal;
	}

	private PersUser fromUi(GUser theUser) throws ProcessingException {
		PersUser retVal;

		if (theUser.getPidOrNull() == null) {
			BasePersAuthenticationHost authHost = myDao.getAuthenticationHostByPid(theUser.getAuthHostPid());
			retVal = myDao.getOrCreateUser(authHost, theUser.getUsername());
			if (retVal.isNewlyCreated() == false) {
				throw new ProcessingException("User '" + theUser.getUsername() + "' already exists!");
			}
		} else {
			retVal = myDao.getUser(theUser.getPid());
		}

		retVal.setAllowSourceIpsAsStrings(theUser.getAllowableSourceIps());
		retVal.setAllowAllDomains(theUser.isAllowAllDomains());
		retVal.setPermissions(theUser.getGlobalPermissions());
		retVal.setUsername(theUser.getUsername());
		retVal.setDomainPermissions(fromUi(theUser.getDomainPermissions(), retVal.getDomainPermissions()));
		retVal.setAuthenticationHost(myDao.getAuthenticationHostByPid(theUser.getAuthHostPid()));

		if (theUser.getPidOrNull() != null && theUser.getChangePassword() == null) {
			PersUser existing = myDao.getUser(theUser.getPid());
			if (StringUtils.isNotBlank(existing.getPasswordHash())) {
				retVal.setPasswordHash(existing.getPasswordHash());
			}
		} else if (theUser.getChangePassword() != null) {
			ourLog.info("Changing password for user {}", theUser.getPidOrNull());
			retVal.setPassword(theUser.getChangePassword());
		}

		return retVal;
	}

	private PersUserDomainPermission fromUi(GUserDomainPermission theObj) {
		PersUserDomainPermission retVal = new PersUserDomainPermission();
		retVal.setPid(theObj.getPidOrNull());
		retVal.setAllowAllServices(theObj.isAllowAllServices());
		retVal.setServiceDomain(myDao.getDomainByPid(theObj.getDomainPid()));
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
		retVal.setService(myDao.getServiceByPid(theObj.getServicePid()));
		retVal.setServiceVersionPermissions(new ArrayList<PersUserServiceVersionPermission>());
		for (GUserServiceVersionPermission next : theObj.getServiceVersionPermissions()) {
			retVal.addServiceVersionPermission(fromUi(next));
		}
		return retVal;
	}

	private PersUserServiceVersionMethodPermission fromUi(GUserServiceVersionMethodPermission theObj) {
		PersUserServiceVersionMethodPermission retVal = new PersUserServiceVersionMethodPermission();
		retVal.setPid(theObj.getPidOrNull());
		retVal.setServiceVersionMethod(myDao.getServiceVersionMethodByPid(theObj.getServiceVersionMethodPid()));
		retVal.setAllow(theObj.isAllow());
		return retVal;
	}

	private PersUserServiceVersionPermission fromUi(GUserServiceVersionPermission theObj) {
		PersUserServiceVersionPermission retVal = new PersUserServiceVersionPermission();
		retVal.setPid(theObj.getPidOrNull());
		retVal.setAllowAllServiceVersionMethods(theObj.isAllowAllServiceVersionMethods());
		retVal.setServiceVersion(myDao.getServiceVersionByPid(theObj.getServiceVersionPid()));
		retVal.setServiceVersionMethodPermissions(new ArrayList<PersUserServiceVersionMethodPermission>());
		for (GUserServiceVersionMethodPermission next : theObj.getServiceVersionMethodPermissions()) {
			retVal.addServiceVersionMethodPermissions(fromUi(next));
		}
		return retVal;
	}

	private Collection<PersUserDomainPermission> fromUi(List<GUserDomainPermission> theDomainPermissions, Collection<PersUserDomainPermission> theExisting) {
		Collection<PersUserDomainPermission> retVal = theExisting;
		retVal.clear();
		for (GUserDomainPermission next : theDomainPermissions) {
			retVal.add(fromUi(next));
		}
		return retVal;
	}

	private PersServiceVersionSoap11 fromUi(PersServiceVersionSoap11 thePersVersion, GSoap11ServiceVersion theVersion) throws ProcessingException {
		thePersVersion.setWsdlUrl(theVersion.getWsdlLocation());
		return thePersVersion;
	}

	private GAuthenticationHostList loadAuthHostList() {
		GAuthenticationHostList retVal = new GAuthenticationHostList();
		for (BasePersAuthenticationHost next : myDao.getAllAuthenticationHosts()) {
			BaseGAuthHost uiObject = toUi(next);
			retVal.add(uiObject);
		}
		return retVal;
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

	private GDomainList loadDomainList(Set<Long> theLoadDomStats, Set<Long> theLoadSvcStats, Set<Long> theLoadVerStats, Set<Long> theLoadVerMethodStats) throws ProcessingException {
		GDomainList domainList = new GDomainList();

		for (PersDomain nextDomain : myDao.getAllDomains()) {
			GDomain gDomain = loadDomain(nextDomain, theLoadDomStats, theLoadSvcStats, theLoadVerStats, theLoadVerMethodStats);

			domainList.add(gDomain);
		} // for domains
		return domainList;
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

	private GHttpClientConfigList loadHttpClientConfigList() {
		GHttpClientConfigList configList = new GHttpClientConfigList();
		for (PersHttpClientConfig next : myDao.getHttpClientConfigs()) {
			configList.add(toUi(next));
		}
		return configList;
	}

	private GUserList loadUserList(boolean theLoadStats) throws ProcessingException {
		GUserList retVal = new GUserList();
		Collection<PersUser> users = myDao.getAllUsersAndInitializeThem();
		for (PersUser persUser : users) {
			retVal.add(toUi(persUser, theLoadStats));
		}
		return retVal;
	}

	private int[] toLatency(List<Integer> theCounts, List<Long> theTimes) {
		assert theCounts.size() == theTimes.size();

		int[] retVal = new int[theCounts.size()];
		int prevValue = -1;
		for (int i = 0; i < theCounts.size(); i++) {
			if (theCounts.get(i) > 0) {
				retVal[i] = (int) Math.min(theTimes.get(i) / theCounts.get(i), Integer.MAX_VALUE);
				prevValue = retVal[i];
			} else if (prevValue > -1) {
				retVal[i] = prevValue;
			}
		}

		return retVal;
	}

	private GSoap11ServiceVersionAndResources toUi(BaseGServiceVersion theUiService, BasePersServiceVersion theSvcVer) throws ProcessingException {
		GSoap11ServiceVersionAndResources retVal = new GSoap11ServiceVersionAndResources();

		retVal.setServiceVersion(theUiService);
		theSvcVer.populateKeepRecentTransactionsToDto(retVal.getServiceVersion());

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

	private BaseGAuthHost toUi(BasePersAuthenticationHost thePersObj) {
		BaseGAuthHost retVal = null;
		switch (thePersObj.getType()) {
		case LOCAL_DATABASE:
			retVal = new GLocalDatabaseAuthHost();
			break;
		case LDAP:
			retVal = new GLdapAuthHost();
			break;
		}

		if (retVal == null) {
			throw new IllegalStateException("Unknown auth host type: " + thePersObj.getType());
		}

		retVal.setPid(thePersObj.getPid());
		retVal.setAutoCreateAuthorizedUsers(thePersObj.isAutoCreateAuthorizedUsers());
		retVal.setCacheSuccessesForMillis(thePersObj.getCacheSuccessfulCredentialsForMillis());
		retVal.setModuleId(thePersObj.getModuleId());
		retVal.setModuleName(thePersObj.getModuleName());
		retVal.setSupportsPasswordChange(thePersObj.isSupportsPasswordChange());

		thePersObj.populateKeepRecentTransactionsToDto(retVal);

		return retVal;
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
		case JSONRPC20:
			retVal = new GServiceVersionJsonRpc20();
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
		retVal.setExplicitProxyPath(theVersion.getExplicitProxyPath());

		PersHttpClientConfig httpClientConfig = theVersion.getHttpClientConfig();
		if (httpClientConfig == null) {
			throw new ProcessingException("Service version doesn't have an HTTP client config");
		}
		retVal.setHttpClientConfigPid(httpClientConfig.getPid());

		if (theLoadStats) {
			retVal.setStatsInitialized(new Date());
			ArrayList<Integer> t60minCount = new ArrayList<Integer>();
			ArrayList<Long> t60minTime = new ArrayList<Long>();

			StatusEnum status = StatusEnum.UNKNOWN;
			extractStatus(retVal, t60minCount, t60minTime, status, theVersion);

			retVal.setTransactions60mins(toArray(t60minCount));
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

	private static int[] toArray(ArrayList<Integer> theT60minCount) {
		int[] retVal = new int[theT60minCount.size()];
		int index = 0;
		for (Integer integer : theT60minCount) {
			retVal[index++] = integer;
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

	private BaseGServerSecurity toUi(PersBaseServerAuth<?, ?> theAuth) throws ProcessingException {
		BaseGServerSecurity retVal = null;

		switch (theAuth.getAuthType()) {
		case WSSEC_UT: {
			GWsSecServerSecurity auth = new GWsSecServerSecurity();
			retVal = auth;
			break;
		}
		case HTTP_BASIC_AUTH: {
			GHttpBasicAuthServerSecurity auth = new GHttpBasicAuthServerSecurity();
			retVal = auth;
			break;
		}
		case JSONRPC_NAMED_PARAMETER: {
			NamedParameterJsonRpcServerAuth pers = (NamedParameterJsonRpcServerAuth) theAuth;
			GNamedParameterJsonRpcServerAuth auth = new GNamedParameterJsonRpcServerAuth();
			auth.setUsernameParameterName(pers.getUsernameParameterName());
			auth.setPasswordParameterName(pers.getPasswordParameterName());
			retVal = auth;
			break;
		}
		}

		if (retVal == null) {
			throw new ProcessingException("Unknown auth type; " + theAuth.getAuthType());
		}

		retVal.setPid(theAuth.getPid());
		retVal.setAuthHostPid(theAuth.getAuthenticationHost().getPid());

		return retVal;
	}

	private GConfig toUi(PersConfig theConfig) {
		GConfig retVal = new GConfig();

		for (PersConfigProxyUrlBase next : theConfig.getProxyUrlBases()) {
			retVal.getProxyUrlBases().add(next.getUrlBase());
		}

		return retVal;
	}

	private GDomain toUi(PersDomain theDomain, boolean theLoadStats) throws ProcessingException {
		GDomain retVal = new GDomain();
		retVal.setPid(theDomain.getPid());
		retVal.setId(theDomain.getDomainId());
		retVal.setName(theDomain.getDomainName());
		retVal.setServerSecured(theDomain.getServerSecured());
		theDomain.populateKeepRecentTransactionsToDto(retVal);

		if (theLoadStats) {
			retVal.setStatsInitialized(new Date());
			StatusEnum status = StatusEnum.UNKNOWN;
			ArrayList<Integer> t60minCount = new ArrayList<Integer>();
			ArrayList<Long> t60minTime = new ArrayList<Long>();

			for (PersService nextService : theDomain.getServices()) {
				status = extractStatus(retVal, status, t60minCount, t60minTime, nextService);
			}

			retVal.setTransactions60mins(toArray(t60minCount));
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

	private GService toUi(PersService theService, boolean theLoadStats) throws ProcessingException {
		GService retVal = new GService();
		retVal.setPid(theService.getPid());
		retVal.setId(theService.getServiceId());
		retVal.setName(theService.getServiceName());
		retVal.setActive(theService.isActive());
		retVal.setServerSecured(theService.getServerSecured());
		theService.populateKeepRecentTransactionsToDto(retVal);

		if (theLoadStats) {
			retVal.setStatsInitialized(new Date());
			StatusEnum status = StatusEnum.UNKNOWN;
			ArrayList<Integer> t60minCount = new ArrayList<Integer>();
			ArrayList<Long> t60minTime = new ArrayList<Long>();

			status = extractStatus(retVal, status, t60minCount, t60minTime, theService);

			retVal.setTransactions60mins(toArray(t60minCount));
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

	private GServiceMethod toUi(PersServiceVersionMethod theMethod, boolean theLoadStats) throws ProcessingException {
		GServiceMethod retVal = new GServiceMethod();
		if (theMethod.getPid() != null) {
			retVal.setPid(theMethod.getPid());
		}
		retVal.setId(theMethod.getName());
		retVal.setName(theMethod.getName());
		retVal.setRootElements(theMethod.getRootElements());

		if (theLoadStats) {
			retVal.setStatsInitialized(new Date());
			StatusEnum status = StatusEnum.UNKNOWN;
			ArrayList<Integer> t60minCount = new ArrayList<Integer>();
			ArrayList<Long> t60minTime = new ArrayList<Long>();

			extractStatus(60, t60minCount, t60minTime, theMethod);

			retVal.setTransactions60mins(toArray(t60minCount));
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

	private GUser toUi(PersUser thePersUser, boolean theLoadStats) throws ProcessingException {
		GUser retVal = new GUser();
		retVal.setPid(thePersUser.getPid());
		retVal.setAllowAllDomains(thePersUser.isAllowAllDomains());
		retVal.setGlobalPermissions(thePersUser.getPermissions());
		retVal.setUsername(thePersUser.getUsername());
		retVal.setDomainPermissions(toUi(thePersUser.getDomainPermissions()));
		retVal.setAuthHostPid(thePersUser.getAuthenticationHost().getPid());
		retVal.setContactNotes(thePersUser.getContact().getNotes());
		retVal.setAllowableSourceIps(thePersUser.getAllowSourceIpsAsStrings());

		if (theLoadStats) {
			PersUserStatus status = thePersUser.getStatus();
			retVal.setStatsInitialized(new Date());
			retVal.setStatsLastAccess(status.getLastAccess());

			final ArrayList<Integer> t60minSuccessCount = new ArrayList<Integer>();
			final ArrayList<Integer> t60minSecurityFailCount = new ArrayList<Integer>();
			IWithStats withStats = new IWithStats() {
				@Override
				public void withStats(int theIndex, BasePersInvocationStats theStats) {
					growToSizeInt(t60minSuccessCount, theIndex);
					growToSizeInt(t60minSecurityFailCount, theIndex);
					t60minSecurityFailCount.set(theIndex, addToInt(t60minSecurityFailCount.get(theIndex), theStats.getServerSecurityFailures()));
					t60minSuccessCount.set(theIndex, addToInt(t60minSuccessCount.get(theIndex), theStats.getSuccessInvocationCount()));
				}
			};
			doWithUserStatsByMinute(myConfigSvc.getConfig(), thePersUser, 60, myStatusSvc, withStats);
			retVal.setStatsSuccessTransactions(toArray(t60minSuccessCount));
			retVal.setStatsSuccessTransactionsAvgPerMin(to60MinAveragePerMin(t60minSuccessCount));
			retVal.setStatsSecurityFailTransactions(toArray(t60minSecurityFailCount));
			retVal.setStatsSecurityFailTransactionsAvgPerMin(to60MinAveragePerMin(t60minSecurityFailCount));

		}

		return retVal;
	}

	private static double to60MinAveragePerMin(ArrayList<Integer> theT60minSecurityFailCount) {
		double total = 0;
		for (Integer integer : theT60minSecurityFailCount) {
			total += integer;
		}
		return total / theT60minSecurityFailCount.size();
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
	 * Convenience for Unit Tests
	 */
	GDomain addDomain(String theId, String theName) throws ProcessingException {
		GDomain domain = new GDomain();
		domain.setId(theId);
		domain.setName(theName);
		GDomain retVal = addDomain(domain);
		return retVal;
	}

	/**
	 * Unit test only
	 */
	void setInvokerSoap11(IServiceInvokerSoap11 theInvokerSoap11) {
		myInvokerSoap11 = theInvokerSoap11;
	}

	void setPersSvc(DaoBean thePersSvc) {
		assert myDao == null;
		myDao = thePersSvc;
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
	public List<GUrlStatus> loadServiceVersionUrlStatuses(long theServiceVersionPid) {
		ourLog.info("Loading Service Version URL statuses for ServiceVersion {}", theServiceVersionPid);

		BasePersServiceVersion svcVer = myDao.getServiceVersionByPid(theServiceVersionPid);
		if (svcVer == null) {
			throw new IllegalArgumentException("Unknown Service Version " + theServiceVersionPid);
		}

		List<GUrlStatus> retVal = new ArrayList<GUrlStatus>();

		for (PersServiceVersionUrl nextUrl : svcVer.getUrls()) {
			PersServiceVersionUrlStatus nextUrlStatus = nextUrl.getStatus();
			retVal.add(toUi(nextUrlStatus, nextUrl));
		}

		return retVal;
	}

	private GUrlStatus toUi(PersServiceVersionUrlStatus theUrlStatus, PersServiceVersionUrl theUrl) {
		GUrlStatus retVal = new GUrlStatus();
		retVal.setUrlPid(theUrl.getPid());
		retVal.setUrl(theUrl.getUrl());
		retVal.setStatus(theUrlStatus.getStatus());
		retVal.setLastFailure(theUrlStatus.getLastFail());
		retVal.setLastFailureMessage(theUrlStatus.getLastFailMessage());
		retVal.setLastSuccess(theUrlStatus.getLastSuccess());
		retVal.setLastSuccessMessage(theUrlStatus.getLastSuccessMessage());
		retVal.setLastFault(theUrlStatus.getLastFault());
		retVal.setLastFaultMessage(theUrlStatus.getLastFaultMessage());
		return retVal;
	}

	public static long addToLong(long theAddTo, long theNumberToAdd) {
		long newValue = theAddTo + theNumberToAdd;
		return newValue;
	}

	/**
	 * Unit test only
	 */
	void setConfigSvc(IConfigService theConfigSvc) {
		myConfigSvc = theConfigSvc;
	}

	@Override
	public GRecentMessageLists loadRecentTransactionListForServiceVersion(long theServiceVersionPid) {
		BasePersServiceVersion svcVer = myDao.getServiceVersionByPid(theServiceVersionPid);

		GRecentMessageLists retVal = new GRecentMessageLists();
		retVal.setKeepSuccess(defaultInteger(svcVer.determineKeepNumRecentTransactions(ResponseTypeEnum.SUCCESS)));
		retVal.setKeepFail(defaultInteger(svcVer.determineKeepNumRecentTransactions(ResponseTypeEnum.FAIL)));
		retVal.setKeepFault(defaultInteger(svcVer.determineKeepNumRecentTransactions(ResponseTypeEnum.FAULT)));
		retVal.setKeepSecurityFail(defaultInteger(svcVer.determineKeepNumRecentTransactions(ResponseTypeEnum.SECURITY_FAIL)));

		if (retVal.getKeepSuccess() > 0) {
			retVal.setSuccessList(toUi(myDao.getServiceVersionRecentMessages(svcVer, ResponseTypeEnum.SUCCESS), true));
		}

		if (retVal.getKeepFail() > 0) {
			retVal.setFailList(toUi(myDao.getServiceVersionRecentMessages(svcVer, ResponseTypeEnum.FAIL), true));
		}

		if (retVal.getKeepSecurityFail() > 0) {
			retVal.setSecurityFailList(toUi(myDao.getServiceVersionRecentMessages(svcVer, ResponseTypeEnum.SECURITY_FAIL), true));
		}

		if (retVal.getKeepFault() > 0) {
			retVal.setFaultList(toUi(myDao.getServiceVersionRecentMessages(svcVer, ResponseTypeEnum.FAULT), true));
		}

		ourLog.info("Returning recent message list for service version {} - {}", theServiceVersionPid, retVal);

		return retVal;
	}

	private List<GRecentMessage> toUi(List<? extends BasePersRecentMessage> theServiceVersionRecentMessages, boolean theLoadMessageContents) {
		List<GRecentMessage> retVal = new ArrayList<GRecentMessage>();

		for (BasePersRecentMessage next : theServiceVersionRecentMessages) {
			retVal.add(toUi(next, theLoadMessageContents));
		}

		return retVal;
	}

	@Override
	public GRecentMessage loadRecentMessageForServiceVersion(long thePid) {
		BasePersRecentMessage msg = myDao.loadRecentMessageForServiceVersion(thePid);
		return toUi(msg, true);
	}

	private GRecentMessage toUi(BasePersRecentMessage theMsg, boolean theLoadMsgContents) {
		GRecentMessage retVal = new GRecentMessage();

		retVal.setPid(theMsg.getPid());
		PersServiceVersionUrl implementationUrl = theMsg.getImplementationUrl();
		if (implementationUrl != null) {
			retVal.setImplementationUrl(implementationUrl.getUrl());
		}
		retVal.setRequestHostIp(theMsg.getRequestHostIp());
		retVal.setTransactionTime(theMsg.getTransactionTime());
		retVal.setTransactionMillis(theMsg.getTransactionMillis());
		retVal.setAuthorizationOutcome(theMsg.getAuthorizationOutcome());

		if (theLoadMsgContents) {
			retVal.setRequestMessage(theMsg.getRequestBody());
			retVal.setResponseMessage(theMsg.getResponseBody());
		}

		if (theMsg instanceof PersServiceVersionRecentMessage) {
			PersServiceVersionRecentMessage msg = (PersServiceVersionRecentMessage) theMsg;
			if (msg.getUser() != null) {
				retVal.setRequestUserPid(msg.getUser().getPid());
				retVal.setRequestUsername(msg.getUser().getUsername());
			}
		} else if (theMsg instanceof PersUserRecentMessage) {
			PersUserRecentMessage msg = (PersUserRecentMessage) theMsg;
			if (msg.getUser() != null) {
				retVal.setRequestUserPid(msg.getUser().getPid());
				retVal.setRequestUsername(msg.getUser().getUsername());
			}
		}

		return retVal;
	}

	@Override
	public GRecentMessageLists loadRecentTransactionListForUser(long thePid) {
		PersUser user = myDao.getUser(thePid);
		BasePersAuthenticationHost authHost = user.getAuthenticationHost();

		GRecentMessageLists retVal = new GRecentMessageLists();
		retVal.setKeepSuccess(defaultInteger(authHost.determineKeepNumRecentTransactions(ResponseTypeEnum.SUCCESS)));
		retVal.setKeepFail(defaultInteger(authHost.determineKeepNumRecentTransactions(ResponseTypeEnum.FAIL)));
		retVal.setKeepFault(defaultInteger(authHost.determineKeepNumRecentTransactions(ResponseTypeEnum.FAULT)));
		retVal.setKeepSecurityFail(defaultInteger(authHost.determineKeepNumRecentTransactions(ResponseTypeEnum.SECURITY_FAIL)));

		if (retVal.getKeepSuccess() > 0) {
			retVal.setSuccessList(toUi(myDao.getUserRecentMessages(user, ResponseTypeEnum.SUCCESS), true));
		}

		if (retVal.getKeepFail() > 0) {
			retVal.setFailList(toUi(myDao.getUserRecentMessages(user, ResponseTypeEnum.FAIL), true));
		}

		if (retVal.getKeepSecurityFail() > 0) {
			retVal.setSecurityFailList(toUi(myDao.getUserRecentMessages(user, ResponseTypeEnum.SECURITY_FAIL), true));
		}

		if (retVal.getKeepFault() > 0) {
			retVal.setFaultList(toUi(myDao.getUserRecentMessages(user, ResponseTypeEnum.FAULT), true));
		}

		ourLog.info("Returning recent message list for service version {} - {}", thePid, retVal);

		return retVal;
	}

	private int defaultInteger(Integer theInt) {
		return theInt != null ? theInt : 0;
	}

	@Override
	public GRecentMessage loadRecentMessageForUser(long thePid) {
		BasePersRecentMessage msg = myDao.loadRecentMessageForUser(thePid);
		return toUi(msg, true);
	}

	@Override
	public GServiceVersionDetailedStats loadServiceVersionDetailedStats(long theVersionPid) throws ProcessingException {

		BasePersServiceVersion ver = myDao.getServiceVersionByPid(theVersionPid);

		final Map<Long, List<Integer>> methodPidToSuccessCount = new HashMap<Long, List<Integer>>();
		final Map<Long, List<Integer>> methodPidToFailCount = new HashMap<Long, List<Integer>>();
		final Map<Long, List<Integer>> methodPidToSecurityFailCount = new HashMap<Long, List<Integer>>();
		final Map<Long, List<Integer>> methodPidToFaultCount = new HashMap<Long, List<Integer>>();

		final List<Long> statsTimestamps = new ArrayList<Long>();

		PersConfig config = myConfigSvc.getConfig();
		for (final PersServiceVersionMethod nextMethod : ver.getMethods()) {
			methodPidToSuccessCount.put(nextMethod.getPid(), new ArrayList<Integer>());
			methodPidToFailCount.put(nextMethod.getPid(), new ArrayList<Integer>());
			methodPidToSecurityFailCount.put(nextMethod.getPid(), new ArrayList<Integer>());
			methodPidToFaultCount.put(nextMethod.getPid(), new ArrayList<Integer>());

			doWithStatsByMinute(config, 60, myStatusSvc, nextMethod, new IWithStats() {
				@Override
				public void withStats(int theIndex, BasePersInvocationStats theStats) {
					List<Integer> successCounts = methodPidToSuccessCount.get(nextMethod.getPid());
					List<Integer> failCounts = methodPidToFailCount.get(nextMethod.getPid());
					List<Integer> securityFailCounts = methodPidToSecurityFailCount.get(nextMethod.getPid());
					List<Integer> faultCounts = methodPidToFaultCount.get(nextMethod.getPid());
					growToSizeInt(successCounts, theIndex);
					growToSizeInt(failCounts, theIndex);
					growToSizeInt(securityFailCounts, theIndex);
					growToSizeInt(faultCounts, theIndex);
					growToSizeLong(statsTimestamps, theIndex);
					successCounts.set(theIndex, addToInt(successCounts.get(theIndex), theStats.getSuccessInvocationCount()));
					failCounts.set(theIndex, addToInt(failCounts.get(theIndex), theStats.getFailInvocationCount()));
					securityFailCounts.set(theIndex, addToInt(securityFailCounts.get(theIndex), theStats.getServerSecurityFailures()));
					faultCounts.set(theIndex, addToInt(faultCounts.get(theIndex), theStats.getFaultInvocationCount()));
					statsTimestamps.set(theIndex, theStats.getPk().getStartTime().getTime());
				}
			});
		}

		GServiceVersionDetailedStats retVal = new GServiceVersionDetailedStats();

		retVal.setMethodPidToSuccessCount(methodPidToSuccessCount);
		retVal.setMethodPidToFailCount(methodPidToFailCount);
		retVal.setMethodPidToSecurityFailCount(methodPidToSecurityFailCount);
		retVal.setMethodPidToFaultCount(methodPidToFaultCount);

		retVal.setStatsTimestamps(statsTimestamps);

		return retVal;
	}

	public static void doWithStatsByMinute(PersConfig theConfig, TimeRange theRange, IRuntimeStatus theStatus, PersServiceVersionMethod theNextMethod, IWithStats theOperator) {

		Date start = new Date(System.currentTimeMillis() - (theRange.getRange().getNumMins() * 60 * 1000L));
		Date end = new Date();
		doWithStatsByMinute(theConfig, theStatus, theNextMethod, theOperator, start, end);
	}

	@Override
	public GDomainList deleteServiceVersion(long thePid) throws ProcessingException {
		ourLog.info("Deleting service version {}", thePid);
		
		BasePersServiceVersion sv = myDao.getServiceVersionByPid(thePid);
		if (sv==null) {
			throw new ProcessingException("Unknown service version ID:"+thePid);
		}
		
		myDao.deleteServiceVersion(sv);
		
		return loadDomainList();
	}

	// private GDomain toUi(PersDomain theDomain) {
	// GDomain retVal=new GDomain();
	// retVal.setPid(theDomain.getPid());
	// retVal.setId(theDomain.getDomainId());
	// retVal.setName(theDomain.getDomainName());
	// return retVal;
	// }

}
