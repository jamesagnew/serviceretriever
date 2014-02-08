package net.svcret.core.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.svcret.admin.shared.model.Pair;
import net.svcret.core.model.entity.PersServiceVersionUrl;

public class SrBeanIncomingResponse {

	private String myBody;
	private int myCode;
	private String myContentType;
	private Map<PersServiceVersionUrl, Failure> myFailedUrls;
	private Map<String, List<String>> myHeaders;
	private long myResponseTime;
	private String myStatusLine;
	private PersServiceVersionUrl mySuccessfulUrl;
	
	public SrBeanIncomingResponse() {
		super();
	}
	public SrBeanIncomingResponse(PersServiceVersionUrl theSuccessfulUrl, String theContentType, int theCode, String theBody) {
		super();
		mySuccessfulUrl = theSuccessfulUrl;
		myCode = theCode;
		myBody = theBody;
		myContentType = theContentType;
	}

	public void addFailedUrl(PersServiceVersionUrl theUrl, String theFailureExplanation, int theStatusCode, String theContentType, String theBody, long theInvocationMillis,
			Map<String, List<String>> theHeaders) {
		if (myFailedUrls == null) {
			myFailedUrls = new HashMap<PersServiceVersionUrl, Failure>();
		}

		Map<String, List<String>> headers = theHeaders;
		if (headers == null) {
			headers = new HashMap<String, List<String>>();
		}

		myFailedUrls.put(theUrl, new Failure(theBody, theContentType, theFailureExplanation, theStatusCode, theInvocationMillis, headers));
	}

	/**
	 * @return the body
	 */
	public String getBody() {
		return myBody;
	}

	public int getCode() {
		return myCode;
	}

	public String getContentType() {
		return myContentType;
	}

	/**
	 * @return the failedUrlPids
	 */
	public Map<PersServiceVersionUrl, Failure> getFailedUrls() {
		if (myFailedUrls == null) {
			return Collections.emptyMap();
		}
		return myFailedUrls;
	}

	public String getFailingResponseBody() {
		for (Failure next : myFailedUrls.values()) {
			return next.getBody();
		}
		return null;
	}

	public Map<String, List<String>> getHeaders() {
		if (myHeaders != null) {
			return myHeaders;
		} else {
			return Collections.emptyMap();
		}
	}

	public List<Pair<String>> getResponseHeadersAsPairList() {
		ArrayList<Pair<String>> retVal = new ArrayList<Pair<String>>();
		for (Entry<String, List<String>> next : getHeaders().entrySet()) {
			for (String nextValue : next.getValue()) {
				retVal.add(new Pair<String>(next.getKey(), nextValue));
			}
		}
		return retVal;
	}

	public long getResponseTime() {
		return myResponseTime;
	}

	/**
	 * This method expects to have only one URL (either sucessful or failing) and returns that URL. If no URLS, or more than one, throws an exception.
	 */
	public PersServiceVersionUrl getSingleUrlOrThrow() {
		if (getSuccessfulUrl() != null) {
			if (getFailedUrls().size() > 0) {
				throw new IllegalStateException("HTTP Response bean contains more than one URL");
			}
			return getSuccessfulUrl();
		}
		if (getFailedUrls().size() > 1) {
			throw new IllegalStateException("HTTP Response bean contains more than one URL");
		}
		if (getFailedUrls().size() == 0) {
			throw new IllegalStateException("HTTP Response bean contains no URLs");
		}
		return getFailedUrls().keySet().iterator().next();
	}

	public String getStatusLine() {
		return myStatusLine;
	}

	/**
	 * @return the successfulUrl
	 */
	public PersServiceVersionUrl getSuccessfulUrl() {
		return mySuccessfulUrl;
	}

	/**
	 * @param theBody
	 *            the body to set
	 */
	public void setBody(String theBody) {
		myBody = theBody;
	}

	/**
	 * @param theCode
	 *            the code to set
	 */
	public void setCode(int theCode) {
		myCode = theCode;
	}

	/**
	 * @param theContentType
	 *            the contentType to set
	 */
	public void setContentType(String theContentType) {
		int indexOf = theContentType.indexOf(';');
		if (indexOf > 0) {
			myContentType = theContentType.substring(0, indexOf).trim();
		} else {
			myContentType = theContentType;
		}
	}

	/**
	 * @param theHeaders
	 *            the headers to set
	 */
	public void setHeaders(Map<String, List<String>> theHeaders) {
		myHeaders = theHeaders;
	}

	/**
	 * @param theResponseTime
	 *            the responseTime to set
	 */
	public void setResponseTime(long theResponseTime) {
		myResponseTime = theResponseTime;
	}

	public void setStatusLine(String theStatusLine) {
		myStatusLine = theStatusLine;
	}

	/**
	 * @param theSuccessfulUrl
	 *            the successfulUrl to set
	 */
	public void setSuccessfulUrl(PersServiceVersionUrl theSuccessfulUrl) {
		mySuccessfulUrl = theSuccessfulUrl;
	}

	public static class Failure {
		private String myBody;
		private String myContentType;
		private String myExplanation;
		private Map<String, List<String>> myHeaders;
		private long myInvocationMillis;
		private int myStatusCode;

		public Failure(String theBody, String theContentType, String theExplanation, int theStatusCode, long theInvocationMillis, Map<String, List<String>> theHeaders) {
			super();
			myBody = theBody;
			myContentType = theContentType;

			myExplanation = theExplanation;
			myStatusCode = theStatusCode;
			myInvocationMillis = theInvocationMillis;
			myHeaders = theHeaders;
		}


		/**
		 * @return the body
		 */
		public String getBody() {
			return myBody;
		}

		/**
		 * @return the contentType
		 */
		public String getContentType() {
			return myContentType;
		}

		/**
		 * @return the explanation
		 */
		public String getExplanation() {
			return myExplanation;
		}

		public Map<String, List<String>> getHeaders() {
			return myHeaders;
		}

		public long getInvocationMillis() {
			return myInvocationMillis;
		}

		/**
		 * @return the statusCode
		 */
		public int getStatusCode() {
			return myStatusCode;
		}
	}
}
