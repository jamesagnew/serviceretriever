package ca.uhn.sail.proxy.ejb.soap;

import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

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

import ca.uhn.sail.proxy.api.ICredentialGrabber;
import ca.uhn.sail.proxy.ex.InternalErrorException;
import ca.uhn.sail.proxy.ex.ProcessingException;
import ca.uhn.sail.proxy.ex.UnknownRequestException;
import ca.uhn.sail.proxy.model.entity.PersBaseClientAuth;
import ca.uhn.sail.proxy.model.entity.PersBaseServerAuth;
import ca.uhn.sail.proxy.model.entity.soap.PersWsSecUsernameTokenClientAuth;
import ca.uhn.sail.proxy.model.entity.soap.PersWsSecUsernameTokenServerAuth;

class RequestPipeline {

	private static XMLEventFactory ourEventFactory;
	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(RequestPipeline.class);
	private static XMLInputFactory ourXmlInputFactory;
	private static XMLOutputFactory ourXmlOutputFactory;
	private final List<PersBaseClientAuth<?>> myClientAuths;
	private List<ICredentialGrabber> myCredentialGrabbers = new ArrayList<ICredentialGrabber>();
	private boolean myPrettyPrint;
	private boolean myUsed = false;
	private List<PersBaseServerAuth<?,?>> myServerAuths;
	private String myMethodName;

	/**
	 * @param theClientAuths
	 *            The authentication types that will be applied to the proxy
	 *            client when it makes requests to the actual service definition
	 */
	public RequestPipeline(List<PersBaseServerAuth<?,?>> theServerAuth, List<PersBaseClientAuth<?>> theClientAuths) {
		myServerAuths = theServerAuth;
		myClientAuths = theClientAuths;
	}

	/**
	 * @return the credentialGrabbers
	 */
	public List<ICredentialGrabber> getCredentialGrabbers() {
		return myCredentialGrabbers;
	}

	public void process(Reader theReader, Writer theWriter) throws InternalErrorException, ProcessingException, UnknownRequestException {
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
				throw new InternalErrorException(e);
			} catch (FactoryConfigurationError e) {
				throw new InternalErrorException(e);
			}
		}

		if (myPrettyPrint) {
			streamWriter = new PrettyPrintWriterWrapper(ourEventFactory, streamWriter);
		}

		try {

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
							setMethodName(elem.getName().getLocalPart());
						}
					} else if (Constants.SOAPENV11_HEADER_QNAME.equals(elem.getName())) {
						processHeader(elem.getName().getPrefix(), streamReader, streamWriter);
						haveProcessedHeader = true;
						continue;
					} else if (Constants.SOAPENV11_BODY_QNAME.equals(elem.getName())) {
						if (!haveProcessedHeader) {
							processHeader(elem.getName().getPrefix(), streamReader, streamWriter);
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
				throw new ProcessingException("No request header was found in request- Not a valid SOAP message!");
			}

		} catch (XMLStreamException e) {
			throw new ProcessingException(e);
		}

		try {
			streamWriter.flush();
			streamWriter.close();
		} catch (XMLStreamException e) {
			throw new ProcessingException(e);
		}

	}

	private void setMethodName(String theMethodName) throws UnknownRequestException {
		myMethodName = theMethodName;
	}

	/**
	 * 
	 * @param theXmlPrefix
	 *            The element prefix. E.g. if the header element is
	 *            &lt;soapenv:Header&gt; this string is "soapenv"
	 * @param theStreamReader
	 * @param theStreamWriter
	 * @throws ProcessingException
	 */
	private void processHeader(String theXmlPrefix, XMLEventReader theStreamReader, XMLEventWriter theStreamWriter) throws ProcessingException {

		try {
			writeSecurityHeader(theXmlPrefix, theStreamWriter);
		} catch (XMLStreamException e) {
			throw new ProcessingException(e);
		}

		List<XMLEvent> headerEvents = new ArrayList<XMLEvent>();
		try {
			while (theStreamReader.hasNext()) {
				XMLEvent nextEvent = theStreamReader.nextEvent();

				if (nextEvent.isEndElement()) {
					EndElement elem = (EndElement) nextEvent;
					if (Constants.SOAPENV11_HEADER_QNAME.equals(elem.getName())) {
						break;
					}
				}

				headerEvents.add(nextEvent);
			}
		} catch (XMLStreamException e) {
			throw new ProcessingException(e);
		}

		/*
		 * Grab security headers
		 */
		for (PersBaseServerAuth<?,?> next : myServerAuths) {
			if (next instanceof PersWsSecUsernameTokenServerAuth) {
				ICredentialGrabber grabber = ((PersWsSecUsernameTokenServerAuth) next).newCredentialGrabber(headerEvents);
				myCredentialGrabbers.add(grabber);
			} else {
				throw new InternalErrorException("Don't know how to handle server auth of type: " + next);
			}
		}

	}

	/**
	 * @param thePrettyPrint
	 *            the prettyPrint to set
	 */
	public void setPrettyPrint(boolean thePrettyPrint) {
		myPrettyPrint = thePrettyPrint;
	}

	private void writeSecurityHeader(String theXmlPrefix, XMLEventWriter theStreamWriter) throws XMLStreamException {
		StartElement headerStart = ourEventFactory.createStartElement(Constants.getSoapenvHeaderQname(theXmlPrefix), null, null);
		theStreamWriter.add(headerStart);

		int secIndex = 1;
		for (PersBaseClientAuth<?> nextAuth : myClientAuths) {
			if (nextAuth instanceof PersWsSecUsernameTokenClientAuth) {
				PersWsSecUsernameTokenClientAuth auth = (PersWsSecUsernameTokenClientAuth) nextAuth;

				ArrayList<Namespace> namespaces = new ArrayList<Namespace>();
				namespaces.add(ourEventFactory.createNamespace("wsu", Constants.NS_WSSEC_UTIL));
				StartElement secStart = ourEventFactory.createStartElement(Constants.getWsseSecurityQname("wsse"), null, namespaces.iterator());
				theStreamWriter.add(secStart);

				// UsernameToken
				{
					StartElement usernameTokenStart = ourEventFactory.createStartElement(Constants.getWsseUsernameTokenQname("wsse"), null, null);
					theStreamWriter.add(usernameTokenStart);

					Attribute idAttr = ourEventFactory.createAttribute(Constants.getWssuIdQname("wsu"), "UsernameToken-" + secIndex++);
					theStreamWriter.add(idAttr);

					// Username
					{
						StartElement usernameStart = ourEventFactory.createStartElement(Constants.getWsseUsernameQname("wsse"), null, null);
						theStreamWriter.add(usernameStart);

						theStreamWriter.add(ourEventFactory.createCharacters(auth.getUsername()));

						EndElement usernameEnd = ourEventFactory.createEndElement(Constants.getWsseUsernameQname("wsse"), null);
						theStreamWriter.add(usernameEnd);
					}

					// Password
					{
						StartElement passwordStart = ourEventFactory.createStartElement(Constants.getWssePasswordQname("wsse"), null, null);
						theStreamWriter.add(passwordStart);

						theStreamWriter.add(ourEventFactory.createAttribute(Constants.WSSE_TYPE_QNAME, Constants.VALUE_WSSE_PASSWORD_TYPE_TEXT));

						theStreamWriter.add(ourEventFactory.createCharacters(auth.getPassword()));

						EndElement usernameEnd = ourEventFactory.createEndElement(Constants.getWssePasswordQname("wsse"), null);
						theStreamWriter.add(usernameEnd);
					}

					EndElement usernameTokenEnd = ourEventFactory.createEndElement(Constants.getWsseUsernameTokenQname("wsse"), null);
					theStreamWriter.add(usernameTokenEnd);

				}
				EndElement secEnd = ourEventFactory.createEndElement(Constants.getWsseSecurityQname("wsse"), null);
				theStreamWriter.add(secEnd);
			}
		}

		EndElement headerEnd = ourEventFactory.createEndElement(Constants.getSoapenvHeaderQname(theXmlPrefix), null);
		theStreamWriter.add(headerEnd);
	}

	public String getMethodName() {
		return myMethodName;
	}
}
