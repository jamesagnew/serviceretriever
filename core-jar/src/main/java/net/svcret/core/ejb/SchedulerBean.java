package net.svcret.core.ejb;

import net.svcret.core.api.IRuntimeStatus;
import net.svcret.core.api.ISecurityService;
import net.svcret.core.ejb.monitor.IMonitorService;
import net.svcret.core.ejb.nodecomm.IBroadcastSender;
import net.svcret.core.log.IFilesystemAuditLogger;
import net.svcret.core.log.ITransactionLogger;

import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class SchedulerBean {

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(SchedulerBean.class);

	@Autowired
	private IFilesystemAuditLogger myFilesystemAuditLogger;

	@Autowired
	private IMonitorService myMonitorSvc;

	@Autowired
	private ISecurityService mySecuritySvc;

	@Autowired
	private IRuntimeStatus myStatsSvc;
	
	@Autowired
	private IBroadcastSender mySynchronousNodeIpcClient;

	@Autowired
	private ITransactionLogger myTransactionLogger;

	@Scheduled(fixedDelay=DateUtils.MILLIS_PER_HOUR)
	public void collapseStats() {
		try {
			ourLog.debug("collapseStats()");
			myStatsSvc.collapseStats();
		} catch (Exception e) {
			ourLog.error("Failed to collapse stats", e);
		}
	}

	@Scheduled(fixedDelay=DateUtils.MILLIS_PER_MINUTE)
	public void flushFilesystemAuditEvents() {
		try {
			ourLog.debug("flushFilesystemAuditEvents()");
			myFilesystemAuditLogger.flushAuditEventsIfNeeded();
		} catch (Exception e) {
			ourLog.error("Failed to collapse stats", e);
		}
	}

	@Scheduled(fixedDelay=15L * DateUtils.MILLIS_PER_MINUTE)
	public synchronized void flushTransactionLogs() {
		try {
			mySynchronousNodeIpcClient.requestFlushTransactionLogs();
		} catch (Exception e) {
			ourLog.error("Failed to flush remote stats", e);
		}

	}

	@Scheduled(fixedDelay=DateUtils.MILLIS_PER_MINUTE)
	public synchronized void flushStatistics() {
		try {
			mySynchronousNodeIpcClient.requestFlushQueuedStats();
		} catch (Exception e) {
			ourLog.error("Failed to flush remote stats", e);
		}
	}

	@Scheduled(fixedDelay=DateUtils.MILLIS_PER_MINUTE)
	public void monitorRunChecks() {
		try {
			myMonitorSvc.runActiveChecks();
			myMonitorSvc.runPassiveChecks();
		} catch (Exception e) {
			ourLog.error("Failed to collapse stats", e);
		}
	}


	@Scheduled(fixedDelay=DateUtils.MILLIS_PER_MINUTE)
	public void recordNodeStats() {
		try {
			myStatsSvc.recordNodeStatistics();
		} catch (Exception e) {
			ourLog.error("Failed to load user catalog", e);
		}
	}

	@Scheduled(fixedDelay=DateUtils.MILLIS_PER_MINUTE)
	public void reloadUserRegistry() {
		try {
			mySecuritySvc.loadUserCatalogIfNeeded();
		} catch (Exception e) {
			ourLog.error("Failed to load user catalog", e);
		}
	}

}
