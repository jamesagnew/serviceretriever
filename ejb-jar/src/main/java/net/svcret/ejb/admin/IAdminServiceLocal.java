package net.svcret.ejb.admin;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import javax.ejb.Local;

import net.svcret.admin.shared.model.BaseDtoAuthenticationHost;
import net.svcret.admin.shared.model.BaseDtoMonitorRule;
import net.svcret.admin.shared.model.BaseDtoServiceVersion;
import net.svcret.admin.shared.model.DtoLibraryMessage;
import net.svcret.admin.shared.model.DtoMonitorRuleActiveCheck;
import net.svcret.admin.shared.model.DtoMonitorRuleActiveCheckOutcome;
import net.svcret.admin.shared.model.DtoServiceVersionSoap11;
import net.svcret.admin.shared.model.DtoStickySessionUrlBinding;
import net.svcret.admin.shared.model.DtoAuthenticationHostList;
import net.svcret.admin.shared.model.DtoConfig;
import net.svcret.admin.shared.model.DtoDomain;
import net.svcret.admin.shared.model.DtoDomainList;
import net.svcret.admin.shared.model.DtoHttpClientConfig;
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
import net.svcret.ejb.ex.UnexpectedFailureException;

@Local
public interface IAdminServiceLocal {

	DtoDomain addDomain(DtoDomain theDomain) throws ProcessingException, UnexpectedFailureException;

	GService addService(long theDomainPid, GService theService) throws ProcessingException, UnexpectedFailureException;

	byte[] createWsdlBundle(long theServiceVersionPid) throws ProcessingException, IOException;

	DtoAuthenticationHostList deleteAuthenticationHost(long thePid) throws ProcessingException;

	void deleteDomain(long thePid) throws ProcessingException, UnexpectedFailureException;

	GHttpClientConfigList deleteHttpClientConfig(long thePid) throws ProcessingException, UnexpectedFailureException;

	DtoDomainList deleteService(long theServicePid) throws ProcessingException, UnexpectedFailureException;

	DtoDomainList deleteServiceVersion(long thePid) throws ProcessingException, UnexpectedFailureException;

	DtoMonitorRuleActiveCheck executeMonitorRuleActiveCheck(DtoMonitorRuleActiveCheck theCheck) throws ProcessingException;

	Collection<DtoStickySessionUrlBinding> getAllStickySessions();

	long getDefaultHttpClientConfigPid();

	DtoDomain getDomainByPid(long theDomain) throws ProcessingException, UnexpectedFailureException;

	long getDomainPid(String theDomainId) throws ProcessingException;

	DtoLibraryMessage getLibraryMessage(long theMessagePid) throws ProcessingException;

	Collection<DtoLibraryMessage> getLibraryMessages(HierarchyEnum theType, long thePid, boolean theLoadContents) throws ProcessingException;

	Collection<DtoLibraryMessage> getLibraryMessagesForService(long thePid, boolean theLoadContents) throws ProcessingException;

	long getServicePid(long theDomainPid, String theServiceId) throws ProcessingException;

	Collection<GMonitorRuleFiring> loadAllActiveRuleFirings() throws ProcessingException;

	BaseDtoAuthenticationHost loadAuthenticationHost(long thePid) throws ProcessingException;

	DtoConfig loadConfig() throws UnexpectedFailureException;

	DtoDomainList loadDomainList() throws ProcessingException, UnexpectedFailureException;

	Collection<DtoLibraryMessage> loadLibraryMessages() throws ProcessingException;

	ModelUpdateResponse loadModelUpdate(ModelUpdateRequest theRequest) throws ProcessingException, UnexpectedFailureException;

	BaseDtoMonitorRule loadMonitorRuleAndDetailedSatistics(long theRulePid);

	List<GMonitorRuleFiring> loadMonitorRuleFirings(Long theDomainPid, Long theServicePid, Long theServiceVersionPid, int theStart);

	GMonitorRuleList loadMonitorRuleList() throws ProcessingException, UnexpectedFailureException;

	GRecentMessage loadRecentMessageForServiceVersion(long thePid) throws UnknownPidException;

	GRecentMessage loadRecentMessageForUser(long thePid) throws UnknownPidException;

	GRecentMessageLists loadRecentTransactionListForServiceVersion(long theServiceVersionPid);

	GRecentMessageLists loadRecentTransactionListForUser(long thePid);

	GSoap11ServiceVersionAndResources loadServiceVersion(long theServiceVersionPid) throws UnexpectedFailureException;

	GServiceVersionDetailedStats loadServiceVersionDetailedStats(long theVersionPid) throws UnexpectedFailureException;

	GSoap11ServiceVersionAndResources loadSoap11ServiceVersionFromWsdl(DtoServiceVersionSoap11 theService, DtoHttpClientConfig theHttpClientConfig, String theWsdlUrl) throws ProcessingException,
			UnexpectedFailureException;

	GUser loadUser(long thePid, boolean theLoadStats) throws ProcessingException, UnexpectedFailureException;

	GPartialUserList loadUsers(PartialUserListRequest theRequest) throws ProcessingException, UnexpectedFailureException;

	GServiceVersionUrl resetCircuitBreaker(long theUrlPid) throws  UnexpectedFailureException;

	DtoAuthenticationHostList saveAuthenticationHost(BaseDtoAuthenticationHost theAuthHost) throws ProcessingException;

	DtoConfig saveConfig(DtoConfig theConfig) throws UnexpectedFailureException;

	DtoDomainList saveDomain(DtoDomain theDomain) throws ProcessingException, UnexpectedFailureException;

	DtoHttpClientConfig saveHttpClientConfig(DtoHttpClientConfig theConfig, byte[] theNewTruststore, String theNewTruststorePass, byte[] theNewKeystore, String theNewKeystorePass)
			throws ProcessingException, UnexpectedFailureException;

	void saveLibraryMessage(DtoLibraryMessage theMessage) throws ProcessingException;

	void saveMonitorRule(BaseDtoMonitorRule theRule) throws UnexpectedFailureException, ProcessingException;

	DtoDomainList saveService(GService theService) throws ProcessingException, UnexpectedFailureException;

	<T extends BaseDtoServiceVersion> T saveServiceVersion(long theDomain, long theService, T theVersion, List<GResource> theResources) throws ProcessingException, UnexpectedFailureException;

	GUser saveUser(GUser theUser) throws  UnexpectedFailureException, ProcessingException;

	String suggestNewVersionNumber(Long theDomainPid, Long theServicePid);

	GServiceVersionSingleFireResponse testServiceVersionWithSingleMessage(String theMessageText, String theContentType, long thePid, String theRequestedByString) throws ProcessingException;

	DtoMonitorRuleActiveCheckOutcome loadMonitorRuleActiveCheckOutcomeDetails(long thePid) throws UnexpectedFailureException;

}
