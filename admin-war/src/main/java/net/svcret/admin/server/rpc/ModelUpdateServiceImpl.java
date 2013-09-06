package net.svcret.admin.server.rpc;

import static org.apache.commons.lang3.StringUtils.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import javax.ejb.EJB;
import javax.servlet.http.HttpSession;

import net.svcret.admin.client.rpc.ModelUpdateService;
import net.svcret.admin.shared.ServiceFailureException;
import net.svcret.admin.shared.model.AddServiceVersionResponse;
import net.svcret.admin.shared.model.BaseGAuthHost;
import net.svcret.admin.shared.model.BaseGMonitorRule;
import net.svcret.admin.shared.model.BaseGServiceVersion;
import net.svcret.admin.shared.model.DtoLibraryMessage;
import net.svcret.admin.shared.model.DtoServiceVersionJsonRpc20;
import net.svcret.admin.shared.model.DtoServiceVersionSoap11;
import net.svcret.admin.shared.model.GAuthenticationHostList;
import net.svcret.admin.shared.model.GConfig;
import net.svcret.admin.shared.model.GDomain;
import net.svcret.admin.shared.model.GDomainList;
import net.svcret.admin.shared.model.GMonitorRuleFiring;
import net.svcret.admin.shared.model.GMonitorRuleList;
import net.svcret.admin.shared.model.GPartialUserList;
import net.svcret.admin.shared.model.GRecentMessage;
import net.svcret.admin.shared.model.GRecentMessageLists;
import net.svcret.admin.shared.model.GResource;
import net.svcret.admin.shared.model.GService;
import net.svcret.admin.shared.model.GServiceVersionDetailedStats;
import net.svcret.admin.shared.model.GServiceVersionSingleFireResponse;
import net.svcret.admin.shared.model.GServiceVersionUrl;
import net.svcret.admin.shared.model.GSoap11ServiceVersionAndResources;
import net.svcret.admin.shared.model.GUser;
import net.svcret.admin.shared.model.HierarchyEnum;
import net.svcret.admin.shared.model.ModelUpdateRequest;
import net.svcret.admin.shared.model.ModelUpdateResponse;
import net.svcret.admin.shared.model.PartialUserListRequest;
import net.svcret.admin.shared.model.ServiceProtocolEnum;
import net.svcret.ejb.api.IAdminServiceLocal;
import net.svcret.ejb.ex.ProcessingException;
import net.svcret.ejb.util.Validate;

/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class ModelUpdateServiceImpl extends BaseRpcServlet implements ModelUpdateService {

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(ModelUpdateServiceImpl.class);
	private static final AtomicLong ourNextId = new AtomicLong(0L);
	private static final String SESSION_PREFIX_UNCOMITTED_SVC_VER = "UNC_SVC_VER_";
	private static final String SESSION_PREFIX_UNCOMITTED_SVC_VER_RES = "UNC_SVC_VER_RES_";

	@EJB
	private IAdminServiceLocal myAdminSvc;

	@Override
	public GDomain addDomain(GDomain theDomain) throws ServiceFailureException {
		ourLog.info("Adding domain {}/{}", theDomain.getId(), theDomain.getName());

		if (isMockMode()) {
			return getMock().addDomain(theDomain);
		}

		try {
			return myAdminSvc.addDomain(theDomain);
		} catch (ProcessingException e) {
			ourLog.warn("Failed to add domain", e);
			throw new ServiceFailureException("Failed to add domain: " + e.getMessage());
		}
	}

	@Override
	public GService addService(long theDomainPid, String theId, String theName, boolean theActive) throws ServiceFailureException {
		ourLog.info("Adding new service to domain {} with ID[{}] and name: {}", new Object[] { theDomainPid, theId, theName });

		if (isMockMode()) {
			return getMock().addService(theDomainPid, theId, theName, theActive);
		}

		try {
			return myAdminSvc.addService(theDomainPid, theId, theName, theActive);
		} catch (ProcessingException e) {
			ourLog.warn("Failed to add domain", e);
			throw new ServiceFailureException("Failed to add domain: " + e.getMessage());
		}
	}

	@Override
	public AddServiceVersionResponse addServiceVersion(Long theExistingDomainPid, String theCreateDomainId, Long theExistingServicePid, String theCreateServiceId, BaseGServiceVersion theVersion) throws ServiceFailureException {
		if (theExistingDomainPid == null && isBlank(theCreateDomainId)) {
			throw new IllegalArgumentException("Domain PID and new domain ID are both missing");
		}
		if (theExistingDomainPid != null && isNotBlank(theCreateDomainId)) {
			throw new IllegalArgumentException("Domain PID and new domain ID can not both be provided");
		}
		if (theExistingServicePid == null && isBlank(theCreateServiceId)) {
			throw new IllegalArgumentException("Service PID and new domain ID are both missing");
		}
		if (theExistingServicePid != null && isNotBlank(theCreateServiceId)) {
			throw new IllegalArgumentException("Service PID and new domain ID can not both be provided");
		}
		if (theVersion.getPid() != 0) {
			ourLog.info("Saving service version for Domain[{}/create {}] and Service[{}/create {}] with id: {}", new Object[] { theExistingDomainPid, theCreateDomainId, theExistingServicePid, theCreateServiceId, theVersion.getId() });
		} else {
			ourLog.info("Adding service version for Domain[{}/create {}] and Service[{}/create {}] with id: {}", new Object[] { theExistingDomainPid, theCreateDomainId, theExistingServicePid, theCreateServiceId, theVersion.getId() });
		}

		if (isMockMode()) {
			return getMock().addServiceVersion(theExistingDomainPid, theCreateDomainId, theExistingServicePid, theCreateServiceId, theVersion);
		}

		Long uncommittedSessionId = theVersion.getUncommittedSessionId();
		List<GResource> resList = getServiceVersionResourcesFromSession(uncommittedSessionId);
		if (resList == null) {
			ourLog.info("Unable to find a resource collection in the session with ID " + uncommittedSessionId);
			resList = new ArrayList<GResource>();
		}

		long domain;
		if (isNotBlank(theCreateDomainId)) {
			try {
				GDomain newDomain = new GDomain();
				newDomain.setId(theCreateDomainId);
				newDomain.setName(theCreateDomainId);
				domain = myAdminSvc.addDomain(newDomain).getPid();
			} catch (ProcessingException e) {
				ourLog.error("Failed to create domain " + theCreateDomainId, e);
				throw new ServiceFailureException("Failed to create domain: " + theCreateDomainId + " - " + e.getMessage());
			}
		} else {
			domain = theExistingDomainPid;
		}

		long service;
		if (isNotBlank(theCreateServiceId)) {
			try {
				service = myAdminSvc.addService(domain, theCreateServiceId, theCreateServiceId, true).getPid();
			} catch (ProcessingException e) {
				ourLog.error("Failed to create service " + theCreateServiceId, e);
				throw new ServiceFailureException("Failed to create service: " + theCreateDomainId + " - " + e.getMessage());
			}
		} else {
			service = theExistingServicePid;
		}

		BaseGServiceVersion newVersion;
		try {
			newVersion = myAdminSvc.saveServiceVersion(domain, service, theVersion, resList);
		} catch (ProcessingException e) {
			ourLog.error("Failed to add service version", e);
			throw new ServiceFailureException(e.getMessage());
		}

		AddServiceVersionResponse retVal = new AddServiceVersionResponse();

		retVal.setNewServiceVersion(newVersion);
		try {
			retVal.setNewDomain(myAdminSvc.getDomainByPid(domain));
		} catch (ProcessingException e) {
			ourLog.error("Failure when adding a service version", e);
			throw new ServiceFailureException(e.getMessage());
		}
		try {
			retVal.setNewService(myAdminSvc.getServiceByPid(service));
		} catch (ProcessingException e) {
			ourLog.error("Failed to create service version", e);
			throw new ServiceFailureException("Failed to create service version - " + e.getMessage());
		}

		return retVal;
	}

	@Override
	public BaseGServiceVersion createNewServiceVersion(ServiceProtocolEnum theProtocol, Long theDomainPid, Long theServicePid, Long theUncommittedId) {
		BaseGServiceVersion retVal = null;
		HttpSession session = getThreadLocalRequest().getSession(true);

		if (theUncommittedId != null) {
			String key = SESSION_PREFIX_UNCOMITTED_SVC_VER + theUncommittedId;
			retVal = (BaseGServiceVersion) session.getAttribute(key);
			if (retVal != null && retVal.getProtocol() == theProtocol) {
				ourLog.info("Retrieving {} Service Version with uncommitted ID[{}]", retVal.getProtocol().name(), theUncommittedId);
				return retVal;
			}
		}

		switch (theProtocol) {
		case JSONRPC20:
			retVal = new DtoServiceVersionJsonRpc20();
			break;
		case SOAP11:
			retVal = new DtoServiceVersionSoap11();
			break;
		}

		if (retVal == null) {
			throw new java.lang.IllegalStateException("Unknown type: " + theProtocol);
		}

		if (isMockMode()) {
			retVal.setHttpClientConfigPid(getMock().getDefaultHttpClientConfigPid());
			retVal.setId("1.0");
		} else {
			retVal.setHttpClientConfigPid(myAdminSvc.getDefaultHttpClientConfigPid());
			if (theDomainPid != null && theServicePid != null) {
				retVal.setId(myAdminSvc.suggestNewVersionNumber(theDomainPid, theServicePid));
			} else {
				retVal.setId("1.0");
			}
		}

		long sessionId = theUncommittedId != null ? theUncommittedId : ourNextId.getAndIncrement();
		retVal.setUncommittedSessionId(sessionId);

		String key = SESSION_PREFIX_UNCOMITTED_SVC_VER + sessionId;
		session.setAttribute(key, retVal);

		ourLog.info("Creating {} Service Version with uncommitted UNC_ID[{}]", theProtocol.name(), sessionId);

		return retVal;
	}

	@Override
	public Map<Long, GMonitorRuleFiring> getLatestFailingMonitorRuleFiringForRulePids() throws ServiceFailureException {
		if (isMockMode()) {
			return getMock().getLatestFailingMonitorRuleFiringForRulePids();
		}
		try {
			HashMap<Long, GMonitorRuleFiring> retVal = new HashMap<Long, GMonitorRuleFiring>();

			for (GMonitorRuleFiring next : myAdminSvc.loadAllActiveRuleFirings()) {
				if (retVal.containsKey(next.getRulePid())) {
					if (next.getStartDate().after(retVal.get(next.getRulePid()).getStartDate())) {
						retVal.put(next.getRulePid(), next);
					}
				} else {
					retVal.put(next.getRulePid(), next);
				}
			}

			return retVal;
		} catch (ProcessingException e) {
			ourLog.error("Failed to load library messages", e);
			throw new ServiceFailureException(e.getMessage());
		}
	}

	@Override
	public GConfig loadConfig() throws ServiceFailureException {
		if (isMockMode()) {
			return getMock().loadConfig();
		}

		try {
			return myAdminSvc.loadConfig();
		} catch (ProcessingException e) {
			ourLog.error("Failed to load config", e);
			throw new ServiceFailureException(e.getMessage());
		}
	}

	@Override
	public DtoLibraryMessage loadLibraryMessage(long theMessagePid) throws ServiceFailureException {
		if (isMockMode()) {
			return getMock().loadLibraryMessage(theMessagePid);
		}
		try {
			return myAdminSvc.getLibraryMessage(theMessagePid);
		} catch (ProcessingException e) {
			ourLog.error("Failed to load library message", e);
			throw new ServiceFailureException(e.getMessage());
		}
	}

	@Override
	public Collection<DtoLibraryMessage> loadLibraryMessages() throws ServiceFailureException {
		if (isMockMode()) {
			return getMock().loadLibraryMessages();
		}
		try {
			return myAdminSvc.loadLibraryMessages();
		} catch (ProcessingException e) {
			ourLog.error("Failed to load library messages", e);
			throw new ServiceFailureException(e.getMessage());
		}
	}

	@Override
	public Collection<DtoLibraryMessage> loadLibraryMessages(HierarchyEnum theType, long thePid) throws ServiceFailureException {
		if (isMockMode()) {
			return getMock().loadLibraryMessages(theType, thePid);
		}
		try {
			return myAdminSvc.getLibraryMessages(theType, thePid, false);
		} catch (ProcessingException e) {
			ourLog.error("Failed to load library messages", e);
			throw new ServiceFailureException(e.getMessage());
		}

	}

	@Override
	public ModelUpdateResponse loadModelUpdate(ModelUpdateRequest theRequest) throws ServiceFailureException {
		Validate.notNull(theRequest, "ModelUpdateRequest");

		ourLog.info("Requesting a model update from backend server for: " + theRequest.toString());

		long start = System.currentTimeMillis();

		ModelUpdateResponse retVal;
		try {
			if (isMockMode()) {
				retVal = getMock().loadModelUpdate(theRequest);
			} else {
				retVal = myAdminSvc.loadModelUpdate(theRequest);
			}
		} catch (Throwable e) {
			ourLog.error("Failed to load model update", e);
			throw new ServiceFailureException(e.getMessage());
		}

		long delay = System.currentTimeMillis() - start;
		ourLog.info("Loaded model update in {}ms", delay);

		if (ourLog.isTraceEnabled()) {
			for (GDomain nextDomain : retVal.getDomainList()) {
				ourLog.trace(" * Returning Domain: {}", nextDomain);
				for (GService nextSvc : nextDomain.getServiceList()) {
					for (BaseGServiceVersion nextSvcVer : nextSvc.getVersionList()) {
						ourLog.trace(" * Returning SvcVer: {}", nextSvcVer);
					}
				}
			}
		}

		if (theRequest.isLoadAuthHosts()) {
			if (retVal.getAuthenticationHostList() == null || retVal.getAuthenticationHostList().size() == 0) {
				throw new ServiceFailureException("Failed to return any authentication hosts!");
			}
		}

		return retVal;
	}

	@Override
	public List<GMonitorRuleFiring> loadMonitorRuleFirings(Long theDomainPid, Long theServicePid, Long theServiceVersionPid, int theStart) {
		if (isMockMode()) {
			return getMock().loadMonitorRuleFirings(theDomainPid, theServicePid, theServiceVersionPid, theStart);
		}
		return myAdminSvc.loadMonitorRuleFirings(theDomainPid, theServicePid, theServiceVersionPid, theStart);
	}

	@Override
	public GMonitorRuleList loadMonitorRuleList() throws ServiceFailureException {
		GMonitorRuleList retVal;

		if (isMockMode()) {
			retVal = getMock().loadMonitorRuleList();
		} else {
			try {
				retVal = myAdminSvc.loadMonitorRuleList();
			} catch (ProcessingException e) {
				ourLog.error("Failed to load monitor rules", e);
				throw new ServiceFailureException(e.getMessage());
			}
		}

		return retVal;
	}

	@Override
	public GRecentMessage loadRecentMessageForServiceVersion(long thePid) throws ServiceFailureException {
		if (isMockMode()) {
			return getMock().loadRecentMessageForServiceVersion(thePid);
		}
		try {
			return myAdminSvc.loadRecentMessageForServiceVersion(thePid);
		} catch (ProcessingException e) {
			ourLog.error("Failed to load transaction", e);
			throw new ServiceFailureException(e.getMessage());
		}
	}

	@Override
	public GRecentMessage loadRecentMessageForUser(long thePid) throws ServiceFailureException {
		if (isMockMode()) {
			return getMock().loadRecentMessageForUser(thePid);
		}
		try {
			return myAdminSvc.loadRecentMessageForUser(thePid);
		} catch (ProcessingException e) {
			ourLog.error("Failed to load transaction", e);
			throw new ServiceFailureException(e.getMessage());
		}
	}

	@Override
	public GRecentMessageLists loadRecentTransactionListForServiceVersion(long theServiceVersionPid) {
		if (isMockMode()) {
			return getMock().loadRecentTransactionListForServiceVersion(theServiceVersionPid);
		}

		return myAdminSvc.loadRecentTransactionListForServiceVersion(theServiceVersionPid);
	}

	@Override
	public GRecentMessageLists loadRecentTransactionListForuser(long thePid) {
		if (isMockMode()) {
			return getMock().loadRecentTransactionListForuser(thePid);
		}

		return myAdminSvc.loadRecentTransactionListForUser(thePid);
	}

	@Override
	public GServiceVersionDetailedStats loadServiceVersionDetailedStats(long theVersionPid) throws ServiceFailureException {
		if (isMockMode()) {
			return getMock().loadServiceVersionDetailedStats(theVersionPid);
		}

		try {
			return myAdminSvc.loadServiceVersionDetailedStats(theVersionPid);
		} catch (ProcessingException e) {
			ourLog.error("Failed to load detailed stats", e);
			throw new ServiceFailureException(e.getMessage());
		}
	}

	@Override
	public BaseGServiceVersion loadServiceVersionIntoSession(long theServiceVersionPid) throws ServiceFailureException {
		BaseGServiceVersion retVal;

		GSoap11ServiceVersionAndResources serviceAndResources;
		if (isMockMode()) {

			retVal = getMock().loadServiceVersionIntoSession(theServiceVersionPid);

			serviceAndResources = new GSoap11ServiceVersionAndResources();
			serviceAndResources.setServiceVersion(retVal);

		} else {

			try {
				serviceAndResources = myAdminSvc.loadServiceVersion(theServiceVersionPid);
			} catch (ProcessingException e) {
				ourLog.error("Failed to load service version", e);
				throw new ServiceFailureException(e.getMessage());
			}

		}

		retVal = serviceAndResources.getServiceVersion();
		retVal.setUncommittedSessionId(ourNextId.incrementAndGet());
		saveServiceVersionToSession(retVal);
		saveServiceVersionResourcesToSession(serviceAndResources);

		retVal.setDetailedStats(loadServiceVersionDetailedStats(theServiceVersionPid));

		return retVal;
	}

	@Override
	public UserAndAuthHost loadUser(long thePid, boolean theLoadStats) throws ServiceFailureException {
		if (isMockMode()) {
			return getMock().loadUser(thePid, theLoadStats);
		}

		GUser user;
		try {
			user = myAdminSvc.loadUser(thePid, theLoadStats);
			BaseGAuthHost authHost = myAdminSvc.loadAuthenticationHost(user.getAuthHostPid());
			return new UserAndAuthHost(user, authHost);
		} catch (ProcessingException e) {
			ourLog.error("Failed to load user " + thePid, e);
			throw new ServiceFailureException(e.getMessage());
		}
	}

	@Override
	public GPartialUserList loadUsers(PartialUserListRequest theRequest) throws ServiceFailureException {
		GPartialUserList retVal;
		if (isMockMode()) {
			retVal = getMock().loadUsers(theRequest);
			return retVal;
		} else {
			try {
				retVal = myAdminSvc.loadUsers(theRequest);
			} catch (ProcessingException e) {
				ourLog.error("Failed to load users", e);
				throw new ServiceFailureException(e.getMessage());
			}
		}

		// Response validation
		if (theRequest.isLoadStats()) {
			for (GUser next : retVal) {
				Validate.notNull(next.getStatsInitialized());
			}
		}

		return retVal;
	}

	@Override
	public DtoServiceVersionSoap11 loadWsdl(DtoServiceVersionSoap11 theService, String theWsdlUrl) throws ServiceFailureException {
		Validate.notNull(theService, "Service");
		Validate.notNull(theService.getUncommittedSessionId(), "Service#UncommittedSessionId");
		Validate.notBlank(theWsdlUrl, "Service");

		DtoServiceVersionSoap11 retVal;
		if (isMockMode()) {
			retVal = getMock().loadWsdl(theService, theWsdlUrl);
		} else {

			GSoap11ServiceVersionAndResources serviceAndResources;
			try {
				serviceAndResources = myAdminSvc.loadSoap11ServiceVersionFromWsdl(theService, theWsdlUrl);
			} catch (ProcessingException e) {
				ourLog.error("Failed to load service version from WSDL", e);
				throw new ServiceFailureException(e.getMessage());
			}

			saveServiceVersionResourcesToSession(serviceAndResources);

			retVal = (DtoServiceVersionSoap11) serviceAndResources.getServiceVersion();
		}

		/*
		 * Merge security
		 */
		retVal.getServerSecurityList().mergeResults(theService.getServerSecurityList());
		retVal.getClientSecurityList().mergeResults(theService.getClientSecurityList());

		saveServiceVersionToSession(retVal);
		return retVal;
	}

	@Override
	public GAuthenticationHostList removeAuthenticationHost(long thePid) throws ServiceFailureException {
		if (isMockMode()) {
			return getMock().removeAuthenticationHost(thePid);
		}

		try {
			return myAdminSvc.deleteAuthenticationHost(thePid);
		} catch (ProcessingException e) {
			ourLog.error("Failed to delete authentication host", e);
			throw new ServiceFailureException(e.getMessage());
		}
	}

	@Override
	public GDomainList removeDomain(long thePid) throws ServiceFailureException {
		ourLog.info("Removing domain: {}", thePid);

		if (isMockMode()) {
			return getMock().removeDomain(thePid);
		}

		try {
			myAdminSvc.deleteDomain(thePid);
		} catch (ProcessingException e) {
			ourLog.error("Failed to delete domain", e);
			throw new ServiceFailureException(e.getMessage());
		}

		try {
			return myAdminSvc.loadDomainList();
		} catch (ProcessingException e) {
			ourLog.error("Failed to load domain list", e);
			throw new ServiceFailureException(e.getMessage());
		}
	}

	@Override
	public GDomainList removeService(long theDomainPid, long theServicePid) throws ServiceFailureException {
		if (isMockMode()) {
			return getMock().removeService(theDomainPid, theServicePid);
		}

		ourLog.info("Removing service for DOMAIN {} SERVICE {}", theDomainPid, theServicePid);

		try {
			return myAdminSvc.deleteService(theServicePid);
		} catch (ProcessingException e) {
			ourLog.error("Failed to delete service", e);
			throw new ServiceFailureException(e.getMessage());
		}
	}

	@Override
	public GDomainList removeServiceVersion(long thePid) throws ServiceFailureException {
		Validate.greaterThanZero(thePid, "PID");
		ourLog.info("Removing service version {}", thePid);

		if (isMockMode()) {
			return getMock().removeServiceVersion(thePid);
		}

		try {
			return myAdminSvc.deleteServiceVersion(thePid);
		} catch (ProcessingException e) {
			ourLog.error("Failed to delete service version", e);
			throw new ServiceFailureException(e.getMessage());
		}

	}

	@Override
	public void reportClientError(String theMessage, Throwable theException) {
		ourLog.warn("Client error - " + theMessage, theException);
	}

	@Override
	public GServiceVersionUrl resetCircuitBreakerForServiceVersionUrl(long theUrlPid) throws ServiceFailureException {
		try {
			if (isMockMode()) {
				return getMock().resetCircuitBreakerForServiceVersionUrl(theUrlPid);
			}
			return myAdminSvc.resetCircuitBreaker(theUrlPid);
		} catch (ProcessingException e) {
			ourLog.error("Failed to reset circuit breaker", e);
			throw new ServiceFailureException(e.getMessage());
		}
	}

	@Override
	public GAuthenticationHostList saveAuthenticationHost(BaseGAuthHost theAuthHost) throws ServiceFailureException {
		if (isMockMode()) {
			return getMock().saveAuthenticationHost(theAuthHost);
		}

		if (theAuthHost.getPid() <= 0) {
			theAuthHost.setPid(0);
			ourLog.info("Saving new authentication host");
		} else {
			ourLog.info("Saving authentication host {} / {}", theAuthHost.getPid(), theAuthHost.getModuleId());
		}

		try {
			return myAdminSvc.saveAuthenticationHost(theAuthHost);
		} catch (ProcessingException e) {
			ourLog.error("Failed to save authentication host", e);
			throw new ServiceFailureException(e.getMessage());
		}

	}

	@Override
	public void saveConfig(GConfig theConfig) throws ServiceFailureException {
		ourLog.info("Saving config");

		try {
			myAdminSvc.saveConfig(theConfig);
		} catch (ProcessingException e) {
			ourLog.error("Failed to save config", e);
			throw new ServiceFailureException(e.getMessage());
		}
	}

	@Override
	public GDomainList saveDomain(GDomain theDomain) throws ServiceFailureException {
		if (isMockMode()) {
			return getMock().saveDomain(theDomain);
		}

		ourLog.info("Saving domain with PID {}", theDomain.getPid());

		try {
			return myAdminSvc.saveDomain(theDomain);
		} catch (ProcessingException e) {
			ourLog.error("Failed to save domain", e);
			throw new ServiceFailureException(e.getMessage());
		}
	}

	@Override
	public void saveLibraryMessage(DtoLibraryMessage theMessage) throws ServiceFailureException {
		if (isMockMode()) {
			getMock().saveLibraryMessage(theMessage);
			return;
		}

		try {
			myAdminSvc.saveLibraryMessage(theMessage);
		} catch (ProcessingException e) {
			ourLog.error("Failed to save library message", e);
			throw new ServiceFailureException(e.getMessage());
		}
	}

	@Override
	public GMonitorRuleList saveMonitorRule(BaseGMonitorRule theRule) throws ServiceFailureException {
		try {
			if (isMockMode()) {
				getMock().saveMonitorRule(theRule);
			} else {
				myAdminSvc.saveMonitorRule(theRule);
			}
			return loadMonitorRuleList();
		} catch (ServiceFailureException e) {
			ourLog.error("Failed to save rule", e);
			throw new ServiceFailureException(e.getMessage());
		} catch (ProcessingException e) {
			ourLog.error("Failed to save rule", e);
			throw new ServiceFailureException(e.getMessage());
		}
	}

	@Override
	public GDomainList saveService(GService theService) throws ServiceFailureException {
		if (isMockMode()) {
			return getMock().saveService(theService);
		}

		try {
			return myAdminSvc.saveService(theService);
		} catch (ProcessingException e) {
			ourLog.error("Failed to save service", e);
			throw new ServiceFailureException(e.getMessage());
		}
	}

	@Override
	public void saveServiceVersionToSession(BaseGServiceVersion theServiceVersion) {
		Validate.notNull(theServiceVersion, "ServiceVersion");
		Validate.notNull(theServiceVersion.getUncommittedSessionId(), "ServiceVersion#UncommittedSessionId");

		ourLog.info("Saving Service Version UNC_ID[{}] PID[{}] to Session", theServiceVersion.getUncommittedSessionId(), theServiceVersion.getPidOrNull());

		String key = SESSION_PREFIX_UNCOMITTED_SVC_VER + theServiceVersion.getUncommittedSessionId();
		getThreadLocalRequest().getSession(true).setAttribute(key, theServiceVersion);
	}

	@Override
	public void saveUser(GUser theUser) throws ServiceFailureException {
		ourLog.info("Saving user {} / {}", theUser.getPid(), theUser.getUsername());

		if (isMockMode()) {
			getMock().saveUser(theUser);
			return;
		}

		try {
			myAdminSvc.saveUser(theUser);
		} catch (ProcessingException e) {
			ourLog.error("Failed to save user", e);
			throw new ServiceFailureException(e.getMessage());
		}

		ourLog.info("Done saving user {} / {}", theUser.getPid(), theUser.getUsername());
	}

	@Override
	public GServiceVersionSingleFireResponse testServiceVersionWithSingleMessage(String theMessageText, String theContentType, long thePid) throws ServiceFailureException {
		try {
			if (isMockMode()) {
				return getMock().testServiceVersionWithSingleMessage(theMessageText, theContentType, thePid);
			}
			return myAdminSvc.testServiceVersionWithSingleMessage(theMessageText, theContentType, thePid, "ServiceRetriever Admin Console");
		} catch (ProcessingException e) {
			ourLog.error("Failed to test service version", e);
			throw new ServiceFailureException(e.getMessage());
		}
	}

	@SuppressWarnings("unchecked")
	private List<GResource> getServiceVersionResourcesFromSession(long theServiceVersionUncommittedSessionId) {
		String key = SESSION_PREFIX_UNCOMITTED_SVC_VER_RES + theServiceVersionUncommittedSessionId;
		return (List<GResource>) getThreadLocalRequest().getSession(true).getAttribute(key);
	}

	private void saveServiceVersionResourcesToSession(GSoap11ServiceVersionAndResources theServiceAndResources) {
		BaseGServiceVersion serviceVersion = theServiceAndResources.getServiceVersion();
		ourLog.info("Storing service resource collection to temporary session with id: " + serviceVersion.getUncommittedSessionId());
		String key = SESSION_PREFIX_UNCOMITTED_SVC_VER_RES + theServiceAndResources.getServiceVersion().getUncommittedSessionId();
		getThreadLocalRequest().getSession(true).setAttribute(key, theServiceAndResources.getResource());
	}

	/**
	 * For unit test only
	 */
	void setAdminSvc(IAdminServiceLocal theAdminSvc) {
		myAdminSvc = theAdminSvc;
	}

}
