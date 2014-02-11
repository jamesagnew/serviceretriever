package net.svcret.core.integrationtest;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;
import javax.xml.ws.Holder;

import net.svcret.admin.api.IAdminServiceLocal;
import net.svcret.admin.api.ProcessingException;
import net.svcret.admin.api.UnexpectedFailureException;
import net.svcret.admin.shared.enm.AuthorizationOutcomeEnum;
import net.svcret.admin.shared.enm.InvocationStatsIntervalEnum;
import net.svcret.admin.shared.enm.ResponseTypeEnum;
import net.svcret.admin.shared.enm.ThrottlePeriodEnum;
import net.svcret.admin.shared.model.BaseDtoAuthenticationHost;
import net.svcret.admin.shared.model.BaseDtoDashboardObject;
import net.svcret.admin.shared.model.BaseDtoServiceVersion;
import net.svcret.admin.shared.model.DtoAuthenticationHostLocalDatabase;
import net.svcret.admin.shared.model.DtoConfig;
import net.svcret.admin.shared.model.DtoDomain;
import net.svcret.admin.shared.model.DtoDomainList;
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
import net.svcret.admin.shared.model.GPartialUserList;
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
import net.svcret.admin.shared.model.PartialUserListRequest;
import net.svcret.admin.shared.model.RetrieverNodeTypeEnum;
import net.svcret.admin.shared.model.ServiceProtocolEnum;
import net.svcret.admin.shared.model.UserGlobalPermissionEnum;
import net.svcret.core.api.IConfigService;
import net.svcret.core.api.IDao;
import net.svcret.core.api.IHttpClient;
import net.svcret.core.api.IRuntimeStatus;
import net.svcret.core.api.IRuntimeStatusQueryLocal;
import net.svcret.core.api.ISecurityService;
import net.svcret.core.api.IServiceOrchestrator;
import net.svcret.core.api.IServiceRegistry;
import net.svcret.core.api.SrBeanIncomingRequest;
import net.svcret.core.api.SrBeanIncomingResponse;
import net.svcret.core.api.SrBeanProcessedRequest;
import net.svcret.core.api.SrBeanProcessedResponse;
import net.svcret.core.ejb.ConfigServiceBean;
import net.svcret.core.invoker.soap.IServiceInvokerSoap11;
import net.svcret.core.log.IFilesystemAuditLogger;
import net.svcret.core.log.ITransactionLogger;
import net.svcret.core.model.entity.BasePersAuthenticationHost;
import net.svcret.core.model.entity.BasePersServiceVersion;
import net.svcret.core.model.entity.PersDomain;
import net.svcret.core.model.entity.PersHttpClientConfig;
import net.svcret.core.model.entity.PersLibraryMessage;
import net.svcret.core.model.entity.PersMethod;
import net.svcret.core.model.entity.PersService;
import net.svcret.core.model.entity.PersServiceVersionResource;
import net.svcret.core.model.entity.PersServiceVersionUrl;
import net.svcret.core.model.entity.PersUser;
import net.svcret.core.model.entity.soap.PersServiceVersionSoap11;
import net.svcret.core.throttle.IThrottlingService;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.ResourceUtils;

public class AdminServiceBeanIntegrationTest /* extends BaseJpaTest */{

	private static final String HTTP_ZZZZZZ = "http://127.0.0.6";

	private static final String HTTP_URL2 = "http://127.0.0.4";

	private static final String HTTP_URL1 = "http://127.0.0.5";

	private static final String HTTP_SVCURL = "http://127.0.0.3";

	private static final String HTTP_BAR = "http://127.0.0.1";

	private static final String HTTP_FOO = "http://127.0.0.2";

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(AdminServiceBeanIntegrationTest.class);

	static IConfigService ourConfigSvc;
	static IDao ourDao;
	private Date myEverythingInvocationTime;
	static ISecurityService ourSecSvc;
	static IServiceInvokerSoap11 ourSoapInvoker;
	static IRuntimeStatus ourStatsSvc;
	static IRuntimeStatusQueryLocal ourStatsQuerySvc;
	static IAdminServiceLocal ourAdminSvc;
	static IServiceRegistry ourSvcReg;
	static ITransactionLogger ourTransactionLogSvc;
	static ClassPathXmlApplicationContext ourAppCtx;
	static JpaTransactionManager ourTxManager;
	static TransactionTemplate ourTxTemplate;
	static IHttpClient ourHttpClientMock;
	static IFilesystemAuditLogger ourFilesystemAuditLogger;
	static IThrottlingService ourThrottlingService;
	static IServiceOrchestrator ourOrchSvc;
	static EntityManagerFactory ourEntityManagerFactory;

	@AfterClass
	public static void afterClass() {

		// newEntityManager();
		//
		// Query q = myEntityManager.createQuery("DELETE FROM PersDomain p");
		// q.executeUpdate();
		//
		// newEntityManager();

		ourAppCtx.close();

	}

	public static final Object unwrapProxy(Object bean) throws Exception {
		  
		 /*
		  * If the given object is a proxy, set the return value as the object
		  * being proxied, otherwise return the given object.
		  */
		 if (AopUtils.isAopProxy(bean) && bean instanceof Advised) {
		   
		  Advised advised = (Advised) bean;
		   
		  bean = advised.getTargetSource().getTarget();
		 }
		  
		 return bean;
		}
	
	@BeforeClass
	public static void beforeClass() {
		ourAppCtx = new ClassPathXmlApplicationContext("classpath:svcret-unittest.xml");

		ourTxManager = (JpaTransactionManager) ourAppCtx.getBean("myTxManager");
		ourAdminSvc = ourAppCtx.getBean(IAdminServiceLocal.class);
		ourConfigSvc = ourAppCtx.getBean(IConfigService.class);
		ourDao = ourAppCtx.getBean(IDao.class);
		ourSecSvc = ourAppCtx.getBean(ISecurityService.class);
		ourSoapInvoker = ourAppCtx.getBean(IServiceInvokerSoap11.class);
		ourStatsSvc = ourAppCtx.getBean(IRuntimeStatus.class);
		ourStatsQuerySvc = ourAppCtx.getBean(IRuntimeStatusQueryLocal.class);
		ourSvcReg = ourAppCtx.getBean(IServiceRegistry.class);
		ourTransactionLogSvc = ourAppCtx.getBean(ITransactionLogger.class);
		ourHttpClientMock = ourAppCtx.getBean(IHttpClient.class);
		ourFilesystemAuditLogger = ourAppCtx.getBean(IFilesystemAuditLogger.class);
		ourOrchSvc = ourAppCtx.getBean(IServiceOrchestrator.class);
		ourThrottlingService=ourAppCtx.getBean(IThrottlingService.class);
		ourEntityManagerFactory = ourAppCtx.getBean(EntityManagerFactory.class);
		ourTxTemplate = new TransactionTemplate(ourTxManager);
	}

	@Before
	public void before() throws ProcessingException, UnexpectedFailureException {
		deleteEverything();
	}

	@After
	public void after() throws ProcessingException, UnexpectedFailureException {
		deleteEverything();
	}

	static void deleteEverything() throws ProcessingException, UnexpectedFailureException {
		Collection<Long> pids = ourAdminSvc.getAllDomainPids();
		for (Long dtoDomain : pids) {
			ourAdminSvc.deleteDomain(dtoDomain);
		}

		pids = ourAdminSvc.getAllMonitorRulePids();
		for (Long pid : pids) {
			ourAdminSvc.deleteMonitorRule(pid);
		}

		PartialUserListRequest ureq = new PartialUserListRequest();
		GPartialUserList users = ourAdminSvc.loadUsers(ureq);
		for (GUser next : users) {
			if (!next.getUsername().equals(PersUser.DEFAULT_ADMIN_USERNAME)) {
				ourAdminSvc.deleteUser(next.getPid());
			}
		}

		for (DtoLibraryMessage next : ourAdminSvc.loadLibraryMessages()) {
			ourAdminSvc.deleteLibraryMessage(next.getPid());
		}
		
		reset(ourHttpClientMock);
	}

	public static void injectConfigServiceDefaults(ConfigServiceBean theConfigService) {
		theConfigService.setNodeType(RetrieverNodeTypeEnum.PRIMARY);
		theConfigService.setNodeId("mock");
	}

	private DtoDomain createEverything() throws Exception {
		SrBeanIncomingRequest iReq = new SrBeanIncomingRequest();
		newEntityManager();

		ourSecSvc.loadUserCatalogIfNeeded();

		BasePersAuthenticationHost authHost = ourDao.getAuthenticationHost(BasePersAuthenticationHost.MODULE_ID_ADMIN_AUTH);
		authHost.setKeepNumRecentTransactionsFail(100);
		authHost.setKeepNumRecentTransactionsFault(100);
		authHost.setKeepNumRecentTransactionsSecurityFail(100);
		authHost.setKeepNumRecentTransactionsSuccess(100);
		ourDao.saveAuthenticationHost(authHost);

		newEntityManager();

		DtoDomain d1 = ourAdminSvc.addDomain(new DtoDomain("asv_did", "asv_did"));
		GService d1s1 = ourAdminSvc.addService(d1.getPid(), createTestService());

		newEntityManager();

		DtoServiceVersionSoap11 d1s1v1 = new DtoServiceVersionSoap11();
		d1s1v1.setActive(true);
		d1s1v1.setId("ASV_SV1");
		d1s1v1.setName("ASV_SV1_Name");
		d1s1v1.setWsdlLocation(HTTP_FOO);
		d1s1v1.setHttpClientConfigPid(ourAdminSvc.getDefaultHttpClientConfigPid());

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

		DtoServiceVersionSoap11 ver = ourAdminSvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v1, resources);

		ver.getUrlList().add(new GServiceVersionUrl("url1", HTTP_FOO));
		ver = ourAdminSvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), ver, resources);

		newEntityManager();
		// BasePersServiceVersion persVer =
		// myDao.getServiceVersionByPid(ver.getPid());

		// Create a user with access
		GUser user = new GUser();
		user.setAuthHostPid(ourDao.getAllAuthenticationHosts().iterator().next().getPid());
		user.setUsername("username2");
		user.setDomainPermissions(new ArrayList<GUserDomainPermission>());
		user.getDomainPermissions().add(new GUserDomainPermission());
		user.getDomainPermissions().get(0).setDomainPid(d1.getPid());
		user.getDomainPermissions().get(0).setServicePermissions(new ArrayList<GUserServicePermission>());
		user.getDomainPermissions().get(0).getServicePermissions().add(new GUserServicePermission());
		user.getDomainPermissions().get(0).getServicePermissions().get(0).setServicePid(d1s1.getPid());
		user.getDomainPermissions().get(0).getServicePermissions().get(0).setServiceVersionPermissions(new ArrayList<GUserServiceVersionPermission>());
		user.getDomainPermissions().get(0).getServicePermissions().get(0).getServiceVersionPermissions().add(new GUserServiceVersionPermission());
		user.getDomainPermissions().get(0).getServicePermissions().get(0).getServiceVersionPermissions().get(0).setServiceVersionPid(ver.getPid());
		user.getDomainPermissions().get(0).getServicePermissions().get(0).getServiceVersionPermissions().get(0).getOrCreateServiceVersionMethodPermission(ver.getMethodList().get(0).getPid());

		user = ourAdminSvc.saveUser(user);

		newEntityManager();

		// Add stats
		// persVer = myDao.getServiceVersionByPid(ver.getPid());
		// PersServiceVersionStatus status = persVer.getStatus();
		// assertNotNull(status);

		PersMethod m1 = ourDao.getServiceVersionMethodByPid(ver.getMethodList().iterator().next().getPid());

		newEntityManager();

		// Record invocation
		SrBeanIncomingResponse httpResponse = new SrBeanIncomingResponse();
		httpResponse.setBody("1234");
		httpResponse.setResponseTime(123);
		SrBeanProcessedResponse bean = new SrBeanProcessedResponse();
		bean.setResponseType(ResponseTypeEnum.SUCCESS);

		myEverythingInvocationTime = new Date(System.currentTimeMillis() - (60 * 1000L));
		ourStatsSvc.recordInvocationMethod(myEverythingInvocationTime, 100, SrBeanProcessedRequest.forUnitTest(m1), null, httpResponse, bean, iReq);

		newEntityManager();

		ourStatsSvc.flushStatus();

		newEntityManager();

		BasePersServiceVersion persVer = ourDao.getServiceVersionByPid(ver.getPid());

		SrBeanIncomingRequest request = new SrBeanIncomingRequest();
		request.setRequestHostIp("127.0.0.1");
		request.setRequestHeaders(new HashMap<String, List<String>>());
		request.setRequestTime(new Date());
		String requestBody = "request body";
		request.setInputReader(new StringReader(requestBody));

		SrBeanProcessedResponse invocationResponse = new SrBeanProcessedResponse();
		invocationResponse.setResponseHeaders(new HashMap<String, List<String>>());
		invocationResponse.setResponseType(ResponseTypeEnum.SUCCESS);
		httpResponse.setSuccessfulUrl(ourDao.getServiceVersionUrlByPid(ver.getUrlList().get(0).getPid()));
		AuthorizationOutcomeEnum authorizationOutcome = AuthorizationOutcomeEnum.AUTHORIZED;
		PersUser persUser = ourDao.getUser(user.getPidOrNull());
		SrBeanProcessedRequest processedRequest = new SrBeanProcessedRequest();
		processedRequest.setObscuredRequestBody(requestBody);
		processedRequest.setServiceVersion(persVer);
		processedRequest.setResultMethod(m1, requestBody, "text/plain");
		ourTransactionLogSvc.logTransaction(request, persUser, invocationResponse, httpResponse, authorizationOutcome, processedRequest);

		newEntityManager();

		ourTransactionLogSvc.flush();

		newEntityManager();

		ourSvcReg.reloadRegistryFromDatabase();

		newEntityManager();

		return ourAdminSvc.getDomainByPid(d1.getPid());
	}

	protected void newEntityManager() {
		// if (myTxManager.getTransaction(definition))
		// super.newEntityManager();
		//
		// myDao.setEntityManager(myEntityManager);
	}

	@Test
	public void testAddAndSaveService() throws Exception {
		newEntityManager();

		DtoDomain domain = ourAdminSvc.addDomain(new DtoDomain("domain_id3", "domain_name"));
		newEntityManager();

		GService service = ourAdminSvc.addService(domain.getPid(), createTestService());

		newEntityManager();

		assertEquals(createTestService().getId(), service.getId());
		assertEquals(createTestService().getName(), service.getName());

		assertFalse(service.isStatsInitialized());

		newEntityManager();

		service.setName("name2");
		ourAdminSvc.saveService(service);

	}

	@Test
	public void testAddDomain() throws Exception {
		newEntityManager();

		DtoDomain domain = ourAdminSvc.addDomain(new DtoDomain("domain_id", "domain_name"));
		newEntityManager();

		assertEquals("domain_id", domain.getId());
		assertEquals("domain_name", domain.getName());
		assertFalse(domain.isStatsInitialized());
	}

	@Test(expected = PersistenceException.class)
	public void testAddDomainDuplicate() throws Exception {
		newEntityManager();

		DtoDomain domain = new DtoDomain("domain_id2", "domain_name");
		ourAdminSvc.addDomain(domain);
		ourAdminSvc.addDomain(domain);

	}

	@Test
	public void testAddMonitorRuleActive() throws Exception {

		newEntityManager();

		DtoDomain d1 = ourAdminSvc.addDomain(new DtoDomain("asv_did", "asv_did"));
		GService d1s1 = ourAdminSvc.addService(d1.getPid(), createTestService());
		ourDao.getOrCreateAuthenticationHostLocalDatabase("AUTHHOST");

		newEntityManager();

		DtoServiceVersionSoap11 d1s1v1 = new DtoServiceVersionSoap11();
		d1s1v1.setActive(true);
		d1s1v1.setId("ASV_SV1");
		d1s1v1.setName("ASV_SV1_Name");
		d1s1v1.setWsdlLocation(HTTP_FOO);
		d1s1v1.setHttpClientConfigPid(ourAdminSvc.getDefaultHttpClientConfigPid());
		d1s1v1 = ourAdminSvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v1, new ArrayList<GResource>());

		DtoServiceVersionSoap11 d1s1v2 = new DtoServiceVersionSoap11();
		d1s1v2.setActive(true);
		d1s1v2.setId("ASV_SV2");
		d1s1v2.setName("ASV_SV2_Name");
		d1s1v2.setWsdlLocation(HTTP_FOO);
		d1s1v2.setHttpClientConfigPid(ourAdminSvc.getDefaultHttpClientConfigPid());
		d1s1v2 = ourAdminSvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v2, new ArrayList<GResource>());

		newEntityManager();

		DtoLibraryMessage msg1 = new DtoLibraryMessage();
		msg1.setAppliesToServiceVersionPids(d1s1v1.getPid());
		msg1.setContentType("text/xml");
		msg1.setDescription("desc1");
		msg1.setMessage("message text1");
		ourAdminSvc.saveLibraryMessage(msg1);

		DtoLibraryMessage msg2 = new DtoLibraryMessage();
		msg2.setAppliesToServiceVersionPids(d1s1v1.getPid());
		msg2.setContentType("text/xml");
		msg2.setDescription("desc2");
		msg2.setMessage("message text2");
		ourAdminSvc.saveLibraryMessage(msg2);

		newEntityManager();

		List<DtoLibraryMessage> msgs = new ArrayList<DtoLibraryMessage>(ourAdminSvc.getLibraryMessages(HierarchyEnum.VERSION, d1s1v1.getPid(), false));
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

		ourAdminSvc.saveMonitorRule(rule);

		newEntityManager();

		/*
		 * Add a second active check
		 */

		GMonitorRuleList ruleList = ourAdminSvc.loadMonitorRuleList();
		assertEquals(1, ruleList.size());

		rule = (DtoMonitorRuleActive) ruleList.get(0);
		assertEquals(1, rule.getCheckList().size());

		rule.getCheckList().add(new DtoMonitorRuleActiveCheck());
		rule.getCheckList().get(1).setCheckFrequencyNum(6);
		rule.getCheckList().get(1).setCheckFrequencyUnit(ThrottlePeriodEnum.MINUTE);
		rule.getCheckList().get(1).setMessagePid(msgs.get(1).getPid());
		rule.getCheckList().get(1).setServiceVersionPid(d1s1v2.getPid());
		rule.getCheckList().get(1).setExpectResponseType(ResponseTypeEnum.SUCCESS);

		ourAdminSvc.saveMonitorRule(rule);

		newEntityManager();

		ruleList = ourAdminSvc.loadMonitorRuleList();
		assertEquals(1, ruleList.size());

		rule = (DtoMonitorRuleActive) ruleList.get(0);
		assertEquals(2, rule.getCheckList().size());
		assertEquals(5, rule.getCheckList().get(0).getCheckFrequencyNum());
		assertEquals(6, rule.getCheckList().get(1).getCheckFrequencyNum());

		ourTxTemplate.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				ourSvcReg.reloadRegistryFromDatabase();

				Collection<BasePersServiceVersion> svcVerList = ourSvcReg.getAllDomains().iterator().next().getServices().iterator().next().getVersions();
				assertEquals(2, svcVerList.size());
				Iterator<BasePersServiceVersion> iterator = svcVerList.iterator();
				BasePersServiceVersion v1 = iterator.next();
				BasePersServiceVersion v2 = iterator.next();
				assertEquals(1, v1.getActiveChecks().size());
				assertEquals(1, v2.getActiveChecks().size());
			}
		});

	}

	@Test
	public void testAddMonitorRulePassive() throws Exception {

		newEntityManager();

		DtoDomain d1 = ourAdminSvc.addDomain(new DtoDomain("asv_did", "asv_did"));
		GService d1s1 = ourAdminSvc.addService(d1.getPid(), createTestService());
		ourDao.getOrCreateAuthenticationHostLocalDatabase("AUTHHOST");

		newEntityManager();

		DtoServiceVersionSoap11 d1s1v1 = new DtoServiceVersionSoap11();
		d1s1v1.setActive(true);
		d1s1v1.setId("ASV_SV1");
		d1s1v1.setName("ASV_SV1_Name");
		d1s1v1.setWsdlLocation(HTTP_FOO);
		d1s1v1.setHttpClientConfigPid(ourAdminSvc.getDefaultHttpClientConfigPid());
		d1s1v1 = ourAdminSvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v1, new ArrayList<GResource>());

		newEntityManager();

		assertEquals(0, ourAdminSvc.loadMonitorRuleList().size());

		newEntityManager();

		GMonitorRulePassive rule = new GMonitorRulePassive();
		rule.setPassiveFireIfAllBackingUrlsAreUnavailable(true);
		rule.setPassiveFireForBackingServiceLatencyIsAboveMillis(100);
		rule.applyTo(d1, true);
		rule.applyTo(d1, d1s1, true);
		rule.getNotifyEmailContacts().add("foo@foo.com");
		rule.getNotifyEmailContacts().add("bar@bar.com");

		ourAdminSvc.saveMonitorRule(rule);

		newEntityManager();

		GMonitorRuleList rules = ourAdminSvc.loadMonitorRuleList();
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

		ourAdminSvc.saveMonitorRule(rule);

		newEntityManager();

		rules = ourAdminSvc.loadMonitorRuleList();
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

		final DtoDomain d1 = ourAdminSvc.addDomain(new DtoDomain("asv_did", "asv_did"));
		GService d1s1 = ourAdminSvc.addService(d1.getPid(), createTestService());

		newEntityManager();

		DtoServiceVersionSoap11 d1s1v1 = new DtoServiceVersionSoap11();
		d1s1v1.setActive(true);
		d1s1v1.setId("ASV_SV1");
		d1s1v1.setName("ASV_SV1_Name");
		d1s1v1.setWsdlLocation(HTTP_FOO);
		d1s1v1.setHttpClientConfigPid(ourAdminSvc.getDefaultHttpClientConfigPid());

		List<GResource> resources = new ArrayList<GResource>();
		resources.add(new GResource(HTTP_FOO, "text/xml", "contents1"));
		resources.add(new GResource(HTTP_BAR, "text/xml", "contents2"));

		d1s1v1.getUrlList().add(new GServiceVersionUrl("url1", HTTP_URL1));
		d1s1v1.getUrlList().add(new GServiceVersionUrl("url2", HTTP_URL2));

		d1s1v1 = ourAdminSvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v1, resources);

		newEntityManager();

		ourTxTemplate.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				PersDomain pDomain = ourDao.getDomainByPid(d1.getPid());
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
		});

	}

	@Test(expected = PersistenceException.class)
	public void testAddServiceWithDuplicates() throws Exception {
		newEntityManager();

		DtoDomain domain = ourAdminSvc.addDomain(new DtoDomain("domain_id4", "domain_name"));
		newEntityManager();

		ourAdminSvc.addService(domain.getPid(), createTestService());

		newEntityManager();

		ourAdminSvc.addService(domain.getPid(), createTestService());
	}

	@Test
	public void testDeleteDomain() throws Exception {

		createEverything();

		long pid = ourAdminSvc.getDomainPid("asv_did");
		assertTrue(pid > 0);

		DtoDomainList newList = ourAdminSvc.deleteDomain(pid);
		assertEquals(0, newList.size());

		newEntityManager();

		DtoDomain domain = ourAdminSvc.getDomainByPid(pid);
		assertNull(domain);

	}

	@Test
	public void testDeleteServiceVersion() throws Exception {
		newEntityManager();

		DtoDomain d1 = ourAdminSvc.addDomain(new DtoDomain("asv_did", "asv_did"));
		GService d1s1 = ourAdminSvc.addService(d1.getPid(), createTestService());

		newEntityManager();

		DtoServiceVersionSoap11 d1s1v1 = new DtoServiceVersionSoap11();
		d1s1v1.setActive(true);
		d1s1v1.setId("ASV_SV1");
		d1s1v1.setName("ASV_SV1_Name");
		d1s1v1.setWsdlLocation(HTTP_FOO);
		d1s1v1.setHttpClientConfigPid(ourAdminSvc.getDefaultHttpClientConfigPid());

		List<GResource> resources = new ArrayList<GResource>();
		resources.add(new GResource(HTTP_FOO, "text/xml", "contents1"));
		resources.add(new GResource(HTTP_BAR, "text/xml", "contents2"));

		d1s1v1.getUrlList().add(new GServiceVersionUrl("url1", HTTP_URL1));
		d1s1v1.getUrlList().add(new GServiceVersionUrl("url2", HTTP_URL2));

		d1s1v1 = ourAdminSvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v1, resources);

		newEntityManager();

		ModelUpdateResponse model = ourAdminSvc.loadModelUpdate(new ModelUpdateRequest());
		Collection<BaseDtoServiceVersion> versions = model.getDomainList().getDomainByPid(d1.getPid()).getServiceList().get(0).getAllServiceVersions();

		assertEquals(1, versions.size());

		newEntityManager();

		long pid = d1s1v1.getPid();
		ourAdminSvc.deleteServiceVersion(pid);

		newEntityManager();

		BasePersServiceVersion ver = ourDao.getServiceVersionByPid(pid);
		assertNull(ver);

		model = ourAdminSvc.loadModelUpdate(new ModelUpdateRequest());
		versions = model.getDomainList().getDomainByPid(d1.getPid()).getServiceList().get(0).getAllServiceVersions();

		if (versions.size() > 0) {
			BaseDtoServiceVersion ver2 = versions.iterator().next();
			fail(ver2.toString());
		}

	}

	@Test
	public void testDeleteUser() throws Exception {

		createEverything();

		GUser user = new GUser();
		user.setAuthHostPid(ourAdminSvc.getDefaultHttpClientConfigPid());
		user.setUsername("aaaaaaaa");
		user.setChangePassword("123");
		GUser newUser = ourAdminSvc.saveUser(user);

		GPartialUserList users = ourAdminSvc.loadUsers(new PartialUserListRequest());
		assertEquals(3, users.size()); // one was already created by
										// createEverything()

		ourAdminSvc.deleteUser(newUser.getPid());

		users = ourAdminSvc.loadUsers(new PartialUserListRequest());
		assertEquals(2, users.size());

	}

	@Test
	public void testLibrary() throws Exception {
		final Holder<Long> d0pid = new Holder<Long>();
		final Holder<Long> v0pid = new Holder<Long>();
		final Holder<Long> s0pid = new Holder<Long>();
		final Holder<Long> v1pid = new Holder<Long>();

		ourTxTemplate.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				try {
					PersDomain domain = ourDao.getOrCreateDomainWithId("DOMAIN_ID");
					PersService service = ourDao.getOrCreateServiceWithId(domain, "SERVICE_ID");
					PersServiceVersionSoap11 version0 = (PersServiceVersionSoap11) ourDao.getOrCreateServiceVersionWithId(service, "VersionId0", ServiceProtocolEnum.SOAP11);
					PersServiceVersionSoap11 version1 = (PersServiceVersionSoap11) ourDao.getOrCreateServiceVersionWithId(service, "VersionId1", ServiceProtocolEnum.SOAP11);
					d0pid.value = domain.getPid();
					v0pid.value = version0.getPid();
					v1pid.value = version1.getPid();
					s0pid.value = service.getPid();
				} catch (ProcessingException e) {
					throw new Error(e);
				}

			}
		});

		DtoLibraryMessage m0 = new DtoLibraryMessage();
		m0.setAppliesToServiceVersionPids(v0pid.value);
		m0.setContentType("ct0");
		m0.setDescription("desc0");
		m0.setMessage("m0");
		ourAdminSvc.saveLibraryMessage(m0);

		DtoLibraryMessage m1 = new DtoLibraryMessage();
		m1.setAppliesToServiceVersionPids(v1pid.value);
		m1.setContentType("ct1");
		m1.setDescription("desc1");
		m1.setMessage("m1");
		ourAdminSvc.saveLibraryMessage(m1);

		newEntityManager();

		Collection<DtoLibraryMessage> msgs = ourAdminSvc.getLibraryMessages(HierarchyEnum.VERSION, v0pid.value, true);
		assertEquals(1, msgs.size());

		final DtoLibraryMessage message = msgs.iterator().next();
		assertEquals("ct0", message.getContentType());
		assertEquals("desc0", message.getDescription());
		assertEquals("m0", message.getMessage());

		msgs = ourAdminSvc.getLibraryMessages(HierarchyEnum.VERSION, v1pid.value, true);
		assertEquals(1, msgs.size());

		message.setAppliesToServiceVersionPids(v0pid.value, v1pid.value);

		ourAdminSvc.saveLibraryMessage(message);

		newEntityManager();

		msgs = ourAdminSvc.getLibraryMessages(HierarchyEnum.VERSION, v1pid.value, true);
		assertEquals(2, msgs.size());
		msgs = ourAdminSvc.getLibraryMessages(HierarchyEnum.SERVICE, s0pid.value, true);
		assertEquals(2, msgs.size());

		message.setAppliesToServiceVersionPids(v1pid.value);

		ourAdminSvc.saveLibraryMessage(message);

		ourTxTemplate.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				PersLibraryMessage pm = ourDao.getLibraryMessageByPid(message.getPid());
				assertEquals(1, pm.getAppliesTo().size());
			}
		});

		msgs = ourAdminSvc.getLibraryMessages(HierarchyEnum.VERSION, v0pid.value, true);
		assertEquals(0, msgs.size());
		msgs = ourAdminSvc.getLibraryMessages(HierarchyEnum.VERSION, v1pid.value, true);
		assertEquals(2, msgs.size());

		msgs = ourAdminSvc.getLibraryMessages(HierarchyEnum.SERVICE, s0pid.value, true);
		assertEquals(2, msgs.size());

		msgs = ourAdminSvc.getLibraryMessages(HierarchyEnum.DOMAIN, d0pid.value, true);
		assertEquals(2, msgs.size());

		msgs = ourAdminSvc.loadLibraryMessages();
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
		ModelUpdateResponse modelResp = ourAdminSvc.loadModelUpdate(request);
		domain = modelResp.getDomainList().get(0);
		testLoad60MinuteStats_CheckStats(domain);

		// Service
		request = new ModelUpdateRequest();
		request.addServiceToLoadStats(svc.getPid());
		modelResp = ourAdminSvc.loadModelUpdate(request);
		domain = modelResp.getDomainList().get(0);
		svc = domain.getServiceList().get(0);
		testLoad60MinuteStats_CheckStats(svc);

		// Service Version
		request = new ModelUpdateRequest();
		request.addVersionToLoadStats(svcVer.getPid());
		modelResp = ourAdminSvc.loadModelUpdate(request);
		domain = modelResp.getDomainList().get(0);
		svc = domain.getServiceList().get(0);
		svcVer = svc.getVersionList().get(0);
		testLoad60MinuteStats_CheckStats(svcVer);

		// Try again.. should we clear cache?

		request = new ModelUpdateRequest();
		request.addDomainToLoadStats(domain.getPid());
		request.addServiceToLoadStats(svc.getPid());
		request.addVersionToLoadStats(svcVer.getPid());
		modelResp = ourAdminSvc.loadModelUpdate(request);

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

		DtoDomain d1 = ourAdminSvc.addDomain(new DtoDomain("asv_did", "asv_did"));
		GService d1s1 = ourAdminSvc.addService(d1.getPid(), createTestService());
		ourDao.getOrCreateAuthenticationHostLocalDatabase("AUTHHOST");

		newEntityManager();

		DtoServiceVersionSoap11 d1s1v1 = new DtoServiceVersionSoap11();
		d1s1v1.setActive(true);
		d1s1v1.setId("ASV_SV1");
		d1s1v1.setName("ASV_SV1_Name");
		d1s1v1.setWsdlLocation(HTTP_FOO);
		d1s1v1.setHttpClientConfigPid(ourAdminSvc.getDefaultHttpClientConfigPid());
		d1s1v1 = ourAdminSvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v1, new ArrayList<GResource>());

		newEntityManager();

		// Add one

		d1s1v1 = (DtoServiceVersionSoap11) ourAdminSvc.loadServiceVersion(d1s1v1.getPid()).getServiceVersion();
		GWsSecUsernameTokenClientSecurity cli = new GWsSecUsernameTokenClientSecurity();
		cli.setUsername("un0");
		cli.setPassword("pw0");
		d1s1v1.getClientSecurityList().add(cli);

		ourAdminSvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v1, new ArrayList<GResource>());

		newEntityManager();

		d1s1v1 = (DtoServiceVersionSoap11) ourAdminSvc.loadServiceVersion(d1s1v1.getPid()).getServiceVersion();
		ourAdminSvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v1, new ArrayList<GResource>());

		newEntityManager();

		// Add a second

		d1s1v1 = (DtoServiceVersionSoap11) ourAdminSvc.loadServiceVersion(d1s1v1.getPid()).getServiceVersion();
		cli = new GWsSecUsernameTokenClientSecurity();
		cli.setUsername("un1");
		cli.setPassword("pw1");
		d1s1v1.getClientSecurityList().add(cli);

		ourAdminSvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v1, new ArrayList<GResource>());

		newEntityManager();

		d1s1v1 = (DtoServiceVersionSoap11) ourAdminSvc.loadServiceVersion(d1s1v1.getPid()).getServiceVersion();
		assertEquals(2, d1s1v1.getClientSecurityList().size());
		assertEquals("un0", ((GWsSecUsernameTokenClientSecurity) d1s1v1.getClientSecurityList().get(0)).getUsername());
		assertEquals("pw0", ((GWsSecUsernameTokenClientSecurity) d1s1v1.getClientSecurityList().get(0)).getPassword());
		assertEquals("un1", ((GWsSecUsernameTokenClientSecurity) d1s1v1.getClientSecurityList().get(1)).getUsername());
		assertEquals("pw1", ((GWsSecUsernameTokenClientSecurity) d1s1v1.getClientSecurityList().get(1)).getPassword());

		// Remove one

		d1s1v1.getClientSecurityList().remove(d1s1v1.getClientSecurityList().get(0));
		ourAdminSvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v1, new ArrayList<GResource>());

		newEntityManager();

		d1s1v1 = (DtoServiceVersionSoap11) ourAdminSvc.loadServiceVersion(d1s1v1.getPid()).getServiceVersion();
		assertEquals(1, d1s1v1.getClientSecurityList().size());
		assertEquals("un1", ((GWsSecUsernameTokenClientSecurity) d1s1v1.getClientSecurityList().get(0)).getUsername());
		assertEquals("pw1", ((GWsSecUsernameTokenClientSecurity) d1s1v1.getClientSecurityList().get(0)).getPassword());

	}

	@Test
	public void testLoadAndSaveSvcVerPropertyCaptures() throws Exception {

		newEntityManager();

		DtoDomain d1 = ourAdminSvc.addDomain(new DtoDomain("asv_did", "asv_did"));
		GService d1s1 = ourAdminSvc.addService(d1.getPid(), createTestService());
		ourDao.getOrCreateAuthenticationHostLocalDatabase("AUTHHOST");

		newEntityManager();

		DtoServiceVersionSoap11 d1s1v1 = new DtoServiceVersionSoap11();
		d1s1v1.setActive(true);
		d1s1v1.setId("ASV_SV1");
		d1s1v1.setName("ASV_SV1_Name");
		d1s1v1.setWsdlLocation(HTTP_FOO);
		d1s1v1.setHttpClientConfigPid(ourAdminSvc.getDefaultHttpClientConfigPid());
		d1s1v1 = ourAdminSvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v1, new ArrayList<GResource>());

		newEntityManager();

		// Add one

		d1s1v1 = (DtoServiceVersionSoap11) ourAdminSvc.loadServiceVersion(d1s1v1.getPid()).getServiceVersion();
		DtoPropertyCapture cap1 = new DtoPropertyCapture();
		cap1.setPropertyName("cap1");
		cap1.setXpathExpression("//cap1");
		d1s1v1.getPropertyCaptures().add(cap1);

		ourAdminSvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v1, new ArrayList<GResource>());

		newEntityManager();

		d1s1v1 = (DtoServiceVersionSoap11) ourAdminSvc.loadServiceVersion(d1s1v1.getPid()).getServiceVersion();
		assertEquals(1, d1s1v1.getPropertyCaptures().size());
		assertEquals("cap1", d1s1v1.getPropertyCaptures().iterator().next().getPropertyName());
		ourAdminSvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v1, new ArrayList<GResource>());
		d1s1v1 = (DtoServiceVersionSoap11) ourAdminSvc.loadServiceVersion(d1s1v1.getPid()).getServiceVersion();
		assertEquals(1, d1s1v1.getPropertyCaptures().size());
		assertEquals("cap1", d1s1v1.getPropertyCaptures().iterator().next().getPropertyName());

		newEntityManager();

		// Add a second

		d1s1v1 = (DtoServiceVersionSoap11) ourAdminSvc.loadServiceVersion(d1s1v1.getPid()).getServiceVersion();
		DtoPropertyCapture cap2 = new DtoPropertyCapture();
		cap2.setPropertyName("cap2");
		cap2.setXpathExpression("//cap2");
		d1s1v1.getPropertyCaptures().add(cap2);

		ourAdminSvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v1, new ArrayList<GResource>());

		newEntityManager();

		d1s1v1 = (DtoServiceVersionSoap11) ourAdminSvc.loadServiceVersion(d1s1v1.getPid()).getServiceVersion();
		assertEquals(2, d1s1v1.getPropertyCaptures().size());
		assertEquals("cap1", d1s1v1.getPropertyCaptures().get(0).getPropertyName());
		assertEquals("cap2", d1s1v1.getPropertyCaptures().get(1).getPropertyName());

		// Remove one

		d1s1v1.getPropertyCaptures().remove(d1s1v1.getPropertyCaptures().get(0));
		ourAdminSvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v1, new ArrayList<GResource>());

		newEntityManager();

		d1s1v1 = (DtoServiceVersionSoap11) ourAdminSvc.loadServiceVersion(d1s1v1.getPid()).getServiceVersion();
		assertEquals(1, d1s1v1.getPropertyCaptures().size());
		assertEquals("cap2", d1s1v1.getPropertyCaptures().get(0).getPropertyName());

	}

	@Test
	public void testLoadAndSaveSvcVerThrottle() throws Exception {

		newEntityManager();

		DtoDomain d1 = ourAdminSvc.addDomain(new DtoDomain("asv_did", "asv_did"));
		GService d1s1 = ourAdminSvc.addService(d1.getPid(), createTestService());
		ourDao.getOrCreateAuthenticationHostLocalDatabase("AUTHHOST");

		newEntityManager();

		DtoServiceVersionSoap11 d1s1v1 = new DtoServiceVersionSoap11();
		d1s1v1.setActive(true);
		d1s1v1.setId("ASV_SV1");
		d1s1v1.setName("ASV_SV1_Name");
		d1s1v1.setWsdlLocation(HTTP_FOO);
		d1s1v1.setHttpClientConfigPid(ourAdminSvc.getDefaultHttpClientConfigPid());
		d1s1v1 = ourAdminSvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v1, new ArrayList<GResource>());

		newEntityManager();

		// Add one

		d1s1v1 = (DtoServiceVersionSoap11) ourAdminSvc.loadServiceVersion(d1s1v1.getPid()).getServiceVersion();
		assertNull(d1s1v1.getThrottle());

		DtoServiceVersionThrottle dtoThrottle = new DtoServiceVersionThrottle();
		dtoThrottle.setApplyPerUser(true);
		dtoThrottle.setThrottleMaxRequests(5);
		dtoThrottle.setThrottlePeriod(ThrottlePeriodEnum.SECOND);
		d1s1v1.setThrottle(dtoThrottle);

		ourAdminSvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v1, new ArrayList<GResource>());

		newEntityManager();

		d1s1v1 = (DtoServiceVersionSoap11) ourAdminSvc.loadServiceVersion(d1s1v1.getPid()).getServiceVersion();
		assertNotNull(d1s1v1.getThrottle());
		assertEquals(true, d1s1v1.getThrottle().isApplyPerUser());
		assertEquals(5, d1s1v1.getThrottle().getThrottleMaxRequests().intValue());
		assertEquals(ThrottlePeriodEnum.SECOND, d1s1v1.getThrottle().getThrottlePeriod());

		newEntityManager();

		// Modify one

		d1s1v1 = (DtoServiceVersionSoap11) ourAdminSvc.loadServiceVersion(d1s1v1.getPid()).getServiceVersion();
		assertNotNull(d1s1v1.getThrottle());

		dtoThrottle = d1s1v1.getThrottle();
		dtoThrottle.setApplyPerUser(false);
		dtoThrottle.setThrottleMaxRequests(6);
		dtoThrottle.setThrottlePeriod(ThrottlePeriodEnum.SECOND);
		d1s1v1.setThrottle(dtoThrottle);

		ourAdminSvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v1, new ArrayList<GResource>());

		newEntityManager();

		d1s1v1 = (DtoServiceVersionSoap11) ourAdminSvc.loadServiceVersion(d1s1v1.getPid()).getServiceVersion();
		assertNotNull(d1s1v1.getThrottle());
		assertEquals(false, d1s1v1.getThrottle().isApplyPerUser());
		assertEquals(6, d1s1v1.getThrottle().getThrottleMaxRequests().intValue());
		assertEquals(ThrottlePeriodEnum.SECOND, d1s1v1.getThrottle().getThrottlePeriod());

		newEntityManager();

		// Remove it

		d1s1v1 = (DtoServiceVersionSoap11) ourAdminSvc.loadServiceVersion(d1s1v1.getPid()).getServiceVersion();
		d1s1v1.setThrottle(null);
		ourAdminSvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v1, new ArrayList<GResource>());

		newEntityManager();

		d1s1v1 = (DtoServiceVersionSoap11) ourAdminSvc.loadServiceVersion(d1s1v1.getPid()).getServiceVersion();
		assertNull(d1s1v1.getThrottle());

	}

	@Test
	public void testLoadAndSaveSvcVerJsonRpc20() throws Exception {

		newEntityManager();

		DtoDomain d1 = ourAdminSvc.addDomain(new DtoDomain("asv_did", "asv_did"));
		GService d1s1 = ourAdminSvc.addService(d1.getPid(), createTestService());

		newEntityManager();

		DtoServiceVersionJsonRpc20 d1s1v1 = new DtoServiceVersionJsonRpc20();
		d1s1v1.setActive(true);
		d1s1v1.setId("ASV_SV1");
		d1s1v1.setName("ASV_SV1_Name");
		d1s1v1.setHttpClientConfigPid(ourAdminSvc.getDefaultHttpClientConfigPid());

		List<GResource> resources = new ArrayList<GResource>();
		resources.add(new GResource(HTTP_FOO, "text/xml", "contents1"));
		resources.add(new GResource(HTTP_BAR, "text/xml", "contents2"));

		d1s1v1.getUrlList().add(new GServiceVersionUrl("url1", HTTP_URL1));
		d1s1v1.getUrlList().add(new GServiceVersionUrl("url2", HTTP_URL2));

		d1s1v1 = ourAdminSvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v1, resources);

		newEntityManager();

		GSoap11ServiceVersionAndResources copy = ourAdminSvc.loadServiceVersion(d1s1v1.getPid());
		DtoServiceVersionJsonRpc20 svcVer = (DtoServiceVersionJsonRpc20) copy.getServiceVersion();
		assertEquals("ASV_SV1", svcVer.getId());
		assertEquals(2, copy.getResource().size());

		svcVer.setId("2.0");

		newEntityManager();

		ourAdminSvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), svcVer, copy.getResource());

		newEntityManager();

		copy = ourAdminSvc.loadServiceVersion(d1s1v1.getPid());
		svcVer = (DtoServiceVersionJsonRpc20) copy.getServiceVersion();
		assertEquals("2.0", svcVer.getId());
		assertEquals(2, copy.getResource().size());

	}

	@Test
	public void testLoadAndSaveSvcVerMethods() throws Exception {

		newEntityManager();

		DtoDomain d1 = ourAdminSvc.addDomain(new DtoDomain("asv_did", "asv_did"));
		GService d1s1 = ourAdminSvc.addService(d1.getPid(), createTestService());

		newEntityManager();

		DtoServiceVersionSoap11 d1s1v1 = new DtoServiceVersionSoap11();
		d1s1v1.setActive(true);
		d1s1v1.setId("ASV_SV1");
		d1s1v1.setName("ASV_SV1_Name");
		d1s1v1.setWsdlLocation(HTTP_FOO);
		d1s1v1.setHttpClientConfigPid(ourAdminSvc.getDefaultHttpClientConfigPid());
		d1s1v1 = ourAdminSvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v1, new ArrayList<GResource>());

		newEntityManager();

		d1s1v1 = (DtoServiceVersionSoap11) ourAdminSvc.loadServiceVersion(d1s1v1.getPid()).getServiceVersion();
		GServiceMethod method = new GServiceMethod();
		method.setName("123");
		d1s1v1.getMethodList().add(method);

		ourAdminSvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v1, new ArrayList<GResource>());

		newEntityManager();

		d1s1v1 = (DtoServiceVersionSoap11) ourAdminSvc.loadServiceVersion(d1s1v1.getPid()).getServiceVersion();
		ourAdminSvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v1, new ArrayList<GResource>());

		newEntityManager();

		ourSvcReg.reloadRegistryFromDatabase();

		newEntityManager();

		ModelUpdateResponse response = ourAdminSvc.loadModelUpdate(new ModelUpdateRequest());
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
		ourAdminSvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v1, new ArrayList<GResource>());

		newEntityManager();

		response = ourAdminSvc.loadModelUpdate(new ModelUpdateRequest());
		assertEquals(1, response.getDomainList().size());
		assertEquals(1, response.getDomainList().get(0).getServiceList().size());
		assertEquals(1, response.getDomainList().get(0).getServiceList().get(0).getVersionList().size());
		assertEquals(1, response.getDomainList().get(0).getServiceList().get(0).getVersionList().get(0).getMethodList().size());
		assertEquals(oldMethod.getPid(), response.getDomainList().get(0).getServiceList().get(0).getVersionList().get(0).getMethodList().get(0).getPid());

	}

	@Test
	public void testLoadAndSaveSvcVerServerSecurity() throws Exception {

		newEntityManager();

		DtoDomain d1 = ourAdminSvc.addDomain(new DtoDomain("asv_did", "asv_did"));
		GService d1s1 = ourAdminSvc.addService(d1.getPid(), createTestService());

		DtoAuthenticationHostLocalDatabase authHost = new DtoAuthenticationHostLocalDatabase();
		authHost.setModuleId("AUTHHOST");
		authHost.setModuleName("AUTHHOST");
		BaseDtoAuthenticationHost auth = ourAdminSvc.saveAuthenticationHost(authHost).getAuthHostById("AUTHHOST");

		authHost = new DtoAuthenticationHostLocalDatabase();
		authHost.setModuleId("AUTHHOST2");
		authHost.setModuleName("AUTHHOST2");
		BaseDtoAuthenticationHost auth2 = ourAdminSvc.saveAuthenticationHost(authHost).getAuthHostById("AUTHHOST2");

		newEntityManager();

		DtoServiceVersionSoap11 d1s1v1 = new DtoServiceVersionSoap11();
		d1s1v1.setActive(true);
		d1s1v1.setId("ASV_SV1");
		d1s1v1.setName("ASV_SV1_Name");
		d1s1v1.setWsdlLocation(HTTP_FOO);
		d1s1v1.setHttpClientConfigPid(ourAdminSvc.getDefaultHttpClientConfigPid());
		d1s1v1 = ourAdminSvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v1, new ArrayList<GResource>());

		newEntityManager();

		// Add one

		d1s1v1 = (DtoServiceVersionSoap11) ourAdminSvc.loadServiceVersion(d1s1v1.getPid()).getServiceVersion();
		GWsSecServerSecurity cli = new GWsSecServerSecurity();
		cli.setAuthHostPid(auth.getPid());
		d1s1v1.getServerSecurityList().add(cli);

		ourAdminSvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v1, new ArrayList<GResource>());

		newEntityManager();

		d1s1v1 = (DtoServiceVersionSoap11) ourAdminSvc.loadServiceVersion(d1s1v1.getPid()).getServiceVersion();
		ourAdminSvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v1, new ArrayList<GResource>());

		newEntityManager();

		// Add a second

		d1s1v1 = (DtoServiceVersionSoap11) ourAdminSvc.loadServiceVersion(d1s1v1.getPid()).getServiceVersion();
		cli = new GWsSecServerSecurity();
		cli.setAuthHostPid(auth2.getPid());
		d1s1v1.getServerSecurityList().add(cli);

		ourAdminSvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v1, new ArrayList<GResource>());

		newEntityManager();

		d1s1v1 = (DtoServiceVersionSoap11) ourAdminSvc.loadServiceVersion(d1s1v1.getPid()).getServiceVersion();
		assertEquals(2, d1s1v1.getServerSecurityList().size());
		assertEquals(auth.getPid(), d1s1v1.getServerSecurityList().get(0).getAuthHostPid());
		assertEquals(auth2.getPid(), d1s1v1.getServerSecurityList().get(1).getAuthHostPid());

		// Remove one

		ourLog.info("Removing sec with PID {} but keeping {}", d1s1v1.getServerSecurityList().get(0).getPid(), d1s1v1.getServerSecurityList().get(1).getPid());

		d1s1v1.getServerSecurityList().remove(d1s1v1.getServerSecurityList().get(0));
		ourAdminSvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v1, new ArrayList<GResource>());

		newEntityManager();

		d1s1v1 = (DtoServiceVersionSoap11) ourAdminSvc.loadServiceVersion(d1s1v1.getPid()).getServiceVersion();
		assertEquals(1, d1s1v1.getServerSecurityList().size());
		assertEquals(auth2.getPid(), d1s1v1.getServerSecurityList().get(0).getAuthHostPid());

	}

	@Test(expected = IllegalArgumentException.class)
	public void testLoadAndSaveSvcVerServerSecurityNoAuthHost() throws Exception {

		newEntityManager();

		DtoDomain d1 = ourAdminSvc.addDomain(new DtoDomain("asv_did", "asv_did"));
		GService d1s1 = ourAdminSvc.addService(d1.getPid(), createTestService());
		ourDao.getOrCreateAuthenticationHostLocalDatabase("AUTHHOST");

		newEntityManager();

		DtoServiceVersionSoap11 d1s1v1 = new DtoServiceVersionSoap11();
		d1s1v1.setActive(true);
		d1s1v1.setId("ASV_SV1");
		d1s1v1.setName("ASV_SV1_Name");
		d1s1v1.setWsdlLocation(HTTP_FOO);
		d1s1v1.setHttpClientConfigPid(ourAdminSvc.getDefaultHttpClientConfigPid());
		d1s1v1 = ourAdminSvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v1, new ArrayList<GResource>());

		newEntityManager();

		// Add one

		d1s1v1 = (DtoServiceVersionSoap11) ourAdminSvc.loadServiceVersion(d1s1v1.getPid()).getServiceVersion();
		GWsSecServerSecurity cli = new GWsSecServerSecurity();

		// Don't set the auth host, this should mean an exception is thrown

		d1s1v1.getServerSecurityList().add(cli);

		ourAdminSvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v1, new ArrayList<GResource>());
	}

	@Test
	public void testLoadAndSaveSvcVerSoap11() throws Exception {

		newEntityManager();

		DtoDomain d1 = ourAdminSvc.addDomain(new DtoDomain("asv_did", "asv_did"));
		GService d1s1 = ourAdminSvc.addService(d1.getPid(), createTestService());

		newEntityManager();

		DtoServiceVersionSoap11 d1s1v1 = new DtoServiceVersionSoap11();
		d1s1v1.setActive(true);
		d1s1v1.setId("ASV_SV1");
		d1s1v1.setName("ASV_SV1_Name");
		d1s1v1.setWsdlLocation(HTTP_FOO);
		d1s1v1.setHttpClientConfigPid(ourAdminSvc.getDefaultHttpClientConfigPid());

		List<GResource> resources = new ArrayList<GResource>();
		resources.add(new GResource(HTTP_FOO, "text/xml", "contents1"));
		resources.add(new GResource(HTTP_BAR, "text/xml", "contents2"));

		d1s1v1.getUrlList().add(new GServiceVersionUrl("url1", HTTP_URL1));
		d1s1v1.getUrlList().add(new GServiceVersionUrl("url2", HTTP_URL2));

		d1s1v1 = ourAdminSvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v1, resources);

		newEntityManager();

		/*
		 * Putting in the same old resources, but they look new because they
		 * don't have IDs
		 */

		d1s1v1.setWsdlLocation(HTTP_BAR);
		ourAdminSvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v1, resources);

		newEntityManager();

		GSoap11ServiceVersionAndResources copy = ourAdminSvc.loadServiceVersion(d1s1v1.getPid());
		DtoServiceVersionSoap11 svcVer = (DtoServiceVersionSoap11) copy.getServiceVersion();
		assertEquals(HTTP_BAR, svcVer.getWsdlLocation());
		assertEquals(2, copy.getResource().size());

		/*
		 * Now try saving with the existing resources back in
		 */

		d1s1v1.setWsdlLocation("http://baz");
		ourAdminSvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v1, copy.getResource());

		newEntityManager();

		/*
		 * Now remove a resource
		 */

		d1s1v1.setWsdlLocation("http://baz");
		List<GResource> resource = copy.getResource();
		resource.remove(0);
		ourAdminSvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v1, resource);

		newEntityManager();

		copy = ourAdminSvc.loadServiceVersion(d1s1v1.getPid());
		svcVer = (DtoServiceVersionSoap11) copy.getServiceVersion();
		assertEquals("http://baz", svcVer.getWsdlLocation());
		assertEquals(1, copy.getResource().size());

		/*
		 * Now change the URL
		 */

		svcVer.getUrlList().get(0).setUrl(HTTP_ZZZZZZ);
		ourAdminSvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), svcVer, resource);

		newEntityManager();

		copy = ourAdminSvc.loadServiceVersion(d1s1v1.getPid());
		svcVer = (DtoServiceVersionSoap11) copy.getServiceVersion();
		assertEquals(2, svcVer.getUrlList().size());
		assertEquals("url1", svcVer.getUrlList().get(0).getId());
		assertEquals(HTTP_ZZZZZZ, svcVer.getUrlList().get(0).getUrl());

		/*
		 * Now change a URL ID
		 */

		svcVer.getUrlList().get(0).setId("aaaaa");
		ourAdminSvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), svcVer, resource);

		newEntityManager();

		copy = ourAdminSvc.loadServiceVersion(d1s1v1.getPid());
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
		persSvcVer.addResource("http:/theBody/xsdurl", "application/xml", "xsdcontents");

		PersServiceVersionUrl url = new PersServiceVersionUrl();
		url.setUrlId("url1");
		url.setUrl(HTTP_SVCURL);
		url.setServiceVersion(persSvcVer);
		persSvcVer.addUrl(url);

		PersHttpClientConfig cfg = new PersHttpClientConfig();
		cfg.setDefaults();
		// when(ourSoapInvoker.introspectServiceFromUrl(cfg,
		// "http://wsdlurl")).thenReturn(persSvcVer);

		String wsdl0body = IOUtils.toString(ResourceUtils.getURL("classpath:test_simple.wsdl"));
		String xsd0body = IOUtils.toString(ResourceUtils.getURL("classpath:basic_schema2.xsd"));
		{
		SrBeanIncomingResponse resp = new SrBeanIncomingResponse();
		resp.setCode(200);
		resp.setContentType("application/xml");
		resp.setBody(wsdl0body);
		when(ourHttpClientMock.getOneTime((PersHttpClientConfig) any(PersHttpClientConfig.class), eq("http://wsdlurl/wsdl.wsdl"))).thenReturn(resp);
		}
		{
		SrBeanIncomingResponse resp = new SrBeanIncomingResponse();
		resp.setCode(200);
		resp.setContentType("application/xml");
		resp.setBody(xsd0body);
		when(ourHttpClientMock.getOneTime((PersHttpClientConfig) any(PersHttpClientConfig.class), eq("http://wsdlurl/bar.xsd"))).thenReturn(resp);
		}
		
		GSoap11ServiceVersionAndResources verAndRes = ourAdminSvc.loadSoap11ServiceVersionFromWsdl(ver, cfg.toDto(), "http://wsdlurl/wsdl.wsdl");

		assertEquals(2, verAndRes.getResource().size());
		assertEquals("http://wsdlurl/wsdl.wsdl", verAndRes.getResource().get(0).getUrl());
		assertEquals(wsdl0body, verAndRes.getResource().get(0).getText());
		assertEquals("bar.xsd", verAndRes.getResource().get(1).getUrl());
		assertEquals(xsd0body, verAndRes.getResource().get(1).getText());
		assertEquals("http://wsdlurl/wsdl.wsdl", verAndRes.getServiceVersion().getResourcePointerList().get(0).getUrl());
		assertEquals("application/xml", verAndRes.getServiceVersion().getResourcePointerList().get(0).getType());
		assertEquals(wsdl0body.length(), verAndRes.getServiceVersion().getResourcePointerList().get(0).getSize());
		assertEquals("getActivePatientsByAttendingPhysicianId", verAndRes.getServiceVersion().getMethodList().get(0).getName());
		assertEquals("getActivePatientsByAttendingPhysicianIdExtended", verAndRes.getServiceVersion().getMethodList().get(1).getName());

		// Load stats
		newEntityManager();

		DtoDomain d0 = ourAdminSvc.addDomain(new DtoDomain("d0", "d0"));
		GService s0 = ourAdminSvc.addService(d0.getPid(), new GService("s0", "s0", true));
		verAndRes.getServiceVersion().setId("v0");
		verAndRes.getServiceVersion().setHttpClientConfigPid(ourAdminSvc.getDefaultHttpClientConfigPid());

		final BaseDtoServiceVersion newVer = ourAdminSvc.saveServiceVersion(d0.getPid(), s0.getPid(), verAndRes.getServiceVersion(), verAndRes.getResource());

		newEntityManager();

		TransactionTemplate tmpl = new TransactionTemplate(ourTxManager);
		tmpl.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				BasePersServiceVersion svcVer = ourDao.getServiceVersionByPid(newVer.getPid());
				PersServiceVersionResource res = svcVer.getResourceForUri("http://wsdlurl/wsdl.wsdl");
				ourStatsSvc.recordInvocationStaticResource(new Date(), res);
				ourStatsSvc.flushStatus();
			}
		});

		newEntityManager();

		// Now change the WSDL URL
		{
			SrBeanIncomingResponse resp = new SrBeanIncomingResponse();
			resp.setCode(200);
			resp.setContentType("text/xml");
			resp.setBody(IOUtils.toString(ResourceUtils.getURL("classpath:cdyne_weatherws.wsdl")));
			when(ourHttpClientMock.getOneTime((PersHttpClientConfig) any(PersHttpClientConfig.class), eq("http://wsdlurl2"))).thenReturn(resp);
		}
		verAndRes = ourAdminSvc.loadSoap11ServiceVersionFromWsdl((DtoServiceVersionSoap11) newVer, cfg.toDto(), "http://wsdlurl2");
		BaseDtoServiceVersion newVer2 = ourAdminSvc.saveServiceVersion(d0.getPid(), s0.getPid(), verAndRes.getServiceVersion(), verAndRes.getResource());
		assertEquals(1, newVer2.getResourcePointerList().size());

	}

	// @Test
	public void testMultipleStatsLoadsInParallel() throws Exception {
		newEntityManager();

		ourConfigSvc.getConfig();

		newEntityManager();

		PersDomain d0 = ourDao.getOrCreateDomainWithId("d0");
		PersService d0s0 = ourDao.getOrCreateServiceWithId(d0, "d0s0");
		PersServiceVersionSoap11 d0s0v0 = (PersServiceVersionSoap11) ourDao.getOrCreateServiceVersionWithId(d0s0, "d0s0v0", ServiceProtocolEnum.SOAP11);
		d0s0v0.getOrCreateAndAddMethodWithName("d0s0v0m0");
		ourDao.saveServiceVersionInNewTransaction(d0s0v0);

		newEntityManager();

		ourSvcReg.reloadRegistryFromDatabase();

		newEntityManager();

		final ModelUpdateRequest req = new ModelUpdateRequest();
		req.addServiceToLoadStats(d0s0.getPid());

		List<RetrieverThread> ts = new ArrayList<RetrieverThread>();
		for (int i = 0; i < 3; i++) {
			RetrieverThread t = new RetrieverThread(req, ourSvcReg);
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

		DtoDomain d1 = ourAdminSvc.addDomain(new DtoDomain("asv_did", "asv_did"));
		GService d1s1 = ourAdminSvc.addService(d1.getPid(), createTestService());

		newEntityManager();

		DtoServiceVersionSoap11 d1s1v1 = new DtoServiceVersionSoap11();
		d1s1v1.setActive(true);
		d1s1v1.setId("ASV_SV1");
		d1s1v1.setName("ASV_SV1_Name");
		d1s1v1.setWsdlLocation(HTTP_FOO);
		d1s1v1.setHttpClientConfigPid(ourAdminSvc.getDefaultHttpClientConfigPid());

		List<GResource> resources = new ArrayList<GResource>();
		d1s1v1 = ourAdminSvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v1, resources);

		newEntityManager();

		// PersDomain pDomain = myDao.getDomainByPid(d1.getPid());
		// Collection<BasePersServiceVersion> versions = mySvc.getAll
		ModelUpdateRequest request = new ModelUpdateRequest();
		ModelUpdateResponse model = ourAdminSvc.loadModelUpdate(request);
		assertEquals(1, model.getDomainList().getDomainByPid(d1.getPid()).getServiceList().get(0).getAllServiceVersionPids().size());

		d1s1v1.setId("newId");
		ourAdminSvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v1, resources);

		newEntityManager();

		model = ourAdminSvc.loadModelUpdate(request);
		assertEquals(1, model.getDomainList().getDomainByPid(d1.getPid()).getServiceList().get(0).getAllServiceVersionPids().size());
		assertEquals("newId", model.getDomainList().getDomainByPid(d1.getPid()).getServiceList().get(0).getAllServiceVersions().get(0).getId());

	}

	@Test
	public void testSaveConfigWithUrlBases() throws Exception {
		newEntityManager();

		DtoConfig config = ourAdminSvc.loadConfig();

		newEntityManager();

		config.getProxyUrlBases().clear();
		config.getProxyUrlBases().add(HTTP_FOO);

		config = ourAdminSvc.saveConfig(config);

		newEntityManager();

		config = ourAdminSvc.loadConfig();
		assertEquals(1, config.getProxyUrlBases().size());
		assertEquals(HTTP_FOO, config.getProxyUrlBases().get(0));

	}

	@Test
	public void testGetStatsOnEmptyObjects() throws Exception {
		ModelUpdateRequest req = new ModelUpdateRequest();
		ModelUpdateResponse resp = ourAdminSvc.loadModelUpdate(req);

		DtoDomain d1 = ourAdminSvc.addDomain(new DtoDomain("asv_did", "asv_did"));

		long d1pid = d1.getPid();
		req.addDomainToLoadStats(d1pid);
		resp = ourAdminSvc.loadModelUpdate(req);
		d1.merge(resp.getDomainList().getDomainByPid(d1pid));

		GService s1 = ourAdminSvc.addService(d1pid, new GService("s1", "s1", true));

		long s1pid = s1.getPid();
		req.addServiceToLoadStats(s1pid);
		resp = ourAdminSvc.loadModelUpdate(req);
		d1.merge(resp.getDomainList().getDomainByPid(d1pid));
		s1.merge(resp.getDomainList().getDomainByPid(d1pid).getServiceList().getServiceByPid(s1pid));

		DtoServiceVersionJsonRpc20 v1 = ourAdminSvc.saveServiceVersion(d1pid, s1pid, new DtoServiceVersionJsonRpc20("v1", "v1", ourAdminSvc.getDefaultHttpClientConfigPid()), new ArrayList<GResource>());

		long v1pid = v1.getPid();
		req.addVersionToLoadStats(v1pid);
		resp = ourAdminSvc.loadModelUpdate(req);
		d1.merge(resp.getDomainList().getDomainByPid(d1pid));
		s1.merge(resp.getDomainList().getDomainByPid(d1pid).getServiceList().getServiceByPid(s1pid));
		v1.merge(resp.getDomainList().getDomainByPid(d1pid).getServiceList().getServiceByPid(s1pid).getVersionList().getVersionByPid(v1pid));

	}

	@Test
	public void testSaveDomain() throws Exception {

		newEntityManager();

		DtoDomain d1 = ourAdminSvc.addDomain(new DtoDomain("asv_did", "asv_did"));

		newEntityManager();

		DtoDomain domain = ourAdminSvc.getDomainByPid(d1.getPid());
		assertEquals("asv_did", domain.getId());

		newEntityManager();

		d1.setId("b");
		ourAdminSvc.saveDomain(d1);

		newEntityManager();

		domain = ourAdminSvc.getDomainByPid(d1.getPid());
		assertEquals("b", domain.getId());

	}

	@Test
	public void testSaveHttpClientConfig() throws Exception {
		createEverything();
		newEntityManager();

		ModelUpdateRequest req = new ModelUpdateRequest();
		req.setLoadHttpClientConfigs(true);
		ModelUpdateResponse resp = ourAdminSvc.loadModelUpdate(req);

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
		ourAdminSvc.saveHttpClientConfig(cfg, null, null, null, null);

		newEntityManager();
		req = new ModelUpdateRequest();
		req.setLoadHttpClientConfigs(true);
		resp = ourAdminSvc.loadModelUpdate(req);
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
		ourAdminSvc.saveHttpClientConfig(cfg, null, null, null, null);

		newEntityManager();
		req = new ModelUpdateRequest();
		req.setLoadHttpClientConfigs(true);
		resp = ourAdminSvc.loadModelUpdate(req);
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
		ourAdminSvc.saveHttpClientConfig(cfg, null, null, null, null);

		newEntityManager();
		req = new ModelUpdateRequest();
		req.setLoadHttpClientConfigs(true);
		resp = ourAdminSvc.loadModelUpdate(req);
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

		DtoDomain d1 = ourAdminSvc.addDomain(new DtoDomain("asv_did1", "asv_did1"));
		DtoDomain d2 = ourAdminSvc.addDomain(new DtoDomain("asv_did2", "asv_did2"));

		GService d1s1 = ourAdminSvc.addService(d1.getPid(), createTestService());
		GService d2s1 = ourAdminSvc.addService(d2.getPid(), new GService("d2s1", "d2s1", true));

		DtoServiceVersionSoap11 d1s1v1 = new DtoServiceVersionSoap11();
		d1s1v1.setActive(true);
		d1s1v1.setId("D1S1V1");
		d1s1v1.setName("D1S1V1");
		d1s1v1.setWsdlLocation(HTTP_FOO);
		d1s1v1.setHttpClientConfigPid(ourAdminSvc.getDefaultHttpClientConfigPid());
		List<GResource> resources = new ArrayList<GResource>();
		resources.add(new GResource(HTTP_FOO, "text/xml", "contents1"));
		resources.add(new GResource(HTTP_BAR, "text/xml", "contents2"));
		d1s1v1 = ourAdminSvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v1, resources);

		DtoServiceVersionSoap11 d2s1v1 = new DtoServiceVersionSoap11();
		d2s1v1.setActive(true);
		d2s1v1.setId("D2S1V1");
		d2s1v1.setName("D2S1V1");
		d2s1v1.setWsdlLocation(HTTP_FOO);
		d2s1v1.setHttpClientConfigPid(ourAdminSvc.getDefaultHttpClientConfigPid());
		d2s1v1 = ourAdminSvc.saveServiceVersion(d2.getPid(), d2s1.getPid(), d2s1v1, resources);

		GServiceMethod d1s1v1m1 = new GServiceMethod();
		d1s1v1m1.setName("d1s1v1m1");
		d1s1v1m1 = ourAdminSvc.addServiceVersionMethod(d1s1v1.getPid(), d1s1v1m1);

		GServiceMethod d2s1v1m1 = new GServiceMethod();
		d2s1v1m1.setName("d2s1v1m1");
		d2s1v1m1 = ourAdminSvc.addServiceVersionMethod(d2s1v1.getPid(), d2s1v1m1);

		DtoAuthenticationHostLocalDatabase authHost = new DtoAuthenticationHostLocalDatabase();
		authHost.setModuleId("authHost");
		authHost.setModuleName("authHost");
		authHost = (DtoAuthenticationHostLocalDatabase) ourAdminSvc.saveAuthenticationHost(authHost).get(0);

		newEntityManager();

		GUser user = new GUser();
		user.setUsername("username");
		user.setAuthHostPid(authHost.getPid());
		user = ourAdminSvc.saveUser(user);

		assertThat(user.getPid(), Matchers.greaterThan(0L));
		newEntityManager();

		/*
		 * Add Global Permission
		 */

		assertThat(user.getGlobalPermissions(), org.hamcrest.Matchers.not(org.hamcrest.Matchers.contains(UserGlobalPermissionEnum.SUPERUSER)));
		user.addGlobalPermission(UserGlobalPermissionEnum.SUPERUSER);
		ourAdminSvc.saveUser(user);
		newEntityManager();
		user = ourAdminSvc.loadUser(user.getPid(), false);
		assertThat(user.getGlobalPermissions(), (org.hamcrest.Matchers.contains(UserGlobalPermissionEnum.SUPERUSER)));

		/*
		 * Remove Global permission
		 */

		user.removeGlobalPermission(UserGlobalPermissionEnum.SUPERUSER);
		ourAdminSvc.saveUser(user);
		newEntityManager();
		user = ourAdminSvc.loadUser(user.getPid(), false);
		assertThat(user.getGlobalPermissions(), org.hamcrest.Matchers.not(org.hamcrest.Matchers.contains(UserGlobalPermissionEnum.SUPERUSER)));

		/*
		 * Add Domain Permission
		 */

		GUserDomainPermission domPerm1 = new GUserDomainPermission();
		domPerm1.setDomainPid(d1.getPid());
		domPerm1.setAllowAllServices(true);
		user.addDomainPermission(domPerm1);
		ourAdminSvc.saveUser(user);
		newEntityManager();
		user = ourAdminSvc.loadUser(user.getPid(), false);
		assertEquals(1, user.getDomainPermissions().size());
		assertEquals(d1.getPid(), user.getDomainPermissions().get(0).getDomainPid());

		/*
		 * Add second domain permission
		 */

		GUserDomainPermission domPerm2 = new GUserDomainPermission();
		domPerm2.setDomainPid(d2.getPid());
		domPerm2.setAllowAllServices(true);
		user.addDomainPermission(domPerm2);
		ourAdminSvc.saveUser(user);
		newEntityManager();
		user = ourAdminSvc.loadUser(user.getPid(), false);
		assertEquals(2, user.getDomainPermissions().size());
		assertThat(user.getDomainPermissions().get(0).getDomainPid(), org.hamcrest.Matchers.isOneOf(d1.getPid(), d2.getPid()));
		assertThat(user.getDomainPermissions().get(1).getDomainPid(), org.hamcrest.Matchers.isOneOf(d1.getPid(), d2.getPid()));

		/*
		 * Add service permission to each domain permission
		 */

		user.getDomainPermissions().get(0).getOrCreateServicePermission(d1s1.getPid()).setAllowAllServiceVersions(true);
		user.getDomainPermissions().get(1).getOrCreateServicePermission(d2s1.getPid()).setAllowAllServiceVersions(true);
		ourAdminSvc.saveUser(user);
		newEntityManager();
		user = ourAdminSvc.loadUser(user.getPid(), false);
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
		ourAdminSvc.saveUser(user);
		newEntityManager();
		user = ourAdminSvc.loadUser(user.getPid(), false);
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
		ourAdminSvc.saveUser(user);
		newEntityManager();
		user = ourAdminSvc.loadUser(user.getPid(), false);
		assertEquals(2, user.getDomainPermissions().size());
		assertEquals(1, user.getDomainPermissions().get(0).getServicePermissions().get(0).getServiceVersionPermissions().get(0).getServiceVersionMethodPermissions().size());
		assertEquals(1, user.getDomainPermissions().get(1).getServicePermissions().get(0).getServiceVersionPermissions().get(0).getServiceVersionMethodPermissions().size());
		assertEquals(d1s1v1m1.getPid(), user.getDomainPermissions().get(0).getServicePermissions().get(0).getServiceVersionPermissions().get(0).getServiceVersionMethodPermissions().get(0).getServiceVersionMethodPid());
		assertEquals(d2s1v1m1.getPid(), user.getDomainPermissions().get(1).getServicePermissions().get(0).getServiceVersionPermissions().get(0).getServiceVersionMethodPermissions().get(0).getServiceVersionMethodPid());

		/*
		 * Remove domain permission
		 */

		user.removeDomainPermission(user.getDomainPermissions().get(0));
		ourAdminSvc.saveUser(user);
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

	private final class RetrieverThread extends Thread {
		private Exception myFailed;
		private final ModelUpdateRequest myReq;

		private RetrieverThread(ModelUpdateRequest theReq, IServiceRegistry theSvcReg) {
			myReq = theReq;
		}

		@Override
		public void run() {
			try {

				// EntityManager entityManager =
				// ourEntityManagerFactory.createEntityManager();
				// entityManager.getTransaction().begin();
				// DaoBean dao = new DaoBean();
				// dao.setEntityManager(entityManager);
				//
				// RuntimeStatusBean rs = new RuntimeStatusBean();
				// rs.setDao(dao);
				//
				// ConfigServiceBean cs = new ConfigServiceBean();
				// cs.setBroadcastSender(mock(IBroadcastSender.class));
				// cs.setDao(dao);
				//
				// RuntimeStatusQueryBean rqb = new RuntimeStatusQueryBean();
				// rqb.setConfigSvcForUnitTest(cs);
				// rqb.setStatusSvcForUnitTest(rs);
				//
				// AdminServiceBean sSvc = new AdminServiceBean();
				// sSvc.setPersSvc(dao);
				// sSvc.setConfigSvc(cs);
				// sSvc.setServiceRegistry(mySvcReg);
				// sSvc.setRuntimeStatusSvc(rs);
				// sSvc.setRuntimeStatusQuerySvcForUnitTests(rqb);
				//
				// RuntimeStatusBean statsSvc = new RuntimeStatusBean();
				// statsSvc.setDao(dao);

				ourAdminSvc.loadModelUpdate(myReq);

				// entityManager.getTransaction().commit();

			} catch (Exception e) {
				myFailed = e;
				e.printStackTrace();
				fail("" + e.getMessage());
			}
		}
	}

}
