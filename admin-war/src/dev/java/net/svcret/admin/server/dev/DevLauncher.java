package net.svcret.admin.server.dev;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.server.AbstractConnector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.webapp.WebAppContext;

import com.google.gwt.core.ext.ServletContainer;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.dev.shell.jetty.JettyLauncher;

public class DevLauncher extends JettyLauncher {

	private TreeLogger.Type baseLogLevel = TreeLogger.INFO;

	private String bindAddress;
	private final Object privateInstanceLock = new Object();

	@Override
	public void setBindAddress(String bindAddress) {
		this.bindAddress = bindAddress;
		super.setBindAddress(bindAddress);
	}

	@Override
	public ServletContainer start(TreeLogger logger, int port, File appRootDir) throws Exception {
		TreeLogger branch = logger.branch(TreeLogger.TRACE, "Starting Jetty on port " + port, null);

		checkStartParams(branch, port, appRootDir);

		// Setup our branch logger during startup.
		Log.setLog(new JettyTreeLogger(branch));

		// Force load some JRE singletons that can pin the classloader.
		jreLeakPrevention(logger);

		// Turn off XML validation.
		System.setProperty("org.mortbay.xml.XmlParser.Validating", "false");

		Server server = new Server();

		AbstractConnector connector = getConnector(logger);
		setupConnector(connector, bindAddress, port);
		server.addConnector(connector);

		initializeServer(server);

		
		// Create a new web app in the war directory.
		WebAppContext wac = createWebAppContext(logger, appRootDir);

		RequestLogHandler logHandler = new RequestLogHandler();
		logHandler.setRequestLog(new JettyRequestLogger(logger, getBaseLogLevel()));
		logHandler.setHandler(wac);
		server.setHandler(logHandler);
		server.start();
		server.setStopAtShutdown(true);

		// Now that we're started, log to the top level logger.
		Log.setLog(new JettyTreeLogger(logger));

		// DevMode#doStartUpServer() fails from time to time (rarely) due
		// to an unknown error. Adding some logging to pinpoint the problem.
		int connectorPort = connector.getLocalPort();
		if (connector.getLocalPort() < 0) {
			branch.log(TreeLogger.ERROR, String.format("Failed to connect to open channel with port %d (return value %d)", port, connectorPort));
			if (connector.getConnection() == null) {
				branch.log(TreeLogger.TRACE, "Connection is null");
			}
		}

		return createServletContainer(logger, appRootDir, server, wac, connectorPort);
	}

	private void initializeServer(Server server) throws Error {
		HashLoginService realm;
		realm = new HashLoginService("svcret-realm", "WEB-INF/testusers.properties");
		server.addBean(realm,true);
	}

	private void checkStartParams(TreeLogger logger, int port, File appRootDir) {
		if (logger == null) {
			throw new NullPointerException("logger cannot be null");
		}

		if (port < 0 || port > 65535) {
			throw new IllegalArgumentException("port must be either 0 (for auto) or less than 65536");
		}

		if (appRootDir == null) {
			throw new NullPointerException("app root direcotry cannot be null");
		}
	}

	/*
	 * TODO: This is a hack to pass the base log level to the SCL. We'll have to
	 * figure out a better way to do this for SCLs in general.
	 */
	private TreeLogger.Type getBaseLogLevel() {
		synchronized (privateInstanceLock) {
			return this.baseLogLevel;
		}
	}

	/**
	 * This is a modified version of JreMemoryLeakPreventionListener.java found
	 * in the Apache Tomcat project at
	 * 
	 * http://svn.apache.org/repos/asf/tomcat/trunk/java/org/apache/catalina/
	 * core/ JreMemoryLeakPreventionListener.java
	 * 
	 * Relevant part of the Tomcat NOTICE, retrieved from
	 * http://svn.apache.org/repos/asf/tomcat/trunk/NOTICE Apache Tomcat
	 * Copyright 1999-2010 The Apache Software Foundation
	 * 
	 * This product includes software developed by The Apache Software
	 * Foundation (http://www.apache.org/).
	 */
	private void jreLeakPrevention(TreeLogger logger) {
		// Trigger a call to sun.awt.AppContext.getAppContext(). This will
		// pin the common class loader in memory but that shouldn't be an
		// issue.
		ImageIO.getCacheDirectory();

		/*
		 * Several components end up calling: sun.misc.GC.requestLatency(long)
		 * 
		 * Those libraries / components known to trigger memory leaks due to
		 * eventual calls to requestLatency(long) are: -
		 * javax.management.remote.rmi.RMIConnectorServer.start()
		 */
		try {
			Class<?> clazz = Class.forName("sun.misc.GC");
			Method method = clazz.getDeclaredMethod("requestLatency", new Class[] { long.class });
			method.invoke(null, Long.valueOf(3600000));
		} catch (ClassNotFoundException e) {
			logger.log(TreeLogger.ERROR, "jreLeakPrevention.gcDaemonFail", e);
		} catch (SecurityException e) {
			logger.log(TreeLogger.ERROR, "jreLeakPrevention.gcDaemonFail", e);
		} catch (NoSuchMethodException e) {
			logger.log(TreeLogger.ERROR, "jreLeakPrevention.gcDaemonFail", e);
		} catch (IllegalArgumentException e) {
			logger.log(TreeLogger.ERROR, "jreLeakPrevention.gcDaemonFail", e);
		} catch (IllegalAccessException e) {
			logger.log(TreeLogger.ERROR, "jreLeakPrevention.gcDaemonFail", e);
		} catch (InvocationTargetException e) {
			logger.log(TreeLogger.ERROR, "jreLeakPrevention.gcDaemonFail", e);
		}

		/*
		 * Calling getPolicy retains a static reference to the context class
		 * loader.
		 */
		try {
			// Policy.getPolicy();
			Class<?> policyClass = Class.forName("javax.security.auth.Policy");
			Method method = policyClass.getMethod("getPolicy");
			method.invoke(null);
		} catch (ClassNotFoundException e) {
			// Ignore. The class is deprecated.
		} catch (SecurityException e) {
			// Ignore. Don't need call to getPolicy() to be successful,
			// just need to trigger static initializer.
		} catch (NoSuchMethodException e) {
			logger.log(TreeLogger.WARN, "jreLeakPrevention.authPolicyFail", e);
		} catch (IllegalArgumentException e) {
			logger.log(TreeLogger.WARN, "jreLeakPrevention.authPolicyFail", e);
		} catch (IllegalAccessException e) {
			logger.log(TreeLogger.WARN, "jreLeakPrevention.authPolicyFail", e);
		} catch (InvocationTargetException e) {
			logger.log(TreeLogger.WARN, "jreLeakPrevention.authPolicyFail", e);
		}

		/*
		 * Creating a MessageDigest during web application startup initializes
		 * the Java Cryptography Architecture. Under certain conditions this
		 * starts a Token poller thread with TCCL equal to the web application
		 * class loader.
		 * 
		 * Instead we initialize JCA right now.
		 */
		java.security.Security.getProviders();

		/*
		 * Several components end up opening JarURLConnections without first
		 * disabling caching. This effectively locks the file. Whilst more
		 * noticeable and harder to ignore on Windows, it affects all operating
		 * systems.
		 * 
		 * Those libraries/components known to trigger this issue include: -
		 * log4j versions 1.2.15 and earlier -
		 * javax.xml.bind.JAXBContext.newInstance()
		 */

		// Set the default URL caching policy to not to cache
		try {
			// Doesn't matter that this JAR doesn't exist - just as long as
			// the URL is well-formed
			URL url = new URL("jar:file://dummy.jar!/");
			URLConnection uConn = url.openConnection();
			uConn.setDefaultUseCaches(false);
		} catch (MalformedURLException e) {
			logger.log(TreeLogger.ERROR, "jreLeakPrevention.jarUrlConnCacheFail", e);
		} catch (IOException e) {
			logger.log(TreeLogger.ERROR, "jreLeakPrevention.jarUrlConnCacheFail", e);
		}

		/*
		 * Haven't got to the root of what is going on with this leak but if a
		 * web app is the first to make the calls below the web application
		 * class loader will be pinned in memory.
		 */
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			logger.log(TreeLogger.ERROR, "jreLeakPrevention.xmlParseFail", e);
		}
	}


	/**
	 * Setup a connector for the bind address/port.
	 * 
	 * @param connector
	 * @param bindAddress
	 * @param port
	 */
	private static void setupConnector(AbstractConnector connector, String bindAddress, int port) {
		if (bindAddress != null) {
			connector.setHost(bindAddress.toString());
		}
		connector.setPort(port);

		// Don't share ports with an existing process.
		connector.setReuseAddress(false);

		// Linux keeps the port blocked after shutdown if we don't disable this.
		connector.setSoLingerTime(0);
	}

}
