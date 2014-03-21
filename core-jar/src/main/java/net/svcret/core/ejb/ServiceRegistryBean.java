package net.svcret.core.ejb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;

import net.svcret.admin.api.ProcessingException;
import net.svcret.admin.api.UnexpectedFailureException;
import net.svcret.admin.shared.model.BaseDtoServiceVersion;
import net.svcret.admin.shared.model.ServiceProtocolEnum;
import net.svcret.admin.shared.util.Validate;
import net.svcret.core.api.IDao;
import net.svcret.core.api.IHttpClient;
import net.svcret.core.api.IServiceRegistry;
import net.svcret.core.ejb.nodecomm.IBroadcastSender;
import net.svcret.core.ex.InvocationFailedDueToInternalErrorException;
import net.svcret.core.model.entity.BasePersMonitorRule;
import net.svcret.core.model.entity.BasePersServiceVersion;
import net.svcret.core.model.entity.PersDomain;
import net.svcret.core.model.entity.PersHttpClientConfig;
import net.svcret.core.model.entity.PersMethod;
import net.svcret.core.model.entity.PersMonitorRuleActive;
import net.svcret.core.model.entity.PersMonitorRulePassive;
import net.svcret.core.model.entity.PersService;
import net.svcret.core.model.entity.PersServiceVersionUrl;
import net.svcret.core.model.entity.PersServiceVersionUrlStatus;
import net.svcret.core.model.entity.PersUser;
import net.svcret.core.model.entity.virtual.PersServiceVersionVirtual;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.google.common.annotations.VisibleForTesting;

@Service
public class ServiceRegistryBean implements IServiceRegistry {

	private static Map<String, PersDomain> ourDomainMap;
	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(ServiceRegistryBean.class);
	private static volatile Map<Long, PersDomain> ourPidToDomains;
	private static volatile Map<Long, PersService> ourPidToServices;
	private static volatile Map<Long, BasePersServiceVersion> ourPidToServiceVersions;
	private static volatile Map<String, BasePersServiceVersion> ourProxyPathToServices;
	private static volatile Map<String, BasePersServiceVersion> ourProxyPathFuzzyToServices;
	private static Object ourRegistryLock = new Object();
	private static final String STATE_KEY = ServiceRegistryBean.class.getName() + "_VERSION";

	@Autowired
	private IBroadcastSender myBroadcastSender;

	private long myCurrentVersion;

	@Autowired
	private IDao myDao;

	@Autowired
	private PlatformTransactionManager myPlatformTransactionManager;

	@Autowired
	private IHttpClient mySvcHttpClient;

	/**
	 * Constructor
	 */
	public ServiceRegistryBean() {
		super();
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void deleteDomain(long thePid) {
		PersDomain domain = myDao.getDomainByPid(thePid);
		if (domain == null) {
			throw new IllegalArgumentException("Unknown domain PID: " + thePid);
		}

		ourLog.info("DELETING domain with PID {} and ID {}", thePid, domain.getDomainId());

		// domain.loadAllAssociations();

		myDao.deleteDomain(domain);

		synchronizeCatalogChange();
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@Override
	public void deleteHttpClientConfig(long thePid) throws ProcessingException {
		PersHttpClientConfig config = myDao.getHttpClientConfig(thePid);
		if (config == null) {
			throw new ProcessingException("Unknown HTTP Client Config PID: " + thePid);
		}

		ourLog.info("Deleting HTTP Client Config {} / {}", thePid, config.getId());

		myDao.deleteHttpClientConfig(config);
		synchronizeCatalogChange();
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@Override
	public void deleteMonitorRule(Long thePid) {
		BasePersMonitorRule rule = myDao.getMonitorRule(thePid);
		myDao.deleteMonitorRule(rule);
		synchronizeCatalogChange();
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@Override
	public void deleteService(long thePid) throws ProcessingException {
		PersService srv = myDao.getServiceByPid(thePid);
		if (srv == null) {
			throw new ProcessingException("Unknown service PID:" + thePid);
		}
		myDao.deleteService(srv);

		synchronizeCatalogChange();
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@Override
	public void deleteServiceVersion(long thePid) throws ProcessingException {
		BasePersServiceVersion sv = myDao.getServiceVersionByPid(thePid);
		if (sv == null) {
			throw new ProcessingException("Unknown service version ID:" + thePid);
		}
		myDao.deleteServiceVersion(sv);

		synchronizeCatalogChange();
	}

	@Override
	public Collection<PersDomain> getAllDomains() {
		return ourDomainMap.values();
	}

	@Override
	public PersDomain getDomainByPid(Long theDomainPid) {
		return ourPidToDomains.get(theDomainPid);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public PersDomain getOrCreateDomainWithId(String theId) throws ProcessingException {
		PersDomain orCreateDomainWithId = myDao.getOrCreateDomainWithId(theId);
		return orCreateDomainWithId;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public BasePersServiceVersion getOrCreateServiceVersionWithId(PersService theService, ServiceProtocolEnum theProtocol, String theVersionId) throws ProcessingException {
		BasePersServiceVersion retVal = myDao.getOrCreateServiceVersionWithId(theService, theVersionId, theProtocol);
		return retVal;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public BasePersServiceVersion getOrCreateServiceVersionWithId(PersService theService, ServiceProtocolEnum theProtocol, String theVersionId, PersServiceVersionVirtual theSvcVerToUseIfCreatingNew) {
		BasePersServiceVersion retVal = myDao.getOrCreateServiceVersionWithId(theService, theVersionId, theProtocol, theSvcVerToUseIfCreatingNew);
		return retVal;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public PersService getOrCreateServiceWithId(PersDomain theDomain, String theId) throws ProcessingException {
		PersService retVal = myDao.getOrCreateServiceWithId(theDomain, theId);
		return retVal;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public PersMethod getOrCreateUnknownMethodEntryForServiceVersion(BasePersServiceVersion theServiceVersion) throws InvocationFailedDueToInternalErrorException {
		PersMethod method = theServiceVersion.getMethod(BaseDtoServiceVersion.METHOD_NAME_UNKNOWN);
		if (method == null) {
			ourLog.info("Creating 'unknown' method for service version {} to store statistics against", theServiceVersion.getPid());
			BasePersServiceVersion dbSvcVer = myDao.getServiceVersionByPid(theServiceVersion.getPid());
			dbSvcVer.getOrCreateAndAddMethodWithName(BaseDtoServiceVersion.METHOD_NAME_UNKNOWN);
			try {
				dbSvcVer = saveServiceVersion(dbSvcVer);
			} catch (ProcessingException e) {
				ourLog.error("Failed to auto-create method", e);
				throw new InvocationFailedDueToInternalErrorException(e, "Failed to auto-create method '" + BaseDtoServiceVersion.METHOD_NAME_UNKNOWN + "'. Error was: " + e.getMessage());
			}
			method = dbSvcVer.getMethod(BaseDtoServiceVersion.METHOD_NAME_UNKNOWN);
			ourLog.info("Created new method '{}' and got PID {}", BaseDtoServiceVersion.METHOD_NAME_UNKNOWN, method.getPid());
		}

		return method;
	}

	@Override
	public PersService getServiceByPid(Long theServicePid) {
		return ourPidToServices.get(theServicePid);
	}

	@Override
	public BasePersServiceVersion getServiceVersionByPid(long theServiceVersionPid) {
		return ourPidToServiceVersions.get(theServiceVersionPid);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(propagation = Propagation.SUPPORTS)
	public BasePersServiceVersion getServiceVersionForPath(String thePath) {
		if (thePath == null) {
			throw new IllegalArgumentException("Path can not be null");
		}
		BasePersServiceVersion retVal = ourProxyPathToServices.get(thePath);
		if (retVal == null) {
			for (Entry<String, BasePersServiceVersion> next : ourProxyPathFuzzyToServices.entrySet()) {
				if (thePath.startsWith(next.getKey())) {
					return next.getValue();
				}
			}
		}
		return retVal;
	}

	@Override
	public List<String> getValidPaths() {
		List<String> retVal = new ArrayList<>();

		retVal.addAll(ourProxyPathToServices.keySet());
		Collections.sort(retVal);

		return retVal;
	}

	@PostConstruct
	public void postConstruct() {
		TransactionTemplate tmpl = new TransactionTemplate(myPlatformTransactionManager);
		tmpl.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				reloadRegistryFromDatabase();
			}
		});

		ourLog.info("Setting up Logback");

		// FIXME: move this to main app
		// try {
		//
		// LoggerContext context = (LoggerContext)
		// LoggerFactory.getILoggerFactory();
		// JoranConfigurator jc = new JoranConfigurator();
		// jc.setContext(context);
		// context.reset();
		//
		// InputStream inputStream =
		// AdminServiceBean.class.getResourceAsStream("/svcret-ejb-logback.xml");
		// ourLog.info("Configuring using {}", inputStream);
		//
		// jc.doConfigure(inputStream);
		//
		// } catch (Exception e) {
		// ourLog.error("Failed to set up logback", e);
		// }

	}

	@Transactional(propagation = Propagation.REQUIRED)
	@Override
	public void reloadRegistryFromDatabase() {

		long newVersion = myDao.getStateCounter(STATE_KEY);

		ourLog.info("New service registry version is {} - Have version {} in memory", newVersion, myCurrentVersion);

		if (newVersion == 0 || newVersion > myCurrentVersion) {
			doReloadRegistryFromDatabase();
		}

	}

	@Override
	public PersServiceVersionUrl resetCircuitBreaker(long theUrlPid) throws UnexpectedFailureException {
		PersServiceVersionUrl url = myDao.getServiceVersionUrlByPid(theUrlPid);
		PersServiceVersionUrlStatus status = url.getStatus();

		status.setNextCircuitBreakerReset(new Date());
		status.setNextCircuitBreakerResetTimestamp(new Date());

		myDao.saveServiceVersionUrlStatusInNewTransaction(Collections.singletonList(status));

		myBroadcastSender.notifyUrlStatusChanged(url.getPid());

		return url;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@Override
	public PersDomain saveDomain(PersDomain theDomain) {
		// TODO: not new transaction
		PersDomain retVal = myDao.saveDomain(theDomain);

		synchronizeCatalogChange();

		return retVal;
	}

	@Override
	public PersHttpClientConfig saveHttpClientConfig(PersHttpClientConfig theConfig) throws UnexpectedFailureException {
		PersHttpClientConfig retVal = myDao.saveHttpClientConfigInNewTransaction(theConfig);
		catalogHasChanged();
		return retVal;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@Override
	public BasePersMonitorRule saveRule(BasePersMonitorRule theRule) throws UnexpectedFailureException {

		BasePersMonitorRule rule;
		if (theRule.getPid() != null) {
			BasePersMonitorRule existing = myDao.getMonitorRule(theRule.getPid());
			switch (existing.getRuleType()) {
			case ACTIVE:
				((PersMonitorRuleActive) existing).merge(theRule);
				break;
			case PASSIVE:
				((PersMonitorRulePassive) existing).merge((PersMonitorRulePassive) theRule);
				break;
			}
			rule = existing;
		} else {
			rule = theRule;
		}

		// TODO: method below is not new transaction
		BasePersMonitorRule retVal = myDao.saveMonitorRuleInNewTransaction(rule);

		catalogHasChanged();

		return retVal;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@Override
	public PersService saveService(PersService theService) {
		PersService retVal = myDao.saveServiceInNewTransaction(theService);
		synchronizeCatalogChange();
		return retVal;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@Override
	public BasePersServiceVersion saveServiceVersion(BasePersServiceVersion theSv) throws ProcessingException {
		// reloadRegistryFromDatabase();
		BasePersServiceVersion retVal = myDao.saveServiceVersionInNewTransaction(theSv);

		synchronizeCatalogChange();
		return retVal;
	}

	@VisibleForTesting
	public void setBroadcastSender(IBroadcastSender theBroadcastSender) {
		myBroadcastSender = theBroadcastSender;
	}

	@VisibleForTesting
	public void setDao(IDao theSvcPersistence) {
		Validate.isNull(myDao, "IDao");
		myDao = theSvcPersistence;
	}

	@VisibleForTesting
	public void setSvcHttpClient(IHttpClient theSvcHttpClient) {
		Validate.isNull(mySvcHttpClient, "IServicHttpClient");
		mySvcHttpClient = theSvcHttpClient;
	}

	private static void addProxyPath(Map<String, BasePersServiceVersion> pathToServiceVersions, Map<String, BasePersServiceVersion> pathFuzzyToServiceVersions, Map<Long, BasePersServiceVersion> pidToServiceVersions, BasePersServiceVersion nextVersion,
			String nextProxyPath) {
		pidToServiceVersions.put(nextVersion.getPid(), nextVersion);
		if (pathToServiceVersions.containsKey(nextProxyPath)) {
			ourLog.warn("Service version {} ({}/{}/{}) created duplicate proxy path '{}', so it will be ignored!", new Object[] { nextVersion.getPid(),
					nextVersion.getService().getDomain().getDomainId(), nextVersion.getService().getServiceId(), nextVersion.getVersionId(), nextProxyPath });
		} else {
			pathToServiceVersions.put(nextProxyPath, nextVersion);
			if (nextVersion.isAllowSubUrls()) {
				pathFuzzyToServiceVersions.put(nextProxyPath, nextVersion);
			}
			
		}
	}

	private void catalogHasChanged() throws UnexpectedFailureException {
		incrementStateVersion();
		myBroadcastSender.notifyServiceCatalogChanged();
		reloadRegistryFromDatabase();
	}

	private void doReloadRegistryFromDatabase() {
		ourLog.info("Reloading service registry from database");

		long newVersion = myDao.getStateCounter(STATE_KEY);

		Map<String, PersDomain> domainMap = new HashMap<>();
		Map<String, BasePersServiceVersion> pathToServiceVersions = new HashMap<>();
		Map<String, BasePersServiceVersion> pathFuzzyToServiceVersions = new HashMap<>();
		Map<Long, BasePersServiceVersion> pidToServiceVersions = new HashMap<>();
		Map<Long, PersDomain> pidToDomains = new HashMap<>();
		Map<Long, PersService> pidToServices = new HashMap<>();

		Collection<PersDomain> domains = myDao.getAllDomains();
		for (PersDomain nextDomain : domains) {
			domainMap.put(nextDomain.getDomainId(), nextDomain);
			pidToDomains.put(nextDomain.getPid(), nextDomain);
			nextDomain.loadAllAssociations();

			for (PersService nextService : nextDomain.getServices()) {
				pidToServices.put(nextService.getPid(), nextService);
				for (BasePersServiceVersion nextVersion : nextService.getVersions()) {

					if (nextVersion.isUseDefaultProxyPath()) {
						addProxyPath(pathToServiceVersions, pathFuzzyToServiceVersions, pidToServiceVersions, nextVersion, nextVersion.getDefaultProxyPath());
					}

					if (nextVersion.getExplicitProxyPath() != null && nextVersion.getExplicitProxyPath().startsWith("/")) {
						addProxyPath(pathToServiceVersions,pathFuzzyToServiceVersions,  pidToServiceVersions, nextVersion, nextVersion.getExplicitProxyPath());
					}
					
				}
			}

		}

		Map<String, PersUser> serviceUserMap = new HashMap<>();
		Collection<PersUser> users = myDao.getAllUsersAndInitializeThem();
		for (PersUser nextUser : users) {
			serviceUserMap.put(nextUser.getUsername(), nextUser);
		}

		ourLog.info("Done loading service registry from database");

		synchronized (ourRegistryLock) {
			ourDomainMap = domainMap;
			ourPidToDomains = pidToDomains;
			ourPidToServices = pidToServices;
			ourPidToServiceVersions = pidToServiceVersions;
			ourProxyPathToServices = pathToServiceVersions;
			ourProxyPathFuzzyToServices = pathFuzzyToServiceVersions;
			myCurrentVersion = newVersion;
		}
	}

	private void incrementStateVersion() {
		long newVersion = myDao.incrementStateCounter(STATE_KEY);
		ourLog.info("State counter is now {}", newVersion);
	}

	private void synchronizeCatalogChange() {
		if (!TransactionSynchronizationManager.isSynchronizationActive()) {
			// reloadRegistryFromDatabase();
			// catalogHasChanged();
		} else {
			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
				@Override
				public void afterCommit() {
					try {
						reloadRegistryFromDatabase();
						catalogHasChanged();
					} catch (UnexpectedFailureException e) {
						ourLog.warn("Failed to notify peers of change", e);
					}
				}
			});
		}
	}

}
