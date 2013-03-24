package net.svcret.ejb.api;

import java.util.ArrayList;
import java.util.Collection;

import javax.ejb.Local;

import net.svcret.ejb.ex.ProcessingException;
import net.svcret.ejb.model.entity.BasePersAuthenticationHost;
import net.svcret.ejb.model.entity.BasePersInvocationStats;
import net.svcret.ejb.model.entity.BasePersMethodStats;
import net.svcret.ejb.model.entity.PersAuthenticationHostLdap;
import net.svcret.ejb.model.entity.PersDomain;
import net.svcret.ejb.model.entity.PersEnvironment;
import net.svcret.ejb.model.entity.PersHttpClientConfig;
import net.svcret.ejb.model.entity.PersInvocationAnonStats;
import net.svcret.ejb.model.entity.PersInvocationAnonStatsPk;
import net.svcret.ejb.model.entity.PersInvocationStatsPk;
import net.svcret.ejb.model.entity.PersInvocationUserStats;
import net.svcret.ejb.model.entity.PersInvocationUserStatsPk;
import net.svcret.ejb.model.entity.PersService;
import net.svcret.ejb.model.entity.PersServiceUser;
import net.svcret.ejb.model.entity.PersServiceVersionStatus;
import net.svcret.ejb.model.entity.PersServiceVersionUrlStatus;
import net.svcret.ejb.model.entity.soap.PersServiceVersionSoap11;


@Local
public interface IServicePersistence {

	PersDomain getOrCreateDomainWithId(String theId) throws ProcessingException;
	
	Collection<PersDomain> getAllDomains();

	PersService getOrCreateServiceWithId(PersDomain theDomain, String theServiceId) throws ProcessingException;

	Collection<PersService> getAllServices();

	Collection<PersServiceVersionSoap11> getAllServiceVersions();

	PersServiceVersionSoap11 getOrCreateServiceVersionWithId(PersService theService, String theVersionId) throws ProcessingException;

	void removeServiceVersion(long thePid) throws ProcessingException;

	void saveServiceVersion(PersServiceVersionSoap11 theVersion) throws ProcessingException;
	
	PersEnvironment getOrCreateEnvironment(String theEnv) throws ProcessingException;
	
	PersServiceUser getOrCreateServiceUser(String theUsername) throws ProcessingException;
	
	void saveServiceUser(PersServiceUser theUser);

	Collection<PersServiceUser> getAllServiceUsers();

	PersAuthenticationHostLdap getOrCreateAuthenticationHostLdap(String theModuleId) throws ProcessingException;
	
	BasePersAuthenticationHost getAuthenticationHost(String theModuleId) throws ProcessingException;

	PersServiceVersionStatus getStatusForServiceVersionWithPid(long theServicePid);
	
	BasePersInvocationStats getOrCreateInvocationStats(PersInvocationStatsPk thePk);

	PersInvocationUserStats getOrCreateInvocationUserStats(PersInvocationUserStatsPk thePk);

	void saveInvocationStats(Collection<BasePersMethodStats> theStats);

	PersInvocationAnonStats getOrCreateInvocationAnonStats(PersInvocationAnonStatsPk thePk);

	PersHttpClientConfig getOrCreateHttpClientConfig(String theId);

	void saveServiceVersionUrlStatus(ArrayList<PersServiceVersionUrlStatus> theUrlStatuses);

	void saveDomain(PersDomain theDomain);

	Collection<PersHttpClientConfig> getHttpClientConfigs();
	
	/**
	 * Returns <code>null</code> if not found
	 */
	PersDomain getDomainByPid(long theDomainPid);

	void saveService(PersService theService);

	PersDomain getDomainById(String theDomainId);

	PersService getServiceByPid(long theServicePid);

	PersService getServiceById(long theDomainPid, String theServiceId);
	
}
