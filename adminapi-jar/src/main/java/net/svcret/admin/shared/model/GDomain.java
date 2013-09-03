package net.svcret.admin.shared.model;

import java.util.HashSet;
import java.util.Set;

public class GDomain extends BaseDtoServiceCatalogItem {

	private static final long serialVersionUID = 1L;

	private GServiceList myServiceList = new GServiceList();


	public GServiceList getServiceList() {
		return myServiceList;
	}

	@Override
	public void merge(BaseGObject theObject) {
		super.merge(theObject);

		GDomain obj=(GDomain) theObject;
		if (obj.getServiceList() != null) {
			getServiceList().mergeResults(obj.getServiceList());
		}

	}

	public Set<Long> getAllServiceVersionPids() {
		HashSet<Long> retVal = new HashSet<Long>();
		for (GService next : getServiceList()) {
			retVal.addAll(next.getAllServiceVersionPids());
		}
		return retVal;
	}


}
