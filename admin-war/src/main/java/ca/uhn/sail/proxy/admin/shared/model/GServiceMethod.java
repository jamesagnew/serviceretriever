package ca.uhn.sail.proxy.admin.shared.model;

public class GServiceMethod extends BaseGDashboardObject<GServiceMethod> {

	private static final long serialVersionUID = 1L;

	@Override
	public void initChildList() {
		// ignore
	}

	@Override
	public void merge(GServiceMethod theObject) {
		super.merge((BaseGDashboardObject<GServiceMethod>) theObject);
		
		markInitialized();
	}

}
