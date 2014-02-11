package net.svcret.core.api;

import net.svcret.admin.api.ProcessingException;


public interface ICredentialGrabber {

	String getUsername();
	
	String getPassword();
	
	String getCredentialHash() throws ProcessingException;
	
}
