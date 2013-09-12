package net.svcret.ejb.ejb;

import java.io.IOException;
import java.nio.charset.Charset;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.EJB;
import javax.ejb.PrePassivate;
import javax.ejb.Singleton;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import net.svcret.ejb.Messages;
import net.svcret.ejb.api.HttpResponseBean;
import net.svcret.ejb.api.IHttpClient;
import net.svcret.ejb.api.IKeystoreService;
import net.svcret.ejb.api.IResponseValidator;
import net.svcret.ejb.api.IResponseValidator.ValidationResponse;
import net.svcret.ejb.api.UrlPoolBean;
import net.svcret.ejb.ex.ProcessingException;
import net.svcret.ejb.model.entity.PersHttpClientConfig;
import net.svcret.ejb.model.entity.PersServiceVersionUrl;
import net.svcret.ejb.util.Validate;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.scheme.SchemeSocketFactory;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.impl.conn.SchemeRegistryFactory;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;

import com.google.common.annotations.VisibleForTesting;

@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class HttpClientBean implements IHttpClient {

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(HttpClientBean.class);

	private ConcurrentHashMap<Long, HttpClientImpl> myClientConfigPidToClient = new ConcurrentHashMap<Long, HttpClientBean.HttpClientImpl>();
	private DefaultHttpClient myDefaultSimpleGetClient;
	@EJB
	private IKeystoreService myKeystoreService;
	private PoolingClientConnectionManager mySimpleClientConMgr;
	private Charset ourDefaultCharset = Charset.forName("UTF-8");

	public HttpClientBean() {
	}

	@PrePassivate
	public void cleanUp() {
		ourLog.info("Shuting down HttpClient");

		for (HttpClientImpl next : myClientConfigPidToClient.values()) {
			next.getConnectionManager().shutdown();
		}
		myClientConfigPidToClient.clear();

		mySimpleClientConMgr.shutdown();
		mySimpleClientConMgr = null;
	}

	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	@Override
	public HttpResponseBean get(String theUrl) throws ClientProtocolException, IOException {
		Validate.notBlank(theUrl, "URL");
		ourLog.debug("Requesting URL: {}", theUrl);

		HttpUriRequest httpReq = new HttpGet(theUrl);

		HttpEntity entity = null;
		HttpResponseBean retVal = new HttpResponseBean();
		try {
			long start = System.currentTimeMillis();
			HttpResponse httpResp = myDefaultSimpleGetClient.execute(httpReq);
			entity = httpResp.getEntity();

			retVal.setBody(IOUtils.toString(entity.getContent()));
			retVal.setCode(httpResp.getStatusLine().getStatusCode());
			retVal.setContentType(httpResp.getEntity().getContentType().getValue());
			retVal.setHeaders(toHeaderMap(httpResp.getAllHeaders()));

			long delay = System.currentTimeMillis() - start;
			retVal.setResponseTime(delay);

			ourLog.debug("Done requesting URL \"{}\" in {}ms", theUrl, delay);
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

	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	@Override
	public HttpResponseBean post(PersHttpClientConfig theClientConfig, IResponseValidator theResponseValidator, UrlPoolBean theUrlPool, String theContentBody, Map<String, List<String>> theHeaders,
			String theContentType) {
		if (theClientConfig.getConnectTimeoutMillis() <= 0) {
			throw new IllegalArgumentException("ConnectTimeout may not be <= 0");
		}
		if (theClientConfig.getConnectTimeoutMillis() > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("ConnectTimeout may not be > MAX_INT");
		}
		if (theClientConfig.getReadTimeoutMillis() <= 0) {
			throw new IllegalArgumentException("ReadTimeout may not be <= 0");
		}
		if (theClientConfig.getReadTimeoutMillis() > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("ReadTimeout may not be > MAX_INT");
		}
		Validate.notNull(theClientConfig.getPid());

		HttpClientImpl client = myClientConfigPidToClient.get(theClientConfig.getPid());
		if (client == null || client.getClientConfig().getOptLock() != theClientConfig.getOptLock()) {
			ourLog.info("Creating a new HTTP client instance for config PID {} (has version {})", theClientConfig.getPid(), theClientConfig.getOptLock());
			try {
				client = new HttpClientImpl(theClientConfig);
			} catch (ClientConfigException e) {
				ourLog.error("Failed to initialize HTTP client", e);
				HttpResponseBean retVal = new HttpResponseBean();
				retVal.addFailedUrl(theUrlPool.getPreferredUrl(), "ServiceRetriever failed to initialize HTTP client, problem was: " + e.getMessage(), 0, "", "", 0);
				return retVal;
			}
			myClientConfigPidToClient.put(theClientConfig.getPid(), client);
		}

		return client.post(theClientConfig, theResponseValidator, theUrlPool, theContentBody, theHeaders, theContentType);
	}

	@PostConstruct
	public void setUp() throws Exception {
		ourLog.info("Starting new HttpClient instance");

		HttpParams params = new BasicHttpParams();
		params.setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 10 * 1000);
		params.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 10 * 1000);
		params.setBooleanParameter(CoreConnectionPNames.STALE_CONNECTION_CHECK, true);

		mySimpleClientConMgr = new PoolingClientConnectionManager(SchemeRegistryFactory.createDefault(), 5000, TimeUnit.MILLISECONDS);
		SchemeRegistry sr = mySimpleClientConMgr.getSchemeRegistry();
		SchemeSocketFactory ssf = new SSLSocketFactory(new TrustStrategy() {
			@Override
			public boolean isTrusted(X509Certificate[] theChain, String theAuthType) throws CertificateException {
				return true;
			}
		});
		sr.register(new Scheme("https", 443, ssf));

		myDefaultSimpleGetClient = new DefaultHttpClient(mySimpleClientConMgr, params);
	}

	private void doPost(HttpResponseBean theResponse, IResponseValidator theResponseValidator, Map<String, List<String>> theHeaders, HttpEntity postEntity, DefaultHttpClient client,
			PersServiceVersionUrl theNextUrl, int theFailureRetries) {
		int failuresRemaining = theFailureRetries + 1;
		for (;;) {
			
			failuresRemaining--;
			
			HttpPost post = new HttpPost(theNextUrl.getUrl());
			post.setEntity(postEntity);
			if (theHeaders != null) {
				for (Entry<String, List<String>> next : theHeaders.entrySet()) {
					if (next != null && next.getValue() != null) {
						for (String nextValue : next.getValue()) {
							post.addHeader(next.getKey(), nextValue);
						}
					}
				}
			}

			long start = System.currentTimeMillis();
			HttpEntity entity = null;
			long delay = 0;
			try {
				HttpResponse resp = client.execute(post);
				delay = System.currentTimeMillis() - start;

				entity = resp.getEntity();
				String body = IOUtils.toString(entity.getContent());
				int statusCode = resp.getStatusLine().getStatusCode();
				String contentType;
				contentType = resp.getEntity() != null && resp.getEntity().getContentType() != null ? resp.getEntity().getContentType().getValue() : "";

				int sep = contentType.indexOf(';');
				if (sep > -1) {
					contentType = contentType.substring(0, sep);
				}

				Map<String, List<String>> headerMap = toHeaderMap(resp.getAllHeaders());

				ValidationResponse validates = theResponseValidator.validate(body, statusCode, contentType);
				if (validates.isValidates() == false) {

					if (failuresRemaining > 0) {
						ourLog.debug("Failed to invoke service at URL[{}] with {} retries remaining: {}", new Object[] { theNextUrl, failuresRemaining, validates.getFailureExplanation() });
						continue;
					}
					ourLog.debug("Failed to invoke service at URL[{}]: {}", theNextUrl, validates.getFailureExplanation());
					theResponse.addFailedUrl(theNextUrl, validates.getFailureExplanation(), statusCode, contentType, body, delay);
					theResponse.setResponseTime(delay);
					return;

				} else {

					theResponse.setBody(body);
					theResponse.setCode(statusCode);
					theResponse.setContentType(contentType);
					theResponse.setHeaders(headerMap);
					theResponse.setResponseTime(delay);
					theResponse.setSuccessfulUrl(theNextUrl);
					return;

				}

			} catch (ClientProtocolException e) {
				if (failuresRemaining > 0) {
					ourLog.info("Failed to invoke service at URL[{}] with {} retries remaining: {}", new Object[] { theNextUrl, failuresRemaining, e.toString() });
					continue;
				}
				ourLog.debug("Exception while invoking remote service", e);
				if (delay == 0) {
					delay = System.currentTimeMillis() - start;
				}
				theResponse.addFailedUrl(theNextUrl, Messages.getString("HttpClientBean.postClientProtocolException", e.toString()), 0, null, null, delay);
				theResponse.setResponseTime(System.currentTimeMillis() - start);
				return;
			} catch (Exception e) {
				if (failuresRemaining > 0) {
					ourLog.info("Failed to invoke service at URL[{}] with {} retries remaining: {}", new Object[] { theNextUrl, failuresRemaining, e.toString() });
					continue;
				}
				ourLog.debug("Exception while invoking remote service", e);
				if (delay == 0) {
					delay = System.currentTimeMillis() - start;
				}
				theResponse.addFailedUrl(theNextUrl, Messages.getString("HttpClientBean.postIoException", e.toString()), 0, null, null, delay);
				theResponse.setResponseTime(System.currentTimeMillis() - start);
				return;
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

	private Map<String, List<String>> toHeaderMap(Header[] theAllHeaders) {
		HashMap<String, List<String>> retVal = new HashMap<String, List<String>>();
		for (Header header : theAllHeaders) {
			List<String> list = retVal.get(header.getName());
			if (list == null) {
				list = new ArrayList<String>(2);
				retVal.put(header.getName(), list);
			}
			list.add(header.getValue());
		}
		return retVal;
	}

	public class HttpClientImpl {

		private PersHttpClientConfig myClientConfig;
		private PoolingClientConnectionManager myConMgr;

		public HttpClientImpl(PersHttpClientConfig theClientConfig) throws ClientConfigException {
			myClientConfig = theClientConfig;
			myConMgr = new PoolingClientConnectionManager(SchemeRegistryFactory.createDefault(), 5000, TimeUnit.MILLISECONDS);

			String algorithm = "TLS";
			KeyStore keystore = null;
			if (myClientConfig.getTlsKeystore() != null) {
				try {
					keystore = myKeystoreService.loadKeystore(myClientConfig.getTlsKeystore(), myClientConfig.getTlsKeystorePassword());
				} catch (ProcessingException e) {
					throw new ClientConfigException("Failed to initialize keystore", e);
				}
			}
			String keystorePassword = myClientConfig.getTlsKeystorePassword();

			KeyStore truststore = null;

			if (myClientConfig.getTlsTruststore() != null) {
				try {
					truststore = myKeystoreService.loadKeystore(myClientConfig.getTlsTruststore(), myClientConfig.getTlsTruststorePassword());
				} catch (ProcessingException e) {
					throw new ClientConfigException("Failed to initialize truststore", e);
				}
			}

			SecureRandom random = null;
			TrustStrategy trustStrategy = null;
			X509HostnameVerifier hostnameVerifier = null;
			SSLSocketFactory ssf;
			try {
				ssf = new SSLSocketFactory(algorithm, keystore, keystorePassword, truststore, random, trustStrategy, hostnameVerifier);
			} catch (Exception e) {
				throw new ClientConfigException("Failed to initialize SSL/TLS context, check keystore and truststore settings for this client config", e);
			}

			SchemeRegistry sr = myConMgr.getSchemeRegistry();
			sr.register(new Scheme("https", 443, ssf));

		}

		public PersHttpClientConfig getClientConfig() {
			return myClientConfig;
		}

		public PoolingClientConnectionManager getConnectionManager() {
			return myConMgr;
		}

		public HttpResponseBean post(PersHttpClientConfig theClientConfig, IResponseValidator theResponseValidator, UrlPoolBean theUrlPool, String theContentBody,
				Map<String, List<String>> theHeaders, String theContentType) {
			HttpParams params = new BasicHttpParams();
			params.setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, theClientConfig.getConnectTimeoutMillis());
			params.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, theClientConfig.getReadTimeoutMillis());
			params.setBooleanParameter(CoreConnectionPNames.STALE_CONNECTION_CHECK, true);
			params.setBooleanParameter(CoreConnectionPNames.SO_KEEPALIVE, true);

			ContentType contentType = ContentType.create(theContentType, ourDefaultCharset);
			HttpEntity postEntity = new StringEntity(theContentBody, contentType);

			DefaultHttpClient client = new DefaultHttpClient(myConMgr, params);
			HttpResponseBean retVal = new HttpResponseBean();

			PersServiceVersionUrl url = theUrlPool.getPreferredUrl();
			int failureRetries = theClientConfig.getFailureRetriesBeforeAborting();

			doPost(retVal, theResponseValidator, theHeaders, postEntity, client, url, failureRetries);

			if (retVal.getSuccessfulUrl() == null) {
				for (PersServiceVersionUrl nextUrl : theUrlPool.getAlternateUrls()) {
					doPost(retVal, theResponseValidator, theHeaders, postEntity, client, nextUrl, failureRetries);
					if (retVal.getSuccessfulUrl() != null) {
						break;
					}
				}
			}

			return retVal;
		}

	}

	public static class ClientConfigException extends Exception {

		public ClientConfigException(String theMessage, Exception theE) {
			super(theMessage, theE);
		}

		private static final long serialVersionUID = 5053765957107834824L;

	}

	@VisibleForTesting
	void setKeystoreServiceForUnitTest(KeystoreServiceBean theKss) {
		myKeystoreService = theKss;
	}

}
