package net.svcret.ejb.api;

import java.security.KeyStore;

import javax.ejb.Local;

import net.svcret.admin.shared.model.DtoKeystoreAnalysis;
import net.svcret.admin.shared.model.DtoKeystoreToSave;
import net.svcret.ejb.ex.ProcessingException;

@Local
public interface IKeystoreService {

	DtoKeystoreAnalysis analyzeKeystore(DtoKeystoreToSave theRequest) throws ProcessingException;

	DtoKeystoreAnalysis analyzeKeystore(byte[] theKeystore, String theKeystorePassword) throws ProcessingException;

	KeyStore loadKeystore(byte[] theKeystore, String theKeystorePassword) throws ProcessingException;

}
