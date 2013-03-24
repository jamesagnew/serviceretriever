package net.svcret.ejb.api;

import java.util.Date;

import javax.ejb.Local;

import net.svcret.ejb.api.HttpResponseBean.Failure;
import net.svcret.ejb.model.entity.BasePersServiceVersion;
import net.svcret.ejb.model.entity.PersServiceUser;
import net.svcret.ejb.model.entity.PersServiceVersionMethod;
import net.svcret.ejb.model.entity.PersServiceVersionResource;
import net.svcret.ejb.model.entity.PersServiceVersionUrl;


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
