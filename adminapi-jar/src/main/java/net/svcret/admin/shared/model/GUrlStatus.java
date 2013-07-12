package net.svcret.admin.shared.model;

import java.io.Serializable;
import java.util.Date;

public class GUrlStatus implements Serializable {

	private static final long serialVersionUID = 1L;

	private Date myLastFailure;
	private String myLastFailureMessage;
	private Date myLastFault;
	private String myLastFaultMessage;
	private Date myLastSuccess;
	private String myLastSuccessMessage;
	private StatusEnum myStatus;
	private String myUrl;
	private long myUrlPid;

	/**
	 * @return the lastFailure
	 */
	public Date getLastFailure() {
		return myLastFailure;
	}

	/**
	 * @return the lastFailureMessage
	 */
	public String getLastFailureMessage() {
		return myLastFailureMessage;
	}

	/**
	 * @return the lastFault
	 */
	public Date getLastFault() {
		return myLastFault;
	}

	/**
	 * @return the lastFaultMessage
	 */
	public String getLastFaultMessage() {
		return myLastFaultMessage;
	}

	/**
	 * @return the lastSuccess
	 */
	public Date getLastSuccess() {
		return myLastSuccess;
	}

	/**
	 * @return the lastSuccessMessage
	 */
	public String getLastSuccessMessage() {
		return myLastSuccessMessage;
	}

	/**
	 * @return the status
	 */
	public StatusEnum getStatus() {
		if (myStatus == null) {
			return StatusEnum.UNKNOWN;
		}
		return myStatus;
	}

//	/**
//	 * @return the url
//	 */
//	public String getUrl() {
//		return myUrl;
//	}

	/**
	 * @return the urlPid
	 */
	public long getUrlPid() {
		return myUrlPid;
	}

	/**
	 * @param theLastFailure
	 *            the lastFailure to set
	 */
	public void setLastFailure(Date theLastFailure) {
		myLastFailure = theLastFailure;
	}

	/**
	 * @param theLastFailureMessage
	 *            the lastFailureMessage to set
	 */
	public void setLastFailureMessage(String theLastFailureMessage) {
		myLastFailureMessage = theLastFailureMessage;
	}

	/**
	 * @param theLastFault
	 *            the lastFault to set
	 */
	public void setLastFault(Date theLastFault) {
		myLastFault = theLastFault;
	}

	/**
	 * @param theLastFaultMessage
	 *            the lastFaultMessage to set
	 */
	public void setLastFaultMessage(String theLastFaultMessage) {
		myLastFaultMessage = theLastFaultMessage;
	}

	/**
	 * @param theLastSuccess
	 *            the lastSuccess to set
	 */
	public void setLastSuccess(Date theLastSuccess) {
		myLastSuccess = theLastSuccess;
	}

	/**
	 * @param theLastSuccessMessage
	 *            the lastSuccessMessage to set
	 */
	public void setLastSuccessMessage(String theLastSuccessMessage) {
		myLastSuccessMessage = theLastSuccessMessage;
	}

	/**
	 * @param theStatus
	 *            the status to set
	 */
	public void setStatus(StatusEnum theStatus) {
		myStatus = theStatus;
	}

//	/**
//	 * @param theUrl
//	 *            the url to set
//	 */
//	public void setUrl(String theUrl) {
//		myUrl = theUrl;
//	}

	/**
	 * @param theUrlPid
	 *            the urlPid to set
	 */
	public void setUrlPid(long theUrlPid) {
		myUrlPid = theUrlPid;
	}

}
