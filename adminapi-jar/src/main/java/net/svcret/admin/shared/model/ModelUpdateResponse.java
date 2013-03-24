package net.svcret.admin.shared.model;

import java.io.Serializable;

public class ModelUpdateResponse implements Serializable {

	private static final long serialVersionUID = 1L;

	private GDomainList myDomainList;
	private GHttpClientConfigList myHttpClientConfigList;

	/**
	 * @return the domainList
	 */
	public GDomainList getDomainList() {
		return myDomainList;
	}

	/**
	 * @return the httpClientConfigList
	 */
	public GHttpClientConfigList getHttpClientConfigList() {
		return myHttpClientConfigList;
	}

	/**
	 * @param theDomainList
	 *            the domainList to set
	 */
	public void setDomainList(GDomainList theDomainList) {
		myDomainList = theDomainList;
	}

	/**
	 * @param theHttpClientConfigList
	 *            the httpClientConfigList to set
	 */
	public void setHttpClientConfigList(GHttpClientConfigList theHttpClientConfigList) {
		myHttpClientConfigList = theHttpClientConfigList;
	}
	
}
