package net.svcret.admin.shared.model;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class GServiceVersionDetailedStats implements Serializable {

	private static final long serialVersionUID = 1L;

	private Map<Long, int[]> myMethodPidToFailCount;
	private Map<Long, int[]> myMethodPidToFaultCount;
	private Map<Long, int[]> myMethodPidToSecurityFailCount;
	private Map<Long, int[]> myMethodPidToSuccessCount;
	private List<Long> myStatsTimestamps;

	/**
	 * @return the methodPidToFailCount
	 */
	public Map<Long, int[]> getMethodPidToFailCount() {
		return myMethodPidToFailCount;
	}

	/**
	 * @return the methodPidToFaultCount
	 */
	public Map<Long, int[]> getMethodPidToFaultCount() {
		return myMethodPidToFaultCount;
	}

	/**
	 * @return the methodPidToSecurityFailCount
	 */
	public Map<Long, int[]> getMethodPidToSecurityFailCount() {
		return myMethodPidToSecurityFailCount;
	}

	/**
	 * @return the methodPidToSuccessCount
	 */
	public Map<Long, int[]> getMethodPidToSuccessCount() {
		return myMethodPidToSuccessCount;
	}

	/**
	 * @return the statsTimestamps
	 */
	public List<Long> getStatsTimestamps() {
		return myStatsTimestamps;
	}

	public void setMethodPidToFailCount(Map<Long, int[]> theMethodPidToFailCount) {
		myMethodPidToFailCount = theMethodPidToFailCount;
	}

	public void setMethodPidToFaultCount(Map<Long, int[]> theMethodPidToFaultCount) {
		myMethodPidToFaultCount = theMethodPidToFaultCount;
	}

	public void setMethodPidToSecurityFailCount(Map<Long, int[]> theMethodPidToSecurityFailCount) {
		myMethodPidToSecurityFailCount = theMethodPidToSecurityFailCount;

	}

	public void setMethodPidToSuccessCount(Map<Long, int[]> theMethodPidToSuccessCount) {
		myMethodPidToSuccessCount = theMethodPidToSuccessCount;
	}

	public void setStatsTimestamps(List<Long> theStatsTimestamps) {
		myStatsTimestamps = theStatsTimestamps;
	}

}
