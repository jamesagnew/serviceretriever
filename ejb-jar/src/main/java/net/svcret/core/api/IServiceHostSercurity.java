package net.svcret.core.api;

import net.svcret.core.model.entity.PersBaseServerAuth;


/**
 * Provides security verification for incoming service requests (i.e. authorizing clients
 * who are attempting to invoke proxied services)
 */
public interface IServiceHostSercurity {

	void authenticate(PersBaseServerAuth<?,?> theServerAuth, ICredentialGrabber theCredentialGrabber);
	
}
