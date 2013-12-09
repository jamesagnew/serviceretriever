package net.svcret.admin.client.rpc;

import java.util.Collection;

import net.svcret.admin.shared.model.DtoKeystoreAnalysis;
import net.svcret.admin.shared.model.DtoStickySessionUrlBinding;
import net.svcret.admin.shared.model.DtoHttpClientConfig;
import net.svcret.admin.shared.model.GHttpClientConfigList;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface HttpClientConfigServiceAsync {

	void deleteHttpClientConfig(long thePid,
			AsyncCallback<GHttpClientConfigList> theCallback);

	void saveHttpClientConfig(boolean theCreate, boolean theUseNewTruststore,
			boolean theUseNewKeystore, DtoHttpClientConfig theConfig,
			AsyncCallback<DtoHttpClientConfig> theAsyncCallback);

	void analyzeTransientTrustStore(long theHttpClientConfig,
			AsyncCallback<DtoKeystoreAnalysis> callback);

	void analyzeTransientKeyStore(long theHttpClientConfig,
			AsyncCallback<DtoKeystoreAnalysis> callback);

	void getAllStickySessions(
			AsyncCallback<Collection<DtoStickySessionUrlBinding>> theAsyncCallback);

}
