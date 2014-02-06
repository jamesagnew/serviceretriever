package net.svcret.admin.shared.model;

import java.util.Collections;
import java.util.List;

import net.svcret.admin.shared.enm.MethodSecurityPolicyEnum;

public class GServiceMethod extends BaseDtoDashboardObject {

	private static final long serialVersionUID = 1L;

	
	private String myRootElements;
	private MethodSecurityPolicyEnum mySecurityPolicy = MethodSecurityPolicyEnum.getDefault();

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

	public MethodSecurityPolicyEnum getSecurityPolicy() {
		return mySecurityPolicy;
	}

	@Override
	public void merge(BaseDtoObject theObject) {
		super.merge(theObject);

		GServiceMethod obj = (GServiceMethod) theObject;
		myRootElements = obj.getRootElements();
		mySecurityPolicy = obj.getSecurityPolicy();

	}


	public void setRootElements(String theRootElements) {
		myRootElements = theRootElements;
	}

	public void setSecurityPolicy(MethodSecurityPolicyEnum theSecurityPolicy) {
		mySecurityPolicy = theSecurityPolicy;
	}

	@Override
	public List<BaseDtoServiceVersion> getAllServiceVersions() {
		return Collections.emptyList();
	}


}
