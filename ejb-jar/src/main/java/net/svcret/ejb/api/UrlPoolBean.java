package net.svcret.ejb.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.svcret.ejb.model.entity.PersServiceVersionUrl;

public class UrlPoolBean {

	private List<PersServiceVersionUrl> myAlternateUrls;
	private int myConnectTimeoutMillis;
	private int myFailureRetriesBeforeAborting;
	private PersServiceVersionUrl myPreferredUrl;
	private int myReadTimeoutMillis;

	public UrlPoolBean() {
		
	}
	
	public Collection<PersServiceVersionUrl> getAllUrls() {
		ArrayList<PersServiceVersionUrl> retVal = new ArrayList<PersServiceVersionUrl>();
		retVal.add(getPreferredUrl());
		retVal.addAll(getAlternateUrls());
		return retVal;
	}

	/**
	 * @return the alternateUrls
	 */
	public List<PersServiceVersionUrl> getAlternateUrls() {
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
	public PersServiceVersionUrl getPreferredUrl() {
		assert myAlternateUrls == null || !myAlternateUrls.contains(myPreferredUrl);
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
	public void setAlternateUrls(List<PersServiceVersionUrl> theAlternateUrls) {
		myAlternateUrls = theAlternateUrls;
	}

	/**
	 * @param theAlternateUrls
	 *            the alternateUrls to set
	 */
	public void setAlternateUrls(PersServiceVersionUrl... theAlternateUrls) {
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
	public void setPreferredUrl(PersServiceVersionUrl thePreferredUrl) {
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
