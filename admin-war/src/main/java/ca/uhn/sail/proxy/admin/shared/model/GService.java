package ca.uhn.sail.proxy.admin.shared.model;

public class GService extends BaseGDashboardObject<GService> {

	private static final long serialVersionUID = 1L;
	private boolean myActive;
	private GServiceVersionList myVersionList;

	public GServiceVersionList getVersionList() {
		return myVersionList;
	}

	@Override
	public void initChildList() {
		assert myVersionList == null;
		myVersionList = new GServiceVersionList();
	}

	/**
	 * @return the active
	 */
	public boolean isActive() {
		return myActive;
	}

	@Override
	public void merge(GService theObject) {
		super.merge((BaseGDashboardObject<GService>) theObject);

		if (theObject.getVersionList() != null) {
			getVersionList().mergeResults(theObject.getVersionList());
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

}
