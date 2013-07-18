package net.svcret.admin.shared.util;

import org.junit.Assert;
import org.junit.Test;

public class StringUtilTest {

	@Test
	public void testHtml() {
		
		String actual = StringUtil.convertPlaintextToHtml("hello http://foo goodbye");
		Assert.assertEquals("hello <a href=\"http://foo\">http://foo</a> goodbye", actual);
		
		actual = StringUtil.convertPlaintextToHtml("http://foo goodbye");
		Assert.assertEquals("<a href=\"http://foo\">http://foo</a> goodbye", actual);
		
		actual = StringUtil.convertPlaintextToHtml("hello http://foo");
		Assert.assertEquals("hello <a href=\"http://foo\">http://foo</a>", actual);

		actual = StringUtil.convertPlaintextToHtml("hello\nhttp://foo\n");
		Assert.assertEquals("hello<br/><a href=\"http://foo\">http://foo</a><br/>", actual);
		
	}
	
	
}
