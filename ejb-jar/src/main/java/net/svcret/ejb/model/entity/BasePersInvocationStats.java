package net.svcret.ejb.model.entity;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import com.google.common.base.Objects.ToStringHelper;

@MappedSuperclass
public abstract class BasePersInvocationStats<P extends BasePersInvocationStatsPk<?,?>, O extends BasePersInvocationStats<?,?>> extends BasePersStats<P,O> {

	private static final long serialVersionUID = 1L;

	@Column(name = "MAX_SUC_REQ_BYTES", nullable = false)
	private volatile long myMaxSuccessRequestMessageBytes;
	@Column(name = "MAX_SUC_RES_BYTES", nullable = false)
	private volatile long myMaxSuccessResponseMessageBytes;
	@Column(name = "MIN_SUC_REQ_BYTES", nullable = false)
	private volatile long myMinSuccessRequestMessageBytes = -1;
	@Column(name = "MIN_SUC_RES_BYTES", nullable = false)
	private volatile long myMinSuccessResponseMessageBytes;

	@Column(name = "TOT_FAIL_INV_COUNT", nullable = false)
	private volatile long myFailInvocationCount;
	@Column(name = "TOT_FAIL_INV_TIME", nullable = false)
	private volatile long myFailInvocationTime;
	@Column(name = "TOT_FAIL_REQ_BYTES", nullable = false)
	private volatile long myFailRequestMessageBytes;
	@Column(name = "TOT_FAIL_RES_BYTES", nullable = false)
	private volatile long myFailResponseMessageBytes;

	@Column(name = "TOT_FAULT_INV_COUNT", nullable = false)
	private volatile long myFaultInvocationCount;
	@Column(name = "TOT_FAULT_INV_TIME", nullable = false)
	private volatile long myFaultInvocationTime;
	@Column(name = "TOT_FAULT_REQ_BYTES", nullable = false)
	private volatile long myFaultRequestMessageBytes;
	@Column(name = "TOT_FAULT_RES_BYTES", nullable = false)
	private volatile long myFaultResponseMessageBytes;

	@Column(name = "TOT_SRV_SECURITY_FAILS", nullable = false)
	private volatile long myTotalServerSecurityFailures;

	@Column(name = "TOT_THROTTLE_REJECTS", nullable = true)
	private volatile long myTotalThrottleRejections;
	@Column(name = "TOT_THROTTLE_ACCEPTS", nullable = true)
	private volatile long myTotalThrottleAccepts;
	@Column(name = "TOT_THROTTLE_TIME", nullable = true)
	private volatile long myTotalThrottleTime;

	@Column(name = "TOT_SUC_INV_COUNT", nullable = false)
	private volatile long myTotalSuccessInvocationCount;
	@Column(name = "TOT_SUC_INV_TIME", nullable = false)
	private volatile long myTotalSuccessInvocationTime;
	@Column(name = "TOT_SUC_REQ_BYTES", nullable = false)
	private volatile long myTotalSuccessRequestMessageBytes;

	@Column(name = "TOT_SUC_RES_BYTES", nullable = false)
	private volatile long myTotalSuccessResponseMessageBytes;

	public synchronized void addThrottleReject() {
		myTotalThrottleRejections++;
	}

	public synchronized void addThrottleAccept(long theThrottleTime) {
		myTotalThrottleAccepts++;
		myTotalThrottleTime += theThrottleTime;
	}

	public synchronized void addFailInvocation(long theTime, long theRequestBytes, long theResponseBytes) {
		myFailInvocationCount++;
		myFailInvocationTime += theTime;
		myFailRequestMessageBytes += theRequestBytes;
		myFailResponseMessageBytes += theResponseBytes;
	}

	public synchronized void addFaultInvocation(long theTime, long theRequestBytes, long theResponseBytes) {
		myFaultInvocationCount++;
		myFaultInvocationTime += theTime;
		myFaultRequestMessageBytes += theRequestBytes;
		myFaultResponseMessageBytes += theResponseBytes;
	}

	public synchronized void addServerSecurityFailInvocation() {
		myTotalServerSecurityFailures++;
	}

	public synchronized void addSuccessInvocation(long theTime, long theRequestBytes, long theResponseBytes) {
		myTotalSuccessInvocationCount++;
		myTotalSuccessInvocationTime += theTime;
		myTotalSuccessRequestMessageBytes += theRequestBytes;
		myTotalSuccessResponseMessageBytes += theResponseBytes;
		if (myMinSuccessRequestMessageBytes == -1) {
			myMinSuccessRequestMessageBytes = theRequestBytes;
			myMaxSuccessRequestMessageBytes = theRequestBytes;
			myMinSuccessResponseMessageBytes = theResponseBytes;
			myMaxSuccessResponseMessageBytes = theResponseBytes;
		} else {
			myMinSuccessRequestMessageBytes = Math.min(myMinSuccessRequestMessageBytes, theRequestBytes);
			myMaxSuccessRequestMessageBytes = Math.max(myMaxSuccessRequestMessageBytes, theRequestBytes);
			myMinSuccessResponseMessageBytes = Math.min(myMinSuccessResponseMessageBytes, theResponseBytes);
			myMaxSuccessResponseMessageBytes = Math.max(myMaxSuccessResponseMessageBytes, theResponseBytes);
		}
	}

	/**
	 * @return the totalFailInvocationCount
	 */
	public  long getFailInvocationCount() {
		return myFailInvocationCount;
	}

	/**
	 * @return the totalFailInvocationTime
	 */
	public  long getFailInvocationTime() {
		return myFailInvocationTime;
	}

	/**
	 * @return the totalFailRequestMessageBytes
	 */
	public  long getFailRequestMessageBytes() {
		return myFailRequestMessageBytes;
	}

	/**
	 * @return the totalFailResponseMessageBytes
	 */
	public  long getFailResponseMessageBytes() {
		return myFailResponseMessageBytes;
	}

	public long getTotalThrottleRejections() {
		return myTotalThrottleRejections;
	}

	public long getTotalThrottleAccepts() {
		return myTotalThrottleAccepts;
	}

	public long getTotalThrottleTime() {
		return myTotalThrottleTime;
	}

	/**
	 * @return the totalFaultInvocationCount
	 */
	public  long getFaultInvocationCount() {
		return myFaultInvocationCount;
	}

	/**
	 * @return the totalFaultInvocationTime
	 */
	public  long getFaultInvocationTime() {
		return myFaultInvocationTime;
	}

	/**
	 * @return the totalFaultRequestMessageBytes
	 */
	public  long getFaultRequestMessageBytes() {
		return myFaultRequestMessageBytes;
	}

	/**
	 * @return the totalFaultResponseMessageBytes
	 */
	public  long getFaultResponseMessageBytes() {
		return myFaultResponseMessageBytes;
	}

	/**
	 * @return the maxSuccessRequestMessageBytes
	 */
	public  long getMaxSuccessRequestMessageBytes() {
		return myMaxSuccessRequestMessageBytes;
	}

	/**
	 * @return the maxSuccessResponseMessageBytes
	 */
	public  long getMaxSuccessResponseMessageBytes() {
		return myMaxSuccessResponseMessageBytes;
	}

	/**
	 * @return the minSuccessRequestMessageBytes
	 */
	public  long getMinSuccessRequestMessageBytes() {
		return myMinSuccessRequestMessageBytes;
	}

	/**
	 * @return the minSuccessResponseMessageBytes
	 */
	public  long getMinSuccessResponseMessageBytes() {
		return myMinSuccessResponseMessageBytes;
	}

	/**
	 * @return the totalServerSecurityFailures
	 */
	public  long getServerSecurityFailures() {
		return myTotalServerSecurityFailures;
	}

	public abstract StatsTypeEnum getStatsType();

	public synchronized long getSuccessInvocationAvgRequestBytes() {
		if (myTotalSuccessInvocationCount == 0) {
			return 0;
		} else {
			return myTotalSuccessRequestMessageBytes / myTotalSuccessInvocationCount;
		}
	}

	public synchronized long getSuccessInvocationAvgResponseBytes() {
		if (myTotalSuccessInvocationCount == 0) {
			return 0;
		} else {
			return myTotalSuccessResponseMessageBytes / myTotalSuccessInvocationCount;
		}
	}

	public synchronized long getSuccessInvocationAvgTime() {
		if (myTotalSuccessInvocationCount == 0) {
			return 0;
		} else {
			return myTotalSuccessInvocationTime / myTotalSuccessInvocationCount;
		}
	}

	public synchronized long getSuccessInvocationCount() {
		return myTotalSuccessInvocationCount;
	}

	public synchronized long getSuccessInvocationTotalTime() {
		return myTotalSuccessInvocationTime;
	}

	/**
	 * @return the totalSuccessRequestMessageBytes
	 */
	public synchronized long getSuccessRequestMessageBytes() {
		return myTotalSuccessRequestMessageBytes;
	}

	/**
	 * @return the totalSuccessResponseMessageBytes
	 */
	public synchronized long getSuccessResponseMessageBytes() {
		return myTotalSuccessResponseMessageBytes;
	}

	public synchronized void mergeUnsynchronizedEvents(O stats) {
		synchronized (stats) {
			myTotalSuccessInvocationCount += stats.getSuccessInvocationCount();
			myTotalSuccessInvocationTime += stats.getSuccessInvocationTotalTime();
			myTotalSuccessRequestMessageBytes += stats.getSuccessRequestMessageBytes();
			myTotalSuccessResponseMessageBytes += stats.getSuccessResponseMessageBytes();

			myFailInvocationCount += stats.getFailInvocationCount();
			myFailInvocationTime += stats.getFailInvocationTime();
			myFailRequestMessageBytes += stats.getFailRequestMessageBytes();
			myFailResponseMessageBytes += stats.getFailResponseMessageBytes();

			myFaultInvocationCount += stats.getFaultInvocationCount();
			myFaultInvocationTime += stats.getFaultInvocationTime();
			myFaultRequestMessageBytes += stats.getFaultRequestMessageBytes();
			myFaultResponseMessageBytes += stats.getFaultResponseMessageBytes();

			myTotalServerSecurityFailures += stats.getServerSecurityFailures();

			myTotalThrottleAccepts += stats.getTotalThrottleAccepts();
			myTotalThrottleRejections += stats.getTotalThrottleRejections();
			myTotalThrottleTime += stats.getTotalThrottleTime();

			if (myMinSuccessRequestMessageBytes == -1) {
				myMinSuccessRequestMessageBytes = stats.getMinSuccessRequestMessageBytes();
				myMaxSuccessRequestMessageBytes = stats.getMaxSuccessRequestMessageBytes();
				myMinSuccessResponseMessageBytes = stats.getMinSuccessResponseMessageBytes();
				myMaxSuccessResponseMessageBytes = stats.getMaxSuccessResponseMessageBytes();
			} else {
				myMinSuccessRequestMessageBytes = Math.min(myMinSuccessRequestMessageBytes, stats.getMinSuccessRequestMessageBytes());
				myMaxSuccessRequestMessageBytes = Math.max(myMaxSuccessRequestMessageBytes, stats.getMaxSuccessRequestMessageBytes());
				myMinSuccessResponseMessageBytes = Math.min(myMinSuccessResponseMessageBytes, stats.getMinSuccessResponseMessageBytes());
				myMaxSuccessResponseMessageBytes = Math.max(myMaxSuccessResponseMessageBytes, stats.getMaxSuccessResponseMessageBytes());
			}
		}
	}

	@Override
	public String toString() {
		ToStringHelper tos = getPk().getToStringHelper();
		boolean haveStats = false;
		if (getSuccessInvocationCount() > 0) {
			tos.add("Success", getSuccessInvocationCount());
			haveStats = true;
		}
		if (getFaultInvocationCount() > 0) {
			tos.add("Fault", getFaultInvocationCount());
			haveStats = true;
		}
		if (getFailInvocationCount() > 0) {
			tos.add("Fail", getFailInvocationCount());
			haveStats = true;
		}
		if (getServerSecurityFailures() > 0) {
			tos.add("SecurityFail", getServerSecurityFailures());
			haveStats = true;
		}
		if (!haveStats) {
			tos.add("NoStats", true);
		}
		return tos.toString();
	}

	public static enum StatsTypeEnum {
		INVOCATION, 
		USER,
		URL

	}

}
