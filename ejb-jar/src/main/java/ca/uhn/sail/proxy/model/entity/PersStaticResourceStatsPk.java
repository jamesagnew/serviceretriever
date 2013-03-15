package ca.uhn.sail.proxy.model.entity;

import java.util.Date;

import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import com.google.common.base.Objects;

@Embeddable
public class PersStaticResourceStatsPk extends BasePersMethodStatsPk {

	private static final long serialVersionUID = 1L;

	@Transient
	private volatile int myHashCode;
	
	@ManyToOne(cascade= {})
	@JoinColumn(name="RESOURCE_PID", referencedColumnName="PID", nullable=false)
	private PersServiceVersionResource myResource;
	
	public PersStaticResourceStatsPk() {
		super();
	}

	public PersStaticResourceStatsPk(InvocationStatsIntervalEnum theInterval, Date theStartTime, PersServiceVersionResource theResource) {
		super(theInterval,theStartTime);
		myResource = theResource;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object theObj) {
		if (!(theObj instanceof PersStaticResourceStatsPk)) {
			return false;
		}
		
		PersStaticResourceStatsPk pk = (PersStaticResourceStatsPk)theObj;
		return super.equals(pk) ///-
				&& myResource.equals(pk.getResource()); //-
	}

	@Override
	protected boolean doEquals(BasePersMethodStatsPk theObj) {
		PersStaticResourceStatsPk obj = (PersStaticResourceStatsPk) theObj;
		return getInterval().equals(obj.getInterval()) // -
				&& getResourcePid().equals(obj.getResourcePid()) // -
				&& getStartTime().equals(obj.getStartTime()); // -
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		if (myHashCode == 0) {
			myHashCode = Objects.hashCode(super.hashCode(), myResource);
		}
		return myHashCode;
	}

	/**
	 * @param theResource the user to set
	 */
	public void setResource(PersServiceVersionResource theResource) {
		myResource = theResource;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return Objects.toStringHelper(this).add("Interval", getInterval().name()).add("StartTime", getStartTime()).add("Method", getResourcePid()).add("ResourceUri", getResource().getResourceUrl()).toString();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public PersStaticResourceStats newObjectInstance() {
		return new PersStaticResourceStats(this);
	}

	
	
}
