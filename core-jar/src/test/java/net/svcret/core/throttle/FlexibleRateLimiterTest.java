package net.svcret.core.throttle;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class FlexibleRateLimiterTest {

	@Test
	public void testRateBelowOnePerSecond() {
		
		FlexibleRateLimiter rl = new FlexibleRateLimiter(5.0 / 60.00); // 5 per minute 
		assertTrue(rl.tryAcquire());
		assertTrue(rl.tryAcquire());
		assertTrue(rl.tryAcquire());
		assertTrue(rl.tryAcquire());
		assertTrue(rl.tryAcquire());
		
		// After 5, we should fail (and this should only be passable again after ~60 seconds)
		assertFalse(rl.tryAcquire());
		
	}
	
}
