package net.svcret.admin.server.rpc;

import java.io.Serializable;
import java.util.Collection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import net.svcret.admin.api.AdminServiceProvider;
import net.svcret.admin.api.IAdminServiceLocal;
import net.svcret.admin.api.ProcessingException;
import net.svcret.admin.api.UnexpectedFailureException;
import net.svcret.admin.client.rpc.HttpClientConfigService;
import net.svcret.admin.shared.ServiceFailureException;
import net.svcret.admin.shared.model.DtoHttpClientConfig;
import net.svcret.admin.shared.model.DtoKeystoreAnalysis;
import net.svcret.admin.shared.model.DtoKeystoreToSave;
import net.svcret.admin.shared.model.DtoStickySessionUrlBinding;
import net.svcret.admin.shared.model.GHttpClientConfigList;
import net.svcret.admin.shared.util.KeystoreUtils;
import net.svcret.admin.shared.util.Validate;

public class HttpClientConfigServiceImpl extends BaseRpcServlet implements HttpClientConfigService {

	public static final String SESSVAR_PREFIX_KEYSTORE = HttpClientConfigServiceImpl.class.getSimpleName() + "_KEYSTORE_";
	public static final String SESSVAR_PREFIX_TRUSTSTORE = HttpClientConfigServiceImpl.class.getSimpleName() + "_TRUSTSTORE_";
	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(HttpClientConfigServiceImpl.class);
	private static final long serialVersionUID = 1L;

	private IAdminServiceLocal myAdminSvc;

	@Override
	public void init() throws ServletException {
		super.init();
		
		myAdminSvc = AdminServiceProvider.getInstance().getAdminService();
	}


	@Override
	public GHttpClientConfigList deleteHttpClientConfig(long thePid) throws ServiceFailureException {
		if (thePid <= 0) {
			throw new IllegalArgumentException("Invalid PID: " + thePid);
		}

		if (isMockMode()) {
			return getMock().deleteHttpClientConfig(thePid);
		}

		try {
			return myAdminSvc.deleteHttpClientConfig(thePid);
		} catch (ProcessingException e) {
			ourLog.error("Failed to save config", e);
			throw new ServiceFailureException(e.getMessage());
		} catch (UnexpectedFailureException e) {
			ourLog.error("Failed to save config", e);
			throw new ServiceFailureException(e.getMessage());
		}
	}

	@Override
	public DtoHttpClientConfig saveHttpClientConfig(boolean theCreate, boolean theUseNewTruststore, boolean theUseNewKeystore, DtoHttpClientConfig theConfig) throws ServiceFailureException {
		Validate.notNull(theConfig, "HttpClientConfig");

		if (theCreate) {
			ourLog.info("Saving new HTTP client config");
		} else {
			ourLog.info("Saving HTTP client config ID[{}]", theConfig.getPid());
		}

		byte[] newKeystore=null;
		String newKeystorePass=null;
		ourLog.info("New keystore:{} New truststore:{}", theUseNewKeystore, theUseNewTruststore);
		if (theUseNewKeystore) {
			SessionUploadedKeystore ks = getTransientKeyStore(theConfig.getPid(), false);
			if (ks == null) {
				throw new ServiceFailureException("No keystore in session");
			}
			newKeystore = ks.getKeystore();
			newKeystorePass = ks.getPassword();
		}

		byte[] newTruststore=null;
		String newTruststorePass=null;
		if (theUseNewTruststore) {
			SessionUploadedKeystore ts = getTransientTrustStore(theConfig.getPid(), false);
			if (ts == null) {
				throw new ServiceFailureException("No truststore in session");
			}
			newTruststore = ts.getKeystore();
			newTruststorePass = ts.getPassword();
		}

		if (isMockMode()) {
			return getMock().saveHttpClientConfig(theCreate, theUseNewTruststore, theUseNewKeystore, theConfig);
		}

		if (theCreate) {
			theConfig.setPid(0);
		}

		try {
			return myAdminSvc.saveHttpClientConfig(theConfig, newTruststore, newTruststorePass, newKeystore, newKeystorePass);
		} catch (ProcessingException e) {
			ourLog.error("Failed to save config", e);
			throw new ServiceFailureException(e.getMessage());
		} catch (UnexpectedFailureException e) {
			ourLog.error("Failed to save config", e);
			throw new ServiceFailureException(e.getMessage());
		}
	}

	public static class SessionUploadedKeystore implements Serializable {
		private static final long serialVersionUID = 1L;

		private byte[] myKeystore;
		private String myPassword;

		public byte[] getKeystore() {
			return myKeystore;
		}

		public String getPassword() {
			return myPassword;
		}

		public void setKeystore(byte[] theKeystore) {
			myKeystore = theKeystore;
		}

		public void setPassword(String thePassword) {
			myPassword = thePassword;
		}
	}

	@Override
	public DtoKeystoreAnalysis analyzeTransientTrustStore(long theHttpClientConfig) throws ServiceFailureException {
		SessionUploadedKeystore ks = getTransientTrustStore(theHttpClientConfig, false);
		return analyzeStore(ks, theHttpClientConfig);
	}

	private SessionUploadedKeystore getTransientTrustStore(long theHttpClientConfig, boolean theRemoveAfter) {
		String sessVar = SESSVAR_PREFIX_TRUSTSTORE + theHttpClientConfig;
		ourLog.info("Loading session variable: {}", sessVar);
		HttpServletRequest request = getThreadLocalRequest();
		SessionUploadedKeystore ks = (SessionUploadedKeystore) request.getSession(true).getAttribute(sessVar);
		if (theRemoveAfter) {
			request.getSession().removeAttribute(sessVar);
		}
		return ks;
	}

	@Override
	public DtoKeystoreAnalysis analyzeTransientKeyStore(long theHttpClientConfig) throws ServiceFailureException {
		SessionUploadedKeystore ks = getTransientKeyStore(theHttpClientConfig, false);
		return analyzeStore(ks, theHttpClientConfig);
	}

	private SessionUploadedKeystore getTransientKeyStore(long theHttpClientConfig, boolean theRemoveAfter) {
		String sessVar = SESSVAR_PREFIX_KEYSTORE + theHttpClientConfig;
		ourLog.info("Loading session variable: {}", sessVar);
		HttpServletRequest request = getThreadLocalRequest();
		SessionUploadedKeystore ks = (SessionUploadedKeystore) request.getSession(true).getAttribute(sessVar);
		if (theRemoveAfter) {
			request.getSession().removeAttribute(sessVar);
		}
		return ks;
	}

	private DtoKeystoreAnalysis analyzeStore(SessionUploadedKeystore theKs, long theHttpClientConfig) throws ServiceFailureException {
		if (theKs == null) {
			throw new ServiceFailureException("Could not find entry in memory for PID: " + theHttpClientConfig);
		}

		if (isMockMode()) {
			return getMock().analyzeKeyStore(theKs);
		}

		DtoKeystoreToSave request = new DtoKeystoreToSave();
		request.setKeystore(theKs.getKeystore());
		request.setPassword(theKs.getPassword());

		try {
			return KeystoreUtils.analyzeKeystore(request);
		} catch (ProcessingException e) {
			ourLog.error("Failed to examine keystore", e);
			throw new ServiceFailureException(e.getMessage());
		}
	}

	@Override
	public Collection<DtoStickySessionUrlBinding> getAllStickySessions() {
		if (isMockMode()) {
			return getMock().getAllStickySessions();
		}
		
		return myAdminSvc.getAllStickySessions();
	}

}
