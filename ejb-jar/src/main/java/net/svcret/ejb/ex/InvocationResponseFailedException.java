package net.svcret.ejb.ex;

import net.svcret.ejb.api.HttpResponseBean;
import net.svcret.ejb.ejb.soap.InvocationFailedException;

public class InvocationResponseFailedException extends InvocationFailedException {

	public InvocationResponseFailedException(Throwable theCause, String theMessage, HttpResponseBean theResponse) {
		super(theCause, theMessage, theResponse);
	}

	public InvocationResponseFailedException(String theMessage) {
		super(theMessage);
	}

	private static final long serialVersionUID = 1L;

}
