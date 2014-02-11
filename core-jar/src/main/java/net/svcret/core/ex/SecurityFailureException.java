package net.svcret.core.ex;

import net.svcret.admin.shared.enm.AuthorizationOutcomeEnum;

public class SecurityFailureException extends Exception {

	private AuthorizationOutcomeEnum myAuthorizationOutcomeEnum;

	public SecurityFailureException(AuthorizationOutcomeEnum theAuthorizationOutcomeEnum, String theResponseStatusMessage) {
		super(theResponseStatusMessage);
		myAuthorizationOutcomeEnum = theAuthorizationOutcomeEnum;
	}

	public AuthorizationOutcomeEnum getAuthorizationOutcomeEnum() {
		return myAuthorizationOutcomeEnum;
	}

	private static final long serialVersionUID = 1L;

}
