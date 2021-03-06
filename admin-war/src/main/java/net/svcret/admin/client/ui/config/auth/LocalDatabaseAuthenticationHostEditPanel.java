package net.svcret.admin.client.ui.config.auth;

import static net.svcret.admin.client.AdminPortal.MSGS;
import net.svcret.admin.client.AdminPortal;
import net.svcret.admin.shared.model.DtoAuthenticationHostLocalDatabase;

public class LocalDatabaseAuthenticationHostEditPanel extends BaseAuthenticationHostEditPanel<DtoAuthenticationHostLocalDatabase> {


	public LocalDatabaseAuthenticationHostEditPanel(AuthenticationHostsPanel thePanel, DtoAuthenticationHostLocalDatabase theAuthHost) {
		super(thePanel, theAuthHost);
	}

	@Override
	protected String getPanelDescription() {
		return AdminPortal.MSGS.localDatabaseAuthenticationHostEditPanel_description();
	}

	@Override
	protected String getPanelTitle() {
		return MSGS.localDatabaseAuthenticationHostEditPanel_title();
	}

	@Override
	protected void applySettingsFromUi() {
		// No extra settings here
	}



}
