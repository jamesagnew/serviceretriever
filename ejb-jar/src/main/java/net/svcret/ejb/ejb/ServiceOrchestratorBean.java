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

import net.svcret.admin.shared.enm.ResponseTypeEnum;
import net.svcret.admin.shared.model.AuthorizationOutcomeEnum;
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
import net.svcret.ejb.api.IServiceInvoker;
import net.svcret.ejb.api.IServiceInvokerHl7OverHttp;
import net.svcret.ejb.api.IServiceInvokerJsonRpc20;
import net.svcret.ejb.api.IServiceInvokerSoap11;
import net.svcret.ejb.api.IServiceOrchestrator;
import net.svcret.ejb.api.IServiceRegistry;
import net.svcret.ejb.api.IThrottlingService;
import net.svcret.ejb.api.ITransactionLogger;
import net.svcret.ejb.api.InvocationResponseResultsBean;
import net.svcret.ejb.api.InvocationResultsBean;
import net.svcret.ejb.api.InvocationResultsBean.ResultTypeEnum;
import net.svcret.ejb.api.RequestType;
import net.svcret.ejb.api.UrlPoolBean;
import net.svcret.ejb.ejb.soap.InvocationFailedException;
import net.svcret.ejb.ex.InvocationFailedDueToInternalErrorException;
import net.svcret.ejb.ex.InvocationRequestFailedException;
import net.svcret.ejb.ex.InvocationResponseFailedException;
import net.svcret.ejb.ex.ProcessingException;
import net.svcret.ejb.ex.SecurityFailureException;
import net.svcret.ejb.ex.ThrottleException;
import net.svcret.ejb.ex.UnknownRequestException;
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
	private IServiceInvokerJsonRpc20 myJsonRpc20ServiceInvoker;

	@EJB
	private IServiceInvokerHl7OverHttp myHl7OverHttpServiceInvoker;

	@EJB
	private IRuntimeStatus myRuntimeStatus;

	@EJB
	private ISecurityService mySecuritySvc;

	@EJB()
	private IServiceInvokerSoap11 mySoap11ServiceInvoker;

	@EJB
	private IServiceRegistry mySvcRegistry;

	@EJB
	private IThrottlingService myThrottlingService;

	@EJB
	private ITransactionLogger myTransactionLogger;

	private OrchestratorResponseBean doHandleServiceRequest(HttpRequestBean theRequest) throws UnknownRequestException, ProcessingException, SecurityFailureException, ThrottleException,
			ThrottleQueueFullException {
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
		InvocationResultsBean results;
		try {
			results = processInvokeService(theRequest, path, serviceVersion, theRequest.getRequestType(), theRequest.getQuery());
		} catch (UnknownRequestException e) {
			handleInvocationFailure(theRequest, serviceVersion, new InvocationRequestFailedException(e));
			throw e;
		} catch (InvocationFailedException e) {
			handleInvocationFailure(theRequest, serviceVersion, e);
			throw new ProcessingException(e);
		} catch (Exception e) {
			handleInvocationFailure(theRequest, serviceVersion, new InvocationRequestFailedException(e));
			throw new ProcessingException(e);
		}

		/*
		 * Security
		 * 
		 * Currently only active for method invocations
		 */
		AuthorizationResultsBean authorized = processSecurity(theRequest, serviceVersion, results);

		/*
		 * Apply throttling if needed (may throw an exception if request is throttled)
		 */
		myThrottlingService.applyThrottle(theRequest, results, authorized);

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

	private void handleInvocationFailure(HttpRequestBean theRequest, BasePersServiceVersion serviceVersion, InvocationFailedException e) throws ProcessingException {
		theRequest.drainInputMessage();

		/* TODO: add some kind of statistic recording for svcVer failed requests that
		 * don't have a method associated
		 */
		
		myTransactionLogger.logTransaction(theRequest, serviceVersion, null, e.getUser(), theRequest.getRequestBody(), e.toInvocationResponse(), e.getImplementationUrl(), e.getHttpResponse(), null,
				null);
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
		String path = svcVer.determineUsableProxyPath();

		HttpRequestBean request = new HttpRequestBean();
		request.setRequestTime(startTime);
		request.setInputReader(reader);
		request.setRequestHostIp("127.0.0.1");
		request.setRequestHeaders(new HashMap<String, List<String>>());
		request.getRequestHeaders().put("X-RequestedBy", Collections.singletonList(theRequestedByString));
		request.getRequestHeaders().put("Content-Type", Collections.singletonList(theContentType));
		AuthorizationResultsBean authorized = null;

		InvocationResultsBean results;
		try {
			results = processInvokeService(request, path, svcVer, RequestType.POST, "");
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
		IServiceInvoker svcInvoker = getServiceInvoker(method.getServiceVersion());
		requestBody = svcInvoker.obscureMessageForLogs(requestBody, method.getServiceVersion().determineObscureRequestElements());
		responseBody = svcInvoker.obscureMessageForLogs(responseBody, method.getServiceVersion().determineObscureResponseElements());

		// Log
		PersServiceVersionUrl successfulUrl = httpResponse != null ? httpResponse.getSuccessfulUrl() : null;
		AuthorizationOutcomeEnum authorizationOutcome = authorized.isAuthorized();
		try {
			myTransactionLogger.logTransaction(theRequest, method.getServiceVersion(), method, user, requestBody, invocationResponse, successfulUrl, httpResponse, authorizationOutcome, responseBody);
		} catch (ProcessingException e) {
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
			myRuntimeStatus.recordInvocationMethod(theRequest.getRequestTime(), 0, results.getMethodDefinition(), theAuthorized.getAuthorizedUser(), null, invocationResponse, theThrottleTimeIfAny);

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
			try {
				retVal = processRequestMethod(theRequest, results, theAuthorized, theThrottleTimeIfAny, true, null, theRequest.getRequestTime());
			} catch (InvocationResponseFailedException e) {
				handleInvocationFailure(theRequest, results.getMethodDefinition().getServiceVersion(), e);
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

	private InvocationResultsBean processInvokeService(HttpRequestBean theRequest, String path, BasePersServiceVersion serviceVersion, RequestType theRequestType, String theRequestQuery)
			throws InvocationFailedException, UnknownRequestException {
		InvocationResultsBean results = null;
		String contentType = theRequest.getContentType();
		IServiceInvoker svcInvoker = getServiceInvoker(serviceVersion);
		ourLog.trace("Handling service with invoker {}", svcInvoker);

		if (svcInvoker == null) {
			throw new InvocationFailedDueToInternalErrorException("Unknown service protocol: " + serviceVersion.getProtocol());
		}

		results = svcInvoker.processInvocation(serviceVersion, theRequestType, path, theRequestQuery, contentType, theRequest.getInputReader());

		if (results == null) {
			throw new InvocationFailedDueToInternalErrorException("Invoker " + svcInvoker + " returned null");
		}

		if (results.getResultType()==ResultTypeEnum.METHOD) {
			results.setMethodHeaders(svcInvoker.createRequestHeaders(theRequest.getRequestHeaders()));
		}
		
		results.setServiceInvoker(svcInvoker);

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

	private IServiceInvoker getServiceInvoker(BasePersServiceVersion serviceVersion) {
		IServiceInvoker svcInvoker = null;
		switch (serviceVersion.getProtocol()) {
		case SOAP11:
			svcInvoker = mySoap11ServiceInvoker;
			break;
		case JSONRPC20:
			svcInvoker = myJsonRpc20ServiceInvoker;
			break;
		case HL7OVERHTTP:
			svcInvoker = myHl7OverHttpServiceInvoker;
			break;
		}
		return svcInvoker;
	}

	private SidechannelOrchestratorResponseBean processRequestMethod(HttpRequestBean theRequest, InvocationResultsBean results, AuthorizationResultsBean authorized, Long theThrottleDelayIfAny,
			boolean theRecordOutcome, PersServiceVersionUrl theForceUrl, Date theRequestStartedTime) throws ProcessingException, InvocationResponseFailedException,
			InvocationFailedDueToInternalErrorException {
		SidechannelOrchestratorResponseBean retVal;
		PersServiceVersionMethod method = results.getMethodDefinition();
		BasePersServiceVersion serviceVersion = method.getServiceVersion();
		Map<String, List<String>> headers = results.getMethodHeaders();
		String contentType = results.getMethodContentType();
		String contentBody = results.getMethodRequestBody();
		IResponseValidator responseValidator = results.getServiceInvoker().provideInvocationResponseValidator();

		UrlPoolBean urlPool;
		if (theForceUrl == null) {
			urlPool = myRuntimeStatus.buildUrlPool(method.getServiceVersion(), headers);
			if (urlPool.getPreferredUrl() == null) {
				/*
				 * TODO: record this failure? ALso should we allow throttled CB reset attempts when all URLs are failing?
				 */
				throw new ProcessingException("No URLs available to service this request!");
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
			Failure exampleFailure = httpResponse.getFailedUrls().values().iterator().next();
			throw new ProcessingException("All service URLs appear to be failing, unable to successfully invoke method. Example failure: " + exampleFailure.getExplanation());
		}

		InvocationResponseResultsBean invocationResponse = results.getServiceInvoker().processInvocationResponse(httpResponse);
		invocationResponse.validate();

		String responseBody = invocationResponse.getResponseBody();
		String responseContentType = invocationResponse.getResponseContentType();
		Map<String, List<String>> responseHeaders = invocationResponse.getResponseHeaders();
		int requestLength = contentBody.length();
		PersUser user = ((authorized != null && authorized.isAuthorized() == AuthorizationOutcomeEnum.AUTHORIZED) ? authorized.getAuthorizedUser() : null);

		if (theRecordOutcome) {
			myRuntimeStatus.recordInvocationMethod(theRequest.getRequestTime(), requestLength, method, user, httpResponse, invocationResponse, theThrottleDelayIfAny);
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

	private AuthorizationResultsBean processSecurity(HttpRequestBean theRequest, BasePersServiceVersion serviceVersion, InvocationResultsBean results) throws SecurityFailureException,
			ProcessingException {
		AuthorizationResultsBean authorized = null;
		if (results.getResultType() == ResultTypeEnum.METHOD) {
			if (ourLog.isDebugEnabled()) {
				if (serviceVersion.getServerAuths().isEmpty()) {
					ourLog.trace("Service has no server auths");
				} else {
					ourLog.trace("Service has the following server auths: {}", serviceVersion.getServerAuths());
				}
			}

			PersServiceVersionMethod method = results.getMethodDefinition();
			List<AuthorizationRequestBean> authRequests = new ArrayList<ISecurityService.AuthorizationRequestBean>();
			for (PersBaseServerAuth<?, ?> nextServerAuth : serviceVersion.getServerAuths()) {
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
			SidechannelOrchestratorResponseBean responseBean;
			Date startedTime = new Date();
			try {
				responseBean = doHandleSideChannelRequest(theServiceVersionPid, theRequestBody, theContentType, theRequestedByString, nextUrl);
				retVal.add(responseBean);
			} catch (ProcessingException e) {
				responseBean = SidechannelOrchestratorResponseBean.forFailure(e, startedTime, nextUrl);
				retVal.add(responseBean);
			} catch (UnknownRequestException e) {
				responseBean = SidechannelOrchestratorResponseBean.forFailure(e, startedTime, nextUrl);
				retVal.add(responseBean);
			}
		}

		return retVal;
	}
}
