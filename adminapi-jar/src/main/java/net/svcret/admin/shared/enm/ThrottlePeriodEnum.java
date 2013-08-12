package net.svcret.admin.shared.enm;

import java.util.ArrayList;
import java.util.List;

public enum ThrottlePeriodEnum {
	DAY("Day") {
		@Override
		public double toRequestsPerSecond(Integer theThrottleMaxRequests) {
			return theThrottleMaxRequests / (60*60*24d);
		}
	},
	HOUR("Hour") {
		@Override
		public double toRequestsPerSecond(Integer theThrottleMaxRequests) {
			return theThrottleMaxRequests / (60*60d);
		}
	},
	MINUTE("Minute") {
		@Override
		public double toRequestsPerSecond(Integer theThrottleMaxRequests) {
			return theThrottleMaxRequests / 60d;
		}
	},
	SECOND("Second") {
		@Override
		public double toRequestsPerSecond(Integer theThrottleMaxRequests) {
			return theThrottleMaxRequests;
		}
	};


	private String myDescription;

	public String getDescription() {
		return myDescription;
	}

	private ThrottlePeriodEnum(String theDescription) {
		myDescription = theDescription;
	}

	public abstract double toRequestsPerSecond(Integer theThrottleMaxRequests);

	public String getFriendlyName(int theForNumber) {
		if (theForNumber==1) {
			return myDescription;
		}
		return myDescription+"s";
	}

	public static List<String> getDescriptions() {
		ArrayList<String> retVal = new ArrayList<String>();
		for (ThrottlePeriodEnum next : values()) {
			retVal.add(next.getDescription());
		}
		return retVal;
	}

	public static ThrottlePeriodEnum forDescription(String theValue) {
		for (ThrottlePeriodEnum next : values()) {
			if (next.getDescription().equals(theValue)) {
				return next;
			}
		}
		throw new IllegalArgumentException("Unknown description: "+theValue);
	}

}
