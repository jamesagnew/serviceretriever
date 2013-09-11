package net.svcret.ejb.api;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.ejb.Local;

import net.svcret.ejb.model.entity.PersHttpClientConfig;

import org.apache.http.client.ClientProtocolException;


@Local
public interface IHttpClient {

	HttpResponseBean get(String theUrl) throws ClientProtocolException, IOException;

	HttpResponseBean post(PersHttpClientConfig theClientConfig, IResponseValidator theResponseValidator, UrlPoolBean theUrlPool, String theContentBody, Map<String, List<String>> theHeaders, String theContentType);
	
}
