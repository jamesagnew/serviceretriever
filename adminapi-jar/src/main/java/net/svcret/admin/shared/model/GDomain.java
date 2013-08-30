package net.svcret.admin.shared.model;

import java.util.HashSet;
import java.util.Set;

public class GDomain extends BaseDtoServiceCatalogItem<GDomain> {

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

	public Set<Long> getAllServiceVersionPids() {
		HashSet<Long> retVal = new HashSet<Long>();
		for (GService next : getServiceList()) {
			retVal.addAll(next.getAllServiceVersionPids());
		}
		return retVal;
	}


}
