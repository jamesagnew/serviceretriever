package net.svcret.ejb.api;

import java.util.Map;

import javax.ejb.Local;


@Local
public interface IHttpClient {

	HttpResponseBean get(String theUrl);

	HttpResponseBean post(IResponseValidator theResponseValidator, UrlPoolBean theUrlPool, String theContentBody, Map<String, String> theHeaders, String theContentType);
	
}
