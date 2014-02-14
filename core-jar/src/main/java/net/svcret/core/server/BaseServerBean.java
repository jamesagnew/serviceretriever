package net.svcret.core.server;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import net.svcret.admin.shared.util.Validate;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.annotation.Required;

public abstract class BaseServerBean implements BeanNameAware {
	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(BaseServerBean.class);

	private String myBeanName;
	private int myMaxThreads = 10;
	private int myMinThreads = 5;
	private int myPort;
	private Server myProxyServer;
	private SslContextFactory mySslContextFactory;

	@PreDestroy
	public void destroy() throws Exception {
		ourLog.info("Shutting down server '{}' on port: {}", myBeanName, myPort);
		myProxyServer.stop();
	}

	public int getMaxThreads() {
		return myMaxThreads;
	}

	public int getMinThreads() {
		return myMinThreads;
	}

	public Server getProxyServer() {
		return myProxyServer;
	}

	@Override
	public void setBeanName(String theName) {
		myBeanName = theName;
	}

	public void setMaxThreads(int theMaxThreads) {
		Validate.greaterThanZero(theMaxThreads, "MaxThreads");
		myMaxThreads = theMaxThreads;
	}

	public void setMinThreads(int theMinThreads) {
		Validate.notNegative(theMinThreads, "MinThreads");
		myMinThreads = theMinThreads;
	}

	@Required
	public void setPort(int thePort) {
		Validate.greaterThanZero(thePort, "Port");
		myPort = thePort;
	}

	public void setSslContextFactory(SslContextFactory theSslContextFactory) {
		mySslContextFactory = theSslContextFactory;
	}

	@SuppressWarnings("resource")
	@PostConstruct
	public void startup() throws Exception {
		ourLog.info("Starting up admin listener on port {} with name: {}", myPort, myBeanName);

		QueuedThreadPool threadPool = new QueuedThreadPool(10, 2);
		threadPool.setName(myBeanName);
		myProxyServer = new Server(threadPool);

		ServerConnector connector = new ServerConnector(myProxyServer, mySslContextFactory);
		connector.setPort(myPort);
		myProxyServer.setConnectors(new Connector[] { connector });

		configureServerBeforeStarting();

		myProxyServer.start();

	}

	protected abstract void configureServerBeforeStarting() throws Exception;

}
