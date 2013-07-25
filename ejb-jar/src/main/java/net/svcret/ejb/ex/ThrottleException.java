package net.svcret.ejb.ex;

import java.util.Date;

import javax.servlet.AsyncContext;

import net.svcret.ejb.api.HttpRequestBean;
import net.svcret.ejb.api.ISecurityService.AuthorizationResultsBean;
import net.svcret.ejb.api.InvocationResultsBean;
import net.svcret.ejb.model.entity.IThrottleable;
import net.svcret.ejb.model.entity.PersUser;

import com.google.common.util.concurrent.RateLimiter;

public class ThrottleException extends Exception {

	private static final long serialVersionUID = 1L;
	private AsyncContext myAsyncContext;
	private AuthorizationResultsBean myAuthorization;
	private HttpRequestBean myHttpRequest;
	private InvocationResultsBean myInvocationRequest;
	private RateLimiter myRateLimiter;
	private IThrottleable myThrottleKey;
	private Date myThrottleStartedException;

	/**
	 * @param theHttpRequest
	 * @param theRateLimiter
	 * @param theInvocationRequest
	 * @param theAuthorization
	 * @param theThrottleKey
	 *            The service catalog object whose throttling is being applied
	 */
	public ThrottleException(HttpRequestBean theHttpRequest, RateLimiter theRateLimiter, InvocationResultsBean theInvocationRequest, AuthorizationResultsBean theAuthorization,
			IThrottleable theThrottleKey) {
		myHttpRequest = theHttpRequest;
		myRateLimiter = theRateLimiter;
		myInvocationRequest = theInvocationRequest;
		myAuthorization = theAuthorization;
		myThrottleKey = theThrottleKey;
		myThrottleStartedException = new Date();
	}

	public long getTimeSinceThrottleStarted() {
		return System.currentTimeMillis() - myThrottleStartedException.getTime();
	}
	
	public AsyncContext getAsyncContext() {
		return myAsyncContext;
	}

	public AuthorizationResultsBean getAuthorization() {
		return myAuthorization;
	}

	public HttpRequestBean getHttpRequest() {
		return myHttpRequest;
	}

	public InvocationResultsBean getInvocationRequest() {
		return myInvocationRequest;
	}

	public RateLimiter getRateLimiter() {
		return myRateLimiter;
	}

	public IThrottleable getThrottleKey() {
		return myThrottleKey;
	}

	public void setAsyncContext(AsyncContext theAsyncContext) {
		myAsyncContext = theAsyncContext;
	}

	public PersUser getUser() {
		return myAuthorization != null ? myAuthorization.getUser() : null;
	}

}
