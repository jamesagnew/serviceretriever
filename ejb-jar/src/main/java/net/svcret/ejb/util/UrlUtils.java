package net.svcret.ejb.util;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;

public class UrlUtils {

	public static boolean isLocalUrl(String theUrl) throws MalformedURLException, UnknownHostException {
		URL url = toUrl(theUrl);
		InetAddress addr = toAddress(url);
		return isLocal(addr);
	}

	public static InetAddress toAddress(URL url) throws UnknownHostException {
		InetAddress addr = InetAddress.getByName(url.getHost());
		return addr;
	}

	public static URL toUrl(String theUrl) throws MalformedURLException {
		URL url = new URL(theUrl);
		return url;
	}
	
	public static boolean isLocal(InetAddress addr) {
	    // Check if the address is a valid special local or loop back
	    if (addr.isAnyLocalAddress() || addr.isLoopbackAddress())
	        return true;

	    // Check if the address is defined on any interface
	    try {
	        return NetworkInterface.getByInetAddress(addr) != null;
	    } catch (SocketException e) {
	        return false;
	    }
	}

}
