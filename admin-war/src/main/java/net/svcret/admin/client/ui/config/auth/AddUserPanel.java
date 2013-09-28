package net.svcret.admin.client.ui.config.auth;

import static net.svcret.admin.client.AdminPortal.*;
import net.svcret.admin.shared.IAsyncLoadCallback;
import net.svcret.admin.shared.Model;
import net.svcret.admin.shared.model.BaseDtoAuthenticationHost;
import net.svcret.admin.shared.model.DtoAuthenticationHostList;
import net.svcret.admin.shared.model.GUser;

public class AddUserPanel extends BaseUserPanel {

	public AddUserPanel(final long theAuthHostPid) {
		super();

		Model.getInstance().loadAuthenticationHosts(new IAsyncLoadCallback<DtoAuthenticationHostList>() {
			@Override
			public void onSuccess(DtoAuthenticationHostList theResult) {
				BaseDtoAuthenticationHost authHost = theResult.getAuthHostByPid(theAuthHostPid);
				initContents();
				GUser user = new GUser();
				user.setAuthHostPid(theAuthHostPid);
				setUser(user, authHost);
			}
		});

	}


	@Override
	protected String getPanelTitle() {
		return MSGS.editUser_Title();
	}

}
