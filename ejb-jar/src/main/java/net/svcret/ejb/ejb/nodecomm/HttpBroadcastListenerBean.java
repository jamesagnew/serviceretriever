package net.svcret.ejb.ejb.nodecomm;

import java.io.IOException;
import java.util.Arrays;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;

import net.svcret.admin.shared.model.DtoStickySessionUrlBinding;
import net.svcret.admin.shared.util.Validate;
import net.svcret.ejb.api.IConfigService;
import net.svcret.ejb.api.IRuntimeStatus;
import net.svcret.ejb.api.ISecurityService;
import net.svcret.ejb.api.IServiceRegistry;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.SerializationUtils;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.ServletHandler;
import org.mortbay.jetty.servlet.ServletHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;

public class HttpBroadcastListenerBean {

	static final String ACTION_PARAM = "act";
	static final String ARG_PARAM = "arg";

	private int myPort;
	private Server myProxyServer;

	@Autowired
	private IConfigService myConfigService;

	@Autowired
	private IRuntimeStatus myRuntimeStatus;

	@Autowired
	private ISecurityService mySecurityService;

	@Autowired
	private IServiceRegistry myServiceRegistry;

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(HttpBroadcastListenerBean.class);

	public HttpBroadcastListenerBean() {

	}

	@PreDestroy
	public void destroy() throws Exception {
		myProxyServer.stop();
	}

	@PostConstruct
	public void initialize() throws Exception {

		myProxyServer = new Server(myPort);

		ServletHandler proxyHandler = new ServletHandler();
		MyServlet serviceServlet = new MyServlet();
		ServletHolder servletHolder = new ServletHolder(serviceServlet);
		proxyHandler.addServletWithMapping(servletHolder, "/");

		myProxyServer.setHandler(proxyHandler);
		myProxyServer.start();

	}

	@Required
	public void setPort(int thePort) {
		Validate.greaterThanZero(thePort, "Port");
		myPort = thePort;
	}

	public enum ActionsEnum {
		UPDATE_CONFIG, UPDATE_MONITOR_RULES, UPDATE_SERVICE_REGISTRY, UPDATE_USER_CATALOG, URL_STATUS_CHANGED, STICKY_SESSION_CHANGED,
	}

	private class MyServlet extends HttpServlet {
		private static final long serialVersionUID = 1L;

		@Override
		public void service(ServletRequest theReq, ServletResponse theResp) throws IOException {
			String actionName = theReq.getParameter(ACTION_PARAM);
			ourLog.info("Incoming broadcast message with action: {}", actionName);

			ActionsEnum action;
			try {
				action = ActionsEnum.valueOf(actionName.toUpperCase());
			} catch (Exception e) {
				ourLog.error("Invalid broadcast action string: {}", actionName);
				theResp.setContentType("text/plain");
				theResp.getWriter().append("Invalid action. Values are:" + Arrays.asList(ActionsEnum.values()));
				return;
			}

			switch (action) {
			case STICKY_SESSION_CHANGED:
				DtoStickySessionUrlBinding binding = (DtoStickySessionUrlBinding) decodeArgument(theReq);
				ourLog.info("Received broadcast for updated sticky session: {}", binding);
				myRuntimeStatus.updatedStickySessionBinding(binding);
				break;
			case UPDATE_CONFIG:
				ourLog.info("Received broadcast for updated config");
				myConfigService.reloadConfigIfNeeded();
				break;
			case UPDATE_MONITOR_RULES:
				ourLog.info("Received broadcast for updated monitor rules");
				// Reload the service registry because the SR contains active
				// rules
				myServiceRegistry.reloadRegistryFromDatabase();
				break;
			case UPDATE_SERVICE_REGISTRY:
				ourLog.info("Received broadcast for updated service registry");
				myServiceRegistry.reloadRegistryFromDatabase();
				break;
			case UPDATE_USER_CATALOG:
				ourLog.info("Received broadcast for updated user catalog");
				mySecurityService.loadUserCatalogIfNeeded();
				break;
			case URL_STATUS_CHANGED:
				Long pid = (Long) decodeArgument(theReq);
				ourLog.info("Received broadcast for updated URL status: {}", pid);
				myRuntimeStatus.reloadUrlStatus(pid);
				break;
			}

		}

		private Object decodeArgument(ServletRequest theArg0) {
			String argBase64 = theArg0.getParameter(ARG_PARAM);
			byte[] argBytes = Base64.decodeBase64(argBase64);
			Object argument = SerializationUtils.deserialize(argBytes);
			return argument;
		}

	}

}
