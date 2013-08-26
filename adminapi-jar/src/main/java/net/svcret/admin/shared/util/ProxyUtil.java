package net.svcret.admin.shared.util;

public class ProxyUtil {

	public static String createDefaultPath(String domainId, String serviceId, String versionId) {
		return "/" + domainId + "/" + serviceId + "/" + versionId;
	}

}
