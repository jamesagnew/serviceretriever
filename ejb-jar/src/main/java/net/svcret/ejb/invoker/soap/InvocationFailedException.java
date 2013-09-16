package net.svcret.ejb.invoker.soap;

import net.svcret.admin.shared.enm.ResponseTypeEnum;
import net.svcret.admin.shared.model.AuthorizationOutcomeEnum;
import net.svcret.ejb.api.HttpResponseBean;
import net.svcret.ejb.api.InvocationResponseResultsBean;
import net.svcret.ejb.model.entity.PersServiceVersionUrl;
import net.svcret.ejb.model.entity.PersUser;

import org.apache.commons.lang3.StringUtils;

public abstract class InvocationFailedException extends Exception {

	private static final long serialVersionUID = 1L;

	private PersUser myUser;
	private InvocationResponseResultsBean myInvocationResponse;
	private PersServiceVersionUrl myImplementationUrl;
	private HttpResponseBean myHttpResponse;
	private AuthorizationOutcomeEnum myAuthorizationOutcome;

	public InvocationFailedException(Throwable theCause, PersUser theUser) {
		this(theCause);

		myUser = theUser;
	}

	public InvocationFailedException(String theMessage) {
		super(theMessage);
		assert StringUtils.isNotBlank(theMessage);
		myUser=null;
	}

	public InvocationFailedException(Throwable theCause, String theMessage, HttpResponseBean theResponse) {
		super(theMessage, theCause);
		myHttpResponse= theResponse;
	}

	public InvocationFailedException(Throwable theCause, String theMessage) {
		super(theMessage,theCause);
	}

	public InvocationFailedException(Throwable theCause) {
		super(toMessage(theCause), theCause);
		assert theCause != null;
	}

	private static String toMessage(Throwable theCause) {
		if (theCause.getMessage() != null) {
			return theCause.getMessage();
		}
		return theCause.toString();
	}

	public PersUser getUser() {
		return myUser;
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

	public InvocationResponseResultsBean toInvocationResponse() {
		InvocationResponseResultsBean retVal = new InvocationResponseResultsBean();
		retVal.setResponseFailureDescription(getMessage());
		retVal.setResponseType(ResponseTypeEnum.FAIL);
		return retVal;
	}

}
