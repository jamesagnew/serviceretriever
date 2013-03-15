package ca.uhn.sail.proxy.api;

import java.util.Date;

import javax.ejb.Local;

import ca.uhn.sail.proxy.api.HttpResponseBean.Failure;
import ca.uhn.sail.proxy.model.entity.BasePersServiceVersion;
import ca.uhn.sail.proxy.model.entity.PersServiceVersionMethod;
import ca.uhn.sail.proxy.model.entity.PersServiceUser;
import ca.uhn.sail.proxy.model.entity.PersServiceVersionResource;
import ca.uhn.sail.proxy.model.entity.PersServiceVersionUrl;

@Local
public interface IRuntimeStatus {

	UrlPoolBean buildUrlPool(BasePersServiceVersion theServiceVersion);

	void recordInvocationMethod(Date theInvocationTime, int theRequestLength, PersServiceVersionMethod theMethod, PersServiceUser theUser, HttpResponseBean theHttpResponse, InvocationResponseResultsBean theInvocationResponseResultsBean);

	/**
	 * Records a single invocation requesting a static resource
	 */
	void recordInvocationStaticResource(Date theInvocationTime, PersServiceVersionResource theResource);

	void recordUrlFailure(PersServiceVersionUrl theUrl, Failure theFailure);

}
