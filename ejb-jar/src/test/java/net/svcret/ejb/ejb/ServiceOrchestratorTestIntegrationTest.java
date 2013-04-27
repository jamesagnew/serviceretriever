package net.svcret.ejb.ejb;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Map;

import net.svcret.ejb.api.HttpResponseBean;
import net.svcret.ejb.api.IBroadcastSender;
import net.svcret.ejb.api.IConfigService;
import net.svcret.ejb.api.IHttpClient;
import net.svcret.ejb.api.IResponseValidator;
import net.svcret.ejb.api.IRuntimeStatus;
import net.svcret.ejb.api.IServiceOrchestrator.OrchestratorResponseBean;
import net.svcret.ejb.api.RequestType;
import net.svcret.ejb.api.UrlPoolBean;
import net.svcret.ejb.ejb.soap.Soap11ServiceInvoker;
import net.svcret.ejb.ex.InternalErrorException;
import net.svcret.ejb.ex.ProcessingException;
import net.svcret.ejb.ex.SecurityFailureException;
import net.svcret.ejb.ex.UnknownRequestException;
import net.svcret.ejb.model.entity.PersAuthenticationHostLocalDatabase;
import net.svcret.ejb.model.entity.PersDomain;
import net.svcret.ejb.model.entity.PersService;
import net.svcret.ejb.model.entity.PersServiceVersionMethod;
import net.svcret.ejb.model.entity.PersServiceVersionUrl;
import net.svcret.ejb.model.entity.PersUser;
import net.svcret.ejb.model.entity.soap.PersServiceVersionSoap11;
import net.svcret.ejb.model.entity.soap.PersWsSecUsernameTokenServerAuth;

import org.junit.Test;

public class ServiceOrchestratorTestIntegrationTest extends BaseJpaTest {

	private IHttpClient myHttpClient;
	private RuntimeStatusBean myRuntimeStatus;
	private DaoBean myDao;
	private SecurityServiceBean mySecurityService;
	private IBroadcastSender myBroadcastSender;
	private LocalDatabaseAuthorizationServiceBean myLocalDbAuthService;
	private Soap11ServiceInvoker mySoapInvoker;
	private IConfigService myConfigService;
	private ServiceRegistryBean myServiceRegistry;
	private ServiceOrchestratorBean mySvc;

	@Test
	public void testSoap11GoodRequest() throws ProcessingException, InternalErrorException, UnknownRequestException, IOException, SecurityFailureException {

		myHttpClient = mock(IHttpClient.class, DefaultAnswer.INSTANCE);
		myBroadcastSender = mock(IBroadcastSender.class);
		myConfigService = mock(IConfigService.class, DefaultAnswer.INSTANCE);

		myDao = new DaoBean();

		myRuntimeStatus = new RuntimeStatusBean();
		myRuntimeStatus.setDao(myDao);

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
		myDao.saveServiceUser(user);

		newEntityManager();
		PersDomain d0 = myServiceRegistry.getOrCreateDomainWithId("d0");
		PersService d0s0 = myServiceRegistry.getOrCreateServiceWithId(d0, "d0s0");
		PersServiceVersionSoap11 d0s0v0 = myServiceRegistry.getOrCreateServiceVersionWithId(d0s0, "d0s0v0");
		PersServiceVersionMethod d0s0v0m0 = new PersServiceVersionMethod();
		d0s0v0m0.setName("d0s0v0m0");
		d0s0v0.addMethod(d0s0v0m0);
		PersWsSecUsernameTokenServerAuth serverAuth = new PersWsSecUsernameTokenServerAuth();
		serverAuth.setAuthenticationHost(authHost);
		d0s0v0.addServerAuth(serverAuth);
		PersServiceVersionUrl url = new PersServiceVersionUrl();
		url.setUrlId("url1");
		url.setUrl("http://foo");
		d0s0v0.addUrl(url);
		myServiceRegistry.saveServiceVersion(d0s0v0);
		newEntityManager();

		myServiceRegistry.reloadRegistryFromDatabase();
		mySecurityService.loadUserCatalogIfNeeded();

		newEntityManager();

		myDao.setEntityManager(null);

		/*
		 * Make request
		 */

		String request = "<soapenv:Envelope xmlns:net=\"net:svcret:demo\" xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" + "   <soapenv:Header>\n"
				+ "      <wsse:Security xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\" xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\">\n"
				+ "         <wsse:UsernameToken wsu:Id=\"UsernameToken-1\">\n" + "            <wsse:Username>test</wsse:Username>\n"
				+ "            <wsse:Password Type=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText\">admin</wsse:Password>\n"
				+ "            <wsse:Nonce EncodingType=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#Base64Binary\">P8ypSWlCHRqR4T1ABYHHbA==</wsse:Nonce>\n" + "            <wsu:Created>2013-04-20T21:18:55.025Z</wsu:Created>\n"
				+ "         </wsse:UsernameToken>\n" + "      </wsse:Security>\n" + "   </soapenv:Header>\n" + "   <soapenv:Body>\n" + "      <net:d0s0v0m0>\n" + "                 <arg0>FAULT</arg0>\n" + " <arg1>?</arg1>\n" + "      </net:d0s0v0m0>\n" + "   </soapenv:Body>\n"
				+ "</soapenv:Envelope>";

		String response = "<S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" + "   <S:Body>\n" + "      <ns2:addStringsResponse xmlns:ns2=\"net:svcret:demo\">\n" + "         <return>aFAULT?</return>\n" + "      </ns2:addStringsResponse>\n" + "   </S:Body>\n"
				+ "</S:Envelope>";

		IResponseValidator theResponseValidator = any();
		UrlPoolBean theUrlPool = any();
		String theContentBody = any();
		Map<String, String> theHeaders = any();
		String theContentType = any();
		HttpResponseBean respBean = new HttpResponseBean();
		respBean.setCode(200);
		respBean.setBody(response);
		respBean.setContentType("text/xml");
		respBean.setResponseTime(100);
		respBean.setSuccessfulUrl("http://foo");
		when(myHttpClient.post(theResponseValidator, theUrlPool, theContentBody, theHeaders, theContentType)).thenReturn(respBean);

		OrchestratorResponseBean resp = null;
		int reps = 100;
		long start = System.currentTimeMillis();
		for (int i = 0; i < reps; i++) {
			String query = "";
			Reader reader = new StringReader(request);
			resp = mySvc.handle(RequestType.POST, "/d0/d0s0/vd0s0v0", query, reader);
		}
		long delay = System.currentTimeMillis() - start;
		assertEquals(response, resp.getResponseBody());

		ourLog.info("Did {} reps in {}ms for {}ms/rep", new Object[] {reps, delay, (delay/reps)});
		
	}
private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(ServiceOrchestratorTestIntegrationTest.class);
	@Override
	protected void newEntityManager() {
		super.newEntityManager();
		myDao.setEntityManager(myEntityManager);
	}

        public static void main(String[] args) throws Exception {
            new ServiceOrchestratorTestIntegrationTest().testSoap11GoodRequest();
        }
        
}
