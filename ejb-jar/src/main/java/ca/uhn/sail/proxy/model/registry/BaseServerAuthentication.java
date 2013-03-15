package ca.uhn.sail.proxy.model.registry;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(namespace = "urn:sail:proxy:registry", name = "BaseServerAuthentication", propOrder = { "myModuleId" })
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class BaseServerAuthentication {

	@XmlElement(name = "module_id")
	private String myModuleId;

	public BaseServerAuthentication() {
	}

	public BaseServerAuthentication(String theModuleId) {
		myModuleId = theModuleId;
	}

	/**
	 * @return the moduleId
	 */
	public String getModuleId() {
		return myModuleId;
	}

	/**
	 * @param theModuleId
	 *            the moduleId to set
	 */
	public void setModuleId(String theModuleId) {
		myModuleId = theModuleId;
	}

}
