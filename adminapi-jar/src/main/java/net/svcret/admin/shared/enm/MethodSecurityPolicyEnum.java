package net.svcret.admin.shared.enm;

import java.util.ArrayList;
import java.util.List;

public enum MethodSecurityPolicyEnum {

	ALLOW("Allow", "<b>Allow</b> means that access will always be granted to this method, unless the user is specifically blocked from accessing it"),

	REJECT_UNLESS_ALLOWED(
			"Reject (can inherit)",
			"When set to <b>Reject unless allowed</b> (which is the default setting), access to this method will be granted only if the user has been granted permission to this method, or one of its parents. For instance if the user has been granted access to the domain in which this method is placed, the user will be granted access."),

	REJECT_UNLESS_SPECIFICALLY_ALLOWED("Reject strict",
			"When set to <b>Reject unless specifically allowed</b>, users will only be granted access to this method if they have been specifically granted permission. This means that permissions which are granted to parent elements (such as the domain or service) will not apply.");

	private final String myDescription;

	private final String myFriendlyName;

	private MethodSecurityPolicyEnum(String theFriendlyName, String theDescription) {
		myFriendlyName = theFriendlyName;
		myDescription = theDescription;
	}

	public String getDescription() {
		return myDescription;
	}

	public String getFriendlyName() {
		return myFriendlyName;
	}

	public static List<String> valuesAsNameList() {
		ArrayList<String> retVal = new ArrayList<String>();
		for (MethodSecurityPolicyEnum next : values()) {
			retVal.add(next.name());
		}
		return retVal;
	}

	public static List<String> valuesAsFriendlyNameList() {
		ArrayList<String> retVal = new ArrayList<String>();
		for (MethodSecurityPolicyEnum next : values()) {
			retVal.add(next.getFriendlyName());
		}
		return retVal;
	}

	public static MethodSecurityPolicyEnum getDefault() {
		return REJECT_UNLESS_ALLOWED;
	}

}
