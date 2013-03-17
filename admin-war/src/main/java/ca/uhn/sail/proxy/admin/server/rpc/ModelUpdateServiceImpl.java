package ca.uhn.sail.proxy.admin.server.rpc;

import java.util.concurrent.atomic.AtomicLong;

import javax.ejb.EJB;
import javax.servlet.http.HttpSession;

import ca.uhn.sail.proxy.admin.client.rpc.ModelUpdateService;
import ca.uhn.sail.proxy.admin.shared.ServiceFailureException;
import ca.uhn.sail.proxy.admin.shared.model.DomainListUpdateRequest;
import ca.uhn.sail.proxy.admin.shared.model.GDomain;
import ca.uhn.sail.proxy.admin.shared.model.GDomainList;
import ca.uhn.sail.proxy.admin.shared.model.GServiceList;
import ca.uhn.sail.proxy.admin.shared.model.GSoap11ServiceVersion;
import ca.uhn.sail.proxy.admin.shared.model.StatusEnum;
import ca.uhn.sail.proxy.api.IAdminService;
import ca.uhn.sail.proxy.model.entity.PersDomain;
import ca.uhn.sail.proxy.util.Validate;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class ModelUpdateServiceImpl extends RemoteServiceServlet implements ModelUpdateService {

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(ModelUpdateServiceImpl.class);
	private static final AtomicLong ourNextId = new AtomicLong(0L);
	private static final String SESSION_PREFIX_UNCOMITTED_SVC_VER = "UNC_SVC_VER_";

	@EJB
	private IAdminService myAdminSvc;
	
	private ModelUpdateServiceMock myMock;

	@Override
	public GDomain addDomain(String theId, String theName) throws ServiceFailureException {
		if (isMockMode()) {
			return myMock.addDomain(theId, theName);
		}
		
		PersDomain domain = myAdminSvc.addDomain(theId, theName);
		return toUi(domain);
	}

	private GDomain toUi(PersDomain theDomain) {
		GDomain retVal = new GDomain();
		retVal.setPid(theDomain.getPid());
		retVal.setId(theDomain.getDomainId());
		retVal.setName(theDomain.getDomainName());
//		retVal.setStatus(StatusEnum.valueOf(theDomain.get))
		
		retVal.initChildList();
//		retVal.get
		
		return retVal;
	}

	@Override
	public GServiceList addService(long theDomainPid, String theId, String theName, boolean theActive) {
		if (isMockMode()) {
			return myMock.addService(theDomainPid, theId, theName, theActive);
		}
		return null;
	}

	@Override
	public GSoap11ServiceVersion createNewSoap11ServiceVersion(Long theUncommittedId) {
		GSoap11ServiceVersion retVal;
		HttpSession session = getThreadLocalRequest().getSession(true);
		
		if (theUncommittedId != null) {
			String key = SESSION_PREFIX_UNCOMITTED_SVC_VER + theUncommittedId;
			retVal = (GSoap11ServiceVersion) session.getAttribute(key);
			if (retVal != null) {
				ourLog.info("Retrieving SOAP 1.1 Service Version with uncommitted ID[{}]", theUncommittedId);
				return retVal;
			}
		}
		
		retVal = new GSoap11ServiceVersion();
		retVal.initChildList();
		
		long sessionId = theUncommittedId != null ? theUncommittedId : ourNextId.getAndIncrement();
		retVal.setUncommittedSessionId(sessionId);

		String key = SESSION_PREFIX_UNCOMITTED_SVC_VER + sessionId;
		session.setAttribute(key, retVal);

		ourLog.info("Creating SOAP 1.1 Service Version with uncommitted ID[{}]", sessionId);

		return retVal;
	}

	private boolean isMockMode() {
		if ("true".equals(System.getProperty("sail.mock"))) {
			if (myMock == null) {
				myMock = new ModelUpdateServiceMock();
			}
			return true;
		}
		return false;
	}

	@Override
	public GDomainList loadDomainList(DomainListUpdateRequest theRequest) {
		if (isMockMode()) {
			return myMock.loadDomainList(theRequest);
		}
		return null;
	}

	@Override
	public GSoap11ServiceVersion loadWsdl(GSoap11ServiceVersion theService, String theWsdlUrl) throws ServiceFailureException {
		Validate.throwIllegalArgumentExceptionIfNull("Service", theService);
		Validate.throwIllegalArgumentExceptionIfNull("Service#UncommittedSessionId", theService.getUncommittedSessionId());
		Validate.throwIllegalArgumentExceptionIfBlank("Service", theWsdlUrl);
		
		GSoap11ServiceVersion retVal;
		if (isMockMode()) {
			retVal = myMock.loadWsdl(theService, theWsdlUrl);
		} else {
			throw new IllegalStateException();
		}
		
		/*
		 * Merge security
		 */
		retVal.getServerSecurityList().mergeResults(theService.getServerSecurityList());
		retVal.getClientSecurityList().mergeResults(theService.getClientSecurityList());
		
		saveServiceVersionToSession(retVal);
		return retVal;
	}

	@Override
	public GDomain saveDomain(long thePid, String theId, String theName) {
		if (isMockMode()) {
			return myMock.saveDomain(thePid, theId, theName);
		}
		return null;
	}

	@Override
	public void saveServiceVersionToSession(GSoap11ServiceVersion theServiceVersion) {
		Validate.throwIllegalArgumentExceptionIfNull("ServiceVersion", theServiceVersion);
		Validate.throwIllegalArgumentExceptionIfNull("ServiceVersion#UncommittedSessionId", theServiceVersion.getUncommittedSessionId());
		
		ourLog.info("Saving Service Version[{}] to Session", theServiceVersion.getUncommittedSessionId());

		String key = SESSION_PREFIX_UNCOMITTED_SVC_VER + theServiceVersion.getUncommittedSessionId();
		getThreadLocalRequest().getSession(true).setAttribute(key, theServiceVersion);
	}

	@Override
	public void reportClientError(String theMessage, Throwable theException) {
		ourLog.warn("Client error - " + theMessage, theException);
	}

}
