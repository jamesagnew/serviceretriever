package net.svcret.ejb.ejb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.svcret.admin.shared.enm.ResponseTypeEnum;
import net.svcret.admin.shared.model.GDomain;
import net.svcret.admin.shared.model.GResource;
import net.svcret.admin.shared.model.GService;
import net.svcret.admin.shared.model.GServiceMethod;
import net.svcret.admin.shared.model.GServiceVersionUrl;
import net.svcret.admin.shared.model.GSoap11ServiceVersion;
import net.svcret.ejb.api.HttpResponseBean;
import net.svcret.ejb.api.IBroadcastSender;
import net.svcret.ejb.api.IServiceInvokerSoap11;
import net.svcret.ejb.api.InvocationResponseResultsBean;
import net.svcret.ejb.ex.ProcessingException;
import net.svcret.ejb.model.entity.BasePersServiceVersion;
import net.svcret.ejb.model.entity.PersHttpClientConfig;
import net.svcret.ejb.model.entity.PersMonitorRule;
import net.svcret.ejb.model.entity.PersServiceVersionMethod;
import net.svcret.ejb.model.entity.PersServiceVersionUrl;

import org.apache.commons.lang3.time.DateUtils;
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
	private GSoap11ServiceVersion mySvcVerG;
	private BasePersServiceVersion mySvcVer;
	private PersServiceVersionMethod myMethod;
	private PersServiceVersionUrl myUrl1;
	private PersServiceVersionUrl myUrl2;
	private Date myNow;
	private Date my30SecsAgo;
	private Date my1Min30SecsAgo;
	private Date my2Min30SecsAgo;

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

		DefaultAnswer.setDesignTime();
		
		myNow = new Date();
		my30SecsAgo = new Date(myNow.getTime() - (30 * DateUtils.MILLIS_PER_SECOND));
		my1Min30SecsAgo = new Date(myNow.getTime() - (DateUtils.MILLIS_PER_MINUTE) - (30 * DateUtils.MILLIS_PER_SECOND));
		my2Min30SecsAgo = new Date(myNow.getTime() - (2*DateUtils.MILLIS_PER_MINUTE) - (30 * DateUtils.MILLIS_PER_SECOND));
		
		
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

		PersMonitorRule rule = new PersMonitorRule();
		rule.setRuleActive(true);
		rule.setAppliesToItems(mySvcVer);
		rule.setFireIfSingleBackingUrlIsUnavailable(true);
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
		assertEquals(myUrl2, svcVer.getMostRecentMonitorRuleFiring().getProblems().iterator().next().getFailedUrl());
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

		PersMonitorRule rule = new PersMonitorRule();
		rule.setRuleActive(true);
		rule.setAppliesToItems(mySvcVer);
		rule.setFireIfSingleBackingUrlIsUnavailable(true);
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

	private void createCatalog() throws ProcessingException {
		newEntityManager();

		GDomain d1 = myOrchSvc.addDomain("asv_did", "asv_did");
		GService d1s1 = myOrchSvc.addService(d1.getPid(), "asv_sid", "asv_sid", true);
		PersHttpClientConfig hcc = myDao.getOrCreateHttpClientConfig("httpclient");

		newEntityManager();

		GSoap11ServiceVersion d1s1v1 = new GSoap11ServiceVersion();
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
