package ca.uhn.sail.proxy.ejb;

import java.util.ArrayList;
import java.util.Collection;
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

import org.apache.commons.lang3.StringUtils;

import ca.uhn.sail.proxy.api.IServicePersistence;
import ca.uhn.sail.proxy.ex.ProcessingException;
import ca.uhn.sail.proxy.model.entity.BasePersAuthenticationHost;
import ca.uhn.sail.proxy.model.entity.BasePersMethodStats;
import ca.uhn.sail.proxy.model.entity.BasePersServiceVersion;
import ca.uhn.sail.proxy.model.entity.PersAuthenticationHostLdap;
import ca.uhn.sail.proxy.model.entity.PersBaseClientAuth;
import ca.uhn.sail.proxy.model.entity.PersDomain;
import ca.uhn.sail.proxy.model.entity.PersEnvironment;
import ca.uhn.sail.proxy.model.entity.PersHttpClientConfig;
import ca.uhn.sail.proxy.model.entity.PersInvocationAnonStats;
import ca.uhn.sail.proxy.model.entity.PersInvocationAnonStatsPk;
import ca.uhn.sail.proxy.model.entity.PersInvocationStats;
import ca.uhn.sail.proxy.model.entity.PersInvocationStatsPk;
import ca.uhn.sail.proxy.model.entity.PersInvocationUserStats;
import ca.uhn.sail.proxy.model.entity.PersInvocationUserStatsPk;
import ca.uhn.sail.proxy.model.entity.PersLocks;
import ca.uhn.sail.proxy.model.entity.PersServiceVersionMethod;
import ca.uhn.sail.proxy.model.entity.PersService;
import ca.uhn.sail.proxy.model.entity.PersServiceUser;
import ca.uhn.sail.proxy.model.entity.PersServiceVersionStatus;
import ca.uhn.sail.proxy.model.entity.PersServiceVersionUrl;
import ca.uhn.sail.proxy.model.entity.PersServiceVersionUrlStatus;
import ca.uhn.sail.proxy.model.entity.PersServiceVersionUrlStatus.StatusEnum;
import ca.uhn.sail.proxy.model.entity.soap.PersServiceVersionSoap11;
import ca.uhn.sail.proxy.util.Validate;

@Stateless
@Singleton
public class ServicePersistenceBean implements IServicePersistence {

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(ServicePersistenceBean.class);

	@PersistenceContext(name = "ServiceProxy_EJBPU", type = PersistenceContextType.TRANSACTION, unitName = "ServiceProxy_EJBPU")
	private EntityManager myEntityManager;

	private Map<Long, Long> myServiceVersionPidToStatusPid = new HashMap<Long, Long>();

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
	public Collection<PersServiceUser> getAllServiceUsers() {
		Query q = myEntityManager.createQuery("SELECT u FROM PersServiceUser u");
		Collection<PersServiceUser> resultList = q.getResultList();
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
			return (PersAuthenticationHostLdap) q.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	@Override
	public PersAuthenticationHostLdap getOrCreateAuthenticationHostLdap(String theModuleId) throws ProcessingException {
		BasePersAuthenticationHost retVal = getAuthenticationHost(theModuleId);

		if (retVal == null) {
			retVal = new PersAuthenticationHostLdap(theModuleId);
			retVal = myEntityManager.merge(retVal);
		} else {

			if (!(retVal instanceof PersAuthenticationHostLdap)) {
				throw new ProcessingException("Authentication host with ID " + theModuleId + " already exists but it is not an LDAP module");
			}

		}

		return (PersAuthenticationHostLdap) retVal;
	}

	@Override
	public PersDomain getOrCreateDomainWithId(String theId) throws ProcessingException {
		Validate.throwProcessingExceptionIfBlank("The ID may not be blank", theId);

		Query q = myEntityManager.createQuery("SELECT d FROM PersDomain d WHERE d.myDomainId = :DOMAIN_ID");
		q.setParameter("DOMAIN_ID", theId);
		try {
			return (PersDomain) q.getSingleResult();
		} catch (NoResultException e) {
			PersDomain retVal = new PersDomain();
			retVal.setDomainId(theId);
			return myEntityManager.merge(retVal);
		}
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
	public PersInvocationAnonStats getOrCreateInvocationAnonStats(PersInvocationAnonStatsPk thePk) {
		PersInvocationAnonStats retVal = myEntityManager.find(PersInvocationAnonStats.class, thePk);

		if (retVal == null) {
			retVal = new PersInvocationAnonStats(thePk);
			retVal = myEntityManager.merge(retVal);
		}

		return retVal;
	}

	@Override
	public PersInvocationStats getOrCreateInvocationStats(PersInvocationStatsPk thePk) {
		PersInvocationStats retVal = myEntityManager.find(PersInvocationStats.class, thePk);

		if (retVal == null) {
			retVal = new PersInvocationStats(thePk);
			retVal = myEntityManager.merge(retVal);
		}

		return retVal;
	}

	@Override
	public PersInvocationUserStats getOrCreateInvocationUserStats(PersInvocationUserStatsPk thePk) {
		PersInvocationUserStats retVal = myEntityManager.find(PersInvocationUserStats.class, thePk);

		if (retVal == null) {
			retVal = new PersInvocationUserStats(thePk);
			retVal = myEntityManager.merge(retVal);
		}

		return retVal;
	}

	@Override
	public PersServiceUser getOrCreateServiceUser(String theUsername) throws ProcessingException {
		Validate.throwProcessingExceptionIfBlank("Username", theUsername);

		Query q = myEntityManager.createQuery("SELECT u FROM PersServiceUser u WHERE u.myUsername = :USERNAME");
		q.setParameter("USERNAME", theUsername);
		PersServiceUser retVal;
		try {
			retVal = (PersServiceUser) q.getSingleResult();
		} catch (NoResultException e) {
			retVal = new PersServiceUser();
			retVal.setUsername(theUsername);
			retVal = myEntityManager.merge(retVal);
		}

		return retVal;
	}

	@PostConstruct
	public void postConstruct() {
		// Create defaults
		getOrCreateHttpClientConfig(PersHttpClientConfig.DEFAULT_ID);
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

			retVal = myEntityManager.merge(retVal);

			PersServiceVersionStatus status = retVal.getStatus();
			status.setServiceVersion(retVal);
			PersServiceVersionStatus newStatus = myEntityManager.merge(status);
			retVal.setStatus(newStatus);

			theService.getVersions().add(retVal);
			myEntityManager.merge(theService);

		}

		return retVal;
	}

	@Override
	public PersService getOrCreateServiceWithId(PersDomain theDomain, String theId, String theServiceName) throws ProcessingException {
		Validate.throwIllegalArgumentExceptionIfNull("PersDomain", theDomain);
		Validate.throwProcessingExceptionIfBlank("The ID may not be blank", theId);
		Validate.throwProcessingExceptionIfBlank("The Name may not be blank", theServiceName);

		Query q = myEntityManager.createQuery("SELECT s FROM PersService s WHERE s.myServiceId = :SERVICE_ID");
		q.setParameter("SERVICE_ID", theId);
		PersService retVal;
		try {
			retVal = (PersService) q.getSingleResult();
			if (theDomain.equals(retVal.getDomain()) && StringUtils.equals(theServiceName, retVal.getServiceName())) {
				return retVal;
			}
		} catch (NoResultException e) {
			retVal = new PersService();
			retVal.setServiceId(theId);
		}

		if (retVal.getDomain() != null && !retVal.getDomain().equals(theDomain)) {
			retVal.getDomain().getServices().remove(retVal);
			myEntityManager.merge(retVal.getDomain());
		}

		retVal.setServiceName(theServiceName);
		retVal.setPersDomain(theDomain);
		retVal = myEntityManager.merge(retVal);

		if (!retVal.getDomain().getServices().contains(retVal)) {
			retVal.getDomain().getServices().add(retVal);
			myEntityManager.merge(retVal.getDomain());
		}

		return retVal;
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

	@Override
	public void removeServiceVersion(long thePid) throws ProcessingException {
		Validate.throwIllegalArgumentExceptionIfNotGreaterThanZero("ServiceVersionPid", thePid);

		BasePersServiceVersion version = myEntityManager.find(PersServiceVersionSoap11.class, thePid);
		if (version == null) {
			throw new ProcessingException("Unknown ServiceVersion[" + thePid + "]");
		}

		ourLog.info("Removing ServiceVersion[{}]", version.getPid());

		PersService service = version.getService();
		service.getVersions().remove(version);
		myEntityManager.merge(service);

		myEntityManager.remove(version);

		myServiceVersionPidToStatusPid.remove(thePid);
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
					persisted.mergeUnsynchronizedEvents(next);
					ucount++;
				} else if (next instanceof PersInvocationAnonStats) {
					PersInvocationAnonStats cNext = (PersInvocationAnonStats) next;
					persisted = getOrCreateInvocationAnonStats(cNext.getPk());
					persisted.mergeUnsynchronizedEvents(next);
					acount++;
				} else if (next instanceof PersInvocationStats) {
					PersInvocationStats cNext = (PersInvocationStats) next;
					persisted = getOrCreateInvocationStats(cNext.getPk());
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

	@Override
	public void saveServiceUser(PersServiceUser theUser) {
		Validate.throwIllegalArgumentExceptionIfNull("User", theUser);
		Validate.throwIllegalArgumentExceptionIfNull("User.myPid", theUser.getPid());

		myEntityManager.merge(theUser);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void saveServiceVersion(PersServiceVersionSoap11 theVersion) throws ProcessingException {
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
		PersServiceVersionSoap11 version = myEntityManager.merge(theVersion);

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
	}

	/**
	 * FOR UNIT TESTS ONLY
	 */
	public void setEntityManager(EntityManager theEntityManager) {
		myEntityManager = theEntityManager;
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
	public void saveServiceVersionUrlStatus(ArrayList<PersServiceVersionUrlStatus> theStatuses) {
		grabLock("URL_STATUS");

		int count = 0;
		if (theStatuses != null) {
			for (Iterator<PersServiceVersionUrlStatus> iter = theStatuses.iterator(); iter.hasNext();) {
				PersServiceVersionUrlStatus next = iter.next();
//				PersServiceVersionUrlStatus existing = myEntityManager.find(PersServiceVersionUrlStatus.class, next.getPid());
//				
//				if (existing == null) {
//					// This shouldn't happen..
//					ourLog.info("URL Status[{}] is missing from database, re-persisting!");
//					myEntityManager.merge(next);
//					continue;
//				}
				
				myEntityManager.merge(next);

				count++;
			}
		}

		ourLog.info("Persisted {} URL status entries", count);
	}

}
