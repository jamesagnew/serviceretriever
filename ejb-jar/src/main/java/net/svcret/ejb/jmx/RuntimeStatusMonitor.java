package net.svcret.ejb.jmx;

import java.lang.management.ManagementFactory;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import net.svcret.ejb.api.IRuntimeStatus;

@Singleton
@Startup
public class RuntimeStatusMonitor implements RuntimeStatusMonitorMBean {


	@EJB
	private IRuntimeStatus myRuntimeStatus;
	
    private MBeanServer myPlatformMBeanServer;
    private ObjectName myObjectName = null;

    @Override
    public int getCachedPopulatedStatCount() {
    	return myRuntimeStatus.getCachedPopulatedKeyCount();
    }
    
    @Override
    public int getCachedNullStatCount() {
    	return myRuntimeStatus.getCachedEmptyKeyCount();
    }

    
    @PostConstruct
    public void registerInJMX() {
        try {
            myObjectName = new ObjectName("net.svcret:type=" + this.getClass().getName());
            myPlatformMBeanServer = ManagementFactory.getPlatformMBeanServer();
            myPlatformMBeanServer.registerMBean(this, myObjectName);
        } catch (Exception e) {
            throw new IllegalStateException("Problem during registration of Monitoring into JMX:" + e);
        }
    }

    @PreDestroy
    public void unregisterFromJMX() {
        try {
            myPlatformMBeanServer.unregisterMBean(this.myObjectName);
        } catch (Exception e) {
            throw new IllegalStateException("Problem during unregistration of Monitoring into JMX:" + e);
        }
    }
}