package net.svcret.ejb.ejb;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.InputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import net.svcret.ejb.api.HttpResponseBean;
import net.svcret.ejb.api.IResponseValidator;
import net.svcret.ejb.api.UrlPoolBean;
import net.svcret.ejb.ejb.soap.Soap11ResponseValidator;
import net.svcret.ejb.model.entity.PersServiceVersionUrl;
import net.svcret.ejb.util.RandomServerPortProvider;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;

public class HttpClientBeanTest {

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(HttpClientBeanTest.class);

	private HttpClientBean mySvc;

	@After
	public void after() {
		mySvc.cleanUp();
	}
	
	@Before
	public void before() {
		mySvc = new HttpClientBean();
		mySvc.setUp();

	}

	private String provideGoodHeaders() {
		String headers = "Content-Type: text/xml; charset=utf-8\n" + // -
				"SampleHeader: some value"; // -
		return headers;
	}

	private String provideTextResponse() {
		String reqBody = "This is a text response";
		return reqBody;
	}

	private String provideXmlRequest() {
		String reqBody = "<SOAP-ENV:Envelope\n" + // -
				"  xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" + // -
				"  SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" + // -
				"   <SOAP-ENV:Body>\n" + // -
				"       <m:GetLastTradePrice xmlns:m=\"Some-URI\">\n" + // -
				"           <symbol>DIS</symbol>\n" + // -
				"       </m:GetLastTradePrice>\n" + // -
				"   </SOAP-ENV:Body>\n" + // -
				"</SOAP-ENV:Envelope>";
		return reqBody;
	}

	private String provideXmlResponse() {
		String body = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" + "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" + // -
				"  <soap:Body>\n" + // -
				"  <EnlightenResponse xmlns=\"http://clearforest.com/\">\n" + // -
				"  <EnlightenResult>string</EnlightenResult>\n" + // -
				"  </EnlightenResponse>\n" + // -
				"  </soap:Body>\n" + // -
				"</soap:Envelope>";
		return body;
	}

	@Test
	public void testGet() throws Exception {

		int port = RandomServerPortProvider.findFreePort();
		String headers = provideGoodHeaders();
		String body = provideXmlResponse();

		TcpResponder resp = new TcpResponder(port, headers, body);
		resp.start();
		resp.waitToStart();

		HttpResponseBean respBean = mySvc.get("http://localhost:" + port + "/Uri");

		assertEquals(body.trim(), respBean.getBody().trim());
		assertEquals("text/xml", respBean.getContentType());
		assertThat(respBean.getResponseTime(), greaterThan(1L));

		if (resp.myFailed != null) {
			throw new Exception(resp.myFailed);
		}
	}

	@Test
	public void testGetShorterContentType() throws Exception {

		int port = RandomServerPortProvider.findFreePort();
		String headers = "Content-Type: text/xml;charset=utf-8\n" + // -
				"SampleHeader: some value"; // -

		String body = provideXmlResponse();

		TcpResponder resp = new TcpResponder(port, headers, body);
		resp.start();
		resp.waitToStart();

		HttpResponseBean respBean = mySvc.get("http://localhost:" + port + "/Uri");

		assertEquals(body.trim(), respBean.getBody().trim());
		assertEquals("text/xml", respBean.getContentType());
		assertThat(respBean.getResponseTime(), greaterThan(1L));

		if (resp.myFailed != null) {
			throw new Exception(resp.myFailed);
		}
	}

	@Test
	public void testPost() throws InterruptedException {

		int port = RandomServerPortProvider.findFreePort();
		String respHeaders = provideGoodHeaders();
		String respBody = provideXmlResponse();
		String reqBody = provideXmlRequest();

		TcpResponder resp = new TcpResponder(port, respHeaders, respBody);
		resp.start();
		resp.waitToStart();

		IResponseValidator validator = new NullResponseValidator();
		UrlPoolBean urlPool = new UrlPoolBean();
		urlPool.setPreferredUrl(new PersServiceVersionUrl(ourNextPid++, "http://localhost:" + port + "/Uri"));
		urlPool.setConnectTimeoutMillis(1000);
		urlPool.setReadTimeoutMillis(1000);

		Map<String, String> reqHeaders = new HashMap<String, String>();
		String reqContentType = "text/xml";
		HttpResponseBean respBean = mySvc.post(validator, urlPool, reqBody, reqHeaders, reqContentType);

		assertEquals(urlPool.getPreferredUrl(), respBean.getSuccessfulUrl());
		assertEquals(0, respBean.getFailedUrls().size());
		assertEquals(respBody.trim(), respBean.getBody().trim());
		assertEquals("text/xml", respBean.getContentType());
		assertThat(respBean.getResponseTime(), greaterThan(1L));

	}

	@Test
	public void testPostWithValidation() throws InterruptedException {

		int port = RandomServerPortProvider.findFreePort();
		String respHeaders = provideGoodHeaders();
		String respBody = provideXmlResponse();
		String reqBody = provideXmlRequest();

		TcpResponder resp = new TcpResponder(port, respHeaders, respBody);
		resp.start();
		resp.waitToStart();

		IResponseValidator validator = new Soap11ResponseValidator();
		
		UrlPoolBean urlPool = new UrlPoolBean();
		urlPool.setPreferredUrl(new PersServiceVersionUrl(ourNextPid++, "http://localhost:" + port + "/Uri"));
		urlPool.setConnectTimeoutMillis(1000);
		urlPool.setReadTimeoutMillis(1000);

		Map<String, String> reqHeaders = new HashMap<String, String>();
		String reqContentType = "text/xml";
		HttpResponseBean respBean = mySvc.post(validator, urlPool, reqBody, reqHeaders, reqContentType);

		assertEquals(urlPool.getPreferredUrl(), respBean.getSuccessfulUrl());
		assertEquals(0, respBean.getFailedUrls().size());
		assertEquals(respBody.trim(), respBean.getBody().trim());
		assertEquals("text/xml", respBean.getContentType());
		assertThat(respBean.getResponseTime(), greaterThan(1L));

	}

	@Test
	public void testPostOneUrlNoRetry() throws InterruptedException {

		int port = RandomServerPortProvider.findFreePort();
		String respHeaders = provideGoodHeaders();
		String respBodyBad = provideTextResponse();
		String respBodyGood = provideXmlResponse();
		String reqBody = provideXmlRequest();

		TcpResponder resp = new TcpResponder(port, respHeaders, respBodyBad, respBodyGood);
		resp.start();
		resp.waitToStart();

		IResponseValidator validator = new ResponseContainsTextValidator("EnlightenResponse");
		UrlPoolBean urlPool = new UrlPoolBean();
		urlPool.setPreferredUrl(new PersServiceVersionUrl(ourNextPid++, "http://localhost:" + port + "/Uri"));
		urlPool.setConnectTimeoutMillis(1000);
		urlPool.setReadTimeoutMillis(1000);

		Map<String, String> reqHeaders = new HashMap<String, String>();
		String reqContentType = "text/xml";
		HttpResponseBean respBean = mySvc.post(validator, urlPool, reqBody, reqHeaders, reqContentType);

		assertEquals(null, respBean.getSuccessfulUrl());
		assertEquals(1, respBean.getFailedUrls().size());
		assertEquals(urlPool.getPreferredUrl(), respBean.getFailedUrls().keySet().iterator().next());
		assertThat(respBean.getResponseTime(), greaterThan(1L));

	}

	@Test
	public void testPostOneUrlWithOneRetry() throws InterruptedException {

		int port = RandomServerPortProvider.findFreePort();
		String respHeaders = provideGoodHeaders();
		String respBodyBad = provideTextResponse();
		String respBodyGood = provideXmlResponse();
		String reqBody = provideXmlRequest();

		TcpResponder resp = new TcpResponder(port, respHeaders, respBodyBad, respBodyGood);
		resp.start();
		resp.waitToStart();

		IResponseValidator validator = new ResponseContainsTextValidator("EnlightenResponse");
		UrlPoolBean urlPool = new UrlPoolBean();
		urlPool.setFailureRetriesBeforeAborting(1);
		urlPool.setPreferredUrl(new PersServiceVersionUrl(ourNextPid++, "http://localhost:" + port + "/Uri"));
		urlPool.setConnectTimeoutMillis(1000);
		urlPool.setReadTimeoutMillis(1000);

		Map<String, String> reqHeaders = new HashMap<String, String>();
		String reqContentType = "text/xml";
		HttpResponseBean respBean = mySvc.post(validator, urlPool, reqBody, reqHeaders, reqContentType);

		assertEquals(urlPool.getPreferredUrl(), respBean.getSuccessfulUrl());
		assertEquals(0, respBean.getFailedUrls().size());
		assertEquals(respBodyGood.trim(), respBean.getBody().trim());
		assertEquals("text/xml", respBean.getContentType());
		assertThat(respBean.getResponseTime(), greaterThan(1L));

	}

	@Test
	public void testPostTwoUrlsAllFailing() throws InterruptedException {

		int port1 = RandomServerPortProvider.findFreePort();
		int port2 = RandomServerPortProvider.findFreePort();
		
		String respHeaders = provideGoodHeaders();
		String respBodyBad = provideTextResponse();
//		String respBodyGood = provideXmlResponse();
		String reqBody = provideXmlRequest();

		TcpResponder resp1 = new TcpResponder(port1, respHeaders, respBodyBad, respBodyBad);
		TcpResponder resp2 = new TcpResponder(port2, respHeaders, respBodyBad, respBodyBad);
		resp1.start();
		resp2.start();
		resp1.waitToStart();
		resp2.waitToStart();

		IResponseValidator validator = new ResponseContainsTextValidator("EnlightenResponse");
		UrlPoolBean urlPool = new UrlPoolBean();
		urlPool.setFailureRetriesBeforeAborting(1);
		urlPool.setPreferredUrl(new PersServiceVersionUrl(ourNextPid++, "http://localhost:" + port1 + "/Uri"));
		urlPool.setAlternateUrls(new PersServiceVersionUrl(ourNextPid++, "http://localhost:" + port2 + "/Uri"));
		urlPool.setConnectTimeoutMillis(1000);
		urlPool.setReadTimeoutMillis(1000);

		Map<String, String> reqHeaders = new HashMap<String, String>();
		String reqContentType = "text/xml";
		HttpResponseBean respBean = mySvc.post(validator, urlPool, reqBody, reqHeaders, reqContentType);

		assertEquals(null, respBean.getSuccessfulUrl());
		assertEquals(2, respBean.getFailedUrls().size());
		assertThat(respBean.getResponseTime(), greaterThan(1L));

	}

	private static long ourNextPid = 1;
	
	@Test
	public void testPostTwoUrlsSecondPassing() throws InterruptedException {

		int port1 = RandomServerPortProvider.findFreePort();
		int port2 = RandomServerPortProvider.findFreePort();
		
		String respHeaders = provideGoodHeaders();
		String respBodyBad = provideTextResponse();
		String respBodyGood = provideXmlResponse();
		String reqBody = provideXmlRequest();

		TcpResponder resp1 = new TcpResponder(port1, respHeaders, respBodyBad, respBodyBad);
		TcpResponder resp2 = new TcpResponder(port2, respHeaders, respBodyBad, respBodyGood);
		resp1.start();
		resp2.start();
		resp1.waitToStart();
		resp2.waitToStart();

		IResponseValidator validator = new ResponseContainsTextValidator("EnlightenResponse");
		UrlPoolBean urlPool = new UrlPoolBean();
		urlPool.setFailureRetriesBeforeAborting(1);
		urlPool.setPreferredUrl(new PersServiceVersionUrl(ourNextPid++, "http://localhost:" + port1 + "/Uri"));
		urlPool.setAlternateUrls(new PersServiceVersionUrl(ourNextPid++, "http://localhost:" + port2 + "/Uri"));
		urlPool.setConnectTimeoutMillis(1000);
		urlPool.setReadTimeoutMillis(1000);

		Map<String, String> reqHeaders = new HashMap<String, String>();
		String reqContentType = "text/xml";
		HttpResponseBean respBean = mySvc.post(validator, urlPool, reqBody, reqHeaders, reqContentType);

		assertEquals(urlPool.getAlternateUrls().get(0), respBean.getSuccessfulUrl());
		assertEquals(1, respBean.getFailedUrls().size());
		assertThat(respBean.getResponseTime(), greaterThan(1L));
		assertEquals(respBodyGood.trim(), respBean.getBody().trim());
		assertEquals("text/xml", respBean.getContentType());

	}

	@Test
	public void testPostTwoUrlsFirstPassingAfterOneRetry() throws InterruptedException {

		int port1 = RandomServerPortProvider.findFreePort();
		int port2 = RandomServerPortProvider.findFreePort();
		
		String respHeaders = provideGoodHeaders();
		String respBodyBad = provideTextResponse();
		String respBodyGood = provideXmlResponse();
		String reqBody = provideXmlRequest();

		TcpResponder resp1 = new TcpResponder(port1, respHeaders, respBodyBad, respBodyGood);
		TcpResponder resp2 = new TcpResponder(port2, respHeaders, respBodyGood, respBodyGood);
		resp1.start();
		resp2.start();
		resp1.waitToStart();
		resp2.waitToStart();

		IResponseValidator validator = new ResponseContainsTextValidator("EnlightenResponse");
		UrlPoolBean urlPool = new UrlPoolBean();
		urlPool.setFailureRetriesBeforeAborting(1);
		urlPool.setPreferredUrl(new PersServiceVersionUrl(ourNextPid++, "http://localhost:" + port1 + "/Uri"));
		urlPool.setAlternateUrls(new PersServiceVersionUrl(ourNextPid++, "http://localhost:" + port2 + "/Uri"));
		urlPool.setConnectTimeoutMillis(1000);
		urlPool.setReadTimeoutMillis(1000);

		Map<String, String> reqHeaders = new HashMap<String, String>();
		String reqContentType = "text/xml";
		HttpResponseBean respBean = mySvc.post(validator, urlPool, reqBody, reqHeaders, reqContentType);

		assertEquals(urlPool.getPreferredUrl(), respBean.getSuccessfulUrl());
		assertEquals(0, respBean.getFailedUrls().size());
		assertThat(respBean.getResponseTime(), greaterThan(1L));
		assertEquals(respBodyGood.trim(), respBean.getBody().trim());
		assertEquals("text/xml", respBean.getContentType());

	}

	@AfterClass
	public static void afterClass() {
		LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
		lc.getLogger(HttpClientBean.class).setLevel(Level.INFO);
	}

	@BeforeClass
	public static void beforeClass() {
		LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
		lc.getLogger(HttpClientBean.class).setLevel(Level.DEBUG);
	}

	private static class TcpResponder extends Thread {
		private ArrayList<String> myBody;
		private Exception myFailed;
		private String myHeaders;
		private CountDownLatch myLatch = new CountDownLatch(1);
		private int myPort;
		private String myResponse = "HTTP/1.1 200 OK\n";

		public TcpResponder(int thePort, String theHeaders, String... theResponseBodies) {
			setName("TcpResponder-" + thePort);
			myPort = thePort;
			myHeaders = theHeaders;
			myBody = new ArrayList<String>(Arrays.asList(theResponseBodies));
		}

		@Override
		public void run() {

			ServerSocket ss;
			try {

				ourLog.info("Opening socket on port {}", myPort);

				ss = new ServerSocket(myPort);

				ss.setSoTimeout(50);
				try {
					ss.accept();
				} catch (Exception e) {
					// ignore
				}
				myLatch.countDown();

				ss.setSoTimeout(1000);

				do {

					Socket s = ss.accept();
					s.setSoTimeout(500);

					ourLog.info("Got connection on port {}", myPort);

					ByteArrayOutputStream bos = new ByteArrayOutputStream();

					InputStream is = s.getInputStream();
					bos.write(is.read());
					while (is.available() > 0) {
						bos.write(is.read());
					}
					String req = new String(bos.toByteArray(), "UTF-8");

					ourLog.info("TcpResponder received request:\n{}", req);

					PrintWriter w = new PrintWriter(s.getOutputStream());

					w.append(myResponse);
					w.append(myHeaders);

					String nextMessage = myBody.remove(0);
					w.append("Content-Length: " + nextMessage.length());
					w.append("\n\n");
					w.append(nextMessage);
					w.append("\n");

					w.close();
					bos.close();

				} while (myBody.size() > 0);

			} catch (Exception e) {
				e.printStackTrace();
				myFailed = e;
			}

		}


		public void waitToStart() throws InterruptedException {
			ourLog.info("Waiting for responder to start");
			myLatch.await();
			ourLog.info("Responder started");
		}

	}

}
