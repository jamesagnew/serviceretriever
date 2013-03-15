package ca.uhn.sail.proxy.api;

import javax.ejb.Local;

import ca.uhn.sail.proxy.ex.InternalErrorException;
import ca.uhn.sail.proxy.ex.ProcessingException;
import ca.uhn.sail.proxy.model.entity.BasePersServiceVersion;

@Local
public interface IServiceRegistry {

	/**
	 * Load a service definition from a String containing an XML Service definition
	 */
	void loadServiceDefinition(String theXmlContents) throws InternalErrorException, ProcessingException;
	
	/**
	 * Directs the service registry to reload a copy of the entire registry from the
	 * databse and cache it in memory
	 */
	void reloadRegistryFromDatabase();
	
	/**
	 * Retrieves the specific service version definition for the given proxy path. Returns
	 * null if none exists.
	 */
	BasePersServiceVersion getServiceVersionForPath(String thePath);
	
}
