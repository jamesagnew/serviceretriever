package net.svcret.ejb.ejb;

import static net.svcret.admin.shared.model.AuthorizationOutcomeEnum.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.apache.commons.lang3.Validate;

import net.svcret.admin.shared.model.AuthorizationOutcomeEnum;
import net.svcret.admin.shared.model.UserGlobalPermissionEnum;
import net.svcret.ejb.api.IAuthorizationService;
import net.svcret.ejb.api.IAuthorizationService.ILdapAuthorizationService;
import net.svcret.ejb.api.IAuthorizationService.ILocalDatabaseAuthorizationService;
import net.svcret.ejb.api.IBroadcastSender;
import net.svcret.ejb.api.ICredentialGrabber;
import net.svcret.ejb.api.IDao;
import net.svcret.ejb.api.ISecurityService;
import net.svcret.ejb.ex.ProcessingException;
import net.svcret.ejb.model.entity.BasePersAuthenticationHost;
import net.svcret.ejb.model.entity.PersAuthenticationHostLocalDatabase;
import net.svcret.ejb.model.entity.PersServiceVersionMethod;
import net.svcret.ejb.model.entity.PersUser;

@Stateless
public class SecurityServiceBean implements ISecurityService {

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(SecurityServiceBean.class);

	private static final String STATE_KEY = SecurityServiceBean.class.getName() + "_VERSION";

	@EJB
	private IBroadcastSender myBroadcastSender;

	private long myCurrentVersion;

	@EJB
	private IDao myDao;

	private volatile InMemoryUserCatalog myInMemoryUserCatalog;

	@EJB
	private ILdapAuthorizationService myLdapAuthService;

	@EJB
	private ILocalDatabaseAuthorizationService myLocalDbAuthService;

	public AuthorizationResultsBean authorizeMethodInvocation(BasePersAuthenticationHost theAuthHost, ICredentialGrabber theCredentialGrabber, PersServiceVersionMethod theMethod, String theRequestHostIp) throws ProcessingException {
		Validate.notNull(theAuthHost, "AuthHost");
		
		if (myInMemoryUserCatalog == null) {
			loadUserCatalogIfNeeded();
		}
		
		BasePersAuthenticationHost authHost = myInMemoryUserCatalog.getAuthHostByPid(theAuthHost.getPid());

		AuthorizationResultsBean retVal = new AuthorizationResultsBean();
		if (authHost == null) {
			ourLog.debug("Authorization host not in user roster (possibly because it has been deleted?)");
			retVal.setAuthorized(FAILED_INTERNAL_ERROR);
			return retVal;
		}

		IAuthorizationService authService = getAuthService(authHost);

		ourLog.debug("Authorizing call using auth service: {}", authService);

		PersUser authorizedUser = authService.authorize(authHost, myInMemoryUserCatalog, theCredentialGrabber);
		if (authorizedUser == null) {
			ourLog.debug("Auth service did not find authorized user in request");
			retVal.setAuthorized(FAILED_BAD_CREDENTIALS_IN_REQUEST);
			// TODO: return the failed user so we can add it to runtimestats
			return retVal;
		}

		retVal.setUser(authorizedUser);

		boolean ipAllowed = authorizedUser.determineIfIpIsAllowed(theRequestHostIp);
		if (ipAllowed == false) {
			if (ourLog.isDebugEnabled()) {
				ourLog.debug("IP {} not allowed by user {} with whitelist {}", new Object[] { theRequestHostIp, authorizedUser, authorizedUser.getAllowSourceIpsAsStrings() });
			}
			retVal.setAuthorized(AuthorizationOutcomeEnum.FAILED_IP_NOT_IN_WHITELIST);
		} else {

			boolean authorized = authorizedUser.hasPermission(theMethod);

			ourLog.debug("Authorization results: {}", authorized);
			if (authorized) {
				retVal.setAuthorized(AUTHORIZED);
			} else {
				retVal.setAuthorized(FAILED_USER_NO_PERMISSIONS);
			}
		}
		return retVal;

	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public synchronized void loadUserCatalogIfNeeded() {
		ourLog.debug("Checking for updated user catalog");

		/*
		 * If the state in the DB hasn't ever been incremented, assume we're in
		 * a completely new installation, and create a default user database
		 * with an admin user who can begin configuring things
		 */

		long newVersion = myDao.getStateCounter(STATE_KEY);
		if (newVersion == 0) {
			initUserCatalog();
		}

		if (newVersion == 0 || newVersion > myCurrentVersion) {
			loadUserCatalog();
		}

	}

	@Override
	public PersUser saveServiceUser(PersUser theUser) throws ProcessingException {
		incrementStateVersion();
		myBroadcastSender.notifyUserCatalogChanged();
		return myDao.saveServiceUser(theUser);
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
		long newVersion = myDao.incrementStateCounter(STATE_KEY);
		ourLog.debug("State counter is now {}", newVersion);
	}

	private void initUserCatalog() {
		ourLog.info("Initializing user catalog");

		PersAuthenticationHostLocalDatabase authHost;
		try {
			authHost = myDao.getOrCreateAuthenticationHostLocalDatabase(BasePersAuthenticationHost.MODULE_ID_ADMIN_AUTH);
		} catch (ProcessingException e) {
			ourLog.error("Failed to initialize authentication host", e);
			incrementStateVersion();
			return;
		}

		authHost.setModuleName(BasePersAuthenticationHost.MODULE_DESC_ADMIN_AUTH);
		myDao.saveAuthenticationHost(authHost);

		/*
		 * Create admin user
		 */

		PersUser adminUser;
		try {
			adminUser = myDao.getOrCreateUser(authHost, PersUser.DEFAULT_ADMIN_USERNAME);
			adminUser.setPassword(PersUser.DEFAULT_ADMIN_PASSWORD);
			adminUser.getPermissions().add(UserGlobalPermissionEnum.SUPERUSER);
			myDao.saveServiceUser(adminUser);
		} catch (ProcessingException e) {
			ourLog.error("Failed to initialize admin user", e);
			incrementStateVersion();
			return;
		}

		incrementStateVersion();
	}

	/**
	 * @return the inMemoryUserCatalog
	 */
	InMemoryUserCatalog getInMemoryUserCatalog() {
		return myInMemoryUserCatalog;
	}

	/**
	 * NB: Call this from synchronized context!
	 */
	void loadUserCatalog() {
		ourLog.info("Loading entire user catalog from databse");
		long newVersion = myDao.getStateCounter(STATE_KEY);

		Map<Long, Map<String, PersUser>> hostPidToUsernameToUser = new HashMap<Long, Map<String, PersUser>>();
		Map<Long, BasePersAuthenticationHost> pidToAuthHost = new HashMap<Long, BasePersAuthenticationHost>();

		Collection<BasePersAuthenticationHost> allAuthHosts = myDao.getAllAuthenticationHosts();
		for (BasePersAuthenticationHost nextHost : allAuthHosts) {
			hostPidToUsernameToUser.put(nextHost.getPid(), new HashMap<String, PersUser>());
			pidToAuthHost.put(nextHost.getPid(), nextHost);
		}

		Collection<PersUser> allUsers = myDao.getAllUsersAndInitializeThem();
		for (PersUser nextUser : allUsers) {
			Long authHostPid = nextUser.getAuthenticationHost().getPid();
			Map<String, PersUser> map = hostPidToUsernameToUser.get(authHostPid);

			assert map != null : "getAllAuthenticationHosts() didn't return host PID " + authHostPid;

			map.put(nextUser.getUsername(), nextUser);
		}

		myInMemoryUserCatalog = new InMemoryUserCatalog(hostPidToUsernameToUser, pidToAuthHost);
		myCurrentVersion = newVersion;

		ourLog.info("Done loading user catalog, found {} users", allUsers.size());
	}

	/**
	 * UNIT TESTS ONLY
	 */
	void setBroadcastSender(IBroadcastSender theBroadcastSender) {
		myBroadcastSender = theBroadcastSender;
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
	void setPersSvc(IDao thePersSvc) {
		myDao = thePersSvc;
	}

}
