package net.svcret.core.orch;

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

import javax.servlet.AsyncContext;

import net.svcret.admin.api.ProcessingException;
import net.svcret.admin.api.UnexpectedFailureException;
import net.svcret.admin.shared.enm.AuthorizationOutcomeEnum;
import net.svcret.admin.shared.enm.ResponseTypeEnum;
import net.svcret.admin.shared.util.Validate;
import net.svcret.core.api.ICredentialGrabber;
import net.svcret.core.api.IHttpClient;
import net.svcret.core.api.IResponseValidator;
import net.svcret.core.api.IRuntimeStatus;
import net.svcret.core.api.ISecurityService;
import net.svcret.core.api.IServiceOrchestrator;
import net.svcret.core.api.IServiceRegistry;
import net.svcret.core.api.RequestType;
import net.svcret.core.api.SrBeanIncomingRequest;
import net.svcret.core.api.SrBeanIncomingResponse;
import net.svcret.core.api.SrBeanOutgoingResponse;
import net.svcret.core.api.SrBeanProcessedRequest;
import net.svcret.core.api.SrBeanProcessedResponse;
import net.svcret.core.api.UrlPoolBean;
import net.svcret.core.api.ISecurityService.AuthorizationRequestBean;
import net.svcret.core.api.ISecurityService.AuthorizationResultsBean;
import net.svcret.core.api.SrBeanIncomingResponse.Failure;
import net.svcret.core.api.SrBeanProcessedRequest.ResultTypeEnum;
import net.svcret.core.ex.InvalidRequestException;
import net.svcret.core.ex.InvocationFailedDueToInternalErrorException;
import net.svcret.core.ex.InvocationRequestFailedException;
import net.svcret.core.ex.InvocationRequestOrResponseFailedException;
import net.svcret.core.ex.InvocationResponseFailedException;
import net.svcret.core.ex.SecurityFailureException;
import net.svcret.core.ex.InvalidRequestException.IssueEnum;
import net.svcret.core.invoker.IServiceInvoker;
import net.svcret.core.invoker.hl7.IServiceInvokerHl7OverHttp;
import net.svcret.core.invoker.hl7.ServiceInvokerHl7OverHttp;
import net.svcret.core.invoker.jsonrpc.IServiceInvokerJsonRpc20;
import net.svcret.core.invoker.soap.IServiceInvokerSoap11;
import net.svcret.core.invoker.soap.InvocationFailedException;
import net.svcret.core.invoker.virtual.IServiceInvokerVirtual;
import net.svcret.core.log.ITransactionLogger;
import net.svcret.core.model.entity.BasePersAuthenticationHost;
import net.svcret.core.model.entity.BasePersServiceVersion;
import net.svcret.core.model.entity.PersBaseClientAuth;
import net.svcret.core.model.entity.PersBaseServerAuth;
import net.svcret.core.model.entity.PersHttpClientConfig;
import net.svcret.core.model.entity.PersMethod;
import net.svcret.core.model.entity.PersServiceVersionResource;
import net.svcret.core.model.entity.PersServiceVersionUrl;
import net.svcret.core.model.entity.PersUser;
import net.svcret.core.model.entity.http.PersHttpBasicClientAuth;
import net.svcret.core.model.entity.http.PersHttpBasicCredentialGrabber;
import net.svcret.core.model.entity.http.PersHttpBasicServerAuth;
import net.svcret.core.propcap.IPropertyCaptureService;
import net.svcret.core.propcap.PropertyCaptureBean;
import net.svcret.core.throttle.IThrottlingService;
import net.svcret.core.throttle.ThrottleException;
import net.svcret.core.throttle.ThrottleQueueFullException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.annotations.VisibleForTesting;

@Service
public class ServiceOrchestratorBean implements IServiceOrchestrator {

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(ServiceOrchestratorBean.class);

	@Autowired
	private IHttpClient myHttpClient;

	@Autowired
	private IPropertyCaptureService myPropertyCapture;

	@Autowired
	private IServiceInvokerJsonRpc20 myServiceInvokerJsonRpc20;

	@Autowired
	private IServiceInvokerHl7OverHttp myServiceInvokerHl7OverHttp;

	@Autowired
	private IRuntimeStatus myRuntimeStatus;

	@Autowired
	private ISecurityService mySecuritySvc;

	@Autowired
	private IServiceInvokerSoap11 myServiceInvokerSoap11;

	@Autowired
	private IServiceRegistry mySvcRegistry;

	@Autowired
	private IThrottlingService myThrottlingService;

	@Autowired
	private ITransactionLogger myTransactionLogger;

	@Autowired
	private IServiceInvokerVirtual myServiceInvokerVirtual;

	private SrBeanOutgoingResponse doHandleServiceRequest(SrBeanIncomingRequest theRequest) throws InvalidRequestException, SecurityFailureException, ThrottleException, ThrottleQueueFullException, InvocationRequestOrResponseFailedException,
			InvocationFailedDueToInternalErrorException {
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
			throw new InvalidRequestException(IssueEnum.INVALID_REQUEST_PATH, path, "Request did not match any known paths: " + path);
		}

		ourLog.trace("Request corresponds to service version {}", serviceVersion.getPid());

		/*
		 * Process request
		 */
		SrBeanProcessedRequest results = new SrBeanProcessedRequest();
		results.setServiceVersion(serviceVersion);

		AuthorizationResultsBean authorized;
		try {

			/*
			 * Process the invocation. This is the first part of handling a
			 * request, where we examine the request, determine where it's going
			 * and prep it for passing to the backend implementation (which
			 * happens later)
			 */
			results = processInvokeService(theRequest, serviceVersion);

			/*
			 * Security
			 * 
			 * Currently only active for method invocations
			 */
			List<PersBaseServerAuth<?, ?>> serverAuths = getServiceInvoker(serviceVersion).provideServerAuthorizationModules(serviceVersion);
			authorized = processSecurity(theRequest, serverAuths, results);

			/*
			 * Apply throttling if needed (may throw an exception if request is
			 * throttled)
			 */
			myThrottlingService.applyThrottle(theRequest, results, authorized);

		} catch (InvalidRequestException e) {
			ourLog.debug("Exception occurred", e);
			handleInvocationFailure(theRequest, results, new InvocationRequestFailedException(e), null, ResponseTypeEnum.FAIL);
			throw e;
		} catch (InvocationRequestOrResponseFailedException e) {
			ourLog.debug("Exception occurred", e);
			handleInvocationFailure(theRequest, results, e, null, ResponseTypeEnum.FAIL);
			throw e;
		} catch (InvocationFailedDueToInternalErrorException e) {
			ourLog.debug("Exception occurred", e);
			handleInvocationFailure(theRequest, results, e, null, ResponseTypeEnum.FAIL);
			throw e;
		} catch (ThrottleException e) {
			ourLog.debug("Exception occurred", e);
			/*
			 * Don't handle this failure, it's the responsibility of the
			 * throttling service to do so
			 */
			throw e;
		} catch (ThrottleQueueFullException e) {
			ourLog.debug("Exception occurred", e);
			/*
			 * Don't handle this failure, it's the responsibility of the
			 * throttling service to do so
			 */
			throw e;
		} catch (SecurityFailureException e) {
			ourLog.debug("Exception occurred", e);
			handleInvocationFailure(theRequest, results, new InvocationRequestFailedException(e), null, ResponseTypeEnum.SECURITY_FAIL);
			throw e;
		} catch (Exception e) {
			ourLog.error("Unexpected failure", e);
			handleInvocationFailure(theRequest, results, new InvocationRequestFailedException(e), null, ResponseTypeEnum.FAIL);
			throw new InvocationFailedDueToInternalErrorException(e);
		}

		/*
		 * Forward request to backend implementation
		 */
		ourLog.debug("Request is of type: {}", results.getResultType());
		SrBeanOutgoingResponse retVal;
		try {
			retVal = invokeProxiedService(theRequest, results, authorized);
		} catch (InvocationFailedDueToInternalErrorException e) {
			throw e;
		}

		return retVal;
	}

	private void handleInvocationFailure(SrBeanIncomingRequest theRequest, SrBeanProcessedRequest theProcessedRequest, InvocationFailedException theInvocationFailure, PersUser theUserIfKnown, ResponseTypeEnum theResponseType) {
		theRequest.drainInputMessage();

		try {
			SrBeanProcessedResponse invocationResponseResultsBean = new SrBeanProcessedResponse();
			invocationResponseResultsBean.setResponseType(theResponseType);
			myRuntimeStatus.recordInvocationMethod(theRequest.getRequestTime(), theRequest.getRequestBody().length(), theProcessedRequest, theUserIfKnown, null, invocationResponseResultsBean, theRequest);
		} catch (UnexpectedFailureException e) {
			// Don't do anything except log here since we're already handling a
			// failure by the
			// time we get here so this is pretty much the last resort..
			ourLog.error("Failed to record method invocation", e);
		} catch (InvocationFailedDueToInternalErrorException e) {
			// Don't do anything except log here since we're already handling a
			// failure by the
			// time we get here so this is pretty much the last resort..
			ourLog.error("Failed to record method invocation", e);
		}

		try {

			String requestBody = theRequest.getRequestBody();
			String responseBody = null;
			if (theInvocationFailure.getHttpResponse() != null) {
				responseBody = theInvocationFailure.getHttpResponse().getFailingResponseBody();
			}

			// Try to obscure
			SrBeanProcessedResponse processedResponse = theInvocationFailure.toInvocationResponse();
			if (theProcessedRequest.getServiceVersion() != null) {
				try {
					BasePersServiceVersion svcVer = theProcessedRequest.getServiceVersion();
					IServiceInvoker svcInvoker = getServiceInvoker(svcVer);
					requestBody = svcInvoker.obscureMessageForLogs(svcVer, requestBody, svcVer.determineObscureRequestElements());
					responseBody = svcInvoker.obscureMessageForLogs(svcVer, responseBody, svcVer.determineObscureResponseElements());

					theProcessedRequest.setObscuredRequestBody(requestBody);
					processedResponse.setObscuredResponseBody(responseBody);

				} catch (Exception e) {
					ourLog.debug("Failed to obscure message", e);
				}
			}

			myTransactionLogger.logTransaction(theRequest, theUserIfKnown, processedResponse, theInvocationFailure.getHttpResponse(), null, theProcessedRequest);

		} catch (UnexpectedFailureException e1) {
			// Don't do anything except log here since we're already handling a
			// failure by the
			// time we get here so this is pretty much the last resort..
			ourLog.error("Failed to record method invocation", e1);
		} catch (ProcessingException e1) {
			// Don't do anything except log here since we're already handling a
			// failure by the
			// time we get here so this is pretty much the last resort..
			ourLog.error("Failed to record method invocation", e1);
		}

	}

	@Override
	public SrBeanOutgoingResponse handlePreviouslyThrottledRequest(SrBeanProcessedRequest theInvocationRequest, AuthorizationResultsBean theAuthorization, SrBeanIncomingRequest theRequest) throws SecurityFailureException, InvocationFailedDueToInternalErrorException {
		return invokeProxiedService(theRequest, theInvocationRequest, theAuthorization);
	}

	@Override
	public SrBeanOutgoingResponse handleServiceRequest(SrBeanIncomingRequest theRequest) throws InvalidRequestException, SecurityFailureException, ThrottleException, ThrottleQueueFullException, InvocationRequestOrResponseFailedException,
			InvocationFailedDueToInternalErrorException {
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

	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	@Override
	public SidechannelOrchestratorResponseBean handleSidechannelRequest(long theServiceVersionPid, String theRequestBody, String theContentType, String theRequestedByString) throws InvalidRequestException, InvocationFailedException {
		return doHandleSideChannelRequest(theServiceVersionPid, theRequestBody, theContentType, theRequestedByString, null);
	}

	private SidechannelOrchestratorResponseBean doHandleSideChannelRequest(long theServiceVersionPid, String theRequestBody, String theContentType, String theRequestedByString, PersServiceVersionUrl theForceUrl) throws InvalidRequestException, InvocationFailedException {
		Date startTime = new Date();
		BasePersServiceVersion svcVer = mySvcRegistry.getServiceVersionByPid(theServiceVersionPid);

		StringReader reader = (new StringReader(theRequestBody));

		SrBeanIncomingRequest request = new SrBeanIncomingRequest();
		request.setRequestTime(startTime);
		request.setInputReader(reader);
		request.setRequestHostIp("127.0.0.1");
		request.setPath(svcVer.determineUsableProxyPath());
		request.setRequestHeaders(new HashMap<String, List<String>>());
		request.getRequestHeaders().put("X-RequestedBy", Collections.singletonList(theRequestedByString));
		request.getRequestHeaders().put("Content-Type", Collections.singletonList(theContentType));
		request.setRequestType(RequestType.POST); // TODO this should be
													// configurable, maybe
													// method specific or
													// something?
		AuthorizationResultsBean authorized = null;

		SrBeanProcessedRequest results = processInvokeService(request, svcVer);
		SidechannelOrchestratorResponseBean retVal = processRequestMethod(request, results, authorized, false, theForceUrl);
		retVal.setSimulatedIncomingRequest(request);
		retVal.setSimulatedProcessedRequest(results);

		return retVal;
	}

	private void logTransaction(SrBeanIncomingRequest theIncomingRequest, AuthorizationResultsBean authorized, SrBeanIncomingResponse httpResponse, SrBeanProcessedResponse theProcessedResponse, SrBeanProcessedRequest theProcessedRequest)
			throws InvocationFailedDueToInternalErrorException {
		PersUser user = authorized.getAuthorizedUser();

		// Log
		AuthorizationOutcomeEnum authorizationOutcome = authorized.isAuthorized();
		try {
			myTransactionLogger.logTransaction(theIncomingRequest, user, theProcessedResponse, httpResponse, authorizationOutcome, theProcessedRequest);
		} catch (ProcessingException e) {
			throw new InvocationFailedDueToInternalErrorException(e);
		} catch (UnexpectedFailureException e) {
			throw new InvocationFailedDueToInternalErrorException(e);
		}
	}

	private SrBeanOutgoingResponse invokeProxiedService(SrBeanIncomingRequest theRequest, SrBeanProcessedRequest results, AuthorizationResultsBean theAuthorized) throws SecurityFailureException, InvocationFailedDueToInternalErrorException {

		if (theAuthorized != null && theAuthorized.isAuthorized() != AuthorizationOutcomeEnum.AUTHORIZED) {
			SrBeanProcessedResponse invocationResponse = new SrBeanProcessedResponse();
			invocationResponse.setResponseType(ResponseTypeEnum.SECURITY_FAIL);
			invocationResponse.setResponseStatusMessage("Failed to authorize credentials");
			// TODO: also pass authorization outcome to save it
			try {
				myRuntimeStatus.recordInvocationMethod(theRequest.getRequestTime(), 0, results, theAuthorized.getAuthorizedUser(), null, invocationResponse, theRequest);
			} catch (UnexpectedFailureException e) {
				throw new InvocationFailedDueToInternalErrorException(e);
			}

			// Log transaction
			logTransaction(theRequest, theAuthorized, null, invocationResponse, results);

			throw new SecurityFailureException(theAuthorized.isAuthorized(), invocationResponse.getResponseStatusMessage());
		}

		SrBeanOutgoingResponse retVal;
		switch (results.getResultType()) {
		case STATIC_RESOURCE:
			retVal = processRequestResource(theRequest, results);
			break;
		case METHOD:
			PersUser user = theAuthorized != null ? theAuthorized.getAuthorizedUser() : null;
			try {
				retVal = processRequestMethod(theRequest, results, theAuthorized, true, null);
			} catch (InvocationResponseFailedException e) {
				handleInvocationFailure(theRequest, results, e, user, ResponseTypeEnum.FAIL);
				throw new InvocationFailedDueToInternalErrorException(e);
			} catch (RuntimeException e) {
				handleInvocationFailure(theRequest, results, new InvocationResponseFailedException(e, "Invocation failed due to ServiceRetriever internal error: " + e.getMessage(), null), user, ResponseTypeEnum.FAIL);
				throw new InvocationFailedDueToInternalErrorException(e);
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

	private SrBeanProcessedRequest processInvokeService(SrBeanIncomingRequest theRequest, BasePersServiceVersion theServiceVersion) throws InvocationFailedException, InvalidRequestException {
		SrBeanProcessedRequest results = null;
		IServiceInvoker svcInvoker = getServiceInvoker(theServiceVersion);
		ourLog.trace("Handling service with invoker {}", svcInvoker);

		if (svcInvoker == null) {
			throw new InvocationFailedDueToInternalErrorException("Unknown service protocol: " + theServiceVersion.getProtocol());
		}

		results = svcInvoker.processInvocation(theRequest, theServiceVersion);

		if (results == null) {
			throw new InvocationFailedDueToInternalErrorException("Invoker " + svcInvoker + " returned null");
		}

		results.setServiceVersion(theServiceVersion);

		if (results.getResultType() == ResultTypeEnum.METHOD) {
			results.setMethodHeaders(svcInvoker.createBackingRequestHeadersForMethodInvocation(theServiceVersion, theRequest.getRequestHeaders()));
		}

		for (PersBaseClientAuth<?> next : theServiceVersion.getClientAuths()) {
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

		myPropertyCapture.captureRequestProperties(theServiceVersion, theRequest, results);

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

	private SidechannelOrchestratorResponseBean processRequestMethod(SrBeanIncomingRequest theIncomingRequest, SrBeanProcessedRequest theProcessedRequest, AuthorizationResultsBean authorized, boolean theRecordOutcome, PersServiceVersionUrl theForceUrl)
			throws InvocationResponseFailedException, InvocationFailedDueToInternalErrorException {
		PersMethod method = theProcessedRequest.getMethodDefinition();
		BasePersServiceVersion serviceVersion = method.getServiceVersion();
		Map<String, List<String>> headers = theProcessedRequest.getMethodHeaders();
		String contentType = theProcessedRequest.getMethodContentType();
		String contentBody = theProcessedRequest.getMethodRequestBody();

		IServiceInvoker invoker = getServiceInvoker(method.getServiceVersion());
		IResponseValidator responseValidator = invoker.provideInvocationResponseValidator(serviceVersion);

		UrlPoolBean urlPool;
		if (theForceUrl == null) {
			try {
				urlPool = myRuntimeStatus.buildUrlPool(method.getServiceVersion(), theIncomingRequest);
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
		SrBeanIncomingResponse httpResponse;
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
			throw new InvocationResponseFailedException(b.toString(), httpResponse);
		}

		SrBeanProcessedResponse invocationResponse = invoker.processInvocationResponse(serviceVersion, httpResponse);
		invocationResponse.validate();

		/*
		 * Obscure request and response
		 */
		BasePersServiceVersion svcVer = method.getServiceVersion();
		IServiceInvoker svcInvoker = getServiceInvoker(svcVer);

		String requestBody = theIncomingRequest.getRequestBody();
		String obscuredRequestBody = svcInvoker.obscureMessageForLogs(svcVer, requestBody, method.getServiceVersion().determineObscureRequestElements());
		theProcessedRequest.setObscuredRequestBody(obscuredRequestBody);

		String responseBody = invocationResponse.getResponseBody();
		String obscuredResponseBody = svcInvoker.obscureMessageForLogs(svcVer, responseBody, method.getServiceVersion().determineObscureResponseElements());
		invocationResponse.setObscuredResponseBody(obscuredResponseBody);

		/*
		 * Logging/Auditing
		 */
		int requestLength = contentBody.length();
		PersUser user = ((authorized != null && authorized.isAuthorized() == AuthorizationOutcomeEnum.AUTHORIZED) ? authorized.getAuthorizedUser() : null);

		if (theRecordOutcome) {
			try {
				myRuntimeStatus.recordInvocationMethod(theIncomingRequest.getRequestTime(), requestLength, theProcessedRequest, user, httpResponse, invocationResponse, theIncomingRequest);
			} catch (UnexpectedFailureException e) {
				throw new InvocationFailedDueToInternalErrorException(e);
			}
			logTransaction(theIncomingRequest, authorized, httpResponse, invocationResponse, theProcessedRequest);
		}

		/*
		 * Return the results back to the incoming client
		 */
		return new SidechannelOrchestratorResponseBean(theIncomingRequest, invocationResponse, httpResponse);

	}

	private SrBeanOutgoingResponse processRequestResource(SrBeanIncomingRequest theRequest, SrBeanProcessedRequest results) {
		String responseBody = results.getStaticResourceText();
		String responseContentType = results.getStaticResourceContentTyoe();
		Map<String, List<String>> responseHeaders = results.getStaticResourceHeaders();
		SrBeanOutgoingResponse retVal = new SrBeanOutgoingResponse(responseBody, responseContentType, responseHeaders);

		PersServiceVersionResource resource = results.getStaticResourceDefinition();
		myRuntimeStatus.recordInvocationStaticResource(theRequest.getRequestTime(), resource);

		ourLog.trace("Handling request for static URL contents: {}", resource);
		return retVal;
	}

	private AuthorizationResultsBean processSecurity(SrBeanIncomingRequest theRequest, List<PersBaseServerAuth<?, ?>> theServerAuths, SrBeanProcessedRequest results) throws SecurityFailureException, ProcessingException {
		AuthorizationResultsBean authorized = null;
		if (results.getResultType() == ResultTypeEnum.METHOD) {
			if (ourLog.isDebugEnabled()) {
				if (theServerAuths.isEmpty()) {
					ourLog.trace("Service has no server auths");
				} else {
					ourLog.trace("Service has the following server auths: {}", theServerAuths);
				}
			}

			PersMethod method = results.getMethodDefinition();
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
	public
	void setHttpClient(IHttpClient theHttpClient) {
		myHttpClient = theHttpClient;
	}

	/**
	 * FOR UNIT TESTS ONLY
	 */
	@VisibleForTesting
	public
	void setRuntimeStatus(IRuntimeStatus theRuntimeStatus) {
		myRuntimeStatus = theRuntimeStatus;
	}

	/**
	 * FOR UNIT TESTS ONLY
	 */
	@VisibleForTesting
	public
	void setSecuritySvc(ISecurityService theSecuritySvc) {
		mySecuritySvc = theSecuritySvc;
	}

	/**
	 * FOR UNIT TESTS ONLY
	 */
	@VisibleForTesting
	public void setSoap11ServiceInvoker(IServiceInvokerSoap11 theSoap11ServiceInvoker) {
		myServiceInvokerSoap11 = theSoap11ServiceInvoker;
	}

	/**
	 * FOR UNIT TESTS ONLY
	 */
	@VisibleForTesting
	public
	void setSvcRegistry(IServiceRegistry theSvcRegistry) {
		mySvcRegistry = theSvcRegistry;
	}

	/**
	 * Unit tests only
	 */
	@VisibleForTesting
	public void setTransactionLogger(ITransactionLogger theTransactionLogger) {
		myTransactionLogger = theTransactionLogger;
	}

	@Override
	public void enqueueThrottledRequest(ThrottleException theE, AsyncContext theAsyncContext) throws ThrottleQueueFullException {
		myThrottlingService.scheduleThrottledTaskForLaterExecution(theE, theAsyncContext);
	}

	/**
	 * Unit tests only
	 */
	@VisibleForTesting
	public void setThrottlingService(IThrottlingService theMock) {
		myThrottlingService = theMock;
	}

	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	@Override
	public Collection<SidechannelOrchestratorResponseBean> handleSidechannelRequestForEachUrl(long theServiceVersionPid, String theRequestBody, String theContentType, String theRequestedByString) {

		BasePersServiceVersion svcVer = mySvcRegistry.getServiceVersionByPid(theServiceVersionPid);
		if (svcVer == null) {
			throw new IllegalArgumentException("Unknown service version " + theServiceVersionPid);
		}

		ArrayList<SidechannelOrchestratorResponseBean> retVal = new ArrayList<IServiceOrchestrator.SidechannelOrchestratorResponseBean>();

		for (PersServiceVersionUrl nextUrl : svcVer.getUrls()) {
			ourLog.debug("About to perform sidechannel request for URL: {} / {}", nextUrl.getPid(), nextUrl.getUrl());

			SrBeanIncomingRequest incomingRequest = new SrBeanIncomingRequest();
			Date startedTime = new Date();
			incomingRequest.setRequestTime(startedTime);

			SrBeanProcessedResponse processedResponse = new SrBeanProcessedResponse();
			processedResponse.setResponseHeaders(new HashMap<String, List<String>>());
			processedResponse.setResponseType(ResponseTypeEnum.FAIL);

			SrBeanIncomingResponse incomingResponse = new SrBeanIncomingResponse();

			SidechannelOrchestratorResponseBean responseBean;
			try {
				responseBean = doHandleSideChannelRequest(theServiceVersionPid, theRequestBody, theContentType, theRequestedByString, nextUrl);
				retVal.add(responseBean);
			} catch (InvalidRequestException e) {
				ourLog.debug("Failed to execute sidechannel request for URL", e);
				responseBean = SidechannelOrchestratorResponseBean.forFailure(e, nextUrl, incomingRequest, processedResponse, incomingResponse);
				retVal.add(responseBean);
			} catch (InvocationFailedException e) {
				ourLog.debug("Failed to execute sidechannel request for URL", e);
				responseBean = SidechannelOrchestratorResponseBean.forFailure(e, nextUrl, incomingRequest, processedResponse, incomingResponse);
				retVal.add(responseBean);
			}
		}

		return retVal;
	}

	@VisibleForTesting
	public
	void setServiceInvokerHl7OverhttpForUnitTests(ServiceInvokerHl7OverHttp theHl7Invoker) {
		myServiceInvokerHl7OverHttp = theHl7Invoker;
	}

	@VisibleForTesting
	public void setServiceInvokerVirtualForUnitTests(IServiceInvokerVirtual theVirtualInvoker) {
		myServiceInvokerVirtual = theVirtualInvoker;
	}

	@VisibleForTesting
	public void setPropertyCaptureForUnitTests(PropertyCaptureBean thePropertyCapture) {
		myPropertyCapture = thePropertyCapture;
	}
}
