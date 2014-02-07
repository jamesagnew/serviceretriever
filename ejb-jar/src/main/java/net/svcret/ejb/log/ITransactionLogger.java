package net.svcret.ejb.log;

import net.svcret.admin.api.ProcessingException;
import net.svcret.admin.api.UnexpectedFailureException;
import net.svcret.admin.shared.enm.AuthorizationOutcomeEnum;
import net.svcret.ejb.api.SrBeanIncomingRequest;
import net.svcret.ejb.api.SrBeanIncomingResponse;
import net.svcret.ejb.api.SrBeanProcessedRequest;
import net.svcret.ejb.api.SrBeanProcessedResponse;
import net.svcret.ejb.model.entity.PersUser;

public interface ITransactionLogger {

	void flush();

	void logTransaction(SrBeanIncomingRequest theRequest, PersUser theUser, SrBeanProcessedResponse theInvocationResponse, SrBeanIncomingResponse theHttpResponse, AuthorizationOutcomeEnum theAuthorizationOutcome, SrBeanProcessedRequest theInvocationResults)
			throws ProcessingException, UnexpectedFailureException;

}
