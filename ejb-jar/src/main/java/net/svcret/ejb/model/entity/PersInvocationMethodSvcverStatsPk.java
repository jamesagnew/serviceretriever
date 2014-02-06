package net.svcret.ejb.model.entity;

import java.util.Date;

import javax.persistence.Embeddable;

import net.svcret.admin.shared.enm.InvocationStatsIntervalEnum;
import net.svcret.ejb.model.entity.BasePersStats.IStatsVisitor;

@Embeddable
public class PersInvocationMethodSvcverStatsPk extends BasePersInvocationMethodStatsPk<PersInvocationMethodSvcverStatsPk, PersInvocationMethodSvcverStats> {

	private static final long serialVersionUID = 1L;
	
	public PersInvocationMethodSvcverStatsPk() {
		super();
	}

	public PersInvocationMethodSvcverStatsPk(InvocationStatsIntervalEnum theInterval, Date theStartTime, long theMethodPid) {
		super(theInterval, theStartTime, theMethodPid);
	}


	public PersInvocationMethodSvcverStatsPk(InvocationStatsIntervalEnum theInterval, Date theStartTime, PersMethod theMethod) {
		super(theInterval, theStartTime, theMethod.getPid());
	}

	public PersInvocationMethodSvcverStatsPk(InvocationStatsIntervalEnum theInterval, long theStartTime, long theMethodPid) {
		super(theInterval, new Date(theStartTime), theMethodPid);
	}

	public PersInvocationMethodSvcverStatsPk(InvocationStatsIntervalEnum theInterval, long theStartTime, PersMethod theMethod) {
		super(theInterval, new Date(theStartTime), theMethod.getPid());
	}

	@Override
	public <T> T accept(IStatsVisitor<T> theVisitor) {
		return theVisitor.visit(null, this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PersInvocationMethodSvcverStats newObjectInstance() {
		return new PersInvocationMethodSvcverStats(this);
	}

	@Override
	protected boolean doEquals(PersInvocationMethodSvcverStatsPk theObj) {
		return getInterval().equals(theObj.getInterval()) // -
				&& getMethod() == (theObj.getMethod()) // -
				&& getStartTime().equals(theObj.getStartTime()); // -
	}

	@Override
	public PersInvocationMethodSvcverStatsPk newPk(InvocationStatsIntervalEnum theInterval, Date theStartTime) {
		return new PersInvocationMethodSvcverStatsPk(theInterval, theStartTime, getMethod());
	}

	@Override
	public Class<PersInvocationMethodSvcverStats> getStatType() {
		return PersInvocationMethodSvcverStats.class;
	}

}
