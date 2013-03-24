package net.svcret.ejb.api;

import java.util.Map;

/**
 * Return type for {@link IServiceInvoker#}
 */
public class InvocationResponseResultsBean {

	private String myResponseBody;
	private String myResponseContentType;
	private String myResponseFaultCode;
	private String myResponseFaultDescription;
	private Map<String, String> myResponseHeaders;
	private String myResponseStatusMessage;
	private ResponseTypeEnum myResponseType;

	public String getResponseBody() {
		return myResponseBody;
	}

	public String getResponseContentType() {
		return myResponseContentType;
	}

	/**
	 * @return the responseFaultCode
	 */
	public String getResponseFaultCode() {
		return myResponseFaultCode;
	}

	/**
	 * @return the responseFaultDescription
	 */
	public String getResponseFaultDescription() {
		return myResponseFaultDescription;
	}

	public Map<String, String> getResponseHeaders() {
		return myResponseHeaders;
	}

	/**
	 * @return the responseStatusMessage
	 */
	public String getResponseStatusMessage() {
		return myResponseStatusMessage;
	}

	/**
	 * @return the responseType
	 */
	public ResponseTypeEnum getResponseType() {
		return myResponseType;
	}

	/**
	 * @param theResponseBody
	 *            the responseBody to set
	 */
	public void setResponseBody(String theResponseBody) {
		myResponseBody = theResponseBody;
	}

	/**
	 * @param theResponseContentType
	 *            the responseContentType to set
	 */
	public void setResponseContentType(String theResponseContentType) {
		myResponseContentType = theResponseContentType;
	}

	public void setResponseFaultCode(String theData) {
		myResponseFaultCode = theData;
	}

	public void setResponseFaultDescription(String theData) {
		myResponseFaultDescription = theData;
	}

	/**
	 * @param theResponseHeaders
	 *            the responseHeaders to set
	 */
	public void setResponseHeaders(Map<String, String> theResponseHeaders) {
		myResponseHeaders = theResponseHeaders;
	}

	public void setResponseStatusMessage(String theString) {
		myResponseStatusMessage = theString;
	}

	public void setResponseType(ResponseTypeEnum theResponseType) {
		myResponseType = theResponseType;
	}

}
