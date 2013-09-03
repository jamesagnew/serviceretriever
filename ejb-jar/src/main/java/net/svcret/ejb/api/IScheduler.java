package net.svcret.ejb.api;

import javax.ejb.Local;

@Local
public interface IScheduler {

	void reloadUserRegistry();

	void collapseStats();

	void monitorRunPassiveChecks();

	void flushInMemoryStatisticsAndTransactionsSecondary();

	void flushInMemoryStatisticsAndTransactionsPrimary();

	void monitorActiveChecks();

	void flushInMemoryStatistics();

	void flushInMemoryStatisticsUnlessItHasHappenedVeryRecently();

	void recordNodeStats();
	
}
