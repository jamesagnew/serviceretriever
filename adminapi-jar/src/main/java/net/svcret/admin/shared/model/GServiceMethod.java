package net.svcret.admin.shared.model;

import java.util.Date;

import net.svcret.admin.shared.enm.MethodSecurityPolicyEnum;

public class GServiceMethod extends BaseGDashboardObject<GServiceMethod> {

	private static final long serialVersionUID = 1L;

	private Date myLastAccess;
	private String myRootElements;
	private MethodSecurityPolicyEnum mySecurityPolicy;

	public GServiceMethod() {
		super();
	}

	public GServiceMethod(String theId) {
		setId(theId);
		setName(theId);
	}

	public Date getLastAccess() {
		return myLastAccess;
	}

	public String getRootElements() {
		return myRootElements;
	}

	public MethodSecurityPolicyEnum getSecurityPolicy() {
		return mySecurityPolicy;
	}

	@Override
	public void merge(GServiceMethod theObject) {
		myRootElements = theObject.getRootElements();
		if (theObject.isStatsInitialized()) {
			myLastAccess = theObject.getLastAccess();
		}
		super.merge((BaseGDashboardObject<GServiceMethod>) theObject);
	}

	public void setLastAccess(Date theLastAccess) {
		myLastAccess = theLastAccess;
	}

	public void setRootElements(String theRootElements) {
		myRootElements = theRootElements;
	}

	public void setSecurityPolicy(MethodSecurityPolicyEnum theSecurityPolicy) {
		mySecurityPolicy = theSecurityPolicy;
	}

}
