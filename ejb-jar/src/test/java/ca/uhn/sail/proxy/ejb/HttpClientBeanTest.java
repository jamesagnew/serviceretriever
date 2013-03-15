package ca.uhn.sail.proxy.ejb;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ca.uhn.sail.proxy.api.HttpResponseBean;
import ca.uhn.sail.proxy.ex.HttpFailureException;

public class HttpClientBeanTest {

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(HttpClientBeanTest.class);
	
	private static final int ourPort = 11223;

	private HttpClientBean mySvc;
	@Test
	public void testGet() throws Exception {
		
		String headers = "HTTP/1.1 200 OK\n" + 
				"Content-Type: text/xml; charset=utf-8\n" +
				"SampleHeader: some value";
		String body = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" + 
				"<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" + 
				"  <soap:Body>\n" + 
				"  <EnlightenResponse xmlns=\"http://clearforest.com/\">\n" + 
				"  <EnlightenResult>string</EnlightenResult>\n" + 
				"  </EnlightenResponse>\n" + 
				"  </soap:Body>\n" + 
				"</soap:Envelope>";
		TcpResponder resp = new TcpResponder(headers, body);
		resp.start();
		
		HttpResponseBean respBean = mySvc.get("http://localhost:" + ourPort + "/Uri");
		
		assertEquals(body.trim(), respBean.getBody().trim());
		assertEquals("text/xml", respBean.getContentType());
		assertThat(respBean.getResponseTime(), greaterThan(1L));
		
		if (resp.myFailed != null) {
			throw new Exception(resp.myFailed);
		}
	}
	
	@After
	public void after() {
		mySvc.cleanUp();
	}
	
	@Before
	public void before() {
		mySvc = new HttpClientBean();
		mySvc.setUp();

	}
	
	private static class TcpResponder extends Thread
	{
		private String myHeaders;
		private String myBody;
		private Exception myFailed;

		public TcpResponder(String theHeaders, String theBody) {
			setName("TcpResponder");
			myHeaders = theHeaders;
			myBody = theBody;
		}

		@Override
		public void run() {
			
			ServerSocket ss;
			try {
				
				ourLog.info("Opening socket on port {}", ourPort);
				
				ss = new ServerSocket(ourPort);
				ss.setSoTimeout(1000);
				
				Socket s = ss.accept();
				s.setSoTimeout(500);
				
				ourLog.info("Got connection on port {}", ourPort);

				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				
				InputStream is = s.getInputStream();
				bos.write(is.read());
				while (is.available() > 0) {
					bos.write(is.read());
				}
				String req = new String(bos.toByteArray(), "UTF-8");
				
				ourLog.info("TcpResponder received request:\n{}", req);
				
				PrintWriter w = new PrintWriter(s.getOutputStream());
				w.append(myHeaders);
				w.append("Content-Length: " + myBody.length());
				w.append("\n\n");
				w.append(myBody);
				w.append("\n");
				w.close();
				
				bos.close();
				
			} catch (Exception e) {
				e.printStackTrace();
				myFailed = e;
			}
			
		}
		
		
		
	}
	
}
