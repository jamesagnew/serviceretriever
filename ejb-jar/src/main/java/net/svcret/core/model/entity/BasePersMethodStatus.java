package net.svcret.core.model.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

@MappedSuperclass
public class BasePersMethodStatus implements Serializable {

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

	@Transient
	private  volatile transient boolean myDirty;
	
	public BasePersMethodStatus() {
		// nothing
	}

	public boolean doesRangeOverlapWithAnyOfMyRanges(Date theStartDate, Date theEndDate) {
		long thisStart = Long.MAX_VALUE;
		long thisEnd = 0;
		
		if (myFirstSuccessfulInvocation != null && myLastSuccessfulInvocation != null) {
			thisStart = Math.min(thisStart, myFirstSuccessfulInvocation.getTime());
			thisEnd = Math.max(thisEnd, myLastSuccessfulInvocation.getTime());
		}
		if (myFirstSecurityFailInvocation != null && myLastSecurityFailInvocation != null) {
			thisStart = Math.min(thisStart, myFirstSecurityFailInvocation.getTime());
			thisEnd = Math.max(thisEnd, myLastSecurityFailInvocation.getTime());
		}
		if (myFirstFailInvocation != null && myLastFailInvocation != null) {
			thisStart = Math.min(thisStart, myFirstFailInvocation.getTime());
			thisEnd = Math.max(thisEnd, myLastFailInvocation.getTime());
		}
		if (myFirstFaultInvocation != null && myLastFaultInvocation != null) {
			thisStart = Math.min(thisStart, myFirstFaultInvocation.getTime());
			thisEnd = Math.max(thisEnd, myLastFaultInvocation.getTime());
		}
		if (myFirstThrottleReject != null && myLastThrottleReject != null) {
			thisStart = Math.min(thisStart, myFirstThrottleReject.getTime());
			thisEnd = Math.max(thisEnd, myLastThrottleReject.getTime());
		}
		
		long start = theStartDate.getTime();
		long end = theEndDate.getTime();
		
		if (start >= thisStart && start <= thisEnd) {
			return true;
		}
		if (end >= thisStart && end <= thisEnd) {
			return true;
		}
		if (thisStart >= start && thisEnd <= end) {
			return true;
		}
		
		return false;
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

	public Date getFirstFailInvocation() {
		return myFirstFailInvocation;
	}

	public Date getFirstSecurityFailInvocation() {
		return myFirstSecurityFailInvocation;
	}

	public Date getFirstSuccessfulInvocation() {
		return myFirstSuccessfulInvocation;
	}

	public Date getFirstThrottleReject() {
		return myFirstThrottleReject;
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

	public void merge(BasePersMethodStatus theStatus) {
		setValuesSuccessfulInvocation(theStatus.getFirstSuccessfulInvocation());
		setValuesSuccessfulInvocation(theStatus.getLastSuccessfulInvocation());

		setValuesFaultInvocation(theStatus.getFirstSuccessfulInvocation());
		setValuesFaultInvocation(theStatus.getLastFaultInvocation());

		setValuesFailInvocation(theStatus.getFirstFailInvocation());
		setValuesFailInvocation(theStatus.getLastFailInvocation());

		setValuesSecurityFailInvocation(theStatus.getFirstSecurityFailInvocation());
		setValuesSecurityFailInvocation(theStatus.getLastSecurityFailInvocation());

		setValuesThrottleReject(theStatus.getFirstThrottleReject());
		setValuesThrottleReject(theStatus.getLastThrottleReject());
	}



	public void setValuesFailInvocation(Date theInvocation) {
		if (myLastFailInvocation == null || (theInvocation != null && myLastFailInvocation.before(theInvocation))) {
			myLastFailInvocation = theInvocation;
			myDirty=true;
		}
		if (myFirstFailInvocation == null || (theInvocation != null && myFirstFailInvocation.after(theInvocation))) {
			myFirstFailInvocation = theInvocation;
			myDirty=true;
		}
	}


	public void setValuesFaultInvocation(Date theInv) {
		if (myLastFaultInvocation == null || (theInv != null && myLastFaultInvocation.before(theInv))) {
			myLastFaultInvocation = theInv;
			myDirty=true;
		}
		if (myFirstFaultInvocation == null || (theInv != null && myFirstFaultInvocation.after(theInv))) {
			myFirstFaultInvocation = theInv;
			myDirty=true;
		}
	}


	public void setValuesSecurityFailInvocation(Date theInv) {
		if (myLastSecurityFailInvocation == null || (theInv != null && myLastSecurityFailInvocation.before(theInv))) {
			myLastSecurityFailInvocation = theInv;
			myDirty=true;
		}
		if (myFirstSecurityFailInvocation == null || (theInv != null && myFirstSecurityFailInvocation.after(theInv))) {
			myFirstSecurityFailInvocation = theInv;
			myDirty=true;
		}
	}


	public void setValuesSuccessfulInvocation(Date theInvocation) {
		if (myFirstSuccessfulInvocation == null || (theInvocation != null && myFirstSuccessfulInvocation.after(theInvocation))) {
			myFirstSuccessfulInvocation = theInvocation;
			myDirty=true;
		}
		if (myLastSuccessfulInvocation == null || (theInvocation != null && myLastSuccessfulInvocation.before(theInvocation))) {
			myLastSuccessfulInvocation = theInvocation;
			myDirty=true;
		}
	}


	public void setValuesThrottleReject(Date theInv) {
		if (myLastThrottleReject == null || (theInv != null && myLastThrottleReject.before(theInv))) {
			myLastThrottleReject = theInv;
			myDirty=true;
		}
		if (myFirstThrottleReject == null || (theInv != null && myFirstThrottleReject.after(theInv))) {
			myFirstThrottleReject = theInv;
			myDirty=true;
		}
	}

	
	public void clearDirty() {
		myDirty=false;
	}

	public boolean isDirty() {
		return myDirty;
	}

}
