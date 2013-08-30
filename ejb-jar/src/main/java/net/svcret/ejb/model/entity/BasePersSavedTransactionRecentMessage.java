package net.svcret.ejb.model.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.MappedSuperclass;

import org.apache.commons.lang3.Validate;

import net.svcret.admin.shared.enm.RecentMessageTypeEnum;
import net.svcret.admin.shared.model.AuthorizationOutcomeEnum;
import net.svcret.ejb.api.HttpRequestBean;
import net.svcret.ejb.api.IDao;
import net.svcret.ejb.api.InvocationResponseResultsBean;

@MappedSuperclass()
public abstract class BasePersSavedTransactionRecentMessage extends BasePersSavedTransaction {

	private static final int MAX_REQ_IP_LEN = 200;
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
	public void populate(Date theTransactionTime, HttpRequestBean theRequest, PersServiceVersionUrl theImplementationUrl, String theRequestBody, InvocationResponseResultsBean theInvocationResult, String theResponseBody) {
		setRequestHostIp(theRequest.getRequestHostIp());
		super.populate(theTransactionTime, theRequest, theImplementationUrl, theRequestBody, theInvocationResult, theResponseBody);
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

	public abstract void trimUsingDao(IDao theDaoBean);

}
