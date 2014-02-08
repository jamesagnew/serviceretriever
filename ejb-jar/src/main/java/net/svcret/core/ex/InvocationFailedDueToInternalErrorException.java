package net.svcret.core.ex;

import net.svcret.core.invoker.soap.InvocationFailedException;

public class InvocationFailedDueToInternalErrorException extends InvocationFailedException {

	public InvocationFailedDueToInternalErrorException(Throwable theCause) {
		super(theCause);
	}

	public InvocationFailedDueToInternalErrorException(String theMessage) {
		super(theMessage);
	}

	public InvocationFailedDueToInternalErrorException(Throwable theCause, String theMessage) {
		super(theCause, theMessage);
	}

	private static final long serialVersionUID = 1L;

}
