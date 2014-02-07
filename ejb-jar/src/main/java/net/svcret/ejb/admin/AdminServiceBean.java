package net.svcret.ejb.admin;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.svcret.admin.api.IAdminServiceLocal;
import net.svcret.admin.api.ProcessingException;
import net.svcret.admin.api.UnexpectedFailureException;
import net.svcret.admin.api.UnknownPidException;
import net.svcret.admin.shared.enm.AuthorizationOutcomeEnum;
import net.svcret.admin.shared.enm.InvocationStatsIntervalEnum;
import net.svcret.admin.shared.enm.ResponseTypeEnum;
import net.svcret.admin.shared.model.BaseDtoAuthenticationHost;
import net.svcret.admin.shared.model.BaseDtoClientSecurity;
import net.svcret.admin.shared.model.BaseDtoMonitorRule;
import net.svcret.admin.shared.model.BaseDtoServerSecurity;
import net.svcret.admin.shared.model.BaseDtoServiceVersion;
import net.svcret.admin.shared.model.DtoAuthenticationHostLdap;
import net.svcret.admin.shared.model.DtoAuthenticationHostList;
import net.svcret.admin.shared.model.DtoAuthenticationHostLocalDatabase;
import net.svcret.admin.shared.model.DtoClientSecurityJsonRpcNamedParameter;
import net.svcret.admin.shared.model.DtoConfig;
import net.svcret.admin.shared.model.DtoDomain;
import net.svcret.admin.shared.model.DtoDomainList;
import net.svcret.admin.shared.model.DtoHttpClientConfig;
import net.svcret.admin.shared.model.DtoLibraryMessage;
import net.svcret.admin.shared.model.DtoMonitorRuleActive;
import net.svcret.admin.shared.model.DtoMonitorRuleActiveCheck;
import net.svcret.admin.shared.model.DtoMonitorRuleActiveCheckOutcome;
import net.svcret.admin.shared.model.DtoPropertyCapture;
import net.svcret.admin.shared.model.DtoServiceVersionSoap11;
import net.svcret.admin.shared.model.DtoStickySessionUrlBinding;
import net.svcret.admin.shared.model.GHttpClientConfigList;
import net.svcret.admin.shared.model.GMonitorRuleAppliesTo;
import net.svcret.admin.shared.model.GMonitorRuleFiring;
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
import net.svcret.admin.shared.model.GServiceVersionSingleFireResponse;
import net.svcret.admin.shared.model.GServiceVersionUrl;
import net.svcret.admin.shared.model.GSoap11ServiceVersionAndResources;
import net.svcret.admin.shared.model.GThrottle;
import net.svcret.admin.shared.model.GUser;
import net.svcret.admin.shared.model.GUserDomainPermission;
import net.svcret.admin.shared.model.GUserList;
import net.svcret.admin.shared.model.GUserServicePermission;
import net.svcret.admin.shared.model.GUserServiceVersionMethodPermission;
import net.svcret.admin.shared.model.GUserServiceVersionPermission;
import net.svcret.admin.shared.model.HierarchyEnum;
import net.svcret.admin.shared.model.ModelUpdateRequest;
import net.svcret.admin.shared.model.ModelUpdateResponse;
import net.svcret.admin.shared.model.Pair;
import net.svcret.admin.shared.model.PartialUserListRequest;
import net.svcret.admin.shared.model.StatusEnum;
import net.svcret.admin.shared.model.TimeRange;
import net.svcret.admin.shared.util.Validate;
import net.svcret.ejb.api.IConfigService;
import net.svcret.ejb.api.IDao;
import net.svcret.ejb.api.IRuntimeStatus;
import net.svcret.ejb.api.IRuntimeStatusQueryLocal;
import net.svcret.ejb.api.ISecurityService;
import net.svcret.ejb.api.IServiceOrchestrator;
import net.svcret.ejb.api.IServiceOrchestrator.SidechannelOrchestratorResponseBean;
import net.svcret.ejb.api.IServiceRegistry;
import net.svcret.ejb.api.RequestType;
import net.svcret.ejb.api.SrBeanIncomingRequest;
import net.svcret.ejb.api.StatusesBean;
import net.svcret.ejb.ejb.DaoBean;
import net.svcret.ejb.ejb.RuntimeStatusBean;
import net.svcret.ejb.ejb.RuntimeStatusQueryBean.StatsAccumulator;
import net.svcret.ejb.ejb.SecurityServiceBean;
import net.svcret.ejb.ejb.ServiceRegistryBean;
import net.svcret.ejb.ejb.monitor.IMonitorService;
import net.svcret.ejb.ejb.monitor.MonitorServiceBean;
import net.svcret.ejb.ejb.nodecomm.IBroadcastSender;
import net.svcret.ejb.ex.InvalidRequestException;
import net.svcret.ejb.ex.InvocationResponseFailedException;
import net.svcret.ejb.invoker.soap.IServiceInvokerSoap11;
import net.svcret.ejb.model.entity.BasePersAuthenticationHost;
import net.svcret.ejb.model.entity.BasePersMonitorRule;
import net.svcret.ejb.model.entity.BasePersSavedTransactionRecentMessage;
import net.svcret.ejb.model.entity.BasePersServiceVersion;
import net.svcret.ejb.model.entity.BasePersStats;
import net.svcret.ejb.model.entity.BasePersStatsPk;
import net.svcret.ejb.model.entity.PersAuthenticationHostLdap;
import net.svcret.ejb.model.entity.PersAuthenticationHostLocalDatabase;
import net.svcret.ejb.model.entity.PersBaseClientAuth;
import net.svcret.ejb.model.entity.PersBaseServerAuth;
import net.svcret.ejb.model.entity.PersConfig;
import net.svcret.ejb.model.entity.PersConfigProxyUrlBase;
import net.svcret.ejb.model.entity.PersDomain;
import net.svcret.ejb.model.entity.PersHttpClientConfig;
import net.svcret.ejb.model.entity.PersInvocationMethodSvcverStats;
import net.svcret.ejb.model.entity.PersInvocationMethodSvcverStatsPk;
import net.svcret.ejb.model.entity.PersLibraryMessage;
import net.svcret.ejb.model.entity.PersLibraryMessageAppliesTo;
import net.svcret.ejb.model.entity.PersMethod;
import net.svcret.ejb.model.entity.PersMonitorAppliesTo;
import net.svcret.ejb.model.entity.PersMonitorRuleActive;
import net.svcret.ejb.model.entity.PersMonitorRuleActiveCheck;
import net.svcret.ejb.model.entity.PersMonitorRuleActiveCheckOutcome;
import net.svcret.ejb.model.entity.PersMonitorRuleFiring;
import net.svcret.ejb.model.entity.PersMonitorRuleNotifyContact;
import net.svcret.ejb.model.entity.PersMonitorRulePassive;
import net.svcret.ejb.model.entity.PersNodeStatus;
import net.svcret.ejb.model.entity.PersPropertyCapture;
import net.svcret.ejb.model.entity.PersService;
import net.svcret.ejb.model.entity.PersServiceVersionRecentMessage;
import net.svcret.ejb.model.entity.PersServiceVersionResource;
import net.svcret.ejb.model.entity.PersServiceVersionThrottle;
import net.svcret.ejb.model.entity.PersServiceVersionUrl;
import net.svcret.ejb.model.entity.PersServiceVersionUrlStatus;
import net.svcret.ejb.model.entity.PersStickySessionUrlBinding;
import net.svcret.ejb.model.entity.PersUser;
import net.svcret.ejb.model.entity.PersUserDomainPermission;
import net.svcret.ejb.model.entity.PersUserRecentMessage;
import net.svcret.ejb.model.entity.PersUserServicePermission;
import net.svcret.ejb.model.entity.PersUserServiceVersionMethodPermission;
import net.svcret.ejb.model.entity.PersUserServiceVersionPermission;
import net.svcret.ejb.model.entity.PersUserStatus;
import net.svcret.ejb.model.entity.http.PersHttpBasicClientAuth;
import net.svcret.ejb.model.entity.http.PersHttpBasicServerAuth;
import net.svcret.ejb.model.entity.jsonrpc.NamedParameterJsonRpcClientAuth;
import net.svcret.ejb.model.entity.jsonrpc.NamedParameterJsonRpcServerAuth;
import net.svcret.ejb.model.entity.soap.PersServiceVersionSoap11;
import net.svcret.ejb.model.entity.soap.PersWsSecUsernameTokenClientAuth;
import net.svcret.ejb.model.entity.soap.PersWsSecUsernameTokenServerAuth;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.annotations.VisibleForTesting;

@Service
@Transactional
public class AdminServiceBean implements IAdminServiceLocal {

	public static final long URL_PID_TO_LOAD_ALL = -1;

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(AdminServiceBean.class);

	@Autowired
	private IBroadcastSender myBroadcastSender;

	@Autowired
	private IConfigService myConfigSvc;

	@Autowired
	private IDao myDao;

	@Autowired
	private IServiceInvokerSoap11 myInvokerSoap11;

	@Autowired
	private IMonitorService myMonitorSvc;

	@Autowired
	private IServiceOrchestrator myOrchestrator;

	@Autowired
	private IRuntimeStatusQueryLocal myRuntimeStatusQuerySvc;

	@Autowired
	private ISecurityService mySecurityService;

	@Autowired
	private IServiceRegistry myServiceRegistry;

	@Autowired
	private IRuntimeStatus myStatusSvc;

	@Autowired
	private IBroadcastSender mySynchronousNodeIpcClient;

	@Override
	public DtoDomain addDomain(DtoDomain theDomain) throws ProcessingException, UnexpectedFailureException {
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

		return domain.toDto();
	}

	@Override
	public GService addService(long theDomainPid, GService theService) throws ProcessingException, UnexpectedFailureException {
		Validate.notBlank(theService.getId(), "ID");
		Validate.notBlank(theService.getName(), "Name");

		ourLog.info("Adding service with ID[{}] and NAME[{}] to domain PID[{}]", new Object[] { theService.getId(), theService.getName(), theDomainPid });

		PersDomain domain = myDao.getDomainByPid(theDomainPid);
		if (domain == null) {
			throw new IllegalArgumentException("Unknown Domain PID: " + theDomainPid);
		}

		PersService service = myServiceRegistry.getOrCreateServiceWithId(domain, theService.getId());
		if (!service.isNewlyCreated()) {
			throw new IllegalArgumentException("Service " + theService.getId() + " already exists for domain: " + domain.getDomainId());
		}

		service.merge(PersService.fromDto(theService));

		myServiceRegistry.saveService(service);

		return service.toDto();
	}

	public GServiceMethod addServiceVersionMethod(long theServiceVersionPid, GServiceMethod theMethod) throws ProcessingException, UnexpectedFailureException {
		ourLog.info("Adding method {} to service version {}", theMethod.getName(), theServiceVersionPid);

		BasePersServiceVersion sv = myDao.getServiceVersionByPid(theServiceVersionPid);
		PersMethod ui = fromUi(theMethod, theServiceVersionPid);
		sv.addMethod(ui);
		sv = myServiceRegistry.saveServiceVersion(sv);
		return sv.getMethod(theMethod.getName()).toDto(false, myRuntimeStatusQuerySvc);
	}

	@Override
	public byte[] createWsdlBundle(long theServiceVersionPid) throws ProcessingException {
		BasePersServiceVersion svcVer = myServiceRegistry.getServiceVersionByPid(theServiceVersionPid);
		if (svcVer == null) {
			throw new IllegalArgumentException("Unknown version: " + theServiceVersionPid);
		}

		switch (svcVer.getProtocol()) {
		case SOAP11:
			return myInvokerSoap11.createWsdlBundle((PersServiceVersionSoap11) svcVer);
		case JSONRPC20:
		case HL7OVERHTTP:
		case VIRTUAL:
			break;
		}

		throw new IllegalArgumentException("Service " + theServiceVersionPid + " is of type " + svcVer.getProtocol() + " which does not support WSDL bundles");
	}

	@Override
	public DtoAuthenticationHostList deleteAuthenticationHost(long thePid) throws ProcessingException {

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
	public void deleteDomain(long thePid) throws ProcessingException, UnexpectedFailureException {
		PersDomain domain = myDao.getDomainByPid(thePid);
		if (domain == null) {
			throw new IllegalArgumentException("Unknown domain PID: " + thePid);
		}

		ourLog.info("DELETING domain with PID {} and ID {}", thePid, domain.getDomainId());

		domain.loadAllAssociations();

		myServiceRegistry.removeDomain(domain);

	}

	@Override
	public GHttpClientConfigList deleteHttpClientConfig(long thePid) throws ProcessingException, UnexpectedFailureException {

		PersHttpClientConfig config = myDao.getHttpClientConfig(thePid);
		if (config == null) {
			throw new ProcessingException("Unknown HTTP Client Config PID: " + thePid);
		}

		ourLog.info("Deleting HTTP Client Config {} / {}", thePid, config.getId());

		myServiceRegistry.deleteHttpClientConfig(config);

		return loadHttpClientConfigList();
	}

	@Override
	public DtoDomainList deleteService(long theServicePid) throws ProcessingException, UnexpectedFailureException {
		ourLog.info("Deleting service {}", theServicePid);

		PersService service = myDao.getServiceByPid(theServicePid);
		if (service == null) {
			throw new ProcessingException("Unknown service PID " + theServicePid);
		}

		myServiceRegistry.deleteService(theServicePid);

		return loadDomainList();
	}

	@Override
	public DtoDomainList deleteServiceVersion(long thePid) throws ProcessingException, UnexpectedFailureException {
		ourLog.info("Deleting service version {}", thePid);

		myServiceRegistry.deleteServiceVersion(thePid);

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
	public DtoMonitorRuleActiveCheck executeMonitorRuleActiveCheck(DtoMonitorRuleActiveCheck theCheck) throws ProcessingException {

		PersMonitorRuleActive rule = null;
		if (theCheck.getPidOrNull() != null) {
			PersMonitorRuleActiveCheck existing = myDao.getMonitorRuleActiveCheck(theCheck.getPid());
			rule = (PersMonitorRuleActive) existing.getRule();
		}

		PersMonitorRuleActiveCheck check = PersMonitorRuleActiveCheck.fromDto(theCheck, rule, myDao);
		try {
			myMonitorSvc.runActiveCheck(check, theCheck.getPidOrNull() != null);
		} catch (UnexpectedFailureException e) {
			throw new ProcessingException(e);
		}

		// TODO: add most recent outcome so that it actually shows up in the UI

		return check.toDto(true);
	}

	@Override
	public Collection<DtoStickySessionUrlBinding> getAllStickySessions() {
		flushOutstandingStats();

		Collection<PersStickySessionUrlBinding> sessions = myDao.getAllStickySessions();
		Collection<DtoStickySessionUrlBinding> retVal = new ArrayList<DtoStickySessionUrlBinding>();
		for (PersStickySessionUrlBinding next : sessions) {
			retVal.add(next.toDao());
		}
		return retVal;
	}

	@Override
	public long getDefaultHttpClientConfigPid() {
		return myDao.getHttpClientConfigs().iterator().next().getPid();
	}

	@Override
	public DtoDomain getDomainByPid(long theDomain) throws UnexpectedFailureException {
		PersDomain domain = myDao.getDomainByPid(theDomain);
		if (domain != null) {
			return domain.toDto();
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
	public DtoLibraryMessage getLibraryMessage(long theMessagePid) {
		return toUi(myDao.getLibraryMessageByPid(theMessagePid), true);
	}

	@Override
	public Collection<DtoLibraryMessage> getLibraryMessages(HierarchyEnum theType, long thePid, boolean theLoadContents) {

		Collection<PersLibraryMessage> msgs = null;
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
	public Collection<DtoLibraryMessage> getLibraryMessagesForService(long thePid, boolean theLoadContents) {
		Collection<PersLibraryMessage> msgs = myDao.getLibraryMessagesWhichApplyToService(thePid);
		return toUiCollectionLibraryMessages(msgs, theLoadContents);
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
	public Collection<GMonitorRuleFiring> loadAllActiveRuleFirings() {
		Collection<GMonitorRuleFiring> retVal = new ArrayList<GMonitorRuleFiring>();
		List<PersMonitorRuleFiring> firings = myDao.getAllMonitorRuleFiringsWhichAreActive();
		for (PersMonitorRuleFiring nextFiring : firings) {
			retVal.add(nextFiring.toDto());
		}
		return retVal;
	}

	@Override
	public BaseDtoAuthenticationHost loadAuthenticationHost(long thePid) throws ProcessingException {
		ourLog.info("Loading authentication host with PID: {}", thePid);

		BasePersAuthenticationHost authHost = myDao.getAuthenticationHostByPid(thePid);
		if (authHost == null) {
			throw new ProcessingException("Unknown authentication host: " + thePid);
		}

		return toUi(authHost);
	}

	@Override
	public DtoConfig loadConfig() throws UnexpectedFailureException {
		return myConfigSvc.getConfig().toDto();
	}

	@Override
	public DtoDomainList loadDomainList() throws UnexpectedFailureException {
		Set<Long> empty = Collections.emptySet();
		return loadDomainList(empty, empty, empty, empty, empty, null);
	}

	@Override
	public Collection<DtoLibraryMessage> loadLibraryMessages() {
		ArrayList<DtoLibraryMessage> retVal = new ArrayList<DtoLibraryMessage>();
		for (PersLibraryMessage next : myDao.loadLibraryMessages()) {
			retVal.add(toUi(next, false));
		}
		return retVal;
	}

	@Override
	public ModelUpdateResponse loadModelUpdate(ModelUpdateRequest theRequest) throws ProcessingException, UnexpectedFailureException {
		flushOutstandingStats();

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
			DtoAuthenticationHostList hostList = loadAuthHostList();
			retVal.setAuthenticationHostList(hostList);
		}

		Set<Long> loadDomStats = theRequest.getDomainsToLoadStats();
		Set<Long> loadSvcStats = theRequest.getServicesToLoadStats();
		Set<Long> loadVerStats = theRequest.getVersionsToLoadStats();
		Set<Long> loadVerMethodStats = theRequest.getVersionMethodsToLoadStats();
		Set<Long> loadUrlStats = theRequest.getUrlsToLoadStats();

		if (theRequest.isLoadAllUrlStats()) {
			loadUrlStats = Collections.singleton(URL_PID_TO_LOAD_ALL);
		}

		StatusesBean statuses = null;
		if (hasAnything(loadDomStats, loadSvcStats, loadVerStats, loadVerMethodStats, loadUrlStats)) {
			statuses = myDao.loadAllStatuses(myConfigSvc.getConfig());
		}

		DtoDomainList domainList = loadDomainList(loadDomStats, loadSvcStats, loadVerStats, loadVerMethodStats, loadUrlStats, statuses);

		retVal.setDomainList(domainList);

		for (PersNodeStatus next : myRuntimeStatusQuerySvc.getAllNodeStatuses()) {
			retVal.getNodeStatuses().add(next.toDao());
		}

		return retVal;
	}

	@Override
	public DtoMonitorRuleActiveCheckOutcome loadMonitorRuleActiveCheckOutcomeDetails(long thePid) throws UnexpectedFailureException {
		PersMonitorRuleActiveCheckOutcome retVal = myDao.loadMonitorRuleActiveCheckOutcome(thePid);
		if (retVal == null) {
			throw new UnexpectedFailureException("Unknown PID: " + thePid);
		}
		return retVal.toDto(true);
	}

	@Override
	public BaseDtoMonitorRule loadMonitorRuleAndDetailedSatistics(long theRulePid) {
		return myDao.getMonitorRule(theRulePid).toDao(true);
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
			retVal.add(next.toDto());
		}

		// Set<Long> svcVerPids = new HashSet<Long>();
		// for (BasePersServiceVersion basePersServiceVersion : allSvcVers) {
		//
		// }

		return retVal;
	}

	@Override
	public GMonitorRuleList loadMonitorRuleList() {
		GMonitorRuleList retVal = new GMonitorRuleList();

		Collection<BasePersMonitorRule> allRules = myDao.getMonitorRules();
		for (BasePersMonitorRule persMonitorRule : allRules) {
			retVal.add(persMonitorRule.toDao(false));
		}

		return retVal;
	}

	@Override
	public GRecentMessage loadRecentMessageForServiceVersion(long thePid) throws UnknownPidException {
		PersServiceVersionRecentMessage msg = myDao.loadRecentMessageForServiceVersion(thePid);
		if (msg == null) {
			throw new UnknownPidException("Unable to find transaction with PID " + thePid + ". Maybe it has been purged?");
		}
		return msg.toDto(true);
	}

	@Override
	public GRecentMessage loadRecentMessageForUser(long thePid) throws UnknownPidException {
		PersUserRecentMessage msg = myDao.loadRecentMessageForUser(thePid);
		if (msg == null) {
			throw new UnknownPidException("Unable to find transaction with PID " + thePid + ". Maybe it has been purged?");
		}
		return msg.toDto(true);
	}

	@Override
	public GRecentMessageLists loadRecentTransactionListForServiceVersion(long theServiceVersionPid) {
		flushRecentTransactions();

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
		flushRecentTransactions();

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
	public GSoap11ServiceVersionAndResources loadServiceVersion(long theServiceVersionPid) throws UnexpectedFailureException {
		BasePersServiceVersion svcVer = myDao.getServiceVersionByPid(theServiceVersionPid);
		if (svcVer == null) {
			throw new IllegalArgumentException("Unknown service version PID: " + theServiceVersionPid);
		}

		Set<Long> methodPids = new HashSet<Long>();
		for (PersMethod next : svcVer.getMethods()) {
			methodPids.add(next.getPid());
		}

		Set<Long> urlPids = new HashSet<Long>();
		for (PersServiceVersionUrl next : svcVer.getUrls()) {
			urlPids.add(next.getPid());
		}

		StatusesBean statuses = myDao.loadAllStatuses(myConfigSvc.getConfig());

		Set<Long> svcVerPids = Collections.singleton(theServiceVersionPid);
		BaseDtoServiceVersion uiService = svcVer.toDto(svcVerPids, myRuntimeStatusQuerySvc, statuses, methodPids, urlPids);
		GSoap11ServiceVersionAndResources retVal = toUi(uiService, svcVer);
		return retVal;
	}

	@Override
	public GServiceVersionDetailedStats loadServiceVersionDetailedStats(long theVersionPid) throws UnexpectedFailureException {

		BasePersServiceVersion ver = myDao.getServiceVersionByPid(theVersionPid);

		final Map<Long, int[]> methodPidToSuccessCount = new HashMap<Long, int[]>();
		final Map<Long, int[]> methodPidToFailCount = new HashMap<Long, int[]>();
		final Map<Long, int[]> methodPidToSecurityFailCount = new HashMap<Long, int[]>();
		final Map<Long, int[]> methodPidToFaultCount = new HashMap<Long, int[]>();

		final List<Long> statsTimestamps = new ArrayList<Long>();

		for (final PersMethod nextMethod : ver.getMethods()) {

			StatsAccumulator accumulator = new StatsAccumulator();
			myRuntimeStatusQuerySvc.extract60MinuteMethodStats(nextMethod, accumulator);

			methodPidToSuccessCount.put(nextMethod.getPid(), toIntArray(accumulator.getSuccessCounts()));
			methodPidToFailCount.put(nextMethod.getPid(), toIntArray(accumulator.getFailCounts()));
			methodPidToSecurityFailCount.put(nextMethod.getPid(), toIntArray(accumulator.getSecurityFailCounts()));
			methodPidToFaultCount.put(nextMethod.getPid(), toIntArray(accumulator.getFaultCounts()));
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
	public GSoap11ServiceVersionAndResources loadSoap11ServiceVersionFromWsdl(DtoServiceVersionSoap11 theService, DtoHttpClientConfig theHttpClientConfig, String theWsdlUrl) throws ProcessingException, UnexpectedFailureException {
		Validate.notNull(theService, "Definition");
		Validate.notBlank(theWsdlUrl, "URL");

		ourLog.info("Loading service version from URL: {}", theWsdlUrl);
		PersHttpClientConfig httpConfig = PersHttpClientConfig.fromDto(theHttpClientConfig, myDao);
		PersServiceVersionSoap11 def = (PersServiceVersionSoap11) myInvokerSoap11.introspectServiceFromUrl(httpConfig, theWsdlUrl);

		theService.getMethodList().clear();
		for (PersMethod next : def.getMethods()) {
			theService.getMethodList().add(next.toDto(false, myRuntimeStatusQuerySvc));
		}

		// Only add URLs if there aren't any already defined for this version
		if (theService.getUrlList().size() == 0) {
			for (PersServiceVersionUrl next : def.getUrls()) {
				theService.getUrlList().add(next.toDto(false, (StatusesBean) null, myRuntimeStatusQuerySvc));
			}
		}

		GSoap11ServiceVersionAndResources retVal = toUi(theService, def);
		return retVal;
	}

	@Override
	public GUser loadUser(long thePid, boolean theLoadStats) throws ProcessingException, UnexpectedFailureException {
		ourLog.info("Loading user {}", thePid);

		PersUser persUser = myDao.getUser(thePid);
		if (persUser == null) {
			throw new ProcessingException("Unknown user PID: " + thePid);
		}

		return toUi(persUser, theLoadStats);
	}

	@Override
	public GPartialUserList loadUsers(PartialUserListRequest theRequest) throws UnexpectedFailureException {
		GPartialUserList retVal = new GPartialUserList();

		ourLog.info("Loading user list: " + theRequest.toString());

		for (PersUser next : myDao.getAllUsersAndInitializeThem()) {
			retVal.add(toUi(next, theRequest.isLoadStats()));
		}

		return retVal;
	}

	@Override
	public GServiceVersionUrl resetCircuitBreaker(long theUrlPid) throws UnexpectedFailureException {
		PersServiceVersionUrl url = myServiceRegistry.resetCircuitBreaker(theUrlPid);
		PersServiceVersionUrlStatus urlStatus = url.getStatus();
		GServiceVersionUrl retVal = new GServiceVersionUrl();
		if (url.getPid() != null) {
			retVal.setPid(url.getPid());
		}
		retVal.setId(url.getUrlId());
		retVal.setUrl(url.getUrl());

		if (true) {
			if (urlStatus.getStatus() == StatusEnum.DOWN) {
				if (urlStatus.getNextCircuitBreakerReset() != null) {
					retVal.setStatsNextCircuitBreakerReset(urlStatus.getNextCircuitBreakerReset());
				}
			}

			retVal.setStatsLastFailure(urlStatus.getLastFail());
			retVal.setStatsLastFailureMessage(urlStatus.getLastFailMessage());
			retVal.setStatsLastFailureStatusCode(urlStatus.getLastFailStatusCode());
			retVal.setStatsLastFailureContentType(urlStatus.getLastFailContentType());

			retVal.setStatsLastSuccess(urlStatus.getLastSuccess());
			retVal.setStatsLastSuccessMessage(urlStatus.getLastSuccessMessage());
			retVal.setStatsLastSuccessStatusCode(urlStatus.getLastSuccessStatusCode());
			retVal.setStatsLastSuccessContentType(urlStatus.getLastSuccessContentType());

			retVal.setStatsLastFault(urlStatus.getLastFault());
			retVal.setStatsLastFaultMessage(urlStatus.getLastFaultMessage());
			retVal.setStatsLastFaultStatusCode(urlStatus.getLastFaultStatusCode());
			retVal.setStatsLastFaultContentType(urlStatus.getLastFaultContentType());

			retVal.setStatus(urlStatus.getStatus());
			retVal.setStatsNextCircuitBreakerReset(urlStatus.getNextCircuitBreakerReset());

			StatsAccumulator accumulator = new StatsAccumulator();
			myRuntimeStatusQuerySvc.extract60MinuteServiceVersionUrlStatistics(url, accumulator);
			accumulator.populateDto(retVal);

			retVal.setStatsInitialized(new Date());

		}
		return retVal;
	}

	@Override
	public DtoAuthenticationHostList saveAuthenticationHost(BaseDtoAuthenticationHost theAuthHost) {
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
	public DtoConfig saveConfig(DtoConfig theConfig) throws UnexpectedFailureException {
		ourLog.info("Saving config");

		ourLog.info("Proxy config now contains the following URL Bases: {}", theConfig.getProxyUrlBases());

		PersConfig existing = myConfigSvc.getConfig();
		existing.merge(fromUi(theConfig));

		PersConfig newCfg = myConfigSvc.saveConfig(existing);

		return newCfg.toDto();

		//
		// PersConfig existing =
		// myPersSvc.getConfigByPid(PersConfig.DEFAULT_ID);
		// existing.merge(fromUi(theConfig));
		// PersConfig retVal = myPersSvc.saveConfig(existing);
		// return toUi(retVal);
	}

	@Override
	public DtoDomainList saveDomain(DtoDomain theDomain) throws ProcessingException, UnexpectedFailureException {
		ourLog.info("Saving domain with PID {}", theDomain.getPid());

		PersDomain domain = myDao.getDomainByPid(theDomain.getPid());
		PersDomain newDomain = fromUi(theDomain);
		domain.merge(newDomain);

		myServiceRegistry.saveDomain(domain);

		// TODO: make this synchronous? (ie don't use a cached version, or force
		// a cache refresh or something?
		return loadDomainList();
	}

	@Override
	public DtoHttpClientConfig saveHttpClientConfig(DtoHttpClientConfig theConfig, byte[] theNewTruststore, String theNewTruststorePass, byte[] theNewKeystore, String theNewKeystorePass) throws ProcessingException, UnexpectedFailureException {
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
			if (existing.getId().equals(DtoHttpClientConfig.DEFAULT_ID)) {
				isDefault = true;
			}
		}

		PersHttpClientConfig config = PersHttpClientConfig.fromDto(theConfig, myDao);
		if (existing != null) {
			config.setOptLock(existing.getOptLock());
		}

		if (isDefault) {
			config.setId(config.getId());
			config.setName(config.getName());
		}

		if (theNewTruststore != null) {
			ourLog.info("HTTP client config has a truststore defined");
			config.setTlsTruststore(theNewTruststore);
			config.setTlsTruststorePassword(theNewTruststorePass);
		} else if (theConfig.getTlsTruststore() == null) {
			ourLog.info("HTTP client config has no truststore defined");
			config.setTlsTruststore(null);
			config.setTlsTruststorePassword(null);
		}

		if (theNewKeystore != null) {
			ourLog.info("HTTP client config has a keystore defined");
			config.setTlsKeystore(theNewKeystore);
			config.setTlsKeystorePassword(theNewKeystorePass);
		} else if (theConfig.getTlsKeystore() == null) {
			ourLog.info("HTTP client config has no keystore defined");
			config.setTlsKeystore(null);
			config.setTlsKeystorePassword(null);
		}

		return myServiceRegistry.saveHttpClientConfig(config).toDto();
	}

	@Override
	public void saveLibraryMessage(DtoLibraryMessage theMessage) throws ProcessingException {
		ourLog.info("Saving library message");

		PersLibraryMessage msg = fromUi(theMessage);
		myDao.saveLibraryMessage(msg);
	}

	@Override
	public void saveMonitorRule(BaseDtoMonitorRule theRule) throws UnexpectedFailureException, ProcessingException {
		BasePersMonitorRule rule = fromUi(theRule);
		myMonitorSvc.saveRule(rule);
	}

	@Override
	public DtoDomainList saveService(GService theService) throws ProcessingException, UnexpectedFailureException {
		ourLog.info("Saving service {}", theService.getPid());

		PersService service = myDao.getServiceByPid(theService.getPid());
		if (service == null) {
			throw new ProcessingException("Unknown service PID " + theService.getPid());
		}

		PersService newService = fromUi(theService);
		service.merge(newService);

		myServiceRegistry.saveService(service);

		// TODO: make this synchronous? (ie don't use a cached version, or force
		// a cache refresh or something?
		return loadDomainList();
	}

	@Override
	public <T extends BaseDtoServiceVersion> T saveServiceVersion(long theDomain, long theService, T theVersion, List<GResource> theResources) throws ProcessingException, UnexpectedFailureException {
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

		BasePersServiceVersion existingVersion = BasePersServiceVersion.fromDto(theVersion, service, myDao, myServiceRegistry);

		// Throttle
		if (existingVersion.getThrottle() != null && theVersion.getThrottle() == null) {
			existingVersion.setThrottle(null);
		} else if (existingVersion.getThrottle() == null && theVersion.getThrottle() != null) {
			existingVersion.setThrottle(PersServiceVersionThrottle.fromDto(theVersion.getThrottle(), existingVersion));
		} else if (existingVersion.getThrottle() != null && theVersion.getThrottle() != null) {
			existingVersion.getThrottle().merge(theVersion.getThrottle());
		}

		// Resources
		Map<String, PersServiceVersionResource> uriToResource = existingVersion.getUriToResource();
		Set<String> urls = new HashSet<String>();
		for (GResource next : theResources) {
			urls.add(next.getUrl());

			if (uriToResource.containsKey(next.getUrl())) {
				PersServiceVersionResource existing = uriToResource.get(next.getUrl());
				existing.merge(fromUi(next, existingVersion));
			} else {
				uriToResource.put(next.getUrl(), fromUi(next, existingVersion));
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
		Collection<PersServiceVersionUrl> urlsToAdd = new ArrayList<PersServiceVersionUrl>();
		for (GServiceVersionUrl nextRequired : theVersion.getUrlList()) {
			boolean alreadyExists = false;
			for (PersServiceVersionUrl nextExisting : existingVersion.getUrls()) {
				if (nextRequired.getPidOrNull() != null && nextRequired.getPidOrNull().equals(nextExisting.getPid())) {
					alreadyExists = true;
					ourLog.debug("Merging URL[{}/{}] into existing URL: {}", new Object[] { nextExisting.getPid(), nextExisting.getUrlId(), nextExisting.getUrl() });
					nextExisting.merge(PersServiceVersionUrl.fromDto(nextRequired, existingVersion));
					break;
				}
			}
			if (!alreadyExists) {
				ourLog.debug("Adding URL[{}/{}] {}", new Object[] { nextRequired.getPid(), nextRequired.getId(), nextRequired.getUrl() });
				urlsToAdd.add(PersServiceVersionUrl.fromDto(nextRequired, existingVersion));
			}
		}

		// Remove any unneccesary URLs
		for (Iterator<PersServiceVersionUrl> iter = existingVersion.getUrls().iterator(); iter.hasNext();) {
			PersServiceVersionUrl nextExisting = iter.next();
			boolean shouldRemove = true;
			for (GServiceVersionUrl nextRequired : theVersion.getUrlList()) {
				if (nextRequired.getPidOrNull() != null && nextRequired.getPidOrNull().equals(nextExisting.getPid())) {
					// if (StringUtils.equals(nextRequired.getUrl(),
					// nextExisting.getUrl())) {
					// if (StringUtils.equals(nextRequired.getId(),
					// nextExisting.getUrlId())) {
					shouldRemove = false;
					break;
					// }
					// }
				}
			}
			if (shouldRemove) {
				ourLog.debug("Removing URL[{}/{}] {}", new Object[] { nextExisting.getPid(), nextExisting.getUrlId(), nextExisting.getUrl() });
				iter.remove();
			}
		}

		existingVersion.getUrls().addAll(urlsToAdd);

		ourLog.debug("Now have {} URLs", existingVersion.getUrls().size());

		// Update URL order
		Collections.sort(existingVersion.getUrls(), new Comparator<PersServiceVersionUrl>() {
			@Override
			public int compare(PersServiceVersionUrl theO1, PersServiceVersionUrl theO2) {
				return theO1.getUrlId().compareTo(theO2.getUrlId());
			}
		});
		int index = 0;
		for (PersServiceVersionUrl next : existingVersion.getUrls()) {
			next.setOrder(index++);
		}

		/*
		 * Methods
		 */

		HashSet<String> methods = new HashSet<String>();
		for (GServiceMethod next : theVersion.getMethodList()) {
			methods.add(next.getName());
			PersMethod existing = existingVersion.getMethod(next.getName());
			if (existing != null) {
				existing.merge(fromUi(next, existingVersion.getPid()));
			} else {
				existingVersion.addMethod(fromUi(next, existingVersion.getPid()));
			}
		}
		existingVersion.retainOnlyMethodsWithNamesAndUnknownMethod(methods);
		index = 0;
		for (PersMethod next : existingVersion.getMethods()) {
			next.setOrder(index++);
		}

		/*
		 * Client Auths
		 */
		Set<Long> pids = new HashSet<Long>();
		for (BaseDtoClientSecurity next : theVersion.getClientSecurityList()) {
			PersBaseClientAuth<?> nextPers = fromUi(next, existingVersion);
			if (nextPers.getPid() != null) {
				PersBaseClientAuth<?> existing = existingVersion.getClientAuthWithPid(nextPers.getPid());
				if (existing != null) {
					existing.merge(nextPers);
				} else {
					pids.add(nextPers.getPid());
				}
				pids.add(nextPers.getPid());
			} else {
				nextPers = myDao.saveClientAuth(nextPers);
				pids.add(nextPers.getPid());
				existingVersion.addClientAuth(nextPers);
			}
		}
		index = 0;
		for (PersBaseClientAuth<?> next : new ArrayList<PersBaseClientAuth<?>>(existingVersion.getClientAuths())) {
			if (next.getPid() != null && !pids.contains(next.getPid())) {
				existingVersion.removeClientAuth(next);
			} else {
				next.setOrder(index++);
			}
		}

		/*
		 * Server Auths
		 */
		pids = new HashSet<Long>();
		for (BaseDtoServerSecurity next : theVersion.getServerSecurityList()) {
			if (next.getAuthHostPid() <= 0) {
				throw new IllegalArgumentException("No auth host PID specified");
			}

			PersBaseServerAuth<?, ?> nextPers = fromUi(next, existingVersion);
			if (nextPers.getPid() != null) {
				PersBaseServerAuth<?, ?> existing = existingVersion.getServerAuthWithPid(nextPers.getPid());
				if (existing != null) {
					existing.merge(nextPers);
				} else {
					pids.add(nextPers.getPid());
				}
				pids.add(nextPers.getPid());
			} else {
				nextPers = myDao.saveServerAuth(nextPers);
				pids.add(nextPers.getPid());
				existingVersion.addServerAuth(nextPers);
			}
			existingVersion.addServerAuth(nextPers);
		}
		index = 0;
		for (PersBaseServerAuth<?, ?> next : new ArrayList<PersBaseServerAuth<?, ?>>(existingVersion.getServerAuths())) {
			if (next.getPid() != null && !pids.contains(next.getPid())) {
				existingVersion.removeServerAuth(next);
			} else {
				next.setOrder(index++);
			}
		}

		/*
		 * Property Captures
		 */
		Set<String> propCaps = new HashSet<String>();
		for (DtoPropertyCapture next : theVersion.getPropertyCaptures()) {
			PersPropertyCapture nextPers = PersPropertyCapture.fromDto(existingVersion, next);
			PersPropertyCapture existing = existingVersion.getPropertyCaptureWithPropertyName(next.getPropertyName());
			if (existing != null) {
				existing.merge(nextPers);
			} else {
				existingVersion.getPropertyCaptures().add(nextPers);
			}
			propCaps.add(nextPers.getPk().getPropertyName());
		}
		index = 0;
		for (PersPropertyCapture next : new ArrayList<PersPropertyCapture>(existingVersion.getPropertyCaptures())) {
			if (!propCaps.contains(next.getPk().getPropertyName())) {
				existingVersion.removePropertyCapture(next);
			} else {
				next.setOrder(index++);
			}
		}

		existingVersion = myServiceRegistry.saveServiceVersion(existingVersion);

		@SuppressWarnings("unchecked")
		T retVal = (T) existingVersion.toDto();

		return retVal;
	}

	// private void extractStatus(int theNumMinsBack, List<Integer>
	// the60MinInvCount, List<Long> the60minTime, PersMethod
	// nextMethod) throws ProcessingException {
	// IRuntimeStatus statusSvc = myStatusSvc;
	// extractSuccessfulInvocationInvocationTimes(myConfigSvc.getConfig(),
	// theNumMinsBack, the60MinInvCount, the60minTime, nextMethod, statusSvc);
	// }

	@Override
	public GUser saveUser(GUser theUser) throws UnexpectedFailureException, ProcessingException {
		ourLog.info("Saving user with PID {}", theUser.getPid());

		PersUser user = fromUi(theUser);

		user = mySecurityService.saveServiceUser(user);

		return toUi(user, false);
	}

	/**
	 * Unit test only
	 */
	@VisibleForTesting
	public void setConfigSvc(IConfigService theConfigSvc) {
		myConfigSvc = theConfigSvc;
	}

	/**
	 * Unit test only
	 */
	@VisibleForTesting
	public void setInvokerSoap11(IServiceInvokerSoap11 theInvokerSoap11) {
		myInvokerSoap11 = theInvokerSoap11;
	}

	@VisibleForTesting
	public void setMonitorSvc(MonitorServiceBean theMonitorSvc) {
		myMonitorSvc = theMonitorSvc;
	}

	@VisibleForTesting
	public void setPersSvc(DaoBean thePersSvc) {
		assert myDao == null;
		myDao = thePersSvc;
	}

	@VisibleForTesting
	public void setRuntimeStatusQuerySvcForUnitTests(IRuntimeStatusQueryLocal theStatsQSvc) {
		myRuntimeStatusQuerySvc = theStatsQSvc;
	}

	/**
	 * FOR UNIT TESTS ONLY
	 */
	@VisibleForTesting
	public void setRuntimeStatusSvc(RuntimeStatusBean theRs) {
		myStatusSvc = theRs;
	}

	/**
	 * FOR UNIT TESTS ONLY
	 */
	public void setSecuritySvc(SecurityServiceBean theSecSvc) {
		mySecurityService = theSecSvc;
	}

	/**
	 * FOR UNIT TESTS ONLY
	 */
	public void setServiceRegistry(ServiceRegistryBean theSvcReg) {
		myServiceRegistry = theSvcReg;
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
	public GServiceVersionSingleFireResponse testServiceVersionWithSingleMessage(String theMessageText, String theContentType, long thePid, String theRequestedByString) {
		ourLog.info("Testing single fire of service version {}", thePid);
		Date transactionTime = new Date();

		BasePersServiceVersion svcVer = myDao.getServiceVersionByPid(thePid);
		if (svcVer == null) {
			throw new IllegalArgumentException("Unknown service version: " + thePid);
		}

		SrBeanIncomingRequest request = new SrBeanIncomingRequest();
		request.setInputReader(new StringReader(theMessageText));
		request.setPath(svcVer.getDefaultProxyPath());
		request.setQuery("");
		request.setRequestHostIp("127.0.0.1");
		request.setRequestType(RequestType.POST);

		GServiceVersionSingleFireResponse retVal = new GServiceVersionSingleFireResponse();
		retVal.setDomainName(svcVer.getService().getDomain().getDomainName());
		retVal.setDomainPid(svcVer.getService().getDomain().getPid());
		retVal.setServiceName(svcVer.getService().getServiceName());
		retVal.setServicePid(svcVer.getService().getPid());
		retVal.setServiceVersionPid(svcVer.getPid());
		retVal.setServiceVersionId(svcVer.getVersionId());

		try {
			SidechannelOrchestratorResponseBean response = myOrchestrator.handleSidechannelRequest(thePid, theMessageText, theContentType, theRequestedByString);

			retVal.setAuthorizationOutcome(AuthorizationOutcomeEnum.AUTHORIZED);
			if (response.getIncomingResponse().getSuccessfulUrl() != null) {
				retVal.setImplementationUrlHref(response.getIncomingResponse().getSuccessfulUrl().getUrl());
				retVal.setImplementationUrlId(response.getIncomingResponse().getSuccessfulUrl().getUrlId());
				retVal.setImplementationUrlPid(response.getIncomingResponse().getSuccessfulUrl().getPid());
			}

			List<Pair<String>> requestHeaders = new ArrayList<Pair<String>>();
			requestHeaders.add(new Pair<String>("Content-Type", svcVer.getProtocol().getRequestContentType()));

			retVal.setRequestContentType(svcVer.getProtocol().getRequestContentType());
			retVal.setRequestHeaders(requestHeaders);

			retVal.setRequestMessage(theMessageText);
			retVal.setResponseContentType(response.getResponseContentType());
			retVal.setResponseHeaders(response.getIncomingResponse().getResponseHeadersAsPairList());
			retVal.setResponseMessage(response.getResponseBody());
			retVal.setTransactionMillis(response.getIncomingResponse().getResponseTime());
			retVal.setTransactionTime(transactionTime);

		} catch (InvalidRequestException e) {
			ourLog.error("Failed to invoke service", e);
			retVal.setOutcomeDescription(e.getMessage());
		} catch (InvocationResponseFailedException e) {
			ourLog.error("Failed to invoke service", e);
			retVal.setOutcomeDescription(e.getMessage());
			if (e.getHttpResponse() != null) {
				retVal.setResponseContentType(e.getHttpResponse().getContentType());
				retVal.setResponseMessage(e.getHttpResponse().getBody());
				retVal.setResponseHeaders(e.getHttpResponse().getResponseHeadersAsPairList());
			}
		} catch (Exception e) {
			ourLog.error("Failed to invoke service", e);
			retVal.setOutcomeDescription("Failed with internal exception: " + e.getMessage());
		}

		return retVal;
	}

	/**
	 * Convenience for Unit Tests
	 */
	@VisibleForTesting
	public DtoDomain unitTestMethod_addDomain(String theId, String theName) throws ProcessingException, UnexpectedFailureException {
		DtoDomain domain = new DtoDomain();
		domain.setId(theId);
		domain.setName(theName);
		DtoDomain retVal = addDomain(domain);
		return retVal;
	}

	private int defaultInteger(Integer theInt) {
		return theInt != null ? theInt : 0;
	}

	private void flushOutstandingStats() {
		ourLog.debug("Beginning loadModelUpdate - Going to flush stats");
		myBroadcastSender.requestFlushQueuedStats();
		ourLog.debug("Done flushing stats");
	}

	private void flushRecentTransactions() {
		mySynchronousNodeIpcClient.requestFlushTransactionLogs();
	}

	private BasePersAuthenticationHost fromUi(BaseDtoAuthenticationHost theAuthHost) {
		BasePersAuthenticationHost retVal = null;

		switch (theAuthHost.getType()) {
		case LDAP:
			PersAuthenticationHostLdap pLdap = new PersAuthenticationHostLdap();
			DtoAuthenticationHostLdap uiLdap = (DtoAuthenticationHostLdap) theAuthHost;
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

	private PersBaseClientAuth<?> fromUi(BaseDtoClientSecurity theObj, BasePersServiceVersion theServiceVersion) {

		PersBaseClientAuth<?> retVal = null;
		switch (theObj.getType()) {
		case WSSEC_UT: {
			retVal = new PersWsSecUsernameTokenClientAuth();
			break;
		}
		case HTTP_BASICAUTH: {
			retVal = new PersHttpBasicClientAuth();
			break;
		}
		case JSONRPC_NAMPARM: {
			DtoClientSecurityJsonRpcNamedParameter obj = (DtoClientSecurityJsonRpcNamedParameter) theObj;
			retVal = new NamedParameterJsonRpcClientAuth();
			((NamedParameterJsonRpcClientAuth) retVal).setUsernameParameterName(obj.getUsernameParameterName());
			((NamedParameterJsonRpcClientAuth) retVal).setPasswordParameterName(obj.getPasswordParameterName());
			break;
		}
		}

		if (retVal == null) {
			throw new IllegalArgumentException("Unknown service type: " + theObj.getType());
		}

		retVal.setPid(theObj.getPidOrNull());
		retVal.setUsername(theObj.getUsername());
		retVal.setPassword(theObj.getPassword());
		retVal.setServiceVersion(theServiceVersion);

		return retVal;
	}

	private BasePersMonitorRule fromUi(BaseDtoMonitorRule theRule) throws ProcessingException {

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
				persRule.getActiveChecks().add(PersMonitorRuleActiveCheck.fromDto(next, persRule, myDao));
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

	private PersBaseServerAuth<?, ?> fromUi(BaseDtoServerSecurity theObj, BasePersServiceVersion theSvcVer) {
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

	private PersConfig fromUi(DtoConfig theConfig) {
		PersConfig retVal = new PersConfig();

		for (String next : theConfig.getProxyUrlBases()) {
			retVal.addProxyUrlBase(new PersConfigProxyUrlBase(next));
		}

		return retVal;
	}

	private PersDomain fromUi(DtoDomain theDomain) {
		PersDomain retVal = new PersDomain();
		retVal.setPid(theDomain.getPidOrNull());
		retVal.setDomainId(theDomain.getId());
		retVal.setDomainName(theDomain.getName());
		retVal.populateKeepRecentTransactionsFromDto(theDomain);
		retVal.populateServiceCatalogItemFromDto(theDomain);
		retVal.setDescription(theDomain.getDescription());
		return retVal;
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
		retVal.setDescription(theService.getDescription());
		retVal.populateKeepRecentTransactionsFromDto(theService);
		retVal.populateServiceCatalogItemFromDto(theService);
		return retVal;
	}

	private PersMethod fromUi(GServiceMethod theMethod, long theServiceVersionPid) {
		PersMethod retVal = new PersMethod();
		retVal.setName(theMethod.getName());
		retVal.setPid(theMethod.getPidOrNull());
		retVal.setServiceVersion(myDao.getServiceVersionByPid(theServiceVersionPid));
		retVal.setRootElements(theMethod.getRootElements());
		retVal.setSecurityPolicy(theMethod.getSecurityPolicy());
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

		if (theUser.getThrottle() != null) {
			retVal.setThrottleMaxRequests(theUser.getThrottle().getThrottleMaxRequests());
			retVal.setThrottlePeriod(theUser.getThrottle().getThrottlePeriod());
			retVal.setThrottleMaxQueueDepth(theUser.getThrottle().getThrottleMaxQueueDepth());
		}

		retVal.populateKeepRecentTransactionsFromDto(theUser);

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

	private Collection<PersUserDomainPermission> fromUi(List<GUserDomainPermission> theDomainPermissions, Collection<PersUserDomainPermission> theExisting) {
		Collection<PersUserDomainPermission> retVal = theExisting;
		retVal.clear();
		for (GUserDomainPermission next : theDomainPermissions) {
			retVal.add(fromUi(next));
		}
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

	private DtoAuthenticationHostList loadAuthHostList() {
		DtoAuthenticationHostList retVal = new DtoAuthenticationHostList();
		for (BasePersAuthenticationHost next : myDao.getAllAuthenticationHosts()) {
			BaseDtoAuthenticationHost uiObject = toUi(next);
			retVal.add(uiObject);
		}
		return retVal;
	}

	private DtoDomainList loadDomainList(Set<Long> theLoadDomStats, Set<Long> theLoadSvcStats, Set<Long> theLoadVerStats, Set<Long> theLoadVerMethodStats, Set<Long> theLoadUrlStats, StatusesBean theStatuses) throws UnexpectedFailureException {
		DtoDomainList domainList = new DtoDomainList();

		for (PersDomain nextDomain : myServiceRegistry.getAllDomains()) {
			DtoDomain gDomain = nextDomain.toDto(theLoadDomStats, theLoadSvcStats, theLoadVerStats, theLoadVerMethodStats, theLoadUrlStats, theStatuses, myRuntimeStatusQuerySvc);
			domainList.add(gDomain);
		} // for domains
		return domainList;
	}

	private GHttpClientConfigList loadHttpClientConfigList() throws ProcessingException {
		GHttpClientConfigList configList = new GHttpClientConfigList();
		for (PersHttpClientConfig next : myDao.getHttpClientConfigs()) {
			configList.add(next.toDto());
		}
		return configList;
	}

	private GUserList loadUserList(boolean theLoadStats) throws UnexpectedFailureException {
		GUserList retVal = new GUserList();
		Collection<PersUser> users = myDao.getAllUsersAndInitializeThem();
		for (PersUser persUser : users) {
			retVal.add(toUi(persUser, theLoadStats));
		}
		return retVal;
	}

	private int[] toIntArray(ArrayList<Integer> theList) {
		int[] retVal = new int[theList.size()];
		for (int i = 0; i < theList.size(); i++) {
			if (theList.get(i) != null) {
				retVal[i] = theList.get(i);
			}
		}
		return retVal;
	}

	private GSoap11ServiceVersionAndResources toUi(BaseDtoServiceVersion theUiService, BasePersServiceVersion theSvcVer) {
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

	private BaseDtoAuthenticationHost toUi(BasePersAuthenticationHost thePersObj) {
		BaseDtoAuthenticationHost retVal = null;
		switch (thePersObj.getType()) {
		case LOCAL_DATABASE:
			retVal = new DtoAuthenticationHostLocalDatabase();
			break;
		case LDAP:
			DtoAuthenticationHostLdap dto = new DtoAuthenticationHostLdap();
			PersAuthenticationHostLdap uiLdap = (PersAuthenticationHostLdap) thePersObj;
			retVal = dto;
			dto.setAuthenticateBaseDn(uiLdap.getAuthenticateBaseDn());
			dto.setAuthenticateFilter(uiLdap.getAuthenticateFilter());
			dto.setBindUserPassword(uiLdap.getBindPassword());
			dto.setBindUserDn(uiLdap.getBindUserDn());
			dto.setUrl(uiLdap.getUrl());
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

	private List<GUserDomainPermission> toUi(Collection<PersUserDomainPermission> theDomainPermissions) {
		List<GUserDomainPermission> retVal = new ArrayList<GUserDomainPermission>();
		for (PersUserDomainPermission next : theDomainPermissions) {
			retVal.add(toUi(next));
		}
		return retVal;
	}

	private List<GRecentMessage> toUi(List<? extends BasePersSavedTransactionRecentMessage> theServiceVersionRecentMessages, boolean theLoadMessageContents) {
		List<GRecentMessage> retVal = new ArrayList<GRecentMessage>();

		for (BasePersSavedTransactionRecentMessage next : theServiceVersionRecentMessages) {
			retVal.add(next.toDto(theLoadMessageContents));
		}

		return retVal;
	}

	private DtoLibraryMessage toUi(PersLibraryMessage theMessage, boolean theLoadContents) {
		DtoLibraryMessage retVal = new DtoLibraryMessage();

		retVal.setContentType(theMessage.getContentType());
		retVal.setDescription(theMessage.getDescription());
		retVal.setPid(theMessage.getPid());
		retVal.setMessageLength(theMessage.getMessageLength());

		for (PersLibraryMessageAppliesTo next : theMessage.getAppliesTo()) {
			retVal.getAppliesToServiceVersionPids().add(next.getPk().getServiceVersion().getPid());
		}

		if (theLoadContents) {
			retVal.setMessage(theMessage.getMessageBody());
		}

		return retVal;
	}

	private GUser toUi(PersUser thePersUser, boolean theLoadStats) throws UnexpectedFailureException {
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
		thePersUser.populateKeepRecentTransactionsToDto(retVal);

		if (thePersUser.getThrottleMaxRequests() != null) {
			retVal.setThrottle(new GThrottle());
			retVal.getThrottle().setThrottleMaxRequests(thePersUser.getThrottleMaxRequests());
			retVal.getThrottle().setThrottlePeriod(thePersUser.getThrottlePeriod());
			retVal.getThrottle().setThrottleMaxQueueDepth(thePersUser.getThrottleMaxQueueDepth());
		}

		if (theLoadStats) {
			PersUserStatus status = thePersUser.getStatus();
			retVal.setStatsInitialized(new Date());
			retVal.setStatsLastAccess(status.getLastAccess());

			StatsAccumulator accumulator = new StatsAccumulator();
			myRuntimeStatusQuerySvc.extract60MinuteUserStats(thePersUser, accumulator);

			accumulator.populateDto(retVal);

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

	private Collection<DtoLibraryMessage> toUiCollectionLibraryMessages(Collection<PersLibraryMessage> theMsgs, boolean theLoadContents) {
		ArrayList<DtoLibraryMessage> retVal = new ArrayList<DtoLibraryMessage>();
		for (PersLibraryMessage next : theMsgs) {
			retVal.add(toUi(next, theLoadContents));
		}
		return retVal;
	}

	// public static void doWithStatsByMinute(PersConfig theConfig, TimeRange
	// theRange, IRuntimeStatus theStatus, PersMethod
	// theNextMethod, IWithStats theOperator, Date end) {
	// Date start = new Date(end.getTime() -
	// (theRange.getWithPresetRange().getNumMins() * 60 * 1000L));
	// doWithStatsByMinute(theConfig, theStatus, theNextMethod, theOperator,
	// start, end);
	// }

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

	public static void doWithStatsByMinute(PersConfig theConfig, int theNumberOfMinutes, IRuntimeStatusQueryLocal statusSvc, PersMethod theMethod, IWithStats<PersInvocationMethodSvcverStatsPk, PersInvocationMethodSvcverStats> theOperator) {
		Date start = getDateXMinsAgoTruncatedToMinute(theNumberOfMinutes);
		Date end = new Date();

		doWithStatsByMinute(theConfig, statusSvc, theMethod, theOperator, start, end);
	}

	public static void doWithStatsByMinute(PersConfig theConfig, IRuntimeStatusQueryLocal statusSvc, PersMethod theMethod, IWithStats<PersInvocationMethodSvcverStatsPk, PersInvocationMethodSvcverStats> theOperator, Date start, Date end) {
		Date date = start;
		for (int min = 0; date.before(end); min++) {

			InvocationStatsIntervalEnum interval = doWithStatsSupportFindInterval(theConfig, date);
			date = doWithStatsSupportFindDate(date, interval);

			ourLog.info("Next stats: {} - {}", interval, date);

			PersInvocationMethodSvcverStatsPk pk = new PersInvocationMethodSvcverStatsPk(interval, date, theMethod.getPid());
			PersInvocationMethodSvcverStats stats = statusSvc.getInvocationStatsSynchronously(pk);
			assert stats != null : pk.toString();

			theOperator.withStats(min, stats);

			date = doWithStatsSupportIncrement(date, interval);

		}
	}

	public static void doWithStatsByMinute(PersConfig theConfig, TimeRange theRange, IRuntimeStatusQueryLocal theStatus, PersMethod theNextMethod, IWithStats<PersInvocationMethodSvcverStatsPk, PersInvocationMethodSvcverStats> theOperator) {
		Date end;
		Date start;
		if (theRange.getWithPresetRange() != null) {
			if (theRange.getWithPresetRangeEndForUnitTest() != null) {
				end = theRange.getWithPresetRangeEndForUnitTest();
			} else {
				end = new Date();
			}
			start = new Date(end.getTime() - (theRange.getWithPresetRange().getNumMins() * 60 * 1000L));
		} else {
			end = theRange.getNoPresetTo();
			start = theRange.getNoPresetFrom();
		}
		doWithStatsByMinute(theConfig, theStatus, theNextMethod, theOperator, start, end);
	}

	public static InvocationStatsIntervalEnum doWithStatsSupportFindInterval(PersConfig theConfig, Date date) {
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

	public static Date doWithStatsSupportIncrement(Date date, InvocationStatsIntervalEnum interval) {
		/*
		 * Note: don't just add millis to the date object, because that fails
		 * when adding a day when daylight savings starts/ends
		 */
		Calendar cal = DateUtils.toCalendar(date);
		switch (interval) {
		case DAY:
			cal.add(Calendar.DATE, 1);
			break;
		case HOUR:
			cal.add(Calendar.HOUR, 1);
			break;
		case MINUTE:
			cal.add(Calendar.MINUTE, 1);
			break;
		case TEN_MINUTE:
			cal.add(Calendar.MINUTE, 10);
			break;
		}
		return cal.getTime();
	}

	public static Date getDateXMinsAgoTruncatedToMinute(int theNumberOfMinutes) {
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

	public static int[] toArray(ArrayList<Integer> theT60minCount) {
		int[] retVal = new int[theT60minCount.size()];
		int index = 0;
		for (Integer integer : theT60minCount) {
			retVal[index++] = integer;
		}
		return retVal;
	}

	public static int[] toLatency(List<Long> theTimes, List<Integer> theCountsEntry0, List<Integer> theCountsEntry1, List<Integer> theCountsEntry2) {
		return toLatency(theTimes, theCountsEntry0, theCountsEntry1, theCountsEntry2, null);
	}

	private static Date doWithStatsSupportFindDate(Date date, InvocationStatsIntervalEnum interval) {
		Date retVal = interval.truncate(date);
		return retVal;
	}

	private static int[] toLatency(List<Long> theTimes, List<Integer> theCountsEntry0, List<Integer> theCountsEntry1, List<Integer> theCountsEntry2, List<Integer> theCountsEntry3) {
		List<List<Integer>> entries = new ArrayList<List<Integer>>(3);
		entries.add(theCountsEntry0);
		entries.add(theCountsEntry1);
		entries.add(theCountsEntry2);
		entries.add(theCountsEntry3);
		return toLatency2(theTimes, entries);
	}

	private static int[] toLatency2(List<Long> theTimes, List<List<Integer>> theCountsEntries) {
		if (theCountsEntries == null || theCountsEntries.size() == 0) {
			throw new IllegalArgumentException("No counts given");
		}

		int[] counts = new int[theTimes.size()];

		for (List<Integer> nextEntry : theCountsEntries) {
			if (nextEntry == null) {
				continue;
			}
			assert nextEntry.size() == theTimes.size();
			int index = 0;
			for (int next : nextEntry) {
				counts[index] = counts[index] + next;
				index++;
			}
		}

		int[] retVal = new int[counts.length];
		int prevValue = -1;
		for (int i = 0; i < counts.length; i++) {
			if (counts[i] > 0) {
				retVal[i] = (int) Math.min(theTimes.get(i) / counts[i], Integer.MAX_VALUE);
				prevValue = retVal[i];
			} else if (prevValue > -1) {
				retVal[i] = prevValue;
			}
		}

		return retVal;
	}

	public interface IWithStats<P extends BasePersStatsPk<P, O>, O extends BasePersStats<P, O>> {

		void withStats(int theIndex, O theStats);

	}

	// private GDomain toUi(PersDomain theDomain) {
	// GDomain retVal=new GDomain();
	// retVal.setPid(theDomain.getPid());
	// retVal.setId(theDomain.getDomainId());
	// retVal.setName(theDomain.getDomainName());
	// return retVal;
	// }

}
