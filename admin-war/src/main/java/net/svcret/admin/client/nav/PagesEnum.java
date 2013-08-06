package net.svcret.admin.client.nav;
import static net.svcret.admin.client.AdminPortal.MSGS;

public enum PagesEnum {

	/** Save recent message to library */
	SML(MSGS.saveRecentMessageToLibrary_Breadcrumb()),

	/** Add domain step 2 */
	MRL(MSGS.MonitorRulesList_Breadcrumb()),

	AMR(MSGS.AddMonitorRule_Breadcrumb()),
	
	EMR(MSGS.EditMonitorRule_Breadcrumb()),

	/** Add domain step 2 */
	AD2(MSGS.addDomainStep2_Breadcrumb()),

	/** Add domain */
	ADD(MSGS.addDomain_Breadcrumb()),

	/** Authentication Host List */
	AHL(MSGS.authenticationHostsPanel_Breadcrumb()),

	/** Add service */
	ASE(MSGS.addService_Breadcrumb()),

	/** Add service version */
	ASV(MSGS.addServiceVersion_Breadcrumb()),

	/** Add service version step 2 */
	AV2(MSGS.addServiceVersionStep2_Breadcrumb()),

	/** Service dashboard */
	DSH(MSGS.serviceDashboard_Breadcrumb()),

	/** Edit domain */
	EDO(MSGS.editDomain_Breadcrumb()),

	/** Edit user */
	EDU(MSGS.editUser_Dashboard()),
	
	/** Edit User List */
	EUL(MSGS.editUsersPanel_Dashboard()),

	/** Edit HTTP client configs */
	HCC(MSGS.httpClientConfigsPanel_Dashboard()), 
	
	/** Delete domain */
	DDO(MSGS.deleteDomainPanel_Breadcrumb()), 
	
	/** Edit service version */
	ESV(MSGS.editServiceVersion_Breadcrumb()), 
	
	/** Delete Service*/
	DSE(MSGS.deleteServicePanel_Breadcrumb()),

	/** Edit Service*/
	ESE(MSGS.editServicePanel_Breadcrumb()), 
	
	/** Service Catalog */
	SEC(MSGS.serviceCatalog_Breadcrumb()), 
	
	/** Config */
	CFG(MSGS.config_Breadcrumb()), 
	
	/** Service Version Status */
	SVS(MSGS.serviceVersionStats_Breadcrumb()), 
	
	/**
	 * View recent message for Service Version
	 */
	RSV(MSGS.viewRecentMessageServiceVersion_Breadcrumb()), 
	
	/**
	 * View recent message for User
	 */
	RUS(MSGS.viewRecentMessagUser_Breadcrumb()), 
	
	/**
	 * Test service version
	 */
	TSV(MSGS.testServiceVersion_Breadcrumb()),
	
	/**
	 * View usee stats
	 */
	VUS(MSGS.viewUserStats_Breadcrumb()), 
	
	/**
	 * Add User
	 */
	ADU(MSGS.addUser_Breadcrumb()), 
	
	/**
	 * Delete service version
	 */
	DSV(MSGS.deleteServiceVersion_Breadcrumb()), 
	
	/**
	 * Service version recent messages
	 */
	SRM(MSGS.serviceVersionRecentMessages_Breadcrumb()), 
	
	/**
	 * Replay Message
	 */
	RPM(MSGS.replayMessage_Breadcrumb()), 
	
	/**
	 * Replay library message
	 */
	RLM(MSGS.replayLibraryMessage_Breadcrumb()),
	
	/**
	 * Service version message library
	 */
	SVL(MSGS.serviceVersionMessageLibrary_Breadcrumb())
	;
	
	private String myBreadcrumb;

	private PagesEnum(String theBreadcrumb) {
		myBreadcrumb = theBreadcrumb;
	}

	/**
	 * @return the breadcrumb
	 */
	public String getBreadcrumb() {
		return myBreadcrumb;
	}
	
}
