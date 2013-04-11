package ca.uhn.sail.proxy.web;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;

@WebServlet(asyncSupported=true, loadOnStartup=1, urlPatterns= {"/Service/*"})
public class ServiceServlet extends HttpServlet {

}
