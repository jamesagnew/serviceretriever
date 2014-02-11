package net.svcret.core.ex;

import net.svcret.core.model.entity.PersUser;

public class InvocationRequestFailedException extends InvocationRequestOrResponseFailedException {

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
