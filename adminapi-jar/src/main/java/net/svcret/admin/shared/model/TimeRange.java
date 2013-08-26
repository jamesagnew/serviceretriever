package net.svcret.admin.shared.model;

import java.util.Date;

public class TimeRange {

	// private static final long serialVersionUID = 1L;

	private Date myNoPresetFrom;
	private Long myNoPresetFromDate;
	private Long myNoPresetFromTime;
	private Date myNoPresetTo;
	private Long myNoPresetToDate;
	private Long myNoPresetToTime;
	private TimeRangeEnum myWithPresetRange;
	private Date myWithPresetRangeEndForUnitTest;

	public void fromUrlValue(String theValue) {
		if (theValue.startsWith("P")) {
			myNoPresetFromDate = null;
			myNoPresetFromTime = null;
			myNoPresetToDate = null;
			myNoPresetToTime = null;
			myWithPresetRange = TimeRangeEnum.valueOf(theValue.substring(1));
		} else {
			String[] parts = theValue.split("_");
			myNoPresetFromDate = (Long.parseLong(parts[0]));
			myNoPresetFromTime = (Long.parseLong(parts[1]));
			myNoPresetToDate = (Long.parseLong(parts[2]));
			myNoPresetToTime = (Long.parseLong(parts[3]));
			myWithPresetRange = null;
		}
	}

	public Date getNoPresetFrom() {
		return myNoPresetFrom;
	}

	public Long getNoPresetFromDate() {
		return myNoPresetFromDate;
	}

	public Long getNoPresetFromTime() {
		return myNoPresetFromTime;
	}

	public Date getNoPresetTo() {
		return myNoPresetTo;
	}

	public Long getNoPresetToDate() {
		return myNoPresetToDate;
	}

	public Long getNoPresetToTime() {
		return myNoPresetToTime;
	}

	/**
	 * @return the range
	 */
	public TimeRangeEnum getWithPresetRange() {
		return myWithPresetRange;
	}

	public Date getWithPresetRangeEndForUnitTest() {
		return myWithPresetRangeEndForUnitTest;
	}

	public void setNoPresetFrom(Date theNoPresetFrom) {
		myNoPresetFrom = theNoPresetFrom;
	}

	public void setNoPresetFromDate(Long theNoPresetFromDate) {
		myNoPresetFromDate = theNoPresetFromDate;
	}

	public void setNoPresetFromTime(Long theNoPresetFromTime) {
		myNoPresetFromTime = theNoPresetFromTime;
	}

	public void setNoPresetTo(Date theNoPresetTo) {
		myNoPresetTo = theNoPresetTo;
	}

	public void setNoPresetToDate(Long theNoPresetToDate) {
		myNoPresetToDate = theNoPresetToDate;
	}

	public void setNoPresetToTime(Long theNoPresetToTime) {
		myNoPresetToTime = theNoPresetToTime;
	}

	public void setWithPresetRange(TimeRangeEnum theWithPresetRange) {
		myWithPresetRange = theWithPresetRange;
	}

	public String toUrlValue() {
		if (myWithPresetRange != null) {
			return "P"+myWithPresetRange.name();
		}
		StringBuilder b = new StringBuilder();
		b.append(myNoPresetFromDate);
		b.append('_');
		b.append(myNoPresetFromTime);
		b.append('_');
		b.append(myNoPresetToDate);
		b.append('_');
		b.append(myNoPresetToTime);
		return b.toString();
	}

	public void setWithPresetRangeEndForUnitTest(Date theParse) {
		myWithPresetRangeEndForUnitTest=theParse;
	}

}
