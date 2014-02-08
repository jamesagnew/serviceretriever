package net.svcret.core.ex;


public class HttpFailureException extends Exception {

	private static final long serialVersionUID = 1L;

	public HttpFailureException(Exception theCause) {
		super(theCause);
	}

}
