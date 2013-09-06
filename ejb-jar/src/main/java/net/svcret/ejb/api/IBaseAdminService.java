package net.svcret.ejb.api;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import net.svcret.admin.shared.model.BaseGAuthHost;
import net.svcret.admin.shared.model.BaseGMonitorRule;
import net.svcret.admin.shared.model.BaseGServiceVersion;
import net.svcret.admin.shared.model.DtoLibraryMessage;
import net.svcret.admin.shared.model.DtoServiceVersionSoap11;
import net.svcret.admin.shared.model.GAuthenticationHostList;
import net.svcret.admin.shared.model.GConfig;
import net.svcret.admin.shared.model.GDomain;
import net.svcret.admin.shared.model.GDomainList;
import net.svcret.admin.shared.model.GHttpClientConfig;
import net.svcret.admin.shared.model.GHttpClientConfigList;
import net.svcret.admin.shared.model.GMonitorRuleFiring;
import net.svcret.admin.shared.model.GMonitorRuleList;
import net.svcret.admin.shared.model.GPartialUserList;
import net.svcret.admin.shared.model.GRecentMessage;
import net.svcret.admin.shared.model.GRecentMessageLists;
import net.svcret.admin.shared.model.GResource;
import net.svcret.admin.shared.model.GService;
import net.svcret.admin.shared.model.GServiceVersionDetailedStats;
import net.svcret.admin.shared.model.GServiceVersionSingleFireResponse;
import net.svcret.admin.shared.model.GServiceVersionUrl;
import net.svcret.admin.shared.model.GSoap11ServiceVersionAndResources;
import net.svcret.admin.shared.model.GUser;
import net.svcret.admin.shared.model.HierarchyEnum;
import net.svcret.admin.shared.model.ModelUpdateRequest;
import net.svcret.admin.shared.model.ModelUpdateResponse;
import net.svcret.admin.shared.model.PartialUserListRequest;
import net.svcret.ejb.ex.ProcessingException;

public interface IBaseAdminService {

	GDomain addDomain(GDomain theDomain) throws ProcessingException;

	GService addService(long theDomainPid, String theId, String theName, boolean theActive) throws ProcessingException;

	byte[] createWsdlBundle(long theServiceVersionPid) throws ProcessingException, IOException;

	GAuthenticationHostList deleteAuthenticationHost(long thePid) throws ProcessingException;

	void deleteDomain(long thePid) throws ProcessingException;

	GHttpClientConfigList deleteHttpClientConfig(long thePid) throws ProcessingException;

	GDomainList deleteService(long theServicePid) throws ProcessingException;

	GDomainList deleteServiceVersion(long thePid) throws ProcessingException;

	long getDefaultHttpClientConfigPid();

	GDomain getDomainByPid(long theDomain) throws ProcessingException;

	long getDomainPid(String theDomainId) throws ProcessingException;

	DtoLibraryMessage getLibraryMessage(long theMessagePid) throws ProcessingException;

	Collection<DtoLibraryMessage> getLibraryMessages(HierarchyEnum theType, long thePid, boolean theLoadContents) throws ProcessingException;

	Collection<DtoLibraryMessage> getLibraryMessagesForService(long thePid, boolean theLoadContents) throws ProcessingException;

	GService getServiceByPid(long theService) throws ProcessingException;

	long getServicePid(long theDomainPid, String theServiceId) throws ProcessingException;

	Collection<GMonitorRuleFiring> loadAllActiveRuleFirings() throws ProcessingException;

	BaseGAuthHost loadAuthenticationHost(long thePid) throws ProcessingException;

	GConfig loadConfig() throws ProcessingException;

	GDomainList loadDomainList() throws ProcessingException;

	Collection<DtoLibraryMessage> loadLibraryMessages() throws ProcessingException;

	ModelUpdateResponse loadModelUpdate(ModelUpdateRequest theRequest) throws ProcessingException;

	List<GMonitorRuleFiring> loadMonitorRuleFirings(Long theDomainPid, Long theServicePid, Long theServiceVersionPid, int theStart);

	GMonitorRuleList loadMonitorRuleList() throws ProcessingException;

	GRecentMessage loadRecentMessageForServiceVersion(long thePid) throws ProcessingException;

	GRecentMessage loadRecentMessageForUser(long thePid) throws ProcessingException;

	GRecentMessageLists loadRecentTransactionListForServiceVersion(long theServiceVersionPid);

	GRecentMessageLists loadRecentTransactionListForUser(long thePid);

	GSoap11ServiceVersionAndResources loadServiceVersion(long theServiceVersionPid) throws ProcessingException;

	GServiceVersionDetailedStats loadServiceVersionDetailedStats(long theVersionPid) throws ProcessingException;

	GSoap11ServiceVersionAndResources loadSoap11ServiceVersionFromWsdl(DtoServiceVersionSoap11 theService, String theWsdlUrl) throws ProcessingException;

	GUser loadUser(long thePid, boolean theLoadStats) throws ProcessingException;

	GPartialUserList loadUsers(PartialUserListRequest theRequest) throws ProcessingException;

	GServiceVersionUrl resetCircuitBreaker(long theUrlPid) throws ProcessingException;

	GAuthenticationHostList saveAuthenticationHost(BaseGAuthHost theAuthHost) throws ProcessingException;

	GConfig saveConfig(GConfig theConfig) throws ProcessingException;

	GDomainList saveDomain(GDomain theDomain) throws ProcessingException;

	GHttpClientConfig saveHttpClientConfig(GHttpClientConfig theConfig, byte[] theNewTruststore, String theNewTruststorePass, byte[] theNewKeystore, String theNewKeystorePass)
			throws ProcessingException;

	void saveLibraryMessage(DtoLibraryMessage theMessage) throws ProcessingException;

	void saveMonitorRule(BaseGMonitorRule theRule) throws ProcessingException;

	GDomainList saveService(GService theService) throws ProcessingException;

	<T extends BaseGServiceVersion> T saveServiceVersion(long theDomain, long theService, T theVersion, List<GResource> theResources) throws ProcessingException;

	GUser saveUser(GUser theUser) throws ProcessingException;

	String suggestNewVersionNumber(Long theDomainPid, Long theServicePid);

	GServiceVersionSingleFireResponse testServiceVersionWithSingleMessage(String theMessageText, String theContentType, long thePid, String theRequestedByString) throws ProcessingException;

}
