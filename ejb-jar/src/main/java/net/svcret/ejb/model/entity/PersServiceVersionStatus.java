package net.svcret.ejb.model.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.ForeignKey;

import net.svcret.ejb.util.Validate;

@Table(name = "PX_SVC_VER_STATUS")
@Entity
public class PersServiceVersionStatus extends BasePersObject {

	private static final long serialVersionUID = 1L;

	@Transient
	private transient boolean myDirty;

	@Column(name = "LAST_FAIL")
	@Temporal(TemporalType.TIMESTAMP)
	private Date myLastFailInvocation;

	@Column(name = "LAST_FAULT")
	@Temporal(TemporalType.TIMESTAMP)
	private Date myLastFaultInvocation;

	@Column(name = "LAST_SAVE")
	@Temporal(TemporalType.TIMESTAMP)
	private Date myLastSave;

	@Column(name = "LAST_SEC_FAIL")
	@Temporal(TemporalType.TIMESTAMP)
	private Date myLastServerSecurityFailure;

	@Column(name = "LAST_SUCCESS")
	@Temporal(TemporalType.TIMESTAMP)
	private Date myLastSuccessfulInvocation;

	@Column(name = "LAST_THROTTLE_REJECT")
	@Temporal(TemporalType.TIMESTAMP)
	private Date myLastThrottleReject;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "PID")
	private Long myPid;

	@OneToOne(cascade = {}, fetch = FetchType.LAZY)
	@ForeignKey(name = "PX_SVCVERSTATUS_SVCVER_PID")
	@JoinColumn(name = "SVC_VERSION_PID", referencedColumnName = "PID", unique = true, nullable = false)
	private BasePersServiceVersion myServiceVersion;

	@Column(name = "SVC_VERSION_PID", updatable = false, insertable = false)
	private long myServiceVersionPid;

	public PersServiceVersionStatus() {
	}

	public PersServiceVersionStatus(long thePid, BasePersServiceVersion theVersion) {
		myPid = thePid;
		myServiceVersion = theVersion;
	}

	public Date getLastFailInvocation() {
		return myLastFailInvocation;
	}

	public Date getLastFaultInvocation() {
		return myLastFaultInvocation;
	}

	/**
	 * @return the lastSave
	 */
	public Date getLastSave() {
		return myLastSave;
	}

	/**
	 * @return the lastServerSecurityFailure
	 */
	public Date getLastServerSecurityFailure() {
		return myLastServerSecurityFailure;
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
	 * @return the pid
	 */
	public Long getPid() {
		return myPid;
	}

	/**
	 * @return the serviceVersion
	 */
	public BasePersServiceVersion getServiceVersion() {
		return myServiceVersion;
	}

	public long getServiceVersionPid() {
		return myServiceVersionPid;
	}

	public boolean isDirty() {
		return myDirty;
	}

	public void merge(PersServiceVersionStatus theStatus) {
		myLastSave = newer(myLastSave, theStatus.getLastSave());
		myLastServerSecurityFailure = newer(myLastServerSecurityFailure, theStatus.getLastServerSecurityFailure());
		myLastSuccessfulInvocation = newer(myLastSuccessfulInvocation, theStatus.getLastSuccessfulInvocation());
		myLastFaultInvocation = newer(myLastFaultInvocation, theStatus.getLastFaultInvocation());
		myLastFailInvocation = newer(myLastFailInvocation, theStatus.getLastFailInvocation());
		myLastThrottleReject = newer(myLastThrottleReject, theStatus.getLastThrottleReject());
	}

	/**
	 * @param theDirty
	 *            the dirty to set
	 */
	public void setDirty(boolean theDirty) {
		myDirty = theDirty;
	}

	public void setLastFailInvocation(Date theLastFailInvocation) {
		myLastFailInvocation = theLastFailInvocation;
	}

	public void setLastFaultInvocation(Date theLastFaultInvocation) {
		myLastFaultInvocation = theLastFaultInvocation;
	}

	/**
	 * @param theLastSave
	 *            the lastSave to set
	 */
	public void setLastSave(Date theLastSave) {
		myLastSave = theLastSave;
	}

	/**
	 * @param theLastServerSecurityFailure
	 *            the lastServerSecurityFailure to set
	 */
	public void setLastServerSecurityFailure(Date theLastServerSecurityFailure) {
		myDirty = true;
		myLastServerSecurityFailure = theLastServerSecurityFailure;
	}

	/**
	 * @param theLastSuccessfulInvocation
	 *            the lastSuccessfulInvocation to set
	 */
	public void setLastSuccessfulInvocation(Date theLastSuccessfulInvocation) {
		myDirty = true;
		myLastSuccessfulInvocation = theLastSuccessfulInvocation;
	}

	public void setLastThrottleReject(Date theLastThrottleReject) {
		myLastThrottleReject = theLastThrottleReject;
	}

	/**
	 * @param thePid
	 *            the pid to set
	 */
	public void setPid(Long thePid) {
		myPid = thePid;
	}

	/**
	 * @param theServiceVersion
	 *            the serviceVersion to set
	 */
	public void setServiceVersion(BasePersServiceVersion theServiceVersion) {
		myServiceVersion = theServiceVersion;
	}

	public static PersInvocationUserStatsPk createEntryPk(InvocationStatsIntervalEnum theInterval, Date theTimestamp, PersUser theUser) {
		Validate.notNull(theInterval, "Interval");
		Validate.notNull(theTimestamp, "Timestamp");

		PersInvocationUserStatsPk pk = new PersInvocationUserStatsPk(theInterval, theTimestamp, theUser);
		return pk;
	}

	public static Date newer(Date theDate1, Date theDate2) {
		if (theDate1 == null) {
			return theDate2;
		}
		if (theDate2 == null) {
			return theDate1;
		}
		if (theDate1.before(theDate2)) {
			return theDate2;
		}
		return theDate1;
	}

	// public static PersInvocationStatsPk
	// createEntryPk(InvocationStatsIntervalEnum theInterval, Date theTimestamp,
	// PersServiceVersionMethod theMethod) {
	// Validate.throwIllegalArgumentExceptionIfNull("Interval", theInterval);
	// Validate.throwIllegalArgumentExceptionIfNull("Timestamp", theTimestamp);
	//
	// Date date = theInterval.truncate(theTimestamp);
	// PersInvocationStatsPk pk = new PersInvocationStatsPk(theInterval, date,
	// theMethod);
	// return pk;
	// }

}
