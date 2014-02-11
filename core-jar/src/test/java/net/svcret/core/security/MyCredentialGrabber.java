package net.svcret.core.security;

import net.svcret.core.model.entity.soap.BaseCredentialGrabber;

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