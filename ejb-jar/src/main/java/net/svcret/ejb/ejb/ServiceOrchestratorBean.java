package net.svcret.ejb.ejb;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import net.svcret.admin.shared.model.AuthorizationOutcomeEnum;
import net.svcret.ejb.api.HttpRequestBean;
import net.svcret.ejb.api.HttpResponseBean;
import net.svcret.ejb.api.HttpResponseBean.Failure;
import net.svcret.ejb.api.ICredentialGrabber;
import net.svcret.ejb.api.IHttpClient;
import net.svcret.ejb.api.IResponseValidator;
import net.svcret.ejb.api.IRuntimeStatus;
import net.svcret.ejb.api.ISecurityService;
import net.svcret.ejb.api.ISecurityService.AuthorizationResultsBean;
import net.svcret.ejb.api.IServiceInvoker;
import net.svcret.ejb.api.IServiceInvokerJsonRpc20;
import net.svcret.ejb.api.IServiceInvokerSoap11;
import net.svcret.ejb.api.IServiceOrchestrator;
import net.svcret.ejb.api.IServiceRegistry;
import net.svcret.ejb.api.ITransactionLogger;
import net.svcret.ejb.api.InvocationResponseResultsBean;
import net.svcret.ejb.api.InvocationResultsBean;
import net.svcret.ejb.api.InvocationResultsBean.ResultTypeEnum;
import net.svcret.ejb.api.ResponseTypeEnum;
import net.svcret.ejb.api.UrlPoolBean;
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
import net.svcret.ejb.model.entity.PersUser;
import net.svcret.ejb.model.entity.http.PersHttpBasicCredentialGrabber;
import net.svcret.ejb.model.entity.http.PersHttpBasicServerAuth;
import net.svcret.ejb.model.entity.jsonrpc.PersServiceVersionJsonRpc20;
import net.svcret.ejb.model.entity.soap.PersServiceVersionSoap11;
import net.svcret.ejb.util.Validate;

import com.google.common.annotations.VisibleForTesting;

@Stateless
public class ServiceOrchestratorBean implements IServiceOrchestrator {

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(ServiceOrchestratorBean.class);

	@EJB
	private IHttpClient myHttpClient;

	@EJB
	private IRuntimeStatus myRuntimeStatus;

	@EJB
	private ISecurityService mySecuritySvc;

	@EJB()
	private IServiceInvokerSoap11 mySoap11ServiceInvoker;

	@EJB()
	private IServiceInvokerJsonRpc20 myJsonRpc20ServiceInvoker;
	
	@EJB
	private IServiceRegistry mySvcRegistry;

	@EJB
	private ITransactionLogger myTransactionLogger;

	@TransactionAttribute(TransactionAttributeType.NEVER)
	@Override
	public OrchestratorResponseBean handle(HttpRequestBean theRequest) throws UnknownRequestException, InternalErrorException, ProcessingException, IOException, SecurityFailureException {
		Validate.notNull(theRequest.getRequestType(), "RequestType");
		Validate.notNull(theRequest.getPath(), "Path");
		Validate.notNull(theRequest.getQuery(), "Query");
		Validate.notNull(theRequest.getInputReader(), "Reader");

		try {
			return doHandle(theRequest);
		} finally {
			theRequest.getInputReader().close();
		}
	}

	@SuppressWarnings("resource")
	private OrchestratorResponseBean doHandle(HttpRequestBean theRequest) throws UnknownRequestException, IOException, ProcessingException, SecurityFailureException {
		Date startTime = new Date();
		CapturingReader reader = new CapturingReader(theRequest.getInputReader());

		if (theRequest.getQuery().length() > 0 && theRequest.getQuery().charAt(0) != '?') {
			throw new IllegalArgumentException("Path must be blank or start with '?'");
		}
		String path;
		if (theRequest.getPath().length() > 0 && theRequest.getPath().charAt(theRequest.getPath().length() - 1) == '/') {
			path = theRequest.getPath().substring(0, theRequest.getPath().length() - 1);
		} else {
			path = theRequest.getPath();
		}

		ourLog.trace("New request of type {} for path: {}", theRequest.getRequestType(), theRequest.getPath());

		/*
		 * Figure out who should handle this request
		 */

		BasePersServiceVersion serviceVersion = mySvcRegistry.getServiceVersionForPath(path);
		if (serviceVersion == null) {
			ourLog.debug("Request did not match any known paths: {}", path);
			List<String> validPaths = mySvcRegistry.getValidPaths();
			throw new UnknownRequestException(path, validPaths);
		}

		ourLog.trace("Request corresponds to service version {}", serviceVersion.getPid());

		/*
		 * Process request
		 */

		InvocationResultsBean results = null;
		IServiceInvoker<?> serviceInvoker=null;
		switch (serviceVersion.getProtocol()) {
		case SOAP11:
			serviceInvoker = mySoap11ServiceInvoker;
			ourLog.trace("Handling service with invoker {}", serviceInvoker);
			results = mySoap11ServiceInvoker.processInvocation((PersServiceVersionSoap11) serviceVersion, theRequest.getRequestType(), path, theRequest.getQuery(), reader);
			break;
		case JSONRPC20:
			serviceInvoker = myJsonRpc20ServiceInvoker;
			ourLog.trace("Handling service with invoker {}", serviceInvoker);
			results = myJsonRpc20ServiceInvoker.processInvocation((PersServiceVersionJsonRpc20) serviceVersion, theRequest.getRequestType(), path, theRequest.getQuery(), reader);
		}

		if (serviceInvoker == null) {
			throw new InternalErrorException("Unknown service protocol: " + serviceVersion.getProtocol());
		}
		
		if (results == null) {
			throw new InternalErrorException("Invoker " + serviceInvoker + " returned null");
		}
		
		/*
		 * Security
		 * 
		 * Currently only active for method invocations
		 */

		AuthorizationResultsBean authorized = null;
		PersUser user = null;
		if (results.getResultType() == ResultTypeEnum.METHOD) {
			if (ourLog.isDebugEnabled()) {
				if (serviceVersion.getServerAuths().isEmpty()) {
					ourLog.trace("Service has no server auths");
				} else {
					ourLog.trace("Service has the following server auths: {}", serviceVersion.getServerAuths());
				}
			}

			for (PersBaseServerAuth<?, ?> nextServerAuth : serviceVersion.getServerAuths()) {
				ICredentialGrabber credentials;
				if (nextServerAuth instanceof PersHttpBasicServerAuth) {
					credentials = new PersHttpBasicCredentialGrabber(theRequest.getRequestHeaders());
				} else {
					Class<? extends ICredentialGrabber> grabber = nextServerAuth.getGrabberClass();
					credentials = results.getCredentialsInRequest(grabber);
				}
				BasePersAuthenticationHost authHost = nextServerAuth.getAuthenticationHost();
				PersServiceVersionMethod method = results.getMethodDefinition();

				if (credentials == null) {
					// This probably shouldn't happen..
					ourLog.trace("No credential grabber in request (Should invoker have provided one)");
					InvocationResponseResultsBean invocationResponse = new InvocationResponseResultsBean();
					invocationResponse.setResponseType(ResponseTypeEnum.SECURITY_FAIL);
					invocationResponse.setResponseStatusMessage("Internal error: ServiceRetriever failed to extract credentials");
					myRuntimeStatus.recordInvocationMethod(startTime, 0, method, null, null, invocationResponse);
					throw new SecurityFailureException();
				}

				ourLog.trace("Checking credentials: {}", credentials);
				authorized = mySecuritySvc.authorizeMethodInvocation(authHost, credentials, method, theRequest.getRequestHostIp());
				if (authorized.isAuthorized() != AuthorizationOutcomeEnum.AUTHORIZED) {
					InvocationResponseResultsBean invocationResponse = new InvocationResponseResultsBean();
					invocationResponse.setResponseType(ResponseTypeEnum.SECURITY_FAIL);
					invocationResponse.setResponseStatusMessage("Failed to authorize credentials");
					// TODO: also pass authorization outcome to save it
					myRuntimeStatus.recordInvocationMethod(startTime, 0, method, authorized.getUser(), null, invocationResponse);

					// Log transaction
					logTransaction(theRequest, startTime, reader, serviceVersion, authorized, null, invocationResponse);

					throw new SecurityFailureException();
				} else {
					user = authorized.getUser();
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
			Map<String, List<String>> responseHeaders = results.getStaticResourceHeaders();
			retVal = new OrchestratorResponseBean(responseBody, responseContentType, responseHeaders);

			PersServiceVersionResource resource = results.getStaticResourceDefinition();
			myRuntimeStatus.recordInvocationStaticResource(startTime, resource);

			ourLog.trace("Handling request for static URL contents: {}", resource);
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
			markUrlsFailed(httpResponse.getFailedUrls());

			if (httpResponse.getSuccessfulUrl() == null) {
				markUrlsFailed(httpResponse.getFailedUrls());
				Failure exampleFailure = httpResponse.getFailedUrls().values().iterator().next();
				throw new ProcessingException("All service URLs appear to be failing, unable to successfully invoke method. Example failure: " + exampleFailure.getExplanation());
			}

			InvocationResponseResultsBean invocationResponse = serviceInvoker.processInvocationResponse(httpResponse);
			invocationResponse.validate();

			int requestLength = contentBody.length();
			myRuntimeStatus.recordInvocationMethod(startTime, requestLength, method, user, httpResponse, invocationResponse);

			String responseBody = invocationResponse.getResponseBody();
			String responseContentType = invocationResponse.getResponseContentType();
			Map<String, List<String>> responseHeaders = invocationResponse.getResponseHeaders();

			/*
			 * Log transaction if needed
			 */
			logTransaction(theRequest, startTime, reader, serviceVersion, authorized, httpResponse, invocationResponse);

			retVal = new OrchestratorResponseBean(responseBody, responseContentType, responseHeaders);
			break;
		}
		default: {
			throw new InternalErrorException("Unknown request type: " + results.getResultType());
		}
		}

		return retVal;
	}

	private void logTransaction(HttpRequestBean theRequest, Date startTime, CapturingReader reader, BasePersServiceVersion serviceVersion, AuthorizationResultsBean authorized, HttpResponseBean httpResponse, InvocationResponseResultsBean invocationResponse) {
		PersUser user = authorized != null ? authorized.getUser() : null;
		String requestBody = reader.getCapturedString();
		PersServiceVersionUrl successfulUrl = httpResponse != null ? httpResponse.getSuccessfulUrl() : null;
		AuthorizationOutcomeEnum authorizationOutcome = authorized != null ? authorized.isAuthorized() : AuthorizationOutcomeEnum.AUTHORIZED;
		myTransactionLogger.logTransaction(startTime, theRequest, serviceVersion, user, requestBody, invocationResponse, successfulUrl, httpResponse, authorizationOutcome);
	}

	private void markUrlsFailed(Map<PersServiceVersionUrl, Failure> theMap) {
		for (Entry<PersServiceVersionUrl, Failure> nextEntry : theMap.entrySet()) {
			PersServiceVersionUrl nextUrl = nextEntry.getKey();
			Failure nextFailure = nextEntry.getValue();
			myRuntimeStatus.recordUrlFailure(nextUrl, nextFailure);
		}
	}

	/**
	 * FOR UNIT TESTS ONLY
	 */
	@VisibleForTesting
	void setHttpClient(IHttpClient theHttpClient) {
		myHttpClient = theHttpClient;
	}

	/**
	 * FOR UNIT TESTS ONLY
	 */
	@VisibleForTesting
	void setRuntimeStatus(IRuntimeStatus theRuntimeStatus) {
		myRuntimeStatus = theRuntimeStatus;
	}

	/**
	 * FOR UNIT TESTS ONLY
	 */
	@VisibleForTesting
	void setSecuritySvc(ISecurityService theSecuritySvc) {
		mySecuritySvc = theSecuritySvc;
	}

	/**
	 * FOR UNIT TESTS ONLY
	 */
	@VisibleForTesting
	void setSoap11ServiceInvoker(IServiceInvokerSoap11 theSoap11ServiceInvoker) {
		mySoap11ServiceInvoker = theSoap11ServiceInvoker;
	}

	/**
	 * FOR UNIT TESTS ONLY
	 */
	@VisibleForTesting
	void setSvcRegistry(IServiceRegistry theSvcRegistry) {
		mySvcRegistry = theSvcRegistry;
	}

	/**
	 * Unit tests only
	 */
	@VisibleForTesting
	void setTransactionLogger(ITransactionLogger theTransactionLogger) {
		myTransactionLogger = theTransactionLogger;
	}
}
