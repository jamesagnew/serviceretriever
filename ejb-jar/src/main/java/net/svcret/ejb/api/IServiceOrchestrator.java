package net.svcret.ejb.api;

import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Local;

import org.apache.commons.lang3.builder.ToStringBuilder;

import net.svcret.admin.shared.enm.ResponseTypeEnum;
import net.svcret.ejb.api.ISecurityService.AuthorizationResultsBean;
import net.svcret.ejb.ejb.ThrottleQueueFullException;
import net.svcret.ejb.ex.InvocationFailedDueToInternalErrorException;
import net.svcret.ejb.ex.InvocationRequestOrResponseFailedException;
import net.svcret.ejb.ex.ProcessingException;
import net.svcret.ejb.ex.SecurityFailureException;
import net.svcret.ejb.ex.ThrottleException;
import net.svcret.ejb.ex.UnknownRequestException;
import net.svcret.ejb.invoker.IServiceInvoker;
import net.svcret.ejb.invoker.soap.InvocationFailedException;
import net.svcret.ejb.model.entity.BasePersServiceVersion;
import net.svcret.ejb.model.entity.PersServiceVersionUrl;

@Local
public interface IServiceOrchestrator {

	void enqueueThrottledRequest(ThrottleException theE) throws ThrottleQueueFullException;

	IServiceInvoker getServiceInvoker(BasePersServiceVersion theServiceVersion);

	OrchestratorResponseBean handlePreviouslyThrottledRequest(InvocationResultsBean theInvocationRequest, AuthorizationResultsBean theAuthorization, HttpRequestBean theRequest, long theThrottleTime)
			throws ProcessingException, SecurityFailureException, InvocationFailedDueToInternalErrorException;

	/**
	 * Process a normal request
	 */
	OrchestratorResponseBean handleServiceRequest(HttpRequestBean theRequest) throws UnknownRequestException, ProcessingException, IOException, SecurityFailureException, ThrottleException,
			ThrottleQueueFullException, InvocationRequestOrResponseFailedException, InvocationFailedDueToInternalErrorException;

	/**
	 * Process a request invoked through a means other than the proxy itself (e.g. monitoring, management console, etc.)
	 * @throws InvocationFailedException 
	 */
	SidechannelOrchestratorResponseBean handleSidechannelRequest(long theServiceVersionPid, String theRequestBody, String theContentType, String theRequestedByString) throws UnknownRequestException, InvocationFailedException;

	Collection<SidechannelOrchestratorResponseBean> handleSidechannelRequestForEachUrl(long theServiceVersionPid, String theRequestBody, String theContentType, String theRequestedByString);

	/**
	 * Response type for {@link IServiceOrchestrator#handle(RequestType, String, String, Reader)}
	 */
	public static class OrchestratorResponseBean {
		private HttpResponseBean myHttpResponse;
		private String myResponseBody;
		private String myResponseContentType;
		private Map<String, List<String>> myResponseHeaders;

		public OrchestratorResponseBean(String theResponseBody, String theResponseContentType, Map<String, List<String>> theResponseHeaders, HttpResponseBean theHttpResponse) {
			super();
			myResponseBody = theResponseBody;
			myResponseContentType = theResponseContentType;
			myResponseHeaders = theResponseHeaders;
			myHttpResponse = theHttpResponse;
		}

		public HttpResponseBean getHttpResponse() {
			return myHttpResponse;
		}

		/**
		 * @return the responseBody
		 */
		public String getResponseBody() {
			return myResponseBody;
		}

		/**
		 * @return the responseContentType
		 */
		public String getResponseContentType() {
			return myResponseContentType;
		}

		/**
		 * @return the responseHeaders
		 */
		public Map<String, List<String>> getResponseHeaders() {
			return myResponseHeaders;
		}

	}

	public static class SidechannelOrchestratorResponseBean extends OrchestratorResponseBean {

		private PersServiceVersionUrl myApplicableUrl;
		private String myFailureDescription;
		private Date myRequestStartedTime;
		private ResponseTypeEnum myResponseType;

		public SidechannelOrchestratorResponseBean(String theResponseBody, String theResponseContentType, Map<String, List<String>> theResponseHeaders, HttpResponseBean theHttpResponse,
				ResponseTypeEnum theResponseType, Date theRequestStartedTime) {
			super(theResponseBody, theResponseContentType, theResponseHeaders, theHttpResponse);

			if (theResponseType==null) {
				throw new NullPointerException("Response type must not be null");
			}
			
			myResponseType = theResponseType;
			myRequestStartedTime = theRequestStartedTime;
		}

		public PersServiceVersionUrl getApplicableUrl() {
			return myApplicableUrl != null ? myApplicableUrl : getHttpResponse().getSingleUrlOrThrow();
		}

		public String getFailureDescription() {
			return myFailureDescription;
		}

		public Date getRequestStartedTime() {
			return myRequestStartedTime;
		}

		public ResponseTypeEnum getResponseType() {
			return myResponseType;
		}

		public void setApplicableUrl(PersServiceVersionUrl theApplicableUrl) {
			myApplicableUrl = theApplicableUrl;
		}

		public void setFailureDescription(String theFailureDescription) {
			myFailureDescription = theFailureDescription;
		}

		public static SidechannelOrchestratorResponseBean forFailure(Exception theException, Date theRequestStartedTime, PersServiceVersionUrl theApplicableUrl) {
			String responseBody = null;
			String responseContentType = null;
			HashMap<String, List<String>> responseHeaders = new HashMap<String, List<String>>();
			HttpResponseBean httpResponse = null;
			SidechannelOrchestratorResponseBean retVal = new SidechannelOrchestratorResponseBean(responseBody, responseContentType, responseHeaders, httpResponse, ResponseTypeEnum.FAIL, theRequestStartedTime);
			retVal.setFailureDescription(theException.toString());
			retVal.setApplicableUrl(theApplicableUrl);
			return retVal;
		}

		@Override
		public String toString() {
			ToStringBuilder b = new ToStringBuilder(this);
			b.append("URL", getApplicableUrl().getPid());
			b.append("Latency", getHttpResponse().getResponseTime());
			return b.build();
		}

	}

}
