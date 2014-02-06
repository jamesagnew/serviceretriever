package net.svcret.ejb.ejb;

import java.util.Date;

import javax.annotation.PostConstruct;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import net.svcret.admin.shared.model.RetrieverNodeTypeEnum;
import net.svcret.ejb.api.IConfigService;
import net.svcret.ejb.api.IRuntimeStatus;
import net.svcret.ejb.api.IScheduler;
import net.svcret.ejb.api.ISecurityService;
import net.svcret.ejb.ejb.monitor.IMonitorService;
import net.svcret.ejb.ejb.nodecomm.ISynchronousNodeIpcClient;
import net.svcret.ejb.log.IFilesystemAuditLogger;
import net.svcret.ejb.log.ITransactionLogger;

@Startup()
@Singleton()
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class SchedulerBean implements IScheduler {

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(SchedulerBean.class);

	@EJB
	@Autowired
	private IConfigService myConfigSvc;

	@EJB
	@Autowired
	private IFilesystemAuditLogger myFilesystemAuditLogger;

	private long myLastRecentMessagesFlush;

	private long myLastStatsFlush;

	@EJB
	@Autowired
	private IMonitorService myMonitorSvc;

	@EJB
	@Autowired
	private ISecurityService mySecuritySvc;

	@EJB
	@Autowired
	private IRuntimeStatus myStatsSvc;
	
	@EJB
	@Autowired
	private ISynchronousNodeIpcClient mySynchronousNodeIpcClient;

	@EJB
	@Autowired
	private ITransactionLogger myTransactionLogger;

	@Override
	@Schedule(second = "0", minute = "*", hour = "*", persistent = false)
	@Scheduled(fixedDelay=DateUtils.MILLIS_PER_HOUR)
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
	@Scheduled(fixedDelay=DateUtils.MILLIS_PER_MINUTE)
	public void flushFilesystemAuditEvents() {
		try {
			ourLog.debug("flushFilesystemAuditEvents()");
			myFilesystemAuditLogger.flushAuditEventsIfNeeded();
		} catch (Exception e) {
			ourLog.error("Failed to collapse stats", e);
		}
	}

	@Override
	@Scheduled(fixedDelay=15L * DateUtils.MILLIS_PER_MINUTE)
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
	@Scheduled(fixedDelay=DateUtils.MILLIS_PER_MINUTE)
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
	@Scheduled(fixedDelay=DateUtils.MILLIS_PER_MINUTE)
	public void monitorRunChecks() {
		try {
			if (myConfigSvc.getNodeType() != RetrieverNodeTypeEnum.PRIMARY) {
				return;
			}

			myMonitorSvc.runActiveChecks();
			myMonitorSvc.runPassiveChecks();
		} catch (Exception e) {
			ourLog.error("Failed to collapse stats", e);
		}
	}


	@Override
	@Schedule(second = "0", minute = "*", hour = "*", persistent = false)
	@Scheduled(fixedDelay=DateUtils.MILLIS_PER_MINUTE)
	public void recordNodeStats() {
		try {
			myStatsSvc.recordNodeStatistics();
		} catch (Exception e) {
			ourLog.error("Failed to load user catalog", e);
		}
	}

	@Override
	@Schedule(second = "0", minute = "*", hour = "*", persistent = false)
	@Scheduled(fixedDelay=DateUtils.MILLIS_PER_MINUTE)
	public void reloadUserRegistry() {
		try {
			mySecuritySvc.loadUserCatalogIfNeeded();
		} catch (Exception e) {
			ourLog.error("Failed to load user catalog", e);
		}
	}

}
