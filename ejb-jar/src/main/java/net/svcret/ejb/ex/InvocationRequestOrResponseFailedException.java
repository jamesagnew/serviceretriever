package net.svcret.ejb.ex;

import net.svcret.ejb.api.HttpResponseBean;
import net.svcret.ejb.invoker.soap.InvocationFailedException;
import net.svcret.ejb.model.entity.PersUser;

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

	public InvocationRequestOrResponseFailedException(Throwable theCause, String theMessage, HttpResponseBean theResponse) {
		super(theCause, theMessage, theResponse);
	}

	public InvocationRequestOrResponseFailedException(String theMessage, HttpResponseBean theHttpResponse) {
		super(theMessage, theHttpResponse);
	}

}
