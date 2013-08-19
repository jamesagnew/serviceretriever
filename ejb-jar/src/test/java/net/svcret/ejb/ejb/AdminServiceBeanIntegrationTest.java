package net.svcret.ejb.ejb;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.persistence.EntityManager;

import net.svcret.admin.shared.enm.ResponseTypeEnum;
import net.svcret.admin.shared.model.AuthorizationOutcomeEnum;
import net.svcret.admin.shared.model.DtoLibraryMessage;
import net.svcret.admin.shared.model.GConfig;
import net.svcret.admin.shared.model.GDomain;
import net.svcret.admin.shared.model.GLocalDatabaseAuthHost;
import net.svcret.admin.shared.model.GMonitorRulePassive;
import net.svcret.admin.shared.model.GMonitorRuleList;
import net.svcret.admin.shared.model.GResource;
import net.svcret.admin.shared.model.GService;
import net.svcret.admin.shared.model.GServiceMethod;
import net.svcret.admin.shared.model.GServiceVersionJsonRpc20;
import net.svcret.admin.shared.model.GServiceVersionUrl;
import net.svcret.admin.shared.model.GSoap11ServiceVersion;
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
import net.svcret.ejb.api.HttpRequestBean;
import net.svcret.ejb.api.HttpResponseBean;
import net.svcret.ejb.api.IBroadcastSender;
import net.svcret.ejb.api.IRuntimeStatus;
import net.svcret.ejb.api.IScheduler;
import net.svcret.ejb.api.IServiceInvokerSoap11;
import net.svcret.ejb.api.InvocationResponseResultsBean;
import net.svcret.ejb.ejb.AdminServiceBean.IWithStats;
import net.svcret.ejb.ex.ProcessingException;
import net.svcret.ejb.model.entity.BasePersAuthenticationHost;
import net.svcret.ejb.model.entity.BasePersServiceVersion;
import net.svcret.ejb.model.entity.PersAuthenticationHostLocalDatabase;
import net.svcret.ejb.model.entity.PersConfig;
import net.svcret.ejb.model.entity.PersDomain;
import net.svcret.ejb.model.entity.PersHttpClientConfig;
import net.svcret.ejb.model.entity.PersLibraryMessage;
import net.svcret.ejb.model.entity.PersService;
import net.svcret.ejb.model.entity.PersServiceVersionMethod;
import net.svcret.ejb.model.entity.PersServiceVersionStatus;
import net.svcret.ejb.model.entity.PersServiceVersionUrl;
import net.svcret.ejb.model.entity.PersUser;
import net.svcret.ejb.model.entity.soap.PersServiceVersionSoap11;

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AdminServiceBeanIntegrationTest extends BaseJpaTest {

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(AdminServiceBeanIntegrationTest.class);

	private DaoBean myDao;
	private IServiceInvokerSoap11 mySoapInvoker;
	private RuntimeStatusBean myStatsSvc;
	private AdminServiceBean mySvc;
	private SecurityServiceBean mySecSvc;
	private IBroadcastSender myBroadcastSender;
	private ServiceRegistryBean mySvcReg;
	private ConfigServiceBean myConfigSvc;
	private TransactionLoggerBean myTransactionLogSvc;

	private MonitorServiceBean myMonitorSvc;

	@After
	public void after2() {

		// newEntityManager();
		//
		// Query q = myEntityManager.createQuery("DELETE FROM PersDomain p");
		// q.executeUpdate();
		//
		// newEntityManager();
	}

	private static long ourNextPid;

	@Test
	public void testLibrary() throws ProcessingException {

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

		Collection<DtoLibraryMessage> msgs = mySvc.getLibraryMessages(HierarchyEnum.VERSION,version0.getPid(), true);
		assertEquals(1, msgs.size());

		DtoLibraryMessage message = msgs.iterator().next();
		assertEquals("ct0", message.getContentType());
		assertEquals("desc0", message.getDescription());
		assertEquals("m0", message.getMessage());

		msgs = mySvc.getLibraryMessages(HierarchyEnum.VERSION,version1.getPid(), true);
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
		
		msgs = mySvc.getLibraryMessages(HierarchyEnum.VERSION,version0.getPid(), true);
		assertEquals(0, msgs.size());
		msgs = mySvc.getLibraryMessages(HierarchyEnum.VERSION,version1.getPid(), true);
		assertEquals(2, msgs.size());
		
		msgs = mySvc.getLibraryMessages(HierarchyEnum.SERVICE,service.getPid(), true);
		assertEquals(2, msgs.size());

		msgs = mySvc.getLibraryMessages(HierarchyEnum.DOMAIN,domain.getPid(), true);
		assertEquals(2, msgs.size());

		msgs = mySvc.loadLibraryMessages();
		assertEquals(2, msgs.size());

	}

	@Test
	public void testDoWithStats() {

		PersDomain domain = new PersDomain(ourNextPid++, "d");
		PersService service = new PersService(ourNextPid++, domain, "s", "s");
		PersServiceVersionSoap11 sv = new PersServiceVersionSoap11(ourNextPid++, service, "v");
		PersServiceVersionMethod method = new PersServiceVersionMethod(ourNextPid++, sv, "m");

		PersConfig config = new PersConfig();
		config.setDefaults();
		IRuntimeStatus statusSvc = mock(IRuntimeStatus.class, DefaultAnswer.INSTANCE);
		IWithStats operator = mock(IWithStats.class);
		AdminServiceBean.doWithStatsByMinute(config, 12 * 60, statusSvc, method, operator);

	}

	@Before
	public void before2() throws SQLException, ProcessingException {
		myDao = new DaoBean();
		myBroadcastSender = mock(IBroadcastSender.class);

		myConfigSvc = new ConfigServiceBean();
		myConfigSvc.setDao(myDao);
		myConfigSvc.setBroadcastSender(myBroadcastSender);

		myStatsSvc = new RuntimeStatusBean();
		myStatsSvc.setDao(myDao);
		myStatsSvc.setConfigSvc(myConfigSvc);

		mySoapInvoker = mock(IServiceInvokerSoap11.class, new DefaultAnswer());

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
		mySvc.setServiceRegistry(mySvcReg);
		mySvc.setInvokerSoap11(mySoapInvoker);
		mySvc.setSecuritySvc(mySecSvc);
		mySvc.setMonitorSvc(myMonitorSvc);
		mySvc.setSchedulerServiceForTesting(mock(IScheduler.class));

		myTransactionLogSvc = new TransactionLoggerBean();
		myTransactionLogSvc.setDao(myDao);

		DefaultAnswer.setDesignTime();
	}

	@Test
	public void testLoadAndSaveSvcVerMethods() throws ProcessingException {

		newEntityManager();

		GDomain d1 = mySvc.addDomain("asv_did", "asv_did");
		GService d1s1 = mySvc.addService(d1.getPid(), "asv_sid", "asv_sid", true);
		PersHttpClientConfig hcc = myDao.getOrCreateHttpClientConfig("httpclient");

		newEntityManager();

		GSoap11ServiceVersion d1s1v1 = new GSoap11ServiceVersion();
		d1s1v1.setActive(true);
		d1s1v1.setId("ASV_SV1");
		d1s1v1.setName("ASV_SV1_Name");
		d1s1v1.setWsdlLocation("http://foo");
		d1s1v1.setHttpClientConfigPid(hcc.getPid());
		d1s1v1 = mySvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v1, new ArrayList<GResource>());

		newEntityManager();

		d1s1v1 = (GSoap11ServiceVersion) mySvc.loadServiceVersion(d1s1v1.getPid()).getServiceVersion();
		GServiceMethod method = new GServiceMethod();
		method.setName("123");
		d1s1v1.getMethodList().add(method);

		mySvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v1, new ArrayList<GResource>());

		newEntityManager();

		d1s1v1 = (GSoap11ServiceVersion) mySvc.loadServiceVersion(d1s1v1.getPid()).getServiceVersion();
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
	public void testAddMonitorRule() throws ProcessingException {

		newEntityManager();

		GDomain d1 = mySvc.addDomain("asv_did", "asv_did");
		GService d1s1 = mySvc.addService(d1.getPid(), "asv_sid", "asv_sid", true);
		PersHttpClientConfig hcc = myDao.getOrCreateHttpClientConfig("httpclient");
		myDao.getOrCreateAuthenticationHostLocalDatabase("AUTHHOST");

		newEntityManager();

		GSoap11ServiceVersion d1s1v1 = new GSoap11ServiceVersion();
		d1s1v1.setActive(true);
		d1s1v1.setId("ASV_SV1");
		d1s1v1.setName("ASV_SV1_Name");
		d1s1v1.setWsdlLocation("http://foo");
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
	public void testLoadAndSaveSvcVerClientSecurity() throws ProcessingException {

		newEntityManager();

		GDomain d1 = mySvc.addDomain("asv_did", "asv_did");
		GService d1s1 = mySvc.addService(d1.getPid(), "asv_sid", "asv_sid", true);
		PersHttpClientConfig hcc = myDao.getOrCreateHttpClientConfig("httpclient");
		myDao.getOrCreateAuthenticationHostLocalDatabase("AUTHHOST");

		newEntityManager();

		GSoap11ServiceVersion d1s1v1 = new GSoap11ServiceVersion();
		d1s1v1.setActive(true);
		d1s1v1.setId("ASV_SV1");
		d1s1v1.setName("ASV_SV1_Name");
		d1s1v1.setWsdlLocation("http://foo");
		d1s1v1.setHttpClientConfigPid(hcc.getPid());
		d1s1v1 = mySvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v1, new ArrayList<GResource>());

		newEntityManager();

		// Add one

		d1s1v1 = (GSoap11ServiceVersion) mySvc.loadServiceVersion(d1s1v1.getPid()).getServiceVersion();
		GWsSecUsernameTokenClientSecurity cli = new GWsSecUsernameTokenClientSecurity();
		cli.setUsername("un0");
		cli.setPassword("pw0");
		d1s1v1.getClientSecurityList().add(cli);

		mySvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v1, new ArrayList<GResource>());

		newEntityManager();

		d1s1v1 = (GSoap11ServiceVersion) mySvc.loadServiceVersion(d1s1v1.getPid()).getServiceVersion();
		mySvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v1, new ArrayList<GResource>());

		newEntityManager();

		// Add a second

		d1s1v1 = (GSoap11ServiceVersion) mySvc.loadServiceVersion(d1s1v1.getPid()).getServiceVersion();
		cli = new GWsSecUsernameTokenClientSecurity();
		cli.setUsername("un1");
		cli.setPassword("pw1");
		d1s1v1.getClientSecurityList().add(cli);

		mySvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v1, new ArrayList<GResource>());

		newEntityManager();

		d1s1v1 = (GSoap11ServiceVersion) mySvc.loadServiceVersion(d1s1v1.getPid()).getServiceVersion();
		assertEquals(2, d1s1v1.getClientSecurityList().size());
		assertEquals("un0", ((GWsSecUsernameTokenClientSecurity) d1s1v1.getClientSecurityList().get(0)).getUsername());
		assertEquals("pw0", ((GWsSecUsernameTokenClientSecurity) d1s1v1.getClientSecurityList().get(0)).getPassword());
		assertEquals("un1", ((GWsSecUsernameTokenClientSecurity) d1s1v1.getClientSecurityList().get(1)).getUsername());
		assertEquals("pw1", ((GWsSecUsernameTokenClientSecurity) d1s1v1.getClientSecurityList().get(1)).getPassword());

		// Remove one

		d1s1v1.getClientSecurityList().remove(d1s1v1.getClientSecurityList().get(0));
		mySvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v1, new ArrayList<GResource>());

		newEntityManager();

		d1s1v1 = (GSoap11ServiceVersion) mySvc.loadServiceVersion(d1s1v1.getPid()).getServiceVersion();
		assertEquals(1, d1s1v1.getClientSecurityList().size());
		assertEquals("un1", ((GWsSecUsernameTokenClientSecurity) d1s1v1.getClientSecurityList().get(0)).getUsername());
		assertEquals("pw1", ((GWsSecUsernameTokenClientSecurity) d1s1v1.getClientSecurityList().get(0)).getPassword());

	}

	@Test(expected = IllegalArgumentException.class)
	public void testLoadAndSaveSvcVerServerSecurityNoAuthHost() throws ProcessingException {

		newEntityManager();

		GDomain d1 = mySvc.addDomain("asv_did", "asv_did");
		GService d1s1 = mySvc.addService(d1.getPid(), "asv_sid", "asv_sid", true);
		PersHttpClientConfig hcc = myDao.getOrCreateHttpClientConfig("httpServer");
		myDao.getOrCreateAuthenticationHostLocalDatabase("AUTHHOST");

		newEntityManager();

		GSoap11ServiceVersion d1s1v1 = new GSoap11ServiceVersion();
		d1s1v1.setActive(true);
		d1s1v1.setId("ASV_SV1");
		d1s1v1.setName("ASV_SV1_Name");
		d1s1v1.setWsdlLocation("http://foo");
		d1s1v1.setHttpClientConfigPid(hcc.getPid());
		d1s1v1 = mySvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v1, new ArrayList<GResource>());

		newEntityManager();

		// Add one

		d1s1v1 = (GSoap11ServiceVersion) mySvc.loadServiceVersion(d1s1v1.getPid()).getServiceVersion();
		GWsSecServerSecurity cli = new GWsSecServerSecurity();

		// Don't set the auth host, this should mean an exception is thrown

		d1s1v1.getServerSecurityList().add(cli);

		mySvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v1, new ArrayList<GResource>());
	}

	@Test
	public void testLoadAndSaveSvcVerServerSecurity() throws ProcessingException {

		newEntityManager();

		GDomain d1 = mySvc.addDomain("asv_did", "asv_did");
		GService d1s1 = mySvc.addService(d1.getPid(), "asv_sid", "asv_sid", true);
		PersHttpClientConfig hcc = myDao.getOrCreateHttpClientConfig("httpServer");
		PersAuthenticationHostLocalDatabase auth = myDao.getOrCreateAuthenticationHostLocalDatabase("AUTHHOST");
		PersAuthenticationHostLocalDatabase auth2 = myDao.getOrCreateAuthenticationHostLocalDatabase("AUTHHOST2");

		newEntityManager();

		GSoap11ServiceVersion d1s1v1 = new GSoap11ServiceVersion();
		d1s1v1.setActive(true);
		d1s1v1.setId("ASV_SV1");
		d1s1v1.setName("ASV_SV1_Name");
		d1s1v1.setWsdlLocation("http://foo");
		d1s1v1.setHttpClientConfigPid(hcc.getPid());
		d1s1v1 = mySvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v1, new ArrayList<GResource>());

		newEntityManager();

		// Add one

		d1s1v1 = (GSoap11ServiceVersion) mySvc.loadServiceVersion(d1s1v1.getPid()).getServiceVersion();
		GWsSecServerSecurity cli = new GWsSecServerSecurity();
		cli.setAuthHostPid(auth.getPid());
		d1s1v1.getServerSecurityList().add(cli);

		mySvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v1, new ArrayList<GResource>());

		newEntityManager();

		d1s1v1 = (GSoap11ServiceVersion) mySvc.loadServiceVersion(d1s1v1.getPid()).getServiceVersion();
		mySvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v1, new ArrayList<GResource>());

		newEntityManager();

		// Add a second

		d1s1v1 = (GSoap11ServiceVersion) mySvc.loadServiceVersion(d1s1v1.getPid()).getServiceVersion();
		cli = new GWsSecServerSecurity();
		cli.setAuthHostPid(auth2.getPid());
		d1s1v1.getServerSecurityList().add(cli);

		mySvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v1, new ArrayList<GResource>());

		newEntityManager();

		d1s1v1 = (GSoap11ServiceVersion) mySvc.loadServiceVersion(d1s1v1.getPid()).getServiceVersion();
		assertEquals(2, d1s1v1.getServerSecurityList().size());
		assertEquals(auth.getPid().longValue(), d1s1v1.getServerSecurityList().get(0).getAuthHostPid());
		assertEquals(auth2.getPid().longValue(), d1s1v1.getServerSecurityList().get(1).getAuthHostPid());

		// Remove one

		ourLog.info("Removing sec with PID {} but keeping {}", d1s1v1.getServerSecurityList().get(0).getPid(), d1s1v1.getServerSecurityList().get(1).getPid());

		d1s1v1.getServerSecurityList().remove(d1s1v1.getServerSecurityList().get(0));
		mySvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v1, new ArrayList<GResource>());

		newEntityManager();

		d1s1v1 = (GSoap11ServiceVersion) mySvc.loadServiceVersion(d1s1v1.getPid()).getServiceVersion();
		assertEquals(1, d1s1v1.getServerSecurityList().size());
		assertEquals(auth2.getPid().longValue(), d1s1v1.getServerSecurityList().get(0).getAuthHostPid());

	}

	@Test
	public void testLoadAndSaveSvcVerSoap11() throws ProcessingException {

		newEntityManager();

		GDomain d1 = mySvc.addDomain("asv_did", "asv_did");
		GService d1s1 = mySvc.addService(d1.getPid(), "asv_sid", "asv_sid", true);
		PersHttpClientConfig hcc = myDao.getOrCreateHttpClientConfig("httpclient");

		newEntityManager();

		GSoap11ServiceVersion d1s1v1 = new GSoap11ServiceVersion();
		d1s1v1.setActive(true);
		d1s1v1.setId("ASV_SV1");
		d1s1v1.setName("ASV_SV1_Name");
		d1s1v1.setWsdlLocation("http://foo");
		d1s1v1.setHttpClientConfigPid(hcc.getPid());

		List<GResource> resources = new ArrayList<GResource>();
		resources.add(new GResource("http://foo", "text/xml", "contents1"));
		resources.add(new GResource("http://bar", "text/xml", "contents2"));

		d1s1v1.getUrlList().add(new GServiceVersionUrl("url1", "http://url1"));
		d1s1v1.getUrlList().add(new GServiceVersionUrl("url2", "http://url2"));

		d1s1v1 = mySvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v1, resources);

		newEntityManager();

		/*
		 * Putting in the same old resources, but they look new because they don't have IDs
		 */

		d1s1v1.setWsdlLocation("http://bar");
		mySvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v1, resources);

		newEntityManager();

		GSoap11ServiceVersionAndResources copy = mySvc.loadServiceVersion(d1s1v1.getPid());
		GSoap11ServiceVersion svcVer = (GSoap11ServiceVersion) copy.getServiceVersion();
		assertEquals("http://bar", svcVer.getWsdlLocation());
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
		svcVer = (GSoap11ServiceVersion) copy.getServiceVersion();
		assertEquals("http://baz", svcVer.getWsdlLocation());
		assertEquals(1, copy.getResource().size());

	}

	@Test
	public void testLoadAndSaveSvcVerJsonRpc20() throws ProcessingException {

		newEntityManager();

		GDomain d1 = mySvc.addDomain("asv_did", "asv_did");
		GService d1s1 = mySvc.addService(d1.getPid(), "asv_sid", "asv_sid", true);
		PersHttpClientConfig hcc = myDao.getOrCreateHttpClientConfig("httpclient");

		newEntityManager();

		GServiceVersionJsonRpc20 d1s1v1 = new GServiceVersionJsonRpc20();
		d1s1v1.setActive(true);
		d1s1v1.setId("ASV_SV1");
		d1s1v1.setName("ASV_SV1_Name");
		d1s1v1.setHttpClientConfigPid(hcc.getPid());

		List<GResource> resources = new ArrayList<GResource>();
		resources.add(new GResource("http://foo", "text/xml", "contents1"));
		resources.add(new GResource("http://bar", "text/xml", "contents2"));

		d1s1v1.getUrlList().add(new GServiceVersionUrl("url1", "http://url1"));
		d1s1v1.getUrlList().add(new GServiceVersionUrl("url2", "http://url2"));

		d1s1v1 = mySvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v1, resources);

		newEntityManager();

		GSoap11ServiceVersionAndResources copy = mySvc.loadServiceVersion(d1s1v1.getPid());
		GServiceVersionJsonRpc20 svcVer = (GServiceVersionJsonRpc20) copy.getServiceVersion();
		assertEquals("ASV_SV1", svcVer.getId());
		assertEquals(2, copy.getResource().size());

		svcVer.setId("2.0");

		newEntityManager();

		mySvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), svcVer, copy.getResource());

		newEntityManager();

		copy = mySvc.loadServiceVersion(d1s1v1.getPid());
		svcVer = (GServiceVersionJsonRpc20) copy.getServiceVersion();
		assertEquals("2.0", svcVer.getId());
		assertEquals(2, copy.getResource().size());

	}

	@Override
	protected void newEntityManager() {
		super.newEntityManager();

		myDao.setEntityManager(myEntityManager);
	}

	@Test
	public void testAddDomain() throws ProcessingException {
		newEntityManager();

		GDomain domain = mySvc.addDomain("domain_id", "domain_name");
		newEntityManager();

		assertEquals("domain_id", domain.getId());
		assertEquals("domain_name", domain.getName());
		assertFalse(domain.isStatsInitialized());
	}

	@Test
	public void testSaveConfigWithUrlBases() throws ProcessingException {
		newEntityManager();

		GConfig config = mySvc.loadConfig();

		newEntityManager();

		config.getProxyUrlBases().clear();
		config.getProxyUrlBases().add("http://foo");

		config = mySvc.saveConfig(config);

		newEntityManager();

		config = mySvc.loadConfig();
		assertEquals(1, config.getProxyUrlBases().size());
		assertEquals("http://foo", config.getProxyUrlBases().get(0));

	}

	@Test(expected = IllegalArgumentException.class)
	public void testAddDomainDuplicate() throws ProcessingException {
		newEntityManager();

		mySvc.addDomain("domain_id2", "domain_name");
		newEntityManager();

		mySvc.addDomain("domain_id2", "domain_name");
		newEntityManager();

	}

	@Test
	public void testAddAndSaveService() throws ProcessingException {
		newEntityManager();

		GDomain domain = mySvc.addDomain("domain_id3", "domain_name");
		newEntityManager();

		GService service = mySvc.addService(domain.getPid(), "svc_id", "svc_name", true);

		newEntityManager();

		assertEquals("svc_id", service.getId());
		assertEquals("svc_name", service.getName());

		assertFalse(service.isStatsInitialized());

		newEntityManager();

		service.setName("name2");
		mySvc.saveService(service);

	}

	@Test
	public void testRenameServiceVersion() throws ProcessingException {
		newEntityManager();

		GDomain d1 = mySvc.addDomain("asv_did", "asv_did");
		GService d1s1 = mySvc.addService(d1.getPid(), "asv_sid", "asv_sid", true);
		PersHttpClientConfig hcc = myDao.getOrCreateHttpClientConfig("httpclient");

		newEntityManager();

		GSoap11ServiceVersion d1s1v1 = new GSoap11ServiceVersion();
		d1s1v1.setActive(true);
		d1s1v1.setId("ASV_SV1");
		d1s1v1.setName("ASV_SV1_Name");
		d1s1v1.setWsdlLocation("http://foo");
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
	public void testAddServiceVersion() throws ProcessingException {
		newEntityManager();

		GDomain d1 = mySvc.addDomain("asv_did", "asv_did");
		GService d1s1 = mySvc.addService(d1.getPid(), "asv_sid", "asv_sid", true);
		PersHttpClientConfig hcc = myDao.getOrCreateHttpClientConfig("httpclient");

		newEntityManager();

		GSoap11ServiceVersion d1s1v1 = new GSoap11ServiceVersion();
		d1s1v1.setActive(true);
		d1s1v1.setId("ASV_SV1");
		d1s1v1.setName("ASV_SV1_Name");
		d1s1v1.setWsdlLocation("http://foo");
		d1s1v1.setHttpClientConfigPid(hcc.getPid());

		List<GResource> resources = new ArrayList<GResource>();
		resources.add(new GResource("http://foo", "text/xml", "contents1"));
		resources.add(new GResource("http://bar", "text/xml", "contents2"));

		d1s1v1.getUrlList().add(new GServiceVersionUrl("url1", "http://url1"));
		d1s1v1.getUrlList().add(new GServiceVersionUrl("url2", "http://url2"));

		mySvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v1, resources);

		newEntityManager();

		PersDomain pDomain = myDao.getDomainByPid(d1.getPid());
		Collection<BasePersServiceVersion> versions = pDomain.getServices().iterator().next().getVersions();

		assertEquals(1, versions.size());

		PersServiceVersionSoap11 pVersion = (PersServiceVersionSoap11) versions.iterator().next();
		assertEquals("ASV_SV1", pVersion.getVersionId());
		assertEquals("http://foo", pVersion.getWsdlUrl());
		assertEquals(2, pVersion.getUriToResource().size());
		assertEquals("contents1", pVersion.getUriToResource().get("http://foo").getResourceText());
		assertEquals("contents2", pVersion.getUriToResource().get("http://bar").getResourceText());

		assertEquals(2, pVersion.getUrls().size());
		assertEquals("url1", pVersion.getUrls().get(0).getUrlId());
		assertEquals("url2", pVersion.getUrls().get(1).getUrlId());

	}

	@Test
	public void testDeleteServiceVersion() throws ProcessingException {
		newEntityManager();

		GDomain d1 = mySvc.addDomain("asv_did", "asv_did");
		GService d1s1 = mySvc.addService(d1.getPid(), "asv_sid", "asv_sid", true);
		PersHttpClientConfig hcc = myDao.getOrCreateHttpClientConfig("httpclient");

		newEntityManager();

		GSoap11ServiceVersion d1s1v1 = new GSoap11ServiceVersion();
		d1s1v1.setActive(true);
		d1s1v1.setId("ASV_SV1");
		d1s1v1.setName("ASV_SV1_Name");
		d1s1v1.setWsdlLocation("http://foo");
		d1s1v1.setHttpClientConfigPid(hcc.getPid());

		List<GResource> resources = new ArrayList<GResource>();
		resources.add(new GResource("http://foo", "text/xml", "contents1"));
		resources.add(new GResource("http://bar", "text/xml", "contents2"));

		d1s1v1.getUrlList().add(new GServiceVersionUrl("url1", "http://url1"));
		d1s1v1.getUrlList().add(new GServiceVersionUrl("url2", "http://url2"));

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

	@Test(expected = IllegalArgumentException.class)
	public void testAddServiceWithDuplicates() throws ProcessingException {
		newEntityManager();

		GDomain domain = mySvc.addDomain("domain_id4", "domain_name");
		newEntityManager();

		mySvc.addService(domain.getPid(), "svc_id2", "svc_name", true);

		newEntityManager();

		mySvc.addService(domain.getPid(), "svc_id2", "svc_name", true);
	}

	@Test
	public void testDeleteDomain() throws ProcessingException {

		createEverything();

		long pid = mySvc.getDomainPid("asv_did");
		assertTrue(pid > 0);

		mySvc.deleteDomain(pid);

		newEntityManager();

		GDomain domain = mySvc.getDomainByPid(pid);
		assertNull(domain);

	}

	@Test
	public void testDeleteUser() throws ProcessingException {

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

	private GDomain createEverything() throws ProcessingException {
		newEntityManager();

		mySecSvc.loadUserCatalogIfNeeded();

		BasePersAuthenticationHost authHost = myDao.getAuthenticationHost(BasePersAuthenticationHost.MODULE_ID_ADMIN_AUTH);
		authHost.setKeepNumRecentTransactionsFail(100);
		authHost.setKeepNumRecentTransactionsFault(100);
		authHost.setKeepNumRecentTransactionsSecurityFail(100);
		authHost.setKeepNumRecentTransactionsSuccess(100);
		myDao.saveAuthenticationHost(authHost);

		newEntityManager();

		GDomain d1 = mySvc.addDomain("asv_did", "asv_did");
		GService d1s1 = mySvc.addService(d1.getPid(), "asv_sid", "asv_sid", true);
		PersHttpClientConfig hcc = myDao.getOrCreateHttpClientConfig("httpclient");

		newEntityManager();

		GSoap11ServiceVersion d1s1v1 = new GSoap11ServiceVersion();
		d1s1v1.setActive(true);
		d1s1v1.setId("ASV_SV1");
		d1s1v1.setName("ASV_SV1_Name");
		d1s1v1.setWsdlLocation("http://foo");
		d1s1v1.setHttpClientConfigPid(hcc.getPid());

		d1s1v1.setKeepNumRecentTransactionsFail(100);
		d1s1v1.setKeepNumRecentTransactionsFault(100);
		d1s1v1.setKeepNumRecentTransactionsSecurityFail(100);
		d1s1v1.setKeepNumRecentTransactionsSuccess(100);

		GServiceMethod d1s1v1m1 = new GServiceMethod();
		d1s1v1m1.setName("d1s1v1m1");
		d1s1v1.getMethodList().add(d1s1v1m1);

		List<GResource> resources = new ArrayList<GResource>();
		resources.add(new GResource("http://foo", "text/xml", "contents1"));
		resources.add(new GResource("http://bar", "text/xml", "contents2"));

		GSoap11ServiceVersion ver = mySvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v1, resources);

		ver.getUrlList().add(new GServiceVersionUrl("url1", "http://foo"));
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

		PersServiceVersionMethod m1 = persVer.getMethods().iterator().next();

		newEntityManager();

		// Record invocation
		HttpResponseBean httpResponse = new HttpResponseBean();
		httpResponse.setBody("1234");
		httpResponse.setResponseTime(123);
		InvocationResponseResultsBean bean = new InvocationResponseResultsBean();
		bean.setResponseType(ResponseTypeEnum.SUCCESS);
		myStatsSvc.recordInvocationMethod(new Date(), 100, m1, null, httpResponse, bean, null);

		newEntityManager();

		myStatsSvc.flushStatus();

		newEntityManager();

		persVer = myDao.getServiceVersionByPid(ver.getPid());

		HttpRequestBean request = new HttpRequestBean();
		request.setRequestHostIp("127.0.0.1");
		request.setRequestHeaders(new HashMap<String, List<String>>());
		request.setRequestTime(new Date());
		String requestBody = "request body";
		InvocationResponseResultsBean invocationResponse = new InvocationResponseResultsBean();
		invocationResponse.setResponseHeaders(new HashMap<String, List<String>>());
		invocationResponse.setResponseType(ResponseTypeEnum.SUCCESS);
		PersServiceVersionUrl implementationUrl = persVer.getUrls().get(0);
		AuthorizationOutcomeEnum authorizationOutcome = AuthorizationOutcomeEnum.AUTHORIZED;
		PersUser persUser = myDao.getUser(user.getPidOrNull());
		myTransactionLogSvc.logTransaction(request, m1, persUser, requestBody, invocationResponse, implementationUrl, httpResponse, authorizationOutcome);

		newEntityManager();

		myTransactionLogSvc.flush();

		newEntityManager();
		return d1;
	}

	@Test
	public void testSaveDomain() throws ProcessingException {

		newEntityManager();

		GDomain d1 = mySvc.addDomain("asv_did", "asv_did");

		newEntityManager();

		GDomain domain = mySvc.getDomainByPid(d1.getPid());
		assertEquals("asv_did", domain.getId());

		newEntityManager();

		d1.setId("b");
		mySvc.saveDomain(d1);

		newEntityManager();

		domain = mySvc.getDomainByPid(d1.getPid());
		assertEquals("b", domain.getId());

	}

	@Test
	public void testLoadWsdl() throws ProcessingException {

		GSoap11ServiceVersion ver = new GSoap11ServiceVersion();

		PersServiceVersionSoap11 persSvcVer = new PersServiceVersionSoap11();
		persSvcVer.setWsdlUrl("http://wsdlurl");

		PersServiceVersionMethod m1 = new PersServiceVersionMethod();
		m1.setName("m1");
		persSvcVer.addMethod(m1);

		PersServiceVersionMethod m2 = new PersServiceVersionMethod();
		m2.setName("m2");
		persSvcVer.addMethod(m2);

		persSvcVer.addResource("http://wsdlurl", "application/xml", "wsdlcontents");
		persSvcVer.addResource("http://xsdurl", "application/xml", "xsdcontents");

		PersServiceVersionUrl url = new PersServiceVersionUrl();
		url.setUrlId("url1");
		url.setUrl("http://svcurl");
		persSvcVer.addUrl(url);

		when(mySoapInvoker.introspectServiceFromUrl("http://wsdlurl")).thenReturn(persSvcVer);

		DefaultAnswer.setRunTime();
		GSoap11ServiceVersionAndResources verAndRes = mySvc.loadSoap11ServiceVersionFromWsdl(ver, "http://wsdlurl");

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

	}

	@Test
	public void testMultipleStatsLoadsInParallel() throws Exception {
		newEntityManager();

		myConfigSvc.getConfig();

		newEntityManager();

		PersDomain d0 = myDao.getOrCreateDomainWithId("d0");
		PersService d0s0 = myDao.getOrCreateServiceWithId(d0, "d0s0");
		PersServiceVersionSoap11 d0s0v0 = (PersServiceVersionSoap11) myDao.getOrCreateServiceVersionWithId(d0s0, "d0s0v0", ServiceProtocolEnum.SOAP11);
		d0s0v0.getOrCreateAndAddMethodWithName("d0s0v0m0");
		myDao.saveServiceVersion(d0s0v0);

		newEntityManager();

		mySvcReg.reloadRegistryFromDatabase();

		newEntityManager();

		final ModelUpdateRequest req = new ModelUpdateRequest();
		req.addServiceToLoadStats(d0s0.getPid());

		List<RetrieverThread> ts = new ArrayList<RetrieverThread>();
		for (int i = 0; i < 3; i++) {
			RetrieverThread t = new RetrieverThread(req);
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
	public void testSaveUser() throws ProcessingException {
		newEntityManager();

		GDomain d1 = mySvc.addDomain("asv_did1", "asv_did1");
		GDomain d2 = mySvc.addDomain("asv_did2", "asv_did2");

		GService d1s1 = mySvc.addService(d1.getPid(), "asv_sid", "asv_sid", true);
		GService d2s1 = mySvc.addService(d2.getPid(), "d2s1", "d2s1", true);
		PersHttpClientConfig hcc = myDao.getOrCreateHttpClientConfig("httpclient");

		GSoap11ServiceVersion d1s1v1 = new GSoap11ServiceVersion();
		d1s1v1.setActive(true);
		d1s1v1.setId("D1S1V1");
		d1s1v1.setName("D1S1V1");
		d1s1v1.setWsdlLocation("http://foo");
		d1s1v1.setHttpClientConfigPid(hcc.getPid());
		List<GResource> resources = new ArrayList<GResource>();
		resources.add(new GResource("http://foo", "text/xml", "contents1"));
		resources.add(new GResource("http://bar", "text/xml", "contents2"));
		d1s1v1 = mySvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v1, resources);

		GSoap11ServiceVersion d2s1v1 = new GSoap11ServiceVersion();
		d2s1v1.setActive(true);
		d2s1v1.setId("D2S1V1");
		d2s1v1.setName("D2S1V1");
		d2s1v1.setWsdlLocation("http://foo");
		d2s1v1.setHttpClientConfigPid(hcc.getPid());
		d2s1v1 = mySvc.saveServiceVersion(d2.getPid(), d2s1.getPid(), d2s1v1, resources);

		GServiceMethod d1s1v1m1 = new GServiceMethod();
		d1s1v1m1.setName("d1s1v1m1");
		d1s1v1m1 = mySvc.addServiceVersionMethod(d1s1v1.getPid(), d1s1v1m1);

		GServiceMethod d2s1v1m1 = new GServiceMethod();
		d2s1v1m1.setName("d2s1v1m1");
		d2s1v1m1 = mySvc.addServiceVersionMethod(d2s1v1.getPid(), d2s1v1m1);

		GLocalDatabaseAuthHost authHost = new GLocalDatabaseAuthHost();
		authHost.setModuleId("authHost");
		authHost.setModuleName("authHost");
		authHost = (GLocalDatabaseAuthHost) mySvc.saveAuthenticationHost(authHost).get(0);

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

		user.getDomainPermissions().get(0).getOrCreateServicePermission(d1s1.getPid()).getOrCreateServiceVersionPermission(d1s1v1.getPid())
				.getOrCreateServiceVersionMethodPermission(d1s1v1m1.getPid());
		user.getDomainPermissions().get(1).getOrCreateServicePermission(d2s1.getPid()).getOrCreateServiceVersionPermission(d2s1v1.getPid())
				.getOrCreateServiceVersionMethodPermission(d2s1v1m1.getPid());
		mySvc.saveUser(user);
		newEntityManager();
		user = mySvc.loadUser(user.getPid(), false);
		assertEquals(2, user.getDomainPermissions().size());
		assertEquals(1, user.getDomainPermissions().get(0).getServicePermissions().get(0).getServiceVersionPermissions().get(0).getServiceVersionMethodPermissions().size());
		assertEquals(1, user.getDomainPermissions().get(1).getServicePermissions().get(0).getServiceVersionPermissions().get(0).getServiceVersionMethodPermissions().size());
		assertEquals(d1s1v1m1.getPid(), user.getDomainPermissions().get(0).getServicePermissions().get(0).getServiceVersionPermissions().get(0).getServiceVersionMethodPermissions().get(0)
				.getServiceVersionMethodPid());
		assertEquals(d2s1v1m1.getPid(), user.getDomainPermissions().get(1).getServicePermissions().get(0).getServiceVersionPermissions().get(0).getServiceVersionMethodPermissions().get(0)
				.getServiceVersionMethodPid());

		/*
		 * Remove domain permission
		 */

		user.removeDomainPermission(user.getDomainPermissions().get(0));
		mySvc.saveUser(user);
		assertEquals(1, user.getDomainPermissions().size());
		assertEquals(1, user.getDomainPermissions().get(0).getServicePermissions().get(0).getServiceVersionPermissions().get(0).getServiceVersionMethodPermissions().size());
		assertEquals(d2s1v1m1.getPid(), user.getDomainPermissions().get(0).getServicePermissions().get(0).getServiceVersionPermissions().get(0).getServiceVersionMethodPermissions().get(0)
				.getServiceVersionMethodPid());

	}

	private final class RetrieverThread extends Thread {
		private Exception myFailed;
		private final ModelUpdateRequest myReq;

		private RetrieverThread(ModelUpdateRequest theReq) {
			myReq = theReq;
		}

		@Override
		public void run() {
			try {

				EntityManager entityManager = ourEntityManagerFactory.createEntityManager();
				entityManager.getTransaction().begin();
				DaoBean persSvc = new DaoBean();
				persSvc.setEntityManager(entityManager);

				RuntimeStatusBean rs = new RuntimeStatusBean();
				rs.setDao(persSvc);

				AdminServiceBean sSvc = new AdminServiceBean();
				sSvc.setPersSvc(persSvc);
				sSvc.setConfigSvc(myConfigSvc);
				sSvc.setServiceRegistry(mySvcReg);
				sSvc.setRuntimeStatusSvc(rs);

				RuntimeStatusBean statsSvc = new RuntimeStatusBean();
				sSvc.setSchedulerServiceForTesting(mock(IScheduler.class));
				statsSvc.setDao(persSvc);

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
