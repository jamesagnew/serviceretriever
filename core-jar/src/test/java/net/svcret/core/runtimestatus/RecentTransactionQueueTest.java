package net.svcret.core.runtimestatus;

import static org.junit.Assert.*;

import java.util.Date;

import net.svcret.core.status.RecentTransactionQueue;

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
	public void testOnlyLongAgo() {
		Date now = new Date();
		
		RecentTransactionQueue q = new RecentTransactionQueue();
		assertEquals(0.0, q.getTransactionsPerMinute(), 0.0);

		q.addDate(new Date(now.getTime() - (200 + (62 * DateUtils.MILLIS_PER_HOUR))));
		q.addDate(new Date(now.getTime() - (150 + (62 * DateUtils.MILLIS_PER_HOUR))));
		q.addDate(new Date(now.getTime() - (150 + (62 * DateUtils.MILLIS_PER_HOUR))));
		
		assertEquals(0.0, q.getTransactionsPerMinute(), 0.0);

	}

	@Test
	public void testThreeMinsAgo() {
		Date now = new Date();
		
		RecentTransactionQueue q = new RecentTransactionQueue();
		q.addDate(new Date(now.getTime() - (3 * DateUtils.MILLIS_PER_MINUTE) + 100));
		q.addDate(new Date(now.getTime() - (3 * DateUtils.MILLIS_PER_MINUTE) + 100));
		q.addDate(new Date(now.getTime() - (3 * DateUtils.MILLIS_PER_MINUTE) + 100));
		/* 
		 * This is 0.6 because with older entries in the queue we rank this
		 * out of 5 minutes (3/5=0.6)
		 */
		assertEquals(0.6, q.getTransactionsPerMinute(), 0.01);

		// And with some old ones in the queue as well
		q = new RecentTransactionQueue();
		q.addDate(new Date(now.getTime() - (6 * DateUtils.MILLIS_PER_MINUTE)+100));
		q.addDate(new Date(now.getTime() - (6 * DateUtils.MILLIS_PER_MINUTE)+100));
		q.addDate(new Date(now.getTime() - (3 * DateUtils.MILLIS_PER_MINUTE) + 100));
		q.addDate(new Date(now.getTime() - (3 * DateUtils.MILLIS_PER_MINUTE) + 100));
		q.addDate(new Date(now.getTime() - (3 * DateUtils.MILLIS_PER_MINUTE) + 100));
		assertEquals(0.6, q.getTransactionsPerMinute(), 0.01);

		// And with some old ones in the queue as well
		q = new RecentTransactionQueue();
		q.addDate(new Date(now.getTime() - (36 * DateUtils.MILLIS_PER_HOUR)));
		q.addDate(new Date(now.getTime() - (36 * DateUtils.MILLIS_PER_HOUR)));
		q.addDate(new Date(now.getTime() - (3 * DateUtils.MILLIS_PER_MINUTE) + 100));
		q.addDate(new Date(now.getTime() - (3 * DateUtils.MILLIS_PER_MINUTE) + 100));
		q.addDate(new Date(now.getTime() - (3 * DateUtils.MILLIS_PER_MINUTE) + 100));
		assertEquals(0.6, q.getTransactionsPerMinute(), 0.01);

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
