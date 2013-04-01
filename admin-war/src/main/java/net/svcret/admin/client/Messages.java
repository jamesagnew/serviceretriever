package net.svcret.admin.client;

public interface Messages extends com.google.gwt.i18n.client.Messages {

	@DefaultMessage("Every service invocation will use an HTTP Client Configuration. These " +
			"configurations may be shared among multiple service implementations. At " +
			"a minimum, a configuration named 'DEFAULT' will always exist, but you may " +
			"also create others for specific purposes.")
	String httpClientConfigsPanel_IntroMessage();

	@DefaultMessage("HTTP Client Config")
	String httpClientConfigsPanel_ListTitle();

	@DefaultMessage("URL Selection Policy")
	String httpClientConfigsPanel_UrlSelectionTitle();

	@DefaultMessage("If the service " +
			"implementation has more than one URL defined (i.e. there " +
			"are multiple redundant implementations) the URL Selection Policy defines how the " +
			"proxy should select which implementation to use.")
	String httpClientConfigsPanel_UrlSelectionDescription();

	@DefaultMessage("ID")
	String propertyNameId();

	@DefaultMessage("Name")
	String propertyNameName();

	@DefaultMessage("<b>Prefer Local</b> means that the proxy will favour any URLs which are on " +
			"the same host as the service retriever itself, and will only use remote " +
			"implementations if all local URLs are down")
	String urlSelectionPolicy_Desc_PreferLocal();

	@DefaultMessage("Edit Details")
	String httpClientConfigsPanel_EditDetailsTitle();

	@DefaultMessage("Circuit Breaker")
	String httpClientConfigsPanel_CircuitBreakerTitle();

	@DefaultMessage("A <a href=\"http://en.wikipedia.org/wiki/Circuit_breaker_design_pattern\" target=\"_blank\">Circuit Breaker</a> " +
			"remembers when a service implementation is down and prevents the proxy from trying to access that service " +
			"repeatedly. In other words, when an attempt to invoke a service fails for some reason, the proxy will remember that " +
			"the service has failed and will not attempt to invoke that service again until a given number of milliseconds has " +
			"elapsed (the reset period). Circuit breakers are particularly useful if there are multiple backing implementations, " +
			"since the proxy will remember the state for each implementation and will quickly move to a good one when " +
			"one backing implementation is failing.")
	String httpClientConfigsPanel_CircuitBreakerDescription();

	@DefaultMessage("Enabled")
	String httpClientConfigsPanel_CircuitBreakerEnabled();

	@DefaultMessage("Reset Period")
	String httpClientConfigsPanel_CircuitBreakerDelayBetweenReset();

	@DefaultMessage("Policy")
	String httpClientConfigsPanel_UrlSelectionPolicyShortName();

	@DefaultMessage("TCP Properties")
	String httpClientConfigsPanel_TcpProperties();
	
	@DefaultMessage("Use the following settings to control the outbound connection settings. These should " +
			"be adjusted to provide sensible defaults so that services which are hung don''t block for too long " +
			"while still allowing for even the longest legitimate queries.")
	String httpClientConfigsPanel_TcpPropertiesDesc();

	@DefaultMessage("Read Timeout (millis):")
	String httpClientConfigsPanel_TcpReadMillis();
	
	@DefaultMessage("Connect Timeout (millis):")
	String httpClientConfigsPanel_TcpConnectMillis();

	@DefaultMessage("Retries")
	String httpClientConfigsPanel_RetriesTitle();

	@DefaultMessage("Set the number of retries the proxy will make against a single backing " +
			"implementation URL before moving on to the next one. For instance, if this is set to " +
			"2 and you have URLs A and B, the proxy will try A three times before moving to B if A " +
			"is failing.")
	String httpClientConfigsPanel_RetriesDesc();

	@DefaultMessage("Retries")
	String httpClientConfigsPanel_RetriesLabel();

	@DefaultMessage("Save")
	String actions_Save();

	@DefaultMessage("Connect timeout must be a positive integer (in millis)")
	String httpClientConfigsPanel_validateFailed_ConnectTimeout();

	@DefaultMessage("Read timeout must be a positive integer (in millis)")
	String httpClientConfigsPanel_validateFailed_ReadTimeout();

	@DefaultMessage("Retries must be 0 or a positive integer")
	String httpClientConfigsPanel_validateFailed_Retries();

	@DefaultMessage("Circuit breaker reset period must be a positive integer (in millis)")
	String httpClientConfigsPanel_validateFailed_CircuitBreakerDelay();

	@DefaultMessage("Cannot remove the default config")
	String httpClientConfigsPanel_CantDeleteDefault();

	@DefaultMessage("Confirm: Are you sure you want to delete the HTTP Client Config: {0}")
	String httpClientConfigsPanel_ConfirmDelete(String theConfigId);

}
