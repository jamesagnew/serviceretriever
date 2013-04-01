package net.svcret.ejb.ejb;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;

import net.svcret.ejb.api.IAuthorizationService.ILocalDatabaseAuthorizationService;
import net.svcret.ejb.api.ICredentialGrabber;
import net.svcret.ejb.api.IServicePersistence;
import net.svcret.ejb.ex.ProcessingException;
import net.svcret.ejb.model.entity.BasePersAuthenticationHost;
import net.svcret.ejb.model.entity.PersAuthenticationHostLocalDatabase;
import net.svcret.ejb.model.entity.PersDomain;
import net.svcret.ejb.model.entity.PersService;
import net.svcret.ejb.model.entity.PersServiceVersionMethod;
import net.svcret.ejb.model.entity.PersUser;
import net.svcret.ejb.model.entity.PersUserDomainPermission;
import net.svcret.ejb.model.entity.PersUserServicePermission;
import net.svcret.ejb.model.entity.PersUserServiceVersionMethodPermission;
import net.svcret.ejb.model.entity.PersUserServiceVersionPermission;
import net.svcret.ejb.model.entity.soap.PersServiceVersionSoap11;

import org.junit.Before;
import org.junit.Test;

public class SecurityServiceBeanTest {

	private SecurityServiceBean mySvc;
	private ILocalDatabaseAuthorizationService myLocalDbAuthService;
	private IServicePersistence myPersSvc;
	private BasePersAuthenticationHost myHost;
	private ICredentialGrabber myGoodGrabber;
	private ArrayList<BasePersAuthenticationHost> myAuthHosts;
	private PersUser myUser;
	private ArrayList<PersUser> myUsers;
	private PersDomain myD0;
	private PersService myD0S0;
	private PersServiceVersionSoap11 myD0S0V0;
	private PersServiceVersionMethod myD0S0V0M0;
	private PersServiceVersionMethod myD0S0V0M1;
	private static int ourNextPid;

	@Before
	public void before() throws ProcessingException {
		DefaultAnswer.setDesignTime();
		
		myLocalDbAuthService = mock(ILocalDatabaseAuthorizationService.class, new DefaultAnswer());
		
		myPersSvc = mock(IServicePersistence.class, new DefaultAnswer());
		
		mySvc = new SecurityServiceBean();
		mySvc.setLocalDbAuthService(myLocalDbAuthService);
		mySvc.setPersSvc(myPersSvc);
		
		myHost = new PersAuthenticationHostLocalDatabase("hostid");
		myHost.setModuleName("hostname1");
		myHost.setPid(111L);
		
		myAuthHosts = new ArrayList<BasePersAuthenticationHost>();
		myAuthHosts.add(myHost);
		when(myPersSvc.getAllAuthenticationHosts()).thenReturn(myAuthHosts);

		myUser = new PersUser();
		myUser.setPid(211L);
		myUser.setUsername("username123");
		myUser.setPassword("password123");
		myUser.setAuthenticationHost(myHost);
		
		myUsers = new ArrayList<PersUser>();
		myUsers.add(myUser);
		when(myPersSvc.getAllServiceUsers()).thenReturn(myUsers);
		
		myGoodGrabber = new MyCredentialGrabber("username123", "password123");
		
		myD0 = new PersDomain(ourNextPid++, "d0");
		myD0S0 = new PersService(ourNextPid++, myD0, "d0s0", "d0s0");
		myD0S0V0 = new PersServiceVersionSoap11(ourNextPid++, myD0S0, "d0s0v0");
		myD0S0V0M0 = new PersServiceVersionMethod(ourNextPid++, myD0S0V0, "d0s0v0m0");
		myD0S0V0M1 = new PersServiceVersionMethod(ourNextPid++, myD0S0V0, "d0s0v0m1");
		
	}
	
	@Test
	public void testAuthorizeNone() throws ProcessingException {
				
		when(dbServiceAuthorizeMethod()).thenReturn(myUser);
		myUser.loadAllAssociations();
		
		DefaultAnswer.setRunTime();
		mySvc.loadUserCatalog();

		assertFalse(mySvc.authorizeMethodInvocation(myHost, myGoodGrabber, myD0S0V0M0));
	}

	@Test
	public void testAuthorizeAllowMethod() throws ProcessingException {
				
		when(dbServiceAuthorizeMethod()).thenReturn(myUser);
		
		PersUserDomainPermission domainPer = myUser.addPermission(myD0);
		PersUserServicePermission servicePer = domainPer.addPermission(myD0S0);
		PersUserServiceVersionPermission versionPer = servicePer.addPermission(myD0S0V0);
		PersUserServiceVersionMethodPermission methodPerm = versionPer.addPermission(myD0S0V0M0);
		
		myUser.loadAllAssociations();
		
		DefaultAnswer.setRunTime();
		mySvc.loadUserCatalog();

		assertTrue(mySvc.authorizeMethodInvocation(myHost, myGoodGrabber, myD0S0V0M0));
		assertFalse(mySvc.authorizeMethodInvocation(myHost, myGoodGrabber, myD0S0V0M1));
		
	}

	@Test
	public void testAuthorizeAllowAllMethods() throws ProcessingException {
				
		when(dbServiceAuthorizeMethod()).thenReturn(myUser);
		
		PersUserDomainPermission domainPer = myUser.addPermission(myD0);
		PersUserServicePermission servicePer = domainPer.addPermission(myD0S0);
		PersUserServiceVersionPermission versionPer = servicePer.addPermission(myD0S0V0);
		versionPer.setAllowAllServiceVersionMethods(true);
		
		myUser.loadAllAssociations();
		
		DefaultAnswer.setRunTime();
		mySvc.loadUserCatalog();

		assertTrue(mySvc.authorizeMethodInvocation(myHost, myGoodGrabber, myD0S0V0M0));
		assertTrue(mySvc.authorizeMethodInvocation(myHost, myGoodGrabber, myD0S0V0M1));
		
	}

	
	@Test
	public void testAuthorizeAllowAllDomains() throws ProcessingException {
				
		when(dbServiceAuthorizeMethod()).thenReturn(myUser);
		myUser.loadAllAssociations();
		myUser.setAllowAllDomains(true);
		
		DefaultAnswer.setRunTime();
		mySvc.loadUserCatalog();

		assertTrue(mySvc.authorizeMethodInvocation(myHost, myGoodGrabber, myD0S0V0M0));
	}

	private PersUser dbServiceAuthorizeMethod() throws ProcessingException {
		return myLocalDbAuthService.authorize(any(BasePersAuthenticationHost.class), any(InMemoryUserCatalog.class), any(ICredentialGrabber.class));
	}
	
}
