package net.svcret.core.server;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.EJB;

import net.svcret.admin.shared.util.Validate;
import net.svcret.ejb.api.IServiceOrchestrator;
import net.svcret.ejb.api.IServiceRegistry;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.ServletHandler;
import org.mortbay.jetty.servlet.ServletHolder;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;

public class ProxyServer implements BeanNameAware {

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(ProxyServer.class);
	
	private String myBeanName;
	private int myPort;

	@Autowired
	private IServiceOrchestrator myServiceOrchestrator;

	@Autowired
	private IServiceRegistry myServiceRegistry;

	private Server myProxyServer;

	@PreDestroy
	public void destroy() throws Exception {
		ourLog.info("Shutting down proxy server on port: {}", myPort);
		myProxyServer.stop();
	}
	
	@Override
	public void setBeanName(String theName) {
		myBeanName = theName;
	}

	@Required
	public void setPort(int thePort) {
		Validate.greaterThanZero(thePort, "Port");
		myPort = thePort;
	}

	@PostConstruct
	public void startup() throws Exception {
		ourLog.info("Starting up proxy listener on port {} with name: {}", myPort, myBeanName);
		
		myProxyServer = new Server(myPort);
		
		ServletHandler proxyHandler = new ServletHandler();
		ServiceServlet serviceServlet = new ServiceServlet();
		serviceServlet.setServiceOrchestrator(myServiceOrchestrator);
		serviceServlet.setServiceRegistry(myServiceRegistry);
		ServletHolder servletHolder = new ServletHolder(serviceServlet);
		proxyHandler.addServletWithMapping(servletHolder, "/");

		myProxyServer.setHandler(proxyHandler);
		myProxyServer.start();

	}

}
