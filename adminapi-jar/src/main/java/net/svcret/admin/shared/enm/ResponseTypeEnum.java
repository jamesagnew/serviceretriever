package net.svcret.admin.shared.enm;

import java.util.ArrayList;
import java.util.List;

public enum ResponseTypeEnum {

	SUCCESS("Success"),
	FAULT("Fault"),
	FAIL("Failure"), 
	SECURITY_FAIL("Security Failure"),
	THROTTLE_REJ("Throttle Rejection");

	ResponseTypeEnum(String theFriendlyName) {
		myFriendlyName = theFriendlyName;
	}

	private final String myFriendlyName;

	public String getFriendlyName() {
		return myFriendlyName;
	}

	public static List<String> backendStatusDescriptions() {
		List<String> retVal=new ArrayList<String>();
		retVal.add(SUCCESS.getFriendlyName());
		retVal.add(FAULT.getFriendlyName());
		retVal.add(FAIL.getFriendlyName());
		return retVal;
	}

	public static List<String> backendStatusIndexes() {
		List<String> retVal=new ArrayList<String>();
		retVal.add("0");
		retVal.add("1");
		retVal.add("2");
		return retVal;
	}
	
	public static ResponseTypeEnum forBackendStatusIndex(String theIndex) {
		if ("0".equals(theIndex)) {
			return SUCCESS;
		}
		if ("1".equals(theIndex)) {
			return FAULT;
		}
		if ("2".equals(theIndex)) {
			return FAIL;
		}
		throw new IllegalArgumentException(theIndex);
	}
}
