package net.svcret.ejb.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class UrlPoolBean {

	private List<String> myAlternateUrls;
	private int myConnectTimeoutMillis;
	private int myFailureRetriesBeforeAborting;
	private String myPreferredUrl;
	private int myReadTimeoutMillis;

	public UrlPoolBean() {
		
	}
	
	public Collection<String> getAllUrls() {
		ArrayList<String> retVal = new ArrayList<String>();
		retVal.add(getPreferredUrl());
		retVal.addAll(getAlternateUrls());
		return retVal;
	}

	/**
	 * @return the alternateUrls
	 */
	public List<String> getAlternateUrls() {
		if (myAlternateUrls == null) {
			return Collections.emptyList();
		}
		return myAlternateUrls;
	}

	/**
	 * @return the connectTimeoutMillis
	 */
	public int getConnectTimeoutMillis() {
		return myConnectTimeoutMillis;
	}

	/**
	 * @return the failureRetriesBeforeAborting
	 */
	public int getFailureRetriesBeforeAborting() {
		return myFailureRetriesBeforeAborting;
	}

	/**
	 * @return the preferredUrl
	 */
	public String getPreferredUrl() {
		return myPreferredUrl;
	}

	/**
	 * @return the readTimeoutMillis
	 */
	public int getReadTimeoutMillis() {
		return myReadTimeoutMillis;
	}

	/**
	 * @param theAlternateUrls
	 *            the alternateUrls to set
	 */
	public void setAlternateUrls(List<String> theAlternateUrls) {
		myAlternateUrls = theAlternateUrls;
	}

	/**
	 * @param theAlternateUrls
	 *            the alternateUrls to set
	 */
	public void setAlternateUrls(String... theAlternateUrls) {
		myAlternateUrls = Arrays.asList(theAlternateUrls);
	}

	
	/**
	 * @param theConnectTimeoutMillis
	 *            the connectTimeoutMillis to set
	 */
	public void setConnectTimeoutMillis(int theConnectTimeoutMillis) {
		myConnectTimeoutMillis = theConnectTimeoutMillis;
	}

	public void setFailureRetriesBeforeAborting(int theFailureRetriesBeforeAborting) {
		myFailureRetriesBeforeAborting = theFailureRetriesBeforeAborting;
	}

	/**
	 * @param thePreferredUrl
	 *            the preferredUrl to set
	 */
	public void setPreferredUrl(String thePreferredUrl) {
		myPreferredUrl = thePreferredUrl;
	}

	/**
	 * @param theReadTimeoutMillis
	 *            the readTimeoutMillis to set
	 */
	public void setReadTimeoutMillis(int theReadTimeoutMillis) {
		myReadTimeoutMillis = theReadTimeoutMillis;
	}

}
