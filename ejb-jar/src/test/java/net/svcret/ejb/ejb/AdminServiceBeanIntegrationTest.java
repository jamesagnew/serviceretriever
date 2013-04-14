package net.svcret.ejb.ejb;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import net.svcret.admin.shared.model.GDomain;
import net.svcret.admin.shared.model.GLocalDatabaseAuthHost;
import net.svcret.admin.shared.model.GResource;
import net.svcret.admin.shared.model.GService;
import net.svcret.admin.shared.model.GServiceMethod;
import net.svcret.admin.shared.model.GServiceVersionUrl;
import net.svcret.admin.shared.model.GSoap11ServiceVersion;
import net.svcret.admin.shared.model.GSoap11ServiceVersionAndResources;
import net.svcret.admin.shared.model.GUser;
import net.svcret.admin.shared.model.GUserDomainPermission;
import net.svcret.admin.shared.model.UserGlobalPermissionEnum;
import net.svcret.ejb.api.HttpResponseBean;
import net.svcret.ejb.api.IServiceInvoker;
import net.svcret.ejb.api.InvocationResponseResultsBean;
import net.svcret.ejb.api.ResponseTypeEnum;
import net.svcret.ejb.ex.ProcessingException;
import net.svcret.ejb.model.entity.BasePersServiceVersion;
import net.svcret.ejb.model.entity.PersDomain;
import net.svcret.ejb.model.entity.PersHttpClientConfig;
import net.svcret.ejb.model.entity.PersServiceVersionMethod;
import net.svcret.ejb.model.entity.PersServiceVersionStatus;
import net.svcret.ejb.model.entity.PersServiceVersionUrl;
import net.svcret.ejb.model.entity.soap.PersServiceVersionSoap11;

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AdminServiceBeanIntegrationTest extends BaseJpaTest {

	private ServicePersistenceBean myPersSvc;
	private AdminServiceBean mySvc;
	private RuntimeStatusBean myStatsSvc;

	@SuppressWarnings("rawtypes")
	private IServiceInvoker mySoapInvoker;

	@SuppressWarnings("unchecked")
	@Before
	public void before2() throws SQLException {
		myPersSvc = new ServicePersistenceBean();

		mySvc = new AdminServiceBean();
		mySvc.setPersSvc(myPersSvc);

		myStatsSvc = new RuntimeStatusBean();
		myStatsSvc.setPersistence(myPersSvc);

		mySoapInvoker = mock(IServiceInvoker.class, new DefaultAnswer());
		mySvc.setInvokerSoap11(mySoapInvoker);

		DefaultAnswer.setDesignTime();
	}

	@Override
	protected void newEntityManager() {
		super.newEntityManager();

		myPersSvc.setEntityManager(myEntityManager);
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
	public void testAddDomain() throws ProcessingException {
		newEntityManager();

		GDomain domain = mySvc.addDomain("domain_id", "domain_name");
		newEntityManager();

		assertEquals("domain_id", domain.getId());
		assertEquals("domain_name", domain.getName());
		assertFalse(domain.isStatsInitialized());
	}

	@After
	public void after2() {

		// newEntityManager();
		//
		// Query q = myEntityManager.createQuery("DELETE FROM PersDomain p");
		// q.executeUpdate();
		//
		// newEntityManager();
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
	public void testDeleteDomain() throws ProcessingException {

		newEntityManager();

		GDomain d1 = mySvc.addDomain("asv_did", "asv_did");
		GService d1s1 = mySvc.addService(d1.getPid(), "asv_sid", "asv_sid", true);
		PersHttpClientConfig hcc = myPersSvc.getOrCreateHttpClientConfig("httpclient");

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

		GSoap11ServiceVersion ver = mySvc.addServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v1, resources);

		newEntityManager();

		// Add stats
		BasePersServiceVersion persVer = myPersSvc.getServiceVersionByPid(ver.getPid());
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
	public void testAddServiceVersion() throws ProcessingException {
		newEntityManager();

		GDomain d1 = mySvc.addDomain("asv_did", "asv_did");
		GService d1s1 = mySvc.addService(d1.getPid(), "asv_sid", "asv_sid", true);
		PersHttpClientConfig hcc = myPersSvc.getOrCreateHttpClientConfig("httpclient");

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

		mySvc.addServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v1, resources);

		newEntityManager();

		PersDomain pDomain = myPersSvc.getDomainByPid(d1.getPid());
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
	public void testSaveUser() throws ProcessingException {
		newEntityManager();

		GDomain d1 = mySvc.addDomain("asv_did1", "asv_did1");
		GDomain d2 = mySvc.addDomain("asv_did2", "asv_did2");

		GService d1s1 = mySvc.addService(d1.getPid(), "asv_sid", "asv_sid", true);
		GService d2s1 = mySvc.addService(d2.getPid(), "d2s1", "d2s1", true);
		PersHttpClientConfig hcc = myPersSvc.getOrCreateHttpClientConfig("httpclient");

		GSoap11ServiceVersion d1s1v1 = new GSoap11ServiceVersion();
		d1s1v1.setActive(true);
		d1s1v1.setId("D1S1V1");
		d1s1v1.setName("D1S1V1");
		d1s1v1.setWsdlLocation("http://foo");
		d1s1v1.setHttpClientConfigPid(hcc.getPid());
		List<GResource> resources = new ArrayList<GResource>();
		resources.add(new GResource("http://foo", "text/xml", "contents1"));
		resources.add(new GResource("http://bar", "text/xml", "contents2"));
		d1s1v1 = mySvc.addServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v1, resources);

		GSoap11ServiceVersion d2s1v1 = new GSoap11ServiceVersion();
		d2s1v1.setActive(true);
		d2s1v1.setId("D2S1V1");
		d2s1v1.setName("D2S1V1");
		d2s1v1.setWsdlLocation("http://foo");
		d2s1v1.setHttpClientConfigPid(hcc.getPid());
		d2s1v1 = mySvc.addServiceVersion(d2.getPid(), d2s1.getPid(), d2s1v1, resources);

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
		user = mySvc.loadUser(user.getPid());
		assertThat(user.getGlobalPermissions(), (contains(UserGlobalPermissionEnum.SUPERUSER)));

		/*
		 * Remove Global permission
		 */

		user.removeGlobalPermission(UserGlobalPermissionEnum.SUPERUSER);
		mySvc.saveUser(user);
		newEntityManager();
		user = mySvc.loadUser(user.getPid());
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
		user = mySvc.loadUser(user.getPid());
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
		user = mySvc.loadUser(user.getPid());
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
		user = mySvc.loadUser(user.getPid());
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
		user = mySvc.loadUser(user.getPid());
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
		user = mySvc.loadUser(user.getPid());
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

}
