package net.svcret.ejb.log;

import javax.ejb.Local;

import net.svcret.admin.shared.enm.AuthorizationOutcomeEnum;
import net.svcret.ejb.api.SrBeanIncomingRequest;
import net.svcret.ejb.api.SrBeanIncomingResponse;
import net.svcret.ejb.api.SrBeanProcessedRequest;
import net.svcret.ejb.api.SrBeanProcessedResponse;
import net.svcret.ejb.ex.ProcessingException;
import net.svcret.ejb.ex.UnexpectedFailureException;
import net.svcret.ejb.model.entity.BasePersServiceVersion;
import net.svcret.ejb.model.entity.PersMethod;
import net.svcret.ejb.model.entity.PersUser;

@Local
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


}
