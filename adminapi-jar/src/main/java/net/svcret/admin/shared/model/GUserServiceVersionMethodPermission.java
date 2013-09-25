package net.svcret.admin.shared.model;

public class GUserServiceVersionMethodPermission extends BaseDtoObject {

	private static final long serialVersionUID = 1L;
	
	private long myServiceVersionMethodPid;
	
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
	public void merge(BaseDtoObject theObject) {
		super.merge(theObject);

		setServiceVersionMethodPid(((GUserServiceVersionMethodPermission)theObject).getServiceVersionMethodPid());
	}

}
