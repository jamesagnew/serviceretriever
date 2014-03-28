package net.svcret.admin.client.rpc;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import net.svcret.admin.client.rpc.ModelUpdateService.UserAndAuthHost;
import net.svcret.admin.shared.AddServiceVersionResponse;
import net.svcret.admin.shared.model.BaseDtoAuthenticationHost;
import net.svcret.admin.shared.model.BaseDtoMonitorRule;
import net.svcret.admin.shared.model.BaseDtoServiceVersion;
import net.svcret.admin.shared.model.DtoLibraryMessage;
import net.svcret.admin.shared.model.DtoMonitorRuleActiveCheck;
import net.svcret.admin.shared.model.DtoMonitorRuleActiveCheckOutcome;
import net.svcret.admin.shared.model.DtoNodeStatusAndStatisticsList;
import net.svcret.admin.shared.model.DtoServiceVersionSoap11;
import net.svcret.admin.shared.model.DtoAuthenticationHostList;
import net.svcret.admin.shared.model.DtoConfig;
import net.svcret.admin.shared.model.DtoDomain;
import net.svcret.admin.shared.model.DtoDomainList;
import net.svcret.admin.shared.model.DtoHttpClientConfig;
import net.svcret.admin.shared.model.GMonitorRuleFiring;
import net.svcret.admin.shared.model.GMonitorRuleList;
import net.svcret.admin.shared.model.GPartialUserList;
import net.svcret.admin.shared.model.GRecentMessage;
import net.svcret.admin.shared.model.GRecentMessageLists;
import net.svcret.admin.shared.model.GService;
import net.svcret.admin.shared.model.GServiceVersionDetailedStats;
import net.svcret.admin.shared.model.GServiceVersionSingleFireResponse;
import net.svcret.admin.shared.model.GServiceVersionUrl;
import net.svcret.admin.shared.model.GUser;
import net.svcret.admin.shared.model.HierarchyEnum;
import net.svcret.admin.shared.model.ModelUpdateRequest;
import net.svcret.admin.shared.model.ModelUpdateResponse;
import net.svcret.admin.shared.model.PartialUserListRequest;
import net.svcret.admin.shared.model.ServiceProtocolEnum;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface ModelUpdateServiceAsync {

	void addDomain(DtoDomain theDomain, AsyncCallback<DtoDomain> callback);

	void addService(long theDomainPid, GService theService, AsyncCallback<GService> theCallback);

	void addServiceVersion(Long theExistingDomainPid, String theCreateDomainId, Long theExistingServicePid, String theCreateServiceId, BaseDtoServiceVersion theVersion, AsyncCallback<AddServiceVersionResponse> theCallback);

	void cloneServiceVersion(long thePidToClone, AsyncCallback<BaseDtoServiceVersion> theCallback);

	void createNewServiceVersion(ServiceProtocolEnum theProtocol, Long theDomainPid, Long theServicePid, Long theUncommittedId, AsyncCallback<BaseDtoServiceVersion> theCallback);

	void executeMonitorRuleActiveCheck(DtoMonitorRuleActiveCheck theCheck, AsyncCallback<DtoMonitorRuleActiveCheck> theAsyncCallback);

	void getLatestFailingMonitorRuleFiringForRulePids(AsyncCallback<Map<Long, GMonitorRuleFiring>> theIAsyncLoadCallback);

	void loadConfig(AsyncCallback<DtoConfig> theAsyncCallback);

	void loadLibraryMessage(long theMessagePid, AsyncCallback<DtoLibraryMessage> theAsyncCallback);

	void loadLibraryMessages(AsyncCallback<Collection<DtoLibraryMessage>> theAsyncCallback);

	void loadLibraryMessages(HierarchyEnum theType, long thePid, AsyncCallback<Collection<DtoLibraryMessage>> theAsyncCallback);

	void loadModelUpdate(ModelUpdateRequest theRequest, AsyncCallback<ModelUpdateResponse> callback);

	void loadMonitorRule(long theRulePid, AsyncCallback<BaseDtoMonitorRule> theAsyncCallback);

	void loadMonitorRuleFirings(Long theDomainPid, Long theServicePid, Long theServiceVersionPid, int theStart, AsyncCallback<List<GMonitorRuleFiring>> theAsyncCallback);

	void loadMonitorRuleList(AsyncCallback<GMonitorRuleList> theAsyncCallback);

	void loadRecentMessageForServiceVersion(long thePid, AsyncCallback<GRecentMessage> theAsyncCallback);

	void loadRecentMessageForUser(long thePid, AsyncCallback<GRecentMessage> theAsyncCallback);

	void loadRecentTransactionListForServiceVersion(long theServiceVersionPid, AsyncCallback<GRecentMessageLists> theAsyncCallback);

	void loadRecentTransactionListForuser(long thePid, AsyncCallback<GRecentMessageLists> theAsyncCallback);

	void loadServiceVersionDetailedStats(long theVersionPid, AsyncCallback<GServiceVersionDetailedStats> theAsyncCallback);

	void loadServiceVersionIntoSession(long theServiceVersionPid, AsyncCallback<BaseDtoServiceVersion> theAsyncCallback);

	void loadUser(long theUserPid, boolean theLoadStats, AsyncCallback<UserAndAuthHost> callback);

	void loadUsers(PartialUserListRequest theRequest, AsyncCallback<GPartialUserList> callback);

	void loadWsdl(DtoServiceVersionSoap11 theService, DtoHttpClientConfig theClientConfig, String theWsdlUrl, AsyncCallback<DtoServiceVersionSoap11> callback);

	void removeAuthenticationHost(long thePid, AsyncCallback<DtoAuthenticationHostList> theAsyncCallback);

	void removeDomain(long thePid, AsyncCallback<DtoDomainList> theAsyncCallback);

	void removeService(long theDomainPid, long theServicePid, AsyncCallback<DtoDomainList> theAsyncCallback);

	void removeServiceVersion(long thePid, AsyncCallback<DtoDomainList> theAsyncCallback);

	void reportClientError(String theMessage, Throwable theException, AsyncCallback<Void> callback);

	void resetCircuitBreakerForServiceVersionUrl(long theUrlPid, AsyncCallback<GServiceVersionUrl> theAsyncCallback);

	void saveAuthenticationHost(BaseDtoAuthenticationHost theAuthHost, AsyncCallback<DtoAuthenticationHostList> theCallback);

	void saveConfig(DtoConfig theConfig, AsyncCallback<Void> theAsyncCallback);

	void saveDomain(DtoDomain theDomain, AsyncCallback<DtoDomainList> theDomainList);

	void saveLibraryMessage(DtoLibraryMessage theMessage, AsyncCallback<Void> theIAsyncLoadCallback);

	void saveMonitorRule(BaseDtoMonitorRule theRule, AsyncCallback<GMonitorRuleList> theAsyncCallback);

	void saveService(GService theService, AsyncCallback<DtoDomainList> theMySaveButtonHandler);

	void saveServiceVersionToSession(BaseDtoServiceVersion theServiceVersion, AsyncCallback<Void> callback);

	void saveUser(GUser theUser, AsyncCallback<GUser> theAsyncCallback);

	void testServiceVersionWithSingleMessage(String theMessageText, String theContentType, long theServiceVersionPid, AsyncCallback<GServiceVersionSingleFireResponse> theAsyncCallback);

	void loadMonitorRuleActiveCheckOutcomeDetails(long thePid, AsyncCallback<DtoMonitorRuleActiveCheckOutcome> theAsyncCallback);

	void loadNodeListAndStatistics(AsyncCallback<DtoNodeStatusAndStatisticsList> theAsyncCallback);

	void removeUser(long thePid, AsyncCallback<Void> theAsyncCallback);

}
