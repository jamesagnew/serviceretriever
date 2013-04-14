package net.svcret.ejb.ejb;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import net.svcret.ejb.api.IRuntimeStatus;
import net.svcret.ejb.api.IScheduler;
import net.svcret.ejb.api.ISecurityService;

@Startup
@Singleton()
public class SchedulerBean implements IScheduler {

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(SchedulerBean.class);
	
	@EJB
	private ISecurityService mySecuritySvc;
	
	@EJB
	private IRuntimeStatus myStatsSvc;
	
	@PostConstruct
	public void postConstruct() {
		ourLog.info("Scheduler has started");
		mySecuritySvc.loadUserCatalogIfNeeded();
	}
	
	@Override
	@Schedule(second = "0", minute = "*", hour = "*")
	public void reloadUserRegistry() {
		mySecuritySvc.loadUserCatalogIfNeeded();
	}

	@Override
	@Schedule(second = "0", minute = "*", hour = "*")
	@TransactionAttribute(TransactionAttributeType.NEVER)
	public void flushStats() {
		myStatsSvc.flushStatus();
	}
	
}
