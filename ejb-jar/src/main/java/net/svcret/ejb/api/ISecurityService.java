package net.svcret.ejb.api;

import javax.ejb.Local;

import net.svcret.ejb.ex.ProcessingException;
import net.svcret.ejb.model.entity.BasePersAuthenticationHost;
import net.svcret.ejb.model.entity.PersServiceVersionMethod;

@Local
public interface ISecurityService {

	boolean authorizeMethodInvocation(BasePersAuthenticationHost theAuthHost, ICredentialGrabber theCredentialGrabber, PersServiceVersionMethod theMethod) throws ProcessingException;

	void loadUserCatalogIfNeeded();

	
}
