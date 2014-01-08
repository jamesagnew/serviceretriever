package net.svcret.ejb.throttle;

import static net.svcret.ejb.util.HttpUtil.sendFailure;
import static net.svcret.ejb.util.HttpUtil.sendSecurityFailure;
import static net.svcret.ejb.util.HttpUtil.sendSuccessfulResponse;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.svcret.admin.shared.enm.ResponseTypeEnum;
import net.svcret.ejb.api.IRuntimeStatus;
import net.svcret.ejb.api.ISecurityService.AuthorizationResultsBean;
import net.svcret.ejb.api.IServiceOrchestrator;
import net.svcret.ejb.api.SrBeanProcessedResponse;
import net.svcret.ejb.api.SrBeanProcessedRequest;
import net.svcret.ejb.api.SrBeanIncomingRequest;
import net.svcret.ejb.api.SrBeanIncomingResponse;
import net.svcret.ejb.api.SrBeanOutgoingResponse;
import net.svcret.ejb.ex.InvocationFailedDueToInternalErrorException;
import net.svcret.ejb.ex.ProcessingException;
import net.svcret.ejb.ex.SecurityFailureException;
import net.svcret.ejb.ex.UnexpectedFailureException;
import net.svcret.ejb.model.entity.PersMethod;
import net.svcret.ejb.model.entity.PersServiceVersionThrottle;
import net.svcret.ejb.model.entity.PersUser;

import org.apache.commons.lang3.Validate;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.RateLimiter;

@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class ThrottlingService implements IThrottlingService {

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(ThrottlingService.class);
	@EJB
	private IRuntimeStatus myRuntimeStatusSvc;

	@EJB
	private IServiceOrchestrator myServiceOrchestrator;

	@EJB
	private IThrottlingService myThis;

	private Map<LimiterKey, ThrottledTaskQueue> myThrottleQueues = new HashMap<LimiterKey, ThrottledTaskQueue>();
	private ConcurrentHashMap<LimiterKey, FlexibleRateLimiter> myUserRateLimiters = new ConcurrentHashMap<LimiterKey, FlexibleRateLimiter>();

	private void applyThrottle(SrBeanIncomingRequest theHttpRequest, SrBeanProcessedRequest theInvocationRequest, AuthorizationResultsBean theAuthorization, Collection<LimiterKey> theRemainingThrottles) throws ThrottleQueueFullException, ThrottleException {
		
		List<FlexibleRateLimiter> rateLimiters = Lists.newArrayList();
		LimiterKey firstThrottleKey = null;
		
		for (LimiterKey nextKey : theRemainingThrottles) {
			
			double requestsPerSecond = nextKey.getRequestsPerSecond();
			Integer maxQueueDepth = nextKey.getMaxQueuedRequests();
			
			FlexibleRateLimiter rateLimiter = myUserRateLimiters.get(nextKey);
			if (rateLimiter == null) {

				ourLog.debug("Creating rate limiter with {} reqs/second", requestsPerSecond);

				FlexibleRateLimiter newRateLimiter = new FlexibleRateLimiter(requestsPerSecond);
				rateLimiter = myUserRateLimiters.putIfAbsent(nextKey, newRateLimiter);
				if (rateLimiter == null) {
					ourLog.info("Creating new RateLimiter for {} with {} reqs/second", nextKey, requestsPerSecond);
					rateLimiter = newRateLimiter;
				}
			}

			if (rateLimiter.getRate() != requestsPerSecond) {
				ourLog.info("Updating RateLimiter for {} with {} reqs/second", nextKey, requestsPerSecond);
				rateLimiter.setRate(requestsPerSecond);
			}

			if (rateLimiter.tryAcquire()) {
				continue;
			}

			ourLog.info("Throttling {} because it has exceeded {} reqs/second", nextKey, requestsPerSecond);

			if (maxQueueDepth == null || maxQueueDepth == 0) {
				recordInvocationForThrottleQueueFull(theHttpRequest, theInvocationRequest, nextKey.getUser());
				throw new ThrottleQueueFullException();
			}
		
			rateLimiters.add(rateLimiter);
			
			/*
			 * Keep a copy of the first throttle we're applying. Currently the first throttle which fails is the one
			 * whose queue depth is respected. This isn't optimal but it works..
			 */
			if (firstThrottleKey == null) {
				firstThrottleKey = nextKey;
			}
			
		}
	
		if (rateLimiters.isEmpty() == false) {
			throw new ThrottleException(theHttpRequest, rateLimiters, theInvocationRequest, theAuthorization, firstThrottleKey);
		}
		
	}

	@Override
	public void applyThrottle(SrBeanIncomingRequest theHttpRequest, SrBeanProcessedRequest theInvocationRequest, AuthorizationResultsBean theAuthorization) throws ThrottleException, ThrottleQueueFullException {
		PersUser user = theAuthorization != null ? theAuthorization.getAuthorizedUser() : null;
		Set<LimiterKey> throttleKeys = new HashSet<LimiterKey>();
		
		/*
		 * Service specific throttle
		 */
		
		PersServiceVersionThrottle svcThrottle = theInvocationRequest.getMethodDefinition().getServiceVersion().getThrottle();
		if (svcThrottle != null) {
			double requestsPerSecond = svcThrottle.getThrottlePeriod().numRequestsToRequestsPerSecond(svcThrottle.getThrottleMaxRequests());
			Integer maxQueueDepth = svcThrottle.getThrottleMaxQueueDepth();
			PersUser svcThrottleUser = svcThrottle.isApplyPerUser() ? user : null;
			String propCapName = svcThrottle.getApplyPropCapName();
			String propCapValue = propCapName != null ? theInvocationRequest.getPropertyCaptures().get(propCapName) : null;
			LimiterKey key = new LimiterKey(svcThrottleUser, propCapName, propCapValue, requestsPerSecond, maxQueueDepth);
			throttleKeys.add(key);
		}
		
		/*
		 * User specific throttle
		 */
		
		if (user != null) {
			if (user.getThrottleMaxRequests() != null && user.getThrottlePeriod() != null) {
				double requestsPerSecond = user.getThrottlePeriod().numRequestsToRequestsPerSecond(user.getThrottleMaxRequests());
				Integer maxQueueDepth = user.getThrottleMaxQueueDepth();
				LimiterKey key = new LimiterKey(user, null, null, requestsPerSecond, maxQueueDepth);
				throttleKeys.add(key);
			}
		}
		
		applyThrottle(theHttpRequest, theInvocationRequest, theAuthorization, throttleKeys);
	}

	private void recordInvocationForThrottleQueueFull(SrBeanIncomingRequest theHttpRequest, SrBeanProcessedRequest theInvocationRequest, PersUser user) {

		switch (theInvocationRequest.getResultType()) {
		case METHOD:
			Date invocationTime = theHttpRequest.getRequestTime();
			int requestLength = theHttpRequest.getRequestBody().length();
			SrBeanIncomingResponse httpResponse = null;
			SrBeanProcessedResponse invocationResponseResultsBean = new SrBeanProcessedResponse();
			invocationResponseResultsBean.setResponseType(ResponseTypeEnum.THROTTLE_REJ);
			try {
				myRuntimeStatusSvc.recordInvocationMethod(invocationTime, requestLength, theInvocationRequest, user, httpResponse, invocationResponseResultsBean);
			} catch (UnexpectedFailureException e) {
				// We'll just log this and end it since we're throwing an error
				// anyhow
				// by the time this method is called
				ourLog.error("Failed to log method invocation", e);
			} catch (InvocationFailedDueToInternalErrorException e) {
				// We'll just log this and end it since we're throwing an error
				// anyhow
				// by the time this method is called
				ourLog.error("Failed to log method invocation", e);
			}
			break;
		case STATIC_RESOURCE:
			throw new UnsupportedOperationException();
		}

	}

	@Override
	public void scheduleThrottledTaskForLaterExecution(ThrottleException theTask, AsyncContext theAsyncContext) throws ThrottleQueueFullException {
		Validate.notNull(theAsyncContext);
		Validate.isTrue(theAsyncContext.getResponse() instanceof HttpServletResponse);

		theTask.setAsyncContext(theAsyncContext);

		ourLog.debug("Going to try and schedule task for later execution");

		Integer throttleMaxQueueDepth = theTask.getFirstThrottleKey().getMaxQueuedRequests();
		if (throttleMaxQueueDepth == null || theTask.getFirstThrottleKey().getMaxQueuedRequests() == 0) {
			recordInvocationForThrottleQueueFull(theTask.getHttpRequest(), theTask.getInvocationRequest(), theTask.getAuthorizedUser());
			throw new ThrottleQueueFullException();
		}

		ThrottledTaskQueue taskQueue;
		synchronized (myThrottleQueues) {
			if (!myThrottleQueues.containsKey(theTask.getFirstThrottleKey())) {
				myThrottleQueues.put(theTask.getFirstThrottleKey(), new ThrottledTaskQueue());
			}
			taskQueue = myThrottleQueues.get(theTask.getFirstThrottleKey());
		}

		try {
			taskQueue.tryToAddTask(theTask);
		} catch (ThrottleQueueFullException e) {
			recordInvocationForThrottleQueueFull(theTask.getHttpRequest(), theTask.getInvocationRequest(), theTask.getAuthorizedUser());
			throw e;
		}

		myThis.serviceThrottledRequests(taskQueue);

	}

	@Override
	@Asynchronous
	@TransactionAttribute(TransactionAttributeType.NEVER)
	public Future<Void> serviceThrottledRequests(ThrottledTaskQueue theTaskQueue) {

		for (;;) {

			if (theTaskQueue.getExecutionSemaphore().tryAcquire()) {
				try {
					ThrottleException taskToExecute = theTaskQueue.pollTask();

					if (taskToExecute == null) {
						
						// no task
						
					} else if (taskToExecute.tryToAquireAllRateLimitersAndRemoveAnyWhichAreAquired()) {

						AsyncContext asyncContext = taskToExecute.getAsyncContext();
						try {
							HttpServletResponse theResp = (HttpServletResponse) asyncContext.getResponse();
							HttpServletRequest theReq = (HttpServletRequest) asyncContext.getRequest();
							SrBeanIncomingRequest request = taskToExecute.getHttpRequest();
							try {

								SrBeanProcessedRequest invocationRequest = taskToExecute.getInvocationRequest();
								AuthorizationResultsBean authorization = taskToExecute.getAuthorization();
								SrBeanIncomingRequest httpRequest = taskToExecute.getHttpRequest();
								
								long throttleTime = taskToExecute.getTimeSinceThrottleStarted();
								invocationRequest.setThrottleTimeIfAny(throttleTime);
								
								SrBeanOutgoingResponse response = myServiceOrchestrator.handlePreviouslyThrottledRequest(invocationRequest, authorization, httpRequest);

								sendSuccessfulResponse(theResp, response);

								long delay = System.currentTimeMillis() - request.getRequestTime().getTime();
								ourLog.info("Handled throttled {} request at path[{}] with {} byte response in {}ms ({}ms throttle time)", new Object[] { request.getRequestType().name(), request.getPath(), response.getResponseBody().length(), delay, throttleTime });

							} catch (ProcessingException e) {
								ourLog.info("Processing Failure", e);
								sendFailure(theResp, e.getMessage());
							} catch (SecurityFailureException e) {
								ourLog.info("Security Failure accessing URL: {}", theReq.getRequestURL());
								sendSecurityFailure(theResp);
							} catch (InvocationFailedDueToInternalErrorException e) {
								ourLog.info("Processing Failure", e);
								sendFailure(theResp, e.getMessage());
							}
						} catch (IOException e) {
							ourLog.error("Failed to respond to throttled request due to IOException: ", e);
						} finally {
							asyncContext.complete();
						}
						
					} else {
						
						/*
						 * Didn't get permission to proceed from the ratelimiter, so try again
						 */
						theTaskQueue.pushTask(taskToExecute);
						
					}

				} finally {
					theTaskQueue.getExecutionSemaphore().release();
				}
			}

			int numTasks = theTaskQueue.numTasks();
			if (numTasks > 0) {
				ourLog.debug("Task queue has {} tasks in it, going to sleep", numTasks);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// ignore
				}
			} else {
				ourLog.debug("Task queue is empty, quitting worker");
				break;
			}

		}

		return new AsyncResult<Void>(null);
	}

	@VisibleForTesting
	void setRuntimeStatusSvcForTesting(IRuntimeStatus theRuntimeStatusSvc) {
		myRuntimeStatusSvc = theRuntimeStatusSvc;
	}

	@VisibleForTesting
	void setServiceOrchestratorForTesting(IServiceOrchestrator theServiceOrchestrator) {
		myServiceOrchestrator = theServiceOrchestrator;
	}

	@VisibleForTesting
	void setThisForTesting(IThrottlingService theThis) {
		myThis = theThis;
	}

}
