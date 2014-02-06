package net.svcret.ejb.api;

import net.svcret.admin.api.ProcessingException;


public interface ICredentialGrabber {

	String getUsername();
	
	String getPassword();
	
	String getCredentialHash() throws ProcessingException;
	
}
