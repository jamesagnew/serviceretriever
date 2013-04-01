package net.svcret.ejb.ejb;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import net.svcret.admin.shared.model.StatusEnum;
import net.svcret.admin.shared.model.UrlSelectionPolicy;
import net.svcret.ejb.api.HttpResponseBean;
import net.svcret.ejb.api.HttpResponseBean.Failure;
import net.svcret.ejb.api.IServicePersistence;
import net.svcret.ejb.api.InvocationResponseResultsBean;
import net.svcret.ejb.api.ResponseTypeEnum;
import net.svcret.ejb.api.UrlPoolBean;
import net.svcret.ejb.model.entity.BasePersInvocationStats;
import net.svcret.ejb.model.entity.BasePersMethodStats;
import net.svcret.ejb.model.entity.BasePersServiceVersion;
import net.svcret.ejb.model.entity.InvocationStatsIntervalEnum;
import net.svcret.ejb.model.entity.PersHttpClientConfig;
import net.svcret.ejb.model.entity.PersInvocationAnonStats;
import net.svcret.ejb.model.entity.PersInvocationStats;
import net.svcret.ejb.model.entity.PersInvocationUserStats;
import net.svcret.ejb.model.entity.PersUser;
import net.svcret.ejb.model.entity.PersServiceVersionMethod;
import net.svcret.ejb.model.entity.PersServiceVersionResource;
import net.svcret.ejb.model.entity.PersServiceVersionUrl;
import net.svcret.ejb.model.entity.PersServiceVersionUrlStatus;
import net.svcret.ejb.model.entity.PersStaticResourceStats;
import net.svcret.ejb.model.entity.soap.PersServiceVersionSoap11;

import org.apache.commons.lang3.StringUtils;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;


public class RuntimeStatusBeanTest {

	private SimpleDateFormat myFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	private PersInvocationAnonStats findAnon(List<BasePersInvocationStats> theValue) {
		for (BasePersInvocationStats next : theValue) {
			if (next instanceof PersInvocationAnonStats) {
				return (PersInvocationAnonStats) next;
			}
		}
		fail("No anons: " + theValue);
		return null;
	}
	
	
	private PersStaticResourceStats findSr(List<BasePersMethodStats> theValue) {
		for (BasePersMethodStats next : theValue) {
			if (next instanceof PersStaticResourceStats) {
				return (PersStaticResourceStats) next;
			}
		}
		fail("No anons: " + theValue);
		return null;
	}
	
	private PersInvocationStats findStats(List<BasePersInvocationStats> theValue) {
		for (BasePersInvocationStats next : theValue) {
			if (next instanceof PersInvocationStats) {
				return (PersInvocationStats) next;
			}
		}
		fail("No anons: " + theValue);
		return null;
	}


	private PersInvocationUserStats findUser(List<BasePersInvocationStats> theValue) {
		for (BasePersInvocationStats next : theValue) {
			if (next instanceof PersInvocationUserStats) {
				return (PersInvocationUserStats) next;
			}
		}
		fail("No anons: " + theValue);
		return null;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testRecordInvocationMethodAnon() throws ParseException {
		/*
		 * Record two invocations
		 */
		
		RuntimeStatusBean svc = new RuntimeStatusBean();
		
		PersServiceVersionMethod method = mock(PersServiceVersionMethod.class, DefaultAnswer.INSTANCE);
		HttpResponseBean httpResponse = mock(HttpResponseBean.class, DefaultAnswer.INSTANCE);
		InvocationResponseResultsBean orchResponse = mock(InvocationResponseResultsBean.class, DefaultAnswer.INSTANCE);
		PersServiceVersionSoap11 svcVer = mock(PersServiceVersionSoap11.class, DefaultAnswer.INSTANCE);
		PersServiceVersionUrl url = mock(PersServiceVersionUrl.class, DefaultAnswer.INSTANCE);
		PersServiceVersionUrlStatus status = mock(PersServiceVersionUrlStatus.class, DefaultAnswer.INSTANCE);

		DefaultAnswer.setDesignTime();
		when(status.getPid()).thenReturn(1122L);
		when(url.getStatus()).thenReturn(status);
		when(method.getPid()).thenReturn(123L);
		when(method.getServiceVersion()).thenReturn(svcVer);
		when(httpResponse.getResponseTime()).thenReturn(2000L);
		when(httpResponse.getBody()).thenReturn(StringUtils.leftPad("", 4000));
		when(orchResponse.getResponseType()).thenReturn(ResponseTypeEnum.SUCCESS);
		when(httpResponse.getSuccessfulUrl()).thenReturn("http://foo");
		when(svcVer.getUrlWithUrl("http://foo")).thenReturn(url);
		when(status.getUrl()).thenReturn(url);
		when(status.getStatus()).thenReturn(StatusEnum.ACTIVE);
		when(httpResponse.getFailedUrls()).thenReturn(new HashMap<String, HttpResponseBean.Failure>());
		DefaultAnswer.setRunTime();
		
		Date ts1 = myFmt.parse("2013-01-01 10:00:03");
		int requestLength1 = 1000;
		svc.recordInvocationMethod(ts1, requestLength1, method, null, httpResponse, orchResponse);

		verify(status, times(1)).setStatus(StatusEnum.ACTIVE);
		
		DefaultAnswer.setDesignTime();
		when(httpResponse.getResponseTime()).thenReturn(4000L);
		when(httpResponse.getBody()).thenReturn(StringUtils.leftPad("", 8000));
		when(status.isDirty()).thenReturn(false);
		DefaultAnswer.setRunTime();

		Date ts2 = myFmt.parse("2013-01-01 10:00:22");
		int requestLength2 = 2000;
		svc.recordInvocationMethod(ts2, requestLength2, method, null, httpResponse, orchResponse);
		
		/*
		 * Flush stats
		 */
		
		IServicePersistence pers = mock(IServicePersistence.class);
		svc.setPersistence(pers);
		svc.flushStatus();
		
		ArgumentCaptor<List> capt = ArgumentCaptor.forClass(List.class);
		verify(pers, times(1)).saveInvocationStats(capt.capture());
		verifyNoMoreInteractions(pers);
		
		List<BasePersInvocationStats> value = capt.getValue();
		assertEquals(2, value.size());
		
		PersInvocationAnonStats anon = findAnon(value);
		assertEquals(myFmt.parse("2013-01-01 10:00:00"), anon.getPk().getStartTime());
		assertEquals(InvocationStatsIntervalEnum.MINUTE, anon.getPk().getInterval());
		assertSame(method, anon.getPk().getMethod());
		assertEquals(2, anon.getSuccessInvocationCount());
		assertEquals(3000, anon.getSuccessInvocationAvgTime());
		assertEquals(1000, anon.getMinSuccessRequestMessageBytes());
		assertEquals(2000, anon.getMaxSuccessRequestMessageBytes());
		assertEquals(4000, anon.getMinSuccessResponseMessageBytes());
		assertEquals(8000, anon.getMaxSuccessResponseMessageBytes());

		PersInvocationStats stats = findStats(value);
		assertEquals(myFmt.parse("2013-01-01 10:00:00"), stats.getPk().getStartTime());
		assertEquals(InvocationStatsIntervalEnum.MINUTE, stats.getPk().getInterval());
		assertSame(method, stats.getPk().getMethod());
		assertEquals(2, stats.getSuccessInvocationCount());
		assertEquals(3000, stats.getSuccessInvocationAvgTime());
		assertEquals(1000, stats.getMinSuccessRequestMessageBytes());
		assertEquals(2000, stats.getMaxSuccessRequestMessageBytes());
		assertEquals(4000, stats.getMinSuccessResponseMessageBytes());
		assertEquals(8000, stats.getMaxSuccessResponseMessageBytes());

	}

	@Test
	public void testFlushStatsWithNoneQueued() throws ParseException {
		
		RuntimeStatusBean svc = new RuntimeStatusBean();
		IServicePersistence pers = mock(IServicePersistence.class);
		svc.setPersistence(pers);
		
		// Make sure no exception is thrown!
		svc.flushStatus();

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
		
		IServicePersistence pers = mock(IServicePersistence.class);
		svc.setPersistence(pers);
		svc.flushStatus();
		
		ArgumentCaptor<List> capt = ArgumentCaptor.forClass(List.class);
		verify(pers, times(1)).saveInvocationStats(capt.capture());
		verifyNoMoreInteractions(pers);
		
		List<BasePersMethodStats> value = capt.getValue();
		assertEquals(1, value.size());
		
		PersStaticResourceStats sr = findSr(value);
		assertEquals(myFmt.parse("2013-01-01 10:00:00"), sr.getPk().getStartTime());
		assertEquals(InvocationStatsIntervalEnum.MINUTE, sr.getPk().getInterval());
		assertSame(resource, sr.getPk().getResource());
		assertEquals(2, sr.getAccessCount());

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
		
		RuntimeStatusBean bean = new RuntimeStatusBean();
		
		when(ver.getUrls()).thenReturn(toList(urlLocal1, urlLocal2, urlRemote1, urlRemote2));
		
		when(statusL1.getStatus()).thenReturn(StatusEnum.ACTIVE);
		when(statusL2.getStatus()).thenReturn(StatusEnum.ACTIVE);
		when(statusR1.getStatus()).thenReturn(StatusEnum.ACTIVE);
		when(statusR2.getStatus()).thenReturn(StatusEnum.ACTIVE);
		DefaultAnswer.setRunTime();
		UrlPoolBean pool = bean.buildUrlPool(ver);
		assertEquals("L1", pool.getPreferredUrl());
		assertEquals(3, pool.getAlternateUrls().size());
		assertEquals("L2", pool.getAlternateUrls().get(0));
		assertEquals("R1", pool.getAlternateUrls().get(1));
		assertEquals("R2", pool.getAlternateUrls().get(2));
		DefaultAnswer.setDesignTime();
		
		
		when(statusL1.getStatus()).thenReturn(StatusEnum.DOWN);
		when(statusL2.getStatus()).thenReturn(StatusEnum.ACTIVE);
		when(statusR1.getStatus()).thenReturn(StatusEnum.ACTIVE);
		when(statusR2.getStatus()).thenReturn(StatusEnum.ACTIVE);
		DefaultAnswer.setRunTime();
		pool = bean.buildUrlPool(ver);
		assertEquals("L2", pool.getPreferredUrl());
		assertEquals(2, pool.getAlternateUrls().size());
		assertEquals("R1", pool.getAlternateUrls().get(0));
		assertEquals("R2", pool.getAlternateUrls().get(1));
		DefaultAnswer.setDesignTime();

		when(statusL1.getStatus()).thenReturn(StatusEnum.DOWN);
		when(statusL2.getStatus()).thenReturn(StatusEnum.DOWN);
		when(statusR1.getStatus()).thenReturn(StatusEnum.ACTIVE);
		when(statusR2.getStatus()).thenReturn(StatusEnum.ACTIVE);
		DefaultAnswer.setRunTime();
		pool = bean.buildUrlPool(ver);
		assertEquals("R1", pool.getPreferredUrl());
		assertEquals(1, pool.getAlternateUrls().size());
		assertEquals("R2", pool.getAlternateUrls().get(0));
		DefaultAnswer.setDesignTime();

		when(statusL1.getStatus()).thenReturn(StatusEnum.DOWN);
		when(statusL2.getStatus()).thenReturn(StatusEnum.DOWN);
		when(statusR1.getStatus()).thenReturn(StatusEnum.DOWN);
		when(statusR2.getStatus()).thenReturn(StatusEnum.ACTIVE);
		DefaultAnswer.setRunTime();
		pool = bean.buildUrlPool(ver);
		assertEquals("R2", pool.getPreferredUrl());
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
		 * Make sure that if a non-local host is down, we still try
		 * to reset its circuit breaker
		 */
		when(statusL1.getStatus()).thenReturn(StatusEnum.ACTIVE);
		when(statusL2.getStatus()).thenReturn(StatusEnum.ACTIVE);
		when(statusR1.getStatus()).thenReturn(StatusEnum.ACTIVE);
		when(statusR2.getStatus()).thenReturn(StatusEnum.DOWN);
		when(statusR2.attemptToResetCircuitBreaker()).thenReturn(true);
		DefaultAnswer.setRunTime();
		pool = bean.buildUrlPool(ver);
		assertEquals("R2", pool.getPreferredUrl());
		assertEquals(3, pool.getAlternateUrls().size());
		assertEquals("L1", pool.getAlternateUrls().get(0));
		assertEquals("L2", pool.getAlternateUrls().get(1));
		assertEquals("R1", pool.getAlternateUrls().get(2));
		DefaultAnswer.setDesignTime();
		
		
	}
	
	
	
	private <T> List<T> toList(T... theObjects) {
		ArrayList<T> retVal = new ArrayList<T>(theObjects.length);
		for (T t : theObjects) {
			retVal.add(t);
		}
		return retVal;
	}

	@Before
	public void before() {
		DefaultAnswer.setDesignTime();
	}
}
