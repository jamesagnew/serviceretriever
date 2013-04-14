package net.svcret.ejb.ex;

import java.util.List;

public class UnknownRequestException extends Exception {

	private String myPath;
	private List<String> myValidPaths;

	public UnknownRequestException(String thePath) {
		this(thePath, (String)null);
	}

	public UnknownRequestException(String thePath, List<String> theValidPaths) {
		myPath = thePath;
		myValidPaths = theValidPaths;
	}

	public UnknownRequestException(String thePath, String theMessage) {
		super(theMessage);
		myPath=thePath;
	}

	/**
	 * @return the validPaths
	 */
	public List<String> getValidPaths() {
		return myValidPaths;
	}

	/**
	 * @return the path
	 */
	public String getPath() {
		return myPath;
	}

	private static final long serialVersionUID = 1L;

}
