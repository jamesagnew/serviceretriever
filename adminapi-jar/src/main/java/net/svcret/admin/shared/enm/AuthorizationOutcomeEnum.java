package net.svcret.admin.shared.enm;


public enum AuthorizationOutcomeEnum {

	AUTHORIZED("Authorized"),
	
	FAILED_INTERNAL_ERROR("Internal Error"),

	FAILED_BAD_CREDENTIALS_IN_REQUEST("Bad credentials (incorrect username or password) found in request"),

	FAILED_USER_NO_PERMISSIONS("User credentials accepted, but user does not have permission to access service"), 
	
	FAILED_IP_NOT_IN_WHITELIST("User credentials accepted, but requesting IP not authorized to access service"), 
	
	FAILED_MISSING_USERNAME("No username found in request"),

	FAILED_MISSING_PASSWORD("No password found in request"), 
	
	FAILED_USER_UNKNOWN_TO_SR("User is unknown to ServiceRetriever");

	private String myDescription;

	private AuthorizationOutcomeEnum(String theDescription) {
		myDescription=theDescription;
	}
	
	public String getDescription() {
		return myDescription;
	}

}
