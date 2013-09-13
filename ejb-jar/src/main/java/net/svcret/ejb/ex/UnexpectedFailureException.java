package net.svcret.ejb.ex;

public class UnexpectedFailureException extends Exception {

	private static final long serialVersionUID = 1L;

	public UnexpectedFailureException(Throwable theCause) {
		super(theCause);
	}

	public UnexpectedFailureException(String theMessage) {
		super(theMessage);
	}

}
