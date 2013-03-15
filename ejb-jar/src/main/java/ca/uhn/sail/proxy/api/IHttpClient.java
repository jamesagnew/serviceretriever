package ca.uhn.sail.proxy.api;

import java.util.Map;

import javax.ejb.Local;

import ca.uhn.sail.proxy.ex.HttpFailureException;

@Local
public interface IHttpClient {

	HttpResponseBean get(String theUrl);

	HttpResponseBean post(IResponseValidator theResponseValidator, UrlPoolBean theUrlPool, String theContentBody, Map<String, String> theHeaders, String theContentType);
	
}
