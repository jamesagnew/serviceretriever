package net.svcret.admin.shared.model;

import java.io.Serializable;

public class GMonitorRuleProblem implements Serializable {
	private static final long serialVersionUID = 1L;

	private String myFailedUrlHref;
	private String myFailedUrlId;
	private String myFailedUrlMessage;
	private Long myLatencyAverageMillisPerCall;
	private Long myLatencyAverageOverMinutes;
	private Boolean myLatencyExceededThreshold;

	/**
	 * @return the failedUrlHref
	 */
	public String getFailedUrlHref() {
		return myFailedUrlHref;
	}

	/**
	 * @return the failedUrlId
	 */
	public String getFailedUrlId() {
		return myFailedUrlId;
	}

	/**
	 * @return the failedUrlMessage
	 */
	public String getFailedUrlMessage() {
		return myFailedUrlMessage;
	}

	/**
	 * @return the latencyAverageMillisPerCall
	 */
	public Long getLatencyAverageMillisPerCall() {
		return myLatencyAverageMillisPerCall;
	}

	/**
	 * @return the latencyAverageOverMinutes
	 */
	public Long getLatencyAverageOverMinutes() {
		return myLatencyAverageOverMinutes;
	}

	/**
	 * @return the latencyExceededThreshold
	 */
	public Boolean getLatencyExceededThreshold() {
		return myLatencyExceededThreshold;
	}

	/**
	 * @param theFailedUrlHref
	 *            the failedUrlHref to set
	 */
	public void setFailedUrlHref(String theFailedUrlHref) {
		myFailedUrlHref = theFailedUrlHref;
	}

	/**
	 * @param theFailedUrlId
	 *            the failedUrlId to set
	 */
	public void setFailedUrlId(String theFailedUrlId) {
		myFailedUrlId = theFailedUrlId;
	}

	/**
	 * @param theFailedUrlMessage
	 *            the failedUrlMessage to set
	 */
	public void setFailedUrlMessage(String theFailedUrlMessage) {
		myFailedUrlMessage = theFailedUrlMessage;
	}

	/**
	 * @param theLatencyAverageMillisPerCall
	 *            the latencyAverageMillisPerCall to set
	 */
	public void setLatencyAverageMillisPerCall(Long theLatencyAverageMillisPerCall) {
		myLatencyAverageMillisPerCall = theLatencyAverageMillisPerCall;
	}

	/**
	 * @param theLatencyAverageOverMinutes
	 *            the latencyAverageOverMinutes to set
	 */
	public void setLatencyAverageOverMinutes(Long theLatencyAverageOverMinutes) {
		myLatencyAverageOverMinutes = theLatencyAverageOverMinutes;
	}

	/**
	 * @param theLatencyExceededThreshold
	 *            the latencyExceededThreshold to set
	 */
	public void setLatencyExceededThreshold(Boolean theLatencyExceededThreshold) {
		myLatencyExceededThreshold = theLatencyExceededThreshold;
	}

}
