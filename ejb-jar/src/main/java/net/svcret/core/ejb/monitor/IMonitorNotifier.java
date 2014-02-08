package net.svcret.core.ejb.monitor;

import net.svcret.admin.api.ProcessingException;
import net.svcret.core.model.entity.PersMonitorRuleFiring;

public interface IMonitorNotifier {

	void notifyFailingRule(PersMonitorRuleFiring theFiring) throws ProcessingException;

}
