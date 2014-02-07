package net.svcret.ejb.throttle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.svcret.admin.shared.enm.ThrottlePeriodEnum;
import net.svcret.ejb.api.IRuntimeStatus;
import net.svcret.ejb.api.ISecurityService.AuthorizationResultsBean;
import net.svcret.ejb.api.SrBeanIncomingRequest;
import net.svcret.ejb.api.SrBeanIncomingResponse;
import net.svcret.ejb.api.SrBeanProcessedRequest;
import net.svcret.ejb.api.SrBeanProcessedResponse;
import net.svcret.ejb.model.entity.PersMethod;
import net.svcret.ejb.model.entity.PersUser;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.stubbing.defaultanswers.ReturnsDeepStubs;

import com.google.common.collect.Lists;

public class ThrottlingServiceTest {

	private ThrottlingService mySvc;
	private IThrottlingService myThis;
	private IRuntimeStatus myRuntimeStatusSvc;

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(ThrottlingServiceTest.class);

	@Before
	public void setUp() {
		mySvc = new ThrottlingService();
		myThis = mock(IThrottlingService.class);
		mySvc.setThisForTesting(myThis);
		myRuntimeStatusSvc = mock(IRuntimeStatus.class);
		mySvc.setRuntimeStatusSvcForTesting(myRuntimeStatusSvc);
	}

	
	@Test
	public void testExecuteSlowRate() throws ThrottleException, ThrottleQueueFullException, InterruptedException {

		when(myThis.serviceThrottledRequests((ThrottledTaskQueue) any())).thenReturn(null);
		PersMethod method = mock(PersMethod.class, new ReturnsDeepStubs());
		when(method.getServiceVersion().getThrottle()).thenReturn(null);
		
		/*
		 * Rate is less than one per second
		 */
		
		PersUser user = new PersUser();
		user.setThrottleMaxRequests(40);
		user.setThrottlePeriod(ThrottlePeriodEnum.MINUTE);
		user.setThrottleMaxQueueDepth(0);

		SrBeanIncomingRequest httpRequest = new SrBeanIncomingRequest();
		httpRequest.setInputReader(new StringReader(""));

		SrBeanProcessedRequest invocationRequest = new SrBeanProcessedRequest();
		invocationRequest.setResultMethod(method, "", "");
		AuthorizationResultsBean authorization = new AuthorizationResultsBean();

		authorization.setAuthorizedUser(user);

		mySvc.applyThrottle(httpRequest, invocationRequest, authorization);
		applyThrottleAndExpectQueueFull(httpRequest, invocationRequest, authorization);
		applyThrottleAndExpectQueueFull(httpRequest, invocationRequest, authorization);
		applyThrottleAndExpectQueueFull(httpRequest, invocationRequest, authorization);
		
		Thread.sleep(1501);
		
		mySvc.applyThrottle(httpRequest, invocationRequest, authorization);
		applyThrottleAndExpectQueueFull(httpRequest, invocationRequest, authorization);
		applyThrottleAndExpectQueueFull(httpRequest, invocationRequest, authorization);
		applyThrottleAndExpectQueueFull(httpRequest, invocationRequest, authorization);

		Thread.sleep(1501);
		
		mySvc.applyThrottle(httpRequest, invocationRequest, authorization);
		applyThrottleAndExpectQueueFull(httpRequest, invocationRequest, authorization);
		applyThrottleAndExpectQueueFull(httpRequest, invocationRequest, authorization);
		applyThrottleAndExpectQueueFull(httpRequest, invocationRequest, authorization);

	}


	private void applyThrottleAndExpectQueueFull(SrBeanIncomingRequest httpRequest, SrBeanProcessedRequest invocationRequest, AuthorizationResultsBean authorization) throws ThrottleException {
		try {
		mySvc.applyThrottle(httpRequest, invocationRequest, authorization);
		fail();
		} catch (ThrottleQueueFullException e) {
			// expected
		}
	}
	
	@Test
	public void testExecuteThrottledUser() throws ThrottleException, ThrottleQueueFullException, InterruptedException {

		when(myThis.serviceThrottledRequests((ThrottledTaskQueue) any())).thenReturn(null);
		PersMethod method = mock(PersMethod.class, new ReturnsDeepStubs());
		when(method.getServiceVersion().getThrottle()).thenReturn(null);
		
		PersUser user = new PersUser();
		user.setThrottleMaxRequests(2);
		user.setThrottlePeriod(ThrottlePeriodEnum.SECOND);
		user.setThrottleMaxQueueDepth(2);

		SrBeanIncomingRequest httpRequest = new SrBeanIncomingRequest();
		httpRequest.setInputReader(new StringReader(""));

		SrBeanProcessedRequest invocationRequest = new SrBeanProcessedRequest();
		invocationRequest.setResultMethod(method, "", "");
		AuthorizationResultsBean authorization = new AuthorizationResultsBean();

		authorization.setAuthorizedUser(user);

		mySvc.applyThrottle(httpRequest, invocationRequest, authorization);

		Thread.sleep(1001);

		mySvc.applyThrottle(httpRequest, invocationRequest, authorization);
		mySvc.applyThrottle(httpRequest, invocationRequest, authorization);

		AsyncContext asyncContext = mock(AsyncContext.class);
		when(asyncContext.getRequest()).thenReturn(mock(HttpServletRequest.class));
		when(asyncContext.getResponse()).thenReturn(mock(HttpServletResponse.class));

		ThrottleException e1 = null;
		ThrottleException e2 = null;
		try {
			mySvc.applyThrottle(httpRequest, invocationRequest, authorization);
			Assert.fail();
		} catch (ThrottleException e) {
			e1 = e;
			e1.setAsyncContext(asyncContext);
			mySvc.scheduleThrottledTaskForLaterExecution(e, asyncContext);
		}

		try {
			mySvc.applyThrottle(httpRequest, invocationRequest, authorization);
			Assert.fail();
		} catch (ThrottleException e) {
			e2 = e;
			e2.setAsyncContext(asyncContext);
			mySvc.scheduleThrottledTaskForLaterExecution(e, asyncContext);
		}

		try {
			mySvc.applyThrottle(httpRequest, invocationRequest, authorization);
			Assert.fail();
		} catch (ThrottleException e) {
			try {
				e.setAsyncContext(asyncContext);
				mySvc.scheduleThrottledTaskForLaterExecution(e, asyncContext);
				Assert.fail();
			} catch (ThrottleQueueFullException te2) {
				// expected
			}
		}

		verify(myThis, times(2)).serviceThrottledRequests((ThrottledTaskQueue) any());

	}

	@Test
	public void testExecuteThrottledUserAndPropertyCapture() throws ThrottleException, ThrottleQueueFullException, InterruptedException {

		when(myThis.serviceThrottledRequests((ThrottledTaskQueue) any())).thenReturn(null);
		PersMethod method = mock(PersMethod.class, new ReturnsDeepStubs());
		when(method.getServiceVersion().getThrottle().getApplyPropCapName()).thenReturn("propCapName");
		when(method.getServiceVersion().getThrottle().getThrottleMaxQueueDepth()).thenReturn(2);
		when(method.getServiceVersion().getThrottle().getThrottleMaxRequests()).thenReturn(2);
		when(method.getServiceVersion().getThrottle().getThrottlePeriod()).thenReturn(ThrottlePeriodEnum.SECOND);
		
		PersUser user = new PersUser();
		user.setThrottleMaxRequests(1);
		user.setThrottlePeriod(ThrottlePeriodEnum.SECOND);
		user.setThrottleMaxQueueDepth(2);

		SrBeanIncomingRequest httpRequest = new SrBeanIncomingRequest();
		httpRequest.setInputReader(new StringReader(""));

		SrBeanProcessedRequest invocationRequest = new SrBeanProcessedRequest();
		invocationRequest.setResultMethod(method, "", "");
		invocationRequest.addPropertyCapture("propCapName", "propCapValue");
		AuthorizationResultsBean authorization = new AuthorizationResultsBean();

		authorization.setAuthorizedUser(user);

		mySvc.applyThrottle(httpRequest, invocationRequest, authorization);

		Thread.sleep(1001);

		mySvc.applyThrottle(httpRequest, invocationRequest, authorization);

		try {
			mySvc.applyThrottle(httpRequest, invocationRequest, authorization);
			fail();
		} catch (ThrottleException e) {
			ourLog.info("Throttle failed because of {}", e.getFirstThrottleKey());
			assertEquals(1, e.getRateLimiters().size());
		}

		try {
			mySvc.applyThrottle(httpRequest, invocationRequest, authorization);
			fail();
		} catch (ThrottleException e) {
			ourLog.info("Throttle failed because of {}", e.getFirstThrottleKey());
			assertEquals(2, e.getRateLimiters().size());
		}

		try {
			mySvc.applyThrottle(httpRequest, invocationRequest, authorization);
			fail();
		} catch (ThrottleException e) {
			ourLog.info("Throttle failed because of {}", e.getFirstThrottleKey());
			assertEquals(2, e.getRateLimiters().size());
		}



	}
	@Test
	public void testRecordStatsForQueueFull() throws Exception {
		SrBeanIncomingRequest httpRequest = new SrBeanIncomingRequest();
		httpRequest.setInputReader(new StringReader(""));

		FlexibleRateLimiter rateLimiter = new FlexibleRateLimiter(2);
		ArrayList<FlexibleRateLimiter> rateLimiters = Lists.newArrayList(rateLimiter);

		SrBeanProcessedRequest invocationRequest = new SrBeanProcessedRequest();
		invocationRequest.setResultMethod(new PersMethod(), null, null);
		AuthorizationResultsBean authorization = new AuthorizationResultsBean();

		LimiterKey throttleKey = new LimiterKey(null, null, null, 12, 2);

		ThrottleException e = new ThrottleException(httpRequest, rateLimiters, invocationRequest, authorization, throttleKey);

		AsyncContext asyncContext = mock(AsyncContext.class);
		when(asyncContext.getRequest()).thenReturn(mock(HttpServletRequest.class));
		when(asyncContext.getResponse()).thenReturn(mock(HttpServletResponse.class));
		e.setAsyncContext(asyncContext);

		mySvc.scheduleThrottledTaskForLaterExecution(e, asyncContext);
		mySvc.scheduleThrottledTaskForLaterExecution(e, asyncContext);

		try {
			mySvc.scheduleThrottledTaskForLaterExecution(e, asyncContext);
			fail();
		} catch (ThrottleQueueFullException e2) {
			// expected
		}

		verify(myRuntimeStatusSvc).recordInvocationMethod((Date) any(), eq(0), (SrBeanProcessedRequest) any(), (PersUser) any(), (SrBeanIncomingResponse)any(), (SrBeanProcessedResponse) any());

	}
	
}
