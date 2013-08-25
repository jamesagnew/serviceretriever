package net.svcret.ejb.ejb.soap;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.svcret.ejb.ex.InternalErrorException;
import net.svcret.ejb.ex.ProcessingException;
import net.svcret.ejb.ex.UnknownRequestException;
import net.svcret.ejb.model.entity.PersBaseClientAuth;
import net.svcret.ejb.model.entity.PersBaseServerAuth;
import net.svcret.ejb.model.entity.PersServiceVersionMethod;
import net.svcret.ejb.model.entity.soap.PersServiceVersionSoap11;
import net.svcret.ejb.model.entity.soap.PersWsSecUsernameTokenClientAuth;
import net.svcret.ejb.model.entity.soap.PersWsSecUsernameTokenServerAuth;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class RequestPipelineTest {

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(RequestPipelineTest.class);

	@Test
	public void testRequestProcessor() throws InternalErrorException, ProcessingException, UnknownRequestException, SAXException, IOException, ParserConfigurationException {

		String methodName = "getPatientByMrn";
		String msg = createRequest(methodName, true);

		StringReader reader = new StringReader(msg);
		StringWriter writer = new StringWriter();

		List<PersBaseClientAuth<?>> clientAuths = new ArrayList<PersBaseClientAuth<?>>();
		clientAuths.add(new PersWsSecUsernameTokenClientAuth("theUsername", "thePassword"));

		List<PersBaseServerAuth<?, ?>> serverAuths = new ArrayList<PersBaseServerAuth<?, ?>>();
		serverAuths.add(new PersWsSecUsernameTokenServerAuth());

		PersServiceVersionSoap11 serviceVer = mock(PersServiceVersionSoap11.class);
		PersServiceVersionMethod method = mock(PersServiceVersionMethod.class);
		when(serviceVer.getMethod("getPatientByMrn")).thenReturn(method);

		RequestPipeline p = new RequestPipeline(serverAuths, clientAuths);
		p.setPrettyPrint(true);
		p.process(reader, writer);

		String out = writer.toString();
		ourLog.info(out);

		validate(out);

		assertFalse(out.contains("<?xml version"));
		assertTrue(out.contains("<svcretwsse:Username>theUsername</svcretwsse:Username>"));
		assertTrue(out.contains("<svcretwsse:Password Type=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText\">thePassword</svcretwsse:Password>"));
		assertEquals(1, p.getCredentialGrabbers().size());
		assertEquals("user", p.getCredentialGrabbers().get(0).getUsername());
		assertEquals(1, StringUtils.countMatches(out, "</soapenv:Header>"));
		assertEquals("pass", p.getCredentialGrabbers().get(0).getPassword());
		assertEquals("http://ws.ehr.uhn.ca:getPatientByMrn", p.getMethodName());

	}

	@Test
	public void testAddWsSecToMessageWithNoHeader() throws InternalErrorException, ProcessingException, UnknownRequestException, SAXException, IOException, ParserConfigurationException {

		String methodName = "getPatientByMrn";
		String msg = createRequest(methodName, false);

		StringReader reader = new StringReader(msg);
		StringWriter writer = new StringWriter();

		List<PersBaseClientAuth<?>> clientAuths = new ArrayList<PersBaseClientAuth<?>>();
		clientAuths.add(new PersWsSecUsernameTokenClientAuth("theUsername", "thePassword"));

		List<PersBaseServerAuth<?, ?>> serverAuths = new ArrayList<PersBaseServerAuth<?, ?>>();
		serverAuths.add(new PersWsSecUsernameTokenServerAuth());

		PersServiceVersionSoap11 serviceVer = mock(PersServiceVersionSoap11.class);
		PersServiceVersionMethod method = mock(PersServiceVersionMethod.class);
		when(serviceVer.getMethod("getPatientByMrn")).thenReturn(method);

		RequestPipeline p = new RequestPipeline(serverAuths, clientAuths);
		p.setPrettyPrint(true);
		p.process(reader, writer);

		String out = writer.toString();
		ourLog.info(out);

		validate(out);
		
		assertFalse(out.contains("<?xml version"));
		assertTrue(out.contains("<svcretwsse:Username>theUsername</svcretwsse:Username>"));
		assertTrue(out.contains("<svcretwsse:Password Type=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText\">thePassword</svcretwsse:Password>"));
		assertEquals(1, p.getCredentialGrabbers().size());
		assertEquals("", p.getCredentialGrabbers().get(0).getUsername());
		assertEquals(1, StringUtils.countMatches(out, "</soapenv:Header>"));
		assertEquals("", p.getCredentialGrabbers().get(0).getPassword());
		assertEquals("http://ws.ehr.uhn.ca:getPatientByMrn", p.getMethodName());

	}

	private void validate(String theOut) throws SAXException, IOException, ParserConfigurationException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		factory.setNamespaceAware(true);

		DocumentBuilder builder = factory.newDocumentBuilder();

		builder.setErrorHandler(new ErrorHandler() {
			
			@Override
			public void warning(SAXParseException theException) throws SAXException {
				ourLog.error("Failed", theException);
				fail(theException.toString());
			}
			
			@Override
			public void fatalError(SAXParseException theException) throws SAXException {
				ourLog.error("Failed", theException);
				fail(theException.toString());
			}
			
			@Override
			public void error(SAXParseException theException) throws SAXException {
				ourLog.error("Failed", theException);
				fail(theException.toString());
			}
		});    
		
		// the "parse" method also validates XML, will throw an exception if misformatted
		String trim = theOut.trim();
		builder.parse(new InputSource(new StringReader(trim)));		
	}

	@Test
	public void testRequestWithoutHeaderElement() throws InternalErrorException, ProcessingException, UnknownRequestException, SAXException, IOException, ParserConfigurationException {

		String methodName = "getPatientByMrn";
		String msg = createRequest(methodName, false);

		StringReader reader = new StringReader(msg);
		StringWriter writer = new StringWriter();

		List<PersBaseClientAuth<?>> clientAuths = new ArrayList<PersBaseClientAuth<?>>();
//		clientAuths.add(new PersWsSecUsernameTokenClientAuth("theUsername", "thePassword"));

		List<PersBaseServerAuth<?, ?>> serverAuths = new ArrayList<PersBaseServerAuth<?, ?>>();
		serverAuths.add(new PersWsSecUsernameTokenServerAuth());

		PersServiceVersionSoap11 serviceVer = mock(PersServiceVersionSoap11.class);
		PersServiceVersionMethod method = mock(PersServiceVersionMethod.class);
		when(serviceVer.getMethod("getPatientByMrn")).thenReturn(method);

		RequestPipeline p = new RequestPipeline(serverAuths, clientAuths);
		p.setPrettyPrint(true);
		p.process(reader, writer);

		String out = writer.toString();
		ourLog.info(out);

		validate(out);

		assertFalse(out.contains("<?xml version"));
		assertEquals("http://ws.ehr.uhn.ca:getPatientByMrn", p.getMethodName());

	}

	static String createRequest(String methodName, boolean theWithHeader) {
		StringBuilder b = new StringBuilder();
		b.append("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ws=\"http://ws.ehr.uhn.ca\">\n");
		if (theWithHeader) {
			b.append("   <soapenv:Header>\n");
			b.append("      <wsse:Security soapenv:mustUnderstand=\"1\" xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\" xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\">\n");
			b.append("         <wsse:UsernameToken wsu:Id=\"UsernameToken-1\">\n");
			b.append("            <wsse:Username>user</wsse:Username>\n");
			b.append("            <wsse:Password Type=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText\">pass</wsse:Password>\n");
			b.append("            <wsse:Nonce EncodingType=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#Base64Binary\">oYz1MICBZPROFVio2aOpRA==</wsse:Nonce>\n");
			b.append("            <wsu:Created>2013-02-09T02:14:13.173Z</wsu:Created>\n");
			b.append("         </wsse:UsernameToken>\n");
			b.append("      </wsse:Security>\n");
			b.append("   </soapenv:Header>\n");
		}
		b.append("   <soapenv:Body>\n");
		b.append("      <ws:");
		b.append(methodName);
		b.append(">\n");
		b.append("         <ws:theMrn>?</ws:theMrn>\n");
		b.append("         <ws:theMrnAuth>?</ws:theMrnAuth>\n");
		b.append("      </ws:");
		b.append(methodName);
		b.append(">\n");
		b.append("   </soapenv:Body>\n");
		b.append("</soapenv:Envelope>");
		String msg = b.toString(); // -
		return msg;
	}
}
