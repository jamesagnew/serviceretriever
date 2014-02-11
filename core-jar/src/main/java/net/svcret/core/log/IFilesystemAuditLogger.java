package net.svcret.core.log;

import net.svcret.admin.api.ProcessingException;
import net.svcret.admin.api.UnexpectedFailureException;
import net.svcret.admin.shared.enm.AuthorizationOutcomeEnum;
import net.svcret.core.api.SrBeanIncomingRequest;
import net.svcret.core.api.SrBeanIncomingResponse;
import net.svcret.core.api.SrBeanProcessedRequest;
import net.svcret.core.api.SrBeanProcessedResponse;
import net.svcret.core.model.entity.BasePersServiceVersion;
import net.svcret.core.model.entity.PersMethod;
import net.svcret.core.model.entity.PersUser;

public interface IFilesystemAuditLogger {

	//TODO: these methods shouldn't take SVCVER and METHOD, they can get those from the processed request 
	// they also shouldn't take the raw message, since they can get the obscured request/response from
	// the beans
	
	void recordServiceTransaction(SrBeanIncomingRequest theRequest, BasePersServiceVersion theSvcVer, PersMethod theMethod, PersUser theUser, String theRequestBody,
			SrBeanProcessedResponse theInvocationResponse, SrBeanIncomingResponse theHttpResponse, AuthorizationOutcomeEnum theAuthorizationOutcome, SrBeanProcessedRequest theInvocationResults)
			throws ProcessingException, UnexpectedFailureException;

	void recordUserTransaction(SrBeanIncomingRequest theRequest, BasePersServiceVersion theSvcVer, PersMethod theMethod, PersUser theUser, String theRequestBody,
			SrBeanProcessedResponse theInvocationResponse, SrBeanIncomingResponse theHttpResponse, AuthorizationOutcomeEnum theAuthorizationOutcome, SrBeanProcessedRequest theInvocationResults)
			throws ProcessingException, UnexpectedFailureException;

	void flushAuditEventsIfNeeded();

	void forceFlush() throws ProcessingException, UnexpectedFailureException;


}
