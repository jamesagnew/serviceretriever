package net.svcret.ejb.throttle;

import java.util.concurrent.Future;

import javax.servlet.AsyncContext;

import net.svcret.ejb.api.ISecurityService.AuthorizationResultsBean;
import net.svcret.ejb.api.SrBeanProcessedRequest;
import net.svcret.ejb.api.SrBeanIncomingRequest;

public interface IThrottlingService {

	void scheduleThrottledTaskForLaterExecution(ThrottleException theTask, AsyncContext theAsyncContext) throws ThrottleQueueFullException;

	Future<Void> serviceThrottledRequests(ThrottledTaskQueue theTaskQueue);

	void applyThrottle(SrBeanIncomingRequest theHttpRequest, SrBeanProcessedRequest theInvocationRequest, AuthorizationResultsBean theAuthorization) throws ThrottleException, ThrottleQueueFullException;

}
