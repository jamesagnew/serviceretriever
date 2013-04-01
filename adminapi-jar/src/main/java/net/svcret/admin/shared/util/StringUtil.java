package net.svcret.admin.shared.util;

public class StringUtil {

	public static String defaultString(String theString) {
		if (theString == null) {
			theString = "";
		}
		return theString;
	}

	public static String defaultString(String theString, String theValue) {
		if (theString == null) {
			theString = theValue;
		}
		return theString;
	}

	public static boolean isBlank(String theString) {
		if (theString == null || theString.trim().length() == 0) {
			return true;
		}
		return false;
	}

	public static String obscure(String theString) {
		StringBuilder b = new StringBuilder();
		if (isBlank(theString)) {
			b.append("<none>");
		} else {
			for (int i = 0; i < theString.length(); i++) {
				b.append('*');
			}
		}
		return b.toString();
	}

	public static boolean isNotBlank(String theString) {
		return !isBlank(theString);
	}

	
	
}
