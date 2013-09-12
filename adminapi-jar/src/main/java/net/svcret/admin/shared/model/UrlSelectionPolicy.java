package net.svcret.admin.shared.model;

import java.util.Arrays;

public enum UrlSelectionPolicy {

	PREFER_LOCAL,
	
	ROUND_ROBIN, 
	
	RR_STICKY_SESSION;

	public static int indexOf(UrlSelectionPolicy theUrlSelectionPolicy) {
		return Arrays.asList(values()).indexOf(theUrlSelectionPolicy);
	}
	
}
