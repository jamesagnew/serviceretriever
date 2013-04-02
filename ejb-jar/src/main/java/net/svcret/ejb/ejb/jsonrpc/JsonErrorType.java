package net.svcret.ejb.ejb.jsonrpc;

public class JsonErrorType {

	private int code;
	private String message;


	/**
	 * @return the code
	 */
	public int getCode() {
		return code;
	}


	/**
	 * @param theCode
	 *            the code to set
	 */
	public void setCode(int theCode) {
		code = theCode;
	}


	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}


	/**
	 * @param theMessage
	 *            the message to set
	 */
	public void setMessage(String theMessage) {
		message = theMessage;
	}
	
}
