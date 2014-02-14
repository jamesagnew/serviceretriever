package net.svcret.admin.shared.enm;

import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;

public enum InvocationStatsIntervalEnum {

	MINUTE, HOUR, DAY, TEN_MINUTE;

	public Date truncate(Date theDate) {
		switch (this) {
		case DAY:
			return DateUtils.truncate(theDate, Calendar.DAY_OF_MONTH);
		case HOUR:
			return DateUtils.truncate(theDate, Calendar.HOUR_OF_DAY);
		case TEN_MINUTE: {
			Calendar cal = DateUtils.toCalendar(theDate);
			Calendar retVal = DateUtils.truncate(cal, Calendar.MINUTE);
			int mins = retVal.get(Calendar.MINUTE);
			mins = mins - (mins % 10);
			retVal.set(Calendar.MINUTE, mins);
			return retVal.getTime();
		}
		case MINUTE:
			return DateUtils.truncate(theDate, Calendar.MINUTE);
		default:
			throw new IllegalStateException("Unknown constant: " + this);
		}
	}

	public long millis() {
		switch (this) {
		case DAY:
			return DateUtils.MILLIS_PER_DAY;
		case HOUR:
			return DateUtils.MILLIS_PER_HOUR;
		case MINUTE:
			return DateUtils.MILLIS_PER_MINUTE;
		case TEN_MINUTE:
			return (10 * DateUtils.MILLIS_PER_MINUTE);
		}

		throw new IllegalStateException("Unknown constant: " + this);
	}

	public long numMinutes() {
		return millis() / DateUtils.MILLIS_PER_MINUTE;
	}

	public Date add(Date theNext) {
		switch (this) {
		case DAY:
			return DateUtils.addDays(theNext, 1);
		case HOUR:
			return DateUtils.addHours(theNext, 1);
		case MINUTE:
			return DateUtils.addMinutes(theNext, 1);
		case TEN_MINUTE:
			return DateUtils.addMinutes(theNext, 10);
		}

		throw new IllegalStateException("Unknown constant: " + this);
	}

}
