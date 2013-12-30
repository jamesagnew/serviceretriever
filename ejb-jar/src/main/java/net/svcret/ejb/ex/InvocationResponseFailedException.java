package net.svcret.ejb.ex;

import net.svcret.ejb.api.SrBeanIncomingResponse;

public class InvocationResponseFailedException extends InvocationRequestOrResponseFailedException {

	public InvocationResponseFailedException(Throwable theCause, String theMessage, SrBeanIncomingResponse theHttpResponse) {
		super(theCause, theMessage, theHttpResponse);
	}

	public InvocationResponseFailedException(String theMessage) {
		super(theMessage);
	}

	public InvocationResponseFailedException(String theMessage, SrBeanIncomingResponse theHttpResponse) {
		super(theMessage, theHttpResponse);
	}

	private static final long serialVersionUID = 1L;

}
