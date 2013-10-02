package net.svcret.ejb.invoker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.svcret.ejb.model.entity.BasePersServiceVersion;
import net.svcret.ejb.model.entity.PersBaseServerAuth;

public abstract class BaseServiceInvoker implements IServiceInvoker {

	private static final String COOKIE = "Cookie";

	@Override
	public List<PersBaseServerAuth<?, ?>> provideServerAuthorizationModules(BasePersServiceVersion theServiceVersion) {
		return theServiceVersion.getServerAuths();
	}

	@Override
	public Map<String, List<String>> createBackingRequestHeadersForMethodInvocation(BasePersServiceVersion theServiceVersion, Map<String, List<String>> theIncomingHeaders) {
		Map<String, List<String>> retVal = new HashMap<String, List<String>>();
		
		if (theIncomingHeaders.containsKey(COOKIE)) {
			retVal.put(COOKIE, theIncomingHeaders.get(COOKIE));
		}
		
		return retVal;
	}

}
