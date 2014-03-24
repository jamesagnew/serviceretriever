package net.svcret.core.api;

import java.io.IOException;
import java.io.Reader;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLSession;

import net.svcret.core.ejb.CapturingReader;

import org.apache.commons.lang3.StringUtils;

public class SrBeanIncomingRequest {

	private String myPathToSvcVer;
	private String myBase;
	private transient String myContentType;
	private String myContextPath;
	private CapturingReader myInputReader;
	private String myPath;
	private int myPort;
	private String myProtocol;
	private String myQuery;
	private String myRequestFullUri;
	private CaseInsensitiveMap<List<String>> myRequestHeaders;
	private String myRequestHostIp;
	private Date myRequestTime;
	private RequestType myRequestType;
	private String myTlsCipherSuite;
	private X509Certificate[] myTlsClientCertificates;
	private Integer myTlsKeySize;
	private SSLSession myTlsSession;

	public void addHeader(String theHeader, String theValue) {
		if (myRequestHeaders == null) {
			myRequestHeaders = new CaseInsensitiveMap<>();
		}
		if (!myRequestHeaders.containsKey(theHeader)) {
			myRequestHeaders.put(theHeader, new ArrayList<String>());
		}
		myRequestHeaders.get(theHeader).add(theValue.toLowerCase());
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
		List<String> headerValues = myRequestHeaders.get("content-type");
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

	public String getPathToSvcVer() {
		return myPathToSvcVer;
	}


	public void setPathToSvcVer(String thePathToSvcVer) {
		myPathToSvcVer = thePathToSvcVer;
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

	public int getPort() {
		return myPort;
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

	public String getTlsCipherSuite() {
		return myTlsCipherSuite;
	}

	public X509Certificate[] getTlsClientCertificates() {
		return myTlsClientCertificates;
	}

	public Integer getTlsKeySize() {
		return myTlsKeySize;
	}

	public SSLSession getTlsSession() {
		return myTlsSession;
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

	public void setPort(int thePort) {
		myPort = thePort;
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
		myRequestHeaders = new CaseInsensitiveMap<>(theRequestHeaders);
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

	public void setTlsCipherSuite(String theTlsCipherSuite) {
		myTlsCipherSuite = theTlsCipherSuite;
	}

	public void setTlsClientCertificates(X509Certificate[] theTlsClientCertificates) {
		myTlsClientCertificates = theTlsClientCertificates;
	}

	public void setTlsKeySize(Integer theTlsKeySize) {
		myTlsKeySize = theTlsKeySize;
	}

	public void setTlsSession(SSLSession theTlsSession) {
		myTlsSession = theTlsSession;
	}

	private static class CaseInsensitiveMap<T> extends HashMap<String, T> {

		private static final long serialVersionUID = 1L;

		public CaseInsensitiveMap() {
		}

		public CaseInsensitiveMap(Map<String, T> theRequestHeaders) {
			for (java.util.Map.Entry<String, T> nextEntry : theRequestHeaders.entrySet()) {
				put(nextEntry.getKey(), nextEntry.getValue());
			}
		}

		@Override
		public boolean containsKey(Object theKey) {
			return super.containsKey(((String) theKey).toLowerCase());
		}

		@Override
		public T get(Object theKey) {
			return super.get(((String) theKey).toLowerCase());
		}

		@Override
		public T put(String key, T value) {
			return super.put(key.toLowerCase(), value);
		}

		@Override
		public T remove(Object theKey) {
			return super.remove(((String) theKey).toLowerCase());
		}

	}

}
