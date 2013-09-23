package net.svcret.ejb.runtimestatus;

import static org.junit.Assert.*;

import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;
import org.junit.Test;

public class RecentTransactionQueueTest {

	@Test
	public void testLotsOfRecent() {
		Date now = new Date();
		
		RecentTransactionQueue q = new RecentTransactionQueue();
		assertEquals(0.0, q.getTransactionsPerMinute(), 0.0);

		q.addDate(new Date(now.getTime() - (3 * DateUtils.MILLIS_PER_MINUTE)));
		q.addDate(new Date(now.getTime() - (2 * DateUtils.MILLIS_PER_MINUTE)));
		q.addDate(new Date(now.getTime() - (1 * DateUtils.MILLIS_PER_MINUTE)));
		q.addDate(new Date(now.getTime() - (0 * DateUtils.MILLIS_PER_MINUTE)));
		
		assertEquals(2.0, q.getTransactionsPerMinute(), 1.0);

		q.addDate(new Date(now.getTime() - (0 * DateUtils.MILLIS_PER_MINUTE)));
		q.addDate(new Date(now.getTime() - (0 * DateUtils.MILLIS_PER_MINUTE)));
		
		assertEquals(4.0, q.getTransactionsPerMinute(), 1.0);

	}
	
	@Test
	public void testIt() {
		Date now = new Date();
		
		RecentTransactionQueue q = new RecentTransactionQueue();
		assertEquals(0.0, q.getTransactionsPerMinute(), 0.0);

		q.addDate(now);
		assertEquals(0.0, q.getTransactionsPerMinute(), 0.0);
		q.addDate(now);
		assertEquals(2.0, q.getTransactionsPerMinute(), 0.0);

		for (int i = 0; i < 498; i++) {
			q.addDate(new Date(now.getTime() + (30 * DateUtils.MILLIS_PER_SECOND)));
		}
		assertEquals(500.0, q.getTransactionsPerMinute(), 0.0);

		for (int i = 0; i < 500; i++) {
			q.addDate(new Date(now.getTime() + (58 * DateUtils.MILLIS_PER_SECOND)));
		}
		assertEquals(1000.0, q.getTransactionsPerMinute(), 50.0);

		for (int i = 0; i < 500; i++) {
			q.addDate(new Date(now.getTime() + (60 * DateUtils.MILLIS_PER_SECOND)));
		}
		assertEquals(1500.0, q.getTransactionsPerMinute(), 20.0);

		for (int i = 0; i < RecentTransactionQueue.MAX_SIZE - 2; i++) {
			q.addDate(new Date(now.getTime() + (61 * DateUtils.MILLIS_PER_SECOND)));
		}
		assertEquals(RecentTransactionQueue.MAX_SIZE * 60.0, q.getTransactionsPerMinute(), 20.0);

		// Now everything will be the same time exactly
		for (int i = 0; i < 10; i++) {
			q.addDate(new Date(now.getTime() + (61 * DateUtils.MILLIS_PER_SECOND)));
		}
		assertEquals(RecentTransactionQueue.MAX_SIZE * 60.0, q.getTransactionsPerMinute(), 20.0);

	}
	
}
