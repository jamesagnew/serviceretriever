package net.svcret.ejb.util;

import java.util.Collection;

import net.svcret.ejb.ex.ProcessingException;

import org.apache.commons.lang3.StringUtils;


public class Validate {

	public static boolean isNotBlankSimpleInteger(String theString) {
		if (theString == null || theString.length() == 0) {
			return false;
		}

		for (int i = 0; i < theString.length(); i++) {
			char nextChar = theString.charAt(i);
			if (nextChar < '0' || nextChar > '9') {
				return false;
			}
		}

		return true;
	}

	public static void throwIllegalArgumentExceptionIfBlank(String theName, String theString) {
		if (StringUtils.isBlank(theString)) {
			throw new IllegalArgumentException(theName + " can not be blank");
		}
	}

	public static void throwIllegalArgumentExceptionIfNotGreaterThanZero(String theName, long theLong) {
		if (theLong <= 0) {
			throw new IllegalArgumentException(theName + " must be greater than zero");
		}
	}

	public static void throwIllegalArgumentExceptionIfNull(String theName, Object theObject) {
		if (theObject == null) {
			throw new IllegalArgumentException(theName + " must not be null");
		}
	}

	public static void throwIllegalStateExceptionIfNotNull(String theName, Object theObject) {
		if (theObject != null) {
			throw new IllegalStateException("State problem - " + theName + " must be null");
		}
	}

	public static void throwProcessingExceptionIfBlank(String theMessage, String theString) throws ProcessingException {
		if (StringUtils.isBlank(theString)) {
			throw new ProcessingException(theMessage);
		}
	}

	public static void throwProcessingExceptionIfEmpty(String theMessage, Collection<?> theCollection) throws ProcessingException {
		if (theCollection == null || theCollection.isEmpty()) {
			throw new ProcessingException(theMessage);
		}
	}

}
