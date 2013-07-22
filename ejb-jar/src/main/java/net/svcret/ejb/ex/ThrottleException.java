package net.svcret.ejb.ex;

import net.svcret.ejb.api.ISecurityService.AuthorizationResultsBean;
import net.svcret.ejb.api.InvocationResultsBean;
import net.svcret.ejb.model.entity.IThrottleable;

import com.google.common.util.concurrent.RateLimiter;

public class ThrottleException extends Exception {

	private static final long serialVersionUID = 1L;
	private AuthorizationResultsBean myAuthorization;
	private InvocationResultsBean myInvocationRequest;
	private RateLimiter myRateLimiter;
	private long myRequestStartTime;
	private IThrottleable myThrottleKey;

	/**
	 * @param theRequestStartTime
	 * @param theRateLimiter
	 * @param theInvocationRequest
	 * @param theAuthorization
	 * @param theThrottleKey The service catalog object whose throttling is being applied
	 */
	public ThrottleException(long theRequestStartTime, RateLimiter theRateLimiter, InvocationResultsBean theInvocationRequest, AuthorizationResultsBean theAuthorization, IThrottleable theThrottleKey) {
		myRequestStartTime = theRequestStartTime;
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

	public long getRequestStartTime() {
		return myRequestStartTime;
	}

}
