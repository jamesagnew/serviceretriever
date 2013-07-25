package net.svcret.admin.shared.model;

import java.io.Serializable;

import net.svcret.admin.shared.enm.ThrottlePeriodEnum;

public class GThrottle implements Serializable {

	private static final long serialVersionUID = 1L;

	private Integer myMaxRequests;
	private ThrottlePeriodEnum myPeriod;
	private Integer myQueue;

	public Integer getMaxRequests() {
		return myMaxRequests;
	}

	public void setMaxRequests(Integer theMaxRequests) {
		myMaxRequests = theMaxRequests;
	}

	public ThrottlePeriodEnum getPeriod() {
		return myPeriod;
	}

	public void setPeriod(ThrottlePeriodEnum thePeriod) {
		myPeriod = thePeriod;
	}

	public Integer getQueue() {
		return myQueue;
	}

	public void setQueue(Integer theQueue) {
		myQueue = theQueue;
	}

}
