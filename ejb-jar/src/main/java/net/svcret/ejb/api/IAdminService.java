package net.svcret.ejb.api;

import java.util.List;

import javax.ejb.Local;

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
import net.svcret.admin.shared.model.GResource;
import net.svcret.admin.shared.model.GService;
import net.svcret.admin.shared.model.GSoap11ServiceVersion;
import net.svcret.admin.shared.model.GSoap11ServiceVersionAndResources;
import net.svcret.admin.shared.model.GUrlStatus;
import net.svcret.admin.shared.model.GUser;
import net.svcret.admin.shared.model.ModelUpdateRequest;
import net.svcret.admin.shared.model.ModelUpdateResponse;
import net.svcret.admin.shared.model.PartialUserListRequest;
import net.svcret.ejb.ex.ProcessingException;

@Local
public interface IAdminService {

	GDomain addDomain(GDomain theDomain) throws ProcessingException;

	GService addService(long theDomainPid, String theId, String theName, boolean theActive) throws ProcessingException;

	GAuthenticationHostList deleteAuthenticationHost(long thePid) throws ProcessingException;

	void deleteDomain(long thePid) throws ProcessingException;

	GHttpClientConfigList deleteHttpClientConfig(long thePid) throws ProcessingException;

	GDomainList deleteService(long theServicePid) throws ProcessingException;

	long getDefaultHttpClientConfigPid();

	GDomain getDomainByPid(long theDomain) throws ProcessingException;

	long getDomainPid(String theDomainId) throws ProcessingException;

	GService getServiceByPid(long theService) throws ProcessingException;

	long getServicePid(long theDomainPid, String theServiceId) throws ProcessingException;

	BaseGAuthHost loadAuthenticationHost(long thePid) throws ProcessingException;

	GConfig loadConfig() throws ProcessingException;

	GDomainList loadDomainList() throws ProcessingException;

	ModelUpdateResponse loadModelUpdate(ModelUpdateRequest theRequest) throws ProcessingException;

	GSoap11ServiceVersionAndResources loadServiceVersion(long theServiceVersionPid) throws ProcessingException;

	List<GUrlStatus> loadServiceVersionUrlStatuses(long theServiceVersionPid);

	GSoap11ServiceVersionAndResources loadSoap11ServiceVersionFromWsdl(GSoap11ServiceVersion theService, String theWsdlUrl) throws ProcessingException;

	GPartialUserList loadUsers(PartialUserListRequest theRequest) throws ProcessingException;

	GAuthenticationHostList saveAuthenticationHost(BaseGAuthHost theAuthHost) throws ProcessingException;

	GConfig saveConfig(GConfig theConfig) throws ProcessingException;

	GDomain saveDomain(GDomain theDomain) throws ProcessingException;

	GHttpClientConfig saveHttpClientConfig(GHttpClientConfig theConfig) throws ProcessingException;

	GDomainList saveService(GService theService) throws ProcessingException;

	<T extends BaseGServiceVersion> T saveServiceVersion(long theDomain, long theService, T theVersion, List<GResource> theResources) throws ProcessingException;

	GUser saveUser(GUser theUser) throws ProcessingException;

	String suggestNewVersionNumber(Long theDomainPid, Long theServicePid);

	GRecentMessageLists loadRecentTransactionListForServiceVersion(long theServiceVersionPid);

	GRecentMessage loadRecentMessageForServiceVersion(long thePid);

	GRecentMessageLists loadRecentTransactionListForUser(long thePid);

	GRecentMessage loadRecentMessageForUser(long thePid);

	GUser loadUser(long thePid, boolean theLoadStats) throws ProcessingException;

}