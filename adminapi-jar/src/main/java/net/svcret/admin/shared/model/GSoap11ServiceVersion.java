package net.svcret.admin.shared.model;


public class GSoap11ServiceVersion extends BaseGServiceVersion {

	private static final long serialVersionUID = 1L;

	private String myWsdlLocation;

	@Override
	public ServiceProtocolEnum getProtocol() {
		return ServiceProtocolEnum.SOAP11;
	}

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
