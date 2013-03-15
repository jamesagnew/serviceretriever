package ca.uhn.sail.proxy.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogUtil {

	private static final DateFormat TIME_FMT = new SimpleDateFormat("HH:mm:ss.SSS");
	
	public static final String formatTime(Date theDate) {
		 return theDate != null ? TIME_FMT.format(theDate) : "null";
	}
	
}
