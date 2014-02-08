package net.svcret.core.model.entity.http;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.svcret.core.model.entity.http.PersHttpBasicCredentialGrabber;

import org.junit.Assert;
import org.junit.Test;

public class PersHttpBasicCredentialGrabberTest {

	@Test
	public void testGrabber() {

		Map<String, List<String>> headers = new HashMap<String, List<String>>();
		headers.put("authorization", new ArrayList<String>());
		headers.get("authorization").add("Basic dXNlcjpwYXNz");

		PersHttpBasicCredentialGrabber grabber = new PersHttpBasicCredentialGrabber(headers);
		Assert.assertEquals("user", grabber.getUsername());
		Assert.assertEquals("user", grabber.getUsername());

	}

}
