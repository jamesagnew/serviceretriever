package net.svcret.admin.shared.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class GService extends BaseGDashboardObjectWithUrls<GService> {

	private static final long serialVersionUID = 1L;
	private boolean myActive;
	private GServiceVersionList myVersionList = new GServiceVersionList();;
	private transient Set<Long> myServiceVersionPids = new HashSet<Long>();

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

		myServiceVersionPids.clear();
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

	public boolean allVersionPidsInThisServiceAreAmongThesePids(Set<Long> theAffectedSvcVerPids) {
		populateSvcVerPids();
		return myServiceVersionPids.containsAll(theAffectedSvcVerPids);
	}

	private void populateSvcVerPids() {
		if (myServiceVersionPids.isEmpty()) {
			for (BaseGServiceVersion next : getVersionList()) {
				myServiceVersionPids.add(next.getPid());
			}
		}
	}

	public boolean anyVersionPidsInThisServiceAreAmongThesePids(Set<Long> theAffectedSvcVerPids) {
		populateSvcVerPids();
		for (long next : myServiceVersionPids) {
			if (theAffectedSvcVerPids.contains(next)) {
				return true;
			}
		}
		return false;
	}

	public Collection<Long> getAllServiceVersionPids() {
		HashSet<Long> retVal = new HashSet<Long>();
		for (BaseGServiceVersion next : getVersionList()) {
			retVal.add(next.getPid());
		}
		return retVal;
	}

}
