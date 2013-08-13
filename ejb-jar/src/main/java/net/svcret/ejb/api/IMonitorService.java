package net.svcret.ejb.api;

import java.util.concurrent.Future;

import javax.ejb.Local;

import net.svcret.ejb.ex.ProcessingException;
import net.svcret.ejb.model.entity.BasePersMonitorRule;
import net.svcret.ejb.model.entity.PersMonitorRuleActiveCheck;

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
	void saveRule(BasePersMonitorRule theRule) throws ProcessingException;

	void runActiveChecks();

	Future<Void> runActiveCheck(PersMonitorRuleActiveCheck theCheck);
	
}
