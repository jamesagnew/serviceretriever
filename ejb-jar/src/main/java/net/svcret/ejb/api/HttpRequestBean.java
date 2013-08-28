package net.svcret.ejb.api;

import java.io.Reader;
import java.util.Date;
import java.util.List;
import java.util.Map;

import net.svcret.ejb.ejb.CapturingReader;

public class HttpRequestBean {

	private CapturingReader myInputReader;
	private String myPath;
	private String myQuery;
	private Map<String, List<String>> myRequestHeaders;
	private String myRequestHostIp;
	private Date myRequestTime;
	private RequestType myRequestType;

	/**
	 * @return the inputReader
	 */
	public Reader getInputReader() {
		return myInputReader;
	}

	/**
	 * @return the path
	 */
	public String getPath() {
		return myPath;
	}

	/**
	 * @return the query
	 */
	public String getQuery() {
		return myQuery;
	}

	public String getRequestBody() {
		return myInputReader.getCapturedString();
	}

	/**
	 * @return the requestHeaders
	 */
	public Map<String, List<String>> getRequestHeaders() {
		return myRequestHeaders;
	}

	/**
	 * @return the requestHostIp
	 */
	public String getRequestHostIp() {
		return myRequestHostIp;
	}

	public Date getRequestTime() {
		return myRequestTime;
	}

	/**
	 * @return the requestType
	 */
	public RequestType getRequestType() {
		return myRequestType;
	}

	/**
	 * @param theInputReader
	 *            the inputReader to set
	 */
	public void setInputReader(Reader theInputReader) {
		myInputReader = new CapturingReader(theInputReader);
	}

	/**
	 * @param thePath
	 *            the path to set
	 */
	public void setPath(String thePath) {
		myPath = thePath;
	}

	/**
	 * @param theQuery
	 *            the query to set
	 */
	public void setQuery(String theQuery) {
		myQuery = theQuery;
	}

	public void setRequestHeaders(Map<String, List<String>> theRequestHeaders) {
		myRequestHeaders = theRequestHeaders;
	}

	/**
	 * @param theRequestHostIp
	 *            the requestHostIp to set
	 */
	public void setRequestHostIp(String theRequestHostIp) {
		myRequestHostIp = theRequestHostIp;
	}

	public void setRequestTime(Date theRequestTime) {
		myRequestTime = theRequestTime;
	}

	/**
	 * @param theRequestType
	 *            the requestType to set
	 */
	public void setRequestType(RequestType theRequestType) {
		myRequestType = theRequestType;
	}

	public String getContentType() {
		if (myRequestHeaders==null) {
			return null;
		}
		List<String> headerValues = myRequestHeaders.get("Content-Type");
		if (headerValues==null||headerValues.isEmpty()) {
			return null;
		}
		
		String retVal = headerValues.get(0);
		int i = retVal.indexOf(';');
		if (i < -1) {
			retVal = retVal.substring(0, i);
		}
		retVal = retVal.trim();
		return retVal;
	}

}
