package net.svcret.ejb.ejb;

import static net.svcret.ejb.util.HttpUtil.sendFailure;
import static net.svcret.ejb.util.HttpUtil.sendSecurityFailure;
import static net.svcret.ejb.util.HttpUtil.sendSuccessfulResponse;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;

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
import net.svcret.admin.shared.enm.ThrottlePeriodEnum;
import net.svcret.ejb.api.HttpRequestBean;
import net.svcret.ejb.api.HttpResponseBean;
import net.svcret.ejb.api.IRuntimeStatus;
import net.svcret.ejb.api.ISecurityService.AuthorizationResultsBean;
import net.svcret.ejb.api.IServiceOrchestrator;
import net.svcret.ejb.api.IServiceOrchestrator.OrchestratorResponseBean;
import net.svcret.ejb.api.IThrottlingService;
import net.svcret.ejb.api.InvocationResponseResultsBean;
import net.svcret.ejb.api.InvocationResultsBean;
import net.svcret.ejb.ex.InvocationFailedDueToInternalErrorException;
import net.svcret.ejb.ex.ProcessingException;
import net.svcret.ejb.ex.SecurityFailureException;
import net.svcret.ejb.ex.ThrottleException;
import net.svcret.ejb.model.entity.IThrottleable;
import net.svcret.ejb.model.entity.PersServiceVersionMethod;
import net.svcret.ejb.model.entity.PersUser;

import org.apache.commons.lang3.Validate;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.util.concurrent.RateLimiter;

@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class ThrottlingService implements IThrottlingService {

	private ConcurrentHashMap<PersUser, RateLimiter> myUserRateLimiters = new ConcurrentHashMap<PersUser, RateLimiter>();
	private Map<IThrottleable, ThrottledTaskQueue> myThrottleQueues = new HashMap<IThrottleable, ThrottledTaskQueue>();

	@EJB
	private IThrottlingService myThis;

	@EJB
	private IRuntimeStatus myRuntimeStatusSvc;

	@EJB
	private IServiceOrchestrator myServiceOrchestrator;

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(ThrottlingService.class);

	@Override
	public void applyThrottle(HttpRequestBean theHttpRequest, InvocationResultsBean theInvocationRequest, AuthorizationResultsBean theAuthorization) throws ThrottleException,
			ThrottleQueueFullException {
		if (theAuthorization == null) {
			return;
		}
		PersUser user = theAuthorization.getAuthorizedUser();
		if (user == null) {
			return;
		}

		if (user.getThrottleMaxRequests() == null || user.getThrottlePeriod() == null) {
			return;
		}

		double requestsPerSecond = user.getThrottlePeriod().toRequestsPerSecond(user.getThrottleMaxRequests());
		RateLimiter rateLimiter = myUserRateLimiters.get(user);
		if (rateLimiter == null) {

			ourLog.debug("Creating rate limiter with {} reqs/second", requestsPerSecond);

			RateLimiter newRateLimiter = RateLimiter.create(requestsPerSecond);
			rateLimiter = myUserRateLimiters.putIfAbsent(user, newRateLimiter);
			if (rateLimiter == null) {
				ourLog.info("Creating new RateLimiter for {} with {} reqs/second", user, requestsPerSecond);
				rateLimiter = newRateLimiter;
			}
		}

		if (rateLimiter.getRate() != requestsPerSecond) {
			ourLog.info("Updating RateLimiter for {} with {} reqs/second", user, requestsPerSecond);
			rateLimiter.setRate(requestsPerSecond);
		}

		if (rateLimiter.tryAcquire()) {
			return;
		}

		ourLog.info("Throttling user {} because it has exceeded {} reqs/second", user.getUsername(), requestsPerSecond);

		if (user.getThrottleMaxQueueDepth() == null || user.getThrottleMaxQueueDepth() == 0) {
			recordInvocationForThrottleQueueFull(theHttpRequest, theInvocationRequest, user);
			throw new ThrottleQueueFullException();
		}

		throw new ThrottleException(theHttpRequest, rateLimiter, theInvocationRequest, theAuthorization, user);

	}

	private void recordInvocationForThrottleQueueFull(HttpRequestBean theHttpRequest, InvocationResultsBean theInvocationRequest, PersUser user) {

		switch (theInvocationRequest.getResultType()) {
		case METHOD:
			Date invocationTime = theHttpRequest.getRequestTime();
			int requestLength = theHttpRequest.getRequestBody().length();
			PersServiceVersionMethod method = theInvocationRequest.getMethodDefinition();
			HttpResponseBean httpResponse = null;
			InvocationResponseResultsBean invocationResponseResultsBean = new InvocationResponseResultsBean();
			invocationResponseResultsBean.setResponseType(ResponseTypeEnum.THROTTLE_REJ);
			try {
				myRuntimeStatusSvc.recordInvocationMethod(invocationTime, requestLength, method, user, httpResponse, invocationResponseResultsBean, null);
			} catch (ProcessingException e) {
				// We'll just log this and end it since we're throwing an error anyhow
				// by the time this method is called
				ourLog.error("Failed to log method invocation", e);
			}
			break;
		case STATIC_RESOURCE:
			throw new UnsupportedOperationException();
		}

	}

	@VisibleForTesting
	void setRuntimeStatusSvcForTesting(IRuntimeStatus theRuntimeStatusSvc) {
		myRuntimeStatusSvc = theRuntimeStatusSvc;
	}

	@Override
	public void scheduleThrottledTaskForLaterExecution(ThrottleException theTask) throws ThrottleQueueFullException {
		Validate.notNull(theTask.getAsyncContext());
		Validate.isTrue(theTask.getAsyncContext().getResponse() instanceof HttpServletResponse);

		ourLog.debug("Going to try and schedule task for later execution");

		Integer throttleMaxQueueDepth = theTask.getThrottleKey().getThrottleMaxQueueDepth();
		if (throttleMaxQueueDepth == null || theTask.getThrottleKey().getThrottleMaxQueueDepth() == 0) {
			recordInvocationForThrottleQueueFull(theTask.getHttpRequest(), theTask.getInvocationRequest(), theTask.getAuthorizedUser());
			throw new ThrottleQueueFullException();
		}

		ThrottledTaskQueue taskQueue;
		synchronized (myThrottleQueues) {
			if (!myThrottleQueues.containsKey(theTask.getThrottleKey())) {
				myThrottleQueues.put(theTask.getThrottleKey(), new ThrottledTaskQueue(theTask.getThrottleKey()));
			}
			taskQueue = myThrottleQueues.get(theTask.getThrottleKey());
		}

		try {
			taskQueue.tryToAddTask(theTask, throttleMaxQueueDepth);
		} catch (ThrottleQueueFullException e) {
			recordInvocationForThrottleQueueFull(theTask.getHttpRequest(), theTask.getInvocationRequest(), theTask.getAuthorizedUser());
			throw e;
		}

		myThis.serviceThrottledRequests(taskQueue);

	}

	@VisibleForTesting
	void setThisForTesting(IThrottlingService theThis) {
		myThis = theThis;
	}

	@VisibleForTesting
	void setServiceOrchestratorForTesting(IServiceOrchestrator theServiceOrchestrator) {
		myServiceOrchestrator = theServiceOrchestrator;
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
					} else if (taskToExecute.getRateLimiter().tryAcquire()) {

						AsyncContext asyncContext = taskToExecute.getAsyncContext();
						try {
							HttpServletResponse theResp = (HttpServletResponse) asyncContext.getResponse();
							HttpServletRequest theReq = (HttpServletRequest) asyncContext.getRequest();
							HttpRequestBean request = taskToExecute.getHttpRequest();
							try {

								InvocationResultsBean invocationRequest = taskToExecute.getInvocationRequest();
								AuthorizationResultsBean authorization = taskToExecute.getAuthorization();
								HttpRequestBean httpRequest = taskToExecute.getHttpRequest();
								long throttleTime = taskToExecute.getTimeSinceThrottleStarted();
								OrchestratorResponseBean response = myServiceOrchestrator.handlePreviouslyThrottledRequest(invocationRequest, authorization, httpRequest, throttleTime);

								sendSuccessfulResponse(theResp, response);

								long delay = System.currentTimeMillis() - request.getRequestTime().getTime();
								ourLog.info("Handled throttled {} request at path[{}] with {} byte response in {}ms ({}ms throttle time)",
										new Object[] { request.getRequestType().name(), request.getPath(), response.getResponseBody().length(), delay, throttleTime });

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

	public static class ThrottledTaskQueue {
		private final ArrayDeque<ThrottleException> myTasks = new ArrayDeque<ThrottleException>();
		private final Semaphore myExecutionSemaphore = new Semaphore(1);
		private IThrottleable myKey;

		public ThrottledTaskQueue(IThrottleable theThrottleKey) {
			myKey = theThrottleKey;
		}

		public Semaphore getExecutionSemaphore() {
			return myExecutionSemaphore;
		}

		public synchronized int numTasks() {
			return myTasks.size();
		}

		public synchronized boolean hasTasks() {
			return myTasks.size() > 0;
		}

		public synchronized ThrottleException pollTask() {
			return myTasks.poll();
		}

		public synchronized void pushTask(ThrottleException theTask) {
			myTasks.push(theTask);
		}

		public synchronized void tryToAddTask(ThrottleException theTask, Integer theThrottleMaxQueueDepth) throws ThrottleQueueFullException {
			if (myTasks.size() >= theThrottleMaxQueueDepth) {
				throw new ThrottleQueueFullException();
			}

			myTasks.add(theTask);

			ourLog.info("Throttle queue for {} now has {} / {} tasks in queue", new Object[] { myKey, myTasks.size(), theThrottleMaxQueueDepth });
		}
	}

	public static void main(String[] args) {

		double requestsPerSecond = ThrottlePeriodEnum.MINUTE.toRequestsPerSecond(5);
		System.out.println(requestsPerSecond);

	}

}
