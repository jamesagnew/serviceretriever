package net.svcret.ejb.ejb;

import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;

import net.svcret.admin.shared.model.AuthorizationOutcomeEnum;
import net.svcret.ejb.api.HttpRequestBean;
import net.svcret.ejb.api.HttpResponseBean;
import net.svcret.ejb.api.IConfigService;
import net.svcret.ejb.api.InvocationResponseResultsBean;
import net.svcret.ejb.ex.ProcessingException;
import net.svcret.ejb.model.entity.PersServiceVersionMethod;
import net.svcret.ejb.model.entity.PersServiceVersionUrl;
import net.svcret.ejb.model.entity.PersUser;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.stubbing.defaultanswers.ReturnsDeepStubs;

public class FilesystemAuditLoggerBeanTest {

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(FilesystemAuditLoggerBeanTest.class);
	private SimpleDateFormat myFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private FilesystemAuditLoggerBean mySvc;

	private File myTempPath;

	@After
	public void after() throws IOException {
		ourLog.info("Cleaning up temp dir: {}", myTempPath);
		FileUtils.deleteDirectory(myTempPath);
	}

	@Before
	public void before() throws IOException, ProcessingException {
		myTempPath = File.createTempFile("sr-unittest", "");
		myTempPath.delete();

		IConfigService cfgSvc = mock(IConfigService.class);
		mySvc = new FilesystemAuditLoggerBean();
		mySvc.setConfigServiceForUnitTests(cfgSvc);
		when(cfgSvc.getFilesystemAuditLoggerPath()).thenReturn(myTempPath.getAbsolutePath());

		mySvc.initialize();
	}

	@Test
	public void testRollFilesAsNeeded() throws IOException, ProcessingException {

		HttpRequestBean request = mock(HttpRequestBean.class, new ReturnsDeepStubs());
		PersServiceVersionMethod method = mock(PersServiceVersionMethod.class, new ReturnsDeepStubs());
		PersUser user = mock(PersUser.class, new ReturnsDeepStubs());
		String requestBody = "this is the request body\nthis is line 2";
		InvocationResponseResultsBean invocationResponse = mock(InvocationResponseResultsBean.class, new ReturnsDeepStubs());
		PersServiceVersionUrl implementationUrl = mock(PersServiceVersionUrl.class, new ReturnsDeepStubs());
		HttpResponseBean httpResponse = mock(HttpResponseBean.class, new ReturnsDeepStubs());
		AuthorizationOutcomeEnum authorizationOutcome = AuthorizationOutcomeEnum.AUTHORIZED;
		mySvc.recordServiceTransaction(request, method.getServiceVersion(), method, user, requestBody, invocationResponse, implementationUrl, httpResponse, authorizationOutcome);

		mySvc.recordServiceTransaction(request, method.getServiceVersion(), method, user, requestBody, invocationResponse, implementationUrl, httpResponse, authorizationOutcome);

		mySvc.forceFlush();
	}

}
