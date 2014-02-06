package net.svcret.ejb.ejb.monitor;

import javax.ejb.Local;

import net.svcret.admin.api.UnexpectedFailureException;
import net.svcret.ejb.model.entity.BasePersMonitorRule;
import net.svcret.ejb.model.entity.PersMonitorRuleActiveCheck;
import net.svcret.ejb.model.entity.PersMonitorRuleFiring;

@Local
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
