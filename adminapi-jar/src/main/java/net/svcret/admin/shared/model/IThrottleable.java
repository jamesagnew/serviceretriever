package net.svcret.admin.shared.model;

import net.svcret.admin.shared.enm.ThrottlePeriodEnum;

public interface IThrottleable {

	Integer getThrottleMaxQueueDepth();

	Integer getThrottleMaxRequests();

	ThrottlePeriodEnum getThrottlePeriod();

	void setThrottleMaxQueueDepth(Integer theThrottleMaxQueueDepth);

	void setThrottleMaxRequests(Integer theThrottleMaxRequests);

	void setThrottlePeriod(ThrottlePeriodEnum theThrottlePeriod);

}