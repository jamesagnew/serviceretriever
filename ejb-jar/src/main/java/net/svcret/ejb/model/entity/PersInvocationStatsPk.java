package net.svcret.ejb.model.entity;

import java.util.Date;

import javax.persistence.Embeddable;

@Embeddable
public class PersInvocationStatsPk extends BasePersInvocationMethodStatsPk {

	private static final long serialVersionUID = 1L;

	public PersInvocationStatsPk() {
		super();
	}

	public PersInvocationStatsPk(InvocationStatsIntervalEnum theInterval, Date theStartTime, PersServiceVersionMethod theMethod) {
		super(theInterval, theStartTime, theMethod);
	}

	public PersInvocationStatsPk(InvocationStatsIntervalEnum theInterval, long theStartTime, PersServiceVersionMethod theMethod) {
		super(theInterval, new Date(theStartTime), theMethod);
	}

	public PersInvocationStatsPk(InvocationStatsIntervalEnum theToIntervalTyoe, Date theStartTime, long theMethodPid) {
		super(theToIntervalTyoe, theStartTime, theMethodPid);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PersInvocationStats newObjectInstance() {
		return new PersInvocationStats(this);
	}
	
	@Override
	protected boolean doEquals(BasePersInvocationStatsPk theObj2) {
		PersInvocationStatsPk theObj = (PersInvocationStatsPk) theObj2;
		return getInterval().equals(theObj.getInterval()) // -
				&& getMethodPid() == (theObj.getMethodPid()) // -
				&& getStartTime().equals(theObj.getStartTime()); // -
	}


}
