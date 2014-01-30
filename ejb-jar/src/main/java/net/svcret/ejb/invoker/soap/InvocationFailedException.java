package net.svcret.ejb.invoker.soap;

import net.svcret.admin.shared.enm.AuthorizationOutcomeEnum;
import net.svcret.admin.shared.enm.ResponseTypeEnum;
import net.svcret.ejb.api.SrBeanIncomingResponse;
import net.svcret.ejb.api.SrBeanProcessedResponse;
import net.svcret.ejb.model.entity.PersUser;

import org.apache.commons.lang3.StringUtils;

public abstract class InvocationFailedException extends Exception {

	private static final long serialVersionUID = 1L;

	private PersUser myUser;
	private SrBeanProcessedResponse myInvocationResponse;
	private SrBeanIncomingResponse myHttpResponse;
	private AuthorizationOutcomeEnum myAuthorizationOutcome;

	public InvocationFailedException(Throwable theCause, PersUser theUser) {
		this(theCause);

		myUser = theUser;
	}

	public InvocationFailedException(String theMessage) {
		super(theMessage);
		assert StringUtils.isNotBlank(theMessage);
		myUser = null;
	}

	public InvocationFailedException(Throwable theCause, String theMessage, SrBeanIncomingResponse theResponse) {
		super(theMessage, theCause);
		myHttpResponse = theResponse;
	}

	public InvocationFailedException(Throwable theCause, String theMessage) {
		super(theMessage, theCause);
	}

	public InvocationFailedException(Throwable theCause) {
		super(toMessage(theCause), theCause);
		assert theCause != null;
	}

	public InvocationFailedException(String theMessage, SrBeanIncomingResponse theHttpResponse) {
		super(theMessage);
		myHttpResponse = theHttpResponse;
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

	public SrBeanProcessedResponse getInvocationResponse() {
		return myInvocationResponse;
	}

	public SrBeanIncomingResponse getHttpResponse() {
		return myHttpResponse;
	}

	public AuthorizationOutcomeEnum getAuthorizationOutcome() {
		return myAuthorizationOutcome;
	}

	public SrBeanProcessedResponse toInvocationResponse() {
		SrBeanProcessedResponse retVal = new SrBeanProcessedResponse();
		retVal.setResponseFailureDescription(getMessage());
		retVal.setResponseType(ResponseTypeEnum.FAIL);
		if (getHttpResponse() != null) {
			retVal.setResponseHeaders(getHttpResponse().getHeaders());
		}
		return retVal;
	}

}
