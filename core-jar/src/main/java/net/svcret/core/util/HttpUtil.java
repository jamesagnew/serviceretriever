package net.svcret.core.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletResponse;

import net.svcret.core.api.IServiceRegistry;
import net.svcret.core.api.SrBeanOutgoingResponse;
import net.svcret.core.ex.InvalidRequestException;
import net.svcret.core.ex.InvalidRequestException.IssueEnum;
import net.svcret.core.ex.SecurityFailureException;

public class HttpUtil {
	private static HashSet<String> ourFilterHeaders;

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(HttpUtil.class);

	static {
		ourFilterHeaders = new HashSet<>();
		ourFilterHeaders.add("Transfer-Encoding");
		ourFilterHeaders.add("Content-Length");
		ourFilterHeaders.add("server");
	}

	public static void sendFailure(HttpServletResponse theResp, String theMessage) throws IOException {
		theResp.setStatus(500);
		theResp.setContentType("text/plain");

		try (PrintWriter w = theResp.getWriter()) {
			w.append("HTTP 500 - ServiceRetriever\n\n");
			w.append("Failure: " + theMessage);
		}
	}

	public static void sendSecurityFailure(HttpServletResponse theResp, SecurityFailureException theException) throws IOException {
		if (theException.getRequestNewAuthorizationWithDomain() != null) {
			theResp.setStatus(401);
			theResp.addHeader("WWW-Authenticate", "Basic realm=\"" + theException.getRequestNewAuthorizationWithDomain() + "\"");
		} else {
			theResp.setStatus(403);
		}
		
		theResp.setContentType("text/plain");

		try (PrintWriter w = theResp.getWriter()) {
			w.append("HTTP 403 - Forbidden (Invalid or unknown credentials, or user does not have access to this service)");
		}

	}

	public static void sendThrottleQueueFullFailure(HttpServletResponse theResp) throws IOException {
		theResp.setStatus(429);
		theResp.setContentType("text/plain");

		try (PrintWriter w = theResp.getWriter()) {
			w.append("HTTP 429 - Too Many Requests (This service is subject to rate limiting)");
		}
	}

	public static void sendSuccessfulResponse(HttpServletResponse theHttpResponse, SrBeanOutgoingResponse theServiceResponse) throws IOException {
		theHttpResponse.setStatus(200);

		Map<String, List<String>> responseHeaders = theServiceResponse.getResponseHeaders();
		if (responseHeaders != null) {
			ourLog.trace("Responding with headers: {}", theServiceResponse.getResponseHeaders());
			for (Entry<String, List<String>> next : responseHeaders.entrySet()) {
				if (ourFilterHeaders.contains(next.getKey())) {
					continue;
				}
				for (String nextValue : next.getValue()) {
					theHttpResponse.addHeader(next.getKey(), nextValue);
				}
			}
		}

		theHttpResponse.addHeader("X-PoweredBy", "ServiceRetriever fetches your data!");
		theHttpResponse.setContentType(theServiceResponse.getResponseContentType());
		theHttpResponse.setCharacterEncoding("UTF-8");

		try (PrintWriter w = theHttpResponse.getWriter()) {
			w.append(theServiceResponse.getResponseBody());
		}
	}

	public static void sendInvalidRequest(HttpServletResponse theResp, InvalidRequestException theE, IServiceRegistry theServiceRegistry) throws IOException {

		// Sensible default
		theResp.setStatus(400);

		switch (theE.getIssue()) {
		case INVALID_QUERY_PARAMETERS:
			theResp.setStatus(404);
			break;
		case INVALID_REQUEST_CONTENT_TYPE:
			theResp.setStatus(415);
			break;
		case INVALID_REQUEST_MESSAGE_BODY:
			theResp.setStatus(400);
			break;
		case INVALID_REQUEST_PATH:
			theResp.setStatus(404);
			break;
		case UNKNOWN_METHOD:
			theResp.setStatus(400);
			break;
		case UNSUPPORTED_ACTION:
			theResp.setStatus(405);
			break;
		}

		theResp.setContentType("text/plain");

		try (PrintWriter w = theResp.getWriter()) {
			w.append("HTTP " + theResp.getStatus() + " - ServiceRetriever\n\n");
			w.append("Request failed with error code: " + theE.getIssue().name() + "\n");
			w.append("Argument: " + theE.getArgument() + "\n\n");

			w.append(theE.getMessage());

			if (theE.getIssue() == IssueEnum.INVALID_REQUEST_PATH) {
				w.append("\n\nValid Paths: ");

				List<String> validPaths = new ArrayList<>(theServiceRegistry.getValidPaths());
				Collections.sort(validPaths);
				for (String string : validPaths) {
					w.append("\n * " + string);
				}
			}

		}
	}

}
