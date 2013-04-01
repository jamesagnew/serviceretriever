package net.svcret.ejb.ejb;

import net.svcret.ejb.model.entity.soap.BaseCredentialGrabber;

class MyCredentialGrabber extends BaseCredentialGrabber
{

	public MyCredentialGrabber(String theUsername, String thePassword) {
		super();
		myUsername = theUsername;
		myPassword = thePassword;
	}

	private String myUsername;
	private String myPassword;

	@Override
	public String getUsername() {
		return myUsername;
	}

	@Override
	public String getPassword() {
		return myPassword;
	}
	
}