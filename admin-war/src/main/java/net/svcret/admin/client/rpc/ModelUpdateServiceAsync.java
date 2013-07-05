package net.svcret.admin.client.rpc;

import java.util.List;

import net.svcret.admin.client.rpc.ModelUpdateService.UserAndAuthHost;
import net.svcret.admin.shared.model.AddServiceVersionResponse;
import net.svcret.admin.shared.model.BaseGAuthHost;
import net.svcret.admin.shared.model.BaseGServiceVersion;
import net.svcret.admin.shared.model.GAuthenticationHostList;
import net.svcret.admin.shared.model.GConfig;
import net.svcret.admin.shared.model.GDomain;
import net.svcret.admin.shared.model.GDomainList;
import net.svcret.admin.shared.model.GHttpClientConfig;
import net.svcret.admin.shared.model.GHttpClientConfigList;
import net.svcret.admin.shared.model.GPartialUserList;
import net.svcret.admin.shared.model.GRecentMessage;
import net.svcret.admin.shared.model.GRecentMessageLists;
import net.svcret.admin.shared.model.GService;
import net.svcret.admin.shared.model.GServiceVersionDetailedStats;
import net.svcret.admin.shared.model.GSoap11ServiceVersion;
import net.svcret.admin.shared.model.GUrlStatus;
import net.svcret.admin.shared.model.GUser;
import net.svcret.admin.shared.model.ModelUpdateRequest;
import net.svcret.admin.shared.model.ModelUpdateResponse;
import net.svcret.admin.shared.model.PartialUserListRequest;
import net.svcret.admin.shared.model.ServiceProtocolEnum;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface ModelUpdateServiceAsync {

	void addDomain(GDomain theDomain, AsyncCallback<GDomain> callback);

	void addService(long theDomainPid, String theId, String theName, boolean theActive, AsyncCallback<GService> theCallback);

	void addServiceVersion(Long theExistingDomainPid, String theCreateDomainId, Long theExistingServicePid, String theCreateServiceId, BaseGServiceVersion theVersion, AsyncCallback<AddServiceVersionResponse> theCallback);

	void createNewServiceVersion(ServiceProtocolEnum theProtocol, Long theDomainPid, Long theServicePid, Long theUncommittedId, AsyncCallback<BaseGServiceVersion> theCallback);

	void deleteHttpClientConfig(long thePid, AsyncCallback<GHttpClientConfigList> theCallback);

	void loadModelUpdate(ModelUpdateRequest theRequest, AsyncCallback<ModelUpdateResponse> callback);

	void loadServiceVersionIntoSession(long theServiceVersionPid, AsyncCallback<BaseGServiceVersion> theAsyncCallback);

	void loadUser(long theUserPid, boolean theLoadStats, AsyncCallback<UserAndAuthHost> callback);

	void loadUsers(PartialUserListRequest theRequest, AsyncCallback<GPartialUserList> callback);

	void loadWsdl(GSoap11ServiceVersion theService, String theWsdlUrl, AsyncCallback<GSoap11ServiceVersion> callback);

	void removeAuthenticationHost(long thePid, AsyncCallback<GAuthenticationHostList> theAsyncCallback);

	void removeDomain(long thePid, AsyncCallback<GDomainList> theAsyncCallback);

	void reportClientError(String theMessage, Throwable theException, AsyncCallback<Void> callback);

	void saveAuthenticationHost(BaseGAuthHost theAuthHost, AsyncCallback<GAuthenticationHostList> theCallback);

	void saveDomain(GDomain theDomain, AsyncCallback<GDomain> callback);

	void saveHttpClientConfig(boolean theCreate, GHttpClientConfig theConfig, AsyncCallback<GHttpClientConfig> theAsyncCallback);

	void saveServiceVersionToSession(BaseGServiceVersion theServiceVersion, AsyncCallback<Void> callback);

	void saveUser(GUser theUser, AsyncCallback<Void> theAsyncCallback);

	void removeService(long theDomainPid, long theServicePid, AsyncCallback<GDomainList> theAsyncCallback);

	void saveService(GService theService, AsyncCallback<GDomainList> theMySaveButtonHandler);

	void loadConfig(AsyncCallback<GConfig> theAsyncCallback);

	void saveConfig(GConfig theConfig, AsyncCallback<Void> theAsyncCallback);

	void loadServiceVersionUrlStatuses(long theServiceVersionPid, AsyncCallback<List<GUrlStatus>> theAsyncCallback);

	void loadRecentTransactionListForServiceVersion(long theServiceVersionPid, AsyncCallback<GRecentMessageLists> theAsyncCallback);

	void loadRecentMessageForServiceVersion(long thePid, AsyncCallback<GRecentMessage> theAsyncCallback);

	void loadRecentTransactionListForuser(long thePid, AsyncCallback<GRecentMessageLists> theAsyncCallback);

	void loadRecentMessageForUser(long thePid, AsyncCallback<GRecentMessage> theAsyncCallback);

	void loadServiceVersionDetailedStats(long theVersionPid, AsyncCallback<GServiceVersionDetailedStats> theAsyncCallback);

	void removeServiceVersion(long thePid, AsyncCallback<GDomainList> theAsyncCallback);

}
