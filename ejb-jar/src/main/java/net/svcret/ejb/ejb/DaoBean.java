package net.svcret.ejb.ejb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.persistence.Query;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;

import net.svcret.admin.shared.model.ServiceProtocolEnum;
import net.svcret.admin.shared.model.StatusEnum;
import net.svcret.ejb.api.IDao;
import net.svcret.ejb.api.ResponseTypeEnum;
import net.svcret.ejb.ejb.TransactionLoggerBean.BaseUnflushed;
import net.svcret.ejb.ex.ProcessingException;
import net.svcret.ejb.model.entity.BasePersAuthenticationHost;
import net.svcret.ejb.model.entity.BasePersInvocationStats;
import net.svcret.ejb.model.entity.BasePersMethodStats;
import net.svcret.ejb.model.entity.BasePersRecentMessage;
import net.svcret.ejb.model.entity.BasePersServiceVersion;
import net.svcret.ejb.model.entity.InvocationStatsIntervalEnum;
import net.svcret.ejb.model.entity.PersAuthenticationHostLdap;
import net.svcret.ejb.model.entity.PersAuthenticationHostLocalDatabase;
import net.svcret.ejb.model.entity.PersBaseClientAuth;
import net.svcret.ejb.model.entity.PersBaseServerAuth;
import net.svcret.ejb.model.entity.PersConfig;
import net.svcret.ejb.model.entity.PersDomain;
import net.svcret.ejb.model.entity.PersEnvironment;
import net.svcret.ejb.model.entity.PersHttpClientConfig;
import net.svcret.ejb.model.entity.PersInvocationStats;
import net.svcret.ejb.model.entity.PersInvocationStatsPk;
import net.svcret.ejb.model.entity.PersInvocationUserStats;
import net.svcret.ejb.model.entity.PersInvocationUserStatsPk;
import net.svcret.ejb.model.entity.PersLocks;
import net.svcret.ejb.model.entity.PersService;
import net.svcret.ejb.model.entity.PersServiceVersionMethod;
import net.svcret.ejb.model.entity.PersServiceVersionRecentMessage;
import net.svcret.ejb.model.entity.PersServiceVersionStatus;
import net.svcret.ejb.model.entity.PersServiceVersionUrl;
import net.svcret.ejb.model.entity.PersServiceVersionUrlStatus;
import net.svcret.ejb.model.entity.PersState;
import net.svcret.ejb.model.entity.PersStaticResourceStats;
import net.svcret.ejb.model.entity.PersStaticResourceStatsPk;
import net.svcret.ejb.model.entity.PersUser;
import net.svcret.ejb.model.entity.PersUserContact;
import net.svcret.ejb.model.entity.PersUserRecentMessage;
import net.svcret.ejb.model.entity.PersUserStatus;
import net.svcret.ejb.model.entity.Queries;
import net.svcret.ejb.model.entity.jsonrpc.PersServiceVersionJsonRpc20;
import net.svcret.ejb.model.entity.soap.PersServiceVersionSoap11;
import net.svcret.ejb.util.Validate;

@Stateless
public class DaoBean implements IDao {

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(DaoBean.class);

	@PersistenceContext(name = "ServiceProxy_EJBPU", type = PersistenceContextType.TRANSACTION, unitName = "ServiceProxy_EJBPU")
	private EntityManager myEntityManager;

	private Map<Long, Long> myServiceVersionPidToStatusPid = new HashMap<Long, Long>();

	@Override
	public void deleteAuthenticationHost(BasePersAuthenticationHost theAuthHost) {
		Validate.notNull(theAuthHost, "AuthenticationHost");
		myEntityManager.remove(theAuthHost);
	}

	@Override
	public void deleteHttpClientConfig(PersHttpClientConfig theConfig) {
		Validate.notNull(theConfig, "HttpClientConfig");
		Validate.notNull(theConfig.getPid(), "HttpClientConfig#PID");

		ourLog.info("Deleting HTTP client config {} / {}", theConfig.getPid(), theConfig.getId());

		myEntityManager.remove(theConfig);
	}

	@Override
	public void deleteService(PersService theService) {
		Validate.notNull(theService);
		Validate.notNull(theService.getPid());

		ourLog.info("Deleting service with PID {}", theService.getPid());

		myEntityManager.remove(theService);
	}

	@Override
	public Collection<BasePersAuthenticationHost> getAllAuthenticationHosts() {
		TypedQuery<BasePersAuthenticationHost> q = myEntityManager.createNamedQuery(Queries.AUTHHOST_FINDALL, BasePersAuthenticationHost.class);
		return q.getResultList();
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
	public Collection<PersUser> getAllUsersAndInitializeThem() {
		Query q = myEntityManager.createQuery("SELECT u FROM PersUser u");
		Collection<PersUser> resultList = q.getResultList();
		for (PersUser nextUser : resultList) {
			nextUser.loadAllAssociations();
			if (nextUser.getStatus() == null) {
				PersUserStatus status = new PersUserStatus();
				status.setUser(nextUser);
				status = myEntityManager.merge(status);
				nextUser.setStatus(status);
				nextUser = myEntityManager.merge(nextUser);
			}
		}
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
		Validate.throwProcessingExceptionIfBlank(theModuleId, "ModuleId");

		Query q = myEntityManager.createQuery("SELECT a FROM BasePersAuthenticationHost a WHERE a.myModuleId = :MODULE_ID");
		q.setParameter("MODULE_ID", theModuleId);
		try {
			return (BasePersAuthenticationHost) q.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	@Override
	public BasePersAuthenticationHost getAuthenticationHostByPid(long thePid) {
		return myEntityManager.find(BasePersAuthenticationHost.class, thePid);
	}

	@Override
	public PersConfig getConfigByPid(long thePid) {
		return myEntityManager.find(PersConfig.class, thePid);
	}

	@Override
	public PersDomain getDomainById(String theDomainId) {
		Validate.notBlank(theDomainId, "DomainId");
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
	public PersInvocationStats getInvocationStats(PersInvocationStatsPk thePk) {
		return myEntityManager.find(PersInvocationStats.class, thePk);
	}

	@Override
	public List<PersInvocationStats> getInvocationStatsBefore(InvocationStatsIntervalEnum theHour, Date theDaysCutoff) {
		TypedQuery<PersInvocationStats> q = myEntityManager.createNamedQuery(Queries.PERSINVOC_STATS, PersInvocationStats.class);
		q.setParameter("INTERVAL", theHour);
		q.setParameter("BEFORE_DATE", theDaysCutoff, TemporalType.TIMESTAMP);
		return q.getResultList();
	}

	@Override
	public List<PersInvocationUserStats> getInvocationUserStatsBefore(InvocationStatsIntervalEnum theHour, Date theDaysCutoff) {
		TypedQuery<PersInvocationUserStats> q = myEntityManager.createNamedQuery(Queries.PERSINVOC_USERSTATS, PersInvocationUserStats.class);
		q.setParameter("INTERVAL", theHour);
		q.setParameter("BEFORE_DATE", theDaysCutoff, TemporalType.TIMESTAMP);
		return q.getResultList();
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
		Validate.throwProcessingExceptionIfBlank(theId, "The ID may not be blank");

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
		Validate.throwProcessingExceptionIfBlank(theEnv, "Env");

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
		Validate.notBlank(theId, "Id");

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
	public PersInvocationStats getOrCreateInvocationStats(PersInvocationStatsPk thePk) {
		PersInvocationStats retVal = myEntityManager.find(PersInvocationStats.class, thePk);

		if (retVal == null) {
			retVal = new PersInvocationStats(thePk);
			ourLog.info("Adding new invocation stats: {}", thePk);
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
			ourLog.info("Adding new invocation user stats: {}", thePk);
			retVal = myEntityManager.merge(retVal);
			retVal.setNewlyCreated(true);
		}

		return retVal;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BasePersServiceVersion getOrCreateServiceVersionWithId(PersService theService, String theId, ServiceProtocolEnum theProtocol) throws ProcessingException {
		Validate.notNull(theService, "PersService");
		Validate.throwProcessingExceptionIfBlank(theId, "The ID may not be blank");

		TypedQuery<BasePersServiceVersion> q = myEntityManager.createQuery("SELECT v FROM BasePersServiceVersion v WHERE v.myService.myPid = :SERVICE_PID AND v.myVersionId = :VERSION_ID", BasePersServiceVersion.class);
		q.setParameter("SERVICE_PID", theService.getPid());
		q.setParameter("VERSION_ID", theId);
		BasePersServiceVersion retVal = null;
		try {
			retVal = q.getSingleResult();
		} catch (NoResultException e) {
			ourLog.info("Creating new service version {} for service {}", theId, theService.getServiceId());

			PersHttpClientConfig config = getOrCreateHttpClientConfig(PersHttpClientConfig.DEFAULT_ID);

			switch (theProtocol) {
			case SOAP11:
				retVal = new PersServiceVersionSoap11();
				break;
			case JSONRPC20:
				retVal = new PersServiceVersionJsonRpc20();
				break;
			}

			if (retVal == null) {
				throw new IllegalStateException("Unknown protocol: " + theProtocol);
			}

			retVal.setService(theService);
			retVal.setVersionId(theId);
			retVal.setHttpClientConfig(config);

			// retVal = myEntityManager.merge(retVal);

			theService.addVersion(retVal);
			PersService service = myEntityManager.merge(theService);

			retVal = service.getVersionWithId(theId);

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
		Validate.notNull(theDomain, "PersDomain");
		Validate.throwProcessingExceptionIfBlank(theId, "The ID may not be blank");

		PersService retVal = getServiceById(theDomain.getPid(), theId);
		if (retVal != null) {
			return retVal;
		}

		retVal = new PersService();

		retVal.setServiceId(theId);
		retVal.setDomain(theDomain);

		// retVal = myEntityManager.merge(retVal);
		PersDomain domain = myEntityManager.merge(retVal.getDomain());

		retVal = domain.getServiceWithId(theId);
		retVal.setNewlyCreated(true);
		return retVal;
	}

	@Override
	public PersUser getOrCreateUser(BasePersAuthenticationHost theAuthHost, String theUsername) throws ProcessingException {
		Validate.notNull(theAuthHost, "AuthenticationHost");
		Validate.notBlank(theUsername, "Username");

		Query q = myEntityManager.createNamedQuery(Queries.PERSUSER_FIND);
		q.setParameter("USERNAME", theUsername);
		q.setParameter("AUTH_HOST", theAuthHost);
		PersUser retVal;
		try {
			retVal = (PersUser) q.getSingleResult();
		} catch (NoResultException e) {
			retVal = new PersUser();
			
			PersUserContact contact = new PersUserContact();
			contact = myEntityManager.merge(contact);
			retVal.setContact(contact);
			contact.getUsers().add(retVal);
			
			retVal.setUsername(theUsername);
			retVal.setAuthenticationHost(theAuthHost);
			retVal = myEntityManager.merge(retVal);
			
			PersUserStatus status = new PersUserStatus();
			status.setUser(retVal);
			retVal.setStatus(status);
			
			status = myEntityManager.merge(status);
			retVal.setStatus(status);
			retVal.setNewlyCreated(true);
		}

		return retVal;
	}

	@Override
	public PersService getServiceById(long theDomainPid, String theServiceId) {
		Validate.notBlank(theServiceId, "ServiceId");

		Query q = myEntityManager.createNamedQuery(Queries.SERVICE_FIND);
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
	public BasePersServiceVersion getServiceVersionByPid(long theServiceVersionPid) {
		return myEntityManager.find(BasePersServiceVersion.class, theServiceVersionPid);
	}

	@Override
	public PersServiceVersionMethod getServiceVersionMethodByPid(long theServiceVersionMethodPid) {
		return myEntityManager.find(PersServiceVersionMethod.class, theServiceVersionMethodPid);
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public long getStateCounter(String theKey) {
		Validate.notBlank(theKey, "Key");

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

	@Override
	public PersUser getUser(long thePid) {
		return myEntityManager.find(PersUser.class, thePid);
	}

	@Override
	public long incrementStateCounter(String theKey) {
		Validate.notBlank(theKey, "Key");

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

	@PostConstruct
	public void postConstruct() {
		// Create defaults
		getOrCreateHttpClientConfig(PersHttpClientConfig.DEFAULT_ID);
	}

	@Override
	public void removeDomain(PersDomain theDomain) {
		myEntityManager.remove(theDomain);
	}

	@Override
	public void removeServiceVersion(long thePid) throws ProcessingException {
		Validate.greaterThanZero(thePid, "ServiceVersionPid");

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
	public void saveAuthenticationHost(BasePersAuthenticationHost theAuthHost) {
		Validate.notNull(theAuthHost, "AuthHost");

		ourLog.info("Saving authentication host {} / {}", theAuthHost.getPid(), theAuthHost.getModuleId());

		myEntityManager.merge(theAuthHost);
	}

	@Override
	public PersBaseClientAuth<?> saveClientAuth(PersBaseClientAuth<?> thePers) {
		return myEntityManager.merge(thePers);
	}

	@Override
	public PersConfig saveConfig(PersConfig theConfig) {
		return myEntityManager.merge(theConfig);
	}

	@Override
	public PersDomain saveDomain(PersDomain theDomain) {
		Validate.notNull(theDomain, "Domain");
		Validate.notNull(theDomain.getPid(), "Domain#PID");

		ourLog.info("Saving domain {}", theDomain.getPid());

		return myEntityManager.merge(theDomain);
	}

	@Override
	public PersHttpClientConfig saveHttpClientConfig(PersHttpClientConfig theConfig) {
		Validate.notNull(theConfig, "HttpClientConfig");

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
		List<BasePersMethodStats> emptyList = Collections.emptyList();
		saveInvocationStats(theStats, emptyList);
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@Override
	public void saveUserStatus(Collection<PersUserStatus> theStatus) {
		ourLog.info("Flushing {} user status entries", theStatus.size());
		
		grabLock("FLUSH_STATS");
		
		for (PersUserStatus persUserStatus : theStatus) {
			
			PersUserStatus existing = myEntityManager.find(PersUserStatus.class, persUserStatus.getPid());
			if (existing == null) {
				ourLog.info("No user status with PID {} so not going to save this one", persUserStatus.getPid());
				continue;
			}
			existing.merge(persUserStatus);
			myEntityManager.merge(existing);
		}
	}

	@Override
	public void saveInvocationStats(Collection<BasePersMethodStats> theStats, List<BasePersMethodStats> theStatsToDelete) {
		Validate.notNull(theStats);
		Validate.notNull(theStatsToDelete);

		ourLog.info("Going to save {} invocation stats entries", theStats.size());

		grabLock("FLUSH_STATS");

		ourLog.debug("Got lock on FLUSH_STATS");

		int count = 0;
		int ucount = 0;
		int acount = 0;
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
			} else if (next instanceof PersInvocationStats) {
				PersInvocationStats cNext = (PersInvocationStats) next;
				persisted = getOrCreateInvocationStats(cNext.getPk());
				if (!persisted.isNewlyCreated()) {
					myEntityManager.refresh(persisted);
				}
				persisted.mergeUnsynchronizedEvents(next);
				count++;
			} else if (next instanceof PersStaticResourceStats) {
				PersStaticResourceStats cNext = (PersStaticResourceStats) next;
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

		for (BasePersMethodStats next : theStatsToDelete) {
			myEntityManager.remove(next);
		}

		ourLog.info("Persisted {} invocation status entries, {} user invocation status entries, and {} anonymous status entries", new Object[] { count, ucount, acount });
	}

	@Override
	public PersBaseServerAuth<?, ?> saveServerAuth(PersBaseServerAuth<?, ?> thePers) {
		return myEntityManager.merge(thePers);
	}

	@Override
	public void saveService(PersService theService) {
		Validate.notNull(theService, "Service");
		Validate.notNull(theService.getPid(), "Service#PID");

		ourLog.info("Saving service with PID[{}]", theService.getPid());

		myEntityManager.merge(theService);
	}

	@Override
	public PersUser saveServiceUser(PersUser theUser) {
		Validate.notNull(theUser, "User");
		Validate.notNull(theUser.getAuthenticationHost(), "User.AuthenticationHost");

		return myEntityManager.merge(theUser);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @return
	 */
	@Override
	public BasePersServiceVersion saveServiceVersion(BasePersServiceVersion theVersion) throws ProcessingException {
		Validate.notNull(theVersion, "ServiceVersion");
		Validate.notNull(theVersion.getPid(), "ServiceVersion.myId");
		Validate.throwProcessingExceptionIfBlank(theVersion.getVersionId(), "ID may not be missing or null");

		ourLog.info("Saving service version with PID {}", theVersion.getPid());

		int i = 0;
		for (PersServiceVersionUrl next : theVersion.getUrls()) {
			Validate.throwProcessingExceptionIfBlank(next.getUrlId(), "URL is missing ID");
			next.setOrder(i++);
			next.setServiceVersion(theVersion);
		}

		i = 0;
		for (PersBaseClientAuth<?> next : theVersion.getClientAuths()) {
			next.setOrder(i++);
			next.setServiceVersion(theVersion);
		}

		i = 0;
		for (PersBaseServerAuth<?, ?> next : theVersion.getServerAuths()) {
			next.setOrder(i++);
			next.setServiceVersion(theVersion);
		}

		i = 0;
		for (PersServiceVersionMethod next : theVersion.getMethods()) {
			Validate.throwProcessingExceptionIfBlank(next.getName(), "Method is missing name");
			next.setOrder(i++);
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

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@Override
	public void saveServiceVersionStatuses(ArrayList<PersServiceVersionStatus> theStatuses) {
		Validate.notNull(theStatuses);

		ourLog.info("Going to save {} service version status entries", theStatuses.size());

		grabLock("FLUSH_STATS");
		ourLog.debug("Got lock on FLUSH_STATS");

		for (PersServiceVersionStatus next : theStatuses) {

			PersServiceVersionStatus existing = myEntityManager.find(PersServiceVersionStatus.class, next.getPid());
			if (existing == null) {
				ourLog.warn("Couldn't find Service Version status with PID {} so can't flush it", next.getPid());
				continue;
			}

			existing.merge(next);
			myEntityManager.merge(existing);

		}

		ourLog.info("Done saving {} service version status entries", theStatuses.size());

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

	private PersHttpClientConfig addDefaultHttpClientConfig() {
		PersHttpClientConfig retVal = new PersHttpClientConfig();
		retVal.setDefaults();
		retVal.setId(PersHttpClientConfig.DEFAULT_ID);

		retVal = myEntityManager.merge(retVal);

		return retVal;
	}

	private BasePersMethodStats getOrCreateInvocationStats(PersStaticResourceStatsPk thePk) {
		PersStaticResourceStats retVal = myEntityManager.find(PersStaticResourceStats.class, thePk);

		if (retVal == null) {
			retVal = new PersStaticResourceStats(thePk);
			ourLog.info("Adding new static resource stats: {}", thePk);
			retVal = myEntityManager.merge(retVal);
			retVal.setNewlyCreated(true);
		}

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

	@Override
	public void saveServiceVersionRecentMessage(PersServiceVersionRecentMessage theMsg) {
		Validate.notNull(theMsg);

		myEntityManager.merge(theMsg);
	}

	@Override
	public List<PersServiceVersionRecentMessage> getServiceVersionRecentMessages(BasePersServiceVersion theSvcVer, ResponseTypeEnum theResponseType) {
		TypedQuery<PersServiceVersionRecentMessage> query = myEntityManager.createNamedQuery(Queries.SVCVER_RECENTMSGS, PersServiceVersionRecentMessage.class);
		query.setParameter("SVC_VER", theSvcVer);
		query.setParameter("RESP_TYPE", theResponseType);

		return query.getResultList();
	}

	@Override
	public void trimServiceVersionRecentMessages(BasePersServiceVersion theVersion, ResponseTypeEnum theType, int theNumberToTrimTo) {
		Validate.notNull(theVersion);
		Validate.notNull(theType);
		Validate.notNegative(theNumberToTrimTo);

		long start = 0;
		if (ourLog.isDebugEnabled()) {
			start = System.currentTimeMillis();
		}

		Query query = myEntityManager.createNamedQuery(Queries.SVCVER_RECENTMSGS_COUNT);
		query.setParameter("SVC_VER", theVersion);
		query.setParameter("RESP_TYPE", theType);

		Number num = (Number) query.getSingleResult();
		int toDelete = num.intValue() - theNumberToTrimTo;

		if (ourLog.isDebugEnabled()) {
			ourLog.debug("Counted recent messages of type {} in {}ms", theType, System.currentTimeMillis() - start);
		}

		ourLog.debug("For recent messages of type {} for version {} we have {} entries and want {}", new Object[] { theType, theVersion.getPid(), num, toDelete });
		if (toDelete <= 0) {
			return;
		}

		List<PersServiceVersionRecentMessage> messages = getServiceVersionRecentMessages(theVersion, theType);
		int index = 0;
		for (Iterator<PersServiceVersionRecentMessage> iter = messages.iterator(); iter.hasNext() && index < toDelete; index++) {
			myEntityManager.remove(iter.next());
		}
	}

	@Override
	public void saveUserRecentMessage(PersUserRecentMessage theMsg) {
		Validate.notNull(theMsg);

		myEntityManager.merge(theMsg);
	}

	@Override
	public List<PersUserRecentMessage> getUserRecentMessages(PersUser theUser, ResponseTypeEnum theResponseType) {
		TypedQuery<PersUserRecentMessage> query = myEntityManager.createNamedQuery(Queries.USER_RECENTMSGS, PersUserRecentMessage.class);
		query.setParameter("USER", theUser);
		query.setParameter("RESP_TYPE", theResponseType);

		return query.getResultList();
	}

	@Override
	public void trimUserRecentMessages(PersUser theUser, ResponseTypeEnum theType, int theNumberToTrimTo) {
		Validate.notNull(theUser);
		Validate.notNull(theType);
		Validate.notNegative(theNumberToTrimTo);

		long start = 0;
		if (ourLog.isDebugEnabled()) {
			start = System.currentTimeMillis();
		}

		Query query = myEntityManager.createNamedQuery(Queries.USER_RECENTMSGS_COUNT);
		query.setParameter("USER", theUser);
		query.setParameter("RESP_TYPE", theType);

		Number num = (Number) query.getSingleResult();
		int toDelete = num.intValue() - theNumberToTrimTo;

		if (ourLog.isDebugEnabled()) {
			ourLog.debug("Counted recent messages of type {} in {}ms", theType, System.currentTimeMillis() - start);
		}

		ourLog.debug("For recent messages of type {} for version {} we have {} entries and want {}", new Object[] { theType, theUser.getPid(), num, toDelete });
		if (toDelete <= 0) {
			return;
		}

		List<PersUserRecentMessage> messages = getUserRecentMessages(theUser, theType);
		int index = 0;
		for (Iterator<PersUserRecentMessage> iter = messages.iterator(); iter.hasNext() && index < toDelete; index++) {
			myEntityManager.remove(iter.next());
		}
	}

	@Override
	public BasePersRecentMessage loadRecentMessageForServiceVersion(long thePid) {
		return myEntityManager.find(PersServiceVersionRecentMessage.class, thePid);
	}

	@Override
	public BasePersRecentMessage loadRecentMessageForUser(long thePid) {
		return myEntityManager.find(PersUserRecentMessage.class, thePid);
	}

	@Override
	public void deleteUser(PersUser theUser) {
		Validate.notNull(theUser);
		
		ourLog.info("Deleting user {}", theUser.getPid());
		
		myEntityManager.remove(theUser);
	}

	@Override
	public BasePersInvocationStats getInvocationUserStats(PersInvocationUserStatsPk thePk) {
		return myEntityManager.find(PersInvocationUserStats.class, thePk);
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void saveRecentMessagesAndTrimInNewTransaction(BaseUnflushed<? extends BasePersRecentMessage> theNextTransactions) {
		
		doSaveRecentMessagesAndTrimInNewTransaction(theNextTransactions.getSuccess());
		doSaveRecentMessagesAndTrimInNewTransaction(theNextTransactions.getFail());
		doSaveRecentMessagesAndTrimInNewTransaction(theNextTransactions.getSecurityFail());
		doSaveRecentMessagesAndTrimInNewTransaction(theNextTransactions.getFault());
		
	}

	private void doSaveRecentMessagesAndTrimInNewTransaction(LinkedList<? extends BasePersRecentMessage> transactions) {
		if (transactions.size() > 0) {
			for (BasePersRecentMessage nextRecentMessage : transactions) {
				nextRecentMessage.addUsingDao(this);
			}
			transactions.get(0).trimUsingDao(this);
		}
	}

}