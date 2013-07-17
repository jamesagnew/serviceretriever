package net.svcret.ejb.ejb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import net.svcret.admin.shared.model.ServiceProtocolEnum;
import net.svcret.ejb.api.IBroadcastSender;
import net.svcret.ejb.api.IDao;
import net.svcret.ejb.api.IHttpClient;
import net.svcret.ejb.api.IServiceRegistry;
import net.svcret.ejb.ex.InternalErrorException;
import net.svcret.ejb.ex.ProcessingException;
import net.svcret.ejb.model.entity.BasePersServiceVersion;
import net.svcret.ejb.model.entity.PersDomain;
import net.svcret.ejb.model.entity.PersHttpClientConfig;
import net.svcret.ejb.model.entity.PersService;
import net.svcret.ejb.model.entity.PersUser;
import net.svcret.ejb.util.Validate;

@Startup
@Singleton
public class ServiceRegistryBean implements IServiceRegistry {

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(ServiceRegistryBean.class);
	private static volatile Map<String, BasePersServiceVersion> ourProxyPathToServices;
	private static volatile Map<Long, BasePersServiceVersion> ourPidToServices;
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
	public ServiceRegistryBean() throws InternalErrorException {
		super();
	}

	private void catalogHasChanged() throws ProcessingException {
		incrementStateVersion();
		myBroadcastSender.notifyServiceCatalogChanged();
	}

	@Override
	public void deleteHttpClientConfig(PersHttpClientConfig theConfig) throws ProcessingException {
		catalogHasChanged();
		myDao.deleteHttpClientConfig(theConfig);
	}

	private void doReloadRegistryFromDatabase() {
		ourLog.info("Reloading service registry from database");

		long newVersion = myDao.getStateCounter(STATE_KEY);
		
		Map<String, PersDomain> domainMap = new HashMap<String, PersDomain>();
		Map<String, BasePersServiceVersion> pathToServiceVersions = new HashMap<String, BasePersServiceVersion>();
		Map<Long, BasePersServiceVersion> pidToServiceVersions=new HashMap<Long, BasePersServiceVersion>();
		
		Collection<PersDomain> domains = myDao.getAllDomains();
		for (PersDomain nextDomain : domains) {
			domainMap.put(nextDomain.getDomainId(), nextDomain);
			nextDomain.loadAllAssociations();

			for (PersService nextService : nextDomain.getServices()) {
				for (BasePersServiceVersion nextVersion : nextService.getVersions()) {
					String nextProxyPath = nextVersion.getProxyPath();
					pidToServiceVersions.put(nextVersion.getPid(), nextVersion);
					if (pathToServiceVersions.containsKey(nextProxyPath)) {
						ourLog.warn("Service version {} created duplicate proxy path, so it will be ignored!", nextVersion.getPid());
						continue;
					}
					pathToServiceVersions.put(nextProxyPath, nextVersion);
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
			ourPidToServices = pidToServiceVersions;
			ourProxyPathToServices = pathToServiceVersions;
			myCurrentVersion = newVersion;
		}
	}

	@Override
	public PersDomain getOrCreateDomainWithId(String theId) throws ProcessingException {
		PersDomain orCreateDomainWithId = myDao.getOrCreateDomainWithId(theId);
		if (orCreateDomainWithId.isNewlyCreated()) {
			catalogHasChanged();
		}
		return orCreateDomainWithId;
	}

	@Override
	public BasePersServiceVersion getOrCreateServiceVersionWithId(PersService theService, ServiceProtocolEnum theProtocol, String theVersionId) throws ProcessingException {
		BasePersServiceVersion retVal = myDao.getOrCreateServiceVersionWithId(theService, theVersionId, theProtocol);
		if (retVal.isNewlyCreated()) {
			catalogHasChanged();
		}
		return retVal;
	}

	@Override
	public PersService getOrCreateServiceWithId(PersDomain theDomain, String theId) throws ProcessingException {
		PersService retVal = myDao.getOrCreateServiceWithId(theDomain, theId);
		if (retVal.isNewlyCreated()) {
			catalogHasChanged();
		}
		return retVal;
	}

	@Override
	public BasePersServiceVersion getServiceVersionByPid(long theServiceVersionPid) {
		return ourPidToServices.get(theServiceVersionPid);
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
	public void removeDomain(PersDomain theDomain) throws ProcessingException {
		catalogHasChanged();
				
		myDao.removeDomain(theDomain);
	}

	@Override
	public PersDomain saveDomain(PersDomain theDomain) throws ProcessingException {
		catalogHasChanged();
		return myDao.saveDomain(theDomain);
	}

	@Override
	public PersHttpClientConfig saveHttpClientConfig(PersHttpClientConfig theConfig) throws ProcessingException {
		catalogHasChanged();
		return myDao.saveHttpClientConfig(theConfig);
	}

	@Override
	public void saveService(PersService theService) throws ProcessingException {
		catalogHasChanged();
		myDao.saveService(theService);
	}

	@Override
	public BasePersServiceVersion saveServiceVersion(BasePersServiceVersion theSv) throws ProcessingException {
		catalogHasChanged();
		return myDao.saveServiceVersion(theSv);
	}

	/**
	 * FOR UNIT TESTS ONLY
	 */
	void setBroadcastSender(IBroadcastSender theBroadcastSender) {
		myBroadcastSender = theBroadcastSender;
	}

	/**
	 * FOR UNIT TESTS ONLY
	 */
	public void setDao(IDao theSvcPersistence) {
		Validate.isNull(myDao, "IDao");
		myDao = theSvcPersistence;
	}

	/**
	 * FOR UNIT TESTS ONLY
	 */
	public void setSvcHttpClient(IHttpClient theSvcHttpClient) {
		Validate.isNull(mySvcHttpClient, "IServicHttpClient");
		mySvcHttpClient = theSvcHttpClient;
	}

}
