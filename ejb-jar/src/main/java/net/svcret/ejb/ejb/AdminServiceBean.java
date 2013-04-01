package net.svcret.ejb.ejb;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import net.svcret.admin.shared.model.BaseGClientSecurity;
import net.svcret.admin.shared.model.BaseGDashboardObjectWithUrls;
import net.svcret.admin.shared.model.BaseGServerSecurity;
import net.svcret.admin.shared.model.BaseGServiceVersion;
import net.svcret.admin.shared.model.GDomain;
import net.svcret.admin.shared.model.GDomainList;
import net.svcret.admin.shared.model.GHttpClientConfig;
import net.svcret.admin.shared.model.GHttpClientConfigList;
import net.svcret.admin.shared.model.GResource;
import net.svcret.admin.shared.model.GService;
import net.svcret.admin.shared.model.GServiceMethod;
import net.svcret.admin.shared.model.GServiceVersionResourcePointer;
import net.svcret.admin.shared.model.GServiceVersionUrl;
import net.svcret.admin.shared.model.GSoap11ServiceVersion;
import net.svcret.admin.shared.model.GSoap11ServiceVersionAndResources;
import net.svcret.admin.shared.model.GWsSecClientSecurity;
import net.svcret.admin.shared.model.GWsSecServerSecurity;
import net.svcret.admin.shared.model.ModelUpdateRequest;
import net.svcret.admin.shared.model.ModelUpdateResponse;
import net.svcret.admin.shared.model.StatusEnum;
import net.svcret.ejb.api.IAdminService;
import net.svcret.ejb.api.IRuntimeStatus;
import net.svcret.ejb.api.IServiceInvoker;
import net.svcret.ejb.api.IServicePersistence;
import net.svcret.ejb.ex.ProcessingException;
import net.svcret.ejb.model.entity.BasePersInvocationStats;
import net.svcret.ejb.model.entity.BasePersServiceVersion;
import net.svcret.ejb.model.entity.InvocationStatsIntervalEnum;
import net.svcret.ejb.model.entity.PersBaseClientAuth;
import net.svcret.ejb.model.entity.PersBaseServerAuth;
import net.svcret.ejb.model.entity.PersDomain;
import net.svcret.ejb.model.entity.PersHttpClientConfig;
import net.svcret.ejb.model.entity.PersInvocationStatsPk;
import net.svcret.ejb.model.entity.PersService;
import net.svcret.ejb.model.entity.PersServiceVersionMethod;
import net.svcret.ejb.model.entity.PersServiceVersionResource;
import net.svcret.ejb.model.entity.PersServiceVersionUrl;
import net.svcret.ejb.model.entity.PersServiceVersionUrlStatus;
import net.svcret.ejb.model.entity.soap.PersServiceVersionSoap11;
import net.svcret.ejb.util.Validate;

import org.apache.commons.lang3.time.DateUtils;

@TransactionAttribute(TransactionAttributeType.REQUIRED)
@Stateless
public class AdminServiceBean implements IAdminService {

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(AdminServiceBean.class);

	@EJB
	private IServicePersistence myPersSvc;

	@EJB
	private IRuntimeStatus myStatusSvc;

	@Override
	public GDomain addDomain(String theId, String theName) throws ProcessingException {
		Validate.throwIllegalArgumentExceptionIfBlank("ID", theId);
		Validate.throwIllegalArgumentExceptionIfBlank("Name", theName);

		ourLog.info("Creating domain {}/{}", theId, theName);

		PersDomain domain = myPersSvc.getOrCreateDomainWithId(theId);
		if (!domain.isNewlyCreated()) {
			throw new IllegalArgumentException("Domain with ID[" + theId + "] already exists");
		}

		domain.setDomainName(theName);
		myPersSvc.saveDomain(domain);

		return toUi(domain, false);
	}

	private GDomain toUi(PersDomain theDomain, boolean theLoadStats) {
		GDomain retVal = new GDomain();
		retVal.setPid(theDomain.getPid());
		retVal.setId(theDomain.getDomainId());
		retVal.setName(theDomain.getDomainName());

		if (theLoadStats) {
			retVal.setStatsInitialized(true);
			StatusEnum status = StatusEnum.UNKNOWN;
			int[] t60minCount = new int[60];
			long[] t60minTime = new long[60];

			for (PersService nextService : theDomain.getServices()) {
				status = extractStatus(retVal, status, t60minCount, t60minTime, nextService);
			}

			retVal.setTransactions60mins(t60minCount);
			retVal.setTransactions60mins(toLatency(t60minCount, t60minTime));
			retVal.setStatus(net.svcret.admin.shared.model.StatusEnum.valueOf(status.name()));

			int urlsActive = 0;
			int urlsDown = 0;
			int urlsUnknown = 0;
			for (PersService nextService : theDomain.getServices()) {
				for (BasePersServiceVersion nextVersion : nextService.getVersions()) {
					for (PersServiceVersionUrl nextUrl : nextVersion.getUrls()) {
						switch (nextUrl.getStatus().getStatus()) {
						case ACTIVE:
							urlsActive++;
							break;
						case DOWN:
							urlsDown++;
							break;
						case UNKNOWN:
							urlsUnknown++;
							break;
						}
					}
				}
			}
			retVal.setUrlsActive(urlsActive);
			retVal.setUrlsDown(urlsDown);
			retVal.setUrlsUnknown(urlsUnknown);

		}
		// retVal.get

		return retVal;
	}

	private int[] toLatency(int[] theCounts, long[] theTimes) {
		assert theCounts.length == theTimes.length;

		int[] retVal = new int[theCounts.length];
		for (int i = 0; i < theCounts.length; i++) {
			if (theCounts[i] > 0) {
				retVal[i] = (int) Math.min(theTimes[i] / theCounts[i], Integer.MAX_VALUE);
			}
		}

		return retVal;
	}

	private GService toUi(PersService theService, boolean theLoadStats) {
		GService retVal = new GService();
		retVal.setPid(theService.getPid());
		retVal.setId(theService.getServiceId());
		retVal.setName(theService.getServiceName());
		retVal.setActive(theService.isActive());
		
		if (theLoadStats) {
			retVal.setStatsInitialized(true);
			StatusEnum status = StatusEnum.UNKNOWN;
			int[] t60minCount = new int[60];
			long[] t60minTime = new long[60];

			status = extractStatus(retVal, status, t60minCount, t60minTime, theService);

			retVal.setTransactions60mins(t60minCount);
			retVal.setLatency60mins(toLatency(t60minCount, t60minTime));
			retVal.setStatus(net.svcret.admin.shared.model.StatusEnum.valueOf(status.name()));

			int urlsActive = 0;
			int urlsDown = 0;
			int urlsUnknown = 0;
			for (BasePersServiceVersion nextVersion : theService.getVersions()) {
				for (PersServiceVersionUrl nextUrl : nextVersion.getUrls()) {
					switch (nextUrl.getStatus().getStatus()) {
					case ACTIVE:
						urlsActive++;
						break;
					case DOWN:
						urlsDown++;
						break;
					case UNKNOWN:
						urlsUnknown++;
						break;
					}
				}
			}
			retVal.setUrlsActive(urlsActive);
			retVal.setUrlsDown(urlsDown);
			retVal.setUrlsUnknown(urlsUnknown);

		}

		return retVal;
	}

	private StatusEnum extractStatus(BaseGDashboardObjectWithUrls<?> theDashboardObject, StatusEnum theInitialStatus, int[] the60MinInvCount, long[] the60minTime, PersService theService) {

		// Value will be changed below
		StatusEnum status = theInitialStatus;

		for (BasePersServiceVersion nextVersion : theService.getVersions()) {
			for (PersServiceVersionUrl nextUrl : nextVersion.getUrls()) {
				PersServiceVersionUrlStatus nextUrlStatus = nextUrl.getStatus();
				switch (nextUrlStatus.getStatus()) {
				case ACTIVE:
					status = StatusEnum.ACTIVE;
					theDashboardObject.setUrlsActive(theDashboardObject.getUrlsActive() + 1);
					break;
				case DOWN:
					if (status != StatusEnum.ACTIVE) {
						status = StatusEnum.DOWN;
					}
					theDashboardObject.setUrlsDown(theDashboardObject.getUrlsDown() + 1);
					break;
				case UNKNOWN:
					theDashboardObject.setUrlsUnknown(theDashboardObject.getUrlsUnknown() + 1);
					break;
				}

			} // end URL

			for (PersServiceVersionMethod nextMethod : nextVersion.getMethods()) {
				Date date60MinsAgo = new Date(System.currentTimeMillis() - (60 * DateUtils.MILLIS_PER_MINUTE));
				Date date = DateUtils.truncate(date60MinsAgo, Calendar.MINUTE);
				for (int min = 0; min <= 59; min++, date = new Date(date.getTime() + DateUtils.MILLIS_PER_MINUTE)) {
					PersInvocationStatsPk pk = new PersInvocationStatsPk(InvocationStatsIntervalEnum.MINUTE, date, nextMethod);
					BasePersInvocationStats stats = myPersSvc.getOrCreateInvocationStats(pk);
					the60MinInvCount[min] = addToInt(the60MinInvCount[min], stats.getSuccessInvocationCount());
					the60minTime[min] = the60minTime[min] + stats.getSuccessInvocationTotalTime();
				}

			}

		} // end VERSION
		return status;
	}

	private int addToInt(int theAddTo, long theNumberToAdd) {
		long newValue = theAddTo + theNumberToAdd;
		if (newValue > Integer.MAX_VALUE) {
			return Integer.MAX_VALUE;
		}
		return (int) newValue;
	}

	void setPersSvc(ServicePersistenceBean thePersSvc) {
		assert myPersSvc == null;
		myPersSvc = thePersSvc;
	}

	@Override
	public GService addService(long theDomainPid, String theId, String theName, boolean theActive) throws ProcessingException {
		Validate.throwIllegalArgumentExceptionIfBlank("ID", theId);
		Validate.throwIllegalArgumentExceptionIfBlank("Name", theName);

		ourLog.info("Adding service with ID[{}] to domain PID[{}]", theId, theDomainPid);

		PersDomain domain = myPersSvc.getDomainByPid(theDomainPid);
		if (domain == null) {
			throw new IllegalArgumentException("Unknown Domain PID: " + theDomainPid);
		}

		PersService service = myPersSvc.getOrCreateServiceWithId(domain, theId);
		if (!service.isNewlyCreated()) {
			throw new IllegalArgumentException("Service " + theId + " already exists for domain: " + domain.getDomainId());
		}

		service.setServiceName(theName);
		service.setActive(theActive);
		myPersSvc.saveService(service);

		return toUi(service, false);
	}

	@EJB(name = "SOAP11Invoker")
	private IServiceInvoker<PersServiceVersionSoap11> myInvokerSoap11;

	@Override
	public GSoap11ServiceVersionAndResources loadSoap11ServiceVersionFromWsdl(GSoap11ServiceVersion theService, String theWsdlUrl) throws ProcessingException {
		Validate.throwIllegalArgumentExceptionIfNull("Definition", theService);
		Validate.throwIllegalArgumentExceptionIfBlank("URL", theWsdlUrl);

		ourLog.info("Loading service version from URL: {}", theWsdlUrl);

		GSoap11ServiceVersionAndResources retVal = new GSoap11ServiceVersionAndResources();

		PersServiceVersionSoap11 def = myInvokerSoap11.introspectServiceFromUrl(theWsdlUrl);
		retVal.setServiceVersion(toUi(def, theService));

		theService.getMethodList().clear();
		for (PersServiceVersionMethod next : def.getMethods()) {
			retVal.getServiceVersion().getMethodList().add(toUi(next));
		}

		theService.getResourcePointerList().clear();
		for (PersServiceVersionResource next : def.getUriToResource().values()) {
			GResource res = new GResource();
			res.setPid(next.getPid());
			res.setText(next.getResourceText());
			res.setUrl(next.getResourceUrl());
			retVal.getResource().add(res);
			theService.getResourcePointerList().add(res.asPointer());
		}

		return null;
	}

	private GSoap11ServiceVersion toUi(PersServiceVersionSoap11 theDef, GSoap11ServiceVersion theService) {
		// nothing here yet
		return theService;
	}

	private GServiceMethod toUi(PersServiceVersionMethod theMethod) {
		GServiceMethod retVal = new GServiceMethod();
		retVal.setId(theMethod.getName());
		retVal.setName(theMethod.getName());
		return retVal;
	}

	@Override
	public long getDomainPid(String theDomainId) throws ProcessingException {
		PersDomain domain = myPersSvc.getDomainById(theDomainId);
		if (domain == null) {
			throw new ProcessingException("Unknown ID: " + theDomainId);
		}
		return domain.getPid();
	}

	@Override
	public long getServicePid(long theDomainPid, String theServiceId) throws ProcessingException {
		PersService service = myPersSvc.getServiceById(theDomainPid, theServiceId);
		if (service == null) {
			throw new ProcessingException("Unknown ID: " + theServiceId);
		}
		return service.getPid();
	}

	@Override
	public GSoap11ServiceVersion addServiceVersion(long theDomain, long theService, GSoap11ServiceVersion theVersion, List<GResource> theResources) throws ProcessingException {
		Validate.throwIllegalArgumentExceptionIfBlank("Version#ID", theVersion.getId());

		ourLog.info("Adding service version {} to domain {} / service {}", new Object[] { theVersion.getPid(), theDomain, theService });

		PersDomain domain = myPersSvc.getDomainByPid(theDomain);
		if (domain == null) {
			throw new ProcessingException("Unknown domain ID: " + theDomain);
		}

		PersService service = myPersSvc.getServiceByPid(theService);
		if (service == null) {
			throw new ProcessingException("Unknown service ID: " + theService);
		}

		if (!domain.equals(service.getDomain())) {
			throw new ProcessingException("Service with ID " + theService + " is not a part of domain " + theDomain);
		}

		String versionId = theVersion.getId();
		PersServiceVersionSoap11 version = myPersSvc.getOrCreateServiceVersionWithId(service, versionId);

		fromUi(version, theVersion);

		for (GResource next : theResources) {
			version.getUriToResource().put(next.getUrl(), fromUi(next, version));
		}

		for (GServiceVersionUrl next : theVersion.getUrlList()) {
			version.getUrls().add(fromUi(next, version));
		}

		myPersSvc.saveServiceVersion(version);

		return null;
	}

	private PersServiceVersionUrl fromUi(GServiceVersionUrl theUrl, BasePersServiceVersion theServiceVersion) {
		PersServiceVersionUrl retVal = new PersServiceVersionUrl();
		retVal.setUrlId(theUrl.getId());
		retVal.setUrl(theUrl.getUrl());
		retVal.setServiceVersion(theServiceVersion);
		return retVal;
	}

	private PersServiceVersionResource fromUi(GResource theRes, BasePersServiceVersion theServiceVersion) {
		PersServiceVersionResource retVal = new PersServiceVersionResource();
		retVal.setResourceContentType(theRes.getContentType());
		retVal.setResourceText(theRes.getText());
		retVal.setResourceUrl(theRes.getUrl());
		retVal.setServiceVersion(theServiceVersion);
		return retVal;
	}

	private PersServiceVersionSoap11 fromUi(PersServiceVersionSoap11 thePersVersion, GSoap11ServiceVersion theVersion) {
		thePersVersion.setActive(thePersVersion.isActive());
		thePersVersion.setVersionId(theVersion.getId());
		thePersVersion.setWsdlUrl(theVersion.getWsdlLocation());
		return thePersVersion;
	}

	@Override
	public ModelUpdateResponse loadModelUpdate(ModelUpdateRequest theRequest) throws ProcessingException {
		ModelUpdateResponse retVal = new ModelUpdateResponse();

		GDomainList domainList = new GDomainList();
		Set<Long> loadDomStats = theRequest.getDomainsToLoadStats();
		Set<Long> loadSvcStats = theRequest.getServicesToLoadStats();
		Set<Long> loadVerStats = theRequest.getVersionsToLoadStats();

		if (theRequest.isLoadHttpClientConfigs()) {
			GHttpClientConfigList configList = loadHttpClientConfigList();
			retVal.setHttpClientConfigList(configList);
		}

		for (PersDomain nextDomain : myPersSvc.getAllDomains()) {
			GDomain gDomain = toUi(nextDomain, loadDomStats.contains(nextDomain.getPid()));
			domainList.add(gDomain);

			for (PersService nextService : nextDomain.getServices()) {
				GService gService = toUi(nextService, loadSvcStats.contains(nextService.getPid()));
				gDomain.getServiceList().add(gService);

				for (BasePersServiceVersion nextVersion : nextService.getVersions()) {
					BaseGServiceVersion gVersion = toUi(nextVersion, loadVerStats.contains(nextVersion.getPid()));

					for (PersServiceVersionMethod nextMethod : nextVersion.getMethods()) {
						GServiceMethod gMethod = toUi(nextMethod);
						gVersion.getMethodList().add(gMethod);
					} // for methods

					for (PersServiceVersionUrl nextUrl : nextVersion.getUrls()) {
						GServiceVersionUrl gUrl = toUi(nextUrl);
						gVersion.getUrlList().add(gUrl);
					} // for URLs

					for (PersServiceVersionResource nextResource : nextVersion.getUriToResource().values()) {
						GServiceVersionResourcePointer gResource = toUi(nextResource);
						gVersion.getResourcePointerList().add(gResource);
					} // for resources

					for (PersBaseServerAuth<?, ?> nextServerAuth : nextVersion.getServerAuths()) {
						BaseGServerSecurity gServerAuth = toUi(nextServerAuth);
						gVersion.getServerSecurityList().add(gServerAuth);
					} // server auths

					for (PersBaseClientAuth<?> nextClientAuth : nextVersion.getClientAuths()) {
						BaseGClientSecurity gClientAuth = toUi(nextClientAuth);
						gVersion.getClientSecurityList().add(gClientAuth);
					} // Client auths

				} // for service versions
			} // for services
		} // for domains

		return retVal;
	}

	private GHttpClientConfigList loadHttpClientConfigList() {
		GHttpClientConfigList configList = new GHttpClientConfigList();
		for (PersHttpClientConfig next : myPersSvc.getHttpClientConfigs()) {
			configList.add(toUi(next));
		}
		return configList;
	}

	private GHttpClientConfig toUi(PersHttpClientConfig theConfig) {
		GHttpClientConfig retVal = new GHttpClientConfig();
		
		retVal.setPid(theConfig.getPid());
		retVal.setId(theConfig.getId());
		retVal.setName(theConfig.getName());
		
		retVal.setCircuitBreakerEnabled(theConfig.isCircuitBreakerEnabled());
		retVal.setCircuitBreakerTimeBetweenResetAttempts(theConfig.getCircuitBreakerTimeBetweenResetAttempts());
		
		retVal.setConnectTimeoutMillis(theConfig.getConnectTimeoutMillis());
		retVal.setReadTimeoutMillis(theConfig.getReadTimeoutMillis());
		
		retVal.setFailureRetriesBeforeAborting(theConfig.getFailureRetriesBeforeAborting());
		
		retVal.setUrlSelectionPolicy(theConfig.getUrlSelectionPolicy());
		
		return retVal;
	}

	private BaseGServerSecurity toUi(PersBaseServerAuth<?, ?> theAuth) throws ProcessingException {
		BaseGServerSecurity retVal = null;

		switch (theAuth.getAuthType()) {
		case WS_SECURITY_USERNAME_TOKEN:
			GWsSecServerSecurity auth = new GWsSecServerSecurity();
			retVal = auth;
			break;
		}

		if (retVal == null) {
			throw new ProcessingException("Unknown auth type; " + theAuth.getAuthType());
		}

		retVal.setPid(theAuth.getPid());

		return retVal;
	}

	private BaseGClientSecurity toUi(PersBaseClientAuth<?> theAuth) throws ProcessingException {
		BaseGClientSecurity retVal = null;

		switch (theAuth.getAuthType()) {
		case WS_SECURITY_USERNAME_TOKEN:
			GWsSecClientSecurity auth = new GWsSecClientSecurity();
			retVal = auth;
			break;
		}

		if (retVal == null) {
			throw new ProcessingException("Unknown auth type; " + theAuth.getAuthType());
		}

		retVal.setPid(theAuth.getPid());

		return retVal;
	}

	private GServiceVersionResourcePointer toUi(PersServiceVersionResource theResource) {
		GServiceVersionResourcePointer retVal = new GServiceVersionResourcePointer();
		retVal.setPid(theResource.getPid());
		retVal.setSize(theResource.getResourceText().length());
		retVal.setType(theResource.getResourceContentType());
		retVal.setUrl(theResource.getResourceUrl());
		return retVal;
	}

	private GServiceVersionUrl toUi(PersServiceVersionUrl theUrl) {
		GServiceVersionUrl retVal = new GServiceVersionUrl();
		retVal.setPid(theUrl.getPid());
		retVal.setId(theUrl.getUrlId());
		retVal.setUrl(theUrl.getUrl());
		return retVal;
	}

	private BaseGServiceVersion toUi(BasePersServiceVersion theVersion, boolean theLoadStats) throws ProcessingException {
		BaseGServiceVersion retVal = null;
		switch (theVersion.getProtocol()) {
		case SOAP11:
			PersServiceVersionSoap11 persSoap11 = (PersServiceVersionSoap11) theVersion;
			GSoap11ServiceVersion soap11RetVal = new GSoap11ServiceVersion();
			soap11RetVal.setWsdlLocation(persSoap11.getWsdlUrl());
			retVal = soap11RetVal;
			break;
		}

		if (retVal == null) {
			throw new ProcessingException("Don't know how to handle service of type " + theVersion.getProtocol());
		}

		retVal.setPid(theVersion.getPid());
		retVal.setId(theVersion.getVersionId());
		retVal.setName(theVersion.getVersionId());

		if (theLoadStats) {
			retVal.setStatsInitialized(true);
			int[] t60minCount = new int[60];
			long[] t60minTime = new long[60];

			retVal.setTransactions60mins(t60minCount);
			retVal.setLatency60mins(toLatency(t60minCount, t60minTime));

			int urlsActive = 0;
			int urlsDown = 0;
			int urlsUnknown = 0;
			for (PersServiceVersionUrl nextUrl : theVersion.getUrls()) {
				switch (nextUrl.getStatus().getStatus()) {
				case ACTIVE:
					urlsActive++;
					break;
				case DOWN:
					urlsDown++;
					break;
				case UNKNOWN:
					urlsUnknown++;
					break;
				}
			}
			retVal.setUrlsActive(urlsActive);
			retVal.setUrlsDown(urlsDown);
			retVal.setUrlsUnknown(urlsUnknown);

			if (urlsDown > 0) {
				retVal.setStatus(net.svcret.admin.shared.model.StatusEnum.DOWN);
			} else if (urlsActive > 0) {
				retVal.setStatus(net.svcret.admin.shared.model.StatusEnum.ACTIVE);
			} else {
				retVal.setStatus(net.svcret.admin.shared.model.StatusEnum.UNKNOWN);
			}
		}

		return retVal;
	}

	@Override
	public GDomain getDomainByPid(long theDomain) {
		PersDomain domain = myPersSvc.getDomainByPid(theDomain);
		if (domain != null) {
			return toUi(domain, false);
		}
		return null;
	}

	@Override
	public GService getServiceByPid(long theService) {
		PersService service = myPersSvc.getServiceByPid(theService);
		if (service != null) {
			return toUi(service, false);
		}
		return null;
	}

	@Override
	public GHttpClientConfig saveHttpClientConfig(GHttpClientConfig theConfig) throws ProcessingException {
		Validate.throwIllegalArgumentExceptionIfNull("HttpClientConfig", theConfig);
		
		PersHttpClientConfig existing = null;
		boolean isDefault = false;
		
		if (theConfig.getPid() <= 0) {
			ourLog.info("Saving new HTTP client config");
		} else {
			ourLog.info("Saving HTTP client config ID[{}]", theConfig.getPid());
			existing = myPersSvc.getHttpClientConfig(theConfig.getPid());
			if (existing == null) {
				throw new ProcessingException("Unknown client config PID: " + theConfig.getPid());
			}
			if (existing.getId().equals(GHttpClientConfig.DEFAULT_ID)) {
				isDefault = true;
			}
		}
		
		PersHttpClientConfig config = fromUi(theConfig);
		if (isDefault) {
			config.setId(config.getId());
			config.setName(config.getName());
		}
	
		return toUi(myPersSvc.saveHttpClientConfig(config));
	}


	private PersHttpClientConfig fromUi(GHttpClientConfig theConfig) {
		PersHttpClientConfig retVal = new PersHttpClientConfig();
		
		if (theConfig.getPid() > 0) {
			retVal.setPid(theConfig.getPid());
		}
		
		retVal.setId(theConfig.getId());
		retVal.setName(theConfig.getName());
		retVal.setCircuitBreakerEnabled(theConfig.isCircuitBreakerEnabled());
		retVal.setCircuitBreakerTimeBetweenResetAttempts(theConfig.getCircuitBreakerTimeBetweenResetAttempts());
		retVal.setConnectTimeoutMillis(theConfig.getConnectTimeoutMillis());
		retVal.setFailureRetriesBeforeAborting(theConfig.getFailureRetriesBeforeAborting());
		retVal.setReadTimeoutMillis(theConfig.getReadTimeoutMillis());
		retVal.setUrlSelectionPolicy(theConfig.getUrlSelectionPolicy());
		
		return retVal;
	}

	@Override
	public GHttpClientConfigList deleteHttpClientConfig(long thePid) throws ProcessingException {
		
		PersHttpClientConfig config = myPersSvc.getHttpClientConfig(thePid);
		if (config == null) {
			throw new ProcessingException("Unknown HTTP Client Config PID: " + thePid);
		}
		
		ourLog.info("Deleting HTTP Client Config {} / {}", thePid, config.getId());
		
		myPersSvc.deleteHttpClientConfig(config);
		
		return loadHttpClientConfigList();
	}

//	private GHttpClientConfig toUi(PersHttpClientConfig theConfig) {
//		GHttpClientConfig retVal = new GHttpClientConfig();
//		
//		if (theConfig.getPid() > 0) {
//			retVal.setPid(theConfig.getPid());
//		}
//		
//		retVal.setId(theConfig.getId());
//		retVal.setName(theConfig.getName());
//		retVal.setCircuitBreakerEnabled(theConfig.isCircuitBreakerEnabled());
//		retVal.setCircuitBreakerTimeBetweenResetAttempts(theConfig.getCircuitBreakerTimeBetweenResetAttempts());
//		retVal.setConnectTimeoutMillis(theConfig.getConnectTimeoutMillis());
//		retVal.setFailureRetriesBeforeAborting(theConfig.getFailureRetriesBeforeAborting());
//		retVal.setReadTimeoutMillis(theConfig.getReadTimeoutMillis());
//		retVal.setUrlSelectionPolicy(theConfig.getUrlSelectionPolicy());
//		
//		return retVal;
//	}

}
