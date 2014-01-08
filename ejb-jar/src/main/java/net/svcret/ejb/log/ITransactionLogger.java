package net.svcret.ejb.log;

import javax.ejb.Local;

import net.svcret.admin.shared.enm.AuthorizationOutcomeEnum;
import net.svcret.ejb.api.SrBeanIncomingRequest;
import net.svcret.ejb.api.SrBeanIncomingResponse;
import net.svcret.ejb.api.SrBeanProcessedResponse;
import net.svcret.ejb.api.SrBeanProcessedRequest;
import net.svcret.ejb.ex.ProcessingException;
import net.svcret.ejb.ex.UnexpectedFailureException;
import net.svcret.ejb.model.entity.BasePersServiceVersion;
import net.svcret.ejb.model.entity.PersMethod;
import net.svcret.ejb.model.entity.PersServiceVersionUrl;
import net.svcret.ejb.model.entity.PersUser;

@Local
public interface ITransactionLogger {

	void flush();

	void logTransaction(SrBeanIncomingRequest theRequest, BasePersServiceVersion theSvcVer, PersMethod theMethod, PersUser theUser, String theRequestBody,
			SrBeanProcessedResponse theInvocationResponse, PersServiceVersionUrl theImplementationUrl, SrBeanIncomingResponse theHttpResponse, AuthorizationOutcomeEnum theAuthorizationOutcome,
			String theResponseBody, SrBeanProcessedRequest theInvocationResults) throws ProcessingException, UnexpectedFailureException;

}
