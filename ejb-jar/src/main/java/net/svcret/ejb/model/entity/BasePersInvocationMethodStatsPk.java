package net.svcret.ejb.model.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.Objects.ToStringHelper;

@MappedSuperclass
public abstract class BasePersInvocationMethodStatsPk<P extends BasePersInvocationMethodStatsPk<P,O>, O extends BasePersInvocationStats<P,O>> extends BasePersStatsPk<P,O> implements Serializable {

	private static final long serialVersionUID = 417296078781529579L;

	@Transient
	private volatile int myHashCode;

	@Column(name = "METHOD_PID")
	private long myMethod;

	public BasePersInvocationMethodStatsPk() {
		super();
	}

	public BasePersInvocationMethodStatsPk(InvocationStatsIntervalEnum theInterval, Date theStartTime, long theMethod) {
		super(theInterval, theStartTime);
		Validate.notNull(theMethod);
		myMethod = theMethod;
	}

	@Override
	protected void doHashCode(HashCodeBuilder theB) {
		theB.append(myMethod);
	}

	/**
	 * @return the method
	 */
	public long getMethod() {
		return myMethod;
	}

	@Override
	ToStringHelper getToStringHelper() {
		ToStringHelper helper = super.getToStringHelper();
		helper = helper.add("Method", getMethod());
		return helper;
	}

	@Override
	public abstract O newObjectInstance();

	@Override
	public String toString() {
		return getToStringHelper().toString();
	}

}
