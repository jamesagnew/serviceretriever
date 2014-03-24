package net.svcret.core.api;

import java.util.Collection;
import java.util.List;

import net.svcret.admin.api.ProcessingException;
import net.svcret.admin.api.UnexpectedFailureException;
import net.svcret.admin.shared.model.ServiceProtocolEnum;
import net.svcret.core.ejb.ServiceRegistryBean.FoundServiceVersionBean;
import net.svcret.core.ex.InvocationFailedDueToInternalErrorException;
import net.svcret.core.model.entity.BasePersMonitorRule;
import net.svcret.core.model.entity.BasePersServiceVersion;
import net.svcret.core.model.entity.PersDomain;
import net.svcret.core.model.entity.PersHttpClientConfig;
import net.svcret.core.model.entity.PersMethod;
import net.svcret.core.model.entity.PersService;
import net.svcret.core.model.entity.PersServiceVersionUrl;
import net.svcret.core.model.entity.virtual.PersServiceVersionVirtual;

public interface IServiceRegistry {

	/**
	 * Save a rule and notify anyone interested
	 */
	BasePersMonitorRule saveRule(BasePersMonitorRule theRule) throws UnexpectedFailureException;

	void deleteMonitorRule(Long thePid) throws UnexpectedFailureException;

	
	/**
	 * Load a service definition from a String containing an XML Service definition
	 * @throws UnexpectedFailureException 
	 */
	// void loadServiceDefinition(String theXmlContents) throws
	// InternalErrorException, ProcessingException;

	void deleteHttpClientConfig(long thePid) throws ProcessingException, UnexpectedFailureException;

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
	FoundServiceVersionBean getServiceVersionForPath(String thePath);

	/**
	 * Provide a set of valid paths which can be used to access services
	 */
	List<String> getValidPaths();

	/**
	 * Directs the service registry to reload a copy of the entire registry from the databse and cache it in memory
	 */
	void reloadRegistryFromDatabase();

	void deleteDomain(long thePid) throws ProcessingException, UnexpectedFailureException;

	PersServiceVersionUrl resetCircuitBreaker(long theUrlPid) throws  UnexpectedFailureException;

	PersDomain saveDomain(PersDomain theDomain) throws ProcessingException, UnexpectedFailureException;

	PersHttpClientConfig saveHttpClientConfig(PersHttpClientConfig theConfig) throws ProcessingException, UnexpectedFailureException;

	PersService saveService(PersService theService) throws ProcessingException, UnexpectedFailureException;

	BasePersServiceVersion saveServiceVersion(BasePersServiceVersion theSv) throws  UnexpectedFailureException, ProcessingException;

	BasePersServiceVersion getOrCreateServiceVersionWithId(PersService theService, ServiceProtocolEnum theProtocol, String theVersionId, PersServiceVersionVirtual theSvcVerToUseIfCreatingNew) throws UnexpectedFailureException;

	PersMethod getOrCreateUnknownMethodEntryForServiceVersion(BasePersServiceVersion theServiceVersion) throws InvocationFailedDueToInternalErrorException;

	void deleteServiceVersion(long thePid) throws ProcessingException, UnexpectedFailureException;

	void deleteService(long theServicePid) throws ProcessingException, UnexpectedFailureException;

}
