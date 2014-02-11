package net.svcret.core.util;

public class DerbyLogConfigurator {

	public void setLogFileLocation(String theLogLocation) {
		System.setProperty("derby.stream.error.file", theLogLocation);
	}
	
}
