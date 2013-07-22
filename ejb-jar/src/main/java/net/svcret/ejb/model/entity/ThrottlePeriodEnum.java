package net.svcret.ejb.model.entity;


public enum ThrottlePeriodEnum {
	DAY {
		@Override
		public double toRequestsPerSecond(Integer theThrottleMaxRequests) {
			return theThrottleMaxRequests / MILLIS_PER_DAY;
		}
	},
	HOUR {
		@Override
		public double toRequestsPerSecond(Integer theThrottleMaxRequests) {
			return theThrottleMaxRequests / MILLIS_PER_HOUR;
		}
	},
	MINUTE {
		@Override
		public double toRequestsPerSecond(Integer theThrottleMaxRequests) {
			return theThrottleMaxRequests / MILLIS_PER_MINUTE;
		}
	},
	SECOND {
		@Override
		public double toRequestsPerSecond(Integer theThrottleMaxRequests) {
			return theThrottleMaxRequests;
		}
	};

    private static final long MILLIS_PER_SECOND = 1000;
    private static final long MILLIS_PER_MINUTE = 60 * MILLIS_PER_SECOND;
    private static final long MILLIS_PER_HOUR = 60 * MILLIS_PER_MINUTE;
    private static final long MILLIS_PER_DAY = 24 * MILLIS_PER_HOUR;

	public abstract double toRequestsPerSecond(Integer theThrottleMaxRequests);
	
}
