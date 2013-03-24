package net.svcret.ejb.model.entity;

import java.util.Date;

import javax.persistence.Embeddable;
import javax.persistence.Transient;

import com.google.common.base.Objects;

@Embeddable
public class PersInvocationAnonStatsPk extends BasePersInvocationStatsPk {

	private static final long serialVersionUID = 1L;

	@Transient
	private volatile int myHashCode;
	
	public PersInvocationAnonStatsPk() {
		super();
	}

	public PersInvocationAnonStatsPk(InvocationStatsIntervalEnum theInterval, Date theStartTime, PersServiceVersionMethod theMethod) {
		super(theInterval,theStartTime,theMethod);
	}
	

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		if (myHashCode == 0) {
			myHashCode = Objects.hashCode(super.hashCode());
		}
		return myHashCode;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return Objects.toStringHelper(this).add("Interval", getInterval().name()).add("StartTime", getStartTime()).add("Method", getMethod().getPid()).toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PersInvocationAnonStats newObjectInstance() {
		return new PersInvocationAnonStats(this);
	}

	@Override
	protected boolean doEquals(BasePersMethodStatsPk theObj) {
		PersInvocationAnonStatsPk obj = (PersInvocationAnonStatsPk)theObj;
		return getInterval().equals(obj.getInterval()) // -
				&& getMethod().equals(obj.getMethod()) // -
				&& getStartTime().equals(obj.getStartTime()); // -
	}
	
	
	
}
