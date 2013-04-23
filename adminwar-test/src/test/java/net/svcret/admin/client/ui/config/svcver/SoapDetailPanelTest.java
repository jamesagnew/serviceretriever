package net.svcret.admin.client.ui.config.svcver;

import static org.mockito.Mockito.*;
import net.svcret.admin.shared.model.GSoap11ServiceVersion;

import org.junit.Test;


public class SoapDetailPanelTest {

	@Test
	public void testModifyService() {
		
		AddServiceVersionPanel parent = mock(AddServiceVersionPanel.class);
		GSoap11ServiceVersion svcVer= mock(GSoap11ServiceVersion.class);
		SoapDetailPanel panel = new SoapDetailPanel(parent, svcVer);
		
	}
	

}
