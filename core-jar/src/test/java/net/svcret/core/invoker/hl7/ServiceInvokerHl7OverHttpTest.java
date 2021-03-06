package net.svcret.core.invoker.hl7;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.StringReader;
import java.util.HashMap;
import java.util.List;

import net.svcret.core.api.IDao;
import net.svcret.core.api.IServiceRegistry;
import net.svcret.core.api.RequestType;
import net.svcret.core.api.SrBeanIncomingRequest;
import net.svcret.core.api.SrBeanIncomingResponse;
import net.svcret.core.api.SrBeanProcessedRequest;
import net.svcret.core.api.SrBeanProcessedResponse;
import net.svcret.core.invoker.hl7.ServiceInvokerHl7OverHttp;
import net.svcret.core.model.entity.PersMethod;
import net.svcret.core.model.entity.hl7.PersServiceVersionHl7OverHttp;

import org.junit.Before;
import org.junit.Test;

public class ServiceInvokerHl7OverHttpTest {

	private ServiceInvokerHl7OverHttp mySvc;
	private IDao myDao;
	private IServiceRegistry myServiceRegistry;

	@Before
	public void before() {
		mySvc = new ServiceInvokerHl7OverHttp();
		
		myDao = mock(IDao.class);
		mySvc.setDaoForUnitTest(myDao);
		mySvc.setTransactionTemplateForUnitTest();

		myServiceRegistry = mock(IServiceRegistry.class);
		mySvc.setServiceRegistryForUnitTest(myServiceRegistry);;
		
	}
	
	@Test
	public void testRequest() throws Exception {
		
		String msgS = "MSH|^~\\&|DATASERVICES|CORPORATE|||20120711120510.2-0500||ADT^A01^ADT_A01|9c906177-dfca-4bbe-9abd-d8eb43df93a0|D|2.6\r" + // -
				"EVN||20120701000000-0500\r" + // -
				"PID|1||397979797^^^SN^SN~4242^^^BKDMDM^PI~1000^^^YARDI^PI||Williams^Rory^H^^^^A||19641028000000-0600|M||||||||||31592^^^YARDI^AN\r";

		PersServiceVersionHl7OverHttp svcVer = mock(PersServiceVersionHl7OverHttp.class);
		when(svcVer.getPid()).thenReturn(111L);
		when(svcVer.getMethodNameTemplate()).thenCallRealMethod();
		
		PersServiceVersionHl7OverHttp dbSvcVer = new PersServiceVersionHl7OverHttp();
		dbSvcVer.setPid(111L);
		when(myDao.getServiceVersionByPid(111L)).thenReturn(dbSvcVer);
		when(myServiceRegistry.saveServiceVersion(dbSvcVer)).thenReturn(dbSvcVer);
		
		SrBeanIncomingRequest req = new SrBeanIncomingRequest();
		req.setPath("/Some/System");
		req.setQuery("");
		req.addHeader("Content-Type", "application/hl7-v2; charset=UTF-8");
		req.setRequestType(RequestType.POST);
		req.setInputReader(new StringReader(msgS));
		SrBeanProcessedRequest result = mySvc.processInvocation(req,svcVer);

		
		verify(myServiceRegistry).saveServiceVersion(eq(dbSvcVer));
		PersMethod method = dbSvcVer.getMethod("ADT");
		assertNotNull(method);
		assertSame(method, result.getMethodDefinition());
	}

	
	@Test
	public void testResponse() throws Exception {
		
		String msgS = "MSH|^~\\&|PRECASE||ORSOS|G|20130828075510.601-0400||ACK^S14|5801|P|2.3\r" + 
				"MSA|AA|38762";
		
		PersServiceVersionHl7OverHttp svcVer = mock(PersServiceVersionHl7OverHttp.class);
		when(svcVer.getPid()).thenReturn(111L);
		
		PersServiceVersionHl7OverHttp dbSvcVer = new PersServiceVersionHl7OverHttp();
		dbSvcVer.setPid(111L);
		when(myDao.getServiceVersionByPid(111L)).thenReturn(dbSvcVer);
		when(myServiceRegistry.saveServiceVersion(dbSvcVer)).thenReturn(dbSvcVer);
		
		SrBeanIncomingResponse response=new SrBeanIncomingResponse();
		response.setBody(msgS);
		response.setCode(200);
		response.setContentType("application/hl7-v2");
		response.setHeaders(new HashMap<String, List<String>>());
		response.setResponseTime(123);
		SrBeanProcessedResponse result = mySvc.processInvocationResponse(dbSvcVer, null, response);

		assertEquals(msgS,result.getResponseBody());
		assertNull(result.getResponseFaultCode());
		assertNull(result.getResponseFaultDescription());
	}

	@Test
	public void testResponseFault() throws Exception {
		
		String msgS = "MSH|^~\\&|PRECASE||ORSOS|G|20130828075510.601-0400||ACK^S14|5801|P|2.3\r" + 
				"MSA|AE|38762";
		
		PersServiceVersionHl7OverHttp svcVer = mock(PersServiceVersionHl7OverHttp.class);
		when(svcVer.getPid()).thenReturn(111L);
		
		PersServiceVersionHl7OverHttp dbSvcVer = new PersServiceVersionHl7OverHttp();
		dbSvcVer.setPid(111L);
		when(myDao.getServiceVersionByPid(111L)).thenReturn(dbSvcVer);
		when(myServiceRegistry.saveServiceVersion(dbSvcVer)).thenReturn(dbSvcVer);
		
		SrBeanIncomingResponse response=new SrBeanIncomingResponse();
		response.setBody(msgS);
		response.setCode(200);
		response.setContentType("application/hl7-v2");
		response.setHeaders(new HashMap<String, List<String>>());
		response.setResponseTime(123);
		SrBeanProcessedResponse result = mySvc.processInvocationResponse(dbSvcVer, null, response);

		assertEquals(msgS,result.getResponseBody());
		assertEquals("AE",result.getResponseFaultCode());
		assertNotNull(result.getResponseFaultDescription());
	}

}
