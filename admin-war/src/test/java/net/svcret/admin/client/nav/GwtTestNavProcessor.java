package net.svcret.admin.client.nav;

import static org.junit.Assert.*;

import org.junit.Test;

import com.google.gwt.junit.client.GWTTestCase;

public class GwtTestNavProcessor extends GWTTestCase {

	@Test
	public void testStripDupes() {
		
		NavProcessor.setCurrentTokenForUnitTest("MRL__EMR_1355234");
		String actual = NavProcessor.getTokenViewActiveCheckOutcomes(1, 2, 3);
		assertEquals("MRL__EMR_1355234__VAC_1_2_3", actual);
		
	}

	@Override
	public String getModuleName() {
		return "net.svcret.admin.AdminPortal";
	}

}
