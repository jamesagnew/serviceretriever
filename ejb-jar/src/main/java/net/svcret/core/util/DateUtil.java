package net.svcret.core.util;

import java.util.Date;

public class DateUtil {

	/**
	 * Returns true if theDate2 is after theDate1 and accounts for SQL Timestamp
	 */
	public static boolean after(Date theDate1, Date theDate2) {
		long time1 = theDate1.getTime();
		long time2 = theDate2.getTime();
		boolean retVal = time2 < time1;
		return retVal;
	}
	
	/**
	 * Returns true if theDate2 is before theDate1 and accounts for SQL Timestamp
	 */
	public static boolean before(Date theDate1, Date theDate2) {
		long time1 = theDate1.getTime();
		long time2 = theDate2.getTime();
		return time2 > time1;
	}

}
