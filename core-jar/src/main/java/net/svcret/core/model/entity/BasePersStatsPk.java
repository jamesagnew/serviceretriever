package net.svcret.core.model.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import net.svcret.admin.shared.enm.InvocationStatsIntervalEnum;
import net.svcret.core.model.entity.BasePersStats.IStatsVisitor;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.Objects;
import com.google.common.base.Objects.ToStringHelper;

@MappedSuperclass
public abstract class BasePersStatsPk<P extends BasePersStatsPk<P,O>, O extends BasePersStats<P,O>> implements Serializable {

	private static final long serialVersionUID = 1L;

	@Transient
	private volatile int myHashCode;

	@Column(name = "INTRVL", length = 10, nullable = false)
	@Enumerated(EnumType.STRING)
	private InvocationStatsIntervalEnum myInterval;

	@Column(name = "START_TIME", nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date myStartTime;

	public BasePersStatsPk() {
		super();
	}

	public BasePersStatsPk(InvocationStatsIntervalEnum theInterval, Date theStartTime) {
		super();
		myInterval = theInterval;
		myStartTime = theInterval.truncate(theStartTime);
	}

	public abstract P newPk(InvocationStatsIntervalEnum theInterval, Date theStartTime);
	
	protected abstract boolean doEquals(P theObj);

	protected abstract void doHashCode(HashCodeBuilder theB);

	public abstract Class<O> getStatType();
	
	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object theObj) {
		if (theObj == null || !theObj.getClass().equals(getClass())) {
			return false;
		}

		return doEquals((P) theObj);
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

	public abstract <T> T accept(IStatsVisitor<T> theVisitor);

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

	public abstract O newObjectInstance();

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
