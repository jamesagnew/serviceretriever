package net.svcret.admin.shared.model;

public class GSoap11ServiceVersion extends BaseGServiceVersion {

	private static final long serialVersionUID = 1L;

	private String myWsdlLocation;

	/**
	 * @return the wsdlLocation
	 */
	public String getWsdlLocation() {
		return myWsdlLocation;
	}

	/**
	 * @param theWsdlLocation
	 *            the wsdlLocation to set
	 */
	public void setWsdlLocation(String theWsdlLocation) {
		myWsdlLocation = theWsdlLocation;
	}


}
