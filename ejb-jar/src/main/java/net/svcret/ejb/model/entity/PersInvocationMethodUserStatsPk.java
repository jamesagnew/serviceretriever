package net.svcret.ejb.model.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Transient;

import net.svcret.admin.shared.enm.InvocationStatsIntervalEnum;
import net.svcret.ejb.model.entity.BasePersStats.IStatsVisitor;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.Objects;
import com.google.common.base.Objects.ToStringHelper;

@Embeddable
public class PersInvocationMethodUserStatsPk extends BasePersInvocationMethodStatsPk<PersInvocationMethodUserStatsPk, PersInvocationMethodUserStats> {

	private static final long serialVersionUID = 1L;

	@Transient
	private volatile int myHashCode;

//	@JoinColumn(name = "USER_PID", nullable = false)
//	@ManyToOne(fetch = FetchType.LAZY, cascade = {})
//	@IgnoreSizeOf
	
	@Column(name = "USER_PID", nullable = false)
	private long myUserPid;

	public PersInvocationMethodUserStatsPk() {
		super();
	}

	public PersInvocationMethodUserStatsPk(InvocationStatsIntervalEnum theInterval, Date theStartTime, long theMethod, long theUser) {
		super(theInterval, theStartTime, theMethod);
		myUserPid = theUser;
	}

	public PersInvocationMethodUserStatsPk(InvocationStatsIntervalEnum theInterval, Date theStartTime, PersMethod theMethod, PersUser theUser) {
		super(theInterval, theStartTime, theMethod.getPid());
		myUserPid = theUser.getPid();
	}

	@Override
	protected boolean doEquals(PersInvocationMethodUserStatsPk theObj) {
		return getInterval().equals(theObj.getInterval()) // -
				&& getMethod()== theObj.getMethod() // -
				&& getStartTime().equals(theObj.getStartTime()) // -
				&& getUserPid()== (theObj.getUserPid()); // -
	}

	@Override
	protected void doHashCode(HashCodeBuilder theB) {
		theB.append(myUserPid);
	}


	@Override
	ToStringHelper getToStringHelper() {
		return super.getToStringHelper().add("User", getUserPid());
	}


	/**
	 * @return the user
	 */
	public long getUserPid() {
		return myUserPid;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		if (myHashCode == 0) {
			myHashCode = Objects.hashCode(super.hashCode(), myUserPid, getMethod());
		}
		return myHashCode;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PersInvocationMethodUserStats newObjectInstance() {
		return new PersInvocationMethodUserStats(this);
	}

	public void setUserPid(long theUserPid) {
		myUserPid = theUserPid;
	}

	@Override
	public String toString() {
		return getToStringHelper().toString();
	}

	@Override
	public PersInvocationMethodUserStatsPk newPk(InvocationStatsIntervalEnum theInterval, Date theStartTime) {
		return new PersInvocationMethodUserStatsPk(theInterval, theStartTime, getMethod(), getUserPid());
	}

	@Override
	public <T> T accept(IStatsVisitor<T> theVisitor) {
		return theVisitor.visit(null, this);
	}
	
	@Override
	public Class<PersInvocationMethodUserStats> getStatType() {
		return PersInvocationMethodUserStats.class;
	}


}
