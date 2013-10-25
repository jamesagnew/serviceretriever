package net.svcret.ejb.ejb.monitor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
import net.svcret.ejb.api.HttpResponseBean;
import net.svcret.ejb.api.IServiceOrchestrator;
import net.svcret.ejb.api.IServiceOrchestrator.SidechannelOrchestratorResponseBean;
import net.svcret.ejb.api.InvocationResponseResultsBean;
import net.svcret.ejb.ejb.AdminServiceBean;
import net.svcret.ejb.ejb.BaseJpaTest;
import net.svcret.ejb.ejb.ConfigServiceBean;
import net.svcret.ejb.ejb.DaoBean;
import net.svcret.ejb.ejb.DefaultAnswer;
import net.svcret.ejb.ejb.RuntimeStatusBean;
import net.svcret.ejb.ejb.RuntimeStatusQueryBean;
import net.svcret.ejb.ejb.SecurityServiceBean;
import net.svcret.ejb.ejb.ServiceRegistryBean;
import net.svcret.ejb.ejb.nodecomm.IBroadcastSender;
import net.svcret.ejb.invoker.soap.IServiceInvokerSoap11;
import net.svcret.ejb.model.entity.BasePersServiceVersion;
import net.svcret.ejb.model.entity.PersHttpClientConfig;
import net.svcret.ejb.model.entity.PersLibraryMessage;
import net.svcret.ejb.model.entity.PersMonitorRuleActive;
import net.svcret.ejb.model.entity.PersMonitorRuleActiveCheck;
import net.svcret.ejb.model.entity.PersMonitorRuleFiring;
import net.svcret.ejb.model.entity.PersMonitorRulePassive;
import net.svcret.ejb.model.entity.PersServiceVersionMethod;
import net.svcret.ejb.model.entity.PersServiceVersionUrl;

import org.junit.Before;
import org.junit.Test;

public class MonitorServiceBeanTest extends BaseJpaTest {

	private DaoBean myDao;
	private IBroadcastSender myBroadcastSender;
	private ConfigServiceBean myConfigSvc;
	private AdminServiceBean myOrchSvc;
	private RuntimeStatusBean myStatsSvc;
	private IServiceInvokerSoap11 mySoapInvoker;
	private SecurityServiceBean mySecSvc;
	private ServiceRegistryBean mySvcReg;
	private MonitorServiceBean mySvc;
	private DtoServiceVersionSoap11 mySvcVerG;
	private BasePersServiceVersion mySvcVer;
	private PersServiceVersionMethod myMethod;
	private PersServiceVersionUrl myUrl1;
	private PersServiceVersionUrl myUrl2;
	private RuntimeStatusQueryBean myQuerySvc;
//	private Date myNow;
//	private Date my30SecsAgo;
//	private Date my1Min30SecsAgo;
//	private Date my2Min30SecsAgo;

	@Before
	public void before2() {
		myDao = new DaoBean();
		myBroadcastSender = mock(IBroadcastSender.class);

		myConfigSvc = new ConfigServiceBean();
		myConfigSvc.setDao(myDao);
		myConfigSvc.setBroadcastSender(myBroadcastSender);

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

		newEntityManager();

		PersMonitorRulePassive rule = new PersMonitorRulePassive();
		rule.setRuleActive(true);
		rule.setAppliesToItems(mySvcVer);
		rule.setPassiveFireIfSingleBackingUrlIsUnavailable(true);
		myDao.saveOrCreateMonitorRule(rule);

		// No calls have happened yet..
		newEntityManager();
		mySvc.runPassiveChecks();
		newEntityManager();
		assertNull(getMostRecentMonitorRuleFiring());

		// Fail one of the URLs
		newEntityManager();
		HttpResponseBean httpResponse = new HttpResponseBean();
		httpResponse.setSuccessfulUrl(myUrl1);
		httpResponse.addFailedUrl(myUrl2, "failure explanation", 500, "text/plain", "response body",0);
		httpResponse.setBody("successful response");
		InvocationResponseResultsBean invocationResponseResultsBean = new InvocationResponseResultsBean();
		invocationResponseResultsBean.setResponseType(ResponseTypeEnum.SUCCESS);
		myStatsSvc.recordInvocationMethod(new Date(), 100, myMethod, null, httpResponse, invocationResponseResultsBean, null);
		newEntityManager();
		myStatsSvc.flushStatus();
		newEntityManager();

		mySvc.runPassiveChecks();
		newEntityManager();
		assertNotNull(getMostRecentMonitorRuleFiring());
		assertNull(getMostRecentMonitorRuleFiring().getEndDate());
		assertEquals(1, getMostRecentMonitorRuleFiring().getProblems().size());
		assertEquals(myUrl2, getMostRecentMonitorRuleFiring().getProblems().iterator().next().getUrl());
		assertEquals("failure explanation", getMostRecentMonitorRuleFiring().getProblems().iterator().next().getFailedUrlMessage());

		// Succeed the URL again
		newEntityManager();
		httpResponse = new HttpResponseBean();
		httpResponse.setSuccessfulUrl(myUrl2);
		httpResponse.setBody("successful response");
		invocationResponseResultsBean = new InvocationResponseResultsBean();
		invocationResponseResultsBean.setResponseType(ResponseTypeEnum.SUCCESS);
		myStatsSvc.recordInvocationMethod(new Date(), 100, myMethod, null, httpResponse, invocationResponseResultsBean, null);
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

		newEntityManager();

		PersMonitorRulePassive rule = new PersMonitorRulePassive();
		rule.setRuleActive(true);
		rule.setAppliesToItems(mySvcVer);
		rule.setPassiveFireIfSingleBackingUrlIsUnavailable(true);
		myDao.saveOrCreateMonitorRule(rule);

		// No calls have happened yet..
		newEntityManager();
		mySvc.runPassiveChecks();
		newEntityManager();
		assertNull(getMostRecentMonitorRuleFiring());

		// Fail one of the URLs
		newEntityManager();
		HttpResponseBean httpResponse = new HttpResponseBean();
		httpResponse.setSuccessfulUrl(myUrl1);
		httpResponse.addFailedUrl(myUrl2, "failure explanation", 500, "text/plain", "response body",0);
		httpResponse.setBody("successful response");
		InvocationResponseResultsBean invocationResponseResultsBean = new InvocationResponseResultsBean();
		invocationResponseResultsBean.setResponseType(ResponseTypeEnum.SUCCESS);
		myStatsSvc.recordInvocationMethod(new Date(), 100, myMethod, null, httpResponse, invocationResponseResultsBean, null);
		newEntityManager();
		myStatsSvc.flushStatus();
		newEntityManager();

		mySvc.runPassiveChecks();
		newEntityManager();
		assertNotNull(getMostRecentMonitorRuleFiring());
		assertNull(getMostRecentMonitorRuleFiring().getEndDate());

		// Succeed the URL again
		newEntityManager();
		httpResponse = new HttpResponseBean();
		httpResponse.setSuccessfulUrl(myUrl2);
		httpResponse.setBody("successful response");
		invocationResponseResultsBean = new InvocationResponseResultsBean();
		invocationResponseResultsBean.setResponseType(ResponseTypeEnum.SUCCESS);
		myStatsSvc.recordInvocationMethod(new Date(), 100, myMethod, null, httpResponse, invocationResponseResultsBean, null);
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
		mySvc.setThisForUnitTests(mySvc);
		
		newEntityManager();

		BasePersServiceVersion svcVer = myDao.getServiceVersionByPid(mySvcVer.getPid());
		assertEquals(StatusEnum.UNKNOWN, svcVer.getUrls().get(0).getStatus().getStatus());
		
		PersLibraryMessage msg = new PersLibraryMessage();
		msg.setAppliesTo(mySvcVer);
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
		check.setServiceVersion(mySvcVer);
		
		rule.getActiveChecks().add(check);
		rule=(PersMonitorRuleActive) mySvc.saveRule(rule);

		// No calls have happened yet..
		newEntityManager();
		mySvc.runPassiveChecks();
		newEntityManager();
		svcVer = myDao.getServiceVersionByPid(mySvcVer.getPid());
		assertNull(getMostRecentMonitorRuleFiring());

		// Fail one of the URLs
		newEntityManager();
		
		check = myDao.getAllMonitorRuleActiveChecks().iterator().next();

		Collection<SidechannelOrchestratorResponseBean> responses=new ArrayList<IServiceOrchestrator.SidechannelOrchestratorResponseBean>();
		HttpResponseBean httpResponse=new HttpResponseBean();
		httpResponse.setSuccessfulUrl(myUrl1);
		httpResponse.setResponseTime(1000);
		responses.add(new SidechannelOrchestratorResponseBean("", "", new HashMap<String, List<String>>(), httpResponse, ResponseTypeEnum.SUCCESS, new Date()));
		when(orch.handleSidechannelRequestForEachUrl(eq(mySvcVer.getPid()), (String)any(), (String)any(), (String)any())).thenReturn(responses);
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

		svcVer = myDao.getServiceVersionByPid(mySvcVer.getPid());
		
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
		svcVer = myDao.getServiceVersionByPid(mySvcVer.getPid());
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

		httpResponse.setResponseTime(1);
		mySvc.runActiveChecks();

		newEntityManager();

		myStatsSvc.flushStatus();
		newEntityManager();

		svcVer = myDao.getServiceVersionByPid(mySvcVer.getPid());
		assertNotNull(getMostRecentMonitorRuleFiring().getEndDate());
		assertEquals(StatusEnum.ACTIVE, svcVer.getUrls().get(0).getStatus().getStatus());

		
	}


	@Test
	public void testActiveTestWithInvocationFailure() throws Exception{
		createCatalog();

		IServiceOrchestrator orch = mock(IServiceOrchestrator.class);
		mySvc.setServiceOrchestratorForUnitTests(orch);
		mySvc.setMonitorNotifierForUnitTests(mock(IMonitorNotifier.class));
		mySvc.setThisForUnitTests(mySvc);
		
		newEntityManager();

		BasePersServiceVersion svcVer = myDao.getServiceVersionByPid(mySvcVer.getPid());
		assertEquals(StatusEnum.UNKNOWN, svcVer.getUrls().get(0).getStatus().getStatus());
		
		PersLibraryMessage msg = new PersLibraryMessage();
		msg.setAppliesTo(mySvcVer);
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
		check.setServiceVersion(mySvcVer);
		
		rule.getActiveChecks().add(check);
		rule=(PersMonitorRuleActive) mySvc.saveRule(rule);

		// No calls have happened yet..
		newEntityManager();
		mySvc.runPassiveChecks();
		newEntityManager();
		svcVer = myDao.getServiceVersionByPid(mySvcVer.getPid());
		assertNull(getMostRecentMonitorRuleFiring());

		// Fail one of the URLs
		newEntityManager();
		
		check = myDao.getAllMonitorRuleActiveChecks().iterator().next();

		Collection<SidechannelOrchestratorResponseBean> responses=new ArrayList<IServiceOrchestrator.SidechannelOrchestratorResponseBean>();
		SidechannelOrchestratorResponseBean rsp = new SidechannelOrchestratorResponseBean("", "", new HashMap<String, List<String>>(), null, ResponseTypeEnum.FAIL, new Date());
		rsp.setApplicableUrl(myUrl1);
		responses.add(rsp);
		when(orch.handleSidechannelRequestForEachUrl(eq(mySvcVer.getPid()), (String)any(), (String)any(), (String)any())).thenReturn(responses);
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

		svcVer = myDao.getServiceVersionByPid(mySvcVer.getPid());
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
		svcVer = myDao.getServiceVersionByPid(mySvcVer.getPid());
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

		HttpResponseBean httpResponse=new HttpResponseBean();
		httpResponse.setSuccessfulUrl(myUrl1);
		httpResponse.setResponseTime(1);
		responses.clear();
		responses.add(new SidechannelOrchestratorResponseBean("", "", new HashMap<String, List<String>>(), httpResponse, ResponseTypeEnum.SUCCESS, new Date()));
		mySvc.runActiveChecks();

		myStatsSvc.flushStatus();
		newEntityManager();
		
		svcVer = myDao.getServiceVersionByPid(mySvcVer.getPid());
		assertNotNull(getMostRecentMonitorRuleFiring().getEndDate());
		assertEquals(StatusEnum.ACTIVE, svcVer.getUrls().get(0).getStatus().getStatus());

		
	}

	@Test
	public void testActiveTestWithFailingInvocation() throws Exception{
		createCatalog();

		IServiceOrchestrator orch = mock(IServiceOrchestrator.class);
		mySvc.setServiceOrchestratorForUnitTests(orch);
		mySvc.setMonitorNotifierForUnitTests(mock(IMonitorNotifier.class));
		mySvc.setThisForUnitTests(mySvc);
		
		newEntityManager();

		PersLibraryMessage msg = new PersLibraryMessage();
		msg.setAppliesTo(mySvcVer);
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
		check.setServiceVersion(mySvcVer);
		
		rule.getActiveChecks().add(check);
		rule=(PersMonitorRuleActive) mySvc.saveRule(rule);

		// No calls have happened yet..
		newEntityManager();
		mySvc.runPassiveChecks();
		newEntityManager();
		BasePersServiceVersion svcVer = myDao.getServiceVersionByPid(mySvcVer.getPid());
		assertNull(getMostRecentMonitorRuleFiring());

		// Fail one of the URLs
		newEntityManager();
		
		check = myDao.getAllMonitorRuleActiveChecks().iterator().next();

		Collection<SidechannelOrchestratorResponseBean> responses=new ArrayList<IServiceOrchestrator.SidechannelOrchestratorResponseBean>();
		SidechannelOrchestratorResponseBean bean = new SidechannelOrchestratorResponseBean("", "", new HashMap<String, List<String>>(), null, ResponseTypeEnum.FAIL, new Date());
		bean.setApplicableUrl(myUrl1);
		responses.add(bean);
		when(orch.handleSidechannelRequestForEachUrl(eq(mySvcVer.getPid()), (String)any(), (String)any(), (String)any())).thenReturn(responses);
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

		svcVer = myDao.getServiceVersionByPid(mySvcVer.getPid());
		assertNotNull(getMostRecentMonitorRuleFiring());
		assertNull(getMostRecentMonitorRuleFiring().getProblems().iterator().next().getCheckFailureMessage(), getMostRecentMonitorRuleFiring().getProblems().iterator().next().getCheckFailureMessage());
		assertNull(getMostRecentMonitorRuleFiring().getEndDate());

		rule = (PersMonitorRuleActive) myDao.getMonitorRule(rule.getPid());
		assertEquals(1, rule.getActiveChecks().iterator().next().getRecentOutcomes().size());
		
		newEntityManager();

		// Make sure the passive check doesn't overwrite things
		mySvc.runPassiveChecks();
		
		newEntityManager();
		svcVer = myDao.getServiceVersionByPid(mySvcVer.getPid());
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
		GService d1s1 = myOrchSvc.addService(d1.getPid(), "asv_sid", "asv_sid", true);
		PersHttpClientConfig hcc = myDao.getOrCreateHttpClientConfig("httpclient");

		newEntityManager();

		DtoServiceVersionSoap11 d1s1v1 = new DtoServiceVersionSoap11();
		d1s1v1.setActive(true);
		d1s1v1.setId("ASV_SV1");
		d1s1v1.setName("ASV_SV1_Name");
		d1s1v1.setWsdlLocation("http://foo");
		d1s1v1.setHttpClientConfigPid(hcc.getPid());

		List<GResource> resources = new ArrayList<GResource>();
		resources.add(new GResource("http://foo", "text/xml", "contents1"));
		resources.add(new GResource("http://bar", "text/xml", "contents2"));

		d1s1v1.getUrlList().add(new GServiceVersionUrl("url1", "http://url1"));
		d1s1v1.getUrlList().add(new GServiceVersionUrl("url2", "http://url2"));

		d1s1v1.getMethodList().add(new GServiceMethod("methodName"));

		mySvcVerG = myOrchSvc.saveServiceVersion(d1.getPid(), d1s1.getPid(), d1s1v1, resources);

		newEntityManager();

		mySvcVer = myDao.getServiceVersionByPid(mySvcVerG.getPid());
		mySvcVer.loadAllAssociations();
		myMethod = mySvcVer.getMethods().iterator().next();
		myUrl1 = mySvcVer.getUrlWithId("url1");
		myUrl2 = mySvcVer.getUrlWithId("url2");

		newEntityManager();

	}

}
