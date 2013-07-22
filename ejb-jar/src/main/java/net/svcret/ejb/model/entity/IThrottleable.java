package net.svcret.ejb.model.entity;

public interface IThrottleable {

	public abstract Integer getThrottleMaxQueueDepth();

	public abstract Integer getThrottleMaxRequests();

	public abstract ThrottlePeriodEnum getThrottlePeriod();

}