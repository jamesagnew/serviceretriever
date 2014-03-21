package net.svcret.admin.shared.model;

import java.util.Set;

public class DtoServiceVersionRest extends BaseDtoServiceVersion {

	private static final long serialVersionUID = 1L;

	private Set<String> myAcceptableRequestContentTypes;
	private Set<String> myAcceptableResponseContentTypes;
	private boolean  myRewriteUrls;

	public Set<String> getAcceptableRequestContentTypes() {
		return myAcceptableRequestContentTypes;
	}

	public Set<String> getAcceptableResponseContentTypes() {
		return myAcceptableResponseContentTypes;
	}

	@Override
	public ServiceProtocolEnum getProtocol() {
		return ServiceProtocolEnum.REST;
	}

	public boolean isRewriteUrls() {
		return myRewriteUrls;
	}

	public void setAcceptableRequestContentTypes(Set<String> theSet) {
		myAcceptableRequestContentTypes = theSet;
	}

	public void setAcceptableResponseContentTypes(Set<String> theSet) {
		myAcceptableResponseContentTypes = theSet;
	}

	public void setRewriteUrls(boolean theRewriteUrls) {
		myRewriteUrls = theRewriteUrls;
	}

}
