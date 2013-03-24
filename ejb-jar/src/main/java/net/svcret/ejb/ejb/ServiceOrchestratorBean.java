package net.svcret.ejb.ejb;

import java.io.IOException;
import java.io.Reader;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import net.svcret.ejb.api.HttpResponseBean;
import net.svcret.ejb.api.IHttpClient;
import net.svcret.ejb.api.IResponseValidator;
import net.svcret.ejb.api.IRuntimeStatus;
import net.svcret.ejb.api.IServiceInvoker;
import net.svcret.ejb.api.IServiceOrchestrator;
import net.svcret.ejb.api.IServiceRegistry;
import net.svcret.ejb.api.InvocationResponseResultsBean;
import net.svcret.ejb.api.InvocationResultsBean;
import net.svcret.ejb.api.RequestType;
import net.svcret.ejb.api.UrlPoolBean;
import net.svcret.ejb.api.HttpResponseBean.Failure;
import net.svcret.ejb.ex.InternalErrorException;
import net.svcret.ejb.ex.ProcessingException;
import net.svcret.ejb.ex.UnknownRequestException;
import net.svcret.ejb.model.entity.BasePersServiceVersion;
import net.svcret.ejb.model.entity.PersHttpClientConfig;
import net.svcret.ejb.model.entity.PersServiceVersionMethod;
import net.svcret.ejb.model.entity.PersServiceVersionResource;
import net.svcret.ejb.model.entity.PersServiceVersionUrl;
import net.svcret.ejb.model.entity.soap.PersServiceVersionSoap11;
import net.svcret.ejb.util.Validate;


@Stateless
public class ServiceOrchestratorBean implements IServiceOrchestrator {

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(ServiceOrchestratorBean.class);

	@EJB
	private IRuntimeStatus myRuntimeStatus;

	@EJB(name = "SOAP11Invoker")
	private IServiceInvoker<PersServiceVersionSoap11> mySoap11ServiceInvoker;

	@EJB
	private IServiceRegistry mySvcRegistry;

	@EJB
	private IHttpClient myHttpClient;

	@TransactionAttribute(TransactionAttributeType.NEVER)
	@Override
	public OrchestratorResponseBean handle(RequestType theRequestType, String thePath, String theQuery, Reader theReader) throws UnknownRequestException, InternalErrorException, ProcessingException, IOException {
		Validate.throwIllegalArgumentExceptionIfNull("RequestType", theRequestType);
		Validate.throwIllegalArgumentExceptionIfNull("Path", thePath);
		Validate.throwIllegalArgumentExceptionIfNull("Query", theQuery);
		Validate.throwIllegalArgumentExceptionIfNull("Reader", theReader);

		Date startTime = new Date();

		if (theQuery.length() > 0 && theQuery.charAt(0) != '?') {
			throw new IllegalArgumentException("Path must be blank or start with '?'");
		}
		String path;
		if (thePath.length() > 0 && thePath.charAt(thePath.length() - 1) == '/') {
			path = thePath.substring(0, thePath.length() - 1);
		} else {
			path = thePath;
		}

		BasePersServiceVersion serviceVersion = mySvcRegistry.getServiceVersionForPath(path);
		if (serviceVersion == null) {
			throw new UnknownRequestException(path);
		}

		InvocationResultsBean results;
		IServiceInvoker<?> serviceInvoker;
		switch (serviceVersion.getProtocol()) {
		case SOAP11:
			PersServiceVersionSoap11 serviceVersionSoap = (PersServiceVersionSoap11) serviceVersion;
			serviceInvoker = mySoap11ServiceInvoker;
			results = mySoap11ServiceInvoker.processInvocation(serviceVersionSoap, theRequestType, path, theQuery, theReader);
			break;
		default:
			throw new InternalErrorException("Unknown service protocol: " + serviceVersion.getProtocol());
		}

		OrchestratorResponseBean retVal;
		switch (results.getResultType()) {

		case STATIC_RESOURCE: {
			String responseBody = results.getStaticResourceDefinition().getResourceText();
			String responseContentType = results.getStaticResourceContentTyoe();
			Map<String, String> responseHeaders = results.getStaticResourceHeaders();
			retVal = new OrchestratorResponseBean(responseBody, responseContentType, responseHeaders);

			PersServiceVersionResource resource = results.getStaticResourceDefinition();
			myRuntimeStatus.recordInvocationStaticResource(startTime, resource);

			ourLog.debug("Handling request for static URL contents: {}", resource);
			break;
		}
		case METHOD: {
			PersServiceVersionMethod method = results.getMethodDefinition();
			Map<String, String> headers = results.getMethodHeaders();
			String contentType = results.getMethodContentType();
			String contentBody = results.getMethodRequestBody();
			IResponseValidator responseValidator = serviceInvoker.provideInvocationResponseValidator();

			UrlPoolBean urlPool = myRuntimeStatus.buildUrlPool(method.getServiceVersion());
			
			PersHttpClientConfig clientConfig = serviceVersion.getHttpClientConfig();
			urlPool.setConnectTimeoutMillis(clientConfig.getConnectTimeoutMillis());
			urlPool.setReadTimeoutMillis(clientConfig.getReadTimeoutMillis());
			urlPool.setFailureRetriesBeforeAborting(clientConfig.getFailureRetriesBeforeAborting());
			
			HttpResponseBean httpResponse;
			httpResponse = myHttpClient.post(responseValidator, urlPool, contentBody, headers, contentType);
			markUrlsFailed(method, httpResponse.getFailedUrls());

			if (httpResponse.getSuccessfulUrl() == null) {
				markUrlsFailed(method, httpResponse.getFailedUrls());
				throw new ProcessingException("All service URLs appear to be failing, unable to successfully invoke method");
			}

			InvocationResponseResultsBean invocationResponse = serviceInvoker.processInvocationResponse(httpResponse);

			int requestLength = contentBody.length();
			myRuntimeStatus.recordInvocationMethod(startTime, requestLength, method, null, httpResponse, invocationResponse);

			String responseBody = invocationResponse.getResponseBody();
			String responseContentType = invocationResponse.getResponseContentType();
			Map<String, String> responseHeaders = invocationResponse.getResponseHeaders();

			retVal = new OrchestratorResponseBean(responseBody, responseContentType, responseHeaders);
			break;
		}
		default: {
			throw new InternalErrorException("Unknown request type: " + results.getResultType());
		}
		}

		return retVal;
	}

	private void markUrlsFailed(PersServiceVersionMethod theMethod, Map<String, Failure> theFailures) {
		for (Entry<String, Failure> nextEntry : theFailures.entrySet()) {
			String nextUrlString = nextEntry.getKey();
			PersServiceVersionUrl nextUrl = theMethod.getServiceVersion().getUrlWithUrl(nextUrlString);
			Failure nextFailure = nextEntry.getValue();
			myRuntimeStatus.recordUrlFailure(nextUrl, nextFailure);
		}
	}
}
