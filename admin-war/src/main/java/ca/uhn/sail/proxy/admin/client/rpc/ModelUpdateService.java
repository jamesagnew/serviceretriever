package ca.uhn.sail.proxy.admin.client.rpc;

import ca.uhn.sail.proxy.admin.shared.ServiceFailureException;
import ca.uhn.sail.proxy.admin.shared.model.DomainListUpdateRequest;
import ca.uhn.sail.proxy.admin.shared.model.GDomain;
import ca.uhn.sail.proxy.admin.shared.model.GDomainList;
import ca.uhn.sail.proxy.admin.shared.model.GServiceList;
import ca.uhn.sail.proxy.admin.shared.model.GSoap11ServiceVersion;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("modelupdate")
public interface ModelUpdateService extends RemoteService {
  
	GDomainList loadDomainList(DomainListUpdateRequest theRequest);
	
	GDomain addDomain(String theId, String theName) throws ServiceFailureException;
	
	GDomain saveDomain(long thePid, String theId, String theName);
	
	GServiceList addService(long theDomainPid, String theId, String theName, boolean theActive);
	
	GSoap11ServiceVersion loadWsdl(String theWsdlUrl) throws ServiceFailureException;
	
}
