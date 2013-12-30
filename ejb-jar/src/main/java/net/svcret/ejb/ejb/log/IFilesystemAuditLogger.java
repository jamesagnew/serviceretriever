package net.svcret.ejb.ejb.log;

import javax.ejb.Local;

import net.svcret.admin.shared.enm.AuthorizationOutcomeEnum;
import net.svcret.ejb.api.SrBeanIncomingRequest;
import net.svcret.ejb.api.SrBeanIncomingResponse;
import net.svcret.ejb.api.InvocationResponseResultsBean;
import net.svcret.ejb.api.InvocationResultsBean;
import net.svcret.ejb.ex.ProcessingException;
import net.svcret.ejb.ex.UnexpectedFailureException;
import net.svcret.ejb.model.entity.BasePersServiceVersion;
import net.svcret.ejb.model.entity.PersServiceVersionMethod;
import net.svcret.ejb.model.entity.PersServiceVersionUrl;
import net.svcret.ejb.model.entity.PersUser;

@Local
public interface IFilesystemAuditLogger {

	void recordServiceTransaction(SrBeanIncomingRequest theRequest, BasePersServiceVersion theSvcVer, PersServiceVersionMethod theMethod, PersUser theUser, String theRequestBody,
			InvocationResponseResultsBean theInvocationResponse, PersServiceVersionUrl theImplementationUrl, SrBeanIncomingResponse theHttpResponse, AuthorizationOutcomeEnum theAuthorizationOutcome, InvocationResultsBean theInvocationResults)
			throws ProcessingException, UnexpectedFailureException;

	void recordUserTransaction(SrBeanIncomingRequest theRequest, BasePersServiceVersion theSvcVer, PersServiceVersionMethod theMethod, PersUser theUser, String theRequestBody,
			InvocationResponseResultsBean theInvocationResponse, PersServiceVersionUrl theImplementationUrl, SrBeanIncomingResponse theHttpResponse, AuthorizationOutcomeEnum theAuthorizationOutcome, InvocationResultsBean theInvocationResults)
			throws ProcessingException, UnexpectedFailureException;

	void flushAuditEventsIfNeeded();


}
