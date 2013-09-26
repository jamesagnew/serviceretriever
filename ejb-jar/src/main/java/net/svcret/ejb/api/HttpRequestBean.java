package net.svcret.ejb.api;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import net.svcret.ejb.ejb.CapturingReader;

public class HttpRequestBean {

	private String myBase;
	private transient String myContentType;
	private String myContextPath;
	private CapturingReader myInputReader;
	private String myPath;
	private String myProtocol;
	private String myQuery;
	private String myRequestFullUri;
	private Map<String, List<String>> myRequestHeaders;
	private String myRequestHostIp;
	private Date myRequestTime;
	private RequestType myRequestType;

	public void addHeader(String theHeader, String theValue) {
		if (myRequestHeaders == null) {
			myRequestHeaders = new HashMap<String, List<String>>();
		}
		if (!myRequestHeaders.containsKey(theHeader)) {
			myRequestHeaders.put(theHeader, new ArrayList<String>());
		}
		myRequestHeaders.get(theHeader).add(theValue);
	}

	public void drainInputMessage() {
		try {
			org.apache.commons.io.IOUtils.toString(myInputReader);
		} catch (IOException e) {
			// ignore
		}
	}

	/**
	 * Trailing "/" will be trimmed
	 */
	public String getBase() {
		String path = StringUtils.defaultString(myBase);
		if (path.length() > 0 && path.charAt(path.length() - 1) == '/') {
			path = path.substring(0, path.length() - 1);
		}
		return path;
	}

	public String getContentType() {
		if (myRequestHeaders == null) {
			return null;
		}
		if (myContentType != null) {
			return myContentType;
		}
		List<String> headerValues = myRequestHeaders.get("Content-Type");
		if (headerValues == null || headerValues.isEmpty()) {
			return null;
		}

		String retVal = headerValues.get(0);
		int i = retVal.indexOf(';');
		if (i < -1) {
			retVal = retVal.substring(0, i);
		}
		myContentType = retVal.trim();
		return retVal;
	}

	/**
	 * Any trailing "/" will be trimmed
	 */
	public String getContextPath() {
		String path = StringUtils.defaultString(myContextPath);
		if (path.length() > 0 && path.charAt(path.length() - 1) == '/') {
			path = path.substring(0, path.length() - 1);
		}
		return path;
	}

	/**
	 * @return the inputReader
	 */
	public Reader getInputReader() {
		return myInputReader;
	}

	/**
	 * E.g. "/SAIL_Infrastructure_JournallingService/JournallingWebService"
	 * 
	 * Any trailing "/" will be trimmed
	 */
	public String getPath() {
		String path = StringUtils.defaultString(myPath);
		if (path.length() > 0 && path.charAt(path.length() - 1) == '/') {
			path = path.substring(0, path.length() - 1);
		}
		return path;
	}

	public String getProtocol() {
		return myProtocol;
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

	public String getRequestFullUri() {
		return myRequestFullUri;
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

	public void setBase(String theBase) {
		myBase = theBase;
	}

	public void setContextPath(String theContextPath) {
		myContextPath = theContextPath;
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

	public void setProtocol(String theProtocol) {
		myProtocol = theProtocol;
	}

	/**
	 * @param theQuery
	 *            the query to set
	 */
	public void setQuery(String theQuery) {
		myQuery = theQuery;
	}

	public void setRequestFullUri(String theRequestURI) {
		myRequestFullUri = theRequestURI;
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

}
