package net.svcret.admin.client.nav;
import static net.svcret.admin.client.AdminPortal.MSGS;

public enum PagesEnum {

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
	RUS(MSGS.viewRecentMessagUser_Breadcrumb());
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
