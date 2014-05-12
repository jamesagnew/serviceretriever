package net.svcret.admin.client.nav;

import net.svcret.admin.client.ui.config.monitor.EditMonitorRulePanel;
import net.svcret.admin.client.ui.config.monitor.MonitorRulesPanel;
import net.svcret.admin.client.ui.config.monitor.ViewActiveCheckOutcomePanel;
import net.svcret.admin.client.ui.dash.ServiceDashboardPanel;
import net.svcret.admin.client.ui.layout.BodyPanel;
import net.svcret.admin.client.ui.layout.BreadcrumbPanel;
import net.svcret.admin.client.ui.layout.OuterLayoutPanel;

import org.junit.Test;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.user.client.History;

public class GwtTestNavProcessor extends GWTTestCase {

	@Test
	public void testStripDupes() {
		
		NavProcessor.setCurrentTokenForUnitTest("MRL__EMR_1355234");
		String actual = NavProcessor.getTokenViewActiveCheckOutcomes(1, 2, 3);
		assertEquals("MRL__EMR_1355234__VAC_1_2_3", actual);
		
	}

	@Test
	public void testNavForwardAndBack() {
		String historyToken = History.getToken();
		GWT.log("Initial token is: "+ historyToken);
		
		new BreadcrumbPanel();
		new BodyPanel();
		NavProcessor.goHome();

		historyToken = History.getToken();
		GWT.log("Token is: "+ historyToken);

		History.newItem("DSH");

		historyToken = History.getToken();
		GWT.log("Token is: "+ historyToken);
		assertEquals("DSH", historyToken);
		assertTrue(BodyPanel.getInstance().getContents().getClass().toString(), BodyPanel.getInstance().getContents() instanceof ServiceDashboardPanel);

		// Rule List 
		
		History.newItem("MRL");

		historyToken = History.getToken();
		GWT.log("Token is: "+ historyToken);
		assertEquals("MRL", historyToken);
		assertTrue(BodyPanel.getInstance().getContents().getClass().toString(), BodyPanel.getInstance().getContents() instanceof MonitorRulesPanel);

		// Edit rule
		
		History.newItem(NavProcessor.getTokenEditMonitorRule(200));

		historyToken = History.getToken();
		GWT.log("Token is: "+ historyToken);
		assertEquals("MRL__EMR_200", historyToken);
		assertTrue(BodyPanel.getInstance().getContents().getClass().toString(), BodyPanel.getInstance().getContents() instanceof EditMonitorRulePanel);

		// View history
		
		History.newItem(NavProcessor.getTokenViewActiveCheckOutcomes(200, 111, 222));

		historyToken = History.getToken();
		GWT.log("Token is: "+ historyToken);
		assertEquals("MRL__EMR_200__VAC_200_111_222", historyToken);
		assertTrue(BodyPanel.getInstance().getContents().getClass().toString(), BodyPanel.getInstance().getContents() instanceof ViewActiveCheckOutcomePanel);

	}

	
	@Override
	public String getModuleName() {
		return "net.svcret.admin.AdminPortal";
	}

}
