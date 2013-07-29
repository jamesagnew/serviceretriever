package net.svcret.ejb.api;

import javax.ejb.Local;

import net.svcret.ejb.ex.ProcessingException;
import net.svcret.ejb.model.entity.PersMonitorRule;

@Local
public interface IMonitorService {

	/**
	 * Do a pass of the monitor, and fire any events that need to be fired. This method
	 * should be called once per minute.
	 */
	void check();

	/**
	 * Save a rule and notify anyone interested
	 */
	void saveRule(PersMonitorRule theRule) throws ProcessingException;
	
}
