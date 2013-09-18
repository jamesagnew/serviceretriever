package net.svcret.ejb.invoker.hl7;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.StringReader;
import java.util.HashMap;
import java.util.List;

import net.svcret.ejb.api.HttpResponseBean;
import net.svcret.ejb.api.IDao;
import net.svcret.ejb.api.IServiceRegistry;
import net.svcret.ejb.api.InvocationResponseResultsBean;
import net.svcret.ejb.api.InvocationResultsBean;
import net.svcret.ejb.api.RequestType;
import net.svcret.ejb.model.entity.PersServiceVersionMethod;
import net.svcret.ejb.model.entity.hl7.PersServiceVersionHl7OverHttp;

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
		
		InvocationResultsBean result = mySvc.processInvocation(svcVer, RequestType.POST, "/Some/System", "", "application/hl7-v2", new StringReader(msgS));
		
		verify(myServiceRegistry).saveServiceVersion(eq(dbSvcVer));
		PersServiceVersionMethod method = dbSvcVer.getMethod("ADT^A01");
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
		
		HttpResponseBean response=new HttpResponseBean();
		response.setBody(msgS);
		response.setCode(200);
		response.setContentType("application/hl7-v2");
		response.setHeaders(new HashMap<String, List<String>>());
		response.setResponseTime(123);
		InvocationResponseResultsBean result = mySvc.processInvocationResponse(dbSvcVer, response);

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
		
		HttpResponseBean response=new HttpResponseBean();
		response.setBody(msgS);
		response.setCode(200);
		response.setContentType("application/hl7-v2");
		response.setHeaders(new HashMap<String, List<String>>());
		response.setResponseTime(123);
		InvocationResponseResultsBean result = mySvc.processInvocationResponse(dbSvcVer, response);

		assertEquals(msgS,result.getResponseBody());
		assertEquals("AE",result.getResponseFaultCode());
		assertNotNull(result.getResponseFaultDescription());
	}

}
