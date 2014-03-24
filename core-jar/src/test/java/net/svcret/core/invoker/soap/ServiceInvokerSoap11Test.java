package net.svcret.core.invoker.soap;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import net.svcret.admin.api.ProcessingException;
import net.svcret.admin.shared.enm.ResponseTypeEnum;
import net.svcret.core.api.IConfigService;
import net.svcret.core.api.IHttpClient;
import net.svcret.core.api.RequestType;
import net.svcret.core.api.SrBeanIncomingRequest;
import net.svcret.core.api.SrBeanIncomingResponse;
import net.svcret.core.api.SrBeanProcessedRequest;
import net.svcret.core.api.SrBeanProcessedResponse;
import net.svcret.core.ejb.HttpClientBean.ClientConfigException;
import net.svcret.core.ex.InvalidRequestException;
import net.svcret.core.invoker.soap.Constants;
import net.svcret.core.invoker.soap.ServiceInvokerSoap11;
import net.svcret.core.model.entity.PersBaseClientAuth;
import net.svcret.core.model.entity.PersBaseServerAuth;
import net.svcret.core.model.entity.PersConfig;
import net.svcret.core.model.entity.PersHttpClientConfig;
import net.svcret.core.model.entity.PersMethod;
import net.svcret.core.model.entity.PersService;
import net.svcret.core.model.entity.PersServiceVersionResource;
import net.svcret.core.model.entity.soap.PersServiceVersionSoap11;
import net.svcret.core.model.entity.soap.PersWsSecUsernameTokenClientAuth;
import net.svcret.core.model.entity.soap.PersWsSecUsernameTokenServerAuth;
import net.svcret.core.util.IOUtils;

import org.hamcrest.core.StringContains;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.stubbing.defaultanswers.ReturnsDeepStubs;

@SuppressWarnings(value= {"static-method"})
public class ServiceInvokerSoap11Test {

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(ServiceInvokerSoap11Test.class);
	private PersHttpClientConfig myHttpConfig;

	@Test
	public void testGetWsdl()  throws Exception {

		PersServiceVersionSoap11 svcVersion = mock(PersServiceVersionSoap11.class);
		
		
		when(svcVersion.getWsdlUrl()).thenReturn("http://the_wsdl_url");
		
		PersServiceVersionResource res = mock(PersServiceVersionResource.class);
		when(res.getPid()).thenReturn(101L);
		when(res.getResourceText()).thenReturn(IOUtils.readClasspathIntoString("/test_simple.wsdl"));
		when(svcVersion.getResourceForUri("http://the_wsdl_url")).thenReturn(res);
		when(svcVersion.getResourceTextForUri("http://the_wsdl_url")).thenReturn(IOUtils.readClasspathIntoString("/test_simple.wsdl"));
		
		when(svcVersion.getPid()).thenReturn(101L);

		when(svcVersion.getResourceTextForUri("bar.xsd")).thenReturn(IOUtils.readClasspathIntoString("/basic_schema.xsd"));
		PersServiceVersionResource resource = mock(PersServiceVersionResource.class);
		when(resource.getResourceText()).thenReturn(IOUtils.readClasspathIntoString("/basic_schema.xsd"));
		when(resource.getPid()).thenReturn(100L);
		when(svcVersion.getResourceForUri("bar.xsd")).thenReturn(resource);
		when(svcVersion.getResourceWithPid(100L)).thenReturn(resource);

		when(svcVersion.getResourceTextForUri("basic_schema2_.xsd")).thenReturn(IOUtils.readClasspathIntoString("/basic_schema2.xsd"));
		PersServiceVersionResource resource2 = mock(PersServiceVersionResource.class);
		when(resource2.getResourceText()).thenReturn(IOUtils.readClasspathIntoString("/basic_schema2.xsd"));
		when(resource2.getPid()).thenReturn(102L);
		when(svcVersion.getResourceForUri("basic_schema2_.xsd")).thenReturn(resource2);
		when(svcVersion.getResourceWithPid(102L)).thenReturn(resource2);

		PersServiceVersionResource wsdlResource = mock(PersServiceVersionResource.class);
		when(wsdlResource.getResourceText()).thenReturn(IOUtils.readClasspathIntoString("/test_simple.wsdl"));
		when(svcVersion.getResourceForUri("http://the_wsdl_url")).thenReturn(wsdlResource);

		IConfigService configService = mock(IConfigService.class);
		ServiceInvokerSoap11 svc = new ServiceInvokerSoap11();
		svc.setConfigService(configService);

		PersConfig config=new PersConfig();
		config.setDefaults();
		config.getProxyUrlBases().iterator().next().setUrlBase("http://foo bar");
		when(configService.getConfig()).thenReturn(config);
		
		
		/*
		 * Load WSDL
		 */
		
		SrBeanIncomingRequest req = new SrBeanIncomingRequest();
		req.setInputReader(new StringReader(""));
		req.setRequestType(RequestType.GET);
		req.setPath("/Some/Path");
		req.setQuery("?wsdl");
		req.addHeader("Content-Type", "text/xml");
		req.setBase("http://localhost:26080");
		req.setContextPath("");
		SrBeanProcessedRequest result = svc.processInvocation(req, svcVersion);

		assertEquals(SrBeanProcessedRequest.ResultTypeEnum.STATIC_RESOURCE, result.getResultType());
		assertEquals(Constants.CONTENT_TYPE_XML, result.getStaticResourceContentTyoe());

//		ourLog.info("Wsdl Outputted:\n{}", result.getStaticResourceText());

		assertTrue(result.getStaticResourceText(), result.getStaticResourceText().contains("<xsd:import namespace=\"urn:2\" schemaLocation=\"http://localhost:26080/Some/Path?xsd&amp;xsdnum=100\"/>"));
		assertEquals("http://the_wsdl_url", result.getStaticResourceUrl());
		
		assertTrue(result.getStaticResourceText().contains("<wsdlsoap:address location=\"http://localhost:26080/Some/Path\"/>"));

		/*
		 * Load XSD
		 */
		
		req = new SrBeanIncomingRequest();
		req.setInputReader(new StringReader(""));
		req.setRequestType(RequestType.GET);
		req.setPath("/Some/Path");
		req.setQuery("?xsd&xsdnum=100");
		req.addHeader("Content-Type", "text/xml");
		req.setBase("http://localhost:26080");
		req.setContextPath("");
		result = svc.processInvocation(req, svcVersion);

		assertEquals(SrBeanProcessedRequest.ResultTypeEnum.STATIC_RESOURCE, result.getResultType());
		assertEquals(Constants.CONTENT_TYPE_XML, result.getStaticResourceContentTyoe());

		ourLog.info("XSD Outputted:\n{}", result.getStaticResourceText());

		assertThat(result.getStaticResourceText(), StringContains.containsString("schemaLocation=\"http://localhost:26080/Some/Path?xsd&amp;xsdnum=102\"/>"));

	}

	@Test
	public void testCreateWsdlBundle()  throws Exception {

		PersServiceVersionSoap11 svcVersion = mock(PersServiceVersionSoap11.class, new ReturnsDeepStubs());
		when(svcVersion.getService().getServiceId()).thenReturn("SVCID");
		when(svcVersion.getVersionId()).thenReturn("VID");
		
		
		when(svcVersion.getWsdlUrl()).thenReturn("http://the_wsdl_url");
		
		PersServiceVersionResource res = mock(PersServiceVersionResource.class);
		when(res.getResourceText()).thenReturn(IOUtils.readClasspathIntoString("/test_simple.wsdl"));
		when(svcVersion.getResourceForUri("http://the_wsdl_url")).thenReturn(res);
		when(svcVersion.getResourceTextForUri("http://the_wsdl_url")).thenReturn(IOUtils.readClasspathIntoString("/test_simple.wsdl"));
		
		when(svcVersion.getResourceTextForUri("bar.xsd")).thenReturn(IOUtils.readClasspathIntoString("/basic_schema.xsd"));
		when(svcVersion.getPid()).thenReturn(101L);

		PersServiceVersionResource resource = mock(PersServiceVersionResource.class);
		when(resource.getResourceText()).thenReturn(IOUtils.readClasspathIntoString("/basic_schema.xsd"));
		when(resource.getPid()).thenReturn(100L);
		when(svcVersion.getResourceForUri("bar.xsd")).thenReturn(resource);

		PersServiceVersionResource wsdlResource = mock(PersServiceVersionResource.class);
		when(wsdlResource.getResourceText()).thenReturn(IOUtils.readClasspathIntoString("/test_simple.wsdl"));
		when(svcVersion.getResourceForUri("http://the_wsdl_url")).thenReturn(wsdlResource);

		IConfigService configService = mock(IConfigService.class);
		ServiceInvokerSoap11 svc = new ServiceInvokerSoap11();
		svc.setConfigService(configService);

		PersConfig config=new PersConfig();
		config.setDefaults();
		config.getProxyUrlBases().iterator().next().setUrlBase("http://foo bar");
		when(configService.getConfig()).thenReturn(config);
		
		
		
		byte[] wsdlBundle = svc.createWsdlBundle(svcVersion);
		ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(wsdlBundle));
		
		ourLog.info("ZIPPED BYTES: " + wsdlBundle.length);
		
		ZipEntry wsdlEntry = zis.getNextEntry();
		assertEquals("SVCID_VID.wsdl",wsdlEntry.getName());
		
		ZipEntry xsdEntry = zis.getNextEntry();
		assertEquals("SVCID_VID_schema_100.xsd",xsdEntry.getName());
		
//		byte[] bytes = new byte[(int) wsdlEntry.getCompressedSize()];
//		zis.read(bytes);
//		assertEquals(IOUtils.readClasspathIntoString("/test_simple.wsdl"), new String(bytes));
		
	}
	@Test
	public void testGetXsd()  throws Exception {

		PersServiceVersionSoap11 svcVersion = mock(PersServiceVersionSoap11.class);
		when(svcVersion.getWsdlUrl()).thenReturn("http://the_wsdl_url");
		when(svcVersion.getResourceTextForUri("http://the_wsdl_url")).thenReturn(IOUtils.readClasspathIntoString("/test_simple.wsdl"));
		when(svcVersion.getResourceTextForUri("bar.xsd")).thenReturn(IOUtils.readClasspathIntoString("/basic_schema.xsd"));
		when(svcVersion.getPid()).thenReturn(101L);

		PersServiceVersionResource resource = mock(PersServiceVersionResource.class);
		when(resource.getResourceText()).thenReturn(IOUtils.readClasspathIntoString("/basic_schema.xsd"));
		when(resource.getResourceUrl()).thenReturn("bar.xsd");
		when(resource.getPid()).thenReturn(100L);
		when(svcVersion.getResourceWithPid(100L)).thenReturn(resource);
		when(svcVersion.getResourceForUri("bar.xsd")).thenReturn(resource);

//		when(svcVersion.getResourceTextForUri("basic_schema2_.xsd")).thenReturn(IOUtils.readClasspathIntoString("/basic_schema2.xsd"));
		resource = mock(PersServiceVersionResource.class);
//		when(resource.getResourceText()).thenReturn(IOUtils.readClasspathIntoString("/basic_schema2.xsd"));
		when(resource.getPid()).thenReturn(102L);
		when(svcVersion.getResourceForUri("basic_schema2_.xsd")).thenReturn(resource);
		when(svcVersion.getResourceWithPid(102L)).thenReturn(resource);

		PersServiceVersionResource wsdlResource = mock(PersServiceVersionResource.class);
		when(svcVersion.getResourceForUri("http://the_wsdl_url")).thenReturn(wsdlResource);

		ServiceInvokerSoap11 svc = new ServiceInvokerSoap11();

		SrBeanIncomingRequest req = new SrBeanIncomingRequest();
		req.setInputReader(new StringReader(""));
		req.setRequestType(RequestType.GET);
		req.setPath("/Some/Path");
		req.setQuery("?xsd&xsdnum=100");
		SrBeanProcessedRequest result = svc.processInvocation(req, svcVersion);

		
		assertEquals(SrBeanProcessedRequest.ResultTypeEnum.STATIC_RESOURCE, result.getResultType());
		assertEquals(Constants.CONTENT_TYPE_XML, result.getStaticResourceContentTyoe());

		ourLog.info("Xsd Outputted:\n{}", result.getStaticResourceDefinition().getResourceText());


		assertEquals(IOUtils.readClasspathIntoString("/basic_schema.xsd"), result.getStaticResourceDefinition().getResourceText());
		assertEquals("bar.xsd", result.getStaticResourceUrl());
	}

	@Test()
	public void testRequestProcessorBadMethod()  throws Exception {

		String methodName = "getPatientByMrnBAD";
		String msg = RequestPipelineTest.createRequest(methodName, true);

		StringReader reader = new StringReader(msg);

		List<PersBaseClientAuth<?>> clientAuths = new ArrayList<>();
		clientAuths.add(new PersWsSecUsernameTokenClientAuth("theUsername", "thePassword"));

		List<PersBaseServerAuth<?,?>> serverAuths = new ArrayList<>();
		serverAuths.add(new PersWsSecUsernameTokenServerAuth());

		PersServiceVersionSoap11 serviceVer = mock(PersServiceVersionSoap11.class);
		PersService service = mock(PersService.class);
		when(serviceVer.getMethodForRootElementName("http://ws.ehr.uhn.ca:getPatientByMrnBAD")).thenReturn(null);
		when(serviceVer.getService()).thenReturn(service);
		when(service.getServiceName()).thenReturn("MyServiceName");
		when(serviceVer.getClientAuths()).thenReturn(new ArrayList<PersBaseClientAuth<?>>());
		when(serviceVer.getServerAuths()).thenReturn(new ArrayList<PersBaseServerAuth<?,?>>());

		ServiceInvokerSoap11 svc = new ServiceInvokerSoap11();
		try {
			SrBeanIncomingRequest req = new SrBeanIncomingRequest();
			req.setInputReader(reader);
			req.setRequestType(RequestType.POST);
			req.setPath("/Some/Path");
			req.setQuery("");
			svc.processInvocation(req, serviceVer);

			fail();
		} catch (InvalidRequestException e) {
			// good!
		}

		verify(serviceVer, atLeastOnce()).getClientAuths();
		verify(serviceVer, atLeastOnce()).getServerAuths();
		verify(serviceVer, atLeastOnce()).getMethodForRootElementName("http://ws.ehr.uhn.ca:getPatientByMrnBAD");
		verify(serviceVer, atLeastOnce()).getService();
		verify(service, atLeastOnce()).getServiceName();
		verifyNoMoreInteractions(serviceVer, service);
	}

	@Test()
	public void testRequestProcessorGoodMethod()  throws Exception {

		String methodName = "getPatientByMrn";
		String msg = RequestPipelineTest.createRequest(methodName, true);

		StringReader reader = new StringReader(msg);

		List<PersBaseClientAuth<?>> clientAuths = new ArrayList<>();
		clientAuths.add(new PersWsSecUsernameTokenClientAuth("theUsername", "thePassword"));

		List<PersBaseServerAuth<?,?>> serverAuths = new ArrayList<>();
		serverAuths.add(new PersWsSecUsernameTokenServerAuth());
		serverAuths.get(0).setPid(124L);
		
		PersServiceVersionSoap11 serviceVer = mock(PersServiceVersionSoap11.class);
		PersService service = mock(PersService.class);
		PersMethod method = mock(PersMethod.class);
		when(serviceVer.getMethodForRootElementName("http://ws.ehr.uhn.ca:getPatientByMrn")).thenReturn(method);
		when(serviceVer.getClientAuths()).thenReturn(clientAuths);
		when(serviceVer.getServerAuths()).thenReturn(serverAuths);

		ServiceInvokerSoap11 svc = new ServiceInvokerSoap11();
		
		SrBeanIncomingRequest req = new SrBeanIncomingRequest();
		req.setInputReader(reader);
		req.setRequestType(RequestType.POST);
		req.setPath("/Some/Path");
		req.setQuery("");
		req.addHeader("Content-Type", "text/xml");
		SrBeanProcessedRequest result = svc.processInvocation(req, serviceVer);

		
		assertEquals("user", result.getCredentialsInRequest(serverAuths.get(0)).getUsername());
		assertEquals("pass", result.getCredentialsInRequest(serverAuths.get(0)).getPassword());
		
		assertEquals(SrBeanProcessedRequest.ResultTypeEnum.METHOD, result.getResultType());
		
		// Security header doesn't match so don't compare it
		String expected = msg.replace("\n", "").replaceAll(".*<soapenv:Body>", "<soapenv:Body>");
		String actual = result.getMethodRequestBody().replace("\n", "").replaceAll(".*<soapenv:Body>", "<soapenv:Body>");
		ourLog.info("Expecting: {}", expected);
		ourLog.info("Got: {}", actual);
		assertEquals(expected, actual);
		
		assertEquals(Constants.CONTENT_TYPE_XML, result.getMethodContentType());
//		assertTrue(result.getMethodHeaders().keySet().contains("SOAPAction"));
//		assertEquals("", result.getMethodHeaders().get("SOAPAction"));
		
		verify(serviceVer, atLeastOnce()).getClientAuths();
		verify(serviceVer, atLeastOnce()).getServerAuths();
		verify(serviceVer, atLeastOnce()).getMethodForRootElementName("http://ws.ehr.uhn.ca:getPatientByMrn");
		verify(method, atLeast(0)).getServiceVersion();
		verifyNoMoreInteractions(serviceVer, service, method);
	}

	@Test
	public void testProcessInvocationResponseFault() throws Exception {
		
		String msg = "<?xml version=\"1.0\"?>\n" +  //-
				"<env:Envelope  xmlns:env=\"http://www.w3.org/2001/12/soap-envelope\" >\n" + //- 
				"    <env:Body>\n" +  //-
				"        <env:Fault>\n" + //- 
				"\n" +  //-
				"          <env:Code>\n" +  //-
				"            <env:Value>SomeCode</env:Value>\n" + //- 
				"          </env:Code>\n" + //- 
				"\n" +  //-
				"          <env:Reason>\n" +  //-
				"            <env:Text xml:lang=\"en-US\">ColorfulIssue</env:Text>\n" +  //-
				"            <env:Text xml:lang=\"en-GB\">ColourfulIssue</env:Text>\n" + //- 
				"          </env:Reason>\n" + //- 
				"\n" + //-
				"        </env:Fault>\n" + //- 
				"    </env:Body>\n" + //- 
				"</env:Envelope>"; //-
		
		msg = "<SOAP-ENV:Envelope\n" +  //-
				"  xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" + //- 
				"   <SOAP-ENV:Body>\n" +  //-
				"       <SOAP-ENV:Fault>\n" +  //-
				"           <faultcode>SOAP-ENV:Server</faultcode>\n" +  //-
				"           <faultstring>Server Error</faultstring>\n" + //- 
				"           <detail>\n" +  //-
				"               <e:myfaultdetails xmlns:e=\"Some-URI\">\n" + //- 
				"                 <message>\n" +  //-
				"                   My application didn't work\n" + //- 
				"                 </message>\n" +  //-
				"                 <errorcode>\n" + //- 
				"                   1001\n" +  //-
				"                 </errorcode>\n" +  //-
				"               </e:myfaultdetails>\n" + //- 
				"           </detail>\n" +  //-
				"       </SOAP-ENV:Fault>\n" + //- 
				"   </SOAP-ENV:Body>\n" + //- 
				"</SOAP-ENV:Envelope>"; //-
		
		SrBeanIncomingResponse httpResponse = mock(SrBeanIncomingResponse.class);
		when(httpResponse.getBody()).thenReturn(msg);
		when(httpResponse.getContentType()).thenReturn("text/xml");
		
		Map<String, List<String>> headers = new HashMap<>();
		when(httpResponse.getHeaders()).thenReturn(headers);
		
		ServiceInvokerSoap11 svc = new ServiceInvokerSoap11();
		SrBeanProcessedResponse response = svc.processInvocationResponse(null, null, httpResponse);
		
		assertEquals(ResponseTypeEnum.FAULT, response.getResponseType());
		assertEquals("SOAP-ENV:Server", response.getResponseFaultCode());
		assertEquals("Server Error", response.getResponseFaultDescription());
		verify(httpResponse, atLeastOnce()).getBody();
		verify(httpResponse, atLeastOnce()).getContentType();
		verify(httpResponse, atLeastOnce()).getHeaders();
		verifyNoMoreInteractions(httpResponse);
	}

	
	
	@Test
	public void testProcessInvocationResponseWrongSoapVersion() throws Exception {
		
		String msg = "<?xml version=\"1.0\"?>\n" +  //-
				"<env:Envelope  xmlns:env=\"http://www.w3.org/2001/12/soap-envelope\" >\n" + //- 
				"    <env:Body>\n" +  //-
				"        <env:Fault>\n" + //- 
				"\n" +  //-
				"          <env:Code>\n" +  //-
				"            <env:Value>SomeCode</env:Value>\n" + //- 
				"          </env:Code>\n" + //- 
				"\n" +  //-
				"          <env:Reason>\n" +  //-
				"            <env:Text xml:lang=\"en-US\">ColorfulIssue</env:Text>\n" +  //-
				"            <env:Text xml:lang=\"en-GB\">ColourfulIssue</env:Text>\n" + //- 
				"          </env:Reason>\n" + //- 
				"\n" + //-
				"        </env:Fault>\n" + //- 
				"    </env:Body>\n" + //- 
				"</env:Envelope>"; //-
		
		
		SrBeanIncomingResponse httpResponse = mock(SrBeanIncomingResponse.class);
		when(httpResponse.getBody()).thenReturn(msg);
		when(httpResponse.getContentType()).thenReturn("text/xml");
		
		Map<String, List<String>> headers = new HashMap<>();
		when(httpResponse.getHeaders()).thenReturn(headers);
		
		ServiceInvokerSoap11 svc = new ServiceInvokerSoap11();
		SrBeanProcessedResponse response = svc.processInvocationResponse(null, null, httpResponse);
		
		assertEquals(ResponseTypeEnum.FAIL, response.getResponseType());
		
	}

	
	@Test
	public void testProcessInvocationResponseFaultWithNoCode() throws Exception {
				
		String msg = "<SOAP-ENV:Envelope\n" +  //-
				"  xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" + //- 
				"   <SOAP-ENV:Body>\n" +  //-
				"       <SOAP-ENV:Fault>\n" +  //-
//				"           <faultcode>SOAP-ENV:Server</faultcode>\n" +  //-
				"           <faultstring>Server Error</faultstring>\n" + //- 
				"           <detail>\n" +  //-
				"               <e:myfaultdetails xmlns:e=\"Some-URI\">\n" + //- 
				"                 <message>\n" +  //-
				"                   My application didn't work\n" + //- 
				"                 </message>\n" +  //-
				"                 <errorcode>\n" + //- 
				"                   1001\n" +  //-
				"                 </errorcode>\n" +  //-
				"               </e:myfaultdetails>\n" + //- 
				"           </detail>\n" +  //-
				"       </SOAP-ENV:Fault>\n" + //- 
				"   </SOAP-ENV:Body>\n" + //- 
				"</SOAP-ENV:Envelope>"; //-
		
		SrBeanIncomingResponse httpResponse = mock(SrBeanIncomingResponse.class);
		when(httpResponse.getBody()).thenReturn(msg);
		when(httpResponse.getContentType()).thenReturn("text/xml");
		
		Map<String, List<String>> headers = new HashMap<>();
		when(httpResponse.getHeaders()).thenReturn(headers);
		
		ServiceInvokerSoap11 svc = new ServiceInvokerSoap11();
		SrBeanProcessedResponse response = svc.processInvocationResponse(null, null, httpResponse);
		
		assertEquals(ResponseTypeEnum.FAULT, response.getResponseType());
		assertEquals(null, response.getResponseFaultCode());
		assertEquals("Server Error", response.getResponseFaultDescription());
		verify(httpResponse, atLeastOnce()).getBody();
		verify(httpResponse, atLeastOnce()).getContentType();
		verify(httpResponse, atLeastOnce()).getHeaders();
		verifyNoMoreInteractions(httpResponse);
	}

	@Test
	public void testProcessInvocationResponseFaultWithEmptyCode() throws Exception {
				
		String msg = "<SOAP-ENV:Envelope\n" +  //-
				"  xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" + //- 
				"   <SOAP-ENV:Body>\n" +  //-
				"       <SOAP-ENV:Fault>\n" +  //-
				"           <faultcode/>\n" +  //-
				"           <faultstring>Server Error</faultstring>\n" + //- 
				"           <detail>\n" +  //-
				"               <e:myfaultdetails xmlns:e=\"Some-URI\">\n" + //- 
				"                 <message>\n" +  //-
				"                   My application didn't work\n" + //- 
				"                 </message>\n" +  //-
				"                 <errorcode>\n" + //- 
				"                   1001\n" +  //-
				"                 </errorcode>\n" +  //-
				"               </e:myfaultdetails>\n" + //- 
				"           </detail>\n" +  //-
				"       </SOAP-ENV:Fault>\n" + //- 
				"   </SOAP-ENV:Body>\n" + //- 
				"</SOAP-ENV:Envelope>"; //-
		
		SrBeanIncomingResponse httpResponse = mock(SrBeanIncomingResponse.class);
		when(httpResponse.getBody()).thenReturn(msg);
		when(httpResponse.getContentType()).thenReturn("text/xml");
		
		Map<String, List<String>> headers = new HashMap<>();
		when(httpResponse.getHeaders()).thenReturn(headers);
		
		ServiceInvokerSoap11 svc = new ServiceInvokerSoap11();
		SrBeanProcessedResponse response = svc.processInvocationResponse(null, null, httpResponse);
		
		assertEquals(ResponseTypeEnum.FAULT, response.getResponseType());
		assertEquals("", response.getResponseFaultCode());
		assertEquals("Server Error", response.getResponseFaultDescription());
		verify(httpResponse, atLeastOnce()).getBody();
		verify(httpResponse, atLeastOnce()).getContentType();
		verify(httpResponse, atLeastOnce()).getHeaders();
		verifyNoMoreInteractions(httpResponse);
	}
	

	@Before
	public void before() {
		
		
		myHttpConfig = new PersHttpClientConfig();
		myHttpConfig.setDefaults();

	}

	@After
	public void after() {
		
		validateMockitoUsage();
	}

	@Test
	public void testIntrospectServiceFromUrl() throws IOException, ProcessingException, ClientConfigException {
		
		ServiceInvokerSoap11 svc = new ServiceInvokerSoap11();
		
		IHttpClient httpClient = mock(IHttpClient.class);
		svc.setHttpClient(httpClient);

		String wsdlBody = IOUtils.readClasspathIntoString("/test_simple.wsdl");
		String wsdlUrl = "http://foo/wsdl.wsdl";
		when(httpClient.getOneTime(any(PersHttpClientConfig.class), eq(wsdlUrl))).thenReturn(new SrBeanIncomingResponse(null, "text/xml", 200, wsdlBody));

		String xsdBody = IOUtils.readClasspathIntoString("/basic_schema.xsd");
		String xsdUrl = "http://foo/bar.xsd";
		when(httpClient.getOneTime(any(PersHttpClientConfig.class), eq(xsdUrl))).thenReturn(new SrBeanIncomingResponse(null, "text/xml", 200, xsdBody));
		
		
		
		PersServiceVersionSoap11 def;
		def = svc.introspectServiceFromUrl(myHttpConfig,wsdlUrl);
		
		assertEquals(def.getMethodNames().toString(), 14, def.getMethods().size());
		assertEquals("getPatientByMrn", def.getMethods().get(0).getName());
		
		Map<String, PersServiceVersionResource> uriToResource = def.getUriToResource();
		assertEquals(wsdlBody, uriToResource.get(wsdlUrl).getResourceText());
		assertEquals("text/xml", uriToResource.get(wsdlUrl).getResourceContentType());
		
		assertEquals(xsdBody, uriToResource.get("bar.xsd").getResourceText());
		
		assertEquals(1, def.getUrls().size());
		assertEquals("http://uhnvesb01d.uhn.on.ca:18780/tst-uhn-ehr-ws/services/ehrPatientService", def.getUrls().get(0).getUrl());
		
		
	}
	
	@Test
	public void testIntrospectServiceFromUrl3() throws Exception {
		ServiceInvokerSoap11 svc = new ServiceInvokerSoap11();
		
		IHttpClient httpClient = mock(IHttpClient.class);
		svc.setHttpClient(httpClient);

		String wsdlBody = IOUtils.readClasspathIntoString("/THIP.wsdl");
		String wsdlUrl = "http://foo/wsdl.wsdl";
		when(httpClient.getOneTime(any(PersHttpClientConfig.class), eq(wsdlUrl))).thenReturn(new SrBeanIncomingResponse(null, "text/xml", 200, wsdlBody));

		PersServiceVersionSoap11 def=null;
		def = svc.introspectServiceFromUrl(myHttpConfig,wsdlUrl);
		
		assertEquals(def.getMethodNames().toString(), 1, def.getMethods().size());
		assertEquals("getErrorQueueWS", def.getMethods().get(0).getName());
		
	}
	
	@Test
	public void testIntrospectServiceFromUrl2() throws Exception {
		
		ServiceInvokerSoap11 svc = new ServiceInvokerSoap11();
		
		IHttpClient httpClient = mock(IHttpClient.class);
		svc.setHttpClient(httpClient);

		String wsdlBody = IOUtils.readClasspathIntoString("/test_simple2.wsdl");
		String wsdlUrl = "http://foo/wsdl.wsdl";
		when(httpClient.getOneTime(any(PersHttpClientConfig.class), eq(wsdlUrl))).thenReturn(new SrBeanIncomingResponse(null, "text/xml", 200, wsdlBody));

		String xsdBody = IOUtils.readClasspathIntoString("/basic_schema2.xsd");
		String xsdUrl = "http://192.168.1.3:8081/DemoServiceSvc/StringConcatService?xsd=1";
		when(httpClient.getOneTime(any(PersHttpClientConfig.class), eq(xsdUrl))).thenReturn(new SrBeanIncomingResponse(null, "text/xml", 200, xsdBody));
		
		PersServiceVersionSoap11 def=null;
		def = svc.introspectServiceFromUrl(myHttpConfig,wsdlUrl);
		
		assertEquals(def.getMethodNames().toString(), 2, def.getMethods().size());
		assertEquals("addStrings", def.getMethods().get(0).getName());
		assertEquals("net:svcret:demo:addStrings", def.getMethods().get(0).getRootElements());
		
		Map<String, PersServiceVersionResource> uriToResource = def.getUriToResource();
		assertEquals(wsdlBody, uriToResource.get(wsdlUrl).getResourceText());
		assertEquals("text/xml", uriToResource.get(wsdlUrl).getResourceContentType());
		
		
	}

	@Test
	public void testIntrospectServiceWithMultipleBindings() throws IOException, ProcessingException, ClientConfigException {
		
		ServiceInvokerSoap11 svc = new ServiceInvokerSoap11();
		
		IHttpClient httpClient = mock(IHttpClient.class);
		svc.setHttpClient(httpClient);

		String wsdlBody = IOUtils.readClasspathIntoString("/cdyne_weatherws.wsdl");
		String wsdlUrl = "http://foo/wsdl.wsdl";
		when(httpClient.getOneTime(any(PersHttpClientConfig.class), eq(wsdlUrl))).thenReturn(new SrBeanIncomingResponse(null, "text/xml", 200, wsdlBody));

		PersServiceVersionSoap11 def=null;
		def = svc.introspectServiceFromUrl(myHttpConfig,wsdlUrl);
		
		assertEquals("GetWeatherInformation", def.getMethods().get(0).getName());
		assertEquals("http://ws.cdyne.com/WeatherWS/:GetWeatherInformation", def.getMethods().get(0).getRootElements());
		
	}

	
	@Test
	public void testIntrospectDocumentServiceFromUrl() throws Throwable {
		
		ServiceInvokerSoap11 svc = new ServiceInvokerSoap11();
		
		IHttpClient httpClient = mock(IHttpClient.class);
		svc.setHttpClient(httpClient);

		String wsdlBody = IOUtils.readClasspathIntoString("/wsdl/journal.wsdl");
		String wsdlUrl = "http://foo/wsdl.wsdl";
		when(httpClient.getOneTime(any(PersHttpClientConfig.class), eq(wsdlUrl))).thenReturn(new SrBeanIncomingResponse(null, "text/xml", 200, wsdlBody));

		when(httpClient.getOneTime(any(PersHttpClientConfig.class), eq("http://foo/journal_1.xsd"))).thenReturn(new SrBeanIncomingResponse(null, "text/xml", 200, IOUtils.readClasspathIntoString("/wsdl/journal_1.xsd")));
		when(httpClient.getOneTime(any(PersHttpClientConfig.class), eq("http://foo/journal_12.xsd"))).thenReturn(new SrBeanIncomingResponse(null, "text/xml", 200, IOUtils.readClasspathIntoString("/wsdl/journal_12.xsd")));
		when(httpClient.getOneTime(any(PersHttpClientConfig.class), eq("http://foo/journal_14.xsd"))).thenReturn(new SrBeanIncomingResponse(null, "text/xml", 200, IOUtils.readClasspathIntoString("/wsdl/journal_14.xsd")));
		when(httpClient.getOneTime(any(PersHttpClientConfig.class), eq("http://foo/journal_4.xsd"))).thenReturn(new SrBeanIncomingResponse(null, "text/xml", 200, IOUtils.readClasspathIntoString("/wsdl/journal_4.xsd")));
		when(httpClient.getOneTime(any(PersHttpClientConfig.class), eq("http://foo/journal_5.xsd"))).thenReturn(new SrBeanIncomingResponse(null, "text/xml", 200, IOUtils.readClasspathIntoString("/wsdl/journal_5.xsd")));
		when(httpClient.getOneTime(any(PersHttpClientConfig.class), eq("http://foo/journal_7.xsd"))).thenReturn(new SrBeanIncomingResponse(null, "text/xml", 200, IOUtils.readClasspathIntoString("/wsdl/journal_7.xsd")));
		
		
		
		PersServiceVersionSoap11 def=null;
		def = svc.introspectServiceFromUrl(myHttpConfig,wsdlUrl);
		
		assertEquals(5, def.getMethods().size());
		
		assertEquals("journal", def.getMethods().get(0).getName());
		assertEquals("urn:sail:xsd:canonical:hl7v2:CanonicalHl7V2MessageElement", def.getMethods().get(0).getRootElements());

		
	}

	
}
