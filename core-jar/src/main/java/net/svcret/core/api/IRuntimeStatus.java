package net.svcret.core.api;

import java.util.Date;

import net.svcret.admin.api.ProcessingException;
import net.svcret.admin.api.UnexpectedFailureException;
import net.svcret.admin.shared.model.DtoStickySessionUrlBinding;
import net.svcret.core.api.SrBeanIncomingResponse.Failure;
import net.svcret.core.ex.InvocationFailedDueToInternalErrorException;
import net.svcret.core.model.entity.BasePersServiceVersion;
import net.svcret.core.model.entity.PersServiceVersionResource;
import net.svcret.core.model.entity.PersServiceVersionUrl;
import net.svcret.core.model.entity.PersUser;

public interface IRuntimeStatus {

	UrlPoolBean buildUrlPool(BasePersServiceVersion theServiceVersion, SrBeanIncomingRequest theIncomingRequest) throws UnexpectedFailureException;

	void collapseStats() throws UnexpectedFailureException;

	/**
	 * Flush all outstanding transactions to the database - Call this from a
	 * background worker thread
	 */
	void flushStatus();

	/**
	 * 
	 * @param theInvocationTime
	 * @param theRequestLength
	 * @param theMethod
	 * @param theAuthorizedUser
	 * @param theHttpResponse
	 *            The response from the actual service implementation, if we got
	 *            that far. Can be null if no request was ever made (e.g.
	 *            because of security failure before that point)
	 * @param theInvocationResponseResultsBean
	 * @throws ProcessingException
	 * @throws UnexpectedFailureException
	 * @throws InvocationFailedDueToInternalErrorException
	 */
	void recordInvocationMethod(Date theInvocationTime, int theRequestLengthChars, SrBeanProcessedRequest theProcessedRequest, PersUser theUser, SrBeanIncomingResponse theHttpResponse, SrBeanProcessedResponse theInvocationResponseResultsBean,
			SrBeanIncomingRequest theIncomingRequest) throws UnexpectedFailureException, InvocationFailedDueToInternalErrorException;

	/**
	 * Records a single invocation requesting a static resource
	 */
	void recordInvocationStaticResource(Date theInvocationTime, PersServiceVersionResource theResource);

	void recordNodeStatistics();

	/**
	 * Records a single invocation which failed due to service failure
	 */
	void recordUrlFailure(PersServiceVersionUrl theUrl, Failure theFailure);

	void recordUrlSuccess(PersServiceVersionUrl theUrl, boolean theWasFault, String theMessage, String theContentType, int theResponseCode);

	void reloadUrlStatus(Long thePid);

	void updatedStickySessionBinding(DtoStickySessionUrlBinding theBinding);


}
