package net.svcret.admin.shared.model;

import java.util.Date;

public abstract class BaseGDashboardObject extends BaseGKeepsRecentMessages {

	private static final long serialVersionUID = 1L;
	private int myAverageLatency60min;
	private double myAverageTransactionsPerMin60min;
	private boolean myExpandedOnDashboard;
	private String myId;
	private Date myLastServerSecurityFailure;
	private Date myLastSuccessfulInvocation;
	private int[] myLatency60mins;
	private String myName;
	private Date myStatsInitialized;
	private StatusEnum myStatus;
	private int[] myTransactions60mins;
	private int myMaxLatency60min;
	private double myMaxTransactionsPerMin60min;

	/**
	 * @return the averageLatency
	 */
	public int getAverageLatency60min() {
		return myAverageLatency60min;
	}

	/**
	 * @return the averageLatency
	 */
	public int getMaxLatency60min() {
		return myMaxLatency60min;
	}

	/**
	 * @return the averageTransactionsPerMin
	 */
	public double getAverageTransactionsPerMin60min() {
		return myAverageTransactionsPerMin60min;
	}

	/**
	 * @return the averageTransactionsPerMin
	 */
	public double getMaxTransactionsPerMin60min() {
		return myMaxTransactionsPerMin60min;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return myId;
	}

	/**
	 * @return the lastServerSecurityFailure
	 */
	public Date getLastServerSecurityFailure() {
		return myLastServerSecurityFailure;
	}

	/**
	 * @return the lastSuccessfulInvocation
	 */
	public Date getLastSuccessfulInvocation() {
		return myLastSuccessfulInvocation;
	}

	/**
	 * @return the latency60mins
	 */
	public int[] getLatency60mins() {
		return myLatency60mins;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return myName;
	}

	/**
	 * @return the status
	 */
	public StatusEnum getStatus() {
		return myStatus;
	}

	/**
	 * @return the transactions60mins
	 */
	public int[] getTransactions60mins() {
		return myTransactions60mins;
	}

	/**
	 * Do we need this? Maybe make it configurable
	 */
	public boolean hideDashboardRowWhenExpanded() {
		return true;
	}

	/**
	 * @return the expandedOnDashboard
	 */
	public boolean isExpandedOnDashboard() {
		return myExpandedOnDashboard;
	}

	/**
	 * @return the statsInitialized
	 */
	public boolean isStatsInitialized() {
		return myStatsInitialized!=null;
	}

	/**
	 * @return the statsInitialized
	 */
	public Date getStatsInitialized() {
		return myStatsInitialized;
	}

	/**
	 * @param theExpandedOnDashboard
	 *            the expandedOnDashboard to set
	 */
	public void setExpandedOnDashboard(boolean theExpandedOnDashboard) {
		myExpandedOnDashboard = theExpandedOnDashboard;
	}

	/**
	 * @param theId
	 *            the id to set
	 */
	public void setId(String theId) {
		myId = theId;
	}
	
	
	/**
	 * @param theLastServerSecurityFailure
	 *            the lastServerSecurityFailure to set
	 */
	public void setLastServerSecurityFailure(Date theLastServerSecurityFailure) {
		myLastServerSecurityFailure = theLastServerSecurityFailure;
	}

	/**
	 * @param theLastSuccessfulInvocation
	 *            the lastSuccessfulInvocation to set
	 */
	public void setLastSuccessfulInvocation(Date theLastSuccessfulInvocation) {
		myLastSuccessfulInvocation = theLastSuccessfulInvocation;
	}

	/**
	 * @param theLatency60mins
	 *            the latency60mins to set
	 */
	public void setLatency60mins(int[] theLatency60mins) {
		myLatency60mins = theLatency60mins;

		int count = 0;
		int total = 0;
		int max = 0;
		for (int i : theLatency60mins) {
			if (i > 0) {
				total++;
				count += i;
			}
			max = Math.max(max, i);
		}
		if (total > 0) {
			myAverageLatency60min = count / total;
		}
		myMaxLatency60min = max;
	}

	/**
	 * @param theName
	 *            the name to set
	 */
	public void setName(String theName) {
		myName = theName;
	}

	/**
	 * @param theStatsInitialized
	 *            the statsInitialized to set
	 */
	public void setStatsInitialized(Date theStatsInitialized) {
		myStatsInitialized = theStatsInitialized;
	}

	/**
	 * @param theStatus
	 *            the status to set
	 */
	public void setStatus(StatusEnum theStatus) {
		if (theStatus == null) {
			throw new NullPointerException("Status can not be null");
		}
		myStatus = theStatus;
	}

	/**
	 * @param theTransactions60mins
	 *            the transactions60mins to set
	 */
	public void setTransactions60mins(int[] theTransactions60mins) {
		myTransactions60mins = theTransactions60mins;
		long total = 0;
		long max = 0;
		for (int i : theTransactions60mins) {
			total += i;
			max = Math.max(max, i);
		}
		if (myTransactions60mins.length > 0) {
			myAverageTransactionsPerMin60min = ((double)total / (double)myTransactions60mins.length);
			myMaxTransactionsPerMin60min = max;
		}
	}

	public void flushStats() {
		myStatsInitialized=null;
	}

	@Override
	public void merge(BaseGObject theObject) {
		super.merge(theObject);
		
		BaseGDashboardObject obj = (BaseGDashboardObject)theObject;
		setId(obj.getId());
		setName(obj.getName());
		
		if (obj.isStatsInitialized()) {
			setStatsInitialized(obj.getStatsInitialized());
			setStatus(obj.getStatus());
			setTransactions60mins(obj.getTransactions60mins());
			setLatency60mins(obj.getLatency60mins());
			setLastServerSecurityFailure(obj.getLastServerSecurityFailure());
			setLastSuccessfulInvocation(obj.getLastSuccessfulInvocation());
		}
	}


}
