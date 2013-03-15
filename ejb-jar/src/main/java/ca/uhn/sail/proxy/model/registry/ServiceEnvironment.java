package ca.uhn.sail.proxy.model.registry;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public enum ServiceEnvironment {

	PROD("Prod"),
	
	DEV("Dev");
	
	private String myUrlPath;
	private static Map<String, ServiceEnvironment> ourUrlPaths;

	private ServiceEnvironment(String theUrlPath) {
		myUrlPath = theUrlPath;
	}
	
	public static ServiceEnvironment getForUrlPath(String theUrlPath) {
		if (ourUrlPaths == null) {
			Map<String, ServiceEnvironment> urlPath = new HashMap<String, ServiceEnvironment>();
			for (ServiceEnvironment next : ServiceEnvironment.values()) {
				urlPath.put(next.myUrlPath, next);
			}
			ourUrlPaths = Collections.unmodifiableMap(urlPath);
		}
		return ourUrlPaths.get(theUrlPath);
	}
	
	
}
