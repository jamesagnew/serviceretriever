package net.svcret.proxyweb;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(loadOnStartup=1, displayName="Info_Servlet", urlPatterns= {"/info/*"})
public class InfoServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(InfoServlet.class);

	/**
     * {@inheritDoc}
     */
    @Override
    protected void doGet(HttpServletRequest theReq, HttpServletResponse theResp) throws ServletException, IOException {
    	ourLog.info("New request in path: {}", theReq.getPathTranslated());
    	
    	theResp.setContentType("text/html");
    	
    	PrintWriter w = theResp.getWriter();
    	w.append("<html>");
    	w.append("<body>");
    	w.append("Received request<br/>");
    	w.append("Path: " + theReq.getRequestURI().substring(theReq.getContextPath().length()) + "<br/><br/>");
    	
    	for (Enumeration<String> e = theReq.getAttributeNames(); e.hasMoreElements(); ) {
    		String attr = e.nextElement();
			w.append("Attr " + attr + " - " + theReq.getAttribute(attr) + "<br/>");
    	}
    	w.append("<br/><br/>");

    	for (Enumeration<String> e = theReq.getParameterNames(); e.hasMoreElements(); ) {
    		String attr = e.nextElement();
			w.append("Param " + attr + " - " + Arrays.asList(theReq.getParameterValues(attr)) + "<br/>");
    	}
    	w.append("<br/><br/>");

    	w.append("LocalAddr: " + theReq.getLocalAddr() + "<br/>");
    	w.append("LocalName: " + theReq.getLocalName() + "<br/>");
    	w.append("Method: " + theReq.getMethod() + "<br/>");
    	w.append("QueryString: " + theReq.getQueryString() + "<br/>");
    	w.append("RequestUrl: " + theReq.getRequestURL() + "<br/>");
    	w.append("RequestUri: " + theReq.getRequestURI() + "<br/>");
    	w.append("ContextPath: " + theReq.getContextPath() + "<br/>");

    	w.append("PathInfo: " + theReq.getPathInfo() + "<br/>");
    	w.append("PathTranslated: " + theReq.getPathTranslated() + "<br/>");

    	w.append("ServletPath: " + theReq.getServletPath() + "<br/>");
    	w.append("ServletPath: " + theReq.getServletPath() + "<br/>");

    	w.append("</body>");
    	w.append("</html>");
    	
    }

    
    
}
