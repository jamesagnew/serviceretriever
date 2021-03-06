package net.svcret.core.invoker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.svcret.admin.api.ProcessingException;
import net.svcret.core.model.entity.BasePersServiceVersion;
import net.svcret.core.model.entity.PersBaseServerAuth;
import net.svcret.core.model.entity.PersHttpClientConfig;

public abstract class BaseServiceInvoker implements IServiceInvoker {

	private static final String COOKIE = "Cookie";

	@Override
	public List<PersBaseServerAuth<?, ?>> provideServerAuthorizationModules(BasePersServiceVersion theServiceVersion) {
		return theServiceVersion.getServerAuths();
	}

	@SuppressWarnings("unused")
	@Override
	public BasePersServiceVersion introspectServiceFromUrl(PersHttpClientConfig theHttpConfig, String theUrl) throws ProcessingException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Map<String, List<String>> createBackingRequestHeadersForMethodInvocation(BasePersServiceVersion theServiceVersion, Map<String, List<String>> theIncomingHeaders) {
		Map<String, List<String>> retVal = new HashMap<>();
		
		if (theIncomingHeaders.containsKey(COOKIE)) {
			retVal.put(COOKIE, theIncomingHeaders.get(COOKIE));
		}
		
		return retVal;
	}

}
