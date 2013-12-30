package net.svcret.admin.shared.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import net.svcret.admin.shared.enm.ThrottlePeriodEnum;

@XmlAccessorType(XmlAccessType.FIELD)
public class GThrottle implements Serializable, IThrottleable {

	private static final long serialVersionUID = 1L;

	@XmlElement(name = "config_ThrottleMaxQueueDepth")
	private Integer myThrottleMaxQueueDepth;
	@XmlElement(name = "config_ThrottleMaxRequests")
	private Integer myThrottleMaxRequests;
	@XmlElement(name = "config_ThrottlePeriod")
	private ThrottlePeriodEnum myThrottlePeriod;

	public Integer getThrottleMaxQueueDepth() {
		return myThrottleMaxQueueDepth;
	}

	public Integer getThrottleMaxRequests() {
		return myThrottleMaxRequests;
	}

	public ThrottlePeriodEnum getThrottlePeriod() {
		return myThrottlePeriod;
	}

	public void setThrottleMaxQueueDepth(Integer theThrottleMaxQueueDepth) {
		myThrottleMaxQueueDepth = theThrottleMaxQueueDepth;
	}

	public void setThrottleMaxRequests(Integer theThrottleMaxRequests) {
		myThrottleMaxRequests = theThrottleMaxRequests;
	}

	public void setThrottlePeriod(ThrottlePeriodEnum theThrottlePeriod) {
		myThrottlePeriod = theThrottlePeriod;
	}

}
