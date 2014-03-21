package net.svcret.core.invoker.crud;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.StringReader;
import java.util.HashMap;
import java.util.List;

import net.svcret.core.api.IDao;
import net.svcret.core.api.IServiceRegistry;
import net.svcret.core.api.RequestType;
import net.svcret.core.api.SrBeanIncomingRequest;
import net.svcret.core.api.SrBeanIncomingResponse;
import net.svcret.core.api.SrBeanProcessedRequest;
import net.svcret.core.api.SrBeanProcessedResponse;
import net.svcret.core.model.entity.PersMethod;
import net.svcret.core.model.entity.crud.PersServiceVersionRest;

import org.junit.Before;
import org.junit.Test;

public class ServiceInvokerRestTest {

	private ServiceInvokerRest mySvc;
	private IDao myDao;
	private IServiceRegistry myServiceRegistry;

	@Before
	public void before() {
		mySvc = new ServiceInvokerRest();
		
		myDao = mock(IDao.class);
		mySvc.setDaoForUnitTest(myDao);
		mySvc.setTransactionTemplateForUnitTest();

		myServiceRegistry = mock(IServiceRegistry.class);
		mySvc.setServiceRegistryForUnitTest(myServiceRegistry);;
		
	}
	
	@Test
	public void testRequest() throws Exception {
		
		String msgS = "{ \"url\" : \"http://foo/base/Resource/1\" }";

		PersServiceVersionRest svcVer = mock(PersServiceVersionRest.class);
		when(svcVer.getPid()).thenReturn(111L);
		
		PersServiceVersionRest dbSvcVer = new PersServiceVersionRest();
		dbSvcVer.setRewriteUrls(true);
		dbSvcVer.setPid(111L);
		when(myDao.getServiceVersionByPid(111L)).thenReturn(dbSvcVer);
		when(myServiceRegistry.saveServiceVersion(dbSvcVer)).thenReturn(dbSvcVer);
		
		SrBeanIncomingRequest req = new SrBeanIncomingRequest();
		req.setPath("/Some/System");
		req.setQuery("");
		req.addHeader("Content-Type", "application/json; charset=UTF-8");
		req.setRequestType(RequestType.POST);
		req.setInputReader(new StringReader(msgS));
		SrBeanProcessedRequest result = mySvc.processInvocation(req,svcVer);

	}

	

}
