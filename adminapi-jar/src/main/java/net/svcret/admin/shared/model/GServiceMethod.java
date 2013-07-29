package net.svcret.admin.shared.model;

import java.util.Date;

public class GServiceMethod extends BaseGDashboardObject<GServiceMethod> {

	private static final long serialVersionUID = 1L;

	private String myRootElements;
	private Date myLastAccess;

	public Date getLastAccess() {
		return myLastAccess;
	}

	public void setLastAccess(Date theLastAccess) {
		myLastAccess = theLastAccess;
	}

	public GServiceMethod() {
		super();
	}

	public GServiceMethod(String theId) {
		setId(theId);
		setName(theId);
	}

	public String getRootElements() {
		return myRootElements;
	}

	@Override
	public void merge(GServiceMethod theObject) {
		myRootElements = theObject.getRootElements();
		if (theObject.isStatsInitialized()) {
			myLastAccess = theObject.getLastAccess();
		}
		super.merge((BaseGDashboardObject<GServiceMethod>) theObject);
	}

	public void setRootElements(String theRootElements) {
		myRootElements = theRootElements;
	}

}
