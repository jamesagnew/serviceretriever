package net.svcret.admin.shared.model;

public class GService extends BaseGDashboardObjectWithUrls<GService> {

	private static final long serialVersionUID = 1L;
	private boolean myActive;
	private GServiceVersionList myVersionList = new GServiceVersionList();;

	public GServiceVersionList getVersionList() {
		return myVersionList;
	}

	/**
	 * @return the active
	 */
	public boolean isActive() {
		return myActive;
	}

	@Override
	public void merge(GService theObject) {
		mergeSimple(theObject);

		if (theObject.getVersionList() != null) {
			getVersionList().mergeResults(theObject.getVersionList());
		}

	}

	/**
	 * @param theActive
	 *            the active to set
	 */
	public void setActive(boolean theActive) {
		myActive = theActive;
	}

	public void mergeSimple(GService theService) {
		super.merge((BaseGDashboardObject<GService>) theService);
	}

}
