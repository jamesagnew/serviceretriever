package net.svcret.ejb.ex;

import net.svcret.ejb.ejb.soap.InvocationFailedException;
import net.svcret.ejb.model.entity.PersUser;

public class InvocationRequestFailedException extends InvocationFailedException {

	public InvocationRequestFailedException(Throwable theCause, PersUser theUser) {
		super(theCause, theUser);
	}
	
	public InvocationRequestFailedException(Throwable theCause) {
		super(theCause, (PersUser)null);
	}

	public InvocationRequestFailedException(Throwable theCause,String theMessage) {
		super(theCause, theMessage);
	}

	public InvocationRequestFailedException(String theMessage) {
		super(theMessage);
	}

	private static final long serialVersionUID = 1L;

}
