package net.svcret.core.ejb;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;

import net.svcret.admin.shared.enm.AuthorizationOutcomeEnum;
import net.svcret.admin.shared.enm.InvocationStatsIntervalEnum;
import net.svcret.admin.shared.enm.ResponseTypeEnum;
import net.svcret.admin.shared.enm.ThrottlePeriodEnum;
import net.svcret.admin.shared.model.BaseDtoDashboardObject;
import net.svcret.admin.shared.model.BaseDtoServiceVersion;
import net.svcret.admin.shared.model.DtoAuthenticationHostLocalDatabase;
import net.svcret.admin.shared.model.DtoConfig;
import net.svcret.admin.shared.model.DtoDomain;
import net.svcret.admin.shared.model.DtoHttpClientConfig;
import net.svcret.admin.shared.model.DtoLibraryMessage;
import net.svcret.admin.shared.model.DtoMonitorRuleActive;
import net.svcret.admin.shared.model.DtoMonitorRuleActiveCheck;
import net.svcret.admin.shared.model.DtoPropertyCapture;
import net.svcret.admin.shared.model.DtoServiceVersionJsonRpc20;
import net.svcret.admin.shared.model.DtoServiceVersionSoap11;
import net.svcret.admin.shared.model.DtoServiceVersionThrottle;
import net.svcret.admin.shared.model.GMonitorRuleList;
import net.svcret.admin.shared.model.GMonitorRulePassive;
import net.svcret.admin.shared.model.GResource;
import net.svcret.admin.shared.model.GService;
import net.svcret.admin.shared.model.GServiceMethod;
import net.svcret.admin.shared.model.GServiceVersionUrl;
import net.svcret.admin.shared.model.GSoap11ServiceVersionAndResources;
import net.svcret.admin.shared.model.GUser;
import net.svcret.admin.shared.model.GUserDomainPermission;
import net.svcret.admin.shared.model.GUserServicePermission;
import net.svcret.admin.shared.model.GUserServiceVersionPermission;
import net.svcret.admin.shared.model.GWsSecServerSecurity;
import net.svcret.admin.shared.model.GWsSecUsernameTokenClientSecurity;
import net.svcret.admin.shared.model.HierarchyEnum;
import net.svcret.admin.shared.model.ModelUpdateRequest;
import net.svcret.admin.shared.model.ModelUpdateResponse;
import net.svcret.admin.shared.model.ServiceProtocolEnum;
import net.svcret.admin.shared.model.UserGlobalPermissionEnum;
import net.svcret.core.admin.AdminServiceBean;
import net.svcret.core.api.SrBeanIncomingRequest;
import net.svcret.core.api.SrBeanIncomingResponse;
import net.svcret.core.api.SrBeanProcessedRequest;
import net.svcret.core.api.SrBeanProcessedResponse;
import net.svcret.core.dao.DaoBean;
import net.svcret.core.ejb.ConfigServiceBean;
import net.svcret.core.ejb.ServiceRegistryBean;
import net.svcret.core.ejb.monitor.MonitorServiceBean;
import net.svcret.core.ejb.nodecomm.IBroadcastSender;
import net.svcret.core.invoker.soap.IServiceInvokerSoap11;
import net.svcret.core.log.TransactionLoggerBean;
import net.svcret.core.model.entity.BasePersAuthenticationHost;
import net.svcret.core.model.entity.BasePersServiceVersion;
import net.svcret.core.model.entity.PersAuthenticationHostLocalDatabase;
import net.svcret.core.model.entity.PersDomain;
import net.svcret.core.model.entity.PersHttpClientConfig;
import net.svcret.core.model.entity.PersLibraryMessage;
import net.svcret.core.model.entity.PersMethod;
import net.svcret.core.model.entity.PersService;
import net.svcret.core.model.entity.PersServiceVersionResource;
import net.svcret.core.model.entity.PersServiceVersionStatus;
import net.svcret.core.model.entity.PersServiceVersionUrl;
import net.svcret.core.model.entity.PersUser;
import net.svcret.core.model.entity.soap.PersServiceVersionSoap11;
import net.svcret.core.security.SecurityServiceBean;
import net.svcret.core.status.RuntimeStatusBean;
import net.svcret.core.status.RuntimeStatusQueryBean;

import org.apache.commons.lang3.time.DateUtils;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AdminServiceBeanIntegrationTest extends BaseJpaTest {

	private static final String HTTP_ZZZZZZ = "http://127.0.0.6";

	private static final String HTTP_URL2 = "http://127.0.0.4";

	private static final String HTTP_URL1 = "http://127.0.0.5";

	private static final String HTTP_SVCURL = "http://127.0.0.3";

	private static final String HTTP_BAR = "http://127.0.0.1";

	private static final String HTTP_FOO = "http://127.0.0.2";

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(AdminServiceBeanIntegrationTest.class);

	private IBroadcastSender myBroadcastSender;
	private ConfigServiceBean myConfigSvc;
	private DaoBean myDao;
	private Date myEverythingInvocationTime;
	private MonitorServiceBean myMonitorSvc;
	private SecurityServiceBean mySecSvc;
	private IServiceInvokerSoap11 mySoapInvoker;
	private RuntimeStatusQueryBean myStatsQSvc;
	private RuntimeStatusBean myStatsSvc;
	private AdminServiceBean mySvc;
	private ServiceRegistryBean mySvcReg;
	private TransactionLoggerBean myTransactionLogSvc;

	@After
	public void after2() {

		// newEntityManager();
		//
		// Query q = myEntityManager.createQuery("DELETE FROM PersDomain p");
		// q.executeUpdate();
		//
		// newEntityManager();
	}

	@Before
	public void before2() {
		myDao = new DaoBean();
		myBroadcastSender = mock(IBroadcastSender.class);

		myConfigSvc = new ConfigServiceBean();
		myConfigSvc.setDao(myDao);
		myConfigSvc.setBroadcastSender(myBroadcastSender);

		myStatsSvc = new RuntimeStatusBean();
		myStatsSvc.setDao(myDao);
		myStatsSvc.setConfigSvc(myConfigSvc);

		myStatsQSvc = new RuntimeStatusQueryBean();
		myStatsQSvc.setConfigSvcForUnitTest(myConfigSvc);
		myStatsQSvc.setStatusSvcForUnitTest(myStatsSvc);
		myStatsQSvc.setDaoForUnitTests(myDao);

		mySoapInvoker = mock(IServiceInvokerSoap11.class);

		mySecSvc = new SecurityServiceBean();
		mySecSvc.setPersSvc(myDao);
		mySecSvc.setBroadcastSender(myBroadcastSender);

		mySvcReg = new ServiceRegistryBean();
		mySvcReg.setBroadcastSender(myBroadcastSender);
		mySvcReg.setDao(myDao);

		myMonitorSvc = new MonitorServiceBean();
		myMonitorSvc.setDao(myDao);
		myMonitorSvc.setBroadcastSender(myBroadcastSender);

		mySvc = new AdminServiceBean();
		mySvc.setPersSvc(myDao);
		mySvc.setConfigSvc(myConfigSvc);
		mySvc.setRuntimeStatusSvc(myStatsSvc);
		mySvc.setRuntimeStatusQuerySvcForUnitTests(myStatsQSvc);
		mySvc.setServiceRegistry(mySvcReg);
		mySvc.setInvokerSoap11(mySoapInvoker);
		mySvc.setSecuritySvc(mySecSvc);
		mySvc.setMonitorSvc(myMonitorSvc);

		myTransactionLogSvc = new TransactionLoggerBean();
		myTransactionLogSvc.setDao(myDao);
		myTransactionLogSvc.setConfigServiceForUnitTests(myConfigSvc);

	}

	private DtoDomain createEverything() throws Exception {
		newEntityManager();

		mySecSvc.loadUserCatalogIfNeeded();

		BasePersAuthenticationHost authHost = myDao.getAuthenticationHost(BasePersAuthenticationHost.MODULE_ID_ADMIN_AUTH);
		authHost.setKeepNumRecentTransactionsFail(100);
		authHost.setKeepNumRecentTransactionsFault(100);
		authHost.setKeepNumRecentTransactionsSecurityFail(100);
		authHost.setKeepNumRecentTransactionsSuccess(100);
		myDao.saveAuthenticationHost(authHost);

		newEntityManager();

		DtoDomain d1 = mySvc.unitTestMethod_addDomain("asv_did", "asv_did");
		GService d1s1 = mySvc.addService(d1.getPid(), createTestService());
		PersHttpClientConfig hcc = myDao.getOrCreateHttpClientConfig("httpclient");

		newEntityManager();

		DtoServiceVersionSoap11 d1s1v1 = new DtoServiceVersionSoap11();
		d1s1v1.setActive(true);
		d1s1v1.setId("ASV_SV1");
		d1s1v1.setName("ASV_SV1_Name");
		d1s1v1.setWsdlLocation(HTTP_FOO);
		d1s1v1.setHttpClientConfigPid(hcc.getPid());

		d1s1v1.setKeepNumRecentTransactionsFail(100);
		d1s1v1.setKeepNumRecentTransactionsFault(100);
		d1s1v1.setKeepNumRecentTransactionsSecurityFail(100);
		d1s1v1.setKeepNumRecentTransactionsSuccess(100);

		GServiceMethod d1s1v1m1 = new GServiceMethod();
		d1s1v1m1.setName("d1s1v1m1");
		d1s1v1.getMethodList().add(d1s1v1m1);

		List<GResource> resources = new ArrayList<GResource>();
		resources.add(new GResource(HTTP_FOO, "text/xml", "contents1"));
		resources.add(new GResource(HTTP_BAR, "text/xml", "contents2"));

		DtoServiceVersionSoap11 ver = mySvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v1, resources);

		ver.getUrlList().add(new GServiceVersionUrl("url1", HTTP_FOO));
		ver = mySvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), ver, resources);

		newEntityManager();
		BasePersServiceVersion persVer = myDao.getServiceVersionByPid(ver.getPid());

		// Create a user with access
		GUser user = new GUser();
		user.setAuthHostPid(myDao.getAllAuthenticationHosts().iterator().next().getPid());
		user.setUsername("username");
		user.setDomainPermissions(new ArrayList<GUserDomainPermission>());
		user.getDomainPermissions().add(new GUserDomainPermission());
		user.getDomainPermissions().get(0).setDomainPid(persVer.getService().getDomain().getPid());
		user.getDomainPermissions().get(0).setServicePermissions(new ArrayList<GUserServicePermission>());
		user.getDomainPermissions().get(0).getServicePermissions().add(new GUserServicePermission());
		user.getDomainPermissions().get(0).getServicePermissions().get(0).setServicePid(persVer.getService().getPid());
		user.getDomainPermissions().get(0).getServicePermissions().get(0).setServiceVersionPermissions(new ArrayList<GUserServiceVersionPermission>());
		user.getDomainPermissions().get(0).getServicePermissions().get(0).getServiceVersionPermissions().add(new GUserServiceVersionPermission());
		user.getDomainPermissions().get(0).getServicePermissions().get(0).getServiceVersionPermissions().get(0).setServiceVersionPid(persVer.getPid());
		user.getDomainPermissions().get(0).getServicePermissions().get(0).getServiceVersionPermissions().get(0).getOrCreateServiceVersionMethodPermission(persVer.getMethods().get(0).getPid());

		user = mySvc.saveUser(user);

		newEntityManager();

		// Add stats
		persVer = myDao.getServiceVersionByPid(ver.getPid());
		PersServiceVersionStatus status = persVer.getStatus();
		assertNotNull(status);

		PersMethod m1 = persVer.getMethods().iterator().next();

		newEntityManager();

		// Record invocation
		SrBeanIncomingResponse httpResponse = new SrBeanIncomingResponse();
		httpResponse.setBody("1234");
		httpResponse.setResponseTime(123);
		SrBeanProcessedResponse bean = new SrBeanProcessedResponse();
		bean.setResponseType(ResponseTypeEnum.SUCCESS);

		myEverythingInvocationTime = new Date(System.currentTimeMillis() - (60 * 1000L));
		myStatsSvc.recordInvocationMethod(myEverythingInvocationTime, 100, SrBeanProcessedRequest.forUnitTest(m1), null, httpResponse, bean);

		newEntityManager();

		myStatsSvc.flushStatus();

		newEntityManager();

		persVer = myDao.getServiceVersionByPid(ver.getPid());

		SrBeanIncomingRequest request = new SrBeanIncomingRequest();
		request.setRequestHostIp("127.0.0.1");
		request.setRequestHeaders(new HashMap<String, List<String>>());
		request.setRequestTime(new Date());
		String requestBody = "request body";
		request.setInputReader(new StringReader(requestBody));
		
		SrBeanProcessedResponse invocationResponse = new SrBeanProcessedResponse();
		invocationResponse.setResponseHeaders(new HashMap<String, List<String>>());
		invocationResponse.setResponseType(ResponseTypeEnum.SUCCESS);
		httpResponse.setSuccessfulUrl(persVer.getUrls().get(0));
		AuthorizationOutcomeEnum authorizationOutcome = AuthorizationOutcomeEnum.AUTHORIZED;
		PersUser persUser = myDao.getUser(user.getPidOrNull());
		SrBeanProcessedRequest processedRequest=new SrBeanProcessedRequest();
		processedRequest.setObscuredRequestBody(requestBody);
		processedRequest.setServiceVersion(persVer);
		processedRequest.setResultMethod(m1, requestBody, "text/plain");
		myTransactionLogSvc.logTransaction(request, persUser, invocationResponse, httpResponse, authorizationOutcome, processedRequest);

		newEntityManager();

		myTransactionLogSvc.flush();

		newEntityManager();

		mySvcReg.reloadRegistryFromDatabase();

		newEntityManager();

		return mySvc.getDomainByPid(d1.getPid());
	}

	@Override
	protected void newEntityManager() {
		super.newEntityManager();

		myDao.setEntityManager(myEntityManager);
	}

	@Test
	public void testAddAndSaveService() throws Exception {
		newEntityManager();

		DtoDomain domain = mySvc.unitTestMethod_addDomain("domain_id3", "domain_name");
		newEntityManager();

		GService service = mySvc.addService(domain.getPid(), createTestService());

		newEntityManager();

		assertEquals(createTestService().getId(), service.getId());
		assertEquals(createTestService().getName(), service.getName());

		assertFalse(service.isStatsInitialized());

		newEntityManager();

		service.setName("name2");
		mySvc.saveService(service);

	}

	@Test
	public void testAddDomain() throws Exception {
		newEntityManager();

		DtoDomain domain = mySvc.unitTestMethod_addDomain("domain_id", "domain_name");
		newEntityManager();

		assertEquals("domain_id", domain.getId());
		assertEquals("domain_name", domain.getName());
		assertFalse(domain.isStatsInitialized());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAddDomainDuplicate() throws Exception {
		newEntityManager();

		mySvc.unitTestMethod_addDomain("domain_id2", "domain_name");
		newEntityManager();

		mySvc.unitTestMethod_addDomain("domain_id2", "domain_name");
		newEntityManager();

	}

	@Test
	public void testAddMonitorRuleActive() throws Exception {

		newEntityManager();

		DtoDomain d1 = mySvc.unitTestMethod_addDomain("asv_did", "asv_did");
		GService d1s1 = mySvc.addService(d1.getPid(), createTestService());
		PersHttpClientConfig hcc = myDao.getOrCreateHttpClientConfig("httpServer");
		myDao.getOrCreateAuthenticationHostLocalDatabase("AUTHHOST");

		newEntityManager();

		DtoServiceVersionSoap11 d1s1v1 = new DtoServiceVersionSoap11();
		d1s1v1.setActive(true);
		d1s1v1.setId("ASV_SV1");
		d1s1v1.setName("ASV_SV1_Name");
		d1s1v1.setWsdlLocation(HTTP_FOO);
		d1s1v1.setHttpClientConfigPid(hcc.getPid());
		d1s1v1 = mySvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v1, new ArrayList<GResource>());

		DtoServiceVersionSoap11 d1s1v2 = new DtoServiceVersionSoap11();
		d1s1v2.setActive(true);
		d1s1v2.setId("ASV_SV2");
		d1s1v2.setName("ASV_SV2_Name");
		d1s1v2.setWsdlLocation(HTTP_FOO);
		d1s1v2.setHttpClientConfigPid(hcc.getPid());
		d1s1v2 = mySvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v2, new ArrayList<GResource>());

		newEntityManager();

		DtoLibraryMessage msg1 = new DtoLibraryMessage();
		msg1.setAppliesToServiceVersionPids(d1s1v1.getPid());
		msg1.setContentType("text/xml");
		msg1.setDescription("desc1");
		msg1.setMessage("message text1");
		mySvc.saveLibraryMessage(msg1);

		DtoLibraryMessage msg2 = new DtoLibraryMessage();
		msg2.setAppliesToServiceVersionPids(d1s1v1.getPid());
		msg2.setContentType("text/xml");
		msg2.setDescription("desc2");
		msg2.setMessage("message text2");
		mySvc.saveLibraryMessage(msg2);

		newEntityManager();

		List<DtoLibraryMessage> msgs = new ArrayList<DtoLibraryMessage>(mySvc.getLibraryMessages(HierarchyEnum.VERSION, d1s1v1.getPid(), false));
		assertEquals(2, msgs.size());

		DtoMonitorRuleActive rule = new DtoMonitorRuleActive();
		rule.setName("ruleName");
		rule.setActive(true);
		rule.getCheckList().add(new DtoMonitorRuleActiveCheck());
		rule.getCheckList().get(0).setCheckFrequencyNum(5);
		rule.getCheckList().get(0).setCheckFrequencyUnit(ThrottlePeriodEnum.MINUTE);
		rule.getCheckList().get(0).setMessagePid(msgs.get(0).getPid());
		rule.getCheckList().get(0).setServiceVersionPid(d1s1v1.getPid());
		rule.getCheckList().get(0).setExpectResponseType(ResponseTypeEnum.SUCCESS);

		mySvc.saveMonitorRule(rule);

		newEntityManager();

		/*
		 * Add a second active check
		 */

		GMonitorRuleList ruleList = mySvc.loadMonitorRuleList();
		assertEquals(1, ruleList.size());

		rule = (DtoMonitorRuleActive) ruleList.get(0);
		assertEquals(1, rule.getCheckList().size());

		rule.getCheckList().add(new DtoMonitorRuleActiveCheck());
		rule.getCheckList().get(1).setCheckFrequencyNum(6);
		rule.getCheckList().get(1).setCheckFrequencyUnit(ThrottlePeriodEnum.MINUTE);
		rule.getCheckList().get(1).setMessagePid(msgs.get(1).getPid());
		rule.getCheckList().get(1).setServiceVersionPid(d1s1v2.getPid());
		rule.getCheckList().get(1).setExpectResponseType(ResponseTypeEnum.SUCCESS);

		mySvc.saveMonitorRule(rule);

		newEntityManager();

		ruleList = mySvc.loadMonitorRuleList();
		assertEquals(1, ruleList.size());

		rule = (DtoMonitorRuleActive) ruleList.get(0);
		assertEquals(2, rule.getCheckList().size());
		assertEquals(5, rule.getCheckList().get(0).getCheckFrequencyNum());
		assertEquals(6, rule.getCheckList().get(1).getCheckFrequencyNum());

		mySvcReg.reloadRegistryFromDatabase();

		newEntityManager();

		Collection<BasePersServiceVersion> svcVerList = mySvcReg.getAllDomains().iterator().next().getServices().iterator().next().getVersions();
		assertEquals(2, svcVerList.size());
		Iterator<BasePersServiceVersion> iterator = svcVerList.iterator();
		BasePersServiceVersion v1 = iterator.next();
		BasePersServiceVersion v2 = iterator.next();
		assertEquals(1, v1.getActiveChecks().size());
		assertEquals(1, v2.getActiveChecks().size());

	}

	@Test
	public void testAddMonitorRulePassive() throws Exception {

		newEntityManager();

		DtoDomain d1 = mySvc.unitTestMethod_addDomain("asv_did", "asv_did");
		GService d1s1 = mySvc.addService(d1.getPid(), createTestService());
		PersHttpClientConfig hcc = myDao.getOrCreateHttpClientConfig("httpclient");
		myDao.getOrCreateAuthenticationHostLocalDatabase("AUTHHOST");

		newEntityManager();

		DtoServiceVersionSoap11 d1s1v1 = new DtoServiceVersionSoap11();
		d1s1v1.setActive(true);
		d1s1v1.setId("ASV_SV1");
		d1s1v1.setName("ASV_SV1_Name");
		d1s1v1.setWsdlLocation(HTTP_FOO);
		d1s1v1.setHttpClientConfigPid(hcc.getPid());
		d1s1v1 = mySvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v1, new ArrayList<GResource>());

		newEntityManager();

		assertEquals(0, mySvc.loadMonitorRuleList().size());

		newEntityManager();

		GMonitorRulePassive rule = new GMonitorRulePassive();
		rule.setPassiveFireIfAllBackingUrlsAreUnavailable(true);
		rule.setPassiveFireForBackingServiceLatencyIsAboveMillis(100);
		rule.applyTo(d1, true);
		rule.applyTo(d1, d1s1, true);
		rule.getNotifyEmailContacts().add("foo@foo.com");
		rule.getNotifyEmailContacts().add("bar@bar.com");

		mySvc.saveMonitorRule(rule);

		newEntityManager();

		GMonitorRuleList rules = mySvc.loadMonitorRuleList();
		assertEquals(1, rules.size());

		rule = (GMonitorRulePassive) rules.get(0);
		assertEquals(true, rule.isPassiveFireIfAllBackingUrlsAreUnavailable());
		assertEquals(false, rule.isPassiveFireIfSingleBackingUrlIsUnavailable());
		assertTrue(rule.getNotifyEmailContacts().contains("foo@foo.com"));
		assertTrue(rule.getNotifyEmailContacts().contains("bar@bar.com"));
		assertTrue(rule.appliesTo(d1));
		assertTrue(rule.appliesTo(d1s1));
		assertFalse(rule.appliesTo(d1s1v1));

		rule.applyTo(d1, d1s1, false);
		rule.applyTo(d1, d1s1, d1s1v1, true);
		rule.getNotifyEmailContacts().remove("foo@foo.com");
		rule.getNotifyEmailContacts().add("baz@baz.com");

		mySvc.saveMonitorRule(rule);

		newEntityManager();

		rules = mySvc.loadMonitorRuleList();
		assertEquals(1, rules.size());

		rule = (GMonitorRulePassive) rules.get(0);
		assertEquals(true, rule.isPassiveFireIfAllBackingUrlsAreUnavailable());
		assertEquals(false, rule.isPassiveFireIfSingleBackingUrlIsUnavailable());
		assertFalse(rule.getNotifyEmailContacts().contains("foo@foo.com"));
		assertTrue(rule.getNotifyEmailContacts().contains("bar@bar.com"));
		assertTrue(rule.getNotifyEmailContacts().contains("baz@baz.com"));
		assertTrue(rule.appliesTo(d1));
		assertFalse(rule.appliesTo(d1s1));
		assertTrue(rule.appliesTo(d1s1v1));

	}

	@Test
	public void testAddServiceVersion() throws Exception {
		newEntityManager();

		DtoDomain d1 = mySvc.unitTestMethod_addDomain("asv_did", "asv_did");
		GService d1s1 = mySvc.addService(d1.getPid(), createTestService());
		PersHttpClientConfig hcc = myDao.getOrCreateHttpClientConfig("httpclient");

		newEntityManager();

		DtoServiceVersionSoap11 d1s1v1 = new DtoServiceVersionSoap11();
		d1s1v1.setActive(true);
		d1s1v1.setId("ASV_SV1");
		d1s1v1.setName("ASV_SV1_Name");
		d1s1v1.setWsdlLocation(HTTP_FOO);
		d1s1v1.setHttpClientConfigPid(hcc.getPid());

		List<GResource> resources = new ArrayList<GResource>();
		resources.add(new GResource(HTTP_FOO, "text/xml", "contents1"));
		resources.add(new GResource(HTTP_BAR, "text/xml", "contents2"));

		d1s1v1.getUrlList().add(new GServiceVersionUrl("url1", HTTP_URL1));
		d1s1v1.getUrlList().add(new GServiceVersionUrl("url2", HTTP_URL2));

		mySvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v1, resources);

		newEntityManager();

		PersDomain pDomain = myDao.getDomainByPid(d1.getPid());
		Collection<BasePersServiceVersion> versions = pDomain.getServices().iterator().next().getVersions();

		assertEquals(1, versions.size());

		PersServiceVersionSoap11 pVersion = (PersServiceVersionSoap11) versions.iterator().next();
		assertEquals("ASV_SV1", pVersion.getVersionId());
		assertEquals(HTTP_FOO, pVersion.getWsdlUrl());
		assertEquals(2, pVersion.getUriToResource().size());
		assertEquals("contents1", pVersion.getUriToResource().get(HTTP_FOO).getResourceText());
		assertEquals("contents2", pVersion.getUriToResource().get(HTTP_BAR).getResourceText());

		assertEquals(2, pVersion.getUrls().size());
		assertEquals("url1", pVersion.getUrls().get(0).getUrlId());
		assertEquals("url2", pVersion.getUrls().get(1).getUrlId());

	}

	@Test(expected = IllegalArgumentException.class)
	public void testAddServiceWithDuplicates() throws Exception {
		newEntityManager();

		DtoDomain domain = mySvc.unitTestMethod_addDomain("domain_id4", "domain_name");
		newEntityManager();

		mySvc.addService(domain.getPid(), createTestService());

		newEntityManager();

		mySvc.addService(domain.getPid(), createTestService());
	}

	@Test
	public void testDeleteDomain() throws Exception {

		createEverything();

		long pid = mySvc.getDomainPid("asv_did");
		assertTrue(pid > 0);

		mySvc.deleteDomain(pid);

		newEntityManager();

		DtoDomain domain = mySvc.getDomainByPid(pid);
		assertNull(domain);

	}

	@Test
	public void testDeleteServiceVersion() throws Exception {
		newEntityManager();

		DtoDomain d1 = mySvc.unitTestMethod_addDomain("asv_did", "asv_did");
		GService d1s1 = mySvc.addService(d1.getPid(), createTestService());
		PersHttpClientConfig hcc = myDao.getOrCreateHttpClientConfig("httpclient");

		newEntityManager();

		DtoServiceVersionSoap11 d1s1v1 = new DtoServiceVersionSoap11();
		d1s1v1.setActive(true);
		d1s1v1.setId("ASV_SV1");
		d1s1v1.setName("ASV_SV1_Name");
		d1s1v1.setWsdlLocation(HTTP_FOO);
		d1s1v1.setHttpClientConfigPid(hcc.getPid());

		List<GResource> resources = new ArrayList<GResource>();
		resources.add(new GResource(HTTP_FOO, "text/xml", "contents1"));
		resources.add(new GResource(HTTP_BAR, "text/xml", "contents2"));

		d1s1v1.getUrlList().add(new GServiceVersionUrl("url1", HTTP_URL1));
		d1s1v1.getUrlList().add(new GServiceVersionUrl("url2", HTTP_URL2));

		d1s1v1 = mySvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v1, resources);

		newEntityManager();

		PersDomain pDomain = myDao.getDomainByPid(d1.getPid());
		Collection<BasePersServiceVersion> versions = pDomain.getServices().iterator().next().getVersions();

		assertEquals(1, versions.size());

		newEntityManager();

		long pid = d1s1v1.getPid();
		mySvc.deleteServiceVersion(pid);

		newEntityManager();

		BasePersServiceVersion ver = myDao.getServiceVersionByPid(pid);
		assertNull(ver);

		pDomain = myDao.getDomainByPid(d1.getPid());
		versions = pDomain.getServices().iterator().next().getVersions();

		if (versions.size() > 0) {
			ver = versions.iterator().next();
			fail(ver.toString());
		}

	}

	@Test
	public void testDeleteUser() throws Exception {

		createEverything();

		BasePersAuthenticationHost authHost = myDao.getAuthenticationHost(BasePersAuthenticationHost.MODULE_ID_ADMIN_AUTH);
		long pid = myDao.getOrCreateUser(authHost, "username").getPid();
		assertTrue(pid > 0);

		assertEquals(2, myDao.getAllUsersAndInitializeThem().size());

		newEntityManager();

		mySvc.deleteUser(pid);

		newEntityManager();

		assertEquals(1, myDao.getAllUsersAndInitializeThem().size());

	}

	@Test
	public void testLibrary() throws Exception {

		newEntityManager();

		PersDomain domain = myDao.getOrCreateDomainWithId("DOMAIN_ID");
		PersService service = myDao.getOrCreateServiceWithId(domain, "SERVICE_ID");
		PersServiceVersionSoap11 version0 = (PersServiceVersionSoap11) myDao.getOrCreateServiceVersionWithId(service, "VersionId0", ServiceProtocolEnum.SOAP11);
		PersServiceVersionSoap11 version1 = (PersServiceVersionSoap11) myDao.getOrCreateServiceVersionWithId(service, "VersionId1", ServiceProtocolEnum.SOAP11);

		newEntityManager();

		DtoLibraryMessage m0 = new DtoLibraryMessage();
		m0.setAppliesToServiceVersionPids(version0.getPid());
		m0.setContentType("ct0");
		m0.setDescription("desc0");
		m0.setMessage("m0");
		mySvc.saveLibraryMessage(m0);

		DtoLibraryMessage m1 = new DtoLibraryMessage();
		m1.setAppliesToServiceVersionPids(version1.getPid());
		m1.setContentType("ct1");
		m1.setDescription("desc1");
		m1.setMessage("m1");
		mySvc.saveLibraryMessage(m1);

		newEntityManager();

		Collection<DtoLibraryMessage> msgs = mySvc.getLibraryMessages(HierarchyEnum.VERSION, version0.getPid(), true);
		assertEquals(1, msgs.size());

		DtoLibraryMessage message = msgs.iterator().next();
		assertEquals("ct0", message.getContentType());
		assertEquals("desc0", message.getDescription());
		assertEquals("m0", message.getMessage());

		msgs = mySvc.getLibraryMessages(HierarchyEnum.VERSION, version1.getPid(), true);
		assertEquals(1, msgs.size());

		message.setAppliesToServiceVersionPids(version0.getPid(), version1.getPid());

		mySvc.saveLibraryMessage(message);

		newEntityManager();

		msgs = mySvc.getLibraryMessages(HierarchyEnum.VERSION, version1.getPid(), true);
		assertEquals(2, msgs.size());
		msgs = mySvc.getLibraryMessages(HierarchyEnum.SERVICE, service.getPid(), true);
		assertEquals(2, msgs.size());

		message.setAppliesToServiceVersionPids(version1.getPid());

		mySvc.saveLibraryMessage(message);

		PersLibraryMessage pm = myDao.getLibraryMessageByPid(message.getPid());
		assertEquals(1, pm.getAppliesTo().size());

		newEntityManager();

		pm = myDao.getLibraryMessageByPid(message.getPid());
		assertEquals(1, pm.getAppliesTo().size());

		msgs = mySvc.getLibraryMessages(HierarchyEnum.VERSION, version0.getPid(), true);
		assertEquals(0, msgs.size());
		msgs = mySvc.getLibraryMessages(HierarchyEnum.VERSION, version1.getPid(), true);
		assertEquals(2, msgs.size());

		msgs = mySvc.getLibraryMessages(HierarchyEnum.SERVICE, service.getPid(), true);
		assertEquals(2, msgs.size());

		msgs = mySvc.getLibraryMessages(HierarchyEnum.DOMAIN, domain.getPid(), true);
		assertEquals(2, msgs.size());

		msgs = mySvc.loadLibraryMessages();
		assertEquals(2, msgs.size());

	}

	@Test
	public void testLoad60MinuteStats() throws Exception {
		DtoDomain domain = createEverything();
		GService svc = domain.getServiceList().get(0);
		BaseDtoServiceVersion svcVer = svc.getVersionList().get(0);

		newEntityManager();

		// Domain
		ModelUpdateRequest request = new ModelUpdateRequest();
		request.addDomainToLoadStats(domain.getPid());
		ModelUpdateResponse modelResp = mySvc.loadModelUpdate(request);
		domain = modelResp.getDomainList().get(0);
		testLoad60MinuteStats_CheckStats(domain);

		// Service
		request = new ModelUpdateRequest();
		request.addServiceToLoadStats(svc.getPid());
		modelResp = mySvc.loadModelUpdate(request);
		domain = modelResp.getDomainList().get(0);
		svc = domain.getServiceList().get(0);
		testLoad60MinuteStats_CheckStats(svc);

		// Service Version
		request = new ModelUpdateRequest();
		request.addVersionToLoadStats(svcVer.getPid());
		modelResp = mySvc.loadModelUpdate(request);
		domain = modelResp.getDomainList().get(0);
		svc = domain.getServiceList().get(0);
		svcVer = svc.getVersionList().get(0);
		testLoad60MinuteStats_CheckStats(svcVer);

		// Try again.. should we clear cache?

		request = new ModelUpdateRequest();
		request.addDomainToLoadStats(domain.getPid());
		request.addServiceToLoadStats(svc.getPid());
		request.addVersionToLoadStats(svcVer.getPid());
		modelResp = mySvc.loadModelUpdate(request);

		domain = modelResp.getDomainList().get(0);
		svc = domain.getServiceList().get(0);
		svcVer = svc.getVersionList().get(0);

		testLoad60MinuteStats_CheckStats(domain);
		testLoad60MinuteStats_CheckStats(svcVer);
		testLoad60MinuteStats_CheckStats(svc);

	}

	private void testLoad60MinuteStats_CheckStats(BaseDtoDashboardObject obj) {
		int[] trans = obj.getTransactions60mins();
		Date firstDate = obj.getStatistics60MinuteFirstDate();
		Date expected = InvocationStatsIntervalEnum.MINUTE.truncate(new Date(System.currentTimeMillis() - (59 * DateUtils.MILLIS_PER_MINUTE)));
		assertEquals(expected, firstDate);
		assertEquals(60, trans.length);
		assertEquals(0, trans[59]);
		assertEquals(1, trans[58]);
		assertEquals(0, trans[57]);
	}

	@Test
	public void testLoadAndSaveSvcVerClientSecurity() throws Exception {

		newEntityManager();

		DtoDomain d1 = mySvc.unitTestMethod_addDomain("asv_did", "asv_did");
		GService d1s1 = mySvc.addService(d1.getPid(), createTestService());
		PersHttpClientConfig hcc = myDao.getOrCreateHttpClientConfig("httpclient");
		myDao.getOrCreateAuthenticationHostLocalDatabase("AUTHHOST");

		newEntityManager();

		DtoServiceVersionSoap11 d1s1v1 = new DtoServiceVersionSoap11();
		d1s1v1.setActive(true);
		d1s1v1.setId("ASV_SV1");
		d1s1v1.setName("ASV_SV1_Name");
		d1s1v1.setWsdlLocation(HTTP_FOO);
		d1s1v1.setHttpClientConfigPid(hcc.getPid());
		d1s1v1 = mySvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v1, new ArrayList<GResource>());

		newEntityManager();

		// Add one

		d1s1v1 = (DtoServiceVersionSoap11) mySvc.loadServiceVersion(d1s1v1.getPid()).getServiceVersion();
		GWsSecUsernameTokenClientSecurity cli = new GWsSecUsernameTokenClientSecurity();
		cli.setUsername("un0");
		cli.setPassword("pw0");
		d1s1v1.getClientSecurityList().add(cli);

		mySvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v1, new ArrayList<GResource>());

		newEntityManager();

		d1s1v1 = (DtoServiceVersionSoap11) mySvc.loadServiceVersion(d1s1v1.getPid()).getServiceVersion();
		mySvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v1, new ArrayList<GResource>());

		newEntityManager();

		// Add a second

		d1s1v1 = (DtoServiceVersionSoap11) mySvc.loadServiceVersion(d1s1v1.getPid()).getServiceVersion();
		cli = new GWsSecUsernameTokenClientSecurity();
		cli.setUsername("un1");
		cli.setPassword("pw1");
		d1s1v1.getClientSecurityList().add(cli);

		mySvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v1, new ArrayList<GResource>());

		newEntityManager();

		d1s1v1 = (DtoServiceVersionSoap11) mySvc.loadServiceVersion(d1s1v1.getPid()).getServiceVersion();
		assertEquals(2, d1s1v1.getClientSecurityList().size());
		assertEquals("un0", ((GWsSecUsernameTokenClientSecurity) d1s1v1.getClientSecurityList().get(0)).getUsername());
		assertEquals("pw0", ((GWsSecUsernameTokenClientSecurity) d1s1v1.getClientSecurityList().get(0)).getPassword());
		assertEquals("un1", ((GWsSecUsernameTokenClientSecurity) d1s1v1.getClientSecurityList().get(1)).getUsername());
		assertEquals("pw1", ((GWsSecUsernameTokenClientSecurity) d1s1v1.getClientSecurityList().get(1)).getPassword());

		// Remove one

		d1s1v1.getClientSecurityList().remove(d1s1v1.getClientSecurityList().get(0));
		mySvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v1, new ArrayList<GResource>());

		newEntityManager();

		d1s1v1 = (DtoServiceVersionSoap11) mySvc.loadServiceVersion(d1s1v1.getPid()).getServiceVersion();
		assertEquals(1, d1s1v1.getClientSecurityList().size());
		assertEquals("un1", ((GWsSecUsernameTokenClientSecurity) d1s1v1.getClientSecurityList().get(0)).getUsername());
		assertEquals("pw1", ((GWsSecUsernameTokenClientSecurity) d1s1v1.getClientSecurityList().get(0)).getPassword());

	}

	@Test
	public void testLoadAndSaveSvcVerPropertyCaptures() throws Exception {

		newEntityManager();

		DtoDomain d1 = mySvc.unitTestMethod_addDomain("asv_did", "asv_did");
		GService d1s1 = mySvc.addService(d1.getPid(), createTestService());
		PersHttpClientConfig hcc = myDao.getOrCreateHttpClientConfig("httpclient");
		myDao.getOrCreateAuthenticationHostLocalDatabase("AUTHHOST");

		newEntityManager();

		DtoServiceVersionSoap11 d1s1v1 = new DtoServiceVersionSoap11();
		d1s1v1.setActive(true);
		d1s1v1.setId("ASV_SV1");
		d1s1v1.setName("ASV_SV1_Name");
		d1s1v1.setWsdlLocation(HTTP_FOO);
		d1s1v1.setHttpClientConfigPid(hcc.getPid());
		d1s1v1 = mySvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v1, new ArrayList<GResource>());

		newEntityManager();

		// Add one

		d1s1v1 = (DtoServiceVersionSoap11) mySvc.loadServiceVersion(d1s1v1.getPid()).getServiceVersion();
		DtoPropertyCapture cap1 = new DtoPropertyCapture();
		cap1.setPropertyName("cap1");
		cap1.setXpathExpression("//cap1");
		d1s1v1.getPropertyCaptures().add(cap1);

		mySvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v1, new ArrayList<GResource>());

		newEntityManager();

		d1s1v1 = (DtoServiceVersionSoap11) mySvc.loadServiceVersion(d1s1v1.getPid()).getServiceVersion();
		assertEquals(1, d1s1v1.getPropertyCaptures().size());
		assertEquals("cap1", d1s1v1.getPropertyCaptures().iterator().next().getPropertyName());
		mySvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v1, new ArrayList<GResource>());
		d1s1v1 = (DtoServiceVersionSoap11) mySvc.loadServiceVersion(d1s1v1.getPid()).getServiceVersion();
		assertEquals(1, d1s1v1.getPropertyCaptures().size());
		assertEquals("cap1", d1s1v1.getPropertyCaptures().iterator().next().getPropertyName());

		newEntityManager();

		// Add a second

		d1s1v1 = (DtoServiceVersionSoap11) mySvc.loadServiceVersion(d1s1v1.getPid()).getServiceVersion();
		DtoPropertyCapture cap2 = new DtoPropertyCapture();
		cap2.setPropertyName("cap2");
		cap2.setXpathExpression("//cap2");
		d1s1v1.getPropertyCaptures().add(cap2);

		mySvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v1, new ArrayList<GResource>());

		newEntityManager();

		d1s1v1 = (DtoServiceVersionSoap11) mySvc.loadServiceVersion(d1s1v1.getPid()).getServiceVersion();
		assertEquals(2, d1s1v1.getPropertyCaptures().size());
		assertEquals("cap1", d1s1v1.getPropertyCaptures().get(0).getPropertyName());
		assertEquals("cap2", d1s1v1.getPropertyCaptures().get(1).getPropertyName());

		// Remove one

		d1s1v1.getPropertyCaptures().remove(d1s1v1.getPropertyCaptures().get(0));
		mySvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v1, new ArrayList<GResource>());

		newEntityManager();

		d1s1v1 = (DtoServiceVersionSoap11) mySvc.loadServiceVersion(d1s1v1.getPid()).getServiceVersion();
		assertEquals(1, d1s1v1.getPropertyCaptures().size());
		assertEquals("cap2", d1s1v1.getPropertyCaptures().get(0).getPropertyName());

	}

	@Test
	public void testLoadAndSaveSvcVerThrottle() throws Exception {

		newEntityManager();

		DtoDomain d1 = mySvc.unitTestMethod_addDomain("asv_did", "asv_did");
		GService d1s1 = mySvc.addService(d1.getPid(), createTestService());
		PersHttpClientConfig hcc = myDao.getOrCreateHttpClientConfig("httpclient");
		myDao.getOrCreateAuthenticationHostLocalDatabase("AUTHHOST");

		newEntityManager();

		DtoServiceVersionSoap11 d1s1v1 = new DtoServiceVersionSoap11();
		d1s1v1.setActive(true);
		d1s1v1.setId("ASV_SV1");
		d1s1v1.setName("ASV_SV1_Name");
		d1s1v1.setWsdlLocation(HTTP_FOO);
		d1s1v1.setHttpClientConfigPid(hcc.getPid());
		d1s1v1 = mySvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v1, new ArrayList<GResource>());

		newEntityManager();

		// Add one

		d1s1v1 = (DtoServiceVersionSoap11) mySvc.loadServiceVersion(d1s1v1.getPid()).getServiceVersion();
		assertNull(d1s1v1.getThrottle());

		DtoServiceVersionThrottle dtoThrottle = new DtoServiceVersionThrottle();
		dtoThrottle.setApplyPerUser(true);
		dtoThrottle.setThrottleMaxRequests(5);
		dtoThrottle.setThrottlePeriod(ThrottlePeriodEnum.SECOND);
		d1s1v1.setThrottle(dtoThrottle);

		mySvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v1, new ArrayList<GResource>());

		newEntityManager();

		d1s1v1 = (DtoServiceVersionSoap11) mySvc.loadServiceVersion(d1s1v1.getPid()).getServiceVersion();
		assertNotNull(d1s1v1.getThrottle());
		assertEquals(true, d1s1v1.getThrottle().isApplyPerUser());
		assertEquals(5, d1s1v1.getThrottle().getThrottleMaxRequests().intValue());
		assertEquals(ThrottlePeriodEnum.SECOND, d1s1v1.getThrottle().getThrottlePeriod());

		newEntityManager();

		// Modify one

		d1s1v1 = (DtoServiceVersionSoap11) mySvc.loadServiceVersion(d1s1v1.getPid()).getServiceVersion();
		assertNotNull(d1s1v1.getThrottle());

		dtoThrottle = d1s1v1.getThrottle();
		dtoThrottle.setApplyPerUser(false);
		dtoThrottle.setThrottleMaxRequests(6);
		dtoThrottle.setThrottlePeriod(ThrottlePeriodEnum.SECOND);
		d1s1v1.setThrottle(dtoThrottle);

		mySvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v1, new ArrayList<GResource>());

		newEntityManager();

		d1s1v1 = (DtoServiceVersionSoap11) mySvc.loadServiceVersion(d1s1v1.getPid()).getServiceVersion();
		assertNotNull(d1s1v1.getThrottle());
		assertEquals(false, d1s1v1.getThrottle().isApplyPerUser());
		assertEquals(6, d1s1v1.getThrottle().getThrottleMaxRequests().intValue());
		assertEquals(ThrottlePeriodEnum.SECOND, d1s1v1.getThrottle().getThrottlePeriod());

		newEntityManager();

		// Remove it

		d1s1v1 = (DtoServiceVersionSoap11) mySvc.loadServiceVersion(d1s1v1.getPid()).getServiceVersion();
		d1s1v1.setThrottle(null);
		mySvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v1, new ArrayList<GResource>());

		newEntityManager();

		d1s1v1 = (DtoServiceVersionSoap11) mySvc.loadServiceVersion(d1s1v1.getPid()).getServiceVersion();
		assertNull(d1s1v1.getThrottle());

	}

	@Test
	public void testLoadAndSaveSvcVerJsonRpc20() throws Exception {

		newEntityManager();

		DtoDomain d1 = mySvc.unitTestMethod_addDomain("asv_did", "asv_did");
		GService d1s1 = mySvc.addService(d1.getPid(), createTestService());
		PersHttpClientConfig hcc = myDao.getOrCreateHttpClientConfig("httpclient");

		newEntityManager();

		DtoServiceVersionJsonRpc20 d1s1v1 = new DtoServiceVersionJsonRpc20();
		d1s1v1.setActive(true);
		d1s1v1.setId("ASV_SV1");
		d1s1v1.setName("ASV_SV1_Name");
		d1s1v1.setHttpClientConfigPid(hcc.getPid());

		List<GResource> resources = new ArrayList<GResource>();
		resources.add(new GResource(HTTP_FOO, "text/xml", "contents1"));
		resources.add(new GResource(HTTP_BAR, "text/xml", "contents2"));

		d1s1v1.getUrlList().add(new GServiceVersionUrl("url1", HTTP_URL1));
		d1s1v1.getUrlList().add(new GServiceVersionUrl("url2", HTTP_URL2));

		d1s1v1 = mySvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v1, resources);

		newEntityManager();

		GSoap11ServiceVersionAndResources copy = mySvc.loadServiceVersion(d1s1v1.getPid());
		DtoServiceVersionJsonRpc20 svcVer = (DtoServiceVersionJsonRpc20) copy.getServiceVersion();
		assertEquals("ASV_SV1", svcVer.getId());
		assertEquals(2, copy.getResource().size());

		svcVer.setId("2.0");

		newEntityManager();

		mySvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), svcVer, copy.getResource());

		newEntityManager();

		copy = mySvc.loadServiceVersion(d1s1v1.getPid());
		svcVer = (DtoServiceVersionJsonRpc20) copy.getServiceVersion();
		assertEquals("2.0", svcVer.getId());
		assertEquals(2, copy.getResource().size());

	}

	@Test
	public void testLoadAndSaveSvcVerMethods() throws Exception {

		newEntityManager();

		DtoDomain d1 = mySvc.unitTestMethod_addDomain("asv_did", "asv_did");
		GService d1s1 = mySvc.addService(d1.getPid(), createTestService());
		PersHttpClientConfig hcc = myDao.getOrCreateHttpClientConfig("httpclient");

		newEntityManager();

		DtoServiceVersionSoap11 d1s1v1 = new DtoServiceVersionSoap11();
		d1s1v1.setActive(true);
		d1s1v1.setId("ASV_SV1");
		d1s1v1.setName("ASV_SV1_Name");
		d1s1v1.setWsdlLocation(HTTP_FOO);
		d1s1v1.setHttpClientConfigPid(hcc.getPid());
		d1s1v1 = mySvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v1, new ArrayList<GResource>());

		newEntityManager();

		d1s1v1 = (DtoServiceVersionSoap11) mySvc.loadServiceVersion(d1s1v1.getPid()).getServiceVersion();
		GServiceMethod method = new GServiceMethod();
		method.setName("123");
		d1s1v1.getMethodList().add(method);

		mySvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v1, new ArrayList<GResource>());

		newEntityManager();

		d1s1v1 = (DtoServiceVersionSoap11) mySvc.loadServiceVersion(d1s1v1.getPid()).getServiceVersion();
		mySvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v1, new ArrayList<GResource>());

		newEntityManager();

		mySvcReg.reloadRegistryFromDatabase();

		newEntityManager();

		ModelUpdateResponse response = mySvc.loadModelUpdate(new ModelUpdateRequest());
		assertEquals(1, response.getDomainList().size());
		assertEquals(1, response.getDomainList().get(0).getServiceList().size());
		assertEquals(1, response.getDomainList().get(0).getServiceList().get(0).getVersionList().size());
		assertEquals(1, response.getDomainList().get(0).getServiceList().get(0).getVersionList().get(0).getMethodList().size());

		// Make sure we don't replace identical methods

		GServiceMethod oldMethod = d1s1v1.getMethodList().remove(0);

		d1s1v1.getMethodList().add(new GServiceMethod());
		d1s1v1.getMethodList().get(0).setId(oldMethod.getId());
		d1s1v1.getMethodList().get(0).setName(oldMethod.getName());
		d1s1v1.getMethodList().get(0).setRootElements(oldMethod.getRootElements());
		mySvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v1, new ArrayList<GResource>());

		newEntityManager();

		response = mySvc.loadModelUpdate(new ModelUpdateRequest());
		assertEquals(1, response.getDomainList().size());
		assertEquals(1, response.getDomainList().get(0).getServiceList().size());
		assertEquals(1, response.getDomainList().get(0).getServiceList().get(0).getVersionList().size());
		assertEquals(1, response.getDomainList().get(0).getServiceList().get(0).getVersionList().get(0).getMethodList().size());
		assertEquals(oldMethod.getPid(), response.getDomainList().get(0).getServiceList().get(0).getVersionList().get(0).getMethodList().get(0).getPid());

	}

	@Test
	public void testLoadAndSaveSvcVerServerSecurity() throws Exception {

		newEntityManager();

		DtoDomain d1 = mySvc.unitTestMethod_addDomain("asv_did", "asv_did");
		GService d1s1 = mySvc.addService(d1.getPid(), createTestService());
		PersHttpClientConfig hcc = myDao.getOrCreateHttpClientConfig("httpServer");
		PersAuthenticationHostLocalDatabase auth = myDao.getOrCreateAuthenticationHostLocalDatabase("AUTHHOST");
		PersAuthenticationHostLocalDatabase auth2 = myDao.getOrCreateAuthenticationHostLocalDatabase("AUTHHOST2");

		newEntityManager();

		DtoServiceVersionSoap11 d1s1v1 = new DtoServiceVersionSoap11();
		d1s1v1.setActive(true);
		d1s1v1.setId("ASV_SV1");
		d1s1v1.setName("ASV_SV1_Name");
		d1s1v1.setWsdlLocation(HTTP_FOO);
		d1s1v1.setHttpClientConfigPid(hcc.getPid());
		d1s1v1 = mySvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v1, new ArrayList<GResource>());

		newEntityManager();

		// Add one

		d1s1v1 = (DtoServiceVersionSoap11) mySvc.loadServiceVersion(d1s1v1.getPid()).getServiceVersion();
		GWsSecServerSecurity cli = new GWsSecServerSecurity();
		cli.setAuthHostPid(auth.getPid());
		d1s1v1.getServerSecurityList().add(cli);

		mySvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v1, new ArrayList<GResource>());

		newEntityManager();

		d1s1v1 = (DtoServiceVersionSoap11) mySvc.loadServiceVersion(d1s1v1.getPid()).getServiceVersion();
		mySvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v1, new ArrayList<GResource>());

		newEntityManager();

		// Add a second

		d1s1v1 = (DtoServiceVersionSoap11) mySvc.loadServiceVersion(d1s1v1.getPid()).getServiceVersion();
		cli = new GWsSecServerSecurity();
		cli.setAuthHostPid(auth2.getPid());
		d1s1v1.getServerSecurityList().add(cli);

		mySvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v1, new ArrayList<GResource>());

		newEntityManager();

		d1s1v1 = (DtoServiceVersionSoap11) mySvc.loadServiceVersion(d1s1v1.getPid()).getServiceVersion();
		assertEquals(2, d1s1v1.getServerSecurityList().size());
		assertEquals(auth.getPid().longValue(), d1s1v1.getServerSecurityList().get(0).getAuthHostPid());
		assertEquals(auth2.getPid().longValue(), d1s1v1.getServerSecurityList().get(1).getAuthHostPid());

		// Remove one

		ourLog.info("Removing sec with PID {} but keeping {}", d1s1v1.getServerSecurityList().get(0).getPid(), d1s1v1.getServerSecurityList().get(1).getPid());

		d1s1v1.getServerSecurityList().remove(d1s1v1.getServerSecurityList().get(0));
		mySvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v1, new ArrayList<GResource>());

		newEntityManager();

		d1s1v1 = (DtoServiceVersionSoap11) mySvc.loadServiceVersion(d1s1v1.getPid()).getServiceVersion();
		assertEquals(1, d1s1v1.getServerSecurityList().size());
		assertEquals(auth2.getPid().longValue(), d1s1v1.getServerSecurityList().get(0).getAuthHostPid());

	}

	@Test(expected = IllegalArgumentException.class)
	public void testLoadAndSaveSvcVerServerSecurityNoAuthHost() throws Exception {

		newEntityManager();

		DtoDomain d1 = mySvc.unitTestMethod_addDomain("asv_did", "asv_did");
		GService d1s1 = mySvc.addService(d1.getPid(), createTestService());
		PersHttpClientConfig hcc = myDao.getOrCreateHttpClientConfig("httpServer");
		myDao.getOrCreateAuthenticationHostLocalDatabase("AUTHHOST");

		newEntityManager();

		DtoServiceVersionSoap11 d1s1v1 = new DtoServiceVersionSoap11();
		d1s1v1.setActive(true);
		d1s1v1.setId("ASV_SV1");
		d1s1v1.setName("ASV_SV1_Name");
		d1s1v1.setWsdlLocation(HTTP_FOO);
		d1s1v1.setHttpClientConfigPid(hcc.getPid());
		d1s1v1 = mySvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v1, new ArrayList<GResource>());

		newEntityManager();

		// Add one

		d1s1v1 = (DtoServiceVersionSoap11) mySvc.loadServiceVersion(d1s1v1.getPid()).getServiceVersion();
		GWsSecServerSecurity cli = new GWsSecServerSecurity();

		// Don't set the auth host, this should mean an exception is thrown

		d1s1v1.getServerSecurityList().add(cli);

		mySvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v1, new ArrayList<GResource>());
	}

	@Test
	public void testLoadAndSaveSvcVerSoap11() throws Exception {

		newEntityManager();

		DtoDomain d1 = mySvc.unitTestMethod_addDomain("asv_did", "asv_did");
		GService d1s1 = mySvc.addService(d1.getPid(), createTestService());
		PersHttpClientConfig hcc = myDao.getOrCreateHttpClientConfig("httpclient");

		newEntityManager();

		DtoServiceVersionSoap11 d1s1v1 = new DtoServiceVersionSoap11();
		d1s1v1.setActive(true);
		d1s1v1.setId("ASV_SV1");
		d1s1v1.setName("ASV_SV1_Name");
		d1s1v1.setWsdlLocation(HTTP_FOO);
		d1s1v1.setHttpClientConfigPid(hcc.getPid());

		List<GResource> resources = new ArrayList<GResource>();
		resources.add(new GResource(HTTP_FOO, "text/xml", "contents1"));
		resources.add(new GResource(HTTP_BAR, "text/xml", "contents2"));

		d1s1v1.getUrlList().add(new GServiceVersionUrl("url1", HTTP_URL1));
		d1s1v1.getUrlList().add(new GServiceVersionUrl("url2", HTTP_URL2));

		d1s1v1 = mySvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v1, resources);

		newEntityManager();

		/*
		 * Putting in the same old resources, but they look new because they
		 * don't have IDs
		 */

		d1s1v1.setWsdlLocation(HTTP_BAR);
		mySvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v1, resources);

		newEntityManager();

		GSoap11ServiceVersionAndResources copy = mySvc.loadServiceVersion(d1s1v1.getPid());
		DtoServiceVersionSoap11 svcVer = (DtoServiceVersionSoap11) copy.getServiceVersion();
		assertEquals(HTTP_BAR, svcVer.getWsdlLocation());
		assertEquals(2, copy.getResource().size());

		/*
		 * Now try saving with the existing resources back in
		 */

		d1s1v1.setWsdlLocation("http://baz");
		mySvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v1, copy.getResource());

		newEntityManager();

		/*
		 * Now remove a resource
		 */

		d1s1v1.setWsdlLocation("http://baz");
		List<GResource> resource = copy.getResource();
		resource.remove(0);
		mySvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v1, resource);

		newEntityManager();

		copy = mySvc.loadServiceVersion(d1s1v1.getPid());
		svcVer = (DtoServiceVersionSoap11) copy.getServiceVersion();
		assertEquals("http://baz", svcVer.getWsdlLocation());
		assertEquals(1, copy.getResource().size());

		/*
		 * Now change the URL
		 */

		svcVer.getUrlList().get(0).setUrl(HTTP_ZZZZZZ);
		mySvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), svcVer, resource);

		newEntityManager();

		copy = mySvc.loadServiceVersion(d1s1v1.getPid());
		svcVer = (DtoServiceVersionSoap11) copy.getServiceVersion();
		assertEquals(2, svcVer.getUrlList().size());
		assertEquals("url1", svcVer.getUrlList().get(0).getId());
		assertEquals(HTTP_ZZZZZZ, svcVer.getUrlList().get(0).getUrl());

		/*
		 * Now change a URL ID
		 */

		svcVer.getUrlList().get(0).setId("aaaaa");
		mySvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), svcVer, resource);

		newEntityManager();

		copy = mySvc.loadServiceVersion(d1s1v1.getPid());
		svcVer = (DtoServiceVersionSoap11) copy.getServiceVersion();
		assertEquals(2, svcVer.getUrlList().size());
		assertEquals("aaaaa", svcVer.getUrlList().get(0).getId());
		assertEquals(HTTP_ZZZZZZ, svcVer.getUrlList().get(0).getUrl());

	}

	@Test
	public void testLoadWsdl() throws Exception {

		DtoServiceVersionSoap11 ver = new DtoServiceVersionSoap11();

		PersServiceVersionSoap11 persSvcVer = new PersServiceVersionSoap11();
		persSvcVer.setPid(ver.getPid());
		persSvcVer.setWsdlUrl("http://wsdlurl");

		PersMethod m1 = new PersMethod();
		m1.setName("m1");
		persSvcVer.addMethod(m1);

		PersMethod m2 = new PersMethod();
		m2.setName("m2");
		persSvcVer.addMethod(m2);

		persSvcVer.addResource("http://wsdlurl", "application/xml", "wsdlcontents");
		persSvcVer.addResource("http://xsdurl", "application/xml", "xsdcontents");

		PersServiceVersionUrl url = new PersServiceVersionUrl();
		url.setUrlId("url1");
		url.setUrl(HTTP_SVCURL);
		url.setServiceVersion(persSvcVer);
		persSvcVer.addUrl(url);

		PersHttpClientConfig cfg = new PersHttpClientConfig();
		cfg.setDefaults();
		when(mySoapInvoker.introspectServiceFromUrl(cfg, "http://wsdlurl")).thenReturn(persSvcVer);

		GSoap11ServiceVersionAndResources verAndRes = mySvc.loadSoap11ServiceVersionFromWsdl(ver, cfg.toDto(), "http://wsdlurl");

		assertEquals(2, verAndRes.getResource().size());
		assertEquals("http://wsdlurl", verAndRes.getResource().get(0).getUrl());
		assertEquals("wsdlcontents", verAndRes.getResource().get(0).getText());
		assertEquals("http://xsdurl", verAndRes.getResource().get(1).getUrl());
		assertEquals("xsdcontents", verAndRes.getResource().get(1).getText());
		assertEquals("http://wsdlurl", verAndRes.getServiceVersion().getResourcePointerList().get(0).getUrl());
		assertEquals("application/xml", verAndRes.getServiceVersion().getResourcePointerList().get(0).getType());
		assertEquals("wsdlcontents".length(), verAndRes.getServiceVersion().getResourcePointerList().get(0).getSize());
		assertEquals("m1", verAndRes.getServiceVersion().getMethodList().get(0).getName());
		assertEquals("m2", verAndRes.getServiceVersion().getMethodList().get(1).getName());

		// Load stats
		newEntityManager();

		PersDomain d0 = myDao.getOrCreateDomainWithId("d0");
		PersService s0 = myDao.getOrCreateServiceWithId(d0, "s0");
		verAndRes.getServiceVersion().setId("v0");
		verAndRes.getServiceVersion().setHttpClientConfigPid(myDao.getHttpClientConfigs().iterator().next().getPid());

		BaseDtoServiceVersion newVer = mySvc.saveServiceVersion(d0.getPid(), s0.getPid(), verAndRes.getServiceVersion(), verAndRes.getResource());

		newEntityManager();

		BasePersServiceVersion svcVer = myDao.getServiceVersionByPid(newVer.getPid());
		PersServiceVersionResource res = svcVer.getResourceForUri("http://wsdlurl");
		myStatsSvc.recordInvocationStaticResource(new Date(), res);
		myStatsSvc.flushStatus();

		newEntityManager();

		// Now change the WSDL URL

		persSvcVer = new PersServiceVersionSoap11();
		persSvcVer.setPid(ver.getPid());
		persSvcVer.addMethod(m1);
		persSvcVer.addMethod(m2);
		persSvcVer.addResource("http://wsdlurl2", "application/xml", "wsdlcontents");
		persSvcVer.addResource("http://xsdurl", "application/xml", "xsdcontents");
		persSvcVer.setWsdlUrl("http://wsdlurl2");
		when(mySoapInvoker.introspectServiceFromUrl(cfg, "http://wsdlurl2")).thenReturn(persSvcVer);
		verAndRes = mySvc.loadSoap11ServiceVersionFromWsdl((DtoServiceVersionSoap11) newVer, cfg.toDto(), "http://wsdlurl2");
		newVer = mySvc.saveServiceVersion(d0.getPid(), s0.getPid(), verAndRes.getServiceVersion(), verAndRes.getResource());

		newEntityManager();

	}

	// @Test
	public void testMultipleStatsLoadsInParallel() throws Exception {
		newEntityManager();

		myConfigSvc.getConfig();

		newEntityManager();

		PersDomain d0 = myDao.getOrCreateDomainWithId("d0");
		PersService d0s0 = myDao.getOrCreateServiceWithId(d0, "d0s0");
		PersServiceVersionSoap11 d0s0v0 = (PersServiceVersionSoap11) myDao.getOrCreateServiceVersionWithId(d0s0, "d0s0v0", ServiceProtocolEnum.SOAP11);
		d0s0v0.getOrCreateAndAddMethodWithName("d0s0v0m0");
		myDao.saveServiceVersionInNewTransaction(d0s0v0);

		newEntityManager();

		mySvcReg.reloadRegistryFromDatabase();

		newEntityManager();

		final ModelUpdateRequest req = new ModelUpdateRequest();
		req.addServiceToLoadStats(d0s0.getPid());

		List<RetrieverThread> ts = new ArrayList<RetrieverThread>();
		for (int i = 0; i < 3; i++) {
			RetrieverThread t = new RetrieverThread(req, mySvcReg);
			t.start();
			ts.add(t);
		}

		ourLog.info("Waiting for threads to die");
		for (RetrieverThread thread : ts) {
			thread.join();
			if (thread.myFailed != null) {
				throw thread.myFailed;
			}
		}
		ourLog.info("Done");
	}

	@Test
	public void testRenameServiceVersion() throws Exception {
		newEntityManager();

		DtoDomain d1 = mySvc.unitTestMethod_addDomain("asv_did", "asv_did");
		GService d1s1 = mySvc.addService(d1.getPid(), createTestService());
		PersHttpClientConfig hcc = myDao.getOrCreateHttpClientConfig("httpclient");

		newEntityManager();

		DtoServiceVersionSoap11 d1s1v1 = new DtoServiceVersionSoap11();
		d1s1v1.setActive(true);
		d1s1v1.setId("ASV_SV1");
		d1s1v1.setName("ASV_SV1_Name");
		d1s1v1.setWsdlLocation(HTTP_FOO);
		d1s1v1.setHttpClientConfigPid(hcc.getPid());

		List<GResource> resources = new ArrayList<GResource>();
		d1s1v1 = mySvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v1, resources);

		newEntityManager();

		PersDomain pDomain = myDao.getDomainByPid(d1.getPid());
		Collection<BasePersServiceVersion> versions = pDomain.getServices().iterator().next().getVersions();

		assertEquals(1, versions.size());

		d1s1v1.setId("newId");
		mySvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v1, resources);

		newEntityManager();

		pDomain = myDao.getDomainByPid(d1.getPid());
		versions = pDomain.getServices().iterator().next().getVersions();

		assertEquals(1, versions.size());

	}

	@Test
	public void testSaveConfigWithUrlBases() throws Exception {
		newEntityManager();

		DtoConfig config = mySvc.loadConfig();

		newEntityManager();

		config.getProxyUrlBases().clear();
		config.getProxyUrlBases().add(HTTP_FOO);

		config = mySvc.saveConfig(config);

		newEntityManager();

		config = mySvc.loadConfig();
		assertEquals(1, config.getProxyUrlBases().size());
		assertEquals(HTTP_FOO, config.getProxyUrlBases().get(0));

	}

	@Test
	public void testSaveDomain() throws Exception {

		newEntityManager();

		DtoDomain d1 = mySvc.unitTestMethod_addDomain("asv_did", "asv_did");

		newEntityManager();

		DtoDomain domain = mySvc.getDomainByPid(d1.getPid());
		assertEquals("asv_did", domain.getId());

		newEntityManager();

		d1.setId("b");
		mySvc.saveDomain(d1);

		newEntityManager();

		domain = mySvc.getDomainByPid(d1.getPid());
		assertEquals("b", domain.getId());

	}

	@Test
	public void testSaveHttpClientConfig() throws Exception {
		createEverything();
		newEntityManager();

		ModelUpdateRequest req = new ModelUpdateRequest();
		req.setLoadHttpClientConfigs(true);
		ModelUpdateResponse resp = mySvc.loadModelUpdate(req);

		DtoHttpClientConfig cfg = resp.getHttpClientConfigList().get(0);

		newEntityManager();

		int cb1 = 1111;
		int cb2 = 1112;
		int cb3 = 1113;
		int cb4 = 1114;
		cfg.setCircuitBreakerEnabled(true);
		cfg.setCircuitBreakerTimeBetweenResetAttempts(cb1);
		cfg.setConnectTimeoutMillis(cb2);
		cfg.setFailureRetriesBeforeAborting(cb3);
		cfg.setReadTimeoutMillis(cb4);
		mySvc.saveHttpClientConfig(cfg, null, null, null, null);

		newEntityManager();
		req = new ModelUpdateRequest();
		req.setLoadHttpClientConfigs(true);
		resp = mySvc.loadModelUpdate(req);
		cfg = resp.getHttpClientConfigList().get(0);
		assertEquals(cb1, cfg.getCircuitBreakerTimeBetweenResetAttempts());
		assertEquals(cb2, cfg.getConnectTimeoutMillis());
		assertEquals(cb3, cfg.getFailureRetriesBeforeAborting());
		assertEquals(cb4, cfg.getReadTimeoutMillis());

		newEntityManager();

		cb1 = 2111;
		cb2 = 2112;
		cb3 = 2113;
		cb4 = 2114;
		cfg.setCircuitBreakerEnabled(true);
		cfg.setCircuitBreakerTimeBetweenResetAttempts(cb1);
		cfg.setConnectTimeoutMillis(cb2);
		cfg.setFailureRetriesBeforeAborting(cb3);
		cfg.setReadTimeoutMillis(cb4);
		mySvc.saveHttpClientConfig(cfg, null, null, null, null);

		newEntityManager();
		req = new ModelUpdateRequest();
		req.setLoadHttpClientConfigs(true);
		resp = mySvc.loadModelUpdate(req);
		cfg = resp.getHttpClientConfigList().get(0);
		assertEquals(cb1, cfg.getCircuitBreakerTimeBetweenResetAttempts());
		assertEquals(cb2, cfg.getConnectTimeoutMillis());
		assertEquals(cb3, cfg.getFailureRetriesBeforeAborting());
		assertEquals(cb4, cfg.getReadTimeoutMillis());

		newEntityManager();

		cb1 = 3111;
		cb2 = 3112;
		cb3 = 3113;
		cb4 = 3114;
		cfg.setCircuitBreakerEnabled(true);
		cfg.setCircuitBreakerTimeBetweenResetAttempts(cb1);
		cfg.setConnectTimeoutMillis(cb2);
		cfg.setFailureRetriesBeforeAborting(cb3);
		cfg.setReadTimeoutMillis(cb4);
		mySvc.saveHttpClientConfig(cfg, null, null, null, null);

		newEntityManager();
		req = new ModelUpdateRequest();
		req.setLoadHttpClientConfigs(true);
		resp = mySvc.loadModelUpdate(req);
		cfg = resp.getHttpClientConfigList().get(0);
		assertEquals(cb1, cfg.getCircuitBreakerTimeBetweenResetAttempts());
		assertEquals(cb2, cfg.getConnectTimeoutMillis());
		assertEquals(cb3, cfg.getFailureRetriesBeforeAborting());
		assertEquals(cb4, cfg.getReadTimeoutMillis());

		newEntityManager();

	}

	@Test
	public void testSaveUser() throws Exception {
		newEntityManager();

		DtoDomain d1 = mySvc.unitTestMethod_addDomain("asv_did1", "asv_did1");
		DtoDomain d2 = mySvc.unitTestMethod_addDomain("asv_did2", "asv_did2");

		GService d1s1 = mySvc.addService(d1.getPid(), createTestService());
		GService d2s1 = mySvc.addService(d2.getPid(), new GService("d2s1", "d2s1", true));
		PersHttpClientConfig hcc = myDao.getOrCreateHttpClientConfig("httpclient");

		DtoServiceVersionSoap11 d1s1v1 = new DtoServiceVersionSoap11();
		d1s1v1.setActive(true);
		d1s1v1.setId("D1S1V1");
		d1s1v1.setName("D1S1V1");
		d1s1v1.setWsdlLocation(HTTP_FOO);
		d1s1v1.setHttpClientConfigPid(hcc.getPid());
		List<GResource> resources = new ArrayList<GResource>();
		resources.add(new GResource(HTTP_FOO, "text/xml", "contents1"));
		resources.add(new GResource(HTTP_BAR, "text/xml", "contents2"));
		d1s1v1 = mySvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v1, resources);

		DtoServiceVersionSoap11 d2s1v1 = new DtoServiceVersionSoap11();
		d2s1v1.setActive(true);
		d2s1v1.setId("D2S1V1");
		d2s1v1.setName("D2S1V1");
		d2s1v1.setWsdlLocation(HTTP_FOO);
		d2s1v1.setHttpClientConfigPid(hcc.getPid());
		d2s1v1 = mySvc.saveServiceVersion(d2.getPid(), d2s1.getPid(), d2s1v1, resources);

		GServiceMethod d1s1v1m1 = new GServiceMethod();
		d1s1v1m1.setName("d1s1v1m1");
		d1s1v1m1 = mySvc.addServiceVersionMethod(d1s1v1.getPid(), d1s1v1m1);

		GServiceMethod d2s1v1m1 = new GServiceMethod();
		d2s1v1m1.setName("d2s1v1m1");
		d2s1v1m1 = mySvc.addServiceVersionMethod(d2s1v1.getPid(), d2s1v1m1);

		DtoAuthenticationHostLocalDatabase authHost = new DtoAuthenticationHostLocalDatabase();
		authHost.setModuleId("authHost");
		authHost.setModuleName("authHost");
		authHost = (DtoAuthenticationHostLocalDatabase) mySvc.saveAuthenticationHost(authHost).get(0);

		newEntityManager();

		GUser user = new GUser();
		user.setUsername("username");
		user.setAuthHostPid(authHost.getPid());
		user = mySvc.saveUser(user);

		assertThat(user.getPid(), Matchers.greaterThan(0L));
		newEntityManager();

		/*
		 * Add Global Permission
		 */

		assertThat(user.getGlobalPermissions(), not(contains(UserGlobalPermissionEnum.SUPERUSER)));
		user.addGlobalPermission(UserGlobalPermissionEnum.SUPERUSER);
		mySvc.saveUser(user);
		newEntityManager();
		user = mySvc.loadUser(user.getPid(), false);
		assertThat(user.getGlobalPermissions(), (contains(UserGlobalPermissionEnum.SUPERUSER)));

		/*
		 * Remove Global permission
		 */

		user.removeGlobalPermission(UserGlobalPermissionEnum.SUPERUSER);
		mySvc.saveUser(user);
		newEntityManager();
		user = mySvc.loadUser(user.getPid(), false);
		assertThat(user.getGlobalPermissions(), not(contains(UserGlobalPermissionEnum.SUPERUSER)));

		/*
		 * Add Domain Permission
		 */

		GUserDomainPermission domPerm1 = new GUserDomainPermission();
		domPerm1.setDomainPid(d1.getPid());
		domPerm1.setAllowAllServices(true);
		user.addDomainPermission(domPerm1);
		mySvc.saveUser(user);
		newEntityManager();
		user = mySvc.loadUser(user.getPid(), false);
		assertEquals(1, user.getDomainPermissions().size());
		assertEquals(d1.getPid(), user.getDomainPermissions().get(0).getDomainPid());

		/*
		 * Add second domain permission
		 */

		GUserDomainPermission domPerm2 = new GUserDomainPermission();
		domPerm2.setDomainPid(d2.getPid());
		domPerm2.setAllowAllServices(true);
		user.addDomainPermission(domPerm2);
		mySvc.saveUser(user);
		newEntityManager();
		user = mySvc.loadUser(user.getPid(), false);
		assertEquals(2, user.getDomainPermissions().size());
		assertThat(user.getDomainPermissions().get(0).getDomainPid(), isOneOf(d1.getPid(), d2.getPid()));
		assertThat(user.getDomainPermissions().get(1).getDomainPid(), isOneOf(d1.getPid(), d2.getPid()));

		/*
		 * Add service permission to each domain permission
		 */

		user.getDomainPermissions().get(0).getOrCreateServicePermission(d1s1.getPid()).setAllowAllServiceVersions(true);
		user.getDomainPermissions().get(1).getOrCreateServicePermission(d2s1.getPid()).setAllowAllServiceVersions(true);
		mySvc.saveUser(user);
		newEntityManager();
		user = mySvc.loadUser(user.getPid(), false);
		assertEquals(2, user.getDomainPermissions().size());
		assertEquals(1, user.getDomainPermissions().get(0).getServicePermissions().size());
		assertEquals(1, user.getDomainPermissions().get(1).getServicePermissions().size());
		assertEquals(d1s1.getPid(), user.getDomainPermissions().get(0).getServicePermissions().get(0).getServicePid());
		assertEquals(d2s1.getPid(), user.getDomainPermissions().get(1).getServicePermissions().get(0).getServicePid());

		/*
		 * Add service version permission to each service permission
		 */

		user.getDomainPermissions().get(0).getOrCreateServicePermission(d1s1.getPid()).getOrCreateServiceVersionPermission(d1s1v1.getPid()).setAllowAllServiceVersionMethods(true);
		user.getDomainPermissions().get(1).getOrCreateServicePermission(d2s1.getPid()).getOrCreateServiceVersionPermission(d2s1v1.getPid()).setAllowAllServiceVersionMethods(true);
		mySvc.saveUser(user);
		newEntityManager();
		user = mySvc.loadUser(user.getPid(), false);
		assertEquals(2, user.getDomainPermissions().size());
		assertEquals(1, user.getDomainPermissions().get(0).getServicePermissions().get(0).getServiceVersionPermissions().size());
		assertEquals(1, user.getDomainPermissions().get(1).getServicePermissions().get(0).getServiceVersionPermissions().size());
		assertEquals(d1s1v1.getPid(), user.getDomainPermissions().get(0).getServicePermissions().get(0).getServiceVersionPermissions().get(0).getServiceVersionPid());
		assertEquals(d2s1v1.getPid(), user.getDomainPermissions().get(1).getServicePermissions().get(0).getServiceVersionPermissions().get(0).getServiceVersionPid());

		/*
		 * Add method to each version perm
		 */

		user.getDomainPermissions().get(0).getOrCreateServicePermission(d1s1.getPid()).getOrCreateServiceVersionPermission(d1s1v1.getPid()).getOrCreateServiceVersionMethodPermission(d1s1v1m1.getPid());
		user.getDomainPermissions().get(1).getOrCreateServicePermission(d2s1.getPid()).getOrCreateServiceVersionPermission(d2s1v1.getPid()).getOrCreateServiceVersionMethodPermission(d2s1v1m1.getPid());
		mySvc.saveUser(user);
		newEntityManager();
		user = mySvc.loadUser(user.getPid(), false);
		assertEquals(2, user.getDomainPermissions().size());
		assertEquals(1, user.getDomainPermissions().get(0).getServicePermissions().get(0).getServiceVersionPermissions().get(0).getServiceVersionMethodPermissions().size());
		assertEquals(1, user.getDomainPermissions().get(1).getServicePermissions().get(0).getServiceVersionPermissions().get(0).getServiceVersionMethodPermissions().size());
		assertEquals(d1s1v1m1.getPid(), user.getDomainPermissions().get(0).getServicePermissions().get(0).getServiceVersionPermissions().get(0).getServiceVersionMethodPermissions().get(0).getServiceVersionMethodPid());
		assertEquals(d2s1v1m1.getPid(), user.getDomainPermissions().get(1).getServicePermissions().get(0).getServiceVersionPermissions().get(0).getServiceVersionMethodPermissions().get(0).getServiceVersionMethodPid());

		/*
		 * Remove domain permission
		 */

		user.removeDomainPermission(user.getDomainPermissions().get(0));
		mySvc.saveUser(user);
		assertEquals(1, user.getDomainPermissions().size());
		assertEquals(1, user.getDomainPermissions().get(0).getServicePermissions().get(0).getServiceVersionPermissions().get(0).getServiceVersionMethodPermissions().size());
		assertEquals(d2s1v1m1.getPid(), user.getDomainPermissions().get(0).getServicePermissions().get(0).getServiceVersionPermissions().get(0).getServiceVersionMethodPermissions().get(0).getServiceVersionMethodPid());

	}

	private GService createTestService() {
		GService retVal = new GService();
		retVal.setId("asv_sid");
		retVal.setName("asv_sname");
		retVal.setActive(true);
		return retVal;
	}

	private static final class RetrieverThread extends Thread {
		private Exception myFailed;
		private final ModelUpdateRequest myReq;
		private ServiceRegistryBean mySvcReg;

		private RetrieverThread(ModelUpdateRequest theReq, ServiceRegistryBean theSvcReg) {
			myReq = theReq;
			mySvcReg = theSvcReg;
		}

		@Override
		public void run() {
			try {

				EntityManager entityManager = ourEntityManagerFactory.createEntityManager();
				entityManager.getTransaction().begin();
				DaoBean dao = new DaoBean();
				dao.setEntityManager(entityManager);

				RuntimeStatusBean rs = new RuntimeStatusBean();
				rs.setDao(dao);

				ConfigServiceBean cs = new ConfigServiceBean();
				cs.setBroadcastSender(mock(IBroadcastSender.class));
				cs.setDao(dao);

				RuntimeStatusQueryBean rqb = new RuntimeStatusQueryBean();
				rqb.setConfigSvcForUnitTest(cs);
				rqb.setStatusSvcForUnitTest(rs);

				AdminServiceBean sSvc = new AdminServiceBean();
				sSvc.setPersSvc(dao);
				sSvc.setConfigSvc(cs);
				sSvc.setServiceRegistry(mySvcReg);
				sSvc.setRuntimeStatusSvc(rs);
				sSvc.setRuntimeStatusQuerySvcForUnitTests(rqb);

				RuntimeStatusBean statsSvc = new RuntimeStatusBean();
				statsSvc.setDao(dao);

				sSvc.loadModelUpdate(myReq);

				entityManager.getTransaction().commit();

			} catch (Exception e) {
				myFailed = e;
				e.printStackTrace();
				fail("" + e.getMessage());
			}
		}
	}

}