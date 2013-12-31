package net.svcret.ejb.model.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.MappedSuperclass;

import net.svcret.admin.shared.enm.AuthorizationOutcomeEnum;
import net.svcret.admin.shared.enm.RecentMessageTypeEnum;
import net.svcret.admin.shared.model.GRecentMessage;
import net.svcret.admin.shared.model.Pair;
import net.svcret.ejb.api.SrBeanIncomingRequest;
import net.svcret.ejb.api.IDao;
import net.svcret.ejb.api.SrBeanProcessedResponse;

import org.apache.commons.lang3.Validate;

@MappedSuperclass()
public abstract class BasePersSavedTransactionRecentMessage extends BasePersSavedTransaction {

	static final int MAX_REQ_IP_LEN = 200;
	private static final long serialVersionUID = 1L;

	@Column(name = "AUTHN_OUTCOME")
	@Enumerated(EnumType.STRING)
	private AuthorizationOutcomeEnum myAuthorizationOutcome;

	@Column(name = "REQ_HOST_IP", nullable = false, length = MAX_REQ_IP_LEN)
	private String myRequestHostIp;

	public abstract void addUsingDao(IDao theDaoBean);

	/**
	 * @return the authorizationOutcome
	 */
	public AuthorizationOutcomeEnum getAuthorizationOutcome() {
		return myAuthorizationOutcome;
	}

	public abstract PersServiceVersionMethod getMethod();

	public abstract RecentMessageTypeEnum getRecentMessageType();

	/**
	 * @return the requestHostIp
	 */
	public String getRequestHostIp() {
		return myRequestHostIp;
	}

	public abstract BasePersServiceVersion getServiceVersion();

	@Override
	public void populate(PersConfig theConfig, Date theTransactionTime, SrBeanIncomingRequest theRequest, PersServiceVersionUrl theImplementationUrl, String theRequestBody, SrBeanProcessedResponse theInvocationResult, String theResponseBody) {
		setRequestHostIp(theRequest.getRequestHostIp());
		super.populate(theConfig, theTransactionTime, theRequest, theImplementationUrl, theRequestBody, theInvocationResult, theResponseBody);
	}

	/**
	 * @param theAuthorizationOutcome
	 *            the authorizationOutcome to set
	 */
	public void setAuthorizationOutcome(AuthorizationOutcomeEnum theAuthorizationOutcome) {
		myAuthorizationOutcome = theAuthorizationOutcome;
	}

	/**
	 * @param theRequestHostIp
	 *            the requestHostIp to set
	 */
	public void setRequestHostIp(String theRequestHostIp) {
		Validate.notNull(theRequestHostIp);

		if (theRequestHostIp.length() > MAX_REQ_IP_LEN) {
			myRequestHostIp = theRequestHostIp.substring(0, MAX_REQ_IP_LEN);
		} else {
			myRequestHostIp = theRequestHostIp;
		}
	}

	public GRecentMessage toDto(boolean theLoadMessageContents) {
		GRecentMessage retVal = new GRecentMessage();

		retVal.setPid(this.getPid());
		PersServiceVersionUrl implementationUrl = this.getImplementationUrl();
		if (implementationUrl != null) {
			retVal.setImplementationUrlId(implementationUrl.getUrlId());
			retVal.setImplementationUrlHref(implementationUrl.getUrl());
			retVal.setImplementationUrlPid(implementationUrl.getPid());
		}

		BasePersServiceVersion svcVer = this.getServiceVersion();
		if (svcVer != null) {
			retVal.setDomainPid(svcVer.getService().getDomain().getPid());
			retVal.setDomainName(svcVer.getService().getDomain().getDomainNameOrId());

			retVal.setServicePid(svcVer.getService().getPid());
			retVal.setServiceName(svcVer.getService().getServiceNameOrId());

			retVal.setServiceVersionPid(svcVer.getPid());
			retVal.setServiceVersionId(svcVer.getVersionId());
		}

		PersServiceVersionMethod method = this.getMethod();
		if (method != null) {
			retVal.setMethodPid(method.getPid());
			retVal.setMethodName(method.getName());
		}

		retVal.setRecentMessageType(this.getRecentMessageType());
		retVal.setRequestHostIp(this.getRequestHostIp());
		retVal.setTransactionTime(this.getTransactionTime());
		retVal.setTransactionMillis(this.getTransactionMillis());
		retVal.setAuthorizationOutcome(this.getAuthorizationOutcome());
		retVal.setFailDescription(this.getFailDescription());
		retVal.setResponseType(this.getResponseType());

		if (theLoadMessageContents) {
			int bodyIdx = this.getRequestBody().indexOf("\r\n\r\n");
			if (bodyIdx == -1) {
				retVal.setRequestMessage(this.getRequestBody());
				retVal.setRequestHeaders(new ArrayList<Pair<String>>());
				retVal.setRequestContentType("unknown");
			} else {
				retVal.setRequestMessage(this.getRequestBody().substring(bodyIdx + 4));
				retVal.setRequestActionLine(toActionLine(this.getRequestBody()));
				retVal.setRequestHeaders(toHeaders(this.getRequestBody().substring(0, bodyIdx)));
				retVal.setRequestContentType(toHeaderContentType(retVal.getRequestHeaders()));
			}

			bodyIdx = this.getResponseBody().indexOf("\r\n\r\n");
			if (bodyIdx == -1) {
				retVal.setResponseMessage(this.getResponseBody());
				retVal.setResponseHeaders(new ArrayList<Pair<String>>());
				retVal.setResponseContentType("unknown");
			} else {
				retVal.setResponseMessage(this.getResponseBody().substring(bodyIdx + 4));
				retVal.setResponseHeaders(toHeaders(this.getResponseBody().substring(0, bodyIdx)));
				retVal.setResponseContentType(toHeaderContentType(retVal.getResponseHeaders()));
			}

		}

		if (this instanceof PersServiceVersionRecentMessage) {
			PersServiceVersionRecentMessage msg = (PersServiceVersionRecentMessage) this;
			if (msg.getUser() != null) {
				retVal.setRequestUserPid(msg.getUser().getPid());
				retVal.setRequestUsername(msg.getUser().getUsername());
			}
		} else if (this instanceof PersUserRecentMessage) {
			PersUserRecentMessage msg = (PersUserRecentMessage) this;
			if (msg.getUser() != null) {
				retVal.setRequestUserPid(msg.getUser().getPid());
				retVal.setRequestUsername(msg.getUser().getUsername());
			}
		}

		return retVal;
	}

	private static String toActionLine(String theRequestBody) {
		int idx = theRequestBody.indexOf("\r\n");
		if (idx == -1) {
			return null;
		}

		String firstLine = theRequestBody.substring(0, idx);
		idx = firstLine.indexOf(": ");
		if (idx == -1) {
			// If the first line has no colon, it's the action line
			return firstLine;
		} else {
			return null;
		}
	}

	public abstract long trimUsingDao(IDao theDaoBean);

	private static String toHeaderContentType(List<Pair<String>> theResponseHeaders) {
		for (Pair<String> pair : theResponseHeaders) {
			if (pair.getFirst().equalsIgnoreCase("content-type")) {
				return pair.getSecond().split(";")[0].trim();
			}
		}
		return null;
	}

	private static List<Pair<String>> toHeaders(String theHeaders) {
		ArrayList<Pair<String>> retVal = new ArrayList<Pair<String>>();
		int index = 0;
		for (String next : theHeaders.split("\\r\\n")) {
			int colonIndex = next.indexOf(": ");
			if (index == 0 && colonIndex == -1) {
				// First line is generally the action line for request messages
				continue;
			}
			retVal.add(new Pair<String>(next.substring(0, colonIndex), next.substring(colonIndex + 2)));
			index++;
		}
		return retVal;
	}

}
