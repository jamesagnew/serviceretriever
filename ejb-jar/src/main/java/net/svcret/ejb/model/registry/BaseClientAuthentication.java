package net.svcret.ejb.model.registry;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(namespace = "urn:sail:proxy:registry", name = "BaseClientAuthentication", propOrder= {"myUsername", "myPassword"})
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class BaseClientAuthentication {

	@XmlElement(name = "password")
	private String myPassword;

	@XmlElement(name = "username")
	private String myUsername;

	/**
	 * @return the password
	 */
	public String getPassword() {
		return myPassword;
	}

	/**
	 * @return the username
	 */
	public String getUsername() {
		return myUsername;
	}

	/**
	 * @param thePassword
	 *            the password to set
	 */
	public void setPassword(String thePassword) {
		myPassword = thePassword;
	}

	/**
	 * @param theUsername
	 *            the username to set
	 */
	public void setUsername(String theUsername) {
		myUsername = theUsername;
	}

}
