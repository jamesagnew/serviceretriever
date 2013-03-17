package ca.uhn.sail.proxy.admin.client.rpc;

import ca.uhn.sail.proxy.admin.shared.model.DomainListUpdateRequest;
import ca.uhn.sail.proxy.admin.shared.model.GDomain;
import ca.uhn.sail.proxy.admin.shared.model.GDomainList;
import ca.uhn.sail.proxy.admin.shared.model.GServiceList;
import ca.uhn.sail.proxy.admin.shared.model.GSoap11ServiceVersion;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface ModelUpdateServiceAsync {

	void loadDomainList(DomainListUpdateRequest theRequest, AsyncCallback<GDomainList> callback);

	void addDomain(String theId, String theName, AsyncCallback<GDomain> callback);

	void saveDomain(long thePid, String theId, String theName, AsyncCallback<GDomain> callback);

	void addService(long theDomainPid, String theId, String theName, boolean theActive, AsyncCallback<GServiceList> theCallback);

	void loadWsdl(GSoap11ServiceVersion theService, String theWsdlUrl, AsyncCallback<GSoap11ServiceVersion> callback);

	void saveServiceVersionToSession(GSoap11ServiceVersion theServiceVersion, AsyncCallback<Void> theCallback);

	void createNewSoap11ServiceVersion(Long theUncommittedId, AsyncCallback<GSoap11ServiceVersion> theCallback);

	void reportClientError(String theMessage, Throwable theException, AsyncCallback<Void> callback);

}
