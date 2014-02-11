package net.svcret.core.server;

import net.svcret.core.api.IServiceOrchestrator;
import net.svcret.core.api.IServiceRegistry;

import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.springframework.beans.factory.annotation.Autowired;

public class ProxyServer extends BaseServerBean {

	@Autowired
	private IServiceOrchestrator myServiceOrchestrator;

	@Autowired
	private IServiceRegistry myServiceRegistry;

	@Override
	protected void configureServerBeforeStarting() {
		ServletHandler proxyHandler = new ServletHandler();
		ServiceServlet serviceServlet = new ServiceServlet();
		serviceServlet.setServiceOrchestrator(myServiceOrchestrator);
		serviceServlet.setServiceRegistry(myServiceRegistry);
		ServletHolder servletHolder = new ServletHolder(serviceServlet);
		proxyHandler.addServletWithMapping(servletHolder, "/");

		getProxyServer().setHandler(proxyHandler);
	}



}
