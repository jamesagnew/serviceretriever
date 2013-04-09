package net.svcret.admin.shared.model;

public abstract class BaseGObject<T> extends BaseGListenable<T> {

	private static final long serialVersionUID = 1L;

	private long myPid;

	/**
	 * ID which is local to the admin portal
	 */
	private Long myUncommittedSessionId;

	@Override
	public boolean equals(Object theObj) {
		if (theObj == null) {
			return false;
		}
		if (!theObj.getClass().equals(getClass())) {
			return false;
		}
		
		BaseGObject<?> obj = (BaseGObject<?>) theObj;
		
		if (myPid != 0 && obj.myPid != 0) {
			return myPid == obj.myPid;
		}
		
		if (myUncommittedSessionId != null && obj.myUncommittedSessionId != null) {
			return myUncommittedSessionId.equals(obj.myUncommittedSessionId);
		}
		
		return theObj == this;
	}

	public long getPid() {
		return myPid;
	}

	public Long getPidOrNull() {
		return myPid > 0 ? myPid : null;
	}

	/**
	 * @return the uncommittedSessionId
	 */
	public Long getUncommittedSessionId() {
		return myUncommittedSessionId;
	}

	@Override
	public int hashCode() {
		if (myPid != 0) {
			return Long.valueOf(myPid).hashCode();
		}
		if (myUncommittedSessionId != null) {
			return myUncommittedSessionId.hashCode();
		}
		return 0;
	}

	public abstract void merge(T theObject);

	public void setPid(long thePid) {
		myPid = thePid;
	}

	/**
	 * @param theUncommittedSessionId
	 *            the uncommittedSessionId to set
	 */
	public void setUncommittedSessionId(Long theUncommittedSessionId) {
		myUncommittedSessionId = theUncommittedSessionId;
	}
}
