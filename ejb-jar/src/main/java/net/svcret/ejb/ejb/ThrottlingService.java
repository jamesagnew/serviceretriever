package net.svcret.ejb.ejb;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;

import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Singleton;

import net.svcret.ejb.api.ISecurityService.AuthorizationResultsBean;
import net.svcret.ejb.api.IServiceOrchestrator;
import net.svcret.ejb.api.IServiceOrchestrator.OrchestratorResponseBean;
import net.svcret.ejb.api.IThrottlingService;
import net.svcret.ejb.api.InvocationResultsBean;
import net.svcret.ejb.ex.ThrottleException;
import net.svcret.ejb.model.entity.IThrottleable;
import net.svcret.ejb.model.entity.PersUser;

import com.google.common.util.concurrent.RateLimiter;

@Singleton
public class ThrottlingService implements IThrottlingService {

	private ConcurrentHashMap<PersUser, RateLimiter> myUserRateLimiters = new ConcurrentHashMap<PersUser, RateLimiter>();
	private Map<IThrottleable, ThrottledTaskQueue> myThrottleQueues = new HashMap<IThrottleable, ThrottledTaskQueue>();

	@EJB
	private ThrottlingService myThis;

	@EJB
	private IServiceOrchestrator myServiceOrchestrator;

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(ThrottlingService.class);

	@Override
	public void applyThrottle(long theRequestStartTime, InvocationResultsBean theInvocationRequest, AuthorizationResultsBean theAuthorization) throws ThrottleException, ThrottleQueueFullException {
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

		if (user.getThrottleMaxQueueDepth() == null || user.getThrottleMaxQueueDepth() == 0) {
			throw new ThrottleQueueFullException();
		}

		throw new ThrottleException(theRequestStartTime, rateLimiter, theInvocationRequest, theAuthorization, user);

	}

	@Override
	public void scheduleThrottledTaskForLaterExecution(ThrottleException theTask) throws ThrottleQueueFullException {

		Integer throttleMaxQueueDepth = theTask.getThrottleKey().getThrottleMaxQueueDepth();
		if (throttleMaxQueueDepth == null || theTask.getThrottleKey().getThrottleMaxQueueDepth() == 0) {
			throw new ThrottleQueueFullException();
		}

		ThrottledTaskQueue taskQueue;
		synchronized (myThrottleQueues) {
			if (!myThrottleQueues.containsKey(theTask.getThrottleKey())) {
				myThrottleQueues.put(theTask.getThrottleKey(), new ThrottledTaskQueue());
			}
			taskQueue = myThrottleQueues.get(theTask.getThrottleKey());
		}

		taskQueue.tryToAddTask(theTask, throttleMaxQueueDepth);

		serviceThrottledRequests(taskQueue);

	}

	@Override
	@Asynchronous
	public Future<Void> serviceThrottledRequests(ThrottledTaskQueue theTaskQueue) {
		
		if (theTaskQueue.getExecutionSemaphore().tryAcquire()) {
			try {
				ThrottleException taskToExecute = theTaskQueue.getTasks().poll();
				if (taskToExecute.getRateLimiter().tryAcquire()) {
					OrchestratorResponseBean response = myServiceOrchestrator.handlePreviouslyThrottledRequest(taskToExecute.getRequestStartTime(), taskToExecute.getInvocationRequest(), taskToExecute.getAuthorization());
				} else {
					theTaskQueue.getTasks().push(taskToExecute);
				}
				
			}finally {
				theTaskQueue.getExecutionSemaphore().release();
			}
		}
		
		
		return new AsyncResult<Void>(null);
	}

	public static class ThrottledTaskQueue {
		private final ArrayDeque<ThrottleException> myTasks = new ArrayDeque<ThrottleException>();
		private final Semaphore myExecutionSemaphore = new Semaphore(1);

		public Semaphore getExecutionSemaphore() {
			return myExecutionSemaphore;
		}

		public ArrayDeque<ThrottleException> getTasks() {
			return myTasks;
		}


		public synchronized void tryToAddTask(ThrottleException theTask, Integer theThrottleMaxQueueDepth) throws ThrottleQueueFullException {
			if (getTasks().size() > theThrottleMaxQueueDepth) {
				throw new ThrottleQueueFullException();
			}

			getTasks().add(theTask);

		}
	}

}
