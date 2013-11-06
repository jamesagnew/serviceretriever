package net.svcret.admin.client.rpc;

import java.util.Collection;

import net.svcret.admin.shared.ServiceFailureException;
import net.svcret.admin.shared.model.DtoKeystoreAnalysis;
import net.svcret.admin.shared.model.DtoStickySessionUrlBinding;
import net.svcret.admin.shared.model.GHttpClientConfig;
import net.svcret.admin.shared.model.GHttpClientConfigList;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("httpclientconfig")
public interface HttpClientConfigService extends RemoteService {

	GHttpClientConfigList deleteHttpClientConfig(long thePid) throws ServiceFailureException;

	DtoKeystoreAnalysis analyzeTransientTrustStore(long theHttpClientConfig) throws ServiceFailureException;

	DtoKeystoreAnalysis analyzeTransientKeyStore(long theHttpClientConfig) throws ServiceFailureException;

	Collection<DtoStickySessionUrlBinding> getAllStickySessions();

	GHttpClientConfig saveHttpClientConfig(boolean theCreate,
			boolean theUseNewTruststore, boolean theUseNewKeystore,
			GHttpClientConfig theConfig) throws ServiceFailureException;
	
}
