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
	@JoinColumn(name = "FAILED_URL_PID", nullable = true)
	private PersServiceVersionUrl myFailedUrl;

	@Column(name="FAILED_URL_MSG", length=PersServiceVersionUrlStatus.MAX_LENGTH_MSG)
	private String myFailedUrlMessage;
	
	@ManyToOne()
	@JoinColumn(name = "FIRING_PID", nullable = false, referencedColumnName = "PID")
	private PersMonitorRuleFiring myFiring;

	@Column(name = "LATENCY_AVG_CALLMILLIS", nullable = true)
	private Long myLatencyAverageMillisPerCall;

	@Column(name = "LATENCY_AVG_OVERMINS", nullable = true)
	private Long myLatencyAverageOverMinutes;

	@Column(name = "LATENCY_EXCEEDED", nullable = true)
	private Boolean myLatencyExceededThreshold;

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
		
		PersMonitorRuleFiringProblem obj = (PersMonitorRuleFiringProblem)theObj;
		boolean retVal = Objects.equal(obj.getServiceVersion(), getServiceVersion());
		retVal &= Objects.equal(obj.getFailedUrl(), getFailedUrl());
		retVal &= Objects.equal(obj.getLatencyExceededThreshold(), getLatencyExceededThreshold());
		
		return retVal;
		
	}

	/**
	 * @return the failedUrl
	 */
	public PersServiceVersionUrl getFailedUrl() {
		return myFailedUrl;
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
	 * @return the latencyExceededThreshold
	 */
	public Boolean getLatencyExceededThreshold() {
		return myLatencyExceededThreshold;
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
		b.append(getFailedUrl());
		b.append(getLatencyExceededThreshold());
		return b.toHashCode();
	}

	/**
	 * @param theFailedUrl
	 *            the failedUrl to set
	 */
	public void setFailedUrl(PersServiceVersionUrl theFailedUrl) {
		if (myLatencyExceededThreshold == Boolean.TRUE) {
			throw new IllegalStateException();
		}
		myFailedUrl = theFailedUrl;
	}

	/**
	 * @param theFailedUrlMessage the failedUrlMessage to set
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

	/**
	 * @param theLatencyAverageOverMinutes
	 *            the latencyAverageOverMinutes to set
	 */
	public void setLatencyAverageOverMinutes(Long theLatencyAverageOverMinutes) {
		myLatencyAverageOverMinutes = theLatencyAverageOverMinutes;
	}

	/**
	 * @param theLatencyExceededThreshold the latencyExceededThreshold to set
	 */
	public void setLatencyExceededThreshold(Boolean theLatencyExceededThreshold) {
		if (myFailedUrl != null) {
			throw new IllegalStateException();
		}
		myLatencyExceededThreshold = theLatencyExceededThreshold;
	}

	/**
	 * @param theServiceVersion
	 *            the serviceVersion to set
	 */
	public void setServiceVersion(BasePersServiceVersion theServiceVersion) {
		myServiceVersion = theServiceVersion;
	}

	public static PersMonitorRuleFiringProblem getInstanceForServiceLatency(BasePersServiceVersion theSvcVer, long theAvgLatency, int theAvgLatencyOverMinutes) {
		PersMonitorRuleFiringProblem retVal = new PersMonitorRuleFiringProblem();
		retVal.setServiceVersion(theSvcVer);
		retVal.setLatencyAverageMillisPerCall(theAvgLatency);
		retVal.setLatencyAverageOverMinutes((long)theAvgLatencyOverMinutes);
		retVal.setLatencyExceededThreshold(true);
		return retVal;
	}

	public static PersMonitorRuleFiringProblem getInstanceForUrlDown(BasePersServiceVersion theSvcVer, PersServiceVersionUrl theUrl, String theMessage) {
		PersMonitorRuleFiringProblem retVal = new PersMonitorRuleFiringProblem();
		retVal.setServiceVersion(theSvcVer);
		retVal.setFailedUrl(theUrl);
		retVal.setFailedUrlMessage(theMessage);
		return retVal;
	}

}
