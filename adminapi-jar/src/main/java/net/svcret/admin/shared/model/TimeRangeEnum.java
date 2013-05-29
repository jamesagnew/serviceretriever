package net.svcret.admin.shared.model;

public enum TimeRangeEnum {

	ONE_HOUR(60, "Last Hour"),

	SIX_HOURS(6 * 60, "Last 6 Hours"),

	TWELVE_HOURS(12 * 60, "Last 12 Hours"),

	ONE_DAY(24 * 60, "Last Day"),

	ONE_WEEK(7 * 24 * 60, "Last Week"),

	ONE_MONTH(31 * 24 * 60, "Last Month");

	private int myNumMins;
	private String myFriendlyName;

	private TimeRangeEnum(int theNumMins, String theFriendlyName) {
		myNumMins = theNumMins;
		myFriendlyName=theFriendlyName;
	}

	/**
	 * @return the numMins
	 */
	public int getNumMins() {
		return myNumMins;
	}

	public String getFriendlyName() {
		return myFriendlyName;
	}

}
