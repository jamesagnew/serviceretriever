package net.svcret.admin.shared.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GSoap11ServiceVersionAndResources implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<GResource> myResource;
	private BaseGServiceVersion myServiceVersion;

	/**
	 * @return the resource
	 */
	public List<GResource> getResource() {
		if (myResource == null) {
			myResource = new ArrayList<GResource>();
		}
		return myResource;
	}

	/**
	 * @return the serviceVersion
	 */
	public BaseGServiceVersion getServiceVersion() {
		return myServiceVersion;
	}

	/**
	 * @param theResource
	 *            the resource to set
	 */
	public void setResource(List<GResource> theResource) {
		myResource = theResource;
	}

	/**
	 * @param theServiceVersion
	 *            the serviceVersion to set
	 */
	public void setServiceVersion(BaseGServiceVersion theServiceVersion) {
		myServiceVersion = theServiceVersion;
	}
}
