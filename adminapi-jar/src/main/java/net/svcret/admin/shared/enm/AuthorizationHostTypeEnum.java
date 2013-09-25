package net.svcret.admin.shared.enm;

public enum AuthorizationHostTypeEnum {

	// TODO: i18n for these strings
	
	LOCAL_DATABASE("Local DB"),
	LDAP("LDAP");

	private String myDescription;

	private AuthorizationHostTypeEnum(String theDescription) {
		myDescription = theDescription;
	}
	
	public String description() {
		return myDescription;
	}
	
}
