package net.svcret.ejb.api;

import javax.ejb.Local;

import net.svcret.admin.shared.model.AuthorizationOutcomeEnum;
import net.svcret.ejb.ex.ProcessingException;
import net.svcret.ejb.model.entity.BasePersAuthenticationHost;
import net.svcret.ejb.model.entity.PersServiceVersionMethod;
import net.svcret.ejb.model.entity.PersUser;

@Local
public interface ISecurityService {

	AuthorizationResultsBean authorizeMethodInvocation(BasePersAuthenticationHost theAuthHost, ICredentialGrabber theCredentialGrabber, PersServiceVersionMethod theMethod, String theRequestHostIp) throws ProcessingException;

	void loadUserCatalogIfNeeded();

	PersUser saveServiceUser(PersUser theUser) throws ProcessingException;

	public class AuthorizationResultsBean {

		private AuthorizationOutcomeEnum myAuthorized;
		private PersUser myUser;

		/**
		 * @return the user
		 */
		public PersUser getUser() {
			return myUser;
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

		/**
		 * @param theUser
		 *            the user to set
		 */
		public void setUser(PersUser theUser) {
			myUser = theUser;
		}

	}

}
