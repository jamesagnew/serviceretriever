package net.svcret.ejb.ejb;

import java.lang.management.ManagementFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.management.MBeanServer;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.management.ManagementService;
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
public class SchedulerBean implements IScheduler {

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(SchedulerBean.class);

	private CacheManager myCacheManager;

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
	
	private ManagementService registry;

	@Override
	@Schedule(second = "0", minute = "*", hour = "*")
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
	@Schedule(second = "0", minute = "*", hour = "*")
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
	@Schedule(second = "0", minute = "*", hour = "*")
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

		ourLog.info("Registering MBeans");
		myCacheManager = CacheManager.getInstance();
		MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
		registry = new ManagementService(myCacheManager, mBeanServer, true, true, true, true, true);

		registry.init();
	}

	@Override
	@Schedule(second = "0", minute = "*", hour = "*")
	public void reloadUserRegistry() {
		try {
			mySecuritySvc.loadUserCatalogIfNeeded();
		} catch (Exception e) {
			ourLog.error("Failed to load user catalog", e);
		}
	}

	@PreDestroy
	public void unregisterMBeans() {
		ourLog.info("Unegistering MBeans");
		registry.dispose();
	}

}
