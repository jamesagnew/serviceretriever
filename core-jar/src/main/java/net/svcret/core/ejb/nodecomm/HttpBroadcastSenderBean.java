package net.svcret.core.ejb.nodecomm;

import static net.svcret.core.ejb.nodecomm.HttpBroadcastListenerBean.ACTION_PARAM;
import static net.svcret.core.ejb.nodecomm.HttpBroadcastListenerBean.ARG_PARAM;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import net.svcret.core.api.IHttpClient;
import net.svcret.core.ejb.nodecomm.HttpBroadcastListenerBean.ActionsEnum;
import net.svcret.core.model.entity.PersStickySessionUrlBinding;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.http.client.ClientProtocolException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;

public class HttpBroadcastSenderBean implements IBroadcastSender {

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(HttpBroadcastSenderBean.class);

	@Autowired
	private IHttpClient myHttpClient;

	private long myLastStatsFlush;

	private long myLastTransactionFlush;
	private ReentrantLock myStatsFlushLock = new ReentrantLock();

	@Autowired
	private TaskScheduler myTaskScheduler;

	private ReentrantLock myTransactionFlushLock = new ReentrantLock();

	private List<String> myUrls;

	@Override
	public void notifyConfigChanged() {
		notify(ActionsEnum.UPDATE_CONFIG);
	}

	@Override
	public void notifyMonitorRulesChanged() {
		notify(ActionsEnum.UPDATE_MONITOR_RULES);
	}

	@Override
	public void notifyNewStickySession(PersStickySessionUrlBinding theExisting) {
		notifyAsynchronously(ActionsEnum.STICKY_SESSION_CHANGED, theExisting);
	}

	@Override
	public void notifyServiceCatalogChanged() {
		notify(ActionsEnum.UPDATE_SERVICE_REGISTRY);
	}

	@Override
	public void notifyUrlStatusChanged(Long thePid) {
		notifyAsynchronously(ActionsEnum.URL_STATUS_CHANGED, thePid);
	}

	@Override
	public void notifyUserCatalogChanged() {
		notify(ActionsEnum.UPDATE_USER_CATALOG);
	}

	@Override
	public void requestFlushQueuedStats() {
		try {
			if (!myStatsFlushLock.tryLock(5000, TimeUnit.MILLISECONDS)) {
				ourLog.warn("Gave up waiting for stats flush lock");
				return;
			}
		} catch (InterruptedException e) {
			return;
		}

		try {
			notifySynchronously(ActionsEnum.FLUSH_QUEUED_STATS, null);
		} finally {
			myStatsFlushLock.unlock();
		}

		myLastStatsFlush = System.currentTimeMillis();
	}

	public void requestFlushQueuedStatsUnlessItHasHappenedecently() {
		try {
			if (!myStatsFlushLock.tryLock(5000, TimeUnit.MILLISECONDS)) {
				ourLog.warn("Gave up waiting for stats flush lock");
				return;
			}
		} catch (InterruptedException e) {
			return;
		}

		try {
			if ((myLastStatsFlush + 5000) > System.currentTimeMillis()) {
				ourLog.debug("Last stats flush was at {}, not going to do another", new Date(myLastStatsFlush));
				return;
			}

			requestFlushQueuedStats();
		} finally {
			myStatsFlushLock.unlock();
		}

	}

	@Override
	public void requestFlushTransactionLogs() {
		try {
			if (!myTransactionFlushLock.tryLock(5000, TimeUnit.MILLISECONDS)) {
				ourLog.warn("Gave up waiting for transaction flush lock");
				return;
			}
		} catch (InterruptedException e) {
			return;
		}

		try {
			notifySynchronously(ActionsEnum.FLUSH_TRANSACTION_LOGS, null);
		} finally {
			myTransactionFlushLock.unlock();
		}

		myLastTransactionFlush = System.currentTimeMillis();

	}

	public void requestFlushTransactionLogsUnlessItHasHappenedecently() {
		try {
			if (!myTransactionFlushLock.tryLock(5000, TimeUnit.MILLISECONDS)) {
				ourLog.warn("Gave up waiting for transaction flush lock");
				return;
			}
		} catch (InterruptedException e) {
			return;
		}

		try {
			if ((myLastTransactionFlush + 5000) > System.currentTimeMillis()) {
				ourLog.debug("Last stats flush was at {}, not going to do another", new Date(myLastTransactionFlush));
				return;
			}

			requestFlushTransactionLogs();
		} finally {
			myTransactionFlushLock.unlock();
		}

	}

	public void setUrls(List<String> theUrls) {
		myUrls = theUrls;
	}

	private void notify(ActionsEnum theAction) {
		notifyAsynchronously(theAction, null);
	}

	private void notifyAsynchronously(final ActionsEnum theAction, final Serializable theArgument) {
		Runnable task = new Runnable() {
			@Override
			public void run() {
				notifySynchronously(theAction, theArgument);
			}

		};
		myTaskScheduler.schedule(task, new Date(System.currentTimeMillis() + (5 * DateUtils.MILLIS_PER_SECOND)));
	}

	private void notifySynchronously(final ActionsEnum theAction, final Serializable theArgument) {
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
				ourLog.warn("Failed to connect to remote broadcast URL: {} - Error: {}", nextUrl, e.toString());
			} catch (IOException e) {
				ourLog.warn("Failed to connect to remote broadcast URL: {} - Error: {}", nextUrl, e.toString());
			}
		}
	}

}
