package net.svcret.ejb.ejb.nodecomm;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;

import net.svcret.ejb.api.IConfigService;
import net.svcret.ejb.ejb.log.ITransactionLogger;

import com.google.common.collect.Lists;

@Stateless
public class SynchronousClientBean implements ISynchronousNodeIpcClient {
	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(SynchronousClientBean.class);

	@EJB
	private IConfigService myConfigSvc;

	private Map<String, SynchronousInvokerClient> myRemoteClients = new HashMap<String, SynchronousInvokerClient>();

	@EJB
	private ITransactionLogger myTransactionLogger;

	private ISynchronousInvoker getClient(String nextUrl) throws MalformedURLException {
		if (!myRemoteClients.containsKey(nextUrl)) {
			myRemoteClients.put(nextUrl, new SynchronousInvokerClient(nextUrl));
		}
		SynchronousInvokerClient invokerClient = myRemoteClients.get(nextUrl);
		ISynchronousInvoker client = invokerClient.getClient();

		// Add the logging handler
		BindingProvider bp = (BindingProvider) client;
		Binding binding = bp.getBinding();

		@SuppressWarnings("rawtypes")
		List<Handler> handlerList = binding.getHandlerChain();
		if (handlerList == null) {
			handlerList = Lists.newArrayList();
		}

		SoapLoggingHandler loggingHandler = new SoapLoggingHandler();
		handlerList.add(loggingHandler);

		binding.setHandlerChain(handlerList);

		return client;
	}

	@Override
	public void invokeFlushRuntimeStatus() {
		for (String nextUrl : myConfigSvc.getSecondaryNodeRefreshUrls()) {
			ourLog.debug("Invoking secondary refresh URL: {}", nextUrl);
			try {
				getClient(nextUrl).flushRuntimeStatus(new FlushRuntimeStatusRequest());
			} catch (Exception e) {
				ourLog.debug("Failed to invoke URL to flush statistics on remote node", e);
				ourLog.error("Failed to invoke URL '{}' to flush statistics on remote node: {}", nextUrl, e.toString());
			}
		}
	}

	@Override
	public void invokeFlushTransactionLogs() {
		myTransactionLogger.flush();

		for (String nextUrl : myConfigSvc.getSecondaryNodeRefreshUrls()) {
			ourLog.debug("Invoking secondary refresh URL: {}", nextUrl);
			try {
				getClient(nextUrl).flushTransactionLogs(new FlushTransactionLogsRequest());
			} catch (Exception e) {
				ourLog.debug("Failed to invoke URL to flush statistics on remote node", e);
				ourLog.error("Failed to invoke URL '{}' to flush statistics on remote node: {}", nextUrl, e.toString());
			}
		}
	}

}
