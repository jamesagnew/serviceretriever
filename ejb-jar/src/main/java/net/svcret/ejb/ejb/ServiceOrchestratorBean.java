package net.svcret.ejb.ejb;

import java.io.IOException;
import java.io.Reader;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import net.svcret.ejb.api.HttpResponseBean;
import net.svcret.ejb.api.ICredentialGrabber;
import net.svcret.ejb.api.IHttpClient;
import net.svcret.ejb.api.IResponseValidator;
import net.svcret.ejb.api.IRuntimeStatus;
import net.svcret.ejb.api.ISecurityService;
import net.svcret.ejb.api.ISecurityService.AuthorizationResultsBean;
import net.svcret.ejb.api.InvocationResultsBean.ResultTypeEnum;
import net.svcret.ejb.api.IServiceInvoker;
import net.svcret.ejb.api.IServiceOrchestrator;
import net.svcret.ejb.api.IServiceRegistry;
import net.svcret.ejb.api.InvocationResponseResultsBean;
import net.svcret.ejb.api.InvocationResultsBean;
import net.svcret.ejb.api.RequestType;
import net.svcret.ejb.api.ResponseTypeEnum;
import net.svcret.ejb.api.UrlPoolBean;
import net.svcret.ejb.api.HttpResponseBean.Failure;
import net.svcret.ejb.ex.InternalErrorException;
import net.svcret.ejb.ex.ProcessingException;
import net.svcret.ejb.ex.SecurityFailureException;
import net.svcret.ejb.ex.UnknownRequestException;
import net.svcret.ejb.model.entity.BasePersAuthenticationHost;
import net.svcret.ejb.model.entity.BasePersServiceVersion;
import net.svcret.ejb.model.entity.PersBaseServerAuth;
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

	@EJB
	private ISecurityService mySecuritySvc;

	@TransactionAttribute(TransactionAttributeType.NEVER)
	@Override
	public OrchestratorResponseBean handle(RequestType theRequestType, String thePath, String theQuery, Reader theReader) throws UnknownRequestException, InternalErrorException, ProcessingException, IOException, SecurityFailureException {
		Validate.notNull(theRequestType, "RequestType");
		Validate.notNull(thePath, "Path");
		Validate.notNull(theQuery, "Query");
		Validate.notNull(theReader, "Reader");

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

		ourLog.debug("New request of type {} for path: {}", theRequestType, thePath);

		/*
		 * Figure out who should handle this request
		 */

		BasePersServiceVersion serviceVersion = mySvcRegistry.getServiceVersionForPath(path);
		if (serviceVersion == null) {
			ourLog.debug("Request did not match any known paths: {}", path);
			List<String> validPaths = mySvcRegistry.getValidPaths();
			throw new UnknownRequestException(path, validPaths);
		}

		ourLog.debug("Request corresponds to service version {}", serviceVersion.getPid());

		/*
		 * Process request
		 */

		InvocationResultsBean results;
		IServiceInvoker<?> serviceInvoker;
		switch (serviceVersion.getProtocol()) {
		case SOAP11:
			PersServiceVersionSoap11 serviceVersionSoap = (PersServiceVersionSoap11) serviceVersion;
			serviceInvoker = mySoap11ServiceInvoker;
			ourLog.debug("Handling service with invoker {}", serviceInvoker);
			results = mySoap11ServiceInvoker.processInvocation(serviceVersionSoap, theRequestType, path, theQuery, theReader);
			break;
		default:
			throw new InternalErrorException("Unknown service protocol: " + serviceVersion.getProtocol());
		}

		/*
		 * Security
		 * 
		 * Currently only active for method invocations
		 */

		if (results.getResultType() == ResultTypeEnum.METHOD) {
			if (ourLog.isDebugEnabled()) {
				if (serviceVersion.getServerAuths().isEmpty()) {
					ourLog.debug("Service has no server auths");
				} else {
					ourLog.debug("Service has the following server auths: {}", serviceVersion.getServerAuths());
				}
			}

			for (PersBaseServerAuth<?, ?> nextServerAuth : serviceVersion.getServerAuths()) {
				Class<? extends ICredentialGrabber> grabber = nextServerAuth.getGrabberClass();
				ICredentialGrabber credentials = results.getCredentialsInRequest(grabber);
				BasePersAuthenticationHost authHost = nextServerAuth.getAuthenticationHost();
				PersServiceVersionMethod method = results.getMethodDefinition();

				if (credentials == null) {
					// This probably shouldn't happen..
					ourLog.debug("No credential grabber in request (Should invoker have provided one)");
					InvocationResponseResultsBean invocationResponse = new InvocationResponseResultsBean();
					invocationResponse.setResponseType(ResponseTypeEnum.SECURITY_FAIL);
					invocationResponse.setResponseStatusMessage("ServiceRetriever failed to extract credentials");
					myRuntimeStatus.recordInvocationMethod(startTime, 0, method, null, null, invocationResponse);
					throw new SecurityFailureException();
				}

				ourLog.debug("Checking credentials: {}", grabber);
				AuthorizationResultsBean authorized = mySecuritySvc.authorizeMethodInvocation(authHost, credentials, method);
				if (!authorized.isAuthorized()) {
					InvocationResponseResultsBean invocationResponse = new InvocationResponseResultsBean();
					invocationResponse.setResponseType(ResponseTypeEnum.SECURITY_FAIL);
					invocationResponse.setResponseStatusMessage("ServiceRetriever failed to extract credentials");
					myRuntimeStatus.recordInvocationMethod(startTime, 0, method, authorized.getUser(), null, invocationResponse);
					throw new SecurityFailureException();
				}
			}
		}

		/*
		 * Forward request to backend implementation
		 */
		ourLog.info("Request is of type: {}", results.getResultType());

		OrchestratorResponseBean retVal;
		switch (results.getResultType()) {

		case STATIC_RESOURCE: {
			String responseBody = results.getStaticResourceText();
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
			if (urlPool.getPreferredUrl() == null) {
				/*
				 * TODO: record this failure? ALso should we allow throttled CB
				 * reset attempts when all URLs are failing?
				 */
				throw new ProcessingException("No URLs available to service this request!");
			}

			PersHttpClientConfig clientConfig = serviceVersion.getHttpClientConfig();
			urlPool.setConnectTimeoutMillis(clientConfig.getConnectTimeoutMillis());
			urlPool.setReadTimeoutMillis(clientConfig.getReadTimeoutMillis());
			urlPool.setFailureRetriesBeforeAborting(clientConfig.getFailureRetriesBeforeAborting());

			HttpResponseBean httpResponse;
			httpResponse = myHttpClient.post(responseValidator, urlPool, contentBody, headers, contentType);
			markUrlsFailed(method, httpResponse.getFailedUrls());

			if (httpResponse.getSuccessfulUrl() == null) {
				markUrlsFailed(method, httpResponse.getFailedUrls());
				Failure exampleFailure = httpResponse.getFailedUrls().values().iterator().next();
				throw new ProcessingException("All service URLs appear to be failing, unable to successfully invoke method. Example failure: " + exampleFailure.getExplanation());
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

	/**
	 * FOR UNIT TESTS ONLY
	 */
	void setRuntimeStatus(IRuntimeStatus theRuntimeStatus) {
		myRuntimeStatus = theRuntimeStatus;
	}

	/**
	 * FOR UNIT TESTS ONLY
	 */
	void setSoap11ServiceInvoker(IServiceInvoker<PersServiceVersionSoap11> theSoap11ServiceInvoker) {
		mySoap11ServiceInvoker = theSoap11ServiceInvoker;
	}

	/**
	 * FOR UNIT TESTS ONLY
	 */
	void setSvcRegistry(IServiceRegistry theSvcRegistry) {
		mySvcRegistry = theSvcRegistry;
	}

	/**
	 * FOR UNIT TESTS ONLY
	 */
	void setHttpClient(IHttpClient theHttpClient) {
		myHttpClient = theHttpClient;
	}

	/**
	 * FOR UNIT TESTS ONLY
	 */
	void setSecuritySvc(ISecurityService theSecuritySvc) {
		mySecuritySvc = theSecuritySvc;
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
