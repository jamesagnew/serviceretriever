package net.svcret.admin.client.nav;
import static net.svcret.admin.client.AdminPortal.MSGS;

public enum PagesEnum {

	/** Add domain step 2 */
	AD2(MSGS.addDomainStep2_Breadcrumb()),

	/** Add domain */
	ADD(MSGS.addDomain_Breadcrumb()),

	/**
	 * Add User
	 */
	ADU(MSGS.addUser_Breadcrumb()),
	
	/** Authentication Host List */
	AHL(MSGS.authenticationHostsPanel_Breadcrumb()),

	AMR(MSGS.AddMonitorRule_Breadcrumb()),

	/** Add service */
	ASE(MSGS.addService_Breadcrumb()),

	/** Add service version */
	ASV(MSGS.addServiceVersion_Breadcrumb()),

	/** Add service version step 2 */
	AV2(MSGS.addServiceVersionStep2_Breadcrumb()),

	/** Config */
	CFG(MSGS.config_Breadcrumb()),

	/** Create library message */
	CLM(MSGS.createLibraryMessage_Breadcrumb()),
	
	/** Delete domain */
	DDO(MSGS.deleteDomainPanel_Breadcrumb()),

	/** Delete Service*/
	DSE(MSGS.deleteServicePanel_Breadcrumb()),

	/** Service dashboard */
	DSH(MSGS.serviceDashboard_Breadcrumb()),

	/**
	 * Delete service version
	 */
	DSV(MSGS.deleteServiceVersion_Breadcrumb()),

	/** Edit domain */
	EDO(MSGS.name_Domain()),
	
	/** Edit user */
	EDU(MSGS.editUser_Dashboard()),

	/** Edit library message */
	ELM(MSGS.editLibraryMessage_Breadcrumb()), 
	
	EMR(MSGS.EditMonitorRule_Breadcrumb()), 
	
	/** Edit Service*/
	ESE(MSGS.editServicePanel_Breadcrumb()), 
	
	/** Edit service version */
	ESV(MSGS.editServiceVersion_Breadcrumb()),

	/** Edit User List */
	EUL(MSGS.editUsersPanel_Dashboard()), 
	
	/** Edit HTTP client configs */
	HCC(MSGS.httpClientConfigsPanel_Dashboard()), 
	
	/**
	 * Service version message library
	 */
	MLB(MSGS.serviceVersionMessageLibrary_Breadcrumb()), 
	
	/** Add domain step 2 */
	MRL(MSGS.MonitorRulesList_Breadcrumb()), 
	
	/**
	 * Replay library message
	 */
	RLM(MSGS.replayLibraryMessage_Breadcrumb()), 
	
	/**
	 * Replay SvcVer Message
	 */
	RPM(MSGS.replayMessage_Breadcrumb()), 

	/**
	 * Replay User Message
	 */
	RPU(MSGS.replayUserMessage_Breadcrumb()), 

	/**
	 * View recent message for Service Version
	 */
	RSV(MSGS.viewRecentMessageServiceVersion_Breadcrumb()),
	
	/**
	 * View recent message for User
	 */
	RUS(MSGS.viewRecentMessagUser_Breadcrumb()), 
	
	/** Service Catalog */
	SEC(MSGS.serviceCatalog_Breadcrumb()), 
	
	/** Save recent message to library */
	SML(MSGS.saveRecentMessageToLibrary_Breadcrumb()), 
	
	/**
	 * Service version recent messages
	 */
	SRM(MSGS.serviceVersionRecentMessages_Breadcrumb()), 
	
	/** Service Version Status */
	SVS(MSGS.serviceVersionStats_Breadcrumb()), 
	
	/**
	 * Test service version
	 */
	TSV(MSGS.testServiceVersion_Breadcrumb()),
	
	/**
	 * View user recent messages
	 */
	URM(MSGS.userRecentTransactions_Breadcrumb()), 
	
	/**
	 * User stats
	 */
	UST(MSGS.userStats_Breadcrumb())
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
