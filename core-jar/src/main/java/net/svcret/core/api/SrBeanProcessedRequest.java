package net.svcret.core.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.svcret.core.model.entity.BasePersServiceVersion;
import net.svcret.core.model.entity.PersBaseServerAuth;
import net.svcret.core.model.entity.PersMethod;
import net.svcret.core.model.entity.PersServiceVersionResource;

import org.apache.commons.lang3.Validate;

import com.google.common.annotations.VisibleForTesting;

public class SrBeanProcessedRequest {

	private Map<PersBaseServerAuth<?, ?>, ICredentialGrabber> myCredentialsInRequest = new HashMap<>();
	private String myMethodContentType;
	private PersMethod myMethodDefinition;
    /*
        By default, all methods can be throttled.
        Only flagged (blacklisted) methods are
        exempted from throttling.
    */
    private boolean myThrottleDisabled = false;
	private Map<String, List<String>> myMethodHeaders;
	private String myMethodRequestBody;
	private String myObscuredRequestBody;
	private Map<String, String> myPropertyCaptures;
	private ResultTypeEnum myResultType;
	private BasePersServiceVersion myServiceVersion;
	private String myStaticResourceContentTyoe;
	private PersServiceVersionResource myStaticResourceDefinition;
	private Map<String, List<String>> myStaticResourceHeaders;
	private String myStaticResourceText;
	private String myStaticResourceUrl;
	private Long myThrottleTimeIfAny;
	private String myUrlSuffix;

	public void addCredentials(PersBaseServerAuth<?, ?> theServerAuth, ICredentialGrabber theCredentials) {
		Validate.notNull(theCredentials);

		if (myCredentialsInRequest.containsKey(theServerAuth)) {
			throw new IllegalArgumentException("Already have an entry for server auth: " + theServerAuth.getPid());
		}

		myCredentialsInRequest.put(theServerAuth, theCredentials);
	}

	public void addPropertyCapture(String thePropertyName, String theResult) {
		if (myPropertyCaptures == null) {
			myPropertyCaptures = new HashMap<>();
		}
		myPropertyCaptures.put(thePropertyName, theResult);
	}

	/**
	 * @return the credentialsInRequest
	 */
	public ICredentialGrabber getCredentialsInRequest(PersBaseServerAuth<?, ?> theType) {
		return myCredentialsInRequest.get(theType);
	}

	/**
	 * @return the methodContentType
	 */
	public String getMethodContentType() {
		return myMethodContentType;
	}

	/**
	 * @return the methodDefinition
	 */
	public PersMethod getMethodDefinition() {
		return myMethodDefinition;
	}

    /**
     * @return <code>true</code> if the throttle is disabled,
     *         <code>false</code> otherwise
     */
    public boolean isThrottleDisabled() {
        return myThrottleDisabled;
    }

    /**
	 * @return the methodHeaders
	 */
	public Map<String, List<String>> getMethodHeaders() {
		if (myMethodHeaders == null) {
			myMethodHeaders = new HashMap<>();
		}
		return myMethodHeaders;
	}

	/**
	 * @return the methodRequestBody
	 */
	public String getMethodRequestBody() {
		return myMethodRequestBody;
	}

	public String getObscuredRequestBody() {
		return myObscuredRequestBody;
	}

	public Map<String, String> getPropertyCaptures() {
		return myPropertyCaptures;
	}

	/**
	 * @return the resultType
	 */
	public ResultTypeEnum getResultType() {
		return myResultType;
	}

	public BasePersServiceVersion getServiceVersion() {
		return myServiceVersion;
	}

	/**
	 * @return the staticResourceContentTyoe
	 */
	public String getStaticResourceContentTyoe() {
		return myStaticResourceContentTyoe;
	}

	/**
	 * @return the staticResourceBody
	 */
	public PersServiceVersionResource getStaticResourceDefinition() {
		return myStaticResourceDefinition;
	}

	/**
	 * @return the staticResourceHeaders
	 */
	public Map<String, List<String>> getStaticResourceHeaders() {
		return myStaticResourceHeaders;
	}

	/**
	 * @return the staticResourceText
	 */
	public String getStaticResourceText() {
		return myStaticResourceText;
	}

	/**
	 * @return the staticResourceUrl
	 */
	public String getStaticResourceUrl() {
		return myStaticResourceUrl;
	}

	public Long getThrottleTimeIfAny() {
		return myThrottleTimeIfAny;
	}

	public String getUrlSuffix() {
		return myUrlSuffix;
	}

	public void setMethodHeaders(Map<String, List<String>> theMethodHeaders) {
		myMethodHeaders = theMethodHeaders;
	}

	public void setObscuredRequestBody(String theObscuredRequestBody) {
		myObscuredRequestBody = theObscuredRequestBody;
	}

	public void setResultMethod(PersMethod theMethod, String theRequestBody, String theContentType) {
		validateResultTypeNotSet();
		myResultType = ResultTypeEnum.METHOD;
		myMethodDefinition = theMethod;
        myThrottleDisabled = theMethod.isThrottleDisabled();
		myServiceVersion = theMethod.getServiceVersion();
		myMethodRequestBody = theRequestBody;
		myMethodContentType = theContentType;
	}

	public void setResultStaticResource(String theResourceUrl, PersServiceVersionResource theResource, String theResourceText, String theContentType, Map<String, List<String>> theHeaders) {
		validateResultTypeNotSet();
		myResultType = ResultTypeEnum.STATIC_RESOURCE;
		myStaticResourceUrl = theResourceUrl;
		myStaticResourceDefinition = theResource;
		myStaticResourceContentTyoe = theContentType;
		myStaticResourceHeaders = theHeaders;
		myStaticResourceText = theResourceText;
	}

    public void setServiceVersion(BasePersServiceVersion theServiceVersion) {
		myServiceVersion = theServiceVersion;
	}

    public void setThrottleDisabled(boolean theThrottleDisabled) {
        myThrottleDisabled = theThrottleDisabled;
    }

    public void setThrottleTimeIfAny(Long theThrottleTimeIfAny) {
		myThrottleTimeIfAny = theThrottleTimeIfAny;
	}

	public void setUrlSuffix(String theUrlSuffix) {
		myUrlSuffix = theUrlSuffix;
	}

	private void validateResultTypeNotSet() {
		if (myResultType != null) {
			throw new IllegalStateException("Request type already set");
		}
	}

	@VisibleForTesting
	public static SrBeanProcessedRequest forUnitTest(PersMethod theM1) {
		SrBeanProcessedRequest retVal = new SrBeanProcessedRequest();
		retVal.myMethodDefinition = theM1;
		retVal.myServiceVersion = theM1.getServiceVersion();
		return retVal;
	}

	public static enum ResultTypeEnum {

		METHOD,

		STATIC_RESOURCE

	}

}
