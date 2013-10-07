package net.svcret.ejb.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogUtil {

	private static final DateFormat TIME_FMT = new SimpleDateFormat("HH:mm:ss.SSS");
	
	public static final String formatTime(Date theDate) {
		 return theDate != null ? TIME_FMT.format(theDate) : "null";
	}
	
	public static String formatByteCount(long bytes, boolean si) {
	    int unit = si ? 1000 : 1024;
	    if (bytes < unit) return bytes + " B";
	    int exp = (int) (Math.log(bytes) / Math.log(unit));
	    String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
	    return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}
	
}
