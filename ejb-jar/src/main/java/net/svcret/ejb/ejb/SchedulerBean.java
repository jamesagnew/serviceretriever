package net.svcret.ejb.ejb;

import javax.annotation.PostConstruct;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import net.svcret.admin.shared.model.RetrieverNodeTypeEnum;
import net.svcret.ejb.api.IConfigService;
import net.svcret.ejb.api.IHttpClient;
import net.svcret.ejb.api.IMonitorService;
import net.svcret.ejb.api.IRuntimeStatus;
import net.svcret.ejb.api.IScheduler;
import net.svcret.ejb.api.ISecurityService;
import net.svcret.ejb.api.ITransactionLogger;

@Startup
@Singleton()
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class SchedulerBean implements IScheduler {

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(SchedulerBean.class);

	@EJB
	private IMonitorService myMonitorSvc;

	@EJB
	private ISecurityService mySecuritySvc;

	@EJB
	private IConfigService myConfigSvc;
	
	@EJB
	private IRuntimeStatus myStatsSvc;

	@EJB
	private ITransactionLogger myTransactionLogger;

	@EJB
	private IHttpClient myHttpClient;
	
	@Override
	@Schedule(second = "0", minute = "*", hour = "*", persistent=false)
	@TransactionAttribute(TransactionAttributeType.NEVER)
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
	@Schedule(second = "0", minute = "*", hour = "*", persistent=false)
	@TransactionAttribute(TransactionAttributeType.NEVER)
	public void flushInMemoryStatisticsAndTransactionsPrimary() {
		try {
			if (myConfigSvc.getNodeType() != RetrieverNodeTypeEnum.PRIMARY) {
				return;
			}

			ourLog.debug("flushStats()");
			myStatsSvc.flushStatus();

			ourLog.debug("flushTransactionLogs()");
			myTransactionLogger.flush();

			for (String nextUrl : myConfigSvc.getSecondaryNodeRefreshUrls()) {
				ourLog.debug("Invoking secondary refresh URL: {}", nextUrl);
				myHttpClient.get(nextUrl);
			}
			
		} catch (Exception e) {
			ourLog.error("Failed to flush stats", e);
		}
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NEVER)
	public void flushInMemoryStatisticsAndTransactionsSecondary() {
		try {
			ourLog.debug("flushStats()");
			myStatsSvc.flushStatus();

			ourLog.debug("flushTransactionLogs()");
			myTransactionLogger.flush();
		} catch (Exception e) {
			ourLog.error("Failed to flush stats", e);
		}
	}

	
	@Override
	@Schedule(second = "0", minute = "*", hour = "*", persistent=false)
	@TransactionAttribute(TransactionAttributeType.NEVER)
	public void monitorCheck() {
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
	@Schedule(second = "0", minute = "*", hour = "*", persistent=false)
	@TransactionAttribute(TransactionAttributeType.NEVER)
	public void reloadUserRegistry() {
		try {
			mySecuritySvc.loadUserCatalogIfNeeded();
		} catch (Exception e) {
			ourLog.error("Failed to load user catalog", e);
		}
	}

}
