package net.svcret.ejb.ejb.log;

import java.util.Date;

import net.svcret.admin.shared.enm.AuthorizationOutcomeEnum;
import net.svcret.ejb.api.HttpRequestBean;
import net.svcret.ejb.api.HttpResponseBean;
import net.svcret.ejb.api.InvocationResponseResultsBean;
import net.svcret.ejb.ejb.TransactionLoggerBean;
import net.svcret.ejb.model.entity.BasePersServiceVersion;
import net.svcret.ejb.model.entity.PersConfig;
import net.svcret.ejb.model.entity.PersServiceVersionMethod;
import net.svcret.ejb.model.entity.PersServiceVersionUrl;
import net.svcret.ejb.model.entity.PersUser;
import net.svcret.ejb.model.entity.PersUserRecentMessage;
import net.svcret.ejb.util.Validate;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class UnflushedUserRecentMessages extends BaseUnflushed<PersUserRecentMessage> {
	private PersUser myUser;

	public UnflushedUserRecentMessages(PersUser theUser) {
		myUser = theUser;
	}
private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(UnflushedUserRecentMessages.class);
	public synchronized void recordTransaction(PersConfig theConfig, Date theTransactionTime, HttpRequestBean theRequest, BasePersServiceVersion theSvcVer, PersServiceVersionMethod theMethod, PersUser theUser, String theRequestBody,
			InvocationResponseResultsBean theInvocationResponse, PersServiceVersionUrl theImplementationUrl, HttpResponseBean theHttpResponse, AuthorizationOutcomeEnum theAuthorizationOutcome,
			String theResponseBody) {
		Validate.notNull(theInvocationResponse);

		initIfNeeded();

		Integer keepNum = theUser.determineInheritedKeepNumRecentTransactions(theInvocationResponse.getResponseType());

		ourLog.debug("Keeping {} recent User transactions for response type {}", keepNum, theInvocationResponse.getResponseType());

		if (keepNum != null && keepNum > 0) {

			PersUserRecentMessage userMessage = new PersUserRecentMessage();
			userMessage.populate(theConfig, theTransactionTime, theRequest, theImplementationUrl, theRequestBody, theInvocationResponse, theResponseBody);
			userMessage.setUser(theUser);
			userMessage.setServiceVersion(theSvcVer);
			userMessage.setMethod(theMethod);
			userMessage.setTransactionTime(theTransactionTime);
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