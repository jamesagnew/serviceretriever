package net.svcret.ejb.api;

import java.util.Collection;
import java.util.List;

import javax.ejb.Local;

import net.svcret.admin.shared.model.ServiceProtocolEnum;
import net.svcret.ejb.ex.ProcessingException;
import net.svcret.ejb.ex.UnexpectedFailureException;
import net.svcret.ejb.model.entity.BasePersServiceVersion;
import net.svcret.ejb.model.entity.PersDomain;
import net.svcret.ejb.model.entity.PersHttpClientConfig;
import net.svcret.ejb.model.entity.PersService;
import net.svcret.ejb.model.entity.PersServiceVersionUrl;

@Local
public interface IServiceRegistry {

	/**
	 * Load a service definition from a String containing an XML Service definition
	 * @throws UnexpectedFailureException 
	 */
	// void loadServiceDefinition(String theXmlContents) throws
	// InternalErrorException, ProcessingException;

	void deleteHttpClientConfig(PersHttpClientConfig theConfig) throws ProcessingException, UnexpectedFailureException;

	Collection<PersDomain> getAllDomains();

	PersDomain getDomainByPid(Long theDomainPid);

	PersDomain getOrCreateDomainWithId(String theId) throws ProcessingException, UnexpectedFailureException;

	BasePersServiceVersion getOrCreateServiceVersionWithId(PersService theService, ServiceProtocolEnum theProtocol, String theVersionId) throws ProcessingException, UnexpectedFailureException;

	PersService getOrCreateServiceWithId(PersDomain theDomain, String theId) throws ProcessingException, UnexpectedFailureException;

	PersService getServiceByPid(Long theServicePid);

	BasePersServiceVersion getServiceVersionByPid(long theServiceVersionPid);

	/**
	 * Retrieves the specific service version definition for the given proxy path. Returns null if none exists.
	 */
	BasePersServiceVersion getServiceVersionForPath(String thePath);

	/**
	 * Provide a set of valid paths which can be used to access services
	 */
	List<String> getValidPaths();

	/**
	 * Directs the service registry to reload a copy of the entire registry from the databse and cache it in memory
	 */
	void reloadRegistryFromDatabase();

	void removeDomain(PersDomain theDomain) throws ProcessingException, UnexpectedFailureException;

	PersServiceVersionUrl resetCircuitBreaker(long theUrlPid) throws  UnexpectedFailureException;

	PersDomain saveDomain(PersDomain theDomain) throws ProcessingException, UnexpectedFailureException;

	PersHttpClientConfig saveHttpClientConfig(PersHttpClientConfig theConfig) throws ProcessingException, UnexpectedFailureException;

	void saveService(PersService theService) throws ProcessingException, UnexpectedFailureException;

	BasePersServiceVersion saveServiceVersion(BasePersServiceVersion theSv) throws  UnexpectedFailureException, ProcessingException;

}
