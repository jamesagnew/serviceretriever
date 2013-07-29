package net.svcret.ejb.model.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.Objects;
import com.google.common.base.Objects.ToStringHelper;

@MappedSuperclass
public abstract class BasePersInvocationStatsPk implements Serializable {

	private static final long serialVersionUID = 1L;

	@Transient
	private volatile int myHashCode;

	@Column(name = "INTRVL", length = 10, nullable = false)
	@Enumerated(EnumType.STRING)
	private InvocationStatsIntervalEnum myInterval;

	@Column(name = "START_TIME", nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date myStartTime;

	public BasePersInvocationStatsPk() {
		super();
	}

	public BasePersInvocationStatsPk(InvocationStatsIntervalEnum theInterval, Date theStartTime) {
		super();
		myInterval = theInterval;
		myStartTime = theInterval.truncate(theStartTime);
	}

	protected abstract boolean doEquals(BasePersInvocationStatsPk theObj);

	protected abstract void doHashCode(HashCodeBuilder theB);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object theObj) {
		if (theObj == null || !theObj.getClass().equals(getClass())) {
			return false;
		}

		return doEquals((BasePersInvocationStatsPk) theObj);
	}

	/**
	 * @return the interval
	 */
	public InvocationStatsIntervalEnum getInterval() {
		return myInterval;
	}

	/**
	 * @return the startTime
	 */
	public Date getStartTime() {
		return myStartTime;
	}

	ToStringHelper getToStringHelper() {
		return Objects.toStringHelper(this).add("Interval", getInterval().name()).add("StartTime", myStartTime);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		if (myHashCode == 0) {
			HashCodeBuilder b = new HashCodeBuilder();
			b.append(myInterval);
			b.append(myStartTime);
			doHashCode(b);
			myHashCode = b.toHashCode();
		}
		return myHashCode;
	}

	public abstract BasePersInvocationStats newObjectInstance();

	/**
	 * @param theInterval
	 *            the interval to set
	 */
	public void setInterval(InvocationStatsIntervalEnum theInterval) {
		myInterval = theInterval;
	}

	/**
	 * @param theStartTime
	 *            the startTime to set
	 */
	public void setStartTime(Date theStartTime) {
		myStartTime = theStartTime;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return getToStringHelper().toString();
	}

}
