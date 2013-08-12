package net.svcret.admin.client;

import java.util.Date;

import net.svcret.admin.client.ui.components.CssConstants;

import com.google.gwt.i18n.client.impl.plurals.DefaultRule_en;
import com.google.gwt.safehtml.shared.SafeHtml;

public interface Messages extends com.google.gwt.i18n.client.Messages {

	static final String SVCVER_DESC = "Each service can have one or more versions. A Service Version is the " + "central unit in a service definition, as it defines the fundamental building "
			+ "block. A Service Version has a defined protocol, security model, and other " + "configuration. A Service Version will also have one or more Methods it "
			+ "can provide, and will be backed by one or more Implementation URLs. Each " + "Service will have one or more Service Versions, and Services are grouped " + "into Domains.";

	@DefaultMessage("Add")
	String actions_Add();

	@DefaultMessage("Add New...")
	String actions_AddNewDotDotDot();

	@DefaultMessage("Back")
	String actions_Back();

	@DefaultMessage("Edit")
	String actions_Edit();

	@DefaultMessage("Remove")
	String actions_Remove();

	@DefaultMessage("Delete Domain")
	String actions_RemoveDomain();

	@DefaultMessage("Remove Selected...")
	String actions_RemoveSelectedDotDotDot();

	@DefaultMessage("Delete Service")
	String actions_RemoveService();

	@DefaultMessage("Replay")
	String actions_Replay();

	@DefaultMessage("Save")
	String actions_Save();

	@DefaultMessage("Test / Manually Invoke")
	String actions_TestServiceVersion();

	@DefaultMessage("View")
	String actions_View();

	@DefaultMessage("View Runtime Status")
	String actions_ViewRuntimeStatus();

	@DefaultMessage("View Usage")
	String actions_ViewStats();

	@DefaultMessage("Add Domain")
	String addDomain_Breadcrumb();

	@DefaultMessage("Domain Added")
	String addDomainStep2_Breadcrumb();

	@DefaultMessage("Add Monitor Rule")
	String AddMonitorRule_Breadcrumb();

	@DefaultMessage("Add Service")
	String addService_Breadcrumb();

	@DefaultMessage("Add Service Version")
	String addServiceVersion_Breadcrumb();

	@DefaultMessage(SVCVER_DESC)
	String addServiceVersion_Description();

	@DefaultMessage("Service Version Added")
	String addServiceVersionStep2_Breadcrumb();

	@DefaultMessage("Add User")
	String addUser_Breadcrumb();

	@DefaultMessage("Authentication Hosts")
	String authenticationHostsPanel_Breadcrumb();

	@DefaultMessage("An <b>Authentication host</b> is a module which is used to validate username/password " + // -
			"combinations and grant users access to specific services, as well as to administer the ServiceRetriever " + // -
			"itself. An <b>authentication host</b> can store its credentials in the ServiceRetriever configuration " + // -
			"database (this is a \"local database\" host) or can be backed by an external source (e.g. an LDAP/Active Directory " + // -
			"store).<br/><br/>" + // -
			"" + // -
			"In either case, each service user will have a record which is stored in the ServiceRetriever database which is " + // -
			"used to store specific permissions as well as to track usage statistics.")
	// -
	String authenticationHostsPanel_IntroMessage();

	@DefaultMessage("Authentication Hosts")
	String authenticationHostsPanel_ListTitle();

	@DefaultMessage("Cache")
	String baseAuthenticationHostEditPanel_CacheResponses();

	@DefaultMessage("If <b>cache responses</b> is enabled, the ServiceRetriever will keep a weak (SHA-512) hash of "
			+ "any successful authentication credentials in memory for the given amount of time. This " + "means that performance will be improved as there will be fewer calls to the backing "
			+ "credential store. Naturally, this does increase the chance that someone could " + "deduce your passwords if they have physical access to the server.")
	String baseAuthenticationHostEditPanel_CacheResponsesDesc();

	@DefaultMessage("Enabled - Cache for (ms):")
	SafeHtml baseAuthenticationHostEditPanel_CacheResponsesEnabled();

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

	@DefaultMessage("Configuration")
	String config_Breadcrumb();

	@DefaultMessage("ServiceRetriever Configuration")
	String configPanel_Title();

	@DefaultMessage("URL Base")
	String configPanel_UrlBase();

	@DefaultMessage("The URL Base is the URL where the service proxy is deployed, and forms the base for "
			+ "any service endpoints exposed by ServiceRetriever. Typically this is a simple URL expressing the "
			+ "hostname and port of the server that ServiceRetriever is deployed (e.g. \"http://somehost:8080\" but "
			+ "if ServiceRetriever is deployed behind a load balancer or other network infrastructure this might "
			+ "be something different. Note that URLs within WSDL and XSD links for exposed services will be translated " + "to use this base as well.")
	String configPanel_UrlBaseDesc();

	@DefaultMessage("Domain has {0} Services")
	@AlternateMessage({ 
		"one", "Domain has one Service",
		"=0", "Domain has no Services"
		})
	String dashboard_ActionDomainServicesHeader(@PluralCount(DefaultRule_en.class) int theServiceCount);

	@DefaultMessage("Service has {0} Versions")
	@AlternateMessage({ 
		"one", "Service has one Version",
		"=0", "Service has no Versions"
		})
	String dashboard_ActionServiceVersionsHeader(@PluralCount(DefaultRule_en.class) int theServiceCount);

	@DefaultMessage(" <span class=\"" + CssConstants.DASHBOARD_NAME_SUFFIX + "\">(No Services Defined)</span>")
	String dashboard_DomainNoServicesSuffix();

	@DefaultMessage(" <span class=\"" + CssConstants.DASHBOARD_NAME_PREFIX + "\">Domain:</span>")
	String dashboard_DomainPrefix();

	@DefaultMessage("Never")
	String dashboard_LastInvocNever();

	@DefaultMessage("{0,number} days ago")
	@AlternateMessage({ "one", "1 day ago" })
	String dashboard_LastInvocOver1Day(@PluralCount(DefaultRule_en.class) int theDay);

	@DefaultMessage("{0,number} hrs ago")
	@AlternateMessage({ "one", "1 hr ago" })
	String dashboard_LastInvocUnder1Day(@PluralCount(DefaultRule_en.class) int theHour);

	@DefaultMessage("{0,number} mins ago")
	@AlternateMessage({ "one", "1 min ago" })
	String dashboard_LastInvocUnder1Hour(@PluralCount(DefaultRule_en.class) int theMins);

	@DefaultMessage("< 1min")
	String dashboard_LastInvocUnder60Secs();

	@DefaultMessage("Secured")
	String dashboard_SecuredFully();

	@DefaultMessage("Not Secured")
	String dashboard_SecuredNot();

	@DefaultMessage("Partial")
	String dashboard_SecuredPartial();

	@DefaultMessage(" <span class=\"" + CssConstants.DASHBOARD_NAME_SUFFIX + "\">(No Service Versions Defined)</span>")
	String dashboard_ServiceNoServiceVersionsSuffix();

	@DefaultMessage(" <span class=\"" + CssConstants.DASHBOARD_NAME_PREFIX + "\">Service:</span>")
	String dashboard_ServicePrefix();

	@DefaultMessage(" <span class=\"" + CssConstants.DASHBOARD_NAME_PREFIX + "\">Version:</span>")
	String dashboard_ServiceVersionPrefix();

	@DefaultMessage("")
	String dashboard_TransactionDateNever();

	@DefaultMessage("{0,localdatetime,MMMdd HHmm} ({1,number} days ago)")
	@AlternateMessage({ "one", "{0,localdatetime,MMMdd HHmm} (1 day ago)" })
	String dashboard_TransactionDateOver1Day(Date theDate, @PluralCount(DefaultRule_en.class) int theDay);

	@DefaultMessage("{0,localdatetime,HHmm} ({1,number} hrs ago)")
	@AlternateMessage({ "one", "{0,localdatetime,HHmm} (1 hr ago)" })
	String dashboard_TransactionDateUnder1Day(Date theDate, @PluralCount(DefaultRule_en.class) int theHour);

	@DefaultMessage("{0,localdatetime,HHmmss} ({1,number} mins ago)")
	@AlternateMessage({ "one", "{0,localdatetime,HHmmss} (1 min ago)" })
	String dashboard_TransactionDateUnder1Hour(Date theDate, @PluralCount(DefaultRule_en.class) int theMins);

	@DefaultMessage("{0,localdatetime,HHmmss} (< 1min)")
	String dashboard_TransactionDateUnder60Secs(Date theDate);

	@DefaultMessage("Delete Domain")
	String deleteDomainPanel_Breadcrumb();

	@DefaultMessage("Are you sure you want to delete the domain \"{0}\"? This will " + // -
			"delete all services associated with this domain as well!")
	String deleteDomainPanel_Confirm(String theId);

	@DefaultMessage("Delete Domain")
	String deleteDomainPanel_Title();

	@DefaultMessage("Delete Service")
	String deleteServicePanel_Breadcrumb();

	@DefaultMessage("Are you sure you want to delete the service \"{1}\" from domain \"{0}\"? This will " + // -
			"delete all service versions associated with this service as well!")
	String deleteServicePanel_Confirm(String theDomainId, String theServiceId);

	@DefaultMessage("Delete Service")
	String deleteServicePanel_Title();

	@DefaultMessage("Delete Service Version")
	String deleteServiceVersion_Breadcrumb();

	@DefaultMessage("Are you sure you want to delete the service version \"{0}\"?")
	String deleteServiceVersionPanel_Confirm(String theId);

	@DefaultMessage("Delete Service Version")
	String deleteServiceVersionPanel_Title();

	@DefaultMessage("Domain")
	String editDomain_Breadcrumb();

	@DefaultMessage("Edit Monitor Rule")
	String EditMonitorRule_Breadcrumb();

	@DefaultMessage("Edit Service")
	String editServicePanel_Breadcrumb();

	@DefaultMessage("Edit Service Version")
	String editServiceVersion_Breadcrumb();

	@DefaultMessage(SVCVER_DESC)
	String editServiceVersion_Description();

	@DefaultMessage("Edit Service Version")
	String editServiceVersion_Title();

	@DefaultMessage("Notes")
	String editUser_ContactNotes();

	@DefaultMessage("Contact Information")
	String editUser_ContactTitle();

	@DefaultMessage("Edit User")
	String editUser_Dashboard();

	@DefaultMessage("Saved User")
	String editUser_DoneSaving();

	@DefaultMessage("A list of allowable IPs may optionally be specified. If specified, only " + "requests originating from one of the allowable IPs will be permitted to access "
			+ "services using this user.")
	String editUser_IpsDesc();

	@DefaultMessage("Service Firewall")
	String editUser_IpsTitle();

	@DefaultMessage("Change Password")
	String editUser_Password();

	@DefaultMessage("Permissions")
	String editUser_PermissionsTitle();

	@DefaultMessage("Recent Transactions")
	String editUser_RecentMessagesTitle();

	@DefaultMessage("Edit User")
	String editUser_Title();

	@DefaultMessage("Username")
	String editUser_Username();

	@DefaultMessage("")
	String editUsersPanel_ColumnActions();

	@DefaultMessage("Authentication Host")
	String editUsersPanel_ColumnAuthHost();

	@DefaultMessage("Last Access")
	String editUsersPanel_ColumnLastServiceAccess();

	@DefaultMessage("Transactions")
	String editUsersPanel_ColumnTransactions();

	@DefaultMessage("Username")
	String editUsersPanel_ColumnUsername();

	@DefaultMessage("Users")
	String editUsersPanel_Dashboard();

	@DefaultMessage("The following table lists all of the defined users. A user account may " + "be defined to have permissions to access specific servives, or to administer "
			+ "them, or even to administer ServiceProxy itself.")
	String editUsersPanel_ListDescription();

	@DefaultMessage("Edit Users")
	String editUsersPanel_Title();

	@DefaultMessage("HTTP Basic Auth")
	String httpBasicAuthServerSecurity_Name();

	@DefaultMessage("Cannot remove the default config")
	String httpClientConfigsPanel_CantDeleteDefault();

	@DefaultMessage("Reset Period")
	String httpClientConfigsPanel_CircuitBreakerDelayBetweenReset();

	@DefaultMessage("A <a href=\"http://en.wikipedia.org/wiki/Circuit_breaker_design_pattern\" target=\"_blank\">Circuit Breaker</a> "
			+ "remembers when a service implementation is down and prevents the proxy from trying to access that service "
			+ "repeatedly. In other words, when an attempt to invoke a service fails for some reason, the proxy will remember that "
			+ "the service has failed and will not attempt to invoke that service again until a given number of milliseconds has "
			+ "elapsed (the reset period). Circuit breakers are particularly useful if there are multiple backing implementations, "
			+ "since the proxy will remember the state for each implementation and will quickly move to a good one when " + "one backing implementation is failing.")
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

	@DefaultMessage("Every service invocation will use an HTTP Client Configuration. These " + "configurations may be shared among multiple service implementations. At "
			+ "a minimum, a configuration named 'DEFAULT' will always exist, but you may " + "also create others for specific purposes.")
	String httpClientConfigsPanel_IntroMessage();

	@DefaultMessage("HTTP Client Config")
	String httpClientConfigsPanel_ListTitle();

	@DefaultMessage("Set the number of retries the proxy will make against a single backing " + "implementation URL before moving on to the next one. For instance, if this is set to "
			+ "2 and you have URLs A and B, the proxy will try A three times before moving to B if A " + "is failing.")
	String httpClientConfigsPanel_RetriesDesc();

	@DefaultMessage("Retries")
	String httpClientConfigsPanel_RetriesLabel();

	@DefaultMessage("Retries")
	String httpClientConfigsPanel_RetriesTitle();

	@DefaultMessage("Connect Timeout (millis):")
	String httpClientConfigsPanel_TcpConnectMillis();

	@DefaultMessage("TCP Properties")
	String httpClientConfigsPanel_TcpProperties();

	@DefaultMessage("Use the following settings to control the outbound connection settings. These should "
			+ "be adjusted to provide sensible defaults so that services which are hung don''t block for too long " + "while still allowing for even the longest legitimate queries.")
	String httpClientConfigsPanel_TcpPropertiesDesc();

	@DefaultMessage("Read Timeout (millis):")
	String httpClientConfigsPanel_TcpReadMillis();

	@DefaultMessage("If the service " + "implementation has more than one URL defined (i.e. there " + "are multiple redundant implementations) the URL Selection Policy defines how the "
			+ "proxy should select which implementation to use.")
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

	@DefaultMessage("JSON-RPC Named Parameter Server Security")
	String jsonRpcNamedParameterServerSecurity_Name();

	@DefaultMessage("Error: An invalid value {0} was found. Numbers must be 0 (meaning no messages will be stored) or positive (meaning this number will be stored before the oldest entries are deleted), and for performance reasons must be below {1}")
	String keepRecentTransactionsPanel_AlertInvalidValue(String theValue, String theMax);

	@DefaultMessage("If enabled, the given number of recent transactions will be kept in the Service Retriever database "
			+ "for troubleshooting, etc. As new transactions are performed, old transactions are removed from the database, "
			+ "so this feature is really only intended for troubleshooting and health checks. Responses are stored in "
			+ "different buckets according to transaction outcome, so it is possible to save (for example) "
			+ "more security failure transactions than successful ones. This also means that if most transactions "
			+ "have one outcome, transactions will still be saved for other outcomes. It is important to note "
			+ "that for performance, transactions to be written to the database are buffered in memory for " + "up to a minute so it is important to not choose a very large number here.")
	String keepRecentTransactionsPanel_Description();

	@DefaultMessage("Fail")
	String keepRecentTransactionsPanel_OutcomeFail();

	@DefaultMessage("Put the number of recent failed transactions to store here. If you do not " + "wish to store transactions with this outcome, put zero or leave this field blank. Failed "
			+ "transactions are transactions where underlying service implementation failed to produce " + "a valid response, or was unavailable.")
	String keepRecentTransactionsPanel_OutcomeFailDesc();

	@DefaultMessage("Fault")
	String keepRecentTransactionsPanel_OutcomeFault();

	@DefaultMessage("Put the number of recent fault transactions to store here. If you do not " + "wish to store transactions with this outcome, put zero or leave this field blank. Fault "
			+ "transactions are transactions where the underlying service implementation produced a result " + "which was valid, but which constituted a non-successful response.")
	String keepRecentTransactionsPanel_OutcomeFaultDesc();

	@DefaultMessage("Security Fail")
	String keepRecentTransactionsPanel_OutcomeSecurityFail();

	@DefaultMessage("Put the number of recent security failure transactions to store here. If you do not "
			+ "wish to store transactions with this outcome, put zero or leave this field blank. Security failure "
			+ "transactions are transactions where Service Retriever could not successfully authenticate "
			+ "the requesting user, so the request was not allowed to proceed to the underlying service.")
	String keepRecentTransactionsPanel_OutcomeSecurityFailDesc();

	@DefaultMessage("Success")
	String keepRecentTransactionsPanel_OutcomeSuccess();

	@DefaultMessage("Put the number of recent successful transactions to store here. If you do not " + "wish to store transactions with this outcome, put zero or leave this field blank. Successful "
			+ "transactions are transactions where the underlying service implementation successfully returned " + "a result.")
	String keepRecentTransactionsPanel_OutcomeSuccessDesc();

	@DefaultMessage("Transaction Log")
	String keepRecentTransactionsPanel_Title();

	@DefaultMessage("The base DN to search for the user being authenticated")
	String ldapAuthenticationHostEditPanel_AuthenticateBaseDnDescription();

	@DefaultMessage("Base DN")
	String ldapAuthenticationHostEditPanel_AuthenticateBaseDnName();

	@DefaultMessage("This is the LDAP filter to search for. This should contain a parameter as \"{0}\" which will then be substited with the username in the service request.")
	String ldapAuthenticationHostEditPanel_AuthenticateFilterDescription(String theParam0);

	@DefaultMessage("Filter")
	String ldapAuthenticationHostEditPanel_AuthenticateFilterName();

	@DefaultMessage("This is the complete DN name of the user to use to bind to the LDAP directory in order to authenticate users")
	String ldapAuthenticationHostEditPanel_BindUserDnDescription();

	@DefaultMessage("Bind User DN")
	String ldapAuthenticationHostEditPanel_BindUserDnName();

	@DefaultMessage("The password to use with the \"Bind User DN\" to bind initially to the LDAP")
	String ldapAuthenticationHostEditPanel_BindUserPasswordDescription();

	@DefaultMessage("Bind User Password")
	String ldapAuthenticationHostEditPanel_BindUserPasswordName();

	@DefaultMessage("An <b>LDAP</b> Authentication host stores users and their " + "passwords in an external LDAP database, such as an Active Directory server. Use this option if "
			+ "you have external LDAP database against which users can be authenticated. Note that for each "
			+ "record in the LDAP database which is used to actually authorize service invocations, a parallel entry "
			+ "is created in the ServiceRetriever database. This entry is used to link to usage statistics and may " + "also be used to store permission rules.")
	String ldapAuthenticationHostEditPanel_description();

	@DefaultMessage("LDAP Properties")
	String ldapAuthenticationHostEditPanel_LdapPropertiesTitle();

	@DefaultMessage("LDAP")
	String ldapAuthenticationHostEditPanel_title();

	@DefaultMessage("This is the URL to use to connect to the LDAP server, in the form: \"ldap://server.com:389\"")
	String ldapAuthenticationHostEditPanel_UrlDescription();

	@DefaultMessage("URL")
	String ldapAuthenticationHostEditPanel_UrlName();

	@DefaultMessage("Authentication Hosts")
	String leftPanel_AuthenticationHosts();

	@DefaultMessage("Configuration")
	String leftPanel_Configuration();

	@DefaultMessage("Edit Users")
	String leftPanel_EditUsers();

	@DefaultMessage("HTTP Clients")
	String leftPanel_HttpClients();

	@DefaultMessage("Manually Test Service")
	String leftPanel_ManuallyTestService();

	@DefaultMessage("Edit Rules")
	String leftPanel_MonitorRules();

	@DefaultMessage("This version")
	SafeHtml libraryMessageAppliesToPanel_TreeAllMethodsCheckbox();

	@DefaultMessage("Any service in this domain")
	SafeHtml libraryMessageAppliesToPanel_TreeAllServicesCheckbox();

	@DefaultMessage("Any version of this service")
	SafeHtml libraryMessageAppliesToPanel_TreeAllServiceVersionsCheckbox();

	@DefaultMessage("A <b>Local Database</b> Authentication host stores users and their " + "passwords in the ServiceRetriever database. Use this option if you do not have "
			+ "an external database or LDAP against which users can be authenticated.")
	String localDatabaseAuthenticationHostEditPanel_description();

	@DefaultMessage("Local Database")
	String localDatabaseAuthenticationHostEditPanel_title();

	@DefaultMessage("Monitor Rules")
	String MonitorRulesList_Breadcrumb();

	@DefaultMessage("Administration Permissions")
	String permissionsPanel_AdministrationPermissions();

	@DefaultMessage("Full access")
	String permissionsPanel_AllDomainsCheckbox();

	@DefaultMessage("When this checkbox is selected, the user has access to all services within all domains")
	String permissionsPanel_AllDomainsDesc();

	@DefaultMessage("This user has the following service permissions. Note that the permission " + "list here applies only to services which are configured to use ServiceProxy host security.")
	String permissionsPanel_ServicePermissionsDesc();

	
	@DefaultMessage("Service Permissions")
	String permissionsPanel_ServicePermissionsTitle();

	@DefaultMessage("Super User")
	String permissionsPanel_SuperUserCheckbox();

	@DefaultMessage("Any user with super user permissions is able to configure any aspects of " + "the ServiceProxy")
	String permissionsPanel_SuperUserDesc();

	@DefaultMessage("Grant access to all {0} methods")
	SafeHtml permissionsPanel_TreeAllMethodsCheckbox(int count);

	

	@DefaultMessage("Grant access to all {0} services")
	SafeHtml permissionsPanel_TreeAllServicesCheckbox(int count);

	@DefaultMessage("Grant access to all {0} versions")
	SafeHtml permissionsPanel_TreeAllServiceVersionsCheckbox(int count);

	
	
	@DefaultMessage("Allow Access")
	SafeHtml permissionsPanel_TreeMethodCheckbox();

	@DefaultMessage("ID")
	String propertyNameId();

	@DefaultMessage("Name")
	String propertyNameName();


	@DefaultMessage("Authorization")
	String recentMessagesGrid_ColAuthorization();

	@DefaultMessage("Implementation URL")
	String recentMessagesGrid_ColImplementationUrl();

	@DefaultMessage("Requesting IP")
	String recentMessagesGrid_ColIp();

	@DefaultMessage("Millis")
	String recentMessagesGrid_ColMillis();

	@DefaultMessage("Service")
	String recentMessagesGrid_ColService();

	@DefaultMessage("Time")
	String recentMessagesGrid_ColTimestamp();

	@DefaultMessage("User")
	String recentMessagesGrid_ColUser();

	/**
	 * Header value for column
	 */
	@DefaultMessage("View")
	String recentMessagesGrid_ColView();

	/**
	 * Actual row value for column
	 */
	@DefaultMessage("View")
	String recentMessagesGrid_View();

	@DefaultMessage("Replay")
	String replayLibraryMessage_Breadcrumb();

	@DefaultMessage("Replay Message")
	String replayMessage_Breadcrumb();

	@DefaultMessage("Applies to this version")
	SafeHtml ruleAppliesToPanel_TreeAllMethodsCheckbox();

	@DefaultMessage("Applies to entire domain ({0} services)")
	SafeHtml ruleAppliesToPanel_TreeAllServicesCheckbox(int count);

	@DefaultMessage("Applies to all versions of service ({0} versions)")
	SafeHtml ruleAppliesToPanel_TreeAllServiceVersionsCheckbox(int count);

	@DefaultMessage("Applies")
	SafeHtml ruleAppliesToPanel_TreeMethodCheckbox();

	@DefaultMessage("Save Message to Library")
	String saveRecentMessageToLibrary_Breadcrumb();

	@DefaultMessage("Service Catalog")
	String serviceCatalog_Breadcrumb();

	@DefaultMessage("Service Catalog")
	String serviceCatalog_Title();

	@DefaultMessage("Dashboard")
	String serviceDashboard_Breadcrumb();

	@DefaultMessage("Recent Messages")
	String serviceVersionRecentMessages_Breadcrumb();

	@DefaultMessage("Service Version Status")
	String serviceVersionStats_Breadcrumb();

	@DefaultMessage("Graphs")
	String serviceVersionStats_GraphsTitle();

	@DefaultMessage("Service Latency")
	String serviceVersionStats_LatencyTitle();

	@DefaultMessage("Message Sizes")
	String serviceVersionStats_MessageSizeTitle();

	@DefaultMessage("This {0} is not configured to retain any recent transactions")
	String serviceVersionStats_NoRecentMsgs(String theUnitType);

	@DefaultMessage("This {0} is configured to retain the last {1} recent {2} transactions")
	String serviceVersionStats_RecentConfigNum(String theUnitType, int theNumber, String theTransactionType);

	@DefaultMessage("Failed (Non-Security) Transactions")
	String serviceVersionStats_RecentFailTitle();

	@DefaultMessage("Fault Transactions")
	String serviceVersionStats_RecentFaultTitle();

	@DefaultMessage("Failed (Security) Transactions")
	String serviceVersionStats_RecentSecurityFailTitle();

	@DefaultMessage("Successful Transactions")
	String serviceVersionStats_RecentSuccessTitle();

	@DefaultMessage("Request Throttling")
	String serviceVersionStats_ThrottlingTitle();

	@DefaultMessage("Service Version Status: {0}")
	String serviceVersionStats_Title(String theServiceVersion);

	@DefaultMessage("Implementation URLs")
	String serviceVersionStats_UrlsTitle();

	@DefaultMessage("Service Usage")
	String serviceVersionStats_UsageTitle();

	@DefaultMessage("Test Service Version")
	String testServiceVersion_Breadcrumb();
	
	@DefaultMessage("<b>Prefer Local</b> means that the proxy will favour any URLs which are on " + "the same host as the service retriever itself, and will only use remote "
			+ "implementations if all local URLs are down")
	String urlSelectionPolicy_Desc_PreferLocal();

	@DefaultMessage("<b>Round Robin</b> means that the proxy will attempt to distribute requests " + "evenly across all URLs")
	String urlSelectionPolicy_Desc_RoundRobin();

	@DefaultMessage("Request Throttling")
	String user_requestThrottling();

	@DefaultMessage("View Transaction")
	String viewRecentMessageServiceVersion_Breadcrumb();

	@DefaultMessage("Request Message")
	String viewRecentMessageServiceVersion_RequestMessage();

	@DefaultMessage("Response Message")
	String viewRecentMessageServiceVersion_ResponseMessage();

	@DefaultMessage("View Service Version Transaction")
	String viewRecentMessageServiceVersion_Title();

	@DefaultMessage("View User Transaction")
	String viewRecentMessageUser_Title();

	@DefaultMessage("Recent Transactions")
	String viewRecentMessagUser_Breadcrumb();

	@DefaultMessage("User Statistics and Usage")
	String viewUserStats_Breadcrumb();

	@DefaultMessage("Authentication Host")
	String wsSecServerSecurity_AuthenticationHost();

	@DefaultMessage("WS-Security")
	String wsSecServerSecurity_Name();

	@DefaultMessage("Using authentication host: {0}")
	String wsSecServerSecurity_UsesAuthenticationHost(String theModuleId);

	@DefaultMessage("Message Library")
	String serviceVersionMessageLibrary_Breadcrumb();

	@DefaultMessage("Message Library")
	String actions_MessageLibrary();

	@DefaultMessage("Recent Transactions")
	String actions_RecentTransactions();

	
	
}
