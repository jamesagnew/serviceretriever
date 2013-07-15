package net.svcret.admin.shared.model;

import java.io.Serializable;

public class GMonitorRuleAppliesTo implements Serializable {

	private static final long serialVersionUID = 1L;

	private String myDomainName;
	private long myDomainPid;
	private String myServiceName;
	private Long myServicePid;
	private Long myServiceVersionPid;
	private String myVersionId;

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof GMonitorRuleAppliesTo)) {
			return false;
		}
		GMonitorRuleAppliesTo other = (GMonitorRuleAppliesTo) obj;
		if (myDomainPid != other.myDomainPid) {
			return false;
		}
		if (myServicePid == null) {
			if (other.myServicePid != null) {
				return false;
			}
		} else if (!myServicePid.equals(other.myServicePid)) {
			return false;
		}
		if (myServiceVersionPid == null) {
			if (other.myServiceVersionPid != null) {
				return false;
			}
		} else if (!myServiceVersionPid.equals(other.myServiceVersionPid)) {
			return false;
		}
		return true;
	}

	/**
	 * @return the domainName
	 */
	public String getDomainName() {
		return myDomainName;
	}

	/**
	 * @return the domainPid
	 */
	public long getDomainPid() {
		return myDomainPid;
	}

	/**
	 * @return the serviceName
	 */
	public String getServiceName() {
		return myServiceName;
	}

	/**
	 * @return the servicePid
	 */
	public Long getServicePid() {
		return myServicePid;
	}

	/**
	 * @return the serviceVersionPid
	 */
	public Long getServiceVersionPid() {
		return myServiceVersionPid;
	}

	/**
	 * @return the versionId
	 */
	public String getVersionId() {
		return myVersionId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (myDomainPid ^ (myDomainPid >>> 32));
		result = prime * result + ((myServicePid == null) ? 0 : myServicePid.hashCode());
		result = prime * result + ((myServiceVersionPid == null) ? 0 : myServiceVersionPid.hashCode());
		return result;
	}

	/**
	 * @param theDomainName
	 *            the domainName to set
	 */
	public void setDomainName(String theDomainName) {
		myDomainName = theDomainName;
	}

	/**
	 * @param theDomainPid
	 *            the domainPid to set
	 */
	public void setDomainPid(long theDomainPid) {
		myDomainPid = theDomainPid;
	}

	/**
	 * @param theServiceName
	 *            the serviceName to set
	 */
	public void setServiceName(String theServiceName) {
		myServiceName = theServiceName;
	}

	/**
	 * @param theServicePid
	 *            the servicePid to set
	 */
	public void setServicePid(Long theServicePid) {
		myServicePid = theServicePid;
	}

	/**
	 * @param theServiceVersionPid
	 *            the serviceVersionPid to set
	 */
	public void setServiceVersionPid(Long theServiceVersionPid) {
		myServiceVersionPid = theServiceVersionPid;
	}

	/**
	 * @param theVersionId
	 *            the versionId to set
	 */
	public void setVersionId(String theVersionId) {
		myVersionId = theVersionId;
	}

}
