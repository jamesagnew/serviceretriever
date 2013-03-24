package net.svcret.admin.server.rpc;

import java.util.Date;
import java.util.Set;

import net.svcret.admin.client.rpc.ModelUpdateService;
import net.svcret.admin.shared.ServiceFailureException;
import net.svcret.admin.shared.model.AddServiceVersionResponse;
import net.svcret.admin.shared.model.BaseGDashboardObjectWithUrls;
import net.svcret.admin.shared.model.BaseGServiceVersion;
import net.svcret.admin.shared.model.GDomain;
import net.svcret.admin.shared.model.GDomainList;
import net.svcret.admin.shared.model.GHttpClientConfig;
import net.svcret.admin.shared.model.GHttpClientConfigList;
import net.svcret.admin.shared.model.GService;
import net.svcret.admin.shared.model.GServiceMethod;
import net.svcret.admin.shared.model.GServiceVersionUrl;
import net.svcret.admin.shared.model.GSoap11ServiceVersion;
import net.svcret.admin.shared.model.ModelUpdateRequest;
import net.svcret.admin.shared.model.ModelUpdateResponse;
import net.svcret.admin.shared.model.StatusEnum;

import org.apache.commons.lang.StringUtils;

public class ModelUpdateServiceMock implements ModelUpdateService {

	private static long ourNextPid = 1000000L;
	private GDomainList myDomainList;
	private GHttpClientConfigList myClientConfigList;

	public ModelUpdateServiceMock() {
		myDomainList = new GDomainList();

		GDomain dom = new GDomain();
		dom.setPid(ourNextPid++);
		dom.setId("domain1");
		dom.setName("Domain 1");
		myDomainList.add(dom);

		GService svc = new GService();
		svc.setId("svc1a");
		svc.setName("Service 1-A");
		svc.setPid(10L);
		dom.getServiceList().add(svc);

		BaseGServiceVersion ver = new GSoap11ServiceVersion();
		ver.setActive(true);
		ver.setId("Version 1-A-1");
		ver.setPid(100L);
		ver.setName("Version 1-A-1");
		ver.setLastAccess(new Date());
		svc.getVersionList().add(ver);

		GServiceMethod met = new GServiceMethod();
		met.setPid(1000L);
		met.setId("Method 1");
		met.setName("Method 1");
		ver.getMethodList().add(met);

		svc = new GService();
		svc.setId("svc1b");
		svc.setName("Service 1-B");
		svc.setPid(11L);
		dom.getServiceList().add(svc);

		dom = new GDomain();
		dom.setPid(2L);
		dom.setId("domain2");
		dom.setName("Domain 2");
		myDomainList.add(dom);

		myClientConfigList = new GHttpClientConfigList();
		GHttpClientConfig defCfg = new GHttpClientConfig();
		defCfg.setPid(ourNextPid++);
		defCfg.setId("DEFAULT");
		defCfg.setCircuitBreakerTimeBetweenResetAttempts(2);
		defCfg.setReadTimeoutMillis(1000);
		defCfg.setConnectTimeoutMillis(2000);
		myClientConfigList.add(defCfg);

	}

	@Override
	public ModelUpdateResponse loadModelUpdate(ModelUpdateRequest theRequest) {
		ModelUpdateResponse retVal = new ModelUpdateResponse();

		retVal.setDomainList(new GDomainList());
		retVal.getDomainList().mergeResults(myDomainList);

		for (GDomain nextDomain : retVal.getDomainList()) {
			Set<Long> domainsToLoadStats = theRequest.getDomainsToLoadStats();
			long nextDomainPid = nextDomain.getPid();
			if (domainsToLoadStats.contains(nextDomainPid)) {
				populateRandom(nextDomain);
			}
			for (GService nextService : nextDomain.getServiceList()) {
				if (theRequest.getServicesToLoadStats().contains(nextService.getPid())) {
					populateRandom(nextService);
				}
				for (BaseGServiceVersion nextVersion : nextService.getVersionList()) {
					if (theRequest.getVersionsToLoadStats().contains(nextVersion.getPid())) {
						populateRandom(nextVersion);
					}
				}
			}
		}

		return retVal;
	}

	private void populateRandom(BaseGDashboardObjectWithUrls<?> obj) {
		obj.setStatsInitialized(true);
		obj.setStatus(randomStatus());
		obj.setTransactions60mins(random60mins());
		obj.setLatency60mins(random60mins());
		obj.setUrlsActive(randomUrlNumber());
		obj.setUrlsDown(randomUrlNumber());
		obj.setUrlsUnknown(randomUrlNumber());
	}

	private int randomUrlNumber() {
		return (int) (5.0 * Math.random());
	}

	private StatusEnum randomStatus() {
		double rnd = 3.0 * Math.random();
		if (rnd < 1) {
			return StatusEnum.ACTIVE;
		}
		if (rnd < 2) {
			return StatusEnum.DOWN;
		}
		return StatusEnum.UNKNOWN;
	}

	private int[] random60mins() {
		int[] retVal = new int[60];
		for (int i = 0; i < 60; i++) {
			retVal[i] = (int) (Math.random() * 100.0);
		}
		return retVal;
	}

	@Override
	public GDomain addDomain(String theId, String theName) throws ServiceFailureException {
		GDomain retVal = new GDomain();
		retVal.setId(theId);
		retVal.setName(theName);
		retVal.setPid(ourNextPid++);
		myDomainList.add(retVal);
		return retVal;
	}

	@Override
	public GDomain saveDomain(long thePid, String theId, String theName) {
		GDomain retVal = myDomainList.getDomainByPid(thePid);
		retVal.setPid(thePid);
		retVal.setId(theId);
		retVal.setName(theName);
		return retVal;
	}

	@Override
	public GService addService(long theDomainPid, String theId, String theName, boolean theActive) {

		GDomain dom = myDomainList.getDomainByPid(theDomainPid);

		GService svc = new GService();
		svc.setPid(ourNextPid++);
		svc.setId(theId);
		svc.setName(theName);
		svc.setStatus(StatusEnum.ACTIVE);
		svc.setTransactions60mins(random60mins());
		svc.setLatency60mins(random60mins());
		svc.setActive(theActive);

		dom.getServiceList().add(svc);

		return svc;
	}

	@Override
	public GSoap11ServiceVersion loadWsdl(GSoap11ServiceVersion theService, String theWsdlUrl) throws ServiceFailureException {
		if (StringUtils.isBlank(theWsdlUrl)) {
			throw new ServiceFailureException("Failed to load URL: \"" + theWsdlUrl + '"');
		}

		GSoap11ServiceVersion retVal = new GSoap11ServiceVersion();
		retVal.setWsdlLocation(theWsdlUrl);

		retVal.setActive(true);
		retVal.setUncommittedSessionId(theService.getUncommittedSessionId());

		GServiceMethod method = new GServiceMethod();
		method.setId("method1");
		method.setName("method1");
		retVal.getMethodList().add(method);

		method = new GServiceMethod();
		method.setId("method2");
		method.setName("method2");
		retVal.getMethodList().add(method);

		GServiceVersionUrl url = new GServiceVersionUrl();
		url.setId("url1");
		url.setUrl("http://something/aaaa.html");
		retVal.getUrlList().add(url);

		return retVal;
	}

	@Override
	public void saveServiceVersionToSession(GSoap11ServiceVersion theServiceVersion) {
		throw new UnsupportedOperationException();
	}

	@Override
	public GSoap11ServiceVersion createNewSoap11ServiceVersion(Long theUncomittedId) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void reportClientError(String theMessage, Throwable theException) {
		throw new UnsupportedOperationException();
	}


	@Override
	public AddServiceVersionResponse addServiceVersion(Long theExistingDomainPid, String theCreateDomainId, Long theExistingServicePid, String theCreateServiceId, GSoap11ServiceVersion theVersion) throws ServiceFailureException {
		GDomain dom;
		if (theExistingDomainPid != null) {
			dom = myDomainList.getDomainByPid(theExistingDomainPid);
			if (dom == null) {
				throw new NullPointerException("Unknown dom " + theExistingDomainPid);
			}
		} else {
			dom = new GDomain();
			dom.setPid(ourNextPid++);
			dom.setId(theCreateDomainId);
			dom.setName(theCreateDomainId);
			myDomainList.add(dom);
		}

		GService svc;
		if (theExistingServicePid != null) {
			svc = dom.getServiceList().getServiceByPid(theExistingServicePid);
			if (svc == null) {
				throw new NullPointerException("Unknown service " + theExistingDomainPid);
			}
		} else {
			svc = new GService();
			svc.setPid(ourNextPid++);
			svc.setId(theCreateServiceId);
			svc.setName(theCreateServiceId);
			dom.getServiceList().add(svc);
		}

		theVersion.setPid(ourNextPid++);
		
		svc.getVersionList().add(theVersion);
		
		AddServiceVersionResponse retVal= new AddServiceVersionResponse();
		retVal.setNewDomain(dom);
		retVal.setNewService(svc);
		retVal.setNewServiceVersion(theVersion);
		return retVal;
	}

}
