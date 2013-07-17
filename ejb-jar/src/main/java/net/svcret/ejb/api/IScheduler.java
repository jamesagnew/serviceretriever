package net.svcret.ejb.api;

import javax.ejb.Local;

@Local
public interface IScheduler {

	void reloadUserRegistry();

	void collapseStats();

	void monitorCheck();

	void flushInMemoryStatisticsAndTransactionsSecondary();

	void flushInMemoryStatisticsAndTransactionsPrimary();

}
