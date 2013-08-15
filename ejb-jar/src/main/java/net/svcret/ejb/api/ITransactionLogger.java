package net.svcret.ejb.api;

import javax.ejb.Local;

import net.svcret.admin.shared.model.AuthorizationOutcomeEnum;
import net.svcret.ejb.model.entity.BasePersServiceVersion;
import net.svcret.ejb.model.entity.PersServiceVersionUrl;
import net.svcret.ejb.model.entity.PersUser;

@Local
public interface ITransactionLogger {

	void flush();

	void logTransaction(HttpRequestBean theRequest, BasePersServiceVersion theServiceVersion, PersUser theAuthorizedUser, String theRequestBody, InvocationResponseResultsBean theInvocationResponse, PersServiceVersionUrl theImplementationUrl,
			HttpResponseBean theHttpResponse, AuthorizationOutcomeEnum theAuthorizationOutcome);

}
