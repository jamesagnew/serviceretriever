package net.svcret.admin.shared;

import java.io.Serializable;

import net.svcret.admin.shared.model.BaseDtoServiceVersion;
import net.svcret.admin.shared.model.GDomain;

public class AddServiceVersionResponse implements Serializable {

	private static final long serialVersionUID = 1L;

	private GDomain myNewDomain;
	private BaseDtoServiceVersion myNewServiceVersion;

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
	 * @return the newServiceVersion
	 */
	public BaseDtoServiceVersion getNewServiceVersion() {
		return myNewServiceVersion;
	}

	/**
	 * @param theNewServiceVersion the newServiceVersion to set
	 */
	public void setNewServiceVersion(BaseDtoServiceVersion theNewServiceVersion) {
		myNewServiceVersion = theNewServiceVersion;
	}
}
