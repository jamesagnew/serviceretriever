package net.svcret.admin.shared.model;


public enum AuthorizationOutcomeEnum {

	AUTHORIZED("Authorized"),
	
	FAILED_INTERNAL_ERROR("Internal Error"),

	FAILED_BAD_CREDENTIALS_IN_REQUEST("Bad or missing credentials"),

	FAILED_USER_NO_PERMISSIONS("User does not have permission to access service"), 
	
	FAILED_IP_NOT_IN_WHITELIST("Requesting IP not authorized to access service"), 
	
	FAILED_MISSING_USERNAME("No username found in request"),

	FAILED_MISSING_PASSWORD("No password found in request"), 
	
	FAILED_USER_UNKNOWN_TO_SR("User credentials succeeded but were unknown to ServiceRetriever");

	private String myDescription;

	private AuthorizationOutcomeEnum(String theDescription) {
		myDescription=theDescription;
	}
	
	public String getDescription() {
		return myDescription;
	}

}
