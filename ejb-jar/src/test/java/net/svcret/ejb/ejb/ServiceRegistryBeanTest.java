package net.svcret.ejb.ejb;

import static org.mockito.Mockito.*;
import net.svcret.admin.shared.model.ServiceProtocolEnum;
import net.svcret.ejb.api.HttpResponseBean;
import net.svcret.ejb.api.IDao;
import net.svcret.ejb.api.IHttpClient;
import net.svcret.ejb.model.entity.PersDomain;
import net.svcret.ejb.model.entity.PersService;
import net.svcret.ejb.model.entity.soap.PersServiceVersionSoap11;
import net.svcret.ejb.model.registry.ServiceVersion;
import net.svcret.ejb.model.registry.Services;
import net.svcret.ejb.model.registry.Services.Service;
import net.svcret.ejb.util.IOUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class ServiceRegistryBeanTest {

	private ServiceRegistryBean mySvc;
	private IHttpClient myHttpClient;
	private IDao myPersistence;

	@Before
	public void before()  throws Exception {

		mySvc = new ServiceRegistryBean();

		myHttpClient = mock(IHttpClient.class, DefaultAnswer.INSTANCE);
		mySvc.setSvcHttpClient(myHttpClient);

		myPersistence = mock(IDao.class, DefaultAnswer.INSTANCE);
		mySvc.setDao(myPersistence);

		DefaultAnswer.setDesignTime();
	}
	
	@Test
	public void testParseSimple()  throws Exception {

		Services svcs = new Services();
		Service def = svcs.addService();
		def.setDomainId("DOMAIN");
		def.setServiceId("SERVICE");
		def.setServiceName("ServiceName");

		ServiceVersion verDef = new ServiceVersion();
		verDef.setActive(true);
		verDef.setVersionId("VER0");
		verDef.setWsdlUrl("http://foo/wsdl.wsdl");
		def.getVersions().add(verDef);

		svcs.toXml();

		
		String wsdlBody = IOUtils.readClasspathIntoString("/test_simple.wsdl");
		when(myHttpClient.get("http://foo/wsdl.wsdl")).thenReturn(new HttpResponseBean(null, "text/xml", 200, wsdlBody));

		PersDomain domain = new PersDomain(1L, "DOMAIN");
		when(myPersistence.getOrCreateDomainWithId("DOMAIN")).thenReturn(domain);

		PersService service = new PersService(2L, domain, "SERVICE", "ServiceName");
		when(myPersistence.getOrCreateServiceWithId(domain, "SERVICE")).thenReturn(service);

		PersServiceVersionSoap11 version = new PersServiceVersionSoap11(3L, service, "VER0");
		when(myPersistence.getOrCreateServiceVersionWithId(service, "VER0", ServiceProtocolEnum.SOAP11)).thenReturn(version);

//		when(myPersistence.saveServiceVersion(version));
		
		DefaultAnswer.setRunTime();
//		mySvc.loadServiceDefinition(defString);

	}

	@After
	public void after() {
		DefaultAnswer.setDesignTime();
		validateMockitoUsage();
	}

}
