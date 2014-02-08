package net.svcret.core.ex;

import net.svcret.core.api.SrBeanIncomingResponse;
import net.svcret.core.invoker.soap.InvocationFailedException;
import net.svcret.core.model.entity.PersUser;

public abstract class InvocationRequestOrResponseFailedException extends InvocationFailedException {

	private static final long serialVersionUID = 1L;

	public InvocationRequestOrResponseFailedException(String theMessage) {
		super(theMessage);
	}

	public InvocationRequestOrResponseFailedException(Throwable theCause) {
		super(theCause, (PersUser) null);
	}

	public InvocationRequestOrResponseFailedException(Throwable theCause, PersUser theUser) {
		super(theCause, theUser);
	}

	public InvocationRequestOrResponseFailedException(Throwable theCause, String theMessage) {
		super(theCause, theMessage);
	}

	public InvocationRequestOrResponseFailedException(Throwable theCause, String theMessage, SrBeanIncomingResponse theResponse) {
		super(theCause, theMessage, theResponse);
	}

	public InvocationRequestOrResponseFailedException(String theMessage, SrBeanIncomingResponse theHttpResponse) {
		super(theMessage, theHttpResponse);
	}

}
