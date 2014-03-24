package net.svcret.core.security;

import static net.svcret.admin.shared.enm.AuthorizationOutcomeEnum.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import net.svcret.admin.api.ProcessingException;
import net.svcret.admin.api.UnexpectedFailureException;
import net.svcret.admin.api.UnknownPidException;
import net.svcret.admin.shared.enm.AuthorizationOutcomeEnum;
import net.svcret.admin.shared.enm.MethodSecurityPolicyEnum;
import net.svcret.admin.shared.enm.ServerSecurityModeEnum;
import net.svcret.admin.shared.model.UserGlobalPermissionEnum;
import net.svcret.core.api.IAuthorizationService;
import net.svcret.core.api.IAuthorizationService.ILdapAuthorizationService;
import net.svcret.core.api.IAuthorizationService.ILocalDatabaseAuthorizationService;
import net.svcret.core.api.IAuthorizationService.UserOrFailure;
import net.svcret.core.api.IDao;
import net.svcret.core.api.ISecurityService;
import net.svcret.core.ejb.InMemoryUserCatalog;
import net.svcret.core.ejb.nodecomm.IBroadcastSender;
import net.svcret.core.model.entity.BasePersAuthenticationHost;
import net.svcret.core.model.entity.BasePersObject;
import net.svcret.core.model.entity.PersAuthenticationHostLocalDatabase;
import net.svcret.core.model.entity.PersMethod;
import net.svcret.core.model.entity.PersUser;
import net.svcret.core.model.entity.PersUserStatus;

import org.apache.commons.lang3.Validate;
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
public class SecurityServiceBean implements ISecurityService {

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(SecurityServiceBean.class);

	private static final String STATE_KEY = SecurityServiceBean.class.getName() + "_VERSION";

	@Autowired
	private IBroadcastSender myBroadcastSender;

	private long myCurrentVersion;

	@Autowired
	private IDao myDao;

	private volatile InMemoryUserCatalog myInMemoryUserCatalog;

	@Autowired
	private ILdapAuthorizationService myLdapAuthService;

	@Autowired
	private ILocalDatabaseAuthorizationService myLocalDbAuthService;

	@Autowired
	protected PlatformTransactionManager myPlatformTransactionManager;

	private volatile List<BasePersAuthenticationHost> myInMemoryAuthenticationHostList;

	@PostConstruct
	public void postConstruct() {
		TransactionTemplate tmpl = new TransactionTemplate(myPlatformTransactionManager);
		tmpl.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				loadUserCatalogIfNeeded();
			}
		});
	}

	@Override
	public AuthorizationResultsBean authorizeMethodInvocation(List<AuthorizationRequestBean> theAuthRequests, PersMethod theMethod, String theRequestHostIp) throws ProcessingException {
		Validate.notNull(theAuthRequests, "AuthRequests");

		if (myInMemoryUserCatalog == null) {
			loadUserCatalogIfNeeded();
		}

		AuthorizationResultsBean retVal = new AuthorizationResultsBean();
		ServerSecurityModeEnum serverSecurityMode = theMethod.getServiceVersion().getServerSecurityMode();

		int failed = 0;
		int passed = 0;
		if (serverSecurityMode != ServerSecurityModeEnum.NONE) {

			for (AuthorizationRequestBean next : theAuthRequests) {
				BasePersAuthenticationHost authHost = myInMemoryUserCatalog.getAuthHostByPid(next.getAuthenticationHost().getPid());
				if (authHost == null) {
					ourLog.debug("Authorization host not in user roster (possibly because it has been deleted?)");
					retVal.setAuthorized(FAILED_INTERNAL_ERROR);
					return retVal;
				}

				IAuthorizationService authService = getAuthService(authHost);
				ourLog.debug("Authorizing call using auth service: {}", authService);

				UserOrFailure authorize = authService.authorize(authHost, myInMemoryUserCatalog, next.getCredentialGrabber());

				/*
				 * TODO: keep track of the failure reason by authrequest so that
				 * we can display all of them in the UI (i.e. if the credentials
				 * fail for different reasons for each service..)
				 */

				PersUser authorizedUser = authorize.getUser();
				boolean authFailed;

				if (authorizedUser == null) {
					if (theMethod.getSecurityPolicy() == MethodSecurityPolicyEnum.ALLOW) {
						authFailed = false;
					} else {
						authFailed = true;
					}
				} else {
					boolean ipAllowed = authorizedUser.determineIfIpIsAllowed(theRequestHostIp);
					if (ipAllowed == false) {
						if (ourLog.isDebugEnabled()) {
							ourLog.debug("IP {} not allowed by user {} with whitelist {}", new Object[] { theRequestHostIp, authorizedUser, authorizedUser.getAllowSourceIpsAsStrings() });
						}
						retVal.setAuthorized(AuthorizationOutcomeEnum.FAILED_IP_NOT_IN_WHITELIST);
						authFailed = true;
					} else {

						boolean authorized;
						switch (theMethod.getSecurityPolicy()) {
						case ALLOW:
							authorized = true;
							break;
						case REJECT_UNLESS_ALLOWED:
							authorized = authorizedUser.hasPermission(theMethod, false);
							break;
						case REJECT_UNLESS_SPECIFICALLY_ALLOWED:
							authorized = authorizedUser.hasPermission(theMethod, true);
							break;
						default:
							throw new IllegalStateException("Unknown security policy: " + theMethod.getSecurityPolicy());
						}

						ourLog.debug("Authorization results: {}", authorized);
						if (authorized) {
							authFailed = false;
							if (retVal.getAuthorizedUser() == null) {
								retVal.setAuthorizedUser(authorizedUser);
							}
						} else {
							authFailed = true;
							retVal.setAuthorized(FAILED_USER_NO_PERMISSIONS);
						}
					}

				}

				if (authFailed) {
					failed++;
				} else {
					passed++;
				}

				if (serverSecurityMode == ServerSecurityModeEnum.ALLOW_ANY || serverSecurityMode == ServerSecurityModeEnum.REQUIRE_ANY) {
					if (passed > 0) {
						break;
					}
				}

			}// for server security modules

		}

		switch (serverSecurityMode) {
		case ALLOW_ANY:
			retVal.setAuthorized(AUTHORIZED);
			break;

		case NONE:
			retVal.setAuthorized(AUTHORIZED);
			break;

		case REQUIRE_ALL:
			if (passed == 0 && failed == 0) {
				retVal.setAuthorized(AUTHORIZED);
			} else if (failed > 0) {
				if (retVal.getAuthorized() == null) {
					retVal.setAuthorized(FAILED_BAD_CREDENTIALS_IN_REQUEST);
				}
			} else {
				retVal.setAuthorized(AUTHORIZED);
			}
			break;

		case REQUIRE_ANY:
			if (passed == 0 && failed == 0) {
				retVal.setAuthorized(AUTHORIZED);
			} else if (passed == 0) {
				if (retVal.getAuthorized() == null) {
					retVal.setAuthorized(FAILED_BAD_CREDENTIALS_IN_REQUEST);
				}
			} else {
				retVal.setAuthorized(AUTHORIZED);
			}
			break;

		}

		return retVal;

	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
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
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public PersUser saveServiceUser(PersUser theUser) {

		if (theUser.getPid() == null) {
			theUser.setStatus(new PersUserStatus(theUser));
		}

		PersUser retVal = myDao.saveServiceUser(theUser);
		synchronizeUserCatalogChanged();

		retVal.loadAllAssociations();

		return retVal;
	}

	private void synchronizeUserCatalogChanged() {
//		if (!BasePersObject.isUnitTestMode()) {
			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
				@Override
				public void afterCommit() {
					try {
						incrementStateVersion();
						loadUserCatalogIfNeeded();
						myBroadcastSender.notifyUserCatalogChanged();
					} catch (UnexpectedFailureException e) {
						ourLog.warn("Failed to notify peers of change", e);
					}
				}
			});
//		}
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

		// Users

		Map<Long, Map<String, PersUser>> hostPidToUsernameToUser = new HashMap<>();
		Map<Long, BasePersAuthenticationHost> pidToAuthHost = new HashMap<>();

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

		// Authentication hosts
		List<BasePersAuthenticationHost> authHosts = new ArrayList<>();
		for (BasePersAuthenticationHost next : myDao.getAllAuthenticationHosts()) {
			authHosts.add(next);
		}

		// Done!
		
		myInMemoryAuthenticationHostList = Collections.unmodifiableList(authHosts);
		myInMemoryUserCatalog = new InMemoryUserCatalog(hostPidToUsernameToUser, pidToAuthHost);
		myCurrentVersion = newVersion;

		ourLog.info("Done loading user catalog, found {} users", allUsers.size());
	}

	@VisibleForTesting
	public void setBroadcastSender(IBroadcastSender theBroadcastSender) {
		myBroadcastSender = theBroadcastSender;
	}

	@VisibleForTesting
	public void setLocalDbAuthService(ILocalDatabaseAuthorizationService theLocalDbAuthService) {
		myLocalDbAuthService = theLocalDbAuthService;
	}

	@VisibleForTesting
	public void setPersSvc(IDao thePersSvc) {
		myDao = thePersSvc;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void deleteUser(long thePid) throws ProcessingException {
		ourLog.info("Deleting user {}", thePid);

		PersUser user = myDao.getUser(thePid);
		if (user == null) {
			throw new ProcessingException("Unknown user: " + thePid);
		}

		myDao.deleteUser(user);

	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@Override
	public void saveAuthenticationHost(BasePersAuthenticationHost theAuthHost) {
		if (theAuthHost.getPid() != null) {
			BasePersAuthenticationHost existingHost = myDao.getAuthenticationHostByPid(theAuthHost.getPid());
			existingHost.merge(theAuthHost);
			ourLog.info("Saving existing authentication host of type {} with id {} / {}", new Object[] { theAuthHost.getClass().getSimpleName(), theAuthHost.getPid(), theAuthHost.getModuleId() });
			myDao.saveAuthenticationHost(existingHost);
		} else {
			ourLog.info("Saving new authentication host of type {} with id {} / {}", new Object[] { theAuthHost.getClass().getSimpleName(), theAuthHost.getPid(), theAuthHost.getModuleId() });
			myDao.saveAuthenticationHost(theAuthHost);
		}
		
		synchronizeUserCatalogChanged();
	}

	@Override
	public void forceLoadUserCatalog() {
		incrementStateVersion();
		loadUserCatalogIfNeeded();
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@Override
	public void deleteAuthenticationHost(long thePid) throws UnknownPidException {
		BasePersAuthenticationHost authHost = myDao.getAuthenticationHostByPid(thePid);
		if (authHost == null) {
			ourLog.info("Invalid request to delete unknown authentication host with PID {}", thePid);
			throw new UnknownPidException("Unknown authentication host: " + thePid);
		}

		ourLog.info("Removing authentication host {} / {}", thePid, authHost.getModuleId());

		myDao.deleteAuthenticationHost(authHost);

		synchronizeUserCatalogChanged();
	}

	@Override
	public List<BasePersAuthenticationHost> getAllAuthenticationHosts() {
		return myInMemoryAuthenticationHostList;
	}

}
