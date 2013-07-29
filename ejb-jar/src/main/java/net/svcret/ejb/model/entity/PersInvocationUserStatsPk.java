package net.svcret.ejb.model.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Transient;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.Objects;
import com.google.common.base.Objects.ToStringHelper;

@Embeddable
public class PersInvocationUserStatsPk extends BasePersInvocationStatsPk {

	private static final long serialVersionUID = 1L;

	@Transient
	private volatile int myHashCode;

//	@JoinColumn(name = "USER_PID", nullable = false)
//	@ManyToOne(fetch = FetchType.LAZY, cascade = {})
//	@IgnoreSizeOf
	
	@Column(name = "USER_PID", nullable = false)
	private long myUserPid;

	public PersInvocationUserStatsPk() {
		super();
	}

	public PersInvocationUserStatsPk(InvocationStatsIntervalEnum theInterval, Date theStartTime, long theUser) {
		super(theInterval, theStartTime);
		myUserPid = theUser;
	}

	public PersInvocationUserStatsPk(InvocationStatsIntervalEnum theInterval, Date theStartTime, PersUser theUser) {
		super(theInterval, theStartTime);
		myUserPid = theUser.getPid();
	}

	@Override
	protected boolean doEquals(BasePersInvocationStatsPk theObj) {
		PersInvocationUserStatsPk obj = (PersInvocationUserStatsPk) theObj;
		return getInterval().equals(obj.getInterval()) // -
				&& getStartTime().equals(obj.getStartTime()) // -
				&& getUserPid()== (obj.getUserPid()); // -
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
			myHashCode = Objects.hashCode(super.hashCode(), myUserPid);
		}
		return myHashCode;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PersInvocationUserStats newObjectInstance() {
		return new PersInvocationUserStats(this);
	}

	public void setUserPid(long theUserPid) {
		myUserPid = theUserPid;
	}

	@Override
	public String toString() {
		return getToStringHelper().toString();
	}

}
