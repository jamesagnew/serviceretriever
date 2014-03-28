package net.svcret.admin.server.rpc;

import static org.apache.commons.lang3.StringUtils.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;

import net.svcret.admin.api.AdminServiceProvider;
import net.svcret.admin.api.IAdminServiceLocal;
import net.svcret.admin.api.ProcessingException;
import net.svcret.admin.api.UnexpectedFailureException;
import net.svcret.admin.api.UnknownPidException;
import net.svcret.admin.client.rpc.ModelUpdateService;
import net.svcret.admin.shared.AddServiceVersionResponse;
import net.svcret.admin.shared.ServiceFailureException;
import net.svcret.admin.shared.model.BaseDtoAuthenticationHost;
import net.svcret.admin.shared.model.BaseDtoMonitorRule;
import net.svcret.admin.shared.model.BaseDtoObject;
import net.svcret.admin.shared.model.BaseDtoServiceVersion;
import net.svcret.admin.shared.model.DtoAuthenticationHostList;
import net.svcret.admin.shared.model.DtoConfig;
import net.svcret.admin.shared.model.DtoDomain;
import net.svcret.admin.shared.model.DtoDomainList;
import net.svcret.admin.shared.model.DtoHttpClientConfig;
import net.svcret.admin.shared.model.DtoLibraryMessage;
import net.svcret.admin.shared.model.DtoMonitorRuleActiveCheck;
import net.svcret.admin.shared.model.DtoMonitorRuleActiveCheckOutcome;
import net.svcret.admin.shared.model.DtoNodeStatusAndStatisticsList;
import net.svcret.admin.shared.model.DtoServiceVersionHl7OverHttp;
import net.svcret.admin.shared.model.DtoServiceVersionJsonRpc20;
import net.svcret.admin.shared.model.DtoServiceVersionRest;
import net.svcret.admin.shared.model.DtoServiceVersionSoap11;
import net.svcret.admin.shared.model.DtoServiceVersionVirtual;
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
import net.svcret.admin.shared.util.Validate;

/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class ModelUpdateServiceImpl extends BaseRpcServlet implements ModelUpdateService {

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(ModelUpdateServiceImpl.class);
	private static final AtomicLong ourNextId = new AtomicLong(0L);
	private static final String SESSION_PREFIX_UNCOMITTED_SVC_VER = "UNC_SVC_VER_";
	private static final String SESSION_PREFIX_UNCOMITTED_SVC_VER_RES = "UNC_SVC_VER_RES_";

	private IAdminServiceLocal myAdminSvc;

	@Override
	public void init() throws ServletException {
		super.init();
		
		myAdminSvc = AdminServiceProvider.getInstance().getAdminService();
	}

	@Override
	public DtoDomain addDomain(DtoDomain theDomain) throws ServiceFailureException {
		ourLog.info("Adding domain {}/{}", theDomain.getId(), theDomain.getName());

		if (isMockMode()) {
			return getMock().addDomain(theDomain);
		}

		try {
			return myAdminSvc.addDomain(theDomain);
		} catch (ProcessingException e) {
			ourLog.warn("Failed to add domain", e);
			throw new ServiceFailureException("Failed to add domain: " + e.getMessage());
		} catch (UnexpectedFailureException e) {
			ourLog.warn("Failed to add domain", e);
			throw new ServiceFailureException("Failed to add domain: " + e.getMessage());
		}
	}

	@Override
	public GService addService(long theDomainPid, GService theService) throws ServiceFailureException {
		ourLog.info("Adding new service to domain {} with ID[{}] and name: {}", new Object[] { theDomainPid, theService.getId(), theService.getName() });

		if (isMockMode()) {
			return getMock().addService(theDomainPid, theService);
		}

		try {
			return myAdminSvc.addService(theDomainPid, theService);
		} catch (ProcessingException e) {
			ourLog.warn("Failed to add domain", e);
			throw new ServiceFailureException("Failed to add domain: " + e.getMessage());
		} catch (UnexpectedFailureException e) {
			ourLog.warn("Failed to add domain", e);
			throw new ServiceFailureException("Failed to add domain: " + e.getMessage());
		}
	}

	@Override
	public AddServiceVersionResponse addServiceVersion(Long theExistingDomainPid, String theCreateDomainId, Long theExistingServicePid, String theCreateServiceId, BaseDtoServiceVersion theVersion) throws ServiceFailureException {
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
			resList = new ArrayList<>();
		}

		long domain;
		if (isNotBlank(theCreateDomainId)) {
			try {
				DtoDomain newDomain = new DtoDomain();
				newDomain.setId(theCreateDomainId);
				newDomain.setName(theCreateDomainId);
				domain = myAdminSvc.addDomain(newDomain).getPid();
			} catch (ProcessingException e) {
				ourLog.error("Failed to create domain " + theCreateDomainId, e);
				throw new ServiceFailureException("Failed to create domain: " + theCreateDomainId + " - " + e.getMessage());
			} catch (UnexpectedFailureException e) {
				ourLog.error("Failed to create domain " + theCreateDomainId, e);
				throw new ServiceFailureException("Failed to create domain: " + theCreateDomainId + " - " + e.getMessage());
			}
		} else {
			domain = theExistingDomainPid;
		}

		long service;
		if (isNotBlank(theCreateServiceId)) {
			try {
				GService svc = new GService();
				svc.setId(theCreateServiceId);
				svc.setName(theCreateServiceId);
				svc.setActive(true);
				service = myAdminSvc.addService(domain, svc).getPid();
			} catch (ProcessingException e) {
				ourLog.error("Failed to create service " + theCreateServiceId, e);
				throw new ServiceFailureException("Failed to create service: " + theCreateDomainId + " - " + e.getMessage());
			} catch (UnexpectedFailureException e) {
				ourLog.error("Failed to create service " + theCreateServiceId, e);
				throw new ServiceFailureException("Failed to create service: " + theCreateDomainId + " - " + e.getMessage());
			}
		} else {
			service = theExistingServicePid;
		}

		BaseDtoServiceVersion newVersion;
		try {
			newVersion = myAdminSvc.saveServiceVersion(domain, service, theVersion, resList);
		} catch (ProcessingException e) {
			ourLog.error("Failed to add service version", e);
			throw new ServiceFailureException(e.getMessage());
		} catch (UnexpectedFailureException e) {
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
		} catch (UnexpectedFailureException e) {
			ourLog.error("Failure when adding a service version", e);
			throw new ServiceFailureException(e.getMessage());
		}

		return retVal;
	}

	@Override
	public BaseDtoServiceVersion createNewServiceVersion(ServiceProtocolEnum theProtocol, Long theDomainPid, Long theServicePid, Long theUncommittedId) {
		BaseDtoServiceVersion retVal = null;
		HttpSession session = getThreadLocalRequest().getSession(true);

		if (theUncommittedId != null) {
			String key = SESSION_PREFIX_UNCOMITTED_SVC_VER + theUncommittedId;
			retVal = (BaseDtoServiceVersion) session.getAttribute(key);
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
		case HL7OVERHTTP:
			retVal= new DtoServiceVersionHl7OverHttp();
			break;
		case VIRTUAL:
			retVal= new DtoServiceVersionVirtual();
			break;
		case REST:
			retVal= new DtoServiceVersionRest();
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
			HashMap<Long, GMonitorRuleFiring> retVal = new HashMap<>();

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
	public DtoConfig loadConfig() throws ServiceFailureException {
		if (isMockMode()) {
			return getMock().loadConfig();
		}

		try {
			return myAdminSvc.loadConfig();
		} catch (UnexpectedFailureException e) {
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
			for (DtoDomain nextDomain : retVal.getDomainList()) {
				ourLog.trace(" * Returning Domain: {}", nextDomain);
				for (GService nextSvc : nextDomain.getServiceList()) {
					for (BaseDtoServiceVersion nextSvcVer : nextSvc.getVersionList()) {
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
			} catch (UnexpectedFailureException e) {
				ourLog.error("Failed to load monitor rules", e);
				throw new ServiceFailureException(e.getMessage());
			}
		}

		return retVal;
	}

	@Override
	public GRecentMessage loadRecentMessageForServiceVersion(long thePid) throws ServiceFailureException {
		if (isMockMode()) {
			if (Math.random() < 0.3) {
				return null;
			}
			return getMock().loadRecentMessageForServiceVersion(thePid);
		}
		try {
			return myAdminSvc.loadRecentMessageForServiceVersion(thePid);
		} catch (UnknownPidException e) {
			ourLog.error("Failed to load transaction", e);
			return null;
		}
	}

	@Override
	public GRecentMessage loadRecentMessageForUser(long thePid) throws ServiceFailureException {
		if (isMockMode()) {
			if (Math.random() < 0.3) {
				return null;
			}
			return getMock().loadRecentMessageForUser(thePid);
		}
		try {
			return myAdminSvc.loadRecentMessageForUser(thePid);
		} catch (UnknownPidException e) {
			ourLog.error("Failed to load transaction", e);
			return null;
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
		} catch (UnexpectedFailureException e) {
			ourLog.error("Failed to load detailed stats", e);
			throw new ServiceFailureException(e.getMessage());
		}
	}

	@Override
	public BaseDtoServiceVersion loadServiceVersionIntoSession(long theServiceVersionPid) throws ServiceFailureException {
		return loadServiceVersionIntoSession(theServiceVersionPid, false);
	}

	private BaseDtoServiceVersion loadServiceVersionIntoSession(long theServiceVersionPid, boolean theClearPids) throws ServiceFailureException {
		BaseDtoServiceVersion retVal;

		GSoap11ServiceVersionAndResources serviceAndResources;
		if (isMockMode()) {

			retVal = getMock().loadServiceVersionIntoSession(theServiceVersionPid);

			serviceAndResources = new GSoap11ServiceVersionAndResources();
			serviceAndResources.setServiceVersion(retVal);

		} else {

			try {
				serviceAndResources = myAdminSvc.loadServiceVersion(theServiceVersionPid);
			} catch (UnexpectedFailureException e) {
				ourLog.error("Failed to load service version", e);
				throw new ServiceFailureException(e.getMessage());
			}

		}

		retVal = serviceAndResources.getServiceVersion();
		retVal.setUncommittedSessionId(ourNextId.incrementAndGet());
		saveServiceVersionToSession(retVal);
		saveServiceVersionResourcesToSession(serviceAndResources);

		if (theClearPids) {
			for (GResource next : serviceAndResources.getResource()) {
				next.clearPid();
			}
			retVal.clearPid();
			for (BaseDtoObject next : retVal.getMethodList()) {
				next.clearPid();
			}
			for (BaseDtoObject next : retVal.getClientSecurityList()) {
				next.clearPid();
			}
			for (BaseDtoObject next : retVal.getServerSecurityList()) {
				next.clearPid();
			}
			for (BaseDtoObject next : retVal.getUrlList()) {
				next.clearPid();
			}
		}else {
			retVal.setDetailedStats(loadServiceVersionDetailedStats(theServiceVersionPid));
		}

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
			BaseDtoAuthenticationHost authHost = myAdminSvc.loadAuthenticationHost(user.getAuthHostPid());
			return new UserAndAuthHost(user, authHost);
		} catch (ProcessingException e) {
			ourLog.error("Failed to load user " + thePid, e);
			throw new ServiceFailureException(e.getMessage());
		} catch (UnexpectedFailureException e) {
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
			} catch (UnexpectedFailureException e) {
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
	public DtoServiceVersionSoap11 loadWsdl(DtoServiceVersionSoap11 theService, DtoHttpClientConfig theClientConfig, String theWsdlUrl) throws ServiceFailureException {
		Validate.notNull(theService, "Service");
		Validate.notNull(theService.getUncommittedSessionId(), "Service#UncommittedSessionId");
		Validate.notBlank(theWsdlUrl, "Service");

		DtoServiceVersionSoap11 retVal;
		if (isMockMode()) {
			retVal = getMock().loadWsdl(theService, theClientConfig, theWsdlUrl);
		} else {

			GSoap11ServiceVersionAndResources serviceAndResources;
			try {
				serviceAndResources = myAdminSvc.loadSoap11ServiceVersionFromWsdl(theService, theClientConfig, theWsdlUrl);
			} catch (ProcessingException e) {
				ourLog.error("Failed to load service version from WSDL", e);
				throw new ServiceFailureException(e.getMessage());
			} catch (UnexpectedFailureException e) {
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
	public DtoAuthenticationHostList removeAuthenticationHost(long thePid) throws ServiceFailureException {
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
	public DtoDomainList removeDomain(long thePid) throws ServiceFailureException {
		ourLog.info("Removing domain: {}", thePid);

		if (isMockMode()) {
			return getMock().removeDomain(thePid);
		}

		try {
			return myAdminSvc.deleteDomain(thePid);
		} catch (ProcessingException e) {
			ourLog.error("Failed to delete domain", e);
			throw new ServiceFailureException(e.getMessage());
		} catch (UnexpectedFailureException e) {
			ourLog.error("Failed to delete domain", e);
			throw new ServiceFailureException(e.getMessage());
		}

	}

	@Override
	public DtoDomainList removeService(long theDomainPid, long theServicePid) throws ServiceFailureException {
		if (isMockMode()) {
			return getMock().removeService(theDomainPid, theServicePid);
		}

		ourLog.info("Removing service for DOMAIN {} SERVICE {}", theDomainPid, theServicePid);

		try {
			return myAdminSvc.deleteService(theServicePid);
		} catch (ProcessingException e) {
			ourLog.error("Failed to delete service", e);
			throw new ServiceFailureException(e.getMessage());
		} catch (UnexpectedFailureException e) {
			ourLog.error("Failed to delete service", e);
			throw new ServiceFailureException(e.getMessage());
		}
	}

	@Override
	public DtoDomainList removeServiceVersion(long thePid) throws ServiceFailureException {
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
		} catch (UnexpectedFailureException e) {
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
		} catch (UnexpectedFailureException e) {
			ourLog.error("Failed to reset circuit breaker", e);
			throw new ServiceFailureException(e.getMessage());
		}
	}

	@Override
	public DtoAuthenticationHostList saveAuthenticationHost(BaseDtoAuthenticationHost theAuthHost) throws ServiceFailureException {
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
	public void saveConfig(DtoConfig theConfig) throws ServiceFailureException {
		ourLog.info("Saving config");

		try {
			myAdminSvc.saveConfig(theConfig);
		} catch (UnexpectedFailureException e) {
			ourLog.error("Failed to save config", e);
			throw new ServiceFailureException(e.getMessage());
		}
	}

	@Override
	public DtoDomainList saveDomain(DtoDomain theDomain) throws ServiceFailureException {
		if (isMockMode()) {
			return getMock().saveDomain(theDomain);
		}

		ourLog.info("Saving domain with PID {}", theDomain.getPid());

		try {
			return myAdminSvc.saveDomain(theDomain);
		} catch (ProcessingException e) {
			ourLog.error("Failed to save domain", e);
			throw new ServiceFailureException(e.getMessage());
		} catch (UnexpectedFailureException e) {
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
	public GMonitorRuleList saveMonitorRule(BaseDtoMonitorRule theRule) throws ServiceFailureException {
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
		} catch (UnexpectedFailureException e) {
			ourLog.error("Failed to save rule", e);
			throw new ServiceFailureException(e.getMessage());
		}
	}

	@Override
	public DtoDomainList saveService(GService theService) throws ServiceFailureException {
		if (isMockMode()) {
			return getMock().saveService(theService);
		}

		try {
			return myAdminSvc.saveService(theService);
		} catch (ProcessingException e) {
			ourLog.error("Failed to save service", e);
			throw new ServiceFailureException(e.getMessage());
		} catch (UnexpectedFailureException e) {
			ourLog.error("Failed to save service", e);
			throw new ServiceFailureException(e.getMessage());
		}
	}

	@Override
	public void saveServiceVersionToSession(BaseDtoServiceVersion theServiceVersion) {
		Validate.notNull(theServiceVersion, "ServiceVersion");
		Validate.notNull(theServiceVersion.getUncommittedSessionId(), "ServiceVersion#UncommittedSessionId");

		ourLog.info("Saving Service Version UNC_ID[{}] PID[{}] to Session", theServiceVersion.getUncommittedSessionId(), theServiceVersion.getPidOrNull());

		String key = SESSION_PREFIX_UNCOMITTED_SVC_VER + theServiceVersion.getUncommittedSessionId();
		getThreadLocalRequest().getSession(true).setAttribute(key, theServiceVersion);
	}

	@Override
	public GUser saveUser(GUser theUser) throws ServiceFailureException {
		ourLog.info("Saving user {} / {}", theUser.getPid(), theUser.getUsername());

		if (isMockMode()) {
			return getMock().saveUser(theUser);
		}

		GUser retVal;
		try {
			retVal = myAdminSvc.saveUser(theUser);
		} catch (ProcessingException e) {
			ourLog.error("Failed to save user", e);
			throw new ServiceFailureException(e.getMessage());
		} catch (UnexpectedFailureException e) {
			ourLog.error("Failed to save user", e);
			throw new ServiceFailureException(e.getMessage());
		}

		ourLog.info("Done saving user {} / {}", theUser.getPid(), theUser.getUsername());
		return retVal;
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
		BaseDtoServiceVersion serviceVersion = theServiceAndResources.getServiceVersion();
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

	@Override
	public BaseDtoMonitorRule loadMonitorRule(long theRulePid) {
		if (isMockMode()) {
			return getMock().loadMonitorRule(theRulePid);
		}
		return myAdminSvc.loadMonitorRuleAndDetailedSatistics(theRulePid);
	}

	@Override
	public DtoMonitorRuleActiveCheck executeMonitorRuleActiveCheck(DtoMonitorRuleActiveCheck theCheck) throws ServiceFailureException {
		if (isMockMode()) {
			return getMock().executeMonitorRuleActiveCheck(theCheck);
		}
		try {
			return myAdminSvc.executeMonitorRuleActiveCheck(theCheck);
		} catch (ProcessingException e) {
			ourLog.error("Failed to execute active check", e);
			throw new ServiceFailureException(e.getMessage());
		}
	}

	@Override
	public BaseDtoServiceVersion cloneServiceVersion(long thePidToClone) throws ServiceFailureException {
		return loadServiceVersionIntoSession(thePidToClone,true);
	}

	@Override
	public DtoMonitorRuleActiveCheckOutcome loadMonitorRuleActiveCheckOutcomeDetails(long thePid) throws ServiceFailureException {
		if (isMockMode()) {
			return getMock().loadMonitorRuleActiveCheckOutcomeDetails(thePid);
		}
		try {
			return myAdminSvc.loadMonitorRuleActiveCheckOutcomeDetails(thePid);
		} catch (UnexpectedFailureException e) {
			ourLog.error("Failed to load outcome details", e);
			throw new ServiceFailureException(e.getMessage());
		}
	}

	@Override
	public DtoNodeStatusAndStatisticsList loadNodeListAndStatistics() {
		if (isMockMode()) {
			return getMock().loadNodeListAndStatistics();
		}
		return myAdminSvc.loadAllNodeStatuses();
	}

	@Override
	public void removeUser(long thePid) throws ServiceFailureException {
		if (isMockMode()) {
			getMock().removeUser(thePid);
		}
		
		try {
			myAdminSvc.deleteUser(thePid);
		} catch (ProcessingException e) {
			ourLog.error("Failed to load outcome details", e);
			throw new ServiceFailureException(e.getMessage());
		}
	}

}
