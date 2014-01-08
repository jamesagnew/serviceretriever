package net.svcret.ejb.ex;


public class InvalidRequestException extends Exception {

	private IssueEnum myIssue;
	private Object myArgument;

	private static final long serialVersionUID = 1L;

	public InvalidRequestException(IssueEnum theIssue, Object theArgument, String theMessage) {
		super(theMessage);
		myIssue = theIssue;
		myArgument = theArgument;
	}

	public Object getArgument() {
		return myArgument;
	}

	public IssueEnum getIssue() {
		return myIssue;
	}

	public enum IssueEnum {
		UNSUPPORTED_ACTION, 
		INVALID_QUERY_PARAMETERS, 
		UNKNOWN_METHOD, 
		INVALID_REQUEST_MESSAGE_BODY, 
		INVALID_REQUEST_PATH, 
		INVALID_REQUEST_CONTENT_TYPE
	}

}
