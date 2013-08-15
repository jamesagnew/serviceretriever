package net.svcret.ejb.api;

import javax.ejb.Local;

import net.svcret.ejb.ex.ProcessingException;
import net.svcret.ejb.model.entity.PersMonitorRuleFiring;

@Local
public interface IMonitorNotifier {

	void notifyFailingRule(PersMonitorRuleFiring theFiring) throws ProcessingException;

}
