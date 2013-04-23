package net.svcret.ejb.api;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;

import javax.ejb.Local;

import net.svcret.ejb.ex.InternalErrorException;
import net.svcret.ejb.ex.ProcessingException;
import net.svcret.ejb.ex.SecurityFailureException;
import net.svcret.ejb.ex.UnknownRequestException;


@Local
public interface IServiceOrchestrator {

	OrchestratorResponseBean handle(RequestType theRequestType, String thePath, String theQuery, Reader theReader) throws UnknownRequestException, InternalErrorException, ProcessingException, IOException, SecurityFailureException;

	/**
	 * Response type for {@link IServiceOrchestrator#handle(RequestType, String, String, Reader)}
	 */
	public static class OrchestratorResponseBean {
		private String myResponseBody;
		private String myResponseContentType;
		private Map<String, String> myResponseHeaders;

		public OrchestratorResponseBean(String theResponseBody, String theResponseContentType, Map<String, String> theResponseHeaders) {
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
		public Map<String, String> getResponseHeaders() {
			return myResponseHeaders;
		}

	}

}
