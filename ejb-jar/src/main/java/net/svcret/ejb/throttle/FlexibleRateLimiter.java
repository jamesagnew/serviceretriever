package net.svcret.ejb.throttle;

import java.util.concurrent.atomic.AtomicLong;

import com.google.common.util.concurrent.RateLimiter;

/**
 * RateLimiter which wraps the Google Guava RateLimiter, but which uses its own
 * internal implementation for request rates which should allow less than one
 * permit per second. This is useful because the Guava RateLimiter resets after
 * each second, making it unable to issue tickets at a rate lower than that
 * (e.g. 2 tickets per minute)
 */
public class FlexibleRateLimiter {

	private long myPeriodInMillis;
	private AtomicLong myNextRequest;
	private RateLimiter myWrappedRateLimiter;
	private double myRate;

	public FlexibleRateLimiter(double theRequestsPerSecond) {
		setRate(theRequestsPerSecond);
	}

	public final void setRate(double theRequestsPerSecond) {
		myRate = theRequestsPerSecond;

		if (theRequestsPerSecond > 1.0) {
			myWrappedRateLimiter = RateLimiter.create(theRequestsPerSecond);
		} else {
			myWrappedRateLimiter = null;
		}

		myPeriodInMillis = (long) ((1.0 / theRequestsPerSecond) * 1000L);
		myNextRequest = new AtomicLong(System.currentTimeMillis());
	}

	public boolean tryAcquire() {
		if (myWrappedRateLimiter != null) {
			return myWrappedRateLimiter.tryAcquire();
		}

		long now = System.currentTimeMillis();
		long currentValue = myNextRequest.get();
		if (now < currentValue) {
			return false;
		}

		while (now == currentValue) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				// ignore
			}
			now = System.currentTimeMillis();
		}

		long nextAllowed = now + myPeriodInMillis;
		return myNextRequest.compareAndSet(currentValue, nextAllowed);

	}

	public double getRate() {
		return myRate;
	}

}
