package net.svcret.ejb.model.entity;

import java.util.Date;

import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import net.sf.ehcache.pool.sizeof.annotations.IgnoreSizeOf;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.Objects.ToStringHelper;

@Embeddable
public class PersStaticResourceStatsPk extends BasePersInvocationStatsPk {

	private static final long serialVersionUID = 1L;

	@Transient
	private volatile int myHashCode;

	@ManyToOne(cascade = {})
	@JoinColumn(name = "RESOURCE_PID", referencedColumnName = "PID", nullable = false)
	@IgnoreSizeOf
	private PersServiceVersionResource myResource;

	public PersStaticResourceStatsPk() {
		super();
	}

	public PersStaticResourceStatsPk(InvocationStatsIntervalEnum theInterval, Date theStartTime, PersServiceVersionResource theResource) {
		super(theInterval, theStartTime);
		myResource = theResource;
	}

	@Override
	protected boolean doEquals(BasePersInvocationStatsPk theObj) {
		PersStaticResourceStatsPk obj = (PersStaticResourceStatsPk) theObj;
		return getInterval().equals(obj.getInterval()) // -
				&& getResourcePid().equals(obj.getResourcePid()) // -
				&& getStartTime().equals(obj.getStartTime()); // -
	}

	@Override
	protected void doHashCode(HashCodeBuilder theB) {
		theB.append(myResource);
	}

	/**
	 * @return the user
	 */
	public PersServiceVersionResource getResource() {
		return myResource;
	}

	/**
	 * @return the user
	 */
	public Long getResourcePid() {
		return getResource().getPid();
	}

	@Override
	ToStringHelper getToStringHelper() {
		return super.getToStringHelper().add("Resource", getResource().getResourceUrl());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PersStaticResourceStats newObjectInstance() {
		return new PersStaticResourceStats(this);
	}

	/**
	 * @param theResource
	 *            the user to set
	 */
	public void setResource(PersServiceVersionResource theResource) {
		myResource = theResource;
	}

	@Override
	public String toString() {
		return getToStringHelper().toString();
	}

}
