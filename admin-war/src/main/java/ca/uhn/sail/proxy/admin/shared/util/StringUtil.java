package ca.uhn.sail.proxy.admin.shared.util;

public class StringUtil {

	public static String defaultString(String theToken) {
		if (theToken == null) {
			theToken = "";
		}
		return theToken;
	}

	public static boolean isBlank(String theString) {
		if (theString == null || theString.trim().length() == 0) {
			return true;
		}
		return false;
	}

	
	
}
