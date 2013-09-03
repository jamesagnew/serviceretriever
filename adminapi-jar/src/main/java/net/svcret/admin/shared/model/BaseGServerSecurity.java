package net.svcret.admin.shared.model;

public abstract class BaseGServerSecurity extends BaseGObject {

	private static final long serialVersionUID = 1L;

	private long myAuthHostPid;
	
	private transient boolean myEditMode;
	
	/**
	 * @return the authHostPid
	 */
	public long getAuthHostPid() {
		return myAuthHostPid;
	}

	public abstract ServerSecurityEnum getType();

	/**
	 * @return the editMode
	 */
	public boolean isEditMode() {
		return myEditMode;
	}

	/**
	 * @param theAuthHostPid the authHostPid to set
	 */
	public void setAuthHostPid(long theAuthHostPid) {
		myAuthHostPid = theAuthHostPid;
	}
	
	/**
	 * @param theEditMode the editMode to set
	 */
	public void setEditMode(boolean theEditMode) {
		myEditMode = theEditMode;
	}

}
