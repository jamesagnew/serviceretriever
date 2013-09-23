package net.svcret.admin.shared.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ModelUpdateResponse implements Serializable {

	private static final long serialVersionUID = 1L;

	private GAuthenticationHostList myAuthenticationHostList;
	private GDomainList myDomainList;
	private GHttpClientConfigList myHttpClientConfigList;
	private List<DtoNodeStatus> myNodeStatuses;
	private GUserList myUserList;

	/**
	 * @return the authenticationHostList
	 */
	public GAuthenticationHostList getAuthenticationHostList() {
		return myAuthenticationHostList;
	}

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

	public List<DtoNodeStatus> getNodeStatuses() {
		if (myNodeStatuses == null) {
			myNodeStatuses = new ArrayList<DtoNodeStatus>();
		}
		return myNodeStatuses;
	}

	/**
	 * @return the userList
	 */
	public GUserList getUserList() {
		return myUserList;
	}

	public void setAuthenticationHostList(GAuthenticationHostList theAuthenticationHostList) {
		myAuthenticationHostList = theAuthenticationHostList;
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

	public void setUserList(GUserList theUserList) {
		myUserList = theUserList;
	}

}
