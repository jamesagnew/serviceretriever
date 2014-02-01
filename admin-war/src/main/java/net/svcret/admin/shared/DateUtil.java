package net.svcret.admin.shared;

import static net.svcret.admin.client.AdminPortal.MSGS;

import java.util.Date;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.safehtml.shared.SafeHtml;

public class DateUtil {

	private static DateTimeFormat ourDateFormat = DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_LONG);

	private static DateTimeFormat ourTimeFormat = DateTimeFormat.getFormat(PredefinedFormat.TIME_MEDIUM);

	public static final long MILLIS_PER_MINUTE = 60 * 1000L;
	public static final long MILLIS_PER_HOUR = 60 * MILLIS_PER_MINUTE;
	public static final long MILLIS_PER_DAY = MILLIS_PER_HOUR * 24;
	public static final long MILLIS_PER_SECOND = 1000L;

	// TODO: this method should be called formatDateTime I guess?
	public static String formatTime(Date theTime) {
		if (theTime == null) {
			return "";
		}
		return ourDateFormat.format(theTime);
	}

	public static String formatTimeOnly(Date theTime) {
		if (theTime == null) {
			return "";
		}
		return ourTimeFormat.format(theTime);
	}

	public static String formatTimeElapsedForLastInvocation(Date theLastInvoc) {
		return formatTimeElapsedForLastInvocation(theLastInvoc, false);
	}

	public static String formatTimeElapsedForLastInvocation(Date theLastInvoc, boolean theExactForUnder60Secs) {
		return formatTimeElapsed(theLastInvoc, theExactForUnder60Secs, " ago");
	}
	public static String formatTimeElapsed(Date theLastInvoc, boolean theExactForUnder60Secs) {
		return formatTimeElapsed(theLastInvoc, theExactForUnder60Secs, "");
	}
	
	public static String formatTimeElapsed(Date theLastInvoc, boolean theExactForUnder60Secs, String theSuffix) {
		long age = theLastInvoc != null ? System.currentTimeMillis() - theLastInvoc.getTime() : 0;
		String text;
		String suffix;
		if (theLastInvoc == null) {
			text = MSGS.dashboard_LastInvocNever();
			suffix = "";
		} else if (age < MILLIS_PER_MINUTE) {
			if (theExactForUnder60Secs) {
				int ageSecs = (int) (age / MILLIS_PER_SECOND);
				if (ageSecs < 1) {
					ageSecs = 1;
				}
				text = (MSGS.dashboard_LastInvocExactUnder60Secs(ageSecs));
			} else {
				text = (MSGS.dashboard_LastInvocUnder60Secs());
			}
			suffix = theSuffix;
		} else if (age < MILLIS_PER_HOUR) {
			text = (MSGS.dashboard_LastInvocUnder1Hour((int) (age / MILLIS_PER_MINUTE)));
			suffix = theSuffix;
		} else if (age < MILLIS_PER_DAY) {
			text = (MSGS.dashboard_LastInvocUnder1Day((int) (age / MILLIS_PER_HOUR)));
			suffix = theSuffix;
		} else {
			text = (MSGS.dashboard_LastInvocOver1Day((int) (age / MILLIS_PER_DAY)));
			suffix = theSuffix;
		}

		return text + suffix;
	}

	public static SafeHtml formatTimeElapsedForMessage(Date theTransactionTime) {
		long age = theTransactionTime != null ? System.currentTimeMillis() - theTransactionTime.getTime() : 0;
		SafeHtml text;
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
