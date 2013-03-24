package net.svcret.ejb.ejb;

import static org.junit.Assert.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.svcret.admin.shared.model.GDomain;
import net.svcret.admin.shared.model.GResource;
import net.svcret.admin.shared.model.GService;
import net.svcret.admin.shared.model.GSoap11ServiceVersion;
import net.svcret.admin.shared.model.StatusEnum;
import net.svcret.ejb.ex.ProcessingException;
import net.svcret.ejb.model.entity.BasePersServiceVersion;
import net.svcret.ejb.model.entity.PersDomain;
import net.svcret.ejb.model.entity.soap.PersServiceVersionSoap11;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AdminServiceBeanIntegrationTest extends BaseJpaTest {

	private ServicePersistenceBean myPersSvc;
	private AdminServiceBean mySvc;

	@Before
	public void before2() throws SQLException {
		myPersSvc = new ServicePersistenceBean();

		mySvc = new AdminServiceBean();
		mySvc.setPersSvc(myPersSvc);
	}

	@Override
	protected void newEntityManager() {
		super.newEntityManager();
		
		myPersSvc.setEntityManager(myEntityManager);
	}

	@Test
	public void testAddDomain() throws ProcessingException {
		newEntityManager();
		
		GDomain domain = mySvc.addDomain("domain_id", "domain_name");
		newEntityManager();

		assertEquals("domain_id", domain.getId());
		assertEquals("domain_name", domain.getName());
		assertEquals(StatusEnum.UNKNOWN, domain.getStatus());
		assertEquals(0, domain.getUrlsActive());
		assertEquals(0, domain.getUrlsUnknown());
		assertEquals(0, domain.getUrlsDown());
		
		assertEquals(60, domain.getTransactions60mins().length);
		for (int i = 0; i <= 59; i++) {
			assertEquals(0, domain.getTransactions60mins()[i]);
		}
	}

	@After
	public void after2() {
		
//		newEntityManager();
//		
//		Query q = myEntityManager.createQuery("DELETE FROM PersDomain p");
//		q.executeUpdate();
//		
//		newEntityManager();
	}
	
	@Test(expected=IllegalArgumentException.class)
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
		assertEquals(StatusEnum.UNKNOWN, service.getStatus());
		assertEquals(0, service.getUrlsActive());
		assertEquals(0, service.getUrlsUnknown());
		assertEquals(0, service.getUrlsDown());
		
		assertEquals(60, domain.getTransactions60mins().length);
		for (int i = 0; i <= 59; i++) {
			assertEquals(0, domain.getTransactions60mins()[i]);
		}
	}

	@Test
	public void testAddServiceVersion() throws ProcessingException {
		newEntityManager();

		GDomain domain = mySvc.addDomain("asv_did", "asv_did");
		GService service = mySvc.addService(domain.getPid(), "asv_sid", "asv_sid", true);
		
		newEntityManager();
		
		GSoap11ServiceVersion version = new GSoap11ServiceVersion();
		version.setActive(true);
		version.setId("ASV_SV1");
		version.setName("ASV_SV1_Name");
		version.setWsdlLocation("http://foo");
		
		List<GResource> resources = new ArrayList<GResource>();
		resources.add(new GResource("http://foo", "text/xml", "contents1"));
		resources.add(new GResource("http://bar", "text/xml", "contents2"));
		
		mySvc.addServiceVersion(domain.getPid(), service.getPid(), version, resources);
		
		newEntityManager();
		
		PersDomain pDomain = myPersSvc.getDomainByPid(domain.getPid());
		Collection<BasePersServiceVersion> versions = pDomain.getServices().iterator().next().getVersions();
		
		assertEquals(1, versions.size());
		
		PersServiceVersionSoap11 pVersion = (PersServiceVersionSoap11) versions.iterator().next();
		assertEquals("ASV_SV1", pVersion.getVersionId());
		assertEquals("http://foo", pVersion.getWsdlUrl());
		assertEquals(2, pVersion.getUriToResource().size());
		assertEquals("contents1", pVersion.getUriToResource().get("http://foo").getResourceText());
		assertEquals("contents2", pVersion.getUriToResource().get("http://bar").getResourceText());
		
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testAddServiceWithDuplicates() throws ProcessingException {
		newEntityManager();
		
		GDomain domain = mySvc.addDomain("domain_id4", "domain_name");
		newEntityManager();

		mySvc.addService(domain.getPid(), "svc_id2", "svc_name", true);
		
		newEntityManager();

		mySvc.addService(domain.getPid(), "svc_id2", "svc_name", true);
	}
	
}
