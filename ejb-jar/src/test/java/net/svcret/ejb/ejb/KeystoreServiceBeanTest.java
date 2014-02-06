package net.svcret.ejb.ejb;

import static org.junit.Assert.*;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import net.svcret.admin.api.ProcessingException;
import net.svcret.admin.shared.model.DtoKeystoreAnalysis;
import net.svcret.admin.shared.model.DtoKeystoreToSave;
import net.svcret.admin.shared.util.KeystoreUtils;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

public class KeystoreServiceBeanTest {

	private static final SimpleDateFormat DTFMT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(KeystoreServiceBeanTest.class);

	private KeystoreUtils mySvc;

	@Before
	public void extracted() {
		mySvc = new KeystoreUtils();
	}

	@Test
	public void testExamineKeystore() throws IOException, ProcessingException, ParseException {

		byte[] bytes = IOUtils.toByteArray(KeystoreUtils.class.getResourceAsStream("/keystore/keystore.jks"));

		DtoKeystoreToSave request = new DtoKeystoreToSave();
		request.setKeystore(bytes);
		request.setPassword("changeit");

		DtoKeystoreAnalysis analysis = mySvc.analyzeKeystore(request);

		ourLog.info("Aliases: {}", analysis.getKeyAliases());
		assertEquals(DTFMT.parse("2012-10-21 22:40:45"), analysis.getExpiryDate().get("testcert"));
		assertEquals("CN=James, OU=TestOU, O=TestOrg, L=Toronto, ST=ON, C=CA", analysis.getIssuer().get("testcert"));
		assertEquals("CN=James, OU=TestOU, O=TestOrg, L=Toronto, ST=ON, C=CA", analysis.getSubject().get("testcert"));
		assertEquals(Boolean.TRUE, analysis.getKeyEntry().get("testcert"));
		
	}

	@Test
	public void testExamineKeystoreBadPassword() throws Exception {

		byte[] bytes = IOUtils.toByteArray(KeystoreUtils.class.getResourceAsStream("/keystore/keystore.jks"));

		DtoKeystoreToSave request = new DtoKeystoreToSave();
		request.setKeystore(bytes);
		request.setPassword("ZZZ");

		DtoKeystoreAnalysis analysis = mySvc.analyzeKeystore(request);

		assertEquals(false, analysis.isPasswordAccepted());
	}

	@Test
	public void testExamineKeystoreBadFile() throws Exception {

		byte[] bytes = IOUtils.toByteArray(KeystoreUtils.class.getResourceAsStream("/logback-test.xml"));

		DtoKeystoreToSave request = new DtoKeystoreToSave();
		request.setKeystore(bytes);
		request.setPassword("ZZZ");

		DtoKeystoreAnalysis analysis = mySvc.analyzeKeystore(request);

		assertEquals(false, analysis.isPasswordAccepted());
	}

	@Test
	public void testExamineTruststore() throws IOException, ProcessingException, ParseException {

		byte[] bytes = IOUtils.toByteArray(KeystoreUtils.class.getResourceAsStream("/keystore/truststore.jks"));

		DtoKeystoreToSave request = new DtoKeystoreToSave();
		request.setKeystore(bytes);
		request.setPassword("changeit");

		DtoKeystoreAnalysis analysis = mySvc.analyzeKeystore(request);

		ourLog.info("Aliases: {}", analysis.getKeyAliases());
		assertEquals(DTFMT.parse("2012-10-21 22:40:45"), analysis.getExpiryDate().get("testcert"));
		assertEquals("CN=James, OU=TestOU, O=TestOrg, L=Toronto, ST=ON, C=CA", analysis.getIssuer().get("testcert"));
		assertEquals("CN=James, OU=TestOU, O=TestOrg, L=Toronto, ST=ON, C=CA", analysis.getSubject().get("testcert"));
		assertEquals(Boolean.FALSE, analysis.getKeyEntry().get("testcert"));
		
	}

}




