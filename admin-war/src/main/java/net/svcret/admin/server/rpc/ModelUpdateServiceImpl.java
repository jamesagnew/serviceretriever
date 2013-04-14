package net.svcret.admin.server.rpc;

import static org.apache.commons.lang.StringUtils.*;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import javax.ejb.EJB;
import javax.servlet.http.HttpSession;

import net.svcret.admin.client.rpc.ModelUpdateService;
import net.svcret.admin.shared.ServiceFailureException;
import net.svcret.admin.shared.model.AddServiceVersionResponse;
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
import net.svcret.ejb.api.IAdminService;
import net.svcret.ejb.ex.ProcessingException;
import net.svcret.ejb.util.Validate;

import org.apache.commons.lang.StringUtils;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class ModelUpdateServiceImpl extends RemoteServiceServlet implements ModelUpdateService {

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(ModelUpdateServiceImpl.class);
	private static final AtomicLong ourNextId = new AtomicLong(0L);
	private static final String SESSION_PREFIX_UNCOMITTED_SVC_VER = "UNC_SVC_VER_";
	private static final String SESSION_PREFIX_UNCOMITTED_SVC_VER_RES = "UNC_SVC_VER_RES_";

	@EJB
	private IAdminService myAdminSvc;

	private ModelUpdateServiceMock myMock;

	@Override
	public GDomain addDomain(String theId, String theName) throws ServiceFailureException {
		ourLog.info("Adding domain {}/{}", theId, theName);

		if (isMockMode()) {
			return myMock.addDomain(theId, theName);
		}

		try {
			return myAdminSvc.addDomain(theId, theName);
		} catch (ProcessingException e) {
			ourLog.warn("Failed to add domain", e);
			throw new ServiceFailureException("Failed to add domain: " + e.getMessage());
		}
	}

	@Override
	public GService addService(long theDomainPid, String theId, String theName, boolean theActive) throws ServiceFailureException {
		ourLog.info("Adding new service to domain {} with ID[{}] and name: {}", new Object[] { theDomainPid, theId, theName });

		if (isMockMode()) {
			return myMock.addService(theDomainPid, theId, theName, theActive);
		}

		try {
			return myAdminSvc.addService(theDomainPid, theId, theName, theActive);
		} catch (ProcessingException e) {
			ourLog.warn("Failed to add domain", e);
			throw new ServiceFailureException("Failed to add domain: " + e.getMessage());
		}
	}

	@Override
	public AddServiceVersionResponse addServiceVersion(Long theExistingDomainPid, String theCreateDomainId, Long theExistingServicePid, String theCreateServiceId, GSoap11ServiceVersion theVersion) throws ServiceFailureException {
		if (theExistingDomainPid == null && StringUtils.isBlank(theCreateDomainId)) {
			throw new IllegalArgumentException("Domain PID and new domain ID are both missing");
		}
		if (theExistingDomainPid != null && StringUtils.isNotBlank(theCreateDomainId)) {
			throw new IllegalArgumentException("Domain PID and new domain ID can not both be provided");
		}
		if (theExistingServicePid == null && StringUtils.isBlank(theCreateServiceId)) {
			throw new IllegalArgumentException("Service PID and new domain ID are both missing");
		}
		if (theExistingServicePid != null && StringUtils.isNotBlank(theCreateServiceId)) {
			throw new IllegalArgumentException("Service PID and new domain ID can not both be provided");
		}
		Validate.throwIllegalArgumentExceptionIfBlank("Service Version ID", theVersion.getId());
		if (theVersion.getPid() != 0) {
			throw new IllegalArgumentException("ServiceVersion already has a PID");
		}

		ourLog.info("Adding service version for Domain[{}/create {}] and Service[{}/create {}] with id: {}", new Object[] { theExistingDomainPid, theCreateDomainId, theExistingServicePid, theCreateServiceId, theVersion.getId() });

		if (isMockMode()) {
			return myMock.addServiceVersion(theExistingDomainPid, theCreateDomainId, theExistingServicePid, theCreateServiceId, theVersion);
		}

		Long uncommittedSessionId = theVersion.getUncommittedSessionId();
		List<GResource> resList = getServiceVersionResourcesFromSession(uncommittedSessionId);
		if (resList == null) {
			ourLog.info("Unable to find a resource collection in the session with ID " + uncommittedSessionId);
			throw new ServiceFailureException("An internal failure occurred, please reload the WSDL for this service and try again.");
		}

		long domain;
		if (isNotBlank(theCreateDomainId)) {
			try {
				domain = myAdminSvc.addDomain(theCreateDomainId, theCreateDomainId).getPid();
			} catch (ProcessingException e) {
				ourLog.error("Failed to create domain " + theCreateDomainId, e);
				throw new ServiceFailureException("Failed to create domain: " + theCreateDomainId + " - " + e.getMessage());
			}
		} else {
			domain = theExistingDomainPid;
		}

		long service;
		if (isNotBlank(theCreateServiceId)) {
			try {
				service = myAdminSvc.addService(domain, theCreateServiceId, theCreateServiceId, true).getPid();
			} catch (ProcessingException e) {
				ourLog.error("Failed to create service " + theCreateServiceId, e);
				throw new ServiceFailureException("Failed to create service: " + theCreateDomainId + " - " + e.getMessage());
			}
		} else {
			service = theExistingServicePid;
		}

		GSoap11ServiceVersion newVersion;
		try {
			newVersion = myAdminSvc.addServiceVersion(domain, service, theVersion, resList);
		} catch (ProcessingException e) {
			ourLog.error("Failed to add service version", e);
			throw new ServiceFailureException(e.getMessage());
		}

		AddServiceVersionResponse retVal = new AddServiceVersionResponse();

		retVal.setNewServiceVersion(newVersion);
		retVal.setNewDomain(myAdminSvc.getDomainByPid(domain));
		retVal.setNewService(myAdminSvc.getServiceByPid(service));

		return retVal;
	}

	@Override
	public GSoap11ServiceVersion createNewSoap11ServiceVersion(Long theDomainPid, Long theServicePid, Long theUncommittedId) {
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

		if (isMockMode()) {
			retVal.setHttpClientConfigPid(myMock.getDefaultHttpClientConfigPid());
			retVal.setId("1.0");
		} else {
			retVal.setHttpClientConfigPid(myAdminSvc.getDefaultHttpClientConfigPid());
			if (theDomainPid != null && theServicePid != null) {
				retVal.setId(myAdminSvc.suggestNewVersionNumber(theDomainPid, theServicePid));
			} else {
				retVal.setId("1.0");
			}
		}

		long sessionId = theUncommittedId != null ? theUncommittedId : ourNextId.getAndIncrement();
		retVal.setUncommittedSessionId(sessionId);

		String key = SESSION_PREFIX_UNCOMITTED_SVC_VER + sessionId;
		session.setAttribute(key, retVal);

		ourLog.info("Creating SOAP 1.1 Service Version with uncommitted ID[{}]", sessionId);

		return retVal;
	}

	@Override
	public GHttpClientConfigList deleteHttpClientConfig(long thePid) throws ServiceFailureException {
		if (thePid <= 0) {
			throw new IllegalArgumentException("Invalid PID: " + thePid);
		}

		if (isMockMode()) {
			return myMock.deleteHttpClientConfig(thePid);
		}

		try {
			return myAdminSvc.deleteHttpClientConfig(thePid);
		} catch (ProcessingException e) {
			ourLog.error("Failed to save config", e);
			throw new ServiceFailureException(e.getMessage());
		}
	}

	@SuppressWarnings("unchecked")
	private List<GResource> getServiceVersionResourcesFromSession(long theServiceVersionUncommittedSessionId) {
		String key = SESSION_PREFIX_UNCOMITTED_SVC_VER_RES + theServiceVersionUncommittedSessionId;
		return (List<GResource>) getThreadLocalRequest().getSession(true).getAttribute(key);
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
	public ModelUpdateResponse loadModelUpdate(ModelUpdateRequest theRequest) throws ServiceFailureException {
		Validate.throwIllegalArgumentExceptionIfNull("ModelUpdateRequest", theRequest);

		ourLog.info("Requesting a model update from backend server for: " + theRequest.toString());

		long start = System.currentTimeMillis();

		ModelUpdateResponse retVal;
		try {
			if (isMockMode()) {
				retVal = myMock.loadModelUpdate(theRequest);
			} else {
				retVal = myAdminSvc.loadModelUpdate(theRequest);
			}
		} catch (ProcessingException e) {
			ourLog.error("Failed to load model update", e);
			throw new ServiceFailureException(e.getMessage());
		}

		long delay = System.currentTimeMillis() - start;
		ourLog.info("Loaded model update in {}ms", delay);

		if (theRequest.isLoadAuthHosts()) {
			if (retVal.getAuthenticationHostList() == null || retVal.getAuthenticationHostList().size() == 0) {
				throw new ServiceFailureException("Failed to return any authentication hosts!");
			}
		}

		return retVal;
	}

	@Override
	public UserAndAuthHost loadUser(long thePid) throws ServiceFailureException {
		if (isMockMode()) {
			return myMock.loadUser(thePid);
		}

		GUser user;
		try {
			user = myAdminSvc.loadUser(thePid);
			BaseGAuthHost authHost = myAdminSvc.loadAuthenticationHost(user.getPid());
			return new UserAndAuthHost(user, authHost);
		} catch (ProcessingException e) {
			ourLog.error("Failed to load user " + thePid, e);
			throw new ServiceFailureException(e.getMessage());
		}
	}

	@Override
	public GPartialUserList loadUsers(PartialUserListRequest theRequest) {
		if (isMockMode()) {
			return myMock.loadUsers(theRequest);
		}

		return myAdminSvc.loadUsers(theRequest);
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

			GSoap11ServiceVersionAndResources serviceAndResources;
			try {
				serviceAndResources = myAdminSvc.loadSoap11ServiceVersionFromWsdl(theService, theWsdlUrl);
			} catch (ProcessingException e) {
				ourLog.error("Failed to load service version from WSDL", e);
				throw new ServiceFailureException(e.getMessage());
			}

			saveServiceVersionResourcesToSession(serviceAndResources);

			retVal = serviceAndResources.getServiceVersion();
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
	public GAuthenticationHostList removeAuthenticationHost(long thePid) throws ServiceFailureException {
		if (isMockMode()) {
			return myMock.removeAuthenticationHost(thePid);
		}

		try {
			return myAdminSvc.deleteAuthenticationHost(thePid);
		} catch (ProcessingException e) {
			ourLog.error("Failed to delete authentication host", e);
			throw new ServiceFailureException(e.getMessage());
		}
	}

	@Override
	public GDomainList removeDomain(long thePid) throws ServiceFailureException {
		ourLog.info("Removing domain: {}", thePid);

		if (isMockMode()) {
			return myMock.removeDomain(thePid);
		}

		myAdminSvc.deleteDomain(thePid);

		try {
			return myAdminSvc.loadDomainList();
		} catch (ProcessingException e) {
			ourLog.error("Failed to load domain list", e);
			throw new ServiceFailureException(e.getMessage());
		}
	}

	@Override
	public void reportClientError(String theMessage, Throwable theException) {
		ourLog.warn("Client error - " + theMessage, theException);
	}

	@Override
	public GAuthenticationHostList saveAuthenticationHost(GLocalDatabaseAuthHost theAuthHost) throws ServiceFailureException {
		if (isMockMode()) {
			return myMock.saveAuthenticationHost(theAuthHost);
		}

		if (theAuthHost.getPid() <= 0) {
			theAuthHost.setPid(0);
			ourLog.info("Saving new authentication host");
		} else {
			ourLog.info("Saving authentication host {} / {}", theAuthHost.getPid(), theAuthHost.getModuleId());
		}

		try {
			return myAdminSvc.saveAuthenticationHost(theAuthHost);
		} catch (ProcessingException e) {
			ourLog.error("Failed to save authentication host", e);
			throw new ServiceFailureException(e.getMessage());
		}

	}

	@Override
	public GDomain saveDomain(long thePid, String theId, String theName) {
		if (isMockMode()) {
			return myMock.saveDomain(thePid, theId, theName);
		}
		return null;
	}

	@Override
	public GHttpClientConfig saveHttpClientConfig(boolean theCreate, GHttpClientConfig theConfig) throws ServiceFailureException {
		Validate.throwIllegalArgumentExceptionIfNull("HttpClientConfig", theConfig);

		if (theCreate) {
			ourLog.info("Saving new HTTP client config");
		} else {
			ourLog.info("Saving HTTP client config ID[{}]", theConfig.getPid());
		}

		if (isMockMode()) {
			return myMock.saveHttpClientConfig(theCreate, theConfig);
		}

		if (theCreate) {
			theConfig.setPid(0);
		}

		try {
			return myAdminSvc.saveHttpClientConfig(theConfig);
		} catch (ProcessingException e) {
			ourLog.error("Failed to save config", e);
			throw new ServiceFailureException(e.getMessage());
		}
	}

	private void saveServiceVersionResourcesToSession(GSoap11ServiceVersionAndResources theServiceAndResources) {
		GSoap11ServiceVersion serviceVersion = theServiceAndResources.getServiceVersion();
		ourLog.info("Storing service resource collection to temporary session with id: " + serviceVersion.getUncommittedSessionId());
		String key = SESSION_PREFIX_UNCOMITTED_SVC_VER_RES + theServiceAndResources.getServiceVersion().getUncommittedSessionId();
		getThreadLocalRequest().getSession(true).setAttribute(key, theServiceAndResources.getResource());
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
	public void saveUser(GUser theUser) {
		ourLog.info("Saving user {} / {}", theUser.getPid(), theUser.getUsername());

		if (isMockMode()) {
			myMock.saveUser(theUser);
			return;
		}

		myAdminSvc.saveUser(theUser);

		ourLog.info("Done saving user {} / {}", theUser.getPid(), theUser.getUsername());
	}

	/**
	 * For unit test only
	 */
	void setAdminSvc(IAdminService theAdminSvc) {
		myAdminSvc = theAdminSvc;
	}

}
