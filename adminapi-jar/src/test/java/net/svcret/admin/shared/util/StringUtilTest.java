package net.svcret.admin.shared.util;

import org.junit.Test;

public class StringUtilTest {

	@Test
	public void testHtml() {
		
		System.out.println(StringUtil.convertPlaintextToHtml("hello http://foo goodbye"));
		
	}
	
	
}
