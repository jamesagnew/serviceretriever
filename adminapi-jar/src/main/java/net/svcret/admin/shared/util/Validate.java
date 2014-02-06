package net.svcret.admin.shared.util;

import java.util.Collection;

import net.svcret.admin.api.ProcessingException;

import org.apache.commons.lang3.StringUtils;

public class Validate extends org.apache.commons.lang3.Validate {

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

	public static void greaterThanZero(long theLong, String theName) {
		if (theLong <= 0) {
			throw new IllegalArgumentException(theName + " must be greater than zero");
		}
	}

	private static void greaterThanZero(int theNumber, String theName) {
		if (theNumber <= 0) {
			throw new IllegalArgumentException(theName + " must be a positive integer");
		}
	}

	public static void greaterThanZero(Integer theNumber, String theName) {
		if (theNumber == null) {
			throw new IllegalArgumentException(theName + " must not be null");
		}
		greaterThanZero(theNumber.intValue(), theName);
	}

	public static void throwProcessingExceptionIfBlank(String theString, String theMessage) throws ProcessingException {
		if (StringUtils.isBlank(theString)) {
			throw new ProcessingException(theMessage);
		}
	}

	public static void throwProcessingExceptionIfEmpty(Collection<?> theCollection, String theMessage) throws ProcessingException {
		if (theCollection == null || theCollection.isEmpty()) {
			throw new ProcessingException(theMessage);
		}
	}

	public static void throwIllegalArgumentExceptionIfNotPositive(Long theLong, String theName) {
		if (theLong == null) {
			throw new IllegalArgumentException(theName + " must not be null");
		}
		if (theLong <= 0) {
			throw new IllegalArgumentException(theName + " must be a positive number");
		}
	}

	public static void isNull(Object theObject, String theMessage) {
		if (theObject != null) {
			throw new IllegalArgumentException(theMessage + " must be null");
		}
	}

	public static void notNegative(int theNumber) {
		if (theNumber < 0) {
			throw new IllegalArgumentException("Number must not be negative: " + theNumber);
		}
	}

	public static void isNull(Object theObject) {
		if (theObject != null) {
			throw new IllegalArgumentException("Must be null");
		}
	}

}
