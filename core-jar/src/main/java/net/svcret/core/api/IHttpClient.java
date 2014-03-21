package net.svcret.core.api;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import net.svcret.core.ejb.HttpClientBean.ClientConfigException;
import net.svcret.core.model.entity.PersHttpClientConfig;

import org.apache.http.client.ClientProtocolException;


public interface IHttpClient {

	SrBeanIncomingResponse get(String theUrl) throws ClientProtocolException, IOException;

	SrBeanIncomingResponse getOneTime(PersHttpClientConfig theHttpClientConfig, String theUrl) throws ClientProtocolException, IOException, ClientConfigException;

	SrBeanIncomingResponse post(PersHttpClientConfig theClientConfig, IResponseValidator theResponseValidator, UrlPoolBean theUrlPool, String theContentBody, Map<String, List<String>> theHeaders, String theContentType);

	SrBeanIncomingResponse post(PersHttpClientConfig theClientConfig, IResponseValidator theResponseValidator, UrlPoolBean theUrlPool, String theContentBody, Map<String, List<String>> theHeaders,
			String theContentType, String theUrlSuffix);
	
}
