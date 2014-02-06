package net.svcret.ejb.model.entity;

import java.util.Date;

import javax.persistence.MappedSuperclass;

import net.svcret.admin.shared.enm.InvocationStatsIntervalEnum;

@MappedSuperclass
public abstract class BasePersInvocationStatsPk<P extends BasePersInvocationStatsPk<P,O>, O extends BasePersInvocationStats<P,O>> extends BasePersStatsPk<P, O> {

	public BasePersInvocationStatsPk() {
	}
	
	public BasePersInvocationStatsPk(InvocationStatsIntervalEnum theInterval, Date theStartTime) {
		super(theInterval, theStartTime);
	}

	private static final long serialVersionUID = 1L;

}
