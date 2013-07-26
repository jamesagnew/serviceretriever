package net.svcret.ejb.api;

import java.util.concurrent.Future;

import net.svcret.ejb.api.ISecurityService.AuthorizationResultsBean;
import net.svcret.ejb.ejb.ThrottleQueueFullException;
import net.svcret.ejb.ejb.ThrottlingService.ThrottledTaskQueue;
import net.svcret.ejb.ex.ThrottleException;

public interface IThrottlingService {

	void scheduleThrottledTaskForLaterExecution(ThrottleException theTask) throws ThrottleQueueFullException;

	Future<Void> serviceThrottledRequests(ThrottledTaskQueue theTaskQueue);

	void applyThrottle(HttpRequestBean theHttpRequest, InvocationResultsBean theInvocationRequest, AuthorizationResultsBean theAuthorization) throws ThrottleException, ThrottleQueueFullException;

}