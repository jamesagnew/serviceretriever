package net.svcret.ejb.ejb;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import net.svcret.admin.shared.model.GConfig;
import net.svcret.admin.shared.model.GDomain;
import net.svcret.admin.shared.model.GLocalDatabaseAuthHost;
import net.svcret.admin.shared.model.GResource;
import net.svcret.admin.shared.model.GService;
import net.svcret.admin.shared.model.GServiceMethod;
import net.svcret.admin.shared.model.GServiceVersionJsonRpc20;
import net.svcret.admin.shared.model.GServiceVersionUrl;
import net.svcret.admin.shared.model.GSoap11ServiceVersion;
import net.svcret.admin.shared.model.GSoap11ServiceVersionAndResources;
import net.svcret.admin.shared.model.GUser;
import net.svcret.admin.shared.model.GUserDomainPermission;
import net.svcret.admin.shared.model.GWsSecServerSecurity;
import net.svcret.admin.shared.model.GWsSecUsernameTokenClientSecurity;
import net.svcret.admin.shared.model.ModelUpdateRequest;
import net.svcret.admin.shared.model.ServiceProtocolEnum;
import net.svcret.admin.shared.model.UserGlobalPermissionEnum;
import net.svcret.ejb.api.HttpResponseBean;
import net.svcret.ejb.api.IBroadcastSender;
import net.svcret.ejb.api.IRuntimeStatus;
import net.svcret.ejb.api.IServiceInvoker;
import net.svcret.ejb.api.InvocationResponseResultsBean;
import net.svcret.ejb.api.ResponseTypeEnum;
import net.svcret.ejb.ejb.AdminServiceBean.IWithStats;
import net.svcret.ejb.ex.ProcessingException;
import net.svcret.ejb.model.entity.BasePersServiceVersion;
import net.svcret.ejb.model.entity.PersAuthenticationHostLocalDatabase;
import net.svcret.ejb.model.entity.PersConfig;
import net.svcret.ejb.model.entity.PersDomain;
import net.svcret.ejb.model.entity.PersHttpClientConfig;
import net.svcret.ejb.model.entity.PersService;
import net.svcret.ejb.model.entity.PersServiceVersionMethod;
import net.svcret.ejb.model.entity.PersServiceVersionStatus;
import net.svcret.ejb.model.entity.PersServiceVersionUrl;
import net.svcret.ejb.model.entity.soap.PersServiceVersionSoap11;

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AdminServiceBeanIntegrationTest extends BaseJpaTest {

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(AdminServiceBeanIntegrationTest.class);

	private DaoBean myDao;
	@SuppressWarnings("rawtypes")
	private IServiceInvoker mySoapInvoker;
	private RuntimeStatusBean myStatsSvc;
	private AdminServiceBean mySvc;

	private SecurityServiceBean mySecSvc;

	private IBroadcastSender myBroadcastSender;

	private ServiceRegistryBean mySvcReg;

	private ConfigServiceBean myConfigSvc;

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

	@SuppressWarnings("unchecked")
	@Before
	public void before2() throws SQLException, ProcessingException {
		myDao = new DaoBean();
		myBroadcastSender = mock(IBroadcastSender.class);

		myConfigSvc = new ConfigServiceBean();
		myConfigSvc.setDao(myDao);
		myConfigSvc.setBroadcastSender(myBroadcastSender);

		mySvc = new AdminServiceBean();
		mySvc.setPersSvc(myDao);
		mySvc.setConfigSvc(myConfigSvc);

		myStatsSvc = new RuntimeStatusBean();
		myStatsSvc.setDao(myDao);
		myStatsSvc.setConfigSvc(myConfigSvc);

		mySoapInvoker = mock(IServiceInvoker.class, new DefaultAnswer());
		mySvc.setInvokerSoap11(mySoapInvoker);


		mySecSvc = new SecurityServiceBean();
		mySecSvc.setPersSvc(myDao);
		mySecSvc.setBroadcastSender(myBroadcastSender);
		mySvc.setSecuritySvc(mySecSvc);

		mySvcReg = new ServiceRegistryBean();
		mySvcReg.setBroadcastSender(myBroadcastSender);
		mySvcReg.setDao(myDao);
		mySvc.setServiceRegistry(mySvcReg);

		
		
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
		 * Putting in the same old resources, but they look new because they
		 * don't have IDs
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
	public void testAddService() throws ProcessingException {
		newEntityManager();

		GDomain domain = mySvc.addDomain("domain_id3", "domain_name");
		newEntityManager();

		GService service = mySvc.addService(domain.getPid(), "svc_id", "svc_name", true);

		assertEquals("svc_id", service.getId());
		assertEquals("svc_name", service.getName());

		assertFalse(service.isStatsInitialized());

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

		GServiceMethod d1s1v1m1 = new GServiceMethod();
		d1s1v1m1.setName("d1s1v1m1");
		d1s1v1.getMethodList().add(d1s1v1m1);

		List<GResource> resources = new ArrayList<GResource>();
		resources.add(new GResource("http://foo", "text/xml", "contents1"));
		resources.add(new GResource("http://bar", "text/xml", "contents2"));

		GSoap11ServiceVersion ver = mySvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v1, resources);

		newEntityManager();

		// Add stats
		BasePersServiceVersion persVer = myDao.getServiceVersionByPid(ver.getPid());
		PersServiceVersionStatus status = persVer.getStatus();
		assertNotNull(status);

		PersServiceVersionMethod m1 = persVer.getMethods().iterator().next();

		newEntityManager();

		HttpResponseBean httpResponse = new HttpResponseBean();
		httpResponse.setBody("1234");
		httpResponse.setResponseTime(123);
		InvocationResponseResultsBean bean = new InvocationResponseResultsBean();
		bean.setResponseType(ResponseTypeEnum.SUCCESS);
		myStatsSvc.recordInvocationMethod(new Date(), 100, m1, null, httpResponse, bean);

		newEntityManager();

		mySvc.deleteDomain(d1.getPid());

		newEntityManager();

		GDomain domain = mySvc.getDomainByPid(d1.getPid());
		assertNull(domain);

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

		user.getDomainPermissions().get(0).getOrCreateServicePermission(d1s1.getPid()).getOrCreateServiceVersionPermission(d1s1v1.getPid()).getOrCreateServiceVersionMethodPermission(d1s1v1m1.getPid()).setAllow(true);
		user.getDomainPermissions().get(1).getOrCreateServicePermission(d2s1.getPid()).getOrCreateServiceVersionPermission(d2s1v1.getPid()).getOrCreateServiceVersionMethodPermission(d2s1v1m1.getPid()).setAllow(true);
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
				sSvc.setRuntimeStatusBean(rs);

				RuntimeStatusBean statsSvc = new RuntimeStatusBean();
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