package ca.uhn.sail.proxy.admin.shared.model;

public abstract class BaseGObject<T> extends BaseGListenable<T> {

	private static final long serialVersionUID = 1L;

	private long myPid;

	public long getPid() {
		return myPid;
	}

	public void setPid(long thePid) {
		myPid = thePid;
	}

	@Override
	public int hashCode() {
		return Long.valueOf(myPid).hashCode();
	}

	@Override
	public boolean equals(Object theObj) {
		if (theObj == null) {
			return false;
		}
		if (!theObj.getClass().equals(getClass())) {
			return false;
		}
		return myPid == ((BaseGObject<?>) theObj).myPid;
	}

	public abstract void initChildList();
	
	public abstract void merge(T theObject);
}
