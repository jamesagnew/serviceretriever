package net.svcret.admin.client;

import com.google.gwt.safehtml.shared.SafeHtml;


public interface Messages extends com.google.gwt.i18n.client.Messages {

	@DefaultMessage("Add")
	String actions_Add();

	@DefaultMessage("Edit")
	String actions_Edit();

	@DefaultMessage("Remove")
	String actions_Remove();

	@DefaultMessage("Save")
	String actions_Save();

	@DefaultMessage("Add Domain")
	String addDomain_Breadcrumb();

	@DefaultMessage("Domain Added")
	String addDomainStep2_Breadcrumb();

	@DefaultMessage("Add Service")
	String addService_Breadcrumb();

	@DefaultMessage("Add Service Version")
	String addServiceVersion_Breadcrumb();

	@DefaultMessage("Each service can have one or more versions. A Service Version is the " +
			"central unit in a service definition, as it defines the fundamental building " +
			"block. A Service Version has a defined protocol, security model, and other " +
			"configuration. A Service Version will also have one or more Methods it " + 
			"can provide, and will be backed by one or more Implementation URLs. Each " +
			"Service will have one or more Service Versions, and Services are grouped " +
			"into Domains.")
	String addServiceVersion_Description();

	@DefaultMessage("Service Version Added")
	String addServiceVersionStep2_Breadcrumb();

	@DefaultMessage("Authentication Hosts")
	String authenticationHostsPanel_Breadcrumb();

	@DefaultMessage("An <b>Authentication host</b> is a module which is used to validate username/password " +//-
			"combinations and grant users access to specific services, as well as to administer the ServiceRetriever " +//-
			"itself. An <b>authentication host</b> can store its credentials in the ServiceRetriever configuration " +//-
			"database (this is a \"local database\" host) or can be backed by an external source (e.g. an LDAP/Active Directory " +//-
			"store).<br/><br/>" +//-
			"" +//-
			"In either case, each service user will have a record which is stored in the ServiceRetriever database which is " + //-
			"used to store specific permissions as well as to track usage statistics.")//-
	String authenticationHostsPanel_IntroMessage();

	@DefaultMessage("Authentication Hosts")
	String authenticationHostsPanel_ListTitle();

	@DefaultMessage("Enabled - Cache for Millis:")
	String baseAuthenticationHostEditPanel_CacheEnabledCacheForMillis();
	
	@DefaultMessage("If <b>cache responses</b> is enabled, the ServiceRetriever will keep a weak (SHA-512) hash of " +
			"any successful authentication credentials in memory for the given amount of time. This " +
			"means that performance will be improved as there will be fewer calls to the backing " +
			"credential store. Naturally, this does increase the chance that someone could " +
			"deduce your passwords if they have physical access to the server.")
	String baseAuthenticationHostEditPanel_CacheResponsesDesc();

	@DefaultMessage("Cache Responses")
	String baseAuthenticationHostEditPanel_CacheResponsesTitle();
	
	@DefaultMessage("Confirm: Are you sure you want to delete authentication host: {0}")
	String baseAuthenticationHostEditPanel_ConfirmDelete(String theModuleId);

	@DefaultMessage("Error: Unable to remove this authentication host, as you must have at least one host")
	String baseAuthenticationHostEditPanel_ErrorCantRemoveConfigOnlyOne();

	@DefaultMessage("Cache milliseconds must be a positive integer if caching is enabled")
	String baseAuthenticationHostEditPanel_ErrorNoCacheValue();

	@DefaultMessage("You must provide an ID")
	String baseAuthenticationHostEditPanel_errorNoId();

	@DefaultMessage("You must provide a Name")
	String baseAuthenticationHostEditPanel_errorNoName();

	@DefaultMessage("Saved Authentication Host")
	String baseAuthenticationHostEditPanel_Saved();

	@DefaultMessage("Edit Domain")
	String editDomain_Breadcrumb();

	@DefaultMessage("Edit User")
	String editUser_Dashboard();

	@DefaultMessage("Actions")
	String editUsersPanel_ColumnActions();

	@DefaultMessage("Username")
	String editUsersPanel_ColumnUsername();

	@DefaultMessage("Users")
	String editUsersPanel_Dashboard();

	@DefaultMessage("The following table lists all of the defined users. A user account may " +
			"be defined to have permissions to access specific servives, or to administer " +
			"them, or even to administer ServiceProxy itself.")
	String editUsersPanel_ListDescription();

	@DefaultMessage("Edit Users")
	String editUsersPanel_Title();

	@DefaultMessage("Cannot remove the default config")
	String httpClientConfigsPanel_CantDeleteDefault();

	@DefaultMessage("Reset Period")
	String httpClientConfigsPanel_CircuitBreakerDelayBetweenReset();

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

	@DefaultMessage("Circuit Breaker")
	String httpClientConfigsPanel_CircuitBreakerTitle();

	@DefaultMessage("Confirm: Are you sure you want to delete the HTTP Client Config: {0}")
	String httpClientConfigsPanel_ConfirmDelete(String theConfigId);

	@DefaultMessage("HTTP Client Config")
	String httpClientConfigsPanel_Dashboard();

	@DefaultMessage("Edit Details")
	String httpClientConfigsPanel_EditDetailsTitle();

	@DefaultMessage("Every service invocation will use an HTTP Client Configuration. These " +
			"configurations may be shared among multiple service implementations. At " +
			"a minimum, a configuration named 'DEFAULT' will always exist, but you may " +
			"also create others for specific purposes.")
	String httpClientConfigsPanel_IntroMessage();

	@DefaultMessage("HTTP Client Config")
	String httpClientConfigsPanel_ListTitle();

	@DefaultMessage("Set the number of retries the proxy will make against a single backing " +
			"implementation URL before moving on to the next one. For instance, if this is set to " +
			"2 and you have URLs A and B, the proxy will try A three times before moving to B if A " +
			"is failing.")
	String httpClientConfigsPanel_RetriesDesc();

	@DefaultMessage("Retries")
	String httpClientConfigsPanel_RetriesLabel();

	@DefaultMessage("Retries")
	String httpClientConfigsPanel_RetriesTitle();

	@DefaultMessage("Connect Timeout (millis):")
	String httpClientConfigsPanel_TcpConnectMillis();

	@DefaultMessage("TCP Properties")
	String httpClientConfigsPanel_TcpProperties();

	@DefaultMessage("Use the following settings to control the outbound connection settings. These should " +
			"be adjusted to provide sensible defaults so that services which are hung don''t block for too long " +
			"while still allowing for even the longest legitimate queries.")
	String httpClientConfigsPanel_TcpPropertiesDesc();

	@DefaultMessage("Read Timeout (millis):")
	String httpClientConfigsPanel_TcpReadMillis();

	@DefaultMessage("If the service " +
			"implementation has more than one URL defined (i.e. there " +
			"are multiple redundant implementations) the URL Selection Policy defines how the " +
			"proxy should select which implementation to use.")
	String httpClientConfigsPanel_UrlSelectionDescription();

	@DefaultMessage("Policy")
	String httpClientConfigsPanel_UrlSelectionPolicyShortName();

	@DefaultMessage("URL Selection Policy")
	String httpClientConfigsPanel_UrlSelectionTitle();

	@DefaultMessage("Circuit breaker reset period must be a positive integer (in millis)")
	String httpClientConfigsPanel_validateFailed_CircuitBreakerDelay();

	@DefaultMessage("Connect timeout must be a positive integer (in millis)")
	String httpClientConfigsPanel_validateFailed_ConnectTimeout();

	@DefaultMessage("Read timeout must be a positive integer (in millis)")
	String httpClientConfigsPanel_validateFailed_ReadTimeout();

	@DefaultMessage("Retries must be 0 or a positive integer")
	String httpClientConfigsPanel_validateFailed_Retries();

	@DefaultMessage("Authentication Hosts")
	String leftPanel_AuthenticationHosts();

	@DefaultMessage("Edit Users")
	String leftPanel_EditUsers();

	@DefaultMessage("HTTP Clients")
	String leftPanel_HttpClients();

	@DefaultMessage("A <b>Local Database</b> Authentication host stores users and their " +
			"passwords in the ServiceRetriever database. Use this option if you do not have " +
			"an external database or LDAP against which users can be authenticated.")
	String localDatabaseAuthenticationHostEditPanel_description();

	@DefaultMessage("Local Database")
	String localDatabaseAuthenticationHostEditPanel_title();

	@DefaultMessage("ID")
	String propertyNameId();

	@DefaultMessage("Name")
	String propertyNameName();

	@DefaultMessage("Dashboard")
	String serviceDashboard_Breadcrumb();

	@DefaultMessage("<b>Prefer Local</b> means that the proxy will favour any URLs which are on " +
			"the same host as the service retriever itself, and will only use remote " +
			"implementations if all local URLs are down")
	String urlSelectionPolicy_Desc_PreferLocal();

	@DefaultMessage("Edit User")
	String editUser_Title();

	@DefaultMessage("Administration Permissions")
	String permissionsPanel_AdministrationPermissions();

	@DefaultMessage("Any user with super user permissions is able to configure any aspects of " +
			"the ServiceProxy")
	String permissionsPanel_SuperUserDesc();

	@DefaultMessage("Super User")
	String permissionsPanel_SuperUserCheckbox();

	@DefaultMessage("Service Permissions")
	String permissionsPanel_ServicePermissionsTitle();

	@DefaultMessage("This user has the following service permissions. Note that the permission " +
			"list here applies only to services which are configured to use ServiceProxy host security.")
	String permissionsPanel_ServicePermissionsDesc();

	@DefaultMessage("Full access")
	SafeHtml permissionsPanel_AllDomainsCheckbox();

	@DefaultMessage("Full access")
	SafeHtml permissionsPanel_TreeAllServicesCheckbox();

	@DefaultMessage("Full access")
	SafeHtml permissionsPanel_TreeAllServiceVersionsCheckbox();

	@DefaultMessage("Full access")
	SafeHtml permissionsPanel_TreeAllMethodsCheckbox();

	@DefaultMessage("Username")
	String editUser_Username();

	@DefaultMessage("Change Password")
	String editUser_Password();

	@DefaultMessage("Saved User")
	String editUser_DoneSaving();

}
