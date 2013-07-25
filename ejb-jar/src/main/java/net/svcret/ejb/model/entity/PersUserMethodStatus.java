package net.svcret.ejb.model.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity()
@Table(name = "PX_USER_METHOD_STATUS")
public class PersUserMethodStatus implements Serializable {

	private static final long serialVersionUID = 1L;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "LAST_FAIL_INVOC")
	private Date myLastFailInvocation;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "LAST_FAULT_INVOC")
	private Date myLastFaultInvocation;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "LAST_SECFAIL_INVOC")
	private Date myLastSecurityFailInvocation;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "LAST_THROTTLE_REJECT")
	private Date myLastThrottleReject;

	public Date getLastThrottleReject() {
		return myLastThrottleReject;
	}

	public void setLastThrottleReject(Date theLastThrottleReject) {
		myLastThrottleReject = theLastThrottleReject;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "LAST_SUC_INVOC")
	private Date myLastSuccessfulInvocation;

	@EmbeddedId
	private PersUserMethodStatusPk myPk;

	public PersUserMethodStatus() {
	}

	public PersUserMethodStatus(PersUserStatus thePersUserStatus, PersServiceVersionMethod theMethod) {
		myPk = new PersUserMethodStatusPk(thePersUserStatus, theMethod);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof PersUserMethodStatus)) {
			return false;
		}
		PersUserMethodStatus other = (PersUserMethodStatus) obj;
		if (myPk == null) {
			if (other.myPk != null) {
				return false;
			}
		} else if (!myPk.equals(other.myPk)) {
			return false;
		}
		return true;
	}

	/**
	 * @return the lastFailInvocation
	 */
	public Date getLastFailInvocation() {
		return myLastFailInvocation;
	}

	/**
	 * @return the lastFaultInvocation
	 */
	public Date getLastFaultInvocation() {
		return myLastFaultInvocation;
	}

	/**
	 * @return the lastSecurityFailInvocation
	 */
	public Date getLastSecurityFailInvocation() {
		return myLastSecurityFailInvocation;
	}

	/**
	 * @return the lastSuccessfulInvocation
	 */
	public Date getLastSuccessfulInvocation() {
		return myLastSuccessfulInvocation;
	}

	/**
	 * @return the pk
	 */
	public PersUserMethodStatusPk getPk() {
		return myPk;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((myPk == null) ? 0 : myPk.hashCode());
		return result;
	}

	/**
	 * @param theLastFailInvocation the lastFailInvocation to set
	 */
	public void setLastFailInvocation(Date theLastFailInvocation) {
		myLastFailInvocation = theLastFailInvocation;
	}

	/**
	 * @param theLastFaultInvocation the lastFaultInvocation to set
	 */
	public void setLastFaultInvocation(Date theLastFaultInvocation) {
		myLastFaultInvocation = theLastFaultInvocation;
	}

	/**
	 * @param theLastSecurityFailInvocation the lastSecurityFailInvocation to set
	 */
	public void setLastSecurityFailInvocation(Date theLastSecurityFailInvocation) {
		myLastSecurityFailInvocation = theLastSecurityFailInvocation;
	}

	/**
	 * @param theLastSuccessfulInvocation the lastSuccessfulInvocation to set
	 */
	public void setLastSuccessfulInvocation(Date theLastSuccessfulInvocation) {
		myLastSuccessfulInvocation = theLastSuccessfulInvocation;
	}

	public void merge(PersUserMethodStatus theStatus) {
		setLastSuccessfulInvocationIfNewer(theStatus.getLastSuccessfulInvocation());
		setLastFaultInvocationIfNewer(theStatus.getLastFaultInvocation());
		setLastFailInvocationIfNewer(theStatus.getLastFailInvocation());
		setLastSecurityFailInvocationIfNewer(theStatus.getLastSecurityFailInvocation());
	}

	public void setLastSecurityFailInvocationIfNewer(Date lastSecurityFailInvocation) {
		if (myLastSecurityFailInvocation == null || (lastSecurityFailInvocation != null && myLastSecurityFailInvocation.before(lastSecurityFailInvocation))) {
			myLastSecurityFailInvocation = lastSecurityFailInvocation;
		}
	}

	public void setLastThrottleRejectIfNewer(Date lastReject) {
		if (myLastThrottleReject == null || (lastReject != null && myLastThrottleReject.before(lastReject))) {
			myLastThrottleReject = lastReject;
		}
	}

	public void setLastFailInvocationIfNewer(Date lastFailInvocation) {
		if (myLastFailInvocation == null || (lastFailInvocation != null && myLastFailInvocation.before(lastFailInvocation))) {
			myLastFailInvocation = lastFailInvocation;
		}
	}

	public void setLastFaultInvocationIfNewer(Date lastFaultInvocation) {
		if (myLastFaultInvocation == null || (lastFaultInvocation != null && myLastFaultInvocation.before(lastFaultInvocation))) {
			myLastFaultInvocation = lastFaultInvocation;
		}
	}

	public void setLastSuccessfulInvocationIfNewer(Date lastSuccessfulInvocation) {
		if (myLastSuccessfulInvocation == null || (lastSuccessfulInvocation != null && myLastSuccessfulInvocation.before(lastSuccessfulInvocation))) {
			myLastSuccessfulInvocation = lastSuccessfulInvocation;
		}
	}


}
