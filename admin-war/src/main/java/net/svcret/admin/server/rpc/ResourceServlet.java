package net.svcret.admin.server.rpc;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.svcret.ejb.admin.IAdminServiceLocal;

//@WebServlet(urlPatterns = { "/resources/*" })
public class ResourceServlet extends HttpServlet {

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(ResourceServlet.class);

	private static final long serialVersionUID = 1L;

	@EJB
	private IAdminServiceLocal myAdminSvc;

	@Override
	protected void doGet(HttpServletRequest theReq, HttpServletResponse theResp) throws ServletException, IOException {
		String pathInfo = theReq.getPathInfo();

		Matcher m = Pattern.compile("^\\/wsdl_bundle_([0-9]+)\\.zip$").matcher(pathInfo);
		if (m.matches()) {
			String pidString = m.group(1);
			
			ourLog.info("Returning WSDL bundle for SvcVer: {}", pidString);
			
			try {
				byte[] wsdlBundle = myAdminSvc.createWsdlBundle(Long.parseLong(pidString));
				
				theResp.setContentType("application/octet-stream");
				theResp.getOutputStream().write(wsdlBundle);
				theResp.getOutputStream().close();
				
			} catch (Exception e) {
				throw new ServletException(e);
			}
			return;
		}
		
		throw new ServletException("Unknown resource path: " + pathInfo);
	}

}
