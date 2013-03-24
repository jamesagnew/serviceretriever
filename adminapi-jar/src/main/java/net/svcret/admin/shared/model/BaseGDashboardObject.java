package net.svcret.admin.shared.model;

public abstract class BaseGDashboardObject<T> extends BaseGObject<T> {

	private static final long serialVersionUID = 1L;
	private boolean myExpandedOnDashboard;
	private String myId;
	private int[] myLatency60mins;
	private String myName;
	private boolean myStatsInitialized;
	private StatusEnum myStatus;
	private int[] myTransactions60mins;

	/**
	 * @return the id
	 */
	public String getId() {
		return myId;
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
	 * @return the expandedOnDashboard
	 */
	public boolean isExpandedOnDashboard() {
		return myExpandedOnDashboard;
	}

	/**
	 * @return the statsInitialized
	 */
	public boolean isStatsInitialized() {
		return myStatsInitialized;
	}

	protected void merge(BaseGDashboardObject<T> theObject) {
		setPid(theObject.getPid());
		setId(theObject.getId());
		setName(theObject.getName());

		if (theObject.isStatsInitialized()) {
			setStatsInitialized(true);
			setStatus(theObject.getStatus());
			setTransactions60mins(theObject.getTransactions60mins());
			setLatency60mins(theObject.getLatency60mins());
		}
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
	 * @param theLatency60mins
	 *            the latency60mins to set
	 */
	public void setLatency60mins(int[] theLatency60mins) {
		myLatency60mins = theLatency60mins;
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
	public void setStatsInitialized(boolean theStatsInitialized) {
		myStatsInitialized = theStatsInitialized;
	}

	/**
	 * @param theStatus
	 *            the status to set
	 */
	public void setStatus(StatusEnum theStatus) {
		myStatus = theStatus;
	}

	/**
	 * @param theTransactions60mins
	 *            the transactions60mins to set
	 */
	public void setTransactions60mins(int[] theTransactions60mins) {
		myTransactions60mins = theTransactions60mins;
	}

}
