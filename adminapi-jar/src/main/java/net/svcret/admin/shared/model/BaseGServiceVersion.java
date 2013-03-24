package net.svcret.admin.shared.model;

import java.util.Date;

public abstract class BaseGServiceVersion extends BaseGDashboardObjectWithUrls<BaseGServiceVersion> implements IProvidesUrlCount {

	private static final long serialVersionUID = 7886801527330335503L;

	private boolean myActive;
	private BaseGClientSecurityList myClientSecurityList;
	private Date myLastAccess;
	private GServiceVersionResourcePointerList myResourcePointerList;
	private BaseGServerSecurityList myServerSecurityList;
	private GServiceMethodList myServiceMethodList;
	private GServiceVersionUrlList myServiceUrlList;

	public BaseGServiceVersion() {
		myServiceMethodList = new GServiceMethodList();
		myServiceUrlList = new GServiceVersionUrlList();
		myServerSecurityList = new BaseGServerSecurityList();
		myClientSecurityList = new BaseGClientSecurityList();
		myResourcePointerList = new GServiceVersionResourcePointerList();
	}
	
	public boolean hasMethodWithName(String theName) {
		for (GServiceMethod next : myServiceMethodList) {
			if (next.getName().equals(theName)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean hasUrlWithName(String theUrl) {
		for (GServiceVersionUrl next : myServiceUrlList) {
			if (next.getUrl().equals(theUrl)) {
				return true;
			}
		}
		return false;
	}



	/**
	 * @return the clientSecurityList
	 */
	public BaseGClientSecurityList getClientSecurityList() {
		return myClientSecurityList;
	}

	/**
	 * @return the lastAccess
	 */
	public Date getLastAccess() {
		return myLastAccess;
	}

	/**
	 * @return the serviceUrlList
	 */
	public GServiceMethodList getMethodList() {
		return myServiceMethodList;
	}

	/**
	 * @return the resourcePointerList
	 */
	public GServiceVersionResourcePointerList getResourcePointerList() {
		return myResourcePointerList;
	}

	/**
	 * @return the serverSecurityList
	 */
	public BaseGServerSecurityList getServerSecurityList() {
		return myServerSecurityList;
	}

	public GServiceVersionUrlList getUrlList() {
		return myServiceUrlList;
	}

	/**
	 * @return the active
	 */
	public boolean isActive() {
		return myActive;
	}

	@Override
	public void merge(BaseGServiceVersion theObject) {
		super.merge((BaseGDashboardObject<BaseGServiceVersion>) theObject);
		
		myActive = theObject.myActive;
		myLastAccess = theObject.myLastAccess;

		if (theObject.getMethodList() != null) {
			getMethodList().mergeResults(theObject.getMethodList());
		}

		if (theObject.getUrlList() != null) {
			getUrlList().mergeResults(theObject.getUrlList());
		}

		if (theObject.getServerSecurityList() != null) {
			getServerSecurityList().mergeResults(theObject.getServerSecurityList());
		}

		if (theObject.getClientSecurityList() != null) {
			getClientSecurityList().mergeResults(theObject.getClientSecurityList());
		}

		if (theObject.getResourcePointerList() != null) {
			getResourcePointerList().mergeResults(theObject.getResourcePointerList());
		}

	}

	/**
	 * @param theActive
	 *            the active to set
	 */
	public void setActive(boolean theActive) {
		myActive = theActive;
	}

	/**
	 * @param theLastAccess
	 *            the lastAccess to set
	 */
	public void setLastAccess(Date theLastAccess) {
		myLastAccess = theLastAccess;
	}

	/**
	 * @param theResourcePointerList
	 *            the resourcePointerList to set
	 */
	public void setResourcePointerList(GServiceVersionResourcePointerList theResourcePointerList) {
		myResourcePointerList = theResourcePointerList;
	}

}
