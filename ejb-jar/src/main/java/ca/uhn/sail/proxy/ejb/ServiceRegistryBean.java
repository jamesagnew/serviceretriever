package ca.uhn.sail.proxy.ejb;

import java.io.StringReader;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import ca.uhn.sail.proxy.Messages;
import ca.uhn.sail.proxy.api.HttpResponseBean;
import ca.uhn.sail.proxy.api.IHttpClient;
import ca.uhn.sail.proxy.api.IServicePersistence;
import ca.uhn.sail.proxy.api.IServiceRegistry;
import ca.uhn.sail.proxy.ejb.soap.Constants;
import ca.uhn.sail.proxy.ex.InternalErrorException;
import ca.uhn.sail.proxy.ex.ProcessingException;
import ca.uhn.sail.proxy.ex.ProcessingRuntimeException;
import ca.uhn.sail.proxy.model.entity.BasePersServiceVersion;
import ca.uhn.sail.proxy.model.entity.PersDomain;
import ca.uhn.sail.proxy.model.entity.PersService;
import ca.uhn.sail.proxy.model.entity.PersServiceUser;
import ca.uhn.sail.proxy.model.entity.PersServiceVersionMethod;
import ca.uhn.sail.proxy.model.entity.soap.PersServiceVersionSoap11;
import ca.uhn.sail.proxy.model.registry.ServiceVersion;
import ca.uhn.sail.proxy.model.registry.Services;
import ca.uhn.sail.proxy.model.registry.Services.Service;
import ca.uhn.sail.proxy.util.UrlUtil;
import ca.uhn.sail.proxy.util.Validate;
import ca.uhn.sail.proxy.util.WsdlDescriptionType;
import ca.uhn.sail.proxy.util.XMLUtils;

import com.google.common.collect.Lists;

@Stateless
public class ServiceRegistryBean implements IServiceRegistry {
	private static volatile Map<String, PersDomain> ourDomains;
	private static JAXBContext ourJaxbContext;
	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(ServiceRegistryBean.class);

	private static volatile Map<String, PersServiceVersionSoap11> ourProxyPathToServices;
	private static Object ourRegistryLock = new Object();
	private static volatile Map<String, PersServiceUser> ourServiceUsers;
	// private static WSDLFactory ourWsdlFactory;

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

	private void loadServiceDefinition(Service theService) throws ProcessingException {
		Validate.throwProcessingExceptionIfBlank("Service ID is blank/missing", theService.getServiceId());
		Validate.throwProcessingExceptionIfBlank("PersDomain ID is blank/missing", theService.getDomainId());
		Validate.throwProcessingExceptionIfBlank("Service Name is blank/missing", theService.getServiceName());
		Validate.throwProcessingExceptionIfEmpty("No versions provided", theService.getVersions());

		PersDomain pDomain = mySvcPersistence.getOrCreateDomainWithId(theService.getDomainId());
		PersService pService = mySvcPersistence.getOrCreateServiceWithId(pDomain, theService.getServiceId(), theService.getServiceName());

		assert pDomain != null;
		assert pService != null;

		ourLog.info("Initializing Service with ID[{}]: {}", pService.getServiceId(), pService.getServiceName());

		Collection<ServiceVersion> versions = theService.getVersions();
		for (ServiceVersion nextVersion : versions) {

			PersServiceVersionSoap11 pVersion = mySvcPersistence.getOrCreateServiceVersionWithId(pService, nextVersion.getVersionId());
			pVersion.setActive(pVersion.isActive());
			pVersion.setWsdlUrl(nextVersion.getWsdlUrl());

			ourLog.info("Loading WSDL URL: {}", nextVersion.getWsdlUrl());
			HttpResponseBean wsdlHttpResponse = mySvcHttpClient.get(nextVersion.getWsdlUrl());

			if (wsdlHttpResponse.getSuccessfulUrl() == null) {
				throw new ProcessingException(Messages.getString("ServiceRegistryBean.retrieveWsdlFail", nextVersion.getWsdlUrl(), wsdlHttpResponse.getFailedUrls().get(nextVersion.getWsdlUrl()).getExplanation()));
			}

			if (200 != wsdlHttpResponse.getCode()) {
				throw new ProcessingException(Messages.getString("ServiceRegistryBean.retrieveWsdlFailNon200", nextVersion.getWsdlUrl(), wsdlHttpResponse.getCode()));
			}

			ourLog.info("Loaded WSDL ({} bytes) in {}ms, going to parse", wsdlHttpResponse.getBody().length(), wsdlHttpResponse.getResponseTime());

			Document wsdlDocument = XMLUtils.parse(wsdlHttpResponse.getBody());
			NodeList portTypeElements = wsdlDocument.getElementsByTagNameNS(Constants.NS_WSDL, "portType");

			if (portTypeElements.getLength() == 0) {
				throw new ProcessingException("WSDL \"" + nextVersion.getWsdlUrl() + "\" has no PortType defined");
			}

			if (portTypeElements.getLength() > 1) {
				throw new ProcessingException("WSDL \"" + nextVersion.getWsdlUrl() + "\" has more than one PortType defined (this is not currently supported by ServiceProxy)");
			}

			/*
			 * Process Schema Imports
			 */
			NodeList typesList = wsdlDocument.getElementsByTagNameNS(Constants.NS_WSDL, "types");
			for (int typesIdx = 0; typesIdx < typesList.getLength(); typesIdx++) {
				Element typesElem = (Element) typesList.item(typesIdx);
				NodeList schemaList = typesElem.getElementsByTagNameNS(Constants.NS_WSDL, "schema");
				for (int schemaIdx = 0; schemaIdx < schemaList.getLength(); schemaIdx++) {
					Element schemaElem = (Element) schemaList.item(schemaIdx);
					NodeList importList = schemaElem.getElementsByTagNameNS(Constants.NS_WSDL, "import");
					for (int importIdx = 0; importIdx < importList.getLength(); importIdx++) {
						Element importElem = (Element) importList.item(importIdx);

						String importLocation = importElem.getAttribute("schemaLocation");
						if (StringUtils.isBlank(importLocation)) {
							continue;
						}

						String norm;
						try {
							norm = UrlUtil.calculateRelativeUrl(nextVersion.getWsdlUrl(), importLocation);
						} catch (URISyntaxException e) {
							throw new ProcessingRuntimeException("Invalid URL found within WSDL: " + e.getMessage(), e);
						}
						ourLog.info("Retrieving Import - Parent: {} - Location: {} - Normalized: {}", new Object[] { nextVersion.getWsdlUrl(), importLocation });

						HttpResponseBean schemaHttpResponse = mySvcHttpClient.get(norm);

						if (schemaHttpResponse.getSuccessfulUrl() == null) {
							throw new ProcessingException(Messages.getString("ServiceRegistryBean.retrieveSchemaFail", nextVersion.getWsdlUrl(), wsdlHttpResponse.getFailedUrls().get(nextVersion.getWsdlUrl()).getExplanation()));
						}

						if (200 != schemaHttpResponse.getCode()) {
							throw new ProcessingException(Messages.getString("ServiceRegistryBean.retrieveSchemaFailNon200", nextVersion.getWsdlUrl(), wsdlHttpResponse.getCode()));
						}

						pVersion.addResource(importLocation, schemaHttpResponse.getBody());

					}
				}
			}

			/*
			 * Process Operations
			 */

			Element portType = (Element) portTypeElements.item(0);
			NodeList operations = portType.getElementsByTagNameNS(Constants.NS_WSDL, "operation");

			if (operations.getLength() == 0) {
				throw new ProcessingException("WSDL \"" + nextVersion.getWsdlUrl() + "\" has no operations defined");
			}

			ourLog.info("Parsed WSDL and found {} methods", operations.getLength());

			List<String> operationNames = Lists.newArrayList();
			for (int i = 0; i < operations.getLength(); i++) {

				Element nextOperationElem = (Element) operations.item(i);
				String opName = nextOperationElem.getAttribute("name");
				ourLog.info(" * Found operation: {}", opName);

				PersServiceVersionMethod method = pVersion.getOrCreateAndAddMethodWithName(opName);
				pVersion.putMethodAtIndex(method, operationNames.size());

				operationNames.add(opName);
			}

			pVersion.retainOnlyMethodsWithNames(operationNames);

			mySvcPersistence.saveServiceVersion(pVersion);
		}
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@Override
	public void loadServiceDefinition(String theXmlContents) throws ProcessingException {

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
			loadServiceDefinition(nextService);
		}

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
				for (PersServiceVersionSoap11 nextVersion : nextService.getVersions()) {
					String nextProxyPath = nextVersion.getProxyPath();
					if (pathToServiceVersions.containsKey(nextProxyPath)) {
						ourLog.warn("Service version {} created duplicate proxy path, so it will be ignored!", nextVersion.getPid());
						continue;
					}
					pathToServiceVersions.put(nextProxyPath, nextVersion);
				}
			}

		}

		Map<String, PersServiceUser> serviceUserMap = new HashMap<String, PersServiceUser>();
		Collection<PersServiceUser> users = mySvcPersistence.getAllServiceUsers();
		for (PersServiceUser nextUser : users) {
			serviceUserMap.put(nextUser.getUsername(), nextUser);
			nextUser.loadAllAssociations();
		}

		ourLog.info("Done loading service registry from database");

		synchronized (ourRegistryLock) {
			ourDomains = domainMap;
			ourServiceUsers = serviceUserMap;
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
