package net.svcret.ejb.model.entity;

import static org.junit.Assert.*;

import java.sql.SQLException;

import net.svcret.admin.shared.model.ServiceProtocolEnum;
import net.svcret.ejb.ejb.BaseJpaTest;
import net.svcret.ejb.ejb.DaoBean;
import net.svcret.ejb.ex.ProcessingException;
import net.svcret.ejb.model.entity.soap.PersServiceVersionSoap11;

import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("unused")
public class PersUserTest extends BaseJpaTest {

	private DaoBean mySvc;
	private PersEnvironment e0;
	private PersEnvironment e1;
	private PersDomain d0;
	private PersService d0s0;
	private PersService d0s1;
	private PersServiceVersionSoap11 d0s0v0;
	private PersServiceVersionSoap11 d0s0v1;
	private PersServiceVersionSoap11 d0s1v0;
	private PersServiceVersionSoap11 d0s1v1;
	private PersServiceVersionMethod d0s0v0m0;
	private PersDomain d1;
	private PersService d1s0;
	private PersServiceVersionSoap11 d1s0v0;
	private PersServiceVersionMethod d1s0v0m0;
	private PersAuthenticationHostLocalDatabase myAh;

	@Before
	public void before2() throws SQLException, ProcessingException {
		mySvc = new DaoBean();

		newEntityManager();

		e0 = mySvc.getOrCreateEnvironment("E0");
		e1 = mySvc.getOrCreateEnvironment("E1");
		d0 = mySvc.getOrCreateDomainWithId("D0");
		d0s0 = mySvc.getOrCreateServiceWithId(d0, "D0S0");
		d0s1 = mySvc.getOrCreateServiceWithId(d0, "D0S1");
		d0s0v0 = (PersServiceVersionSoap11) mySvc.getOrCreateServiceVersionWithId(d0s0, "D0S0V0", ServiceProtocolEnum.SOAP11);
		d0s0v1 = (PersServiceVersionSoap11) mySvc.getOrCreateServiceVersionWithId(d0s0, "D0S0V1", ServiceProtocolEnum.SOAP11);
		d0s1v0 = (PersServiceVersionSoap11) mySvc.getOrCreateServiceVersionWithId(d0s0, "D0S1V0", ServiceProtocolEnum.SOAP11);
		d0s1v1 = (PersServiceVersionSoap11) mySvc.getOrCreateServiceVersionWithId(d0s0, "D0S1V1", ServiceProtocolEnum.SOAP11);

		d0s0v0m0 = d0s0v0.getOrCreateAndAddMethodWithName("D0S0V0M0");
		mySvc.saveServiceVersion(d0s0v0);

		d1 = mySvc.getOrCreateDomainWithId("D1");
		d1s0 = mySvc.getOrCreateServiceWithId(d1, "D1S0");
		d1s0v0 = (PersServiceVersionSoap11) mySvc.getOrCreateServiceVersionWithId(d1s0, "D1S0V0", ServiceProtocolEnum.SOAP11);
		d1s0v0m0 = d1s0v0.getOrCreateAndAddMethodWithName("D0S0V0M0");

		myAh = mySvc.getOrCreateAuthenticationHostLocalDatabase("ah0");

	}

	@Override
	protected void newEntityManager() {
		super.newEntityManager();
		mySvc.setEntityManager(myEntityManager);
	}

	@Test
	public void testUserPerms() throws ProcessingException {

		newEntityManager();
		PersUser user = mySvc.getOrCreateUser(mySvc.getAuthenticationHost("ah0"), "Username");
		user.loadAllAssociations();

		assertEquals(false, user.hasPermission(d0s0v0m0, false));
		assertEquals(false, user.hasPermission(d1s0v0m0,false));

		user.setAllowAllDomains(true);
		mySvc.saveServiceUser(user);

		newEntityManager();
		user = mySvc.getOrCreateUser(mySvc.getAuthenticationHost("ah0"), "Username");
		user.loadAllAssociations();

		assertEquals(true, user.hasPermission(d0s0v0m0,false));
		assertEquals(true, user.hasPermission(d1s0v0m0,false));

	}

}
