package net.svcret.ejb.api;

import net.svcret.ejb.ex.ProcessingException;


public interface ICredentialGrabber {

	String getUsername();
	
	String getPassword();
	
	String getCredentialHash() throws ProcessingException;
	
}
