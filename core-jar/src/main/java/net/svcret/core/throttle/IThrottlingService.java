package net.svcret.core.throttle;

import java.util.concurrent.Future;

import javax.servlet.AsyncContext;

import net.svcret.core.api.SrBeanIncomingRequest;
import net.svcret.core.api.SrBeanProcessedRequest;
import net.svcret.core.api.ISecurityService.AuthorizationResultsBean;

public interface IThrottlingService {

	void scheduleThrottledTaskForLaterExecution(ThrottleException theTask, AsyncContext theAsyncContext) throws ThrottleQueueFullException;

	Future<Void> serviceThrottledRequests(ThrottledTaskQueue theTaskQueue);

	void applyThrottle(SrBeanIncomingRequest theHttpRequest, SrBeanProcessedRequest theInvocationRequest, AuthorizationResultsBean theAuthorization) throws ThrottleException, ThrottleQueueFullException;

}
