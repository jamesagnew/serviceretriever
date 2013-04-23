package net.svcret.ejb.model.entity;

import static org.junit.Assert.*;

import java.net.InetAddress;
import java.net.UnknownHostException;

import net.svcret.ejb.ejb.BaseJpaTest;
import net.svcret.ejb.model.entity.PersDomain;
import net.svcret.ejb.model.entity.PersHttpClientConfig;
import net.svcret.ejb.model.entity.PersService;
import net.svcret.ejb.model.entity.PersServiceVersionUrl;
import net.svcret.ejb.model.entity.soap.PersServiceVersionSoap11;

import org.junit.Test;


public class PersServiceVersionUrlTest extends BaseJpaTest {

	@Test(expected=NullPointerException.class)
	public void testSetNull() {
		PersServiceVersionUrl url = new PersServiceVersionUrl();
		url.setUrl(null);
	}
	
	@Test
	public void testUrlProperties() throws UnknownHostException {
		newEntityManager();
		
		PersServiceVersionUrl urlObj = createPersUrl("http://localhost/");
		
		urlObj = myEntityManager.find(PersServiceVersionUrl.class, urlObj.getPid());
		assertEquals(true, urlObj.isValid());
		assertEquals(true, urlObj.isLocal());

		urlObj.setUrl("");
		assertEquals(false, urlObj.isValid());
		assertEquals(false, urlObj.isLocal());

		urlObj.setUrl("http");
		assertEquals(false, urlObj.isValid());
		assertEquals(false, urlObj.isLocal());

		urlObj.setUrl("http://google.com");
		assertEquals(true, urlObj.isValid());
		assertEquals(false, urlObj.isLocal());

		urlObj.setUrl("http://localhost/");
		assertEquals(true, urlObj.isValid());
		assertEquals(true, urlObj.isLocal());

		urlObj.setUrl("http://"+InetAddress.getLocalHost().getHostName()+"/");
		assertEquals(true, urlObj.isValid());
		assertEquals(true, urlObj.isLocal());
		
	}

	private PersServiceVersionUrl createPersUrl(String url2) {
		PersHttpClientConfig cfg = new PersHttpClientConfig();
		cfg.setDefaults();
		cfg.setId("cfgid");
		cfg = myEntityManager.merge(cfg);
		
		newEntityManager();
		
		PersDomain dom = new PersDomain();
		dom.setDomainId("did");
		
		PersService svc = new PersService();
		svc.setServiceId("id");
		svc.setServiceName("name");
		svc.setDomain(dom);
		

		PersServiceVersionSoap11 ver = new PersServiceVersionSoap11();
		ver.setHttpClientConfig(cfg);
		ver.setService(svc);
		ver.setVersionId("vid");
				
		PersServiceVersionUrl urlObj = new PersServiceVersionUrl();
		urlObj.setUrlId("id");
		urlObj.setServiceVersion(ver);
		urlObj.setUrl(url2);
		ver.addUrl(urlObj);
		
		dom = myEntityManager.merge(dom);
		newEntityManager();

		dom = myEntityManager.find(PersDomain.class, dom.getPid());
		dom.loadAllAssociations();
		
		PersService serviceWithId = dom.getServiceWithId("id");
		BasePersServiceVersion versionWithId = serviceWithId.getVersionWithId("vid");
		PersServiceVersionUrl urlWithId = versionWithId.getUrlWithId("id");
		return urlWithId;
	}
	
	
}
