package net.svcret.admin.shared.model;


public enum AuthorizationOutcomeEnum {

	AUTHORIZED,
	
	FAILED_INTERNAL_ERROR,

	FAILED_BAD_CREDENTIALS_IN_REQUEST,

	FAILED_USER_NO_PERMISSIONS, 
	
	FAILED_IP_NOT_IN_WHITELIST

}
