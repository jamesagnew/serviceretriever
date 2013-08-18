package net.svcret.admin.client.ui.test;


public class ServiceVersionTestPanel extends BaseServiceVersionTestPanel {

	public ServiceVersionTestPanel() {
		super(null, null);
		initAllPanels();
	}

	public ServiceVersionTestPanel(Long theServiceVersionPid) {
		super(theServiceVersionPid, null);
		initAllPanels();
	}

}
