package net.svcret.core.ejb.monitor;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import net.svcret.admin.shared.enm.ResponseTypeEnum;
import net.svcret.admin.shared.enm.ThrottlePeriodEnum;
import net.svcret.admin.shared.model.DtoDomain;
import net.svcret.admin.shared.model.DtoServiceVersionSoap11;
import net.svcret.admin.shared.model.GResource;
import net.svcret.admin.shared.model.GService;
import net.svcret.admin.shared.model.GServiceMethod;
import net.svcret.admin.shared.model.GServiceVersionUrl;
import net.svcret.admin.shared.model.StatusEnum;
import net.svcret.core.admin.AdminServiceBean;
import net.svcret.core.api.IServiceOrchestrator;
import net.svcret.core.api.RequestType;
import net.svcret.core.api.SrBeanIncomingRequest;
import net.svcret.core.api.SrBeanIncomingResponse;
import net.svcret.core.api.SrBeanProcessedRequest;
import net.svcret.core.api.SrBeanProcessedResponse;
import net.svcret.core.api.IServiceOrchestrator.SidechannelOrchestratorResponseBean;
import net.svcret.core.dao.DaoBean;
import net.svcret.core.ejb.BaseJpaTest;
import net.svcret.core.ejb.ConfigServiceBean;
import net.svcret.core.ejb.DefaultAnswer;
import net.svcret.core.ejb.ServiceRegistryBean;
import net.svcret.core.ejb.monitor.IMonitorNotifier;
import net.svcret.core.ejb.monitor.MonitorServiceBean;
import net.svcret.core.ejb.nodecomm.IBroadcastSender;
import net.svcret.core.integrationtest.AdminServiceBeanIntegrationTest;
import net.svcret.core.invoker.soap.IServiceInvokerSoap11;
import net.svcret.core.model.entity.BasePersServiceVersion;
import net.svcret.core.model.entity.PersHttpClientConfig;
import net.svcret.core.model.entity.PersLibraryMessage;
import net.svcret.core.model.entity.PersMethod;
import net.svcret.core.model.entity.PersMonitorRuleActive;
import net.svcret.core.model.entity.PersMonitorRuleActiveCheck;
import net.svcret.core.model.entity.PersMonitorRuleFiring;
import net.svcret.core.model.entity.PersMonitorRulePassive;
import net.svcret.core.model.entity.PersServiceVersionUrl;
import net.svcret.core.security.SecurityServiceBean;
import net.svcret.core.status.RuntimeStatusBean;
import net.svcret.core.status.RuntimeStatusQueryBean;

import org.junit.Before;
import org.junit.Test;

public class MonitorServiceBeanTest extends BaseJpaTest {

	private static final String HTTP_BAR2 = "http://127.0.0.1";
	private static final String HTTP_FOO2 = "http://127.0.0.2";
	private static final String HTTP_URL2 = "http://127.0.0.3";
	private static final String HTTP_URL1 = "http://127.0.0.4";
	private static final String HTTP_BAR = "http://127.0.0.5";
	private static final String HTTP_FOO = "http://127.0.0.6";
	private DaoBean myDao;
	private IBroadcastSender myBroadcastSender;
	private ConfigServiceBean myConfigSvc;
	private AdminServiceBean myOrchSvc;
	private RuntimeStatusBean myStatsSvc;
	private IServiceInvokerSoap11 mySoapInvoker;
	private SecurityServiceBean mySecSvc;
	private ServiceRegistryBean mySvcReg;
	private MonitorServiceBean mySvc;
	private DtoServiceVersionSoap11 mySvcVer1G;
	private BasePersServiceVersion myD1S1V1;
	private PersMethod myD1S1V1M1;
	private PersServiceVersionUrl myD1M1S1U1;
	private PersServiceVersionUrl myD1M1S1U2;
	private RuntimeStatusQueryBean myQuerySvc;
	private DtoServiceVersionSoap11 mySvcVer2G;
	private BasePersServiceVersion myD1S1V2;
	@SuppressWarnings("unused")
	private PersMethod myD1S1V2M1;
	@SuppressWarnings("unused")
	private PersServiceVersionUrl myD1M1S2U1;
	@SuppressWarnings("unused")
	private PersServiceVersionUrl myD1M1S2U2;

	@Before
	public void before2() {
		myDao = new DaoBean();
		myBroadcastSender = mock(IBroadcastSender.class);

		myConfigSvc = new ConfigServiceBean();
		myConfigSvc.setDao(myDao);
		myConfigSvc.setBroadcastSender(myBroadcastSender);
		AdminServiceBeanIntegrationTest.injectConfigServiceDefaults(myConfigSvc);
		
		myOrchSvc = new AdminServiceBean();
		myOrchSvc.setPersSvc(myDao);
		myOrchSvc.setConfigSvc(myConfigSvc);

		myStatsSvc = new RuntimeStatusBean();
		myStatsSvc.setDao(myDao);
		myStatsSvc.setConfigSvc(myConfigSvc);

		myQuerySvc = new RuntimeStatusQueryBean();
		myQuerySvc.setConfigSvcForUnitTest(myConfigSvc);
		myQuerySvc.setStatusSvcForUnitTest(myStatsSvc);
		
		mySoapInvoker = mock(IServiceInvokerSoap11.class, new DefaultAnswer());
		myOrchSvc.setInvokerSoap11(mySoapInvoker);

		mySecSvc = new SecurityServiceBean();
		mySecSvc.setPersSvc(myDao);
		mySecSvc.setBroadcastSender(myBroadcastSender);
		myOrchSvc.setSecuritySvc(mySecSvc);

		mySvcReg = new ServiceRegistryBean();
		mySvcReg.setBroadcastSender(myBroadcastSender);
		mySvcReg.setDao(myDao);
		myOrchSvc.setServiceRegistry(mySvcReg);

		mySvc = new MonitorServiceBean();
		mySvc.setDao(myDao);
		mySvc.setRuntimeStatus(myQuerySvc);
		mySvc.setRuntimeStatus(myStatsSvc);
		mySvc.setConfigServiceForUnitTests(myConfigSvc);
		mySvc.setBroadcastSender(mock(IBroadcastSender.class));

		DefaultAnswer.setDesignTime();
		
//		myNow = new Date();
//		my30SecsAgo = new Date(myNow.getTime() - (30 * DateUtils.MILLIS_PER_SECOND));
//		my1Min30SecsAgo = new Date(myNow.getTime() - (DateUtils.MILLIS_PER_MINUTE) - (30 * DateUtils.MILLIS_PER_SECOND));
//		my2Min30SecsAgo = new Date(myNow.getTime() - (2*DateUtils.MILLIS_PER_MINUTE) - (30 * DateUtils.MILLIS_PER_SECOND));
		
		
	}

	@Override
	protected void newEntityManager() {
		super.newEntityManager();

		myDao.setEntityManager(myEntityManager);
	}

	@Test
	public void testUrlUnavailable() throws Exception {
		createCatalog();

		SrBeanIncomingRequest iReq = new SrBeanIncomingRequest();
		
		PersMonitorRulePassive rule = new PersMonitorRulePassive();
		rule.setRuleActive(true);
		rule.setAppliesToItems(myD1S1V1);
		rule.setPassiveFireIfSingleBackingUrlIsUnavailable(true);
		myDao.saveOrCreateMonitorRule(rule);

		// No calls have happened yet..
		newEntityManager();
		mySvc.runPassiveChecks();
		newEntityManager();
		assertNull(getMostRecentMonitorRuleFiring());

		// Fail one of the URLs
		newEntityManager();
		SrBeanIncomingResponse httpResponse = new SrBeanIncomingResponse();
		httpResponse.setSuccessfulUrl(myD1M1S1U1);
		httpResponse.addFailedUrl(myD1M1S1U2, "failure explanation", 500, "text/plain", "response body",0, null);
		httpResponse.setBody("successful response");
		SrBeanProcessedResponse invocationResponseResultsBean = new SrBeanProcessedResponse();
		invocationResponseResultsBean.setResponseType(ResponseTypeEnum.SUCCESS);
		myStatsSvc.recordInvocationMethod(new Date(), 100, SrBeanProcessedRequest.forUnitTest(myD1S1V1M1), null, httpResponse, invocationResponseResultsBean, iReq);
		newEntityManager();
		myStatsSvc.flushStatus();
		newEntityManager();

		mySvc.runPassiveChecks();
		newEntityManager();
		assertNotNull(getMostRecentMonitorRuleFiring());
		assertNull(getMostRecentMonitorRuleFiring().getEndDate());
		assertEquals(1, getMostRecentMonitorRuleFiring().getProblems().size());
		assertEquals(myD1M1S1U2, getMostRecentMonitorRuleFiring().getProblems().iterator().next().getUrl());
		assertEquals("failure explanation", getMostRecentMonitorRuleFiring().getProblems().iterator().next().getFailedUrlMessage());

		// Succeed the URL again
		newEntityManager();
		httpResponse = new SrBeanIncomingResponse();
		httpResponse.setSuccessfulUrl(myD1M1S1U2);
		httpResponse.setBody("successful response");
		invocationResponseResultsBean = new SrBeanProcessedResponse();
		invocationResponseResultsBean.setResponseType(ResponseTypeEnum.SUCCESS);
		myStatsSvc.recordInvocationMethod(new Date(), 100, SrBeanProcessedRequest.forUnitTest(myD1S1V1M1), null, httpResponse, invocationResponseResultsBean, iReq);
		newEntityManager();
		myStatsSvc.flushStatus();
		newEntityManager();

		mySvc.runPassiveChecks();
		newEntityManager();
		assertNotNull(getMostRecentMonitorRuleFiring().getEndDate());
		
		
	}

	@Test
	public void testLatencyExceedsThreshold() throws Exception {
		createCatalog();

		SrBeanIncomingRequest iReq = new SrBeanIncomingRequest();

		PersMonitorRulePassive rule = new PersMonitorRulePassive();
		rule.setRuleActive(true);
		rule.setAppliesToItems(myD1S1V1);
		rule.setPassiveFireIfSingleBackingUrlIsUnavailable(true);
		myDao.saveOrCreateMonitorRule(rule);

		// No calls have happened yet..
		newEntityManager();
		mySvc.runPassiveChecks();
		newEntityManager();
		assertNull(getMostRecentMonitorRuleFiring());

		// Fail one of the URLs
		newEntityManager();
		SrBeanIncomingResponse httpResponse = new SrBeanIncomingResponse();
		httpResponse.setSuccessfulUrl(myD1M1S1U1);
		httpResponse.addFailedUrl(myD1M1S1U2, "failure explanation", 500, "text/plain", "response body",0, null);
		httpResponse.setBody("successful response");
		SrBeanProcessedResponse invocationResponseResultsBean = new SrBeanProcessedResponse();
		invocationResponseResultsBean.setResponseType(ResponseTypeEnum.SUCCESS);
		myStatsSvc.recordInvocationMethod(new Date(), 100, SrBeanProcessedRequest.forUnitTest(myD1S1V1M1), null, httpResponse, invocationResponseResultsBean, iReq);
		newEntityManager();
		myStatsSvc.flushStatus();
		newEntityManager();

		mySvc.runPassiveChecks();
		newEntityManager();
		assertNotNull(getMostRecentMonitorRuleFiring());
		assertNull(getMostRecentMonitorRuleFiring().getEndDate());

		// Succeed the URL again
		newEntityManager();
		httpResponse = new SrBeanIncomingResponse();
		httpResponse.setSuccessfulUrl(myD1M1S1U2);
		httpResponse.setBody("successful response");
		invocationResponseResultsBean = new SrBeanProcessedResponse();
		invocationResponseResultsBean.setResponseType(ResponseTypeEnum.SUCCESS);
		myStatsSvc.recordInvocationMethod(new Date(), 100, SrBeanProcessedRequest.forUnitTest(myD1S1V1M1), null, httpResponse, invocationResponseResultsBean, iReq);
		newEntityManager();
		myStatsSvc.flushStatus();
		newEntityManager();

		mySvc.runPassiveChecks();
		newEntityManager();
		assertNotNull(getMostRecentMonitorRuleFiring().getEndDate());
		
		
	}


	@Test
	public void testActiveTest() throws Exception{
		createCatalog();

		IServiceOrchestrator orch = mock(IServiceOrchestrator.class);
		mySvc.setServiceOrchestratorForUnitTests(orch);
		mySvc.setMonitorNotifierForUnitTests(mock(IMonitorNotifier.class));
		
		newEntityManager();

		BasePersServiceVersion svcVer = myDao.getServiceVersionByPid(myD1S1V1.getPid());
		assertEquals(StatusEnum.UNKNOWN, svcVer.getUrls().get(0).getStatus().getStatus());
		
		PersLibraryMessage msg = new PersLibraryMessage();
		msg.setAppliesTo(myD1S1V1);
		msg.setContentType("ct");
		msg.setDescription("desc");
		msg.setMessage("body");
		myDao.saveLibraryMessage(msg);
		
		newEntityManager();

		PersMonitorRuleActive rule = new PersMonitorRuleActive();
		rule.setRuleActive(true);
		
		PersMonitorRuleActiveCheck check=new PersMonitorRuleActiveCheck();
		check.setCheckFrequencyNum(1);
		check.setCheckFrequencyUnit(ThrottlePeriodEnum.SECOND);
		check.setExpectLatencyUnderMillis(100L);
		check.setExpectResponseType(ResponseTypeEnum.SUCCESS);
		check.setMessage(msg);
		check.setServiceVersion(myD1S1V1);
		
		rule.getActiveChecks().add(check);
		rule=(PersMonitorRuleActive) mySvcReg.saveRule(rule);

		// No calls have happened yet..
		newEntityManager();
		mySvc.runPassiveChecks();
		newEntityManager();
		svcVer = myDao.getServiceVersionByPid(myD1S1V1.getPid());
		assertNull(getMostRecentMonitorRuleFiring());

		// Fail the rule
		newEntityManager();
		
		check = myDao.getAllMonitorRuleActiveChecks().iterator().next();

		Collection<SidechannelOrchestratorResponseBean> responses=new ArrayList<IServiceOrchestrator.SidechannelOrchestratorResponseBean>();
		
		SidechannelOrchestratorResponseBean rsp = createOrchestratorResponse(ResponseTypeEnum.SUCCESS);
		rsp.setApplicableUrl(myD1M1S1U1);
		rsp.getIncomingResponse().setResponseTime(1000L);
		rsp.getIncomingResponse().setSuccessfulUrl(myD1M1S1U1);
		responses.add(rsp);
		
		when(orch.handleSidechannelRequestForEachUrl(eq(myD1S1V1.getPid()), (String)any(), (String)any(), (String)any())).thenReturn(responses);
		mySvc.runActiveChecks();
		
		myStatsSvc.flushStatus();
		newEntityManager();

		svcVer = myDao.getServiceVersionByPid(myD1S1V1.getPid());
		
		// This doesn't change because the service didn't fail, just the rule
		assertEquals(StatusEnum.UNKNOWN, svcVer.getUrls().get(0).getStatus().getStatus());

		assertNotNull(getMostRecentMonitorRuleFiring());
		assertNull(getMostRecentMonitorRuleFiring().getProblems().iterator().next().getCheckFailureMessage(),getMostRecentMonitorRuleFiring().getProblems().iterator().next().getCheckFailureMessage());
		assertNull(getMostRecentMonitorRuleFiring().getEndDate());

		rule = (PersMonitorRuleActive) myDao.getMonitorRule(rule.getPid());
		assertEquals(1, rule.getActiveChecks().iterator().next().getRecentOutcomes().size());
		
		newEntityManager();

		// Make sure the passive check doesn't overwrite things
		mySvc.runPassiveChecks();
		
		newEntityManager();
		svcVer = myDao.getServiceVersionByPid(myD1S1V1.getPid());
		assertNotNull(getMostRecentMonitorRuleFiring());
		assertNull(getMostRecentMonitorRuleFiring().getEndDate());

		// Succeed the URL again
		newEntityManager();
//		httpResponse = new HttpResponseBean();
//		httpResponse.setSuccessfulUrl(myUrl2);
//		httpResponse.setBody("successful response");
//		invocationResponseResultsBean = new InvocationResponseResultsBean();
//		invocationResponseResultsBean.setResponseType(ResponseTypeEnum.SUCCESS);
//		myStatsSvc.recordInvocationMethod(new Date(), 100, myMethod, null, httpResponse, invocationResponseResultsBean, null);

		rsp.getIncomingResponse().setResponseTime(1L);
		mySvc.clearRateLimitersForUnitTests();
		mySvc.runActiveChecks();

		newEntityManager();

		myStatsSvc.flushStatus();
		newEntityManager();

		svcVer = myDao.getServiceVersionByPid(myD1S1V1.getPid());
		assertNotNull(getMostRecentMonitorRuleFiring().getEndDate());
		assertEquals(StatusEnum.ACTIVE, svcVer.getUrls().get(0).getStatus().getStatus());

		
	}


	@Test
	public void testActiveTestWithInvocationFailure() throws Exception{
		createCatalog();

		IServiceOrchestrator orch = mock(IServiceOrchestrator.class);
		mySvc.setServiceOrchestratorForUnitTests(orch);
		mySvc.setMonitorNotifierForUnitTests(mock(IMonitorNotifier.class));
		
		newEntityManager();

		BasePersServiceVersion svcVer = myDao.getServiceVersionByPid(myD1S1V1.getPid());
		assertEquals(StatusEnum.UNKNOWN, svcVer.getUrls().get(0).getStatus().getStatus());
		
		PersLibraryMessage msg = new PersLibraryMessage();
		msg.setAppliesTo(myD1S1V1);
		msg.setContentType("ct");
		msg.setDescription("desc");
		msg.setMessage("body");
		myDao.saveLibraryMessage(msg);
		
		newEntityManager();

		PersMonitorRuleActive rule = new PersMonitorRuleActive();
		rule.setRuleActive(true);
		
		PersMonitorRuleActiveCheck check=new PersMonitorRuleActiveCheck();
		check.setCheckFrequencyNum(1);
		check.setCheckFrequencyUnit(ThrottlePeriodEnum.SECOND);
		check.setExpectLatencyUnderMillis(100L);
		check.setExpectResponseType(ResponseTypeEnum.SUCCESS);
		check.setMessage(msg);
		check.setServiceVersion(myD1S1V1);
		
		rule.getActiveChecks().add(check);
		rule=(PersMonitorRuleActive) mySvcReg.saveRule(rule);

		// No calls have happened yet..
		newEntityManager();
		mySvc.runPassiveChecks();
		newEntityManager();
		svcVer = myDao.getServiceVersionByPid(myD1S1V1.getPid());
		assertNull(getMostRecentMonitorRuleFiring());

		// Fail one of the URLs
		newEntityManager();
		
		check = myDao.getAllMonitorRuleActiveChecks().iterator().next();

		Collection<SidechannelOrchestratorResponseBean> responses=new ArrayList<IServiceOrchestrator.SidechannelOrchestratorResponseBean>();
		SidechannelOrchestratorResponseBean rsp = createOrchestratorResponse(ResponseTypeEnum.FAIL);
		rsp.setApplicableUrl(myD1M1S1U1);
		rsp.getIncomingResponse().setResponseTime(100);
		rsp.getIncomingResponse().setSuccessfulUrl(myD1M1S1U1);
		responses.add(rsp);
		when(orch.handleSidechannelRequestForEachUrl(eq(myD1S1V1.getPid()), (String)any(), (String)any(), (String)any())).thenReturn(responses);
		mySvc.runActiveChecks();
		
//		HttpResponseBean httpResponse = new HttpResponseBean();
//		httpResponse.setSuccessfulUrl(myUrl1);
//		httpResponse.addFailedUrl(myUrl2, "failure explanation", 500, "text/plain", "response body");
//		httpResponse.setBody("successful response");
//		InvocationResponseResultsBean invocationResponseResultsBean = new InvocationResponseResultsBean();
//		invocationResponseResultsBean.setResponseType(ResponseTypeEnum.SUCCESS);
//		myStatsSvc.recordInvocationMethod(new Date(), 100, myMethod, null, httpResponse, invocationResponseResultsBean, null);
//		newEntityManager();

		myStatsSvc.flushStatus();
		newEntityManager();

		svcVer = myDao.getServiceVersionByPid(myD1S1V1.getPid());
		assertEquals(StatusEnum.DOWN, svcVer.getUrls().get(0).getStatus().getStatus());

		assertNotNull(getMostRecentMonitorRuleFiring());
		assertNull(getMostRecentMonitorRuleFiring().getProblems().iterator().next().getCheckFailureMessage(),getMostRecentMonitorRuleFiring().getProblems().iterator().next().getCheckFailureMessage());
		assertNull(getMostRecentMonitorRuleFiring().getEndDate());

		rule = (PersMonitorRuleActive) myDao.getMonitorRule(rule.getPid());
		assertEquals(1, rule.getActiveChecks().iterator().next().getRecentOutcomes().size());
		
		newEntityManager();

		// Make sure the passive check doesn't overwrite things
		mySvc.runPassiveChecks();
		
		newEntityManager();
		svcVer = myDao.getServiceVersionByPid(myD1S1V1.getPid());
		assertNotNull(getMostRecentMonitorRuleFiring());
		assertNull(getMostRecentMonitorRuleFiring().getEndDate());

		// Succeed the URL again
		newEntityManager();
//		httpResponse = new HttpResponseBean();
//		httpResponse.setSuccessfulUrl(myUrl2);
//		httpResponse.setBody("successful response");
//		invocationResponseResultsBean = new InvocationResponseResultsBean();
//		invocationResponseResultsBean.setResponseType(ResponseTypeEnum.SUCCESS);
//		myStatsSvc.recordInvocationMethod(new Date(), 100, myMethod, null, httpResponse, invocationResponseResultsBean, null);
//		newEntityManager();
//		myStatsSvc.flushStatus();

		SrBeanIncomingResponse httpResponse=new SrBeanIncomingResponse();
		httpResponse.setSuccessfulUrl(myD1M1S1U1);
		httpResponse.setResponseTime(1);
		
		rsp = createOrchestratorResponse(ResponseTypeEnum.SUCCESS);
		rsp.getIncomingResponse().setResponseTime(1);
		rsp.getIncomingResponse().setSuccessfulUrl(myD1M1S1U1);

		responses.clear();
		responses.add(rsp);
		
		ourLog.info("Making a success");
		
		mySvc.clearRateLimitersForUnitTests();
		mySvc.runActiveChecks();

		myStatsSvc.flushStatus();
		newEntityManager();
		
		svcVer = myDao.getServiceVersionByPid(myD1S1V1.getPid());
		assertNotNull(getMostRecentMonitorRuleFiring().getEndDate());
		assertEquals(StatusEnum.ACTIVE, svcVer.getUrls().get(0).getStatus().getStatus());

		
	}
private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(MonitorServiceBeanTest.class);
	private SidechannelOrchestratorResponseBean createOrchestratorResponse(ResponseTypeEnum theResponseType) {
		SrBeanIncomingRequest request=new SrBeanIncomingRequest();
		request.setRequestTime(new Date());
		request.setRequestHeaders(new HashMap<String, List<String>>());
		request.setInputReader(new StringReader(""));
		request.setRequestType(RequestType.POST);
		SrBeanProcessedResponse resp=new SrBeanProcessedResponse();
		resp.setResponseType(theResponseType);
		SrBeanIncomingResponse incResp=new SrBeanIncomingResponse();
		incResp.setHeaders(new HashMap<String, List<String>>());
		
		SidechannelOrchestratorResponseBean rsp = new SidechannelOrchestratorResponseBean(request, resp, incResp);
		
		SrBeanIncomingRequest simReq = new SrBeanIncomingRequest();
		simReq.setRequestType(RequestType.POST);
		rsp.setSimulatedIncomingRequest(simReq);
		
		SrBeanProcessedRequest simPrc=new SrBeanProcessedRequest();
		rsp.setSimulatedProcessedRequest(simPrc);
		
		return rsp;
	}

	@Test
	public void testActiveTestWithFailingInvocation() throws Exception{
		createCatalog();

		IServiceOrchestrator orch = mock(IServiceOrchestrator.class);
		mySvc.setServiceOrchestratorForUnitTests(orch);
		mySvc.setMonitorNotifierForUnitTests(mock(IMonitorNotifier.class));
		
		newEntityManager();

		PersLibraryMessage msg = new PersLibraryMessage();
		msg.setAppliesTo(myD1S1V1);
		msg.setContentType("ct");
		msg.setDescription("desc");
		msg.setMessage("body");
		myDao.saveLibraryMessage(msg);
		
		newEntityManager();

		PersMonitorRuleActive rule = new PersMonitorRuleActive();
		rule.setRuleActive(true);
		
		PersMonitorRuleActiveCheck check=new PersMonitorRuleActiveCheck();
		check.setCheckFrequencyNum(1);
		check.setCheckFrequencyUnit(ThrottlePeriodEnum.SECOND);
		check.setExpectLatencyUnderMillis(100L);
		check.setExpectResponseType(ResponseTypeEnum.SUCCESS);
		check.setMessage(msg);
		check.setServiceVersion(myD1S1V1);
		
		rule.getActiveChecks().add(check);
		rule=(PersMonitorRuleActive) mySvcReg.saveRule(rule);

		// No calls have happened yet..
		newEntityManager();
		mySvc.runPassiveChecks();
		newEntityManager();

		//		BasePersServiceVersion svcVer = myDao.getServiceVersionByPid(myD1S1V1.getPid());
		assertNull(getMostRecentMonitorRuleFiring());

		// Fail one of the URLs
		newEntityManager();
		
		check = myDao.getAllMonitorRuleActiveChecks().iterator().next();

		Collection<SidechannelOrchestratorResponseBean> responses=new ArrayList<IServiceOrchestrator.SidechannelOrchestratorResponseBean>();
		SidechannelOrchestratorResponseBean bean = createOrchestratorResponse(ResponseTypeEnum.FAIL);
		bean.setApplicableUrl(myD1M1S1U1);
		bean.getIncomingResponse().addFailedUrl(myD1M1S1U1, "Failed", 200, "text/plain", "", 100, new HashMap<String, List<String>>());
		responses.add(bean);
		when(orch.handleSidechannelRequestForEachUrl(eq(myD1S1V1.getPid()), (String)any(), (String)any(), (String)any())).thenReturn(responses);
		mySvc.runActiveChecks();
		
//		HttpResponseBean httpResponse = new HttpResponseBean();
//		httpResponse.setSuccessfulUrl(myUrl1);
//		httpResponse.addFailedUrl(myUrl2, "failure explanation", 500, "text/plain", "response body");
//		httpResponse.setBody("successful response");
//		InvocationResponseResultsBean invocationResponseResultsBean = new InvocationResponseResultsBean();
//		invocationResponseResultsBean.setResponseType(ResponseTypeEnum.SUCCESS);
//		myStatsSvc.recordInvocationMethod(new Date(), 100, myMethod, null, httpResponse, invocationResponseResultsBean, null);
//		newEntityManager();
//		myStatsSvc.flushStatus();
		newEntityManager();

		assertNotNull(getMostRecentMonitorRuleFiring());
		assertNull(getMostRecentMonitorRuleFiring().getProblems().iterator().next().getCheckFailureMessage(), getMostRecentMonitorRuleFiring().getProblems().iterator().next().getCheckFailureMessage());
		assertNull(getMostRecentMonitorRuleFiring().getEndDate());

		rule = (PersMonitorRuleActive) myDao.getMonitorRule(rule.getPid());
		assertEquals(1, rule.getActiveChecks().iterator().next().getRecentOutcomes().size());
		
		newEntityManager();

		// Make sure the passive check doesn't overwrite things
		mySvc.runPassiveChecks();
		
		newEntityManager();
//		svcVer = myDao.getServiceVersionByPid(myD1S1V1.getPid());
		assertNotNull(getMostRecentMonitorRuleFiring());
		assertNull(getMostRecentMonitorRuleFiring().getEndDate());

		// Succeed the URL again
		newEntityManager();
		
		
		
	}

	
	private PersMonitorRuleFiring getMostRecentMonitorRuleFiring() {
		@SuppressWarnings("rawtypes")
		List list = myEntityManager.createQuery("SELECT f FROM PersMonitorRuleFiring f").getResultList();
		if (list.isEmpty()) {
			return null;
		}
		if (list.size() > 1) {
			throw new IllegalStateException();
		}
		return (PersMonitorRuleFiring) list.get(0);
	}

	private void createCatalog() throws Exception {
		newEntityManager();

		DtoDomain d1 = myOrchSvc.unitTestMethod_addDomain("asv_did", "asv_did");
		GService d1s1 = myOrchSvc.addService(d1.getPid(), new GService("asv_sid", "asv_sid", true));
		
		newEntityManager();
		
		PersHttpClientConfig hcc = myDao.getOrCreateHttpClientConfig("httpclient");

		newEntityManager();

		DtoServiceVersionSoap11 d1s1v1 = new DtoServiceVersionSoap11();
		d1s1v1.setActive(true);
		d1s1v1.setId("ASV_SV1");
		d1s1v1.setName("ASV_SV1_Name");
		d1s1v1.setWsdlLocation(HTTP_FOO);
		d1s1v1.setHttpClientConfigPid(hcc.getPid());

		List<GResource> resources = new ArrayList<GResource>();
		resources.add(new GResource(HTTP_FOO, "text/xml", "contents1"));
		resources.add(new GResource(HTTP_BAR, "text/xml", "contents2"));

		d1s1v1.getUrlList().add(new GServiceVersionUrl("url1", HTTP_URL1));
		d1s1v1.getUrlList().add(new GServiceVersionUrl("url2", HTTP_URL2));

		d1s1v1.getMethodList().add(new GServiceMethod("methodName"));

		mySvcVer1G = myOrchSvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v1, resources);

		newEntityManager();

		DtoServiceVersionSoap11 d1s2v1 = new DtoServiceVersionSoap11();
		d1s2v1.setActive(true);
		d1s2v1.setId("ASV_SV2");
		d1s2v1.setName("ASV_SV2_Name");
		d1s2v1.setWsdlLocation(HTTP_FOO2);
		d1s2v1.setHttpClientConfigPid(hcc.getPid());

		List<GResource> resources2 = new ArrayList<GResource>();
		resources2.add(new GResource(HTTP_FOO2, "text/xml", "contents1"));
		resources2.add(new GResource(HTTP_BAR2, "text/xml", "contents2"));

		d1s2v1.getUrlList().add(new GServiceVersionUrl("url1", HTTP_URL1));
		d1s2v1.getUrlList().add(new GServiceVersionUrl("url2", HTTP_URL2));

		d1s2v1.getMethodList().add(new GServiceMethod("methodName"));

		mySvcVer2G = myOrchSvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), d1s2v1, resources2);
		
		newEntityManager();

		myD1S1V1 = myDao.getServiceVersionByPid(mySvcVer1G.getPid());
		myD1S1V1.loadAllAssociations();
		myD1S1V1M1 = myD1S1V1.getMethods().iterator().next();
		myD1M1S1U1 = myD1S1V1.getUrlWithId("url1");
		myD1M1S1U2 = myD1S1V1.getUrlWithId("url2");

		myD1S1V2 = myDao.getServiceVersionByPid(mySvcVer2G.getPid());
		myD1S1V2.loadAllAssociations();
		myD1S1V2M1 = myD1S1V2.getMethods().iterator().next();
		myD1M1S2U1 = myD1S1V2.getUrlWithId("url1");
		myD1M1S2U2 = myD1S1V2.getUrlWithId("url2");

		newEntityManager();

	}
	
	
	
	
	@Test
	public void testActiveTestWithMultipleChecks() throws Exception{
		createCatalog();

		IServiceOrchestrator orch = mock(IServiceOrchestrator.class);
		mySvc.setServiceOrchestratorForUnitTests(orch);
		mySvc.setMonitorNotifierForUnitTests(mock(IMonitorNotifier.class));
		
		newEntityManager();

		BasePersServiceVersion svcVer = myDao.getServiceVersionByPid(myD1S1V1.getPid());
		assertEquals(StatusEnum.UNKNOWN, svcVer.getUrls().get(0).getStatus().getStatus());
		
		PersLibraryMessage msg = new PersLibraryMessage();
		msg.setAppliesTo(myD1S1V1);
		msg.setContentType("ct");
		msg.setDescription("desc");
		msg.setMessage("body");
		myDao.saveLibraryMessage(msg);
		
		newEntityManager();

		PersMonitorRuleActive rule = new PersMonitorRuleActive();
		rule.setRuleActive(true);
		
		PersMonitorRuleActiveCheck check=new PersMonitorRuleActiveCheck();
		check.setCheckFrequencyNum(1);
		check.setCheckFrequencyUnit(ThrottlePeriodEnum.SECOND);
		check.setExpectLatencyUnderMillis(100L);
		check.setExpectResponseType(ResponseTypeEnum.SUCCESS);
		check.setMessage(msg);
		check.setServiceVersion(myD1S1V1);
		rule.getActiveChecks().add(check);
		
		PersMonitorRuleActiveCheck check2 = new PersMonitorRuleActiveCheck();
		check2.setCheckFrequencyNum(1);
		check2.setCheckFrequencyUnit(ThrottlePeriodEnum.SECOND);
		check2.setExpectLatencyUnderMillis(100L);
		check2.setExpectResponseType(ResponseTypeEnum.SUCCESS);
		check2.setMessage(msg);
		check2.setServiceVersion(myD1S1V2);
		rule.getActiveChecks().add(check2);
		
		rule=(PersMonitorRuleActive) mySvcReg.saveRule(rule);

		// No calls have happened yet..
		newEntityManager();
		mySvc.runActiveChecks();
		newEntityManager();
		svcVer = myDao.getServiceVersionByPid(myD1S1V1.getPid());
		assertNull(getMostRecentMonitorRuleFiring());

		// Fail one of the URLs
		newEntityManager();
		
		check2 = myDao.getAllMonitorRuleActiveChecks().iterator().next();

		Collection<SidechannelOrchestratorResponseBean> responses=new ArrayList<IServiceOrchestrator.SidechannelOrchestratorResponseBean>();
		SidechannelOrchestratorResponseBean rsp = createOrchestratorResponse(ResponseTypeEnum.SUCCESS);
		rsp.getIncomingResponse().setResponseTime(1000); // too long
		rsp.getIncomingResponse().setSuccessfulUrl(myD1M1S1U1);
		responses.add(rsp);
		
		when(orch.handleSidechannelRequestForEachUrl(eq(myD1S1V1.getPid()), (String)any(), (String)any(), (String)any())).thenReturn(responses);
		
		mySvc.clearRateLimitersForUnitTests();
		mySvc.runActiveChecks();
		
		myStatsSvc.flushStatus();
		newEntityManager();

		svcVer = myDao.getServiceVersionByPid(myD1S1V1.getPid());

		assertNotNull(getMostRecentMonitorRuleFiring());

		
	}

}
