package net.svcret.ejb.ejb;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import net.svcret.admin.shared.enm.ResponseTypeEnum;
import net.svcret.admin.shared.model.StatusEnum;
import net.svcret.admin.shared.model.UrlSelectionPolicy;
import net.svcret.ejb.api.HttpResponseBean;
import net.svcret.ejb.api.IConfigService;
import net.svcret.ejb.api.IDao;
import net.svcret.ejb.api.IRuntimeStatus;
import net.svcret.ejb.api.InvocationResponseResultsBean;
import net.svcret.ejb.api.UrlPoolBean;
import net.svcret.ejb.model.entity.BasePersInvocationStats;
import net.svcret.ejb.model.entity.BasePersServiceVersion;
import net.svcret.ejb.model.entity.InvocationStatsIntervalEnum;
import net.svcret.ejb.model.entity.PersConfig;
import net.svcret.ejb.model.entity.PersDomain;
import net.svcret.ejb.model.entity.PersHttpClientConfig;
import net.svcret.ejb.model.entity.PersInvocationStats;
import net.svcret.ejb.model.entity.PersInvocationStatsPk;
import net.svcret.ejb.model.entity.PersInvocationUserStats;
import net.svcret.ejb.model.entity.PersService;
import net.svcret.ejb.model.entity.PersServiceVersionMethod;
import net.svcret.ejb.model.entity.PersServiceVersionResource;
import net.svcret.ejb.model.entity.PersServiceVersionStatus;
import net.svcret.ejb.model.entity.PersServiceVersionUrl;
import net.svcret.ejb.model.entity.PersServiceVersionUrlStatus;
import net.svcret.ejb.model.entity.PersStaticResourceStats;
import net.svcret.ejb.model.entity.PersUser;
import net.svcret.ejb.model.entity.PersUserStatus;
import net.svcret.ejb.model.entity.soap.PersServiceVersionSoap11;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class RuntimeStatusBeanTest {

	private static long ourNextPid = 1;

	private SimpleDateFormat myFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	@Before
	public void before() {
		DefaultAnswer.setDesignTime();
	}

	@Test
	public void buildUrlPoolPreferLocal() {

		PersServiceVersionUrl urlLocal1 = mock(PersServiceVersionUrl.class, DefaultAnswer.INSTANCE);
		PersServiceVersionUrl urlLocal2 = mock(PersServiceVersionUrl.class, DefaultAnswer.INSTANCE);
		PersServiceVersionUrl urlRemote1 = mock(PersServiceVersionUrl.class, DefaultAnswer.INSTANCE);
		PersServiceVersionUrl urlRemote2 = mock(PersServiceVersionUrl.class, DefaultAnswer.INSTANCE);

		PersServiceVersionUrlStatus statusL1 = mock(PersServiceVersionUrlStatus.class, DefaultAnswer.INSTANCE);
		PersServiceVersionUrlStatus statusL2 = mock(PersServiceVersionUrlStatus.class, DefaultAnswer.INSTANCE);
		PersServiceVersionUrlStatus statusR1 = mock(PersServiceVersionUrlStatus.class, DefaultAnswer.INSTANCE);
		PersServiceVersionUrlStatus statusR2 = mock(PersServiceVersionUrlStatus.class, DefaultAnswer.INSTANCE);

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

		PersServiceVersionSoap11 ver = mock(PersServiceVersionSoap11.class, DefaultAnswer.INSTANCE);

		PersHttpClientConfig cfg = mock(PersHttpClientConfig.class, DefaultAnswer.INSTANCE);
		when(ver.getHttpClientConfig()).thenReturn(cfg);
		when(cfg.getUrlSelectionPolicy()).thenReturn(UrlSelectionPolicy.PREFER_LOCAL);

		IRuntimeStatus bean = new RuntimeStatusBean();

		when(ver.getUrls()).thenReturn(toList(urlLocal1, urlLocal2, urlRemote1, urlRemote2));

		when(statusL1.getStatus()).thenReturn(StatusEnum.ACTIVE);
		when(statusL2.getStatus()).thenReturn(StatusEnum.ACTIVE);
		when(statusR1.getStatus()).thenReturn(StatusEnum.ACTIVE);
		when(statusR2.getStatus()).thenReturn(StatusEnum.ACTIVE);
		DefaultAnswer.setRunTime();
		UrlPoolBean pool = bean.buildUrlPool(ver);
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
		pool = bean.buildUrlPool(ver);
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
		pool = bean.buildUrlPool(ver);
		assertEquals(urlRemote1, pool.getPreferredUrl());
		assertEquals(1, pool.getAlternateUrls().size());
		assertEquals(urlRemote2, pool.getAlternateUrls().get(0));
		DefaultAnswer.setDesignTime();

		when(statusL1.getStatus()).thenReturn(StatusEnum.DOWN);
		when(statusL2.getStatus()).thenReturn(StatusEnum.DOWN);
		when(statusR1.getStatus()).thenReturn(StatusEnum.DOWN);
		when(statusR2.getStatus()).thenReturn(StatusEnum.ACTIVE);
		DefaultAnswer.setRunTime();
		pool = bean.buildUrlPool(ver);
		assertEquals(urlRemote2, pool.getPreferredUrl());
		assertEquals(0, pool.getAlternateUrls().size());
		DefaultAnswer.setDesignTime();

		when(statusL1.getStatus()).thenReturn(StatusEnum.DOWN);
		when(statusL2.getStatus()).thenReturn(StatusEnum.DOWN);
		when(statusR1.getStatus()).thenReturn(StatusEnum.DOWN);
		when(statusR2.getStatus()).thenReturn(StatusEnum.DOWN);
		DefaultAnswer.setRunTime();
		pool = bean.buildUrlPool(ver);
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
		pool = bean.buildUrlPool(ver);
		assertEquals(urlRemote2, pool.getPreferredUrl());
		assertEquals(3, pool.getAlternateUrls().size());
		assertEquals(urlLocal1, pool.getAlternateUrls().get(0));
		assertEquals(urlLocal2, pool.getAlternateUrls().get(1));
		assertEquals(urlRemote1, pool.getAlternateUrls().get(2));
		DefaultAnswer.setDesignTime();

	}

	@Test
	public void buildUrlPoolRoundRobin() {

		PersServiceVersionUrl urlLocal1 = mock(PersServiceVersionUrl.class, DefaultAnswer.INSTANCE);
		PersServiceVersionUrl urlLocal2 = mock(PersServiceVersionUrl.class, DefaultAnswer.INSTANCE);
		PersServiceVersionUrl urlRemote1 = mock(PersServiceVersionUrl.class, DefaultAnswer.INSTANCE);
		PersServiceVersionUrl urlRemote2 = mock(PersServiceVersionUrl.class, DefaultAnswer.INSTANCE);

		when(urlLocal1.toString()).thenReturn("urlLocal1");
		when(urlLocal2.toString()).thenReturn("urlLocal2");
		when(urlRemote1.toString()).thenReturn("urlRemote1");
		when(urlRemote2.toString()).thenReturn("urlRemote2");

		PersServiceVersionUrlStatus statusL1 = mock(PersServiceVersionUrlStatus.class, DefaultAnswer.INSTANCE);
		PersServiceVersionUrlStatus statusL2 = mock(PersServiceVersionUrlStatus.class, DefaultAnswer.INSTANCE);
		PersServiceVersionUrlStatus statusR1 = mock(PersServiceVersionUrlStatus.class, DefaultAnswer.INSTANCE);
		PersServiceVersionUrlStatus statusR2 = mock(PersServiceVersionUrlStatus.class, DefaultAnswer.INSTANCE);

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

		PersServiceVersionSoap11 ver = mock(PersServiceVersionSoap11.class, DefaultAnswer.INSTANCE);

		PersHttpClientConfig cfg = mock(PersHttpClientConfig.class, DefaultAnswer.INSTANCE);
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

		UrlPoolBean pool = bean.buildUrlPool(ver);
		assertEquals(urlLocal1, pool.getPreferredUrl());
		assertEquals(2, pool.getAlternateUrls().size());
		assertEquals(urlLocal2, pool.getAlternateUrls().get(0));
		assertEquals(urlRemote1, pool.getAlternateUrls().get(1));

		pool = bean.buildUrlPool(ver);
		assertEquals(urlLocal2, pool.getPreferredUrl());
		assertEquals(2, pool.getAlternateUrls().size());
		assertEquals(urlRemote1, pool.getAlternateUrls().get(0));
		assertEquals(urlLocal1, pool.getAlternateUrls().get(1));

		pool = bean.buildUrlPool(ver);
		assertEquals(urlRemote1, pool.getPreferredUrl());
		assertEquals(2, pool.getAlternateUrls().size());
		assertEquals(urlLocal1, pool.getAlternateUrls().get(0));
		assertEquals(urlLocal2, pool.getAlternateUrls().get(1));

		// Three are active so we reset to the first one now.....

		pool = bean.buildUrlPool(ver);
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
		pool = bean.buildUrlPool(ver);
		assertEquals(urlLocal1, pool.getPreferredUrl());
		assertEquals(2, pool.getAlternateUrls().size());
		assertEquals(urlLocal2, pool.getAlternateUrls().get(0));
		assertEquals(urlRemote1, pool.getAlternateUrls().get(1));

		pool = bean.buildUrlPool(ver);
		assertEquals(urlLocal2, pool.getPreferredUrl());
		assertEquals(2, pool.getAlternateUrls().size());
		assertEquals(urlRemote1, pool.getAlternateUrls().get(0));
		assertEquals(urlLocal1, pool.getAlternateUrls().get(1));

		pool = bean.buildUrlPool(ver);
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

		pool = bean.buildUrlPool(ver);
		assertEquals(urlRemote2, pool.getPreferredUrl());
		assertEquals(3, pool.getAlternateUrls().size());
		assertEquals(urlLocal1, pool.getAlternateUrls().get(0));
		assertEquals(urlLocal2, pool.getAlternateUrls().get(1));
		assertEquals(urlRemote1, pool.getAlternateUrls().get(2));

		DefaultAnswer.setDesignTime();
		when(statusR2.attemptToResetCircuitBreaker()).thenReturn(false);
		DefaultAnswer.setRunTime();

		pool = bean.buildUrlPool(ver);
		assertEquals(urlLocal1, pool.getPreferredUrl());
		assertEquals(2, pool.getAlternateUrls().size());
		assertEquals(urlLocal2, pool.getAlternateUrls().get(0));
		assertEquals(urlRemote1, pool.getAlternateUrls().get(1));

		pool = bean.buildUrlPool(ver);
		assertEquals(urlLocal2, pool.getPreferredUrl());
		assertEquals(2, pool.getAlternateUrls().size());
		assertEquals(urlRemote1, pool.getAlternateUrls().get(0));
		assertEquals(urlLocal1, pool.getAlternateUrls().get(1));

		pool = bean.buildUrlPool(ver);
		assertEquals(urlRemote1, pool.getPreferredUrl());
		assertEquals(2, pool.getAlternateUrls().size());
		assertEquals(urlLocal1, pool.getAlternateUrls().get(0));
		assertEquals(urlLocal2, pool.getAlternateUrls().get(1));

	}

	@SuppressWarnings({ "rawtypes", "cast", "unchecked" })
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
		PersServiceVersionMethod method = new PersServiceVersionMethod(ourNextPid++, version, "method1");

		List<PersInvocationStats> minuteStats = new ArrayList<PersInvocationStats>();
		PersInvocationStats stats = new PersInvocationStats(new PersInvocationStatsPk(InvocationStatsIntervalEnum.MINUTE, myFmt.parse("2013-01-01 00:03:00"), method));
		stats.addSuccessInvocation(100, 200, 300);
		minuteStats.add(stats);

		PersInvocationStats stats2 = new PersInvocationStats(new PersInvocationStatsPk(InvocationStatsIntervalEnum.MINUTE, myFmt.parse("2013-01-01 00:03:00"), method));
		stats2.addSuccessInvocation(100, 200, 300);
		minuteStats.add(stats2);

		when(dao.getInvocationStatsBefore(InvocationStatsIntervalEnum.HOUR, myFmt.parse("2013-07-01 00:00:00"))).thenReturn(new ArrayList<PersInvocationStats>());
		when(dao.getInvocationStatsBefore(InvocationStatsIntervalEnum.TEN_MINUTE, myFmt.parse("2013-01-27 07:00:00"))).thenReturn(new ArrayList<PersInvocationStats>());
		when(dao.getInvocationStatsBefore(InvocationStatsIntervalEnum.MINUTE, myFmt.parse("2013-04-29 05:00:00"))).thenReturn(minuteStats);

		PersInvocationStats existingStats = new PersInvocationStats(new PersInvocationStatsPk(InvocationStatsIntervalEnum.TEN_MINUTE, myFmt.parse("2013-01-01 00:00:00"), method));
		when(dao.getOrCreateInvocationStats(new PersInvocationStatsPk(InvocationStatsIntervalEnum.TEN_MINUTE, myFmt.parse("2013-01-01 00:00:00"), method))).thenReturn(existingStats);

		svc.setNowForUnitTests(myFmt.parse("2013-04-29 07:00:00"));
		svc.collapseStats();

		ArgumentCaptor<Collection> createCaptor = ArgumentCaptor.forClass(Collection.class);
		ArgumentCaptor<List> deleteCaptor = ArgumentCaptor.forClass(List.class);

		verify(dao).saveInvocationStats((Collection<BasePersInvocationStats>) createCaptor.capture(), (List<BasePersInvocationStats>) deleteCaptor.capture());

		assertEquals(1, createCaptor.getAllValues().size());
		PersInvocationStats obj = (PersInvocationStats) createCaptor.getValue().iterator().next();
		assertEquals(2, obj.getSuccessInvocationCount());
		
		List<List> allValues = deleteCaptor.getAllValues();
		assertEquals(1, allValues.size());
	}

	@Test
	public void testFlushStatsWithNoneQueued() throws ParseException {

		RuntimeStatusBean svc = new RuntimeStatusBean();
		IDao pers = mock(IDao.class);
		svc.setDao(pers);

		// Make sure no exception is thrown!
		svc.flushStatus();

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void testRecordServerSecurityFailure() throws ParseException {
		Date ts1 = myFmt.parse("2013-01-01 10:00:09");
		Date ts2 = myFmt.parse("2013-01-01 10:00:10");

		IDao dao = mock(IDao.class);

		RuntimeStatusBean svc = new RuntimeStatusBean();
		svc.setDao(dao);

		PersDomain domain = new PersDomain(ourNextPid++, "domain_id");
		PersService service = new PersService(ourNextPid++, domain, "service_id", "service_name");
		BasePersServiceVersion version = new PersServiceVersionSoap11(ourNextPid++, service, "1.0");
		PersServiceVersionMethod method = new PersServiceVersionMethod(ourNextPid++, version, "method1");
		PersServiceVersionStatus status = new PersServiceVersionStatus(ourNextPid++, version);
		version.setStatus(status);

		PersUser user = new PersUser(32L);
		user.setStatus(new PersUserStatus(33L));

		InvocationResponseResultsBean invocationResponse = new InvocationResponseResultsBean();
		invocationResponse.setResponseType(ResponseTypeEnum.SECURITY_FAIL);
		invocationResponse.setResponseStatusMessage("Security fail");

		svc.recordInvocationMethod(ts1, 0, method, user, null, invocationResponse, null);
		svc.recordInvocationMethod(ts2, 0, method, user, null, invocationResponse, null);

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
			if (next instanceof PersInvocationUserStats) {
				PersInvocationUserStats userStats = (PersInvocationUserStats) next;
				assertEquals(2, userStats.getServerSecurityFailures());
			} else {
				PersInvocationStats verStats = (PersInvocationStats) next;
				assertEquals(2, verStats.getServerSecurityFailures());
			}
		}

	}


	
	@SuppressWarnings({ "rawtypes", "unchecked", "unused" })
	@Test
	public void testRecordThrottleReject() throws ParseException {
		Date ts1 = myFmt.parse("2013-01-01 10:00:09");
		Date ts2 = myFmt.parse("2013-01-01 10:00:10");

		IDao dao = mock(IDao.class);

		RuntimeStatusBean svc = new RuntimeStatusBean();
		svc.setDao(dao);

		PersDomain domain = new PersDomain(ourNextPid++, "domain_id");
		PersService service = new PersService(ourNextPid++, domain, "service_id", "service_name");
		BasePersServiceVersion version = new PersServiceVersionSoap11(ourNextPid++, service, "1.0");
		PersServiceVersionMethod method = new PersServiceVersionMethod(ourNextPid++, version, "method1");
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
		HttpResponseBean httpResponse = null;
		InvocationResponseResultsBean invocationResponseResultsBean = new InvocationResponseResultsBean();
		invocationResponseResultsBean.setResponseType(ResponseTypeEnum.THROTTLE_REJ);
		svc.recordInvocationMethod(invocationTime, requestLength, method, user, httpResponse, invocationResponseResultsBean, null);

		svc.flushStatus();

		ArgumentCaptor<Collection> forCollection = ArgumentCaptor.forClass(Collection.class);
		verify(dao, times(1)).saveInvocationStats(forCollection.capture());

		Iterator statsIter = forCollection.getValue().iterator();

		for (int i = 0; i < 2; i++) {
			Object next = statsIter.next();
			if (next instanceof PersInvocationUserStats) {
				PersInvocationUserStats userStats = (PersInvocationUserStats) next;
				assertEquals(1, userStats.getTotalThrottleRejections());
			} else {
				PersInvocationStats verStats = (PersInvocationStats) next;
				assertEquals(1, verStats.getTotalThrottleRejections());
			}
		}

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testRecordStaticResource() throws ParseException {
		/*
		 * Record two invocations
		 */

		RuntimeStatusBean svc = new RuntimeStatusBean();

		PersServiceVersionResource resource = mock(PersServiceVersionResource.class, DefaultAnswer.INSTANCE);
		HttpResponseBean httpResponse = mock(HttpResponseBean.class, DefaultAnswer.INSTANCE);
		InvocationResponseResultsBean orchResponse = mock(InvocationResponseResultsBean.class, DefaultAnswer.INSTANCE);

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

		IDao pers = mock(IDao.class);
		svc.setDao(pers);
		svc.flushStatus();

		ArgumentCaptor<List> capt = ArgumentCaptor.forClass(List.class);
		verify(pers, times(1)).saveInvocationStats(capt.capture());
		verifyNoMoreInteractions(pers);

		List<BasePersInvocationStats> value = capt.getValue();
		assertEquals(1, value.size());

		PersStaticResourceStats sr = findSr(value);
		assertEquals(myFmt.parse("2013-01-01 10:00:00"), sr.getPk().getStartTime());
		assertEquals(InvocationStatsIntervalEnum.MINUTE, sr.getPk().getInterval());
		assertSame(resource, sr.getPk().getResource());
		assertEquals(2, sr.getAccessCount());

	}

	private PersStaticResourceStats findSr(List<BasePersInvocationStats> theValue) {
		for (BasePersInvocationStats next : theValue) {
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
