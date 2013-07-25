package net.svcret.ejb.model.entity;

import net.svcret.admin.shared.enm.ThrottlePeriodEnum;

public interface IThrottleable {

	public abstract Integer getThrottleMaxQueueDepth();

	public abstract Integer getThrottleMaxRequests();

	public abstract ThrottlePeriodEnum getThrottlePeriod();

}