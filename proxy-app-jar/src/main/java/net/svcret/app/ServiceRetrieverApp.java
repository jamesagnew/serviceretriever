package net.svcret.app;

import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;

public class ServiceRetrieverApp {

	private static ClassPathXmlApplicationContext ourCtx;

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(ServiceRetrieverApp.class);

	public static void main(String[] args) throws Exception {
		Runtime.getRuntime().addShutdownHook(new ShutdownHook());

		LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

		try {
			JoranConfigurator configurator = new JoranConfigurator();
			configurator.setContext(context);
			context.reset();
			configurator.doConfigure("conf/logback-startup.xml");
		} catch (JoranException je) {
			// StatusPrinter will handle this
		}
		StatusPrinter.printInCaseOfErrorsOrWarnings(context);

		ourCtx = new ClassPathXmlApplicationContext("file:conf/svcret-app.xml");
		ourCtx.start();

	}

	private static void shutdown() {
		ourLog.info("ServiceRetriever is going to shut down...");
		if (ourCtx != null) {
			ourCtx.stop();
		}
		ourLog.info("Shutdown completed");
	}

	private final static class ShutdownHook extends Thread {
		@Override
		public void run() {
			shutdown();
		}
	}

}
