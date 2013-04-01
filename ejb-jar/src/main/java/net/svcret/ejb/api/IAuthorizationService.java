package net.svcret.ejb.api;

import net.svcret.ejb.ejb.InMemoryUserCatalog;
import net.svcret.ejb.ex.ProcessingException;
import net.svcret.ejb.model.entity.BasePersAuthenticationHost;
import net.svcret.ejb.model.entity.PersUser;

public interface IAuthorizationService {

	PersUser authorize(BasePersAuthenticationHost theHost, InMemoryUserCatalog theUserCatalog, ICredentialGrabber theCredentialGrabber) throws ProcessingException;

	public interface ILocalDatabaseAuthorizationService extends IAuthorizationService{
		
	}
	
	public interface ILdapAuthorizationService extends IAuthorizationService{
		
	}

}
