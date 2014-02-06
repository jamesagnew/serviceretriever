package net.svcret.ejb.ejb.monitor;

import javax.ejb.Local;

import net.svcret.admin.api.ProcessingException;
import net.svcret.ejb.model.entity.PersMonitorRuleFiring;

@Local
public interface IMonitorNotifier {

	void notifyFailingRule(PersMonitorRuleFiring theFiring) throws ProcessingException;

}
