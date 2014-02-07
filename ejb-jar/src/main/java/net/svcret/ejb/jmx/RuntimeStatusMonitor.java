package net.svcret.ejb.jmx;

import java.lang.management.ManagementFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import net.svcret.ejb.api.IRuntimeStatusQueryLocal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RuntimeStatusMonitor implements RuntimeStatusMonitorMBean {

	@Autowired
	private IRuntimeStatusQueryLocal myRuntimeStatusQuery;

	private MBeanServer myPlatformMBeanServer;
	private ObjectName myObjectName = null;

	@Override
	public int getCachedPopulatedStatCount() {
		return myRuntimeStatusQuery.getCachedPopulatedKeyCount();
	}

	@Override
	public int getCachedNullStatCount() {
		return myRuntimeStatusQuery.getCachedEmptyKeyCount();
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

	@Override
	public int getMaxCachedPopulatedStatCount() {
		return myRuntimeStatusQuery.getMaxCachedPopulatedStatCount();
	}

	@Override
	public int getMaxCachedNullStatCount() {
		return myRuntimeStatusQuery.getMaxCachedNullStatCount();
	}

	@Override
	public void setMaxCachedPopulatedStatCount(int theCount) {
		myRuntimeStatusQuery.setMaxCachedPopulatedStatCount(theCount);
	}

	@Override
	public void setMaxCachedNullStatCount(int theCount) {
		myRuntimeStatusQuery.setMaxCachedNullStatCount(theCount);
	}

	@Override
	public void purgeCachedStats() {
		myRuntimeStatusQuery.purgeCachedStats();
	}
}