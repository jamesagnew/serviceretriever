package net.svcret.admin.shared.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import net.svcret.admin.shared.util.XmlConstants;

@XmlType(namespace = XmlConstants.DTO_NAMESPACE, name = "KeystoreToSave")
@XmlAccessorType(XmlAccessType.FIELD)
public class DtoKeystoreToSave implements Serializable {

	private static final long serialVersionUID = 1L;

	@XmlElement(name="config_Keystore")
	private byte[] myKeystore;

	@XmlElement(name="config_Password")
	private String myPassword;

	public byte[] getKeystore() {
		return myKeystore;
	}

	public void setKeystore(byte[] theKeystore) {
		myKeystore = theKeystore;
	}

	public String getPassword() {
		return myPassword;
	}

	public void setPassword(String thePassword) {
		myPassword = thePassword;
	}
	
}
