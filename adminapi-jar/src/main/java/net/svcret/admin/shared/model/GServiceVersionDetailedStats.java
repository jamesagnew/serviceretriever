package net.svcret.admin.shared.model;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class GServiceVersionDetailedStats implements Serializable {

	private static final long serialVersionUID = 1L;

	private Map<Long, List<Integer>> myMethodPidToFailCount;
	private Map<Long, List<Integer>> myMethodPidToFaultCount;
	private Map<Long, List<Integer>> myMethodPidToSecurityFailCount;
	private Map<Long, List<Integer>> myMethodPidToSuccessCount;

	private List<Long> myStatsTimestamps;

	/**
	 * @return the methodPidToFailCount
	 */
	public Map<Long, List<Integer>> getMethodPidToFailCount() {
		return myMethodPidToFailCount;
	}

	/**
	 * @return the methodPidToFaultCount
	 */
	public Map<Long, List<Integer>> getMethodPidToFaultCount() {
		return myMethodPidToFaultCount;
	}

	/**
	 * @return the methodPidToSecurityFailCount
	 */
	public Map<Long, List<Integer>> getMethodPidToSecurityFailCount() {
		return myMethodPidToSecurityFailCount;
	}

	/**
	 * @return the methodPidToSuccessCount
	 */
	public Map<Long, List<Integer>> getMethodPidToSuccessCount() {
		return myMethodPidToSuccessCount;
	}

	/**
	 * @return the statsTimestamps
	 */
	public List<Long> getStatsTimestamps() {
		return myStatsTimestamps;
	}

	public void setMethodPidToFailCount(Map<Long, List<Integer>> theMethodPidToFailCount) {
		myMethodPidToFailCount = theMethodPidToFailCount;
	}

	public void setMethodPidToFaultCount(Map<Long, List<Integer>> theMethodPidToFaultCount) {
		myMethodPidToFaultCount = theMethodPidToFaultCount;
	}

	public void setMethodPidToSecurityFailCount(Map<Long, List<Integer>> theMethodPidToSecurityFailCount) {
		myMethodPidToSecurityFailCount = theMethodPidToSecurityFailCount;

	}

	public void setMethodPidToSuccessCount(Map<Long, List<Integer>> theMethodPidToSuccessCount) {
		myMethodPidToSuccessCount = theMethodPidToSuccessCount;
	}

	public void setStatsTimestamps(List<Long> theStatsTimestamps) {
		myStatsTimestamps = theStatsTimestamps;
	}

}
