package net.svcret.admin.shared.model;

import java.io.Serializable;

public class AddServiceVersionResponse implements Serializable {

	private static final long serialVersionUID = 1L;

	private GDomain myNewDomain;
	
	private GService myNewService;
	
	private BaseGServiceVersion myNewServiceVersion;

	/**
	 * @return the newDomain
	 */
	public GDomain getNewDomain() {
		return myNewDomain;
	}

	/**
	 * @param theNewDomain the newDomain to set
	 */
	public void setNewDomain(GDomain theNewDomain) {
		myNewDomain = theNewDomain;
	}

	/**
	 * @return the newService
	 */
	public GService getNewService() {
		return myNewService;
	}

	/**
	 * @param theNewService the newService to set
	 */
	public void setNewService(GService theNewService) {
		myNewService = theNewService;
	}

	/**
	 * @return the newServiceVersion
	 */
	public BaseGServiceVersion getNewServiceVersion() {
		return myNewServiceVersion;
	}

	/**
	 * @param theNewServiceVersion the newServiceVersion to set
	 */
	public void setNewServiceVersion(BaseGServiceVersion theNewServiceVersion) {
		myNewServiceVersion = theNewServiceVersion;
	}
}
