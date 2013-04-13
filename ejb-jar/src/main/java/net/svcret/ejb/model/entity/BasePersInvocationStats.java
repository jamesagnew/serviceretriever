package net.svcret.ejb.model.entity;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class BasePersInvocationStats extends BasePersMethodStats {

	private static final long serialVersionUID = 1L;
	
	@Column(name = "MAX_SUC_REQ_BYTES", nullable = false)
	private long myMaxSuccessRequestMessageBytes;
	@Column(name = "MAX_SUC_RES_BYTES", nullable = false)
	private long myMaxSuccessResponseMessageBytes;
	@Column(name = "MIN_SUC_REQ_BYTES", nullable = false)
	private long myMinSuccessRequestMessageBytes = -1;
	@Column(name = "MIN_SUC_RES_BYTES", nullable = false)
	private long myMinSuccessResponseMessageBytes;

	@Column(name = "TOT_FAIL_INV_COUNT", nullable = false)
	private long myTotalFailInvocationCount;
	@Column(name = "TOT_FAIL_INV_TIME", nullable = false)
	private long myTotalFailInvocationTime;
	@Column(name = "TOT_FAIL_REQ_BYTES", nullable = false)
	private long myTotalFailRequestMessageBytes;
	@Column(name = "TOT_FAIL_RES_BYTES", nullable = false)
	private long myTotalFailResponseMessageBytes;

	@Column(name = "TOT_FAULT_INV_COUNT", nullable = false)
	private long myTotalFaultInvocationCount;
	@Column(name = "TOT_FAULT_INV_TIME", nullable = false)
	private long myTotalFaultInvocationTime;
	@Column(name = "TOT_FAULT_REQ_BYTES", nullable = false)
	private long myTotalFaultRequestMessageBytes;
	@Column(name = "TOT_FAULT_RES_BYTES", nullable = false)
	private long myTotalFaultResponseMessageBytes;

	@Column(name = "TOT_SUC_INV_COUNT", nullable = false)
	private long myTotalSuccessInvocationCount;
	@Column(name = "TOT_SUC_INV_TIME", nullable = false)
	private long myTotalSuccessInvocationTime;
	@Column(name = "TOT_SUC_REQ_BYTES", nullable = false)
	private long myTotalSuccessRequestMessageBytes;
	@Column(name = "TOT_SUC_RES_BYTES", nullable = false)
	private long myTotalSuccessResponseMessageBytes;

	public synchronized void addFailInvocation(long theTime, long theRequestBytes, long theResponseBytes) {
		myTotalFailInvocationCount++;
		myTotalFailInvocationTime += theTime;
		myTotalFailRequestMessageBytes += theRequestBytes;
		myTotalFailResponseMessageBytes += theResponseBytes;
	}

	public synchronized void addFaultInvocation(long theTime, long theRequestBytes, long theResponseBytes) {
		myTotalFaultInvocationCount++;
		myTotalFaultInvocationTime += theTime;
		myTotalFaultRequestMessageBytes += theRequestBytes;
		myTotalFaultResponseMessageBytes += theResponseBytes;
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
	 * @return the maxSuccessRequestMessageBytes
	 */
	public synchronized long getMaxSuccessRequestMessageBytes() {
		return myMaxSuccessRequestMessageBytes;
	}

	/**
	 * @return the maxSuccessResponseMessageBytes
	 */
	public synchronized long getMaxSuccessResponseMessageBytes() {
		return myMaxSuccessResponseMessageBytes;
	}

	/**
	 * @return the minSuccessRequestMessageBytes
	 */
	public synchronized long getMinSuccessRequestMessageBytes() {
		return myMinSuccessRequestMessageBytes;
	}

	/**
	 * @return the minSuccessResponseMessageBytes
	 */
	public synchronized long getMinSuccessResponseMessageBytes() {
		return myMinSuccessResponseMessageBytes;
	}

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

	public synchronized long getSuccessInvocationTotalTime() {
		return myTotalSuccessInvocationTime;
	}

	public synchronized long getSuccessInvocationCount() {
		return myTotalSuccessInvocationCount;
	}


	public synchronized void mergeUnsynchronizedEvents(BasePersMethodStats theStats) {
		BasePersInvocationStats stats = (BasePersInvocationStats)theStats;
		synchronized (stats) {
			myTotalSuccessInvocationCount += stats.myTotalSuccessInvocationCount;
			myTotalSuccessInvocationTime += stats.myTotalSuccessInvocationTime;
			myTotalSuccessRequestMessageBytes += stats.myTotalSuccessRequestMessageBytes;
			myTotalSuccessResponseMessageBytes += stats.myTotalSuccessResponseMessageBytes;

			myTotalFailInvocationCount += stats.myTotalFailInvocationCount;
			myTotalFailInvocationTime += stats.myTotalFailInvocationTime;
			myTotalFailRequestMessageBytes += stats.myTotalFailRequestMessageBytes;
			myTotalFailResponseMessageBytes += stats.myTotalFailResponseMessageBytes;

			myTotalFaultInvocationCount += stats.myTotalFaultInvocationCount;
			myTotalFaultInvocationTime += stats.myTotalFaultInvocationTime;
			myTotalFaultRequestMessageBytes += stats.myTotalFaultRequestMessageBytes;
			myTotalFaultResponseMessageBytes += stats.myTotalFaultResponseMessageBytes;

			if (myMinSuccessRequestMessageBytes == -1) {
				myMinSuccessRequestMessageBytes = stats.myMinSuccessRequestMessageBytes;
				myMaxSuccessRequestMessageBytes = stats.myMaxSuccessRequestMessageBytes;
				myMinSuccessResponseMessageBytes = stats.myMinSuccessResponseMessageBytes;
				myMaxSuccessResponseMessageBytes = stats.myMaxSuccessResponseMessageBytes;
			} else {
				myMinSuccessRequestMessageBytes = Math.min(myMinSuccessRequestMessageBytes, stats.myMinSuccessRequestMessageBytes);
				myMaxSuccessRequestMessageBytes = Math.max(myMaxSuccessRequestMessageBytes, stats.myMaxSuccessRequestMessageBytes);
				myMinSuccessResponseMessageBytes = Math.min(myMinSuccessResponseMessageBytes, stats.myMinSuccessResponseMessageBytes);
				myMaxSuccessResponseMessageBytes = Math.max(myMaxSuccessResponseMessageBytes, stats.myMaxSuccessResponseMessageBytes);
			}
		}
	}

}
