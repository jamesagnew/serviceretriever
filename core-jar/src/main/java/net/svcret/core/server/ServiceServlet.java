package net.svcret.core.server;

import static net.svcret.core.util.HttpUtil.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLSession;
import javax.servlet.AsyncContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.svcret.core.api.IServiceOrchestrator;
import net.svcret.core.api.IServiceRegistry;
import net.svcret.core.api.RequestType;
import net.svcret.core.api.SrBeanIncomingRequest;
import net.svcret.core.api.SrBeanOutgoingResponse;
import net.svcret.core.ex.InvalidRequestException;
import net.svcret.core.ex.InvocationFailedDueToInternalErrorException;
import net.svcret.core.ex.InvocationRequestOrResponseFailedException;
import net.svcret.core.ex.SecurityFailureException;
import net.svcret.core.throttle.ThrottleException;
import net.svcret.core.throttle.ThrottleQueueFullException;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Iterators;

class ServiceServlet extends HttpServlet {

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(ServiceServlet.class);
	private static final long serialVersionUID = 1L;

	private IServiceOrchestrator myServiceOrchestrator;
	private IServiceRegistry myServiceRegistry;
	private int myPort;

	@Override
	public void init(ServletConfig theConfig) {
		ourLog.debug("Starting servlet with path: " + theConfig.getServletContext().getContextPath());
		ourLog.debug("Real path: " + theConfig.getServletContext().getRealPath("/"));
	}

	public void setServiceOrchestrator(IServiceOrchestrator theServiceOrchestrator) {
		myServiceOrchestrator = theServiceOrchestrator;
	}
	
	public void setServiceRegistry(IServiceRegistry theServiceRegistry) {
		myServiceRegistry = theServiceRegistry;
	}

	private void doHandle(HttpServletRequest theReq, HttpServletResponse theResp, RequestType requestAction) throws IOException {
		long start = System.currentTimeMillis();

		String requestURI = theReq.getRequestURI();
		String contextPath = theReq.getContextPath();
		contextPath = StringUtils.defaultIfBlank(contextPath, "");
		String path = requestURI.substring(contextPath.length());
		try {
			path = new URLCodec().decode(path);
		} catch (DecoderException e1) {
			ourLog.error("Failed to decode path", e1);
			sendFailure(theResp, "Failed to decode path: " + e1.getMessage());
		}

		String query = "?" + theReq.getQueryString();
		String requestURL = theReq.getRequestURL().toString();
		String requestHostIp = theReq.getRemoteAddr();
		String protocol = theReq.getProtocol();
		String base = extractBase(contextPath, requestURL);

		ourLog.debug("New {} request at path[{}] and base[{}] and context path[{}]", new Object[] { requestAction.name(), path, base, contextPath });

		SrBeanOutgoingResponse response;
		try {
			SrBeanIncomingRequest request = new SrBeanIncomingRequest();
			request.setRequestType(requestAction);
			request.setRequestHostIp(StringUtils.defaultString(requestHostIp, "UNKNOWN"));
			request.setPath(path);
			request.setRequestFullUri(requestURI);
			request.setQuery(query);
			request.setBase(base);
			request.setPort(myPort);
			request.setProtocol(protocol);
			request.setContextPath(contextPath);
			request.setInputReader(theReq.getReader());
			request.setRequestTime(new Date(start));
			request.setTlsClientCertificates((X509Certificate[]) theReq.getAttribute("javax.servlet.request.X509Certificate"));
			request.setTlsCipherSuite((String) theReq.getAttribute("javax.servlet.request.cipher_suite"));
			request.setTlsSession((SSLSession) theReq.getAttribute("javax.servlet.request.cipher_suite"));
			request.setTlsKeySize((Integer) theReq.getAttribute("javax.servlet.request.key_size"));

			Map<String, List<String>> requestHeaders = new HashMap<>();
			for (Iterator<String> nameIter = Iterators.forEnumeration(theReq.getHeaderNames()); nameIter.hasNext();) {
				String nextName = nameIter.next();
				ArrayList<String> values = Collections.list(theReq.getHeaders(nextName));
				requestHeaders.put(nextName, values);
			}
			request.setRequestHeaders(requestHeaders);

			response = myServiceOrchestrator.handleServiceRequest(request);
		} catch (InvalidRequestException e) {
			ourLog.info("Invalid Request Detected ({}) : {} - Message {}", new Object[] {e.getIssue().name(), e.getArgument(), e.getMessage()});
			sendInvalidRequest(theResp, e, myServiceRegistry);
			return;
		} catch (InvocationRequestOrResponseFailedException e) {
			ourLog.info("Processing Failure", e.getMessage());
			sendFailure(theResp, e.toString());
			return;
		} catch (InvocationFailedDueToInternalErrorException e) {
			ourLog.error("Processing Failure", e);
			sendFailure(theResp, e.toString());
			return;
		} catch (SecurityFailureException e) {
			ourLog.info("Security Failure accessing URL: {}", theReq.getRequestURL());
			sendSecurityFailure(theResp);
			return;
		} catch (ThrottleException e) {

			AsyncContext asyncContext = theReq.startAsync();

			try {
				myServiceOrchestrator.enqueueThrottledRequest(e, asyncContext);
			} catch (ThrottleQueueFullException e1) {
				ourLog.info("Request was throttled and queue was full for URL: {}", theReq.getRequestURL());
				sendThrottleQueueFullFailure(theResp);
			}
			return;

		} catch (ThrottleQueueFullException e) {
			ourLog.info("Request was throttled and queue was full for URL: {}", theReq.getRequestURL());
			sendThrottleQueueFullFailure(theResp);
			return;
		} catch (Throwable e) {
			ourLog.info("Processing Failure", e);
			sendFailure(theResp, e.getMessage());
			return;
		}

		sendSuccessfulResponse(theResp, response);

		long delay = System.currentTimeMillis() - start;
		ourLog.info("Handled {} request at path[{}] with {} byte response in {} ms", new Object[] { requestAction.name(), path, response.getResponseBody().length(), delay });
	}

	private void handle(HttpServletRequest theReq, HttpServletResponse theResp, RequestType get) throws IOException, ServletException {
		try {
			doHandle(theReq, theResp, get);
		} catch (IOException e) {
			throw e;
		} catch (Throwable e) {
			ourLog.error("Failed to process service request", e);
			throw new ServletException(e);
		}
	}

	@Override
	protected void doGet(HttpServletRequest theReq, HttpServletResponse theResp) throws ServletException, IOException {
		handle(theReq, theResp, RequestType.GET);
	}

	@Override
	protected void doPost(HttpServletRequest theReq, HttpServletResponse theResp) throws ServletException, IOException {
		handle(theReq, theResp, RequestType.POST);
	}

	private static String extractBase(String contextPath, String requestURL) {
		int hostIndex = requestURL.indexOf("//") + 2;
		int pathStart = requestURL.indexOf('/', hostIndex);
		return requestURL.substring(0, pathStart) + contextPath;
	}

	public void setPort(int thePort) {
		myPort=thePort;
	}

}
