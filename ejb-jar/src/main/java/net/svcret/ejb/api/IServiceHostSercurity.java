package net.svcret.ejb.api;

import net.svcret.ejb.model.entity.PersBaseServerAuth;


/**
 * Provides security verification for incoming service requests (i.e. authorizing clients
 * who are attempting to invoke proxied services)
 */
public interface IServiceHostSercurity {

	void authenticate(PersBaseServerAuth<?,?> theServerAuth, ICredentialGrabber theCredentialGrabber);
	
}
