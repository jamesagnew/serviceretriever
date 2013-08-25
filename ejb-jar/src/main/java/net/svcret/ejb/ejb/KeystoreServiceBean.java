package net.svcret.ejb.ejb;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;

import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Singleton;

import net.svcret.admin.shared.model.DtoKeystoreAnalysis;
import net.svcret.admin.shared.model.DtoKeystoreToSave;
import net.svcret.ejb.api.IKeystoreService;
import net.svcret.ejb.ex.ProcessingException;

@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class KeystoreServiceBean implements IKeystoreService {

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(KeystoreServiceBean.class);

	@Override
	public DtoKeystoreAnalysis analyzeKeystore(byte[] bytes, String password) throws ProcessingException {
		DtoKeystoreAnalysis retVal = new DtoKeystoreAnalysis();
		retVal.setPassword(password);

		KeyStore newKeystore;
		try {
			newKeystore = KeyStore.getInstance("jks");
		} catch (KeyStoreException e) {
			throw new ProcessingException(e);
		}

		try {
			newKeystore.load(new ByteArrayInputStream(bytes), password.toCharArray());
		} catch (IOException e) {
			ourLog.error("Failed to work with keystore", e);
			retVal.setPasswordAccepted(false);
			retVal.setProblemDescription(e.toString());
			return retVal;
		} catch (Exception e) {
			ourLog.error("Failed to work with keystore", e);
			retVal.setPasswordAccepted(false);
			retVal.setProblemDescription(e.toString());
			return retVal;
		}

		retVal.setPasswordAccepted(true);
		ArrayList<String> aliases;
		try {
			aliases = Collections.list(newKeystore.aliases());
		} catch (KeyStoreException e) {
			ourLog.error("Failed to work with keystore", e);
			retVal.setPasswordAccepted(false);
			retVal.setProblemDescription(e.toString());
			return retVal;
		}

		Collections.sort(aliases);
		retVal.getKeyAliases().addAll(aliases);

		for (String next : aliases) {
			try {
				X509Certificate cert = (X509Certificate) newKeystore.getCertificate(next);
				retVal.getExpiryDate().put(next, cert.getNotAfter());
				retVal.getSubject().put(next, cert.getSubjectDN().toString());
				retVal.getIssuer().put(next, cert.getIssuerDN().toString());
				retVal.getKeyEntry().put(next, newKeystore.isKeyEntry(next));
			} catch (Exception e) {
				ourLog.error("Failed to work with keystore", e);
			}

		}

		return retVal;
	}

	@Override
	public DtoKeystoreAnalysis analyzeKeystore(DtoKeystoreToSave theRequest) throws ProcessingException {

		byte[] bytes = theRequest.getKeystore();
		String password = theRequest.getPassword();

		return analyzeKeystore(bytes, password);
	}

	@Override
	public KeyStore loadKeystore(byte[] theKeystore, String theKeystorePassword) throws ProcessingException {
		KeyStore newKeystore;
		try {
			newKeystore = KeyStore.getInstance("jks");
		} catch (KeyStoreException e) {
			throw new ProcessingException(e);
		}

		try {
			newKeystore.load(new ByteArrayInputStream(theKeystore), theKeystorePassword.toCharArray());
		} catch (Exception e) {
			throw new ProcessingException(e);
		}
		
		return newKeystore;
	}
}
