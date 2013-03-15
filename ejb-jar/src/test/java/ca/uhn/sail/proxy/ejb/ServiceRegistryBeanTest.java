package ca.uhn.sail.proxy.ejb;

import static org.mockito.Mockito.*;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ca.uhn.sail.proxy.api.HttpResponseBean;
import ca.uhn.sail.proxy.api.IHttpClient;
import ca.uhn.sail.proxy.api.IServicePersistence;
import ca.uhn.sail.proxy.ex.HttpFailureException;
import ca.uhn.sail.proxy.ex.InternalErrorException;
import ca.uhn.sail.proxy.ex.ProcessingException;
import ca.uhn.sail.proxy.model.entity.PersDomain;
import ca.uhn.sail.proxy.model.entity.PersService;
import ca.uhn.sail.proxy.model.entity.soap.PersServiceVersionSoap11;
import ca.uhn.sail.proxy.model.registry.ServiceVersion;
import ca.uhn.sail.proxy.model.registry.Services;
import ca.uhn.sail.proxy.model.registry.Services.Service;
import ca.uhn.sail.proxy.util.IOUtils;

public class ServiceRegistryBeanTest {

	private ServiceRegistryBean mySvc;
	private IHttpClient myHttpClient;
	private IServicePersistence myPersistence;

	@Before
	public void before() throws InternalErrorException {

		mySvc = new ServiceRegistryBean();

		myHttpClient = mock(IHttpClient.class, DefaultAnswer.INSTANCE);
		mySvc.setSvcHttpClient(myHttpClient);

		myPersistence = mock(IServicePersistence.class, DefaultAnswer.INSTANCE);
		mySvc.setSvcPersistence(myPersistence);

	}
	
	@Test
	public void testParseSimple() throws JAXBException, ProcessingException, HttpFailureException, IOException {

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

		String defString = svcs.toXml();

		
		String wsdlBody = IOUtils.readClasspathIntoString("/test_simple.wsdl");
		when(myHttpClient.get("http://foo/wsdl.wsdl")).thenReturn(new HttpResponseBean("/test_simple.wsdl", 200, wsdlBody));

		PersDomain domain = new PersDomain(1L, "DOMAIN");
		when(myPersistence.getOrCreateDomainWithId("DOMAIN")).thenReturn(domain);

		PersService service = new PersService(2L, domain, "SERVICE", "ServiceName");
		when(myPersistence.getOrCreateServiceWithId(domain, "SERVICE", "ServiceName")).thenReturn(service);

		PersServiceVersionSoap11 version = new PersServiceVersionSoap11(3L, service, "VER0");
		when(myPersistence.getOrCreateServiceVersionWithId(service, "VER0")).thenReturn(version);

//		when(myPersistence.saveServiceVersion(version));
		
		DefaultAnswer.setRunTime();
		mySvc.loadServiceDefinition(defString);

	}

	@After
	public void after() {
		DefaultAnswer.setDesignTime();
		validateMockitoUsage();
	}

}
