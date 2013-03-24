package net.svcret.admin.shared.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.google.gwt.user.client.rpc.IsSerializable;

public class ModelUpdateRequest implements Serializable, IsSerializable {

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append("ModelUpdateRequest[");
		
		if (myLoadHttpClientConfigs) {
			b.append("LoadHttpConfigs");
		} else {
			b.append("NoHttpConfigs");
		}
		
		if (myDomainsToLoadStats != null) {
			b.append(", Domains").append(myDomainsToLoadStats.toString());
		}
		if (myServicesToLoadStats != null) {
			b.append(", Services").append(myServicesToLoadStats.toString());
		}
		if (myVersionsToLoadStats != null) {
			b.append(", Versions").append(myVersionsToLoadStats.toString());
		}
		
		b.append("]");
		return b.toString();
	}

	private static final long serialVersionUID = 1L;

	private Set<Long> myDomainsToLoadStats;
	private boolean myLoadHttpClientConfigs;
	private Set<Long> myServicesToLoadStats;
	private Set<Long> myVersionsToLoadStats;

	public void addDomainToLoadStats(long theDomainToLoadStats) {
		if (myDomainsToLoadStats == null) {
			myDomainsToLoadStats = new HashSet<Long>();
		}
		myDomainsToLoadStats.add(theDomainToLoadStats);
	}

	public void addServiceToLoadStats(long theServiceToLoadStats) {
		if (myServicesToLoadStats == null) {
			myServicesToLoadStats = new HashSet<Long>();
		}
		myServicesToLoadStats.add(theServiceToLoadStats);
	}

	public void addVersionToLoadStats(long thePid) {
		if (myVersionsToLoadStats == null) {
			myVersionsToLoadStats = new HashSet<Long>();
		}
		myVersionsToLoadStats.add(thePid);
	}

	public Set<Long> getDomainsToLoadStats() {
		Set<Long> retVal;
		if (myDomainsToLoadStats != null) {
			retVal = myDomainsToLoadStats;
		} else {
			retVal = Collections.emptySet();
		}
		return retVal;
	}

	public Set<Long> getServicesToLoadStats() {
		Set<Long> retVal;
		if (myServicesToLoadStats != null) {
			retVal = myServicesToLoadStats;
		} else {
			retVal = Collections.emptySet();
		}
		return retVal;
	}

	public Set<Long> getVersionsToLoadStats() {
		Set<Long> retVal;
		if (myVersionsToLoadStats != null) {
			retVal = myVersionsToLoadStats;
		} else {
			retVal = Collections.emptySet();
		}
		return retVal;
	}

	public boolean isLoadHttpClientConfigs() {
		return myLoadHttpClientConfigs;
	}

	/**
	 * @param theLoadHttpClientConfigs
	 *            the loadHttpClientConfigs to set
	 */
	public void setLoadHttpClientConfigs(boolean theLoadHttpClientConfigs) {
		myLoadHttpClientConfigs = theLoadHttpClientConfigs;
	}
}
