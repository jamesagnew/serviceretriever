package ca.uhn.sail.proxy.api;

import javax.ejb.Local;

import ca.uhn.sail.proxy.model.entity.PersBaseServerAuth;

/**
 * Provides security verification for incoming service requests (i.e. authorizing clients
 * who are attempting to invoke proxied services)
 */
@Local
public interface IServiceHostSercurity {

	void authenticate(PersBaseServerAuth<?,?> theServerAuth, ICredentialGrabber theCredentialGrabber);
	
}
