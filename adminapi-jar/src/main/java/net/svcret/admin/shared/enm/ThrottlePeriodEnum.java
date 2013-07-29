package net.svcret.admin.shared.enm;

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

}
