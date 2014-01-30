package net.svcret.ejb.model.entity;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.MappedSuperclass;

import net.svcret.admin.shared.enm.AuthorizationOutcomeEnum;
import net.svcret.admin.shared.enm.RecentMessageTypeEnum;
import net.svcret.admin.shared.model.GRecentMessage;
import net.svcret.ejb.api.IDao;
import net.svcret.ejb.api.SrBeanIncomingRequest;
import net.svcret.ejb.api.SrBeanIncomingResponse;
import net.svcret.ejb.api.SrBeanProcessedRequest;
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

	public abstract PersMethod getMethod();

	public abstract RecentMessageTypeEnum getRecentMessageType();

	/**
	 * @return the requestHostIp
	 */
	public String getRequestHostIp() {
		return myRequestHostIp;
	}

	@Override
	public void populate(PersConfig theConfig, SrBeanIncomingRequest theRequest, SrBeanProcessedResponse theInvocationResult, SrBeanProcessedRequest theProcessedRequest, SrBeanIncomingResponse theIncomingResponse) {
		setRequestHostIp(theRequest.getRequestHostIp());
		super.populate(theConfig, theRequest, theInvocationResult, theProcessedRequest, theIncomingResponse);
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

		super.populateDto(retVal, theLoadMessageContents);

		BasePersServiceVersion svcVer = this.getServiceVersion();
		if (svcVer != null) {
			retVal.setDomainPid(svcVer.getService().getDomain().getPid());
			retVal.setDomainName(svcVer.getService().getDomain().getDomainNameOrId());

			retVal.setServicePid(svcVer.getService().getPid());
			retVal.setServiceName(svcVer.getService().getServiceNameOrId());

			retVal.setServiceVersionPid(svcVer.getPid());
			retVal.setServiceVersionId(svcVer.getVersionId());
		}

		PersMethod method = this.getMethod();
		if (method != null) {
			retVal.setMethodPid(method.getPid());
			retVal.setMethodName(method.getName());
		}

		retVal.setRecentMessageType(this.getRecentMessageType());
		retVal.setRequestHostIp(this.getRequestHostIp());
		retVal.setAuthorizationOutcome(this.getAuthorizationOutcome());

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

	public abstract long trimUsingDao(IDao theDaoBean);

}
