package net.svcret.ejb.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.svcret.ejb.model.entity.PersServiceVersionMethod;
import net.svcret.ejb.model.entity.PersServiceVersionResource;

import org.apache.commons.lang3.Validate;

public class InvocationResultsBean {

	private Map<Class<? extends ICredentialGrabber>, ICredentialGrabber> myCredentialsInRequest = new HashMap<Class<? extends ICredentialGrabber>, ICredentialGrabber>();
	private String myMethodContentType;
	private PersServiceVersionMethod myMethodDefinition;
	private Map<String, String> myMethodHeaders;
	private String myMethodRequestBody;
	private ResultTypeEnum myResultType;
	private IServiceInvoker<?> myServiceInvoker;
	private String myStaticResourceContentTyoe;
	private PersServiceVersionResource myStaticResourceDefinition;
	private Map<String, List<String>> myStaticResourceHeaders;
	private String myStaticResourceText;
	private String myStaticResourceUrl;

	public void addCredentials(ICredentialGrabber theCredentials) {
		Validate.notNull(theCredentials);

		if (myCredentialsInRequest.containsKey(theCredentials.getClass())) {
			throw new IllegalArgumentException("Duplicate credential grabber type: " + theCredentials.getClass());
		}

		myCredentialsInRequest.put(theCredentials.getClass(), theCredentials);
	}

	/**
	 * @return the credentialsInRequest
	 */
	public ICredentialGrabber getCredentialsInRequest(Class<? extends ICredentialGrabber> theType) {
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
	public PersServiceVersionMethod getMethodDefinition() {
		return myMethodDefinition;
	}

	/**
	 * @return the methodHeaders
	 */
	public Map<String, String> getMethodHeaders() {
		return myMethodHeaders;
	}

	/**
	 * @return the methodRequestBody
	 */
	public String getMethodRequestBody() {
		return myMethodRequestBody;
	}

	/**
	 * @return the resultType
	 */
	public ResultTypeEnum getResultType() {
		return myResultType;
	}

	public IServiceInvoker<?> getServiceInvoker() {
		return myServiceInvoker;
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

	public void setResultMethod(PersServiceVersionMethod theMethod, String theRequestBody, String theContentType, Map<String, String> theHeaders) {
		validateResultTypeNotSet();
		myResultType = ResultTypeEnum.METHOD;
		myMethodDefinition = theMethod;
		myMethodRequestBody = theRequestBody;
		myMethodContentType = theContentType;
		myMethodHeaders = theHeaders;
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

	public void setServiceInvoker(IServiceInvoker<?> theServiceInvoker) {
		myServiceInvoker = theServiceInvoker;
	}

	private void validateResultTypeNotSet() {
		if (myResultType != null) {
			throw new IllegalStateException("Request type already set");
		}
	}

	public static enum ResultTypeEnum {

		METHOD,

		STATIC_RESOURCE

	}

}
