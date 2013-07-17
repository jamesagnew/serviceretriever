package net.svcret.admin.client.ui.stats;

import static net.svcret.admin.client.AdminPortal.MSGS;

import java.util.Date;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;

public class DateUtil {

	private static DateTimeFormat ourDateFormat = DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_LONG);
	
	public static final long MILLIS_PER_MINUTE = 60 * 1000L;
	public static final long MILLIS_PER_HOUR = 60 * MILLIS_PER_MINUTE;
	public static final long MILLIS_PER_DAY = MILLIS_PER_HOUR * 24;

	public static String formatTime(Date theTime) {
		if (theTime==null) {
			return "";
		}
		return ourDateFormat.format(theTime);
	}

	public static String formatTimeElapsedForLastInvocation(Date theLastInvoc) {
		long age = theLastInvoc != null ? System.currentTimeMillis() - theLastInvoc.getTime() : 0;
		String text;
		if (theLastInvoc == null) {
			text = MSGS.dashboard_LastInvocNever();
		} else if (age < MILLIS_PER_MINUTE) {
			text = (MSGS.dashboard_LastInvocUnder60Secs());
		} else if (age < MILLIS_PER_HOUR) {
			text = (MSGS.dashboard_LastInvocUnder1Hour((int) (age / MILLIS_PER_MINUTE)));
		} else if (age < MILLIS_PER_DAY) {
			text = (MSGS.dashboard_LastInvocUnder1Day((int) (age / MILLIS_PER_HOUR)));
		} else {
			text = (MSGS.dashboard_LastInvocOver1Day((int) (age / MILLIS_PER_DAY)));
		}
		
		return text;
	}

	public static String formatTimeElapsedForMessage(Date theTransactionTime) {
		long age = theTransactionTime != null ? System.currentTimeMillis() - theTransactionTime.getTime() : 0;
		String text;
		if (theTransactionTime == null) {
			text = MSGS.dashboard_TransactionDateNever();
		} else if (age < MILLIS_PER_MINUTE) {
			text = (MSGS.dashboard_TransactionDateUnder60Secs(theTransactionTime));
		} else if (age < MILLIS_PER_HOUR) {
			text = (MSGS.dashboard_TransactionDateUnder1Hour(theTransactionTime, (int) (age / MILLIS_PER_MINUTE)));
		} else if (age < MILLIS_PER_DAY) {
			text = (MSGS.dashboard_TransactionDateUnder1Day(theTransactionTime, (int) (age / MILLIS_PER_HOUR)));
		} else {
			text = (MSGS.dashboard_TransactionDateOver1Day(theTransactionTime, (int) (age / MILLIS_PER_DAY)));
		}
		
		return text;

	}

}
