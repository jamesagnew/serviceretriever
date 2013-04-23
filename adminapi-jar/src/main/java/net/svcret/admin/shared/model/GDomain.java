package net.svcret.admin.shared.model;

public class GDomain extends BaseGDashboardObjectWithUrls<GDomain> {

	private static final long serialVersionUID = 1L;

	private GServiceList myServiceList = new GServiceList();


	public GServiceList getServiceList() {
		return myServiceList;
	}

	@Override
	public void merge(GDomain theObject) {
		mergeSimple(theObject);

		if (theObject.getServiceList() != null) {
			getServiceList().mergeResults(theObject.getServiceList());
		}

	}

	public void mergeSimple(GDomain theObject) {
		super.merge((BaseGDashboardObject<GDomain>) theObject);
	}


}
