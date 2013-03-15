package ca.uhn.sail.proxy.model.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import com.google.common.base.Objects;

@MappedSuperclass
public abstract class BasePersMethodStatsPk implements Serializable {

	private static final long serialVersionUID = 1L;

	@Transient
	private volatile int myHashCode;

	@Column(name = "INTERVAL", length = 10, nullable = false)
	@Enumerated(EnumType.STRING)
	private InvocationStatsIntervalEnum myInterval;

	@Column(name = "START_TIME", nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date myStartTime;

	public BasePersMethodStatsPk() {
		super();
	}

	public BasePersMethodStatsPk(InvocationStatsIntervalEnum theInterval, Date theStartTime) {
		super();
		myInterval = theInterval;
		myStartTime = theInterval.truncate(theStartTime);
	}

	protected abstract boolean doEquals(BasePersMethodStatsPk theObj);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object theObj) {
		if (theObj == null || !theObj.getClass().equals(getClass())) {
			return false;
		}

		return doEquals((BasePersMethodStatsPk) theObj);
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


	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		if (myHashCode == 0) {
			myHashCode = Objects.hashCode(myInterval, myStartTime);
		}
		return myHashCode;
	}

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
		return Objects.toStringHelper(this).add("Interval", getInterval().name()).add("StartTime", myStartTime).toString();
	}

	public abstract BasePersMethodStats newObjectInstance();

}
