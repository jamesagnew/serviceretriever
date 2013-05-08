package net.svcret.ejb.api;

import java.io.IOException;
import java.util.Map;

import javax.ejb.Local;

import org.apache.http.client.ClientProtocolException;


@Local
public interface IHttpClient {

	HttpResponseBean get(String theUrl) throws ClientProtocolException, IOException;

	HttpResponseBean post(IResponseValidator theResponseValidator, UrlPoolBean theUrlPool, String theContentBody, Map<String, String> theHeaders, String theContentType);
	
}
