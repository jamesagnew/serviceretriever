package net.svcret.ejb.api;

import javax.ejb.Local;

import net.svcret.admin.shared.model.AuthorizationOutcomeEnum;
import net.svcret.ejb.ex.ProcessingException;
import net.svcret.ejb.model.entity.PersServiceVersionMethod;
import net.svcret.ejb.model.entity.PersServiceVersionUrl;
import net.svcret.ejb.model.entity.PersUser;

@Local
public interface IFilesystemAuditLogger {

	void recordServiceTransaction(HttpRequestBean theRequest, PersServiceVersionMethod theMethod, PersUser theUser, String theRequestBody, InvocationResponseResultsBean theInvocationResponse,
			PersServiceVersionUrl theImplementationUrl, HttpResponseBean theHttpResponse, AuthorizationOutcomeEnum theAuthorizationOutcome) throws ProcessingException;

	void recordUserTransaction(HttpRequestBean theRequest, PersServiceVersionMethod theMethod, PersUser theUser, String theRequestBody, InvocationResponseResultsBean theInvocationResponse,
			PersServiceVersionUrl theImplementationUrl, HttpResponseBean theHttpResponse, AuthorizationOutcomeEnum theAuthorizationOutcome) throws ProcessingException;

	void flushAuditEventsIfNeeded();

}
