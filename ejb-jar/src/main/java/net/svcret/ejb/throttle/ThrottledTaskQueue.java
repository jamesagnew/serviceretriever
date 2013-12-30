package net.svcret.ejb.throttle;

import java.util.ArrayDeque;
import java.util.concurrent.Semaphore;

public class ThrottledTaskQueue {
	private final Semaphore myExecutionSemaphore = new Semaphore(1);
	private final ArrayDeque<ThrottleException> myTasks = new ArrayDeque<ThrottleException>();
	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(ThrottledTaskQueue.class);
	
	public ThrottledTaskQueue() {
	}

	public Semaphore getExecutionSemaphore() {
		return myExecutionSemaphore;
	}

	public synchronized boolean hasTasks() {
		return myTasks.size() > 0;
	}

	public synchronized int numTasks() {
		return myTasks.size();
	}

	public synchronized ThrottleException pollTask() {
		return myTasks.poll();
	}

	public synchronized void pushTask(ThrottleException theTask) {
		myTasks.push(theTask);
	}

	public synchronized void tryToAddTask(ThrottleException theTask) throws ThrottleQueueFullException {
		if (myTasks.size() >= theTask.getFirstThrottleKey().getMaxQueuedRequests()) {
			throw new ThrottleQueueFullException();
		}

		myTasks.add(theTask);

		ourLog.info("Throttle queue for {} now has {} / {} tasks in queue", new Object[] { theTask.getFirstThrottleKey(), myTasks.size(), theTask.getFirstThrottleKey().getMaxQueuedRequests() });
	}
}