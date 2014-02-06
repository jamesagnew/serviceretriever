package net.svcret.proxy.app;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ServiceRetrieverApp {

	public static void main(String[] args) throws Exception {
		
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("classpath:svcret-app.xml");
		ctx.start();

//		{
//			Server proxyServer = new Server(8880);
//
//			ServletHandler proxyHandler = new ServletHandler();
//			ServiceServlet serviceServlet = new ServiceServlet();
//			serviceServlet.setServiceOrchestrator((IServiceOrchestrator) ctx.getBean("myServiceOrchestrator"));
//			serviceServlet.setServiceRegistry((IServiceRegistry) ctx.getBean("myServiceRegistry"));
//			ServletHolder servletHolder = new ServletHolder(serviceServlet);
//			proxyHandler.addServletWithMapping(servletHolder, "/");
//
//			proxyServer.setHandler(proxyHandler);
//			proxyServer.start();
//		}
//		{
//			Server proxyServer = new Server(8881);
//
//			WebAppContext adminCtx = new WebAppContext();
//			adminCtx.setWar("file:../admin-war/target/serviceretriever-admin-war-1.0.war");
//			adminCtx.setContextPath("/admin");
//			adminCtx.setExtractWAR(false);
//			proxyServer.setHandler(adminCtx);
//			
//			UserRealm realm;
//			try {
//				realm = new HashUserRealm("svcret-realm", "file:src/main/resources/testusers.properties");
//			} catch (IOException e) {
//				throw new Error(e);
//			}
//			proxyServer.setUserRealms(new UserRealm[] { realm });
//
//			proxyServer.start();
//
//		}

	}

}
