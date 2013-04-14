package ca.uhn.sail.proxy.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.Map.Entry;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.net.URLCodec;

import net.svcret.ejb.api.IServiceOrchestrator;
import net.svcret.ejb.api.IServiceOrchestrator.OrchestratorResponseBean;
import net.svcret.ejb.api.RequestType;
import net.svcret.ejb.ex.InternalErrorException;
import net.svcret.ejb.ex.ProcessingException;
import net.svcret.ejb.ex.UnknownRequestException;

@WebServlet(asyncSupported = true, loadOnStartup = 1, urlPatterns = { "/" })
public class ServiceServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(ServiceServlet.class);

	@EJB
	private IServiceOrchestrator myOrch;

	@Override
	protected void doGet(HttpServletRequest theReq, HttpServletResponse theResp) throws ServletException, IOException {
		handle(theReq, theResp, RequestType.GET);
	}

	private void handle(HttpServletRequest theReq, HttpServletResponse theResp, RequestType get) throws IOException {
		String path = theReq.getRequestURI().substring(theReq.getContextPath().length());
		try {
			path = new URLCodec().decode(path);
		} catch (DecoderException e1) {
			ourLog.error("Failed to decode path", e1);
			sendFailure(theResp, "Failed to decode path: " + e1.getMessage());
		}

		String contextPath = theReq.getContextPath();
		String query = "?" + theReq.getQueryString();

		ourLog.debug("New GET request at path[{}] and context path[{}]", path, contextPath);

		OrchestratorResponseBean response;
		try {
			response = myOrch.handle(get, path, query, theReq.getReader());
		} catch (InternalErrorException e) {
			ourLog.info("Processing Failure", e);
			sendFailure(theResp, e.getMessage());
			return;
		} catch (UnknownRequestException e) {
			ourLog.info("Unknown request location: {} - Message {}", path, e.getMessage());
			sendUnknownLocation(theResp, e);
			return;
		} catch (ProcessingException e) {
			ourLog.info("Processing Failure", e);
			sendFailure(theResp, e.getMessage());
			return;
		}

		theResp.setStatus(200);

		for (Entry<String, String> next : response.getResponseHeaders().entrySet()) {
			theResp.addHeader(next.getKey(), next.getValue());
		}

		theResp.setContentType(theResp.getContentType());

		PrintWriter w = theResp.getWriter();
		w.append(response.getResponseBody());
		w.close();

		ourLog.debug("Done handling request");
	}

	private void sendUnknownLocation(HttpServletResponse theResp, UnknownRequestException theE) throws IOException {
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

	private void sendFailure(HttpServletResponse theResp, String theMessage) throws IOException {
		theResp.setStatus(500);
		theResp.setContentType("text/plain");

		PrintWriter w = theResp.getWriter();
		w.append("HTTP 500 - ServiceRetriever\n\n");
		w.append("Failure: " + theMessage);
		w.close();
	}

	@Override
	protected void doPost(HttpServletRequest theReq, HttpServletResponse theResp) throws ServletException, IOException {
		handle(theReq, theResp, RequestType.POST);
	}

}
