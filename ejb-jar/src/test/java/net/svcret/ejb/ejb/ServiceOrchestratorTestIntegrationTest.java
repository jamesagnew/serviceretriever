package net.svcret.ejb.ejb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.svcret.admin.shared.model.ServiceProtocolEnum;
import net.svcret.admin.shared.model.UrlSelectionPolicy;
import net.svcret.ejb.api.HttpRequestBean;
import net.svcret.ejb.api.HttpResponseBean;
import net.svcret.ejb.api.IBroadcastSender;
import net.svcret.ejb.api.IConfigService;
import net.svcret.ejb.api.IHttpClient;
import net.svcret.ejb.api.IResponseValidator;
import net.svcret.ejb.api.IServiceOrchestrator.OrchestratorResponseBean;
import net.svcret.ejb.api.IThrottlingService;
import net.svcret.ejb.api.ITransactionLogger;
import net.svcret.ejb.api.RequestType;
import net.svcret.ejb.api.UrlPoolBean;
import net.svcret.ejb.ejb.soap.Soap11ServiceInvoker;
import net.svcret.ejb.ex.ProcessingException;
import net.svcret.ejb.model.entity.BasePersServiceVersion;
import net.svcret.ejb.model.entity.PersAuthenticationHostLocalDatabase;
import net.svcret.ejb.model.entity.PersDomain;
import net.svcret.ejb.model.entity.PersHttpClientConfig;
import net.svcret.ejb.model.entity.PersService;
import net.svcret.ejb.model.entity.PersServiceVersionMethod;
import net.svcret.ejb.model.entity.PersServiceVersionUrl;
import net.svcret.ejb.model.entity.PersStickySessionUrlBinding;
import net.svcret.ejb.model.entity.PersUser;
import net.svcret.ejb.model.entity.soap.PersWsSecUsernameTokenServerAuth;

import org.apache.commons.io.FileUtils;
import org.hamcrest.core.IsNot;
import org.hamcrest.core.StringContains;
import org.hamcrest.text.IsEmptyString;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.internal.matchers.CapturingMatcher;

public class ServiceOrchestratorTestIntegrationTest extends BaseJpaTest {

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(ServiceOrchestratorTestIntegrationTest.class);
	private IBroadcastSender myBroadcastSender;
	private IConfigService myConfigService;
	private DaoBean myDao;
	private IHttpClient myHttpClient;
	private LocalDatabaseAuthorizationServiceBean myLocalDbAuthService;
	private RuntimeStatusBean myRuntimeStatus;
	private SecurityServiceBean mySecurityService;
	private ServiceRegistryBean myServiceRegistry;
	private Soap11ServiceInvoker mySoapInvoker;
	private ServiceOrchestratorBean mySvc;
	private PersServiceVersionUrl myUrl;
	private String myTempPath;
	private Long mySvcVerPid;
	private PersServiceVersionUrl myUrl2;

	@Before
	public void before() {
		DefaultAnswer.setDesignTime();
	}

	@SuppressWarnings("null")
	@Test
	public void testSoap11GoodRequest() throws Exception {
		setUpSoap11Test();

		/*
		 * Make request
		 */

		String request = "<soapenv:Envelope xmlns:net=\"net:svcret:demo\" xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">\n"
				+ "   <soapenv:Header>\n"
				+ "      <wsse:Security xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\" xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\">\n"
				+ "         <wsse:UsernameToken wsu:Id=\"UsernameToken-1\">\n" + "            <wsse:Username>test</wsse:Username>\n"
				+ "            <wsse:Password Type=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText\">admin</wsse:Password>\n"
				+ "            <wsse:Nonce EncodingType=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#Base64Binary\">P8ypSWlCHRqR4T1ABYHHbA==</wsse:Nonce>\n"
				+ "            <wsu:Created>2013-04-20T21:18:55.025Z</wsu:Created>\n" + "         </wsse:UsernameToken>\n" + "      </wsse:Security>\n" + "   </soapenv:Header>\n"
				+ "   <soapenv:Body>\n" 
				+ "      <net:d0s0v0m0>\n" 
				+ "          <arg0>FAULT</arg0>\n" 
				+ "          <arg1>?</arg1>\n" 
				+ "      </net:d0s0v0m0>\n" 
				+ "   </soapenv:Body>\n"
				+ "</soapenv:Envelope>";

		String response = "<S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" + "   <S:Body>\n" + "      <ns2:addStringsResponse xmlns:ns2=\"net:svcret:demo\">\n"
				+ "         <return>aFAULT?</return>\n" + "      </ns2:addStringsResponse>\n" + "   </S:Body>\n" + "</S:Envelope>";

		IResponseValidator theResponseValidator = any();
		UrlPoolBean theUrlPool = any();
		String theContentBody = any();
		Map<String, List<String>> theHeaders = any();
		String theContentType = any();
		HttpResponseBean respBean = new HttpResponseBean();
		respBean.setCode(200);
		respBean.setBody(response);
		respBean.setContentType("text/xml");
		respBean.setResponseTime(100);
		respBean.setSuccessfulUrl(myUrl);
		respBean.setHeaders(new HashMap<String, List<String>>());
		PersHttpClientConfig httpClient = any();
		when(myHttpClient.post(httpClient, theResponseValidator, theUrlPool, theContentBody, theHeaders, theContentType)).thenReturn(respBean);

		ITransactionLogger transactionLogger = mock(ITransactionLogger.class);
		mySvc.setTransactionLogger(transactionLogger);

		DefaultAnswer.setRunTime();

		OrchestratorResponseBean resp = null;
		int reps = 100;
		long start = System.currentTimeMillis();
		for (int i = 0; i < reps; i++) {
			String query = "";
			Reader reader = new StringReader(request);
			HttpRequestBean req = new HttpRequestBean();
			req.setRequestType(RequestType.POST);
			req.setRequestHostIp("127.0.0.1");
			req.setPath("/d0/d0s0/d0s0v0");
			req.setQuery(query);
			req.setInputReader(reader);
			req.setRequestTime(new Date());
			req.setRequestHeaders(new HashMap<String, List<String>>());
			resp = mySvc.handleServiceRequest(req);
		}
		long delay = System.currentTimeMillis() - start;
		assertEquals(response, resp.getResponseBody());

		ourLog.info("Did {} reps in {}ms for {}ms/rep", new Object[] { reps, delay, (delay / reps) });

	}

	@Test
	public void testSoap11InvalidRequest() throws Exception {
		setUpSoap11Test();

		/*
		 * Make request
		 */

		//@formatter:off
		String request = 
				"<soapenv2:Envelope xmlns:net=\"net:svcret:demo\" xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">\n"+
				"   <soapenv:Header>\n"+
				"   </soapenv:Header>\n"+
				"   <soapenv:Body></soapenv:Body>\n"+
				"</soapenv:Envelope>";

		String response = 
				"<S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" + 
				"   <S:Body>\n" + 
				"      <ns2:addStringsResponse xmlns:ns2=\"net:svcret:demo\">\n" + 
				"         <return>aFAULT?</return>\n" + 
				"      </ns2:addStringsResponse>\n" + 
				"   </S:Body>\n" + 
				"</S:Envelope>";
		//@formatter:on

		IResponseValidator theResponseValidator = any();
		UrlPoolBean theUrlPool = any();
		String theContentBody = any();
		Map<String, List<String>> theHeaders = any();
		String theContentType = any();
		HttpResponseBean respBean = new HttpResponseBean();
		respBean.setCode(200);
		respBean.setBody(response);
		respBean.setContentType("text/xml");
		respBean.setResponseTime(100);
		respBean.setSuccessfulUrl(myUrl);
		respBean.setHeaders(new HashMap<String, List<String>>());
		PersHttpClientConfig httpClient = any();
		when(myHttpClient.post(httpClient, theResponseValidator, theUrlPool, theContentBody, theHeaders, theContentType)).thenReturn(respBean);

		TransactionLoggerBean logger = new TransactionLoggerBean();
		logger.setDao(myDao);
		mySvc.setTransactionLogger(logger);

		FilesystemAuditLoggerBean fsAuditLogger = new FilesystemAuditLoggerBean();
		fsAuditLogger.setConfigServiceForUnitTests(myConfigService);
		fsAuditLogger.initialize();
		logger.setFilesystemAuditLoggerForUnitTests(fsAuditLogger);

		DefaultAnswer.setRunTime();

		OrchestratorResponseBean resp = null;
		String query = "";
		Reader reader = new StringReader(request);
		HttpRequestBean req = new HttpRequestBean();
		req.setRequestType(RequestType.POST);
		req.setRequestHostIp("127.0.0.1");
		req.setPath("/d0/d0s0/d0s0v0");
		req.setQuery(query);
		req.setInputReader(reader);
		req.setRequestTime(new Date());
		req.setRequestHeaders(new HashMap<String, List<String>>());
		try {
			resp = mySvc.handleServiceRequest(req);
			fail();
		} catch (ProcessingException e) {
			ourLog.info("Message: "+e.getMessage());
			assertThat(e.getMessage(), IsNot.not(IsEmptyString.isEmptyOrNullString()));
		}
		assertNull(resp);

		newEntityManager();

		logger.flush();
		fsAuditLogger.forceFlush();

		newEntityManager();

		FileReader fr = new FileReader(getSvcVerFileName());
		String entireLog = org.apache.commons.io.IOUtils.toString(fr);
		ourLog.info("Journal file: {}", entireLog);

		assertThat(entireLog, StringContains.containsString("</soapenv:Envelope>"));
	}

	/**
	 * Have the invoker throw an NPE
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testSoap11InvokerThrowsException() throws Exception {
		setUpSoap11Test();

		/*
		 * Make request
		 */

		//@formatter:off
		String request = 
				"<soapenv:Envelope xmlns:net=\"net:svcret:demo\" xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">\n"+
				"   <soapenv:Header>\n"+
				"   </soapenv:Header>\n"+
				"   <soapenv:Body></soapenv:Body>\n"+
				"</soapenv:Envelope>";
		//@formatter:on

		TransactionLoggerBean logger = new TransactionLoggerBean();
		logger.setDao(myDao);
		mySvc.setTransactionLogger(logger);

		FilesystemAuditLoggerBean fsAuditLogger = new FilesystemAuditLoggerBean();
		fsAuditLogger.setConfigServiceForUnitTests(myConfigService);
		fsAuditLogger.initialize();
		logger.setFilesystemAuditLoggerForUnitTests(fsAuditLogger);

		DefaultAnswer.setRunTime();

		Soap11ServiceInvoker invoker = mock(Soap11ServiceInvoker.class);
		mySvc.setSoap11ServiceInvoker(invoker);
		when(invoker.processInvocation(any(BasePersServiceVersion.class), any(RequestType.class), any(String.class), any(String.class), any(String.class),any(Reader.class))).thenThrow(NullPointerException.class);
		
		OrchestratorResponseBean resp = null;
		String query = "";
		Reader reader = new StringReader(request);
		HttpRequestBean req = new HttpRequestBean();
		req.setRequestType(RequestType.POST);
		req.setRequestHostIp("127.0.0.1");
		req.setPath("/d0/d0s0/d0s0v0");
		req.setQuery(query);
		req.setInputReader(reader);
		req.setRequestTime(new Date());
		req.setRequestHeaders(new HashMap<String, List<String>>());
		try {
			resp = mySvc.handleServiceRequest(req);
			fail();
		} catch (ProcessingException e) {
			ourLog.info("Message: "+e.getMessage());
			assertThat(e.getMessage(), IsNot.not(IsEmptyString.isEmptyOrNullString()));
		}
		assertNull(resp);

		newEntityManager();

		logger.flush();
		fsAuditLogger.forceFlush();

		newEntityManager();

		FileReader fr = new FileReader(getSvcVerFileName());
		String entireLog = org.apache.commons.io.IOUtils.toString(fr);
		ourLog.info("Journal file: {}", entireLog);

		assertThat(entireLog, StringContains.containsString("</soapenv:Envelope>"));
	}

	private String getSvcVerFileName() {
		for (File next : new File(myTempPath).listFiles()) {
			if (next.getAbsolutePath().contains("svcver_")) {
				return next.getAbsolutePath();
			}
		}
		throw new Error("Failed with: " + Arrays.asList(new File(myTempPath).listFiles()));
	}

	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testSoap11WithStickySession() throws Exception {
		setUpSoap11Test();

		/*
		 * Make request
		 */

		//@formatter:off
		String request = 
				"<soapenv:Envelope xmlns:net=\"net:svcret:demo\" xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">\n"+
				"   <soapenv:Header>\n"+
				"   </soapenv:Header>\n"+
				"   <soapenv:Body>\n" +
				"      <net:d0s0v0m0>\n"+ 
				"          <arg0>FAULT</arg0>\n"+ 
				"          <arg1>?</arg1>\n" +
				"      </net:d0s0v0m0>\n" +
				"   </soapenv:Body>\n"+
				"</soapenv:Envelope>";
		
		String response = 
				"<S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" + 
				"   <S:Body>\n" + 
				"      <ns2:addStringsResponse xmlns:ns2=\"net:svcret:demo\">\n" + 
				"         <return>aFAULT?</return>\n" + 
				"      </ns2:addStringsResponse>\n" + 
				"   </S:Body>\n" + 
				"</S:Envelope>";
		//@formatter:on

		TransactionLoggerBean logger = new TransactionLoggerBean();
		logger.setDao(myDao);
		mySvc.setTransactionLogger(logger);

		FilesystemAuditLoggerBean fsAuditLogger = new FilesystemAuditLoggerBean();
		fsAuditLogger.setConfigServiceForUnitTests(myConfigService);
		fsAuditLogger.initialize();
		logger.setFilesystemAuditLoggerForUnitTests(fsAuditLogger);

		newEntityManager();
		
		PersHttpClientConfig cfg = myServiceRegistry.getServiceVersionByPid(mySvcVerPid).getHttpClientConfig();
		cfg.setUrlSelectionPolicy(UrlSelectionPolicy.RR_STICKY_SESSION);
		cfg.setStickySessionCookieForSessionId("JSESSIONID");
		myServiceRegistry.saveHttpClientConfig(cfg);
		myServiceRegistry.reloadRegistryFromDatabase();

		IResponseValidator theResponseValidator = any();
		UrlPoolBean theUrlPool = any();
		String theContentBody = any();
		Map<String, List<String>> theHeaders = any();
		String theContentType = any();
		HttpResponseBean respBean = new HttpResponseBean();
		respBean.setCode(200);
		respBean.setBody(response);
		respBean.setContentType("text/xml");
		respBean.setResponseTime(100);
		respBean.setSuccessfulUrl(myUrl);
		respBean.setHeaders(new HashMap<String, List<String>>());
		PersHttpClientConfig httpClient = any();
		when(myHttpClient.post(httpClient, theResponseValidator, theUrlPool, theContentBody, theHeaders, theContentType)).thenReturn(respBean);
		
		OrchestratorResponseBean resp = null;
		String query = "";
		HttpRequestBean req = new HttpRequestBean();
		req.setRequestType(RequestType.POST);
		req.setRequestHostIp("127.0.0.1");
		req.setPath("/d0/d0s0/d0s0v0");
		req.setQuery(query);
		
		/*
		 * First query
		 */
		
		req.setInputReader(new StringReader(request));
		req.setRequestTime(new Date());
		req.setRequestHeaders(new HashMap<String, List<String>>());

		resp = mySvc.handleServiceRequest(req);

		ArgumentCaptor<UrlPoolBean> urlPool = ArgumentCaptor.forClass(UrlPoolBean.class);
		ArgumentCaptor<Map> headers = ArgumentCaptor.forClass(Map.class);
		verify(myHttpClient).post(any(PersHttpClientConfig.class), any(IResponseValidator.class), urlPool.capture(), any(String.class), headers.capture(), any(String.class));
		assertEquals(myUrl, urlPool.getValue().getPreferredUrl());
		newEntityManager();

		/*
		 * Second query, no sesion cookie still so we should round robin the URLs. This one
		 * will return a cookie though
		 */
		
		req.setInputReader(new StringReader(request));
		req.setRequestTime(new Date());
		req.setRequestHeaders(new HashMap<String, List<String>>());
		respBean.getHeaders().put("Set-Cookie", Collections.singletonList("JSESSIONID=THE_SESSION_ID"));
		respBean.setSuccessfulUrl(myUrl2);
		
		resp = mySvc.handleServiceRequest(req);

		urlPool = ArgumentCaptor.forClass(UrlPoolBean.class);
		headers = ArgumentCaptor.forClass(Map.class);
		verify(myHttpClient, times(2)).post(any(PersHttpClientConfig.class), any(IResponseValidator.class), urlPool.capture(), any(String.class), headers.capture(), any(String.class));
		assertEquals(myUrl2, urlPool.getAllValues().get(1).getPreferredUrl());
		newEntityManager();

		Collection<PersStickySessionUrlBinding> stickySessions = myDao.getAllStickySessions();
		assertEquals(1, stickySessions.size());

		newEntityManager();

		/*
		 * Third query, should use the second URL because we have a sticky session now
		 */
		
		req.setInputReader(new StringReader(request));
		req.setRequestTime(new Date());
		req.getRequestHeaders().put("Cookie", Collections.singletonList("JSESSIONID=THE_SESSION_ID"));
		
		resp = mySvc.handleServiceRequest(req);

		urlPool = ArgumentCaptor.forClass(UrlPoolBean.class);
		headers = ArgumentCaptor.forClass(Map.class);
		verify(myHttpClient, times(3)).post(any(PersHttpClientConfig.class), any(IResponseValidator.class), urlPool.capture(), any(String.class), headers.capture(), any(String.class));
		assertEquals(myUrl2, urlPool.getAllValues().get(2).getPreferredUrl());
		newEntityManager();

		stickySessions = myDao.getAllStickySessions();
		assertEquals(1, stickySessions.size());

		newEntityManager();

	}
	
	@After
	public void after() throws IOException {
		if (myTempPath != null) {
			FileUtils.deleteDirectory(new File(myTempPath));
		}
	}

	private void setUpSoap11Test() throws ProcessingException, IOException {
		myHttpClient = mock(IHttpClient.class, DefaultAnswer.INSTANCE);
		myBroadcastSender = mock(IBroadcastSender.class);

		File tempFile = File.createTempFile("st-unittest", "");
		tempFile.delete();
		tempFile.mkdirs();
		myTempPath = tempFile.getAbsolutePath();

		myConfigService = mock(IConfigService.class, DefaultAnswer.INSTANCE);
		when(myConfigService.getFilesystemAuditLoggerPath()).thenReturn(myTempPath);

		myDao = new DaoBean();
		myDao.setThisForUnitTest();

		myRuntimeStatus = new RuntimeStatusBean();
		myRuntimeStatus.setDao(myDao);
		myRuntimeStatus.setBroadcastSender(myBroadcastSender);

		myLocalDbAuthService = new LocalDatabaseAuthorizationServiceBean();
		myLocalDbAuthService.setDao(myDao);

		mySecurityService = new SecurityServiceBean();
		mySecurityService.setPersSvc(myDao);
		mySecurityService.setLocalDbAuthService(myLocalDbAuthService);
		mySecurityService.setBroadcastSender(myBroadcastSender);

		mySoapInvoker = new Soap11ServiceInvoker();
		mySoapInvoker.setConfigService(myConfigService);
		mySoapInvoker.setHttpClient(myHttpClient);

		myServiceRegistry = new ServiceRegistryBean();
		myServiceRegistry.setBroadcastSender(myBroadcastSender);
		myServiceRegistry.setDao(myDao);
		myServiceRegistry.setSvcHttpClient(myHttpClient);

		mySvc = new ServiceOrchestratorBean();
		mySvc.setHttpClient(myHttpClient);
		mySvc.setRuntimeStatus(myRuntimeStatus);
		mySvc.setSecuritySvc(mySecurityService);
		mySvc.setSoap11ServiceInvoker(mySoapInvoker);
		mySvc.setSvcRegistry(myServiceRegistry);
		mySvc.setThrottlingService(mock(IThrottlingService.class));

		/*
		 * Start test
		 */

		newEntityManager();

		PersAuthenticationHostLocalDatabase authHost = myDao.getOrCreateAuthenticationHostLocalDatabase("authHost");
		authHost.setCacheSuccessfulCredentialsForMillis(1000000);
		myDao.saveAuthenticationHost(authHost);

		PersUser user = myDao.getOrCreateUser(authHost, "test");
		user.setPassword("admin");
		user.setAllowAllDomains(true);
		user.setAllowSourceIpsAsStrings(Arrays.asList("127.0.0.1"));
		myDao.saveServiceUser(user);

		newEntityManager();
		PersDomain d0 = myServiceRegistry.getOrCreateDomainWithId("d0");
		PersService d0s0 = myServiceRegistry.getOrCreateServiceWithId(d0, "d0s0");
		BasePersServiceVersion d0s0v0 = myServiceRegistry.getOrCreateServiceVersionWithId(d0s0, ServiceProtocolEnum.SOAP11, "d0s0v0");
		PersServiceVersionMethod d0s0v0m0 = new PersServiceVersionMethod();
		d0s0v0m0.setName("d0s0v0m0");
		d0s0v0.addMethod(d0s0v0m0);
		d0s0v0m0.setRootElements("net:svcret:demo:d0s0v0m0");
		PersWsSecUsernameTokenServerAuth serverAuth = new PersWsSecUsernameTokenServerAuth();
		serverAuth.setAuthenticationHost(authHost);
		d0s0v0.addServerAuth(serverAuth);
		PersServiceVersionUrl url = new PersServiceVersionUrl();
		url.setUrlId("url1");
		url.setUrl("http://foo");
		d0s0v0.addUrl(url);

		PersServiceVersionUrl url2 = new PersServiceVersionUrl();
		url2.setUrlId("url2");
		url2.setUrl("http://bar");
		d0s0v0.addUrl(url2);

		d0s0v0.setKeepNumRecentTransactionsFail(100);
		d0s0v0.setKeepNumRecentTransactionsSecurityFail(100);
		d0s0v0.setKeepNumRecentTransactionsFault(100);
		d0s0v0.setKeepNumRecentTransactionsSuccess(100);
		d0s0v0.setAuditLogEnable(true);

		d0s0v0 = myServiceRegistry.saveServiceVersion(d0s0v0);
		newEntityManager();

		myServiceRegistry.reloadRegistryFromDatabase();
		mySecurityService.loadUserCatalogIfNeeded();

		d0s0v0 = myDao.getServiceVersionByPid(d0s0v0.getPid());

		newEntityManager();

		myDao.setEntityManager(null);
		myUrl = d0s0v0.getUrls().get(0);
		myUrl2 = d0s0v0.getUrls().get(1);
		
		mySvcVerPid = d0s0v0.getPid();
	}

	@Override
	protected void newEntityManager() {
		super.newEntityManager();
		myDao.setEntityManager(myEntityManager);
	}

	public static void main(String[] args) throws Exception {
		new ServiceOrchestratorTestIntegrationTest().testSoap11GoodRequest();
	}

}
