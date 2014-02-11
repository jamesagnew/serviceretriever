package net.svcret.core.log;

import net.svcret.admin.shared.enm.AuthorizationOutcomeEnum;
import net.svcret.admin.shared.util.Validate;
import net.svcret.core.api.SrBeanIncomingRequest;
import net.svcret.core.api.SrBeanIncomingResponse;
import net.svcret.core.api.SrBeanProcessedRequest;
import net.svcret.core.api.SrBeanProcessedResponse;
import net.svcret.core.model.entity.BasePersServiceVersion;
import net.svcret.core.model.entity.PersConfig;
import net.svcret.core.model.entity.PersMethod;
import net.svcret.core.model.entity.PersServiceVersionRecentMessage;
import net.svcret.core.model.entity.PersUser;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class UnflushedServiceVersionRecentMessages extends BaseUnflushed<PersServiceVersionRecentMessage> {
	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(UnflushedServiceVersionRecentMessages.class);

	private BasePersServiceVersion myServiceVersion;

	public UnflushedServiceVersionRecentMessages(BasePersServiceVersion theServiceVersion) {
		myServiceVersion = theServiceVersion;
	}

	public synchronized void recordTransaction(PersConfig theConfig, PersUser theUser, SrBeanProcessedResponse theInvocationResponse, SrBeanIncomingRequest theRequest, SrBeanIncomingResponse theHttpResponse,
			AuthorizationOutcomeEnum theAuthorizationOutcome, SrBeanProcessedRequest theProcessedRequest) {
		Validate.notNull(theInvocationResponse);
		Validate.notNull(theRequest.getRequestTime());

		initIfNeeded();

		BasePersServiceVersion svcVer = theProcessedRequest.getServiceVersion(); 
		PersMethod method=theProcessedRequest.getMethodDefinition(); 
		Integer keepRecent = svcVer.determineKeepNumRecentTransactions(theInvocationResponse.getResponseType());

		ourLog.debug("Keeping {} recent SvcVer transactions for response type {}", keepRecent, theInvocationResponse.getResponseType());

		if (keepRecent != null && keepRecent > 0) {

			PersServiceVersionRecentMessage message = new PersServiceVersionRecentMessage();
			message.populate(theConfig, theRequest, theInvocationResponse, theProcessedRequest, theHttpResponse);
			message.setServiceVersion(svcVer);
			message.setMethod(method);
			message.setUser(theUser);
			message.setAuthorizationOutcome(theAuthorizationOutcome);

			long responseTime = theHttpResponse != null ? theHttpResponse.getResponseTime() : 0;
			message.setTransactionMillis(responseTime);

			switch (theInvocationResponse.getResponseType()) {
			case FAIL:
				getFail().add(message);
				TransactionLoggerBean.trimOldest(getFail(), keepRecent);
				break;
			case FAULT:
				getFault().add(message);
				TransactionLoggerBean.trimOldest(getFault(), keepRecent);
				break;
			case SECURITY_FAIL:
				getSecurityFail().add(message);
				TransactionLoggerBean.trimOldest(getSecurityFail(), keepRecent);
				break;
			case SUCCESS:
				getSuccess().add(message);
				TransactionLoggerBean.trimOldest(getSuccess(), keepRecent);
				break;
			case THROTTLE_REJ:
				throw new UnsupportedOperationException();
			}

		}

	}

	@Override
	public String toString() {
		ToStringBuilder b = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
		b.append("svcVer", myServiceVersion.getPid());
		b.append("fail", getFail().size());
		b.append("secfail", getSecurityFail().size());
		b.append("fault", getFault().size());
		b.append("success", getSuccess().size());
		return b.build();
	}

}