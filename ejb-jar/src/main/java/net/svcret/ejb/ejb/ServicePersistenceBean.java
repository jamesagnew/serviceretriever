package net.svcret.ejb.ejb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import net.svcret.admin.shared.model.StatusEnum;
import net.svcret.ejb.api.IServicePersistence;
import net.svcret.ejb.ex.ProcessingException;
import net.svcret.ejb.model.entity.BasePersAuthenticationHost;
import net.svcret.ejb.model.entity.BasePersMethodStats;
import net.svcret.ejb.model.entity.BasePersServiceVersion;
import net.svcret.ejb.model.entity.PersAuthenticationHostLdap;
import net.svcret.ejb.model.entity.PersAuthenticationHostLocalDatabase;
import net.svcret.ejb.model.entity.PersBaseClientAuth;
import net.svcret.ejb.model.entity.PersDomain;
import net.svcret.ejb.model.entity.PersEnvironment;
import net.svcret.ejb.model.entity.PersHttpClientConfig;
import net.svcret.ejb.model.entity.PersInvocationAnonStats;
import net.svcret.ejb.model.entity.PersInvocationAnonStatsPk;
import net.svcret.ejb.model.entity.PersInvocationStats;
import net.svcret.ejb.model.entity.PersInvocationStatsPk;
import net.svcret.ejb.model.entity.PersInvocationUserStats;
import net.svcret.ejb.model.entity.PersInvocationUserStatsPk;
import net.svcret.ejb.model.entity.PersLocks;
import net.svcret.ejb.model.entity.PersService;
import net.svcret.ejb.model.entity.PersServiceVersionMethod;
import net.svcret.ejb.model.entity.PersServiceVersionStatus;
import net.svcret.ejb.model.entity.PersServiceVersionUrl;
import net.svcret.ejb.model.entity.PersServiceVersionUrlStatus;
import net.svcret.ejb.model.entity.PersState;
import net.svcret.ejb.model.entity.PersUser;
import net.svcret.ejb.model.entity.Queries;
import net.svcret.ejb.model.entity.soap.PersServiceVersionSoap11;
import net.svcret.ejb.util.Validate;

@Stateless
@Singleton
public class ServicePersistenceBean implements IServicePersistence {

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(ServicePersistenceBean.class);

	@PersistenceContext(name = "ServiceProxy_EJBPU", type = PersistenceContextType.TRANSACTION, unitName = "ServiceProxy_EJBPU")
	private EntityManager myEntityManager;

	private Map<Long, Long> myServiceVersionPidToStatusPid = new HashMap<Long, Long>();

	private PersHttpClientConfig addDefaultHttpClientConfig() {
		PersHttpClientConfig retVal = new PersHttpClientConfig();
		retVal.setDefaults();
		retVal.setId(PersHttpClientConfig.DEFAULT_ID);

		retVal = myEntityManager.merge(retVal);

		return retVal;
	}

	@Override
	public void deleteHttpClientConfig(PersHttpClientConfig theConfig) {
		Validate.throwIllegalArgumentExceptionIfNull("HttpClientConfig", theConfig);
		Validate.throwIllegalArgumentExceptionIfNull("HttpClientConfig#PID", theConfig.getPid());

		ourLog.info("Deleting HTTP client config {} / {}", theConfig.getPid(), theConfig.getId());

		myEntityManager.remove(theConfig);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<PersDomain> getAllDomains() {
		Query q = myEntityManager.createQuery("SELECT d FROM PersDomain d");
		List<PersDomain> resultList = q.getResultList();
		return resultList;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<PersService> getAllServices() {
		Query q = myEntityManager.createQuery("SELECT s FROM PersService s");
		Collection<PersService> resultList = q.getResultList();
		return resultList;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<PersUser> getAllServiceUsers() {
		Query q = myEntityManager.createQuery("SELECT u FROM PersUser u");
		Collection<PersUser> resultList = q.getResultList();
		return resultList;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<PersServiceVersionSoap11> getAllServiceVersions() {
		Query q = myEntityManager.createQuery("SELECT v FROM PersServiceVersionSoap11 v");
		Collection<PersServiceVersionSoap11> resultList = q.getResultList();
		return resultList;
	}

	@Override
	public BasePersAuthenticationHost getAuthenticationHost(String theModuleId) throws ProcessingException {
		Validate.throwProcessingExceptionIfBlank("ModuleId", theModuleId);

		Query q = myEntityManager.createQuery("SELECT a FROM BasePersAuthenticationHost a WHERE a.myModuleId = :MODULE_ID");
		q.setParameter("MODULE_ID", theModuleId);
		try {
			return (BasePersAuthenticationHost) q.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	@Override
	public PersDomain getDomainById(String theDomainId) {
		Validate.throwIllegalArgumentExceptionIfBlank("DomainId", theDomainId);
		Query q = myEntityManager.createQuery("SELECT d FROM PersDomain d WHERE d.myDomainId = :DOMAIN_ID");
		q.setParameter("DOMAIN_ID", theDomainId);
		try {
			return (PersDomain) q.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	@Override
	public PersDomain getDomainByPid(long theDomainPid) {
		return myEntityManager.find(PersDomain.class, theDomainPid);
	}

	@Override
	public PersHttpClientConfig getHttpClientConfig(long thePid) {
		return myEntityManager.find(PersHttpClientConfig.class, thePid);
	}

	@Override
	public Collection<PersHttpClientConfig> getHttpClientConfigs() {
		TypedQuery<PersHttpClientConfig> q = myEntityManager.createQuery("SELECT s FROM PersHttpClientConfig s", PersHttpClientConfig.class);
		List<PersHttpClientConfig> results = q.getResultList();

		boolean foundDefault = false;
		for (PersHttpClientConfig next : results) {
			if (next.getId().equals(PersHttpClientConfig.DEFAULT_ID)) {
				foundDefault = true;
				break;
			}
		}

		if (!foundDefault) {
			results = Collections.singletonList(addDefaultHttpClientConfig());
		}

		return results;
	}

	@Override
	public PersAuthenticationHostLdap getOrCreateAuthenticationHostLdap(String theModuleId) throws ProcessingException {
		BasePersAuthenticationHost retVal = getAuthenticationHost(theModuleId);

		if (retVal == null) {
			PersAuthenticationHostLdap host = new PersAuthenticationHostLdap(theModuleId);
			host.setModuleName(theModuleId);
			host.setDefaults();
			
			retVal = host;
			retVal = myEntityManager.merge(retVal);
			retVal.setNewlyCreated(true);
		} else {

			if (!(retVal instanceof PersAuthenticationHostLdap)) {
				throw new ProcessingException("Authentication host with ID " + theModuleId + " already exists but it is not an LDAP module");
			}

		}

		return (PersAuthenticationHostLdap) retVal;
	}

	@Override
	public PersAuthenticationHostLocalDatabase getOrCreateAuthenticationHostLocalDatabase(String theModuleId) throws ProcessingException {
		BasePersAuthenticationHost retVal = getAuthenticationHost(theModuleId);

		if (retVal == null) {
			PersAuthenticationHostLocalDatabase host = new PersAuthenticationHostLocalDatabase(theModuleId);
			host.setModuleName(theModuleId);
			retVal = host;
			retVal = myEntityManager.merge(retVal);
			retVal.setNewlyCreated(true);
		} else {

			if (!(retVal instanceof PersAuthenticationHostLocalDatabase)) {
				throw new ProcessingException("Authentication host with ID " + theModuleId + " already exists but it is not a Local Database module");
			}

		}

		return (PersAuthenticationHostLocalDatabase) retVal;
	}

	@Override
	public PersDomain getOrCreateDomainWithId(String theId) throws ProcessingException {
		Validate.throwProcessingExceptionIfBlank("The ID may not be blank", theId);

		PersDomain retVal = getDomainById(theId);
		if (retVal == null) {
			retVal = new PersDomain();
			retVal.setDomainId(theId);
			retVal = myEntityManager.merge(retVal);
			retVal.setNewlyCreated(true);
		}
		return retVal;
	}

	@Override
	public PersEnvironment getOrCreateEnvironment(String theEnv) throws ProcessingException {
		Validate.throwProcessingExceptionIfBlank("Env", theEnv);

		Query q = myEntityManager.createQuery("SELECT e FROM PersEnvironment e WHERE e.myEnv = :ENV");
		q.setParameter("ENV", theEnv);
		PersEnvironment retVal;
		try {
			retVal = (PersEnvironment) q.getSingleResult();
		} catch (NoResultException e) {
			retVal = new PersEnvironment();
			retVal.setEnv(theEnv);
			retVal = myEntityManager.merge(retVal);
		}

		return retVal;
	}

	@Override
	public PersHttpClientConfig getOrCreateHttpClientConfig(String theId) {
		Validate.throwIllegalArgumentExceptionIfBlank("Id", theId);

		Query q = myEntityManager.createQuery("SELECT c FROM PersHttpClientConfig c WHERE c.myId = :ID");
		q.setMaxResults(1);
		q.setParameter("ID", theId);

		try {
			return (PersHttpClientConfig) q.getSingleResult();
		} catch (NoResultException e) {

			ourLog.info("Creating new HTTP client config with ID: {}", theId);

			PersHttpClientConfig c = new PersHttpClientConfig();
			c.setId(theId);
			c.setDefaults();
			c = myEntityManager.merge(c);
			return c;

		}
	}

	@Override
	public PersInvocationAnonStats getOrCreateInvocationAnonStats(PersInvocationAnonStatsPk thePk) {
		PersInvocationAnonStats retVal = myEntityManager.find(PersInvocationAnonStats.class, thePk);

		if (retVal == null) {
			retVal = new PersInvocationAnonStats(thePk);
			retVal = myEntityManager.merge(retVal);
			retVal.setNewlyCreated(true);
		}

		return retVal;
	}

	@Override
	public PersInvocationStats getOrCreateInvocationStats(PersInvocationStatsPk thePk) {
		PersInvocationStats retVal = myEntityManager.find(PersInvocationStats.class, thePk);

		if (retVal == null) {
			retVal = new PersInvocationStats(thePk);
			retVal = myEntityManager.merge(retVal);
			retVal.setNewlyCreated(true);
		}

		return retVal;
	}

	@Override
	public PersInvocationUserStats getOrCreateInvocationUserStats(PersInvocationUserStatsPk thePk) {
		PersInvocationUserStats retVal = myEntityManager.find(PersInvocationUserStats.class, thePk);

		if (retVal == null) {
			retVal = new PersInvocationUserStats(thePk);
			retVal = myEntityManager.merge(retVal);
			retVal.setNewlyCreated(true);
		}

		return retVal;
	}

	@Override
	public PersUser getOrCreateUser(BasePersAuthenticationHost theAuthHost, String theUsername) throws ProcessingException {
		Validate.throwIllegalArgumentExceptionIfNull("AuthenticationHost", theAuthHost);
		Validate.throwIllegalArgumentExceptionIfBlank("Username", theUsername);

		Query q = myEntityManager.createNamedQuery(Queries.PERSUSER_FIND);
		q.setParameter("USERNAME", theUsername);
		q.setParameter("AUTH_HOST", theAuthHost);
		PersUser retVal;
		try {
			retVal = (PersUser) q.getSingleResult();
		} catch (NoResultException e) {
			retVal = new PersUser();
			retVal.setUsername(theUsername);
			retVal.setAuthenticationHost(theAuthHost);
			retVal = myEntityManager.merge(retVal);
		}

		return retVal;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PersServiceVersionSoap11 getOrCreateServiceVersionWithId(PersService theService, String theId) throws ProcessingException {
		Validate.throwIllegalArgumentExceptionIfNull("PersService", theService);
		Validate.throwProcessingExceptionIfBlank("The ID may not be blank", theId);

		Query q = myEntityManager.createQuery("SELECT v FROM PersServiceVersionSoap11 v WHERE v.myService.myPid = :SERVICE_PID AND v.myVersionId = :VERSION_ID");
		q.setParameter("SERVICE_PID", theService.getPid());
		q.setParameter("VERSION_ID", theId);
		PersServiceVersionSoap11 retVal;
		try {
			retVal = (PersServiceVersionSoap11) q.getSingleResult();
		} catch (NoResultException e) {
			ourLog.info("Creating new service version {} for service {}", theId, theService.getServiceId());

			PersHttpClientConfig config = getOrCreateHttpClientConfig(PersHttpClientConfig.DEFAULT_ID);

			retVal = new PersServiceVersionSoap11();
			retVal.setService(theService);
			retVal.setVersionId(theId);
			retVal.setHttpClientConfig(config);

//			retVal = myEntityManager.merge(retVal);

			theService.addVersion(retVal);
			PersService service = myEntityManager.merge(theService);
			

			retVal = (PersServiceVersionSoap11) service.getVersionWithId(theId);
			
			// Create a status entry
			
			PersServiceVersionStatus status = retVal.getStatus();
			status.setServiceVersion(retVal);
			PersServiceVersionStatus newStatus = myEntityManager.merge(status);
			retVal.setStatus(newStatus);
			
			retVal.setNewlyCreated(true);
		}

		return retVal;
	}

	@Override
	public PersService getOrCreateServiceWithId(PersDomain theDomain, String theId) throws ProcessingException {
		Validate.throwIllegalArgumentExceptionIfNull("PersDomain", theDomain);
		Validate.throwProcessingExceptionIfBlank("The ID may not be blank", theId);

		PersService retVal = getServiceById(theDomain.getPid(), theId);
		if (retVal != null) {
			return retVal;
		}

		retVal = new PersService();

		retVal.setServiceId(theId);
		retVal.setDomain(theDomain);

//		retVal = myEntityManager.merge(retVal);
		PersDomain domain = myEntityManager.merge(retVal.getDomain());

		retVal = domain.getServiceWithId(theId);
		retVal.setNewlyCreated(true);
		return retVal;
	}

	@Override
	public PersService getServiceById(long theDomainPid, String theServiceId) {
		Validate.throwIllegalArgumentExceptionIfBlank("ServiceId", theServiceId);

		Query q = myEntityManager.createQuery("SELECT s FROM PersService s WHERE s.myServiceId = :SERVICE_ID AND s.myPersDomain.myPid = :DOMAIN_PID");
		q.setParameter("SERVICE_ID", theServiceId);
		q.setParameter("DOMAIN_PID", theDomainPid);
		PersService retVal;
		try {
			retVal = (PersService) q.getSingleResult();
			if (!retVal.getDomain().getPid().equals(theDomainPid)) {
				throw new IllegalArgumentException("Can't get service " + retVal.getPid() + " from domain " + theDomainPid + " because it belongs to domain " + retVal.getDomain().getPid());
			}
			return retVal;
		} catch (NoResultException e) {
			return null;
		}
	}

	@Override
	public PersService getServiceByPid(long theService) {
		return myEntityManager.find(PersService.class, theService);
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public long getStateCounter(String theKey) {
		Validate.throwIllegalArgumentExceptionIfBlank("Key", theKey);
		
		PersState state = myEntityManager.find(PersState.class, theKey);
		if (state == null) {
			state = new PersState(theKey);
			state = myEntityManager.merge(state);
		}
		
		return state.getVersion();
	}

	@Override
	public PersServiceVersionStatus getStatusForServiceVersionWithPid(long theServicePid) {
		Long statusPid = myServiceVersionPidToStatusPid.get(theServicePid);
		if (statusPid == null) {
			Query q = myEntityManager.createQuery("SELECT s FROM PersServiceVersionStatus s WHERE s.myServiceVersion.myPid = :PID");
			q.setParameter("PID", theServicePid);
			try {
				PersServiceVersionStatus status = (PersServiceVersionStatus) q.getSingleResult();
				statusPid = status.getPid();
				myServiceVersionPidToStatusPid.put(theServicePid, statusPid);
			} catch (NoResultException e) {
				return null;
			}
		}

		PersServiceVersionStatus retVal = myEntityManager.find(PersServiceVersionStatus.class, statusPid);
		return retVal;
	}

	private void grabLock(String lockName) {
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put("javax.persistence.lock.timeout", 10 * 1000);
		PersLocks lock = myEntityManager.find(PersLocks.class, lockName, LockModeType.PESSIMISTIC_WRITE, properties);
		if (lock == null) {
			lock = new PersLocks(lockName);
			myEntityManager.persist(lock);
			myEntityManager.flush();
			lock = myEntityManager.find(PersLocks.class, lockName, LockModeType.PESSIMISTIC_WRITE, properties);
			// myEntityManager.lock(lock, LockModeType.PESSIMISTIC_WRITE,
			// properties);
		}
	}

	@PostConstruct
	public void postConstruct() {
		// Create defaults
		getOrCreateHttpClientConfig(PersHttpClientConfig.DEFAULT_ID);
	}

	@Override
	public void removeServiceVersion(long thePid) throws ProcessingException {
		Validate.throwIllegalArgumentExceptionIfNotGreaterThanZero("ServiceVersionPid", thePid);

		BasePersServiceVersion version = myEntityManager.find(PersServiceVersionSoap11.class, thePid);
		if (version == null) {
			throw new ProcessingException("Unknown ServiceVersion[" + thePid + "]");
		}

		ourLog.info("Removing ServiceVersion[{}]", version.getPid());

		PersService service = version.getService();
		service.removeVersion(version);
		myEntityManager.merge(service);

		myEntityManager.remove(version);

		myServiceVersionPidToStatusPid.remove(thePid);
	}

	@Override
	public void saveDomain(PersDomain theDomain) {
		Validate.throwIllegalArgumentExceptionIfNull("Domain", theDomain);
		Validate.throwIllegalArgumentExceptionIfNull("Domain#PID", theDomain.getPid());

		ourLog.info("Saving domain {}", theDomain.getPid());

		myEntityManager.merge(theDomain);
	}

	@Override
	public PersHttpClientConfig saveHttpClientConfig(PersHttpClientConfig theConfig) {
		Validate.throwIllegalArgumentExceptionIfNull("HttpClientConfig", theConfig);

		boolean isNew = false;
		if (theConfig.getPid() == null) {
			isNew = true;
			ourLog.info("Creating HTTP client config {}", theConfig.getPid(), theConfig.getId());
		} else {
			ourLog.info("Saving HTTP client config {} / {}", theConfig.getPid(), theConfig.getId());
		}

		PersHttpClientConfig retVal = myEntityManager.merge(theConfig);

		if (isNew) {
			ourLog.info("Done creating HTTP client config {}, assigned PID {}", theConfig.getId(), theConfig.getPid());
		}

		return retVal;
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@Override
	public void saveInvocationStats(Collection<BasePersMethodStats> theStats) {
		grabLock("FLUSH_STATS");

		int count = 0;
		int ucount = 0;
		int acount = 0;
		if (theStats != null) {
			for (Iterator<BasePersMethodStats> iter = theStats.iterator(); iter.hasNext();) {
				BasePersMethodStats next = iter.next();

				BasePersMethodStats persisted;
				if (next instanceof PersInvocationUserStats) {
					PersInvocationUserStats cNext = (PersInvocationUserStats) next;
					persisted = getOrCreateInvocationUserStats(cNext.getPk());
					if (!persisted.isNewlyCreated()) {
						myEntityManager.refresh(persisted);
					}
					persisted.mergeUnsynchronizedEvents(next);
					ucount++;
				} else if (next instanceof PersInvocationAnonStats) {
					PersInvocationAnonStats cNext = (PersInvocationAnonStats) next;
					persisted = getOrCreateInvocationAnonStats(cNext.getPk());
					if (!persisted.isNewlyCreated()) {
						myEntityManager.refresh(persisted);
					}
					persisted.mergeUnsynchronizedEvents(next);
					acount++;
				} else if (next instanceof PersInvocationStats) {
					PersInvocationStats cNext = (PersInvocationStats) next;
					persisted = getOrCreateInvocationStats(cNext.getPk());
					if (!persisted.isNewlyCreated()) {
						myEntityManager.refresh(persisted);
					}
					persisted.mergeUnsynchronizedEvents(next);
					count++;
				} else {
					throw new IllegalArgumentException("Unknown stats type: " + next.getClass());
				}

				myEntityManager.merge(persisted);
			}
		}

		ourLog.info("Persisted {} invocation status entries, {} user invocation status entries, and {} anonymous status entries", new Object[] { count, ucount, acount });
	}

	@Override
	public void saveService(PersService theService) {
		Validate.throwIllegalArgumentExceptionIfNull("Service", theService);
		Validate.throwIllegalArgumentExceptionIfNull("Service#PID", theService.getPid());

		ourLog.info("Saving service with PID[{}]", theService.getPid());

		myEntityManager.merge(theService);
	}

	@Override
	public PersUser saveServiceUser(PersUser theUser) {
		Validate.throwIllegalArgumentExceptionIfNull("User", theUser);
		Validate.throwIllegalArgumentExceptionIfNull("User.AuthenticationHost", theUser.getAuthenticationHost());

		return myEntityManager.merge(theUser);
	}

	/**
	 * {@inheritDoc}
	 * @return 
	 */
	@Override
	public BasePersServiceVersion saveServiceVersion(BasePersServiceVersion theVersion) throws ProcessingException {
		Validate.throwIllegalArgumentExceptionIfNull("ServiceVersion", theVersion);
		Validate.throwIllegalArgumentExceptionIfNull("ServiceVersion.myId", theVersion.getPid());
		Validate.throwProcessingExceptionIfBlank("ID may not be missing or null", theVersion.getVersionId());

		ourLog.info("Saving service version with PID {}", theVersion.getPid());

		for (PersServiceVersionUrl next : theVersion.getUrls()) {
			Validate.throwProcessingExceptionIfBlank("URL is missing ID", next.getUrlId());
			next.setServiceVersion(theVersion);
		}

		for (PersBaseClientAuth<?> next : theVersion.getClientAuths()) {
			next.setServiceVersion(theVersion);
		}

		for (PersServiceVersionMethod next : theVersion.getMethods()) {
			Validate.throwProcessingExceptionIfBlank("Method is missing name", next.getName());
			next.setServiceVersion(theVersion);
		}

		ourLog.info("Merging servicde version {}", theVersion.getVersionId());
		BasePersServiceVersion version = myEntityManager.merge(theVersion);

		// Add status entity for any URLs that don't yet have one

		for (PersServiceVersionUrl nextUrl : version.getUrls()) {
			if (nextUrl.getStatus() == null) {
				PersServiceVersionUrlStatus stats = new PersServiceVersionUrlStatus();
				stats.setStatus(StatusEnum.UNKNOWN);
				stats.setUrl(nextUrl);
				nextUrl.setStatus(stats);
				myEntityManager.persist(stats);
			}

		}
		
		return version;
	}

	@Override
	public void saveServiceVersionUrlStatus(ArrayList<PersServiceVersionUrlStatus> theStatuses) {
		grabLock("URL_STATUS");

		int count = 0;
		if (theStatuses != null) {
			for (Iterator<PersServiceVersionUrlStatus> iter = theStatuses.iterator(); iter.hasNext();) {
				PersServiceVersionUrlStatus next = iter.next();
				// PersServiceVersionUrlStatus existing =
				// myEntityManager.find(PersServiceVersionUrlStatus.class,
				// next.getPid());
				//
				// if (existing == null) {
				// // This shouldn't happen..
				// ourLog.info("URL Status[{}] is missing from database, re-persisting!");
				// myEntityManager.merge(next);
				// continue;
				// }

				myEntityManager.merge(next);

				count++;
			}
		}

		ourLog.info("Persisted {} URL status entries", count);
	}

	/**
	 * FOR UNIT TESTS ONLY
	 */
	public void setEntityManager(EntityManager theEntityManager) {
		myEntityManager = theEntityManager;
	}

	@Override
	public long incrementStateCounter(String theKey) {
		Validate.throwIllegalArgumentExceptionIfBlank("Key", theKey);
		
		PersState state = myEntityManager.find(PersState.class, theKey);
		if (state == null) {
			state = new PersState(theKey);
			state.setVersion(1);
			state = myEntityManager.merge(state);
		} else {
			state.incrementVersion();
			state = myEntityManager.merge(state);
		}
		
		return state.getVersion();
	}

	@Override
	public void saveAuthenticationHost(PersAuthenticationHostLocalDatabase theAuthHost) {
		Validate.throwIllegalArgumentExceptionIfNull("AuthHost", theAuthHost);
		
		ourLog.info("Saving authentication host {} / {}", theAuthHost.getPid(), theAuthHost.getModuleId());
		
		myEntityManager.merge(theAuthHost);
	}

	@Override
	public Collection<BasePersAuthenticationHost> getAllAuthenticationHosts() {
		TypedQuery<BasePersAuthenticationHost> q = myEntityManager.createNamedQuery(Queries.AUTHHOST_FINDALL, BasePersAuthenticationHost.class);
		return q.getResultList();
	}

	@Override
	public BasePersAuthenticationHost getAuthenticationHostByPid(long thePid) {
		return myEntityManager.find(BasePersAuthenticationHost.class, thePid);
	}

	@Override
	public void deleteAuthenticationHost(BasePersAuthenticationHost theAuthHost) {
		Validate.throwIllegalArgumentExceptionIfNull("AuthenticationHost", theAuthHost);
		myEntityManager.remove(theAuthHost);
	}

	@Override
	public PersUser getUser(long thePid) {
		return myEntityManager.find(PersUser.class, thePid);
	}

	@Override
	public PersServiceVersionMethod getServiceVersionMethodByPid(long theServiceVersionMethodPid) {
		return myEntityManager.find(PersServiceVersionMethod.class, theServiceVersionMethodPid);
	}

	@Override
	public BasePersServiceVersion getServiceVersionByPid(long theServiceVersionPid) {
		return myEntityManager.find(BasePersServiceVersion.class, theServiceVersionPid);
	}

}
