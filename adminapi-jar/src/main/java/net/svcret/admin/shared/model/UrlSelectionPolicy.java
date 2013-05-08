package net.svcret.admin.shared.model;

import java.util.Arrays;

public enum UrlSelectionPolicy {

	PREFER_LOCAL,
	
	ROUND_ROBIN;

	public static int indexOf(UrlSelectionPolicy theUrlSelectionPolicy) {
		return Arrays.asList(values()).indexOf(theUrlSelectionPolicy);
	}
	
}
