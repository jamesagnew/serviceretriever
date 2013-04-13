package net.svcret.ejb.ejb;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import net.svcret.admin.shared.model.UserGlobalPermissionEnum;
import net.svcret.ejb.api.IAuthorizationService;
import net.svcret.ejb.api.IAuthorizationService.ILdapAuthorizationService;
import net.svcret.ejb.api.IAuthorizationService.ILocalDatabaseAuthorizationService;
import net.svcret.ejb.api.ICredentialGrabber;
import net.svcret.ejb.api.ISecurityService;
import net.svcret.ejb.api.IServicePersistence;
import net.svcret.ejb.ex.ProcessingException;
import net.svcret.ejb.model.entity.BasePersAuthenticationHost;
import net.svcret.ejb.model.entity.PersAuthenticationHostLocalDatabase;
import net.svcret.ejb.model.entity.PersServiceVersionMethod;
import net.svcret.ejb.model.entity.PersUser;

@Stateless
public class SecurityServiceBean implements ISecurityService {

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(SecurityServiceBean.class);

	private static final String STATE_KEY = SecurityServiceBean.class.getName() + "_VERSION";

	private long myCurrentVersion;

	private volatile InMemoryUserCatalog myInMemoryUserCatalog;

	@EJB
	private ILdapAuthorizationService myLdapAuthService;

	@EJB
	private ILocalDatabaseAuthorizationService myLocalDbAuthService;

	@EJB
	private IServicePersistence myPersSvc;

	public boolean authorizeMethodInvocation(BasePersAuthenticationHost theAuthHost, ICredentialGrabber theCredentialGrabber, PersServiceVersionMethod theMethod) throws ProcessingException {
		BasePersAuthenticationHost authHost = myInMemoryUserCatalog.getAuthHostByPid(theAuthHost.getPid());
		if (authHost == null) {
			ourLog.debug("Authorization host not in user roster (possibly because it has been deleted?)");
			return false;
		}

		IAuthorizationService authService = getAuthService(authHost);

		ourLog.debug("Authorizing call using auth service: {}", authService);

		PersUser authorizedUser = authService.authorize(authHost, myInMemoryUserCatalog, theCredentialGrabber);
		if (authorizedUser == null) {
			ourLog.debug("Auth service did not find authorized user in request");
			return false;
		}
		
		boolean authorized = authorizedUser.hasPermission(theMethod);
		
		ourLog.debug("Authorization results: {}", authorized);

		return authorized;

	}

	private IAuthorizationService getAuthService(BasePersAuthenticationHost authHost) {
		IAuthorizationService authService = null;
		switch (authHost.getType()) {
		case LOCAL_DATABASE:
			authService = myLocalDbAuthService;
			break;
		case LDAP:
			authService = myLdapAuthService;
			break;
		}
		return authService;
	}

	private void incrementStateVersion() {
		myCurrentVersion = myPersSvc.incrementStateCounter(STATE_KEY);
		ourLog.debug("State counter is now {}", myCurrentVersion);
	}

	private void initUserCatalog() {
		ourLog.info("Initializing user catalog");

		PersAuthenticationHostLocalDatabase authHost;
		try {
			authHost = myPersSvc.getOrCreateAuthenticationHostLocalDatabase(PersAuthenticationHostLocalDatabase.MODULE_ID_ADMIN_AUTH);
		} catch (ProcessingException e) {
			ourLog.error("Failed to initialize authentication host", e);
			incrementStateVersion();
			return;
		}

		authHost.setModuleName(PersAuthenticationHostLocalDatabase.MODULE_DESC_ADMIN_AUTH);
		myPersSvc.saveAuthenticationHost(authHost);

		/*
		 * Create admin user
		 */

		PersUser adminUser;
		try {
			adminUser = myPersSvc.getOrCreateUser(authHost, PersUser.DEFAULT_ADMIN_USERNAME);
			adminUser.setPassword(PersUser.DEFAULT_ADMIN_PASSWORD);
			adminUser.getPermissions().add(UserGlobalPermissionEnum.SUPERUSER);
			myPersSvc.saveServiceUser(adminUser);
		} catch (ProcessingException e) {
			ourLog.error("Failed to initialize admin user", e);
			incrementStateVersion();
			return;
		}

		incrementStateVersion();
	}

	/**
	 * NB: Call this from synchronized context!
	 */
	void loadUserCatalog() {
		ourLog.info("Loading entire user catalog from databse");

		Map<Long, Map<String, PersUser>> hostPidToUsernameToUser = new HashMap<Long, Map<String, PersUser>>();
		Map<Long, BasePersAuthenticationHost> pidToAuthHost = new HashMap<Long, BasePersAuthenticationHost>();

		Collection<BasePersAuthenticationHost> allAuthHosts = myPersSvc.getAllAuthenticationHosts();
		for (BasePersAuthenticationHost nextHost : allAuthHosts) {
			hostPidToUsernameToUser.put(nextHost.getPid(), new HashMap<String, PersUser>());
			pidToAuthHost.put(nextHost.getPid(), nextHost);
		}

		Collection<PersUser> allUsers = myPersSvc.getAllServiceUsers();
		for (PersUser nextUser : allUsers) {
			Long authHostPid = nextUser.getAuthenticationHost().getPid();
			Map<String, PersUser> map = hostPidToUsernameToUser.get(authHostPid);
			
			assert map != null : "getAllAuthenticationHosts() didn't return host PID " + authHostPid;
			
			map.put(nextUser.getUsername(), nextUser);
		}

		myInMemoryUserCatalog = new InMemoryUserCatalog(hostPidToUsernameToUser, pidToAuthHost);

		ourLog.info("Done loading user catalog, found {} users", allUsers.size());
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public synchronized void loadUserCatalogIfNeeded() {
		ourLog.debug("Checking for updated user catalog");

		/*
		 * If the state in the DB hasn't ever been incremented, assume we're in
		 * a completely new installation, and create a default user database
		 * with an admin user who can begin configuring things
		 */

		long newVersion = myPersSvc.getStateCounter(STATE_KEY);
		if (newVersion == 0) {
			initUserCatalog();
		}

		if (newVersion == 0 || newVersion > myCurrentVersion) {
			loadUserCatalog();
		}

	}

	/**
	 * UNIT TESTS ONLY
	 */
	void setLocalDbAuthService(ILocalDatabaseAuthorizationService theLocalDbAuthService) {
		myLocalDbAuthService = theLocalDbAuthService;
	}

	/**
	 * UNIT TESTS ONLY
	 */
	void setPersSvc(IServicePersistence thePersSvc) {
		myPersSvc = thePersSvc;
	}

}
