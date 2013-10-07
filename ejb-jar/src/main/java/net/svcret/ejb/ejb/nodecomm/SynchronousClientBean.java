package net.svcret.ejb.ejb.nodecomm;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import net.svcret.ejb.api.IConfigService;

@Stateless
public class SynchronousClientBean implements ISynchronousNodeIpcClient {
	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(SynchronousClientBean.class);

	@EJB
	private IConfigService myConfigSvc;

	private Map<String, SynchronousInvokerClient> myRemoteClients = new HashMap<String, SynchronousInvokerClient>();

	@Override
	public void invokeFlushRuntimeStatus() {
		for (String nextUrl : myConfigSvc.getSecondaryNodeRefreshUrls()) {
			ourLog.debug("Invoking secondary refresh URL: {}", nextUrl);
			try {
				getClient(nextUrl).invokeFlushRuntimeStatus();
			} catch (Exception e) {
				ourLog.debug("Failed to invoke URL to flush statistics on remote node", e);
				ourLog.error("Failed to invoke URL '{}' to flush statistics on remote node: {}", nextUrl, e.toString());
			}
		}
	}

	@Override
	public void invokeFlushTransactionLogs() {
		for (String nextUrl : myConfigSvc.getSecondaryNodeRefreshUrls()) {
			ourLog.debug("Invoking secondary refresh URL: {}", nextUrl);
			try {
				getClient(nextUrl).invokeFlushTransactionLogs();
			} catch (Exception e) {
				ourLog.debug("Failed to invoke URL to flush statistics on remote node", e);
				ourLog.error("Failed to invoke URL '{}' to flush statistics on remote node: {}", nextUrl, e.toString());
			}
		}
	}

	private ISynchronousNodeIpcClient getClient(String nextUrl) throws MalformedURLException {
		if (!myRemoteClients.containsKey(nextUrl)) {
			myRemoteClients.put(nextUrl, new SynchronousInvokerClient(nextUrl));
		}
		ISynchronousNodeIpcClient client = myRemoteClients.get(nextUrl).getClient();
		return client;
	}

}
