package net.svcret.ejb.model.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import net.sf.ehcache.pool.sizeof.annotations.IgnoreSizeOf;

import com.google.common.base.Objects;
import com.google.common.base.Objects.ToStringHelper;

@MappedSuperclass
public abstract class BasePersInvocationMethodStatsPk extends BasePersInvocationStatsPk implements Serializable {

	private static final long serialVersionUID = 417296078781529579L;

	@Transient
	private volatile int myHashCode;

	@JoinColumn(name = "METHOD_PID", nullable = false)
	@ManyToOne(fetch = FetchType.LAZY, cascade = {})
	@IgnoreSizeOf
	private transient PersServiceVersionMethod myMethod;

	@Column(name="METHOD_PID", nullable=false)
	private Long myMethodPid;

	@Override
	protected void doHashCode(HashCodeBuilder theB) {
		theB.append(myMethodPid);
	}


	public BasePersInvocationMethodStatsPk() {
		super();
	}

	public BasePersInvocationMethodStatsPk(InvocationStatsIntervalEnum theInterval, Date theStartTime, PersServiceVersionMethod theMethod) {
		super(theInterval, theStartTime);
		myMethod = theMethod;
		myMethodPid = theMethod.getPid();
	}

	public BasePersInvocationMethodStatsPk(InvocationStatsIntervalEnum theInterval, Date theStartTime, long theMethodPid) {
		super(theInterval, theStartTime);
		myMethodPid = theMethodPid;
	}

	/**
	 * @return the method
	 */
	public PersServiceVersionMethod getMethod() {
		return myMethod;
	}

	public long getMethodPid() {
		return myMethodPid;
	}

	@Override
	ToStringHelper getToStringHelper() {
		return super.getToStringHelper().add("Method", getMethod().getName());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		if (myHashCode == 0) {
			myHashCode = Objects.hashCode(super.hashCode(), myMethodPid);
		}
		return myHashCode;
	}
	
	@Override
	public abstract BasePersInvocationStats newObjectInstance();

	/**
	 * @param theMethod
	 *            the method to set
	 */
	public void setMethod(PersServiceVersionMethod theMethod) {
		myMethod = theMethod;
	}


	
	@Override
	public String toString() {
		return getToStringHelper().toString();
	}


}
