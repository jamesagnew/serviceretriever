package ca.uhn.sail.proxy.admin.shared.model;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DomainListUpdateRequest implements Serializable, IsSerializable {

	private static final long serialVersionUID = 1L;

	private List<Long> myDomainsToLoad;
	private boolean myLoadAllDomains;
	private List<Long> myServicesToLoad;

	private List<Long> myVersionsToLoad;

	public void addDomainToLoad(long theDomainToLoad) {
		if (myDomainsToLoad == null) {
			myDomainsToLoad = new ArrayList<Long>();
		}
		myDomainsToLoad.add(theDomainToLoad);
	}

	public void addServiceToLoad(long theServiceToLoad) {
		if (myServicesToLoad == null) {
			myServicesToLoad = new ArrayList<Long>();
		}
		myServicesToLoad.add(theServiceToLoad);
	}

	public void addVersionToLoad(long thePid) {
		if (myVersionsToLoad == null) {
			myVersionsToLoad = new ArrayList<Long>();
		}
		myVersionsToLoad.add(thePid);
	}

	public List<Long> getDomainsToLoad() {
		List<Long> retVal;
		if (myDomainsToLoad != null) {
			retVal = myDomainsToLoad;
		} else {
			retVal = Collections.emptyList();
		}
		return retVal;
	}

	public List<Long> getServicesToLoad() {
		List<Long> retVal;
		if (myServicesToLoad != null) {
			retVal = myServicesToLoad;
		} else {
			retVal = Collections.emptyList();
		}
		return retVal;
	}

	public List<Long> getVersionsToLoad() {
		List<Long> retVal;
		if (myVersionsToLoad != null) {
			retVal = myVersionsToLoad;
		} else {
			retVal = Collections.emptyList();
		}
		return retVal;
	}

	public void initDomainsToLoad() {
		if (myDomainsToLoad == null) {
			myDomainsToLoad = new ArrayList<Long>();
		}
	}

	/**
	 * @return the loadAllDomains
	 */
	public boolean isLoadAllDomains() {
		return myLoadAllDomains;
	}

	public void setLoadAllDomains(boolean theLoadAllDomains) {
		myLoadAllDomains = theLoadAllDomains;
	}
}
