package net.svcret.ejb.model.entity;

import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;

public enum InvocationStatsIntervalEnum {

	MINUTE, HOUR, DAY;

	public Date truncate(Date theDate) {
		switch (this) {
		case DAY:
			return DateUtils.truncate(theDate, Calendar.DAY_OF_MONTH);
		case HOUR:
			return DateUtils.truncate(theDate, Calendar.HOUR_OF_DAY);
		case MINUTE:
			return DateUtils.truncate(theDate, Calendar.MINUTE);
		default:
			throw new IllegalStateException("Unknown constant: " + this);
		}
	}

}
