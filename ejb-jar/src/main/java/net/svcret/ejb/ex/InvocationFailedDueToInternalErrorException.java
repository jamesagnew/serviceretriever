package net.svcret.ejb.ex;

import net.svcret.ejb.ejb.soap.InvocationFailedException;

public class InvocationFailedDueToInternalErrorException extends InvocationFailedException {

	public InvocationFailedDueToInternalErrorException(Throwable theCause) {
		super(theCause);
	}

	public InvocationFailedDueToInternalErrorException(String theMessage) {
		super(theMessage);
	}

	private static final long serialVersionUID = 1L;

}
