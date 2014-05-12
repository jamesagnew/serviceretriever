package net.svcret.core.jmx;

public interface RuntimeStatusMonitorMBean {

	int getCachedPopulatedStatCount();

	int getCachedNullStatCount();
	
	int getMaxCachedPopulatedStatCount();

	int getMaxCachedNullStatCount();

	void setMaxCachedPopulatedStatCount(int theCount);

	void setMaxCachedNullStatCount(int theCount);

	void purgeCachedStats();
}
