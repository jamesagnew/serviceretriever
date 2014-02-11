package net.svcret.core.ejb.nodecomm;

import java.io.IOException;
import java.util.Arrays;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;

import net.svcret.admin.shared.model.DtoStickySessionUrlBinding;
import net.svcret.core.api.IConfigService;
import net.svcret.core.api.IRuntimeStatus;
import net.svcret.core.api.ISecurityService;
import net.svcret.core.api.IServiceRegistry;
import net.svcret.core.log.ITransactionLogger;
import net.svcret.core.server.BaseServerBean;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.SerializationUtils;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.springframework.beans.factory.annotation.Autowired;

public class HttpBroadcastListenerBean extends BaseServerBean {

	static final String ACTION_PARAM = "act";
	static final String ARG_PARAM = "arg";

	@Autowired
	private IConfigService myConfigService;

	@Autowired
	private IRuntimeStatus myRuntimeStatus;

	@Autowired
	private ISecurityService mySecurityService;

	@Autowired
	private IServiceRegistry myServiceRegistry;

	@Autowired
	private ITransactionLogger myTransactionLoggerService;

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(HttpBroadcastListenerBean.class);

	public HttpBroadcastListenerBean() {
		setMinThreads(1);
		setMaxThreads(5);
	}

	@Override
	protected void configureServerBeforeStarting() throws Exception {
		ServletHandler proxyHandler = new ServletHandler();
		MyServlet serviceServlet = new MyServlet();
		ServletHolder servletHolder = new ServletHolder(serviceServlet);
		proxyHandler.addServletWithMapping(servletHolder, "/");
		getProxyServer().setHandler(proxyHandler);
	}

	public enum ActionsEnum {
		UPDATE_CONFIG, UPDATE_MONITOR_RULES, UPDATE_SERVICE_REGISTRY, UPDATE_USER_CATALOG, URL_STATUS_CHANGED, STICKY_SESSION_CHANGED, FLUSH_TRANSACTION_LOGS, FLUSH_QUEUED_STATS,
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
			case FLUSH_TRANSACTION_LOGS:
				myTransactionLoggerService.flush();
				break;
			case FLUSH_QUEUED_STATS:
				myRuntimeStatus.flushStatus();
				break;
			}

			theResp.setContentType("text/plain");
			theResp.getWriter().append("Ok");
			theResp.getWriter().close();

		}

		private Object decodeArgument(ServletRequest theArg0) {
			String argBase64 = theArg0.getParameter(ARG_PARAM);
			byte[] argBytes = Base64.decodeBase64(argBase64);
			Object argument = SerializationUtils.deserialize(argBytes);
			return argument;
		}

	}

}
