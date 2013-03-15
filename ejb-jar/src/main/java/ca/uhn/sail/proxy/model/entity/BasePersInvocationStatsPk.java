package ca.uhn.sail.proxy.model.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import com.google.common.base.Objects;

@MappedSuperclass
public abstract class BasePersInvocationStatsPk extends BasePersMethodStatsPk implements Serializable {

	private static final long serialVersionUID = 417296078781529579L;

	@JoinColumn(name = "METHOD_PID", nullable = false)
	@ManyToOne(fetch = FetchType.LAZY, cascade = {})
	private PersServiceVersionMethod myMethod;

	@Transient
	private volatile int myHashCode;

	public BasePersInvocationStatsPk() {
		super();
	}

	public BasePersInvocationStatsPk(InvocationStatsIntervalEnum theInterval, Date theStartTime, PersServiceVersionMethod theMethod) {
		super(theInterval, theStartTime);
		myMethod = theMethod;
	}

	@Override
	public abstract BasePersInvocationStats newObjectInstance();

	/**
	 * @return the method
	 */
	public PersServiceVersionMethod getMethod() {
		return myMethod;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		if (myHashCode == 0) {
			myHashCode = Objects.hashCode(super.hashCode(), myMethod);
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
	 * @param theMethod
	 *            the method to set
	 */
	public void setMethod(PersServiceVersionMethod theMethod) {
		myMethod = theMethod;
	}


}
