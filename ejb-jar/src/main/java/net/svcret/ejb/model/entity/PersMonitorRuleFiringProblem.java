package net.svcret.ejb.model.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.Objects;

@Entity()
@Table(name = "PX_MONITOR_RULE_FIRING_PRB")
public class PersMonitorRuleFiringProblem extends BasePersObject {

	private static final long serialVersionUID = 1L;

	@ManyToOne(optional = true)
	@JoinColumn(name = "ACTIVE_MESSAGE", nullable = true)
	private PersMonitorRuleActiveCheck myActiveCheck;

	@Column(name = "CHECK_FAILURE_MESSAGE", nullable = true, length = EntityConstants.MAXLEN_MONITOR_FIRING_CHECK_FAILURE_MSG)
	private String myCheckFailureMessage;

	@ManyToOne(optional = true)
	@JoinColumn(name = "URL_PID", nullable = true)
	private PersServiceVersionUrl myUrl;

	@Column(name = "FAILED_URL_MSG", length = EntityConstants.MAXLEN_INVOC_OUTCOME_MSG, nullable = true)
	private String myFailedUrlMessage;

	@ManyToOne()
	@JoinColumn(name = "FIRING_PID", nullable = false, referencedColumnName = "PID")
	private PersMonitorRuleFiring myFiring;

	@Column(name = "LATENCY_AVG_CALLMILLIS", nullable = true)
	private Long myLatencyAverageMillisPerCall;

	@Column(name = "LATENCY_THRESHOLD", nullable = true)
	private Long myLatencyThreshold;

	@Column(name = "LATENCY_AVG_OVERMINS", nullable = true)
	private Long myLatencyAverageOverMinutes;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "PID")
	private Long myPid;

	@ManyToOne(cascade = {}, optional = false)
	@JoinColumn(name = "SVCVER_PID", nullable = false)
	private BasePersServiceVersion myServiceVersion;

	@Override
	public boolean equals(Object theObj) {
		if (!(theObj instanceof PersMonitorRuleFiringProblem)) {
			return false;
		}

		PersMonitorRuleFiringProblem obj = (PersMonitorRuleFiringProblem) theObj;
		boolean retVal = Objects.equal(obj.getServiceVersion(), getServiceVersion());
		retVal &= Objects.equal(obj.getFailedUrlMessage(), getFailedUrlMessage());
		retVal &= Objects.equal(obj.getLatencyAverageMillisPerCall(), getLatencyAverageMillisPerCall());
		retVal &= Objects.equal(obj.getFiring(), getFiring());
		retVal &= Objects.equal(obj.getCheckFailureMessage(), getCheckFailureMessage());

		return retVal;

	}

	public String toStringShort() {
		if (getCheckFailureMessage() != null) {
			return "checkFailure='" + getCheckFailureMessage() + "'";
		}
		if (getLatencyAverageMillisPerCall() != null) {
			return "latency=" + getLatencyAverageMillisPerCall();
		}
		if (getFailedUrlMessage() != null) {
			return "urlFailure='" + getFailedUrlMessage() + "'";
		}
		return "unknown";
	}

	public PersMonitorRuleActiveCheck getActiveCheck() {
		return myActiveCheck;
	}

	public String getCheckFailureMessage() {
		return myCheckFailureMessage;
	}

	/**
	 * @return the failedUrl
	 */
	public PersServiceVersionUrl getUrl() {
		return myUrl;
	}

	/**
	 * @return the failedUrlMessage
	 */
	public String getFailedUrlMessage() {
		return myFailedUrlMessage;
	}

	/**
	 * @return the firing
	 */
	public PersMonitorRuleFiring getFiring() {
		return myFiring;
	}

	/**
	 * @return the latencyAverageMillisPerCall
	 */
	public Long getLatencyAverageMillisPerCall() {
		return myLatencyAverageMillisPerCall;
	}

	/**
	 * @return the latencyAverageOverMinutes
	 */
	public Long getLatencyAverageOverMinutes() {
		return myLatencyAverageOverMinutes;
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

	@Override
	public int hashCode() {
		HashCodeBuilder b = new HashCodeBuilder();
		b.append(getServiceVersion());
		b.append(getFailedUrlMessage());
		b.append(getLatencyAverageMillisPerCall());
		b.append(getCheckFailureMessage());
		return b.toHashCode();
	}

	public void setActiveCheck(PersMonitorRuleActiveCheck theActiveCheck) {
		myActiveCheck = theActiveCheck;
	}

	public void setCheckFailureMessage(String theCheckFailureMessage) {
		if (theCheckFailureMessage.length() > EntityConstants.MAXLEN_MONITOR_FIRING_CHECK_FAILURE_MSG) {
			myFailedUrlMessage = theCheckFailureMessage.substring(0, EntityConstants.MAXLEN_MONITOR_FIRING_CHECK_FAILURE_MSG);
		} else {
			myCheckFailureMessage = theCheckFailureMessage;
		}
	}

	/**
	 * @param theFailedUrl
	 *            the failedUrl to set
	 */
	public void setUrl(PersServiceVersionUrl theUrl) {
		myUrl = theUrl;
	}

	/**
	 * @param theFailedUrlMessage
	 *            the failedUrlMessage to set
	 */
	public void setFailedUrlMessage(String theFailedUrlMessage) {
		myFailedUrlMessage = theFailedUrlMessage;
	}

	/**
	 * @param theFiring
	 *            the firing to set
	 */
	public void setFiring(PersMonitorRuleFiring theFiring) {
		myFiring = theFiring;
	}

	/**
	 * @param theLatencyAverageMillisPerCall
	 *            the latencyAverageMillisPerCall to set
	 */
	public void setLatencyAverageMillisPerCall(Long theLatencyAverageMillisPerCall) {
		myLatencyAverageMillisPerCall = theLatencyAverageMillisPerCall;
	}

	public Long getLatencyThreshold() {
		return myLatencyThreshold;
	}

	public void setLatencyThreshold(Long theLatencyThreshold) {
		myLatencyThreshold = theLatencyThreshold;
	}

	/**
	 * @param theLatencyAverageOverMinutes
	 *            the latencyAverageOverMinutes to set
	 */
	public void setLatencyAverageOverMinutes(Long theLatencyAverageOverMinutes) {
		myLatencyAverageOverMinutes = theLatencyAverageOverMinutes;
	}

	/**
	 * @param theServiceVersion
	 *            the serviceVersion to set
	 */
	public void setServiceVersion(BasePersServiceVersion theServiceVersion) {
		myServiceVersion = theServiceVersion;
	}

	public static PersMonitorRuleFiringProblem getInstanceForCheckFailure(BasePersServiceVersion theServiceVersion, String theMessage) {
		PersMonitorRuleFiringProblem retVal = new PersMonitorRuleFiringProblem();
		retVal.setServiceVersion(theServiceVersion);
		retVal.setCheckFailureMessage(theMessage);
		return retVal;
	}

	public static PersMonitorRuleFiringProblem getInstanceForServiceLatency(BasePersServiceVersion theSvcVer, long theAvgLatency, long theThreshold, Long theAvgLatencyOverMinutes,
			PersServiceVersionUrl theUrl) {
		PersMonitorRuleFiringProblem retVal = new PersMonitorRuleFiringProblem();
		retVal.setServiceVersion(theSvcVer);
		retVal.setLatencyAverageMillisPerCall(theAvgLatency);
		retVal.setLatencyThreshold(theThreshold);
		retVal.setLatencyAverageOverMinutes(theAvgLatencyOverMinutes);
		retVal.setUrl(theUrl);
		return retVal;
	}

	public static PersMonitorRuleFiringProblem getInstanceForUrlDown(BasePersServiceVersion theSvcVer, PersServiceVersionUrl theUrl, String theMessage) {
		PersMonitorRuleFiringProblem retVal = new PersMonitorRuleFiringProblem();
		retVal.setServiceVersion(theSvcVer);
		retVal.setUrl(theUrl);
		retVal.setFailedUrlMessage(theMessage);
		return retVal;
	}

}
