package net.svcret.ejb.api;

import javax.ejb.Local;

@Local
public interface IScheduler {

	void reloadUserRegistry();

	void collapseStats();

	void monitorRunPassiveChecks();

	void monitorActiveChecks();

	void flushInMemoryStatisticsUnlessItHasHappenedVeryRecently();

	void flushInMemoryRecentMessagesUnlessItHasHappenedVeryRecently();

	void recordNodeStats();

	void flushFilesystemAuditEvents();
	
}
