package net.svcret.ejb.model.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import net.svcret.ejb.model.entity.BasePersStats.IStatsVisitor;

import org.apache.commons.lang3.builder.HashCodeBuilder;

@Embeddable
public class PersInvocationUrlStatsPk extends BasePersStatsPk<PersInvocationUrlStatsPk, PersInvocationUrlStats> {

	private static final long serialVersionUID = 1L;
	
	@Column(name="URL_PID", nullable=false)
	private long myUrlPid;
	
	public PersInvocationUrlStatsPk() {
		super();
	}

	public PersInvocationUrlStatsPk(InvocationStatsIntervalEnum theInterval, Date theStartTime, long theUrlPid) {
		super(theInterval, theStartTime);
		
		myUrlPid=theUrlPid;
	}


	public PersInvocationUrlStatsPk(InvocationStatsIntervalEnum theInterval, Date theStartTime, PersServiceVersionUrl theUrl) {
		super(theInterval, theStartTime);
		myUrlPid=theUrl.getPid();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PersInvocationUrlStats newObjectInstance() {
		return new PersInvocationUrlStats(this);
	}

	@Override
	protected boolean doEquals(PersInvocationUrlStatsPk theObj) {
		return getInterval().equals(theObj.getInterval()) // -
				&& getUrlPid() == (theObj.getUrlPid()) // -
				&& getStartTime().equals(theObj.getStartTime()); // -
	}

	public long getUrlPid() {
		return myUrlPid;
	}

	@Override
	protected void doHashCode(HashCodeBuilder theB) {
		theB.append(myUrlPid);
	}

	@Override
	public <T> T accept(IStatsVisitor<T> theVisitor) {
		return theVisitor.visit(null, this);
	}

	@Override
	public PersInvocationUrlStatsPk newPk(InvocationStatsIntervalEnum theInterval, Date theStartTime) {
		return new PersInvocationUrlStatsPk(theInterval, theStartTime, getUrlPid());
	}

	@Override
	public Class<PersInvocationUrlStats> getStatType() {
		return PersInvocationUrlStats.class;
	}

}
