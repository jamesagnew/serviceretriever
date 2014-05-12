package net.svcret.core.log;

import net.svcret.admin.api.ProcessingException;
import net.svcret.admin.api.UnexpectedFailureException;
import net.svcret.admin.shared.enm.AuthorizationOutcomeEnum;
import net.svcret.core.api.SrBeanIncomingRequest;
import net.svcret.core.api.SrBeanIncomingResponse;
import net.svcret.core.api.SrBeanProcessedRequest;
import net.svcret.core.api.SrBeanProcessedResponse;
import net.svcret.core.model.entity.PersUser;

public interface ITransactionLogger {

	void flush();

	void logTransaction(SrBeanIncomingRequest theRequest, PersUser theUser, SrBeanProcessedResponse theInvocationResponse, SrBeanIncomingResponse theHttpResponse, AuthorizationOutcomeEnum theAuthorizationOutcome, SrBeanProcessedRequest theInvocationResults)
			throws ProcessingException, UnexpectedFailureException;

}
