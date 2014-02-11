package net.svcret.core.throttle;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.servlet.AsyncContext;

import net.svcret.core.api.SrBeanIncomingRequest;
import net.svcret.core.api.SrBeanProcessedRequest;
import net.svcret.core.api.ISecurityService.AuthorizationResultsBean;
import net.svcret.core.model.entity.PersUser;

/**
 * Note that this exception is public since it is passed to the Orchestration
 * service, but all of its internals are default protected since they are
 * specific to the throttling service that can throw this exception.
 */
public class ThrottleException extends Exception {

	private static final long serialVersionUID = 1L;
	private AsyncContext myAsyncContext;
	private AuthorizationResultsBean myAuthorization;
	private SrBeanIncomingRequest myHttpRequest;
	private SrBeanProcessedRequest myInvocationRequest;
	private List<FlexibleRateLimiter> myRateLimiters;
	private Date myThrottleStartedException;
	private LimiterKey myThrottleKey;

	/**
	 * @param theHttpRequest
	 * @param theRateLimiters
	 * @param theInvocationRequest
	 * @param theAuthorization
	 * @param theFirstThrottleKey 
	 * @param theThrottleKey
	 *            The service catalog object whose throttling is being applied
	 */
	ThrottleException(SrBeanIncomingRequest theHttpRequest, List<FlexibleRateLimiter> theRateLimiters, SrBeanProcessedRequest theInvocationRequest, AuthorizationResultsBean theAuthorization, LimiterKey theFirstThrottleKey) {
		myHttpRequest = theHttpRequest;
		myRateLimiters = new ArrayList<FlexibleRateLimiter>(theRateLimiters);
		myInvocationRequest = theInvocationRequest;
		myAuthorization = theAuthorization;
		myThrottleStartedException = new Date();
		myThrottleKey = theFirstThrottleKey;
	}

	LimiterKey getFirstThrottleKey() {
		return myThrottleKey;
	}
	
	long getTimeSinceThrottleStarted() {
		return System.currentTimeMillis() - myThrottleStartedException.getTime();
	}

	AsyncContext getAsyncContext() {
		return myAsyncContext;
	}

	AuthorizationResultsBean getAuthorization() {
		return myAuthorization;
	}

	SrBeanIncomingRequest getHttpRequest() {
		return myHttpRequest;
	}

	SrBeanProcessedRequest getInvocationRequest() {
		return myInvocationRequest;
	}

	List<FlexibleRateLimiter> getRateLimiters() {
		return myRateLimiters;
	}

	public void setAsyncContext(AsyncContext theAsyncContext) {
		myAsyncContext = theAsyncContext;
	}

	PersUser getAuthorizedUser() {
		return myAuthorization != null ? myAuthorization.getAuthorizedUser() : null;
	}

	public boolean tryToAquireAllRateLimitersAndRemoveAnyWhichAreAquired() {
		for (Iterator<FlexibleRateLimiter> iterator = myRateLimiters.iterator(); iterator.hasNext();) {
			FlexibleRateLimiter next = iterator.next();
			if (!next.tryAcquire()) {
				return false;
			}
			iterator.remove();
		}
		return true;
	}

}
