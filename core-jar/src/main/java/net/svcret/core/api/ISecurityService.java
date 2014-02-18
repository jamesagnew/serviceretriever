package net.svcret.core.api;

import java.util.List;

import net.svcret.admin.api.ProcessingException;
import net.svcret.admin.api.UnexpectedFailureException;
import net.svcret.admin.api.UnknownPidException;
import net.svcret.admin.shared.enm.AuthorizationOutcomeEnum;
import net.svcret.core.model.entity.BasePersAuthenticationHost;
import net.svcret.core.model.entity.PersMethod;
import net.svcret.core.model.entity.PersUser;

public interface ISecurityService {

	AuthorizationResultsBean authorizeMethodInvocation(List<AuthorizationRequestBean> theAuthorizationRequests, PersMethod theMethod, String theRequestHostIp) throws ProcessingException;

	void loadUserCatalogIfNeeded();

	PersUser saveServiceUser(PersUser theUser) throws UnexpectedFailureException;

	public class AuthorizationRequestBean {
		private BasePersAuthenticationHost myAuthenticationHost;
		private ICredentialGrabber myCredentialGrabber;

		public AuthorizationRequestBean(BasePersAuthenticationHost theAuthenticationHost, ICredentialGrabber theCredentialGrabber) {
			super();
			myAuthenticationHost = theAuthenticationHost;
			myCredentialGrabber = theCredentialGrabber;
		}

		public BasePersAuthenticationHost getAuthenticationHost() {
			return myAuthenticationHost;
		}

		public ICredentialGrabber getCredentialGrabber() {
			return myCredentialGrabber;
		}
	}

	public class AuthorizationResultsBean {

		private AuthorizationOutcomeEnum myAuthorized;
		private PersUser myAuthorizedUser;

		/**
		 * @return the user
		 */
		public PersUser getAuthorizedUser() {
			return myAuthorizedUser;
		}

		/**
		 * @return the authorized
		 */
		public AuthorizationOutcomeEnum isAuthorized() {
			return myAuthorized;
		}

		/**
		 * @param theAuthorized
		 *            the authorized to set
		 */
		public void setAuthorized(AuthorizationOutcomeEnum theAuthorized) {
			myAuthorized = theAuthorized;
		}

		public AuthorizationOutcomeEnum getAuthorized() {
			return myAuthorized;
		}

		/**
		 * @param theUser
		 *            the user to set
		 */
		public void setAuthorizedUser(PersUser theUser) {
			myAuthorizedUser = (theUser);
		}

	}

	void deleteUser(long thePid) throws ProcessingException;

	void saveAuthenticationHost(BasePersAuthenticationHost theHost);

	void forceLoadUserCatalog();

	void deleteAuthenticationHost(long thePid) throws UnknownPidException;

	List<BasePersAuthenticationHost> getAllAuthenticationHosts();

}
