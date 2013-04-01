package net.svcret.admin.client.rpc;

import net.svcret.admin.shared.ServiceFailureException;
import net.svcret.admin.shared.model.AddServiceVersionResponse;
import net.svcret.admin.shared.model.GDomain;
import net.svcret.admin.shared.model.GHttpClientConfig;
import net.svcret.admin.shared.model.GHttpClientConfigList;
import net.svcret.admin.shared.model.GService;
import net.svcret.admin.shared.model.GSoap11ServiceVersion;
import net.svcret.admin.shared.model.ModelUpdateRequest;
import net.svcret.admin.shared.model.ModelUpdateResponse;
import net.svcret.ejb.ex.ProcessingException;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("modelupdate")
public interface ModelUpdateService extends RemoteService {
  
	ModelUpdateResponse loadModelUpdate(ModelUpdateRequest theRequest) throws ServiceFailureException;
	
	GDomain addDomain(String theId, String theName) throws ServiceFailureException;
	
	GDomain saveDomain(long thePid, String theId, String theName);
	
	GService addService(long theDomainPid, String theId, String theName, boolean theActive) throws ServiceFailureException;
	
	GSoap11ServiceVersion loadWsdl(GSoap11ServiceVersion theService, String theWsdlUrl) throws ServiceFailureException;

	void saveServiceVersionToSession(GSoap11ServiceVersion theServiceVersion);

	GSoap11ServiceVersion createNewSoap11ServiceVersion(Long theUncommittedId);
	
	void reportClientError(String theMessage, Throwable theException);

	AddServiceVersionResponse addServiceVersion(Long theExistingDomainPid, String theCreateDomainId, Long theExistingServicePid, String theCreateServiceId, GSoap11ServiceVersion theVersion) throws ServiceFailureException;

	GHttpClientConfig saveHttpClientConfig(boolean theCreate, GHttpClientConfig theConfig) throws ServiceFailureException;

	GHttpClientConfigList deleteHttpClientConfig(long thePid) throws ServiceFailureException;
}
