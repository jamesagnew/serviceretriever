package net.svcret.ejb.ejb.soap;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import net.svcret.ejb.ejb.soap.RequestPipeline;
import net.svcret.ejb.ex.InternalErrorException;
import net.svcret.ejb.ex.ProcessingException;
import net.svcret.ejb.ex.UnknownRequestException;
import net.svcret.ejb.model.entity.PersBaseClientAuth;
import net.svcret.ejb.model.entity.PersBaseServerAuth;
import net.svcret.ejb.model.entity.PersServiceVersionMethod;
import net.svcret.ejb.model.entity.soap.PersServiceVersionSoap11;
import net.svcret.ejb.model.entity.soap.PersWsSecUsernameTokenClientAuth;
import net.svcret.ejb.model.entity.soap.PersWsSecUsernameTokenServerAuth;

import org.junit.Test;


public class RequestPipelineTest {

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(RequestPipelineTest.class);
	
	@Test
	public void testRequestProcessor() throws InternalErrorException, ProcessingException, UnknownRequestException {
		
		String methodName ="getPatientByMrn";
		String msg = createRequest(methodName);
		
		StringReader reader = new StringReader(msg);
		StringWriter writer = new StringWriter();
		
		List<PersBaseClientAuth<?>> clientAuths = new ArrayList<PersBaseClientAuth<?>>();
		clientAuths.add(new PersWsSecUsernameTokenClientAuth("theUsername", "thePassword"));
		
		List<PersBaseServerAuth<?,?>> serverAuths = new ArrayList<PersBaseServerAuth<?,?>>();
		serverAuths.add(new PersWsSecUsernameTokenServerAuth());
		
		PersServiceVersionSoap11 serviceVer = mock(PersServiceVersionSoap11.class);
		PersServiceVersionMethod method = mock(PersServiceVersionMethod.class);
		when(serviceVer.getMethod("getPatientByMrn")).thenReturn(method);
		
		RequestPipeline p = new RequestPipeline(serverAuths, clientAuths);
		p.setPrettyPrint(true);
		p.process(reader, writer);
		
		String out = writer.toString();
		ourLog.info(out);
		
		assertFalse(out.contains("<?xml version"));
		assertTrue(out.contains("<wsse:Username>theUsername</wsse:Username>"));
		assertTrue(out.contains("<wsse:Password Type=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText\">thePassword</wsse:Password>"));
		assertEquals(1, p.getCredentialGrabbers().size());
		assertEquals("user", p.getCredentialGrabbers().get(0).getUsername());
		assertEquals("pass", p.getCredentialGrabbers().get(0).getPassword());
		assertEquals("getPatientByMrn", p.getMethodName());
		
	}


	static String createRequest(String methodName) {
		String msg = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ws=\"http://ws.ehr.uhn.ca\">\n" + //- 
				"   <soapenv:Header>\n" + //-
				"      <wsse:Security soapenv:mustUnderstand=\"1\" xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\" xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\">\n" + //- 
				"         <wsse:UsernameToken wsu:Id=\"UsernameToken-1\">\n" +  //-
				"            <wsse:Username>user</wsse:Username>\n" +  //-
				"            <wsse:Password Type=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText\">pass</wsse:Password>\n" + //- 
				"            <wsse:Nonce EncodingType=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#Base64Binary\">oYz1MICBZPROFVio2aOpRA==</wsse:Nonce>\n" + //- 
				"            <wsu:Created>2013-02-09T02:14:13.173Z</wsu:Created>\n" +  //-
				"         </wsse:UsernameToken>\n" +  //-
				"      </wsse:Security>\n" +  //-
				"   </soapenv:Header>\n" +  //-
				"   <soapenv:Body>\n" +  //-
				"      <ws:" + methodName + ">\n" + //- 
				"         <ws:theMrn>?</ws:theMrn>\n" + //- 
				"         <ws:theMrnAuth>?</ws:theMrnAuth>\n" +  //-
				"      </ws:" + methodName + ">\n" +  //-
				"   </soapenv:Body>\n" +  //-
				"</soapenv:Envelope>"; //-
		return msg;
	}
}
