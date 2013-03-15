package ca.uhn.sail.proxy.admin.server.rpc;

import ca.uhn.sail.proxy.admin.client.rpc.ModelUpdateService;
import ca.uhn.sail.proxy.admin.shared.ServiceFailureException;
import ca.uhn.sail.proxy.admin.shared.model.DomainListUpdateRequest;
import ca.uhn.sail.proxy.admin.shared.model.GDomain;
import ca.uhn.sail.proxy.admin.shared.model.GDomainList;
import ca.uhn.sail.proxy.admin.shared.model.GServiceList;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class ModelUpdateServiceImpl extends RemoteServiceServlet implements ModelUpdateService {

	private ModelUpdateService myMock;

	private boolean isMockMode() {
		if ("true".equals(System.getProperty("sail.mock"))) {
			if (myMock == null) {
			myMock = new ModelUpdateServiceMock();
			}
			return true;
		}
		return false;
	}
	

	public GDomainList loadDomainList(DomainListUpdateRequest theRequest) {
		if (isMockMode()) {
			return myMock.loadDomainList(theRequest);
		}
		return null;
	}


	public GDomain addDomain(String theId, String theName) throws ServiceFailureException {
		if (isMockMode()) {
			return myMock.addDomain(theId, theName);
		}
		return null;
	}


	public GDomain saveDomain(long thePid, String theId, String theName) {
		if (isMockMode()) {
			return myMock.saveDomain(thePid, theId, theName);
		}
		return null;
	}


	public GServiceList addService(long theDomainPid, String theId, String theName, boolean theActive) {
		if (isMockMode()) {
			return myMock.addService(theDomainPid, theId, theName, theActive);
		}
		return null;
	}

	
}
