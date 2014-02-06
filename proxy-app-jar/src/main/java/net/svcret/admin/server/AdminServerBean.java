package net.svcret.admin.server;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import net.svcret.admin.shared.util.Validate;
import net.svcret.ejb.api.IServiceOrchestrator;
import net.svcret.ejb.api.IServiceRegistry;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.security.HashUserRealm;
import org.mortbay.jetty.security.UserRealm;
import org.mortbay.jetty.webapp.WebAppContext;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.io.Resource;

public class AdminServerBean implements BeanNameAware {

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(AdminServerBean.class);
	
	private String myBeanName;
	private int myPort;

	@Autowired
	private IServiceOrchestrator myServiceOrchestrator;

	@Autowired
	private IServiceRegistry myServiceRegistry;

	private Server myProxyServer;

	private Resource myWar;

	@PreDestroy
	public void destroy() throws Exception {
		ourLog.info("Shutting down admin server on port: {}", myPort);
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
	
	@Required
	public void setWar(Resource theWar) throws IOException {
		if (!theWar.getFile().exists()) {
			throw new IllegalArgumentException("Unknown WAR location: " + theWar.getFile().getAbsolutePath());
		}
		
		myWar = theWar;
	}
	

	@PostConstruct
	public void startup() throws Exception {
		ourLog.info("Starting up admin listener on port {} with name: {}", myPort, myBeanName);
		
		myProxyServer = new Server(myPort);

		WebAppContext adminCtx = new WebAppContext();
		adminCtx.setWar("file:" + myWar.getFile().getAbsolutePath());
		adminCtx.setContextPath("/");
		adminCtx.setExtractWAR(false);
		myProxyServer.setHandler(adminCtx);
		
		UserRealm realm;
		try {
			realm = new HashUserRealm("svcret-realm", "file:src/main/resources/testusers.properties");
		} catch (IOException e) {
			throw new Error(e);
		}
		myProxyServer.setUserRealms(new UserRealm[] { realm });

		myProxyServer.start();

	}

}
