package net.svcret.ejb.ejb;

import static org.junit.Assert.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import net.svcret.ejb.api.ResponseTypeEnum;
import net.svcret.ejb.ex.ProcessingException;
import net.svcret.ejb.model.entity.BasePersAuthenticationHost;
import net.svcret.ejb.model.entity.BasePersInvocationStats;
import net.svcret.ejb.model.entity.BasePersMethodStats;
import net.svcret.ejb.model.entity.BasePersServiceVersion;
import net.svcret.ejb.model.entity.InvocationStatsIntervalEnum;
import net.svcret.ejb.model.entity.PersAuthenticationHostLdap;
import net.svcret.ejb.model.entity.PersAuthenticationHostLocalDatabase;
import net.svcret.ejb.model.entity.PersBaseClientAuth;
import net.svcret.ejb.model.entity.PersBaseServerAuth;
import net.svcret.ejb.model.entity.PersDomain;
import net.svcret.ejb.model.entity.PersHttpClientConfig;
import net.svcret.ejb.model.entity.PersInvocationStats;
import net.svcret.ejb.model.entity.PersInvocationStatsPk;
import net.svcret.ejb.model.entity.PersInvocationUserStats;
import net.svcret.ejb.model.entity.PersInvocationUserStatsPk;
import net.svcret.ejb.model.entity.PersKeepRecentTransactions;
import net.svcret.ejb.model.entity.PersService;
import net.svcret.ejb.model.entity.PersServiceVersionMethod;
import net.svcret.ejb.model.entity.PersServiceVersionStatus;
import net.svcret.ejb.model.entity.PersServiceVersionUrl;
import net.svcret.ejb.model.entity.PersUser;
import net.svcret.ejb.model.entity.soap.PersServiceVersionSoap11;
import net.svcret.ejb.model.entity.soap.PersWsSecUsernameTokenClientAuth;
import net.svcret.ejb.model.entity.soap.PersWsSecUsernameTokenServerAuth;

import org.junit.Before;
import org.junit.Test;

public class DaoBeanTest extends BaseJpaTest {

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(DaoBeanTest.class);
	private DaoBean mySvc;

	@Test
	public void testHttpClientConfigCreateDefault() {
		newEntityManager();

		Collection<PersHttpClientConfig> configs = mySvc.getHttpClientConfigs();
		assertEquals(1, configs.size());

		PersHttpClientConfig config = configs.iterator().next();
		assertEquals(PersHttpClientConfig.DEFAULT_ID, config.getId());
		assertEquals(PersHttpClientConfig.DEFAULT_CB_TIME_BETWEEN_ATTEMPTS, config.getCircuitBreakerTimeBetweenResetAttempts());
		assertEquals(PersHttpClientConfig.DEFAULT_CONNECT_TIMEOUT_MILLIS, config.getConnectTimeoutMillis());
		assertEquals(PersHttpClientConfig.DEFAULT_READ_TIMEOUT_MILLIS, config.getReadTimeoutMillis());
		assertEquals(PersHttpClientConfig.DEFAULT_URL_SELECTION_POLICY, config.getUrlSelectionPolicy());
	}

	@Test
	public void testStatus() throws ProcessingException {
		Date now = new Date();

		newEntityManager();

		PersDomain domain = mySvc.getOrCreateDomainWithId("DOMAIN_ID");
		PersService service = mySvc.getOrCreateServiceWithId(domain, "SERVICE_ID");
		PersServiceVersionSoap11 ver = mySvc.getOrCreateServiceVersionWithId(service, "VersionId0");
		PersServiceVersionMethod method = ver.getOrCreateAndAddMethodWithName("MethodName");

		mySvc.saveServiceVersion(ver);

		newEntityManager();

		ver = mySvc.getOrCreateServiceVersionWithId(service, "VersionId0");
		method = ver.getOrCreateAndAddMethodWithName("MethodName");

		PersServiceVersionStatus status = ver.getStatus();
		assertNotNull(status.getPid());

		newEntityManager();

		status = mySvc.getStatusForServiceVersionWithPid(ver.getPid());
		assertNotNull(status);

		PersInvocationStats stats = new PersInvocationStats(InvocationStatsIntervalEnum.MINUTE, now, method);
		stats.addSuccessInvocation(100, 0, 0);
		stats.addSuccessInvocation(200, 0, 0);

		mySvc.saveInvocationStats(sing(stats));

		newEntityManager();

		status = mySvc.getStatusForServiceVersionWithPid(ver.getPid());
		assertNotNull(status);
		PersInvocationStatsPk pk = new PersInvocationStatsPk(InvocationStatsIntervalEnum.MINUTE, now, method);
		BasePersInvocationStats loadedStats = mySvc.getOrCreateInvocationStats(pk);

		assertEquals(2, loadedStats.getSuccessInvocationCount());
		assertEquals(150, loadedStats.getSuccessInvocationAvgTime());

		newEntityManager();

		stats = new PersInvocationStats(InvocationStatsIntervalEnum.MINUTE, now, method);
		stats.addSuccessInvocation(200, 0, 0);

		mySvc.saveInvocationStats(sing(stats));

		newEntityManager();

		status = mySvc.getStatusForServiceVersionWithPid(ver.getPid());
		assertNotNull(status);
		pk = new PersInvocationStatsPk(InvocationStatsIntervalEnum.MINUTE, now, method);
		loadedStats = mySvc.getOrCreateInvocationStats(pk);

		assertEquals(3, loadedStats.getSuccessInvocationCount());
		assertEquals(166, loadedStats.getSuccessInvocationAvgTime());

	}

	@Test
	public void testUserStatus() throws ProcessingException {
		Date now = new Date();
		newEntityManager();

		mySvc.getOrCreateAuthenticationHostLocalDatabase("ah0");
		
		newEntityManager();

		PersDomain domain = mySvc.getOrCreateDomainWithId("DOMAIN_ID");
		PersService service = mySvc.getOrCreateServiceWithId(domain, "SERVICE_ID");
		PersServiceVersionSoap11 ver = mySvc.getOrCreateServiceVersionWithId(service, "VersionId0");
		PersServiceVersionMethod method = ver.getOrCreateAndAddMethodWithName("MethodName");
		PersUser user = mySvc.getOrCreateUser(mySvc.getAuthenticationHost("ah0"), "Username");

		mySvc.saveServiceVersion(ver);

		newEntityManager();

		ver = mySvc.getOrCreateServiceVersionWithId(service, "VersionId0");
		method = ver.getOrCreateAndAddMethodWithName("MethodName");

		PersServiceVersionStatus status = ver.getStatus();
		assertNotNull(status.getPid());

		newEntityManager();

		status = mySvc.getStatusForServiceVersionWithPid(ver.getPid());
		assertNotNull(status);

		PersInvocationUserStats stats = new PersInvocationUserStats(InvocationStatsIntervalEnum.MINUTE, now, method, user);
		stats.addSuccessInvocation(100, 0, 0);
		stats.addSuccessInvocation(200, 0, 0);

		mySvc.saveInvocationStats(sing(stats));

		newEntityManager();

		status = mySvc.getStatusForServiceVersionWithPid(ver.getPid());
		assertNotNull(status);
		PersInvocationUserStatsPk pk = PersServiceVersionStatus.createEntryPk(InvocationStatsIntervalEnum.MINUTE, now, method, user);
		BasePersInvocationStats loadedStats = mySvc.getOrCreateInvocationUserStats(pk);

		assertEquals(2, loadedStats.getSuccessInvocationCount());
		assertEquals(150, loadedStats.getSuccessInvocationAvgTime());

		newEntityManager();

		stats = new PersInvocationUserStats(InvocationStatsIntervalEnum.MINUTE, now, method, user);
		stats.addSuccessInvocation(200, 0, 0);

		mySvc.saveInvocationStats(sing(stats));

		newEntityManager();

		status = mySvc.getStatusForServiceVersionWithPid(ver.getPid());
		assertNotNull(status);
		pk = PersServiceVersionStatus.createEntryPk(InvocationStatsIntervalEnum.MINUTE, now, method, user);
		loadedStats = mySvc.getOrCreateInvocationUserStats(pk);

		assertEquals(3, loadedStats.getSuccessInvocationCount());
		assertEquals(166, loadedStats.getSuccessInvocationAvgTime());

	}

	private Collection<BasePersMethodStats> sing(BasePersInvocationStats theStats) {
		ArrayList<BasePersMethodStats> retVal = new ArrayList<BasePersMethodStats>();
		retVal.add(theStats);
		return retVal;
	}

	@Before
	public void before2() throws SQLException {
		mySvc = new DaoBean();
	}

	@Override
	protected void newEntityManager() {
		super.newEntityManager();
		((DaoBean) mySvc).setEntityManager(myEntityManager);
	}

	@Test
	public void testGetOrCreateAuthenticationHostLdap() throws ProcessingException {
		newEntityManager();

		PersAuthenticationHostLdap ldap0 = mySvc.getOrCreateAuthenticationHostLdap("module0");

		newEntityManager();

		PersAuthenticationHostLdap ldap0b = mySvc.getOrCreateAuthenticationHostLdap("module0");
		PersAuthenticationHostLdap ldap1 = mySvc.getOrCreateAuthenticationHostLdap("module1");

		assertEquals(ldap0.getPid(), ldap0b.getPid());
		assertNotEquals(ldap1.getPid(), ldap0.getPid());

		newEntityManager();

		BasePersAuthenticationHost ldap0c = mySvc.getAuthenticationHost("module0");
		assertEquals(ldap0, ldap0c);

		BasePersAuthenticationHost ldap1b = mySvc.getAuthenticationHost("module1");
		assertEquals(ldap1, ldap1b);
		assertNotEquals(ldap1, ldap0c);

	}

	@Test
	public void testGetOrCreateAuthenticationHostLocalDatabase() throws ProcessingException {
		newEntityManager();

		PersAuthenticationHostLocalDatabase ldap0 = mySvc.getOrCreateAuthenticationHostLocalDatabase("module0");

		newEntityManager();

		PersAuthenticationHostLocalDatabase ldap0b = mySvc.getOrCreateAuthenticationHostLocalDatabase("module0");
		PersAuthenticationHostLocalDatabase ldap1 = mySvc.getOrCreateAuthenticationHostLocalDatabase("module1");

		assertEquals(ldap0.getPid(), ldap0b.getPid());
		assertNotEquals(ldap1.getPid(), ldap0.getPid());

		newEntityManager();

		BasePersAuthenticationHost ldap0c = mySvc.getAuthenticationHost("module0");
		assertEquals(ldap0, ldap0c);

		BasePersAuthenticationHost ldap1b = mySvc.getAuthenticationHost("module1");
		assertEquals(ldap1, ldap1b);
		assertNotEquals(ldap1, ldap0c);

	}

	@Test
	public void testGetOrCreateDomain() throws ProcessingException {
		newEntityManager();

		PersDomain domain = mySvc.getOrCreateDomainWithId("DOMAIN_ID");
		assertTrue(domain.isNewlyCreated());
		assertTrue(domain.getPid() > 0);

		newEntityManager();

		Collection<PersDomain> allDomains = mySvc.getAllDomains();
		assertEquals(1, allDomains.size());
		assertTrue(domain.equals(allDomains.iterator().next()));

		newEntityManager();

		PersDomain domain2 = mySvc.getOrCreateDomainWithId("DOMAIN_ID");
		assertFalse(domain2.isNewlyCreated());
		assertEquals(domain.getPid(), domain2.getPid());

	}

	@Test
	public void testGetOrCreateService() throws ProcessingException {
		newEntityManager();

		PersDomain domain = mySvc.getOrCreateDomainWithId("DOMAIN_ID");
		PersService service = mySvc.getOrCreateServiceWithId(domain, "SERVICE_ID");

		newEntityManager();

		assertTrue(service.getPid() > 0);

		service = mySvc.getOrCreateServiceWithId(domain, "SERVICE_ID");
		service.setServiceName("ServiceName");
		mySvc.saveService(service);
		assertEquals("ServiceName", service.getServiceName());

		newEntityManager();

		Collection<PersService> allServices = mySvc.getAllServices();
		assertEquals(1, allServices.size());
		assertEquals(service, allServices.iterator().next());

		newEntityManager();

		PersService service2 = mySvc.getOrCreateServiceWithId(domain, "SERVICE_ID");
		assertEquals(service.getPid(), service2.getPid());

	}

	@Test
	public void testGetOrCreateServiceVersionResource() throws ProcessingException {
		newEntityManager();

		PersDomain domain = mySvc.getOrCreateDomainWithId("DOMAIN_ID");
		PersService service = mySvc.getOrCreateServiceWithId(domain, "SERVICE_ID");
		PersServiceVersionSoap11 ver = mySvc.getOrCreateServiceVersionWithId(service, "VersionId0");

		ver.addResource("http://foo", "text/plain", "foo contents");
		ver.addResource("http://bar", "text/plain", "bar contents");
		ver.addResource("http://baz", "text/plain", "baz contents");

		mySvc.saveServiceVersion(ver);

		newEntityManager();

		ver = mySvc.getAllServiceVersions().iterator().next();
		assertEquals(3, ver.getUriToResource().size());
		assertEquals("foo contents", ver.getUriToResource().get("http://foo").getResourceText());
		assertEquals("bar contents", ver.getUriToResource().get("http://bar").getResourceText());
		assertEquals("baz contents", ver.getUriToResource().get("http://baz").getResourceText());

		ver.getUriToResource().remove("http://foo");
		mySvc.saveServiceVersion(ver);

		ver = mySvc.getAllServiceVersions().iterator().next();
		assertEquals(2, ver.getUriToResource().size());
		assertNull(ver.getUriToResource().get("http://foo"));
		assertEquals("bar contents", ver.getUriToResource().get("http://bar").getResourceText());
		assertEquals("baz contents", ver.getUriToResource().get("http://baz").getResourceText());

	}

	@Test
	public void testGetOrCreateServiceVersionUrl() throws ProcessingException {
		newEntityManager();

		PersDomain domain = mySvc.getOrCreateDomainWithId("DOMAIN_ID");
		PersService service = mySvc.getOrCreateServiceWithId(domain, "SERVICE_ID");
		PersServiceVersionSoap11 ver = mySvc.getOrCreateServiceVersionWithId(service, "VersionId0");

		PersServiceVersionUrl url0 = new PersServiceVersionUrl();
		url0.setUrlId("url0");
		url0.setUrl("http://url0");
		ver.getUrls().add(url0);

		PersServiceVersionUrl url1 = new PersServiceVersionUrl();
		url1.setUrlId("url1");
		url1.setUrl("http://url1");
		ver.getUrls().add(url1);

		PersServiceVersionUrl url2 = new PersServiceVersionUrl();
		url2.setUrlId("url2");
		url2.setUrl("http://url2");
		ver.getUrls().add(url2);

		mySvc.saveServiceVersion(ver);

		newEntityManager();

		ver = mySvc.getAllServiceVersions().iterator().next();
		assertEquals(3, ver.getUrls().size());
		assertNotNull(ver.getUrls().get(0).getStatus());

		ver.retainOnlyUrlsWithIds("url0", "url2");
		ver.getUrlWithId("url0").setUrl("http://url0b");

		mySvc.saveServiceVersion(ver);

		newEntityManager();

		ver = mySvc.getAllServiceVersions().iterator().next();
		assertEquals(2, ver.getUrls().size());
		Iterator<PersServiceVersionUrl> iter = ver.getUrls().iterator();
		assertEquals("http://url0b", iter.next().getUrl());
		assertEquals("http://url2", iter.next().getUrl());

		newEntityManager();

		ver = mySvc.getAllServiceVersions().iterator().next();

		PersServiceVersionUrl url1b = new PersServiceVersionUrl();
		url1b.setUrlId("url1b");
		url1b.setUrl("http://url1b");
		ver.getUrls().add(1, url1b);

		mySvc.saveServiceVersion(ver);

		newEntityManager();

		ver = mySvc.getAllServiceVersions().iterator().next();
		assertEquals(3, ver.getUrls().size());
		iter = ver.getUrls().iterator();
		assertEquals("http://url0b", iter.next().getUrl());
		assertEquals("http://url1b", iter.next().getUrl());
		assertEquals("http://url2", iter.next().getUrl());

	}
	
	@Test
	public void testGetOrCreateServiceVersionKeepRecent() throws ProcessingException {
		newEntityManager();

		PersDomain domain = mySvc.getOrCreateDomainWithId("DOMAIN_ID");
		PersService service = mySvc.getOrCreateServiceWithId(domain, "SERVICE_ID");
		PersServiceVersionSoap11 ver = mySvc.getOrCreateServiceVersionWithId(service, "VersionId0");

		mySvc.saveServiceVersion(ver);
		newEntityManager();

//		mySvc.saveServiceVersion(ver);
//		newEntityManager();

		ver = (PersServiceVersionSoap11) mySvc.getServiceVersionByPid(ver.getPid());
		ver.getKeepRecentTransactions().add(new PersKeepRecentTransactions(ResponseTypeEnum.FAIL, 5));
		ver.getKeepRecentTransactions().add(new PersKeepRecentTransactions(ResponseTypeEnum.FAULT, 10));
		
		ver = (PersServiceVersionSoap11) mySvc.saveServiceVersion(ver);
		newEntityManager();

		ver = (PersServiceVersionSoap11) mySvc.getServiceVersionByPid(ver.getPid());
		assertEquals(5, ver.getKeepRecentTransactions(ResponseTypeEnum.FAIL).getKeepNum());
		assertEquals(10, ver.getKeepRecentTransactions(ResponseTypeEnum.FAULT).getKeepNum());
		
	}
	@Test
	public void testGetOrCreateServiceVersionClientAuth() throws ProcessingException {
		newEntityManager();

		PersDomain domain = mySvc.getOrCreateDomainWithId("DOMAIN_ID");
		PersService service = mySvc.getOrCreateServiceWithId(domain, "SERVICE_ID");
		PersServiceVersionSoap11 ver = mySvc.getOrCreateServiceVersionWithId(service, "VersionId0");
		PersAuthenticationHostLdap ldap = mySvc.getOrCreateAuthenticationHostLdap("Ldap0");

		PersWsSecUsernameTokenClientAuth ca0 = new PersWsSecUsernameTokenClientAuth();
		ca0.setUsername("un0");
		ver.addClientAuth(ca0);

		PersWsSecUsernameTokenClientAuth ca1 = new PersWsSecUsernameTokenClientAuth();
		ca1.setUsername("un1");
		ver.addClientAuth(ca1);

		PersWsSecUsernameTokenClientAuth ca2 = new PersWsSecUsernameTokenClientAuth();
		ca2.setUsername("un2");
		ver.addClientAuth(ca2);

		mySvc.saveServiceVersion(ver);

		newEntityManager();

		ver = mySvc.getAllServiceVersions().iterator().next();
		assertEquals(3, ver.getClientAuths().size());

		ver.removeClientAuth(ver.getClientAuths().get(1));

		mySvc.saveServiceVersion(ver);

		newEntityManager();

		ver = mySvc.getAllServiceVersions().iterator().next();
		assertEquals(2, ver.getClientAuths().size());
		Iterator<PersBaseClientAuth<?>> iter = ver.getClientAuths().iterator();
		assertEquals("un0", iter.next().getUsername());
		assertEquals("un2", iter.next().getUsername());

		newEntityManager();

		ver = mySvc.getAllServiceVersions().iterator().next();

		PersWsSecUsernameTokenClientAuth url1b = new PersWsSecUsernameTokenClientAuth();
		url1b.setUsername("un1b");
		ver.addClientAuth(1, url1b);

		mySvc.saveServiceVersion(ver);

		newEntityManager();

		ver = mySvc.getAllServiceVersions().iterator().next();
		assertEquals(3, ver.getClientAuths().size());

		iter = ver.getClientAuths().iterator();
		assertEquals("un0", iter.next().getUsername());
		assertEquals("un1b", iter.next().getUsername());
		assertEquals("un2", iter.next().getUsername());

	}

	@Test
	public void testGetOrCreateServiceVersionServerAuth() throws ProcessingException {
		newEntityManager();

		PersDomain domain = mySvc.getOrCreateDomainWithId("DOMAIN_ID");
		PersService service = mySvc.getOrCreateServiceWithId(domain, "SERVICE_ID");
		PersServiceVersionSoap11 ver = mySvc.getOrCreateServiceVersionWithId(service, "VersionId0");
		PersAuthenticationHostLdap ldap = mySvc.getOrCreateAuthenticationHostLdap("Ldap0");

		PersWsSecUsernameTokenServerAuth auth = new PersWsSecUsernameTokenServerAuth();
		auth.setAuthenticationHost(ldap);

		ver.addServerAuth(auth);

		mySvc.saveServiceVersion(ver);

		newEntityManager();

		ver = mySvc.getAllServiceVersions().iterator().next();
		assertEquals(1, ver.getServerAuths().size());

		PersBaseServerAuth<?, ?> serverAuth = ver.getServerAuths().get(0);
		assertEquals(PersWsSecUsernameTokenServerAuth.class, serverAuth.getClass());
		assertEquals(ldap, serverAuth.getAuthenticationHost());
	}

	@Test
	public void testGetOrCreateServiceVersion() throws ProcessingException {
		newEntityManager();

		PersDomain domain = mySvc.getOrCreateDomainWithId("DOMAIN_ID");
		PersService service = mySvc.getOrCreateServiceWithId(domain, "SERVICE_ID");
		PersServiceVersionSoap11 version0 = mySvc.getOrCreateServiceVersionWithId(service, "VersionId0");
		PersServiceVersionSoap11 version1 = mySvc.getOrCreateServiceVersionWithId(service, "VersionId1");

		assertTrue(version0.getPid() > 0);
		assertNotNull(version0.getStatus());
		assertNotNull(version1.getStatus());
		assertNotNull(version0.getStatus().getPid());
		assertNotNull(version1.getStatus().getPid());

		newEntityManager();

		Collection<PersServiceVersionSoap11> allVersions = mySvc.getAllServiceVersions();
		assertEquals(2, allVersions.size());
		assertTrue(version0.equals(allVersions.iterator().next()));
		
		Collection<BasePersServiceVersion> versions = service.getVersions();
		assertEquals(2, versions.size());

		newEntityManager();

		// Update the service version properties
		version0 = (PersServiceVersionSoap11) mySvc.getServiceVersionByPid(version0.getPid());
		assertNotNull(version0.getStatus().getPid());
		version0.setWsdlUrl("http://foo");
		mySvc.saveServiceVersion(version0);

		version1 = (PersServiceVersionSoap11) mySvc.getServiceVersionByPid(version1.getPid());
		assertNotNull(version1.getStatus().getPid());
		version1.setWsdlUrl("http://bar");
		mySvc.saveServiceVersion(version1);

		newEntityManager();

		allVersions = mySvc.getAllServiceVersions();
		assertEquals(2, allVersions.size());
		assertEquals("http://foo", allVersions.iterator().next().getWsdlUrl());

		// Remove a service version
		mySvc.removeServiceVersion(version0.getPid());

		newEntityManager();

		allVersions = mySvc.getAllServiceVersions();
		assertEquals(1, allVersions.size());
		assertEquals("http://bar", allVersions.iterator().next().getWsdlUrl());

		service = mySvc.getOrCreateServiceWithId(domain, "SERVICE_ID");
		assertEquals(1, service.getVersions().size());
		PersServiceVersionSoap11 next = (PersServiceVersionSoap11) service.getVersions().iterator().next();
		assertEquals("http://bar", next.getWsdlUrl());

		assertEquals(PersHttpClientConfig.DEFAULT_ID, service.getVersions().iterator().next().getHttpClientConfig().getId());

	}

}
