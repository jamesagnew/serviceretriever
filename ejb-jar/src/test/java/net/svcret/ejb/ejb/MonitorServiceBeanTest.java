package net.svcret.ejb.ejb;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import net.svcret.admin.shared.enm.ResponseTypeEnum;
import net.svcret.admin.shared.enm.ThrottlePeriodEnum;
import net.svcret.admin.shared.model.GDomain;
import net.svcret.admin.shared.model.GResource;
import net.svcret.admin.shared.model.GService;
import net.svcret.admin.shared.model.GServiceMethod;
import net.svcret.admin.shared.model.GServiceVersionUrl;
import net.svcret.admin.shared.model.DtoServiceVersionSoap11;
import net.svcret.ejb.api.HttpResponseBean;
import net.svcret.ejb.api.IBroadcastSender;
import net.svcret.ejb.api.IMonitorNotifier;
import net.svcret.ejb.api.IServiceInvokerSoap11;
import net.svcret.ejb.api.IServiceOrchestrator;
import net.svcret.ejb.api.IServiceOrchestrator.SidechannelOrchestratorResponseBean;
import net.svcret.ejb.api.InvocationResponseResultsBean;
import net.svcret.ejb.ex.InternalErrorException;
import net.svcret.ejb.ex.ProcessingException;
import net.svcret.ejb.ex.UnknownRequestException;
import net.svcret.ejb.model.entity.BasePersServiceVersion;
import net.svcret.ejb.model.entity.PersHttpClientConfig;
import net.svcret.ejb.model.entity.PersLibraryMessage;
import net.svcret.ejb.model.entity.PersMonitorRuleActive;
import net.svcret.ejb.model.entity.PersMonitorRuleActiveCheck;
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
//	private Date myNow;
//	private Date my30SecsAgo;
//	private Date my1Min30SecsAgo;
//	private Date my2Min30SecsAgo;

	@Before
	public void before2() throws SQLException, ProcessingException {
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
		mySvc.setRuntimeStatus(myStatsSvc);
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
	public void testUrlUnavailable() throws ProcessingException {
		createCatalog();

		newEntityManager();

		PersMonitorRulePassive rule = new PersMonitorRulePassive();
		rule.setRuleActive(true);
		rule.setAppliesToItems(mySvcVer);
		rule.setPassiveFireIfSingleBackingUrlIsUnavailable(true);
		myDao.saveOrCreateMonitorRule(rule);

		// No calls have happened yet..
		newEntityManager();
		mySvc.check();
		newEntityManager();
		BasePersServiceVersion svcVer = myDao.getServiceVersionByPid(mySvcVer.getPid());
		assertNull(svcVer.getMostRecentMonitorRuleFiring());

		// Fail one of the URLs
		newEntityManager();
		HttpResponseBean httpResponse = new HttpResponseBean();
		httpResponse.setSuccessfulUrl(myUrl1);
		httpResponse.addFailedUrl(myUrl2, "failure explanation", 500, "text/plain", "response body");
		httpResponse.setBody("successful response");
		InvocationResponseResultsBean invocationResponseResultsBean = new InvocationResponseResultsBean();
		invocationResponseResultsBean.setResponseType(ResponseTypeEnum.SUCCESS);
		myStatsSvc.recordInvocationMethod(new Date(), 100, myMethod, null, httpResponse, invocationResponseResultsBean, null);
		newEntityManager();
		myStatsSvc.flushStatus();
		newEntityManager();

		mySvc.check();
		newEntityManager();
		svcVer = myDao.getServiceVersionByPid(mySvcVer.getPid());
		assertNotNull(svcVer.getMostRecentMonitorRuleFiring());
		assertNull(svcVer.getMostRecentMonitorRuleFiring().getEndDate());
		assertEquals(1, svcVer.getMostRecentMonitorRuleFiring().getProblems().size());
		assertEquals(myUrl2, svcVer.getMostRecentMonitorRuleFiring().getProblems().iterator().next().getUrl());
		assertEquals("failure explanation", svcVer.getMostRecentMonitorRuleFiring().getProblems().iterator().next().getFailedUrlMessage());

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

		mySvc.check();
		newEntityManager();
		svcVer = myDao.getServiceVersionByPid(mySvcVer.getPid());
		assertNotNull(svcVer.getMostRecentMonitorRuleFiring().getEndDate());
		
		
	}

	@Test
	public void testLatencyExceedsThreshold() throws ProcessingException {
		createCatalog();

		newEntityManager();

		PersMonitorRulePassive rule = new PersMonitorRulePassive();
		rule.setRuleActive(true);
		rule.setAppliesToItems(mySvcVer);
		rule.setPassiveFireIfSingleBackingUrlIsUnavailable(true);
		myDao.saveOrCreateMonitorRule(rule);

		// No calls have happened yet..
		newEntityManager();
		mySvc.check();
		newEntityManager();
		BasePersServiceVersion svcVer = myDao.getServiceVersionByPid(mySvcVer.getPid());
		assertNull(svcVer.getMostRecentMonitorRuleFiring());

		// Fail one of the URLs
		newEntityManager();
		HttpResponseBean httpResponse = new HttpResponseBean();
		httpResponse.setSuccessfulUrl(myUrl1);
		httpResponse.addFailedUrl(myUrl2, "failure explanation", 500, "text/plain", "response body");
		httpResponse.setBody("successful response");
		InvocationResponseResultsBean invocationResponseResultsBean = new InvocationResponseResultsBean();
		invocationResponseResultsBean.setResponseType(ResponseTypeEnum.SUCCESS);
		myStatsSvc.recordInvocationMethod(new Date(), 100, myMethod, null, httpResponse, invocationResponseResultsBean, null);
		newEntityManager();
		myStatsSvc.flushStatus();
		newEntityManager();

		mySvc.check();
		newEntityManager();
		svcVer = myDao.getServiceVersionByPid(mySvcVer.getPid());
		assertNotNull(svcVer.getMostRecentMonitorRuleFiring());
		assertNull(svcVer.getMostRecentMonitorRuleFiring().getEndDate());

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

		mySvc.check();
		newEntityManager();
		svcVer = myDao.getServiceVersionByPid(mySvcVer.getPid());
		assertNotNull(svcVer.getMostRecentMonitorRuleFiring().getEndDate());
		
		
	}

	@Test
	public void testActiveTest() throws ProcessingException, InternalErrorException, UnknownRequestException {
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
		mySvc.saveRule(rule);

		// No calls have happened yet..
		newEntityManager();
		mySvc.check();
		newEntityManager();
		BasePersServiceVersion svcVer = myDao.getServiceVersionByPid(mySvcVer.getPid());
		assertNull(svcVer.getMostRecentMonitorRuleFiring());

		// Fail one of the URLs
		newEntityManager();
		
		check = myDao.getAllMonitorRuleActiveChecks().iterator().next();

		Collection<SidechannelOrchestratorResponseBean> responses=new ArrayList<IServiceOrchestrator.SidechannelOrchestratorResponseBean>();
		HttpResponseBean httpResponse=new HttpResponseBean();
		httpResponse.setSuccessfulUrl(myUrl1);
		httpResponse.setResponseTime(1000);
		responses.add(new SidechannelOrchestratorResponseBean("", "", new HashMap<String, List<String>>(), httpResponse, ResponseTypeEnum.SUCCESS));
		when(orch.handleSidechannelRequestForEachUrl(eq(mySvcVer.getPid()), (String)any(), (String)any(), (String)any())).thenReturn(responses);
		mySvc.runActiveCheck(check);
		
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
		assertNotNull(svcVer.getMostRecentMonitorRuleFiring());
		assertNull(svcVer.getMostRecentMonitorRuleFiring().getProblems().iterator().next().getCheckFailureMessage(),svcVer.getMostRecentMonitorRuleFiring().getProblems().iterator().next().getCheckFailureMessage());
		assertNull(svcVer.getMostRecentMonitorRuleFiring().getEndDate());

		newEntityManager();

		// Make sure the passive check doesn't overwrite things
		mySvc.check();
		
		newEntityManager();
		svcVer = myDao.getServiceVersionByPid(mySvcVer.getPid());
		assertNotNull(svcVer.getMostRecentMonitorRuleFiring());
		assertNull(svcVer.getMostRecentMonitorRuleFiring().getEndDate());

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

		httpResponse.setResponseTime(1);
		mySvc.runActiveCheck(check);

		newEntityManager();
		
		svcVer = myDao.getServiceVersionByPid(mySvcVer.getPid());
		assertNotNull(svcVer.getMostRecentMonitorRuleFiring().getEndDate());
		
		
	}

	
	private void createCatalog() throws ProcessingException {
		newEntityManager();

		GDomain d1 = myOrchSvc.addDomain("asv_did", "asv_did");
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
