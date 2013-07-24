package net.svcret.ejb.ex;

import java.util.Date;

import net.svcret.ejb.api.HttpRequestBean;
import net.svcret.ejb.api.ISecurityService.AuthorizationResultsBean;
import net.svcret.ejb.api.InvocationResultsBean;
import net.svcret.ejb.model.entity.IThrottleable;

import com.google.common.util.concurrent.RateLimiter;

public class ThrottleException extends Exception {

	private static final long serialVersionUID = 1L;
	private AuthorizationResultsBean myAuthorization;
	private InvocationResultsBean myInvocationRequest;
	private RateLimiter myRateLimiter;
	private HttpRequestBean myHttpRequest;
	private IThrottleable myThrottleKey;

	/**
	 * @param theHttpRequest
	 * @param theRateLimiter
	 * @param theInvocationRequest
	 * @param theAuthorization
	 * @param theThrottleKey The service catalog object whose throttling is being applied
	 */
	public ThrottleException(HttpRequestBean theHttpRequest, RateLimiter theRateLimiter, InvocationResultsBean theInvocationRequest, AuthorizationResultsBean theAuthorization, IThrottleable theThrottleKey) {
		myHttpRequest = theHttpRequest;
		myRateLimiter = theRateLimiter;
		myInvocationRequest = theInvocationRequest;
		myAuthorization = theAuthorization;
		myThrottleKey = theThrottleKey;
	}

	public IThrottleable getThrottleKey() {
		return myThrottleKey;
	}

	public AuthorizationResultsBean getAuthorization() {
		return myAuthorization;
	}

	public InvocationResultsBean getInvocationRequest() {
		return myInvocationRequest;
	}

	public RateLimiter getRateLimiter() {
		return myRateLimiter;
	}

	public HttpRequestBean getHttpRequest() {
		return myHttpRequest;
	}

}
