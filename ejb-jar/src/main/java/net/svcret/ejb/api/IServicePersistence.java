package net.svcret.ejb.api;

import java.util.ArrayList;
import java.util.Collection;

import javax.ejb.Local;

import net.svcret.ejb.ex.ProcessingException;
import net.svcret.ejb.model.entity.BasePersAuthenticationHost;
import net.svcret.ejb.model.entity.BasePersInvocationStats;
import net.svcret.ejb.model.entity.BasePersMethodStats;
import net.svcret.ejb.model.entity.BasePersServiceVersion;
import net.svcret.ejb.model.entity.PersAuthenticationHostLdap;
import net.svcret.ejb.model.entity.PersAuthenticationHostLocalDatabase;
import net.svcret.ejb.model.entity.PersDomain;
import net.svcret.ejb.model.entity.PersEnvironment;
import net.svcret.ejb.model.entity.PersHttpClientConfig;
import net.svcret.ejb.model.entity.PersInvocationAnonStats;
import net.svcret.ejb.model.entity.PersInvocationAnonStatsPk;
import net.svcret.ejb.model.entity.PersInvocationStatsPk;
import net.svcret.ejb.model.entity.PersInvocationUserStats;
import net.svcret.ejb.model.entity.PersInvocationUserStatsPk;
import net.svcret.ejb.model.entity.PersService;
import net.svcret.ejb.model.entity.PersServiceVersionMethod;
import net.svcret.ejb.model.entity.PersServiceVersionStatus;
import net.svcret.ejb.model.entity.PersServiceVersionUrlStatus;
import net.svcret.ejb.model.entity.PersUser;
import net.svcret.ejb.model.entity.soap.PersServiceVersionSoap11;


@Local
public interface IServicePersistence {

	void deleteHttpClientConfig(PersHttpClientConfig theConfig);
	
	Collection<PersDomain> getAllDomains();

	long incrementStateCounter(String theKey);
	
	long getStateCounter(String theKey);

	Collection<PersService> getAllServices();

	Collection<PersUser> getAllServiceUsers();

	Collection<PersServiceVersionSoap11> getAllServiceVersions();

	BasePersAuthenticationHost getAuthenticationHost(String theModuleId) throws ProcessingException;

	PersDomain getDomainById(String theDomainId);

	/**
	 * Returns <code>null</code> if not found
	 */
	PersDomain getDomainByPid(long theDomainPid);
	
	PersHttpClientConfig getHttpClientConfig(long thePid);
	
	Collection<PersHttpClientConfig> getHttpClientConfigs();
	
	PersAuthenticationHostLdap getOrCreateAuthenticationHostLdap(String theModuleId) throws ProcessingException;

	PersDomain getOrCreateDomainWithId(String theId) throws ProcessingException;

	PersEnvironment getOrCreateEnvironment(String theEnv) throws ProcessingException;
	
	PersHttpClientConfig getOrCreateHttpClientConfig(String theId);

	PersInvocationAnonStats getOrCreateInvocationAnonStats(PersInvocationAnonStatsPk thePk);
	
	BasePersInvocationStats getOrCreateInvocationStats(PersInvocationStatsPk thePk);

	PersInvocationUserStats getOrCreateInvocationUserStats(PersInvocationUserStatsPk thePk);

	PersUser getOrCreateUser(BasePersAuthenticationHost theAuthHost, String theUsername) throws ProcessingException;

	PersServiceVersionSoap11 getOrCreateServiceVersionWithId(PersService theService, String theVersionId) throws ProcessingException;

	PersService getOrCreateServiceWithId(PersDomain theDomain, String theServiceId) throws ProcessingException;

	PersService getServiceById(long theDomainPid, String theServiceId);

	PersService getServiceByPid(long theServicePid);

	PersServiceVersionStatus getStatusForServiceVersionWithPid(long theServicePid);
	
	void removeServiceVersion(long thePid) throws ProcessingException;

	void saveDomain(PersDomain theDomain);

	PersHttpClientConfig saveHttpClientConfig(PersHttpClientConfig theConfig);

	void saveInvocationStats(Collection<BasePersMethodStats> theStats);

	void saveService(PersService theService);

	PersUser saveServiceUser(PersUser theUser);

	BasePersServiceVersion saveServiceVersion(BasePersServiceVersion theVersion) throws ProcessingException;

	void saveServiceVersionUrlStatus(ArrayList<PersServiceVersionUrlStatus> theUrlStatuses);

	PersAuthenticationHostLocalDatabase getOrCreateAuthenticationHostLocalDatabase(String theModuleIdAdminAuth) throws ProcessingException;

	void saveAuthenticationHost(PersAuthenticationHostLocalDatabase theAuthHost);

	Collection<BasePersAuthenticationHost> getAllAuthenticationHosts();

	BasePersAuthenticationHost getAuthenticationHostByPid(long thePid);

	void deleteAuthenticationHost(BasePersAuthenticationHost theAuthHost);

	PersUser getUser(long thePid);

	PersServiceVersionMethod getServiceVersionMethodByPid(long theServiceVersionMethodPid);

	BasePersServiceVersion getServiceVersionByPid(long theServiceVersionPid);

	void removeDomain(PersDomain theDomain);
	
}
