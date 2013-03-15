package ca.uhn.sail.proxy.api;

import java.util.ArrayList;
import java.util.Collection;

import javax.ejb.Local;

import ca.uhn.sail.proxy.ex.ProcessingException;
import ca.uhn.sail.proxy.model.entity.BasePersAuthenticationHost;
import ca.uhn.sail.proxy.model.entity.BasePersInvocationStats;
import ca.uhn.sail.proxy.model.entity.BasePersMethodStats;
import ca.uhn.sail.proxy.model.entity.PersAuthenticationHostLdap;
import ca.uhn.sail.proxy.model.entity.PersDomain;
import ca.uhn.sail.proxy.model.entity.PersEnvironment;
import ca.uhn.sail.proxy.model.entity.PersHttpClientConfig;
import ca.uhn.sail.proxy.model.entity.PersInvocationAnonStats;
import ca.uhn.sail.proxy.model.entity.PersInvocationAnonStatsPk;
import ca.uhn.sail.proxy.model.entity.PersInvocationStatsPk;
import ca.uhn.sail.proxy.model.entity.PersInvocationUserStats;
import ca.uhn.sail.proxy.model.entity.PersInvocationUserStatsPk;
import ca.uhn.sail.proxy.model.entity.PersService;
import ca.uhn.sail.proxy.model.entity.PersServiceUser;
import ca.uhn.sail.proxy.model.entity.PersServiceVersionStatus;
import ca.uhn.sail.proxy.model.entity.PersServiceVersionUrlStatus;
import ca.uhn.sail.proxy.model.entity.soap.PersServiceVersionSoap11;

@Local
public interface IServicePersistence {

	PersDomain getOrCreateDomainWithId(String theId) throws ProcessingException;
	
	Collection<PersDomain> getAllDomains();

	PersService getOrCreateServiceWithId(PersDomain theDomain, String theServiceId, String theServiceName) throws ProcessingException;

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
	
}
