package net.svcret.core.api;

import java.util.List;
import java.util.Map;

/**
 * Response type for {@link IServiceOrchestrator#handle(RequestType, String, String, Reader)}
 */
public class SrBeanOutgoingResponse {
	private String myResponseBody;
	private String myResponseContentType;
	private Map<String, List<String>> myResponseHeaders;

	public SrBeanOutgoingResponse(String theResponseBody, String theResponseContentType, Map<String, List<String>> theResponseHeaders) {
		super();
		myResponseBody = theResponseBody;
		myResponseContentType = theResponseContentType;
		myResponseHeaders = theResponseHeaders;
	}

	public SrBeanOutgoingResponse(SrBeanProcessedResponse theProcessedResponse) {
		super();
		myResponseBody = theProcessedResponse.getResponseBody();
		myResponseContentType = theProcessedResponse.getResponseContentType();
		myResponseHeaders = theProcessedResponse.getResponseHeaders();
	}

//	public SrBeanIncomingResponse getHttpResponse() {
//		return myHttpResponse;
//	}

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