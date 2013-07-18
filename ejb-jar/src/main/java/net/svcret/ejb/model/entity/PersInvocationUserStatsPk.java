package net.svcret.ejb.model.entity;

import java.util.Date;

import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import net.sf.ehcache.pool.sizeof.annotations.IgnoreSizeOf;

import com.google.common.base.Objects;
import com.google.common.base.Objects.ToStringHelper;

@Embeddable
public class PersInvocationUserStatsPk extends BasePersInvocationStatsPk {

	private static final long serialVersionUID = 1L;

	@Transient
	private volatile int myHashCode;

	@JoinColumn(name = "USER_PID", nullable = false)
	@ManyToOne(fetch = FetchType.LAZY, cascade = {})
	@IgnoreSizeOf
	private PersUser myUser;

	public PersInvocationUserStatsPk() {
		super();
	}

	public PersInvocationUserStatsPk(InvocationStatsIntervalEnum theInterval, Date theStartTime, PersUser theUser) {
		super(theInterval, theStartTime);
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

		PersInvocationUserStatsPk pk = (PersInvocationUserStatsPk) theObj;
		return super.equals(pk) // /-
				&& myUser.equals(pk.getUser()); // -
	}

	@Override
	protected boolean doEquals(BasePersInvocationStatsPk theObj) {
		PersInvocationUserStatsPk obj = (PersInvocationUserStatsPk) theObj;
		return getInterval().equals(obj.getInterval()) // -
				&& getStartTime().equals(obj.getStartTime()) // -
				&& getUser().equals(obj.getUser()); // -
	}

	/**
	 * @return the user
	 */
	public PersUser getUser() {
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
	 * @param theUser
	 *            the user to set
	 */
	public void setUser(PersUser theUser) {
		myUser = theUser;
	}

	@Override
	public String toString() {
		return getToStringHelper().toString();
	}

	@Override
	ToStringHelper getToStringHelper() {
		return super.getToStringHelper().add("User", getUser().getUsername());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PersInvocationUserStats newObjectInstance() {
		return new PersInvocationUserStats(this);
	}

}
