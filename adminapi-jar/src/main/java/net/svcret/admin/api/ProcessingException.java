package net.svcret.admin.api;

public class ProcessingException extends Exception {

	private static final long serialVersionUID = 2269344625307830130L;

	public ProcessingException() {
		super();
	}

	public ProcessingException(String theMessage, Throwable theCause) {
		super(theMessage, theCause);
	}

	public ProcessingException(String theMessage) {
		super(theMessage);
	}

	public ProcessingException(Throwable theCause) {
		super(theCause);
	}

}
