package net.svcret.core.ejb;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import net.svcret.admin.shared.enm.InvocationStatsIntervalEnum;
import net.svcret.admin.shared.enm.ResponseTypeEnum;
import net.svcret.admin.shared.model.StatusEnum;
import net.svcret.admin.shared.model.UrlSelectionPolicy;
import net.svcret.core.api.IConfigService;
import net.svcret.core.api.IDao;
import net.svcret.core.api.IRuntimeStatus;
import net.svcret.core.api.SrBeanIncomingResponse;
import net.svcret.core.api.SrBeanProcessedRequest;
import net.svcret.core.api.SrBeanProcessedResponse;
import net.svcret.core.api.UrlPoolBean;
import net.svcret.core.api.SrBeanIncomingResponse.Failure;
import net.svcret.core.model.entity.BasePersServiceVersion;
import net.svcret.core.model.entity.BasePersStats;
import net.svcret.core.model.entity.PersConfig;
import net.svcret.core.model.entity.PersDomain;
import net.svcret.core.model.entity.PersHttpClientConfig;
import net.svcret.core.model.entity.PersInvocationMethodSvcverStats;
import net.svcret.core.model.entity.PersInvocationMethodSvcverStatsPk;
import net.svcret.core.model.entity.PersInvocationMethodUserStats;
import net.svcret.core.model.entity.PersMethod;
import net.svcret.core.model.entity.PersMethodStatus;
import net.svcret.core.model.entity.PersService;
import net.svcret.core.model.entity.PersServiceVersionResource;
import net.svcret.core.model.entity.PersServiceVersionStatus;
import net.svcret.core.model.entity.PersServiceVersionUrl;
import net.svcret.core.model.entity.PersServiceVersionUrlStatus;
import net.svcret.core.model.entity.PersStaticResourceStats;
import net.svcret.core.model.entity.PersUser;
import net.svcret.core.model.entity.PersUserMethodStatus;
import net.svcret.core.model.entity.PersUserStatus;
import net.svcret.core.model.entity.soap.PersServiceVersionSoap11;
import net.svcret.core.status.RuntimeStatusBean;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.internal.matchers.CapturingMatcher;
import org.mockito.internal.stubbing.defaultanswers.ReturnsDeepStubs;

import com.google.common.collect.Maps;

public class RuntimeStatusBeanTest {

	private static long ourNextPid = 1;

	private SimpleDateFormat myFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private RuntimeStatusBean svc;
	private IDao dao;
	private IConfigService myConfigSvc;

	@Before
	public void before() {
		
		myConfigSvc = mock(IConfigService.class, new ReturnsDeepStubs());
		when(myConfigSvc.getNodeId()).thenReturn("unittest.node");
		
		dao = mock(IDao.class, new ReturnsDeepStubs());

		svc = new RuntimeStatusBean();
		svc.setDao(dao);
		svc.setConfigSvc(myConfigSvc);
		
	}

	@Test
	public void buildUrlPoolPreferLocal() throws Exception{

		PersServiceVersionUrl urlLocal1 = mock(PersServiceVersionUrl.class);
		PersServiceVersionUrl urlLocal2 = mock(PersServiceVersionUrl.class);
		PersServiceVersionUrl urlRemote1 = mock(PersServiceVersionUrl.class);
		PersServiceVersionUrl urlRemote2 = mock(PersServiceVersionUrl.class);

		PersServiceVersionUrlStatus statusL1 = mock(PersServiceVersionUrlStatus.class);
		PersServiceVersionUrlStatus statusL2 = mock(PersServiceVersionUrlStatus.class);
		PersServiceVersionUrlStatus statusR1 = mock(PersServiceVersionUrlStatus.class);
		PersServiceVersionUrlStatus statusR2 = mock(PersServiceVersionUrlStatus.class);

		when(urlLocal1.isLocal()).thenReturn(true);
		when(urlLocal1.getUrl()).thenReturn("L1");
		when(urlLocal1.getStatus()).thenReturn(statusL1);
		when(statusL1.getPid()).thenReturn(9990L);
		when(statusL1.attemptToResetCircuitBreaker()).thenReturn(false);

		when(urlLocal2.getStatus()).thenReturn(statusL2);
		when(urlLocal2.isLocal()).thenReturn(true);
		when(urlLocal2.getUrl()).thenReturn("L2");
		when(statusL2.getPid()).thenReturn(9991L);
		when(statusL2.attemptToResetCircuitBreaker()).thenReturn(false);

		when(urlRemote1.getStatus()).thenReturn(statusR1);
		when(urlRemote1.isLocal()).thenReturn(false);
		when(urlRemote1.getUrl()).thenReturn("R1");
		when(statusR1.getPid()).thenReturn(9992L);
		when(statusR1.attemptToResetCircuitBreaker()).thenReturn(false);

		when(urlRemote2.getStatus()).thenReturn(statusR2);
		when(urlRemote2.isLocal()).thenReturn(false);
		when(urlRemote2.getUrl()).thenReturn("R2");
		when(statusR2.getPid()).thenReturn(9993L);
		when(statusR2.attemptToResetCircuitBreaker()).thenReturn(false);

		PersServiceVersionSoap11 ver = mock(PersServiceVersionSoap11.class);

		PersHttpClientConfig cfg = mock(PersHttpClientConfig.class);
		when(ver.getHttpClientConfig()).thenReturn(cfg);
		when(cfg.getUrlSelectionPolicy()).thenReturn(UrlSelectionPolicy.PREFER_LOCAL);

		IRuntimeStatus bean = new RuntimeStatusBean();

		when(ver.getUrls()).thenReturn(toList(urlLocal1, urlLocal2, urlRemote1, urlRemote2));

		when(statusL1.getStatus()).thenReturn(StatusEnum.ACTIVE);
		when(statusL2.getStatus()).thenReturn(StatusEnum.ACTIVE);
		when(statusR1.getStatus()).thenReturn(StatusEnum.ACTIVE);
		when(statusR2.getStatus()).thenReturn(StatusEnum.ACTIVE);
		DefaultAnswer.setRunTime();
		UrlPoolBean pool = bean.buildUrlPool(ver,null);
		assertEquals(urlLocal1, pool.getPreferredUrl());
		assertEquals(3, pool.getAlternateUrls().size());
		assertEquals(urlLocal2, pool.getAlternateUrls().get(0));
		assertEquals(urlRemote1, pool.getAlternateUrls().get(1));
		assertEquals(urlRemote2, pool.getAlternateUrls().get(2));
		DefaultAnswer.setDesignTime();

		when(statusL1.getStatus()).thenReturn(StatusEnum.DOWN);
		when(statusL2.getStatus()).thenReturn(StatusEnum.ACTIVE);
		when(statusR1.getStatus()).thenReturn(StatusEnum.ACTIVE);
		when(statusR2.getStatus()).thenReturn(StatusEnum.ACTIVE);
		DefaultAnswer.setRunTime();
		pool = bean.buildUrlPool(ver,null);
		assertEquals(urlLocal2, pool.getPreferredUrl());
		assertEquals(2, pool.getAlternateUrls().size());
		assertEquals(urlRemote1, pool.getAlternateUrls().get(0));
		assertEquals(urlRemote2, pool.getAlternateUrls().get(1));
		DefaultAnswer.setDesignTime();

		when(statusL1.getStatus()).thenReturn(StatusEnum.DOWN);
		when(statusL2.getStatus()).thenReturn(StatusEnum.DOWN);
		when(statusR1.getStatus()).thenReturn(StatusEnum.ACTIVE);
		when(statusR2.getStatus()).thenReturn(StatusEnum.ACTIVE);
		DefaultAnswer.setRunTime();
		pool = bean.buildUrlPool(ver,null);
		assertEquals(urlRemote1, pool.getPreferredUrl());
		assertEquals(1, pool.getAlternateUrls().size());
		assertEquals(urlRemote2, pool.getAlternateUrls().get(0));
		DefaultAnswer.setDesignTime();

		when(statusL1.getStatus()).thenReturn(StatusEnum.DOWN);
		when(statusL2.getStatus()).thenReturn(StatusEnum.DOWN);
		when(statusR1.getStatus()).thenReturn(StatusEnum.DOWN);
		when(statusR2.getStatus()).thenReturn(StatusEnum.ACTIVE);
		DefaultAnswer.setRunTime();
		pool = bean.buildUrlPool(ver,null);
		assertEquals(urlRemote2, pool.getPreferredUrl());
		assertEquals(0, pool.getAlternateUrls().size());
		DefaultAnswer.setDesignTime();

		when(statusL1.getStatus()).thenReturn(StatusEnum.DOWN);
		when(statusL2.getStatus()).thenReturn(StatusEnum.DOWN);
		when(statusR1.getStatus()).thenReturn(StatusEnum.DOWN);
		when(statusR2.getStatus()).thenReturn(StatusEnum.DOWN);
		DefaultAnswer.setRunTime();
		pool = bean.buildUrlPool(ver,null);
		assertEquals(null, pool.getPreferredUrl());
		assertEquals(0, pool.getAlternateUrls().size());
		DefaultAnswer.setDesignTime();

		/*
		 * Make sure that if a non-local host is down, we still try to reset its
		 * circuit breaker
		 */
		when(statusL1.getStatus()).thenReturn(StatusEnum.ACTIVE);
		when(statusL2.getStatus()).thenReturn(StatusEnum.ACTIVE);
		when(statusR1.getStatus()).thenReturn(StatusEnum.ACTIVE);
		when(statusR2.getStatus()).thenReturn(StatusEnum.DOWN);
		when(statusR2.attemptToResetCircuitBreaker()).thenReturn(true);
		DefaultAnswer.setRunTime();
		pool = bean.buildUrlPool(ver,null);
		assertEquals(urlRemote2, pool.getPreferredUrl());
		assertEquals(3, pool.getAlternateUrls().size());
		assertEquals(urlLocal1, pool.getAlternateUrls().get(0));
		assertEquals(urlLocal2, pool.getAlternateUrls().get(1));
		assertEquals(urlRemote1, pool.getAlternateUrls().get(2));
		DefaultAnswer.setDesignTime();

	}

	@Test
	public void buildUrlPoolRoundRobin() throws Exception{

		PersServiceVersionUrl urlLocal1 = mock(PersServiceVersionUrl.class);
		PersServiceVersionUrl urlLocal2 = mock(PersServiceVersionUrl.class);
		PersServiceVersionUrl urlRemote1 = mock(PersServiceVersionUrl.class);
		PersServiceVersionUrl urlRemote2 = mock(PersServiceVersionUrl.class);

		when(urlLocal1.toString()).thenReturn("urlLocal1");
		when(urlLocal2.toString()).thenReturn("urlLocal2");
		when(urlRemote1.toString()).thenReturn("urlRemote1");
		when(urlRemote2.toString()).thenReturn("urlRemote2");

		PersServiceVersionUrlStatus statusL1 = mock(PersServiceVersionUrlStatus.class);
		PersServiceVersionUrlStatus statusL2 = mock(PersServiceVersionUrlStatus.class);
		PersServiceVersionUrlStatus statusR1 = mock(PersServiceVersionUrlStatus.class);
		PersServiceVersionUrlStatus statusR2 = mock(PersServiceVersionUrlStatus.class);

		when(urlLocal1.isLocal()).thenReturn(true);
		when(urlLocal1.getUrl()).thenReturn("L1");
		when(urlLocal1.getStatus()).thenReturn(statusL1);
		when(statusL1.getPid()).thenReturn(9990L);
		when(statusL1.attemptToResetCircuitBreaker()).thenReturn(false);

		when(urlLocal2.getStatus()).thenReturn(statusL2);
		when(urlLocal2.isLocal()).thenReturn(true);
		when(urlLocal2.getUrl()).thenReturn("L2");
		when(statusL2.getPid()).thenReturn(9991L);
		when(statusL2.attemptToResetCircuitBreaker()).thenReturn(false);

		when(urlRemote1.getStatus()).thenReturn(statusR1);
		when(urlRemote1.isLocal()).thenReturn(false);
		when(urlRemote1.getUrl()).thenReturn("R1");
		when(statusR1.getPid()).thenReturn(9992L);
		when(statusR1.attemptToResetCircuitBreaker()).thenReturn(false);

		when(urlRemote2.getStatus()).thenReturn(statusR2);
		when(urlRemote2.isLocal()).thenReturn(false);
		when(urlRemote2.getUrl()).thenReturn("R2");
		when(statusR2.getPid()).thenReturn(9993L);
		when(statusR2.attemptToResetCircuitBreaker()).thenReturn(false);

		PersServiceVersionSoap11 ver = mock(PersServiceVersionSoap11.class);

		PersHttpClientConfig cfg = mock(PersHttpClientConfig.class);
		when(ver.getHttpClientConfig()).thenReturn(cfg);
		when(cfg.getUrlSelectionPolicy()).thenReturn(UrlSelectionPolicy.ROUND_ROBIN);
		when(ver.getUrlCounter()).thenReturn(new AtomicInteger());

		IRuntimeStatus bean = new RuntimeStatusBean();

		when(ver.getUrls()).thenReturn(toList(urlLocal1, urlLocal2, urlRemote1, urlRemote2));
		when(statusL1.getStatus()).thenReturn(StatusEnum.ACTIVE);
		when(statusL2.getStatus()).thenReturn(StatusEnum.ACTIVE);
		when(statusR1.getStatus()).thenReturn(StatusEnum.ACTIVE);
		when(statusR2.getStatus()).thenReturn(StatusEnum.DOWN);

		DefaultAnswer.setRunTime();

		// Rotate through everybody

		UrlPoolBean pool = bean.buildUrlPool(ver,null);
		assertEquals(urlLocal1, pool.getPreferredUrl());
		assertEquals(2, pool.getAlternateUrls().size());
		assertEquals(urlLocal2, pool.getAlternateUrls().get(0));
		assertEquals(urlRemote1, pool.getAlternateUrls().get(1));

		pool = bean.buildUrlPool(ver,null);
		assertEquals(urlLocal2, pool.getPreferredUrl());
		assertEquals(2, pool.getAlternateUrls().size());
		assertEquals(urlRemote1, pool.getAlternateUrls().get(0));
		assertEquals(urlLocal1, pool.getAlternateUrls().get(1));

		pool = bean.buildUrlPool(ver,null);
		assertEquals(urlRemote1, pool.getPreferredUrl());
		assertEquals(2, pool.getAlternateUrls().size());
		assertEquals(urlLocal1, pool.getAlternateUrls().get(0));
		assertEquals(urlLocal2, pool.getAlternateUrls().get(1));

		// Three are active so we reset to the first one now.....

		pool = bean.buildUrlPool(ver,null);
		assertEquals(urlLocal1, pool.getPreferredUrl());
		assertEquals(2, pool.getAlternateUrls().size());
		assertEquals(urlLocal2, pool.getAlternateUrls().get(0));
		assertEquals(urlRemote1, pool.getAlternateUrls().get(1));

		/*
		 * The first one actually get repeated once because we're at index 3 in
		 * the previous invocation but index 3 doesn't get used because it's
		 * down so we move on to 0, but then in the next invocation we move on
		 * to 0. This isn't perfect but it doesn't matter... If we ever fix
		 * that, this unit test will break but it will be ok.
		 */
		pool = bean.buildUrlPool(ver,null);
		assertEquals(urlLocal1, pool.getPreferredUrl());
		assertEquals(2, pool.getAlternateUrls().size());
		assertEquals(urlLocal2, pool.getAlternateUrls().get(0));
		assertEquals(urlRemote1, pool.getAlternateUrls().get(1));

		pool = bean.buildUrlPool(ver,null);
		assertEquals(urlLocal2, pool.getPreferredUrl());
		assertEquals(2, pool.getAlternateUrls().size());
		assertEquals(urlRemote1, pool.getAlternateUrls().get(0));
		assertEquals(urlLocal1, pool.getAlternateUrls().get(1));

		pool = bean.buildUrlPool(ver,null);
		assertEquals(urlRemote1, pool.getPreferredUrl());
		assertEquals(2, pool.getAlternateUrls().size());
		assertEquals(urlLocal1, pool.getAlternateUrls().get(0));
		assertEquals(urlLocal2, pool.getAlternateUrls().get(1));

		/*
		 * Now we would normally loop back to the first one since number 4 (AKA
		 * Remote2) is DOWN but make sure that we try downed links occasionally
		 * so that we reset the circuit breaker if things are good now
		 */

		DefaultAnswer.setDesignTime();
		when(statusR2.attemptToResetCircuitBreaker()).thenReturn(true);
		DefaultAnswer.setRunTime();

		pool = bean.buildUrlPool(ver,null);
		assertEquals(urlRemote2, pool.getPreferredUrl());
		assertEquals(3, pool.getAlternateUrls().size());
		assertEquals(urlLocal1, pool.getAlternateUrls().get(0));
		assertEquals(urlLocal2, pool.getAlternateUrls().get(1));
		assertEquals(urlRemote1, pool.getAlternateUrls().get(2));

		DefaultAnswer.setDesignTime();
		when(statusR2.attemptToResetCircuitBreaker()).thenReturn(false);
		DefaultAnswer.setRunTime();

		pool = bean.buildUrlPool(ver,null);
		assertEquals(urlLocal1, pool.getPreferredUrl());
		assertEquals(2, pool.getAlternateUrls().size());
		assertEquals(urlLocal2, pool.getAlternateUrls().get(0));
		assertEquals(urlRemote1, pool.getAlternateUrls().get(1));

		pool = bean.buildUrlPool(ver,null);
		assertEquals(urlLocal2, pool.getPreferredUrl());
		assertEquals(2, pool.getAlternateUrls().size());
		assertEquals(urlRemote1, pool.getAlternateUrls().get(0));
		assertEquals(urlLocal1, pool.getAlternateUrls().get(1));

		pool = bean.buildUrlPool(ver,null);
		assertEquals(urlRemote1, pool.getPreferredUrl());
		assertEquals(2, pool.getAlternateUrls().size());
		assertEquals(urlLocal1, pool.getAlternateUrls().get(0));
		assertEquals(urlLocal2, pool.getAlternateUrls().get(1));

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void testCollapseStats() throws Exception {

		IDao dao = mock(IDao.class);
		IConfigService configSvc = mock(IConfigService.class);

		RuntimeStatusBean svc = new RuntimeStatusBean();
		svc.setDao(dao);
		svc.setConfigSvc(configSvc);

		PersConfig config = new PersConfig();
		config.setDefaults();
		when(configSvc.getConfig()).thenReturn(config);

		PersDomain domain = new PersDomain(ourNextPid++, "domain_id");
		PersService service = new PersService(ourNextPid++, domain, "service_id", "service_name");
		BasePersServiceVersion version = new PersServiceVersionSoap11(ourNextPid++, service, "1.0");
		PersMethod method = new PersMethod(ourNextPid++, version, "method1");

		List<PersInvocationMethodSvcverStats> minuteStats = new ArrayList<PersInvocationMethodSvcverStats>();
		PersInvocationMethodSvcverStats stats = new PersInvocationMethodSvcverStats(new PersInvocationMethodSvcverStatsPk(InvocationStatsIntervalEnum.MINUTE, myFmt.parse("2013-01-01 00:03:00"), method));
		stats.addSuccessInvocation(100, 200, 300);
		minuteStats.add(stats);

		PersInvocationMethodSvcverStats stats2 = new PersInvocationMethodSvcverStats(new PersInvocationMethodSvcverStatsPk(InvocationStatsIntervalEnum.MINUTE, myFmt.parse("2013-01-01 00:03:00"), method));
		stats2.addSuccessInvocation(100, 200, 300);
		minuteStats.add(stats2);

		when(dao.getInvocationStatsBefore(InvocationStatsIntervalEnum.HOUR, myFmt.parse("2013-07-01 00:00:00"))).thenReturn(new ArrayList<PersInvocationMethodSvcverStats>());
		when(dao.getInvocationStatsBefore(InvocationStatsIntervalEnum.TEN_MINUTE, myFmt.parse("2013-01-27 07:00:00"))).thenReturn(new ArrayList<PersInvocationMethodSvcverStats>());
		when(dao.getInvocationStatsBefore(InvocationStatsIntervalEnum.MINUTE, myFmt.parse("2013-04-29 05:00:00"))).thenReturn(minuteStats);

		PersInvocationMethodSvcverStats existingStats = new PersInvocationMethodSvcverStats(new PersInvocationMethodSvcverStatsPk(InvocationStatsIntervalEnum.TEN_MINUTE, myFmt.parse("2013-01-01 00:00:00"), method));
		when(dao.getOrCreateStats(new PersInvocationMethodSvcverStatsPk(InvocationStatsIntervalEnum.TEN_MINUTE, myFmt.parse("2013-01-01 00:00:00"), method))).thenReturn(existingStats);

		svc.setNowForUnitTests(myFmt.parse("2013-04-29 07:00:00"));
		svc.collapseStats();

		ArgumentCaptor<Collection> createCaptor = ArgumentCaptor.forClass(Collection.class);
		ArgumentCaptor<List> deleteCaptor = ArgumentCaptor.forClass(List.class);

		verify(dao).saveInvocationStats(createCaptor.capture(), deleteCaptor.capture());

		assertEquals(1, createCaptor.getAllValues().size());
		PersInvocationMethodSvcverStats obj = (PersInvocationMethodSvcverStats) createCaptor.getValue().iterator().next();
		assertEquals(2, obj.getSuccessInvocationCount());
		
		List<List> allValues = deleteCaptor.getAllValues();
		assertEquals(1, allValues.size());
	}

	@Test
	public void testFlushStatsWithNoneQueued() {

		// Make sure no exception is thrown!
		svc.flushStatus();

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void testRecordServerSecurityFailure() throws Exception {
		Date ts1 = myFmt.parse("2013-01-01 10:00:09");
		Date ts2 = myFmt.parse("2013-01-01 10:00:10");


		PersDomain domain = new PersDomain(ourNextPid++, "domain_id");
		PersService service = new PersService(ourNextPid++, domain, "service_id", "service_name");
		BasePersServiceVersion version = new PersServiceVersionSoap11(ourNextPid++, service, "1.0");
		PersMethod method = new PersMethod(ourNextPid++, version, "method1");
		PersServiceVersionStatus status = new PersServiceVersionStatus(ourNextPid++, version);
		version.setStatus(status);

		PersUser user = new PersUser(32L);
		user.setStatus(new PersUserStatus(33L));

		SrBeanProcessedResponse invocationResponse = new SrBeanProcessedResponse();
		invocationResponse.setResponseType(ResponseTypeEnum.SECURITY_FAIL);
		invocationResponse.setResponseStatusMessage("Security fail");

		svc.recordInvocationMethod(ts1, 0, SrBeanProcessedRequest.forUnitTest(method), user, null, invocationResponse);
		svc.recordInvocationMethod(ts2, 0, SrBeanProcessedRequest.forUnitTest(method), user, null, invocationResponse);

		svc.flushStatus();

		ArgumentCaptor<ArrayList> forClass = ArgumentCaptor.forClass(ArrayList.class);
		verify(dao, times(1)).saveServiceVersionStatuses(forClass.capture());

		PersServiceVersionStatus captSatus = (PersServiceVersionStatus) forClass.getValue().get(0);
		assertEquals(ts2, captSatus.getLastServerSecurityFailure());

		ArgumentCaptor<Collection> forCollection = ArgumentCaptor.forClass(Collection.class);
		verify(dao, times(1)).saveInvocationStats(forCollection.capture());

		Iterator statsIter = forCollection.getValue().iterator();

		for (int i = 0; i < 2; i++) {
			Object next = statsIter.next();
			if (next instanceof PersInvocationMethodUserStats) {
				PersInvocationMethodUserStats userStats = (PersInvocationMethodUserStats) next;
				assertEquals(2, userStats.getServerSecurityFailures());
			} else {
				PersInvocationMethodSvcverStats verStats = (PersInvocationMethodSvcverStats) next;
				assertEquals(2, verStats.getServerSecurityFailures());
			}
		}

	}


	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void testRecordMethodStatus() throws Exception {
		Date ts1 = myFmt.parse("2013-01-01 10:00:09");
		Date ts2 = myFmt.parse("2013-01-01 10:00:10");


		PersDomain domain = new PersDomain(ourNextPid++, "domain_id");
		PersService service = new PersService(ourNextPid++, domain, "service_id", "service_name");
		BasePersServiceVersion version = new PersServiceVersionSoap11(ourNextPid++, service, "1.0");
		PersMethod method = new PersMethod(ourNextPid++, version, "method1");
		PersServiceVersionStatus status = new PersServiceVersionStatus(ourNextPid++, version);
		version.setStatus(status);

		PersUser user = new PersUser(32L);
		user.setStatus(new PersUserStatus(33L));

		SrBeanProcessedResponse invocationResponse = new SrBeanProcessedResponse();
		invocationResponse.setResponseType(ResponseTypeEnum.SUCCESS);
		invocationResponse.setResponseStatusMessage("Msg");

		SrBeanIncomingResponse httpResp=new SrBeanIncomingResponse();
		httpResp.setBody("http response body");
		svc.recordInvocationMethod(ts1, 0, SrBeanProcessedRequest.forUnitTest(method), user, httpResp, invocationResponse);
		svc.recordInvocationMethod(ts2, 0, SrBeanProcessedRequest.forUnitTest(method), user, httpResp, invocationResponse);

		svc.flushStatus();

		ArgumentCaptor<List> forUserStatus = ArgumentCaptor.forClass(List.class);
		verify(dao, times(1)).saveUserStatus(forUserStatus.capture());
		List<PersUserStatus> userStatus = forUserStatus.getValue();

		ArgumentCaptor<List> forMethodStatus = ArgumentCaptor.forClass(List.class);
		verify(dao, times(1)).saveMethodStatuses(forMethodStatus.capture());
		List<PersMethodStatus> methodStatus = forMethodStatus.getValue();

		assertEquals(1, userStatus.size());
		assertEquals(1, userStatus.get(0).getMethodStatuses().size());
		assertTrue(userStatus.get(0).getMethodStatuses().containsKey(method));
		
		PersUserMethodStatus userMethodStatus = userStatus.get(0).getMethodStatuses().get(method);
		assertEquals(ts1, userMethodStatus.getFirstSuccessfulInvocation());
		assertEquals(ts2, userStatus.get(0).getMethodStatuses().get(method).getLastSuccessfulInvocation());

		assertEquals(1, methodStatus.size());
		assertEquals(ts1, methodStatus.get(0).getFirstSuccessfulInvocation());
		assertEquals(ts2, methodStatus.get(0).getLastSuccessfulInvocation());

	}

	
	
	@SuppressWarnings({ "rawtypes", "unchecked", "unused" })
	@Test
	public void testRecordThrottleReject() throws Exception {
		Date ts1 = myFmt.parse("2013-01-01 10:00:09");
		Date ts2 = myFmt.parse("2013-01-01 10:00:10");

		PersDomain domain = new PersDomain(ourNextPid++, "domain_id");
		PersService service = new PersService(ourNextPid++, domain, "service_id", "service_name");
		BasePersServiceVersion version = new PersServiceVersionSoap11(ourNextPid++, service, "1.0");
		PersMethod method = new PersMethod(ourNextPid++, version, "method1");
		PersServiceVersionStatus status = new PersServiceVersionStatus(ourNextPid++, version);
		version.setStatus(status);

		PersUser user = new PersUser(32L);
		user.setStatus(new PersUserStatus(33L));

//		InvocationResponseResultsBean invocationResponse = new InvocationResponseResultsBean();
//		invocationResponse.setResponseType(ResponseTypeEnum.SECURITY_FAIL);
//		invocationResponse.setResponseStatusMessage("Security fail");
//
//		svc.recordInvocationMethod(ts1, 0, method, user, null, invocationResponse, null);
//		svc.recordInvocationMethod(ts2, 0, method, user, null, invocationResponse, null);

		Date invocationTime = ts1;
		int requestLength = 1001;
		SrBeanIncomingResponse httpResponse = null;
		SrBeanProcessedResponse invocationResponseResultsBean = new SrBeanProcessedResponse();
		invocationResponseResultsBean.setResponseType(ResponseTypeEnum.THROTTLE_REJ);
		svc.recordInvocationMethod(invocationTime, requestLength, SrBeanProcessedRequest.forUnitTest(method), user, httpResponse, invocationResponseResultsBean);

		svc.flushStatus();

		ArgumentCaptor<Collection> forCollection = ArgumentCaptor.forClass(Collection.class);
		verify(dao, times(1)).saveInvocationStats(forCollection.capture());

		Iterator statsIter = forCollection.getValue().iterator();

		for (int i = 0; i < 2; i++) {
			Object next = statsIter.next();
			if (next instanceof PersInvocationMethodUserStats) {
				PersInvocationMethodUserStats userStats = (PersInvocationMethodUserStats) next;
				assertEquals(1, userStats.getTotalThrottleRejections());
			} else {
				PersInvocationMethodSvcverStats verStats = (PersInvocationMethodSvcverStats) next;
				assertEquals(1, verStats.getTotalThrottleRejections());
			}
		}

	}

	
	@SuppressWarnings("unchecked")
	@Test
	public void testRecordUrlStatus() {
		
		BasePersServiceVersion svcVer = mock(BasePersServiceVersion.class, new ReturnsDeepStubs());
		
		PersServiceVersionUrl url = new PersServiceVersionUrl();
		url.setServiceVersion(svcVer);
		url.setPid(1L);
		PersServiceVersionUrlStatus status = new PersServiceVersionUrlStatus(10L);
		url.setStatus(status);
		
		PersServiceVersionUrlStatus savedStatus = new PersServiceVersionUrlStatus(10L);
		when(dao.getServiceVersionUrlStatusByPid(10L)).thenReturn(savedStatus);
		
		svc.recordUrlFailure(url, new Failure("fail body", "text/plain", "fail reason", 404, 123L, new HashMap<String, List<String>>()));
		svc.flushStatus();
		
		ArgumentCaptor<List> capt = ArgumentCaptor.forClass(List.class);
		verify(dao, times(1)).saveServiceVersionUrlStatusInNewTransaction(capt.capture());

		/*
		 * Timestamps coming back have a datatype of "Timestamp"
		 */
		PersServiceVersionUrlStatus saved = (PersServiceVersionUrlStatus) capt.getValue().get(0);
		savedStatus.setLastFail(new Timestamp(saved.getLastFail().getTime()));
		savedStatus.setStatusTimestamp(new Timestamp(saved.getStatusTimestamp().getTime()));
		
		svc.flushStatus();
		verify(dao, times(1)).saveServiceVersionUrlStatusInNewTransaction(any(List.class));
		
	}
	
	
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testRecordStaticResource() throws ParseException {
		/*
		 * Record two invocations
		 */

		PersServiceVersionResource resource = mock(PersServiceVersionResource.class);
		SrBeanIncomingResponse httpResponse = mock(SrBeanIncomingResponse.class);
		SrBeanProcessedResponse orchResponse = mock(SrBeanProcessedResponse.class);

		DefaultAnswer.setDesignTime();
		when(resource.getPid()).thenReturn(123L);
		when(httpResponse.getResponseTime()).thenReturn(2000L);
		when(httpResponse.getBody()).thenReturn(StringUtils.leftPad("", 4000));
		when(orchResponse.getResponseType()).thenReturn(ResponseTypeEnum.SUCCESS);
		DefaultAnswer.setRunTime();

		Date ts1 = myFmt.parse("2013-01-01 10:00:03");
		svc.recordInvocationStaticResource(ts1, resource);

		Date ts2 = myFmt.parse("2013-01-01 10:00:22");
		svc.recordInvocationStaticResource(ts2, resource);

		/*
		 * Flush stats
		 */

		svc.flushStatus();

		ArgumentCaptor<List> capt = ArgumentCaptor.forClass(List.class);
		verify(dao, times(1)).saveInvocationStats(capt.capture());
		verify(dao,times(1)).getAllStickySessions();
//		verifyNoMoreInteractions(dao);
		

		List<BasePersStats> value = capt.getValue();
		assertEquals(1, value.size());

		PersStaticResourceStats sr = findSr(value);
		assertEquals(myFmt.parse("2013-01-01 10:00:00"), sr.getPk().getStartTime());
		assertEquals(InvocationStatsIntervalEnum.MINUTE, sr.getPk().getInterval());
		assertSame(resource, sr.getPk().getResource());
		assertEquals(2, sr.getAccessCount());

	}

	@SuppressWarnings("rawtypes")
	private PersStaticResourceStats findSr(List<BasePersStats> theValue) {
		for (BasePersStats next : theValue) {
			if (next instanceof PersStaticResourceStats) {
				return (PersStaticResourceStats) next;
			}
		}
		fail("No anons: " + theValue);
		return null;
	}

	private <T> List<T> toList(T... theObjects) {
		ArrayList<T> retVal = new ArrayList<T>(theObjects.length);
		for (T t : theObjects) {
			retVal.add(t);
		}
		return retVal;
	}
}
