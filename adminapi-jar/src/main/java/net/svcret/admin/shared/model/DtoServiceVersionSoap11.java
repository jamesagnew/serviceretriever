package net.svcret.admin.shared.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import net.svcret.admin.shared.util.XmlConstants;


@XmlType(namespace=XmlConstants.DTO_NAMESPACE, name="ServiceVersionSoap11")
@XmlRootElement(namespace=XmlConstants.DTO_NAMESPACE, name="ServiceVersionSoap11")
@XmlAccessorType(XmlAccessType.FIELD)
public class DtoServiceVersionSoap11 extends BaseDtoServiceVersion {

	private static final long serialVersionUID = 1L;

	@XmlElement(name="WsdlLocation")
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
