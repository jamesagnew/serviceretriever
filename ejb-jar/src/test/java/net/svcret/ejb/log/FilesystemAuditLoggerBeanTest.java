package net.svcret.ejb.log;

import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;

import net.svcret.admin.shared.enm.AuthorizationOutcomeEnum;
import net.svcret.admin.shared.enm.ResponseTypeEnum;
import net.svcret.ejb.api.SrBeanIncomingRequest;
import net.svcret.ejb.api.SrBeanIncomingResponse;
import net.svcret.ejb.api.IConfigService;
import net.svcret.ejb.api.SrBeanProcessedResponse;
import net.svcret.ejb.api.SrBeanProcessedRequest;
import net.svcret.ejb.ex.ProcessingException;
import net.svcret.ejb.ex.UnexpectedFailureException;
import net.svcret.ejb.log.FilesystemAuditLoggerBean;
import net.svcret.ejb.model.entity.PersConfig;
import net.svcret.ejb.model.entity.PersMethod;
import net.svcret.ejb.model.entity.PersServiceVersionUrl;
import net.svcret.ejb.model.entity.PersUser;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.stubbing.defaultanswers.ReturnsDeepStubs;

public class FilesystemAuditLoggerBeanTest {

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(FilesystemAuditLoggerBeanTest.class);
	private FilesystemAuditLoggerBean mySvc;

	private File myTempPath;

	@After
	public void after() throws IOException {
		ourLog.info("Cleaning up temp dir: {}", myTempPath);
		FileUtils.deleteDirectory(myTempPath);
	}

	@Before
	public void before() throws IOException, ProcessingException, UnexpectedFailureException {
		myTempPath = File.createTempFile("sr-unittest", "");
		myTempPath.delete();

		IConfigService cfgSvc = mock(IConfigService.class);
		mySvc = new FilesystemAuditLoggerBean();
		mySvc.setConfigServiceForUnitTests(cfgSvc);
		when(cfgSvc.getFilesystemAuditLoggerPath()).thenReturn(myTempPath.getAbsolutePath());
		when(cfgSvc.getConfig()).thenReturn(new PersConfig());

		mySvc.initialize();
	}

	@Test
	public void testRollFilesAsNeeded() throws Exception {

		SrBeanIncomingRequest request = mock(SrBeanIncomingRequest.class, new ReturnsDeepStubs());
		when(request.getRequestHostIp()).thenReturn("127.0.0.2");
		PersMethod method = mock(PersMethod.class, new ReturnsDeepStubs());
		when(method.getServiceVersion().getVersionId()).thenReturn("1.2");
		when(method.getServiceVersion().getService().getServiceId()).thenReturn("service1.0");
		when(method.getServiceVersion().getService().getDomain().getDomainId()).thenReturn("service1.0");
		PersUser user = mock(PersUser.class, new ReturnsDeepStubs());
		String requestBody = "this is the request body\nthis is line 2";
		SrBeanProcessedResponse invocationResponse = mock(SrBeanProcessedResponse.class, new ReturnsDeepStubs());
		when(invocationResponse.getResponseType()).thenReturn(ResponseTypeEnum.FAULT);
		PersServiceVersionUrl implementationUrl = mock(PersServiceVersionUrl.class, new ReturnsDeepStubs());
		when(implementationUrl.getUrlId()).thenReturn("url1");
		when(implementationUrl.getUrl()).thenReturn("http://foo");
		SrBeanIncomingResponse httpResponse = mock(SrBeanIncomingResponse.class, new ReturnsDeepStubs());
		AuthorizationOutcomeEnum authorizationOutcome = AuthorizationOutcomeEnum.AUTHORIZED;
		SrBeanProcessedRequest req = new SrBeanProcessedRequest();
		mySvc.recordServiceTransaction(request, method.getServiceVersion(), method, user, requestBody, invocationResponse, implementationUrl, httpResponse, authorizationOutcome, req);

		mySvc.recordServiceTransaction(request, method.getServiceVersion(), method, user, requestBody, invocationResponse, implementationUrl, httpResponse, authorizationOutcome, req);

		mySvc.forceFlush();
	}

}
