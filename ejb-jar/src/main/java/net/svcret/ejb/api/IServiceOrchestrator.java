package net.svcret.ejb.api;

import java.io.IOException;
import java.io.Reader;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.ejb.Local;

import net.svcret.admin.shared.model.Pair;
import net.svcret.ejb.ex.InternalErrorException;
import net.svcret.ejb.ex.ProcessingException;
import net.svcret.ejb.ex.SecurityFailureException;
import net.svcret.ejb.ex.UnknownRequestException;

@Local
public interface IServiceOrchestrator {

	/**
	 * Process a normal request
	 */
	OrchestratorResponseBean handleServiceRequest(HttpRequestBean theRequest) throws UnknownRequestException, InternalErrorException, ProcessingException, IOException, SecurityFailureException;

	/**
	 * Process a request invoked through a means other than the proxy itself (e.g. monitoring, management console, etc.)
	 */
	SidechannelOrchestratorResponseBean handleSidechannelRequest(long theServiceVersionPid, String theRequestBody) throws UnknownRequestException, InternalErrorException, ProcessingException,
			IOException, SecurityFailureException;

	/**
	 * Response type for {@link IServiceOrchestrator#handle(RequestType, String, String, Reader)}
	 */
	public static class OrchestratorResponseBean {
		private String myResponseBody;
		private String myResponseContentType;
		private Map<String, List<String>> myResponseHeaders;

		public OrchestratorResponseBean(String theResponseBody, String theResponseContentType, Map<String, List<String>> theResponseHeaders) {
			super();
			myResponseBody = theResponseBody;
			myResponseContentType = theResponseContentType;
			myResponseHeaders = theResponseHeaders;
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

		private HttpResponseBean myHttpResponse;

		public SidechannelOrchestratorResponseBean(String theResponseBody, String theResponseContentType, Map<String, List<String>> theResponseHeaders, HttpResponseBean theHttpResponse) {
			super(theResponseBody, theResponseContentType, theResponseHeaders);
			myHttpResponse = theHttpResponse;
		}

		public HttpResponseBean getHttpResponse() {
			return myHttpResponse;
		}

	}

}
