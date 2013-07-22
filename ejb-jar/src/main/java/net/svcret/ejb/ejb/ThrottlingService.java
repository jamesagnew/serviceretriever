package net.svcret.ejb.ejb;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.ejb.Singleton;

import net.svcret.ejb.api.ISecurityService.AuthorizationResultsBean;
import net.svcret.ejb.api.IThrottlingService;
import net.svcret.ejb.api.InvocationResultsBean;
import net.svcret.ejb.ex.ThrottleException;
import net.svcret.ejb.model.entity.IThrottleable;
import net.svcret.ejb.model.entity.PersUser;

import com.google.common.util.concurrent.RateLimiter;

@Singleton
public class ThrottlingService implements IThrottlingService {

	private ConcurrentHashMap<PersUser, RateLimiter> myUserRateLimiters = new ConcurrentHashMap<PersUser, RateLimiter>();
	private Map<IThrottleable, ArrayDeque<ThrottleException>> myThrottleQueues = new HashMap<IThrottleable, ArrayDeque<ThrottleException>>();
	
	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(ThrottlingService.class);
	
	@Override
	public void applyThrottle(long theRequestStartTime, InvocationResultsBean theInvocationRequest, AuthorizationResultsBean theAuthorization) throws ThrottleException {
		PersUser user = theAuthorization.getUser();
		if (user == null) {
			return;
		}

		if (user.getThrottleMaxRequests() == null || user.getThrottlePeriod() == null) {
			return;
		}

		double requestsPerSecond = user.getThrottlePeriod().toRequestsPerSecond(user.getThrottleMaxRequests());
		RateLimiter rateLimiter = myUserRateLimiters.get(user);
		if (rateLimiter == null) {
			RateLimiter newRateLimiter = RateLimiter.create(requestsPerSecond);
			rateLimiter = myUserRateLimiters.putIfAbsent(user, newRateLimiter);
			if (rateLimiter == null) {
				rateLimiter = newRateLimiter;
			}
		}
		
		if (rateLimiter.getRate() != requestsPerSecond) {
			rateLimiter.setRate(requestsPerSecond);
		}
		
		if (rateLimiter.tryAcquire()) {
			return;
		}
		
		ourLog.info("Throttling user {} because it has exceeded {} reqs/second", user.getUsername(), requestsPerSecond);
		
		throw new ThrottleException(theRequestStartTime, rateLimiter, theInvocationRequest, theAuthorization, user);
	}


	
	
}
