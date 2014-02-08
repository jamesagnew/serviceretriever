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
import net.svcret.core.model.entity.PersUser;
import net.svcret.core.model.entity.PersUserRecentMessage;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class UnflushedUserRecentMessages extends BaseUnflushed<PersUserRecentMessage> {
	private PersUser myUser;

	public UnflushedUserRecentMessages(PersUser theUser) {
		myUser = theUser;
	}
private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(UnflushedUserRecentMessages.class);
	public synchronized void recordTransaction(PersConfig theConfig, SrBeanIncomingRequest theRequest, PersUser theUser, SrBeanProcessedResponse theInvocationResponse, SrBeanIncomingResponse theHttpResponse, AuthorizationOutcomeEnum theAuthorizationOutcome,
			SrBeanProcessedRequest theProcessedRequest) {
		Validate.notNull(theInvocationResponse);

		initIfNeeded();

		Integer keepNum = theUser.determineInheritedKeepNumRecentTransactions(theInvocationResponse.getResponseType());

		ourLog.debug("Keeping {} recent User transactions for response type {}", keepNum, theInvocationResponse.getResponseType());

		if (keepNum != null && keepNum > 0) {

			BasePersServiceVersion svcVer = theProcessedRequest.getServiceVersion(); 
			PersMethod method=theProcessedRequest.getMethodDefinition(); 

			PersUserRecentMessage userMessage = new PersUserRecentMessage();
			userMessage.populate(theConfig, theRequest, theInvocationResponse, theProcessedRequest, theHttpResponse);
			userMessage.setUser(theUser);
			userMessage.setServiceVersion(svcVer);
			userMessage.setMethod(method);
			userMessage.setAuthorizationOutcome(theAuthorizationOutcome);
			
			long responseTime = theHttpResponse != null ? theHttpResponse.getResponseTime() : 0;
			userMessage.setTransactionMillis(responseTime);

			switch (theInvocationResponse.getResponseType()) {
			case FAIL:
				getFail().add(userMessage);
				TransactionLoggerBean.trimOldest(getFail(), keepNum);
				break;
			case FAULT:
				getFault().add(userMessage);
				TransactionLoggerBean.trimOldest(getFault(), keepNum);
				break;
			case SECURITY_FAIL:
				getSecurityFail().add(userMessage);
				TransactionLoggerBean.trimOldest(getSecurityFail(), keepNum);
				break;
			case SUCCESS:
				getSuccess().add(userMessage);
				TransactionLoggerBean.trimOldest(getSuccess(), keepNum);
				break;
			case THROTTLE_REJ:
				throw new UnsupportedOperationException();
			}

		}
	}

	@Override
	public String toString() {
		ToStringBuilder b = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
		b.append("user", myUser.getPid());
		b.append("fail", getFail().size());
		b.append("secfail", getSecurityFail().size());
		b.append("fault", getFault().size());
		b.append("success", getSuccess().size());
		return b.build();
	}

}