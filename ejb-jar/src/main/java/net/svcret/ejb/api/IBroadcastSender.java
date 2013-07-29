package net.svcret.ejb.api;

import javax.ejb.Local;

import net.svcret.ejb.ex.ProcessingException;

@Local
public interface IBroadcastSender {

	void notifyUserCatalogChanged() throws ProcessingException;
	
	void notifyServiceCatalogChanged() throws ProcessingException;

	void notifyConfigChanged() throws ProcessingException;

	void monitorRulesChanged() throws ProcessingException;
	
}
