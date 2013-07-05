package net.svcret.ejb.api;

import javax.ejb.Local;

@Local
public interface IScheduler {

	void reloadUserRegistry();
	
	void flushStats();

	void collapseStats();

	void flushTransactionLogs();

	void monitorCheck();
 
	
}
