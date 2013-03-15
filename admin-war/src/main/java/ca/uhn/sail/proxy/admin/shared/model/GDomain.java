package ca.uhn.sail.proxy.admin.shared.model;

public class GDomain extends BaseGDashboardObject<GDomain> {

	private static final long serialVersionUID = 1L;

	private GServiceList myServiceList;

	@Override
	public void merge(GDomain theObject) {
		super.merge((BaseGDashboardObject<GDomain>) theObject);
		
		if (theObject.getServiceList() != null) {
			getServiceList().mergeResults(theObject.getServiceList());
		}
		
		markInitialized();
	}

	public GServiceList getServiceList() {
		return myServiceList;
	}

	@Override
	public void initChildList() {
//		assert myServiceList == null;
		myServiceList = new GServiceList();
	}
}
