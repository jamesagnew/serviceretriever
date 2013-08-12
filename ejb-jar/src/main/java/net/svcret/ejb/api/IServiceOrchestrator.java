package net.svcret.ejb.api;

import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.ejb.Local;

import net.svcret.admin.shared.enm.ResponseTypeEnum;
import net.svcret.ejb.api.ISecurityService.AuthorizationResultsBean;
import net.svcret.ejb.ejb.ThrottleQueueFullException;
import net.svcret.ejb.ex.InternalErrorException;
import net.svcret.ejb.ex.ProcessingException;
import net.svcret.ejb.ex.SecurityFailureException;
import net.svcret.ejb.ex.ThrottleException;
import net.svcret.ejb.ex.UnknownRequestException;

@Local
public interface IServiceOrchestrator {

	void enqueueThrottledRequest(ThrottleException theE) throws ThrottleQueueFullException;

	OrchestratorResponseBean handlePreviouslyThrottledRequest(InvocationResultsBean theInvocationRequest, AuthorizationResultsBean theAuthorization, HttpRequestBean theRequest, long theThrottleTime)
			throws ProcessingException, SecurityFailureException;

	/**
	 * Process a normal request
	 */
	OrchestratorResponseBean handleServiceRequest(HttpRequestBean theRequest) throws UnknownRequestException, InternalErrorException, ProcessingException, IOException, SecurityFailureException,
			ThrottleException, ThrottleQueueFullException;

	/**
	 * Process a request invoked through a means other than the proxy itself (e.g. monitoring, management console, etc.)
	 */
	SidechannelOrchestratorResponseBean handleSidechannelRequest(long theServiceVersionPid, String theRequestBody, String theContentType, String theRequestedByString) throws InternalErrorException,
			ProcessingException, UnknownRequestException;

	Collection<SidechannelOrchestratorResponseBean> handleSidechannelRequestForEachUrl(long theServiceVersionPid, String theRequestBody, String theContentType, String theRequestedByString)
			throws InternalErrorException, ProcessingException, UnknownRequestException;

	/**
	 * Response type for {@link IServiceOrchestrator#handle(RequestType, String, String, Reader)}
	 */
	public static class OrchestratorResponseBean {
		private HttpResponseBean myHttpResponse;
		private String myResponseBody;
		private String myResponseContentType;
		private Map<String, List<String>> myResponseHeaders;

		public OrchestratorResponseBean(String theResponseBody, String theResponseContentType, Map<String, List<String>> theResponseHeaders, HttpResponseBean theHttpResponse) {
			super();
			myResponseBody = theResponseBody;
			myResponseContentType = theResponseContentType;
			myResponseHeaders = theResponseHeaders;
			myHttpResponse = theHttpResponse;
		}

		public HttpResponseBean getHttpResponse() {
			return myHttpResponse;
		}

		/**
		 * @return the responseBody
		 */
		public String getResponseBody() {
			return myResponseBody;
		}

		/**
		 * @return the responseContentType
		 */
		public String getResponseContentType() {
			return myResponseContentType;
		}

		/**
		 * @return the responseHeaders
		 */
		public Map<String, List<String>> getResponseHeaders() {
			return myResponseHeaders;
		}

	}

	public static class SidechannelOrchestratorResponseBean extends OrchestratorResponseBean {

		private ResponseTypeEnum myResponseType;
		
		public SidechannelOrchestratorResponseBean(String theResponseBody, String theResponseContentType, Map<String, List<String>> theResponseHeaders, HttpResponseBean theHttpResponse, ResponseTypeEnum theResponseType) {
			super(theResponseBody, theResponseContentType, theResponseHeaders, theHttpResponse);
			
			myResponseType=theResponseType;
		}

		public ResponseTypeEnum getResponseType() {
			return myResponseType;
		}

	}

}
