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
	private ISecurityService mySecuritySvc;

	@EJB
	private IRuntimeStatus myStatsSvc;

	@EJB
	private ITransactionLogger myTransactionLogger;

	@EJB
	private IMonitorService myMonitorSvc;
	
	private ManagementService registry;

	@Override
	@Schedule(second = "0", minute = "*", hour = "*")
	@TransactionAttribute(TransactionAttributeType.NEVER)
	public void collapseStats() {
		try {
			myStatsSvc.collapseStats();
		} catch (Exception e) {
			ourLog.error("Failed to collapse stats", e);
		}
	}

	@Override
	@Schedule(second = "0", minute = "*", hour = "*")
	@TransactionAttribute(TransactionAttributeType.NEVER)
	public void monitorCheck() {
		try {
			myMonitorSvc.check();
		} catch (Exception e) {
			ourLog.error("Failed to collapse stats", e);
		}
	}

	@Override
	@Schedule(second = "0", minute = "*", hour = "*")
	@TransactionAttribute(TransactionAttributeType.NEVER)
	public void flushStats() {
		try {
			myStatsSvc.flushStatus();
		} catch (Exception e) {
			ourLog.error("Failed to flush stats", e);
		}
	}

	@Override
	@Schedule(second = "0", minute = "*", hour = "*")
	public void flushTransactionLogs() {
		try {
			myTransactionLogger.flush();
		} catch (Exception e) {
			ourLog.error("Failed to flush transaction logs", e);
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
