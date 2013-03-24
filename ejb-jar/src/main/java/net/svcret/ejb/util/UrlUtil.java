package net.svcret.ejb.util;

import java.net.URI;
import java.net.URISyntaxException;

public class UrlUtil {

	public static String calculateRelativeUrl(String theParent, String theChild) throws URISyntaxException {
		URI uri = new URI(theParent);
		
		URI child = new URI(theChild);
//		if (child.getScheme() != null) {
//			return child.toString();
//		}
//		
//		if (child.getPath().startsWith("/")) {
//			String path = uri.getPath();
//			int lastSlash = path.lastIndexOf('/');
//			if (lastSlash > -1) {
//				path = path.substring(0, lastSlash) + '/';
//			}
//			uri.
//		}
		
		if (child.getAuthority() == null && child.getHost() == null) {
			String path;

			String scheme = uri.getScheme();
			String userInfo = uri.getUserInfo();
			String host = uri.getHost();
			int port = uri.getPort();
			
			if (child.getPath().startsWith("/")) {
				path = child.getPath();
			} else {
				path = uri.getPath();
				int lastSlash = path.lastIndexOf('/');
				if (lastSlash > -1) {
					path = path.substring(0, lastSlash) + '/';
				} else {
					path = "/";
				}
				path = path + child.getPath();
			}
			
			String query = child.getQuery();
			String fragment = child.getFragment();
			child = new URI(scheme, userInfo, host, port, path, query, fragment);
		}
		
		return child.toString();
		
	}
	
}
