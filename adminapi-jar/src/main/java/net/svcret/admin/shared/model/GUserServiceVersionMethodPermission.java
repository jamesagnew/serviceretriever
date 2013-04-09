package net.svcret.admin.shared.model;

public class GUserServiceVersionMethodPermission extends BaseGObject<GUserServiceVersionMethodPermission> {

	private static final long serialVersionUID = 1L;
	
	private long myServiceVersionMethodPid;
	private boolean myAllow;
	
	/**
	 * @return the allow
	 */
	public boolean isAllow() {
		return myAllow;
	}
	/**
	 * @param theAllow the allow to set
	 */
	public void setAllow(boolean theAllow) {
		myAllow = theAllow;
	}
	/**
	 * @return the ServiceVersionPid
	 */
	public long getServiceVersionMethodPid() {
		return myServiceVersionMethodPid;
	}
	/**
	 * @param theServiceVersionMethodPid the ServiceVersionPid to set
	 */
	public void setServiceVersionMethodPid(long theServiceVersionMethodPid) {
		myServiceVersionMethodPid = theServiceVersionMethodPid;
	}

	@Override
	public void merge(GUserServiceVersionMethodPermission theObject) {
		setPid(theObject.getPid());
		setServiceVersionMethodPid(theObject.getServiceVersionMethodPid());
		setAllow(theObject.isAllow());
	}

}
