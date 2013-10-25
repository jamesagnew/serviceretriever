package net.svcret.ejb.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.svcret.admin.shared.model.Pair;
import net.svcret.ejb.model.entity.PersServiceVersionUrl;

public class HttpResponseBean {

	private String myBody;
	private int myCode;
	private String myContentType;
	private Map<PersServiceVersionUrl, Failure> myFailedUrls;
	private Map<String, List<String>> myHeaders;
	private long myResponseTime;
	private PersServiceVersionUrl mySuccessfulUrl;

	/**
	 * @return the successfulUrl
	 */
	public PersServiceVersionUrl getSuccessfulUrl() {
		return mySuccessfulUrl;
	}

	/**
	 * @param theSuccessfulUrl
	 *            the successfulUrl to set
	 */
	public void setSuccessfulUrl(PersServiceVersionUrl theSuccessfulUrl) {
		mySuccessfulUrl = theSuccessfulUrl;
	}

	public HttpResponseBean() {
		super();
	}

	public HttpResponseBean(PersServiceVersionUrl theSuccessfulUrl, String theContentType, int theCode, String theBody) {
		super();
		mySuccessfulUrl = theSuccessfulUrl;
		myCode = theCode;
		myBody = theBody;
		myContentType = theContentType;
	}

	public void addFailedUrl(PersServiceVersionUrl theUrl, String theFailureExplanation, int theStatusCode, String theContentType, String theBody, long theInvocationMillis) {
		if (myFailedUrls == null) {
			myFailedUrls = new HashMap<PersServiceVersionUrl, Failure>();
		}

		myFailedUrls.put(theUrl, new Failure(theBody, theContentType, theFailureExplanation, theStatusCode, theInvocationMillis));
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

	public Map<String, List<String>> getHeaders() {
		return myHeaders;
	}

	public long getResponseTime() {
		return myResponseTime;
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

	public static class Failure {
		private String myBody;
		private String myContentType;
		private String myExplanation;
		private int myStatusCode;
		private long myInvocationMillis;

		public Failure(String theBody, String theContentType, String theExplanation, int theStatusCode, long theInvocationMillis) {
			super();
			myBody = theBody;
			myContentType = theContentType;
			myExplanation = theExplanation;
			myStatusCode = theStatusCode;
			myInvocationMillis=theInvocationMillis;
		}

		public long getInvocationMillis() {
			return myInvocationMillis;
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

		/**
		 * @return the statusCode
		 */
		public int getStatusCode() {
			return myStatusCode;
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

	/**
	 * This method expects to have only one URL (either sucessful or failing) and returns
	 * that URL. If no URLS, or more than one, throws an exception.
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

	public String getFailingResponseBody() {
		for (Failure next : myFailedUrls.values()) {
			return next.getBody();
		}
		return null;
	}
}
