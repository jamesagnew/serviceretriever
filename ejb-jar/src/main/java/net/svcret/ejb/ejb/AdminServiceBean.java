package net.svcret.ejb.ejb;

import java.io.IOException;
import java.io.StringReader;
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

import net.svcret.admin.shared.enm.ResponseTypeEnum;
import net.svcret.admin.shared.enm.ServerSecurityModeEnum;
import net.svcret.admin.shared.model.AuthorizationOutcomeEnum;
import net.svcret.admin.shared.model.BaseGAuthHost;
import net.svcret.admin.shared.model.BaseGClientSecurity;
import net.svcret.admin.shared.model.BaseGDashboardObjectWithUrls;
import net.svcret.admin.shared.model.BaseGMonitorRule;
import net.svcret.admin.shared.model.BaseGServerSecurity;
import net.svcret.admin.shared.model.BaseGServiceVersion;
import net.svcret.admin.shared.model.DtoLibraryMessage;
import net.svcret.admin.shared.model.DtoMonitorRuleActive;
import net.svcret.admin.shared.model.DtoMonitorRuleActiveCheck;
import net.svcret.admin.shared.model.GAuthenticationHostList;
import net.svcret.admin.shared.model.GConfig;
import net.svcret.admin.shared.model.GDomain;
import net.svcret.admin.shared.model.GDomainList;
import net.svcret.admin.shared.model.GHttpBasicAuthServerSecurity;
import net.svcret.admin.shared.model.GHttpClientConfig;
import net.svcret.admin.shared.model.GHttpClientConfigList;
import net.svcret.admin.shared.model.GLdapAuthHost;
import net.svcret.admin.shared.model.GLocalDatabaseAuthHost;
import net.svcret.admin.shared.model.GMonitorRuleAppliesTo;
import net.svcret.admin.shared.model.GMonitorRuleFiring;
import net.svcret.admin.shared.model.GMonitorRuleFiringProblem;
import net.svcret.admin.shared.model.GMonitorRuleList;
import net.svcret.admin.shared.model.GMonitorRulePassive;
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
import net.svcret.admin.shared.model.GServiceVersionSingleFireResponse;
import net.svcret.admin.shared.model.GServiceVersionUrl;
import net.svcret.admin.shared.model.GSoap11ServiceVersion;
import net.svcret.admin.shared.model.GSoap11ServiceVersionAndResources;
import net.svcret.admin.shared.model.GThrottle;
import net.svcret.admin.shared.model.GUrlStatus;
import net.svcret.admin.shared.model.GUser;
import net.svcret.admin.shared.model.GUserDomainPermission;
import net.svcret.admin.shared.model.GUserList;
import net.svcret.admin.shared.model.GUserServicePermission;
import net.svcret.admin.shared.model.GUserServiceVersionMethodPermission;
import net.svcret.admin.shared.model.GUserServiceVersionPermission;
import net.svcret.admin.shared.model.GWsSecServerSecurity;
import net.svcret.admin.shared.model.GWsSecUsernameTokenClientSecurity;
import net.svcret.admin.shared.model.HierarchyEnum;
import net.svcret.admin.shared.model.ModelUpdateRequest;
import net.svcret.admin.shared.model.ModelUpdateResponse;
import net.svcret.admin.shared.model.Pair;
import net.svcret.admin.shared.model.PartialUserListRequest;
import net.svcret.admin.shared.model.StatusEnum;
import net.svcret.admin.shared.model.TimeRange;
import net.svcret.ejb.api.HttpRequestBean;
import net.svcret.ejb.api.IAdminService;
import net.svcret.ejb.api.IConfigService;
import net.svcret.ejb.api.IDao;
import net.svcret.ejb.api.IMonitorService;
import net.svcret.ejb.api.IRuntimeStatus;
import net.svcret.ejb.api.IScheduler;
import net.svcret.ejb.api.ISecurityService;
import net.svcret.ejb.api.IServiceInvokerSoap11;
import net.svcret.ejb.api.IServiceOrchestrator;
import net.svcret.ejb.api.IServiceOrchestrator.SidechannelOrchestratorResponseBean;
import net.svcret.ejb.api.IServiceRegistry;
import net.svcret.ejb.api.RequestType;
import net.svcret.ejb.api.StatusesBean;
import net.svcret.ejb.ex.ProcessingException;
import net.svcret.ejb.model.entity.BasePersAuthenticationHost;
import net.svcret.ejb.model.entity.BasePersMethodInvocationStats;
import net.svcret.ejb.model.entity.BasePersMonitorRule;
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
import net.svcret.ejb.model.entity.PersLibraryMessage;
import net.svcret.ejb.model.entity.PersLibraryMessageAppliesTo;
import net.svcret.ejb.model.entity.PersMonitorAppliesTo;
import net.svcret.ejb.model.entity.PersMonitorRuleActive;
import net.svcret.ejb.model.entity.PersMonitorRuleActiveCheck;
import net.svcret.ejb.model.entity.PersMonitorRuleFiring;
import net.svcret.ejb.model.entity.PersMonitorRuleFiringProblem;
import net.svcret.ejb.model.entity.PersMonitorRuleNotifyContact;
import net.svcret.ejb.model.entity.PersMonitorRulePassive;
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

import com.google.common.annotations.VisibleForTesting;

@TransactionAttribute(TransactionAttributeType.REQUIRED)
@Stateless
public class AdminServiceBean implements IAdminService {

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(AdminServiceBean.class);

	@EJB
	private IConfigService myConfigSvc;

	@EJB
	private IDao myDao;

	@EJB
	private IServiceInvokerSoap11 myInvokerSoap11;

	@EJB
	private IMonitorService myMonitorSvc;

	@EJB
	private IServiceOrchestrator myOrchestrator;

	@EJB
	private IScheduler myScheduler;

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

		domain.merge(fromUi(theDomain));

		domain = myServiceRegistry.saveDomain(domain);

		return toUi(domain, false, null);
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

		return toUi(service, false, null);
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
	public byte[] createWsdlBundle(long theServiceVersionPid) throws ProcessingException, IOException {
		BasePersServiceVersion svcVer = myServiceRegistry.getServiceVersionByPid(theServiceVersionPid);
		if (svcVer == null) {
			throw new IllegalArgumentException("Unknown version: " + theServiceVersionPid);
		}

		switch (svcVer.getProtocol()) {
		case SOAP11:
			return myInvokerSoap11.createWsdlBundle((PersServiceVersionSoap11) svcVer);
		case JSONRPC20:
			break;
		}

		throw new IllegalArgumentException("Service " + theServiceVersionPid + " is of type " + svcVer.getProtocol() + " which does not support WSDL bundles");
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

		domain.loadAllAssociations();

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
	public GDomainList deleteServiceVersion(long thePid) throws ProcessingException {
		ourLog.info("Deleting service version {}", thePid);

		BasePersServiceVersion sv = myDao.getServiceVersionByPid(thePid);
		if (sv == null) {
			throw new ProcessingException("Unknown service version ID:" + thePid);
		}

		myDao.deleteServiceVersion(sv);

		return loadDomainList();
	}

	public void deleteUser(long thePid) throws ProcessingException {
		ourLog.info("Deleting user {}", thePid);

		PersUser user = myDao.getUser(thePid);
		if (user == null) {
			throw new ProcessingException("Unknown user: " + thePid);
		}

		myDao.deleteUser(user);
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
			return loadDomain(domain, empty, empty, empty, empty, null);
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
	public DtoLibraryMessage getLibraryMessage(long theMessagePid) throws ProcessingException {
		return toUi(myDao.getLibraryMessageByPid(theMessagePid), true);
	}

	@Override
	public Collection<DtoLibraryMessage> getLibraryMessages(HierarchyEnum theType, long thePid, boolean theLoadContents) throws ProcessingException {

		Collection<PersLibraryMessage> msgs=null;
		switch (theType) {
		case DOMAIN:
			msgs = myDao.getLibraryMessagesWhichApplyToDomain(thePid);
			break;
		case SERVICE:
			msgs = myDao.getLibraryMessagesWhichApplyToService(thePid);
			break;
		case VERSION:
			msgs = myDao.getLibraryMessagesWhichApplyToServiceVersion(thePid);
			break;
		case METHOD:
			break;
		}

		if (msgs == null) {
			throw new IllegalArgumentException("Invalid type: " + theType);
		}

		return toUiCollectionLibraryMessages(msgs, theLoadContents);
	}

	@Override
	public Collection<DtoLibraryMessage> getLibraryMessagesForService(long thePid, boolean theLoadContents) throws ProcessingException {
		Collection<PersLibraryMessage> msgs = myDao.getLibraryMessagesWhichApplyToService(thePid);
		return toUiCollectionLibraryMessages(msgs, theLoadContents);
	}

	@Override
	public GService getServiceByPid(long theService) throws ProcessingException {
		PersService service = myDao.getServiceByPid(theService);
		if (service != null) {
			return toUi(service, false, null);
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
		return loadDomainList(set, set, set, set, null);
	}

	@Override
	public Collection<DtoLibraryMessage> loadLibraryMessages() throws ProcessingException {
		ArrayList<DtoLibraryMessage> retVal = new ArrayList<DtoLibraryMessage>();
		for (PersLibraryMessage next : myDao.loadLibraryMessages()) {
			retVal.add( toUi(next,false));
		}
		return retVal;
	}

	@Override
	public ModelUpdateResponse loadModelUpdate(ModelUpdateRequest theRequest) throws ProcessingException {

		ourLog.debug("Beginning loadModelUpdate - Going to flush stats");
		myScheduler.flushInMemoryStatisticsUnlessItHasHappenedVeryRecently();
		ourLog.debug("Done flushing stats");

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

		StatusesBean statuses = null;
		if (hasAnything(loadDomStats, loadSvcStats, loadVerStats, loadVerMethodStats)) {
			statuses = myDao.loadAllStatuses();
		}

		GDomainList domainList = loadDomainList(loadDomStats, loadSvcStats, loadVerStats, loadVerMethodStats, statuses);

		retVal.setDomainList(domainList);

		return retVal;
	}

	@Override
	public List<GMonitorRuleFiring> loadMonitorRuleFirings(Long theDomainPid, Long theServicePid, Long theServiceVersionPid, int theStart) {

		Set<BasePersServiceVersion> allSvcVers;
		if (theDomainPid != null) {
			assert theServicePid == null;
			assert theServiceVersionPid == null;
			PersDomain domain = myServiceRegistry.getDomainByPid(theDomainPid);
			allSvcVers = domain.getAllServiceVersions();
		} else if (theServicePid != null) {
			assert theDomainPid == null;
			assert theServiceVersionPid == null;
			PersService service = myServiceRegistry.getServiceByPid(theServicePid);
			allSvcVers = service.getAllServiceVersions();
		} else if (theServiceVersionPid != null) {
			assert theServicePid == null;
			assert theDomainPid == null;
			allSvcVers = Collections.singleton(myServiceRegistry.getServiceVersionByPid(theServiceVersionPid));
		} else {
			throw new NullPointerException("No domain/service/svcver pid provided!");
		}

		List<GMonitorRuleFiring> retVal = new ArrayList<GMonitorRuleFiring>();
		List<PersMonitorRuleFiring> firings = myDao.loadMonitorRuleFirings(allSvcVers, theStart);
		for (PersMonitorRuleFiring next : firings) {
			retVal.add(toUi(next));
		}

		// Set<Long> svcVerPids = new HashSet<Long>();
		// for (BasePersServiceVersion basePersServiceVersion : allSvcVers) {
		//
		// }

		return retVal;
	}

	@Override
	public GMonitorRuleList loadMonitorRuleList() throws ProcessingException {
		GMonitorRuleList retVal = new GMonitorRuleList();

		Collection<BasePersMonitorRule> allRules = myDao.getMonitorRules();
		for (BasePersMonitorRule persMonitorRule : allRules) {
			retVal.add(toUi(persMonitorRule));
		}

		return retVal;
	}

	@Override
	public GRecentMessage loadRecentMessageForServiceVersion(long thePid) throws ProcessingException {
		BasePersRecentMessage msg = myDao.loadRecentMessageForServiceVersion(thePid);
		if (msg == null) {
			throw new ProcessingException("Unable to find transaction with PID " + thePid + ". Maybe it has been purged?");
		}
		return toUi(msg, true);
	}

	@Override
	public GRecentMessage loadRecentMessageForUser(long thePid) throws ProcessingException {
		BasePersRecentMessage msg = myDao.loadRecentMessageForUser(thePid);
		if (msg == null) {
			throw new ProcessingException("Unable to find transaction with PID " + thePid + ". Maybe it has been purged?");
		}
		return toUi(msg, true);
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

	@Override
	public GSoap11ServiceVersionAndResources loadServiceVersion(long theServiceVersionPid) throws ProcessingException {
		BasePersServiceVersion svcVer = myDao.getServiceVersionByPid(theServiceVersionPid);
		if (svcVer == null) {
			throw new ProcessingException("Unknown service version PID: " + theServiceVersionPid);
		}

		Set<Long> methodPids = new HashSet<Long>();
		for (PersServiceVersionMethod next : svcVer.getMethods()) {
			methodPids.add(next.getPid());
		}

		StatusesBean statuses = myDao.loadAllStatuses();

		BaseGServiceVersion uiService = toUi(svcVer, true, methodPids, statuses);
		GSoap11ServiceVersionAndResources retVal = toUi(uiService, svcVer);
		return retVal;
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
				public void withStats(int theIndex, BasePersMethodInvocationStats theStats) {
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

	@Override
	public List<GUrlStatus> loadServiceVersionUrlStatuses(long theServiceVersionPid) {
		ourLog.info("Loading Service Version URL statuses for ServiceVersion {}", theServiceVersionPid);

		BasePersServiceVersion svcVer = myDao.getServiceVersionByPid(theServiceVersionPid);
		if (svcVer == null) {
			throw new IllegalArgumentException("Unknown Service Version " + theServiceVersionPid);
		}

		List<GUrlStatus> retVal = new ArrayList<GUrlStatus>();

		StatusesBean statuses = myDao.loadAllStatuses();
		for (PersServiceVersionUrl nextUrl : svcVer.getUrls()) {
			retVal.add(toUi(nextUrl, statuses));
		}

		return retVal;
	}

	@Override
	public GSoap11ServiceVersionAndResources loadSoap11ServiceVersionFromWsdl(GSoap11ServiceVersion theService, String theWsdlUrl) throws ProcessingException {
		Validate.notNull(theService, "Definition");
		Validate.notBlank(theWsdlUrl, "URL");

		ourLog.info("Loading service version from URL: {}", theWsdlUrl);
		PersServiceVersionSoap11 def = myInvokerSoap11.introspectServiceFromUrl(theWsdlUrl);

		theService.getMethodList().clear();
		for (PersServiceVersionMethod next : def.getMethods()) {
			theService.getMethodList().add(toUi(next, false));
		}

		// Only add URLs if there aren't any already defined for this version
		if (theService.getUrlList().size() == 0) {
			for (PersServiceVersionUrl next : def.getUrls()) {
				theService.getUrlList().add(toUi(next));
			}
		}

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
	public GDomainList saveDomain(GDomain theDomain) throws ProcessingException {
		ourLog.info("Saving domain with PID {}", theDomain.getPid());

		PersDomain domain = myDao.getDomainByPid(theDomain.getPid());
		PersDomain newDomain = fromUi(theDomain);
		domain.merge(newDomain);

		myServiceRegistry.saveDomain(domain);

		// TODO: make this synchronous? (ie don't use a cached version, or force a cache refresh or something?
		return loadDomainList();
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
	public void saveLibraryMessage(DtoLibraryMessage theMessage) throws ProcessingException {
		ourLog.info("Saving library message");

		PersLibraryMessage msg = fromUi(theMessage);
		myDao.saveLibraryMessage(msg);
	}

	@Override
	public void saveMonitorRule(BaseGMonitorRule theRule) throws ProcessingException {
		BasePersMonitorRule rule = fromUi(theRule);
		myMonitorSvc.saveRule(rule);
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
		myDao.saveService(service);

		// TODO: make this synchronous? (ie don't use a cached version, or force a cache refresh or something?
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

		BasePersServiceVersion version = fromUi(theVersion, service, versionId);

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
		T retVal = (T) toUi(version, false, null);

		return retVal;
	}

	@Override
	public GUser saveUser(GUser theUser) throws ProcessingException {
		ourLog.info("Saving user with PID {}", theUser.getPid());

		PersUser user = fromUi(theUser);

		user = mySecurityService.saveServiceUser(user);

		return toUi(user, false);
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

	@Override
	public GServiceVersionSingleFireResponse testServiceVersionWithSingleMessage(String theMessageText, String theContentType, long thePid, String theRequestedByString) throws ProcessingException {
		ourLog.info("Testing single fire of service version {}", thePid);
		Date transactionTime = new Date();

		BasePersServiceVersion svcVer = myDao.getServiceVersionByPid(thePid);
		if (svcVer == null) {
			throw new IllegalArgumentException("Unknown service version: " + thePid);
		}

		HttpRequestBean request = new HttpRequestBean();
		request.setInputReader(new StringReader(theMessageText));
		request.setPath(svcVer.getProxyPath());
		request.setQuery("");
		request.setRequestHostIp("127.0.0.1");
		request.setRequestType(RequestType.POST);

		GServiceVersionSingleFireResponse retVal = new GServiceVersionSingleFireResponse();
		try {
			SidechannelOrchestratorResponseBean response = myOrchestrator.handleSidechannelRequest(thePid, theMessageText, theContentType, theRequestedByString);

			retVal.setAuthorizationOutcome(AuthorizationOutcomeEnum.AUTHORIZED);
			retVal.setDomainName(svcVer.getService().getDomain().getDomainName());
			retVal.setDomainPid(svcVer.getService().getDomain().getPid());
			if (response.getHttpResponse().getSuccessfulUrl() != null) {
				retVal.setImplementationUrlHref(response.getHttpResponse().getSuccessfulUrl().getUrl());
				retVal.setImplementationUrlId(response.getHttpResponse().getSuccessfulUrl().getUrlId());
				retVal.setImplementationUrlPid(response.getHttpResponse().getSuccessfulUrl().getPid());
			}

			List<Pair<String>> requestHeaders = new ArrayList<Pair<String>>();
			requestHeaders.add(new Pair<String>("Content-Type", svcVer.getProtocol().getRequestContentType()));

			retVal.setRequestContentType(svcVer.getProtocol().getRequestContentType());
			retVal.setRequestHeaders(requestHeaders);

			retVal.setRequestMessage(theMessageText);
			retVal.setResponseContentType(response.getResponseContentType());
			retVal.setResponseHeaders(response.getHttpResponse().getResponseHeadersAsPairList());
			retVal.setResponseMessage(response.getResponseBody());
			retVal.setServiceName(svcVer.getService().getServiceName());
			retVal.setServicePid(svcVer.getService().getPid());
			retVal.setServiceVersionId(svcVer.getVersionId());
			retVal.setServiceVersionPid(svcVer.getPid());
			retVal.setTransactionMillis(response.getHttpResponse().getResponseTime());
			retVal.setTransactionTime(transactionTime);

		} catch (Exception e) {
			ourLog.error("Failed to invoke service", e);
			retVal.setOutcomeDescription("Failed with internal exception: " + e.getMessage());
		}

		return retVal;
	}

	private int defaultInteger(Integer theInt) {
		return theInt != null ? theInt : 0;
	}

	private StatusEnum extractStatus(BaseGDashboardObjectWithUrls<?> theDashboardObject, StatusEnum theInitialStatus, List<Integer> the60MinInvCount, List<Long> the60minTime, PersService theService,
			StatusesBean theStatuses) throws ProcessingException {

		// Value will be changed below
		StatusEnum status = theInitialStatus;

		for (BasePersServiceVersion nextVersion : theService.getVersions()) {
			status = extractStatus(theDashboardObject, theStatuses, the60MinInvCount, the60minTime, status, nextVersion);

		} // end VERSION
		return status;
	}

	private StatusEnum extractStatus(BaseGDashboardObjectWithUrls<?> theDashboardObject, StatusesBean theStatuses, List<Integer> the60MinInvCount, List<Long> the60minTime, StatusEnum theStatus,
			BasePersServiceVersion nextVersion) throws ProcessingException {
		StatusEnum status = theStatus;

		for (PersServiceVersionUrl nextUrl : nextVersion.getUrls()) {
			PersServiceVersionUrlStatus nextUrlStatus = theStatuses.getUrlStatus(nextUrl.getPid());
			if (nextUrlStatus == null) {
				continue;
			}

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
		
		// Failing monitor rules
		List<PersMonitorRuleFiring> failingRules = theStatuses.getFirings(nextVersion.getPid());
		for (PersMonitorRuleFiring next : failingRules) {
			theDashboardObject.getFailingApplicableRulePids().add(next.getPid());
		}
		
		return status;
	}

	private void extractStatus(int theNumMinsBack, List<Integer> the60MinInvCount, List<Long> the60minTime, PersServiceVersionMethod nextMethod) throws ProcessingException {
		IRuntimeStatus statusSvc = myStatusSvc;
		extractSuccessfulInvocationInvocationTimes(myConfigSvc.getConfig(), theNumMinsBack, the60MinInvCount, the60minTime, nextMethod, statusSvc);
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

	private BasePersMonitorRule fromUi(BaseGMonitorRule theRule) throws ProcessingException {

		BasePersMonitorRule retVal = null;

		switch (theRule.getRuleType()) {
		case PASSIVE: {
			PersMonitorRulePassive persRule = new PersMonitorRulePassive();
			GMonitorRulePassive rule = (GMonitorRulePassive) theRule;
			persRule.setPassiveFireForBackingServiceLatencyIsAboveMillis(rule.getPassiveFireForBackingServiceLatencyIsAboveMillis());
			persRule.setPassiveFireForBackingServiceLatencySustainTimeMins(rule.getPassiveFireForBackingServiceLatencySustainTimeMins());
			persRule.setPassiveFireIfAllBackingUrlsAreUnavailable(rule.isPassiveFireIfAllBackingUrlsAreUnavailable());
			persRule.setPassiveFireIfSingleBackingUrlIsUnavailable(rule.isPassiveFireIfSingleBackingUrlIsUnavailable());
			for (GMonitorRuleAppliesTo next : rule.getAppliesTo()) {
				if (next.getServiceVersionPid() != null) {
					persRule.getAppliesTo().add(new PersMonitorAppliesTo(persRule, myDao.getServiceVersionByPid(next.getServiceVersionPid())));
				} else if (next.getServicePid() != null) {
					persRule.getAppliesTo().add(new PersMonitorAppliesTo(persRule, myDao.getServiceByPid(next.getServicePid())));
				} else {
					persRule.getAppliesTo().add(new PersMonitorAppliesTo(persRule, myDao.getDomainByPid(next.getDomainPid())));
				}
			}

			retVal = persRule;
			break;
		}
		case ACTIVE: {
			PersMonitorRuleActive persRule = new PersMonitorRuleActive();
			DtoMonitorRuleActive rule = (DtoMonitorRuleActive) theRule;
			for (DtoMonitorRuleActiveCheck next : rule.getCheckList()) {
				persRule.getActiveChecks().add(fromUi(next, persRule));
			}
			retVal = persRule;
			break;
		}
		}

		if (retVal == null) {
			throw new IllegalStateException("Unknown type: " + theRule.getRuleType());
		}

		retVal.setRuleActive(theRule.isActive());
		retVal.setRuleName(theRule.getName());
		retVal.setPid(theRule.getPidOrNull());

		for (String next : theRule.getNotifyEmailContacts()) {
			retVal.getNotifyContact().add(new PersMonitorRuleNotifyContact(next));
		}

		return retVal;
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

	private PersLibraryMessage fromUi(DtoLibraryMessage theMessage) throws ProcessingException {
		PersLibraryMessage retVal = new PersLibraryMessage();

		retVal.setPid(theMessage.getPid());
		retVal.setContentType(theMessage.getContentType());
		retVal.setDescription(theMessage.getDescription());
		retVal.setMessage(theMessage.getMessage());

		for (Long next : theMessage.getAppliesToServiceVersionPids()) {
			BasePersServiceVersion ver = myDao.getServiceVersionByPid(next);
			if (ver == null) {
				throw new ProcessingException("Unknown svcver PID: " + next);
			}
			retVal.getAppliesTo().add(new PersLibraryMessageAppliesTo(retVal, ver));
		}

		return retVal;
	}

	private PersMonitorRuleActiveCheck fromUi(DtoMonitorRuleActiveCheck theNext, BasePersMonitorRule theRule) throws ProcessingException {
		PersMonitorRuleActiveCheck retVal = new PersMonitorRuleActiveCheck();
		retVal.setCheckFrequencyNum(theNext.getCheckFrequencyNum());
		retVal.setCheckFrequencyUnit(theNext.getCheckFrequencyUnit());
		retVal.setExpectLatencyUnderMillis(theNext.getExpectLatencyUnderMillis());
		retVal.setExpectResponseContainsText(theNext.getExpectResponseContainsText());
		retVal.setExpectResponseType(theNext.getExpectResponseType());
		retVal.setLastTransactionDate(theNext.getLastTransactionDate());
		retVal.setLastTransactionOutcome(theNext.getLastTransactionOutcome());
		retVal.setMessage(myDao.getLibraryMessageByPid(theNext.getMessagePid()));
		if (retVal.getMessage() == null) {
			throw new ProcessingException("Unknown message PID: " + theNext.getMessagePid());
		}
		retVal.setPid(theNext.getPidOrNull());
		retVal.setRule(theRule);
		retVal.setServiceVersion(myDao.getServiceVersionByPid(theNext.getServiceVersionPid()));
		if (retVal.getServiceVersion() == null) {
			throw new ProcessingException("Unknown service version PID: " + theNext.getServiceVersionPid());
		}
		return retVal;
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
		retVal.setSecurityPolicy(theMethod.getSecurityPolicy());
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
			if (authHost == null) {
				throw new ProcessingException("Unknown authentication host PID " + theUser.getAuthHostPid());
			}
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
		retVal.setDescription(theUser.getDescription());

		// Contact Notes
		retVal.getContact().setNotes(theUser.getContactNotes());
		retVal.getContact().setEmailAddresses(theUser.getContactEmails());

		if (theUser.getPidOrNull() != null && theUser.getChangePassword() == null) {
			PersUser existing = myDao.getUser(theUser.getPid());
			if (StringUtils.isNotBlank(existing.getPasswordHash())) {
				retVal.setPasswordHash(existing.getPasswordHash());
			}
		} else if (theUser.getChangePassword() != null) {
			ourLog.info("Changing password for user {}", theUser.getPidOrNull());
			retVal.setPassword(theUser.getChangePassword());
		}

		retVal.setThrottleMaxRequests(theUser.getThrottle().getMaxRequests());
		retVal.setThrottlePeriod(theUser.getThrottle().getPeriod());
		retVal.setThrottleMaxQueueDepth(theUser.getThrottle().getQueue());

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

	private Collection<PersUserDomainPermission> fromUi(List<GUserDomainPermission> theDomainPermissions, Collection<PersUserDomainPermission> theExisting) {
		Collection<PersUserDomainPermission> retVal = theExisting;
		retVal.clear();
		for (GUserDomainPermission next : theDomainPermissions) {
			retVal.add(fromUi(next));
		}
		return retVal;
	}

	@SuppressWarnings("unused")
	private void fromUi(PersServiceVersionJsonRpc20 theRetVal, GServiceVersionJsonRpc20 theVersion) {
		// nothing in here yet
	}

	private PersServiceVersionSoap11 fromUi(PersServiceVersionSoap11 thePersVersion, GSoap11ServiceVersion theVersion) throws ProcessingException {
		thePersVersion.setWsdlUrl(theVersion.getWsdlLocation());
		return thePersVersion;
	}

	private <T extends BaseGServiceVersion> BasePersServiceVersion fromUi(T theVersion, PersService theService, String theVersionId) throws ProcessingException {
		Validate.notNull(theVersion);
		Validate.notNull(theService);
		Validate.notBlank(theVersionId);

		BasePersServiceVersion retVal;
		if (theVersion.getPidOrNull() != null) {
			ourLog.debug("Retrieving existing service version PID[{}]", theVersion.getPidOrNull());
			retVal = myDao.getServiceVersionByPid(theVersion.getPid());
		} else {
			ourLog.debug("Retrieving service version ID[{}]", theVersionId);
			retVal = myServiceRegistry.getOrCreateServiceVersionWithId(theService, theVersion.getProtocol(), theVersionId);
			ourLog.debug("Found service version NEW[{}], PID[{}], PROTOCOL[{}]", new Object[] { retVal.isNewlyCreated(), retVal.getPid(), retVal.getProtocol().name() });
		}

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
		retVal.setDescription(theVersion.getDescription());

		retVal.setServerSecurityMode(theVersion.getServerSecurityMode());
		if (retVal.getServerSecurityMode() == null) {
			if (theVersion.getServerSecurityList().size() > 0) {
				retVal.setServerSecurityMode(ServerSecurityModeEnum.REQUIRE_ANY);
			} else {
				retVal.setServerSecurityMode(ServerSecurityModeEnum.NONE);
			}
		}

		PersHttpClientConfig httpClientConfig = myDao.getHttpClientConfig(theVersion.getHttpClientConfigPid());
		if (httpClientConfig == null) {
			throw new ProcessingException("Unknown HTTP client config PID: " + theVersion.getHttpClientConfigPid());
		}
		retVal.setHttpClientConfig(httpClientConfig);

		retVal.populateKeepRecentTransactionsFromDto(theVersion);

		return retVal;
	}

	private boolean hasAnything(Set<?>... theSets) {
		for (Set<?> next : theSets) {
			if (next != null && next.isEmpty() == false) {
				return true;
			}
		}
		return false;
	}

	private GAuthenticationHostList loadAuthHostList() {
		GAuthenticationHostList retVal = new GAuthenticationHostList();
		for (BasePersAuthenticationHost next : myDao.getAllAuthenticationHosts()) {
			BaseGAuthHost uiObject = toUi(next);
			retVal.add(uiObject);
		}
		return retVal;
	}

	private GDomain loadDomain(PersDomain nextDomain, Set<Long> theLoadDomStats, Set<Long> theLoadSvcStats, Set<Long> theLoadVerStats, Set<Long> theLoadVerMethodStats, StatusesBean statuses)
			throws ProcessingException {
		GDomain gDomain = toUi(nextDomain, theLoadDomStats.contains(nextDomain.getPid()), statuses);

		for (PersService nextService : nextDomain.getServices()) {
			GService gService = toUi(nextService, theLoadSvcStats.contains(nextService.getPid()), statuses);
			gDomain.getServiceList().add(gService);

			for (BasePersServiceVersion nextVersion : nextService.getVersions()) {
				BaseGServiceVersion gVersion = toUi(nextVersion, theLoadVerStats.contains(nextVersion.getPid()), theLoadVerMethodStats, statuses);
				gService.getVersionList().add(gVersion);

			} // for service versions
		} // for services
		return gDomain;
	}

	private GDomainList loadDomainList(Set<Long> theLoadDomStats, Set<Long> theLoadSvcStats, Set<Long> theLoadVerStats, Set<Long> theLoadVerMethodStats, StatusesBean statuses)
			throws ProcessingException {
		GDomainList domainList = new GDomainList();

		for (PersDomain nextDomain : myServiceRegistry.getAllDomains()) {
			GDomain gDomain = loadDomain(nextDomain, theLoadDomStats, theLoadSvcStats, theLoadVerStats, theLoadVerMethodStats, statuses);

			domainList.add(gDomain);
		} // for domains
		return domainList;
	}

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

	private String toHeaderContentType(List<Pair<String>> theResponseHeaders) {
		for (Pair<String> pair : theResponseHeaders) {
			if (pair.getFirst().equalsIgnoreCase("content-type")) {
				return pair.getSecond().split(";")[0].trim();
			}
		}
		return null;
	}

	private List<Pair<String>> toHeaders(String theHeaders) {
		ArrayList<Pair<String>> retVal = new ArrayList<Pair<String>>();
		for (String next : theHeaders.split("\\r\\n")) {
			int idx = next.indexOf(": ");
			retVal.add(new Pair<String>(next.substring(0, idx), next.substring(idx + 2)));
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

		//
		// theUiService.getClientSecurityList().clear();
		// for (PersBaseClientAuth<?> next : theSvcVer.getClientAuths()) {
		// theUiService.getClientSecurityList().add(toUi(next));
		// }
		//
		// theUiService.getServerSecurityList().clear();
		// for (PersBaseServerAuth<?, ?> next : theSvcVer.getServerAuths()) {
		// theUiService.getServerSecurityList().add(toUi(next));
		// }

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

	private BaseGMonitorRule toUi(BasePersMonitorRule theRule) throws ProcessingException {
		BaseGMonitorRule retVal = null;
		switch (theRule.getRuleType()) {
		case PASSIVE: {
			GMonitorRulePassive ruleDto = new GMonitorRulePassive();
			PersMonitorRulePassive rule = (PersMonitorRulePassive) theRule;
			ruleDto.setPassiveFireForBackingServiceLatencyIsAboveMillis(rule.getPassiveFireForBackingServiceLatencyIsAboveMillis());
			ruleDto.setPassiveFireForBackingServiceLatencySustainTimeMins(rule.getPassiveFireForBackingServiceLatencySustainTimeMins());
			ruleDto.setPassiveFireIfAllBackingUrlsAreUnavailable(rule.isPassiveFireIfAllBackingUrlsAreUnavailable());
			ruleDto.setPassiveFireIfSingleBackingUrlIsUnavailable(rule.isPassiveFireIfSingleBackingUrlIsUnavailable());
			for (PersMonitorAppliesTo next : rule.getAppliesTo()) {
				if (next.getItem() instanceof PersDomain) {
					PersDomain domain = (PersDomain) next.getItem();
					ruleDto.applyTo(toUi(domain, false, null), true);
				} else if (next.getItem() instanceof PersService) {
					PersService service = (PersService) next.getItem();
					PersDomain domain = service.getDomain();
					ruleDto.applyTo(toUi(domain, false, null), toUi(service, false, null), true);
				} else if (next.getItem() instanceof BasePersServiceVersion) {
					BasePersServiceVersion svcVer = (BasePersServiceVersion) next.getItem();
					PersService service = svcVer.getService();
					PersDomain domain = service.getDomain();
					ruleDto.applyTo(toUi(domain, false, null), toUi(service, false, null), toUi(svcVer, false, null), true);
				}
			}

			retVal = ruleDto;
			break;
		}
		case ACTIVE: {
			DtoMonitorRuleActive ruleDto = new DtoMonitorRuleActive();
			PersMonitorRuleActive rule = (PersMonitorRuleActive) theRule;
			for (PersMonitorRuleActiveCheck next : rule.getActiveChecks()) {
				ruleDto.getCheckList().add(toUi(next));
			}
			retVal = ruleDto;
			break;
		}
		}

		if (retVal == null) {
			throw new IllegalStateException("Unknown type: " + theRule.getRuleType());
		}
		retVal.setPid(theRule.getPid());
		retVal.setActive(theRule.isRuleActive());
		retVal.setName(theRule.getRuleName());

		for (PersMonitorRuleNotifyContact next : theRule.getNotifyContact()) {
			retVal.getNotifyEmailContacts().add(next.getEmail());
		}

		return retVal;
	}

	private GRecentMessage toUi(BasePersRecentMessage theMsg, boolean theLoadMsgContents) {
		GRecentMessage retVal = new GRecentMessage();

		retVal.setPid(theMsg.getPid());
		PersServiceVersionUrl implementationUrl = theMsg.getImplementationUrl();
		if (implementationUrl != null) {
			retVal.setImplementationUrlId(implementationUrl.getUrlId());
			retVal.setImplementationUrlHref(implementationUrl.getUrl());
			retVal.setImplementationUrlPid(implementationUrl.getPid());
		}

		BasePersServiceVersion svcVer = theMsg.getServiceVersion();
		if (svcVer != null) {
			retVal.setDomainPid(svcVer.getService().getDomain().getPid());
			retVal.setDomainName(svcVer.getService().getDomain().getDomainNameOrId());

			retVal.setServicePid(svcVer.getService().getPid());
			retVal.setServiceName(svcVer.getService().getServiceNameOrId());

			retVal.setServiceVersionPid(svcVer.getPid());
			retVal.setServiceVersionId(svcVer.getVersionId());
		}

		PersServiceVersionMethod method = theMsg.getMethod();
		if (method != null) {
			retVal.setMethodPid(method.getPid());
			retVal.setMethodName(method.getName());
		}

		retVal.setRecentMessageType(theMsg.getRecentMessageType());
		retVal.setRequestHostIp(theMsg.getRequestHostIp());
		retVal.setTransactionTime(theMsg.getTransactionTime());
		retVal.setTransactionMillis(theMsg.getTransactionMillis());
		retVal.setAuthorizationOutcome(theMsg.getAuthorizationOutcome());

		if (theLoadMsgContents) {
			int bodyIdx = theMsg.getRequestBody().indexOf("\r\n\r\n");
			if (bodyIdx == -1) {
				retVal.setRequestMessage(theMsg.getRequestBody());
				retVal.setRequestHeaders(new ArrayList<Pair<String>>());
				retVal.setRequestContentType("unknown");
			} else {
				retVal.setRequestMessage(theMsg.getRequestBody().substring(bodyIdx + 4));
				retVal.setRequestHeaders(toHeaders(theMsg.getRequestBody().substring(0, bodyIdx)));
				retVal.setRequestContentType(toHeaderContentType(retVal.getRequestHeaders()));
			}

			bodyIdx = theMsg.getResponseBody().indexOf("\r\n\r\n");
			if (bodyIdx == -1) {
				retVal.setResponseMessage(theMsg.getResponseBody());
				retVal.setResponseHeaders(new ArrayList<Pair<String>>());
				retVal.setResponseContentType("unknown");
			} else {
				retVal.setResponseMessage(theMsg.getResponseBody().substring(bodyIdx + 4));
				retVal.setResponseHeaders(toHeaders(theMsg.getResponseBody().substring(0, bodyIdx)));
				retVal.setResponseContentType(toHeaderContentType(retVal.getResponseHeaders()));
			}

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

	private BaseGServiceVersion toUi(BasePersServiceVersion theVersion, boolean theLoadStats, Set<Long> theLoadMethodStats, StatusesBean theStatuses) throws ProcessingException {
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
		retVal.setParentServiceName(theVersion.getService().getServiceName());
		retVal.setParentServicePid(theVersion.getService().getPid());
		retVal.setDescription(theVersion.getDescription());
		retVal.setServerSecurityMode(theVersion.getServerSecurityMode());

		theVersion.populateKeepRecentTransactionsToDto(retVal);
		toUiMonitorRules(theVersion, retVal);

		for (PersServiceVersionMethod nextMethod : theVersion.getMethods()) {
			boolean loadStats = theLoadMethodStats != null && theLoadMethodStats.contains(nextMethod.getPid());
			GServiceMethod gMethod = toUi(nextMethod, loadStats);
			retVal.getMethodList().add(gMethod);
		} // for methods

		for (PersServiceVersionUrl nextUrl : theVersion.getUrls()) {
			GServiceVersionUrl gUrl = toUi(nextUrl);
			retVal.getUrlList().add(gUrl);
		} // for URLs

		for (PersServiceVersionResource nextResource : theVersion.getUriToResource().values()) {
			GServiceVersionResourcePointer gResource = toUi(nextResource);
			retVal.getResourcePointerList().add(gResource);
		} // for resources

		for (PersBaseServerAuth<?, ?> nextServerAuth : theVersion.getServerAuths()) {
			BaseGServerSecurity gServerAuth = toUi(nextServerAuth);
			retVal.getServerSecurityList().add(gServerAuth);
		} // server auths

		for (PersBaseClientAuth<?> nextClientAuth : theVersion.getClientAuths()) {
			BaseGClientSecurity gClientAuth = toUi(nextClientAuth);
			retVal.getClientSecurityList().add(gClientAuth);
		} // Client auths

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
			extractStatus(retVal, theStatuses, t60minCount, t60minTime, status, theVersion);

			retVal.setTransactions60mins(toArray(t60minCount));
			retVal.setLatency60mins(toLatency(t60minCount, t60minTime));

			int urlsActive = 0;
			int urlsDown = 0;
			int urlsUnknown = 0;
			for (PersServiceVersionUrl nextUrl : theVersion.getUrls()) {

				PersServiceVersionUrlStatus urlStatus = theStatuses.getUrlStatus(nextUrl.getPid());
				if (urlStatus == null) {
					continue;
				}

				switch (urlStatus.getStatus()) {
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

			PersServiceVersionStatus svcVerStatus = theStatuses.getServiceVersionStatus(theVersion.getPid());
			if (svcVerStatus != null) {
				retVal.setLastServerSecurityFailure(svcVerStatus.getLastServerSecurityFailure());
				retVal.setLastSuccessfulInvocation(svcVerStatus.getLastSuccessfulInvocation());
			}

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

	private BaseGServiceVersion toUi(BasePersServiceVersion theSvcVer, boolean theLoadStats, StatusesBean theStatuses) throws ProcessingException {
		Set<Long> emptySet = Collections.emptySet();
		return toUi(theSvcVer, theLoadStats, emptySet, theStatuses);
	}

	private List<GUserDomainPermission> toUi(Collection<PersUserDomainPermission> theDomainPermissions) {
		List<GUserDomainPermission> retVal = new ArrayList<GUserDomainPermission>();
		for (PersUserDomainPermission next : theDomainPermissions) {
			retVal.add(toUi(next));
		}
		return retVal;
	}

	private List<GRecentMessage> toUi(List<? extends BasePersRecentMessage> theServiceVersionRecentMessages, boolean theLoadMessageContents) {
		List<GRecentMessage> retVal = new ArrayList<GRecentMessage>();

		for (BasePersRecentMessage next : theServiceVersionRecentMessages) {
			retVal.add(toUi(next, theLoadMessageContents));
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

	private GDomain toUi(PersDomain theDomain, boolean theLoadStats, StatusesBean theStatuses) throws ProcessingException {
		GDomain retVal = new GDomain();
		retVal.setPid(theDomain.getPid());
		retVal.setId(theDomain.getDomainId());
		retVal.setName(theDomain.getDomainName());
		retVal.setServerSecured(theDomain.getServerSecured());

		toUiMonitorRules(theDomain, retVal);

		theDomain.populateKeepRecentTransactionsToDto(retVal);

		if (theLoadStats) {
			retVal.setStatsInitialized(new Date());
			StatusEnum status = StatusEnum.UNKNOWN;
			ArrayList<Integer> t60minCount = new ArrayList<Integer>();
			ArrayList<Long> t60minTime = new ArrayList<Long>();

			for (PersService nextService : theDomain.getServices()) {
				status = extractStatus(retVal, status, t60minCount, t60minTime, nextService, theStatuses);
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
						PersServiceVersionUrlStatus urlStatus = theStatuses.getUrlStatus(nextUrl.getPid());
						if (urlStatus == null) {
							continue;
						}
						switch (urlStatus.getStatus()) {
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

	private DtoLibraryMessage toUi(PersLibraryMessage theMessage, boolean theLoadContents) throws ProcessingException {
		DtoLibraryMessage retVal = new DtoLibraryMessage();

		retVal.setContentType(theMessage.getContentType());
		retVal.setDescription(theMessage.getDescription());
		retVal.setPid(theMessage.getPid());
		retVal.setMessageLength(theMessage.getMessageLength());

		for (PersLibraryMessageAppliesTo next : theMessage.getAppliesTo()) {
			retVal.getAppliesToServiceVersionPids().add(next.getPk().getServiceVersion().getPid());
		}

		if (theLoadContents) {
			retVal.setMessage(theMessage.getMessage());
		}

		return retVal;
	}

	private DtoMonitorRuleActiveCheck toUi(PersMonitorRuleActiveCheck theNext) throws ProcessingException {
		DtoMonitorRuleActiveCheck retVal = new DtoMonitorRuleActiveCheck();
		retVal.setCheckFrequencyNum(theNext.getCheckFrequencyNum());
		retVal.setCheckFrequencyUnit(theNext.getCheckFrequencyUnit());
		retVal.setExpectLatencyUnderMillis(theNext.getExpectLatencyUnderMillis());
		retVal.setExpectResponseContainsText(theNext.getExpectResponseContainsText());
		retVal.setExpectResponseType(theNext.getExpectResponseType());
		retVal.setLastTransactionDate(theNext.getLastTransactionDate());
		retVal.setLastTransactionOutcome(theNext.getLastTransactionOutcome());
		retVal.setMessagePid(theNext.getMessage().getPid());
		retVal.setMessageDescription(theNext.getMessage().getDescription());
		retVal.setPid(theNext.getPid());
		retVal.setServiceVersionPid(theNext.getServiceVersion().getPid());
		return retVal;
	}

	private GMonitorRuleFiring toUi(PersMonitorRuleFiring theNext) {
		GMonitorRuleFiring retVal = new GMonitorRuleFiring();
		retVal.setPid(theNext.getPid());
		retVal.setStartDate(theNext.getStartDate());
		retVal.setEndDate(theNext.getEndDate());

		for (PersMonitorRuleFiringProblem next : theNext.getProblems()) {
			retVal.getProblems().add(toUi(next));
		}

		return retVal;
	}

	private GMonitorRuleFiringProblem toUi(PersMonitorRuleFiringProblem theNext) {
		GMonitorRuleFiringProblem retVal = new GMonitorRuleFiringProblem();
		retVal.setPid(theNext.getPid());
		retVal.setServiceVersionPid(theNext.getServiceVersion().getPid());

		if (theNext.getUrl() != null) {
			retVal.setUrlPid(theNext.getUrl().getPid());
		}

		if (theNext.getFailedUrlMessage() != null) {
			retVal.setFailedUrlMessage(theNext.getFailedUrlMessage());
		}

		if (theNext.getLatencyAverageMillisPerCall() != null) {
			retVal.setFailedLatencyAverageMillisPerCall(theNext.getLatencyAverageMillisPerCall());
			retVal.setFailedLatencyAverageOverMinutes(theNext.getLatencyAverageOverMinutes());
			retVal.setFailedLatencyThreshold(theNext.getLatencyThreshold());
		}

		return retVal;
	}

	private GService toUi(PersService theService, boolean theLoadStats, StatusesBean theStatuses) throws ProcessingException {
		GService retVal = new GService();
		retVal.setPid(theService.getPid());
		retVal.setId(theService.getServiceId());
		retVal.setName(theService.getServiceName());
		retVal.setActive(theService.isActive());
		retVal.setServerSecured(theService.getServerSecured());

		theService.populateKeepRecentTransactionsToDto(retVal);
		toUiMonitorRules(theService, retVal);

		if (theLoadStats) {
			retVal.setStatsInitialized(new Date());
			StatusEnum status = StatusEnum.UNKNOWN;
			ArrayList<Integer> t60minCount = new ArrayList<Integer>();
			ArrayList<Long> t60minTime = new ArrayList<Long>();

			status = extractStatus(retVal, status, t60minCount, t60minTime, theService, theStatuses);

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
					PersServiceVersionUrlStatus urlStatus = theStatuses.getUrlStatus(nextUrl.getPid());
					if (urlStatus == null) {
						continue;
					}
					switch (urlStatus.getStatus()) {
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

	private GServiceMethod toUi(PersServiceVersionMethod theMethod, boolean theLoadStats) throws ProcessingException {
		GServiceMethod retVal = new GServiceMethod();
		if (theMethod.getPid() != null) {
			retVal.setPid(theMethod.getPid());
		}
		retVal.setId(theMethod.getName());
		retVal.setName(theMethod.getName());
		retVal.setRootElements(theMethod.getRootElements());
		retVal.setSecurityPolicy(theMethod.getSecurityPolicy());

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

	private GUrlStatus toUi(PersServiceVersionUrl theUrl, StatusesBean theStatuses) {
		GUrlStatus retVal = new GUrlStatus();
		retVal.setUrlPid(theUrl.getPid());

		PersServiceVersionUrlStatus urlStatus = theStatuses.getUrlStatus(theUrl.getPid());
		retVal.setStatus(urlStatus.getStatus());
		retVal.setLastFailure(urlStatus.getLastFail());
		retVal.setLastFailureMessage(urlStatus.getLastFailMessage());
		retVal.setLastSuccess(urlStatus.getLastSuccess());
		retVal.setLastSuccessMessage(urlStatus.getLastSuccessMessage());
		retVal.setLastFault(urlStatus.getLastFault());
		retVal.setLastFaultMessage(urlStatus.getLastFaultMessage());
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
		retVal.setDescription(thePersUser.getDescription());
		retVal.setContactEmails(thePersUser.getContact().getEmailAddresses());

		if (theLoadStats) {
			PersUserStatus status = thePersUser.getStatus();
			retVal.setStatsInitialized(new Date());
			retVal.setStatsLastAccess(status.getLastAccess());

			final ArrayList<Integer> t60minSuccessCount = new ArrayList<Integer>();
			final ArrayList<Integer> t60minSecurityFailCount = new ArrayList<Integer>();
			final ArrayList<Integer> t60minFaultCount = new ArrayList<Integer>();
			final long[] t60minStatisticsStart = new long[1];
			IWithStats withStats = new IWithStats() {
				@Override
				public void withStats(int theIndex, BasePersMethodInvocationStats theStats) {
					if (theIndex == 0) {
						t60minStatisticsStart[0] = theStats.getPk().getStartTime().getTime();
					}
					growToSizeInt(t60minSuccessCount, theIndex);
					growToSizeInt(t60minSecurityFailCount, theIndex);
					growToSizeInt(t60minFaultCount, theIndex);
					t60minFaultCount.set(theIndex, addToInt(t60minFaultCount.get(theIndex), theStats.getFaultInvocationCount()));
					t60minSecurityFailCount.set(theIndex, addToInt(t60minSecurityFailCount.get(theIndex), theStats.getServerSecurityFailures()));
					t60minSuccessCount.set(theIndex, addToInt(t60minSuccessCount.get(theIndex), theStats.getSuccessInvocationCount()));
				}
			};
			doWithUserStatsByMinute(myConfigSvc.getConfig(), thePersUser, 60, myStatusSvc, withStats);
			retVal.setStatsStartTime(t60minStatisticsStart[0]);
			retVal.setStatsSuccessTransactions((t60minSuccessCount));
			retVal.setStatsSuccessTransactionsAvgPerMin(to60MinAveragePerMin(t60minSuccessCount));
			retVal.setStatsSecurityFailTransactions((t60minSecurityFailCount));
			retVal.setStatsSecurityFailTransactionsAvgPerMin(to60MinAveragePerMin(t60minSecurityFailCount));
			retVal.setStatsFaultTransactions((t60minSecurityFailCount));
			retVal.setStatsFaultTransactionsAvgPerMin(to60MinAveragePerMin(t60minSecurityFailCount));

			retVal.setThrottle(new GThrottle());
			retVal.getThrottle().setMaxRequests(thePersUser.getThrottleMaxRequests());
			retVal.getThrottle().setPeriod(thePersUser.getThrottlePeriod());
			retVal.getThrottle().setQueue(thePersUser.getThrottleMaxQueueDepth());

		}

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

	private Collection<DtoLibraryMessage> toUiCollectionLibraryMessages(Collection<PersLibraryMessage> theMsgs, boolean theLoadContents) throws ProcessingException {
		ArrayList<DtoLibraryMessage> retVal = new ArrayList<DtoLibraryMessage>();
		for (PersLibraryMessage next : theMsgs) {
			retVal.add(toUi(next, theLoadContents));
		}
		return retVal;
	}

	private void toUiMonitorRules(BasePersServiceVersion theSvcVer, BaseGDashboardObjectWithUrls<?> retVal) {
		for (PersMonitorAppliesTo nextRule : theSvcVer.getMonitorRules()) {
			if (nextRule.getItem().equals(theSvcVer)) {
				retVal.getMonitorRulePids().add(nextRule.getPid());
			}
		}
		for (PersMonitorRuleActiveCheck next : theSvcVer.getActiveChecks()) {
			retVal.getMonitorRulePids().add(next.getRule().getPid());
		}

	}

	private void toUiMonitorRules(PersDomain theDomain, BaseGDashboardObjectWithUrls<?> retVal) {
		for (PersService nextSvc : theDomain.getServices()) {
			toUiMonitorRules(nextSvc, retVal);
		}
		for (PersMonitorAppliesTo nextRule : theDomain.getMonitorRules()) {
			if (nextRule.getItem().equals(theDomain)) {
				retVal.getMonitorRulePids().add(nextRule.getPid());
			}
		}
	}

	private void toUiMonitorRules(PersService theService, BaseGDashboardObjectWithUrls<?> retVal) {
		for (BasePersServiceVersion nextSvcVer : theService.getVersions()) {
			toUiMonitorRules(nextSvcVer, retVal);
		}
		for (PersMonitorAppliesTo nextRule : theService.getMonitorRules()) {
			if (nextRule.getItem().equals(theService)) {
				retVal.getMonitorRulePids().add(nextRule.getPid());
			}
		}
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
	void setConfigSvc(IConfigService theConfigSvc) {
		myConfigSvc = theConfigSvc;
	}

	/**
	 * Unit test only
	 */
	void setInvokerSoap11(IServiceInvokerSoap11 theInvokerSoap11) {
		myInvokerSoap11 = theInvokerSoap11;
	}

	@VisibleForTesting
	void setMonitorSvc(MonitorServiceBean theMonitorSvc) {
		myMonitorSvc = theMonitorSvc;
	}

	void setPersSvc(DaoBean thePersSvc) {
		assert myDao == null;
		myDao = thePersSvc;
	}

	/**
	 * FOR UNIT TESTS ONLY
	 */
	@VisibleForTesting
	void setRuntimeStatusSvc(RuntimeStatusBean theRs) {
		myStatusSvc = theRs;
	}

	@VisibleForTesting
	void setSchedulerServiceForTesting(IScheduler theMock) {
		myScheduler = theMock;
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

	public static int addToInt(int theAddTo, long theNumberToAdd) {
		long newValue = theAddTo + theNumberToAdd;
		if (newValue > Integer.MAX_VALUE) {
			return Integer.MAX_VALUE;
		}
		return (int) newValue;
	}

	public static long addToLong(long theAddTo, long theNumberToAdd) {
		long newValue = theAddTo + theNumberToAdd;
		return newValue;
	}

	public static void doWithStatsByMinute(PersConfig theConfig, int theNumberOfMinutes, IRuntimeStatus statusSvc, PersServiceVersionMethod theMethod, IWithStats theOperator) {
		Date start = getDateXMinsAgo(theNumberOfMinutes);
		Date end = new Date();

		doWithStatsByMinute(theConfig, statusSvc, theMethod, theOperator, start, end);
	}

	public static void doWithStatsByMinute(PersConfig theConfig, TimeRange theRange, IRuntimeStatus theStatus, PersServiceVersionMethod theNextMethod, IWithStats theOperator) {

		Date end = new Date();
		doWithStatsByMinute(theConfig, theRange, theStatus, theNextMethod, theOperator, end);
	}

	public static void doWithStatsByMinute(PersConfig theConfig, TimeRange theRange, IRuntimeStatus theStatus, PersServiceVersionMethod theNextMethod, IWithStats theOperator, Date end) {
		Date start = new Date(end.getTime() - (theRange.getRange().getNumMins() * 60 * 1000L));
		doWithStatsByMinute(theConfig, theStatus, theNextMethod, theOperator, start, end);
	}

	public static void doWithUserStatsByMinute(PersConfig theConfig, PersUser theUser, int theNumberOfMinutes, IRuntimeStatus statusSvc, IWithStats theOperator) {
		Date xMinsAgo = getDateXMinsAgo(theNumberOfMinutes);
		Date date = xMinsAgo;

		for (int min = 0; date.before(new Date()); min++) {

			InvocationStatsIntervalEnum interval = doWithStatsSupportFindInterval(theConfig, date);
			date = doWithStatsSupportFindDate(date, interval);

			PersInvocationUserStatsPk pk = new PersInvocationUserStatsPk(interval, date, theUser);
			BasePersMethodInvocationStats stats = statusSvc.getInvocationUserStatsSynchronously(pk);
			theOperator.withStats(min, stats);

			date = doWithStatsSupportIncrement(date, interval);

		}
	}

	/**
	 * @return The start timestamp
	 */
	public static void extractSuccessfulInvocationInvocationTimes(PersConfig theConfig, int theNumMinsBack, final List<Integer> the60MinInvCount, final List<Long> the60minTime,
			PersServiceVersionMethod nextMethod, IRuntimeStatus statusSvc) {
		doWithStatsByMinute(theConfig, theNumMinsBack, statusSvc, nextMethod, new IWithStats() {
			@Override
			public void withStats(int theIndex, BasePersMethodInvocationStats theStats) {
				growToSizeInt(the60MinInvCount, theIndex);
				growToSizeLong(the60minTime, theIndex);
				the60MinInvCount.set(theIndex, addToInt(the60MinInvCount.get(theIndex), theStats.getSuccessInvocationCount()));
				the60minTime.set(theIndex, the60minTime.get(theIndex) + theStats.getSuccessInvocationTotalTime());
			}
		});
	}

	public static Date getDateXMinsAgo(int theNumberOfMinutes) {
		Date date60MinsAgo = new Date(System.currentTimeMillis() - (theNumberOfMinutes * DateUtils.MILLIS_PER_MINUTE));
		Date date = DateUtils.truncate(date60MinsAgo, Calendar.MINUTE);
		return date;
	}

	public static void growToSizeDouble(List<Double> theCountArray, int theIndex) {
		while (theCountArray.size() <= (theIndex)) {
			theCountArray.add(0.0);
		}
	}

	public static void growToSizeInt(List<Integer> theCountArray, int theIndex) {
		while (theCountArray.size() <= (theIndex)) {
			theCountArray.add(0);
		}
	}

	public static void growToSizeLong(List<Long> theCountArray, int theIndex) {
		while (theCountArray.size() <= (theIndex)) {
			theCountArray.add(0L);
		}
	}

	private static void doWithStatsByMinute(PersConfig theConfig, IRuntimeStatus statusSvc, PersServiceVersionMethod theMethod, IWithStats theOperator, Date start, Date end) {
		Date date = start;
		for (int min = 0; date.before(end); min++) {

			InvocationStatsIntervalEnum interval = doWithStatsSupportFindInterval(theConfig, date);
			date = doWithStatsSupportFindDate(date, interval);

			PersInvocationStatsPk pk = new PersInvocationStatsPk(interval, date, theMethod.getPid());
			BasePersMethodInvocationStats stats = statusSvc.getInvocationStatsSynchronously(pk);
			theOperator.withStats(min, stats);

			date = doWithStatsSupportIncrement(date, interval);

		}
	}

	private static Date doWithStatsSupportFindDate(Date date, InvocationStatsIntervalEnum interval) {
		Date retVal = interval.truncate(date);
		return retVal;
	}

	private static InvocationStatsIntervalEnum doWithStatsSupportFindInterval(PersConfig theConfig, Date date) {
		InvocationStatsIntervalEnum interval;
		Date collapseStatsToDaysCutoff = InvocationStatsIntervalEnum.DAY.truncate(theConfig.getCollapseStatsToDaysCutoff());
		if (date.before(collapseStatsToDaysCutoff)) {
			interval = InvocationStatsIntervalEnum.DAY;
		} else {
			Date collapseStatsToHoursCutoff = InvocationStatsIntervalEnum.HOUR.truncate(theConfig.getCollapseStatsToHoursCutoff());
			if (date.before(collapseStatsToHoursCutoff)) {
				interval = InvocationStatsIntervalEnum.HOUR;
			} else {
				Date collapseStatsToTenMinutesCutoff = InvocationStatsIntervalEnum.TEN_MINUTE.truncate(theConfig.getCollapseStatsToTenMinutesCutoff());
				if (date.before(collapseStatsToTenMinutesCutoff)) {
					interval = InvocationStatsIntervalEnum.TEN_MINUTE;
				} else {
					interval = InvocationStatsIntervalEnum.MINUTE;
				}
			}
		}
		return interval;
	}

	private static Date doWithStatsSupportIncrement(Date date, InvocationStatsIntervalEnum interval) {
		Date retVal = new Date(date.getTime() + interval.millis());
		return retVal;
	}

	private static double to60MinAveragePerMin(ArrayList<Integer> theT60minSecurityFailCount) {
		double total = 0;
		for (Integer integer : theT60minSecurityFailCount) {
			total += integer;
		}
		return total / theT60minSecurityFailCount.size();
	}

	private static int[] toArray(ArrayList<Integer> theT60minCount) {
		int[] retVal = new int[theT60minCount.size()];
		int index = 0;
		for (Integer integer : theT60minCount) {
			retVal[index++] = integer;
		}
		return retVal;
	}

	public interface IWithStats {

		void withStats(int theIndex, BasePersMethodInvocationStats theStats);

	}

	// private GDomain toUi(PersDomain theDomain) {
	// GDomain retVal=new GDomain();
	// retVal.setPid(theDomain.getPid());
	// retVal.setId(theDomain.getDomainId());
	// retVal.setName(theDomain.getDomainName());
	// return retVal;
	// }

}
