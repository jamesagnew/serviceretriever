package net.svcret.core.integrationtest;

import static net.svcret.core.integrationtest.AdminServiceBeanIntegrationTest.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.activation.DataSource;
import javax.persistence.EntityManager;

import net.svcret.admin.api.ProcessingException;
import net.svcret.admin.api.UnexpectedFailureException;
import net.svcret.admin.shared.enm.ResponseTypeEnum;
import net.svcret.admin.shared.enm.ServerSecurityModeEnum;
import net.svcret.admin.shared.model.BaseDtoServiceVersion;
import net.svcret.admin.shared.model.DtoDomain;
import net.svcret.admin.shared.model.DtoHttpBasicAuthServerSecurity;
import net.svcret.admin.shared.model.DtoHttpClientConfig;
import net.svcret.admin.shared.model.DtoServiceVersionHl7OverHttp;
import net.svcret.admin.shared.model.DtoServiceVersionSoap11;
import net.svcret.admin.shared.model.DtoServiceVersionVirtual;
import net.svcret.admin.shared.model.GHttpClientConfigList;
import net.svcret.admin.shared.model.GResource;
import net.svcret.admin.shared.model.GService;
import net.svcret.admin.shared.model.GServiceMethod;
import net.svcret.admin.shared.model.GServiceVersionUrl;
import net.svcret.admin.shared.model.GWsSecServerSecurity;
import net.svcret.admin.shared.model.GWsSecUsernameTokenClientSecurity;
import net.svcret.admin.shared.model.ModelUpdateRequest;
import net.svcret.admin.shared.model.ModelUpdateResponse;
import net.svcret.admin.shared.model.ServiceProtocolEnum;
import net.svcret.admin.shared.model.UrlSelectionPolicy;
import net.svcret.core.api.IResponseValidator;
import net.svcret.core.api.ISecurityService.AuthorizationResultsBean;
import net.svcret.core.api.RequestType;
import net.svcret.core.api.SrBeanIncomingRequest;
import net.svcret.core.api.SrBeanIncomingResponse;
import net.svcret.core.api.SrBeanOutgoingResponse;
import net.svcret.core.api.SrBeanProcessedRequest;
import net.svcret.core.api.UrlPoolBean;
import net.svcret.core.ejb.BaseJpaTest;
import net.svcret.core.ex.InvalidRequestException;
import net.svcret.core.ex.InvalidRequestException.IssueEnum;
import net.svcret.core.ex.InvocationFailedDueToInternalErrorException;
import net.svcret.core.ex.InvocationRequestFailedException;
import net.svcret.core.ex.InvocationRequestOrResponseFailedException;
import net.svcret.core.ex.InvocationResponseFailedException;
import net.svcret.core.ex.SecurityFailureException;
import net.svcret.core.invoker.soap.ServiceInvokerSoap11;
import net.svcret.core.log.FilesystemAuditLoggerBean;
import net.svcret.core.model.entity.BasePersObject;
import net.svcret.core.model.entity.BasePersServiceVersion;
import net.svcret.core.model.entity.BasePersStats;
import net.svcret.core.model.entity.BasePersStatsPk;
import net.svcret.core.model.entity.PersAuthenticationHostLocalDatabase;
import net.svcret.core.model.entity.PersDomain;
import net.svcret.core.model.entity.PersHttpClientConfig;
import net.svcret.core.model.entity.PersInvocationMethodSvcverStats;
import net.svcret.core.model.entity.PersMethod;
import net.svcret.core.model.entity.PersService;
import net.svcret.core.model.entity.PersServiceVersionRecentMessage;
import net.svcret.core.model.entity.PersServiceVersionUrl;
import net.svcret.core.model.entity.PersStickySessionUrlBinding;
import net.svcret.core.model.entity.PersUser;
import net.svcret.core.model.entity.http.PersHttpBasicServerAuth;
import net.svcret.core.model.entity.soap.PersServiceVersionSoap11;
import net.svcret.core.model.entity.soap.PersWsSecUsernameTokenClientAuth;
import net.svcret.core.model.entity.soap.PersWsSecUsernameTokenServerAuth;
import net.svcret.core.model.entity.virtual.PersServiceVersionVirtual;
import net.svcret.core.orch.ServiceOrchestratorBean;
import net.svcret.core.status.RuntimeStatusBean;
import net.svcret.core.status.RuntimeStatusQueryBean.StatsAccumulator;
import net.svcret.core.throttle.IThrottlingService;
import net.svcret.core.throttle.ThrottleException;
import net.svcret.core.throttle.ThrottleQueueFullException;

import org.apache.commons.io.FileUtils;
import org.hamcrest.collection.IsIterableContainingInAnyOrder;
import org.hamcrest.core.IsNot;
import org.hamcrest.core.StringContains;
import org.hamcrest.text.IsEmptyString;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;

import ca.uhn.hl7v2.parser.PipeParser;

public class ServiceOrchestratorBeanIntegrationTest {

	private static final String WSDL_URL = "http://127.0.0.1/example.wsdl";
	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(ServiceOrchestratorBeanIntegrationTest.class);
	private PersServiceVersionUrl myUrl;
	private String myTempPath;
	private Long mySvcVerPid;
	private PersServiceVersionUrl myUrl2;
	private GServiceMethod myMethod;
	private DtoServiceVersionSoap11 mySvcVer;
	private PersAuthenticationHostLocalDatabase myAuthHost;
	private ServiceOrchestratorBean myOrchSvcUnwrapped;
	private EntityManager ourEntityManager;
	private DtoDomain d0;
	private GService d0s0;

	@Test
	public void testSoap11GoodRequest() throws Exception {

		/*
		 * Make request
		 */

		//@formatter:off
		String request = "<soapenv:Envelope xmlns:net=\"net:svcret:demo\" xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">\n"
				+ "   <soapenv:Header>\n"
				+ "      <wsse:Security xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\" xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\">\n"
				+ "         <wsse:UsernameToken wsu:Id=\"UsernameToken-1\">\n" 
				+ "            <wsse:Username>test</wsse:Username>\n"
				+ "            <wsse:Password Type=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText\">admin</wsse:Password>\n"
				+ "            <wsse:Nonce EncodingType=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#Base64Binary\">P8ypSWlCHRqR4T1ABYHHbA==</wsse:Nonce>\n"
				+ "            <wsu:Created>2013-04-20T21:18:55.025Z</wsu:Created>\n" 
				+ "         </wsse:UsernameToken>\n" 
				+ "      </wsse:Security>\n" 
				+ "   </soapenv:Header>\n"
				+ "   <soapenv:Body>\n" 
				+ "      <net:d0s0v0m0>\n" 
				+ "          <arg0>FAULT</arg0>\n" 
				+ "          <arg1>?</arg1>\n" 
				+ "      </net:d0s0v0m0>\n" 
				+ "   </soapenv:Body>\n"
				+ "</soapenv:Envelope>";

		String response = "<S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" 
				+ "   <S:Body>\n" 
				+ "      <ns2:addStringsResponse xmlns:ns2=\"net:svcret:demo\">\n"
				+ "         <return>aFAULT?</return>\n" 
				+ "      </ns2:addStringsResponse>\n" 
				+ "   </S:Body>\n" 
				+ "</S:Envelope>";
		//@formatter:on

		IResponseValidator theResponseValidator = any();
		UrlPoolBean theUrlPool = any();
		String theContentBody = any();
		Map<String, List<String>> theHeaders = any();
		String theContentType = any();
		SrBeanIncomingResponse respBean = new SrBeanIncomingResponse();
		respBean.setCode(200);
		respBean.setBody(response);
		respBean.setContentType("text/xml");
		respBean.setResponseTime(100);
		respBean.setSuccessfulUrl(myUrl);
		respBean.setHeaders(new HashMap<String, List<String>>());
		PersHttpClientConfig httpClient = any();
		when(ourHttpClientMock.post(httpClient, theResponseValidator, theUrlPool, theContentBody, theHeaders, theContentType)).thenReturn(respBean);

		// ITransactionLogger transactionLogger =
		// mock(ITransactionLogger.class);
		// ourOrchSvc.setTransactionLogger(transactionLogger);

		SrBeanOutgoingResponse resp = provideNull(); // avoid a warning about
														// potential null
		int reps = 100;
		long start = System.currentTimeMillis();
		for (int i = 0; i < 100; i++) {
			String query = "";
			Reader reader = new StringReader(request);
			SrBeanIncomingRequest req = new SrBeanIncomingRequest();
			req.setRequestType(RequestType.POST);
			req.setRequestHostIp("127.0.0.1");
			req.setPath("/d0/d0s0/d0s0v0");
			req.setQuery(query);
			req.setInputReader(reader);
			req.setRequestTime(new Date());
			req.setRequestHeaders(new HashMap<String, List<String>>());
			resp = ourOrchSvc.handleServiceRequest(req);
		}
		long delay = System.currentTimeMillis() - start;
		assertEquals(response, resp.getResponseBody());

		ourLog.info("Did {} reps in {}ms for {}ms/rep", new Object[] { reps, delay, (delay / reps) });

	}

	private SrBeanOutgoingResponse provideNull() {
		return null;
	}

	@Test
	public void testSoap11WithHttpBasicAuth() throws Exception {

		ModelUpdateResponse resp = ourAdminSvc.loadModelUpdate(new ModelUpdateRequest());
		BaseDtoServiceVersion svcVer = resp.getDomainList().getServiceVersionByPid(mySvcVerPid);
		assertEquals(1, svcVer.getServerSecurityList().size());
		
		/*
		 * Make request
		 */

		//@formatter:off
		String request = "<soapenv:Envelope xmlns:net=\"net:svcret:demo\" xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">\n"
				+ "   <soapenv:Header>\n"
				+ "      <wsse:Security xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\" xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\">\n"
				+ "         <wsse:UsernameToken wsu:Id=\"UsernameToken-1\">\n" 
				+ "            <wsse:Username>test</wsse:Username>\n"
				+ "            <wsse:Password Type=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText\">admin</wsse:Password>\n"
				+ "            <wsse:Nonce EncodingType=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#Base64Binary\">P8ypSWlCHRqR4T1ABYHHbA==</wsse:Nonce>\n"
				+ "            <wsu:Created>2013-04-20T21:18:55.025Z</wsu:Created>\n" 
				+ "         </wsse:UsernameToken>\n" 
				+ "      </wsse:Security>\n" 
				+ "   </soapenv:Header>\n"
				+ "   <soapenv:Body>\n" 
				+ "      <net:d0s0v0m0>\n" 
				+ "          <arg0>FAULT</arg0>\n" 
				+ "          <arg1>?</arg1>\n" 
				+ "      </net:d0s0v0m0>\n" 
				+ "   </soapenv:Body>\n"
				+ "</soapenv:Envelope>";

		String response = "<S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" 
				+ "   <S:Body>\n" 
				+ "      <ns2:addStringsResponse xmlns:ns2=\"net:svcret:demo\">\n"
				+ "         <return>aFAULT?</return>\n" 
				+ "      </ns2:addStringsResponse>\n" 
				+ "   </S:Body>\n" 
				+ "</S:Envelope>";
		//@formatter:on

		IResponseValidator theResponseValidator = any();
		UrlPoolBean theUrlPool = any();
		String theContentBody = any();
		Map<String, List<String>> theHeaders = any();
		String theContentType = any();
		SrBeanIncomingResponse respBean = new SrBeanIncomingResponse();
		respBean.setCode(200);
		respBean.setBody(response);
		respBean.setContentType("text/xml");
		respBean.setResponseTime(100);
		respBean.setSuccessfulUrl(myUrl);
		respBean.setHeaders(new HashMap<String, List<String>>());
		PersHttpClientConfig httpClient = any();
		when(ourHttpClientMock.post(httpClient, theResponseValidator, theUrlPool, theContentBody, theHeaders, theContentType)).thenReturn(respBean);

		// ITransactionLogger transactionLogger =
		// mock(ITransactionLogger.class);
		// ourOrchSvc.setTransactionLogger(transactionLogger);

		newEntityManager();

		DtoHttpBasicAuthServerSecurity auth = new DtoHttpBasicAuthServerSecurity();
		auth.setAuthHostPid(myAuthHost.getPid());
		mySvcVer.addServerAuth(auth);
		mySvcVer.setServerSecurityMode(ServerSecurityModeEnum.REQUIRE_ALL);
		mySvcVer = ourAdminSvc.saveServiceVersion(d0.getPid(), d0s0.getPid(), mySvcVer, new ArrayList<GResource>());

		resp = ourAdminSvc.loadModelUpdate(new ModelUpdateRequest());
		svcVer = resp.getDomainList().getServiceVersionByPid(mySvcVerPid);
		assertEquals(2, svcVer.getServerSecurityList().size());

		newEntityManager();

		SrBeanIncomingRequest req = new SrBeanIncomingRequest();
		req.setRequestType(RequestType.POST);
		req.setRequestHostIp("127.0.0.1");
		req.setPath("/d0/d0s0/d0s0v0");
		req.setQuery("");
		req.setInputReader(new StringReader(request));
		req.setRequestTime(new Date());
		req.setRequestHeaders(new HashMap<String, List<String>>());

		try {
			ourOrchSvc.handleServiceRequest(req);
			fail();
		} catch (SecurityFailureException e) {
			// expected
		}

		// Try again with credentials

		req.setInputReader(new StringReader(request));
		req.getRequestHeaders().put("AUTHorization", Collections.singletonList("Basic dGVzdDphZG1pbg=="));
		ourOrchSvc.handleServiceRequest(req);

	}

	@SuppressWarnings("unchecked")
	@Test
	public void testVirtualService() throws Exception {

		/*
		 * Make request
		 */

		//@formatter:off
		String request = "<soapenv:Envelope xmlns:net=\"net:svcret:demo\" xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">\n"
				+ "   <soapenv:Header>\n"
				+ "      <wsse:Security xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\" xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\">\n"
				+ "         <wsse:UsernameToken wsu:Id=\"UsernameToken-1\">\n" 
				+ "            <wsse:Username>test</wsse:Username>\n"
				+ "            <wsse:Password Type=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText\">admin</wsse:Password>\n"
				+ "            <wsse:Nonce EncodingType=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#Base64Binary\">P8ypSWlCHRqR4T1ABYHHbA==</wsse:Nonce>\n"
				+ "            <wsu:Created>2013-04-20T21:18:55.025Z</wsu:Created>\n" 
				+ "         </wsse:UsernameToken>\n" 
				+ "      </wsse:Security>\n" 
				+ "   </soapenv:Header>\n"
				+ "   <soapenv:Body>\n" 
				+ "      <net:d0s0v0m0>\n" 
				+ "          <arg0>FAULT</arg0>\n" 
				+ "          <arg1>?</arg1>\n" 
				+ "      </net:d0s0v0m0>\n" 
				+ "   </soapenv:Body>\n"
				+ "</soapenv:Envelope>";

		String response = "<S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" 
				+ "   <S:Body>\n" 
				+ "      <ns2:addStringsResponse xmlns:ns2=\"net:svcret:demo\">\n"
				+ "         <return>aFAULT?</return>\n" 
				+ "      </ns2:addStringsResponse>\n" 
				+ "   </S:Body>\n" 
				+ "</S:Envelope>";
		//@formatter:on

		ArgumentCaptor<String> contentBodyCaptor = ArgumentCaptor.forClass(String.class);
		SrBeanIncomingResponse respBean = new SrBeanIncomingResponse();
		respBean.setCode(200);
		respBean.setBody(response);
		respBean.setContentType("text/xml");
		respBean.setResponseTime(100);
		respBean.setSuccessfulUrl(myUrl);
		respBean.setHeaders(new HashMap<String, List<String>>());
		when(ourHttpClientMock.post(any(PersHttpClientConfig.class), any(IResponseValidator.class), any(UrlPoolBean.class), contentBodyCaptor.capture(), any(Map.class), any(String.class))).thenReturn(respBean);

		// ITransactionLogger transactionLogger =
		// mock(ITransactionLogger.class);
		// ourOrchSvc.setTransactionLogger(transactionLogger);

		newEntityManager();

		DtoServiceVersionVirtual svcver = new DtoServiceVersionVirtual(mySvcVer.getPid());
		svcver.setId("d0s0v1");
		svcver.setName("d0s0v1");
		svcver.setHttpClientConfigPid(ourAdminSvc.getDefaultHttpClientConfigPid());
		svcver.setTargetServiceVersionPid(mySvcVer.getPid());
		ourAdminSvc.saveServiceVersion(d0.getPid(), d0s0.getPid(), svcver, new ArrayList<GResource>());

		newEntityManager();

		// PersWsSecUsernameTokenServerAuth sa = new
		// PersWsSecUsernameTokenServerAuth();
		// sa.setAuthenticationHost(myDao.getOrCreateAuthenticationHostLocalDatabase("TEST"));
		// mySvcVer.addServerAuth(sa);

		mySvcVer.addClientAuth(new GWsSecUsernameTokenClientSecurity("newuser", "newpass"));

		ourAdminSvc.saveServiceVersion(d0.getPid(), d0s0.getPid(), mySvcVer, new ArrayList<GResource>());

		newEntityManager();

		SrBeanOutgoingResponse resp = null;
		String query = "";
		Reader reader = new StringReader(request);
		SrBeanIncomingRequest req = new SrBeanIncomingRequest();
		req.setRequestType(RequestType.POST);
		req.setRequestHostIp("127.0.0.1");
		req.setPath("/d0/d0s0/d0s0v1");
		req.setQuery(query);
		req.setInputReader(reader);
		req.setRequestTime(new Date());
		req.setRequestHeaders(new HashMap<String, List<String>>());
		resp = ourOrchSvc.handleServiceRequest(req);
		assertEquals(response, resp.getResponseBody());

		String requestBody = contentBodyCaptor.getValue();
		assertThat(requestBody, StringContains.containsString("newuser"));
		assertThat(requestBody, StringContains.containsString("newpass"));

		req.setInputReader(new StringReader(request.replace("<wsse:Username>test</wsse:Username>", "<wsse:Username>test2222</wsse:Username>")));
		try {
			ourOrchSvc.handleServiceRequest(req);
			fail();
		} catch (SecurityFailureException e) {
			// expected
		}

		/*
		 * Check that stats were saved to the virtual service
		 */
		RuntimeStatusBean statsSvc = (RuntimeStatusBean) unwrapProxy(ourStatsSvc);
		ConcurrentHashMap<BasePersStatsPk<?, ?>, BasePersStats<?, ?>> stats = statsSvc.getUnflushedInvocationStatsForUnitTests();
		Collection<BasePersStats<?, ?>> values = new ArrayList<BasePersStats<?, ?>>(stats.values());
		assertEquals(3, stats.size());

		for (BasePersStats<?, ?> next : values) {
			if (next instanceof PersInvocationMethodSvcverStats) {
				PersInvocationMethodSvcverStats nextStats = (PersInvocationMethodSvcverStats) next;
				assertEquals(1, nextStats.getSuccessInvocationCount());
			}
		}

	}

	@Test
	public void testSoap11InvalidGetRequest() throws Exception {

		/*
		 * Make request
		 */

		// IResponseValidator theResponseValidator = any();
		// UrlPoolBean theUrlPool = any();
		// String theContentBody = any();
		// Map<String, List<String>> theHeaders = any();
		// String theContentType = any();
		// SrBeanIncomingResponse respBean = new SrBeanIncomingResponse();
		// respBean.setCode(200);
		// respBean.setBody(response);
		// respBean.setContentType("text/xml");
		// respBean.setResponseTime(100);
		// respBean.setSuccessfulUrl(myUrl);
		// respBean.setHeaders(new HashMap<String, List<String>>());
		// PersHttpClientConfig httpClient = any();
		// when(myHttpClient.post(httpClient, theResponseValidator, theUrlPool,
		// theContentBody, theHeaders, theContentType)).thenReturn(respBean);

		// TransactionLoggerBean logger = new TransactionLoggerBean();
		// logger.setDao(myDao);
		// logger.setConfigServiceForUnitTests(myConfigService);
		// mySvc.setTransactionLogger(logger);

		// FilesystemAuditLoggerBean fsAuditLogger = new
		// FilesystemAuditLoggerBean();
		// fsAuditLogger.setConfigServiceForUnitTests(myConfigService);
		// fsAuditLogger.initialize();
		// logger.setFilesystemAuditLoggerForUnitTests(fsAuditLogger);

		SrBeanOutgoingResponse resp = null;
		String query = "";
		Reader reader = new StringReader("");
		SrBeanIncomingRequest req = new SrBeanIncomingRequest();
		req.setRequestType(RequestType.GET);
		req.setRequestHostIp("127.0.0.1");
		req.setPath("/d0/d0s0/d0s0v0");
		req.setQuery(query);
		req.setInputReader(reader);
		req.setRequestTime(new Date(System.currentTimeMillis() - (60L * 1000L)));
		req.setRequestHeaders(new HashMap<String, List<String>>());
		try {
			resp = ourOrchSvc.handleServiceRequest(req);
			fail();
		} catch (InvalidRequestException e) {
			ourLog.info("Message: " + e.getMessage());
			assertEquals(IssueEnum.UNSUPPORTED_ACTION, e.getIssue());
		}
		assertNull(resp);

		newEntityManager();

		ourTxTemplate.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				try {
					ourTransactionLogSvc.flush();
					ourFilesystemAuditLogger.forceFlush();
				} catch (Exception e) {
					throw new Error(e);
				}
			}
		});

		FileReader fr = new FileReader(getSvcVerFileName());
		String entireLog = org.apache.commons.io.IOUtils.toString(fr);
		ourLog.info("Journal file: {}", entireLog);

		assertThat(entireLog, StringContains.containsString("GET"));

		ourTxTemplate.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				try {
					// Make sure we keep stats
					ourStatsSvc.flushStatus();
					newEntityManager();
				} catch (Exception e) {
					throw new Error(e);
				}
			}
		});

		ourTxTemplate.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				try {
					ourSvcReg.reloadRegistryFromDatabase();
				} catch (Exception e) {
					throw new Error(e);
				}
			}
		});

		ourTxTemplate.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				try {
					PersServiceVersionSoap11 svcVer = (PersServiceVersionSoap11) ourDao.getServiceVersionByPid(mySvcVer.getPid());
					StatsAccumulator stats = ourStatsQuerySvc.extract60MinuteStats(svcVer);
					assertEquals(stats.getFailCounts().size() + " - " + stats.getFailCounts().toString(), 1, stats.getFailCounts().get(58).intValue());
				} catch (Exception e) {
					throw new Error(e);
				}
			}
		});
	}

	@Test
	public void testSoap11InvalidPostRequest() throws Exception {

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
		SrBeanIncomingResponse respBean = new SrBeanIncomingResponse();
		respBean.setCode(200);
		respBean.setBody(response);
		respBean.setContentType("text/xml");
		respBean.setResponseTime(100);
		respBean.setSuccessfulUrl(myUrl);
		respBean.setHeaders(new HashMap<String, List<String>>());
		PersHttpClientConfig httpClient = any();
		when(ourHttpClientMock.post(httpClient, theResponseValidator, theUrlPool, theContentBody, theHeaders, theContentType)).thenReturn(respBean);

		// TransactionLoggerBean logger = new TransactionLoggerBean();
		// logger.setDao(myDao);
		// logger.setConfigServiceForUnitTests(myConfigService);
		// mySvc.setTransactionLogger(logger);

		// FilesystemAuditLoggerBean fsAuditLogger = new
		// FilesystemAuditLoggerBean();
		// fsAuditLogger.setConfigServiceForUnitTests(myConfigService);
		// fsAuditLogger.initialize();
		// logger.setFilesystemAuditLoggerForUnitTests(fsAuditLogger);

		SrBeanOutgoingResponse resp = null;
		String query = "";
		Reader reader = new StringReader(request);
		SrBeanIncomingRequest req = new SrBeanIncomingRequest();
		req.setRequestType(RequestType.POST);
		req.setRequestHostIp("127.0.0.1");
		req.setPath("/d0/d0s0/d0s0v0");
		req.setQuery(query);
		req.setInputReader(reader);
		req.setRequestTime(new Date(System.currentTimeMillis() - (60 * 1000)));
		req.setRequestHeaders(new HashMap<String, List<String>>());
		try {
			resp = ourOrchSvc.handleServiceRequest(req);
			fail();
		} catch (InvocationRequestFailedException e) {
			ourLog.info("Message: " + e.getMessage());
			assertThat(e.getMessage(), IsNot.not(IsEmptyString.isEmptyOrNullString()));
		}
		assertNull(resp);

		newEntityManager();

		ourTransactionLogSvc.flush();
		ourFilesystemAuditLogger.forceFlush();

		newEntityManager();

		FileReader fr = new FileReader(getSvcVerFileName());
		String entireLog = org.apache.commons.io.IOUtils.toString(fr);
		ourLog.info("Journal file: {}", entireLog);

		assertThat(entireLog, StringContains.containsString("</soapenv:Envelope>"));

		// Make sure we keep stats
		ourStatsSvc.flushStatus();
		newEntityManager();

		ourSvcReg.reloadRegistryFromDatabase();

		newEntityManager();

		ourTxTemplate.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				try {
					PersServiceVersionSoap11 svcVer = (PersServiceVersionSoap11) ourDao.getServiceVersionByPid(mySvcVer.getPid());
					StatsAccumulator stats = ourStatsQuerySvc.extract60MinuteStats(svcVer);
					assertEquals(stats.getFailCounts().toString(), 1, stats.getFailCounts().get(58).intValue());
				} catch (Exception e) {
					throw new Error(e);
				}
			}
		});

	}

	@SuppressWarnings("unchecked")
	@Test
	public void testSoap11FailingUrl() throws Exception {

		mySvcVer.setServerSecurityMode(ServerSecurityModeEnum.ALLOW_ANY);
		mySvcVer = ourAdminSvc.saveServiceVersion(d0.getPid(), d0s0.getPid(), mySvcVer, new ArrayList<GResource>());

		newEntityManager();

		/*
		 * Make request
		 */

		//@formatter:off
		String request = "<S:Envelope xmlns:net=\"net:svcret:demo\" xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\">\n"
				+ "   <S:Body>\n" 
				+ "      <net:d0s0v0m0>\n" 
				+ "      </net:d0s0v0m0>\n" 
				+ "   </S:Body>\n"
				+ "</S:Envelope>";

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
		SrBeanIncomingResponse respBean = new SrBeanIncomingResponse();
		respBean.setCode(200);
		respBean.setBody(response);
		respBean.setContentType("text/xml");
		respBean.setResponseTime(100);
		respBean.addFailedUrl(myUrl, "This is the failure explanation", 200, "text/plain", "This is the failure body", 100, new HashMap<String, List<String>>());

		respBean.setHeaders(new HashMap<String, List<String>>());
		PersHttpClientConfig httpClient = any();
		when(ourHttpClientMock.post(httpClient, theResponseValidator, theUrlPool, theContentBody, theHeaders, theContentType)).thenReturn(respBean);

		// TransactionLoggerBean logger = new TransactionLoggerBean();
		// logger.setConfigServiceForUnitTests(myConfigService);
		// logger.setDao(myDao);
		// mySvc.setTransactionLogger(logger);

		// FilesystemAuditLoggerBean fsAuditLogger = new
		// FilesystemAuditLoggerBean();
		// fsAuditLogger.setConfigServiceForUnitTests(myConfigService);
		// fsAuditLogger.initialize();
		// logger.setFilesystemAuditLoggerForUnitTests(fsAuditLogger);

		SrBeanOutgoingResponse resp = null;
		String query = "";
		Reader reader = new StringReader(request);
		SrBeanIncomingRequest req = new SrBeanIncomingRequest();
		req.setRequestType(RequestType.POST);
		req.setRequestHostIp("127.0.0.1");
		req.setPath("/d0/d0s0/d0s0v0");
		req.setQuery(query);
		req.setInputReader(reader);
		req.setRequestTime(new Date());
		req.setRequestHeaders(new HashMap<String, List<String>>());
		try {
			resp = ourOrchSvc.handleServiceRequest(req);
			fail();
		} catch (InvocationFailedDueToInternalErrorException e) {
			ourLog.info("Message: " + e.getMessage());
			assertThat(e.getMessage(), IsNot.not(IsEmptyString.isEmptyOrNullString()));
		}
		assertNull(resp);

		newEntityManager();

		ourStatsSvc.flushStatus();
		ourTransactionLogSvc.flush();
		ourFilesystemAuditLogger.forceFlush();

		newEntityManager();

		ourTxTemplate.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				try {

					PersMethod method = ourDao.getServiceVersionMethodByPid(myMethod.getPid());
					StatsAccumulator accumulator = new StatsAccumulator();
					ourStatsQuerySvc.extract60MinuteMethodStats(method, accumulator);
					assertEquals(Integer.valueOf(1), accumulator.getFailCounts().get(59));

					BasePersServiceVersion svcVer = ourDao.getServiceVersionByPid(mySvcVer.getPid());
					List<PersServiceVersionRecentMessage> recentMessages = ourDao.getServiceVersionRecentMessages(svcVer, ResponseTypeEnum.FAIL);
					assertEquals(1, recentMessages.size());
					assertThat(recentMessages.get(0).getFailDescription(), StringContains.containsString("All service URLs appear to be failing"));
					assertThat(recentMessages.get(0).getFailDescription(), StringContains.containsString("This is the failure explanation"));
					assertThat(recentMessages.get(0).getResponseBody(), StringContains.containsString("This is the failure body"));
				} catch (Exception e) {
					throw new Error(e);
				}
			}
		});

		verify(ourHttpClientMock, times(1)).post(any(PersHttpClientConfig.class), any(IResponseValidator.class), any(UrlPoolBean.class), any(String.class), any(Map.class), any(String.class));

		String svcVerFileName = getSvcVerFileName();
		FileReader fr = new FileReader(svcVerFileName);
		String entireLog = org.apache.commons.io.IOUtils.toString(fr);
		ourLog.info("Journal file: {}", entireLog);

		assertThat(entireLog, StringContains.containsString("</S:Envelope>"));
	}

	@Test
	public void testSoap11SidechannelRequestWithBackingError() throws Exception {

		/*
		 * Make request
		 */

		//@formatter:off
		String request = "<soapenv:Envelope xmlns:net=\"net:svcret:demo\" xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">\n"
				+ "   <soapenv:Header>\n"
				+ "      <wsse:Security xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\" xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\">\n"
				+ "         <wsse:UsernameToken wsu:Id=\"UsernameToken-1\">\n" 
				+ "            <wsse:Username>test</wsse:Username>\n"
				+ "            <wsse:Password Type=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText\">admin</wsse:Password>\n"
				+ "            <wsse:Nonce EncodingType=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#Base64Binary\">P8ypSWlCHRqR4T1ABYHHbA==</wsse:Nonce>\n"
				+ "            <wsu:Created>2013-04-20T21:18:55.025Z</wsu:Created>\n" 
				+ "         </wsse:UsernameToken>\n" 
				+ "      </wsse:Security>\n" 
				+ "   </soapenv:Header>\n"
				+ "   <soapenv:Body>\n" 
				+ "      <net:d0s0v0m0>\n" 
				+ "          <arg0>FAULT</arg0>\n" 
				+ "          <arg1>?</arg1>\n" 
				+ "      </net:d0s0v0m0>\n" 
				+ "   </soapenv:Body>\n"
				+ "</soapenv:Envelope>";

		String response = 
				"500 horrible internal exception";
		//@formatter:on

		IResponseValidator theResponseValidator = any();
		UrlPoolBean theUrlPool = any();
		String theContentBody = any();
		Map<String, List<String>> theHeaders = any();
		String theContentType = any();
		SrBeanIncomingResponse respBean = new SrBeanIncomingResponse();
		respBean.setCode(500);
		respBean.setBody(response);
		respBean.setContentType("text/xml");
		respBean.setResponseTime(100);
		HashMap<String, List<String>> headers = new HashMap<String, List<String>>();
		headers.put("Header0", new ArrayList<String>());
		headers.get("Header0").add("Header0Value");
		respBean.addFailedUrl(myUrl, "This is the failure explanation", 500, "text/xml", response, 100, headers);
		respBean.setHeaders(headers);

		PersHttpClientConfig httpClient = any();
		when(ourHttpClientMock.post(httpClient, theResponseValidator, theUrlPool, theContentBody, theHeaders, theContentType)).thenReturn(respBean);

		// TransactionLoggerBean logger = new TransactionLoggerBean();
		// logger.setConfigServiceForUnitTests(myConfigService);
		// logger.setDao(myDao);
		// mySvc.setTransactionLogger(logger);

		// FilesystemAuditLoggerBean fsAuditLogger = new
		// FilesystemAuditLoggerBean();
		// fsAuditLogger.setConfigServiceForUnitTests(myConfigService);
		// fsAuditLogger.initialize();
		// logger.setFilesystemAuditLoggerForUnitTests(fsAuditLogger);

		String query = "";
		Reader reader = new StringReader(request);
		SrBeanIncomingRequest req = new SrBeanIncomingRequest();
		req.setRequestType(RequestType.POST);
		req.setRequestHostIp("127.0.0.1");
		req.setPath("/d0/d0s0/d0s0v0");
		req.setQuery(query);
		req.setInputReader(reader);
		req.setRequestTime(new Date());
		req.setRequestHeaders(new HashMap<String, List<String>>());

		try {
			ourOrchSvc.handleSidechannelRequest(mySvcVerPid, request, "text/xml", "Admin Console");
			fail();
		} catch (InvocationResponseFailedException e) {
			assertThat(e.getHttpResponse().getBody(), StringContains.containsString("500 horrible "));
			assertThat(e.getHttpResponse().getContentType(), StringContains.containsString("text/xml"));
			assertThat(e.getHttpResponse().getHeaders().get("Header0"), IsIterableContainingInAnyOrder.containsInAnyOrder("Header0Value"));
		}

	}

	/**
	 * Have the invoker throw an NPE
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testSoap11InvokerThrowsException() throws Exception {

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

		// TransactionLoggerBean logger = new TransactionLoggerBean();
		// logger.setDao(myDao);
		// logger.setConfigServiceForUnitTests(myConfigService);
		// mySvc.setTransactionLogger(logger);

		// FilesystemAuditLoggerBean fsAuditLogger = new
		// FilesystemAuditLoggerBean();
		// fsAuditLogger.setConfigServiceForUnitTests(myConfigService);
		// fsAuditLogger.initialize();
		// logger.setFilesystemAuditLoggerForUnitTests(fsAuditLogger);

		ServiceInvokerSoap11 invoker = mock(ServiceInvokerSoap11.class);
		myOrchSvcUnwrapped.setSoap11ServiceInvoker(invoker);

		when(invoker.processInvocation(any(SrBeanIncomingRequest.class), any(BasePersServiceVersion.class))).thenThrow(NullPointerException.class);
		when(invoker.obscureMessageForLogs(any(BasePersServiceVersion.class), eq(request), any(Set.class))).thenReturn("OBSCURED METHOD BODY");

		SrBeanOutgoingResponse resp = null;
		String query = "";
		Reader reader = new StringReader(request);
		SrBeanIncomingRequest req = new SrBeanIncomingRequest();
		req.setRequestType(RequestType.POST);
		req.setRequestHostIp("127.0.0.1");
		req.setPath("/d0/d0s0/d0s0v0");
		req.setQuery(query);
		req.setInputReader(reader);
		req.setRequestTime(new Date());
		req.setRequestHeaders(new HashMap<String, List<String>>());
		try {
			resp = ourOrchSvc.handleServiceRequest(req);
			fail();
		} catch (InvocationFailedDueToInternalErrorException e) {
			ourLog.info("Message: " + e.getMessage());
			assertThat(e.getMessage(), IsNot.not(IsEmptyString.isEmptyOrNullString()));
		}
		assertNull(resp);

		newEntityManager();

		ourTransactionLogSvc.flush();
		ourFilesystemAuditLogger.forceFlush();

		newEntityManager();

		FileReader fr = new FileReader(getSvcVerFileName());
		String entireLog = org.apache.commons.io.IOUtils.toString(fr);
		ourLog.info("Journal file: {}", entireLog);

		assertThat(entireLog, StringContains.containsString("OBSCURED METHOD BODY"));
	}

	private String getSvcVerFileName() {
		for (File next : new File(myTempPath).listFiles()) {
			if (next.getAbsolutePath().contains("svcver_")) {
				return next.getAbsolutePath();
			}
		}
		throw new Error("Failed with: " + Arrays.asList(new File(myTempPath).listFiles()));
	}

	/**
	 * Test that we recover gracefully when a backing service fails
	 */
	@Test
	public void testUnexpectedServiceFailure() throws ThrottleException, ThrottleQueueFullException, InvocationRequestOrResponseFailedException, InvalidRequestException, SecurityFailureException {
		newEntityManager();

		IThrottlingService throttlingServiceMock = mock(IThrottlingService.class);
		myOrchSvcUnwrapped.setThrottlingService(throttlingServiceMock);

		doThrow(NullPointerException.class).when(throttlingServiceMock).applyThrottle(any(SrBeanIncomingRequest.class), any(SrBeanProcessedRequest.class), any(AuthorizationResultsBean.class));

		SrBeanIncomingRequest req = new SrBeanIncomingRequest();
		req.setRequestType(RequestType.GET);
		req.setRequestHostIp("127.0.0.1");
		req.setPath("/d0/d0s0/d0s0v0");
		req.setQuery("?wsdl");
		req.setInputReader(new StringReader(""));
		req.setRequestTime(new Date());
		req.setRequestHeaders(new HashMap<String, List<String>>());

		try {
			ourOrchSvc.handleServiceRequest(req);
			fail();
		} catch (InvocationFailedDueToInternalErrorException e) {
			assertThat(e.getMessage(), StringContains.containsString("NullPointerEx"));
		}

	}

	/**
	 * Test that we recover gracefully when a backing service fails
	 */
	@Test
	public void testSoap11GetWsdl() throws Exception {
		newEntityManager();

		// ThrottlingService throttlingSvc = new ThrottlingService();
		// throttlingSvc.setThisForTesting(throttlingSvc);
		// throttlingSvc.setRuntimeStatusSvcForTesting(ourStatsSvc);
		// throttlingSvc.setServiceOrchestratorForTesting(ourOrchSvc);
		//
		// ourOrchSvc.setThrottlingService(throttlingSvc);

		SrBeanIncomingRequest req = new SrBeanIncomingRequest();
		req.setRequestType(RequestType.GET);
		req.setRequestHostIp("127.0.0.1");
		req.setPath("/d0/d0s0/d0s0v0");
		req.setQuery("?wsdl");
		req.setInputReader(new StringReader(""));
		req.setRequestTime(new Date());
		req.setRequestHeaders(new HashMap<String, List<String>>());

		SrBeanOutgoingResponse rsp = ourOrchSvc.handleServiceRequest(req);
		assertThat(rsp.getResponseBody(), StringContains.containsString("<wsdl/>"));

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testSoap11WithStickySession() throws Exception {

		/*
		 * Make request
		 */

		//@formatter:off
		String request = 
			  "<soapenv:Envelope xmlns:net=\"net:svcret:demo\" xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">\n"
			+ "   <soapenv:Header>\n"
			+ "      <wsse:Security xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\" xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\">\n"
			+ "         <wsse:UsernameToken wsu:Id=\"UsernameToken-1\">\n" 
			+ "            <wsse:Username>test</wsse:Username>\n"
			+ "            <wsse:Password Type=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText\">admin</wsse:Password>\n"
			+ "            <wsse:Nonce EncodingType=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#Base64Binary\">P8ypSWlCHRqR4T1ABYHHbA==</wsse:Nonce>\n"
			+ "            <wsu:Created>2013-04-20T21:18:55.025Z</wsu:Created>\n" 
			+ "         </wsse:UsernameToken>\n" 
			+ "      </wsse:Security>\n" 
			+ "   </soapenv:Header>\n"
			+ "   <soapenv:Body>\n" 
			+ "      <net:d0s0v0m0>\n"
			+ "          <arg0>FAULT</arg0>\n" 
			+ "          <arg1>?</arg1>\n" 
			+ "      </net:d0s0v0m0>\n" 
			+ "   </soapenv:Body>\n"
			+ "</soapenv:Envelope>";

		String response = 
				"<S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" + 
				"   <S:Body>\n" + 
				"      <ns2:addStringsResponse xmlns:ns2=\"net:svcret:demo\">\n" + 
				"         <return>aFAULT?</return>\n" + 
				"      </ns2:addStringsResponse>\n" + 
				"   </S:Body>\n" + 
				"</S:Envelope>";
		//@formatter:on

		newEntityManager();

		ModelUpdateRequest req = new ModelUpdateRequest();
		req.setLoadHttpClientConfigs(true);
		ModelUpdateResponse modelUpdate = ourAdminSvc.loadModelUpdate(req);
		GHttpClientConfigList clientConfigList = modelUpdate.getHttpClientConfigList();
		DtoHttpClientConfig hc = clientConfigList.getConfigByPid(ourAdminSvc.getDefaultHttpClientConfigPid());
		hc.setUrlSelectionPolicy(UrlSelectionPolicy.RR_STICKY_SESSION);
		hc.setStickySessionCookieForSessionId("JSESSIONID");
		ourAdminSvc.saveHttpClientConfig(hc, null,null,null,null);

		IResponseValidator theResponseValidator = any();
		UrlPoolBean theUrlPool = any();
		String theContentBody = any();
		Map<String, List<String>> theHeaders = any();
		String theContentType = any();
		SrBeanIncomingResponse respBean = new SrBeanIncomingResponse();
		respBean.setCode(200);
		respBean.setBody(response);
		respBean.setContentType("text/xml");
		respBean.setResponseTime(100);
		respBean.setSuccessfulUrl(myUrl);
		respBean.setHeaders(new HashMap<String, List<String>>());
		PersHttpClientConfig httpClient = any();
		when(ourHttpClientMock.post(httpClient, theResponseValidator, theUrlPool, theContentBody, theHeaders, theContentType)).thenReturn(respBean);

		String query = "";
		SrBeanIncomingRequest ireq = new SrBeanIncomingRequest();
		ireq.setRequestType(RequestType.POST);
		ireq.setRequestHostIp("127.0.0.1");
		ireq.setPath("/d0/d0s0/d0s0v0");
		ireq.setQuery(query);

		/*
		 * First query
		 */

		ireq.setInputReader(new StringReader(request));
		Date requestTime = new Date();
		ireq.setRequestTime(requestTime);
		ireq.setRequestHeaders(new HashMap<String, List<String>>());

		ourOrchSvc.handleServiceRequest(ireq);

		ArgumentCaptor<UrlPoolBean> urlPool = ArgumentCaptor.forClass(UrlPoolBean.class);
		ArgumentCaptor<Map> headers = ArgumentCaptor.forClass(Map.class);
		verify(ourHttpClientMock).post(any(PersHttpClientConfig.class), any(IResponseValidator.class), urlPool.capture(), any(String.class), headers.capture(), any(String.class));
		assertEquals(myUrl, urlPool.getValue().getPreferredUrl());
		newEntityManager();

		/*
		 * Second query, no sesion cookie still so we should round robin the
		 * URLs. This one will return a cookie though
		 */

		ireq.setInputReader(new StringReader(request));
		requestTime = new Date();
		ireq.setRequestTime(requestTime);
		ireq.setRequestHeaders(new HashMap<String, List<String>>());
		ireq.setRequestHostIp("1.2.3.4");
		respBean.getHeaders().put("Set-Cookie", Collections.singletonList("JSESSIONID=THE_SESSION_ID"));
		respBean.setSuccessfulUrl(myUrl2);

		ourOrchSvc.handleServiceRequest(ireq);

		urlPool = ArgumentCaptor.forClass(UrlPoolBean.class);
		headers = ArgumentCaptor.forClass(Map.class);
		verify(ourHttpClientMock, times(2)).post(any(PersHttpClientConfig.class), any(IResponseValidator.class), urlPool.capture(), any(String.class), headers.capture(), any(String.class));
		assertEquals(myUrl2, urlPool.getAllValues().get(1).getPreferredUrl());
		newEntityManager();

		Collection<PersStickySessionUrlBinding> stickySessions = ourDao.getAllStickySessions();
		assertEquals(1, stickySessions.size());
		assertEquals("1.2.3.4", stickySessions.iterator().next().getRequestingIp());
		assertEquals(myUrl2.getPid(), stickySessions.iterator().next().getUrl().getPid());
		assertEquals(requestTime, stickySessions.iterator().next().getCreated());
		assertEquals(requestTime, stickySessions.iterator().next().getLastAccessed());
		
		Thread.sleep(10); // make sure request times are different

		/*
		 * Third query, should use the second URL because we have a sticky
		 * session now
		 */

		ireq.setInputReader(new StringReader(request));
		Date requestTime2 = new Date();
		ireq.setRequestTime(requestTime2);
		ireq.getRequestHeaders().put("Cookie", Collections.singletonList("JSESSIONID=THE_SESSION_ID"));

		ourOrchSvc.handleServiceRequest(ireq);

		urlPool = ArgumentCaptor.forClass(UrlPoolBean.class);
		headers = ArgumentCaptor.forClass(Map.class);
		verify(ourHttpClientMock, times(3)).post(any(PersHttpClientConfig.class), any(IResponseValidator.class), urlPool.capture(), any(String.class), headers.capture(), any(String.class));
		assertEquals(myUrl2, urlPool.getAllValues().get(2).getPreferredUrl());
		newEntityManager();

		why does this flush automatically?
				
		stickySessions = ourDao.getAllStickySessions();
		assertEquals(1, stickySessions.size());
		assertEquals("1.2.3.4", stickySessions.iterator().next().getRequestingIp());
		assertEquals(myUrl2.getPid(), stickySessions.iterator().next().getUrl().getPid());
		assertEquals(requestTime, stickySessions.iterator().next().getCreated());
		assertEquals(requestTime2, stickySessions.iterator().next().getLastAccessed());

		newEntityManager();

	}

	@BeforeClass
	public static void beforeClass() {
		AdminServiceBeanIntegrationTest.beforeClass();		
	}

	@AfterClass
	public static void afterClass() {
		AdminServiceBeanIntegrationTest.afterClass();
	}

	@After
	public void after() throws IOException, ProcessingException, UnexpectedFailureException {
		if (myTempPath != null) {
			FileUtils.deleteDirectory(new File(myTempPath));
		}
		myOrchSvcUnwrapped.setSoap11ServiceInvoker(ourSoapInvoker);
		myOrchSvcUnwrapped.setThrottlingService(ourThrottlingService);

		ourTxTemplate.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				ourStatsSvc.flushStatus();
			}
		});

		
		AdminServiceBeanIntegrationTest.deleteEverything();
	}

	@Before
	public void before() throws Exception {
		ourLog.info("***** Starting next test *******");
		
		System.setProperty(BasePersObject.NET_SVCRET_UNITTESTMODE, "true");
		AdminServiceBeanIntegrationTest.deleteEverything();

		File tempFile = File.createTempFile("st-unittest", "");
		tempFile.delete();
		tempFile.mkdirs();
		myTempPath = tempFile.getAbsolutePath();
		((FilesystemAuditLoggerBean) unwrapProxy(ourFilesystemAuditLogger)).setAuditPath(tempFile);

		myOrchSvcUnwrapped = (ServiceOrchestratorBean) unwrapProxy(ourOrchSvc);

		/*
		 * Start test
		 */

		newEntityManager();

		ourTxTemplate.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				try {
					myAuthHost = ourDao.getOrCreateAuthenticationHostLocalDatabase("authHost");
					myAuthHost.setCacheSuccessfulCredentialsForMillis(1000000);
					myAuthHost = (PersAuthenticationHostLocalDatabase) ourDao.saveAuthenticationHost(myAuthHost);
				} catch (ProcessingException e) {
					throw new Error(e);
				}
			}
		});

		newEntityManager();

		ourTxTemplate.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				try {
					PersUser user = ourDao.getOrCreateUser(myAuthHost, "test");
					user.setPassword("admin");
					user.setAllowAllDomains(true);
					user.setAllowSourceIpsAsStrings(Arrays.asList(new String[] {"127.0.0.1", "1.2.3.4"}));
					ourDao.saveServiceUser(user);
				} catch (ProcessingException e) {
					throw new Error(e);
				}
			}
		});

		ourTxTemplate.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				ourSecSvc.forceLoadUserCatalog();
			}
		});

		d0 = ourAdminSvc.addDomain(new DtoDomain("d0", "d0"));
		d0s0 = ourAdminSvc.addService(d0.getPid(), new GService("d0s0", "d0s0", true));

		mySvcVer = ourAdminSvc.saveServiceVersion(d0.getPid(), d0s0.getPid(), new DtoServiceVersionSoap11("d0s0v0", "http://wsdlurl", ourAdminSvc.getDefaultHttpClientConfigPid()), new ArrayList<GResource>());
		mySvcVer.setServerSecurityMode(ServerSecurityModeEnum.REQUIRE_ANY);
		GServiceMethod d0s0v0m0 = new GServiceMethod();
		d0s0v0m0.setName("d0s0v0m0");
		mySvcVer.addMethod(d0s0v0m0);
		d0s0v0m0.setRootElements("net:svcret:demo:d0s0v0m0");
		GWsSecServerSecurity serverAuth = new GWsSecServerSecurity();
		serverAuth.setAuthHostPid(myAuthHost.getPid());
		mySvcVer.addServerAuth(serverAuth);
		GServiceVersionUrl url = new GServiceVersionUrl();
		url.setId("url1");
		url.setUrl("http://example.com/foo");
		mySvcVer.addUrl(url);

		GServiceVersionUrl url2 = new GServiceVersionUrl();
		url2.setId("url2");
		url2.setUrl("http://example.com/bar");
		mySvcVer.addUrl(url2);

		mySvcVer.setKeepNumRecentTransactionsFail(100);
		mySvcVer.setKeepNumRecentTransactionsSecurityFail(100);
		mySvcVer.setKeepNumRecentTransactionsFault(100);
		mySvcVer.setKeepNumRecentTransactionsSuccess(100);
		mySvcVer.setAuditLogEnable(true);

		mySvcVer.setWsdlLocation(WSDL_URL);

		ArrayList<GResource> resList = new ArrayList<GResource>();
		resList.add(new GResource(WSDL_URL, "application/xml", "<wsdl/>"));

		mySvcVer = ourAdminSvc.saveServiceVersion(d0.getPid(), d0s0.getPid(), mySvcVer, resList);

		newEntityManager();

		// ourTxTemplate.execute(new TransactionCallbackWithoutResult() {
		// @Override
		// protected void doInTransactionWithoutResult(TransactionStatus status)
		// {
		// try {
		// ourSvcReg.reloadRegistryFromDatabase();
		// ourSecSvc.loadUserCatalogIfNeeded();
		//
		// mySvcVer = (PersServiceVersionSoap11)
		// ourDao.getServiceVersionByPid(mySvcVer.getPid());
		// } catch (Exception e) {
		// throw new Error(e);
		// }
		// }
		// });
		//
		// newEntityManager();

		ourTxTemplate.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				try {
					PersServiceVersionSoap11 svcVer = (PersServiceVersionSoap11) ourDao.getServiceVersionByPid(mySvcVer.getPid());
					myUrl = svcVer.getUrls().get(0);
					myUrl2 = svcVer.getUrls().get(1);
				} catch (Exception e) {
					throw new Error(e);
				}
			}
		});

		myMethod = mySvcVer.getMethodList().get(0);
//		myAuthHost = (PersAuthenticationHostLocalDatabase) ourDao.getAllAuthenticationHosts().iterator().next();
		mySvcVerPid = mySvcVer.getPid();

		ourTxTemplate.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				try {
					ourSvcReg.reloadRegistryFromDatabase();
				} catch (Exception e) {
					throw new Error(e);
				}
			}
		});

		// ourTransactionLogSvc = new TransactionLoggerBean();
		// ourTransactionLogSvc.setDao(ourDao);
		// ourTransactionLogSvc.setConfigServiceForUnitTests(myConfigService);
		// ourOrchSvc.setTransactionLogger(ourTransactionLogSvc);
		//
		// fsAuditLogger = new FilesystemAuditLoggerBean();
		// fsAuditLogger.setConfigServiceForUnitTests(myConfigService);
		// fsAuditLogger.setAuditPath(tempFile);
		// fsAuditLogger.initialize();
		// ourTransactionLogSvc.setFilesystemAuditLoggerForUnitTests(fsAuditLogger);

	}

	@Test
	public void testHl7OverHttpGoodRequest() throws Exception {
		newEntityManager();

		DtoServiceVersionHl7OverHttp http = new DtoServiceVersionHl7OverHttp();
		http.setId("d0s0v1");
		http.setName("d0s0v1");
		http.setHttpClientConfigPid(ourAdminSvc.getDefaultHttpClientConfigPid());
		http.getUrlList().add(new GServiceVersionUrl("url1", "http://example.com/foo"));
		http = ourAdminSvc.saveServiceVersion(d0.getPid(), d0s0.getPid(), http, new ArrayList<GResource>());

		newEntityManager();

		ourSvcReg.reloadRegistryFromDatabase();

		newEntityManager();

		String request = "MSH|^~\\&|DATASERVICES|CORPORATE|||20120711120510.2-0500||ADT^A01^ADT_A01|9c906177-dfca-4bbe-9abd-d8eb43df93a0|D|2.6\r" + // -
				"EVN||20120701000000-0500\r" + // -
				"PID|1||397979797^^^SN^SN~4242^^^BKDMDM^PI~1000^^^YARDI^PI||Williams^Rory^H^^^^A||19641028000000-0600|M||||||||||31592^^^YARDI^AN\r";
		String response = new PipeParser().parse(request).generateACK().encode();

		Reader reader = new StringReader(request);
		SrBeanIncomingRequest req = new SrBeanIncomingRequest();
		req.setRequestType(RequestType.POST);
		req.setRequestHostIp("127.0.0.1");
		req.setPath("/d0/d0s0/d0s0v1");
		req.setQuery("");
		req.setInputReader(reader);
		req.setRequestTime(new Date());
		req.setRequestHeaders(new HashMap<String, List<String>>());
		req.getRequestHeaders().put("Content-Type", Collections.singletonList("application/hl7-v2"));

		IResponseValidator theResponseValidator = any();
		UrlPoolBean theUrlPool = any();
		String theContentBody = any();
		Map<String, List<String>> theHeaders = any();
		String theContentType = any();
		SrBeanIncomingResponse respBean = new SrBeanIncomingResponse();
		respBean.setCode(200);
		respBean.setBody(response);
		respBean.setContentType("application/hl7-v2");
		respBean.setResponseTime(100);
		respBean.setSuccessfulUrl(ourDao.getServiceVersionUrlByPid(http.getUrlList().get(0).getPid()));
		respBean.setHeaders(new HashMap<String, List<String>>());
		PersHttpClientConfig httpClient = any();
		when(ourHttpClientMock.post(httpClient, theResponseValidator, theUrlPool, theContentBody, theHeaders, theContentType)).thenReturn(respBean);

		ourOrchSvc.handleServiceRequest(req);

		newEntityManager();

		ourTxTemplate.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				try {
					ourSvcReg.reloadRegistryFromDatabase();
				} catch (Exception e) {
					throw new Error(e);
				}
			}
		});

		http = (DtoServiceVersionHl7OverHttp) ourAdminSvc.loadModelUpdate(new ModelUpdateRequest()).getDomainList().getServiceVersionByPid(http.getPid());
		assertEquals(1, http.getMethodList().size());
		assertEquals("ADT", http.getMethodList().get(0).getName());

	}

	private void newEntityManager() {
		// if (ourEntityManager != null) {
		// ourEntityManager.flush();
		// ourEntityManager.getTransaction().commit();
		// ourEntityManager.close();
		// }
		//
		// ourEntityManager = ourEntityManagerFactory.createEntityManager();
		// ourEntityManager.getTransaction().begin();
		//
	}

}
