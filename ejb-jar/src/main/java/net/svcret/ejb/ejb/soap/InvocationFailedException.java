package net.svcret.ejb.ejb.soap;

import net.svcret.admin.shared.model.AuthorizationOutcomeEnum;
import net.svcret.ejb.api.HttpResponseBean;
import net.svcret.ejb.api.InvocationResponseResultsBean;
import net.svcret.ejb.model.entity.PersServiceVersionUrl;
import net.svcret.ejb.model.entity.PersUser;

public class InvocationFailedException extends Exception {

	private static final long serialVersionUID = 1L;
	
	private PersUser myUser;
	private String myRequestBody;
	private InvocationResponseResultsBean myInvocationResponse;
	private PersServiceVersionUrl myImplementationUrl;
	private HttpResponseBean myHttpResponse;
	private AuthorizationOutcomeEnum myAuthorizationOutcome;

	public InvocationFailedException(Exception theCause, PersUser theUser, String theRequestBody, InvocationResponseResultsBean theInvocationResponse) {
		super(theCause);
		myUser = theUser;
		myRequestBody = theRequestBody;
		myInvocationResponse = theInvocationResponse;
	}

	public PersUser getUser() {
		return myUser;
	}

	public String getRequestBody() {
		return myRequestBody;
	}

	public InvocationResponseResultsBean getInvocationResponse() {
		return myInvocationResponse;
	}

	public PersServiceVersionUrl getImplementationUrl() {
		return myImplementationUrl;
	}

	public HttpResponseBean getHttpResponse() {
		return myHttpResponse;
	}

	public AuthorizationOutcomeEnum getAuthorizationOutcome() {
		return myAuthorizationOutcome;
	}

}
