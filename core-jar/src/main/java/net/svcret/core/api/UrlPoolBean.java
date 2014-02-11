package net.svcret.core.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.svcret.core.model.entity.PersServiceVersionUrl;

public class UrlPoolBean {

	private List<PersServiceVersionUrl> myAlternateUrls;
	private PersServiceVersionUrl myPreferredUrl;

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
	 * @return the preferredUrl
	 */
	public PersServiceVersionUrl getPreferredUrl() {
		assert myAlternateUrls == null || !myAlternateUrls.contains(myPreferredUrl);
		return myPreferredUrl;
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
	 * @param thePreferredUrl
	 *            the preferredUrl to set
	 */
	public void setPreferredUrl(PersServiceVersionUrl thePreferredUrl) {
		myPreferredUrl = thePreferredUrl;
	}


}
