package net.svcret.ejb.ex;

public class InternalErrorException extends RuntimeException {

	private static final long serialVersionUID = 2269344625307830130L;

	public InternalErrorException() {
		super();
	}

	public InternalErrorException(String theMessage, Throwable theCause) {
		super(theMessage, theCause);
	}

	public InternalErrorException(String theMessage) {
		super(theMessage);
	}

	public InternalErrorException(Throwable theCause) {
		super(theCause);
	}

}
