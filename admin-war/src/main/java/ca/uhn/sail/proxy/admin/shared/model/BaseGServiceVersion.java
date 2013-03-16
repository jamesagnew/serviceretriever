package ca.uhn.sail.proxy.admin.shared.model;

import java.util.Date;

public abstract class BaseGServiceVersion extends BaseGDashboardObject<BaseGServiceVersion> {

	private static final long serialVersionUID = 7886801527330335503L;

	private boolean myActive;
	private Date myLastAccess;
	private GServiceMethodList myServiceMethodList;
	private GServiceVersionUrlList myServiceUrlList;
	/**
	 * ID which is local to the admin portal
	 */
	private Long myUncommittedSessionId;
	private int myUrlsActive;
	private int myUrlsFailed;
	private int myUrlsUnknown;

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
	 * @return the uncommittedSessionId
	 */
	public Long getUncommittedSessionId() {
		return myUncommittedSessionId;
	}

	public GServiceVersionUrlList getUrlList() {
		return myServiceUrlList;
	}

	/**
	 * @return the urlsActive
	 */
	public int getUrlsActive() {
		return myUrlsActive;
	}

	/**
	 * @return the urlsFailed
	 */
	public int getUrlsFailed() {
		return myUrlsFailed;
	}

	/**
	 * @return the urlsUnknown
	 */
	public int getUrlsUnknown() {
		return myUrlsUnknown;
	}

	@Override
	public void initChildList() {
		assert myServiceMethodList == null;
		myServiceMethodList = new GServiceMethodList();
		myServiceUrlList = new GServiceVersionUrlList();
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
		myUrlsActive = theObject.getUrlsActive();
		myUrlsFailed = theObject.getUrlsFailed();
		myUrlsUnknown = theObject.getUrlsUnknown();

		if (theObject.getMethodList() != null) {
			getMethodList().mergeResults(theObject.getMethodList());
		}

		if (theObject.getUrlList() != null) {
			getUrlList().mergeResults(theObject.getUrlList());
		}

		markInitialized();
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
	 * @param theUncommittedSessionId
	 *            the uncommittedSessionId to set
	 */
	public void setUncommittedSessionId(Long theUncommittedSessionId) {
		myUncommittedSessionId = theUncommittedSessionId;
	}

	/**
	 * @param theUrlsActive
	 *            the urlsActive to set
	 */
	public void setUrlsActive(int theUrlsActive) {
		myUrlsActive = theUrlsActive;
	}

	/**
	 * @param theUrlsFailed
	 *            the urlsFailed to set
	 */
	public void setUrlsFailed(int theUrlsFailed) {
		myUrlsFailed = theUrlsFailed;
	}

	/**
	 * @param theUrlsUnknown
	 *            the urlsUnknown to set
	 */
	public void setUrlsUnknown(int theUrlsUnknown) {
		myUrlsUnknown = theUrlsUnknown;
	}

}
