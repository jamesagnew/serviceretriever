package net.svcret.ejb.api;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.ejb.Local;

import net.svcret.admin.shared.model.DtoStickySessionUrlBinding;
import net.svcret.ejb.api.HttpResponseBean.Failure;
import net.svcret.ejb.ex.ProcessingException;
import net.svcret.ejb.ex.UnexpectedFailureException;
import net.svcret.ejb.model.entity.BasePersServiceVersion;
import net.svcret.ejb.model.entity.PersServiceVersionMethod;
import net.svcret.ejb.model.entity.PersServiceVersionResource;
import net.svcret.ejb.model.entity.PersServiceVersionUrl;
import net.svcret.ejb.model.entity.PersUser;

@Local
public interface IRuntimeStatus {

	UrlPoolBean buildUrlPool(BasePersServiceVersion theServiceVersion, Map<String, List<String>> theRequestHeaders) throws UnexpectedFailureException;

	void collapseStats() throws UnexpectedFailureException;

	/**
	 * Flush all outstanding transactions to the database - Call this from a background worker thread
	 */
	void flushStatus();

	/**
	 * 
	 * @param theInvocationTime
	 * @param theRequestLength
	 * @param theMethod
	 * @param theAuthorizedUser
	 * @param theHttpResponse
	 *            The response from the actual service implementation, if we got that far. Can be null if no request was ever made (e.g. because of security failure before that point)
	 * @param theInvocationResponseResultsBean
	 * @throws ProcessingException
	 * @throws UnexpectedFailureException 
	 */
	void recordInvocationMethod(Date theInvocationTime, int theRequestLength, PersServiceVersionMethod theMethod, PersUser theAuthorizedUser, HttpResponseBean theHttpResponse,
			InvocationResponseResultsBean theInvocationResponseResultsBean, Long theThrottleDelayIfAny) throws UnexpectedFailureException;

	/**
	 * Records a single invocation requesting a static resource
	 */
	void recordInvocationStaticResource(Date theInvocationTime, PersServiceVersionResource theResource);

	void recordNodeStatistics();

	/**
	 * Records a single invocation which failed due to service failure
	 */
	void recordUrlFailure(PersServiceVersionUrl theUrl, Failure theFailure);

	void reloadUrlStatus(Long thePid);

	void updatedStickySessionBinding(DtoStickySessionUrlBinding theBinding);

}
