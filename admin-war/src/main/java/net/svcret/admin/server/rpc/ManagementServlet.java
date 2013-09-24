package net.svcret.admin.server.rpc;

import java.io.IOException;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import net.svcret.ejb.ejb.importexport.IImportExportServiceLocal;
import net.svcret.ejb.ex.UnexpectedFailureException;

public class ManagementServlet extends HttpServlet {

	private static final String PARAM_DOMAINID_NAME = "domainid";
	private static final String PARAM_ACTION_NAME = "action";
	private static final String PARAM_ACTION_VALUE_GETDOMAIN = "getdomain";

	private static final long serialVersionUID = 1L;

	@EJB
	private IImportExportServiceLocal myImportExportService;
	
	@Override
	protected void doGet(HttpServletRequest theReq, HttpServletResponse theResp) throws ServletException, IOException {
		String action = theReq.getParameter(PARAM_ACTION_NAME);
		if (PARAM_ACTION_VALUE_GETDOMAIN.equals(action)) {
			handleGetDomain(theReq, theResp);
		} else {
			throw new ServletException("No action or invalid action specified");
		}
	}

	private void handleGetDomain(HttpServletRequest theReq, HttpServletResponse theResp) throws ServletException, IOException {
		String domainId = theReq.getParameter(PARAM_DOMAINID_NAME); 
		if (StringUtils.isBlank(domainId)) {
			throw new ServletException("No domainid specified");
		}
	
		try {
			String domain = myImportExportService.exportDomain(domainId);
			
			theResp.setContentType("text/xml");
			theResp.getWriter().append(domain);
			theResp.getWriter().close();
			
		} catch (UnexpectedFailureException e) {
			throw new ServletException(e);
		}
		
	}

	
	
}
