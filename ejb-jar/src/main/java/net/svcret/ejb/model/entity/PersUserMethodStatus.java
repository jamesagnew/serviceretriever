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
	@Column(name = "FIRST_FAIL_INVOC")
	private Date myFirstFailInvocation;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "FIRST_FAULT_INVOC")
	private Date myFirstFaultInvocation;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "FIRST_SECFAIL_INVOC")
	private Date myFirstSecurityFailInvocation;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "FIRST_THROTTLE_REJECT")
	private Date myFirstThrottleReject;

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
	@Column(name = "LAST_SUC_INVOC")
	private Date myLastSuccessfulInvocation;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "FIRST_SUC_INVOC")
	private Date myFirstSuccessfulInvocation;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "LAST_THROTTLE_REJECT")
	private Date myLastThrottleReject;

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

	public Date getLastThrottleReject() {
		return myLastThrottleReject;
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

	public void merge(PersUserMethodStatus theStatus) {
		setLastSuccessfulInvocationIfNewer(theStatus.getLastSuccessfulInvocation());
		setLastFaultInvocationIfNewer(theStatus.getLastFaultInvocation());
		setLastFailInvocationIfNewer(theStatus.getLastFailInvocation());
		setLastSecurityFailInvocationIfNewer(theStatus.getLastSecurityFailInvocation());
	}

	/**
	 * @param theLastFailInvocation
	 *            the lastFailInvocation to set
	 */
	public void setLastFailInvocation(Date theLastFailInvocation) {
		myLastFailInvocation = theLastFailInvocation;
	}

	public void setLastFailInvocationIfNewer(Date theInvocation) {
		if (myFirstFailInvocation == null || (theInvocation != null && myFirstFailInvocation.after(theInvocation))) {
			myFirstFailInvocation = theInvocation;
		}
		if (myLastFailInvocation == null || (theInvocation != null && myLastFailInvocation.before(theInvocation))) {
			myLastFailInvocation = theInvocation;
		}
	}


	public void setLastFaultInvocationIfNewer(Date lastFaultInvocation) {
		if (myFirstFaultInvocation == null || (lastFaultInvocation != null && myFirstFaultInvocation.after(lastFaultInvocation))) {
			myFirstFaultInvocation = lastFaultInvocation;
		}
		if (myLastFaultInvocation == null || (lastFaultInvocation != null && myLastFaultInvocation.before(lastFaultInvocation))) {
			myLastFaultInvocation = lastFaultInvocation;
		}
	}


	public void setLastSecurityFailInvocationIfNewer(Date lastSecurityFailInvocation) {
		if (myFirstSecurityFailInvocation == null || (lastSecurityFailInvocation != null && myFirstSecurityFailInvocation.after(lastSecurityFailInvocation))) {
			myFirstSecurityFailInvocation = lastSecurityFailInvocation;
		}
		if (myLastSecurityFailInvocation == null || (lastSecurityFailInvocation != null && myLastSecurityFailInvocation.before(lastSecurityFailInvocation))) {
			myLastSecurityFailInvocation = lastSecurityFailInvocation;
		}
	}


	public void setLastSuccessfulInvocationIfNewer(Date lastSuccessfulInvocation) {
		if (myFirstSuccessfulInvocation == null || (lastSuccessfulInvocation != null && myFirstSuccessfulInvocation.after(lastSuccessfulInvocation))) {
			myFirstSuccessfulInvocation = lastSuccessfulInvocation;
		}
		if (myLastSuccessfulInvocation == null || (lastSuccessfulInvocation != null && myLastSuccessfulInvocation.before(lastSuccessfulInvocation))) {
			myLastSuccessfulInvocation = lastSuccessfulInvocation;
		}
	}

	public void setLastThrottleRejectIfNewer(Date lastReject) {
		if (myFirstThrottleReject == null || (lastReject != null && myFirstThrottleReject.after(lastReject))) {
			myFirstThrottleReject = lastReject;
		}
		if (myLastThrottleReject == null || (lastReject != null && myLastThrottleReject.before(lastReject))) {
			myLastThrottleReject = lastReject;
		}
	}

	public boolean doesDateFallWithinAtLeastOneOfMyRanges(Date theDate) {
		if (myFirstSuccessfulInvocation != null && myLastSuccessfulInvocation != null) {
			if (!myFirstSuccessfulInvocation.after(theDate) && !myLastSuccessfulInvocation.before(theDate)) {
				return true;
			}
		}
		if (myFirstSecurityFailInvocation != null && myLastSecurityFailInvocation != null) {
			if (!myFirstSecurityFailInvocation.after(theDate) && !myLastSecurityFailInvocation.before(theDate)) {
				return true;
			}
		}
		if (myFirstFailInvocation != null && myLastFailInvocation != null) {
			if (!myFirstFailInvocation.after(theDate) && !myLastFailInvocation.before(theDate)) {
				return true;
			}
		}
		if (myFirstFaultInvocation != null && myLastFaultInvocation != null) {
			if (!myFirstFaultInvocation.after(theDate) && !myLastFaultInvocation.before(theDate)) {
				return true;
			}
		}
		if (myFirstThrottleReject != null && myLastThrottleReject != null) {
			if (!myFirstThrottleReject.after(theDate) && !myLastThrottleReject.before(theDate)) {
				return true;
			}
		}
		return false;
	}

}
