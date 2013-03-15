package ca.uhn.sail.proxy.model.entity;

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

import ca.uhn.sail.proxy.util.Validate;

@Table(name = "PX_SVC_VER_STATUS")
@Entity
public class PersServiceVersionStatus extends BasePersObject {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "PID")
	private Long myPid;

	@OneToOne(cascade = {}, fetch = FetchType.LAZY)
	@JoinColumn(name = "SVC_VERSION_PID", referencedColumnName = "PID", unique = true, nullable = false)
	private BasePersServiceVersion myServiceVersion;


	public static PersInvocationUserStatsPk createEntryPk(InvocationStatsIntervalEnum theInterval, Date theTimestamp, PersServiceVersionMethod theMethod, PersServiceUser theUser) {
		Validate.throwIllegalArgumentExceptionIfNull("Interval", theInterval);
		Validate.throwIllegalArgumentExceptionIfNull("Timestamp", theTimestamp);

		PersInvocationUserStatsPk pk = new PersInvocationUserStatsPk(theInterval, theTimestamp, theMethod, theUser);
		return pk;
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

//	public static PersInvocationStatsPk createEntryPk(InvocationStatsIntervalEnum theInterval, Date theTimestamp, PersServiceVersionMethod theMethod) {
//		Validate.throwIllegalArgumentExceptionIfNull("Interval", theInterval);
//		Validate.throwIllegalArgumentExceptionIfNull("Timestamp", theTimestamp);
//
//		Date date = theInterval.truncate(theTimestamp);
//		PersInvocationStatsPk pk = new PersInvocationStatsPk(theInterval, date, theMethod);
//		return pk;
//	}


}
