package net.svcret.ejb.ejb.monitor;

import net.svcret.admin.api.ProcessingException;
import net.svcret.ejb.model.entity.PersMonitorRuleFiring;

public interface IMonitorNotifier {

	void notifyFailingRule(PersMonitorRuleFiring theFiring) throws ProcessingException;

}
