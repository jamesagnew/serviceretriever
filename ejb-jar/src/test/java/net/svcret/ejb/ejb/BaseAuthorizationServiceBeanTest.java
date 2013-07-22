package net.svcret.ejb.ejb;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import net.svcret.ejb.api.ICredentialGrabber;
import net.svcret.ejb.ex.ProcessingException;
import net.svcret.ejb.model.entity.BasePersAuthenticationHost;
import net.svcret.ejb.model.entity.IThrottleable;
import net.svcret.ejb.model.entity.PersAuthenticationHostLdap;
import net.svcret.ejb.model.entity.PersUser;

import org.junit.Before;
import org.junit.Test;

public class BaseAuthorizationServiceBeanTest {

	private MyService mySvc;
	private PersAuthenticationHostLdap myConfig;
	private InMemoryUserCatalog myUserCatalog;
	private long ourNextPid;
	private PersUser myUser;

	@Before
	public void before() {
		mySvc = new MyService();
		myConfig = new PersAuthenticationHostLdap();
		myConfig.setPid(ourNextPid++);

		myUser = new PersUser();
		myUser.setPid(ourNextPid++);
		myUser.setUsername("username123");

		Map<Long, Map<String, PersUser>> users = new HashMap<Long, Map<String, PersUser>>();
		users.put(myConfig.getPid(), new HashMap<String, PersUser>());
		users.get(myConfig.getPid()).put(myUser.getUsername(), myUser);
		Map<Long, BasePersAuthenticationHost> hosts = new HashMap<Long, BasePersAuthenticationHost>();
		hosts.put(myConfig.getPid(), myConfig);
		myUserCatalog = new InMemoryUserCatalog(users, hosts);
	}

	@Test
	public void testCaching() throws ProcessingException, InterruptedException {
		myConfig.setCacheSuccessfulCredentialsForMillis(100);

		MyCredentialGrabber good = new MyCredentialGrabber("username123", "password123");
		MyCredentialGrabber bad = new MyCredentialGrabber("username321", "password321");

		assertEquals(0, mySvc.getInvocationCount());
		assertNotNull(mySvc.authorize(myConfig, myUserCatalog, good));
		assertEquals(1, mySvc.getInvocationCount());
		assertNull(mySvc.authorize(myConfig, myUserCatalog, bad));
		assertEquals(2, mySvc.getInvocationCount());

		/*
		 * Make sure we use the cache
		 */

		assertNotNull(mySvc.authorize(myConfig, myUserCatalog, good));
		assertEquals(2, mySvc.getInvocationCount());
		assertNull(mySvc.authorize(myConfig, myUserCatalog, bad));
		assertEquals(3, mySvc.getInvocationCount());

		Thread.sleep(200);

		/*
		 * Make sure we DON'T use the cache
		 */

		assertNotNull(mySvc.authorize(myConfig, myUserCatalog, good));
		assertEquals(4, mySvc.getInvocationCount());
		assertNull(mySvc.authorize(myConfig, myUserCatalog, bad));
		assertEquals(5, mySvc.getInvocationCount());

	}

	private class MyService extends BaseAuthorizationServiceBean<PersAuthenticationHostLdap> {
		private int myInvocationCount = 0;

		@Override
		protected IThrottleable doAuthorize(PersAuthenticationHostLdap theHost, InMemoryUserCatalog theUserCatalog, ICredentialGrabber theCredentialGrabber) throws ProcessingException {
			myInvocationCount++;

			if (!theCredentialGrabber.getUsername().equals("username123")) {
				return null;
			}

			if (!theCredentialGrabber.getPassword().equals("password123")) {
				return null;
			}

			return myUser;
		}

		/**
		 * @return the invocationCount
		 */
		public int getInvocationCount() {
			return myInvocationCount;
		}

		@Override
		protected Class<PersAuthenticationHostLdap> getConfigType() {
			return PersAuthenticationHostLdap.class;
		}

	}
}
