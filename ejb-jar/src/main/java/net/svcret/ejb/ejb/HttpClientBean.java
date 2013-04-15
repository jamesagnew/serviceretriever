package net.svcret.ejb.ejb;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.ejb.PrePassivate;
import javax.ejb.Stateless;

import net.svcret.ejb.Messages;
import net.svcret.ejb.api.HttpResponseBean;
import net.svcret.ejb.api.IHttpClient;
import net.svcret.ejb.api.IResponseValidator;
import net.svcret.ejb.api.IResponseValidator.ValidationResponse;
import net.svcret.ejb.api.UrlPoolBean;
import net.svcret.ejb.util.Validate;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;

@Stateless
public class HttpClientBean implements IHttpClient {

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(HttpClientBean.class);

	private PoolingClientConnectionManager myConMgr;
	private DefaultHttpClient myDefaultClient;
	private Charset ourDefaultCharset = Charset.forName("UTF-8");

	public HttpClientBean() {
	}

	@PrePassivate
	public void cleanUp() {
		ourLog.info("Shuting down HttpClient");
		myConMgr.shutdown();
		myConMgr = null;
	}

	@Override
	public HttpResponseBean get(String theUrl) {
		Validate.throwIllegalArgumentExceptionIfBlank("URL", theUrl);
		ourLog.debug("Requesting URL: {}", theUrl);

		HttpUriRequest httpReq = new HttpGet(theUrl);

		HttpEntity entity = null;
		HttpResponseBean retVal = new HttpResponseBean();
		try {
			long start = System.currentTimeMillis();
			HttpResponse httpResp = myDefaultClient.execute(httpReq);
			entity = httpResp.getEntity();

			retVal.setBody(IOUtils.toString(entity.getContent()));
			retVal.setCode(httpResp.getStatusLine().getStatusCode());
			retVal.setContentType(httpResp.getEntity().getContentType().getValue());
			retVal.setHeaders(toHeaderMap(httpResp.getAllHeaders()));
			retVal.setSuccessfulUrl(theUrl);

			long delay = System.currentTimeMillis() - start;
			retVal.setResponseTime(delay);

			ourLog.debug("Done requesting URL \"{}\" in {}ms", theUrl, delay);
			return retVal;

		} catch (ClientProtocolException e) {

			ourLog.debug("Exception while invoking remote service", e);
			retVal.addFailedUrl(theUrl, Messages.getString("HttpClientBean.postClientProtocolException", e.getMessage()), 0, null, null);
			return retVal;

		} catch (IOException e) {

			ourLog.debug("Exception while invoking remote service", e);
			retVal.addFailedUrl(theUrl, Messages.getString("HttpClientBean.postIoException", e.getMessage()), 0, null, null);
			return retVal;

		} finally {
			if (entity != null) {
				try {
					entity.getContent().close();
				} catch (IllegalStateException e) {
					ourLog.debug("Error closing input stream: ", e);
				} catch (IOException e) {
					ourLog.debug("Error closing input stream: ", e);
				}
			}
		}

	}

	private Map<String, String> toHeaderMap(Header[] theAllHeaders) {
		HashMap<String, String> retVal = new HashMap<String, String>();
		for (Header header : theAllHeaders) {
			retVal.put(header.getName(), header.getValue());
		}
		return retVal;
	}

	@Override
	public HttpResponseBean post(IResponseValidator theResponseValidator, UrlPoolBean theUrlPool, String theContentBody, Map<String, String> theHeaders, String theContentType) {
		if (theUrlPool.getConnectTimeoutMillis() <= 0) {
			throw new IllegalArgumentException("ConnectTimeout may not be <= 0");
		}
		if (theUrlPool.getConnectTimeoutMillis() > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("ConnectTimeout may not be > MAX_INT");
		}
		if (theUrlPool.getReadTimeoutMillis() <= 0) {
			throw new IllegalArgumentException("ReadTimeout may not be <= 0");
		}
		if (theUrlPool.getReadTimeoutMillis() > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("ReadTimeout may not be > MAX_INT");
		}

		HttpParams params = new BasicHttpParams();
		params.setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, theUrlPool.getConnectTimeoutMillis());
		params.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, theUrlPool.getReadTimeoutMillis());
		params.setBooleanParameter(CoreConnectionPNames.STALE_CONNECTION_CHECK, false);

		ContentType contentType = ContentType.create(theContentType, ourDefaultCharset);
		HttpEntity postEntity = new StringEntity(theContentBody, contentType);

		DefaultHttpClient client = new DefaultHttpClient(myConMgr, params);
		HttpResponseBean retVal = new HttpResponseBean();

		String url = theUrlPool.getPreferredUrl();
		int failureRetries = theUrlPool.getFailureRetriesBeforeAborting();

		doPost(retVal, theResponseValidator, theHeaders, postEntity, client, url, failureRetries);

		if (retVal.getSuccessfulUrl() == null) {
			for (String nextUrl : theUrlPool.getAlternateUrls()) {
				doPost(retVal, theResponseValidator, theHeaders, postEntity, client, nextUrl, failureRetries);
				if (retVal.getSuccessfulUrl() != null) {
					break;
				}
			}
		}

		return retVal;
	}

	private void doPost(HttpResponseBean theResponse, IResponseValidator theResponseValidator, Map<String, String> theHeaders, HttpEntity postEntity, DefaultHttpClient client, String url, int theFailureRetries) {
		int failuresRemaining = theFailureRetries + 1;
		for (;;) {
			failuresRemaining--;
			
			HttpPost post = new HttpPost(url);
			post.setEntity(postEntity);
			for (Entry<String, String> next : theHeaders.entrySet()) {
				post.addHeader(next.getKey(), next.getValue());
			}

			long start = System.currentTimeMillis();
			HttpEntity entity = null;
			try {
				HttpResponse resp = client.execute(post);
				long delay = System.currentTimeMillis() - start;

				entity = resp.getEntity();
				String body = IOUtils.toString(entity.getContent());
				int statusCode = resp.getStatusLine().getStatusCode();
				String contentType = resp.getEntity().getContentType().getValue();
				
				int sep = contentType.indexOf(';');
				if (sep > -1) {
					contentType = contentType.substring(0, sep);
				}
				
				Map<String, String> headerMap = toHeaderMap(resp.getAllHeaders());

				ValidationResponse validates = theResponseValidator.validate(body, statusCode, contentType);
				if (validates.isValidates() == false) {

					if (failuresRemaining > 0) {
						ourLog.debug("Failed to invoke service at URL[{}] with {} retries remaining: {}", new Object[] {url, failuresRemaining, validates.getFailureExplanation()});
						continue;
					}
					ourLog.debug("Failed to invoke service at URL[{}]: {}", url, validates.getFailureExplanation());
					theResponse.addFailedUrl(url, validates.getFailureExplanation(), statusCode, contentType, body);
					theResponse.setResponseTime(delay);
					return;

				} else {

					theResponse.setBody(body);
					theResponse.setCode(statusCode);
					theResponse.setContentType(contentType);
					theResponse.setHeaders(headerMap);
					theResponse.setResponseTime(delay);
					theResponse.setSuccessfulUrl(url);
					return;

				}

			} catch (ClientProtocolException e) {
				if (failuresRemaining > 0) {
					ourLog.info("Failed to invoke service at URL[{}] with {} retries remaining: {}", new Object[] {url, failuresRemaining, e.toString()});
					continue;
				}
				ourLog.debug("Exception while invoking remote service", e);
				theResponse.addFailedUrl(url, Messages.getString("HttpClientBean.postClientProtocolException", e.getMessage()), 0, null, null);
				theResponse.setResponseTime(System.currentTimeMillis() - start);
			} catch (IOException e) {
				if (failuresRemaining > 0) {
					ourLog.info("Failed to invoke service at URL[{}] with {} retries remaining: {}", new Object[] {url, failuresRemaining, e.toString()});
					continue;
				}
				ourLog.debug("Exception while invoking remote service", e);
				theResponse.addFailedUrl(url, Messages.getString("HttpClientBean.postIoException", e.getMessage()), 0, null, null);
				theResponse.setResponseTime(System.currentTimeMillis() - start);
			} finally {
				if (entity != null) {
					try {
						entity.getContent().close();
					} catch (IllegalStateException e) {
						ourLog.debug("Error closing input stream: ", e);
					} catch (IOException e) {
						ourLog.debug("Error closing input stream: ", e);
					}
				}
			}
		}

	}

	@PostConstruct
	public void setUp() {
		ourLog.info("Starting new HttpClient instance");
		myConMgr = new PoolingClientConnectionManager();

		HttpParams params = new BasicHttpParams();
		params.setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 10 * 1000);
		params.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 10 * 1000);
		params.setBooleanParameter(CoreConnectionPNames.STALE_CONNECTION_CHECK, false);

		myDefaultClient = new DefaultHttpClient(myConMgr, params);
	}

}
