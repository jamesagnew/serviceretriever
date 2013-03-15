package ca.uhn.sail.proxy.ex;

public class UnknownRequestException extends Exception {

	private String myPath;

	public UnknownRequestException(String thePath) {
		this(thePath, null);
	}

	public UnknownRequestException(String thePath, String theMessage) {
		super(theMessage);
		myPath = thePath;
	}

	/**
	 * @return the path
	 */
	public String getPath() {
		return myPath;
	}

	private static final long serialVersionUID = 1L;

}
