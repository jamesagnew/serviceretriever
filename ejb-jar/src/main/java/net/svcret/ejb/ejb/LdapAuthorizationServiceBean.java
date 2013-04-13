package net.svcret.ejb.ejb;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import javax.ejb.Singleton;
import javax.ejb.Stateless;

import net.svcret.ejb.api.IAuthorizationService.ILdapAuthorizationService;
import net.svcret.ejb.api.ICredentialGrabber;
import net.svcret.ejb.ex.ProcessingException;
import net.svcret.ejb.model.entity.BasePersAuthenticationHost;
import net.svcret.ejb.model.entity.PersAuthenticationHostLdap;
import net.svcret.ejb.model.entity.PersUser;

import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.ldap.client.api.LdapNetworkConnection;

@Stateless
public class LdapAuthorizationServiceBean extends BaseAuthorizationServiceBean<PersAuthenticationHostLdap> implements ILdapAuthorizationService {

	private ConcurrentHashMap<BasePersAuthenticationHost, MyLdapNetworkConnection> myHostToConnection = new ConcurrentHashMap<BasePersAuthenticationHost, MyLdapNetworkConnection>();
	
	@Override
	public PersUser authorize(BasePersAuthenticationHost theHost, InMemoryUserCatalog theUserCatalog, ICredentialGrabber theCredentialGrabber) throws ProcessingException {
		if (!(theHost instanceof PersAuthenticationHostLdap)) {
			throw new IllegalStateException("Auth host is not an LDAP host entry");
		}

		PersAuthenticationHostLdap ldapHost = (PersAuthenticationHostLdap)theHost;
		
		MyLdapNetworkConnection newConnection = new MyLdapNetworkConnection(ldapHost);
		MyLdapNetworkConnection connection = myHostToConnection.putIfAbsent(theHost, newConnection);
		if (connection == null) {
			connection = newConnection;
		}
		
		connection.setTimeOut(ldapHost.getLdapConnTimeout());
		connection.updateLastUsed();
		
		
		
		if (!connection.isConnected()) {
			try {
				if (!connection.connect()) {
					
				}
			} catch (LdapException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return null;
	}

	
	private static class MyLdapNetworkConnection extends LdapNetworkConnection
	{

		private PersAuthenticationHostLdap myHost;
		private volatile long myLastUsed;

		public MyLdapNetworkConnection(PersAuthenticationHostLdap theLdapHost) {
			super(theLdapHost.getLdapHost(), theLdapHost.getLdapPort());
			myHost =  theLdapHost;
		}

		public void updateLastUsed() {
			myLastUsed = System.currentTimeMillis();
		}
		
	}


	@Override
	protected PersUser doAuthorize(PersAuthenticationHostLdap theHost, InMemoryUserCatalog theUserCatalog, ICredentialGrabber theCredentialGrabber) throws ProcessingException {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	protected Class<PersAuthenticationHostLdap> getConfigType() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
