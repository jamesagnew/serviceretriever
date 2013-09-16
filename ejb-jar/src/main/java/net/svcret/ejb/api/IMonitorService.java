package net.svcret.ejb.api;

import java.util.List;
import java.util.concurrent.Future;

import javax.ejb.Local;

import net.svcret.ejb.ex.UnexpectedFailureException;
import net.svcret.ejb.model.entity.BasePersMonitorRule;
import net.svcret.ejb.model.entity.PersMonitorRuleActiveCheck;
import net.svcret.ejb.model.entity.PersMonitorRuleActiveCheckOutcome;

@Local
public interface IMonitorService {

	/**
	 * Do a pass of the monitor, and fire any events that need to be fired. This method should be called once per minute.
	 */
	void check();

	Future<Void> runActiveCheck(PersMonitorRuleActiveCheck theCheck);

	List<PersMonitorRuleActiveCheckOutcome> runActiveCheckInCurrentTransaction(PersMonitorRuleActiveCheck theCheck, boolean thePersistResults);

	Future<Void> runActiveCheckInNewTransaction(PersMonitorRuleActiveCheck theCheck);

	void runActiveChecks();

	/**
	 * Save a rule and notify anyone interested
	 */
	BasePersMonitorRule saveRule(BasePersMonitorRule theRule) throws UnexpectedFailureException;

}
