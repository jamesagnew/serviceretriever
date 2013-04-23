package net.svcret.ejb.ejb;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import net.svcret.ejb.ex.ProcessingException;
import net.svcret.ejb.model.entity.BasePersAuthenticationHost;
import net.svcret.ejb.model.entity.PersAuthenticationHostLocalDatabase;
import net.svcret.ejb.model.entity.PersUser;

import org.junit.Before;
import org.junit.Test;

public class LocalDatabaseAuthorizationServiceBeanTest {

//	private IDao myPersSvc;
	private LocalDatabaseAuthorizationServiceBean mySvc;
private MyCredentialGrabber goodCredentialGrabber;
private InMemoryUserCatalog userCatalog;
private PersAuthenticationHostLocalDatabase host;
private PersUser user;
private HashMap<Long, Map<String, PersUser>> authHostToUsernameToUser;
private HashMap<Long, BasePersAuthenticationHost> authHosts;
private MyCredentialGrabber badCredentialGrabber;
private PersAuthenticationHostLocalDatabase host2;

	@Before
	public void before() throws ProcessingException {
		mySvc = new LocalDatabaseAuthorizationServiceBean();
		
		host = new PersAuthenticationHostLocalDatabase();
		host.setPid(111L);
		host.setModuleId("hostid");
		host.setModuleName("hostname");
		host.setCacheSuccessfulCredentialsForMillis(null);

		host2 = new PersAuthenticationHostLocalDatabase();
		host2.setPid(112L);
		host2.setModuleId("hostid2");
		host2.setModuleName("hostname2");
		host2.setCacheSuccessfulCredentialsForMillis(null);

		user = new PersUser();
		user.setUsername("username123");
		user.setPassword("password123");
		
		authHostToUsernameToUser = new HashMap<Long, Map<String,PersUser>>();
		authHostToUsernameToUser.put(host.getPid(), new HashMap<String, PersUser>());
		authHostToUsernameToUser.get(host.getPid()).put("username123", user);
		
		authHosts = new HashMap<Long, BasePersAuthenticationHost>();
		
		userCatalog = new InMemoryUserCatalog(authHostToUsernameToUser, authHosts);
		goodCredentialGrabber = new MyCredentialGrabber("username123", "password123");
		badCredentialGrabber = new MyCredentialGrabber("username321", "password321");

	}

	@Test
	public void testPasswordCheck() throws ProcessingException {
		
		assertNotNull(mySvc.authorize(host, userCatalog, goodCredentialGrabber));
		assertNull(mySvc.authorize(host, userCatalog, badCredentialGrabber));
		
		assertNull( mySvc.authorize(host2, userCatalog, goodCredentialGrabber));
		assertNull( mySvc.authorize(host2, userCatalog, badCredentialGrabber));
		
	}
	
}
