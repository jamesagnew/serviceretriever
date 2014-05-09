package net.svcret.core.ex;

import net.svcret.admin.shared.enm.AuthorizationOutcomeEnum;

public class SecurityFailureException extends Exception {

	private AuthorizationOutcomeEnum myAuthorizationOutcomeEnum;
	private String myRequestNewAuthorizationWithDomain;

	/**
	 * @param theRequestNewAuthorizationWithDomain
	 *            If not set to null, authorization failure will be returned to the client as an HTTP 401 Unauthorized instead of the default HTTP 403 Forbidden, which means that browsers should
	 *            prompt for authentication
	 */
	public SecurityFailureException(AuthorizationOutcomeEnum theAuthorizationOutcomeEnum, String theResponseStatusMessage, String theRequestNewAuthorizationWithDomain) {
		super(theResponseStatusMessage);
		myAuthorizationOutcomeEnum = theAuthorizationOutcomeEnum;
		myRequestNewAuthorizationWithDomain = theRequestNewAuthorizationWithDomain;
	}

	public String getRequestNewAuthorizationWithDomain() {
		return myRequestNewAuthorizationWithDomain;
	}

	/**
	 * @param theRequestNewAuthorizationWithDomain
	 *            If not set to null, authorization failure will be returned to the client as an HTTP 401 Unauthorized instead of the default HTTP 403 Forbidden, which means that browsers should
	 *            prompt for authentication
	 */
	public AuthorizationOutcomeEnum getAuthorizationOutcomeEnum() {
		return myAuthorizationOutcomeEnum;
	}

	private static final long serialVersionUID = 1L;

}
