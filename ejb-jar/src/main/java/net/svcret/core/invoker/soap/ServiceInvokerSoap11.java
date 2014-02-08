package net.svcret.core.invoker.soap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import net.svcret.admin.api.ProcessingException;
import net.svcret.admin.shared.enm.ResponseTypeEnum;
import net.svcret.admin.shared.util.Validate;
import net.svcret.core.Messages;
import net.svcret.core.api.IConfigService;
import net.svcret.core.api.ICredentialGrabber;
import net.svcret.core.api.IHttpClient;
import net.svcret.core.api.SrBeanIncomingRequest;
import net.svcret.core.api.SrBeanIncomingResponse;
import net.svcret.core.api.SrBeanProcessedRequest;
import net.svcret.core.api.SrBeanProcessedResponse;
import net.svcret.core.ejb.HttpClientBean.ClientConfigException;
import net.svcret.core.ex.InvalidRequestException;
import net.svcret.core.ex.InvocationFailedDueToInternalErrorException;
import net.svcret.core.ex.InvocationRequestFailedException;
import net.svcret.core.ex.InvocationResponseFailedException;
import net.svcret.core.ex.ProcessingRuntimeException;
import net.svcret.core.ex.InvalidRequestException.IssueEnum;
import net.svcret.core.invoker.BaseServiceInvoker;
import net.svcret.core.model.entity.BasePersServiceVersion;
import net.svcret.core.model.entity.PersBaseClientAuth;
import net.svcret.core.model.entity.PersBaseServerAuth;
import net.svcret.core.model.entity.PersHttpClientConfig;
import net.svcret.core.model.entity.PersMethod;
import net.svcret.core.model.entity.PersServiceVersionResource;
import net.svcret.core.model.entity.PersServiceVersionUrl;
import net.svcret.core.model.entity.soap.PersServiceVersionSoap11;
import net.svcret.core.util.UrlUtil;
import net.svcret.core.util.XMLUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@Service
public class ServiceInvokerSoap11 extends BaseServiceInvoker implements IServiceInvokerSoap11 {

	private static XMLEventFactory ourEventFactory;

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(ServiceInvokerSoap11.class);

	private static final Set<String> ourValidContentTypes = new HashSet<String>();
	private static XMLInputFactory ourXmlInputFactory;
	
	static {
		ourValidContentTypes.add("text/xml");
		ourValidContentTypes.add("application/soap+xml");
	}
	
	@Autowired
	private IConfigService myConfigService;

	@Autowired
	private IHttpClient myHttpClient;

	private BaseResponseValidator myInvocationResultsBean;

	@Override
	public Map<String, List<String>> createBackingRequestHeadersForMethodInvocation(BasePersServiceVersion theServiceDefinition, Map<String, List<String>> theIncomingHeaders) {
		Map<String, List<String>> retVal = super.createBackingRequestHeadersForMethodInvocation(theServiceDefinition, theIncomingHeaders);
		retVal.put("SOAPAction", theIncomingHeaders.get("SOAPAction"));
		return retVal;
	}

	public ServiceInvokerSoap11() {
		myInvocationResultsBean = new Soap11ResponseValidator();
	}

	@Override
	public byte[] createWsdlBundle(PersServiceVersionSoap11 theSvcVer) throws ProcessingException {
		try {
			final String filenamePrefix = (theSvcVer.getService().getServiceId() + "_" + theSvcVer.getVersionId()).replace(' ', '_');

			final Map<String, String> xsdResources = new HashMap<String, String>();
			ICreatesImportUrl urlCreator = new ICreatesImportUrl() {
				@Override
				public String createImportUrlForSchemaImport(PersServiceVersionResource theResource) throws InvocationFailedDueToInternalErrorException {
					String fileName = filenamePrefix + "_schema_" + theResource.getPid() + ".xsd";
					String resourceText = theResource.getResourceText();
					if (resourceText == null) {
						throw new InvocationFailedDueToInternalErrorException("No content found for XSD resource with PID: " + theResource.getPid() + " and resource URL: "
								+ theResource.getResourceUrl());
					}

					xsdResources.put(fileName, resourceText);
					return fileName;
				}
			};
			String wsdl = renderWsdl(theSvcVer, theSvcVer.determineUsableProxyPath(), urlCreator);

			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ZipOutputStream zos = new ZipOutputStream(bos);

			zos.putNextEntry(new ZipEntry(filenamePrefix + ".wsdl"));
			zos.write(wsdl.getBytes("UTF-8"));

			for (String nextFileName : new TreeSet<String>(xsdResources.keySet())) {
				String nextFile = xsdResources.get(nextFileName);

				zos.putNextEntry(new ZipEntry(nextFileName));
				zos.write(nextFile.getBytes("UTF-8"));
				zos.closeEntry();
				zos.flush();
			}

			zos.close();
			bos.close();

			return bos.toByteArray();
		} catch (Exception e) {
			throw new ProcessingException(e);
		}
	}

	private void doHandleGet(SrBeanIncomingRequest theRequest, SrBeanProcessedRequest theResults, PersServiceVersionSoap11 theServiceDefinition, String theQuery) throws InvalidRequestException,
			InvocationFailedDueToInternalErrorException {

		if (theQuery.toLowerCase().equals("?wsdl")) {
			doHandleGetWsdl(theRequest, theResults, theServiceDefinition);
		} else if (theQuery.startsWith("?xsd")) {
			doHandleGetXsd(theRequest, theResults, theServiceDefinition);
		} else {
			throwUnsupportedActionException(theRequest);
		}

	}

	private void throwUnsupportedActionException(SrBeanIncomingRequest theRequest) throws InvalidRequestException {
		throw new InvalidRequestException(IssueEnum.UNSUPPORTED_ACTION, theRequest.getRequestType().name(), "Requests to SOAP services must use HTTP POST. Note that HTTP GET is not supported, except to obtain service WSDL and supporting XSDs.");
	}

	private void doHandleGetWsdl(final SrBeanIncomingRequest theRequest, SrBeanProcessedRequest theResults, PersServiceVersionSoap11 theServiceDefinition) throws InvocationFailedDueToInternalErrorException {
		final String pathBase = toPathBase(theRequest);
		
		ICreatesImportUrl urlCreator = new ICreatesImportUrl() {
			@Override
			public String createImportUrlForSchemaImport(PersServiceVersionResource theResource) {
				return pathBase + "?xsd&xsdnum=" + theResource.getPid();
			}
		};

		String resourceText = renderWsdl(theServiceDefinition, pathBase, urlCreator);
		String contentType = Constants.CONTENT_TYPE_XML;
		Map<String, List<String>> headers = new HashMap<String, List<String>>();
		String wsdlUrl = theServiceDefinition.getWsdlUrl();

		theResults.setResultStaticResource(wsdlUrl, theServiceDefinition.getResourceForUri(wsdlUrl), resourceText, contentType, headers);

	}

	private String toPathBase(final SrBeanIncomingRequest theRequest) {
		String base = theRequest.getBase();
		String contextPath = theRequest.getContextPath();
		final String pathBase = base + contextPath + urlEncode(theRequest.getPath());
		return pathBase;
	}

	private void doHandleGetXsd(SrBeanIncomingRequest theRequest, SrBeanProcessedRequest theResults, PersServiceVersionSoap11 theServiceDefinition) throws InvalidRequestException, InvocationFailedDueToInternalErrorException {
		Validate.notNull(theRequest);
		
		StringTokenizer tok = new StringTokenizer(theRequest.getQuery(), "&");
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
			throw new InvalidRequestException(IssueEnum.INVALID_QUERY_PARAMETERS, theRequest.getQuery(), "No 'xsdnum' parameter found");
		}

		if (!Validate.isNotBlankSimpleInteger(xsdNumString)) {
			throw new InvalidRequestException(IssueEnum.INVALID_QUERY_PARAMETERS, theRequest.getQuery(), "Invalid XSD query, invalid 'xsdnum' parameter found: " + xsdNumString);
		}

		long xsdNum = Long.parseLong(xsdNumString);
		PersServiceVersionResource res = theServiceDefinition.getResourceWithPid(xsdNum);

		if (res == null) {
			throw new InvalidRequestException(IssueEnum.INVALID_QUERY_PARAMETERS, theRequest.getQuery(), "Invalid XSD query, invalid 'xsdnum' parameter found: " + xsdNumString);
		}

		final String pathBase = toPathBase(theRequest);
		ICreatesImportUrl urlCreator = new ICreatesImportUrl() {
			@Override
			public String createImportUrlForSchemaImport(PersServiceVersionResource theResource) {
				return pathBase + "?xsd&xsdnum=" + theResource.getPid();
			}
		};

		String resourceText = renderXsd(theServiceDefinition, res, urlCreator);
		
		String resourceUrl = res.getResourceUrl();
		String contentType = "text/xml";
		Map<String, List<String>> headers = new HashMap<String, List<String>>();
		theResults.setResultStaticResource(resourceUrl, res, resourceText, contentType, headers);

	}

	private String renderXsd(PersServiceVersionSoap11 theServiceDefinition, PersServiceVersionResource theResource, ICreatesImportUrl theUrlCreator) throws InvocationFailedDueToInternalErrorException {
		String xsdResourceText = theResource.getResourceText();

		Document xsdDocument = XMLUtils.parse(xsdResourceText);

		/*
		 * Process Schema Imports
		 */
//		NodeList typesList = xsdDocument.getElementsByTagNameNS(Constants.NS_WSDL, "types");
//		for (int typesIdx = 0; typesIdx < typesList.getLength(); typesIdx++) {
//			Element typesElem = (Element) typesList.item(typesIdx);
//			NodeList schemaList = typesElem.getElementsByTagNameNS(Constants.NS_XSD, "schema");
//			for (int schemaIdx = 0; schemaIdx < schemaList.getLength(); schemaIdx++) {
//				Element schemaElem = (Element) schemaList.item(schemaIdx);
//				NodeList importList = schemaElem.getElementsByTagNameNS(Constants.NS_XSD, "import");
//				for (int importIdx = 0; importIdx < importList.getLength(); importIdx++) {
//					Element importElem = (Element) importList.item(importIdx);
//
//					String importLocation = importElem.getAttribute("schemaLocation");
//					if (StringUtils.isNotBlank(importLocation)) {
//						PersServiceVersionResource nResource = theServiceDefinition.getResourceForUri(importLocation);
//						importElem.setAttribute("schemaLocation", theUrlCreator.createImportUrlForSchemaImport(nResource));
//					}
//				}
//			}
//		}

		/*
		 * Process imports as root elements
		 */
		NodeList typesList = xsdDocument.getElementsByTagNameNS(Constants.NS_XSD, "import");
		for (int typesIdx = 0; typesIdx < typesList.getLength(); typesIdx++) {
			Element typesElem = (Element) typesList.item(typesIdx);
			String importLocation = typesElem.getAttribute("schemaLocation");
			if (StringUtils.isNotBlank(importLocation)) {
				PersServiceVersionResource nResource = theServiceDefinition.getResourceForUri(importLocation);
				if (nResource == null) {
					throw new InvocationFailedDueToInternalErrorException("Service definition has no resource with URI: " + importLocation);
				}
				typesElem.setAttribute("schemaLocation", theUrlCreator.createImportUrlForSchemaImport(nResource));
			}
		}
		
		ourLog.debug("Writing XSD for ServiceVersion[{}]", theServiceDefinition.getPid());
		StringWriter writer = new StringWriter();
		XMLUtils.serialize(xsdDocument, true, writer);

		String resourceText = writer.toString();
		return resourceText;

	}

	private void doHandlePost(SrBeanProcessedRequest theResults, PersServiceVersionSoap11 theServiceDefinition, Reader theReader) throws InvalidRequestException, InvocationRequestFailedException,
			InvocationFailedDueToInternalErrorException {
		// TODO: should we check for SOAPAction header?

		List<PersBaseClientAuth<?>> clientAuths = theServiceDefinition.getClientAuths();
		List<PersBaseServerAuth<?, ?>> serverAuths = theServiceDefinition.getServerAuths();
		RequestPipeline pipeline = new RequestPipeline(serverAuths, clientAuths);

		StringWriter requestBuffer = new StringWriter();

		try {
			pipeline.process(theReader, requestBuffer);
		} catch (XMLStreamException e) {
			throw new InvocationRequestFailedException(e);
		}

		Set<Entry<PersBaseServerAuth<?, ?>, ICredentialGrabber>> credentialGrabbers = pipeline.getCredentialGrabbers().entrySet();
		for (Entry<PersBaseServerAuth<?, ?>, ICredentialGrabber> next : credentialGrabbers) {
			theResults.addCredentials(next.getKey(), next.getValue());
		}

		String methodName = pipeline.getMethodName();
		if (methodName == null) {
			throw new InvalidRequestException(IssueEnum.INVALID_REQUEST_MESSAGE_BODY, "", "Request message does not appear to contain a valid SOAP request");
		}

		PersMethod method = theServiceDefinition.getMethodForRootElementName(methodName);
		if (method == null) {
			throw new InvalidRequestException(IssueEnum.UNKNOWN_METHOD, methodName, "Unknown method \"" + methodName + "\" for Service \"" + theServiceDefinition.getService().getServiceName() + "\"");
		}

		Map<String, String> headers = Maps.newHashMap();
		headers.put("SOAPAction", "");

		String request = requestBuffer.toString();
		String contentType = "text/xml";

		theResults.setResultMethod(method, request, contentType);
	}

	private Element findWsdlBindingInDocument(Document theWsdlDocument) throws ProcessingException {
		NodeList bindingList = theWsdlDocument.getElementsByTagNameNS(Constants.NS_WSDL, "binding");
		if (bindingList.getLength() == 0) {
			throw new ProcessingException("WSDL contains no bindings. This is not supported");
		}

		for (int i = 0; i < bindingList.getLength(); i++) {
			Element binding = (Element) bindingList.item(i);

			if (binding.getElementsByTagNameNS(Constants.NS_WSDLSOAP, "binding").getLength() == 1) {
				return binding;
			}
		}

		throw new ProcessingException("WSDL contains no bindings with SOAP 1.1 protocol. This is not supported");
	}

	private StyleEnum introspectBindingForStyle(Document theWsdlDocument) throws ProcessingException {
		/*
		 * Find binding
		 */

		Element binding = findWsdlBindingInDocument(theWsdlDocument);

		NodeList bindingChildren = binding.getElementsByTagNameNS(Constants.NS_WSDLSOAP, "binding");
		if (bindingChildren.getLength() != 1) {
			throw new ProcessingException("Binding must contain one child named binding, found " + bindingChildren.getLength());
		}
		Element binding2 = (Element) bindingChildren.item(0);

		String transport = binding2.getAttribute("transport");
		if (!"http://schemas.xmlsoap.org/soap/http".equals(transport)) {
			throw new ProcessingException("Binding contains unsupported transport value: " + transport);
		}

		String style = binding2.getAttribute("style");
		if (StringUtils.isBlank(style)) {
			style = "document"; // according to spec, this is the default
		}
		StyleEnum styleEnum;
		try {
			styleEnum = StyleEnum.valueOf(style.toUpperCase());
		} catch (Exception e) {
			throw new ProcessingException("Unknown binding style: " + style);
		}

		return styleEnum;
	}

	private void introspectPortType(String theUrl, PersServiceVersionSoap11 retVal, Document wsdlDocument, Element thePortType) throws ProcessingException {
		/*
		 * Process Operations
		 */

		NodeList operations = thePortType.getElementsByTagNameNS(Constants.NS_WSDL, "operation");

		if (operations.getLength() == 0) {
			throw new ProcessingException("WSDL \"" + theUrl + "\" has no operations defined");
		}

		ourLog.info("Parsed WSDL and found {} methods", operations.getLength());

		List<String> operationNames = Lists.newArrayList();
		for (int i = 0; i < operations.getLength(); i++) {

			Element nextOperationElem = (Element) operations.item(i);
			String opName = nextOperationElem.getAttribute("name");
			ourLog.info(" * Found operation: {}", opName);

			StyleEnum styleEnum = introspectBindingForStyle(wsdlDocument);

			PersMethod method = null;
			switch (styleEnum) {
			case DOCUMENT:
				method = introspectWsdlForDocumentOperation(retVal, wsdlDocument, nextOperationElem, opName);
				break;
			case RPC:
				method = introspectWsdlForRpcOperation(wsdlDocument, opName, retVal);
			}

			retVal.putMethodAtIndex(method, operationNames.size());

			operationNames.add(opName);
		}

		retVal.retainOnlyMethodsWithNamesAndUnknownMethod(operationNames);
	}

	@Override
	public PersServiceVersionSoap11 introspectServiceFromUrl(PersHttpClientConfig theHttpClientConfig, String theUrl) throws ProcessingException {
		PersServiceVersionSoap11 retVal = new PersServiceVersionSoap11();

		// TODO: wrap all getElementsBy... calls to remove
		// non elements in return list, since there can be
		// text and we want to ignore it

		ourLog.info("Loading WSDL URL: {}", theUrl);
		SrBeanIncomingResponse wsdlHttpResponse;
		try {
			wsdlHttpResponse = myHttpClient.getOneTime(theHttpClientConfig, theUrl);
		} catch (ClientProtocolException e1) {
			throw new ProcessingException(Messages.getString("Soap11ServiceInvoker.retrieveWsdlFail", theUrl, e1.getMessage()));
		} catch (IOException e1) {
			throw new ProcessingException(Messages.getString("Soap11ServiceInvoker.retrieveWsdlFail", theUrl, e1.getMessage()));
		} catch (ClientConfigException e1) {
			throw new ProcessingException(Messages.getString("Soap11ServiceInvoker.retrieveWsdlFail", theUrl, e1.getMessage()));
		}

		ourLog.info("Loaded WSDL ({} bytes) in {}ms, going to parse", wsdlHttpResponse.getBody().length(), wsdlHttpResponse.getResponseTime());

		String wsdlBody = wsdlHttpResponse.getBody();
		ourLog.debug("WSDL body is:\n{}", wsdlBody);
		
		Document wsdlDocument = XMLUtils.parse(wsdlBody);

		// if (portTypeElements.getLength() > 1) {
		// throw new ProcessingException("WSDL \"" + theUrl + "\" has more than one PortType defined (this is not currently supported by ServiceProxy)");
		// }

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

					SrBeanIncomingResponse schemaHttpResponse;
					try {
						schemaHttpResponse = myHttpClient.getOneTime(theHttpClientConfig, norm);
					} catch (ClientProtocolException e1) {
						throw new ProcessingException(Messages.getString("Soap11ServiceInvoker.retrieveWsdlFail", theUrl, e1.getMessage()));
					} catch (IOException e1) {
						throw new ProcessingException(Messages.getString("Soap11ServiceInvoker.retrieveWsdlFail", theUrl, e1.getMessage()));
					} catch (ClientConfigException e) {
						throw new ProcessingException(Messages.getString("Soap11ServiceInvoker.retrieveWsdlFail", theUrl, e.getMessage()));
					}

					contentType = schemaHttpResponse.getContentType();
					PersServiceVersionResource res = retVal.addResource(importLocation, contentType, schemaHttpResponse.getBody());
					res.setResourceContentType(schemaHttpResponse.getContentType());

				}
			}
		}

		/*
		 * Process Ports (bound implementation URLs)
		 */

		int idx = 0;
		NodeList servicesList = wsdlDocument.getElementsByTagNameNS(Constants.NS_WSDL, "service");

		String bindingNs = null;
		String bindingLocal = null;

		for (int svcIdx = 0; svcIdx < servicesList.getLength() && bindingNs == null; svcIdx++) {
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

						String bindingNode = portElem.getAttribute("binding");
						if (StringUtils.isNotBlank(bindingNode)) {
							int colonIndex = bindingNode.indexOf(':');
							String prefix = bindingNode.substring(0, colonIndex);
							bindingNs = portElem.getOwnerDocument().lookupNamespaceURI(prefix);
							bindingLocal = bindingNode.substring(colonIndex + 1);
						}

					}
				}

			}
		}

		if (bindingNs == null || bindingLocal == null) {
			throw new ProcessingException("WSDL \"" + theUrl + "\" does not appear to have a SOAP 1.1 binding");
		}

		// We found the service, now find the corresponding binding
		NodeList bindingElements = wsdlDocument.getElementsByTagNameNS(Constants.NS_WSDL, "binding");
		Element bindingElement = null;
		for (int i = 0; i < bindingElements.getLength(); i++) {
			Element nextBindingElement = (Element) bindingElements.item(i);
			if (bindingLocal.equals(nextBindingElement.getAttribute("name"))) {
				bindingElement = nextBindingElement;
			}
		}

		if (bindingElement == null) {
			throw new ProcessingException("WSDL \"" + theUrl + "\" does not appear to have a SOAP 1.1 binding named '" + bindingLocal + "'");
		}

		String bindingPortTypeFull = bindingElement.getAttribute("type");
		if (StringUtils.isBlank(bindingPortTypeFull)) {
			throw new ProcessingException("WSDL \"" + theUrl + "\" binding named '" + bindingLocal + "' does not have a type attribute");
		}

		String bindingPortType = bindingPortTypeFull.replaceFirst(".*\\:", "");
		NodeList portTypeElements = wsdlDocument.getElementsByTagNameNS(Constants.NS_WSDL, "portType");

		if (portTypeElements.getLength() == 0) {
			throw new ProcessingException("WSDL \"" + theUrl + "\" has no PortType defined");
		}

		Element portType = null;
		for (int i = 0; i < portTypeElements.getLength(); i++) {
			Element next = (Element) portTypeElements.item(i);
			String name = next.getAttribute("name");
			if (bindingPortType.equals(name)) {
				portType = next;
			}
		}

		if (portType == null) {
			throw new ProcessingException("WSDL \"" + theUrl + "\" has no PortType named '" + bindingLocal + "'");
		}

		introspectPortType(theUrl, retVal, wsdlDocument, portType);
		// if (portTypeElements.getLength() > 1) {
		// throw new ProcessingException("WSDL \"" + theUrl + "\" has more than one PortType defined (this is not currently supported by ServiceProxy)");
		// }

		return retVal;
	}

	private PersMethod introspectWsdlForDocumentOperation(PersServiceVersionSoap11 retVal, Document wsdlDocument, Element nextOperationElem, String opName) {
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

						for (int partCount = 0; partCount < nextMessage.getChildNodes().getLength(); partCount++) {
							if (!(nextMessage.getChildNodes().item(partCount) instanceof Element)) {
								continue;
							}

							Element partElem = (Element) nextMessage.getChildNodes().item(partCount);
							if (partElem.getLocalName().equals("part")) {

								String type = partElem.getAttribute("type");
								if (StringUtils.isNotBlank(type)) {

									// TODO: infer this from the binding
									// element
									ourLog.info("This looks like an RPC service");

								}

								String element = partElem.getAttribute("element");
								String[] elementParts = element.split("\\:");

								rootElementNs = partElem.lookupNamespaceURI(elementParts[0]);
								rootElementName = elementParts[1];
								ourLog.info("Root element is " + rootElementNs + ":" + rootElementName);
							}

						}
					}

				}

			}

		}

		PersMethod method = retVal.getOrCreateAndAddMethodWithName(opName);
		method.setRootElements(rootElementNs + ":" + rootElementName);
		return method;
	}

	private PersMethod introspectWsdlForRpcOperation(Document theWsdlDocument, String theOpName, PersServiceVersionSoap11 retVal) throws ProcessingException {
		Element binding = findWsdlBindingInDocument(theWsdlDocument);
		NodeList operationList = binding.getElementsByTagNameNS(Constants.NS_WSDL, "operation");
		for (int operationIdx = 0; operationIdx < operationList.getLength(); operationIdx++) {
			Element operation = (Element) operationList.item(operationIdx);
			if (!theOpName.equals(operation.getAttribute("name"))) {
				continue;
			}

			NodeList inputList = operation.getElementsByTagNameNS(Constants.NS_WSDL, "input");
			if (inputList.getLength() != 1) {
				throw new ProcessingException("Binding operation for " + theOpName + " must have 1 input, found " + inputList.getLength());
			}
			Element inputElement = (Element) inputList.item(0);

			NodeList bodyElements = inputElement.getElementsByTagNameNS(Constants.NS_WSDLSOAP, "body");
			if (bodyElements.getLength() != 1) {
				throw new ProcessingException("Binding operation input must have exactly one body tag, found " + bodyElements.getLength());
			}
			Element bodyElement = (Element) bodyElements.item(0);

			String ns = bodyElement.getAttribute("namespace");

			PersMethod method = retVal.getOrCreateAndAddMethodWithName(theOpName);
			method.setRootElements(ns + ":" + theOpName);
			return method;

		}

		throw new ProcessingException("Couldn't find operation '" + theOpName + "' in binding");
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @throws InvocationFailedException
	 * @throws InternalErrorException
	 */
	@Transactional(propagation=Propagation.NEVER)
	@Override
	public SrBeanProcessedRequest processInvocation(SrBeanIncomingRequest theRequest, BasePersServiceVersion theServiceDefinition)
			throws InvalidRequestException, InvocationRequestFailedException, InvocationFailedDueToInternalErrorException {
		SrBeanProcessedRequest retVal = new SrBeanProcessedRequest();

		// TODO: verify that content type is correct

		switch (theRequest.getRequestType()) {
		case GET:
			doHandleGet(theRequest, retVal, (PersServiceVersionSoap11) theServiceDefinition, theRequest.getQuery());
			break;
		case POST:
			doHandlePost(retVal, (PersServiceVersionSoap11) theServiceDefinition, theRequest.getInputReader());
			break;
		default:
			throwUnsupportedActionException(theRequest);
		}

		return retVal;
	}

	@Override
	public SrBeanProcessedResponse processInvocationResponse(BasePersServiceVersion theServiceDefinition, SrBeanIncomingResponse theResponse) throws InvocationResponseFailedException, InvocationFailedDueToInternalErrorException {
		SrBeanProcessedResponse retVal = new SrBeanProcessedResponse();
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
			throw new InvocationFailedDueToInternalErrorException(e);
		} catch (FactoryConfigurationError e) {
			throw new InvocationFailedDueToInternalErrorException(e);
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
				throw new InvocationResponseFailedException(e, "Unable to process XML response. Error was: " + e.getMessage(), theResponse);
			}

		}

		return retVal;
	}

	@Override
	public BaseResponseValidator provideInvocationResponseValidator(BasePersServiceVersion theServiceDefinition) {
		return myInvocationResultsBean;
	}

	private String renderWsdl(PersServiceVersionSoap11 theServiceDefinition, final String thePathBase, ICreatesImportUrl urlCreator) throws InvocationFailedDueToInternalErrorException {
		PersServiceVersionResource resource = theServiceDefinition.getResourceForUri(theServiceDefinition.getWsdlUrl());
		if (resource == null || StringUtils.isBlank(resource.getResourceText())) {
			throw new InvocationFailedDueToInternalErrorException("Service Version " + theServiceDefinition.getPid() + " does not have a resource for URL: " + theServiceDefinition.getWsdlUrl());
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
						importElem.setAttribute("schemaLocation", urlCreator.createImportUrlForSchemaImport(nResource));
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
						importElem.setAttribute("location", thePathBase);
					}
				}
			}
		}

		ourLog.debug("Writing WSDL for ServiceVersion[{}]", theServiceDefinition.getPid());
		StringWriter writer = new StringWriter();
		XMLUtils.serialize(wsdlDocument, true, writer);

		String resourceText = writer.toString();
		return resourceText;
	}

	/**
	 * UNIT TESTS ONLY
	 */
	public void setConfigService(IConfigService theMock) {
		myConfigService = theMock;
	}

	/**
	 * UNIT TESTS ONLY
	 */
	public void setHttpClient(IHttpClient theHttpClient) {
		assert myHttpClient == null;
		myHttpClient = theHttpClient;
	}

	private String urlEncode(String theString) {
		return theString.replace(" ", "%20");
	}

	private static void initEventFactories() throws FactoryConfigurationError {
		synchronized (ServiceInvokerSoap11.class) {
			if (ourEventFactory == null) {
				ourXmlInputFactory = XMLInputFactory.newInstance();
				ourEventFactory = XMLEventFactory.newInstance();
			}
		}
	}

	private interface ICreatesImportUrl {
		String createImportUrlForSchemaImport(PersServiceVersionResource theResource) throws InvocationFailedDueToInternalErrorException;
	}

	private static enum ResponsePositionEnum {
		IN_BODY, IN_DOCUMENT, IN_FAULT, NONE
	}

	private enum StyleEnum {
		DOCUMENT, RPC
	}

	@Override
	public String obscureMessageForLogs(BasePersServiceVersion theServiceDefinition, String theMessage, Set<String> theElementNamesToRedact) {
		// TODO: implement
		return theMessage;
	}

}
