package net.svcret.ejb.model.entity.soap;

import static org.junit.Assert.*;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import net.svcret.ejb.invoker.soap.Constants;

import org.junit.Test;


public class WsSecUsernameTokenCredentialGrabberTest {

	private String createRequest(String usernameLine, String passwordLine) {
		String request = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ws=\"http://ws.ehr.uhn.ca\">\n" + // -
				"   <soapenv:Header>\n" + // -
				"      <wsse:Security soapenv:mustUnderstand=\"1\" xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\" xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\">\n" + // -
				"         <wsse:UsernameToken wsu:Id=\"UsernameToken-1\">\n" + // -
				usernameLine + // -
				passwordLine + // -
				"            <wsse:Nonce EncodingType=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#Base64Binary\">oYz1MICBZPROFVio2aOpRA==</wsse:Nonce>\n" + // -
				"            <wsu:Created>2013-02-09T02:14:13.173Z</wsu:Created>\n" + // -
				"         </wsse:UsernameToken>\n" + // -
				"      </wsse:Security>\n" + // -
				"   </soapenv:Header>\n" + // -
				"   <soapenv:Body>\n" + // -
				"      <ws:getPatientByMrn>\n" + // -
				"         <ws:thePatientMrn>?</ws:thePatientMrn>\n" + // -
				"         <ws:theMrnAuth>?</ws:theMrnAuth>\n" + // -
				"      </ws:getPatientByMrn>\n" + // -
				"   </soapenv:Body>\n" + // -
				"</soapenv:Envelope>\n"; // -
		return request;
	}

	private List<XMLEvent> readEvents(String request) throws FactoryConfigurationError, XMLStreamException {
		XMLInputFactory inputFactory = XMLInputFactory.newInstance();
		XMLEventReader reader = inputFactory.createXMLEventReader(new StringReader(request));

		List<XMLEvent> events = new ArrayList<XMLEvent>();
		boolean inHeader = false;
		while (reader.hasNext()) {
			XMLEvent nextEvent = reader.nextEvent();
			QName schemaType = null;
			if (nextEvent.isStartElement()) {
				schemaType = ((StartElement) nextEvent).getName();
			}
			if (nextEvent.isEndElement()) {
				schemaType = ((EndElement) nextEvent).getName();
			}

			QName soapenvHeaderQname = Constants.SOAPENV11_HEADER_QNAME;
			if (soapenvHeaderQname.equals(schemaType)) {
				inHeader = !inHeader;
			}
			if (inHeader) {
				events.add(nextEvent);
			}
		}
		return events;
	}

	@Test
	public void testGrabCredentials() throws XMLStreamException {

		String usernameLine = "            <wsse:Username>user</wsse:Username>\n";
		String passwordLine = "            <wsse:Password Type=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText\">somepass</wsse:Password>\n";
		String request = createRequest(usernameLine, passwordLine);

		List<XMLEvent> events = readEvents(request);

		WsSecUsernameTokenCredentialGrabber grabber = new WsSecUsernameTokenCredentialGrabber(events);
		assertEquals("user", grabber.getUsername());
		assertEquals("somepass", grabber.getPassword());

	}

	@Test
	public void testGrabCredentialsBlankUsername() throws XMLStreamException {

		String usernameLine = "            <wsse:Username></wsse:Username>\n";
		String passwordLine = "            <wsse:Password Type=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText\">somepass</wsse:Password>\n";
		String request = createRequest(usernameLine, passwordLine);

		List<XMLEvent> events = readEvents(request);

		WsSecUsernameTokenCredentialGrabber grabber = new WsSecUsernameTokenCredentialGrabber(events);
		assertEquals("", grabber.getUsername());
		assertEquals("somepass", grabber.getPassword());

	}

	@Test
	public void testGrabCredentialsBlankUsername2() throws XMLStreamException {

		String usernameLine = "            <wsse:Username/>\n";
		String passwordLine = "            <wsse:Password Type=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText\">somepass</wsse:Password>\n";
		String request = createRequest(usernameLine, passwordLine);

		List<XMLEvent> events = readEvents(request);

		WsSecUsernameTokenCredentialGrabber grabber = new WsSecUsernameTokenCredentialGrabber(events);
		assertEquals("", grabber.getUsername());
		assertEquals("somepass", grabber.getPassword());

	}

	@Test
	public void testGrabCredentialsEmptyPassword() throws XMLStreamException {

		String usernameLine = "            <wsse:Username>user</wsse:Username>\n";
		String passwordLine = "            <wsse:Password Type=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText\"></wsse:Password>\n";
		String request = createRequest(usernameLine, passwordLine);

		List<XMLEvent> events = readEvents(request);

		WsSecUsernameTokenCredentialGrabber grabber = new WsSecUsernameTokenCredentialGrabber(events);
		assertEquals("user", grabber.getUsername());
		assertEquals("", grabber.getPassword());

	}

	@Test
	public void testGrabCredentialsEmptyPassword2() throws XMLStreamException {

		String usernameLine = "            <wsse:Username>user</wsse:Username>\n";
		String passwordLine = "            <wsse:Password Type=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText\"/>\n";
		String request = createRequest(usernameLine, passwordLine);

		List<XMLEvent> events = readEvents(request);

		WsSecUsernameTokenCredentialGrabber grabber = new WsSecUsernameTokenCredentialGrabber(events);
		assertEquals("user", grabber.getUsername());
		assertEquals("", grabber.getPassword());

	}

	@Test
	public void testGrabCredentialsMissingUsername() throws XMLStreamException {

		String usernameLine = "           \n";
		String passwordLine = "            <wsse:Password Type=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText\">somepass</wsse:Password>\n";
		String request = createRequest(usernameLine, passwordLine);

		List<XMLEvent> events = readEvents(request);

		WsSecUsernameTokenCredentialGrabber grabber = new WsSecUsernameTokenCredentialGrabber(events);
		assertEquals("", grabber.getUsername());
		assertEquals("somepass", grabber.getPassword());

	}

	@Test
	public void testGrabCredentialsMissingUsernameAndPassword() throws XMLStreamException {

		String usernameLine = "           \n";
		String passwordLine = " \n";
		String request = createRequest(usernameLine, passwordLine);

		List<XMLEvent> events = readEvents(request);

		WsSecUsernameTokenCredentialGrabber grabber = new WsSecUsernameTokenCredentialGrabber(events);
		assertEquals("", grabber.getUsername());
		assertEquals("", grabber.getPassword());

	}

}
