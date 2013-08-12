package net.svcret.admin.shared.enm;

public enum MonitorRuleTypeEnum {

	ACTIVE("Active"),
	
	PASSIVE("Passive");

	private String myFriendlyName;

	private MonitorRuleTypeEnum(String theFriendlyName) {
		myFriendlyName=theFriendlyName;
	}
	
	public String getFriendlyName() {
		return myFriendlyName;
	}
	
}
