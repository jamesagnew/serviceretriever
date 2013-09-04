package net.svcret.ejb.api;

import net.svcret.admin.shared.model.AuthorizationOutcomeEnum;
import net.svcret.ejb.ejb.InMemoryUserCatalog;
import net.svcret.ejb.ex.ProcessingException;
import net.svcret.ejb.model.entity.BasePersAuthenticationHost;
import net.svcret.ejb.model.entity.PersUser;

public interface IAuthorizationService {

	UserOrFailure authorize(BasePersAuthenticationHost theHost, InMemoryUserCatalog theUserCatalog, ICredentialGrabber theCredentialGrabber) throws ProcessingException;

	public interface ILdapAuthorizationService extends IAuthorizationService{
		
	}
	
	public interface ILocalDatabaseAuthorizationService extends IAuthorizationService{
		
	}
	
	
	public static class UserOrFailure
	{
		private final AuthorizationOutcomeEnum myFailure;
		
		private final PersUser myUser;

		public UserOrFailure(AuthorizationOutcomeEnum theFailure) {
			super();
			myUser = null;
			myFailure = theFailure;
		}

		public UserOrFailure(PersUser theUser) {
			super();
			myUser = theUser;
			myFailure = null;
		}
		public AuthorizationOutcomeEnum getFailure() {
			return myFailure;
		}
		public PersUser getUser() {
			return myUser;
		}
	}

}
