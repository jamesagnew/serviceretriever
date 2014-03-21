package net.svcret.core.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.persistence.Query;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;

import net.svcret.admin.api.ProcessingException;
import net.svcret.admin.api.UnexpectedFailureException;
import net.svcret.admin.shared.enm.InvocationStatsIntervalEnum;
import net.svcret.admin.shared.enm.ResponseTypeEnum;
import net.svcret.admin.shared.enm.ServerSecurityModeEnum;
import net.svcret.admin.shared.model.IThrottleable;
import net.svcret.admin.shared.model.ServiceProtocolEnum;
import net.svcret.admin.shared.model.StatusEnum;
import net.svcret.admin.shared.util.IntegerHolder;
import net.svcret.admin.shared.util.Validate;
import net.svcret.core.api.IDao;
import net.svcret.core.api.StatusesBean;
import net.svcret.core.log.BaseUnflushed;
import net.svcret.core.model.entity.BasePersAuthenticationHost;
import net.svcret.core.model.entity.BasePersMonitorRule;
import net.svcret.core.model.entity.BasePersSavedTransactionRecentMessage;
import net.svcret.core.model.entity.BasePersServiceCatalogItem;
import net.svcret.core.model.entity.BasePersServiceVersion;
import net.svcret.core.model.entity.BasePersStats;
import net.svcret.core.model.entity.BasePersStats.IStatsVisitor;
import net.svcret.core.model.entity.BasePersStatsPk;
import net.svcret.core.model.entity.PersAuthenticationHostLdap;
import net.svcret.core.model.entity.PersAuthenticationHostLocalDatabase;
import net.svcret.core.model.entity.PersBaseClientAuth;
import net.svcret.core.model.entity.PersBaseServerAuth;
import net.svcret.core.model.entity.PersConfig;
import net.svcret.core.model.entity.PersDomain;
import net.svcret.core.model.entity.PersEnvironment;
import net.svcret.core.model.entity.PersHttpClientConfig;
import net.svcret.core.model.entity.PersInvocationMethodSvcverStats;
import net.svcret.core.model.entity.PersInvocationMethodSvcverStatsPk;
import net.svcret.core.model.entity.PersInvocationMethodUserStats;
import net.svcret.core.model.entity.PersInvocationMethodUserStatsPk;
import net.svcret.core.model.entity.PersInvocationUrlStats;
import net.svcret.core.model.entity.PersInvocationUrlStatsPk;
import net.svcret.core.model.entity.PersLibraryMessage;
import net.svcret.core.model.entity.PersLibraryMessageAppliesTo;
import net.svcret.core.model.entity.PersMethod;
import net.svcret.core.model.entity.PersMethodStatus;
import net.svcret.core.model.entity.PersMethodStatusPk;
import net.svcret.core.model.entity.PersMonitorAppliesTo;
import net.svcret.core.model.entity.PersMonitorRuleActive;
import net.svcret.core.model.entity.PersMonitorRuleActiveCheck;
import net.svcret.core.model.entity.PersMonitorRuleActiveCheckOutcome;
import net.svcret.core.model.entity.PersMonitorRuleFiring;
import net.svcret.core.model.entity.PersMonitorRuleFiringProblem;
import net.svcret.core.model.entity.PersMonitorRuleNotifyContact;
import net.svcret.core.model.entity.PersMonitorRulePassive;
import net.svcret.core.model.entity.PersNodeStats;
import net.svcret.core.model.entity.PersNodeStatsPk;
import net.svcret.core.model.entity.PersNodeStatus;
import net.svcret.core.model.entity.PersService;
import net.svcret.core.model.entity.PersServiceVersionRecentMessage;
import net.svcret.core.model.entity.PersServiceVersionStatus;
import net.svcret.core.model.entity.PersServiceVersionUrl;
import net.svcret.core.model.entity.PersServiceVersionUrlStatus;
import net.svcret.core.model.entity.PersState;
import net.svcret.core.model.entity.PersStaticResourceStats;
import net.svcret.core.model.entity.PersStaticResourceStatsPk;
import net.svcret.core.model.entity.PersStickySessionUrlBinding;
import net.svcret.core.model.entity.PersStickySessionUrlBindingPk;
import net.svcret.core.model.entity.PersUser;
import net.svcret.core.model.entity.PersUserAllowableSourceIps;
import net.svcret.core.model.entity.PersUserContact;
import net.svcret.core.model.entity.PersUserRecentMessage;
import net.svcret.core.model.entity.PersUserServiceVersionMethodPermission;
import net.svcret.core.model.entity.PersUserStatus;
import net.svcret.core.model.entity.Queries;
import net.svcret.core.model.entity.crud.PersServiceVersionRest;
import net.svcret.core.model.entity.hl7.PersServiceVersionHl7OverHttp;
import net.svcret.core.model.entity.jsonrpc.PersServiceVersionJsonRpc20;
import net.svcret.core.model.entity.soap.PersServiceVersionSoap11;
import net.svcret.core.model.entity.virtual.PersServiceVersionVirtual;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import com.google.common.annotations.VisibleForTesting;

@Service
public class DaoBean implements IDao {

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(DaoBean.class);

	@PersistenceContext(name = "ServiceProxy_EJBPU", type = PersistenceContextType.TRANSACTION, unitName = "ServiceProxy_EJBPU")
	private EntityManager myEntityManager;

	private Map<Long, Long> myServiceVersionPidToStatusPid = new HashMap<>();

	@Autowired
	protected PlatformTransactionManager myPlatformTransactionManager;

	private TransactionTemplate myTransactionTemplate;

	@PostConstruct
	public void postConstruct() {
		myTransactionTemplate = new TransactionTemplate(myPlatformTransactionManager);
		myTransactionTemplate.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				getHttpClientConfigs();
			}
		});
	}

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
	public void deleteMonitorRuleActiveCheckOutcomesBeforeCutoff(PersMonitorRuleActiveCheck theCheck, Date theCutoff) {
		Validate.notNull(theCheck);
		Validate.notNull(theCutoff);

		Query q = myEntityManager.createNamedQuery(Queries.PMRACO_DELETEBEFORE);
		q.setParameter("CHECK", theCheck);
		q.setParameter("CUTOFF", theCutoff, TemporalType.TIMESTAMP);

		int results = q.executeUpdate();
		ourLog.debug("Deleted {} active check outcomes for check {}", results, theCheck.getPid());
	}

	@Override
	public void deleteService(PersService theService) {
		Validate.notNull(theService);
		Validate.notNull(theService.getPid());

		ourLog.info("Deleting service with PID {}", theService.getPid());

		myEntityManager.remove(theService);
	}

	@Override
	public void deleteServiceVersion(BasePersServiceVersion theSv) {
		BasePersServiceVersion sv = myEntityManager.find(BasePersServiceVersion.class, theSv.getPid());
		myEntityManager.remove(sv);

		PersService svc = sv.getService();
		svc.removeVersion(sv);
		myEntityManager.merge(svc);

	}

	@Override
	public void deleteStickySession(PersStickySessionUrlBinding theStickySession) {
		myEntityManager.remove(theStickySession);
	}

	@Override
	public void deleteUser(PersUser theUser) {
		Validate.notNull(theUser);

		ourLog.info("Deleting user {}", theUser.getPid());

		myEntityManager.remove(theUser);
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

	@Override
	public Collection<PersMethodStatus> getAllMethodStatus() {
		TypedQuery<PersMethodStatus> q = myEntityManager.createNamedQuery(Queries.METHODSTATUS_FINDALL, PersMethodStatus.class);
		Collection<PersMethodStatus> retVal = q.getResultList();
		return retVal;
	}

	@Override
	public Collection<PersMonitorRuleActiveCheck> getAllMonitorRuleActiveChecks() {
		return myEntityManager.createNamedQuery(Queries.PERSACTIVECHECK_FINDALL, PersMonitorRuleActiveCheck.class).getResultList();
	}

	@Override
	public List<PersMonitorRuleFiring> getAllMonitorRuleFiringsWhichAreActive() {
		TypedQuery<PersMonitorRuleFiring> q = myEntityManager.createNamedQuery(Queries.RULEFIRING_FINDACTIVE, PersMonitorRuleFiring.class);
		q.setParameter("NULLDATE", PersMonitorRuleFiring.NULL_DATE);
		List<PersMonitorRuleFiring> activeRuleFailures = q.getResultList();
		return activeRuleFailures;
	}

	@Override
	public Collection<PersNodeStatus> getAllNodeStatuses() {
		return myEntityManager.createNamedQuery(Queries.NODESTATUS_FINDALL, PersNodeStatus.class).getResultList();
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
	public Collection<PersServiceVersionSoap11> getAllServiceVersions() {
		Query q = myEntityManager.createQuery("SELECT v FROM PersServiceVersionSoap11 v");
		Collection<PersServiceVersionSoap11> resultList = q.getResultList();
		return resultList;
	}

	@Override
	public Collection<PersStickySessionUrlBinding> getAllStickySessions() {
		TypedQuery<PersStickySessionUrlBinding> q = myEntityManager.createNamedQuery(Queries.SSURL_FINDALL, PersStickySessionUrlBinding.class);
		return q.getResultList();
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
	@SuppressWarnings({ "cast", "unchecked" })
	public <P extends BasePersStatsPk<P, O>, O extends BasePersStats<P, O>> O getInvocationStats(P thePkToVisit) {

		return (O) thePkToVisit.accept(new IStatsVisitor<O>() {
			@Override
			public O visit(PersInvocationMethodSvcverStats theStats, PersInvocationMethodSvcverStatsPk thePk) {
				return doVisit((P) thePk);
			}

			@Override
			public O visit(PersInvocationMethodUserStats theStats, PersInvocationMethodUserStatsPk thePk) {
				return doVisit((P) thePk);
			}

			@Override
			public O visit(PersInvocationUrlStats theStats, PersInvocationUrlStatsPk thePk) {
				return doVisit((P) thePk);
			}

			@Override
			public O visit(PersNodeStats theStats, PersNodeStatsPk thePk) {
				return doVisit((P) thePk);
			}

			@Override
			public O visit(PersStaticResourceStats theStats, PersStaticResourceStatsPk thePk) {
				return doVisit((P) thePk);
			}

			private O doVisit(P thePk) {
				return (O) myEntityManager.find(thePk.getStatType(), thePk);
			}
		});
	}

	@Override
	public List<PersInvocationMethodSvcverStats> getInvocationStatsBefore(InvocationStatsIntervalEnum theHour, Date theDaysCutoff) {
		TypedQuery<PersInvocationMethodSvcverStats> q = myEntityManager.createNamedQuery(Queries.PERSINVOC_STATS, PersInvocationMethodSvcverStats.class);
		q.setParameter("INTERVAL", theHour);
		q.setParameter("BEFORE_DATE", theDaysCutoff, TemporalType.TIMESTAMP);

		List<PersInvocationMethodSvcverStats> resultList = q.getResultList();
		ourLog.debug("Querying for invocation stats with interval {} before start date {} and found {}", new Object[] { theHour, theDaysCutoff, resultList.size() });
		return resultList;
	}

	@Override
	public List<PersInvocationUrlStats> getInvocationUrlStatsBefore(InvocationStatsIntervalEnum theInterval, Date theCutoff) {
		TypedQuery<PersInvocationUrlStats> q = myEntityManager.createNamedQuery(Queries.PERSINVOC_URLSTATS_FINDINTERVAL, PersInvocationUrlStats.class);
		q.setParameter("INTERVAL", theInterval);
		q.setParameter("BEFORE_DATE", theCutoff, TemporalType.TIMESTAMP);
		return q.getResultList();
	}

	@Override
	public List<PersInvocationMethodUserStats> getInvocationUserStatsBefore(InvocationStatsIntervalEnum theHour, Date theDaysCutoff) {
		TypedQuery<PersInvocationMethodUserStats> q = myEntityManager.createNamedQuery(Queries.PERSINVOC_USERSTATS_FINDINTERVAL, PersInvocationMethodUserStats.class);
		q.setParameter("INTERVAL", theHour);
		q.setParameter("BEFORE_DATE", theDaysCutoff, TemporalType.TIMESTAMP);
		return q.getResultList();
	}

	@Override
	public PersLibraryMessage getLibraryMessageByPid(long theMessagePid) {
		return myEntityManager.find(PersLibraryMessage.class, theMessagePid);
	}

	@Override
	public Collection<PersLibraryMessage> getLibraryMessagesWhichApplyToDomain(long thePid) {
		PersDomain domain = myEntityManager.find(PersDomain.class, thePid);
		if (domain == null) {
			throw new IllegalArgumentException("Unknown domain: " + thePid);
		}

		TypedQuery<PersLibraryMessage> query = myEntityManager.createNamedQuery(Queries.LIBRARY_FINDBYDOMAIN, PersLibraryMessage.class);
		query.setParameter("DOMAIN", domain);

		List<PersLibraryMessage> retVal = query.getResultList();

		return new HashSet<>(retVal);
	}

	@Override
	public Collection<PersLibraryMessage> getLibraryMessagesWhichApplyToService(long thePid) {
		PersService svc = myEntityManager.find(PersService.class, thePid);
		if (svc == null) {
			throw new IllegalArgumentException("Unknown service: " + thePid);
		}

		TypedQuery<PersLibraryMessage> query = myEntityManager.createNamedQuery(Queries.LIBRARY_FINDBYSVC, PersLibraryMessage.class);
		query.setParameter("SVC", svc);

		List<PersLibraryMessage> retVal = query.getResultList();

		return new HashSet<>(retVal);
	}

	@Override
	public Collection<PersLibraryMessage> getLibraryMessagesWhichApplyToServiceVersion(long theServiceVersionPid) {

		BasePersServiceVersion svcVer = myEntityManager.find(BasePersServiceVersion.class, theServiceVersionPid);
		if (svcVer == null) {
			throw new IllegalArgumentException("Unknown service version: " + theServiceVersionPid);
		}

		TypedQuery<PersLibraryMessage> query = myEntityManager.createNamedQuery(Queries.LIBRARY_FINDBYSVCVER, PersLibraryMessage.class);
		query.setParameter("SVC_VERS", svcVer);

		return query.getResultList();
	}

	@Override
	public BasePersMonitorRule getMonitorRule(long thePid) {
		return myEntityManager.find(BasePersMonitorRule.class, thePid);
	}

	@Override
	public PersMonitorRuleActiveCheck getMonitorRuleActiveCheck(long thePid) {
		return myEntityManager.find(PersMonitorRuleActiveCheck.class, thePid);
	}

	@Override
	public List<BasePersMonitorRule> getMonitorRules() {
		TypedQuery<BasePersMonitorRule> q = myEntityManager.createNamedQuery(Queries.MONITORRULE_FINDALL, BasePersMonitorRule.class);
		return q.getResultList();
	}

	@Override
	public List<PersNodeStats> getNodeStatsBefore(InvocationStatsIntervalEnum theInterval, Date theCutoff) {
		TypedQuery<PersNodeStats> q = myEntityManager.createNamedQuery(Queries.PERS_NODESTATS_FINDINTERVAL, PersNodeStats.class);
		q.setParameter("INTERVAL", theInterval);
		q.setParameter("BEFORE_DATE", theCutoff, TemporalType.TIMESTAMP);
		return q.getResultList();
	}

	@Override
	public List<PersNodeStats> getNodeStatsWithinRange(Date theStartInclusive, Date theEndInclusive) {
		TypedQuery<PersNodeStats> q = myEntityManager.createNamedQuery(Queries.PERS_NODESTATS_FINDRANGE, PersNodeStats.class);
		q.setParameter("START_TIME", theStartInclusive, TemporalType.TIMESTAMP);
		q.setParameter("END_TIME", theEndInclusive, TemporalType.TIMESTAMP);
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
			myEntityManager.persist(retVal);
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
			myEntityManager.persist(retVal);
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
			myEntityManager.persist(retVal);
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

	@Transactional(propagation = Propagation.REQUIRED)
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
			myEntityManager.persist(c);
			return c;

		}
	}

	@Override
	@Transactional(propagation=Propagation.REQUIRES_NEW)
	public PersNodeStatus getOrCreateNodeStatusInNewTransaction(String theNodeId) {
		PersNodeStatus retVal = myEntityManager.find(PersNodeStatus.class, theNodeId);
		if (retVal == null) {
			retVal = new PersNodeStatus();
			retVal.setNodeId(theNodeId);
			retVal.setStatusTimestamp(new Date());
			myEntityManager.merge(retVal);
		}
		return retVal;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BasePersServiceVersion getOrCreateServiceVersionWithId(PersService theService, String theVersionId, ServiceProtocolEnum theProtocol) {
		BasePersServiceVersion retVal = null;
		switch (theProtocol) {
		case SOAP11:
			retVal = new PersServiceVersionSoap11();
			break;
		case JSONRPC20:
			retVal = new PersServiceVersionJsonRpc20();
			break;
		case HL7OVERHTTP:
			retVal = new PersServiceVersionHl7OverHttp();
			break;
		case VIRTUAL:
			retVal = new PersServiceVersionVirtual();
			break;
		case REST:
			retVal = new PersServiceVersionRest();
			break;
		}

		if (retVal == null) {
			throw new IllegalStateException("Unknown protocol: " + theProtocol);
		}

		retVal = getOrCreateServiceVersionWithId(theService, theVersionId, theProtocol, retVal);

		return retVal;
	}

	@Override
	public BasePersServiceVersion getOrCreateServiceVersionWithId(PersService theService, String theVersionId, ServiceProtocolEnum theProtocol, BasePersServiceVersion theSvcVerToUseIfCreatingNew) {
		Validate.notNull(theService, "PersService");
		Validate.notBlank(theVersionId, "The ID may not be blank");

		TypedQuery<BasePersServiceVersion> q = myEntityManager.createQuery("SELECT v FROM BasePersServiceVersion v WHERE v.myService.myPid = :SERVICE_PID AND v.myVersionId = :VERSION_ID", BasePersServiceVersion.class);
		q.setParameter("SERVICE_PID", theService.getPid());
		q.setParameter("VERSION_ID", theVersionId);
		BasePersServiceVersion retVal = null;
		try {
			retVal = q.getSingleResult();
		} catch (NoResultException e) {
			ourLog.info("Creating new service version {} for service {}", theVersionId, theService.getServiceId());

			PersHttpClientConfig config = getOrCreateHttpClientConfig(PersHttpClientConfig.DEFAULT_ID);

			retVal = theSvcVerToUseIfCreatingNew;
			retVal.prePersist();

			retVal.setService(theService);
			retVal.setVersionId(theVersionId);
			retVal.setHttpClientConfig(config);
			retVal.setServerSecurityMode(ServerSecurityModeEnum.NONE);

			// retVal = myEntityManager.merge(retVal);

			theService.addVersion(retVal);
			PersService service = myEntityManager.merge(theService);

			retVal = service.getVersionWithId(theVersionId);

			// Create a status entry

			PersServiceVersionStatus status = retVal.getStatus();
			status.setServiceVersion(retVal);
			PersServiceVersionStatus newStatus = myEntityManager.merge(status);
			retVal.setStatus(newStatus);

			retVal.setNewlyCreated(true);
		}

		return myEntityManager.find(BasePersServiceVersion.class, retVal.getPid());
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
		retVal.setServiceName(theId);

		retVal.setDomain(theDomain);

		// retVal = myEntityManager.merge(retVal);
		PersDomain domain = myEntityManager.merge(retVal.getDomain());

		retVal = domain.getServiceWithId(theId);
		retVal.setNewlyCreated(true);
		return retVal;
	}

	@Override
	public <P extends BasePersStatsPk<P, O>, O extends BasePersStats<P, O>> O getOrCreateStats(P thePk) {

		O retVal = myEntityManager.find(thePk.getStatType(), thePk);

		if (retVal == null) {
			retVal = thePk.newObjectInstance();
			ourLog.debug("Adding new stats: {}", thePk);
			retVal = myEntityManager.merge(retVal);
			retVal.setNewlyCreated(true);
		}

		return retVal;
	}

	@Override
	public PersStickySessionUrlBinding createOrUpdateExistingStickySessionUrlBindingInNewTransaction(final PersStickySessionUrlBinding theBinding) throws UnexpectedFailureException {
		for (int i = 0; true; i++) {
			try {
				return myTransactionTemplate.execute(new TransactionCallback<PersStickySessionUrlBinding>() {
					@Override
					public PersStickySessionUrlBinding doInTransaction(TransactionStatus theStatus) {
						return getOrCreateStickySessionUrlBindingInNewTransaction(theBinding);
					}
				});
			} catch (Exception e) {
				if (i < 5) {
					ourLog.debug("Failed to create sticky session, will sleep and try again");
					try {
						Thread.sleep(500);
					} catch (InterruptedException e1) {
						// ignore
					}
					continue;
				} else {
					throw new UnexpectedFailureException(e);
				}
			}
		}
	}

	private PersStickySessionUrlBinding getOrCreateStickySessionUrlBindingInNewTransaction(PersStickySessionUrlBinding theBinding) {

		// SvcVer probably comes from the service registry so it needs to be
		// refreshed
		BasePersServiceVersion svcVer = myEntityManager.find(BasePersServiceVersion.class, theBinding.getPk().getServiceVersion().getPid());
		PersStickySessionUrlBindingPk pk = new PersStickySessionUrlBindingPk(theBinding.getPk().getSessionId(), svcVer);

		PersStickySessionUrlBinding retVal = myEntityManager.find(PersStickySessionUrlBinding.class, pk);
		if (retVal == null) {
			ourLog.debug("Creating new sticky session with ID '{}' for URL {}", pk.getSessionId(), theBinding.getUrl().getPid());
			myEntityManager.persist(theBinding);
			theBinding.setNewlyCreated(true);
			retVal=theBinding;
		}else {
			retVal.merge(theBinding);
		}
		
		return retVal;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public PersUser getOrCreateUser(BasePersAuthenticationHost theAuthHost, String theUsername) {
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
			retVal.getAllowSourceIps();

			retVal.setUsername(theUsername);
			retVal.setAuthenticationHost(theAuthHost);
			myEntityManager.persist(retVal);

			// Status

			PersUserStatus status = new PersUserStatus();
			status.setUser(retVal);
			retVal.setStatus(status);

			myEntityManager.persist(status);
			retVal.setStatus(status);
			retVal.setNewlyCreated(true);

			// Contact

			PersUserContact contact = new PersUserContact();
			myEntityManager.persist(contact);
			retVal.setContact(contact);
			contact.getUsers().add(retVal);

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
	public PersMethod getServiceVersionMethodByPid(long theServiceVersionMethodPid) {
		return myEntityManager.find(PersMethod.class, theServiceVersionMethodPid);
	}

	@Override
	public List<PersServiceVersionRecentMessage> getServiceVersionRecentMessages(BasePersServiceVersion theSvcVer, ResponseTypeEnum theResponseType) {
		TypedQuery<PersServiceVersionRecentMessage> query = myEntityManager.createNamedQuery(Queries.SVCVER_RECENTMSGS, PersServiceVersionRecentMessage.class);
		query.setParameter("SVC_VER", theSvcVer);
		query.setParameter("RESP_TYPE", theResponseType);

		return query.getResultList();
	}

	@Override
	public PersServiceVersionUrl getServiceVersionUrlByPid(long theUrlPid) {
		return myEntityManager.find(PersServiceVersionUrl.class, theUrlPid);
	}

	@Override
	public PersServiceVersionUrlStatus getServiceVersionUrlStatusByPid(Long thePid) {
		return myEntityManager.find(PersServiceVersionUrlStatus.class, thePid);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
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
	public List<PersUserRecentMessage> getUserRecentMessages(IThrottleable theUser, ResponseTypeEnum theResponseType) {
		TypedQuery<PersUserRecentMessage> query = myEntityManager.createNamedQuery(Queries.USER_RECENTMSGS, PersUserRecentMessage.class);
		query.setParameter("USER", theUser);
		query.setParameter("RESP_TYPE", theResponseType);

		return query.getResultList();
	}

	@Override
	public List<PersInvocationMethodUserStats> getUserStatsWithinTimeRange(PersUser theUser, Date theStart, Date theEnd) {
		Validate.notNull(theUser);
		Validate.notNull(theStart);
		Validate.notNull(theEnd);
		if (theEnd.before(theStart)) {
			throw new IllegalArgumentException();
		}

		TypedQuery<PersInvocationMethodUserStats> query = myEntityManager.createNamedQuery(Queries.PERSINVOC_USERSTATS_FINDUSER, PersInvocationMethodUserStats.class);
		query.setParameter("USER_PID", theUser.getPid());
		query.setParameter("START_TIME", theStart);
		query.setParameter("END_TIME", theEnd);

		return query.getResultList();
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
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

	@Override
	public StatusesBean loadAllStatuses(PersConfig theConfig) {
		StatusesBean statusesBean = new StatusesBean(theConfig);

		List<PersServiceVersionUrlStatus> urlStatuses = myEntityManager.createQuery("SELECT c FROM " + PersServiceVersionUrlStatus.class.getSimpleName() + " c", PersServiceVersionUrlStatus.class).getResultList();
		for (PersServiceVersionUrlStatus next : urlStatuses) {
			statusesBean.getUrlPidToStatus().put(next.getUrlPid(), next);
		}

		List<PersServiceVersionStatus> verStatuses = myEntityManager.createQuery("SELECT c FROM " + PersServiceVersionStatus.class.getSimpleName() + " c", PersServiceVersionStatus.class).getResultList();
		for (PersServiceVersionStatus next : verStatuses) {
			statusesBean.getServiceVersionPidToStatus().put(next.getServiceVersionPid(), next);
		}

		for (PersMethodStatus next : myEntityManager.createQuery("SELECT c FROM " + PersMethodStatus.class.getSimpleName() + " c", PersMethodStatus.class).getResultList()) {
			statusesBean.getMethodPidToStatus().put(next.getMethod().getPid(), next);
		}

		List<PersMonitorRuleFiring> activeRuleFailures = getAllMonitorRuleFiringsWhichAreActive();
		for (PersMonitorRuleFiring next : activeRuleFailures) {
			statusesBean.addActiveRuleFiring(next);
		}

		return statusesBean;
	}

	@Override
	public List<PersLibraryMessage> loadLibraryMessages() {
		return myEntityManager.createNamedQuery(Queries.LIBRARY_FINDALL, PersLibraryMessage.class).getResultList();
	}

	@Override
	public PersMonitorRuleActiveCheckOutcome loadMonitorRuleActiveCheckOutcome(long thePid) {
		return myEntityManager.find(PersMonitorRuleActiveCheckOutcome.class, thePid);
	}

	@Override
	public List<PersMonitorRuleFiring> loadMonitorRuleFirings(Set<? extends BasePersServiceVersion> theAllSvcVers, int theStart) {

		TypedQuery<PersMonitorRuleFiring> q = myEntityManager.createNamedQuery(Queries.RULEFIRING, PersMonitorRuleFiring.class);
		q.setParameter("SVC_VERS", theAllSvcVers);
		q.setFirstResult(theStart);
		q.setMaxResults(10);

		return q.getResultList();
	}

	@Override
	public PersServiceVersionRecentMessage loadRecentMessageForServiceVersion(long thePid) {
		return myEntityManager.find(PersServiceVersionRecentMessage.class, thePid);
	}

	@Override
	public PersUserRecentMessage loadRecentMessageForUser(long thePid) {
		return myEntityManager.find(PersUserRecentMessage.class, thePid);
	}

	@Override
	public void deleteDomain(PersDomain theDomain) {
		for (BasePersServiceVersion nextSv : theDomain.getAllServiceVersions()) {
			for (PersMethod nextMethod : nextSv.getMethods()) {
				for (PersUserServiceVersionMethodPermission nextMethodPerm : nextMethod.getUserPermissions()) {
					myEntityManager.remove(nextMethodPerm);
				}
			}
		}

		myEntityManager.remove(theDomain);
	}

	@Override
	public BasePersAuthenticationHost saveAuthenticationHost(BasePersAuthenticationHost theAuthHost) {
		Validate.notNull(theAuthHost, "AuthHost");

		ourLog.info("Saving authentication host {} / {}", theAuthHost.getPid(), theAuthHost.getModuleId());

		if (theAuthHost.getPid() == null) {
			myEntityManager.persist(theAuthHost);
			return theAuthHost;
		} else {
			return myEntityManager.merge(theAuthHost);
		}
	}

	@Override
	public PersBaseClientAuth<?> saveClientAuth(PersBaseClientAuth<?> thePers) {
		return myEntityManager.merge(thePers);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@Override
	public PersConfig saveConfigInNewTransaction(PersConfig theConfig) {
		return myEntityManager.merge(theConfig);
	}

	@Override
	public PersDomain saveDomain(PersDomain theDomain) {
		Validate.notNull(theDomain, "Domain");

		ourLog.info("Saving domain {}", theDomain.getPid());

		if (theDomain.getPid() == null) {
			myEntityManager.persist(theDomain);
			return theDomain;
		}
		return myEntityManager.merge(theDomain);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public PersHttpClientConfig saveHttpClientConfigInNewTransaction(PersHttpClientConfig theConfig) {
		Validate.notNull(theConfig, "HttpClientConfig");

		boolean isNew = false;
		if (theConfig.getPid() == null) {
			isNew = true;
			ourLog.info("Creating HTTP client config {}", theConfig.getPid(), theConfig.getId());
		} else {
			ourLog.info("Saving HTTP client config {} / {}", theConfig.getPid(), theConfig.getId());
		}

		if (StringUtils.isNotBlank(theConfig.getStickySessionCookieForSessionId()) && StringUtils.isNotBlank(theConfig.getStickySessionHeaderForSessionId())) {
			throw new IllegalArgumentException("Must not provide both a sticky session cookie and a sticky session header value");
		}

		PersHttpClientConfig retVal = myEntityManager.merge(theConfig);

		if (isNew) {
			ourLog.info("Done creating HTTP client config {}, assigned PID {}", theConfig.getId(), theConfig.getPid());
		}

		return retVal;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void saveStatsInNewTransaction(Collection<? extends BasePersStats<?, ?>> theStats) {
		List<? extends BasePersStats<?, ?>> emptyList = Collections.emptyList();
		saveStatsInNewTransaction(theStats, emptyList);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void saveStatsInNewTransaction(Collection<? extends BasePersStats<?, ?>> theStatsToSave, List<? extends BasePersStats<?, ?>> theStatsToDelete) {
		Validate.notNull(theStatsToSave);
		Validate.notNull(theStatsToDelete);

		ourLog.info("Going to save {} invocation stats entries and delete {} entries", theStatsToSave.size(), theStatsToDelete.size());

		ourLog.debug("Got lock on FLUSH_STATS");

		final IntegerHolder count = new IntegerHolder();
		final IntegerHolder ucount = new IntegerHolder();
		IntegerHolder acount = new IntegerHolder();
		for (Iterator<? extends BasePersStats<?, ?>> iter = theStatsToSave.iterator(); iter.hasNext();) {
			BasePersStats<?, ?> nextToMerge = iter.next();
			final BasePersStats<?, ?> existingPersisted = nextToMerge.accept(new IStatsVisitor<BasePersStats<?, ?>>() {
				@Override
				public BasePersStats<?, ?> visit(PersInvocationMethodSvcverStats theStats, PersInvocationMethodSvcverStatsPk thePk) {
					count.increment();
					PersInvocationMethodSvcverStats retVal = getOrCreateStats(thePk);
					retVal.mergeUnsynchronizedEvents(theStats);
					return retVal;
				}

				@Override
				public BasePersStats<?, ?> visit(PersInvocationMethodUserStats theStats, PersInvocationMethodUserStatsPk thePk) {
					ucount.increment();
					PersInvocationMethodUserStats retVal = getOrCreateStats(thePk);
					retVal.mergeUnsynchronizedEvents(theStats);
					return retVal;
				}

				@Override
				public BasePersStats<?, ?> visit(PersInvocationUrlStats theStats, PersInvocationUrlStatsPk thePk) {
					count.increment();
					PersInvocationUrlStats retVal = getOrCreateStats(thePk);
					retVal.mergeUnsynchronizedEvents(theStats);
					return retVal;
				}

				@Override
				public BasePersStats<?, ?> visit(PersNodeStats theStats, PersNodeStatsPk thePk) {
					count.increment();
					PersNodeStats retVal = getOrCreateStats(thePk);
					retVal.mergeUnsynchronizedEvents(theStats);
					return retVal;
				}

				@Override
				public BasePersStats<?, ?> visit(PersStaticResourceStats theStats, PersStaticResourceStatsPk thePk) {
					count.increment();
					PersStaticResourceStats retVal = getOrCreateStats(thePk);
					retVal.mergeUnsynchronizedEvents(theStats);
					return retVal;
				}
			});

			ourLog.debug("Merging stats entry: {}", existingPersisted);
			myEntityManager.merge(existingPersisted);
		}

		for (BasePersStats<?, ?> next : theStatsToDelete) {
			ourLog.debug("Removing stats entry: {}", next);

			next = myEntityManager.find(next.getClass(), next.getPk());
			if (next != null) {
				myEntityManager.remove(next);
			}
		}

		ourLog.info("Persisted {} invocation status entries, {} user invocation status entries, and {} anonymous status entries", new Object[] { count, ucount, acount });
	}

	@Override
	public PersLibraryMessage saveLibraryMessage(PersLibraryMessage theMessage) {
		Validate.notNull(theMessage);
		if (theMessage.getPid() == null) {
			myEntityManager.persist(theMessage);
			myEntityManager.flush();
			for (PersLibraryMessageAppliesTo next : theMessage.getAppliesTo()) {
				myEntityManager.persist(next);
			}
			return theMessage;
		} else {

			PersLibraryMessage existing = myEntityManager.find(PersLibraryMessage.class, theMessage.getPid());
			for (PersLibraryMessageAppliesTo next : existing.getAppliesTo()) {
				if (!theMessage.getAppliesTo().contains(next)) {
					myEntityManager.remove(next);
				}
			}
			for (PersLibraryMessageAppliesTo next : theMessage.getAppliesTo()) {
				PersLibraryMessageAppliesTo existingAt = myEntityManager.find(PersLibraryMessageAppliesTo.class, next.getPk());
				if (existingAt == null) {
					myEntityManager.persist(next);
				}
			}

			return myEntityManager.merge(theMessage);
		}
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void saveMethodStatuses(List<PersMethodStatus> theMethodStatuses) {
		for (PersMethodStatus next : theMethodStatuses) {
//			PersMethodStatus existing = myEntityManager.find(PersMethodStatus.class, new PersMethodStatusPk(next.getMethod()));
			PersMethodStatus existing = myEntityManager.find(PersMethodStatus.class, next.getMethod().getPid());
			if (existing == null) {
				myEntityManager.persist(next);
			} else {
				existing.merge(next);
				myEntityManager.merge(existing);
			}
		}
	}

	@Override
	public PersMonitorRuleActiveCheckOutcome saveMonitorRuleActiveCheckOutcome(PersMonitorRuleActiveCheckOutcome theRecentOutcome) {
		return myEntityManager.merge(theRecentOutcome);
	}

	@Override
	public PersMonitorRuleFiring saveMonitorRuleFiring(PersMonitorRuleFiring theFiring) {

		PersMonitorRuleFiring firing = myEntityManager.merge(theFiring);

		List<PersMonitorRuleFiringProblem> newProblems = new ArrayList<>();
		for (PersMonitorRuleFiringProblem next : theFiring.getProblems()) {
			next.setFiring(firing);
			PersMonitorRuleFiringProblem newProblem = myEntityManager.merge(next);
			newProblems.add(newProblem);
		}

		firing.getProblems().clear();
		firing.getProblems().addAll(newProblems);

		return firing;
	}

	@Override
	public BasePersMonitorRule saveMonitorRuleInNewTransaction(BasePersMonitorRule theRule) {

		for (PersMonitorRuleNotifyContact next : theRule.getNotifyContact()) {
			next.setRule(theRule);
		}

		if (theRule instanceof PersMonitorRuleActive) {
			for (PersMonitorRuleActiveCheck next : ((PersMonitorRuleActive) theRule).getActiveChecks()) {
				next.setRule((PersMonitorRuleActive) theRule);
			}
		}

		if (theRule instanceof PersMonitorRulePassive) {
			for (PersMonitorAppliesTo next : ((PersMonitorRulePassive) theRule).getAppliesTo()) {
				next.setRule((PersMonitorRulePassive) theRule);
			}
		}

		return myEntityManager.merge(theRule);
	}

	@Transactional(propagation=Propagation.REQUIRES_NEW)
	@Override
	public void saveNodeStatusInNewTransaction(PersNodeStatus theNodeStatus) {
		myEntityManager.merge(theNodeStatus);
	}

	@Override
	public <T extends BasePersMonitorRule> T saveOrCreateMonitorRule(T theRule) {
		Validate.notNull(theRule, "theRule");

		if (theRule instanceof PersMonitorRulePassive) {
			for (PersMonitorAppliesTo next : ((PersMonitorRulePassive) theRule).getAppliesTo()) {
				next.setRule((PersMonitorRulePassive) theRule);
			}
		}

		for (PersMonitorRuleNotifyContact next : theRule.getNotifyContact()) {
			next.setRule(theRule);
		}

		return myEntityManager.merge(theRule);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public ByteDelta saveRecentMessagesAndTrimInNewTransaction(BaseUnflushed<? extends BasePersSavedTransactionRecentMessage> theNextTransactions) {

		ByteDelta delta = doSaveRecentMessagesAndTrimInNewTransaction(theNextTransactions.getSuccessAndRemove());
		delta.add(doSaveRecentMessagesAndTrimInNewTransaction(theNextTransactions.getFailAndRemove()));
		delta.add(doSaveRecentMessagesAndTrimInNewTransaction(theNextTransactions.getSecurityFailAndRemove()));
		delta.add(doSaveRecentMessagesAndTrimInNewTransaction(theNextTransactions.getFaultAndRemove()));

		return delta;
	}

	@Override
	public PersBaseServerAuth<?, ?> saveServerAuth(PersBaseServerAuth<?, ?> thePers) {
		return myEntityManager.merge(thePers);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public PersService saveServiceInNewTransaction(PersService theService) {
		Validate.notNull(theService, "Service");

		if (theService.getPid() == null) {
			ourLog.info("Saving new service with ID[{}]", theService.getServiceId());
			myEntityManager.persist(theService);
			return theService;
		} else {
			ourLog.info("Saving service with PID[{}]", theService.getPid());
			return myEntityManager.merge(theService);
		}
	}

	@Override
	public BasePersServiceCatalogItem saveServiceCatalogItem(BasePersServiceCatalogItem theItem) {
		return myEntityManager.merge(theItem);
	}

	@Override
	public PersUser saveServiceUser(PersUser theUser) {
		Validate.notNull(theUser, "User");
		Validate.notNull(theUser.getAuthenticationHost(), "User.AuthenticationHost");

		 for (PersUserAllowableSourceIps next :
		 theUser.getAllowableSourceIpsToDelete()) {
		 if (!theUser.getAllowSourceIpsAsStrings().contains(next.getIp())) {
		 myEntityManager.remove(next);
		 }
		 }

		for (int i = 0; i < theUser.getAllowSourceIps().size(); i++) {
			PersUserAllowableSourceIps ip = theUser.getAllowSourceIps().get(i);
			 if (ip.getPid() == null) {
			 ip.setOrder(i);
			 myEntityManager.persist(ip);
			 }
			ip.setOrder(i);
		}

		if (theUser.getPid() == null) {
			myEntityManager.persist(theUser);
			return theUser;
		}
		return myEntityManager.merge(theUser);
	}

	@Override
	public BasePersServiceVersion saveServiceVersionInNewTransaction(BasePersServiceVersion theVersion) throws ProcessingException {
		Validate.notNull(theVersion, "ServiceVersion");
		Validate.notNull(theVersion.getPid(), "ServiceVersion.myPid");
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
		for (PersMethod next : theVersion.getMethods()) {
			Validate.throwProcessingExceptionIfBlank(next.getName(), "Method is missing name");
			next.setOrder(i++);
			next.setServiceVersion(theVersion);
		}

		theVersion.prePersist();

		ourLog.info("Merging servicde version {}", theVersion.getVersionId());
		BasePersServiceVersion retVal = myEntityManager.merge(theVersion);

		// Add status entity for any URLs that don't yet have one

		for (PersServiceVersionUrl nextUrl : retVal.getUrls()) {
			if (nextUrl.getStatus() == null) {
				PersServiceVersionUrlStatus stats = new PersServiceVersionUrlStatus();
				stats.setStatus(StatusEnum.UNKNOWN);
				stats.setUrl(nextUrl);
				nextUrl.setStatus(stats);
				myEntityManager.persist(stats);
			}

		}

		retVal.loadAllAssociations();
		return retVal;
	}

	@Override
	public void saveServiceVersionRecentMessage(PersServiceVersionRecentMessage theMsg) {
		Validate.notNull(theMsg);

		myEntityManager.merge(theMsg);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void saveServiceVersionStatuses(ArrayList<PersServiceVersionStatus> theStatuses) {
		Validate.notNull(theStatuses);

		ourLog.info("Going to save {} service version status entries", theStatuses.size());

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

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@Override
	public void saveServiceVersionUrlStatusInNewTransaction(List<PersServiceVersionUrlStatus> theStatuses) {

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

	@Transactional(propagation=Propagation.REQUIRES_NEW)
	@Override
	public void saveStickySessionUrlBindingInNewTransaction(PersStickySessionUrlBinding theBinding) {
		ourLog.debug("Saving sticky session with ID '{}' for URL {}", theBinding.getPk().getSessionId(), theBinding.getUrl().getPid());
		myEntityManager.merge(theBinding);
	}

	@Override
	public void saveUserRecentMessage(PersUserRecentMessage theMsg) {
		Validate.notNull(theMsg);

		myEntityManager.merge(theMsg);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void saveUserStatus(Collection<PersUserStatus> theStatus) {
		ourLog.info("Flushing {} user status entries", theStatus.size());

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

	/**
	 * * FOR UNIT TESTS ONLY
	 */
	public void setEntityManager(EntityManager theEntityManager) {
		myEntityManager = theEntityManager;
	}

	@Override
	public long trimServiceVersionRecentMessages(BasePersServiceVersion theVersion, ResponseTypeEnum theType, int theNumberToTrimTo) {
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
			return 0;
		}

		List<PersServiceVersionRecentMessage> messages = getServiceVersionRecentMessages(theVersion, theType);
		int index = 0;
		long retVal = 0;
		for (Iterator<PersServiceVersionRecentMessage> iter = messages.iterator(); iter.hasNext() && index < toDelete; index++) {
			PersServiceVersionRecentMessage next = iter.next();
			retVal += next.getRequestBodyBytes() + next.getResponseBodyBytes();
			myEntityManager.remove(next);
		}

		return retVal;
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

	private PersHttpClientConfig addDefaultHttpClientConfig() {
		PersHttpClientConfig retVal = new PersHttpClientConfig();
		retVal.setDefaults();
		retVal.setId(PersHttpClientConfig.DEFAULT_ID);

		retVal = myEntityManager.merge(retVal);

		// myEntityManager.flush();

		return retVal;
	}

	private ByteDelta doSaveRecentMessagesAndTrimInNewTransaction(LinkedList<? extends BasePersSavedTransactionRecentMessage> transactions) {
		ByteDelta retVal = new ByteDelta();

		if (transactions.size() > 0) {
			for (BasePersSavedTransactionRecentMessage nextRecentMessage : transactions) {
				retVal.addAdded(nextRecentMessage.getRequestBodyBytes() + nextRecentMessage.getResponseBodyBytes());
				nextRecentMessage.addUsingDao(this);
			}
			retVal.addRemoved(transactions.get(0).trimUsingDao(this));
		}

		return retVal;
	}

	@VisibleForTesting
	public
	void setTransactionTemplateForUnitTest() {
		myTransactionTemplate = new NullTransactionTemplateForUnitTests();
	}

	@VisibleForTesting
	public void setTransactionTemplateForUnitTest(TransactionTemplate theTransactionTemplate) {
		myTransactionTemplate = theTransactionTemplate;
	}

	@Override
	public void deleteMonitorRule(BasePersMonitorRule theRule) {
		myEntityManager.remove(theRule);
	}

	@Override
	public void deleteLibraryMessage(PersLibraryMessage theMsg) {
		myEntityManager.remove(theMsg);
	}

}
