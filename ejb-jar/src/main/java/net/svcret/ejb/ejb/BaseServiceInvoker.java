package net.svcret.ejb.ejb;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.svcret.ejb.api.IServiceInvoker;

public abstract class BaseServiceInvoker implements IServiceInvoker {

	private static final String COOKIE = "Cookie";

	@Override
	public Map<String, List<String>> createBackingRequestHeadersForMethodInvocation(Map<String, List<String>> theIncomingHeaders) {
		Map<String, List<String>> retVal = new HashMap<String, List<String>>();
		
		if (theIncomingHeaders.containsKey(COOKIE)) {
			retVal.put(COOKIE, theIncomingHeaders.get(COOKIE));
		}
		
		return retVal;
	}

}
