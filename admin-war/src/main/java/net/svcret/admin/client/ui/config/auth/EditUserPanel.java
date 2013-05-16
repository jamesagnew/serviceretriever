package net.svcret.admin.client.ui.config.auth;

import static net.svcret.admin.client.AdminPortal.*;
import net.svcret.admin.client.AdminPortal;
import net.svcret.admin.client.rpc.ModelUpdateService.UserAndAuthHost;
import net.svcret.admin.shared.Model;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class EditUserPanel extends BaseUserPanel {

	public EditUserPanel(long thePid) {
		super();

		AdminPortal.MODEL_SVC.loadUser(thePid, true, new AsyncCallback<UserAndAuthHost>() {
			@Override
			public void onFailure(Throwable theCaught) {
				Model.handleFailure(theCaught);
			}

			@Override
			public void onSuccess(UserAndAuthHost theResult) {
				initContents();
				setUser(theResult.getUser(), theResult.getAuthHost());
			}
		});

	}

	@Override
	protected String getPanelTitle() {
		return MSGS.editUser_Title();
	}

}
