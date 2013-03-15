package ca.uhn.sail.proxy.ex;

public class ProcessingRuntimeException extends RuntimeException {

	private static final long serialVersionUID = 8066812358346078805L;

	public ProcessingRuntimeException(String theMessage, Throwable theCause) {
		super(theMessage, theCause);
	}

}
