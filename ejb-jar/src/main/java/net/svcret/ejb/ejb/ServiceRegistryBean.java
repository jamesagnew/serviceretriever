package net.svcret.ejb.ejb;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import net.svcret.ejb.api.IHttpClient;
import net.svcret.ejb.api.IServicePersistence;
import net.svcret.ejb.api.IServiceRegistry;
import net.svcret.ejb.ex.InternalErrorException;
import net.svcret.ejb.ex.ProcessingException;
import net.svcret.ejb.model.entity.BasePersServiceVersion;
import net.svcret.ejb.model.entity.PersDomain;
import net.svcret.ejb.model.entity.PersService;
import net.svcret.ejb.model.entity.PersUser;
import net.svcret.ejb.model.entity.soap.PersServiceVersionSoap11;
import net.svcret.ejb.model.registry.ServiceVersion;
import net.svcret.ejb.model.registry.Services;
import net.svcret.ejb.model.registry.Services.Service;
import net.svcret.ejb.util.Validate;
import net.svcret.ejb.util.WsdlDescriptionType;

@Startup
@Singleton
public class ServiceRegistryBean implements IServiceRegistry {
	private static JAXBContext ourJaxbContext;
	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(ServiceRegistryBean.class);

	private static volatile Map<String, PersServiceVersionSoap11> ourProxyPathToServices;
	private static Object ourRegistryLock = new Object();
	
	@EJB
	private IHttpClient mySvcHttpClient;

	@EJB
	private IServicePersistence mySvcPersistence;

	/**
	 * Constructor
	 */
	public ServiceRegistryBean() throws InternalErrorException {
		if (ourJaxbContext == null) {
			try {
				ourJaxbContext = JAXBContext.newInstance(Services.class, WsdlDescriptionType.class);
			} catch (JAXBException e) {
				throw new InternalErrorException(e);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BasePersServiceVersion getServiceVersionForPath(String thePath) {
		if (thePath == null) {
			throw new IllegalArgumentException("Path can not be null");
		}
		synchronized (ourRegistryLock) {
			return ourProxyPathToServices.get(thePath);
		}
	}

	private void loadServiceDefinition__(Service theService) throws ProcessingException {
		Validate.throwProcessingExceptionIfBlank("Service ID is blank/missing", theService.getServiceId());
		Validate.throwProcessingExceptionIfBlank("PersDomain ID is blank/missing", theService.getDomainId());
		Validate.throwProcessingExceptionIfBlank("Service Name is blank/missing", theService.getServiceName());
		Validate.throwProcessingExceptionIfEmpty("No versions provided", theService.getVersions());

		PersDomain pDomain = mySvcPersistence.getOrCreateDomainWithId(theService.getDomainId());
		PersService pService = mySvcPersistence.getOrCreateServiceWithId(pDomain, theService.getServiceId());
		
		pService.setServiceName(theService.getServiceName());
		mySvcPersistence.saveService(pService);
		
		assert pDomain != null;
		assert pService != null;

		ourLog.info("Initializing Service with ID[{}]: {}", pService.getServiceId(), pService.getServiceName());

		Collection<ServiceVersion> versions = theService.getVersions();
		for (ServiceVersion nextVersion : versions) {

			PersServiceVersionSoap11 pVersion = mySvcPersistence.getOrCreateServiceVersionWithId(pService, nextVersion.getVersionId());
			pVersion.setActive(pVersion.isActive());
			pVersion.setWsdlUrl(nextVersion.getWsdlUrl());


			mySvcPersistence.saveServiceVersion(pVersion);
		}
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void loadServiceDefinition__(String theXmlContents) throws ProcessingException {

		Services service;
		try {
			service = (Services) ourJaxbContext.createUnmarshaller().unmarshal(new StringReader(theXmlContents));
		} catch (JAXBException e) {
			throw new ProcessingException("Failed to unmarshall XML for service definition", e);
		}

		if (service.getServices().size() == 0) {
			throw new ProcessingException("No services defined in file");
		}

		for (Service nextService : service.getServices()) {
			loadServiceDefinition__(nextService);
		}

	}

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@PostConstruct
	public void postConstruct() {
		reloadRegistryFromDatabase();
	}
	
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@Override
	public void reloadRegistryFromDatabase() {
		ourLog.info("Reloading service registry from database");

		Map<String, PersDomain> domainMap = new HashMap<String, PersDomain>();
		Map<String, PersServiceVersionSoap11> pathToServiceVersions = new HashMap<String, PersServiceVersionSoap11>();
		Collection<PersDomain> domains = mySvcPersistence.getAllDomains();
		for (PersDomain nextDomain : domains) {
			domainMap.put(nextDomain.getDomainId(), nextDomain);
			nextDomain.loadAllAssociations();

			for (PersService nextService : nextDomain.getServices()) {
				for (BasePersServiceVersion nextVersion : nextService.getVersions()) {
					String nextProxyPath = nextVersion.getProxyPath();
					if (pathToServiceVersions.containsKey(nextProxyPath)) {
						ourLog.warn("Service version {} created duplicate proxy path, so it will be ignored!", nextVersion.getPid());
						continue;
					}
					pathToServiceVersions.put(nextProxyPath, (PersServiceVersionSoap11) nextVersion);
				}
			}

		}

		Map<String, PersUser> serviceUserMap = new HashMap<String, PersUser>();
		Collection<PersUser> users = mySvcPersistence.getAllServiceUsers();
		for (PersUser nextUser : users) {
			serviceUserMap.put(nextUser.getUsername(), nextUser);
			nextUser.loadAllAssociations();
		}

		ourLog.info("Done loading service registry from database");

		synchronized (ourRegistryLock) {
			ourProxyPathToServices = pathToServiceVersions;
		}

	}

	/**
	 * FOR UNIT TESTS ONLY
	 */
	public void setSvcHttpClient(IHttpClient theSvcHttpClient) {
		Validate.throwIllegalStateExceptionIfNotNull("IServicHttpClient", mySvcHttpClient);
		mySvcHttpClient = theSvcHttpClient;
	}

	/**
	 * FOR UNIT TESTS ONLY
	 */
	public void setSvcPersistence(IServicePersistence theSvcPersistence) {
		Validate.throwIllegalStateExceptionIfNotNull("IServicePersistence", mySvcPersistence);
		mySvcPersistence = theSvcPersistence;
	}

	@Override
	public List<String> getValidPaths() {
		List<String> retVal=new ArrayList<String>();
		
		retVal.addAll(ourProxyPathToServices.keySet());
		Collections.sort(retVal);
		
		return retVal;
	}

	// public class MyWsdlLocator implements WSDLLocator {
	//
	// private PersServiceVersionSoap11 myVersion;
	// private String myWsdlText;
	//
	// public MyWsdlLocator(PersServiceVersionSoap11 theVersion) {
	// myVersion = theVersion;
	// }
	//
	// @Override
	// public void close() {
	// // nothing
	// }
	//
	// @Override
	// public InputSource getBaseInputSource() {
	// ourLog.info("Retrieving WSDL: {}", myVersion.getWsdlUrl());
	// HttpResponseBean wsdlText;
	// try {
	// wsdlText = mySvcHttpClient.get(myVersion.getWsdlUrl());
	// } catch (HttpFailureException e) {
	// throw new ProcessingRuntimeException("Failed to load WSDL \"" +
	// myVersion.getWsdlUrl() + "\". Problem was: " + e.getMessage(), e);
	// }
	//
	// myWsdlText = wsdlText.getBody();
	//
	// myVersion.addResource(myVersion.getWsdlUrl(), wsdlText.getBody());
	// InputSource wsdlIs = new InputSource(new
	// StringReader(wsdlText.getBody()));
	// return wsdlIs;
	// }
	//
	// @Override
	// public String getBaseURI() {
	// return myVersion.getWsdlUrl();
	// }
	//
	// @Override
	// public InputSource getImportInputSource(String theParentLocation, String
	// theImportLocation) {
	//
	// String norm;
	// try {
	// norm = UrlUtil.calculateRelativeUrl(theParentLocation,
	// theImportLocation);
	// } catch (URISyntaxException e) {
	// throw new ProcessingRuntimeException("Invalid URL found within WSDL: " +
	// e.getMessage(), e);
	// }
	// ourLog.info("Retrieving Import - Parent: {} - Location: {} - Normalized: {}",
	// new Object[] { theParentLocation, theImportLocation });
	//
	// HttpResponseBean wsdlText;
	// try {
	// wsdlText = mySvcHttpClient.get(norm);
	// } catch (HttpFailureException e) {
	// throw new ProcessingRuntimeException("Failed to load schema import \"" +
	// norm + "\". Problem was: " + e.getMessage(), e);
	// }
	//
	// myVersion.addResource(theImportLocation, wsdlText.getBody());
	// InputSource wsdlIs = new InputSource(new
	// StringReader(wsdlText.getBody()));
	// return wsdlIs;
	//
	// // return null;
	// }
	//
	// @Override
	// public String getLatestImportURI() {
	// // TODO Auto-generated method stub
	// return null;
	// }
	//
	// public String getWsdlBody() {
	// return myWsdlText;
	// }
	//
	// }

}
