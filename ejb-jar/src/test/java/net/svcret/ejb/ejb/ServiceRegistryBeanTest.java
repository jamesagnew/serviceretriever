package net.svcret.ejb.ejb;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.validateMockitoUsage;
import net.svcret.ejb.api.IDao;
import net.svcret.ejb.api.IHttpClient;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class ServiceRegistryBeanTest {

	private ServiceRegistryBean mySvc;
	private IHttpClient myHttpClient;
	private IDao myPersistence;

	@Before
	public void before() {

		mySvc = new ServiceRegistryBean();

		myHttpClient = mock(IHttpClient.class, DefaultAnswer.INSTANCE);
		mySvc.setSvcHttpClient(myHttpClient);

		myPersistence = mock(IDao.class, DefaultAnswer.INSTANCE);
		mySvc.setDao(myPersistence);

		DefaultAnswer.setDesignTime();
	}
	
	@Test
	public void testIt() {
		// nothing
	}
	
	
	@After
	public void after() {
		DefaultAnswer.setDesignTime();
		validateMockitoUsage();
	}

}
