package net.svcret.ejb.api;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.ejb.Local;
import javax.servlet.AsyncContext;

import net.svcret.admin.shared.enm.ResponseTypeEnum;
import net.svcret.ejb.api.ISecurityService.AuthorizationResultsBean;
import net.svcret.ejb.ex.InvalidRequestException;
import net.svcret.ejb.ex.InvocationFailedDueToInternalErrorException;
import net.svcret.ejb.ex.InvocationRequestOrResponseFailedException;
import net.svcret.ejb.ex.ProcessingException;
import net.svcret.ejb.ex.SecurityFailureException;
import net.svcret.ejb.invoker.IServiceInvoker;
import net.svcret.ejb.invoker.soap.InvocationFailedException;
import net.svcret.ejb.model.entity.BasePersServiceVersion;
import net.svcret.ejb.model.entity.PersServiceVersionUrl;
import net.svcret.ejb.throttle.ThrottleException;
import net.svcret.ejb.throttle.ThrottleQueueFullException;

import org.apache.commons.lang3.builder.ToStringBuilder;

@Local
public interface IServiceOrchestrator {

	void enqueueThrottledRequest(ThrottleException theE, AsyncContext theAsyncContext) throws ThrottleQueueFullException;

	IServiceInvoker getServiceInvoker(BasePersServiceVersion theServiceVersion);

	SrBeanOutgoingResponse handlePreviouslyThrottledRequest(SrBeanProcessedRequest theInvocationRequest, AuthorizationResultsBean theAuthorization, SrBeanIncomingRequest theRequest) throws ProcessingException, SecurityFailureException, InvocationFailedDueToInternalErrorException;

	/**
	 * Process a normal request
	 */
	SrBeanOutgoingResponse handleServiceRequest(SrBeanIncomingRequest theRequest) throws InvalidRequestException, ProcessingException, IOException, SecurityFailureException, ThrottleException, ThrottleQueueFullException, InvocationRequestOrResponseFailedException,
			InvocationFailedDueToInternalErrorException;

	/**
	 * Process a request invoked through a means other than the proxy itself
	 * (e.g. monitoring, management console, etc.)
	 * 
	 * @throws InvocationFailedException
	 */
	SidechannelOrchestratorResponseBean handleSidechannelRequest(long theServiceVersionPid, String theRequestBody, String theContentType, String theRequestedByString) throws InvalidRequestException, InvocationFailedException;

	Collection<SidechannelOrchestratorResponseBean> handleSidechannelRequestForEachUrl(long theServiceVersionPid, String theRequestBody, String theContentType, String theRequestedByString);

	public static class SidechannelOrchestratorResponseBean extends SrBeanOutgoingResponse {

		private PersServiceVersionUrl myApplicableUrl;
		private String myFailureDescription;
		private SrBeanIncomingRequest myIncomingRequest;
		private SrBeanIncomingResponse myIncomingResponse;
		private SrBeanProcessedResponse myProcessedResponse;
		private Date myRequestStartedTime;
		private ResponseTypeEnum myResponseType;
		private SrBeanIncomingRequest mySimulatedIncomingRequest;
		private SrBeanProcessedRequest mySimulatedProcessedRequest;

		public SidechannelOrchestratorResponseBean(SrBeanIncomingRequest theIncomingRequest, SrBeanProcessedResponse theProcessedResponse, SrBeanIncomingResponse theIncomingResponse) {
			super(theProcessedResponse, theIncomingResponse);

			myIncomingRequest = theIncomingRequest;
			myIncomingResponse = theIncomingResponse;
			myProcessedResponse = theProcessedResponse;

			myResponseType = theProcessedResponse.getResponseType();
			myRequestStartedTime = theIncomingRequest.getRequestTime();

		}

		public PersServiceVersionUrl getApplicableUrl() {
			return myApplicableUrl != null ? myApplicableUrl : myIncomingResponse.getSingleUrlOrThrow();
		}

		public String getFailureDescription() {
			return myFailureDescription;
		}

		public SrBeanIncomingRequest getIncomingRequest() {
			return myIncomingRequest;
		}

		public SrBeanIncomingResponse getIncomingResponse() {
			return myIncomingResponse;
		}

		public SrBeanProcessedResponse getProcessedResponse() {
			return myProcessedResponse;
		}

		// public Date getRequestStartedTime() {
		// return myRequestStartedTime;
		// }

		public ResponseTypeEnum getResponseType() {
			return myResponseType;
		}

		public SrBeanIncomingRequest getSimulatedIncomingRequest() {
			return mySimulatedIncomingRequest;
		}

		public SrBeanProcessedRequest getSimulatedProcessedRequest() {
			return mySimulatedProcessedRequest;
		}

		public void setApplicableUrl(PersServiceVersionUrl theApplicableUrl) {
			myApplicableUrl = theApplicableUrl;
		}

		public void setFailureDescription(String theFailureDescription) {
			myFailureDescription = theFailureDescription;
		}

		public void setSimulatedIncomingRequest(SrBeanIncomingRequest theSimulatedIncomingRequest) {
			mySimulatedIncomingRequest = theSimulatedIncomingRequest;
		}

		public void setSimulatedProcessedRequest(SrBeanProcessedRequest theSimulatedProcessedRequest) {
			mySimulatedProcessedRequest = theSimulatedProcessedRequest;
		}

		@Override
		public String toString() {
			ToStringBuilder b = new ToStringBuilder(this);
			b.append("URL", getApplicableUrl().getPid());
			return b.build();
		}

		public static SidechannelOrchestratorResponseBean forFailure(Exception theException, PersServiceVersionUrl theApplicableUrl, SrBeanIncomingRequest theIncomingRequest, SrBeanProcessedResponse theProcessedResponse, SrBeanIncomingResponse theIncomingResponse) {
			String responseBody = null;
			String responseContentType = null;
			HashMap<String, List<String>> responseHeaders = new HashMap<String, List<String>>();
			SrBeanIncomingResponse httpResponse = null;
			SidechannelOrchestratorResponseBean retVal = new SidechannelOrchestratorResponseBean(theIncomingRequest, theProcessedResponse, theIncomingResponse);
			// SidechannelOrchestratorResponseBean retVal = new
			// SidechannelOrchestratorResponseBean(responseBody,
			// responseContentType, responseHeaders, httpResponse,
			// ResponseTypeEnum.FAIL, theRequestStartedTime);
			retVal.setFailureDescription(theException.toString());
			retVal.setApplicableUrl(theApplicableUrl);
			return retVal;
		}

	}

}
