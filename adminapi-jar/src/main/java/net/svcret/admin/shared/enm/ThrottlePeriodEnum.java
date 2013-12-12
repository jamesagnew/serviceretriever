package net.svcret.admin.shared.enm;

import java.util.ArrayList;
import java.util.List;

public enum ThrottlePeriodEnum {
	DAY("Day") {
		@Override
		public double toRequestsPerSecond(Integer theInterval) {
			return 1.0 / (theInterval * 60.0 * 60.0 * 24.0);
		}
	},
	HOUR("Hour") {
		@Override
		public double toRequestsPerSecond(Integer theInterval) {
			return 1.0 / (theInterval * 60.0 * 60.0);
		}
	},
	MINUTE("Minute") {
		@Override
		public double toRequestsPerSecond(Integer theInterval) {
			return 1.0 / (theInterval * 60.0);
		}
	},
	SECOND("Second") {
		@Override
		public double toRequestsPerSecond(Integer theInterval) {
			return 1.0 / theInterval;
		}
	};

	private String myDescription;

	public String getDescription() {
		return myDescription;
	}

	private ThrottlePeriodEnum(String theDescription) {
		myDescription = theDescription;
	}

	public abstract double toRequestsPerSecond(Integer theInterval);

	public String getFriendlyName(int theForNumber) {
		if (theForNumber == 1) {
			return myDescription;
		}
		return myDescription + "s";
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
		throw new IllegalArgumentException("Unknown description: " + theValue);
	}

}
