package net.svcret.ejb.ejb;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import net.svcret.admin.shared.model.BaseDtoServiceVersion;
import net.svcret.admin.shared.model.ServiceProtocolEnum;
import net.svcret.ejb.admin.AdminServiceBean;
import net.svcret.ejb.api.IDao;
import net.svcret.ejb.api.IHttpClient;
import net.svcret.ejb.api.IServiceRegistry;
import net.svcret.ejb.ejb.nodecomm.IBroadcastSender;
import net.svcret.ejb.ex.InvocationFailedDueToInternalErrorException;
import net.svcret.ejb.ex.ProcessingException;
import net.svcret.ejb.ex.UnexpectedFailureException;
import net.svcret.ejb.model.entity.BasePersServiceVersion;
import net.svcret.ejb.model.entity.PersDomain;
import net.svcret.ejb.model.entity.PersHttpClientConfig;
import net.svcret.ejb.model.entity.PersService;
import net.svcret.ejb.model.entity.PersMethod;
import net.svcret.ejb.model.entity.PersServiceVersionUrl;
import net.svcret.ejb.model.entity.PersServiceVersionUrlStatus;
import net.svcret.ejb.model.entity.PersUser;
import net.svcret.ejb.model.entity.virtual.PersServiceVersionVirtual;
import net.svcret.ejb.util.Validate;

import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;

@Startup
@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class ServiceRegistryBean implements IServiceRegistry {

	private static Map<String, PersDomain> ourDomainMap;
	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(ServiceRegistryBean.class);
	private static volatile Map<Long, PersDomain> ourPidToDomains;
	private static volatile Map<Long, PersService> ourPidToServices;
	private static volatile Map<Long, BasePersServiceVersion> ourPidToServiceVersions;
	private static volatile Map<String, BasePersServiceVersion> ourProxyPathToServices;
	private static Object ourRegistryLock = new Object();
	private static final String STATE_KEY = ServiceRegistryBean.class.getName() + "_VERSION";

	@EJB
	private IBroadcastSender myBroadcastSender;

	private long myCurrentVersion;

	@EJB
	private IDao myDao;

	@EJB
	private IHttpClient mySvcHttpClient;

	/**
	 * Constructor
	 */
	public ServiceRegistryBean() {
		super();
	}

	private void addProxyPath(Map<String, BasePersServiceVersion> pathToServiceVersions, Map<Long, BasePersServiceVersion> pidToServiceVersions, BasePersServiceVersion nextVersion,
			String nextProxyPath) {
		pidToServiceVersions.put(nextVersion.getPid(), nextVersion);
		if (pathToServiceVersions.containsKey(nextProxyPath)) {
			ourLog.warn("Service version {} ({}/{}/{}) created duplicate proxy path '{}', so it will be ignored!", new Object[] { nextVersion.getPid(),
					nextVersion.getService().getDomain().getDomainId(), nextVersion.getService().getServiceId(), nextVersion.getVersionId(), nextProxyPath });
		} else {
			pathToServiceVersions.put(nextProxyPath, nextVersion);
		}
	}

	private void catalogHasChanged() throws UnexpectedFailureException {
		incrementStateVersion();
		myBroadcastSender.notifyServiceCatalogChanged();
	}

	@Override
	public void deleteHttpClientConfig(PersHttpClientConfig theConfig) throws ProcessingException, UnexpectedFailureException {
		catalogHasChanged();
		myDao.deleteHttpClientConfig(theConfig);
	}

	@Override
	public void deleteService(long thePid) throws ProcessingException, UnexpectedFailureException {
		PersService srv = myDao.getServiceByPid(thePid);
		if (srv == null) {
			throw new ProcessingException("Unknown service PID:" + thePid);
		}
		myDao.deleteService(srv);
		
		catalogHasChanged();
		reloadRegistryFromDatabase();
	}

	@Override
	public void deleteServiceVersion(long thePid) throws ProcessingException, UnexpectedFailureException {
		BasePersServiceVersion sv = myDao.getServiceVersionByPid(thePid);
		if (sv == null) {
			throw new ProcessingException("Unknown service version ID:" + thePid);
		}
		myDao.deleteServiceVersion(sv);
		
		catalogHasChanged();
		reloadRegistryFromDatabase();
	}

	private void doReloadRegistryFromDatabase() {
		ourLog.info("Reloading service registry from database");

		long newVersion = myDao.getStateCounter(STATE_KEY);

		Map<String, PersDomain> domainMap = new HashMap<String, PersDomain>();
		Map<String, BasePersServiceVersion> pathToServiceVersions = new HashMap<String, BasePersServiceVersion>();
		Map<Long, BasePersServiceVersion> pidToServiceVersions = new HashMap<Long, BasePersServiceVersion>();
		Map<Long, PersDomain> pidToDomains = new HashMap<Long, PersDomain>();
		Map<Long, PersService> pidToServices = new HashMap<Long, PersService>();

		Collection<PersDomain> domains = myDao.getAllDomains();
		for (PersDomain nextDomain : domains) {
			domainMap.put(nextDomain.getDomainId(), nextDomain);
			pidToDomains.put(nextDomain.getPid(), nextDomain);
			nextDomain.loadAllAssociations();

			for (PersService nextService : nextDomain.getServices()) {
				pidToServices.put(nextService.getPid(), nextService);
				for (BasePersServiceVersion nextVersion : nextService.getVersions()) {

					if (nextVersion.isUseDefaultProxyPath()) {
						addProxyPath(pathToServiceVersions, pidToServiceVersions, nextVersion, nextVersion.getDefaultProxyPath());
					}

					if (nextVersion.getExplicitProxyPath() != null && nextVersion.getExplicitProxyPath().startsWith("/")) {
						addProxyPath(pathToServiceVersions, pidToServiceVersions, nextVersion, nextVersion.getExplicitProxyPath());
					}

				}
			}

		}

		Map<String, PersUser> serviceUserMap = new HashMap<String, PersUser>();
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
			myCurrentVersion = newVersion;
		}
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
	public PersDomain getOrCreateDomainWithId(String theId) throws ProcessingException, UnexpectedFailureException {
		PersDomain orCreateDomainWithId = myDao.getOrCreateDomainWithId(theId);
		if (orCreateDomainWithId.isNewlyCreated()) {
			catalogHasChanged();
		}
		return orCreateDomainWithId;
	}

	@Override
	public BasePersServiceVersion getOrCreateServiceVersionWithId(PersService theService, ServiceProtocolEnum theProtocol, String theVersionId) throws ProcessingException, UnexpectedFailureException {
		BasePersServiceVersion retVal = myDao.getOrCreateServiceVersionWithId(theService, theVersionId, theProtocol);
		if (retVal.isNewlyCreated()) {
			catalogHasChanged();
		}
		return retVal;
	}

	
	@Override
	public BasePersServiceVersion getOrCreateServiceVersionWithId(PersService theService, ServiceProtocolEnum theProtocol, String theVersionId, PersServiceVersionVirtual theSvcVerToUseIfCreatingNew)
			throws UnexpectedFailureException {
		BasePersServiceVersion retVal = myDao.getOrCreateServiceVersionWithId(theService, theVersionId, theProtocol, theSvcVerToUseIfCreatingNew);
		if (retVal.isNewlyCreated()) {
			catalogHasChanged();
		}
		return retVal;
	}

	@Override
	public PersService getOrCreateServiceWithId(PersDomain theDomain, String theId) throws ProcessingException, UnexpectedFailureException {
		PersService retVal = myDao.getOrCreateServiceWithId(theDomain, theId);
		if (retVal.isNewlyCreated()) {
			catalogHasChanged();
		}
		return retVal;
	}

	@Override
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
			} catch (UnexpectedFailureException e) {
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
	public BasePersServiceVersion getServiceVersionForPath(String thePath) {
		if (thePath == null) {
			throw new IllegalArgumentException("Path can not be null");
		}
		synchronized (ourRegistryLock) {
			return ourProxyPathToServices.get(thePath);
		}
	}

	@Override
	public List<String> getValidPaths() {
		List<String> retVal = new ArrayList<String>();

		retVal.addAll(ourProxyPathToServices.keySet());
		Collections.sort(retVal);

		return retVal;
	}

	private void incrementStateVersion() {
		long newVersion = myDao.incrementStateCounter(STATE_KEY);
		ourLog.debug("State counter is now {}", newVersion);
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@PostConstruct
	public void postConstruct() {
		reloadRegistryFromDatabase();

		ourLog.info("Setting up Logback");

		try {

			LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
			JoranConfigurator jc = new JoranConfigurator();
			jc.setContext(context);
			context.reset();

			InputStream inputStream = AdminServiceBean.class.getResourceAsStream("/svcret-ejb-logback.xml");
			ourLog.info("Configuring using {}", inputStream);

			jc.doConfigure(inputStream);

		} catch (Exception e) {
			ourLog.error("Failed to set up logback", e);
		}

	}

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@Override
	public void reloadRegistryFromDatabase() {

		long newVersion = myDao.getStateCounter(STATE_KEY);

		ourLog.debug("New service registry version is {} - Have version {} in memory", newVersion, myCurrentVersion);

		if (newVersion == 0 || newVersion > myCurrentVersion) {
			doReloadRegistryFromDatabase();
		}

	}

	@Override
	public void removeDomain(PersDomain theDomain) throws ProcessingException, UnexpectedFailureException {
		catalogHasChanged();
		myDao.removeDomain(theDomain);
	}

	@Override
	public PersServiceVersionUrl resetCircuitBreaker(long theUrlPid) throws UnexpectedFailureException {
		PersServiceVersionUrl url = myDao.getServiceVersionUrlByPid(theUrlPid);
		PersServiceVersionUrlStatus status = url.getStatus();

		status.setNextCircuitBreakerReset(new Date());
		status.setNextCircuitBreakerResetTimestamp(new Date());

		myDao.saveServiceVersionUrlStatus(Collections.singletonList(status));

		myBroadcastSender.notifyUrlStatusChanged(url.getPid());

		return url;
	}

	@Override
	public PersDomain saveDomain(PersDomain theDomain) throws ProcessingException, UnexpectedFailureException {
		catalogHasChanged();
		PersDomain retVal = myDao.saveDomain(theDomain);
		reloadRegistryFromDatabase();
		return retVal;
	}

	@Override
	public PersHttpClientConfig saveHttpClientConfig(PersHttpClientConfig theConfig) throws ProcessingException, UnexpectedFailureException {
		catalogHasChanged();
		return myDao.saveHttpClientConfig(theConfig);
	}

	@Override
	public void saveService(PersService theService) throws ProcessingException, UnexpectedFailureException {
		catalogHasChanged();
		myDao.saveService(theService);
		reloadRegistryFromDatabase();
	}

	@Override
	public BasePersServiceVersion saveServiceVersion(BasePersServiceVersion theSv) throws UnexpectedFailureException, ProcessingException {
		catalogHasChanged();
		// reloadRegistryFromDatabase();
		return myDao.saveServiceVersion(theSv);
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

}
