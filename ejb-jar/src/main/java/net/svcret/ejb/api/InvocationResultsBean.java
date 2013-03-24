package net.svcret.ejb.api;

import java.util.List;
import java.util.Map;

import net.svcret.ejb.model.entity.PersServiceVersionMethod;
import net.svcret.ejb.model.entity.PersServiceVersionResource;


public class InvocationResultsBean {

	private List<ICredentialGrabber> myCredentialsInRequest;
	private String myMethodContentType;
	private PersServiceVersionMethod myMethodDefinition;
	private Map<String, String> myMethodHeaders;
	private String myMethodRequestBody;
	private ResultTypeEnum myResultType;
	private String myStaticResourceContentTyoe;
	private PersServiceVersionResource myStaticResourceDefinition;
	private Map<String, String> myStaticResourceHeaders;
	private String myStaticResourceText;
	private String myStaticResourceUrl;

	/**
	 * @return the credentialsInRequest
	 */
	public List<ICredentialGrabber> getCredentialsInRequest() {
		return myCredentialsInRequest;
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
	public Map<String, String> getStaticResourceHeaders() {
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

	public void setCredentialsInRequest(List<ICredentialGrabber> theCredentialGrabbers) {
		myCredentialsInRequest = theCredentialGrabbers;
	}

	public void setResultMethod(PersServiceVersionMethod theMethod, String theRequestBody, String theContentType, Map<String, String> theHeaders) {
		validateResultTypeNotSet();
		myResultType = ResultTypeEnum.METHOD;
		myMethodDefinition = theMethod;
		myMethodRequestBody = theRequestBody;
		myMethodContentType = theContentType;
		myMethodHeaders = theHeaders;
	}

	public void setResultStaticResource(String theResourceUrl, PersServiceVersionResource theResource, String theResourceText, String theContentType, Map<String, String> theHeaders) {
		validateResultTypeNotSet();
		myResultType = ResultTypeEnum.STATIC_RESOURCE;
		myStaticResourceUrl = theResourceUrl;
		myStaticResourceDefinition = theResource;
		myStaticResourceContentTyoe = theContentType;
		myStaticResourceHeaders = theHeaders;
		myStaticResourceText = theResourceText;
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
