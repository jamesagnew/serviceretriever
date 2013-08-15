package net.svcret.ejb.ejb;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.svcret.admin.shared.enm.ServerSecurityModeEnum;
import net.svcret.admin.shared.model.AuthorizationOutcomeEnum;
import net.svcret.ejb.api.IAuthorizationService.ILocalDatabaseAuthorizationService;
import net.svcret.ejb.api.ICredentialGrabber;
import net.svcret.ejb.api.IDao;
import net.svcret.ejb.api.ISecurityService.AuthorizationRequestBean;
import net.svcret.ejb.ex.ProcessingException;
import net.svcret.ejb.model.entity.BasePersAuthenticationHost;
import net.svcret.ejb.model.entity.PersAuthenticationHostLocalDatabase;
import net.svcret.ejb.model.entity.PersDomain;
import net.svcret.ejb.model.entity.PersService;
import net.svcret.ejb.model.entity.PersServiceVersionMethod;
import net.svcret.ejb.model.entity.PersUser;
import net.svcret.ejb.model.entity.PersUserDomainPermission;
import net.svcret.ejb.model.entity.PersUserServicePermission;
import net.svcret.ejb.model.entity.PersUserServiceVersionPermission;
import net.svcret.ejb.model.entity.PersUserStatus;
import net.svcret.ejb.model.entity.soap.PersServiceVersionSoap11;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class SecurityServiceBeanTest {

	private SecurityServiceBean mySvc;
	private ILocalDatabaseAuthorizationService myLocalDbAuthService;
	private IDao myPersSvc;
	private PersAuthenticationHostLocalDatabase myHost;
	private ICredentialGrabber myGoodGrabber1;
	private ArrayList<BasePersAuthenticationHost> myAuthHosts;
	private PersUser myUser;
	private ArrayList<PersUser> myUsers;
	private PersDomain myD0;
	private PersService myD0S0;
	private PersServiceVersionSoap11 myD0S0V0;
	private PersServiceVersionMethod myD0S0V0M0;
	private PersServiceVersionMethod myD0S0V0M1;
	private PersUser myUser2;
	private MyCredentialGrabber myGoodGrabber2;
	private MyCredentialGrabber myBadGrabber;
	private static int ourNextPid;

	@Before
	public void before() throws ProcessingException {
		DefaultAnswer.setDesignTime();
		
		myLocalDbAuthService = mock(ILocalDatabaseAuthorizationService.class, new DefaultAnswer());
		
		myPersSvc = mock(IDao.class, new DefaultAnswer());
		when(myPersSvc.getStateCounter(anyString())).thenReturn(1L);
		
		mySvc = new SecurityServiceBean();
		mySvc.setLocalDbAuthService(myLocalDbAuthService);
		mySvc.setPersSvc(myPersSvc);
		
		myHost = new PersAuthenticationHostLocalDatabase("hostid");
		myHost.setModuleName("hostname1");
		myHost.setPid(111L);
		
		myAuthHosts = new ArrayList<BasePersAuthenticationHost>();
		myAuthHosts.add(myHost);
		when(myPersSvc.getAllAuthenticationHosts()).thenReturn(myAuthHosts);

		myUsers = new ArrayList<PersUser>();
		
		myUser = new PersUser();
		myUser.setPid(211L);
		myUser.setUsername("username123");
		myUser.setPassword("password123");
		myUser.setAuthenticationHost(myHost);
		myUser.setStatus(new PersUserStatus());
		myUsers.add(myUser);

		myUser2 = new PersUser();
		myUser2.setPid(212L);
		myUser2.setUsername("username123B");
		myUser2.setPassword("password123B");
		myUser2.setAuthenticationHost(myHost);
		myUser2.setStatus(new PersUserStatus());
		myUsers.add(myUser2);
		
		when(myPersSvc.getAllUsersAndInitializeThem()).thenReturn(myUsers);
		
		myGoodGrabber1 = new MyCredentialGrabber("username123", "password123");
		myGoodGrabber1 = new MyCredentialGrabber("username123", "password123");
		myBadGrabber = new MyCredentialGrabber("XXX", "XXX");
		
		myD0 = new PersDomain(ourNextPid++, "d0");
		myD0S0 = new PersService(ourNextPid++, myD0, "d0s0", "d0s0");
		myD0S0V0 = new PersServiceVersionSoap11(ourNextPid++, myD0S0, "d0s0v0");
		myD0S0V0.setServerSecurityMode(ServerSecurityModeEnum.REQUIRE_ANY);
		
		myD0S0V0M0 = new PersServiceVersionMethod(ourNextPid++, myD0S0V0, "d0s0v0m0");
		myD0S0V0.addMethod(myD0S0V0M0);
		myD0S0V0M1 = new PersServiceVersionMethod(ourNextPid++, myD0S0V0, "d0s0v0m1");
		myD0S0V0.addMethod(myD0S0V0M1);
		
	}
	
	@Test
	public void testInitWorks() throws ProcessingException {
		myLocalDbAuthService = mock(ILocalDatabaseAuthorizationService.class, new DefaultAnswer());
		
		myPersSvc = mock(IDao.class, new DefaultAnswer());
		when(myPersSvc.getStateCounter(anyString())).thenReturn(0L);
		when(myPersSvc.getAuthenticationHost(BasePersAuthenticationHost.MODULE_ID_ADMIN_AUTH)).thenReturn(myHost);
		when(myPersSvc.getOrCreateAuthenticationHostLocalDatabase(BasePersAuthenticationHost.MODULE_ID_ADMIN_AUTH)).thenReturn(myHost);
		
		PersUser value = new PersUser();
		value.setUsername("admin");
		when(myPersSvc.getOrCreateUser(myHost, "admin")).thenReturn(value);
		
		mySvc = new SecurityServiceBean();
		mySvc.setLocalDbAuthService(myLocalDbAuthService);
		mySvc.setPersSvc(myPersSvc);

		mySvc.loadUserCatalogIfNeeded();
		
		ArgumentCaptor<PersUser> captor = ArgumentCaptor.forClass(PersUser.class);
		verify(myPersSvc).saveServiceUser(captor.capture());
		
		assertEquals("admin", captor.getValue().getUsername());
		assertNotNull("admin", captor.getValue().getPasswordHash());
		
	}		
	
	@Test
	public void testAuthorizeNone() throws ProcessingException {
				
		when(dbServiceAuthorizeMethod(myGoodGrabber1)).thenReturn(myUser);
		myUser.loadAllAssociations();
		
		DefaultAnswer.setRunTime();
		mySvc.loadUserCatalog();

		assertEquals(AuthorizationOutcomeEnum.FAILED_USER_NO_PERMISSIONS, mySvc.authorizeMethodInvocation(toList(myHost, myGoodGrabber1), myD0S0V0M0,"").isAuthorized());
	}

	private List<AuthorizationRequestBean> toList(PersAuthenticationHostLocalDatabase theHost, ICredentialGrabber theGoodGrabber) {
		return Collections.singletonList(new AuthorizationRequestBean(theHost, theGoodGrabber));
	}

	private List<AuthorizationRequestBean> toList(PersAuthenticationHostLocalDatabase theHost, ICredentialGrabber theGrabber, PersAuthenticationHostLocalDatabase theHost2, ICredentialGrabber theGrabber2) {
		ArrayList<AuthorizationRequestBean> retVal = new ArrayList<AuthorizationRequestBean>();
		retVal.add(new AuthorizationRequestBean(theHost, theGrabber));
		retVal.add(new AuthorizationRequestBean(theHost2, theGrabber2));
		return retVal;
	}

	@Test
	public void testAuthorizeAllowMethod() throws ProcessingException {
				
		when(dbServiceAuthorizeMethod(myGoodGrabber1)).thenReturn(myUser);
		
		PersUserDomainPermission domainPer = myUser.addPermission(myD0);
		PersUserServicePermission servicePer = domainPer.addPermission(myD0S0);
		PersUserServiceVersionPermission versionPer = servicePer.addPermission(myD0S0V0);
//		PersUserServiceVersionMethodPermission methodPerm = versionPer.addPermission(myD0S0V0M0);
		
		myUser.loadAllAssociations();
		
		DefaultAnswer.setRunTime();
		mySvc.loadUserCatalog();

		assertEquals(AuthorizationOutcomeEnum.AUTHORIZED, mySvc.authorizeMethodInvocation(toList(myHost, myGoodGrabber1), myD0S0V0M0,"").isAuthorized());
		assertEquals(AuthorizationOutcomeEnum.FAILED_USER_NO_PERMISSIONS, mySvc.authorizeMethodInvocation(toList(myHost, myGoodGrabber1), myD0S0V0M1,"").isAuthorized());
		
		versionPer.removePermission(myD0S0V0M0);
		myUser.loadAllAssociations();
		assertEquals(AuthorizationOutcomeEnum.FAILED_USER_NO_PERMISSIONS, mySvc.authorizeMethodInvocation(toList(myHost, myGoodGrabber1), myD0S0V0M0,"").isAuthorized());
		assertEquals(AuthorizationOutcomeEnum.FAILED_USER_NO_PERMISSIONS, mySvc.authorizeMethodInvocation(toList(myHost, myGoodGrabber1), myD0S0V0M1,"").isAuthorized());
	}

	@Test
	public void testAuthorizeRequireAny() throws ProcessingException {
				
		when(dbServiceAuthorizeMethod(myGoodGrabber1)).thenReturn(myUser);
		when(dbServiceAuthorizeMethod(myGoodGrabber2)).thenReturn(myUser);
		when(dbServiceAuthorizeMethod(myBadGrabber)).thenReturn(null);
		
		myUser.loadAllAssociations();
		myUser.setAllowAllDomains(true);
		myD0S0V0.setServerSecurityMode(ServerSecurityModeEnum.REQUIRE_ANY);

		DefaultAnswer.setRunTime();
		mySvc.loadUserCatalog();

		assertEquals(AuthorizationOutcomeEnum.AUTHORIZED, mySvc.authorizeMethodInvocation(toList(myHost, myGoodGrabber1, myHost, myGoodGrabber2), myD0S0V0M0,"").isAuthorized());
		assertEquals(AuthorizationOutcomeEnum.AUTHORIZED, mySvc.authorizeMethodInvocation(toList(myHost, myGoodGrabber1, myHost, myBadGrabber), myD0S0V0M0,"").isAuthorized());
		assertEquals(AuthorizationOutcomeEnum.FAILED_BAD_CREDENTIALS_IN_REQUEST, mySvc.authorizeMethodInvocation(toList(myHost, myBadGrabber, myHost, myBadGrabber), myD0S0V0M0,"").isAuthorized());
		
	}

	@Test
	public void testAuthorizeRequireAll() throws ProcessingException {
		when(dbServiceAuthorizeMethod(myGoodGrabber1)).thenReturn(myUser);
		when(dbServiceAuthorizeMethod(myGoodGrabber2)).thenReturn(myUser);
		when(dbServiceAuthorizeMethod(myBadGrabber)).thenReturn(null);
		
		myUser.loadAllAssociations();
		myUser.setAllowAllDomains(true);
		myD0S0V0.setServerSecurityMode(ServerSecurityModeEnum.REQUIRE_ALL);

		DefaultAnswer.setRunTime();
		mySvc.loadUserCatalog();

		assertEquals(AuthorizationOutcomeEnum.AUTHORIZED, mySvc.authorizeMethodInvocation(toList(myHost, myGoodGrabber1, myHost, myGoodGrabber2), myD0S0V0M0,"").isAuthorized());
		assertEquals(AuthorizationOutcomeEnum.FAILED_BAD_CREDENTIALS_IN_REQUEST, mySvc.authorizeMethodInvocation(toList(myHost, myGoodGrabber1, myHost, myBadGrabber), myD0S0V0M0,"").isAuthorized());
		assertEquals(AuthorizationOutcomeEnum.FAILED_BAD_CREDENTIALS_IN_REQUEST, mySvc.authorizeMethodInvocation(toList(myHost, myBadGrabber, myHost, myBadGrabber), myD0S0V0M0,"").isAuthorized());
	}
	
	@Test
	public void testAuthorizeAllowAny() throws ProcessingException {
		when(dbServiceAuthorizeMethod(myGoodGrabber1)).thenReturn(myUser);
		when(dbServiceAuthorizeMethod(myGoodGrabber2)).thenReturn(myUser);
		when(dbServiceAuthorizeMethod(myBadGrabber)).thenReturn(null);
		
		myUser.loadAllAssociations();
		myUser.setAllowAllDomains(true);
		myD0S0V0.setServerSecurityMode(ServerSecurityModeEnum.ALLOW_ANY);

		DefaultAnswer.setRunTime();
		mySvc.loadUserCatalog();

		assertEquals(AuthorizationOutcomeEnum.AUTHORIZED, mySvc.authorizeMethodInvocation(toList(myHost, myGoodGrabber1, myHost, myGoodGrabber2), myD0S0V0M0,"").isAuthorized());
		assertEquals(AuthorizationOutcomeEnum.AUTHORIZED, mySvc.authorizeMethodInvocation(toList(myHost, myGoodGrabber1, myHost, myBadGrabber), myD0S0V0M0,"").isAuthorized());
		assertEquals(AuthorizationOutcomeEnum.AUTHORIZED, mySvc.authorizeMethodInvocation(toList(myHost, myBadGrabber, myHost, myBadGrabber), myD0S0V0M0,"").isAuthorized());
	}

	@Test
	public void testAuthorizeAllowAllMethods() throws ProcessingException {
				
		when(dbServiceAuthorizeMethod(myGoodGrabber1)).thenReturn(myUser);
		
		PersUserDomainPermission domainPer = myUser.addPermission(myD0);
		PersUserServicePermission servicePer = domainPer.addPermission(myD0S0);
		PersUserServiceVersionPermission versionPer = servicePer.addPermission(myD0S0V0);
		versionPer.setAllowAllServiceVersionMethods(true);
		
		myUser.loadAllAssociations();
		
		DefaultAnswer.setRunTime();
		mySvc.loadUserCatalog();

		assertEquals(AuthorizationOutcomeEnum.AUTHORIZED, mySvc.authorizeMethodInvocation(toList(myHost, myGoodGrabber1), myD0S0V0M0,"").isAuthorized());
		assertEquals(AuthorizationOutcomeEnum.AUTHORIZED, mySvc.authorizeMethodInvocation(toList(myHost, myGoodGrabber1), myD0S0V0M1,"").isAuthorized());
		
	}

	
	@Test
	public void testAuthorizeAllowAllDomains() throws ProcessingException {
				
		when(dbServiceAuthorizeMethod(myGoodGrabber1)).thenReturn(myUser);
		myUser.loadAllAssociations();
		myUser.setAllowAllDomains(true);
		
		DefaultAnswer.setRunTime();
		mySvc.loadUserCatalog();

		assertEquals(AuthorizationOutcomeEnum.AUTHORIZED, mySvc.authorizeMethodInvocation(toList(myHost, myGoodGrabber1), myD0S0V0M0,"").isAuthorized());
	}

	private PersUser dbServiceAuthorizeMethod(ICredentialGrabber theCredentialGrabber) throws ProcessingException {
		return myLocalDbAuthService.authorize(any(BasePersAuthenticationHost.class), any(InMemoryUserCatalog.class), same(theCredentialGrabber));
	}
	
}
