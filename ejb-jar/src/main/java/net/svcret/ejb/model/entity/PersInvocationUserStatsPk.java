package net.svcret.ejb.model.entity;

import java.util.Date;

import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import com.google.common.base.Objects;

@Embeddable
public class PersInvocationUserStatsPk extends BasePersInvocationStatsPk {

	private static final long serialVersionUID = 1L;

	@Transient
	private volatile int myHashCode;
	
	@JoinColumn(name="USER_PID", nullable=false)
	@ManyToOne(fetch=FetchType.LAZY, cascade= {})
	private PersServiceUser myUser;
	
	public PersInvocationUserStatsPk() {
		super();
	}

	public PersInvocationUserStatsPk(InvocationStatsIntervalEnum theInterval, Date theStartTime, PersServiceVersionMethod theMethod, PersServiceUser theUser) {
		super(theInterval,theStartTime,theMethod);
		myUser = theUser;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object theObj) {
		if (!(theObj instanceof PersInvocationUserStatsPk)) {
			return false;
		}
		
		PersInvocationUserStatsPk pk = (PersInvocationUserStatsPk)theObj;
		return super.equals(pk) ///-
				&& myUser.equals(pk.getUser()); //-
	}

	@Override
	protected boolean doEquals(BasePersMethodStatsPk theObj) {
		PersInvocationUserStatsPk obj = (PersInvocationUserStatsPk) theObj;
		return getInterval().equals(obj.getInterval()) // -
				&& getMethod().equals(obj.getMethod()) // -
				&& getStartTime().equals(obj.getStartTime()) // -
				&& getUser().equals(obj.getUser()); // -
	}

	
	/**
	 * @return the user
	 */
	public PersServiceUser getUser() {
		return myUser;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		if (myHashCode == 0) {
			myHashCode = Objects.hashCode(super.hashCode(), myUser);
		}
		return myHashCode;
	}

	/**
	 * @param theUser the user to set
	 */
	public void setUser(PersServiceUser theUser) {
		myUser = theUser;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return Objects.toStringHelper(this).add("Interval", getInterval().name()).add("StartTime", getStartTime()).add("Method", getMethod().getPid()).add("User", getUser().getPid()).toString();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public PersInvocationUserStats newObjectInstance() {
		return new PersInvocationUserStats(this);
	}

	
	
}
