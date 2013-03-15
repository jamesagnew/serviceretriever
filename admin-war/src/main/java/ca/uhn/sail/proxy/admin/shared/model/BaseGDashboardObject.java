package ca.uhn.sail.proxy.admin.shared.model;

public abstract class BaseGDashboardObject<T> extends BaseGObject<T> {

	private static final long serialVersionUID = 1L;
	private boolean myExpandedOnDashboard;
	private String myId;
	private String myName;
	private StatusEnum myStatus;
	private int[] myTransactions60mins;

	protected void merge(BaseGDashboardObject<T> theObject) {
		setPid(theObject.getPid());
		setId(theObject.getId());
		setName(theObject.getName());
		setStatus(theObject.getStatus());
		setTransactions60mins(theObject.getTransactions60mins());
	}
	
	/**
	 * @return the id
	 */
	public String getId() {
		return myId;
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
	 * @param theExpandedOnDashboard the expandedOnDashboard to set
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
	 * @param theName
	 *            the name to set
	 */
	public void setName(String theName) {
		myName = theName;
	}

	/**
	 * @param theStatus the status to set
	 */
	public void setStatus(StatusEnum theStatus) {
		myStatus = theStatus;
	}

	/**
	 * @param theTransactions60mins the transactions60mins to set
	 */
	public void setTransactions60mins(int[] theTransactions60mins) {
		myTransactions60mins = theTransactions60mins;
	}
	

}
