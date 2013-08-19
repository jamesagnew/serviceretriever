package net.svcret.admin.shared.enm;

import java.util.Arrays;

public enum ServerSecurityModeEnum {

	NONE(false,"None", "In None mode, security credentials are not checked"),

	ALLOW_ANY(false,"Allow Any", "In Allow Any mode, security modules will attempt to find valid credentials, but the request will still be allowed to proceed if valid credentials aren't found. This mode means that ServiceRetriever does not actually provide any security, but that statistics are generated."),

	REQUIRE_ANY(true,"Require Any",
			"In Require Any mode, at least one security module must find valid credentials in all requests. A security module may fail to find valid credentials, but the request will stil be allowed to proceed if a difference security module finds credentials."),
	
	REQUIRE_ALL(true,"Require All", "In Require All mode, all security modules must find valid credentials in all requests");


	private String myDescription;
	private String myFriendlyName;
	private boolean mySecure;

	private ServerSecurityModeEnum(boolean theSecure, String theFriendlyName, String theDescription) {
		mySecure=theSecure;
		myFriendlyName = theFriendlyName;
		myDescription = theDescription;
	}

	public boolean isSecure() {
		return mySecure;
	}

	public String getDescription() {
		return myDescription;
	}

	public String getFriendlyName() {
		return myFriendlyName;
	}

	public static int indexOfDefault() {
		return Arrays.asList(values()).indexOf(REQUIRE_ANY);
	}
}
