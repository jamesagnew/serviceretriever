package net.svcret.core.ejb.monitor;

import net.svcret.admin.api.UnexpectedFailureException;
import net.svcret.core.model.entity.BasePersMonitorRule;
import net.svcret.core.model.entity.PersMonitorRuleActiveCheck;
import net.svcret.core.model.entity.PersMonitorRuleFiring;

public interface IMonitorService {

	/**
	 * Do a pass of the monitor, and fire any events that need to be fired. This method should be called once per minute.
	 */
	void runPassiveChecks();

	PersMonitorRuleFiring runActiveCheck(PersMonitorRuleActiveCheck theCheck, boolean thePersistResults) throws UnexpectedFailureException;

	void runActiveChecks();

	/**
	 * Save a rule and notify anyone interested
	 */
	BasePersMonitorRule saveRule(BasePersMonitorRule theRule) throws UnexpectedFailureException;

}
