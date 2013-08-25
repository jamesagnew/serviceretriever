package net.svcret.admin.shared.model;

import java.io.Serializable;

public class DtoKeystoreToSave implements Serializable {

	private static final long serialVersionUID = 1L;

	private byte[] myKeystore;
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
