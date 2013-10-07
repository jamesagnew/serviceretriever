package net.svcret.ejb.ejb;

import java.util.Date;

import javax.annotation.PostConstruct;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import net.svcret.admin.shared.model.RetrieverNodeTypeEnum;
import net.svcret.ejb.api.IConfigService;
import net.svcret.ejb.api.IFilesystemAuditLogger;
import net.svcret.ejb.api.IMonitorService;
import net.svcret.ejb.api.IRuntimeStatus;
import net.svcret.ejb.api.IScheduler;
import net.svcret.ejb.api.ISecurityService;
import net.svcret.ejb.api.ITransactionLogger;
import net.svcret.ejb.ejb.nodecomm.ISynchronousNodeIpcClient;

@Startup()
@Singleton()
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class SchedulerBean implements IScheduler {

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(SchedulerBean.class);

	@EJB
	private IConfigService myConfigSvc;

	@EJB
	private IFilesystemAuditLogger myFilesystemAuditLogger;

	private long myLastRecentMessagesFlush;

	private long myLastStatsFlush;

	@EJB
	private IMonitorService myMonitorSvc;

	@EJB
	private ISecurityService mySecuritySvc;

	@EJB
	private IRuntimeStatus myStatsSvc;
	@EJB
	private ISynchronousNodeIpcClient mySynchronousNodeIpcClient;

	@EJB
	private ITransactionLogger myTransactionLogger;

	@Override
	@Schedule(second = "0", minute = "*", hour = "*", persistent = false)
	public void collapseStats() {
		try {
			if (myConfigSvc.getNodeType() != RetrieverNodeTypeEnum.PRIMARY) {
				return;
			}

			ourLog.debug("collapseStats()");
			myStatsSvc.collapseStats();
		} catch (Exception e) {
			ourLog.error("Failed to collapse stats", e);
		}
	}

	@Override
	@Schedule(second = "0", minute = "*", hour = "*", persistent = false)
	public void flushFilesystemAuditEvents() {
		try {
			ourLog.debug("flushFilesystemAuditEvents()");
			myFilesystemAuditLogger.flushAuditEventsIfNeeded();
		} catch (Exception e) {
			ourLog.error("Failed to collapse stats", e);
		}
	}

	@Override
	@Schedule(second = "0", minute = "0/15", hour = "*", persistent = false)
	public synchronized void flushInMemoryRecentMessagesUnlessItHasHappenedVeryRecently() {
		if ((myLastRecentMessagesFlush + 5000) > System.currentTimeMillis()) {
			ourLog.debug("Last stats flush was at {}, not going to do another", new Date(myLastStatsFlush));
			return;
		}

		if (myConfigSvc.getNodeType() != RetrieverNodeTypeEnum.PRIMARY) {
			ourLog.debug("Not the primary node, not going to flush stats");
			return;
		}

		try {
			mySynchronousNodeIpcClient.invokeFlushTransactionLogs();
		} catch (Exception e) {
			ourLog.error("Failed to flush remote stats", e);
		}

		try {
			myTransactionLogger.flush();
			myLastRecentMessagesFlush = System.currentTimeMillis();
		} catch (Exception e) {
			ourLog.error("Failed to flush local stats", e);
		}

	}

	@Override
	@Schedule(second = "0", minute = "*", hour = "*", persistent = false)
	public synchronized void flushInMemoryStatisticsUnlessItHasHappenedVeryRecently() {
		if ((myLastStatsFlush + 5000) > System.currentTimeMillis()) {
			ourLog.debug("Last stats flush was at {}, not going to do another", new Date(myLastStatsFlush));
			return;
		}

		if (myConfigSvc.getNodeType() != RetrieverNodeTypeEnum.PRIMARY) {
			ourLog.debug("Not the primary node, not going to flush stats");
			return;
		}

		try {
			mySynchronousNodeIpcClient.invokeFlushRuntimeStatus();
		} catch (Exception e) {
			ourLog.error("Failed to flush remote stats", e);
		}

		try {
			ourLog.debug("flushStats()");
			myStatsSvc.flushStatus();
			myLastStatsFlush = System.currentTimeMillis();
		} catch (Exception e) {
			ourLog.error("Failed to flush local stats", e);
		}
	}

	@Override
	@Schedule(second = "0", minute = "*", hour = "*", persistent = false)
	public void monitorActiveChecks() {
		try {
			if (myConfigSvc.getNodeType() != RetrieverNodeTypeEnum.PRIMARY) {
				return;
			}

			myMonitorSvc.runActiveChecks();
		} catch (Exception e) {
			ourLog.error("Failed to collapse stats", e);
		}
	}

	@Override
	@Schedule(second = "0", minute = "*", hour = "*", persistent = false)
	public void monitorRunPassiveChecks() {
		try {
			if (myConfigSvc.getNodeType() != RetrieverNodeTypeEnum.PRIMARY) {
				return;
			}

			myMonitorSvc.check();
		} catch (Exception e) {
			ourLog.error("Failed to collapse stats", e);
		}
	}

	@PostConstruct
	public void postConstruct() {
		ourLog.info("Scheduler has started");
		mySecuritySvc.loadUserCatalogIfNeeded();
	}

	@Override
	@Schedule(second = "0", minute = "*", hour = "*", persistent = false)
	public void recordNodeStats() {
		try {
			myStatsSvc.recordNodeStatistics();
		} catch (Exception e) {
			ourLog.error("Failed to load user catalog", e);
		}
	}

	@Override
	@Schedule(second = "0", minute = "*", hour = "*", persistent = false)
	public void reloadUserRegistry() {
		try {
			mySecuritySvc.loadUserCatalogIfNeeded();
		} catch (Exception e) {
			ourLog.error("Failed to load user catalog", e);
		}
	}

}
