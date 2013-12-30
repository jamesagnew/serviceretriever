package net.svcret.ejb.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletResponse;

import net.svcret.ejb.api.SrBeanOutgoingResponse;
import net.svcret.ejb.ex.UnknownRequestException;

public class HttpUtil {
	private static HashSet<String> ourFilterHeaders;

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(HttpUtil.class);

	static {
		ourFilterHeaders = new HashSet<String>();
		ourFilterHeaders.add("Transfer-Encoding");
		ourFilterHeaders.add("Content-Length");
		ourFilterHeaders.add("server");
	}

	public static void sendFailure(HttpServletResponse theResp, String theMessage) throws IOException {
		theResp.setStatus(500);
		theResp.setContentType("text/plain");

		PrintWriter w = theResp.getWriter();
		w.append("HTTP 500 - ServiceRetriever\n\n");
		w.append("Failure: " + theMessage);
		w.close();
	}

	public static void sendSecurityFailure(HttpServletResponse theResp) throws IOException {
		theResp.setStatus(403);
		theResp.setContentType("text/plain");

		PrintWriter w = theResp.getWriter();
		w.append("HTTP 403 - Forbidden (Invalid or unknown credentials, or user does not have access to this service)");

		w.close();
	}
	
	public static void sendThrottleQueueFullFailure(HttpServletResponse theResp) throws IOException {
		theResp.setStatus(429);
		theResp.setContentType("text/plain");

		PrintWriter w = theResp.getWriter();
		w.append("HTTP 429 - Too Many Requests (This service is subject to rate limiting)");

		w.close();
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

		theHttpResponse.setContentType(theHttpResponse.getContentType());

		PrintWriter w = theHttpResponse.getWriter();
		w.append(theServiceResponse.getResponseBody());
		w.close();
	}

	public static void sendUnknownLocation(HttpServletResponse theResp, UnknownRequestException theE) throws IOException {
		theResp.setStatus(404);
		theResp.setContentType("text/plain");

		PrintWriter w = theResp.getWriter();
		w.append("HTTP 404 - ServiceRetriever\n\n");
		w.append("Unknown Request Location: ");
		w.append(theE.getPath());

		if (theE.getValidPaths() != null) {
			w.append("\nValid Paths: ");
			w.append(theE.getValidPaths().toString());
		}

		if (theE.getMessage() != null) {
			w.append("\nMessage: ");
			w.append(theE.getMessage());
		}

		w.close();
	}

}
