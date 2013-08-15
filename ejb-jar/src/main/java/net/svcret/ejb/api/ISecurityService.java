package net.svcret.ejb.api;

import java.util.List;

import javax.ejb.Local;

import net.svcret.admin.shared.model.AuthorizationOutcomeEnum;
import net.svcret.ejb.ex.ProcessingException;
import net.svcret.ejb.model.entity.BasePersAuthenticationHost;
import net.svcret.ejb.model.entity.PersServiceVersionMethod;
import net.svcret.ejb.model.entity.PersUser;

@Local
public interface ISecurityService {

	AuthorizationResultsBean authorizeMethodInvocation(List<AuthorizationRequestBean> theAuthorizationRequests, PersServiceVersionMethod theMethod, String theRequestHostIp) throws ProcessingException;

	void loadUserCatalogIfNeeded();

	PersUser saveServiceUser(PersUser theUser) throws ProcessingException;

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

}
