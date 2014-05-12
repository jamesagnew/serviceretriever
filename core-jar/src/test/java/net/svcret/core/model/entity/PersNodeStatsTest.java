package net.svcret.core.model.entity;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import net.svcret.core.model.entity.PersNodeStats;

import org.junit.Test;

public class PersNodeStatsTest {

	@Test
	public void testCalculateCpuTime() {
		
		PersNodeStats tst = new PersNodeStats();
		tst.collectMemoryStats();
		assertThat(tst.getMemoryCommitted(), greaterThan(0L));
		assertThat(tst.getMemoryMax(), greaterThan(0L));
		assertThat(tst.getMemoryUsed(), greaterThan(0L));
		
		ourLog.info("Memory          : {}", tst.getMemoryUsed());
		ourLog.info("Memory Committed: {}", tst.getMemoryCommitted());
		ourLog.info("Memory Max      : {}", tst.getMemoryMax());

		// Waste time
		String a = "a";
		for (int i = 0; i < 10000; i++) {
			a = a + i;
		}
		ourLog.info("String is {} bytes", a.length());
		
		tst = new PersNodeStats();
		tst.collectMemoryStats();
		assertThat(tst.getMemoryCommitted(), greaterThan(0L));
		assertThat(tst.getCpuTime(), greaterThan(0.0));
		assertThat(tst.getCpuTime(), lessThan(1.0));

		ourLog.info("Memory          : {}", tst.getMemoryUsed());
		ourLog.info("Memory Committed: {}", tst.getMemoryCommitted());
		ourLog.info("Memory Max      : {}", tst.getMemoryMax());
		ourLog.info("CPU Time        : {}", tst.getCpuTime());
		
		
	}
	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(PersNodeStatsTest.class);
}
