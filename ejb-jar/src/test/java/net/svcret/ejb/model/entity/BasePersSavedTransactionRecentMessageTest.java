package net.svcret.ejb.model.entity;

import org.junit.Assert;
import org.junit.Test;

import net.svcret.admin.shared.model.GRecentMessage;

public class BasePersSavedTransactionRecentMessageTest {

	@Test
	public void testToDto() {

		//@formatter:off
		String reqString = "POST /cGTA/CR/2.1.1 HTTP/1.1\r\n" + 
				"content-type: text/xml; charset=utf-8\r\n" + 
				"host: 142.224.196.34:18880\r\n" + 
				"soapaction: \"\"\r\n" + 
				"content-length: 795\r\n" + 
				"authorization: Basic Y2d0YTpkQDNyJEBUVGcyNDQ2eWhoaDJoNA==\r\n" + 
				"\r\n" + 
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + 
				"<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\r\n" + 
				"<SOAP-ENV:Header xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"></SOAP-ENV:Header><SOAP-ENV:Body>\r\n" + 
				"<tns:getLinkedPatients xmlns:tns=\"soap.ws.pls.uhn.ca\">\r\n" + 
				"    <theMrnNumber>N000070002</theMrnNumber>\r\n" + 
				"    <theMrnAuth>2.16.840.1.113883.3.239.18.150</theMrnAuth>\r\n" + 
				"    <theEcid></theEcid>\r\n" + 
				"    <theOhipForValidation>8924599452</theOhipForValidation>\r\n" + 
				"    <theOhipVcForValidation></theOhipVcForValidation>\r\n" + 
				"    <returnMrnHistory>false</returnMrnHistory>\r\n" + 
				"  </tns:getLinkedPatients>\r\n" + 
				"</SOAP-ENV:Body>\r\n" + 
				"</SOAP-ENV:Envelope>\r\n" + 
				"";
		
		String respString = "Date: Mon, 09 Dec 2013 18:14:58 GMT\r\n" + 
				"Transfer-Encoding: chunked\r\n" + 
				"server: grizzly/1.9.50\r\n" + 
				"Content-Type: text/xml;charset=utf-8\r\n" + 
				"\r\n" + 
				"<?xml version='1.0' encoding='UTF-8'?><S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\"><S:Body><ns2:getLinkedPatientsResponse xmlns:ns2=\"soap.ws.pls.uhn.ca\"><return/></ns2:getLinkedPatientsResponse></S:Body></S:Envelope>";
		//@formatter:on

		PersServiceVersionRecentMessage msg = new PersServiceVersionRecentMessage();
		msg.setRequestBody(reqString, new PersConfig());
		msg.setResponseBody(respString, new PersConfig());
		msg.setPidForUnitTest(123L);
		
		GRecentMessage dto = msg.toDto(true);
		
		Assert.assertEquals("POST /cGTA/CR/2.1.1 HTTP/1.1", dto.getRequestActionLine());
		
	}

	
	
}
