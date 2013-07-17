package net.svcret.ejb.ejb.soap;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import net.svcret.ejb.Messages;
import net.svcret.ejb.api.HttpResponseBean;
import net.svcret.ejb.api.IConfigService;
import net.svcret.ejb.api.ICredentialGrabber;
import net.svcret.ejb.api.IHttpClient;
import net.svcret.ejb.api.IServiceInvokerSoap11;
import net.svcret.ejb.api.InvocationResponseResultsBean;
import net.svcret.ejb.api.InvocationResultsBean;
import net.svcret.ejb.api.RequestType;
import net.svcret.ejb.api.ResponseTypeEnum;
import net.svcret.ejb.ex.InternalErrorException;
import net.svcret.ejb.ex.ProcessingException;
import net.svcret.ejb.ex.ProcessingRuntimeException;
import net.svcret.ejb.ex.UnknownRequestException;
import net.svcret.ejb.model.entity.PersBaseClientAuth;
import net.svcret.ejb.model.entity.PersBaseServerAuth;
import net.svcret.ejb.model.entity.PersServiceVersionMethod;
import net.svcret.ejb.model.entity.PersServiceVersionResource;
import net.svcret.ejb.model.entity.PersServiceVersionUrl;
import net.svcret.ejb.model.entity.soap.PersServiceVersionSoap11;
import net.svcret.ejb.util.UrlUtil;
import net.svcret.ejb.util.Validate;
import net.svcret.ejb.util.XMLUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@Stateless()
public class Soap11ServiceInvoker implements IServiceInvokerSoap11 {

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(Soap11ServiceInvoker.class);

	@EJB
	private IHttpClient myHttpClient;

	private Soap11ResponseValidator myInvocationResultsBean;
	private static XMLEventFactory ourEventFactory;
	private static XMLInputFactory ourXmlInputFactory;
	private static final Set<String> ourValidContentTypes = new HashSet<String>();

	static {
		ourValidContentTypes.add("text/xml");
		ourValidContentTypes.add("application/soap+xml");
	}

	private void doHandleGet(InvocationResultsBean theResults, PersServiceVersionSoap11 theServiceDefinition, String thePath, String theQuery) throws UnknownRequestException, ProcessingException {

		if (theQuery.toLowerCase().equals("?wsdl")) {
			doHandleGetWsdl(theResults, theServiceDefinition, thePath);
		} else if (theQuery.startsWith("?xsd")) {
			doHandleGetXsd(theResults, theServiceDefinition, thePath, theQuery);
		} else {
			throw new UnknownRequestException("Unknown service request: " + thePath + theQuery);
		}

	}

	private void doHandleGetWsdl(InvocationResultsBean theResults, PersServiceVersionSoap11 theServiceDefinition, String thePath) throws DOMException, ProcessingException {
		String wsdlUrl = theServiceDefinition.getWsdlUrl();

		PersServiceVersionResource resource = theServiceDefinition.getResourceForUri(wsdlUrl);
		if (resource == null || StringUtils.isBlank(resource.getResourceText())) {
			throw new InternalErrorException("Service Version " + theServiceDefinition.getPid() + " does not have a resource for URL: " + wsdlUrl);
		}
		String wsdlResourceText = resource.getResourceText();

		Document wsdlDocument = XMLUtils.parse(wsdlResourceText);

		/*
		 * Process Schema Imports
		 */
		NodeList typesList = wsdlDocument.getElementsByTagNameNS(Constants.NS_WSDL, "types");
		for (int typesIdx = 0; typesIdx < typesList.getLength(); typesIdx++) {
			Element typesElem = (Element) typesList.item(typesIdx);
			NodeList schemaList = typesElem.getElementsByTagNameNS(Constants.NS_XSD, "schema");
			for (int schemaIdx = 0; schemaIdx < schemaList.getLength(); schemaIdx++) {
				Element schemaElem = (Element) schemaList.item(schemaIdx);
				NodeList importList = schemaElem.getElementsByTagNameNS(Constants.NS_XSD, "import");
				for (int importIdx = 0; importIdx < importList.getLength(); importIdx++) {
					Element importElem = (Element) importList.item(importIdx);

					String importLocation = importElem.getAttribute("schemaLocation");
					if (StringUtils.isNotBlank(importLocation)) {
						PersServiceVersionResource nResource = theServiceDefinition.getResourceForUri(importLocation);
						String pathBase = urlEncode(getUrlBase() + thePath);
						importElem.setAttribute("schemaLocation", (pathBase + "?xsd&xsdnum=" + nResource.getPid()));
					}
				}
			}
		}

		/*
		 * Process service addresses
		 */

		NodeList serviceList = wsdlDocument.getElementsByTagNameNS(Constants.NS_WSDL, "service");
		for (int serviceIdx = 0; serviceIdx < serviceList.getLength(); serviceIdx++) {
			Element typesElem = (Element) serviceList.item(serviceIdx);
			NodeList portList = typesElem.getElementsByTagNameNS(Constants.NS_WSDL, "port");
			for (int portIdx = 0; portIdx < portList.getLength(); portIdx++) {
				Element schemaElem = (Element) portList.item(portIdx);
				NodeList addressList = schemaElem.getElementsByTagNameNS(Constants.NS_WSDLSOAP, "address");
				for (int addressIdx = 0; addressIdx < addressList.getLength(); addressIdx++) {
					Element importElem = (Element) addressList.item(addressIdx);
					String location = importElem.getAttribute("location");
					if (StringUtils.isNotBlank(location)) {
						String pathBase = urlEncode(getUrlBase() + thePath);
						importElem.setAttribute("location", pathBase);
					}
				}
			}
		}

		ourLog.debug("Writing WSDL for ServiceVersion[{}]", theServiceDefinition.getPid());
		StringWriter writer = new StringWriter();
		XMLUtils.serialize(wsdlDocument, true, writer);

		String resourceText = writer.toString();
		String resourceUrl = wsdlUrl;
		String contentType = Constants.CONTENT_TYPE_XML;
		Map<String, List<String>> headers = new HashMap<String, List<String>>();
		theResults.setResultStaticResource(resourceUrl, resource, resourceText, contentType, headers);

	}

	private String urlEncode(String theString) {
		return theString.replace(" ", "%20");
	}

	@EJB
	private IConfigService myConfigService;

	private String getUrlBase() throws ProcessingException {
		return myConfigService.getConfig().getProxyUrlBases().iterator().next().getUrlBase();
	}

	private void doHandleGetXsd(InvocationResultsBean theResults, PersServiceVersionSoap11 theServiceDefinition, String thePath, String theQuery) throws UnknownRequestException {
		StringTokenizer tok = new StringTokenizer(theQuery, "&");
		String xsdNumString = null;
		while (tok.hasMoreElements()) {
			String nextToken = tok.nextToken();
			if (nextToken.startsWith("xsdnum=")) {
				xsdNumString = nextToken.substring(7);
				break;
			}
		}

		// TODO: handle xsd imports

		if (xsdNumString == null) {
			throw new UnknownRequestException("Invalid XSD query, no 'xsdnum' parameter found");
		}

		if (!Validate.isNotBlankSimpleInteger(xsdNumString)) {
			throw new UnknownRequestException("Invalid XSD query, invalid 'xsdnum' parameter found: " + xsdNumString);
		}

		long xsdNum = Long.parseLong(xsdNumString);
		PersServiceVersionResource res = theServiceDefinition.getResourceWithPid(xsdNum);

		if (res == null) {
			throw new UnknownRequestException(thePath + theQuery, "Invalid XSD query, invalid 'xsdnum' parameter found: " + xsdNumString);
		}

		String resourceText = res.getResourceText();
		String resourceUrl = res.getResourceUrl();
		String contentType = "text/xml";
		Map<String, List<String>> headers = new HashMap<String, List<String>>();
		theResults.setResultStaticResource(resourceUrl, res, resourceText, contentType, headers);

	}

	private void doHandlePost(InvocationResultsBean theResults, PersServiceVersionSoap11 theServiceDefinition, Reader theReader) throws InternalErrorException, ProcessingException, UnknownRequestException {
		// TODO: should we check for SOAPAction header?

		List<PersBaseClientAuth<?>> clientAuths = theServiceDefinition.getClientAuths();
		List<PersBaseServerAuth<?, ?>> serverAuths = theServiceDefinition.getServerAuths();
		RequestPipeline pipeline = new RequestPipeline(serverAuths, clientAuths);

		StringWriter requestBuffer = new StringWriter();
		pipeline.process(theReader, requestBuffer);

		List<ICredentialGrabber> credentialGrabbers = pipeline.getCredentialGrabbers();
		for (ICredentialGrabber nextCredentialGrabber : credentialGrabbers) {
			theResults.addCredentials(nextCredentialGrabber);
		}

		String methodName = pipeline.getMethodName();
		if (methodName == null) {
			throw new UnknownRequestException("No method found in request message for Service \"" + theServiceDefinition.getService().getServiceName() + "\"");
		}

		PersServiceVersionMethod method = theServiceDefinition.getMethodForRootElementName(methodName);
		if (method == null) {
			throw new UnknownRequestException("Unknown method \"" + methodName + "\" for Service \"" + theServiceDefinition.getService().getServiceName() + "\"");
		}

		Map<String, String> headers = Maps.newHashMap();
		headers.put("SOAPAction", "");

		String request = requestBuffer.toString();
		String contentType = "text/xml";

		theResults.setResultMethod(method, request, contentType, headers);
	}

	/**
	 * {@inheritDoc}
	 */
	@TransactionAttribute(TransactionAttributeType.NEVER)
	@Override
	public InvocationResultsBean processInvocation(PersServiceVersionSoap11 theServiceDefinition, RequestType theRequestType, String thePath, String theQuery, Reader theReader) throws ProcessingException, UnknownRequestException {
		InvocationResultsBean retVal = new InvocationResultsBean();

		switch (theRequestType) {
		case GET:
			doHandleGet(retVal, theServiceDefinition, thePath, theQuery);
			break;
		case POST:
			doHandlePost(retVal, theServiceDefinition, theReader);
			break;
		default:
			throw new InternalErrorException("Unknown request type: " + theRequestType);
		}

		return retVal;
	}

	@Override
	public InvocationResponseResultsBean processInvocationResponse(HttpResponseBean theResponse) throws ProcessingException {
		InvocationResponseResultsBean retVal = new InvocationResponseResultsBean();
		retVal.setResponseHeaders(theResponse.getHeaders());
		
		String contentType = theResponse.getContentType();
		if (StringUtils.isBlank(contentType)) {
			retVal.setResponseType(ResponseTypeEnum.FAIL);
			retVal.setResponseStatusMessage("No content type in response");
			return retVal;
		}

		if (!ourValidContentTypes.contains(contentType)) {
			retVal.setResponseType(ResponseTypeEnum.FAIL);
			retVal.setResponseStatusMessage("Content type in response is not XML: " + contentType);
			return retVal;
		}

		XMLEventReader streamReader;
		initEventFactories();
		try {
			streamReader = ourXmlInputFactory.createXMLEventReader(new StringReader(theResponse.getBody()));
		} catch (XMLStreamException e) {
			throw new InternalErrorException(e);
		} catch (FactoryConfigurationError e) {
			throw new InternalErrorException(e);
		}

		ResponsePositionEnum pos = ResponsePositionEnum.NONE;
		;
		while (streamReader.hasNext()) {

			XMLEvent next;
			try {
				next = streamReader.nextEvent();

				if (next.isStartElement()) {
					switch (pos) {
					case NONE:
					default:
						if (Constants.SOAPENV11_ENVELOPE_QNAME.equals(next.asStartElement().getName())) {
							pos = ResponsePositionEnum.IN_DOCUMENT;
						} else {
							retVal.setResponseType(ResponseTypeEnum.FAIL);
							retVal.setResponseStatusMessage("Response is not a SOAP document. First element is: " + next.asStartElement().getName());
						}
						break;
					case IN_DOCUMENT:
						if (Constants.SOAPENV11_BODY_QNAME.equals(next.asStartElement().getName())) {
							pos = ResponsePositionEnum.IN_BODY;
						}
						break;
					case IN_BODY:
						if (Constants.SOAPENV11_FAULT_QNAME.equals(next.asStartElement().getName())) {
							pos = ResponsePositionEnum.IN_FAULT;
							retVal.setResponseType(ResponseTypeEnum.FAULT);
							retVal.setResponseBody(theResponse.getBody());
							retVal.setResponseContentType(theResponse.getContentType());
						} else {
							retVal.setResponseType(ResponseTypeEnum.SUCCESS);
							retVal.setResponseBody(theResponse.getBody());
							retVal.setResponseContentType(theResponse.getContentType());
							// Don't need any headers -
							// retVal.setResponseHeaders(theResponse.getHeaders());
							return retVal;
						}
						break;
					case IN_FAULT:
						if (Constants.TAG_SOAPENV11_FAULTCODE.equals(next.asStartElement().getName().getLocalPart())) {
							StringBuilder b = new StringBuilder();
							while (true) {
								if (next.isEndElement() && Constants.TAG_SOAPENV11_FAULTCODE.equals(next.asEndElement().getName().getLocalPart())) {
									break;
								}
								if (next.isCharacters()) {
									b.append(next.asCharacters().getData());
								}
								next = streamReader.nextEvent();
							}
							retVal.setResponseFaultCode(b.toString());
						} else if (Constants.TAG_SOAPENV11_FAULTSTRING.equals(next.asStartElement().getName().getLocalPart())) {
							while (true) {
								if (next.isEndElement() && Constants.TAG_SOAPENV11_FAULTSTRING.equals(next.asEndElement().getName().getLocalPart())) {
									break;
								}
								if (next.isCharacters()) {
									retVal.setResponseFaultDescription(next.asCharacters().getData());
								}
								next = streamReader.nextEvent();
							}
						}
					}
				} // if start

			} catch (XMLStreamException e) {
				throw new ProcessingException("Unable to process XML response. Error was: " + e.getMessage(), e);
			}

		}

		return retVal;
	}

	private static enum ResponsePositionEnum {
		NONE, IN_DOCUMENT, IN_BODY, IN_FAULT
	}

	private static void initEventFactories() throws FactoryConfigurationError {
		synchronized (Soap11ServiceInvoker.class) {
			if (ourEventFactory == null) {
				ourXmlInputFactory = XMLInputFactory.newInstance();
				ourEventFactory = XMLEventFactory.newInstance();
			}
		}
	}

	@Override
	public Soap11ResponseValidator provideInvocationResponseValidator() {
		return myInvocationResultsBean;
	}

	public Soap11ServiceInvoker() {
		myInvocationResultsBean = new Soap11ResponseValidator();
	}

	@Override
	public PersServiceVersionSoap11 introspectServiceFromUrl(String theUrl) throws ProcessingException {
		PersServiceVersionSoap11 retVal = new PersServiceVersionSoap11();

		// TODO: wrap all getElementsBy... calls to remove
		// non elements in return list, since there can be
		// text and we want to ignore it

		ourLog.info("Loading WSDL URL: {}", theUrl);
		HttpResponseBean wsdlHttpResponse;
		try {
			wsdlHttpResponse = myHttpClient.get(theUrl);
		} catch (ClientProtocolException e1) {
			throw new ProcessingException(Messages.getString("Soap11ServiceInvoker.retrieveWsdlFail", theUrl, e1.getMessage()));
		} catch (IOException e1) {
			throw new ProcessingException(Messages.getString("Soap11ServiceInvoker.retrieveWsdlFail", theUrl, e1.getMessage()));
		}

		ourLog.info("Loaded WSDL ({} bytes) in {}ms, going to parse", wsdlHttpResponse.getBody().length(), wsdlHttpResponse.getResponseTime());

		Document wsdlDocument = XMLUtils.parse(wsdlHttpResponse.getBody());
		NodeList portTypeElements = wsdlDocument.getElementsByTagNameNS(Constants.NS_WSDL, "portType");

		if (portTypeElements.getLength() == 0) {
			throw new ProcessingException("WSDL \"" + theUrl + "\" has no PortType defined");
		}

		if (portTypeElements.getLength() > 1) {
			throw new ProcessingException("WSDL \"" + theUrl + "\" has more than one PortType defined (this is not currently supported by ServiceProxy)");
		}

		retVal.setWsdlUrl(theUrl);
		String contentType = wsdlHttpResponse.getContentType();
		PersServiceVersionResource wsdlRes = retVal.addResource(theUrl, contentType, wsdlHttpResponse.getBody());
		wsdlRes.setResourceContentType(wsdlHttpResponse.getContentType());

		/*
		 * Process Schema Imports
		 */
		NodeList typesList = wsdlDocument.getElementsByTagNameNS(Constants.NS_WSDL, "types");
		for (int typesIdx = 0; typesIdx < typesList.getLength(); typesIdx++) {
			Element typesElem = (Element) typesList.item(typesIdx);
			NodeList schemaList = typesElem.getElementsByTagNameNS(Constants.NS_XSD, "schema");
			for (int schemaIdx = 0; schemaIdx < schemaList.getLength(); schemaIdx++) {
				Element schemaElem = (Element) schemaList.item(schemaIdx);
				NodeList importList = schemaElem.getElementsByTagNameNS(Constants.NS_XSD, "import");
				for (int importIdx = 0; importIdx < importList.getLength(); importIdx++) {
					Element importElem = (Element) importList.item(importIdx);

					String importLocation = importElem.getAttribute("schemaLocation");
					if (StringUtils.isBlank(importLocation)) {
						continue;
					}

					String norm;
					try {
						norm = UrlUtil.calculateRelativeUrl(theUrl, importLocation);
					} catch (URISyntaxException e) {
						throw new ProcessingRuntimeException("Invalid URL found within WSDL: " + e.getMessage(), e);
					}
					ourLog.info("Retrieving Import - Parent: {} - Location: {} - Normalized: {}", new Object[] { theUrl, importLocation, norm });

					HttpResponseBean schemaHttpResponse;
					try {
						schemaHttpResponse = myHttpClient.get(norm);
					} catch (ClientProtocolException e1) {
						throw new ProcessingException(Messages.getString("Soap11ServiceInvoker.retrieveWsdlFail", theUrl, e1.getMessage()));
					} catch (IOException e1) {
						throw new ProcessingException(Messages.getString("Soap11ServiceInvoker.retrieveWsdlFail", theUrl, e1.getMessage()));
					}

					contentType = schemaHttpResponse.getContentType();
					PersServiceVersionResource res = retVal.addResource(importLocation, contentType, schemaHttpResponse.getBody());
					res.setResourceContentType(schemaHttpResponse.getContentType());

				}
			}
		}

		/*
		 * Process Operations
		 */

		Element portType = (Element) portTypeElements.item(0);
		NodeList operations = portType.getElementsByTagNameNS(Constants.NS_WSDL, "operation");

		if (operations.getLength() == 0) {
			throw new ProcessingException("WSDL \"" + theUrl + "\" has no operations defined");
		}

		ourLog.info("Parsed WSDL and found {} methods", operations.getLength());

		List<String> operationNames = Lists.newArrayList();
		for (int i = 0; i < operations.getLength(); i++) {

			Element nextOperationElem = (Element) operations.item(i);
			String opName = nextOperationElem.getAttribute("name");
			ourLog.info(" * Found operation: {}", opName);

			String rootElementNs = null;
			String rootElementName = null;
			
			for (int j = 0; j < nextOperationElem.getChildNodes().getLength(); j++) {
				if (!(nextOperationElem.getChildNodes().item(j) instanceof Element)) {
					continue;
				}
				Element nextOpChild = (Element) nextOperationElem.getChildNodes().item(j);
				
				if (nextOpChild.getLocalName().equals("input")) {
					String[] messageParts = nextOpChild.getAttribute("message").split("\\:");
					String msgNs = messageParts[0];
					String msgName = messageParts[1];
					ourLog.info("Found message reference {} : {}", msgNs, msgName);
					
					NodeList messageList = wsdlDocument.getElementsByTagNameNS(Constants.NS_WSDL, "message");
					for (int msgCount = 0; msgCount < messageList.getLength(); msgCount++) {
						if (!(messageList.item(msgCount) instanceof Element)) {
							continue;
						}
						
						Element nextMessage = (Element) messageList.item(msgCount);
						if (msgName.equals(nextMessage.getAttribute("name"))) {
							
							ourLog.info("Found corresponding message at index {}", msgCount);
							
							for (int partCount = 0; partCount < nextMessage.getChildNodes().getLength();partCount++) {
								if (!(nextMessage.getChildNodes().item(partCount) instanceof Element)) {
									continue;
								}
								
								Element partElem = (Element) nextMessage.getChildNodes().item(partCount);
								if (partElem.getLocalName().equals("part")) {
									
									String element = partElem.getAttribute("element");
									String[] elementParts = element.split("\\:");
									
									rootElementNs = partElem.lookupNamespaceURI(elementParts[0]);
									rootElementName = elementParts[1];
									ourLog.info("Root element is " + rootElementNs+":"+rootElementName);
								}
								
							}
							
						}
						
					}
					
					
				}
				
			}
			
			PersServiceVersionMethod method = retVal.getOrCreateAndAddMethodWithName(opName);
			
			method.setRootElements(rootElementNs + ":" + rootElementName);
			
			retVal.putMethodAtIndex(method, operationNames.size());

			operationNames.add(opName);
		}

		retVal.retainOnlyMethodsWithNames(operationNames);

		/*
		 * Process Ports (bound implementation URLs)
		 */

		int idx = 0;
		NodeList servicesList = wsdlDocument.getElementsByTagNameNS(Constants.NS_WSDL, "service");
		for (int svcIdx = 0; svcIdx < servicesList.getLength(); svcIdx++) {
			Element serviceElem = (Element) servicesList.item(svcIdx);
			NodeList portList = serviceElem.getElementsByTagNameNS(Constants.NS_WSDL, "port");
			for (int portIdx = 0; portIdx < portList.getLength(); portIdx++) {
				Element portElem = (Element) portList.item(portIdx);
				String portName = portElem.getAttribute("name");
				if (StringUtils.isBlank(portName)) {
					portName = "port" + idx++;
				}

				NodeList portChildren = portElem.getChildNodes();
				for (int portChildIdx = 0; portChildIdx < portChildren.getLength(); portChildIdx++) {
					Node nextChildNode = portChildren.item(portChildIdx);
					if (!(nextChildNode instanceof Element)) {
						continue;
					}
					Element nextChild = (Element) nextChildNode;
					if (nextChild.getLocalName().equals("address") && nextChild.getNamespaceURI().equals(Constants.NS_WSDLSOAP)) {
						String locationAttr = nextChild.getAttribute("location");

						PersServiceVersionUrl persUrl = new PersServiceVersionUrl();
						persUrl.setUrlId(portName);
						persUrl.setUrl(locationAttr);
						persUrl.setNewlyCreated(true);

						retVal.addUrl(persUrl);

					}
				}

			}
		}

		return retVal;
	}

	/**
	 * UNIT TESTS ONLY
	 */
	public void setHttpClient(IHttpClient theHttpClient) {
		assert myHttpClient == null;
		myHttpClient = theHttpClient;
	}

	/**
	 * UNIT TESTS ONLY
	 */
	public void setConfigService(IConfigService theMock) {
		myConfigService = theMock;
	}

}
