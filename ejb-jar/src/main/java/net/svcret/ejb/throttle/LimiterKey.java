package net.svcret.ejb.throttle;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import net.svcret.ejb.model.entity.PersUser;

class LimiterKey {
	
	private Integer myHashCode;
	private Integer myMaxQueuedRequests;
	private String myPropertyCaptureKey;
	private String myPropertyCaptureValue;
	private double myRequestsPerSecond;
	private PersUser myUser;

	public LimiterKey(PersUser theUser, String thePropertyCaptureKey, String thePropertyCaptureValue, double theRequestsPerSecond, Integer theMaxQueueDepth) {
		super();
		myUser = theUser;
		myPropertyCaptureKey = thePropertyCaptureKey;
		myPropertyCaptureValue = thePropertyCaptureValue;
		myRequestsPerSecond = theRequestsPerSecond;
		myMaxQueuedRequests = theMaxQueueDepth;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LimiterKey other = (LimiterKey) obj;
		if (myPropertyCaptureKey == null) {
			if (other.myPropertyCaptureKey != null)
				return false;
		} else if (!myPropertyCaptureKey.equals(other.myPropertyCaptureKey))
			return false;
		if (myPropertyCaptureValue == null) {
			if (other.myPropertyCaptureValue != null)
				return false;
		} else if (!myPropertyCaptureValue.equals(other.myPropertyCaptureValue))
			return false;
		if (myUser == null) {
			if (other.myUser != null)
				return false;
		} else if (!myUser.equals(other.myUser))
			return false;
		return true;
	}

	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
		if (myUser != null) {
			builder.append("user", myUser.getPid()+"/" + myUser.getUsername());
		}
		if (myPropertyCaptureKey != null) {
			builder.append("propertyCaptureKey", myPropertyCaptureKey);
			builder.append("propertyCaptureValue", myPropertyCaptureValue);
		}
		return builder.toString();
	}

	public Integer getMaxQueuedRequests() {
		return myMaxQueuedRequests;
	}

	public double getRequestsPerSecond() {
		return myRequestsPerSecond;
	}

	public PersUser getUser() {
		return myUser;
	}

	@Override
	public int hashCode() {
		if (myHashCode != null) {
			return myHashCode;
		}
		final int prime = 31;
		int result = 1;
		result = prime * result + ((myPropertyCaptureKey == null) ? 0 : myPropertyCaptureKey.hashCode());
		result = prime * result + ((myPropertyCaptureValue == null) ? 0 : myPropertyCaptureValue.hashCode());
		result = prime * result + ((myUser == null) ? 0 : myUser.hashCode());
		myHashCode = result;
		return result;
	}
}