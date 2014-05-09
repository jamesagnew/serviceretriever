package net.svcret.core.invoker.soap;

import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import net.svcret.core.api.ICredentialGrabber;
import net.svcret.core.ex.InvocationFailedDueToInternalErrorException;
import net.svcret.core.model.entity.PersBaseClientAuth;
import net.svcret.core.model.entity.PersBaseServerAuth;
import net.svcret.core.model.entity.soap.PersWsSecUsernameTokenClientAuth;
import net.svcret.core.model.entity.soap.PersWsSecUsernameTokenServerAuth;

class RequestPipeline {

	private static XMLEventFactory ourEventFactory;
	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(RequestPipeline.class);
	private static XMLInputFactory ourXmlInputFactory;
	private static XMLOutputFactory ourXmlOutputFactory;
	private final List<PersBaseClientAuth<?>> myClientAuths;
	private Map<PersBaseServerAuth<?, ?>, ICredentialGrabber> myCredentialGrabbers = new HashMap<>();
	private boolean myPrettyPrint;
	private boolean myUsed = false;
	private List<PersBaseServerAuth<?, ?>> myServerAuths;
	private String myMethodName;

	/**
	 * @param theClientAuths
	 *            The authentication types that will be applied to the proxy client when it makes requests to the actual service definition
	 */
	public RequestPipeline(List<PersBaseServerAuth<?, ?>> theServerAuth, List<PersBaseClientAuth<?>> theClientAuths) {
		myServerAuths = theServerAuth;
		myClientAuths = theClientAuths;
	}

	/**
	 * @return the credentialGrabbers
	 */
	public Map<PersBaseServerAuth<?, ?>, ICredentialGrabber> getCredentialGrabbers() {
		return myCredentialGrabbers;
	}

	public void process(Reader theReader, Writer theWriter) throws InvocationFailedDueToInternalErrorException, XMLStreamException {
		if (myUsed) {
			throw new IllegalStateException("Pipeline instance is already used");
		}
		myUsed = true;

		XMLEventReader streamReader;
		XMLEventWriter streamWriter;
		synchronized (RequestPipeline.class) {
			if (ourEventFactory == null) {
				ourXmlInputFactory = XMLInputFactory.newInstance();
				ourXmlOutputFactory = XMLOutputFactory.newInstance();
				ourEventFactory = XMLEventFactory.newInstance();
			}
			try {
				streamReader = ourXmlInputFactory.createXMLEventReader(theReader);
				streamWriter = ourXmlOutputFactory.createXMLEventWriter(theWriter);
			} catch (XMLStreamException e) {
				throw new InvocationFailedDueToInternalErrorException(e);
			} catch (FactoryConfigurationError e) {
				throw new InvocationFailedDueToInternalErrorException(e);
			}
		}

		if (myPrettyPrint) {
			streamWriter = new PrettyPrintWriterWrapper(ourEventFactory, streamWriter);
		}

		boolean haveProcessedHeader = false;
		boolean haveProcessedBody = false;
		while (streamReader.hasNext()) {
			XMLEvent nextEvent = streamReader.nextEvent();

			// Suppress the XML Declaration
			if (nextEvent.isStartDocument()) {
				continue;
			}

			if (nextEvent.isStartElement()) {
				StartElement elem = (StartElement) nextEvent;
				if (haveProcessedBody) {
					if (myMethodName == null) {
						setMethodName(elem.getName().getNamespaceURI() + ":" + elem.getName().getLocalPart());
					}
				} else if (Constants.SOAPENV11_HEADER_QNAME.equals(elem.getName())) {
					processHeader(elem, elem.getName().getPrefix(), streamReader, streamWriter);
					haveProcessedHeader = true;
					continue;
				} else if (Constants.SOAPENV11_BODY_QNAME.equals(elem.getName())) {
					if (!haveProcessedHeader) {
						processHeader(null, elem.getName().getPrefix(), streamReader, streamWriter);
						haveProcessedHeader = true;
					}
					haveProcessedBody = true;
				}
			}

			if (ourLog.isTraceEnabled()) {
				ourLog.trace("Writing elementof type {}: {}", nextEvent.getClass(), nextEvent.getSchemaType());
			}

			streamWriter.add(nextEvent);
		}

		/*
		 * Final sanity check to make sure security isn't bypassed
		 */
		if (!haveProcessedHeader) {
			throw new XMLStreamException("No request header was found in request, and one is required for this service");
		}

		streamWriter.flush();
		streamWriter.close();

	}

	private void setMethodName(String theMethodName) {
		myMethodName = theMethodName;
	}

	/**
	 * 
	 * @param theXmlPrefix
	 *            The element prefix. E.g. if the header element is &lt;soapenv:Header&gt; this string is "soapenv"
	 */
	private void processHeader(StartElement theStartElem, String theXmlPrefix, XMLEventReader theStreamReader, XMLEventWriter theStreamWriter) throws XMLStreamException, InvocationFailedDueToInternalErrorException {

		boolean haveClientSecurity = false;
		for (PersBaseClientAuth<?> next : myClientAuths) {
			if (next instanceof PersWsSecUsernameTokenClientAuth) {
				haveClientSecurity = true;
				break;
			}
		}

		StartElement headerStart = ourEventFactory.createStartElement(Constants.getSoapenvHeaderQname(theXmlPrefix), null, null);
		theStreamWriter.add(headerStart);

		List<XMLEvent> headerEvents = new ArrayList<>();
		if (theStartElem != null) {
			while (theStreamReader.hasNext()) {
				XMLEvent nextEvent = theStreamReader.nextEvent();

				if (nextEvent.isEndElement()) {
					EndElement elem = (EndElement) nextEvent;
					if (Constants.SOAPENV11_HEADER_QNAME.equals(elem.getName())) {
						break;
					}
				}

				headerEvents.add(nextEvent);

				if (!haveClientSecurity) {
					theStreamWriter.add(nextEvent);
				}

			}
		}

		/*
		 * Grab security headers
		 */
		for (PersBaseServerAuth<?, ?> next : myServerAuths) {
			switch (next.getAuthType()) {
			case HTTP_BASIC_AUTH:
				// Can ignore these
				break;
			case JSONRPC_NAMED_PARAMETER:
				throw new InvocationFailedDueToInternalErrorException("Don't know how to handle server auth of type: " + next);
			case WSSEC_UT:
				ICredentialGrabber grabber = ((PersWsSecUsernameTokenServerAuth) next).newCredentialGrabber(headerEvents);
				myCredentialGrabbers.put(next, grabber);
				break;
			}
		}

		if (haveClientSecurity) {
			writeSecurityHeader(theStreamWriter);
		}

		EndElement headerEnd = ourEventFactory.createEndElement(Constants.getSoapenvHeaderQname(theXmlPrefix), null);
		theStreamWriter.add(headerEnd);

	}

	/**
	 * @param thePrettyPrint
	 *            the prettyPrint to set
	 */
	public void setPrettyPrint(boolean thePrettyPrint) {
		myPrettyPrint = thePrettyPrint;
	}

	private void writeSecurityHeader(XMLEventWriter theStreamWriter) throws XMLStreamException {
		int secIndex = 1;

		ourLog.debug("Applying client auth modules: {}", myClientAuths);

		for (PersBaseClientAuth<?> nextAuth : myClientAuths) {
			if (nextAuth instanceof PersWsSecUsernameTokenClientAuth) {
				PersWsSecUsernameTokenClientAuth auth = (PersWsSecUsernameTokenClientAuth) nextAuth;

				ArrayList<Namespace> namespaces = new ArrayList<>();
				Namespace wsuNs = ourEventFactory.createNamespace("svcretwsu", Constants.NS_WSSEC_UTIL);
				namespaces.add(wsuNs);
				theStreamWriter.add(wsuNs);

				Namespace wsseNs = ourEventFactory.createNamespace("svcretwsse", Constants.NS_WSSEC_SECEXT);
				namespaces.add(wsseNs);
				theStreamWriter.add(wsseNs);

				StartElement secStart = ourEventFactory.createStartElement(Constants.getWsseSecurityQname(wsseNs.getPrefix()), null, namespaces.iterator());
				theStreamWriter.add(secStart);

				// UsernameToken
				{
					StartElement usernameTokenStart = ourEventFactory.createStartElement(Constants.getWsseUsernameTokenQname(wsseNs.getPrefix()), null, null);
					theStreamWriter.add(usernameTokenStart);

					Attribute idAttr = ourEventFactory.createAttribute(Constants.getWssuIdQname(wsuNs.getPrefix()), "UsernameToken-" + secIndex++);
					theStreamWriter.add(idAttr);

					// Username
					{
						StartElement usernameStart = ourEventFactory.createStartElement(Constants.getWsseUsernameQname(wsseNs.getPrefix()), null, null);
						theStreamWriter.add(usernameStart);

						theStreamWriter.add(ourEventFactory.createCharacters(auth.getUsername()));

						EndElement usernameEnd = ourEventFactory.createEndElement(Constants.getWsseUsernameQname(wsseNs.getPrefix()), null);
						theStreamWriter.add(usernameEnd);
					}

					// Password
					{
						StartElement passwordStart = ourEventFactory.createStartElement(Constants.getWssePasswordQname(wsseNs.getPrefix()), null, null);
						theStreamWriter.add(passwordStart);

						theStreamWriter.add(ourEventFactory.createAttribute(Constants.WSSE_TYPE_QNAME, Constants.VALUE_WSSE_PASSWORD_TYPE_TEXT));

						theStreamWriter.add(ourEventFactory.createCharacters(auth.getPassword()));

						EndElement usernameEnd = ourEventFactory.createEndElement(Constants.getWssePasswordQname(wsseNs.getPrefix()), null);
						theStreamWriter.add(usernameEnd);
					}

					EndElement usernameTokenEnd = ourEventFactory.createEndElement(Constants.getWsseUsernameTokenQname(wsseNs.getPrefix()), null);
					theStreamWriter.add(usernameTokenEnd);

				}
				EndElement secEnd = ourEventFactory.createEndElement(Constants.getWsseSecurityQname(wsseNs.getPrefix()), null);
				theStreamWriter.add(secEnd);
			}
		}
	}

	public String getMethodName() {
		return myMethodName;
	}
}
