package net.svcret.ejb.invoker.soap;

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

import net.svcret.admin.shared.enm.ResponseTypeEnum;
import net.svcret.ejb.api.HttpResponseBean;
import net.svcret.ejb.api.IConfigService;
import net.svcret.ejb.api.IHttpClient;
import net.svcret.ejb.api.InvocationResponseResultsBean;
import net.svcret.ejb.api.InvocationResultsBean;
import net.svcret.ejb.api.RequestType;
import net.svcret.ejb.ejb.DefaultAnswer;
import net.svcret.ejb.ex.ProcessingException;
import net.svcret.ejb.ex.UnknownRequestException;
import net.svcret.ejb.model.entity.PersBaseClientAuth;
import net.svcret.ejb.model.entity.PersBaseServerAuth;
import net.svcret.ejb.model.entity.PersConfig;
import net.svcret.ejb.model.entity.PersService;
import net.svcret.ejb.model.entity.PersServiceVersionMethod;
import net.svcret.ejb.model.entity.PersServiceVersionResource;
import net.svcret.ejb.model.entity.soap.PersServiceVersionSoap11;
import net.svcret.ejb.model.entity.soap.PersWsSecUsernameTokenClientAuth;
import net.svcret.ejb.model.entity.soap.PersWsSecUsernameTokenServerAuth;
import net.svcret.ejb.util.IOUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.stubbing.defaultanswers.ReturnsDeepStubs;


public class ServiceInvokerSoap11Test {

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(ServiceInvokerSoap11Test.class);

	@Test
	public void testGetWsdl()  throws Exception {

		PersServiceVersionSoap11 svcVersion = mock(PersServiceVersionSoap11.class, new DefaultAnswer());
		
		DefaultAnswer.setDesignTime();
		when(svcVersion.getWsdlUrl()).thenReturn("http://the_wsdl_url");
		
		PersServiceVersionResource res = mock(PersServiceVersionResource.class, new DefaultAnswer());
		when(res.getResourceText()).thenReturn(IOUtils.readClasspathIntoString("/test_simple.wsdl"));
		when(svcVersion.getResourceForUri("http://the_wsdl_url")).thenReturn(res);
		when(svcVersion.getResourceTextForUri("http://the_wsdl_url")).thenReturn(IOUtils.readClasspathIntoString("/test_simple.wsdl"));
		
		
		when(svcVersion.getResourceTextForUri("bar.xsd")).thenReturn(IOUtils.readClasspathIntoString("/basic_schema.xsd"));
		when(svcVersion.getPid()).thenReturn(101L);

		PersServiceVersionResource resource = mock(PersServiceVersionResource.class, new DefaultAnswer());
		when(res.getResourceText()).thenReturn(IOUtils.readClasspathIntoString("/basic_schema.xsd"));
		when(resource.getPid()).thenReturn(100L);
		when(svcVersion.getResourceForUri("bar.xsd")).thenReturn(resource);

		PersServiceVersionResource wsdlResource = mock(PersServiceVersionResource.class, new DefaultAnswer());
		when(wsdlResource.getResourceText()).thenReturn(IOUtils.readClasspathIntoString("/test_simple.wsdl"));
		when(svcVersion.getResourceForUri("http://the_wsdl_url")).thenReturn(wsdlResource);

		IConfigService configService = mock(IConfigService.class);
		ServiceInvokerSoap11 svc = new ServiceInvokerSoap11();
		svc.setConfigService(configService);

		PersConfig config=new PersConfig();
		config.setDefaults();
		config.getProxyUrlBases().iterator().next().setUrlBase("http://foo bar");
		when(configService.getConfig()).thenReturn(config);
		
		DefaultAnswer.setRunTime();
		
		InvocationResultsBean result = svc.processInvocation(svcVersion, RequestType.GET, "/Some/Path", "?wsdl", "text/xml", new StringReader(""));

		assertEquals(InvocationResultsBean.ResultTypeEnum.STATIC_RESOURCE, result.getResultType());
		assertEquals(Constants.CONTENT_TYPE_XML, result.getStaticResourceContentTyoe());

		ourLog.info("Wsdl Outputted:\n{}", result.getStaticResourceText());

		assertTrue(result.getStaticResourceText().contains("<xsd:import namespace=\"urn:2\" schemaLocation=\"http://foo%20bar/Some/Path?xsd&amp;xsdnum=100\"/>"));
		assertEquals("http://the_wsdl_url", result.getStaticResourceUrl());
		
		assertTrue(result.getStaticResourceText().contains("<wsdlsoap:address location=\"http://foo%20bar/Some/Path\"/>"));
		
	}

	@Test
	public void testCreateWsdlBundle()  throws Exception {

		PersServiceVersionSoap11 svcVersion = mock(PersServiceVersionSoap11.class, new ReturnsDeepStubs());
		when(svcVersion.getService().getServiceId()).thenReturn("SVCID");
		when(svcVersion.getVersionId()).thenReturn("VID");
		
		DefaultAnswer.setDesignTime();
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
		
		DefaultAnswer.setRunTime();
		
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

		PersServiceVersionResource wsdlResource = mock(PersServiceVersionResource.class);
		when(svcVersion.getResourceForUri("http://the_wsdl_url")).thenReturn(wsdlResource);

		ServiceInvokerSoap11 svc = new ServiceInvokerSoap11();
		InvocationResultsBean result = svc.processInvocation(svcVersion, RequestType.GET, "/Some/Path", "?xsd&xsdnum=100","text/xml",  new StringReader(""));

		assertEquals(InvocationResultsBean.ResultTypeEnum.STATIC_RESOURCE, result.getResultType());
		assertEquals(Constants.CONTENT_TYPE_XML, result.getStaticResourceContentTyoe());

		ourLog.info("Xsd Outputted:\n{}", result.getStaticResourceDefinition().getResourceText());

		verify(svcVersion, atLeastOnce()).getResourceWithPid(100L);
		verify(resource, atLeastOnce()).getResourceText();
		verify(resource, atLeastOnce()).getResourceUrl();
		verifyNoMoreInteractions(svcVersion);
		verifyNoMoreInteractions(resource);

		assertEquals(IOUtils.readClasspathIntoString("/basic_schema.xsd"), result.getStaticResourceDefinition().getResourceText());
		assertEquals("bar.xsd", result.getStaticResourceUrl());
	}

	@Test()
	public void testRequestProcessorBadMethod()  throws Exception {

		String methodName = "getPatientByMrnBAD";
		String msg = RequestPipelineTest.createRequest(methodName, true);

		StringReader reader = new StringReader(msg);

		List<PersBaseClientAuth<?>> clientAuths = new ArrayList<PersBaseClientAuth<?>>();
		clientAuths.add(new PersWsSecUsernameTokenClientAuth("theUsername", "thePassword"));

		List<PersBaseServerAuth<?,?>> serverAuths = new ArrayList<PersBaseServerAuth<?,?>>();
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
			svc.processInvocation(serviceVer, RequestType.POST, "/Some/Path", "", "text/xml", reader);
			fail();
		} catch (UnknownRequestException e) {
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

		List<PersBaseClientAuth<?>> clientAuths = new ArrayList<PersBaseClientAuth<?>>();
		clientAuths.add(new PersWsSecUsernameTokenClientAuth("theUsername", "thePassword"));

		List<PersBaseServerAuth<?,?>> serverAuths = new ArrayList<PersBaseServerAuth<?,?>>();
		serverAuths.add(new PersWsSecUsernameTokenServerAuth());
		serverAuths.get(0).setPid(124L);
		
		PersServiceVersionSoap11 serviceVer = mock(PersServiceVersionSoap11.class);
		PersService service = mock(PersService.class);
		PersServiceVersionMethod method = mock(PersServiceVersionMethod.class);
		when(serviceVer.getMethodForRootElementName("http://ws.ehr.uhn.ca:getPatientByMrn")).thenReturn(method);
		when(serviceVer.getClientAuths()).thenReturn(clientAuths);
		when(serviceVer.getServerAuths()).thenReturn(serverAuths);

		ServiceInvokerSoap11 svc = new ServiceInvokerSoap11();
		InvocationResultsBean result = svc.processInvocation(serviceVer, RequestType.POST, "/Some/Path", "", "text/xml", reader);
		
		assertEquals("user", result.getCredentialsInRequest(serverAuths.get(0)).getUsername());
		assertEquals("pass", result.getCredentialsInRequest(serverAuths.get(0)).getPassword());
		
		assertEquals(InvocationResultsBean.ResultTypeEnum.METHOD, result.getResultType());
		
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
		
		HttpResponseBean httpResponse = mock(HttpResponseBean.class);
		when(httpResponse.getBody()).thenReturn(msg);
		when(httpResponse.getContentType()).thenReturn("text/xml");
		
		Map<String, List<String>> headers = new HashMap<String, List<String>>();
		when(httpResponse.getHeaders()).thenReturn(headers);
		
		ServiceInvokerSoap11 svc = new ServiceInvokerSoap11();
		InvocationResponseResultsBean response = svc.processInvocationResponse(httpResponse);
		
		assertEquals(ResponseTypeEnum.FAULT, response.getResponseType());
		assertEquals("SOAP-ENV:Server", response.getResponseFaultCode());
		assertEquals("Server Error", response.getResponseFaultDescription());
		verify(httpResponse, atLeastOnce()).getBody();
		verify(httpResponse, atLeastOnce()).getContentType();
		verify(httpResponse, atLeastOnce()).getHeaders();
		verifyNoMoreInteractions(httpResponse);
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
		
		HttpResponseBean httpResponse = mock(HttpResponseBean.class);
		when(httpResponse.getBody()).thenReturn(msg);
		when(httpResponse.getContentType()).thenReturn("text/xml");
		
		Map<String, List<String>> headers = new HashMap<String, List<String>>();
		when(httpResponse.getHeaders()).thenReturn(headers);
		
		ServiceInvokerSoap11 svc = new ServiceInvokerSoap11();
		InvocationResponseResultsBean response = svc.processInvocationResponse(httpResponse);
		
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
		
		HttpResponseBean httpResponse = mock(HttpResponseBean.class);
		when(httpResponse.getBody()).thenReturn(msg);
		when(httpResponse.getContentType()).thenReturn("text/xml");
		
		Map<String, List<String>> headers = new HashMap<String, List<String>>();
		when(httpResponse.getHeaders()).thenReturn(headers);
		
		ServiceInvokerSoap11 svc = new ServiceInvokerSoap11();
		InvocationResponseResultsBean response = svc.processInvocationResponse(httpResponse);
		
		assertEquals(ResponseTypeEnum.FAULT, response.getResponseType());
		assertEquals("", response.getResponseFaultCode());
		assertEquals("Server Error", response.getResponseFaultDescription());
		verify(httpResponse, atLeastOnce()).getBody();
		verify(httpResponse, atLeastOnce()).getContentType();
		verify(httpResponse, atLeastOnce()).getHeaders();
		verifyNoMoreInteractions(httpResponse);
	}
	

	@Before
	public void before()  throws Exception {
		DefaultAnswer.setDesignTime();
	}

	@After
	public void after() {
		DefaultAnswer.setDesignTime();
		validateMockitoUsage();
	}

	@Test
	public void testIntrospectServiceFromUrl() throws IOException, ProcessingException {
		
		ServiceInvokerSoap11 svc = new ServiceInvokerSoap11();
		
		IHttpClient httpClient = mock(IHttpClient.class, DefaultAnswer.INSTANCE);
		svc.setHttpClient(httpClient);

		String wsdlBody = IOUtils.readClasspathIntoString("/test_simple.wsdl");
		String wsdlUrl = "http://foo/wsdl.wsdl";
		when(httpClient.get(wsdlUrl)).thenReturn(new HttpResponseBean(null, "text/xml", 200, wsdlBody));

		String xsdBody = IOUtils.readClasspathIntoString("/basic_schema.xsd");
		String xsdUrl = "http://foo/bar.xsd";
		when(httpClient.get(xsdUrl)).thenReturn(new HttpResponseBean(null, "text/xml", 200, xsdBody));
		
		DefaultAnswer.setRunTime();
		
		PersServiceVersionSoap11 def;
		def = svc.introspectServiceFromUrl(wsdlUrl);
		
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
	public void testIntrospectServiceFromUrl2() throws IOException, ProcessingException {
		
		ServiceInvokerSoap11 svc = new ServiceInvokerSoap11();
		
		IHttpClient httpClient = mock(IHttpClient.class, DefaultAnswer.INSTANCE);
		svc.setHttpClient(httpClient);

		String wsdlBody = IOUtils.readClasspathIntoString("/test_simple2.wsdl");
		String wsdlUrl = "http://foo/wsdl.wsdl";
		when(httpClient.get(wsdlUrl)).thenReturn(new HttpResponseBean(null, "text/xml", 200, wsdlBody));

		String xsdBody = IOUtils.readClasspathIntoString("/basic_schema2.xsd");
		String xsdUrl = "http://192.168.1.3:8081/DemoServiceSvc/StringConcatService?xsd=1";
		when(httpClient.get(xsdUrl)).thenReturn(new HttpResponseBean(null, "text/xml", 200, xsdBody));
		
		PersServiceVersionSoap11 def;
		def = svc.introspectServiceFromUrl(wsdlUrl);
		
		assertEquals(def.getMethodNames().toString(), 2, def.getMethods().size());
		assertEquals("addStrings", def.getMethods().get(0).getName());
		assertEquals("net:svcret:demo:addStrings", def.getMethods().get(0).getRootElements());
		
		Map<String, PersServiceVersionResource> uriToResource = def.getUriToResource();
		assertEquals(wsdlBody, uriToResource.get(wsdlUrl).getResourceText());
		assertEquals("text/xml", uriToResource.get(wsdlUrl).getResourceContentType());
		
		
	}

	@Test
	public void testIntrospectServiceWithMultipleBindings() throws IOException, ProcessingException {
		
		ServiceInvokerSoap11 svc = new ServiceInvokerSoap11();
		
		IHttpClient httpClient = mock(IHttpClient.class, DefaultAnswer.INSTANCE);
		svc.setHttpClient(httpClient);

		String wsdlBody = IOUtils.readClasspathIntoString("/cdyne_weatherws.wsdl");
		String wsdlUrl = "http://foo/wsdl.wsdl";
		when(httpClient.get(wsdlUrl)).thenReturn(new HttpResponseBean(null, "text/xml", 200, wsdlBody));

		PersServiceVersionSoap11 def;
		def = svc.introspectServiceFromUrl(wsdlUrl);
		
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
		when(httpClient.get(wsdlUrl)).thenReturn(new HttpResponseBean(null, "text/xml", 200, wsdlBody));

		when(httpClient.get("http://foo/journal_1.xsd")).thenReturn(new HttpResponseBean(null, "text/xml", 200, IOUtils.readClasspathIntoString("/wsdl/journal_1.xsd")));
		when(httpClient.get("http://foo/journal_12.xsd")).thenReturn(new HttpResponseBean(null, "text/xml", 200, IOUtils.readClasspathIntoString("/wsdl/journal_12.xsd")));
		when(httpClient.get("http://foo/journal_14.xsd")).thenReturn(new HttpResponseBean(null, "text/xml", 200, IOUtils.readClasspathIntoString("/wsdl/journal_14.xsd")));
		when(httpClient.get("http://foo/journal_4.xsd")).thenReturn(new HttpResponseBean(null, "text/xml", 200, IOUtils.readClasspathIntoString("/wsdl/journal_4.xsd")));
		when(httpClient.get("http://foo/journal_5.xsd")).thenReturn(new HttpResponseBean(null, "text/xml", 200, IOUtils.readClasspathIntoString("/wsdl/journal_5.xsd")));
		when(httpClient.get("http://foo/journal_7.xsd")).thenReturn(new HttpResponseBean(null, "text/xml", 200, IOUtils.readClasspathIntoString("/wsdl/journal_7.xsd")));
		
		DefaultAnswer.setRunTime();
		
		PersServiceVersionSoap11 def;
		def = svc.introspectServiceFromUrl(wsdlUrl);
		
		assertEquals(5, def.getMethods().size());
		
		assertEquals("journal", def.getMethods().get(0).getName());
		assertEquals("urn:sail:xsd:canonical:hl7v2:CanonicalHl7V2MessageElement", def.getMethods().get(0).getRootElements());

		
	}

	
}
