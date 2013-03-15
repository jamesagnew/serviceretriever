package ca.uhn.sail.proxy.util;

import static ca.uhn.sail.proxy.util.UrlUtil.*;
import static org.junit.Assert.*;

import java.net.URISyntaxException;

import org.junit.Test;

public class UrlUtilTest {

	@Test
	public void testCalculateRelativeUrl() throws URISyntaxException {
		assertEquals("http://host/path/bar.xsd", calculateRelativeUrl("http://host/path/foo.wsdl", "bar.xsd"));
		assertEquals("http://host/bar.xsd", calculateRelativeUrl("http://host/path/foo.wsdl", "/bar.xsd"));
		assertEquals("http://host2/bar.xsd", calculateRelativeUrl("http://host/path/foo.wsdl", "http://host2/bar.xsd"));
	}
	
}
