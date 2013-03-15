package ca.uhn.sail.proxy.model.entity;

import static org.junit.Assert.*;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.Test;

import ca.uhn.sail.proxy.ejb.BaseJpaTest;
import ca.uhn.sail.proxy.model.entity.soap.PersServiceVersionSoap11;

public class PersServiceVersionUrlTest extends BaseJpaTest {

	@Test(expected=IllegalArgumentException.class)
	public void testSetNull() {
		PersServiceVersionUrl url = new PersServiceVersionUrl();
		url.setUrl(null);
	}
	
	@Test
	public void testUrlProperties() throws UnknownHostException {
		newEntityManager();
		
		PersServiceVersionUrl urlObj = createPersUrl("http://localhost/");
		urlObj = myEntityManager.merge(urlObj);
		
		newEntityManager();
		
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
		PersDomain dom = new PersDomain();
		dom.setDomainId("did");
		dom = myEntityManager.merge(dom);
		
		PersService svc = new PersService();
		svc.setServiceId("id");
		svc.setServiceName("name");
		svc.setPersDomain(dom);
		svc = myEntityManager.merge(svc);
		
		PersHttpClientConfig cfg = new PersHttpClientConfig();
		cfg.setDefaults();
		cfg.setId("cfgid");
		cfg = myEntityManager.merge(cfg);

		PersServiceVersionSoap11 ver = new PersServiceVersionSoap11();
		ver.setHttpClientConfig(cfg);
		ver.setService(svc);
		ver.setVersionId("vid");
		ver = myEntityManager.merge(ver);
				
		PersServiceVersionUrl urlObj = new PersServiceVersionUrl();
		urlObj.setUrlId("id");
		urlObj.setServiceVersion(ver);
		urlObj.setUrl(url2);
		return urlObj;
	}
	
	
}
