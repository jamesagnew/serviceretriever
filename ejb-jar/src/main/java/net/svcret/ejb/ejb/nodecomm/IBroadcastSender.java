package net.svcret.ejb.ejb.nodecomm;

import javax.ejb.Local;

import net.svcret.admin.api.UnexpectedFailureException;
import net.svcret.ejb.model.entity.PersStickySessionUrlBinding;

@Local
public interface IBroadcastSender {

	void notifyUserCatalogChanged() throws UnexpectedFailureException;
	
	void notifyServiceCatalogChanged() throws UnexpectedFailureException;

	void notifyConfigChanged() throws UnexpectedFailureException;

	void monitorRulesChanged() throws UnexpectedFailureException;

	void notifyUrlStatusChanged(Long thePid) throws UnexpectedFailureException;

	void notifyNewStickySession(PersStickySessionUrlBinding theExisting) throws UnexpectedFailureException;
	
}
