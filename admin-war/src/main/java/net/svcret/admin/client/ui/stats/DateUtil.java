package net.svcret.admin.client.ui.stats;

import java.util.Date;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;

public class DateUtil {

	private static DateTimeFormat ourDateFormat = DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_LONG);

	public static String formatTime(Date theTime) {
		if (theTime==null) {
			return "";
		}
		return ourDateFormat.format(theTime);
	}

}
