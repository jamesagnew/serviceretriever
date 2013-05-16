package net.svcret.ejb.model.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import com.google.common.base.Objects;
import com.google.common.base.Objects.ToStringHelper;

@MappedSuperclass
public abstract class BasePersInvocationMethodStatsPk extends BasePersInvocationStatsPk implements Serializable {

	private static final long serialVersionUID = 417296078781529579L;

	@JoinColumn(name = "METHOD_PID", nullable = false)
	@ManyToOne(fetch = FetchType.LAZY, cascade = {})
	private PersServiceVersionMethod myMethod;

	@Transient
	private volatile int myHashCode;

	public BasePersInvocationMethodStatsPk() {
		super();
	}

	public BasePersInvocationMethodStatsPk(InvocationStatsIntervalEnum theInterval, Date theStartTime, PersServiceVersionMethod theMethod) {
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
	
	@Override
	public String toString() {
		return getToStringHelper().toString();
	}

	@Override
	ToStringHelper getToStringHelper() {
		return super.getToStringHelper().add("Method", getMethod().getName());
	}


	
	/**
	 * @param theMethod
	 *            the method to set
	 */
	public void setMethod(PersServiceVersionMethod theMethod) {
		myMethod = theMethod;
	}


}
