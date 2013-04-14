package net.svcret.admin.server.rpc;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import net.svcret.admin.shared.ServiceFailureException;
import net.svcret.admin.shared.model.GDomain;
import net.svcret.admin.shared.model.GResource;
import net.svcret.admin.shared.model.GService;
import net.svcret.admin.shared.model.GServiceVersionResourcePointer;
import net.svcret.admin.shared.model.GServiceVersionUrl;
import net.svcret.admin.shared.model.GSoap11ServiceVersion;
import net.svcret.admin.shared.model.GSoap11ServiceVersionAndResources;
import net.svcret.ejb.api.IAdminService;
import net.svcret.ejb.ejb.DefaultAnswer;
import net.svcret.ejb.ex.ProcessingException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class ModelUpdateServiceImplTest {

	private ModelUpdateServiceImpl mySvc;
	private IAdminService myAdminSvc;

	@Before
	public void before() {
		mySvc = new ModelUpdateServiceImpl();

		myAdminSvc = mock(IAdminService.class, new DefaultAnswer());
		mySvc.setAdminSvc(myAdminSvc);

		DefaultAnswer.setDesignTime();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testCreateNewSoap11Service() throws ServiceFailureException, ProcessingException {

		/*
		 * Create blank entry
		 */

		GSoap11ServiceVersion ver = mySvc.createNewSoap11ServiceVersion(100L, 101L, 102L);
		assertEquals(102L, ver.getUncommittedSessionId().longValue());

		/*
		 * Now load from WSDL
		 */

		GSoap11ServiceVersionAndResources verAndRes = new GSoap11ServiceVersionAndResources();
		verAndRes.setServiceVersion(ver);
		ver.setWsdlLocation("http://wsdlurl");
		ver.getUrlList().add(new GServiceVersionUrl("url1", "http://url1"));

		verAndRes.setResource(new ArrayList<GResource>());
		verAndRes.getResource().add(new GResource("http://wsdlurl", "text/xml", "wsdlcontents"));
		verAndRes.getResource().add(new GResource("http://xsdurl", "text/xml", "xsdcontents"));

		ver.getResourcePointerList().add(verAndRes.getResource().get(0).asPointer());
		ver.getResourcePointerList().add(verAndRes.getResource().get(1).asPointer());

		when(myAdminSvc.loadSoap11ServiceVersionFromWsdl(ver, "http://wsdlurl")).thenReturn(verAndRes);

		DefaultAnswer.setRunTime();
		GSoap11ServiceVersion ver2 = mySvc.loadWsdl(ver, "http://wsdlurl");

		assertEquals("http://url1", ver2.getUrlList().get(0).getUrl());
		assertEquals("http://wsdlurl", ver2.getResourcePointerList().get(0).getUrl());

		/*
		 * Now save it
		 */

		DefaultAnswer.setDesignTime();

		GDomain domain = new GDomain();
		when(myAdminSvc.getDomainByPid(100L)).thenReturn(domain);
		GService service = new GService();
		when(myAdminSvc.getServiceByPid(101L)).thenReturn(service);

		GSoap11ServiceVersion addSvcVal = new GSoap11ServiceVersion();
		ArgumentCaptor<Long> domainCaptor = ArgumentCaptor.forClass(Long.class);
		ArgumentCaptor<Long> serviceCaptor = ArgumentCaptor.forClass(Long.class);
		ArgumentCaptor<GSoap11ServiceVersion> versionCaptor = ArgumentCaptor.forClass(GSoap11ServiceVersion.class);
		ArgumentCaptor<List> resourceCaptor = ArgumentCaptor.forClass(List.class);

		when(myAdminSvc.addServiceVersion(domainCaptor.capture(), serviceCaptor.capture(), versionCaptor.capture(), resourceCaptor.capture())).thenReturn(addSvcVal);

		DefaultAnswer.setRunTime();
		ver2.setId("2.0");
		mySvc.addServiceVersion(100L, null, 101L, null, ver2);

		assertEquals(100L, (long) domainCaptor.getValue());

		ver = versionCaptor.getValue();
		assertEquals("http://wsdlurl", ver.getWsdlLocation());

		List<GResource> resources = resourceCaptor.getValue();
		assertEquals(2, resources.size());
		assertEquals(1, ver.getUrlList().size());

	}

}
