package net.svcret.ejb.api;

import javax.ejb.Local;

@Local
public interface IMonitorService {

	/**
	 * Do a pass of the monitor, and fire any events that need to be fired. This method
	 * should be called once per minute.
	 */
	void check();
	
}
