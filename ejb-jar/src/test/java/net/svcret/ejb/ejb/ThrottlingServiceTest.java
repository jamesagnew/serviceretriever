package net.svcret.ejb.ejb;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.StringReader;

import javax.ejb.AsyncResult;
import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.svcret.admin.shared.enm.ThrottlePeriodEnum;
import net.svcret.ejb.api.HttpRequestBean;
import net.svcret.ejb.api.ISecurityService.AuthorizationResultsBean;
import net.svcret.ejb.api.IRuntimeStatus;
import net.svcret.ejb.api.IThrottlingService;
import net.svcret.ejb.api.InvocationResultsBean;
import net.svcret.ejb.ejb.ThrottlingService.ThrottledTaskQueue;
import net.svcret.ejb.ex.ThrottleException;
import net.svcret.ejb.model.entity.PersUser;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ThrottlingServiceTest {

	private ThrottlingService mySvc;
	private IThrottlingService myThis;

	@Before
	public void setUp() {
		mySvc = new ThrottlingService();
		myThis = mock(IThrottlingService.class);
		mySvc.setThisForTesting(myThis);
		mySvc.setRuntimeStatusSvcForTesting(mock(IRuntimeStatus.class));
	}

	@Test
	public void testExecuteThrottledUser() throws ThrottleException, ThrottleQueueFullException, InterruptedException {

		when(myThis.serviceThrottledRequests((ThrottledTaskQueue) any())).thenReturn(new AsyncResult<Void>(null));

		PersUser user = new PersUser();
		user.setThrottleMaxRequests(2);
		user.setThrottlePeriod(ThrottlePeriodEnum.SECOND);
		user.setThrottleMaxQueueDepth(2);

		HttpRequestBean httpRequest = new HttpRequestBean();
		httpRequest.setInputReader(new StringReader(""));
		
		InvocationResultsBean invocationRequest = new InvocationResultsBean();
		invocationRequest.setResultMethod(null, "", "", null);
		AuthorizationResultsBean authorization = new AuthorizationResultsBean();

		authorization.setUser(user);

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
			mySvc.scheduleThrottledTaskForLaterExecution(e);
		}

		try {
			mySvc.applyThrottle(httpRequest, invocationRequest, authorization);
			Assert.fail();
		} catch (ThrottleException e) {
			e2 = e;
			e2.setAsyncContext(asyncContext);
			mySvc.scheduleThrottledTaskForLaterExecution(e);
		}

		try {
			mySvc.applyThrottle(httpRequest, invocationRequest, authorization);
			Assert.fail();
		} catch (ThrottleException e) {
			try {
				e.setAsyncContext(asyncContext);
				mySvc.scheduleThrottledTaskForLaterExecution(e);
				Assert.fail();
			} catch (ThrottleQueueFullException te2) {
				// expected
			}
		}

		verify(myThis, times(2)).serviceThrottledRequests((ThrottledTaskQueue) any());

	}

}
