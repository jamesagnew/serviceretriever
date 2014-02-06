package net.svcret.ejb.ejb.nodecomm;

import static net.svcret.ejb.ejb.nodecomm.HttpBroadcastListenerBean.*;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import net.svcret.ejb.api.IHttpClient;
import net.svcret.ejb.ejb.nodecomm.HttpBroadcastListenerBean.ActionsEnum;
import net.svcret.ejb.model.entity.PersStickySessionUrlBinding;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.http.client.ClientProtocolException;
import org.springframework.beans.factory.annotation.Autowired;

public class HttpBroadcastSenderBean implements IBroadcastSender {

	private List<String> myUrls;

	@Override
	public void monitorRulesChanged() {
		notify(ActionsEnum.UPDATE_MONITOR_RULES);
	}

	@Override
	public void notifyConfigChanged() {
		notify(ActionsEnum.UPDATE_CONFIG);
	}

	@Override
	public void notifyNewStickySession(PersStickySessionUrlBinding theExisting) {
		notify(ActionsEnum.STICKY_SESSION_CHANGED, theExisting);
	}

	@Override
	public void notifyServiceCatalogChanged() {
		notify(ActionsEnum.UPDATE_SERVICE_REGISTRY);
	}

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(HttpBroadcastSenderBean.class);

	@Override
	public void notifyUrlStatusChanged(Long thePid) {
		notify(ActionsEnum.URL_STATUS_CHANGED, thePid);
	}

	@Override
	public void notifyUserCatalogChanged() {
		notify(ActionsEnum.UPDATE_USER_CATALOG);
	}

	public void setUrls(List<String> theUrls) {
		myUrls = theUrls;
	}

	private void notify(ActionsEnum theAction) {
		notify(theAction, null);
	}

	@Autowired
	private IHttpClient myHttpClient;

	private void notify(ActionsEnum theAction, Serializable theArgument) {
		StringBuilder b = new StringBuilder();
		b.append('?');
		b.append(ACTION_PARAM);
		b.append('=');
		b.append(theAction.name().toLowerCase());
		if (theArgument != null) {
			byte[] argBytes = SerializationUtils.serialize(theArgument);
			String argBase64 = Base64.encodeBase64String(argBytes);
			b.append('&');
			b.append(ARG_PARAM);
			b.append('=');
			b.append(argBase64);
		}

		String argument = b.toString();
		ourLog.info("Going to broadcast arguments: {}", argument);

		for (String nextUrl : myUrls) {
			try {
				myHttpClient.get(nextUrl + argument);
			} catch (ClientProtocolException e) {
				ourLog.warn("Failed to invoke url: {}", nextUrl + argument, e);
			} catch (IOException e) {
				ourLog.warn("Failed to invoke url: {}", nextUrl + argument, e);
			}
		}
	}

}
