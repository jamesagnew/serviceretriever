package net.svcret.ejb.ejb;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import net.svcret.admin.shared.enm.AuthorizationOutcomeEnum;
import net.svcret.admin.shared.enm.ResponseTypeEnum;
import net.svcret.ejb.api.HttpRequestBean;
import net.svcret.ejb.api.HttpResponseBean;
import net.svcret.ejb.api.HttpResponseBean.Failure;
import net.svcret.ejb.api.ICredentialGrabber;
import net.svcret.ejb.api.IHttpClient;
import net.svcret.ejb.api.IResponseValidator;
import net.svcret.ejb.api.IRuntimeStatus;
import net.svcret.ejb.api.ISecurityService;
import net.svcret.ejb.api.ISecurityService.AuthorizationRequestBean;
import net.svcret.ejb.api.ISecurityService.AuthorizationResultsBean;
import net.svcret.ejb.api.IServiceOrchestrator;
import net.svcret.ejb.api.IServiceRegistry;
import net.svcret.ejb.api.IThrottlingService;
import net.svcret.ejb.api.InvocationResponseResultsBean;
import net.svcret.ejb.api.InvocationResultsBean;
import net.svcret.ejb.api.InvocationResultsBean.ResultTypeEnum;
import net.svcret.ejb.api.RequestType;
import net.svcret.ejb.api.UrlPoolBean;
import net.svcret.ejb.ejb.log.ITransactionLogger;
import net.svcret.ejb.ex.InvocationFailedDueToInternalErrorException;
import net.svcret.ejb.ex.InvocationRequestFailedException;
import net.svcret.ejb.ex.InvocationResponseFailedException;
import net.svcret.ejb.ex.ProcessingException;
import net.svcret.ejb.ex.SecurityFailureException;
import net.svcret.ejb.ex.ThrottleException;
import net.svcret.ejb.ex.UnexpectedFailureException;
import net.svcret.ejb.ex.UnknownRequestException;
import net.svcret.ejb.invoker.IServiceInvoker;
import net.svcret.ejb.invoker.hl7.IServiceInvokerHl7OverHttp;
import net.svcret.ejb.invoker.hl7.ServiceInvokerHl7OverHttp;
import net.svcret.ejb.invoker.jsonrpc.IServiceInvokerJsonRpc20;
import net.svcret.ejb.invoker.soap.IServiceInvokerSoap11;
import net.svcret.ejb.invoker.soap.InvocationFailedException;
import net.svcret.ejb.invoker.virtual.IServiceInvokerVirtual;
import net.svcret.ejb.model.entity.BasePersAuthenticationHost;
import net.svcret.ejb.model.entity.BasePersServiceVersion;
import net.svcret.ejb.model.entity.PersBaseClientAuth;
import net.svcret.ejb.model.entity.PersBaseServerAuth;
import net.svcret.ejb.model.entity.PersHttpClientConfig;
import net.svcret.ejb.model.entity.PersServiceVersionMethod;
import net.svcret.ejb.model.entity.PersServiceVersionResource;
import net.svcret.ejb.model.entity.PersServiceVersionUrl;
import net.svcret.ejb.model.entity.PersUser;
import net.svcret.ejb.model.entity.http.PersHttpBasicClientAuth;
import net.svcret.ejb.model.entity.http.PersHttpBasicCredentialGrabber;
import net.svcret.ejb.model.entity.http.PersHttpBasicServerAuth;
import net.svcret.ejb.util.Validate;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;

import com.google.common.annotations.VisibleForTesting;

@Stateless
public class ServiceOrchestratorBean implements IServiceOrchestrator {

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(ServiceOrchestratorBean.class);

	@EJB
	private IHttpClient myHttpClient;

	@EJB
	private IServiceInvokerJsonRpc20 myServiceInvokerJsonRpc20;

	@EJB
	private IServiceInvokerHl7OverHttp myServiceInvokerHl7OverHttp;

	@EJB
	private IRuntimeStatus myRuntimeStatus;

	@EJB
	private ISecurityService mySecuritySvc;

	@EJB
	private IServiceInvokerSoap11 myServiceInvokerSoap11;

	@EJB
	private IServiceRegistry mySvcRegistry;

	@EJB
	private IThrottlingService myThrottlingService;

	@EJB
	private ITransactionLogger myTransactionLogger;

	@EJB
	private IServiceInvokerVirtual myServiceInvokerVirtual;

	private OrchestratorResponseBean doHandleServiceRequest(HttpRequestBean theRequest) throws UnknownRequestException, ProcessingException, SecurityFailureException, ThrottleException,
			ThrottleQueueFullException {
		if (theRequest.getQuery().length() > 0 && theRequest.getQuery().charAt(0) != '?') {
			throw new IllegalArgumentException("Path must be blank or start with '?'");
		}
		String path = theRequest.getPath();

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
		InvocationResultsBean results;
		AuthorizationResultsBean authorized;
		PersServiceVersionMethod invokingMethod = null;
		try {

			/*
			 * Process the invocation. This is the first part of handling a request, where we examine the request, determine where it's going and prep it for passing to the backend implementation
			 * (which happens later)
			 */
			results = processInvokeService(theRequest, serviceVersion);
			invokingMethod = results.getMethodDefinition();

			/*
			 * Security
			 * 
			 * Currently only active for method invocations
			 */
			List<PersBaseServerAuth<?, ?>> serverAuths = getServiceInvoker(serviceVersion).provideServerAuthorizationModules(serviceVersion);
			authorized = processSecurity(theRequest, serverAuths, results);

			/*
			 * Apply throttling if needed (may throw an exception if request is throttled)
			 */
			myThrottlingService.applyThrottle(theRequest, results, authorized);

		} catch (UnknownRequestException e) {
			ourLog.debug("Exception occurred", e);
			handleInvocationFailure(theRequest, serviceVersion, invokingMethod, new InvocationRequestFailedException(e), null, null, ResponseTypeEnum.FAIL);
			throw e;
		} catch (InvocationFailedException e) {
			ourLog.debug("Exception occurred", e);
			handleInvocationFailure(theRequest, serviceVersion, invokingMethod, e, null, null, ResponseTypeEnum.FAIL);
			throw new ProcessingException(e);
		} catch (ThrottleException e) {
			ourLog.debug("Exception occurred", e);
			/*
			 * Don't handle this failure, it's the responsibility of the throttling service to do so
			 */
			throw e;
		} catch (ThrottleQueueFullException e) {
			ourLog.debug("Exception occurred", e);
			/*
			 * Don't handle this failure, it's the responsibility of the throttling service to do so
			 */
			throw e;
		} catch (SecurityFailureException e) {
			ourLog.debug("Exception occurred", e);
			handleInvocationFailure(theRequest, serviceVersion, invokingMethod, new InvocationRequestFailedException(e), null, null, ResponseTypeEnum.SECURITY_FAIL);
			throw e;
		} catch (Exception e) {
			ourLog.error("Unexpected failure", e);
			handleInvocationFailure(theRequest, serviceVersion, invokingMethod, new InvocationRequestFailedException(e), null, null, ResponseTypeEnum.FAIL);
			throw new ProcessingException(e);
		}

		/*
		 * Forward request to backend implementation
		 */
		ourLog.debug("Request is of type: {}", results.getResultType());
		OrchestratorResponseBean retVal;
		try {
			retVal = invokeProxiedService(theRequest, results, authorized, null);
		} catch (InvocationFailedDueToInternalErrorException e) {
			throw new ProcessingException(e);
		}

		return retVal;
	}

	private void handleInvocationFailure(HttpRequestBean theRequest, BasePersServiceVersion serviceVersion, PersServiceVersionMethod theMethodIfKnown, InvocationFailedException theInvocationFailure,
			PersUser theUserIfKnown, Long theThrottleDelayIfAnyAndKnown, ResponseTypeEnum theResponseType) throws ProcessingException {
		theRequest.drainInputMessage();

		if (theMethodIfKnown != null) {
			try {
				InvocationResponseResultsBean invocationResponseResultsBean = new InvocationResponseResultsBean();
				invocationResponseResultsBean.setResponseType(theResponseType);
				myRuntimeStatus.recordInvocationMethod(theRequest.getRequestTime(), theRequest.getRequestBody().length(), theMethodIfKnown, theUserIfKnown, null, invocationResponseResultsBean,
						theThrottleDelayIfAnyAndKnown);

			} catch (UnexpectedFailureException e1) {
				// Don't do anything except log here since we're already handling a failure by the
				// time we get here so this is pretty much the last resort..
				ourLog.error("Failed to record method invocation", e1);
			}
		}

		/*
		 * TODO: add some kind of statistic recording for svcVer failed requests that don't have a method associated
		 */
		try {
			myTransactionLogger.logTransaction(theRequest, serviceVersion, null, theUserIfKnown, theRequest.getRequestBody(), theInvocationFailure.toInvocationResponse(),
					theInvocationFailure.getImplementationUrl(), theInvocationFailure.getHttpResponse(), null, null);
		} catch (UnexpectedFailureException e1) {
			// Don't do anything except log here since we're already handling a failure by the
			// time we get here so this is pretty much the last resort..
			ourLog.error("Failed to record method invocation", e1);
		}

	}

	@TransactionAttribute(TransactionAttributeType.NEVER)
	@Override
	public OrchestratorResponseBean handlePreviouslyThrottledRequest(InvocationResultsBean theInvocationRequest, AuthorizationResultsBean theAuthorization, HttpRequestBean theRequest,
			long theThrottleTime) throws ProcessingException, SecurityFailureException, InvocationFailedDueToInternalErrorException {
		return invokeProxiedService(theRequest, theInvocationRequest, theAuthorization, theThrottleTime);
	}

	@TransactionAttribute(TransactionAttributeType.NEVER)
	@Override
	public OrchestratorResponseBean handleServiceRequest(HttpRequestBean theRequest) throws UnknownRequestException, ProcessingException, SecurityFailureException, ThrottleException,
			ThrottleQueueFullException {
		Validate.notNull(theRequest.getRequestType(), "RequestType");
		Validate.notNull(theRequest.getPath(), "Path");
		Validate.notNull(theRequest.getQuery(), "Query");
		Validate.notNull(theRequest.getInputReader(), "Reader");

		try {
			return doHandleServiceRequest(theRequest);
		} finally {
			try {
				theRequest.getInputReader().close();
			} catch (IOException e) {
				// ignore
			}
		}
	}

	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	@Override
	public SidechannelOrchestratorResponseBean handleSidechannelRequest(long theServiceVersionPid, String theRequestBody, String theContentType, String theRequestedByString)
			throws ProcessingException, UnknownRequestException {
		return doHandleSideChannelRequest(theServiceVersionPid, theRequestBody, theContentType, theRequestedByString, null);
	}

	private SidechannelOrchestratorResponseBean doHandleSideChannelRequest(long theServiceVersionPid, String theRequestBody, String theContentType, String theRequestedByString,
			PersServiceVersionUrl theForceUrl) throws ProcessingException, UnknownRequestException {
		Date startTime = new Date();
		BasePersServiceVersion svcVer = mySvcRegistry.getServiceVersionByPid(theServiceVersionPid);

		StringReader reader = (new StringReader(theRequestBody));

		HttpRequestBean request = new HttpRequestBean();
		request.setRequestTime(startTime);
		request.setInputReader(reader);
		request.setRequestHostIp("127.0.0.1");
		request.setPath(svcVer.determineUsableProxyPath());
		request.setRequestHeaders(new HashMap<String, List<String>>());
		request.getRequestHeaders().put("X-RequestedBy", Collections.singletonList(theRequestedByString));
		request.getRequestHeaders().put("Content-Type", Collections.singletonList(theContentType));
		request.setRequestType(RequestType.POST); // TODO this should be configurable, maybe method specific or something?
		AuthorizationResultsBean authorized = null;

		InvocationResultsBean results;
		try {
			results = processInvokeService(request, svcVer);
		} catch (InvocationFailedException e) {
			throw new ProcessingException(e);
		}
		
		SidechannelOrchestratorResponseBean retVal;
		try {
			retVal = processRequestMethod(request, results, authorized, null, false, theForceUrl, new Date());
		} catch (InvocationFailedException e) {
			throw new ProcessingException(e);
		}
		return retVal;
	}

	private void logTransaction(HttpRequestBean theRequest, PersServiceVersionMethod method, AuthorizationResultsBean authorized, HttpResponseBean httpResponse,
			InvocationResponseResultsBean invocationResponse) throws InvocationFailedDueToInternalErrorException {
		PersUser user = authorized.getAuthorizedUser();

		String requestBody = theRequest.getRequestBody();
		String responseBody = invocationResponse.getResponseBody();

		// Obscure
		BasePersServiceVersion svcVer = method.getServiceVersion();
		IServiceInvoker svcInvoker = getServiceInvoker(svcVer);
		requestBody = svcInvoker.obscureMessageForLogs(svcVer, requestBody, method.getServiceVersion().determineObscureRequestElements());
		responseBody = svcInvoker.obscureMessageForLogs(svcVer, responseBody, method.getServiceVersion().determineObscureResponseElements());

		// Log
		PersServiceVersionUrl successfulUrl = httpResponse != null ? httpResponse.getSuccessfulUrl() : null;
		AuthorizationOutcomeEnum authorizationOutcome = authorized.isAuthorized();
		try {
			myTransactionLogger.logTransaction(theRequest, method.getServiceVersion(), method, user, requestBody, invocationResponse, successfulUrl, httpResponse, authorizationOutcome, responseBody);
		} catch (ProcessingException e) {
			throw new InvocationFailedDueToInternalErrorException(e);
		} catch (UnexpectedFailureException e) {
			throw new InvocationFailedDueToInternalErrorException(e);
		}
	}

	private OrchestratorResponseBean invokeProxiedService(HttpRequestBean theRequest, InvocationResultsBean results, AuthorizationResultsBean theAuthorized, Long theThrottleTimeIfAny)
			throws ProcessingException, SecurityFailureException, InvocationFailedDueToInternalErrorException {

		if (theAuthorized != null && theAuthorized.isAuthorized() != AuthorizationOutcomeEnum.AUTHORIZED) {
			InvocationResponseResultsBean invocationResponse = new InvocationResponseResultsBean();
			invocationResponse.setResponseType(ResponseTypeEnum.SECURITY_FAIL);
			invocationResponse.setResponseStatusMessage("Failed to authorize credentials");
			// TODO: also pass authorization outcome to save it
			try {
				myRuntimeStatus
						.recordInvocationMethod(theRequest.getRequestTime(), 0, results.getMethodDefinition(), theAuthorized.getAuthorizedUser(), null, invocationResponse, theThrottleTimeIfAny);
			} catch (UnexpectedFailureException e) {
				throw new InvocationFailedDueToInternalErrorException(e);
			}

			// Log transaction
			logTransaction(theRequest, results.getMethodDefinition(), theAuthorized, null, invocationResponse);

			throw new SecurityFailureException(theAuthorized.isAuthorized(), invocationResponse.getResponseStatusMessage());
		}

		OrchestratorResponseBean retVal;
		switch (results.getResultType()) {
		case STATIC_RESOURCE:
			retVal = processRequestResource(theRequest, results);
			break;
		case METHOD:
			PersUser user = theAuthorized != null ? theAuthorized.getAuthorizedUser() : null;
			try {
				retVal = processRequestMethod(theRequest, results, theAuthorized, theThrottleTimeIfAny, true, null, theRequest.getRequestTime());
			} catch (InvocationResponseFailedException e) {
				handleInvocationFailure(theRequest, results.getMethodDefinition().getServiceVersion(), results.getMethodDefinition(), e, user, theThrottleTimeIfAny, ResponseTypeEnum.FAIL);
				throw new ProcessingException(e);
			} catch (RuntimeException e) {
				handleInvocationFailure(theRequest, results.getMethodDefinition().getServiceVersion(), results.getMethodDefinition(), new InvocationResponseFailedException(e,
						"Invocation failed due to ServiceRetriever internal error: " + e.getMessage(), null), user, theThrottleTimeIfAny, ResponseTypeEnum.FAIL);
				throw new ProcessingException(e);
			}
			break;
		default:
			throw new InvocationFailedDueToInternalErrorException("Unknown request type: " + results.getResultType());
		}
		return retVal;
	}

	private void markUrlsFailed(Map<PersServiceVersionUrl, Failure> theMap) {
		for (Entry<PersServiceVersionUrl, Failure> nextEntry : theMap.entrySet()) {
			PersServiceVersionUrl nextUrl = nextEntry.getKey();
			Failure nextFailure = nextEntry.getValue();
			myRuntimeStatus.recordUrlFailure(nextUrl, nextFailure);
		}
	}

	private InvocationResultsBean processInvokeService(HttpRequestBean theRequest, BasePersServiceVersion serviceVersion) throws InvocationFailedException, UnknownRequestException {
		InvocationResultsBean results = null;
		IServiceInvoker svcInvoker = getServiceInvoker(serviceVersion);
		ourLog.trace("Handling service with invoker {}", svcInvoker);

		if (svcInvoker == null) {
			throw new InvocationFailedDueToInternalErrorException("Unknown service protocol: " + serviceVersion.getProtocol());
		}

		results = svcInvoker.processInvocation(theRequest, serviceVersion);

		if (results == null) {
			throw new InvocationFailedDueToInternalErrorException("Invoker " + svcInvoker + " returned null");
		}

		if (results.getResultType() == ResultTypeEnum.METHOD) {
			results.setMethodHeaders(svcInvoker.createBackingRequestHeadersForMethodInvocation(serviceVersion, theRequest.getRequestHeaders()));
		}

		for (PersBaseClientAuth<?> next : serviceVersion.getClientAuths()) {
			if (next instanceof PersHttpBasicClientAuth) {
				PersHttpBasicClientAuth clientAuth = (PersHttpBasicClientAuth) next;
				String authorizationUnescaped = StringUtils.defaultString(clientAuth.getUsername()) + ":" + StringUtils.defaultString(clientAuth.getPassword());
				String encoded;
				try {
					encoded = Base64.encodeBase64String(authorizationUnescaped.getBytes("ISO-8859-1"));
				} catch (UnsupportedEncodingException e) {
					throw new Error("Could not find US-ASCII encoding. This shouldn't happen!");
				}
				results.getMethodHeaders().put("Authorization", Collections.singletonList("Basic " + encoded));
			}
		}

		return results;
	}

	@Override
	public IServiceInvoker getServiceInvoker(BasePersServiceVersion serviceVersion) {
		IServiceInvoker svcInvoker = null;
		switch (serviceVersion.getProtocol()) {
		case SOAP11:
			svcInvoker = myServiceInvokerSoap11;
			break;
		case JSONRPC20:
			svcInvoker = myServiceInvokerJsonRpc20;
			break;
		case HL7OVERHTTP:
			svcInvoker = myServiceInvokerHl7OverHttp;
			break;
		case VIRTUAL:
			svcInvoker = myServiceInvokerVirtual;
			break;
		}
		return svcInvoker;
	}

	private SidechannelOrchestratorResponseBean processRequestMethod(HttpRequestBean theRequest, InvocationResultsBean results, AuthorizationResultsBean authorized, Long theThrottleDelayIfAny,
			boolean theRecordOutcome, PersServiceVersionUrl theForceUrl, Date theRequestStartedTime) throws InvocationResponseFailedException, InvocationFailedDueToInternalErrorException {
		SidechannelOrchestratorResponseBean retVal;
		PersServiceVersionMethod method = results.getMethodDefinition();
		BasePersServiceVersion serviceVersion = method.getServiceVersion();
		Map<String, List<String>> headers = results.getMethodHeaders();
		String contentType = results.getMethodContentType();
		String contentBody = results.getMethodRequestBody();

		IServiceInvoker invoker = getServiceInvoker(method.getServiceVersion());
		IResponseValidator responseValidator = invoker.provideInvocationResponseValidator(serviceVersion);

		UrlPoolBean urlPool;
		if (theForceUrl == null) {
			try {
				urlPool = myRuntimeStatus.buildUrlPool(method.getServiceVersion(), headers);
			} catch (UnexpectedFailureException e) {
				throw new InvocationFailedDueToInternalErrorException(e);
			}
			if (urlPool.getPreferredUrl() == null) {
				throw new InvocationResponseFailedException("No URLs available to service this request");
			}
		} else {
			urlPool = new UrlPoolBean();
			urlPool.setPreferredUrl(theForceUrl);
		}

		PersHttpClientConfig clientConfig = serviceVersion.getHttpClientConfig();
		HttpResponseBean httpResponse;
		httpResponse = myHttpClient.post(clientConfig, responseValidator, urlPool, contentBody, headers, contentType);
		markUrlsFailed(httpResponse.getFailedUrls());

		if (httpResponse.getSuccessfulUrl() == null) {
			markUrlsFailed(httpResponse.getFailedUrls());
			Entry<PersServiceVersionUrl, Failure> entry = httpResponse.getFailedUrls().entrySet().iterator().next();
			Failure exampleFailure = entry.getValue();
			PersServiceVersionUrl exampleUrl = entry.getKey();
			StringBuilder b = new StringBuilder();
			b.append("All service URLs appear to be failing (");
			b.append(httpResponse.getFailedUrls().size());
			b.append(" URL in total), unable to successfully invoke method. URL '");
			b.append(exampleUrl.getUrlId());
			b.append("' failed with error: ");
			b.append(exampleFailure.getExplanation());
			throw new InvocationResponseFailedException(b.toString());
		}

		InvocationResponseResultsBean invocationResponse = invoker.processInvocationResponse(serviceVersion, httpResponse);
		invocationResponse.validate();

		String responseBody = invocationResponse.getResponseBody();
		String responseContentType = invocationResponse.getResponseContentType();
		Map<String, List<String>> responseHeaders = invocationResponse.getResponseHeaders();
		int requestLength = contentBody.length();
		PersUser user = ((authorized != null && authorized.isAuthorized() == AuthorizationOutcomeEnum.AUTHORIZED) ? authorized.getAuthorizedUser() : null);

		if (theRecordOutcome) {
			try {
				myRuntimeStatus.recordInvocationMethod(theRequest.getRequestTime(), requestLength, method, user, httpResponse, invocationResponse, theThrottleDelayIfAny);
			} catch (UnexpectedFailureException e) {
				throw new InvocationFailedDueToInternalErrorException(e);
			}
			logTransaction(theRequest, method, authorized, httpResponse, invocationResponse);
		}

		ResponseTypeEnum responseType = invocationResponse.getResponseType();
		retVal = new SidechannelOrchestratorResponseBean(responseBody, responseContentType, responseHeaders, httpResponse, responseType, theRequestStartedTime);
		return retVal;
	}

	private OrchestratorResponseBean processRequestResource(HttpRequestBean theRequest, InvocationResultsBean results) {
		OrchestratorResponseBean retVal;
		String responseBody = results.getStaticResourceText();
		String responseContentType = results.getStaticResourceContentTyoe();
		Map<String, List<String>> responseHeaders = results.getStaticResourceHeaders();
		retVal = new OrchestratorResponseBean(responseBody, responseContentType, responseHeaders, null);

		PersServiceVersionResource resource = results.getStaticResourceDefinition();
		myRuntimeStatus.recordInvocationStaticResource(theRequest.getRequestTime(), resource);

		ourLog.trace("Handling request for static URL contents: {}", resource);
		return retVal;
	}

	private AuthorizationResultsBean processSecurity(HttpRequestBean theRequest, List<PersBaseServerAuth<?, ?>> theServerAuths, InvocationResultsBean results) throws SecurityFailureException,
			ProcessingException {
		AuthorizationResultsBean authorized = null;
		if (results.getResultType() == ResultTypeEnum.METHOD) {
			if (ourLog.isDebugEnabled()) {
				if (theServerAuths.isEmpty()) {
					ourLog.trace("Service has no server auths");
				} else {
					ourLog.trace("Service has the following server auths: {}", theServerAuths);
				}
			}

			PersServiceVersionMethod method = results.getMethodDefinition();
			List<AuthorizationRequestBean> authRequests = new ArrayList<ISecurityService.AuthorizationRequestBean>();
			for (PersBaseServerAuth<?, ?> nextServerAuth : theServerAuths) {
				ICredentialGrabber credentials;
				if (nextServerAuth instanceof PersHttpBasicServerAuth) {
					credentials = new PersHttpBasicCredentialGrabber(theRequest.getRequestHeaders());
				} else {
					credentials = results.getCredentialsInRequest(nextServerAuth);
				}
				BasePersAuthenticationHost authHost = nextServerAuth.getAuthenticationHost();

				if (credentials == null) {
					throw new SecurityFailureException(AuthorizationOutcomeEnum.FAILED_INTERNAL_ERROR, "ServiceRetriever failed to extract credentials.");
				}

				authRequests.add(new AuthorizationRequestBean(authHost, credentials));
				ourLog.trace("Checking credentials: {}", credentials);
			}

			authorized = mySecuritySvc.authorizeMethodInvocation(authRequests, method, theRequest.getRequestHostIp());

		}
		return authorized;
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
		myServiceInvokerSoap11 = theSoap11ServiceInvoker;
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

	@Override
	public void enqueueThrottledRequest(ThrottleException theE) throws ThrottleQueueFullException {
		myThrottlingService.scheduleThrottledTaskForLaterExecution(theE);
	}

	/**
	 * Unit tests only
	 */
	@VisibleForTesting
	void setThrottlingService(IThrottlingService theMock) {
		myThrottlingService = theMock;
	}

	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	@Override
	public Collection<SidechannelOrchestratorResponseBean> handleSidechannelRequestForEachUrl(long theServiceVersionPid, String theRequestBody, String theContentType, String theRequestedByString) {

		BasePersServiceVersion svcVer = mySvcRegistry.getServiceVersionByPid(theServiceVersionPid);
		if (svcVer == null) {
			throw new IllegalArgumentException("Unknown service version " + theServiceVersionPid);
		}

		ArrayList<SidechannelOrchestratorResponseBean> retVal = new ArrayList<IServiceOrchestrator.SidechannelOrchestratorResponseBean>();

		for (PersServiceVersionUrl nextUrl : svcVer.getUrls()) {
			ourLog.debug("About to perform sidechannel request for URL: {} / {}", nextUrl.getPid(), nextUrl.getUrl());
			
			SidechannelOrchestratorResponseBean responseBean;
			Date startedTime = new Date();
			try {
				responseBean = doHandleSideChannelRequest(theServiceVersionPid, theRequestBody, theContentType, theRequestedByString, nextUrl);
				retVal.add(responseBean);
			} catch (ProcessingException e) {
				ourLog.debug("Failed to execute sidechannel request for URL", e);
				responseBean = SidechannelOrchestratorResponseBean.forFailure(e, startedTime, nextUrl);
				retVal.add(responseBean);
			} catch (UnknownRequestException e) {
				ourLog.debug("Failed to execute sidechannel request for URL", e);
				responseBean = SidechannelOrchestratorResponseBean.forFailure(e, startedTime, nextUrl);
				retVal.add(responseBean);
			}
		}

		return retVal;
	}

	@VisibleForTesting
	void setServiceInvokerHl7OverhttpForUnitTests(ServiceInvokerHl7OverHttp theHl7Invoker) {
		myServiceInvokerHl7OverHttp = theHl7Invoker;
	}

	@VisibleForTesting
	public void setServiceInvokerVirtualForUnitTests(IServiceInvokerVirtual theVirtualInvoker) {
		myServiceInvokerVirtual = theVirtualInvoker;
	}
}
