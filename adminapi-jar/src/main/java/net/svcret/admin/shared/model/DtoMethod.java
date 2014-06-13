package net.svcret.admin.shared.model;

import java.util.Collections;
import java.util.List;

import net.svcret.admin.shared.enm.MethodSecurityPolicyEnum;

public class DtoMethod extends BaseDtoDashboardObject {

	private static final long serialVersionUID = 1L;

	private String myRootElements;
	private MethodSecurityPolicyEnum mySecurityPolicy = MethodSecurityPolicyEnum.getDefault();
    /*
        By default, all methods can be throttled.
        Only flagged (blacklisted) methods are
        exempted from throttling.
    */
    private boolean myThrottleDisabled = false;

	public DtoMethod() {
		super();
	}

	public DtoMethod(String theId) {
		setId(theId);
		setName(theId);
	}

    /**
     *
     * @return <code>true</code> if the throttle is disabled,
     *         <code>false</code> otherwise
     */
    public boolean isThrottleDisabled() {
        return myThrottleDisabled;
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

		DtoMethod obj = (DtoMethod) theObject;
		myRootElements = obj.getRootElements();
		mySecurityPolicy = obj.getSecurityPolicy();
        myThrottleDisabled = obj.isThrottleDisabled();

	}

    public void setThrottleDisabled(boolean theThrottleDisabled) {
        myThrottleDisabled = theThrottleDisabled;
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
