package net.svcret.ejb.api;

import net.svcret.ejb.api.ISecurityService.AuthorizationResultsBean;
import net.svcret.ejb.ex.ThrottleException;

public interface IThrottlingService {

	void applyThrottle(long theRequestStartTime, InvocationResultsBean theInvocationRequest, AuthorizationResultsBean theAuthorization) throws ThrottleException;
	
}
