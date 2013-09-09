package net.svcret.admin.server.rpc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.svcret.admin.client.rpc.HttpClientConfigService;
import net.svcret.admin.client.rpc.ModelUpdateService;
import net.svcret.admin.server.rpc.HttpClientConfigServiceImpl.SessionUploadedKeystore;
import net.svcret.admin.shared.ServiceFailureException;
import net.svcret.admin.shared.enm.MethodSecurityPolicyEnum;
import net.svcret.admin.shared.enm.RecentMessageTypeEnum;
import net.svcret.admin.shared.enm.ResponseTypeEnum;
import net.svcret.admin.shared.enm.ServerSecurityModeEnum;
import net.svcret.admin.shared.enm.ThrottlePeriodEnum;
import net.svcret.admin.shared.model.AddServiceVersionResponse;
import net.svcret.admin.shared.model.BaseDtoServiceCatalogItem;
import net.svcret.admin.shared.model.BaseGAuthHost;
import net.svcret.admin.shared.model.BaseGDashboardObject;
import net.svcret.admin.shared.model.BaseGMonitorRule;
import net.svcret.admin.shared.model.BaseGServiceVersion;
import net.svcret.admin.shared.model.DtoKeystoreAnalysis;
import net.svcret.admin.shared.model.DtoLibraryMessage;
import net.svcret.admin.shared.model.DtoMonitorRuleActive;
import net.svcret.admin.shared.model.DtoMonitorRuleActiveCheck;
import net.svcret.admin.shared.model.DtoServiceVersionSoap11;
import net.svcret.admin.shared.model.GAuthenticationHostList;
import net.svcret.admin.shared.model.GConfig;
import net.svcret.admin.shared.model.GDomain;
import net.svcret.admin.shared.model.GDomainList;
import net.svcret.admin.shared.model.GHttpClientConfig;
import net.svcret.admin.shared.model.GHttpClientConfigList;
import net.svcret.admin.shared.model.GLocalDatabaseAuthHost;
import net.svcret.admin.shared.model.GMonitorRuleAppliesTo;
import net.svcret.admin.shared.model.GMonitorRuleFiring;
import net.svcret.admin.shared.model.GMonitorRuleFiringProblem;
import net.svcret.admin.shared.model.GMonitorRuleList;
import net.svcret.admin.shared.model.GMonitorRulePassive;
import net.svcret.admin.shared.model.GPartialUserList;
import net.svcret.admin.shared.model.GRecentMessage;
import net.svcret.admin.shared.model.GRecentMessageLists;
import net.svcret.admin.shared.model.GService;
import net.svcret.admin.shared.model.GServiceList;
import net.svcret.admin.shared.model.GServiceMethod;
import net.svcret.admin.shared.model.GServiceVersionDetailedStats;
import net.svcret.admin.shared.model.GServiceVersionSingleFireResponse;
import net.svcret.admin.shared.model.GServiceVersionUrl;
import net.svcret.admin.shared.model.GUser;
import net.svcret.admin.shared.model.GUserDomainPermission;
import net.svcret.admin.shared.model.GUserList;
import net.svcret.admin.shared.model.HierarchyEnum;
import net.svcret.admin.shared.model.ModelUpdateRequest;
import net.svcret.admin.shared.model.ModelUpdateResponse;
import net.svcret.admin.shared.model.Pair;
import net.svcret.admin.shared.model.PartialUserListRequest;
import net.svcret.admin.shared.model.ServerSecuredEnum;
import net.svcret.admin.shared.model.ServiceProtocolEnum;
import net.svcret.admin.shared.model.StatusEnum;
import net.svcret.admin.shared.model.UrlSelectionPolicy;
import net.svcret.admin.shared.model.UserGlobalPermissionEnum;
import net.svcret.admin.shared.util.StringUtil;
import net.svcret.ejb.model.entity.BasePersAuthenticationHost;
import net.svcret.ejb.model.entity.InvocationStatsIntervalEnum;

import org.apache.commons.lang3.time.DateUtils;

public class ModelUpdateServiceMock implements ModelUpdateService, HttpClientConfigService {

	private static final long MET2_PID = 1001L;
	private static final long MET1_PID = 1000L;
	private static final long SVCVER_PID = 100L;
	private static long ourNextPid = 1000000L;
	private GAuthenticationHostList myAuthHostList;
	private GHttpClientConfigList myClientConfigList;
	private GConfig myConfig;
	private GDomainList myDomainList;
	private GUserList myUserList;
	private GMonitorRuleList myMonitorRuleList;

	public ModelUpdateServiceMock() {
		myConfig = new GConfig();
		myConfig.getProxyUrlBases().add("http://base/proxy");

		myDomainList = new GDomainList();

		{
		GDomain dom = new GDomain();
		dom.setPid(ourNextPid++);
		dom.setId("domain1");
		dom.setName("Domain 1");
		Set<String> obscure=new HashSet<String>();
		obscure.add("clientId");
		obscure.add("clientPass");
		dom.setObscureRequestElementsInLogCache(obscure);
		myDomainList.add(dom);

		GService svc = new GService();
		svc.setId("svc1a");
		svc.setName("Service 1-A");
		svc.setPid(10L);
		svc.setCanInheritKeepNumRecentTransactions(true);
		svc.setInheritedKeepNumRecentTransactionsSuccess(23);
		svc.setInheritedKeepNumRecentTransactionsSecurityFail(22);
		dom.getServiceList().add(svc);

		DtoServiceVersionSoap11 ver = new DtoServiceVersionSoap11();
		ver.setActive(true);
		ver.setWsdlLocation("http://foo");
		ver.setId("Version 1-A-1");
		ver.setPid(SVCVER_PID);
		ver.setName("Version 1-A-1");
		ver.setDefaultProxyPath("/some/service");
		ver.setLastAccess(new Date());
		ver.setServerSecurityMode(ServerSecurityModeEnum.REQUIRE_ANY);
		svc.getVersionList().add(ver);

		GServiceVersionUrl url = new GServiceVersionUrl();
		url.setPid(1);
		url.setId("url1");
		url.setUrl("http://foo");
		url.setStatsLastFailure(new Date());
		url.setStatsLastFailureMessage("This is a fail message");
		url.setStatsLastSuccess(new Date());
		url.setStatsLastSuccessMessage("This is a success message");
		url.setStatus(StatusEnum.ACTIVE);
		ver.getUrlList().add(url);

		url = new GServiceVersionUrl();
		url.setPid(2);
		url.setId("url2");
		url.setUrl("http://bar");
		url.setStatsLastFailure(new Date());
		url.setStatsLastFailureMessage("This is a fail message");
		url.setStatsLastSuccess(new Date());
		url.setStatsLastSuccessMessage("This is a success message");
		url.setStatus(StatusEnum.DOWN);
		url.setNextCircuitBreakerReset(new Date(System.currentTimeMillis()+100000));
		ver.getUrlList().add(url);
		
		GServiceMethod met = new GServiceMethod();
		met.setPid(MET1_PID);
		met.setId("Method 1");
		met.setName("Method 1");
		met.setSecurityPolicy(MethodSecurityPolicyEnum.REJECT_UNLESS_ALLOWED);
		met.setRootElements("http://ws.ehr.uhn.ca:getActivePatientsByAttendingPhysicianIdExtended");
		ver.getMethodList().add(met);

		met = new GServiceMethod();
		met.setPid(MET2_PID);
		met.setId("Method 2");
		met.setName("Method 2");
		met.setSecurityPolicy(MethodSecurityPolicyEnum.REJECT_UNLESS_ALLOWED);
		ver.getMethodList().add(met);

		svc = new GService();
		svc.setCanInheritKeepNumRecentTransactions(true);
		svc.setId("svc1b");
		svc.setName("Service 1-B");
		svc.setPid(11L);
		dom.getServiceList().add(svc);
		}
		{
		GDomain dom = new GDomain();
		dom.setPid(ourNextPid++);
		dom.setId("domain2");
		dom.setName("Domain 2");
		myDomainList.add(dom);

		GService svc = new GService();
		svc.setId("svc1a");
		svc.setName("Service 2-A");
		svc.setPid(ourNextPid++);
		svc.setCanInheritKeepNumRecentTransactions(true);
		svc.setInheritedKeepNumRecentTransactionsSuccess(23);
		svc.setInheritedKeepNumRecentTransactionsSecurityFail(22);
		dom.getServiceList().add(svc);

		DtoServiceVersionSoap11 ver = new DtoServiceVersionSoap11();
		ver.setActive(true);
		ver.setWsdlLocation("http://foo");
		ver.setId("Version 2-A-1");
		ver.setPid(ourNextPid++);
		ver.setName("Version 2-A-1");
		ver.setDefaultProxyPath("/some/service");
		ver.setLastAccess(new Date());
		ver.setServerSecurityMode(ServerSecurityModeEnum.REQUIRE_ANY);
		svc.getVersionList().add(ver);

		}
		
		myClientConfigList = new GHttpClientConfigList();
		GHttpClientConfig defCfg = new GHttpClientConfig();
		defCfg.setPid(ourNextPid++);
		defCfg.setId("DEFAULT");
		defCfg.setName("Default (Can't be mopdified)");
		defCfg.setUrlSelectionPolicy(UrlSelectionPolicy.PREFER_LOCAL);
		defCfg.setCircuitBreakerTimeBetweenResetAttempts(60000);
		defCfg.setReadTimeoutMillis(1000);
		defCfg.setConnectTimeoutMillis(2000);
		defCfg.setCircuitBreakerEnabled(true);
		defCfg.setFailureRetriesBeforeAborting(1);
		defCfg.setTlsKeystore(new DtoKeystoreAnalysis());
		defCfg.getTlsKeystore().getKeyAliases().add("alias1");
		defCfg.getTlsKeystore().getKeyAliases().add("alias2");
		defCfg.getTlsKeystore().getExpiryDate().put("alias1", new Date(System.currentTimeMillis() + 10000L));
		defCfg.getTlsKeystore().getExpiryDate().put("alias2", new Date(System.currentTimeMillis() + 20000L));
		defCfg.getTlsKeystore().getIssuer().put("alias1", "CN=Issuer1");
		defCfg.getTlsKeystore().getIssuer().put("alias2", "CN=Issuer2");
		defCfg.getTlsKeystore().getSubject().put("alias1", "CN=Subject1");
		defCfg.getTlsKeystore().getSubject().put("alias2", "CN=Subject2");
		defCfg.getTlsKeystore().setPasswordAccepted(true);
		defCfg.getTlsKeystore().setPassword("changeit");
		myClientConfigList.add(defCfg);

		myAuthHostList = new GAuthenticationHostList();
		GLocalDatabaseAuthHost hostList = new GLocalDatabaseAuthHost();
		hostList.setPid(ourNextPid++);
		hostList.setModuleId(BasePersAuthenticationHost.MODULE_ID_ADMIN_AUTH);
		hostList.setModuleName(BasePersAuthenticationHost.MODULE_DESC_ADMIN_AUTH);
		hostList.setSupportsPasswordChange(true);
		myAuthHostList.add(hostList);

		myUserList = new GUserList();
		GUser user = new GUser();
		user.setPid(ourNextPid++);
		user.setUsername("admin");
		user.setAuthHostPid(hostList.getPid());
		user.addGlobalPermission(UserGlobalPermissionEnum.SUPERUSER);
		user.setStatsLastAccess(new Date());
		user.setStatsInitialized(new Date());
		populateRandom(user);
		user.setAllowableSourceIps(new ArrayList<String>());
		myUserList.add(user);

		user = new GUser();
		user.setPid(ourNextPid++);
		user.setUsername("testuser");
		GUserDomainPermission perm = new GUserDomainPermission();
		perm.setPid(ourNextPid++);
		perm.setAllowAllServices(true);
		user.addDomainPermission(perm);
		user.setAuthHostPid(hostList.getPid());
		user.addGlobalPermission(UserGlobalPermissionEnum.SUPERUSER);
		user.setStatsInitialized(new Date());
		populateRandom(user);
		user.setAllowableSourceIps(new ArrayList<String>());
		user.getAllowableSourceIps().add("127.0.0.1");
		user.getAllowableSourceIps().add("192.168.1.1");
		myUserList.add(user);

		myMonitorRuleList = new GMonitorRuleList();
		{
			GMonitorRulePassive newRule = new GMonitorRulePassive();
			newRule.setPid(ourNextPid++);
			myMonitorRuleList.add(newRule);
			newRule.setName("Demo Rule");
			newRule.setPassiveFireForBackingServiceLatencyIsAboveMillis(100);
			newRule.setPassiveFireForBackingServiceLatencySustainTimeMins(5);
			newRule.getNotifyEmailContacts().add("foo@example.com");
			newRule.getAppliesTo().add(new GMonitorRuleAppliesTo());
			newRule.getAppliesTo().iterator().next().setDomainPid(1L);
			newRule.getAppliesTo().iterator().next().setDomainName("Service Domain");
			newRule.getAppliesTo().iterator().next().setServicePid(2L);
			newRule.getAppliesTo().iterator().next().setServiceName("Service Domain");
		}
		{
			DtoMonitorRuleActive newRule = new DtoMonitorRuleActive();
			newRule.setPid(ourNextPid++);
			myMonitorRuleList.add(newRule);
			newRule.setName("Demo Rule");
			newRule.getNotifyEmailContacts().add("foo2@example.com");
			DtoMonitorRuleActiveCheck check = new DtoMonitorRuleActiveCheck();
			check.setCheckFrequencyNum(2);
			check.setCheckFrequencyUnit(ThrottlePeriodEnum.MINUTE);
			check.setExpectLatencyUnderMillis(100L);
			check.setExpectResponseContainsText("hello");
			check.setExpectResponseType(ResponseTypeEnum.SUCCESS);
			check.setLastTransactionDate(new Date());
			check.setLastTransactionOutcome(true);
			check.setMessageDescription("this is the description 1");
			check.setMessagePid(1L);
			check.setServiceVersionPid(100L);
			newRule.getCheckList().add(check);
		}

	}

	@Override
	public GDomain addDomain(GDomain theDomain) throws ServiceFailureException {
		theDomain.setPid(ourNextPid++);
		myDomainList.add(theDomain);
		return theDomain;
	}

	@Override
	public GService addService(long theDomainPid, String theId, String theName, boolean theActive) {

		GDomain dom = myDomainList.getDomainByPid(theDomainPid);

		GService svc = new GService();
		svc.setCanInheritKeepNumRecentTransactions(true);
		svc.setPid(ourNextPid++);
		svc.setId(theId);
		svc.setName(theName);
		svc.setStatus(StatusEnum.ACTIVE);
		svc.setTransactions60mins(random60mins());
		svc.setLatency60mins(random60mins());
		svc.setActive(theActive);

		dom.getServiceList().add(svc);

		return svc;
	}

	@Override
	public AddServiceVersionResponse addServiceVersion(Long theExistingDomainPid, String theCreateDomainId, Long theExistingServicePid, String theCreateServiceId, BaseGServiceVersion theVersion) {
		GDomain dom;
		if (theExistingDomainPid != null) {
			dom = myDomainList.getDomainByPid(theExistingDomainPid);
			if (dom == null) {
				throw new NullPointerException("Unknown dom " + theExistingDomainPid);
			}
		} else {
			dom = new GDomain();
			dom.setPid(ourNextPid++);
			dom.setId(theCreateDomainId);
			dom.setName(theCreateDomainId);
			myDomainList.add(dom);
		}

		GService svc;
		if (theExistingServicePid != null) {
			svc = dom.getServiceList().getServiceByPid(theExistingServicePid);
			if (svc == null) {
				throw new NullPointerException("Unknown service " + theExistingServicePid);
			}
		} else {
			svc = new GService();
			svc.setCanInheritKeepNumRecentTransactions(true);
			svc.setPid(ourNextPid++);
			svc.setId(theCreateServiceId);
			svc.setName(theCreateServiceId);
			dom.getServiceList().add(svc);
		}

		if (theVersion.getPidOrNull() != null) {
			BaseGServiceVersion ver = myDomainList.getServiceVersionByPid(theVersion.getPid());
			ver.merge(theVersion);
			theVersion = ver;

		} else {
			theVersion.setPid(ourNextPid++);
			svc.getVersionList().add(theVersion);
		}

		AddServiceVersionResponse retVal = null;
		retVal = new AddServiceVersionResponse();
		retVal.setNewDomain(dom);
		retVal.setNewService(svc);
		retVal.setNewServiceVersion(theVersion);
		return retVal;

	}

	private GRecentMessage createMessage(boolean theIncludeContents, RecentMessageTypeEnum theType) {
		GRecentMessage retVal = new GRecentMessage();

		return createMessage(theIncludeContents, retVal, theType);
	}

	private GRecentMessage createMessage(boolean theIncludeContents, GRecentMessage retVal, RecentMessageTypeEnum theType) {
		String responseMessage = "<req><ello><req><ello><req><ello>some text</ello></req><req><ello><req><ello><req><ello><req><ello><req><ello><req><ello>some text</ello></req><req><ello><req><ello><req><ello>some text</ello></req>some text</ello></req>some text</ello></req>some text</ello></req><req><ello><req><ello><req><ello>some text</ello></req><req><ello><req><ello><req><ello><req><ello><req><ello><req><ello>some text</ello></req><req><ello><req><ello><req><ello>some text</ello></req>some text</ello></req>some text</ello></req>some text</ello></req>some text</ello></req>some text</ello></req>some text</ello></req>some text</ello></req>some text</ello></req>some text</ello></req>some text</ello></req>some text</ello></req>some text</ello></req>some text</ello></req>some text</ello></req>some text</ello><req><ello><req><ello><req><ello>some text</ello></req><req><ello><req><ello><req><ello><req><ello><req><ello><req><ello>some text</ello></req><req><ello><req><ello><req><ello>some text</ello></req>some text</ello></req>some text</ello></req>some text</ello></req><req><ello><req><ello><req><ello>some text</ello></req><req><ello><req><ello><req><ello><req><ello><req><ello><req><ello>some text</ello></req><req><ello><req><ello><req><ello>some text</ello></req>some text</ello></req>some text</ello></req>some text</ello></req>some text</ello></req>some text</ello></req>some text</ello></req>some text</ello></req>some text</ello></req>some text</ello></req>some text</ello></req>some text</ello></req>some text</ello></req>some text</ello></req>some text</ello></req>some text</ello></req></req>";
		String requestMessage = "{\"aaaa\":444, \"bbb\": 555}";
		List<Pair<String>> reqHeaders = new ArrayList<Pair<String>>();
		reqHeaders.add(new Pair<String>("Content-Type", "text/json"));
		reqHeaders.add(new Pair<String>("Content-Encoding", "chunked"));
		List<Pair<String>> respHeaders = new ArrayList<Pair<String>>();
		respHeaders.add(new Pair<String>("Content-Type", "text/xml"));
		respHeaders.add(new Pair<String>("X-Server", "Some Server"));
		String reqCt = "text/json";
		String respCt = "text/xml";
		if (!theIncludeContents) {
			responseMessage = null;
			requestMessage = null;
			reqHeaders = null;
			respHeaders = null;
			reqCt = null;
			respCt = null;
		}

		retVal.setPid(ourNextPid++);
		
		long offset = ((long)(1000000.0 * Math.random()));
		
		retVal.setTransactionTime(new Date(System.currentTimeMillis() - offset));
		retVal.setRequestHostIp("http://foo");
		retVal.setRequestMessage(requestMessage);
		retVal.setResponseMessage(responseMessage);
		retVal.setRequestHeaders(reqHeaders);
		retVal.setResponseHeaders(respHeaders);
		retVal.setRequestContentType(reqCt);
		retVal.setResponseContentType(respCt);

		retVal.setImplementationUrlPid(999L);
		retVal.setImplementationUrlId("dev1");
		retVal.setImplementationUrlHref("http://foo");
		retVal.setDomainPid(0l);
		retVal.setDomainName("DomainName1");
		retVal.setServicePid(0l);
		retVal.setServiceName("ServiceName1");
		retVal.setServiceVersionPid(SVCVER_PID);
		retVal.setMethodPid(MET1_PID);
		retVal.setMethodName("SomeMethod");
		retVal.setServiceVersionId("1.0");

		retVal.setRecentMessageType(theType);

		return retVal;
	}

	@Override
	public DtoServiceVersionSoap11 createNewServiceVersion(ServiceProtocolEnum theProtocol, Long theDomainPid, Long theServicePid, Long theUncomittedId) {
		throw new UnsupportedOperationException();
	}

	@Override
	public GHttpClientConfigList deleteHttpClientConfig(long thePid) throws ServiceFailureException {
		GHttpClientConfig config = myClientConfigList.getConfigByPid(thePid);
		myClientConfigList.remove(config);

		return getHttpClientConfigList();
	}

	private GAuthenticationHostList getAuthHostList() {
		GAuthenticationHostList retVal = new GAuthenticationHostList();
		retVal.mergeResults(myAuthHostList);
		return retVal;
	}

	public long getDefaultHttpClientConfigPid() {
		return myClientConfigList.get(0).getPid();
	}

	private GHttpClientConfigList getHttpClientConfigList() {
		GHttpClientConfigList clientConfigList = new GHttpClientConfigList();
		clientConfigList.mergeResults(myClientConfigList);
		return clientConfigList;
	}

	@Override
	public GConfig loadConfig() {
		return myConfig;
	}

	@Override
	public ModelUpdateResponse loadModelUpdate(ModelUpdateRequest theRequest) {
		ModelUpdateResponse retVal = new ModelUpdateResponse();

		retVal.setDomainList(new GDomainList());
		retVal.getDomainList().mergeResults(myDomainList);

		for (GDomain nextDomain : retVal.getDomainList()) {
			Set<Long> domainsToLoadStats = theRequest.getDomainsToLoadStats();
			long nextDomainPid = nextDomain.getPid();
			if (domainsToLoadStats.contains(nextDomainPid)) {
				populateRandom(nextDomain);
			}
			for (GService nextService : nextDomain.getServiceList()) {
				if (theRequest.getServicesToLoadStats().contains(nextService.getPid())) {
					populateRandom(nextService);
				}
				for (BaseGServiceVersion nextVersion : nextService.getVersionList()) {
					if (theRequest.getVersionsToLoadStats().contains(nextVersion.getPid())) {
						populateRandom(nextVersion);
					}
					for (GServiceMethod nextMethod : nextVersion.getMethodList()) {
						if (theRequest.getVersionMethodsToLoadStats().contains(nextMethod.getPid())) {
							populateRandom(nextMethod);
						}
					}
					for (GServiceVersionUrl nextUrl : nextVersion.getUrlList()) {
						if (theRequest.getUrlsToLoadStats().contains(nextUrl.getPid())) {
							StatusEnum status = nextUrl.getStatus();
							populateRandom(nextUrl);
							nextUrl.setStatus(status);
						}
					}
				}
			}
		}

		if (theRequest.isLoadHttpClientConfigs()) {
			retVal.setHttpClientConfigList(getHttpClientConfigList());
		}

		if (theRequest.isLoadAuthHosts()) {
			retVal.setAuthenticationHostList(getAuthHostList());
		}

		return retVal;
	}

	@Override
	public GRecentMessage loadRecentMessageForServiceVersion(long thePid) {
		return createMessage(true, RecentMessageTypeEnum.SVCVER);
	}

	@Override
	public GRecentMessage loadRecentMessageForUser(long thePid) {
		return createMessage(true, RecentMessageTypeEnum.USER);
	}

	@Override
	public GRecentMessageLists loadRecentTransactionListForServiceVersion(long theServiceVersionPid) {
		return loadRecentTransactionList(RecentMessageTypeEnum.SVCVER);
	}

	private GRecentMessageLists loadRecentTransactionList(RecentMessageTypeEnum theType) {
		GRecentMessageLists retVal = new GRecentMessageLists();

		ArrayList<GRecentMessage> list = new ArrayList<GRecentMessage>();
		// list.add(createMessage(false));
		// list.add(createMessage(false));
		// list.add(createMessage(false));
		// list.add(createMessage(false));
		// retVal.setSuccessList(list);
		// retVal.setKeepSuccess(10);

		list = new ArrayList<GRecentMessage>();
		list.add(createMessage(false, theType));
		list.add(createMessage(false, theType));
		retVal.setFailList(list);
		retVal.setKeepFail(10);

		list = new ArrayList<GRecentMessage>();
		list.add(createMessage(false, theType));
		list.add(createMessage(false, theType));
		list.add(createMessage(false, theType));
		list.add(createMessage(false, theType));
		retVal.setSecurityFailList(list);
		retVal.setKeepSecurityFail(10);

		list = new ArrayList<GRecentMessage>();
		list.add(createMessage(false, theType));
		list.add(createMessage(false, theType));
		list.add(createMessage(false, theType));
		list.add(createMessage(false, theType));
		retVal.setFaultList(list);
		retVal.setKeepFault(10);

		return retVal;
	}

	@Override
	public GRecentMessageLists loadRecentTransactionListForuser(long thePid) {
		return loadRecentTransactionList(RecentMessageTypeEnum.USER);
	}

	@Override
	public GServiceVersionDetailedStats loadServiceVersionDetailedStats(long theVersionPid) {
		for (GDomain nextDomain : myDomainList) {
			for (GService nextService : nextDomain.getServiceList()) {
				for (BaseGServiceVersion nextVersion : nextService.getVersionList()) {
					if (nextVersion.getPid() == theVersionPid) {
						GServiceVersionDetailedStats retVal = new GServiceVersionDetailedStats();
						Map<Long, int[]> fail = new HashMap<Long, int[]>();
						Map<Long, int[]> fault = new HashMap<Long, int[]>();
						Map<Long, int[]> securityFail = new HashMap<Long, int[]>();
						Map<Long, int[]> success = new HashMap<Long, int[]>();
						for (GServiceMethod nextMethod : nextVersion.getMethodList()) {
							success.put(nextMethod.getPid(), random60mins());
							fail.put(nextMethod.getPid(), random60mins());
							securityFail.put(nextMethod.getPid(), random60mins());
							fault.put(nextMethod.getPid(), random60mins());
						}
						retVal.setMethodPidToSuccessCount(success);
						retVal.setMethodPidToFailCount(fail);
						retVal.setMethodPidToFaultCount(fault);
						retVal.setMethodPidToSecurityFailCount(securityFail);
						return retVal;
					}
				}
			}
		}
		throw new IllegalArgumentException("Can't find " + theVersionPid);
	}

	@Override
	public BaseGServiceVersion loadServiceVersionIntoSession(long theServiceVersionPid) throws ServiceFailureException {
		BaseGServiceVersion ver = myDomainList.getServiceVersionByPid(theServiceVersionPid);
		return ver;
	}

	@Override
	public UserAndAuthHost loadUser(long thePid, boolean theLoadStats) {
		GUser user = myUserList.getUserByPid(thePid);
		BaseGAuthHost authHost = myAuthHostList.getAuthHostByPid(user.getAuthHostPid());

		return new UserAndAuthHost(user, authHost);
	}

	@Override
	public GPartialUserList loadUsers(PartialUserListRequest theRequest) {
		GPartialUserList retVal = new GPartialUserList();
		retVal.addAll(myUserList.toCollection());
		return retVal;
	}

	@Override
	public DtoServiceVersionSoap11 loadWsdl(DtoServiceVersionSoap11 theService, String theWsdlUrl) throws ServiceFailureException {
		if (StringUtil.isBlank(theWsdlUrl)) {
			throw new ServiceFailureException("Failed to load URL: \"" + theWsdlUrl + '"');
		}

		DtoServiceVersionSoap11 retVal = new DtoServiceVersionSoap11();
		retVal.setWsdlLocation(theWsdlUrl);

		retVal.setActive(true);
		retVal.setUncommittedSessionId(theService.getUncommittedSessionId());

		GServiceMethod method = new GServiceMethod();
		method.setId("method1");
		method.setName("method1");
		method.setSecurityPolicy(MethodSecurityPolicyEnum.REJECT_UNLESS_ALLOWED);
		retVal.getMethodList().add(method);

		method = new GServiceMethod();
		method.setId("method2");
		method.setName("method2");
		method.setSecurityPolicy(MethodSecurityPolicyEnum.REJECT_UNLESS_ALLOWED);
		retVal.getMethodList().add(method);

		GServiceVersionUrl url = new GServiceVersionUrl();
		url.setId("url1");
		url.setUrl("http://something/aaaa.html");
		url.setStatsLastFailure(new Date());
		url.setStatsLastFailureMessage("This is a fail message");
		url.setStatsLastSuccess(new Date());
		url.setStatsLastSuccessMessage("This is a success message");
		url.setStatus(StatusEnum.ACTIVE);
		retVal.getUrlList().add(url);


		
		return retVal;
	}

	private void populateRandom(BaseGDashboardObject obj) {
		obj.setStatsInitialized(new Date());
		obj.setStatus(randomStatus());
		obj.setTransactions60mins(random60mins());
		obj.setTransactionsFail60mins(random60mins());
		obj.setTransactionsFault60mins(random60mins());
		obj.setTransactionsSecurityFail60mins(random60mins());
		obj.setLatency60mins(random60mins());
		obj.setStatistics60MinuteFirstDate(InvocationStatsIntervalEnum.MINUTE.truncate(new Date(System.currentTimeMillis() - (59 * DateUtils.MILLIS_PER_MINUTE))));
	}

	private void populateRandom(BaseDtoServiceCatalogItem obj) {
		obj.setStatsInitialized(new Date());
		obj.setStatus(randomStatus());
		obj.setTransactions60mins(random60mins());
		obj.setTransactionsFail60mins(random60mins());
		obj.setTransactionsFault60mins(random60mins());
		obj.setTransactionsSecurityFail60mins(random60mins());
		obj.setLatency60mins(random60mins());
		obj.setUrlsActive(randomUrlNumber());
		obj.setUrlsDown(randomUrlNumber());
		obj.setUrlsUnknown(randomUrlNumber());
		obj.setLastSuccessfulInvocation(randomRecentDate());
		obj.setLastServerSecurityFailure(randomRecentDate());
		obj.setServerSecured(ServerSecuredEnum.FULLY);
		obj.setStatistics60MinuteFirstDate(InvocationStatsIntervalEnum.MINUTE.truncate(new Date(System.currentTimeMillis() - (59 * DateUtils.MILLIS_PER_MINUTE))));
	}

	private int[] random60mins() {
		int[] retVal = new int[60];
		for (int i = 0; i < 60; i++) {
			retVal[i] = (int) (Math.random() * 100.0);
		}
		return retVal;
	}

	private List<Integer> random60minsList() {
		List<Integer> retVal = new ArrayList<Integer>();
		for (int next : random60mins()) {
			retVal.add(next);
		}
		return retVal;
	}

	private Date randomRecentDate() {
		return new Date(System.currentTimeMillis() - (long) (DateUtils.MILLIS_PER_DAY * Math.random()));
	}

	private StatusEnum randomStatus() {
		double rnd = 3.0 * Math.random();
		if (rnd < 1) {
			return StatusEnum.ACTIVE;
		}
		if (rnd < 2) {
			return StatusEnum.DOWN;
		}
		return StatusEnum.UNKNOWN;
	}

	private int randomUrlNumber() {
		return (int) (5.0 * Math.random());
	}

	@Override
	public GAuthenticationHostList removeAuthenticationHost(long thePid) {
		myAuthHostList.remove(myAuthHostList.getAuthHostByPid(thePid));
		return getAuthHostList();
	}

	@Override
	public GDomainList removeDomain(long thePid) {
		myDomainList.remove(myDomainList.getDomainByPid(thePid));
		return myDomainList;
	}

	@Override
	public GDomainList removeService(long theDomainPid, long theServicePid) {
		GServiceList serviceList = myDomainList.getDomainByPid(theDomainPid).getServiceList();
		serviceList.remove(serviceList.getServiceByPid(theServicePid));
		return myDomainList;
	}

	@Override
	public GDomainList removeServiceVersion(long thePid) {
		for (GDomain nextDomain : myDomainList) {
			for (GService nextSvc : nextDomain.getServiceList()) {
				BaseGServiceVersion ver = nextSvc.getVersionList().getVersionByPid(thePid);
				if (ver != null) {
					nextSvc.getVersionList().remove(ver);
				}
			}
		}

		return myDomainList;
	}

	@Override
	public void reportClientError(String theMessage, Throwable theException) {
		throw new UnsupportedOperationException();
	}

	@Override
	public GAuthenticationHostList saveAuthenticationHost(BaseGAuthHost theAuthHost) {
		if (theAuthHost.getPid() <= 0) {
			theAuthHost.setPid(ourNextPid++);
			myAuthHostList.add(theAuthHost);
		} else {
			myAuthHostList.getAuthHostByPid(theAuthHost.getPid()).merge(theAuthHost);
		}
		return myAuthHostList;
	}

	@Override
	public void saveConfig(GConfig theConfig) {
		myConfig = theConfig;
	}

	@Override
	public GDomainList saveDomain(GDomain theDomain) {
		GDomain retVal = myDomainList.getDomainByPid(theDomain.getPid());
		retVal.merge(theDomain);

		for (GService next : retVal.getServiceList()) {
			next.setInheritedKeepNumRecentTransactionsSuccess(retVal.getKeepNumRecentTransactionsSuccess());
			next.setInheritedKeepNumRecentTransactionsFail(retVal.getKeepNumRecentTransactionsFail());
			next.setInheritedKeepNumRecentTransactionsFault(retVal.getKeepNumRecentTransactionsFault());
			next.setInheritedKeepNumRecentTransactionsSecurityFail(retVal.getKeepNumRecentTransactionsSecurityFail());
		}

		return myDomainList;
	}

	@Override
	public GHttpClientConfig saveHttpClientConfig(boolean theCreate, boolean theUseNewTruststore, boolean theUseNewKeystore, GHttpClientConfig theConfig) {
		if (theCreate) {
			theConfig.setPid(ourNextPid++);
			myClientConfigList.add(theConfig);
			return theConfig;
		} else {
			GHttpClientConfig existing = myClientConfigList.getConfigByPid(theConfig.getPid());
			existing.merge(theConfig);
			return existing;
		}
	}

	@Override
	public GDomainList saveService(GService theService) {
		theService.removeVersionList();
		
		for (GDomain nextDomain : myDomainList) {
			for (GService nextService : nextDomain.getServiceList()) {
				if (nextService.getPid() == theService.getPid()) {
					nextService.merge(theService);
				}
			}
		}
		return myDomainList;
	}

	@Override
	public void saveServiceVersionToSession(BaseGServiceVersion theServiceVersion) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void saveUser(GUser theUser) {
		myUserList.getUserByPid(theUser.getPid()).merge(theUser);
	}

	@Override
	public GMonitorRuleList loadMonitorRuleList() {
		return myMonitorRuleList;
	}

	@Override
	public GMonitorRuleList saveMonitorRule(BaseGMonitorRule theRule) {
		if (theRule.getPidOrNull() != null) {
			myMonitorRuleList.remove(myMonitorRuleList.getRuleByPid(theRule.getPidOrNull()));
			myMonitorRuleList.add(theRule);
		} else {
			theRule.setPid(ourNextPid++);
			myMonitorRuleList.add(theRule);
		}
		return myMonitorRuleList;
	}

	@Override
	public List<GMonitorRuleFiring> loadMonitorRuleFirings(Long theDomainPid, Long theServicePid, Long theServiceVersionPid, int theStart) {
		ArrayList<GMonitorRuleFiring> list = new ArrayList<GMonitorRuleFiring>();
		GMonitorRuleFiring firing = new GMonitorRuleFiring();
		firing.setStartDate(new Date());
		firing.setEndDate(new Date());
		firing.getProblems().add(new GMonitorRuleFiringProblem());
		firing.getProblems().get(0).setServiceVersionPid(myDomainList.get(0).getServiceList().get(0).getVersionList().get(0).getPid());
		firing.getProblems().get(0).setFailedLatencyAverageMillisPerCall(57l);
		firing.getProblems().get(0).setFailedLatencyAverageOverMinutes(5l);
		firing.getProblems().get(0).setFailedLatencyThreshold(20l);

		list.add(firing);
		list.add(firing);
		list.add(firing);
		list.add(firing);
		list.add(firing);
		list.add(firing);
		list.add(firing);
		list.add(firing);
		list.add(firing);
		list.add(firing);
		list.add(firing);
		list.add(firing);
		list.add(firing);
		list.add(firing);
		list.add(firing);
		list.add(firing);
		list.add(firing);
		list.add(firing);
		list.add(firing);
		list.add(firing);
		list.add(firing);
		list.add(firing);
		list.add(firing);
		list.add(firing);
		list.add(firing);
		list.add(firing);
		list.add(firing);
		list.add(firing);
		list.add(firing);
		return list;
	}

	@Override
	public GServiceVersionSingleFireResponse testServiceVersionWithSingleMessage(String theMessageText, String theContentType, long theServiceVersionPid) {
		return (GServiceVersionSingleFireResponse) createMessage(true, new GServiceVersionSingleFireResponse(), RecentMessageTypeEnum.SVCVER);
	}

	@Override
	public DtoLibraryMessage loadLibraryMessage(long theMessagePid) {
		DtoLibraryMessage msg = new DtoLibraryMessage();
		Collection<Long> pids = myDomainList.getAllServiceVersionPids();
		msg.setAppliesToServiceVersionPids(pids.toArray(new Long[pids.size()]));
		msg.setContentType("text/xml");
		msg.setDescription("this is the description msg " + theMessagePid);
		msg.setMessage("<tag>this is the message</tag>");
		msg.setPid(1L);
		return msg;
	}

	@Override
	public Collection<DtoLibraryMessage> loadLibraryMessages(HierarchyEnum theHierarchy, long thePid) throws ServiceFailureException {
		ArrayList<DtoLibraryMessage> retVal = new ArrayList<DtoLibraryMessage>();

		for (int i = 0; i < 10; i++) {
			DtoLibraryMessage msg = new DtoLibraryMessage();
			Collection<Long> pids = myDomainList.getAllServiceVersionPids();
			msg.setAppliesToServiceVersionPids(pids.toArray(new Long[pids.size()]));
			msg.setContentType("text/xml");
			msg.setDescription("this is the description msg " + i);
			msg.setMessage("<tag>this is the message</tag>");
			msg.setPid((long) i);
			retVal.add(msg);
		}

		return retVal;
	}

	@Override
	public void saveLibraryMessage(DtoLibraryMessage theMessage) {
		// nothing
	}

	@Override
	public Collection<DtoLibraryMessage> loadLibraryMessages() throws ServiceFailureException {
		return loadLibraryMessages(null, 0);
	}

	@Override
	public Map<Long, GMonitorRuleFiring> getLatestFailingMonitorRuleFiringForRulePids() {
		return new HashMap<Long, GMonitorRuleFiring>();
	}

	@Override
	public DtoKeystoreAnalysis analyzeTransientTrustStore(long theHttpClientConfig) {
		throw new UnsupportedOperationException();
	}

	@Override
	public DtoKeystoreAnalysis analyzeTransientKeyStore(long theHttpClientConfig) {
		throw new UnsupportedOperationException();
	}

	public DtoKeystoreAnalysis analyzeKeyStore(SessionUploadedKeystore theKs) {
		DtoKeystoreAnalysis retVal=new DtoKeystoreAnalysis();
		if (theKs.getPassword().equals("changeit")) {
			retVal.getKeyAliases().add("alias1");
			retVal.getKeyAliases().add("alias2");
			retVal.getExpiryDate().put("alias1", new Date(System.currentTimeMillis() + 100000L));
			retVal.getExpiryDate().put("alias2", new Date(System.currentTimeMillis() + 200000L));
			retVal.getIssuer().put("alias1", "CN=Issuer1");
			retVal.getIssuer().put("alias2", "CN=Issuer2");
			retVal.getSubject().put("alias1", "CN=Subject1");
			retVal.getSubject().put("alias2", "CN=Subject2");
			retVal.getKeyEntry().put("alias1", true);
			retVal.getKeyEntry().put("alias2", false);
			retVal.setPasswordAccepted(true);
		}else {
			retVal.setPasswordAccepted(false);
			retVal.setProblemDescription("Keystore has been tampered with or password is invalid");
		}
		return retVal;
	}

	@Override
	public GServiceVersionUrl resetCircuitBreakerForServiceVersionUrl(long theUrlPid) {
		for (GDomain nextDomain : myDomainList) {
			for (GService nextSvc : nextDomain.getServiceList()) {
				for (BaseGServiceVersion nextVer : nextSvc.getVersionList()) {
					for (GServiceVersionUrl nextUrl : nextVer.getUrlList()) {
						if (nextUrl.getPid() == theUrlPid) {
							nextUrl.setNextCircuitBreakerReset(new Date(System.currentTimeMillis() + 10000));
							return nextUrl;
						}
					}
				}
			}
		}
		throw new IllegalArgumentException("Unknown URL pid " + theUrlPid);
	}

}
