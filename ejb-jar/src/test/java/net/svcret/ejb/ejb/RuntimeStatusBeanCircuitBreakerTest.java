package net.svcret.ejb.ejb;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import net.svcret.admin.shared.model.StatusEnum;
import net.svcret.admin.shared.model.UrlSelectionPolicy;
import net.svcret.ejb.api.HttpResponseBean;
import net.svcret.ejb.api.HttpResponseBean.Failure;
import net.svcret.ejb.api.IDao;
import net.svcret.ejb.api.InvocationResponseResultsBean;
import net.svcret.ejb.api.ResponseTypeEnum;
import net.svcret.ejb.api.UrlPoolBean;
import net.svcret.ejb.model.entity.PersHttpClientConfig;
import net.svcret.ejb.model.entity.PersServiceVersionMethod;
import net.svcret.ejb.model.entity.PersServiceVersionStatus;
import net.svcret.ejb.model.entity.PersServiceVersionUrl;
import net.svcret.ejb.model.entity.PersServiceVersionUrlStatus;
import net.svcret.ejb.model.entity.PersUser;
import net.svcret.ejb.model.entity.PersUserStatus;
import net.svcret.ejb.model.entity.soap.PersServiceVersionSoap11;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

public class RuntimeStatusBeanCircuitBreakerTest {

	private RuntimeStatusBean myBean;
	private IDao myDao;
	private PersServiceVersionMethod myMethod;
	private HttpResponseBean httpResponse;
	private InvocationResponseResultsBean invocationResponse;
	private PersServiceVersionSoap11 svcVersion;
	private PersHttpClientConfig httpConfig;
	private PersServiceVersionUrl persUrl1;
	private PersServiceVersionUrl persUrl2;
	private String url1;
	private String url2;
	private PersServiceVersionUrlStatus url2status;
	private PersServiceVersionUrlStatus url1status;
	private PersUser user;

	@Before
	public void before() {
		DefaultAnswer.setDesignTime();

		myBean = new RuntimeStatusBean();
		
		myDao = mock(IDao.class, new DefaultAnswer());
		myBean.setDao(myDao);
		
		myMethod = mock(PersServiceVersionMethod.class, new DefaultAnswer());
		
		user = mock(PersUser.class, new DefaultAnswer());
		
		when(user.getStatus()).thenReturn(new PersUserStatus(1L));
		
		httpResponse = mock(HttpResponseBean.class, new DefaultAnswer());
		when(httpResponse.getResponseTime()).thenReturn(200L);
		when(httpResponse.getBody()).thenReturn("          ");
		
		invocationResponse = mock(InvocationResponseResultsBean.class, new DefaultAnswer());
		when(invocationResponse.getResponseType()).thenReturn(ResponseTypeEnum.SUCCESS);
		
		svcVersion = mock(PersServiceVersionSoap11.class, new DefaultAnswer());
		when(myMethod.getServiceVersion()).thenReturn(svcVersion);
		httpConfig = mock(PersHttpClientConfig.class, new DefaultAnswer());

		when(svcVersion.getHttpClientConfig()).thenReturn(httpConfig);
		when(httpConfig.getUrlSelectionPolicy()).thenReturn(UrlSelectionPolicy.PREFER_LOCAL);

		
		List<PersServiceVersionUrl> persUrls = new ArrayList<PersServiceVersionUrl>();
		
		// URL 1
		
		persUrl1 = mock(PersServiceVersionUrl.class, new DefaultAnswer());
		persUrls.add(persUrl1);
		url1 = "http://foo1";
		when(persUrl1.getUrl()).thenReturn(url1);
		when(persUrl1.isLocal()).thenReturn(true);
		when(persUrl1.getServiceVersion()).thenReturn(svcVersion);
		when(persUrl1.getPid()).thenReturn(11L);
		when(svcVersion.getUrlWithUrl(url1)).thenReturn(persUrl1);
		
		url1status = new PersServiceVersionUrlStatus(1000L); 
		url1status.setUrl(persUrl1);
		when(persUrl1.getStatus()).thenReturn(url1status);
		
		// URL 2
		
		persUrl2 = mock(PersServiceVersionUrl.class, new DefaultAnswer());
		persUrls.add(persUrl2);
		 url2 = "http://foo2";
		when(persUrl2.getUrl()).thenReturn(url2);
		when(persUrl2.isLocal()).thenReturn(false);
		when(persUrl2.getServiceVersion()).thenReturn(svcVersion);
		when(persUrl2.getPid()).thenReturn(12L);
		when(svcVersion.getUrlWithUrl(url2)).thenReturn(persUrl2);

		url2status = new PersServiceVersionUrlStatus(1001L);
		url2status.setUrl(persUrl2);
		when(persUrl2.getStatus()).thenReturn(url2status);

		when(svcVersion.getUrls()).thenReturn(persUrls);
		when(svcVersion.getStatus()).thenReturn(new PersServiceVersionStatus(1L, svcVersion));

	}
	
	@Test
	public void testCircuitBreaker() throws InterruptedException {
		
		when(httpResponse.getSuccessfulUrl()).thenReturn(persUrl1);
		when(httpResponse.getFailedUrls()).thenReturn(new HashMap<PersServiceVersionUrl, HttpResponseBean.Failure>());
		when(httpConfig.isCircuitBreakerEnabled()).thenReturn(true);
		when(httpConfig.getCircuitBreakerTimeBetweenResetAttempts()).thenReturn(200);
		
		DefaultAnswer.setRunTime();
		
		/*
		 * Normal
		 */
		UrlPoolBean pool = myBean.buildUrlPool(svcVersion);
		assertEquals(persUrl1, pool.getPreferredUrl());
		assertThat(pool.getAlternateUrls(), Matchers.hasSize(1));
		assertThat(pool.getAlternateUrls(), Matchers.contains(persUrl2));

		/*
		 * Mark a success and try again
		 */
		myBean.recordInvocationMethod(new Date(), 100, myMethod, user, httpResponse, invocationResponse);
		pool = myBean.buildUrlPool(svcVersion);
		assertEquals(persUrl1, pool.getPreferredUrl());
		assertThat(pool.getAlternateUrls(), Matchers.hasSize(1));
		assertThat(pool.getAlternateUrls(), Matchers.contains(persUrl2));
		assertEquals(StatusEnum.ACTIVE, url1status.getStatus());
		assertEquals(StatusEnum.UNKNOWN, url2status.getStatus());
		
		/*
		 * Mark a failure
		 */
		DefaultAnswer.setDesignTime();
		HashMap<PersServiceVersionUrl, Failure> failures = new HashMap<PersServiceVersionUrl, HttpResponseBean.Failure>();
		failures.put(persUrl1, new Failure("aaa", "", "Excplanation", 400));
		when(httpResponse.getFailedUrls()).thenReturn(failures);
		when(httpResponse.getSuccessfulUrl()).thenReturn(persUrl2);

		DefaultAnswer.setRunTime();
		myBean.recordInvocationMethod(new Date(), 100, myMethod, user, httpResponse, invocationResponse);
		pool = myBean.buildUrlPool(svcVersion);
		assertEquals(persUrl2, pool.getPreferredUrl());
		assertThat(pool.getAlternateUrls(), Matchers.hasSize(0));
		assertEquals(StatusEnum.DOWN, url1status.getStatus());
		assertEquals(StatusEnum.ACTIVE, url2status.getStatus());

		/*
		 * Make sure we keep using the non-tripped URL
		 */
		pool = myBean.buildUrlPool(svcVersion);
		assertEquals(persUrl2, pool.getPreferredUrl());
		assertThat(pool.getAlternateUrls(), Matchers.hasSize(0));
		assertEquals(StatusEnum.DOWN, url1status.getStatus());
		assertEquals(StatusEnum.ACTIVE, url2status.getStatus());

		/*
		 * Wait a bit and then see if we get just one reset attempt
		 */
		Thread.sleep(200);
		
		pool = myBean.buildUrlPool(svcVersion);
		assertEquals(persUrl1, pool.getPreferredUrl());
		assertThat(pool.getAlternateUrls(), Matchers.hasSize(1));
		assertThat(pool.getAlternateUrls(), Matchers.contains(persUrl2));
		assertEquals(StatusEnum.DOWN, url1status.getStatus());
		assertEquals(StatusEnum.ACTIVE, url2status.getStatus());

		pool = myBean.buildUrlPool(svcVersion);
		assertEquals(persUrl2, pool.getPreferredUrl());
		assertThat(pool.getAlternateUrls(), Matchers.hasSize(0));
		assertEquals(StatusEnum.DOWN, url1status.getStatus());
		assertEquals(StatusEnum.ACTIVE, url2status.getStatus());

	}


	@Test
	public void testCircuitBreakerWithAllFailing() throws InterruptedException {
		
		when(httpResponse.getSuccessfulUrl()).thenReturn(persUrl1);
		when(httpResponse.getFailedUrls()).thenReturn(new HashMap<PersServiceVersionUrl, HttpResponseBean.Failure>());
		when(httpConfig.isCircuitBreakerEnabled()).thenReturn(true);
		when(httpConfig.getCircuitBreakerTimeBetweenResetAttempts()).thenReturn(200);
		
		DefaultAnswer.setRunTime();
		
		/*
		 * Normal
		 */
		UrlPoolBean pool = myBean.buildUrlPool(svcVersion);
		assertEquals(persUrl1, pool.getPreferredUrl());
		assertThat(pool.getAlternateUrls(), Matchers.hasSize(1));
		assertThat(pool.getAlternateUrls(), Matchers.contains(persUrl2));

		/*
		 * Mark a failure
		 */
		DefaultAnswer.setDesignTime();
		HashMap<PersServiceVersionUrl, Failure> failures = new HashMap<PersServiceVersionUrl, HttpResponseBean.Failure>();
		failures.put(persUrl1, new Failure("aaa", "", "Excplanation", 400));
		failures.put(persUrl2, new Failure("aaa", "", "Excplanation", 400));
		when(httpResponse.getFailedUrls()).thenReturn(failures);
		when(httpResponse.getSuccessfulUrl()).thenReturn(null);

		DefaultAnswer.setRunTime();
		myBean.recordInvocationMethod(new Date(), 100, myMethod, user, httpResponse, invocationResponse);
		pool = myBean.buildUrlPool(svcVersion);
		assertEquals(null, pool.getPreferredUrl());
		assertThat(pool.getAlternateUrls(), Matchers.hasSize(0));
		assertEquals(StatusEnum.DOWN, url1status.getStatus());
		assertEquals(StatusEnum.DOWN, url2status.getStatus());

		/*
		 * Wait a bit and then see if we get just one reset attempt
		 */
		Thread.sleep(200);
		
		pool = myBean.buildUrlPool(svcVersion);
		assertEquals(persUrl1, pool.getPreferredUrl());
		assertThat(pool.getAlternateUrls(), Matchers.hasSize(0));

		pool = myBean.buildUrlPool(svcVersion);
		assertEquals(persUrl2, pool.getPreferredUrl());
		assertThat(pool.getAlternateUrls(), Matchers.hasSize(0));

	}

}
