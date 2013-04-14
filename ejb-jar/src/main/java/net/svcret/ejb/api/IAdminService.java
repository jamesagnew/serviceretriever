package net.svcret.ejb.api;

import java.util.List;

import javax.ejb.Local;

import net.svcret.admin.shared.model.BaseGAuthHost;
import net.svcret.admin.shared.model.GAuthenticationHostList;
import net.svcret.admin.shared.model.GDomain;
import net.svcret.admin.shared.model.GDomainList;
import net.svcret.admin.shared.model.GHttpClientConfig;
import net.svcret.admin.shared.model.GHttpClientConfigList;
import net.svcret.admin.shared.model.GLocalDatabaseAuthHost;
import net.svcret.admin.shared.model.GPartialUserList;
import net.svcret.admin.shared.model.GResource;
import net.svcret.admin.shared.model.GService;
import net.svcret.admin.shared.model.GSoap11ServiceVersion;
import net.svcret.admin.shared.model.GSoap11ServiceVersionAndResources;
import net.svcret.admin.shared.model.GUser;
import net.svcret.admin.shared.model.ModelUpdateRequest;
import net.svcret.admin.shared.model.ModelUpdateResponse;
import net.svcret.admin.shared.model.PartialUserListRequest;
import net.svcret.ejb.ex.ProcessingException;

@Local
public interface IAdminService {

	GDomain addDomain(String theId, String theName) throws ProcessingException;

	GService addService(long theDomainPid, String theId, String theName, boolean theActive) throws ProcessingException;

	GSoap11ServiceVersion addServiceVersion(long theDomain, long theService, GSoap11ServiceVersion theVersion, List<GResource> theResources) throws ProcessingException;

	long getDomainPid(String theDomainId) throws ProcessingException;

	long getServicePid(long theDomainPid, String theServiceId) throws ProcessingException;

	ModelUpdateResponse loadModelUpdate(ModelUpdateRequest theRequest) throws ProcessingException;

	GSoap11ServiceVersionAndResources loadSoap11ServiceVersionFromWsdl(GSoap11ServiceVersion theService, String theWsdlUrl) throws ProcessingException;

	GDomain getDomainByPid(long theDomain);

	GService getServiceByPid(long theService);

	GHttpClientConfig saveHttpClientConfig(GHttpClientConfig theConfig) throws ProcessingException;

	GHttpClientConfigList deleteHttpClientConfig(long thePid) throws ProcessingException;

	GAuthenticationHostList saveAuthenticationHost(GLocalDatabaseAuthHost theAuthHost) throws ProcessingException;

	GAuthenticationHostList deleteAuthenticationHost(long thePid) throws ProcessingException;

	GPartialUserList loadUsers(PartialUserListRequest theRequest);

	GUser loadUser(long thePid) throws ProcessingException;

	BaseGAuthHost loadAuthenticationHost(long thePid) throws ProcessingException;

	GUser saveUser(GUser theUser);

	long getDefaultHttpClientConfigPid();

	String suggestNewVersionNumber(Long theDomainPid, Long theServicePid);

	void deleteDomain(long thePid);

	GDomainList loadDomainList() throws ProcessingException;

}
