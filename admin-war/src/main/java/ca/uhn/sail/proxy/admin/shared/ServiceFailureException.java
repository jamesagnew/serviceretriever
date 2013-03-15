package ca.uhn.sail.proxy.admin.shared;

public class ServiceFailureException extends Exception {

	private static final long serialVersionUID = 1L;
	
	public ServiceFailureException() {
	}
	
	public ServiceFailureException(String theMessage) {
		super(theMessage);
	}
	
}
