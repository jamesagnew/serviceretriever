package net.svcret.admin.shared.enm;

public enum ThrottlePeriodEnum {
	DAY("Day") {
		@Override
		public double toRequestsPerSecond(Integer theThrottleMaxRequests) {
			return theThrottleMaxRequests / MILLIS_PER_DAY;
		}
	},
	HOUR("Hour") {
		@Override
		public double toRequestsPerSecond(Integer theThrottleMaxRequests) {
			return theThrottleMaxRequests / MILLIS_PER_HOUR;
		}
	},
	MINUTE("Minute") {
		@Override
		public double toRequestsPerSecond(Integer theThrottleMaxRequests) {
			return theThrottleMaxRequests / MILLIS_PER_MINUTE;
		}
	},
	SECOND("Second") {
		@Override
		public double toRequestsPerSecond(Integer theThrottleMaxRequests) {
			return theThrottleMaxRequests;
		}
	};

	private static final long MILLIS_PER_SECOND = 1000;
	private static final long MILLIS_PER_MINUTE = 60 * MILLIS_PER_SECOND;
	private static final long MILLIS_PER_HOUR = 60 * MILLIS_PER_MINUTE;
	private static final long MILLIS_PER_DAY = 24 * MILLIS_PER_HOUR;

	private String myDescription;

	public String getDescription() {
		return myDescription;
	}

	private ThrottlePeriodEnum(String theDescription) {
		myDescription = theDescription;
	}

	public abstract double toRequestsPerSecond(Integer theThrottleMaxRequests);

}
