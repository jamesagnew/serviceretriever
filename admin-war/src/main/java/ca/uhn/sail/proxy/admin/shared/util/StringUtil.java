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

	
	
}
