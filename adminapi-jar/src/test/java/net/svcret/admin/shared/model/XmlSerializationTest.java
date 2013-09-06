package net.svcret.admin.shared.model;

import java.io.StringWriter;

import javax.xml.bind.JAXB;

import org.junit.Test;

public class XmlSerializationTest {

	@Test
	public void testMarshall() {
		
		GDomain domain = new GDomain();
		domain.setPid(123L);
		domain.setName("domainName");
		domain.setId("domainId");

		
		
		StringWriter w = new StringWriter();
		JAXB.marshal(domain, w);
		ourLog.info(w.toString());
	}
	
	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(XmlSerializationTest.class);
}
